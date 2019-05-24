# Alpine Linux with OpenJDK JRE
FROM openjdk:8-jre-alpine
# copy WAR into image
COPY target/*.jar /app.jar
COPY first.json /first.json
COPY second.json /second.json
COPY third.json /third.json
# run application with this command line
CMD ["/usr/bin/java", "-jar -Dspring.profiles.active=prod", "/app.jar"]
