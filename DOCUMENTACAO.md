# 📚 Guia de Documentação - SGC

Este documento serve como índice centralizado de toda a documentação técnica do Sistema de Gestão de Competências (SGC).

---

## 🚀 Início Rápido

**Novo no projeto?** Leia nesta ordem:

1. **[README.md](README.md)** - Visão geral, stack tecnológico e como executar
2. **[AGENTS.md](AGENTS.md)** - ⭐ **LEITURA OBRIGATÓRIA**: Convenções, padrões e regras fundamentais
3. **[STATUS.md](STATUS.md)** - 📊 Status atual de implementação e trabalho em andamento
4. **[backend/README.md](backend/README.md)** - Arquitetura detalhada do backend
5. **[frontend/README.md](frontend/README.md)** - Arquitetura detalhada do frontend

---

## 📖 Documentação por Categoria

#### ADRs Implementados

| ADR                                                                       | Título                         | Status         |
|---------------------------------------------------------------------------|--------------------------------|----------------|
| [ADR-001](backend/etc/docs/adr/ADR-001-facade-pattern.md)                 | Facade Pattern                 | ✅ Implementado |
| [ADR-002](backend/etc/docs/adr/ADR-002-unified-events.md)                 | Unified Events Pattern         | ✅ Implementado |
| [ADR-003](backend/etc/docs/adr/ADR-003-security-architecture.md)          | Security Architecture          | ✅ Implementado |
| [ADR-004](backend/etc/docs/adr/ADR-004-dto-pattern.md)                    | DTO Pattern                    | ✅ Implementado |
| [ADR-005](backend/etc/docs/adr/ADR-005-controller-organization.md)        | Controller Organization        | ✅ Implementado |
| [ADR-006](backend/etc/docs/adr/ADR-006-domain-aggregates-organization.md) | Domain Aggregates Organization | ✅ Aceito       |
| [ADR-007](backend/etc/docs/adr/ADR-007-circular-dependency-resolution.md) | Circular Dependency Resolution | ✅ Implementado |

---

### 📊 Gerenciamento de Projeto

| Documento                                           | Descrição                                      |
|-----------------------------------------------------|------------------------------------------------|
| **[STATUS.md](STATUS.md)**                          | Status atual de implementação e rastreamento   |
| **[test-organization-tracking.md](test-organization-tracking.md)** | Acompanhamento da reorganização de testes      |
| **[test-organization-plan.md](test-organization-plan.md)**         | Plano de reorganização de testes               |
| **[test-organization-report.md](test-organization-report.md)**     | Relatório da reorganização de testes           |

---

### 🎨 Padrões de Código

#### Backend (Java / Spring Boot)

| Documento                                                           | Descrição                                             |
|---------------------------------------------------------------------|-------------------------------------------------------|
| **[backend-padroes.md](backend/etc/regras/backend-padroes.md)**     | Padrões arquiteturais, nomenclatura e design patterns |
| **[guia-dtos.md](backend/etc/regras/guia-dtos.md)**                 | Taxonomia e regras para criação de DTOs               |
| **[guia-validacao.md](backend/etc/regras/guia-validacao.md)**       | Regras de validação com Bean Validation               |
| **[guia-excecoes.md](backend/etc/regras/guia-excecoes.md)**         | Tratamento de exceções e erros                        |
| **[guia-testes-junit.md](backend/etc/regras/guia-testes-junit.md)** | Como criar testes unitários e de integração           |
| **[GUIA-JAVADOC-EXCECOES.md](backend/GUIA-JAVADOC-EXCECOES.md)**    | Documentação de exceções em JavaDoc                   |
| **[GUIA-MELHORIAS-TESTES.md](backend/GUIA-MELHORIAS-TESTES.md)**    | Guia de melhorias de qualidade de testes              |

#### Frontend (Vue / TypeScript)

