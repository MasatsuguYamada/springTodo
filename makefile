goal: dependancy
	echo "In makefile"

dependancy:build
	echo "Get ready"

build:
	echo "frontend build*************";
	cd frontend && npm run build;
	cp -rf frontend/dist/* backend/src/main/resources/static

test-all:test-backend test-frontend

test-backend: start-docker-compose
	echo "backend test*************";
	cd backend && ./gradlew test

start-docker-compose:
	docker compose up -d

test-frontend:
	echo "frontend test*************"
	cd frontend && npm install && npm run test
