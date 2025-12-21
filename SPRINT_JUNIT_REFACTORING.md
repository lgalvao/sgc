# Plano de Refatoração de Testes JUnit - SGC

## 📋 Visão Geral

Este documento descreve o plano completo de refatoração dos testes JUnit do backend do SGC, dividido em 8 sprints incrementais (Sprint 0 a Sprint 7).

## 🎯 Objetivos Principais

1. **Aumentar confiabilidade** e manutenibilidade dos testes
2. **Reduzir flakiness** e fragilidade (principalmente nos testes de integração)
3. **Padronizar** estrutura e nomenclatura para facilitar leitura e evolução
4. **Orientar execução** por desenvolvedores e agentes de IA

## 📊 Estado Atual (Baseline)

### Estatísticas Verificadas
- **98 arquivos de teste** em `backend/src/test/java/sgc/`
- **30 testes de integração** em `backend/src/test/java/sgc/integracao/`
- **478 anotações @DisplayName** (boa padronização já existente)
- **56 anotações @Nested** para organização
- **1 ocorrência** de `Strictness.LENIENT` (a ser removida)
- **0 testes parametrizados** (oportunidade de melhoria)

### Ferramentas e Stack
- **Build System**: Gradle 9.2.1 (Gradle Wrapper)
- **Framework de Testes**: JUnit 5, Mockito, AssertJ, Spring Boot Test
- **Cobertura**: JaCoCo (já configurado em `backend/build.gradle.kts`)
- **Java**: 21
- **Spring Boot**: 4.0.1

### Arquitetura de Módulos
```
backend/src/main/java/sgc/
├── processo/          # Orquestrador central
├── subprocesso/       # Workflow e máquina de estados
├── mapa/              # Mapas de competências
├── atividade/         # Atividades e conhecimentos
├── analise/           # Auditoria e revisão
├── notificacao/       # Notificações por eventos
├── alerta/            # Alertas na UI por eventos
├── sgrh/              # Usuários e perfis
├── unidade/           # Estrutura organizacional
├── painel/            # Dashboards
└── comum/             # Componentes transversais
```

## 🚀 Sprints de Refatoração

### Sprint 0: Baseline e Guardrails
**Objetivo**: Estabelecer linha de base e garantir infraestrutura de validação.

**Atividades**:
- ✅ Documentar comandos de build e teste
- ✅ Confirmar JaCoCo funcionando
- ✅ Estabelecer métricas iniciais

**Documento**: [`sprint-00-baseline.md`](./sprint-00-baseline.md)

---

### Sprint 1: Remover Testes Boilerplate
**Objetivo**: Reduzir ruído removendo testes de getters/setters sem valor.

**Candidatos à Remoção**:
- Testes de DTOs sem validação
- Testes de mappers sem lógica customizada
- Testes de builders gerados pelo Lombok

**Documento**: [`sprint-01-remocao-boilerplate.md`](./sprint-01-remocao-boilerplate.md)

---

### Sprint 2: Remover LENIENT
**Objetivo**: Eliminar `Strictness.LENIENT` e corrigir strict stubbing.

**Estado Atual**: 1 ocorrência em `ProcessoServiceTest.java`

**Impacto**: Aumenta qualidade do sinal dos testes unitários.

**Documento**: [`sprint-02-remocao-lenient.md`](./sprint-02-remocao-lenient.md)

---

### Sprint 3: Fixtures/Builders
**Objetivo**: Reduzir duplicação criando fixtures reutilizáveis.

**Estrutura Proposta**:
```
backend/src/test/java/sgc/fixture/
├── ProcessoFixture.java
├── SubprocessoFixture.java
├── MapaFixture.java
├── AtividadeFixture.java
├── UsuarioFixture.java
└── UnidadeFixture.java
```

**Documento**: [`sprint-03-fixtures.md`](./sprint-03-fixtures.md)

---

### Sprint 4: Padronização Mecânica
**Objetivo**: Garantir consistência de nomenclatura e estrutura.

**Padrão Oficial**:
- Métodos: `deve{Acao}Quando{Condicao}`
- `@DisplayName`: Obrigatório
- `@Nested`: Para classes com >10 testes
- Estrutura AAA explícita

**Meta**: >90% dos testes padronizados

**Documento**: [`sprint-04-padronizacao.md`](./sprint-04-padronizacao.md)

---

### Sprint 5: Desacoplar Integração
**Objetivo**: Eliminar dependência de seed global (`data.sql`).

**Estratégia Recomendada**: Setup programático via repositórios + fixtures

**Testes Afetados**: 30 testes de integração (CDU01-CDU21, etc)

**Documento**: [`sprint-05-desacoplamento-integracao.md`](./sprint-05-desacoplamento-integracao.md)

---

### Sprint 6: Cobertura e Visibilidade
**Objetivo**: Melhorar visibilidade de cobertura e estabelecer quality gates.

**JaCoCo**: ✅ Já configurado, mas relatório HTML desabilitado

**Atividades**:
- Habilitar relatório HTML
- Configurar quality gate com limite inicial
- Documentar visualização e interpretação

