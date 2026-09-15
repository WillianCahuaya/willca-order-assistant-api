# POSTGRESQL

## docker compose

```text
docker compose up -d mongodb
docker compose down mongodb

docker compose up -d rag-postgres
docker compose down rag-postgres
```

## Test

```text
docker exec -it willca-rag-postgres psql -U postgres -d willca_rag

SELECT version();

CREATE EXTENSION IF NOT EXISTS vector;

SELECT extname FROM pg_extension WHERE extname = 'vector';
```

### Logs
```
docker exec -it willca-rag-postgres psql -U postgres -d willca_rag
psql (16.15 (Debian 16.15-1.pgdg12+2))
Type "help" for help.

willca_rag=# SELECT version();
version
------------------------------------------------------------------------------------------------------------------------------------
PostgreSQL 16.15 (Debian 16.15-1.pgdg12+2) on aarch64-unknown-linux-gnu, compiled by gcc (Debian 12.2.0-14+deb12u1) 12.2.0, 64-bit
(1 row)

willca_rag=# CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION
willca_rag=# SELECT extname FROM pg_extension WHERE extname = 'vector';
extname
---------
vector
(1 row)
```