| Documento                                                            | Descrição                                     |
|----------------------------------------------------------------------|-----------------------------------------------|
| **[frontend-padroes.md](frontend/etc/regras/frontend-padroes.md)**   | Padrões Vue 3, componentes, stores, idioma    |
| **[frontend-testes.md](frontend/etc/regras/frontend-testes.md)**     | Testes Vitest e estratégias                   |
| **[guia-correcao-e2e.md](frontend/etc/regras/guia-correcao-e2e.md)** | Como corrigir e manter testes Playwright      |
| **[ESTRATEGIA-ERROS.md](frontend/ESTRATEGIA-ERROS.md)**              | Estratégia unificada de tratamento de erros   |
| **[GUIA-COMPOSABLES.md](frontend/GUIA-COMPOSABLES.md)**              | Como extrair lógica de views para composables |
| **[design-guidelines.md](frontend/design-guidelines.md)**            | Diretrizes de design e UX                     |

---

### 🧪 Testes

| Documento                                                                                          | Descrição                                    |
|----------------------------------------------------------------------------------------------------|----------------------------------------------|
| **[test-coverage-plan.md](test-coverage-plan.md)** ⭐                                               | Plano completo de restauração de cobertura de testes |
| **[coverage-tracking.md](coverage-tracking.md)** ⭐                                                 | Rastreamento de progresso de cobertura      |
| **[GUIA-MELHORIAS-TESTES.md](backend/etc/docs/GUIA-MELHORIAS-TESTES.md)**                         | Guia de melhorias de qualidade de testes     |
| **[e2e/README.md](e2e/README.md)**                                                                 | Documentação dos testes E2E com Playwright   |
| **[e2e/FIXTURES-MIGRATION-GUIDE.md](e2e/FIXTURES-MIGRATION-GUIDE.md)**                             | Guia de migração para fixtures reutilizáveis |
| **[backend/src/test/java/sgc/testutils/README.md](backend/src/test/java/sgc/testutils/README.md)** | Utilitários de teste do backend              |
| **[frontend/src/test-utils/README.md](frontend/src/test-utils/README.md)**                         | Utilitários de teste do frontend             |

---

### 📊 Diagramas

| Documento                                                                                   | Descrição                                 |
|---------------------------------------------------------------------------------------------|-------------------------------------------|
| **[diagramas-arquitetura.md](backend/etc/docs/diagramas-arquitetura.md)**                   | Diagramas de comunicação entre módulos    |
| **[diagramas-servicos-subprocesso.md](backend/etc/docs/diagramas-servicos-subprocesso.md)** | Diagramas de fluxo do domínio Subprocesso |

---

### 🤖 Agente e Skills (Jules)

| Documento                                                               | Descrição                                        |
|-------------------------------------------------------------------------|--------------------------------------------------|
| **[audit-qualidade-backend.md](.jules/skills/audit-qualidade-backend.md)**   | Skill para auditoria de qualidade no backend     |
| **[audit-qualidade-frontend.md](.jules/skills/audit-qualidade-frontend.md)** | Skill para auditoria de qualidade no frontend    |
| **[gestao-testes.md](.jules/skills/gestao-testes.md)**                       | Skill para análise e priorização de testes       |
| **[utilitarios.md](.jules/skills/utilitarios.md)**                           | Skill para ferramentas de ambiente e utilitários |

---

### 📦 Documentação de Módulos

#### Backend

Cada módulo possui um README.md detalhando suas responsabilidades:

