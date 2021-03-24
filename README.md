# DC Project

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/efed856329a74e29b8bfd3685134a238)](https://app.codacy.com/gh/flecomte/dc-project?utm_source=github.com&utm_medium=referral&utm_content=flecomte/dc-project&utm_campaign=Badge_Grade_Settings)
[![CodeFactor](https://www.codefactor.io/repository/github/flecomte/dc-project/badge?s=869dc426625a253a07bea95f9380e23fdb048b94)](https://www.codefactor.io/repository/github/flecomte/dc-project)
[![Tests](https://github.com/flecomte/dc-project/actions/workflows/tests.yml/badge.svg)](https://github.com/flecomte/dc-project/actions/workflows/tests.yml)

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