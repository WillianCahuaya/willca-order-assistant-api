# LLM — Context & Tool Calling

## 1. LLM + Context

### Context

- El LLM no consulta MongoDB directamente.
- Nuestro backend obtiene la información.
- El backend convierte esa información en contexto.
- El contexto se envía junto con el prompt.
- El LLM utiliza ese contexto para generar la respuesta.

```text
                         USER
                          │
                   "¿Cuál es el
                  estado del pedido?"
                          ↓
              ┌─────────────────────┐
              │   YOUR BACKEND      │
              │  OrderContextService│
              └──────────┬──────────┘
                         ↓
                      MongoDB
                         ↓
                     ORDER DATA
                         ↓
                       CONTEXT
        ┌────────────────┴─────────────┐
        ↓                              ↓
 SYSTEM PROMPT                   USER MESSAGE
 "You are an assistant..."      "¿Cuál es el estado...?"
        └────────────────┬─────────────┘
                         ↓
                       GEMINI
                         ↓
                        TEXT
                         ↓
                    USER RESPONSE
```

## 2. LLM + Tool Calling

### Tool Calling

- El LLM decide qué herramienta necesita.
- El LLM NO ejecuta la herramienta.
- El backend recibe el functionCall.
- El backend ejecuta la herramienta.
- La herramienta devuelve el resultado.
- El backend envía functionResponse al LLM.
- El LLM genera la respuesta final.

```text
                         USER
                          │
           "¿Qué pedidos están DELIVERED?"
                          ↓
              ┌─────────────────────┐
              │   YOUR BACKEND      │
              │ GeminiToolService   │
              └──────────┬──────────┘
                         ↓
                       GEMINI
                         │
             ¿Necesito una herramienta?
                         ↓
                  ┌─────────────────────┐
                  │ functionCall        │
                  │                     │
                  │ findOrdersByStatus  │
                  │ status="DELIVERED"  │
                  └──────┬──────────────┘
                         ↓
                    YOUR BACKEND
                         ↓
                     OrderTool
                         ↓
                    OrderService
                         ↓
                      MongoDB
                         ↓
                    O005 + O007
                         ↓
                  functionResponse
                         ↓
                       GEMINI
                         ↓
                    FINAL TEXT
                         ↓
                        USER
```

### 3. Diferencia mental

```
=====LLM + CONTEXT=====

DATABASE
↓
CONTEXT
↓
LLM
↓
RESPONSE

=====LLM + TOOL=====

LLM
↓
"Necesito consultar algo"
↓
TOOL
↓
RESULT
↓
LLM
↓
RESPONSE
```

4. La idea que une ambos

```
        AI APPLICATION
             ↓
            LLM
┌────────────┴────────────┐
↓                         ↓
CONTEXT                 TOOLS
↓                         ↓
Información        Acción / consulta
└────────────┬────────────┘
             ↓
            LLM
             ↓
         RESPONSE
```

## TOOLS FLOW GENERAL

```
┌──────────────────────┐
│       GEMINI         │
│      "Razonar"       │
└──────────┬───────────┘
     functionCall
           ▼
┌──────────────────────┐
│ GeminiToolService    │
│      "Ejecutar"      │
└──────────┬───────────┘
         Tool
           ▼
┌──────────────────────┐
│     Backend/Data     │
└──────────┬───────────┘
        result
           ▼
┌──────────────────────┐
│ GeminiToolService    │
│ "functionResponse"   │
└──────────┬───────────┘
           ▼
┌──────────────────────┐
│       GEMINI         │
└──────────────────────┘
```

## TOOLS FLOW DETAILS

