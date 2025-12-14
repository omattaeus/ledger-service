.PHONY: help build up down restart logs clean test

help: ## Mostra esta mensagem de ajuda
	@echo "Comandos disponíveis:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-15s\033[0m %s\n", $$1, $$2}'

build: ## Build da aplicação e containers
	docker compose build

up: ## Sobe os serviços (build se necessário)
	docker compose up --build

up-d: ## Sobe os serviços em background
	docker compose up -d

down: ## Para os serviços
	docker compose down

down-v: ## Para os serviços e remove volumes (limpa dados)
	docker compose down -v

restart: ## Restart dos serviços
	docker compose restart

logs: ## Mostra logs de todos os serviços
	docker compose logs -f

logs-app: ## Mostra logs apenas da aplicação
	docker compose logs -f app

logs-db: ## Mostra logs apenas do PostgreSQL
	docker compose logs -f postgres

ps: ## Lista status dos containers
	docker compose ps

shell-app: ## Acessa shell do container da aplicação
	docker exec -it ledger-service sh

shell-db: ## Acessa PostgreSQL via psql
	docker exec -it ledger-postgres psql -U postgres -d ledger_db

clean: ## Remove containers, volumes, networks e imagens
	docker compose down -v --rmi all
	docker system prune -f

rebuild: ## Rebuild completo (limpa cache)
	docker compose build --no-cache

test: ## Roda testes localmente (sem Docker)
	./mvnw test

run-local: ## Roda aplicação localmente com perfil dev
	./mvnw spring-boot:run -Dspring-boot.run.profiles=dev