goal: dependancy
	echo "In makefile"

dependancy: test-all
	echo "Get ready"

test-all:test-backend test-frontend

test-backend: start-docker-compose
	echo "test-backend start" && cd backend && ./gradlew test

start-docker-compose:
	docker compose up -d

test-frontend:
	cd frontend && npm run test
