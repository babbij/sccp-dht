FROM maven:3-jdk-11
WORKDIR /build

# add the pom.xmls
# download all dependencies
COPY ./pom.xml /build/pom.xml
COPY ./dhtengine/pom.xml /build/dhtengine/pom.xml
COPY ./dhtmem/pom.xml /build/dhtmem/pom.xml
COPY ./rdfendpoint/pom.xml /build/rdfendpoint/pom.xml
COPY ./utils/pom.xml /build/utils/pom.xml

COPY ./lib/pom.xml /build/lib/pom.xml

COPY ./lib/shared/pom.xml /build/lib/shared/pom.xml
COPY ./lib/kpabe/pom.xml /build/lib/kpabe/pom.xml
COPY ./lib/model/pom.xml /build/lib/model/pom.xml
COPY ./lib/webapp/pom.xml /build/lib/webapp/pom.xml

RUN mvn dependency:resolve-plugins dependency:go-offline --fail-never

COPY . /build
RUN mvn -DskipTests package



FROM openjdk:11
WORKDIR /app
COPY --from=0 /build/dhtengine/target/*.zip /app
RUN unzip /app/*.zip -d /app

CMD java -Djava.library.path=/app/native/linux/ -jar *.jar