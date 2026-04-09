SHELL := /bin/bash
.DEFAULT_GOAL := help

# ── Targets ────────────────────────────────────────────────────────────────────

.PHONY: setup up down build test clean help

setup: ## Generate .env with a random APP_ENCRYPTION_KEY (skips if .env exists)
	@if [ ! -f .env ]; then \
		cp .env.example .env; \
		KEY=$$(openssl rand -hex 32); \
		if [[ "$$OSTYPE" == "darwin"* ]]; then \
			sed -i '' "s|APP_ENCRYPTION_KEY=.*|APP_ENCRYPTION_KEY=$$KEY|" .env; \
		else \
			sed -i "s|APP_ENCRYPTION_KEY=.*|APP_ENCRYPTION_KEY=$$KEY|" .env; \
		fi; \
		echo "✓ .env created with a freshly generated APP_ENCRYPTION_KEY"; \
	else \
		echo "✓ .env already exists — skipping key generation"; \
	fi

up: setup ## Generate .env if needed, then build and start all services
	docker compose up --build

down: ## Stop and remove containers (keeps volumes / database data)
	docker compose down

build: setup ## Build images without starting containers
	docker compose build

test: ## Run all tests inside Docker (no local Java/Node needed)
	./run_tests.sh

clean: ## Stop containers AND delete volumes (full reset including database)
	docker compose down -v

help: ## Show this help
	@echo ""
	@echo "  CampusFit — available make targets"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
	  awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-10s\033[0m %s\n", $$1, $$2}'
	@echo ""
