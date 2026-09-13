# Gemini API — Panorama de GenerateContent

## 1. Visión general

Gemini API permite enviar una solicitud al modelo y recibir una respuesta generada.

```text
REQUEST
   ↓
Gemini
   ↓
RESPONSE
```

En nuestro proyecto utilizamos `generateContent` porque permite entender claramente los flujos de **Context** y **Tool Calling**.

---

## 2. Panorama del Request

```text
Gemini API
└── generateContent
    │
    ├── contents
    ├── systemInstruction
    ├── tools
    ├── toolConfig
    ├── generationConfig
    ├── safetySettings
    ├── cachedContent
    ├── serviceTier
    └── store
```

No todos los campos son necesarios en cada request.

---

## 3. `contents`

Representa el contenido de la conversación.

```text
contents
└── Content[]
    ├── role
    └── parts[]
        ├── text
        ├── image
        ├── audio
        ├── video
        └── ...
```

Ejemplo:

```json
{
  "contents": [
    {
      "role": "user",
      "parts": [
        {
          "text": "¿Qué pedidos están DELIVERED?"
        }
      ]
    }
  ]
}
```

### Mental model

```text
contents
   ↓
¿Qué le estoy enviando a Gemini?
```

En una conversación puede contener varios turnos:

```text
user
 ↓
model
 ↓
user
 ↓
model
 ↓
user
```

---

## 4. `systemInstruction`

Define cómo debe comportarse el modelo.

Ejemplo:

```json
{
  "system_instruction": {
    "parts": [
      {
        "text": "You are an assistant for an order management system."
      }
    ]
  }
}
```

### Mental model

```text
systemInstruction
        ↓
"Cómo debe comportarse Gemini"
```

No representa la pregunta del usuario.

---

## 5. `tools`

Declara las herramientas que Gemini puede utilizar.

Ejemplo conceptual:

```text
tools
└── functionDeclarations
    └── findOrdersByStatus
        ├── name
        ├── description
        └── parameters
```

Ejemplo:

```json
{
  "tools": [
    {
      "functionDeclarations": [
        {
          "name": "findOrdersByStatus",
          "description": "Find orders by their current status",
          "parameters": {
            "type": "OBJECT",
            "properties": {
              "status": {
                "type": "STRING"
              }
            },
            "required": ["status"]
          }
        }
      ]
    }
  ]
}
```

### Importante

```text
tools ≠ ejecución
```

`tools` solamente le dice a Gemini:

> Estas son las capacidades externas disponibles.

Gemini no conoce nuestro código Java ni accede directamente a MongoDB.

---

## 6. `toolConfig`

Configura cómo puede utilizar Gemini las herramientas.

### Mental model

```text
tools
   ↓
¿Qué herramientas existen?

toolConfig
   ↓
¿Cómo puede utilizarlas?
```

Por ahora utilizamos el comportamiento automático y no necesitamos configurarlo.

---

## 7. `generationConfig`

Controla aspectos de la generación.

Ejemplos:

```text
generationConfig
├── temperature
├── maxOutputTokens
├── topP
├── topK
├── stopSequences
└── candidateCount
```

### Mental model

```text
generationConfig
        ↓
"Cómo quieres que genere la respuesta"
```

---

## 8. `safetySettings`

Permite configurar restricciones de seguridad.

```text
safetySettings
└── categoría
    └── threshold
```

### Mental model

```text
safetySettings
        ↓
"Qué contenido debe bloquearse"
```

No es parte central de nuestro Tool Calling actual.

---

## 9. `cachedContent`

Permite reutilizar contenido previamente almacenado en caché.

```text
cachedContent
      ↓
contenido reutilizable
      ↓
Gemini
```

Es útil cuando un mismo contexto se reutiliza frecuentemente.

Por ahora no lo necesitamos.

---

## 10. `serviceTier`

Permite especificar el nivel de servicio de la solicitud.

```text
serviceTier
    ↓
nivel de servicio
```

Es principalmente una preocupación de infraestructura/servicio.

---

## 11. `store`

Controla aspectos relacionados con el almacenamiento de la interacción según las capacidades de la API.

Por ahora no es relevante para nuestro flujo.

---

# 12. Panorama de la Response

La respuesta sigue una estructura diferente:

```text
GenerateContentResponse
└── candidates[]
    └── content
        └── parts[]
            ├── text
            ├── functionCall
            └── ...
```

Ejemplo real de Tool Calling:

```json
{
  "candidates": [
    {
      "content": {
        "parts": [
          {
            "functionCall": {
              "name": "findOrdersByStatus",
              "args": {
                "status": "DELIVERED"
              },
              "id": "call_98498"
            }
          }
        ],
        "role": "model"
      }
    }
  ]
}
```

---

# 13. `functionCall`

Es la respuesta de Gemini solicitando que nuestra aplicación ejecute una herramienta.

```text
functionCall
├── name
├── args
└── id
```

Ejemplo:

```json
{
  "name": "findOrdersByStatus",
  "args": {
    "status": "DELIVERED"
  },
  "id": "call_98498"
}
```

