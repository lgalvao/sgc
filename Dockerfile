# Estágio base de build (toolchain + certificados)
FROM docker.io/library/gradle:jdk25-ubi AS build-base
USER root
WORKDIR /build
ARG FRONTEND_BUILD_MODE=production
ENV FRONTEND_BUILD_MODE=$FRONTEND_BUILD_MODE

# 1. Configura os certificados corporativos no nível do Sistema Operacional
COPY deploy/*.cer deploy/cert-combinados.pem /tmp/certs/
RUN cp /tmp/certs/*.cer /etc/pki/ca-trust/source/anchors/ && \
    update-ca-trust extract && \
    keytool -cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias cert-tre -file /tmp/certs/cert-tre.cer && \
    keytool -cacerts -storepass changeit -noprompt -trustcacerts -importcert -alias cert-for -file /tmp/certs/cert-for.cer

# Configura certificados para o Node.js
ENV NODE_EXTRA_CA_CERTS=/tmp/certs/cert-combinados.pem

# 2. Instala dependências do SO: Node.js (via setup script) e utilitários
RUN curl -k -fsSL https://rpm.nodesource.com/setup_20.x | bash - && \
    microdnf install -y nodejs findutils && \
    npm install -g pnpm@10.33.3

# Estágio de cache das dependências Java
FROM build-base AS deps-java

COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle/ gradle/
COPY backend/build.gradle.kts backend/

# Baixa dependências Java em camada separada para preservar cache quando só o frontend muda
RUN gradle --no-daemon :backend:dependencies

# Estágio de cache das dependências do frontend
FROM deps-java AS deps-frontend

COPY frontend/build.gradle.kts frontend/
COPY frontend/package.json frontend/pnpm-lock.yaml frontend/

# Instala dependências do frontend, preservando a orquestração oficial do Gradle
RUN gradle --no-daemon :frontend:install

# Estágio 1: Build unificado (Backend + Frontend)
FROM deps-frontend AS build-env

# 4. Copia o projeto inteiro
COPY . . 

# 5. Executa o build completo orquestrado pelo Gradle
RUN gradle --no-daemon :backend:bootJar -x test
# Estágio 2: Extrator (prepara as camadas do Spring Boot)
FROM docker.io/library/amazoncorretto:25 AS extrator
WORKDIR /aplicacao

# Re-aplica certificados para o extrator
COPY deploy/*.cer /tmp/certs/
RUN cp /tmp/certs/*.cer /etc/pki/ca-trust/source/anchors/ && \
    update-ca-trust extract

# Pega o JAR gerado no estágio anterior
COPY --from=build-env /build/backend/build/libs/*-plain.jar /dev/null
COPY --from=build-env /build/backend/build/libs/*.jar aplicacao.jar

RUN mkdir extraido && \
    java -Djarmode=tools -jar aplicacao.jar extract --layers --launcher --destination extraido

# Estágio 3: Imagem Final (Runtime)
FROM docker.io/library/amazoncorretto:25

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
