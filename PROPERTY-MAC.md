# CLI

## mostrar carpetas DEFAULT

```text
find src

find src -type d
```
## mostrar carpetas TREE

```text
brew install tree

tree src
```
## mostrar propiedades de MAC

```text
echo "=== MAC ===" && \
system_profiler SPHardwareDataType | grep -E "Chip|Memory|Model Name" && \
echo "=== DISK ===" && \
df -h / | tail -1 && \
echo "=== DOCKER ===" && \
docker --version 2>/dev/null || echo "Docker no disponible"
```

# Configurar API Key como variable de entorno en macOS

## 1. Crear la variable

```bash terminal
export WEATHER_API_KEY="TU_API_KEY_REAL"
echo 'export WEATHER_API_KEY="TU_API_KEY_REAL"' >> ~/.zshrc
source ~/.zshrc
echo $WEATHER_API_KEY
grep -n "WEATHER_API_KEY" ~/.zshrc
```

