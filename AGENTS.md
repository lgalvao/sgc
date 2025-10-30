# Orientações para Agentes de IA

Este documento fornece instruções específicas para agentes de IA que trabalham neste repositório.

---

## 📌 Visão Geral Rápida

**Projeto:** SGC - Sistema de Gestão de Competências  
**Stack:** Java 21 + Spring Boot 3.5.7 + Vue.js 3 + TypeScript  
**Arquitetura:** Event-Driven + Domain-Driven Design  
**Testes:** JUnit + Vitest + Playwright (21 casos de uso E2E)

---

## 🚀 Iniciando Desenvolvimento

### Backend (Modo Desenvolvimento)

Use o perfil `local` para desenvolvimento rápido com H2 em memória:

```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=local'
```

**Características do perfil `local`:**
- ✅ H2 em memória (sem PostgreSQL necessário)
- ❌ NÃO carrega `data.sql` (banco vazio)
- 🔧 Ideal para desenvolvimento com dados controlados
- 🌐 API disponível em: `http://localhost:10000`
- 📚 Swagger: `http://localhost:10000/swagger-ui.html`

**Modo detached (não bloqueia terminal):**
```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=local' > backend.log 2>&1 &
```

### Frontend (Modo Desenvolvimento)

```bash
cd frontend
npm install  # Apenas na primeira vez
npm run dev
```

- 🌐 Aplicação disponível em: `http://localhost:5173`
- 🔄 Hot-reload ativo (mudanças refletidas automaticamente)

**Modo background (não bloqueia terminal):**
```bash
npm run dev > frontend.log 2>&1 &
```

---

## 📊 Perfis Spring - Escolha o Correto

O projeto tem **4 perfis distintos**. Use o apropriado para cada situação:

| Perfil | Quando Usar | Comando | Carrega data.sql? |
|--------|-------------|---------|-------------------|
| `local` | Desenvolvimento diário | `--spring.profiles.active=local` | ❌ Não |
| `e2e` | Testes E2E (Playwright) | `--spring.profiles.active=e2e` | ✅ Sim |
| `test` | Testes JUnit (automático) | (sem parâmetro) | ❌ Não |
| `default` | Produção (PostgreSQL) | (sem parâmetro) | ✅ Sim |

📖 **Detalhes completos:** Consulte [`reqs/PROFILES.md`](reqs/PROFILES.md)

---

---

## 🧪 Testes

### Testes Unitários Frontend (Vitest)

```bash
cd frontend
npm run test:unit
```

- ✅ Framework: Vitest + Vue Test Utils
- 📊 Coverage: `npm run coverage:unit`
- 🎯 Meta: Mínimo 70% de cobertura

### Testes Unitários Backend (JUnit)

```bash
./gradlew :backend:test
```

- ✅ Framework: JUnit 5 + Spring Test
- 🗄️ Banco: H2 em memória (perfil `test` automático)
- 📝 Nota: Usa `agentTest` task que filtra stack traces

### Testes E2E (Playwright) - **ATENÇÃO: Configuração Especial**

⚠️ **CRÍTICO:** Os testes E2E requerem o backend rodando **separadamente** com o perfil `e2e`.

#### Por que Backend Separado?

O Playwright (`playwright.config.ts`) gerencia apenas o **frontend** (`npm run dev`). Tentar configurar o Playwright para gerenciar ambos os servidores causa instabilidade e timeouts.

#### Procedimento Correto (4 Passos)

**1️⃣ Inicie o Backend com Perfil E2E**
```bash
./gradlew :backend:bootRun --args='--spring.profiles.active=e2e' > backend.log 2>&1 &
```

**Por que perfil `e2e`?**
- ✅ Carrega `data.sql` com usuários de teste
- ✅ H2 em memória (rápido)
- ✅ Segurança desabilitada (permite testes sem autenticação real)
- ✅ Configuração otimizada para testes

**2️⃣ Verifique se o Backend Iniciou**
```bash
# Windows (PowerShell)
Get-Content backend.log -Tail 20

# Linux/Mac
tail -f backend.log
```

Procure pela mensagem: `"Started Sgc in X.XXX seconds"`

**3️⃣ Execute os Testes E2E**
```bash
npm run test:e2e
```

Ou teste individual:
```bash
npx playwright test cdu-01.spec.ts
```

**4️⃣ Pare o Backend**
```bash
# Windows
taskkill /F /IM java.exe

# Linux/Mac
pkill java
```

