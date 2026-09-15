# Plan: Load product data from relational DB into pgvector for vector search

## Objective
Synchronize product records from the existing MySQL relational database into a pgvector-backed table so that semantic similarity search can run over product metadata and descriptions.

## Current architecture
- Relational source: MySQL database used by the product catalog service.
- Product tables: `ecom_product`, `ecom_product_category`, and related review data.
- Target vector store: PostgreSQL + pgvector (`ecomPGVectorDB` docker-compose setup).
- Source of truth: product rows in MySQL; pgvector is a derived search index.

## Current progress
- Completed: product-to-text transformation utility was created in `ecomPGVectorSearch` and validated with unit tests.
- Next immediate step: design the PostgreSQL pgvector schema and sync job for loading product embeddings.
- Current working focus: schema creation, then bulk backfill from MySQL into pgvector, then search API integration.

## Planned phases

### 1) Define the embedding payload
Create a canonical text block for each product using fields such as:
- `product_name`
- `product_desc_heading`
- `product_desc_detail`
- category name
- domain
- optional review/comment summaries if needed

Rules:
- Keep text normalized and deduplicated.
- Exclude null/blank fields.
- Include only searchable and business-relevant product attributes.

### 2) Design the pgvector schema
Create a dedicated table in PostgreSQL, for example:
- `product_vector` or `product_embedding`
- columns:
  - `product_id` (bigint, FK to source product id or unique key)
  - `category_id` (optional metadata)
  - `embedding` (vector)
  - `content_text` (stored searchable text preview)
  - `source_updated_at` (timestamp)
  - `created_at`, `updated_at`

Important design decisions:
- Use a unique index on `product_id`.
- Use `vector` type from pgvector with the right dimensions matching the embedding model.
- Add a `HNSW` or `IVFFlat` index later if data volume becomes large.

Status: implemented in `ecomPGVectorDB/init/01_product_vector_schema.sql`.

### 3) Build the ETL/data sync job
Add a service/job that:
- queries the relational product table
- loads only required product rows
- normalizes the text payload
- calls the embedding model
- writes to pgvector using `INSERT ... ON CONFLICT ... DO UPDATE`

Recommended patterns:
- run as a batch job for initial load
- run as a scheduled sync for incremental updates
- support both full backfill and delta sync

### 4) Handle incremental updates
Keep pgvector current with MySQL changes by detecting:
- new products
- updated product metadata
- deleted products

Implementation options:
- trigger-based sync (if database supports it cleanly)
- polling-based sync from a timestamp/last_updated column
- CDC/event-driven ingestion if the platform evolves later

For this project, a scheduled polling job is the simplest and safest first version.

### 5) Implement full backfill and repair
Before production use, run a complete backfill for all existing products:
- read all rows from `ecom_product`
- generate embeddings
- bulk upsert into pgvector

Add a repair path for reindexing when:
- embedding model changes
- vector dimension changes
- data corruption occurs

### 6) Expose similarity search
Create a search API/service that accepts a natural-language user query and does:
- embed the query text
- query pgvector using cosine distance or inner product depending on model
- return product metadata plus similarity score
- optionally combine with relational filters for category/domain/price

Example search query patterns:
- nearest-neighbor search with a vector
- optional filter by category or domain
- return top N results

### 7) Operational safeguards
- Use environment variables for DB credentials and embedding model config.
- Log sync counts, failures, and latency.
- Add retry logic around embedding generation and DB insert failures.
- Validate vector dimensions before inserting rows.
- Preserve a `last_synced_at` checkpoint to avoid reprocessing unchanged rows.

## Minimal implementation plan
1. [x] Add a product-to-text transformation function.
2. [x] Add pgvector schema migration.
3. [x] Add a sync job to read all products from MySQL.
4. [x] Generate embeddings for each product.
5. [x] Upsert to pgvector using product id as key.
6. [x] Add incremental update logic.
7. [x] Add vector search endpoint/service.
8. [x] Add backfill and reindex support.
9. [ ] Validate with sample queries and benchmark results.

## Success criteria
- Product rows from the relational database are mirrored into pgvector.
- Querying pgvector returns relevant product matches by semantic similarity.
- Changes in product content are reflected in pgvector without manual intervention.
- Search latency is acceptable for the expected catalog size.

## Recommended next step
Start with a batch backfill job and a single `product_vector` table, then extend to scheduled delta sync and query API once the first end-to-end pipeline is validated.
