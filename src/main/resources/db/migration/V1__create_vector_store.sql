-- =========================================================
-- RAG VECTOR STORE
-- =========================================================

-- 1. pgvector
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. Tabla de documentos + embeddings
CREATE TABLE IF NOT EXISTS order_embeddings (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL,
    customer_id VARCHAR(50),
    status VARCHAR(50),
    total NUMERIC(10,2),
    restaurant_id VARCHAR(50),
    content TEXT NOT NULL,
    embedding VECTOR(768) NOT NULL,

    CONSTRAINT uk_order_embeddings_order_id
        UNIQUE (order_id)
);

-- 3. Índice para búsquedas por order_id
CREATE INDEX IF NOT EXISTS idx_order_embeddings_order_id
    ON order_embeddings(order_id);

-- 4. Índice para similarity search
-- Lo agregaremos cuando implementemos la búsqueda vectorial.
-- CREATE INDEX ...


--TRUNCATE TABLE order_embeddings RESTART IDENTITY;

-- SQL para verificar
SELECT
    id,
    order_id,
    customer_id,
    status,
    total,
    restaurant_id,
    vector_dims(embedding) AS dimensions
FROM order_embeddings
ORDER BY order_id;

SELECT version();

ALTER TABLE order_embeddings
ADD CONSTRAINT uk_order_embeddings_order_id
UNIQUE (order_id);
