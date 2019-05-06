# Alpine Linux with OpenJDK JRE
FROM openjdk:8-jre-alpine
# copy WAR into image
COPY target/*.jar /app.jar
COPY qa.json /qa.json
# run application with this command line
CMD ["/usr/bin/java", "-jar", "/app.jar"]
