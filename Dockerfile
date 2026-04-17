FROM docker.io/library/amazoncorretto:21 AS extrator
WORKDIR /aplicacao

COPY deploy/*.cer /tmp/certs/
RUN cp /tmp/certs/*.cer /etc/pki/ca-trust/source/anchors/ && \
    update-ca-trust extract

COPY sgc.jar aplicacao.jar
RUN mkdir extraido && \
    java -Djarmode=tools -jar aplicacao.jar extract --layers --launcher --destination extraido

FROM docker.io/library/amazoncorretto:21

LABEL description="Sistema de Gestao de Competencias - SGC" \
      maintainer="SESEL <sesel@tre-pe.jus.br>" \
      version="1.0"

COPY deploy/*.cer /tmp/certs/
RUN set -eux; \
    cp /tmp/certs/*.cer /etc/pki/ca-trust/source/anchors/; \
    update-ca-trust extract; \
    yum clean all; \
    rm -rf /var/cache/yum; \
    yum makecache; \
    yum update -y; \
    yum install -y shadow-utils tzdata; \
    groupadd --gid 333 sgc; \
    useradd --uid 333 --gid 333 --no-create-home --shell /sbin/nologin sgc; \
    yum remove -y shadow-utils; \
    yum clean all; \
    rm -rf /var/cache/yum

ENV TZ=America/Recife
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

RUN keytool -cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias cert-tre -file /tmp/certs/cert-tre.cer && \
    keytool -cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias cert-for -file /tmp/certs/cert-for.cer && \
    rm -rf /tmp/certs

RUN mkdir -p /var/log/sgc /aplicacao && \
    chown -R sgc:sgc /var/log/sgc /aplicacao

WORKDIR /aplicacao

COPY --from=extrator --chown=sgc:sgc aplicacao/extraido/dependencies/ ./
COPY --from=extrator --chown=sgc:sgc aplicacao/extraido/spring-boot-loader/ ./
COPY --from=extrator --chown=sgc:sgc aplicacao/extraido/snapshot-dependencies/ ./
COPY --from=extrator --chown=sgc:sgc aplicacao/extraido/application/ ./

USER sgc:sgc

EXPOSE 10000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
