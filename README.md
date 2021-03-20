# DC Project

[![Tests](https://github.com/flecomte/dc-project/actions/workflows/tests.yml/badge.svg)](https://github.com/flecomte/dc-project/actions/workflows/tests.yml)

[Installation](./doc/installation)

### Run dockers
```bash
$ make run-docker
```

### Add fixtures
```bash
$ make fixtures
```

## Publish package
1. Got to [https://github.com](https://github.com/settings/tokens/new) and create a new token with packages scopes
2. Create a file `GH_TOKEN.txt` and put it the github token

### Maven
```bash
$ make publish-maven
```
### Docker

```bash
$ make publish-docker
```