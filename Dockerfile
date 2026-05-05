# Estágio 1: Build do Frontend
FROM docker.io/library/node:26 AS build-frontend
WORKDIR /build
# Instala o pnpm globalmente
RUN npm install -g pnpm@11.0.0

# Copia arquivos de dependências primeiro para cachear
COPY frontend/package.json frontend/pnpm-lock.yaml ./ 
RUN pnpm install --frozen-lockfile

# Copia o resto e gera o build
COPY frontend/ ./ 
RUN pnpm run build

# Estágio 2: Build do Backend (Spring Boot Jar)
FROM docker.io/library/amazoncorretto:25 AS build-backend
WORKDIR /build

# Instala utilitários necessários para o gradlew (xargs)
RUN yum install -y findutils

# Copia o projeto inteiro
COPY . . 

# Copia o frontend gerado para a pasta de recursos estáticos do backend
# O Gradle vai embutir isso automaticamente no Jar se estiver em src/main/resources/static
COPY --from=build-frontend /build/dist/ ./backend/src/main/resources/static/

# Executa o build do backend (pula o build do frontend via Gradle pois já fizemos)
RUN ./gradlew :backend:bootJar -x test -x :frontend:buildVue -x :frontend:install

# Estágio 3: Extrator (prepara as camadas do Spring Boot)
FROM docker.io/library/amazoncorretto:25 AS extrator
WORKDIR /aplicacao

COPY deploy/*.cer /tmp/certs/
RUN cp /tmp/certs/*.cer /etc/pki/ca-trust/source/anchors/ && \
    update-ca-trust extract

# Pega o JAR gerado no estágio anterior
COPY --from=build-backend /build/backend/build/libs/*.jar aplicacao.jar

RUN mkdir extraido && \
    java -Djarmode=tools -jar aplicacao.jar extract --layers --launcher --destination extraido

# Estágio 4: Imagem Final (Runtime)
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
