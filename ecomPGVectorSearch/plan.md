# Support multi-dimension embeddings (qwen3-embedding:0.6b = 1024-d, text-embedding-3-small = 1536-d) in ecomPGVectorSearch

## Status: Implemented ✅
All items below have been completed and the project compiles successfully
(`mvn -DskipTests compile`).

## Problem
`product_vector` had a single `embedding vector(1536)` column with an HNSW index
(`ecomPGVectorDB/init/01_product_vector_schema.sql`). pgvector requires every vector
in one indexed column to share the same dimension, so switching the active profile
to `qwen` (1024-d) via `EmbeddingRuntimeConfig`/`EmbeddingConfigController` would
have failed on insert (`different vector dimensions`) as soon as it wrote to the
existing column.

## Approach (user-selected)
- Add one vector column per dimension in `product_vector` (`embedding_1536`,
  `embedding_1024`), each with its own HNSW cosine index.
- Migrate existing `embedding` data in place into `embedding_1536` (no re-embedding
  needed for already-indexed OpenAI vectors), then drop the old `embedding` column
  and its index.
- Add a `dimension` property to each embedding profile in `application.yml`; route
  upsert/search dynamically to the correct column based on the active profile's
  dimension. Unsupported dimensions fail fast with a clear error (adding a new
  dimension later needs one new column + index + a map entry).
- Only the active profile's column is written/read per request, so both profiles
  can coexist and switching profiles doesn't require touching the other model's
  vectors. A full reindex per profile is still needed to populate a given
  dimension's column for the first time.

## Files changed

### DB schema (`ecomPGVectorDB`)
- `init/01_product_vector_schema.sql`: now defines `embedding_1536 vector(1536)` and
  `embedding_1024 vector(1024)` instead of a single `embedding` column; two HNSW
  cosine indexes (`idx_product_vector_embedding_1536_hnsw`,
  `idx_product_vector_embedding_1024_hnsw`). Fresh containers get this directly.
- New `init/02_multi_dimension_embedding_migration.sql` (idempotent, safe to run
  against an already-running DB where `docker-entrypoint-initdb.d` scripts won't
  re-run automatically):
  - `ADD COLUMN IF NOT EXISTS embedding_1536 vector(1536)`,
    `embedding_1024 vector(1024)`.
  - Folds in `embedding_provider`/`embedding_model` columns (previously only in
    `ecomPGVectorSearch/scripts/add-embedding-metadata-columns.sql`) via
    `ADD COLUMN IF NOT EXISTS` so this migration is self-contained.
  - `UPDATE product_vector SET embedding_1536 = embedding WHERE embedding IS NOT
    NULL AND embedding_1536 IS NULL` (copies existing OpenAI vectors).
  - Drops the old `idx_product_vector_embedding_hnsw` index and the `embedding`
    column.
  - Creates the two new HNSW indexes (`IF NOT EXISTS`).
- Updated `ecomPGVectorDB/README.md` to describe both columns and how to run the
  migration against an existing container.

### Java (`ecomPGVectorSearch`)
- `EmbeddingModelProperties.EmbeddingProfile`: added `int dimension` (+ getter/setter).
- `application.yml`: added `dimension: 1536` under `openai` profile and
  `dimension: 1024` under `qwen` profile.
- `EmbeddingRuntimeConfig`: added `getDimension()` resolving from the active profile.
- New `EmbeddingVectorColumnResolver` with a `Map<Integer,String>` of supported
  dimension → column name (1536 → `embedding_1536`, 1024 → `embedding_1024`);
  throws `IllegalStateException` with a clear message for an unsupported
  dimension so misconfiguration fails fast instead of silently corrupting data.
- `ProductVectorUpsertService`: builds the `INSERT ... ON CONFLICT` SQL dynamically
  using the resolved column name for the active profile's dimension; only that
  column is set on insert/update (other dimension's column, if present from a
  prior profile, is left untouched).
- `ProductVectorSearchService`: builds the `SELECT ... ORDER BY` SQL dynamically
  using the resolved column name and adds `WHERE <column> IS NOT NULL` so search
  only matches rows that have been indexed with the currently active model.
- No changes needed in `ProductReindexService` / `ProductDeltaSyncService` —
  they already just call `upsertService.upsert(...)`, which routes correctly.

## Notes / follow-ups (not in scope unless requested)
- `embedding_provider` / `embedding_model` remain single columns describing the
  *last* profile written for a row; if a row has both 1024 and 1536 vectors from
  two different reindex runs, those two text columns only reflect the most recent
  one. Splitting them per-dimension (e.g. `embedding_provider_1536/_1024`) is a
  possible future enhancement but wasn't requested.
- **Next step for deployment**: apply
  `ecomPGVectorDB/init/02_multi_dimension_embedding_migration.sql` against any
  already-running Postgres container/volume (fresh containers get the new
  schema automatically from `01_product_vector_schema.sql`).
- After deploying, each profile needs a `/api/products/reindex?profile=<key>` run
  once to populate its column for existing products before search will return
  results for that profile.