- [processo/](backend/src/main/java/sgc/processo/README.md) - Orquestrador central de fluxos
- [subprocesso/](backend/src/main/java/sgc/subprocesso/README.md) - Máquina de estados de tarefas
- [mapa/](backend/src/main/java/sgc/mapa/README.md) - Núcleo do domínio (Mapas, Competências)
- [organizacao/](backend/src/main/java/sgc/organizacao/README.md) - Estrutura organizacional
- [alerta/](backend/src/main/java/sgc/alerta/README.md) - Sistema de alertas
- [notificacao/](backend/src/main/java/sgc/notificacao/README.md) - Sistema de notificações
- [analise/](backend/src/main/java/sgc/analise/README.md) - Auditoria e histórico
- [painel/](backend/src/main/java/sgc/painel/README.md) - Dashboard e métricas
- [comum/](backend/src/main/java/sgc/comum/README.md) - Componentes compartilhados
- [e2e/](backend/src/main/java/sgc/e2e/README.md) - Endpoints auxiliares para testes

#### Frontend

- [components/](frontend/src/components/README.md) - Componentes reutilizáveis
- [stores/](frontend/src/stores/README.md) - Gerenciamento de estado (Pinia)
- [services/](frontend/src/services/README.md) - Camada de serviços HTTP
- [views/](frontend/src/views/README.md) - Páginas da aplicação
- [router/](frontend/src/router/README.md) - Configuração de rotas
- [composables/](frontend/src/composables/README.md) - Lógica reutilizável
- [mappers/](frontend/src/mappers/README.md) - Transformação de dados
- [constants/](frontend/src/constants/README.md) - Constantes da aplicação

---

## 🎯 Navegação Rápida por Contexto

### "Preciso implementar uma nova feature"

1. Identifique o módulo afetado (processo, subprocesso, mapa, etc)
2. Leia o README.md do módulo
3. Consulte os padrões relevantes:
   - Backend: [backend-padroes.md](backend/etc/regras/backend-padroes.md), [guia-dtos.md](backend/etc/regras/guia-dtos.md)
   - Frontend: [frontend-padroes.md](frontend/etc/regras/frontend-padroes.md)
4. Revise os ADRs relacionados

### "Preciso corrigir um bug"

1. Identifique a camada (Controller, Service, Repository, View, Store)
2. Consulte os padrões da camada
3. Execute os testes relacionados antes e depois da correção
4. Para bugs de E2E: [guia-correcao-e2e.md](frontend/etc/regras/guia-correcao-e2e.md)

### "Preciso adicionar testes"

1. Backend: [guia-testes-junit.md](backend/etc/regras/guia-testes-junit.md), [GUIA-MELHORIAS-TESTES.md](backend/GUIA-MELHORIAS-TESTES.md)
2. Frontend: [frontend-testes.md](frontend/etc/regras/frontend-testes.md)
3. E2E: [e2e/README.md](e2e/README.md), [FIXTURES-MIGRATION-GUIDE.md](e2e/FIXTURES-MIGRATION-GUIDE.md)

### "Preciso entender a segurança/controle de acesso"

1. [SECURITY-REFACTORING-COMPLETE.md](backend/etc/docs/SECURITY-REFACTORING-COMPLETE.md)
2. [ADR-003](backend/etc/docs/adr/ADR-003-security-architecture.md)

### "Preciso entender a arquitetura geral"

1. [diagramas-arquitetura.md](backend/etc/docs/diagramas-arquitetura.md)
2. Todos os [ADRs](backend/etc/docs/adr/)

---

## 🤖 Documentação para Agentes de IA

Se você é um agente de IA trabalhando neste projeto:

1. **SEMPRE** leia **[AGENTS.md](AGENTS.md)** primeiro
2. Consulte este índice (DOCUMENTACAO.md) para navegação
3. Siga estritamente os padrões documentados
4. Use idioma **Português Brasileiro** em todo código e documentação

---

## 📝 Contribuindo com a Documentação

Ao criar ou atualizar documentação:

- ✅ Use idioma **Português Brasileiro**
- ✅ Adicione link neste índice se for um documento novo
- ✅ Mantenha exemplos práticos e código
- ✅ Atualize links quando mover arquivos
- ✅ Use Markdown com formatação consistente
- ✅ Inclua tabela de conteúdo em documentos longos
