FROM gradle:8.13-jdk23 AS build
COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src
RUN gradle customFatJar --no-daemon

FROM openjdk:23-jdk

RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/mine-1.0-SNAPSHOT.jar /app/mine.jar

ENTRYPOINT ["java","-jar","/app/mine.jar"]