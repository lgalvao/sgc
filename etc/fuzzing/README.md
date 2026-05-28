# Testes de Segurança Avançados com WuppieFuzz no SGC

Este diretório contém a documentação operacional e os utilitários criados para auditar, fustigar (*fuzzing*) e fortalecer o backend do SGC (Sistema de Gestão de Competências) contra vulnerabilidades e bugs de estado na API REST.

---

## 1. O que é o WuppieFuzz?

O [WuppieFuzz](https://github.com/TNO-S3/WuppieFuzz) é um fuzzer REST API de última geração, de código aberto, baseado em estado (*stateful*) e guiado por cobertura de código (*coverage-guided*), desenvolvido pela **TNO Software and System Security**.

### Principais Diferenciais:
1. **Compreensão de Estado:** Ao ler a especificação OpenAPI do backend, ele entende quais endpoints requerem passos anteriores (ex: garante que um processo ou subprocesso seja criado antes de tentar atualizá-lo ou ler suas transições).
2. **Mutação Inteligente:** Ele gera e muta sequências complexas de requisições HTTP para descobrir comportamentos inesperados do servidor (ex: estouros de memória, erros 500 não tratados ou desvios inesperados de autorização).
3. **Reprodução Simples:** Ao encontrar uma vulnerabilidade, o fuzzer gera logs claros e reproduzíveis descrevendo o fluxo exato de pacotes HTTP que causaram a falha.

---

## 2. Preparação do Ambiente SGC

Para rodar o fuzzer, o backend precisa estar executando em uma configuração previsível e controlada localmente. O perfil ideal é o **`e2e`**, pois ele carrega uma semente de banco de dados pré-configurada (`seed.sql`) e expõe a especificação OpenAPI de forma nativa.

### Passo 1: Subir o Backend
Execute o comando a seguir a partir da raiz do projeto SGC para iniciar o backend com o perfil `e2e` (e pulando o build do frontend para velocidade):
```bash
./gradlew :backend:bootRun -PENV=e2e -PskipFrontend=true
```

### Passo 2: Validar o OpenAPI
Verifique se a especificação da API está disponível no seu navegador:
* URL JSON: `http://localhost:10000/api-docs`
* UI do Swagger: `http://localhost:10000/swagger-ui.html`

---

## 3. Autenticação Automatizada (JWT)

A API do SGC é protegida pelo Spring Security. Como o WuppieFuzz atua batendo nos endpoints protegidos, ele precisa de um token JWT válido de autenticação. 

Fornecemos scripts utilitários que geram este token automaticamente utilizando a conta do Administrador padrão de testes do SGC (`191919`).

### No Windows (PowerShell):
Execute o script abaixo. Ele obterá o token e o copiará automaticamente para a sua área de transferência (Clipboard):
```powershell
powershell -ExecutionPolicy Bypass -File .\etc\fuzzing\obter-token.ps1
```

### No Linux / WSL / Docker:
Execute o script em bash:
```bash
chmod +x ./etc/fuzzing/obter-token.sh
./etc/fuzzing/obter-token.sh
```

---

## 4. Instalando e Executando o WuppieFuzz

O WuppieFuzz é escrito em **Rust** (construído sobre a biblioteca *LibAFL*).

### Instalação

#### Usando Homebrew (macOS / Linux / WSL):
```bash
brew install wuppiefuzz
```

#### Usando Cargo (Rust Toolchain):
Caso tenha a toolchain do Rust instalada:
```bash
cargo install wuppiefuzz
```

### Execução do Fuzzing

Com o backend rodando em `http://localhost:10000` e com o token JWT copiado, inicie a auditoria de robustez rodando o fuzzer:

```bash
wuppiefuzz -o http://localhost:10000/api-docs \
           -h "Authorization: Bearer <SEU_TOKEN_AQUI>" \
           -d ./fuzz-results
```

### Parâmetros Importantes:
* `-o`: Define a URL da especificação OpenAPI gerada pelo Springdoc.
* `-h`: Adiciona o header de autenticação JWT Bearer obtido via script.
* `-d`: Diretório onde o fuzzer depositará os relatórios, mutações e falhas descobertas.

---

## 5. Auditorias Avançadas com Cobertura de Código (*Grey-box*)

O WuppieFuzz se destaca ao usar a cobertura de código para guiar quais requisições geram novos caminhos no backend Java.

Para habilitar isso no SGC localmente:
1. Adicione o agente de cobertura **JaCoCo** na JVM ao subir o backend:
   ```bash
   # Baixe o jacocoagent.jar e configure via jvmArgs no Gradle ou javaagent na JVM
   java -javaagent:etc/jacoco/jacocoagent.jar=output=tcpserver,port=6300,address=localhost -jar backend.jar
   ```
2. Ao rodar o WuppieFuzz, aponte para a porta de escuta do JaCoCo para que ele leia a cobertura em tempo real e evolua o corpus de teste dinamicamente.
