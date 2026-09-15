# PROJECT

```text
                CONTROLLER
                 │
                 │ ──────► DTO
                 ▼
             IASERVICE
                 │
  ┌──────────────┼──────────────┐
  ▼              ▼              ▼
TOOL           CLIENT           RAG
  │              │              │
  ▼              ▼              ▼
SERVICE        GEMINI         DOCUMENT
  │                             │
  ▼                             ▼
REPOSITORY                     EMBEDDING
  │                             │
  ▼                             ▼
MODEL                         CLIENT
  │                             │
  ▼                             ▼
DATABASE                   GEMINI EMBEDDING

```

