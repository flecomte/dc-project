# DC Project

[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=dc-project&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=dc-project)

[![Tests](https://github.com/flecomte/dc-project/actions/workflows/tests.yml/badge.svg)](https://github.com/flecomte/dc-project/actions/workflows/tests.yml)
[![Coverage Status](https://coveralls.io/repos/github/flecomte/dc-project/badge.svg?branch=master)](https://coveralls.io/github/flecomte/dc-project?branch=master)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=dc-project&metric=coverage)](https://sonarcloud.io/dashboard?id=dc-project)

[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=dc-project&metric=ncloc)](https://sonarcloud.io/dashboard?id=dc-project)

[Installation](./doc/installation/Installation.md)

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