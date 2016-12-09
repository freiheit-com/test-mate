FROM anapsix/alpine-java:8

ENV MATE_VERSION=0.7.0

RUN mkdir -p /usr/test-mate/
COPY target/test-mate-$MATE_VERSION.jar /usr/test-mate/
RUN ln -s /usr/test-mate/test-mate-$MATE_VERSION.jar /usr/test-mate/test-mate-current.jar

ENTRYPOINT ["java", "-jar", "/usr/test-mate/test-mate-current.jar"]
CMD [""]
