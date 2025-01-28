FROM openjdk:21-jdk-slim-bullseye

COPY ./backend/build/libs/todo-0.0.1-SNAPSHOT.jar ./

CMD ["java", "-jar", "todo-0.0.1-SNAPSHOT.jar"]