```
🤖 TOOL CALLING — FLUJO COMPLETO
│
├── 1. USER
│      │
│      │ POST /api/assistant
│      │ {
│      │   "message": "¿Cuál es el nombre del cliente de los pedidos CONFIRMED?"
│      │ }
│      ▼
│
├── 2. GeminiToolService
│      │
│      ├── Crea historyContents
│      │   USER
│      │   └── message
│      └── llama GeminiClient.chatWithTools()
│              │
│              ▼
│
├── 3. GeminiClient
│      │
│      ├── Construye request
│      │   contents
│      │   └── user message
│      │
│      │   tools
│      │   ├── findOrdersByStatus
│      │   ├── findOrderById
│      │   └── findCustomerById
│      │
│      └── HTTP POST → Gemini API
│              │
│              ▼
│
├── 4. GEMINI
│      │
│      │ Analiza la pregunta
│      │   "Necesito saber quiénes son los clientes de los pedidos CONFIRMED"
│      │
│      │ Decide:
│      │   "Primero necesito buscar los pedidos"
│      │
│      └── functionCall
│           │
│           │ findOrdersByStatus
│           │ status = CONFIRMED
│           ▼
│
├── 5. GeminiToolService
│      │
│      ├── processGeminiResponse()
│      │
│      ├── Detecta:
│      │      Function calls detected >>> 1
│      │
│      ├── Lee:
│      │      name = findOrdersByStatus
│      │      args = { status: "CONFIRMED" }
│      │
│      └── processToolCalls()
│              │
│              ▼
│
├── 6. executeTool()
│      │
│      │ Identifica:
│      │
│      │ findOrdersByStatus
│      │
│      └── llama OrderTool
│              │
│              ▼
│
├── 7. OrderTool
│      │
│      │ Consulta el backend / repository
│      │
│      └── Resultado
│           │
│           ├── O002 → C002 → CONFIRMED
│           └── O010 → C005 → CONFIRMED
│
│
├── 8. GeminiToolService
│      │
│      │ Convierte resultado en functionResponse
│      │ {
│      │   "functionResponse": {
│      │      "name": "findOrdersByStatus",
│      │      "id": "call_2459111",
│      │      "response": {
│      │         "result": [...]
│      │      }
│      │   }
│      │ }
│      │
│      └── historyContents.add(...)
│
│
├── 9. GeminiToolService
│      │
│      │ Envia nuevamente a Gemini
│      │
│      └── GeminiClient.chatWithToolResponse()
│              │
│              ▼
│
├── 10. Gemini
│       │
│       │ Recibe:
│       │
│       │ O002 → customerId C002
│       │ O010 → customerId C005
│       │
│       │ Razona:
│       │   "Ahora necesito conocer los clientes"
│       │
│       ├───────────────┐
│       ▼               ▼
│   functionCall      functionCall
│     C002              C005
│       └───────┬───────┘
│               ▼
│
├── 11. GeminiToolService
│       │
│       ├── Detecta 2 functionCalls
│       │
│       │ Function calls detected >>> 2
│       │
│       └── processToolCalls()
│               │
│               │ Flux.fromIterable()
│               │       +
│               │ flatMap()
│          ┌────┴────────────┐
│          ▼                 ▼
│
│   executeTool(C002)   executeTool(C005)
│          │                 │
│          ▼                 ▼
│   CustomerTool        CustomerTool
│          │                 │
│          ▼                 ▼
│   María López         Luis Rodríguez
│          │                 │
│          └────────┬────────┘
│                   ▼
│
├── 12. collectList()
│       │
│       │ Espera los resultados de ambos tools
│       │
│       └── [
│             functionResponse(C002),
│             functionResponse(C005)
│           ]
│
│
├── 13. GeminiToolService
│       │
│       │ historyContents.addAll(...)
│       │
│       │ Sending 2 tool response(s) back to Gemini
│       │
│       └── GeminiClient.chatWithToolResponse()
│               │
│               ▼
│
├── 14. Gemini
│       │
│       │ Recibe:
│       │
│       │ C002 → María López
│       │ C005 → Luis Rodríguez
│       │
│       │ Ya tiene toda la información necesaria
│       │
│       └── NO necesita otro tool
│               │
│               ▼
│
├── 15. Gemini genera respuesta final
│       │
│       │ "Los pedidos CONFIRMED pertenecen a:
│       │
│       │  O002 → María López
│       │  O010 → Luis Rodríguez"
│       │
│       ▼
│
├── 16. GeminiToolService
│       │
│       ├── processGeminiResponse()
│       │
│       ├── Function calls detected >>> 0
│       │
│       └── Extrae finalText
│               │
│               ▼
│
└── 17. USER
│
▼
"Los pedidos con estado CONFIRMED pertenecen
a los siguientes clientes:

        O002 → María López
        O010 → Luis Rodríguez"
```
