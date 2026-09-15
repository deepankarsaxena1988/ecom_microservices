CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS product_vector (
    product_id BIGINT PRIMARY KEY,
    category_id BIGINT,
    domain VARCHAR(255),
    content_text TEXT NOT NULL,
    embedding vector(1536),
    source_updated_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE OR REPLACE FUNCTION set_product_vector_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_set_product_vector_updated_at ON product_vector;
CREATE TRIGGER trg_set_product_vector_updated_at
BEFORE UPDATE ON product_vector
FOR EACH ROW
EXECUTE FUNCTION set_product_vector_updated_at();

CREATE INDEX IF NOT EXISTS idx_product_vector_category_id
    ON product_vector (category_id);

CREATE INDEX IF NOT EXISTS idx_product_vector_domain
    ON product_vector (domain);

CREATE INDEX IF NOT EXISTS idx_product_vector_embedding_hnsw
    ON product_vector USING hnsw (embedding vector_cosine_ops);
