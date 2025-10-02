# SGC - Sistema de Gestão de Conhecimentos

Este é um projeto multi-módulo Gradle com um backend Spring Boot e um frontend Vue.js.

## Estrutura do Projeto

- `sgc/` (raiz do projeto)
  - `backend/`: Módulo do Spring Boot (API)
  - `frontend/`: Módulo do Vue.js (UI)
  - `build.gradle.kts`: Script de build principal do Gradle.
  - `settings.gradle.kts`: Configurações dos módulos Gradle.

## Como Desenvolver

O fluxo de trabalho de desenvolvimento envolve executar o backend e o frontend separadamente.

### Executar o Backend

Use o Gradle para iniciar o servidor Spring Boot. O servidor será iniciado em `http://localhost:8080`.

```bash
# No diretório raiz (sgc)
./gradlew :backend:bootRun
```
(Nota: O wrapper do Gradle (`gradlew`) ainda não foi adicionado. Por enquanto, use o comando `gradle` globalmente se o tiver instalado.)

### Executar o Frontend

Navegue até o diretório do frontend e inicie o servidor de desenvolvimento do Vite. A aplicação estará disponível em `http://localhost:5173`.

```bash
cd frontend
npm run dev
```
As chamadas de API feitas para `/api` no frontend serão automaticamente redirecionadas para o backend em `http://localhost:8080`.

## Build de Produção

Para criar um pacote de produção, execute o comando de build do Gradle na raiz do projeto.

```bash
# No diretório raiz (sgc)
./gradlew build
```
Este comando irá:
1. Instalar as dependências do frontend (`npm install`).
2. Construir os arquivos estáticos do frontend (`npm run build`).
3. Copiar os arquivos do frontend para o diretório de recursos do backend.
4. Compilar e empacotar a aplicação Spring Boot em um arquivo JAR.

## Deploy

O resultado do build de produção é um único arquivo JAR localizado em `backend/build/libs/`. Este JAR contém a aplicação Spring Boot com o frontend Vue.js embutido.

Para executar a aplicação a partir do JAR, use o seguinte comando:

```bash
java -jar backend/build/libs/sgc-0.0.1-SNAPSHOT.jar
```