#### Usuários de Teste Disponíveis (Perfil E2E)

| Usuário | Título | Senha | Perfil | Unidade |
|---------|--------|-------|--------|---------|
| Ricardo Alves | `6` | `123` | ADMIN | STIC |
| Paulo Horta | `8` | `123` | GESTOR | SEDESENV |
| Carlos Lima | `2` | `123` | CHEFE | SGP |
| Ana Souza | `1` | `123` | SERVIDOR | SESEL |
| Multi Perfil | `999999999999` | `123` | ADMIN+GESTOR | STIC |

#### Estrutura dos Testes E2E

- 📁 `e2e/` - 21 casos de uso (CDU-01 a CDU-21)
- 📁 `e2e/helpers/` - Funções auxiliares organizadas por domínio
  - `acoes/` - Ações (preencher form, clicar, etc.)
  - `verificacoes/` - Assertions organizadas
  - `navegacao/` - Navegação e rotas
  - `dados/` - Constantes de teste

📖 **Lições aprendidas:** [`licoes-aprendidas.md`](licoes-aprendidas.md) contém soluções para problemas comuns

---

---

## 🏗️ Arquitetura do Backend

### Módulos de Domínio (src/main/java/sgc/)

O backend segue **Domain-Driven Design** com módulos coesos:

| Módulo | Responsabilidade | Tipo |
|--------|------------------|------|
| `processo` | Orquestrador de processos de alto nível | Core |
| `subprocesso` | Máquina de estados e workflow por unidade | Core |
| `mapa` | Mapas de competências (CRUD + análise) | Core |
| `competencia` | Gestão de competências | Core |
| `atividade` | Atividades e conhecimentos | Core |
| `analise` | Trilha de auditoria de análises | Core |
| `notificacao` | Notificações externas (e-mail) | Reativo |
| `alerta` | Alertas internos no sistema | Reativo |
| `sgrh` | Integração com sistema de RH (mockado) | Integração |
| `unidade` | Estrutura organizacional | Modelo |
| `comum` | Exceções, entidade base, painel | Suporte |
| `util` | Utilitários gerais | Suporte |

📖 **Arquitetura detalhada:** [`backend/README.md`](backend/README.md) tem diagramas Mermaid completos

### Comunicação Reativa (Event-Driven)

O módulo `processo` publica **eventos de domínio** que são consumidos por:
- `notificacao` → Envia e-mails assíncronos
- `alerta` → Cria alertas visíveis na UI

Isso mantém os módulos **desacoplados** e permite extensão sem modificar código existente.

---

## 🔐 Estado Atual da Segurança

⚠️ **ATENÇÃO:** A segurança está em estado de transição para facilitar desenvolvimento.

**Configuração Atual:**
- `SecurityConfig.java`: Desabilitado (`@Profile("disabled-for-now")`)
- `E2eSecurityConfig.java`: Ativo para todos os perfis - **permite tudo**
- Autenticação: JWT mockado (Base64, não validado)

**Implicações:**
- ✅ **Bom para desenvolvimento e testes**
- ❌ **NÃO adequado para produção**

**Para Produção (Futuro):**
1. Implementar filtro JWT com validação real
2. Reativar `SecurityConfig` com `@Profile("!e2e")`
3. Restringir `E2eSecurityConfig` com `@Profile("e2e")`

---

## 📂 Estrutura de Diretórios

``
sgc/
├── backend/                 # API REST Spring Boot
│   ├── src/main/java/sgc/   # Código fonte (14 módulos)
│   ├── src/main/resources/  # Configs e data.sql
│   └── src/test/            # Testes JUnit
│
├── frontend/                # Aplicação Vue.js
│   ├── src/components/      # Componentes reutilizáveis
│   ├── src/views/           # Páginas
│   ├── src/stores/          # Pinia (state management)
│   ├── src/services/        # Chamadas à API
│   └── src/router/          # Vue Router
│
├── e2e/                     # Testes Playwright (21 CDUs)
│   ├── cdu-01.spec.ts       # Login e autenticação
│   ├── cdu-02.spec.ts       # Criar processo
│   ├── ...                  # CDU-03 a CDU-21
│   └── helpers/             # Funções auxiliares organizadas
│
├── reqs/                    # Requisitos e documentação
│   ├── cdu-01.md            # Caso de uso 01
│   ├── ...                  # CDU-02 a CDU-21
│   ├── PROFILES.md          # Guia completo de perfis
│   └── _informacoes-gerais.md
│
├── README.md                # Documentação principal
├── AGENTS.md                # Este arquivo
└── licoes-aprendidas.md         # Lições aprendidas (E2E)
``

