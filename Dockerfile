FROM eclipse-temurin:17

RUN mkdir -p /opt/app /opt/app/plugins
COPY build/libs/*.jar /opt/app/app.jar
CMD ["java", "-cp", "/opt/app/app.jar", "-Dloader.path=/opt/app/plugins", "org.springframework.boot.loader.PropertiesLauncher"]