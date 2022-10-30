FROM eclipse-temurin:17

RUN mkdir -p /opt/app/plugins
COPY build/libs/*.jar /opt/app/app.jar

CMD ["java", "-Dloader.path=opt/app/plugins", "-jar", "/opt/app/app.jar"]