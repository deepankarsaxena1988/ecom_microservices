# ecomPGVectorDB

Run with Docker Compose:

```bash
docker compose up -d
```

The database is initialized with the schema in `init/01_product_vector_schema.sql`.

This creates the `vector` extension and a `product_vector` table with:
- `product_id` as the unique key
- `content_text` for the transformed product text
- `embedding_1536` and `embedding_1024` pgvector columns, one per supported
  embedding dimension, so multiple embedding models (e.g. OpenAI
  `text-embedding-3-small` = 1536-d and Ollama `qwen3-embedding:0.6b` = 1024-d)
  can coexist. The app writes/reads whichever column matches the currently
  active embedding profile's dimension (see `ecomPGVectorSearch`'s
  `app.embedding.profiles.*.dimension`).
- `embedding_provider` / `embedding_model` recording which profile last wrote
  a row's vector(s)
- `category_id` and `domain` for search filters
- `source_updated_at` for sync tracking
- a cosine distance HNSW index per embedding column

If you already have a running container/volume created before this change,
apply `init/02_multi_dimension_embedding_migration.sql` manually (it will not
run automatically since `docker-entrypoint-initdb.d` scripts only run once,
against a fresh data directory):

```bash
psql -h localhost -p 5432 -U postgres -d postgres -f init/02_multi_dimension_embedding_migration.sql
```

Manual Docker run:

```bash
docker run --name pgvector-db -e POSTGRES_PASSWORD=root123 -p 5432:5432 -d pgvector/pgvector:pg16
```
