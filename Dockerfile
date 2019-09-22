FROM maven:3-jdk-11
WORKDIR /build

COPY . /build
RUN mvn -DskipTests install dependency:copy-dependencies

FROM openjdk:11

WORKDIR /app

RUN mkdir /db

COPY --from=0 /build/sccp-dht/target/dhtengine-*.jar /app/
COPY --from=0 /build/sccp-dht/target/dependency/ /app/dependency/

CMD java -cp "./*:dependency/*" com.colabriq.engine.EngineModule