### Significado

```text
name
 ↓
¿Qué herramienta quiere ejecutar?

args
 ↓
¿Qué argumentos necesita?

id
 ↓
Identificador de esta llamada
```

---

# 14. ¿Quién define qué?

Hay dos partes diferentes.

## Google / Gemini define

El contrato de la API:

```text
contents
systemInstruction
tools
toolConfig
generationConfig
functionCall
...
```

## Nuestra aplicación define

La herramienta concreta:

```text
findOrdersByStatus
```

y su contrato:

```text
findOrdersByStatus(status)
```

Gemini no conoce nuestro Java:

```text
OrderTool
OrderService
MongoDB
```

Solo conoce la definición que nosotros le enviamos.

---

# 15. ¿Cómo Gemini obtiene `DELIVERED`?

El usuario escribe lenguaje natural:

```text
"¿Qué pedidos están DELIVERED?"
```

Nosotros enviamos:

```text
Mensaje del usuario
+
Definición de las tools
```

Gemini analiza ambos:

```text
Usuario
"¿Qué pedidos están DELIVERED?"

        +

Tool disponible
findOrdersByStatus(status)
```

El modelo determina que esa herramienta es apropiada y genera:

```json
{
  "name": "findOrdersByStatus",
  "args": {
    "status": "DELIVERED"
  }
}
```

No existe código nuestro haciendo:

```java
if (message.contains("DELIVERED")) {
    ...
}
```

El modelo transforma el lenguaje natural en los argumentos de la herramienta.

---

# 16. ¿Gemini ejecuta la herramienta?

No.

Gemini solamente solicita:

```text
functionCall
```

La ejecución pertenece a nuestra aplicación.

```text
Gemini
   ↓
functionCall
   ↓
Nuestra aplicación
   ↓
OrderTool
   ↓
OrderService
   ↓
MongoDB
```

---

# 17. Tool Calling completo

```text
User
 ↓
GeminiToolService
 ↓
GeminiClient
 ↓
Gemini
 ↓
functionCall
 ↓
Nuestra aplicación
 ↓
OrderTool
 ↓
OrderService
 ↓
MongoDB
 ↓
resultado
 ↓
functionResponse
 ↓
Gemini
 ↓
respuesta final
```

Gemini participa en dos momentos:

```text
① Gemini decide qué herramienta necesita

② Gemini recibe el resultado y genera la respuesta final
```

---

# 18. Context vs Tool Calling

## Context Injection

```text
User
 ↓
Nuestra aplicación decide qué consultar
 ↓
OrderContextService
 ↓
MongoDB
 ↓
Context
 ↓
Gemini
 ↓
Respuesta
```

Nuestra aplicación decide qué información necesita el modelo.

## Tool Calling

```text
User
 ↓
Gemini
 ↓
Gemini decide qué herramienta necesita
 ↓
functionCall
 ↓
Nuestra aplicación
 ↓
OrderTool
 ↓
MongoDB
 ↓
functionResponse
 ↓
Gemini
 ↓
Respuesta
```

El modelo participa en la decisión de qué herramienta utilizar.

---

# 19. Mapa mental final

```text
🤖 GEMINI API
│
├── REQUEST
│   │
│   ├── contents
│   │   └── Qué le envío
│   │
│   ├── systemInstruction
│   │   └── Cómo debe comportarse
│   │
│   ├── tools
│   │   └── Qué capacidades externas tiene
│   │
│   ├── toolConfig
│   │   └── Cómo puede usar tools
│   │
│   ├── generationConfig
│   │   └── Cómo generar
│   │
│   ├── safetySettings
│   │   └── Seguridad
│   │
│   └── cachedContent
│       └── Contexto reutilizable
│
└── RESPONSE
    │
    └── candidates[]
        │
        └── content
            │
            └── parts[]
                ├── text
                ├── functionCall
                └── ...
```

## 20. Para nuestro proyecto

Las piezas prioritarias son:

```text
contents
systemInstruction
tools
functionCall
functionResponse
```

Posteriormente podremos incorporar:

```text
conversation
RAG
Agents
structured output
observability
evaluation
```

---

## 21. Mental model principal

```text
              GEMINI API
                  │
        ┌─────────┴─────────┐
        │                   │
      REQUEST            RESPONSE
        │                   │
        ├─ contents         └─ candidates
        ├─ systemInstruction       │
        ├─ tools                   └─ content
        ├─ toolConfig                   │
        ├─ generationConfig             └─ parts
        ├─ safetySettings                    ├─ text
        └─ cachedContent                     └─ functionCall
```

La idea fundamental:

```text
REQUEST
   ↓
"Esto es lo que Gemini recibe"
   ↓
GEMINI
   ↓
RESPONSE
   ↓
"Esto es lo que Gemini decide/genera"
```

Y en Tool Calling:

```text
tools
   ↓
Gemini conoce las capacidades disponibles
   ↓
functionCall
   ↓
Nuestra aplicación ejecuta la capacidad
   ↓
functionResponse
   ↓
Gemini genera la respuesta final
```
