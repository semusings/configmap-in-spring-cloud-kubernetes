.PHONY: help
.DEFAULT_GOAL := help
help:
	@echo "---------------------------------------------------------------------------------------"
	@echo ""
	@echo "				CLI"
	@echo ""
	@echo "---------------------------------------------------------------------------------------"
	@echo ""
	@awk 'BEGIN {FS = ":.*##"; printf "Usage: make \033[36m<target>\033[0m\n"} /^[a-zA-Z_-]+:.*?##/ { printf "  \033[36m%-25s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Development

build: ## Build
	mvn clean install && docker push docker.io/bhuwanupadhyay/configmap-in-spring-cloud-kubernetes:0.0.2-SNAPSHOT

##@ Helm

start: ## Minikube start
	minikube start

add-repos: ## Add helm repos
	./helm.sh --add-repos

update-charts: ## Update charts
	./helm.sh --update-charts

deploy: ## Helm deploy
	./helm.sh --deploy

deploy-prod: ## Helm deploy prod
	./helm.sh --deploy-prod

delete: ## Helm delete
	./helm.sh --delete
