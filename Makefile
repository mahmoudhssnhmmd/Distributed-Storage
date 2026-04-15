SHELL := /bin/zsh

.PHONY: help test run compose-up compose-down compose-scale logs

help:
	@echo "Available targets:"
	@echo "  make test          - Run all tests"
	@echo "  make run           - Run Spring Boot app locally"
	@echo "  make compose-up    - Start docker stack"
	@echo "  make compose-down  - Stop docker stack"
	@echo "  make compose-scale - Start docker stack with 2 app instances"
	@echo "  make logs          - Follow docker compose logs"

test:
	./mvnw test

run:
	./mvnw spring-boot:run

compose-up:
	docker compose up --build

compose-down:
	docker compose down

compose-scale:
	docker compose up --build --scale app=2

logs:
	docker compose logs -f

