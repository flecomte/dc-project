.EXPORT_ALL_VARIABLES:
VERSION=$(shell ./hook/version.sh)
GITHUB_USERNAME=$(shell git config user.email)
GITHUB_TOKEN=$(shell cat ./GH_TOKEN.txt)

# HELP
# This will output the help for each task
# thanks to https://marmelab.com/blog/2016/02/29/auto-documented-makefile.html
.PHONY: help

help: ## This help.
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

.DEFAULT_GOAL := help

bd: build-docker

build-docker: ## Build the docker image of application
	docker build -t dc-project -f docker/app/Dockerfile .

pd: publish-docker

publish-docker: build-docker ## Publish docker image of application to Github
	git diff --quiet --exit-code || (echo "The git is DIRTY !!! You cannot publish this crap!" && exit 1)
	cat ./GH_TOKEN.txt | docker login docker.pkg.github.com -u ${GITHUB_USERNAME} --password-stdin
	docker tag dc-project docker.pkg.github.com/flecomte/dc-project/dc-project:${VERSION}
	docker push docker.pkg.github.com/flecomte/dc-project/dc-project:${VERSION}

rd: run-docker

run-docker: ## Build and Run all docker services
	docker-compose up -d --build

pm: publish-maven

publish-maven: ## Publish JAR file to Github
	@git diff --quiet --exit-code || (echo "The git is DIRTY !!! You cannot publish this crap!" && exit 1)
	gradlew publish

f: fixtures

fixtures: ## Import fixtures
	bash src/main/resources/sql/fixtures/fixtures.sh

v: vertion

vertion: ## Show current version
	@echo ${VERSION}
