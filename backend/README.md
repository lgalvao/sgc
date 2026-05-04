# Backend do Sistema de Gestão de Competências (SGC)

## Visão geral

Este diretório contém o código do backend do SGC. Ele fornece uma API REST robusta para o frontend, seguindo princípios
de arquitetura modular e controle de acesso rigoroso.

## 🏗️ Arquitetura e Stack

A aplicação é um sistema modular construído com:

* **Java 25**: Utilizando as últimas funcionalidades da linguagem (Pattern Matching, Sealed Classes, Record).
* **Spring Boot 4**: Framework de aplicação moderno.
* **Hibernate/JPA**: Camada de persistência com validações ricas via Bean Validation.
* **Oracle JDBC (ojdbc11)**: Driver para banco de dados de produção.
* **H2 Database**: Banco de dados em memória para desenvolvimento local e testes rápidos.
* **Gradle**: Gerenciamento de build e dependências com script Kotlin (`build.gradle.kts`).

## 📦 Estrutura de Módulos (Packages)

O código está organizado em `src/main/java/sgc/` seguindo uma divisão por domínios:

* **`processo`**: Gerencia o ciclo de vida dos processos de Mapeamento ou Revisão.
* **`subprocesso`**: Gerencia a execução das tarefas por unidade. Inclui o motor de estados e o módulo de *
  *Análise/Auditoria**.
* **`mapa`**: Núcleo do domínio de competências (Mapas, Competências, Atividades, Conhecimentos).
* **`organizacao`**: Modelagem organizacional (Usuários, Unidades, Perfis e Hierarquia).
* **`seguranca`**: Controle de acesso centralizado (`SgcPermissionEvaluator`) e autenticação JWT.
* **`alerta`**: Sistema de alertas internos e notificações reativas.
* **`relatorio`**: Geração de documentos e exportações (PDF/Excel).
* **`parametros`**: Configurações dinâmicas do sistema.
* **`comum`**: Utilitários, exceções base e componentes compartilhados.

## 🚀 Como executar

A partir da raiz do projeto, você pode usar o `bootRun` do Gradle. O sistema utiliza perfis para carregar variáveis de
ambiente de arquivos `.env.<perfil>`.

```powershell
# Execução padrão (usa application.yml)
./gradlew :backend:bootRun

# Execução em perfil específico
./gradlew :backend:bootRun -PENV=e2e
```

A API estará disponível em `http://localhost:10000`.

### Perfis principais

| Perfil  | Banco         | Actuator                                             | Uso                    |
|---------|---------------|------------------------------------------------------|------------------------|
| `local` | H2            | `health`, `info`                                     | Desenvolvimento rápido |
| `e2e`   | H2 + fixtures | `health`, `info`                                     | Testes automáticos E2E |
| `hom`   | Oracle        | `health`, `info`, `metrics`, `logfile`, `prometheus` | Homologação            |
| `prod`  | Oracle        | `health`, `info`, `metrics`, `logfile`, `prometheus` | Produção               |

Para rodar o benchmark de desempenho contra homologação usando as variáveis de `.env.hom`, use `..\benchmark-hom.ps1` no
Windows ou `../benchmark-hom.sh` no shell.

Para rodar um teste end-to-end com monitoramento e log reduzido, use `../e2e-monitorado.sh <spec_playwright>`.

No profile `hom`, administradores também podem remover um processo inteiro para limpar dados de teste via
`POST /api/processos/{codigo}/excluir-completo`.

## 🧪 Estratégia de Testes

O backend possui uma suíte rigorosa de testes para garantir 100% de cobertura na lógica de negócio.

* **Testes Unitários**: Rápidos, usam Mockito e JUnit 6.
  ```powershell
  ./gradlew :backend:unitTest
  ```
* **Testes de Integração**: Testam os fluxos de ponta a ponta (CDUs).
  ```powershell
  ./gradlew :backend:integrationTest
  ```
* **Mutation Testing**: PIT é usado para validar a qualidade da suíte de testes.
  ```powershell
  ./gradlew :backend:mutationTest
  ```

## 🔭 Observabilidade

### Spring Boot Actuator

Os endpoints de gerenciamento estão disponíveis em `/actuator` e exigem autenticação com perfil **ADMIN**.

```bash
# Saúde da aplicação com detalhes
curl -H "Authorization: Bearer <token>" http://localhost:10000/actuator/health

# Informações da build (versão, data)
curl -H "Authorization: Bearer <token>" http://localhost:10000/actuator/info

# Métricas no formato Prometheus
curl -H "Authorization: Bearer <token>" http://localhost:10000/actuator/prometheus

# Últimas linhas do log em arquivo (hom/prod)
curl -H "Authorization: Bearer <token>" http://localhost:10000/actuator/logfile
```

### Logs em Arquivo

Nos perfis `hom` e `prod`, os logs são escritos em arquivo com rotação automática:

```bash
# Ativado via variável de ambiente (já configurado no compose.hom.yaml)
LOGGING_FILE_NAME=/var/log/sgc/sgc.log
```

Política: arquivos de até 10 MB, retenção de 30 dias, cap total de 200 MB.

### Métricas Prometheus + Grafana

Ver instruções completas no [README raiz](../README.md#prometheus--grafana) e em [
`../compose.monitoring.yaml`](../compose.monitoring.yaml).

### Monitoramento de Desempenho Interno

O SGC possui um aspecto de monitoramento (`MonitoramentoAspect`) que registra chamadas lentas em Services, Repos e
Facades. Ativado via:

```yaml
sgc:
  monitoramento:
    modo: sim          # nao (padrão) | sim
    tempo-minimo-java-ms: 500
    tempo-http-lento-ms: 100
```

## 🛡️ Segurança e Controle de Acesso

O SGC implementa a **"Regra de Ouro"** de acesso através do `SgcPermissionEvaluator`:

1. **Leitura (Visualização)**: Baseada na **Hierarquia**. Usuários veem dados de sua unidade (Chefe/Servidor) ou de sua
   árvore hierárquica (Gestor).
2. **Escrita (Execução)**: Baseada na **Localização**. Apenas usuários lotados na unidade onde o Subprocesso se encontra
   no momento podem realizar alterações.

A segurança é aplicada nos Controllers via expressões:

```java
@PreAuthorize("hasPermission(#codigo, 'Subprocesso', 'EDITAR')")
```

## 📏 Convenções de Código

### Identificadores

**NUNCA** use `id`. Use sempre `codigo` tanto no Java quanto no banco de dados.

### Idioma

Todo o código, comentários e documentação deve ser em **Português brasileiro**.

### REST API

Seguimos um padrão de ações explícitas via `POST`:

* `GET /api/recurso/{codigo}`: Consulta.
* `POST /api/recurso`: Criação.
* `POST /api/recurso/{codigo}/atualizar`: Alteração.
* `POST /api/recurso/{codigo}/excluir`: Remoção.
* `POST /api/recurso/{codigo}/iniciar`: Ações de workflow.

## 📊 Dashboard de QA

Para visualizar a saúde do projeto, utilizamos o Dashboard de QA que consolida lint, testes e cobertura.

```bash
npm run qa:dashboard
```

Os resultados são gerados em `etc/qa-dashboard/latest/ultimo-resumo.md`.

---
*Para mais detalhes sobre padrões específicos, consulte o arquivo [AGENTS.md](../AGENTS.md) na raiz do projeto.*
