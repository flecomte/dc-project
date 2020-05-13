#### BUILD ####
FROM gradle:5.6.4-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src
RUN gradle build -x test -x ktlintKotlinScriptCheck -x ktlintTestSourceSetCheck -x ktlintMainSourceSetCheck --no-daemon

#### RUN ####
FROM adoptopenjdk/openjdk11:jre-11.0.4_11-alpine
ENV APPLICATION_USER ktor
RUN adduser -D -g '' $APPLICATION_USER

RUN mkdir /app
RUN chown -R $APPLICATION_USER /app

USER $APPLICATION_USER

COPY --from=build /home/gradle/src/build/libs/dcproject-latest-all.jar /app/dcproject.jar
WORKDIR /app

CMD ["java", "-server", "-XX:+UnlockExperimentalVMOptions", "-XX:InitialRAMFraction=2", "-XX:MinRAMFraction=2", "-XX:MaxRAMFraction=2", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100", "-XX:+UseStringDeduplication", "-jar", "dcproject.jar"]
