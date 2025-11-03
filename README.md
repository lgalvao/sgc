# SGC - Sistema de Gestão de Competências

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.5-green.svg)](https://vuejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.9-blue.svg)](https://www.typescriptlang.org/)
[![Playwright](https://img.shields.io/badge/Playwright-E2E%20Tests-45ba4b.svg)](https://playwright.dev/)

Sistema para gerenciar sistematicamente as competências técnicas das unidades organizacionais do TRE-PE, incluindo mapeamento, revisão e diagnóstico de competências.

---

## 📋 Visão Geral

O SGC permite:

- **Mapeamento de Competências**: Coleta sistemática de atividades e conhecimentos de cada unidade operacional
- **Revisão Periódica**: Atualização dos mapas de competencias
- **Diagnóstico**: Avaliação de importância e domínio das competências, identificando gaps de capacitação
- **Gestão de Processos**: Workflow completo com máquina de estados e trilha de auditoria
- **Notificações**: Alertas visuais e notificações por e-mail sobre evolucoes nos processos e subprocessos

---

## 🏗️ Arquitetura

### Stack Tecnológico

**Backend:**
- Java 21
- Spring Boot 3.5.7
- JPA/Hibernate
- PostgreSQL (produção) / H2 (desenvolvimento e testes)
- Arquitetura: Em camadas, estruturada por domínio

**Frontend:**
- Vue.js 3.5 + TypeScript
- Vite (build)
- Pinia (estado)
- Bootstrap 5
- Axios (cliente http)

**Testes:**
- JUnit 5 (testes unitários do backend)
- Vitest (testes unitários do frontend)
- Playwright (testes E2E - organizados por casos de uso)

### Estrutura do Projeto

```
sgc/
├── backend/              # API REST Spring Boot
│   ├── src/main/java/sgc/
│   │   ├── processo/     # Principal conceito do sistema, de onde partem todos os fluxos.
│   │   ├── subprocesso/  # Cada unidade envolvida em um processo tem o seu subprocesso
│   │   ├── mapa/         # Mapas de competências
│   │   ├── competencia/  # Gestão de competências
│   │   ├── atividade/    # Atividades e conhecimentos
│   │   ├── analise/      # Trilha de auditoria
│   │   ├── notificacao/  # Sistema de notificações
│   │   ├── alerta/       # Alertas internos
│   │   ├── sgrh/         # Integração com RH
│   │   ├── unidade/      # Estrutura organizacional
│   │   └── comum/        # Componentes compartilhados
│   └── src/main/resources/
│       ├── application.yml         # Config padrão (PostgreSQL)
│       ├── application-local.yml   # Desenvolvimento (H2)
│       ├── application-e2e.yml     # Testes E2E (H2 + data)
│       └── data.sql                # Dados iniciais para testes
│
├── frontend/             # Aplicação Vue.js
│   ├── src/
│   │   ├── components/   # Componentes reutilizáveis
│   │   ├── views/        # Páginas da aplicação
│   │   ├── stores/       # Pinia stores
│   │   ├── services/     # Serviços de API
│   │   ├── router/       # Vue Router
│   │   └── types/        # Tipo TypeScript
│   └── build/            # Build artifacts
│
├── e2e/                  # Testes End-to-End (Playwright)
│   ├── cdu-01.spec.ts    # Login e autenticação
│   ├── cdu-02.spec.ts    # Criar processo
│   ├── ...               # 21 casos de uso
│   ├── helpers/          # Funções auxiliares
│   │   ├── acoes/        # Ações por domínio
│   │   ├── verificacoes/ # Verificações por domínio
│   │   ├── navegacao/    # Navegação e rotas
│   │   └── dados/        # Constantes de testes
│   └── support/          # Configurações de testes
│
├── reqs/                 # Documentação de requisitos
│   ├── cdu-01.md         # Caso de uso 01: Login
│   ├── cdu-02.md         # Caso de uso 02: Criar processo
│   ├── ...               # Ao toodo, 21 casos de uso documentados
│   ├── PROFILES.md       # Guia de perfis Spring
│   └── _informacoes-gerais.md
│
├── build.gradle.kts      # Build raiz (multi-projeto)
├── package.json          # Scripts E2E
├── playwright.config.ts  # Configuração Playwright
├── AGENTS.md             # Guia para agentes de IA
└── licoes-aprendidas.md  # Lições aprendidas (ao corrigir os testes E2E)
```

---

## 🚀 Quick Start

### Pré-requisitos

- **Java 21** (OpenJDK ou Oracle JDK)
- **Node.js 18+** e npm
- **PostgreSQL 14+** (apenas para produção)

### 1. Clone o Repositório

```bash
git clone https://github.com/lgalvao/sgc.git
cd sgc
```

### 2. Desenvolvimento Local (Recomendado)

#### Terminal 1: Backend
```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=local'
```
- Usa H2 em memória (sem PostgreSQL necessário)
- API disponível em: `http://localhost:10000`
- Swagger UI: `http://localhost:10000/swagger-ui.html`

#### Terminal 2: Frontend
```bash
cd frontend
npm install
npm run dev
```
- Aplicação disponível em: `http://localhost:5173`

### 3. Acesso ao Sistema

**Usuários de teste (perfil `local` ou `e2e`):**

| Usuário | Título | Senha | Perfil | Unidade |
|---------|--------|-------|--------|---------|
| Ricardo Alves | `6` | `123` | ADMIN | STIC |
| Paulo Horta | `8` | `123` | GESTOR | SEDESENV |
| Carlos Henrique Lima | `2` | `123` | CHEFE | SGP |
| Ana Paula Souza | `1` | `123` | SERVIDOR | SESEL |
| Usuario Multi Perfil | `999999999999` | `123` | ADMIN + GESTOR | STIC |

---

## 🧪 Testes

### Testes Unitários Backend (JUnit)
```bash
./gradlew :backend:test
```
- Usa perfil `test` automaticamente
- Banco H2 em memória (limpo a cada teste)

### Testes Unitários Frontend (Vitest)
```bash
cd frontend
npm run test:unit
```

### Testes E2E (Playwright) - 21 Casos de Uso

⚠️ **Importante:** Testes E2E exigem backend rodando separadamente.

**Passo 1:** Inicie o backend com perfil `e2e`
```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=e2e' > backend.log 2>&1 &
```

**Passo 2:** Aguarde o backend iniciar (verificar log)
```bash
tail -f backend.log
# Procure por "Started Sgc"
```

**Passo 3:** Execute os testes E2E
```bash
npm run test:e2e
```

**Passo 4:** Pare o backend
```bash
# No Windows
taskkill /F /IM java.exe

# No Linux/Mac
pkill java
```

**Casos de uso testados:**
- CDU-01: Login e seleção de perfil
- CDU-02: Criação de processo
- CDU-03 a CDU-21: Workflow completo de mapeamento e validação

---

## 📊 Perfis Spring

O projeto usa **4 perfis distintos**:

| Perfil | Quando Usar | Banco | Carrega data.sql? | Porta |
|--------|-------------|-------|-------------------|-------|
| **default** | Produção/Homologação | PostgreSQL | ✅ | 10000 |
| **local** | Desenvolvimento diário | H2 | ❌ | 10000 |
| **e2e** | Testes E2E (Playwright) | H2 | ✅ | 10000 |
| **test** | Testes JUnit (auto) | H2 | ❌ | N/A |

📖 **Guia completo:** [`reqs/PROFILES.md`](reqs/PROFILES.md)

### Comandos por Perfil

```bash
# Produção (PostgreSQL)
./gradlew :backend:bootRun

# Desenvolvimento (H2, sem dados)
./gradlew :backend:bootRun --args='--spring.profiles.active=local'

# Testes E2E (H2 + dados de teste)
./gradlew :backend:bootRun --args='--spring.profiles.active=e2e'

# Testes JUnit (automático)
./gradlew :backend:test
```

---

## 📐 Domínios de Negócio

### 1. Processo (Orquestrador)
Gerencia o ciclo de vida dos processos de alto nível (Mapeamento, Revisão, Diagnóstico). Publica eventos de domínio para desacoplar módulos.

### 2. Subprocesso (Máquina de Estados)
Gerencia o workflow detalhado de cada unidade organizacional com transições de estado e histórico imutável de movimentações.

**Estados principais:**
- `PENDENTE_CADASTRO` → `CADASTRO_DISPONIBILIZADO` → `EM_ANALISE_GESTOR`
- `MAPA_AJUSTADO` → `EM_ANALISE_ADMIN` → `HOMOLOGADO`

### 3. Mapa de Competências
Orquestra criação, cópia e análise de impacto dos mapas. Cada mapa está vinculado a uma unidade e pode ter diferentes situações (ATIVO, ARQUIVADO, etc.).

### 4. Competências, Atividades e Conhecimentos
- **Competência**: Elemento sintetizante (ex: "Desenvolvimento de Software")
- **Atividade**: Ação específica (ex: "Desenvolver APIs REST")
- **Conhecimento**: Saber técnico necessário (ex: "Spring Boot")

### 5. Notificações e Alertas (Reativos)
Sistema orientado a eventos que reage aos eventos de domínio:
- **Alertas**: Visíveis na interface do usuário
- **Notificações**: E-mails assíncronos

---

## 🔐 Segurança

⚠️ **Estado Atual:** Segurança em transição (desenvolvimento)

- `SecurityConfig.java`: Temporariamente desabilitado (`@Profile("disabled-for-now")`)
- `E2eSecurityConfig.java`: Ativo - permite todas as requisições
- Autenticação JWT mockada (token Base64 não validado)

**Para produção:**
1. Implementar filtro JWT com validação real
2. Reativar `SecurityConfig` com `@Profile("!e2e")`
3. Restringir `E2eSecurityConfig` com `@Profile("e2e")`

---

## 🛠️ Build e Deploy

### Build Completo (Backend + Frontend)

```bash
# Instala dependências do frontend
./gradlew installFrontend

# Compila frontend (Vite)
./gradlew buildFrontend

# Copia build do frontend para resources/static
./gradlew copyFrontend

# Compila backend + embedded frontend
./gradlew :backend:build
```

O JAR resultante estará em: `backend/build/libs/backend-1.0.0.jar`

### Executar JAR em Produção

```bash
java -jar backend/build/libs/backend-1.0.0.jar
```

O backend servirá tanto a API REST (`/api/**`) quanto o frontend estático (`/`).

---

## 📚 Documentação Adicional

- **[AGENTS.md](AGENTS.md)**: Guia para agentes de IA trabalhando no projeto
- **[reqs/PROFILES.md](reqs/PROFILES.md)**: Detalhes completos dos perfis Spring
- **[licoes-aprendidas.md](licoes-aprendidas.md)**: Lições aprendidas ao corrigir testes E2E
- **[backend/README.md](backend/README.md)**: Arquitetura detalhada do backend com diagramas Mermaid
- **[reqs/](reqs/)**: 21 casos de uso documentados (CDU-01 a CDU-21)

### Swagger API
```
http://localhost:10000/swagger-ui.html
http://localhost:10000/api-docs
```

---

## 🤝 Contribuindo

### Estrutura de Commits
- `feat:` Nova funcionalidade
- `fix:` Correção de bug
- `test:` Adição ou correção de testes
- `docs:` Documentação
- `refactor:` Refatoração de código
- `chore:` Tarefas de manutenção

### Workflow de Desenvolvimento

1. **Crie uma branch** do `main`
2. **Desenvolva** com testes
3. **Execute testes locais**:
   ```bash
   ./gradlew :backend:test
   cd frontend && npm run test:unit
   npm run test:e2e
   ```
4. **Commit** com mensagem descritiva
5. **Push** e abra Pull Request

### Regras de Testes

- ✅ Backend: Testes unitários obrigatórios para serviços
- ✅ Frontend: Cobertura mínima de 70% (Vitest)
- ✅ E2E: Todos os 21 casos de uso devem passar antes de merge

---

## 📝 Licença

Este projeto é propriedade do TRE-PE (Tribunal Regional Eleitoral de Pernambuco).

---

## 👥 Autores

- **Leonardo Galvão** - Desenvolvimento inicial
- **Equipe SEDESENV/COSIS/TRE-PE**

---

## 📞 Suporte

Para dúvidas ou problemas:
1. Consulte a [documentação](reqs/)
2. Verifique issues abertas no repositório
3. Contate a equipe de desenvolvimento SEDESENV

---

## 🔄 Estado do Projeto

- ✅ Backend: Arquitetura completa e funcional
- ✅ Frontend: Interface responsiva com Bootstrap 5
- ✅ Testes E2E: 21 casos de uso automatizados
- ⚠️ Segurança: Em implementação (JWT real pendente)
- 🚧 Integração SGRH: Simulada (produção pendente)

**Versão:** 1.0.0
**Última atualização:** 2025-10-30
