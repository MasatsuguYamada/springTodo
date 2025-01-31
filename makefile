goal: dependancy
	echo "In makefile"

dependancy: test-all
	echo "Get ready"

test-all:test-backend test-frontend

test-backend:
	echo "test-backend start" && cd backend && ./gradlew test

test-frontend:
	cd frontend && npm run test
