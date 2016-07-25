FROM anapsix/alpine-java
VOLUME /tmp
ADD target/rhizomerAPI-0.1.war app.jar
RUN sh -c 'touch /app.jar'
EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]