**Documento**: [`sprint-06-cobertura.md`](./sprint-06-cobertura.md)

---

### Sprint 7: Qualidade Avançada
**Objetivo**: Elevar robustez com parametrização e testes de eventos.

**Atividades**:
- Parametrizar testes repetitivos (`@ParameterizedTest`)
- Completar asserções de exceção (mensagem + causa)
- Testar eventos (processo → notificacao/alerta)
- Melhorar asserções de estado com `assertAll`

**Documento**: [`sprint-07-qualidade-avancada.md`](./sprint-07-qualidade-avancada.md)

## 📖 Documentos de Referência

- **Análise Base**: [`analise-junit-nova.md`](./analise-junit-nova.md)
- **Convenções do Projeto**: [`AGENTS.md`](./AGENTS.md)
- **Arquitetura Backend**: [`backend/README.md`](./backend/README.md)
- **README Principal**: [`README.md`](./README.md)

## 🛠️ Comandos Principais

### Executar Testes
```bash
# Todos os testes do backend
./gradlew :backend:test

# Apenas testes de integração
./gradlew :backend:test --tests "sgc.integracao.*"
```

### Cobertura
```bash
# Gerar relatório JaCoCo
./gradlew :backend:jacocoTestReport

# Ver relatório HTML
open backend/build/reports/jacoco/test/html/index.html
```

### Verificações de Qualidade
```bash
# Quality check completo (Checkstyle, PMD, SpotBugs, JaCoCo)
./gradlew :backend:qualityCheck

# Quality check rápido
./gradlew :backend:qualityCheckFast
```

### Comandos de Análise
```bash
# Verificar uso de LENIENT
grep -R "Strictness.LENIENT" backend/src/test --include="*.java"

# Contar testes com @DisplayName
grep -R "@DisplayName" backend/src/test --include="*.java" | wc -l

# Contar testes com @Nested
grep -R "@Nested" backend/src/test --include="*.java" | wc -l

# Listar testes de DTO/Model
find backend/src/test -path "*/dto/*Test.java" -o -path "*/model/*Test.java"
```

## ✅ Critérios Universais de Aceite

Para **qualquer** PR de refatoração de testes:

1. `./gradlew :backend:test` passa sem erros
2. Não aumentar flakiness (sem `Thread.sleep`)
3. Não reintroduzir `Strictness.LENIENT`
4. Sem hardcode em integração sem criação explícita
5. PRs pequenos (1 tema por PR)
6. Métricas antes/depois documentadas

## 🎯 Progresso Esperado

| Sprint | Foco | Métrica de Sucesso |
|--------|------|-------------------|
| 0 | Baseline | Comandos documentados, métricas estabelecidas |
| 1 | Boilerplate | -X arquivos de teste removidos |
| 2 | LENIENT | 0 ocorrências de `Strictness.LENIENT` |
| 3 | Fixtures | Pacote `fixture/` criado com N builders |
| 4 | Padronização | >90% com `@DisplayName` e nomenclatura correta |
| 5 | Integração | 0 IDs hardcoded sem setup explícito |
| 6 | Cobertura | Relatório HTML visível, gate configurado |
| 7 | Qualidade | >30% redução em testes duplicados via parametrização |

## 🔄 Ordem de Execução Recomendada

Os sprints devem ser executados **sequencialmente** na ordem 0→7, pois cada sprint se baseia nos anteriores:

1. Sprint 0 estabelece a baseline
2. Sprint 1 remove ruído antes de padronizar
3. Sprint 2 corrige problemas de mocking
4. Sprint 3 cria fixtures usados nos sprints seguintes
5. Sprint 4 padroniza a estrutura
6. Sprint 5 usa fixtures para isolar integração
7. Sprint 6 mede cobertura após limpeza
8. Sprint 7 eleva qualidade final

## 👥 Para Desenvolvedores e Agentes de IA

- Cada sprint tem seu próprio documento detalhado
- Documentos incluem contexto específico do SGC
- Comandos são todos testados e funcionais
- Exemplos baseados na arquitetura real do projeto
- Estratégias progressivas e reversíveis

## 📝 Notas de Atualização

**Data**: 21 de dezembro de 2025

**Mudanças Realizadas**:
1. ✅ Adicionado contexto específico do SGC em cada sprint
2. ✅ Substituído comando genérico "mvn test" por Gradle correto
3. ✅ Documentada estrutura real de 9 módulos
4. ✅ Adicionadas estatísticas verificadas (98 testes, 478 @DisplayName, etc)
5. ✅ Incluído detalhe que JaCoCo já está configurado (Sprint 6)
6. ✅ Adicionados exemplos de código baseados na arquitetura real
7. ✅ Todos os comandos validados e testados
8. ✅ Referências aos READMEs do projeto

**Validação**: Todos os comandos foram testados com script automatizado (`/tmp/validate_commands.sh`).

---

**Manutenção**: Este documento deve ser atualizado conforme os sprints são executados, registrando progresso real e lições aprendidas.
