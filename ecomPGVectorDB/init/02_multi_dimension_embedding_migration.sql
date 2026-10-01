-- Migrates product_vector from a single fixed-dimension `embedding vector(1536)`
-- column to two dimension-specific columns so the app can support multiple
-- embedding models side by side (e.g. OpenAI text-embedding-3-small = 1536-d,
-- Ollama qwen3-embedding:0.6b = 1024-d). pgvector requires every vector stored
-- in (and indexed on) one column to share the same dimension, so each model
-- family gets its own column + HNSW index.
--
-- Safe to run multiple times. Only needed against an already-running database
-- whose data directory was initialized before this migration was added --
-- `init/` scripts under docker-entrypoint-initdb.d do not re-run on existing
-- volumes. Fresh containers already get this schema from
-- 01_product_vector_schema.sql.

CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE product_vector
    ADD COLUMN IF NOT EXISTS embedding_1536 vector(1536),
    ADD COLUMN IF NOT EXISTS embedding_1024 vector(1024),
    ADD COLUMN IF NOT EXISTS embedding_provider VARCHAR(50),
    ADD COLUMN IF NOT EXISTS embedding_model VARCHAR(100);

-- Copy existing (OpenAI, 1536-d) vectors into the new dimension-specific
-- column. No re-embedding needed since the dimension is unchanged.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'product_vector' AND column_name = 'embedding'
    ) THEN
        UPDATE product_vector
        SET embedding_1536 = embedding
        WHERE embedding IS NOT NULL
          AND embedding_1536 IS NULL;

        DROP INDEX IF EXISTS idx_product_vector_embedding_hnsw;
        ALTER TABLE product_vector DROP COLUMN embedding;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_product_vector_embedding_1536_hnsw
    ON product_vector USING hnsw (embedding_1536 vector_cosine_ops);

CREATE INDEX IF NOT EXISTS idx_product_vector_embedding_1024_hnsw
    ON product_vector USING hnsw (embedding_1024 vector_cosine_ops);
