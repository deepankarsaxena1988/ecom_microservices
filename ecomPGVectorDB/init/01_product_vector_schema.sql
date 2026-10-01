CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS product_vector (
    product_id BIGINT PRIMARY KEY,
    category_id BIGINT,
    domain VARCHAR(255),
    content_text TEXT NOT NULL,
    embedding_1536 vector(1536),
    embedding_1024 vector(1024),
    embedding_provider VARCHAR(50),
    embedding_model VARCHAR(100),
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

CREATE INDEX IF NOT EXISTS idx_product_vector_embedding_1536_hnsw
    ON product_vector USING hnsw (embedding_1536 vector_cosine_ops);

CREATE INDEX IF NOT EXISTS idx_product_vector_embedding_1024_hnsw
    ON product_vector USING hnsw (embedding_1024 vector_cosine_ops);