---

## 🛠️ Ferramentas e Comandos Úteis

### Build e Verificação

``bash
# Build completo (backend + frontend)
./gradlew build

# Apenas backend
./gradlew :backend:build

# Limpar builds
./gradlew clean
``

### Lint e Formatação

``bash
# Frontend (ESLint)
cd frontend
npm run lint

# TypeScript check
npm run typecheck
``

### Coverage de Testes

``bash
# Frontend
cd frontend
npm run coverage:unit

# Backend (via Gradle)
./gradlew :backend:test jacocoTestReport
``

### Documentação da API

``bash
# Inicie o backend e acesse:
# http://localhost:10000/swagger-ui.html
# http://localhost:10000/api-docs (JSON)
``

---

## 📚 Documentação de Referência

### Essencial
- 📖 **[README.md](README.md)** - Visão geral completa do projeto
- 📊 **[reqs/PROFILES.md](reqs/PROFILES.md)** - Guia detalhado dos 4 perfis Spring
- 🎓 **[licoes-aprendidas.md](licoes-aprendidas.md)** - Lições aprendidas ao corrigir testes E2E

### Backend
- 🏗️ **[backend/README.md](backend/README.md)** - Arquitetura com diagramas Mermaid
- 📐 **[reqs/_informacoes-gerais.md](reqs/_informacoes-gerais.md)** - Conceitos de negócio

### Casos de Uso
- 📝 **[reqs/cdu-01.md](reqs/cdu-01.md)** a **[reqs/cdu-21.md](reqs/cdu-21.md)** - 21 casos de uso documentados

---

## ⚡ Dicas para Agentes de IA

### 1. **Sempre Use o Perfil Correto**
- Desenvolvimento? → `local`
- Testes E2E? → `e2e`
- Nunca rode E2E sem o perfil correto (faltarão dados)

### 2. **Timeouts em Testes E2E**
- Use **mínimo 15s** para navegações
- Use **mínimo 15s** para elementos aparecerem
- Não use os 5s padrão do Playwright

### 3. **Dados de Teste**
- Perfil `e2e`: dados em `backend/src/main/resources/data.sql`
- Perfil `test`: dados em `backend/src/test/resources/*.sql`
- Perfil `local`: sem dados (crie via API)

### 4. **Segurança Desabilitada**
- Todos os endpoints `/api/**` estão abertos
- Não há validação JWT real
- Isso é **temporário** para desenvolvimento

### 5. **Estrutura de Helpers E2E**
Os helpers estão organizados por domínio:
``typescript
// Importar de e2e/helpers/index.ts
import {
  navegarParaLogin,      // helpers/navegacao
  preencherFormulario,   // helpers/acoes
  verificarPainel        // helpers/verificacoes
} from './helpers';
``

### 6. **Consulte Lições Aprendidas**
Antes de corrigir testes E2E, leia [`licoes-aprendidas.md`](licoes-aprendidas.md) para evitar problemas conhecidos:
- Ordem de inicialização do banco
- Conflitos de beans Spring Security
- Timeouts insuficientes
- Backend em modo detached

---

## 🐛 Troubleshooting Comum

### Backend não inicia
``bash
# Verifique se a porta 10000 está livre
netstat -ano | findstr :10000  # Windows
lsof -i :10000                 # Linux/Mac
``

### Testes E2E falhando com "Usuário não encontrado"
``bash
# Verifique o perfil (deve ser 'e2e')
# Verifique o log do backend
Get-Content backend.log | Select-String "data.sql"
``

### Conflito de beans SecurityFilterChain
``bash
# Apenas UM SecurityConfig pode estar ativo
# Verifique os @Profile em:
# - SecurityConfig.java (deve ser "disabled-for-now")
# - E2eSecurityConfig.java (sem @Profile ou com "e2e")
``

### Frontend não conecta no backend
``bash
# Verifique se o backend está rodando em localhost:10000
curl http://localhost:10000/actuator/health
``

---

## 📞 Precisa de Ajuda?

1. **Consulte a documentação** listada acima
2. **Verifique issues conhecidas** em `licoes-aprendidas.md`
3. **Leia o README do backend** para entender a arquitetura
4. **Consulte PROFILES.md** para problemas de configuração

---

**Última atualização:** 2025-10-30  
**Versão do projeto:** 1.0.0
