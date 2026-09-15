# ecomPGVectorDB

Run with Docker Compose:

```bash
docker compose up -d
```

The database is initialized with the schema in `init/01_product_vector_schema.sql`.

This creates the `vector` extension and a `product_vector` table with:
- `product_id` as the unique key
- `content_text` for the transformed product text
- `embedding` stored as a pgvector column
- `category_id` and `domain` for search filters
- `source_updated_at` for sync tracking
- a cosine distance HNSW index for similarity search

Manual Docker run:

```bash
docker run --name pgvector-db -e POSTGRES_PASSWORD=root123 -p 5432:5432 -d pgvector/pgvector:pg16
```
