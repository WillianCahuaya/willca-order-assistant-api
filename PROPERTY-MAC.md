echo "=== MAC ===" && \
system_profiler SPHardwareDataType | grep -E "Chip|Memory|Model Name" && \
echo "=== DISK ===" && \
df -h / | tail -1 && \
echo "=== DOCKER ===" && \
docker --version 2>/dev/null || echo "Docker no disponible"


