# RAG

- Document
- Chunk
- Embedding
- Vector Store

```text
     📄 DOCUMENT
          ↓
     CHUNKING
          │
  ┌───────┼───────┐
  ↓       ↓       ↓
 C1      C2      C3
  ↓       ↓       ↓
Embedding Embedding Embedding
  └───────┼───────┘
          ↓
    VECTOR STORE
          │
    ← CONSULTA →
          │
       Question
          ↓
      Embedding
          ↓
  Similarity Search
          ↓
        Top-K
          ↓
  Relevant Chunks
          ↓
       Context
          ↓
        Gemini
          ↓
       Answer
```
ss

```
   INGESTION
      │
  CSV / Excel
      ↓
   CHUNKING
      ↓
  EMBEDDING
      ↓
 VECTOR STORE
      │
      ▼
  QUERY
      │
 User Question
      ↓
  EMBEDDING
      ↓
Similarity Search
      ↓
    Top-K
      ↓
Relevant Chunks
      ↓
   CONTEXT
      ↓
  GEMINI / LLM
      ↓
    ANSWER
```

## Flujo del proyecto implementación

```
┌───────────────────────────────────────┐
│       USUARIO                         │
│ "¿Cuáles son los pedidos confirmados?"│
└────────────┬──────────────────────────┘
             ▼
┌──────────────────────┐
│   RagController      │
│   /api/rag/ask       │
└────────────┬─────────┘
             ▼
┌──────────────────────────┐
│  GeminiContextService    │
│ Orquesta el flujo RAG    │
└────────────┬─────────────┘
             ▼
┌──────────────────────────┐
│    RagSearchService      │
└────────────┬─────────────┘
             ▼
┌──────────────────────────┐
│ GeminiEmbeddingClient    │
│ Question → Vector 768    │
└────────────┬─────────────┘
             ▼
┌──────────────────────────┐
│   PostgreSQL + pgvector  │
│                          │
│  Similarity Search       │
│  embedding <=> query     │
└────────────┬─────────────┘
             ▼
        ┌─────────┐
        │  Top-K  │
        │   K=3   │
        └────┬────┘
   ┌─────────┼─────────┐
   ▼         ▼         ▼
  O002      O007      O010
   └─────────┼─────────┘
             ▼
   ┌───────────────────────┐
   │      Context          │
   │ O002 + O007 + O010    │
   └─────────┬─────────────┘
             ▼
┌──────────────────────────┐
│      GeminiClient        │
│ System Prompt            │
│ + Context                │
│ + User Question          │
└────────────┬─────────────┘
             ▼
 ┌──────────────────────┐
 │      Gemini LLM      │
 │ Interpret + Generate │
 └──────────┬───────────┘
            ▼
 ┌──────────────────────┐
 │        ANSWER        │
 │ O002, O007, O010     │
 └──────────────────────┘
```

## La Preparacion de los documentos

```
 INGESTION / INDEXING
         ▼
       CSV
         ▼
     CHUNKING
         ▼
  OrderDocument
         ▼
  Embedding Model
         ▼
Vector 768 dimensiones
         ▼
PostgreSQL + pgvector
═════════╪══════════
         ▼
    RETRIEVAL
         │
      Question
         ▼
  Embedding Model
         ▼
    Query Vector
         ▼
 Similarity Search
         ▼
       Top-K
         ▼
  Relevant Chunks
         ▼
      Context
         ▼
═════════╪═════════
         ▼
     GENERATION
         ▼
     Gemini LLM
         ▼
       Answer
```

