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

build-docker: ## Build the docker image of application (alias: bd)
	docker build -t dc-project -f docker/app/Dockerfile .

pd: publish-docker

publish-docker: build-docker ## Publish docker image of application to Github (alias: pd)
	@git diff --quiet --exit-code || (echo "The git is DIRTY !!! You cannot publish this crap!" && exit 1)
	@cat ./GH_TOKEN.txt | docker login docker.pkg.github.com -u ${GITHUB_USERNAME} --password-stdin
	@docker tag dc-project docker.pkg.github.com/flecomte/dc-project/dc-project:${VERSION}
	docker push docker.pkg.github.com/flecomte/dc-project/dc-project:${VERSION}

rd: run-docker

run-docker: ## Build and Run all docker services (alias: rd)
	docker-compose up -d --build

rdd: run-docker-dependencies

run-docker-dependencies: ## Build and Run dependencies docker services (alias: rdd)
	docker-compose up -d --build openapi rabbitmq redis elasticsearch db sonarqube_db sonarqube

pm: publish-maven

publish-maven: ## Publish JAR file to Github (alias: pm)
	@git diff --quiet --exit-code || (echo "The git is DIRTY !!! You cannot publish this crap!" && exit 1)
	gradlew publish

f: fixtures

fixtures: ## Import fixtures (alias: f)
	bash src/main/resources/sql/fixtures/fixtures.sh

reset-database: ## Reset database !!!
	cd src/main/resources/sql/ ; bash resetDB.sh

test-sql: ## Test sql
	cd src/test/sql/ ; bash test.sh 1

v: version

version: ## Show current version (alias: v)
	@echo ${VERSION}
