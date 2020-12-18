FROM clojure:openjdk-8-lein as builder
COPY . .
RUN lein uberjar

FROM openjdk:8

ENV MATE_VERSION=0.10.0

RUN mkdir -p /usr/test-mate/
COPY --from=builder /tmp/target/test-mate-$MATE_VERSION-standalone.jar /usr/test-mate/
RUN ln -s /usr/test-mate/test-mate-$MATE_VERSION-standalone.jar /usr/test-mate/test-mate-current.jar

ENTRYPOINT ["java", "-jar", "/usr/test-mate/test-mate-current.jar"]
CMD [""]
