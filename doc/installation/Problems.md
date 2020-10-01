Problems
========

1. [max_map_count](#max_map_count)

max_map_count
-------------

**Context**:

    Elasticsearch in Docker on WSL2

**Error**:
```
max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]
```

**Resolution**:

[Stackoverflow](https://stackoverflow.com/questions/42111566/elasticsearch-in-windows-docker-image-vm-max-map-count)

In Powershell:
```powershell
wsl -d docker-desktop
sysctl -w vm.max_map_count=262144
echo "vm.max_map_count = 262144" > /etc/sysctl.d/99-docker-desktop.conf
```

restart docker-desktop