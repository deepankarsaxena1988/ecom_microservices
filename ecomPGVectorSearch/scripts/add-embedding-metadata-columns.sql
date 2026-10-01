-- Adds embedding provenance columns so each vector row records which
-- provider/model produced its embedding (e.g. openai/text-embedding-3-small
-- vs. ollama/qwen3-embedding:0.6b). Run this once against the pgvector database
-- before using the multi-provider embedding support.

ALTER TABLE product_vector
    ADD COLUMN IF NOT EXISTS embedding_provider VARCHAR(50),
    ADD COLUMN IF NOT EXISTS embedding_model VARCHAR(100);
