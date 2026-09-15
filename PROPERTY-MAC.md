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

