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
- **646 anotações @DisplayName** (Aumento significativo após Sprint 4)
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
**Objetivo**: Eliminar dependência de seed global (`data.sql`) utilizando Fixtures e configuração programática.

**Estratégia**: Substituir IDs hardcoded (ex: `100L`, `1L`) por entidades criadas dinamicamente via `UnidadeFixture`, `UsuarioFixture`, etc.

**Subdivisão do Trabalho**:
1.  **Lote 1:** CDU-01 (Login) e CDU-03 (Processos).
2.  **Lote 2:** CDU-04 a CDU-08 (Gestão de Workflow).
3.  **Lote 3:** CDU-09 a CDU-15 (Mapas e Atividades).
4.  **Lote 4:** CDU-16 a CDU-21 (Restante).

**Testes Afetados**: 30 testes de integração (CDU01-CDU21, etc)

**Estado**: ✅ Concluído
- ✅ CDU-02 (Piloto) - Concluído.
- ✅ CDU-01 - Concluído.
- ✅ CDU-03 - Concluído.
- ✅ CDU-04 a CDU-08 (Lote 2) - Concluído.
- ✅ CDU-09 a CDU-15 (Lote 3) - Concluído.
- ✅ CDU-16 a CDU-21 (Lote 4) - Concluído.

**Documento**: [`sprint-05-desacoplamento-integracao.md`](./sprint-05-desacoplamento-integracao.md)

#### Detalhamento da Execução - Lote 3 (Sprint 5)

| Teste | Descrição | Estado Atual | Ação Planejada |
|-------|-----------|--------------|----------------|
| `CDU09IntegrationTest` | Cadastrar competência | ✅ Concluído | Refatorado para Fixtures |
| `CDU10IntegrationTest` | Consultar competência | ✅ Concluído | Refatorado para Fixtures |
| `CDU11IntegrationTest` | Alterar competência | ✅ Concluído | Refatorado para Fixtures |
| `CDU12IntegrationTest` | Excluir competência | ✅ Concluído | Refatorado para Fixtures |
| `CDU13IntegrationTest` | Devolução/Homologação | ✅ Concluído | Refatorado para Fixtures e setup JDBC |
| `CDU14IntegrationTest` | Revisão e Análise | ✅ Concluído | Refatorado para Fixtures e setup JDBC |
| `CDU15IntegrationTest` | Manter Mapa | ✅ Concluído | Refatorado para Fixtures |

#### Detalhamento da Execução - Lote 4 (Sprint 5)

| Teste | Descrição | Estado Atual | Ação Realizada |
|-------|-----------|--------------|----------------|
| `CDU16IntegrationTest` | Ajustar mapa de competências | ✅ Concluído | Refatorado para Fixtures (Unidade, Processo, Mapa, Subprocesso) |
| `CDU17IntegrationTest` | Disponibilizar Mapa | ✅ Concluído | Já refatorado (Fixtures + JdbcTemplate) |
| `CDU18IntegrationTest` | Visualizar Mapa | ✅ Concluído | Refatorado para Fixtures |
| `CDU19IntegrationTest` | Validar Mapa | ✅ Concluído | Refatorado para Fixtures com hierarquia (2 níveis), corrigidas assertions hardcoded |
| `CDU20IntegrationTest` | Analisar validação | ✅ Concluído | Refatorado para Fixtures com hierarquia (3 níveis), corrigidas assertions hardcoded |
| `CDU21IntegrationTest` | Finalizar Processo | ✅ Concluído | Refatorado para Fixtures (Unidades + Usuários dinâmicos, mocks atualizados) |

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

| Sprint | Foco | Métrica de Sucesso | Status |
|--------|------|-------------------|--------|
| 0 | Baseline | Comandos documentados, métricas estabelecidas | ✅ Concluído |
| 1 | Boilerplate | -15 arquivos de teste removidos | ✅ Concluído |
| 2 | LENIENT | 0 ocorrências de `Strictness.LENIENT` | ✅ Concluído |
| 3 | Fixtures | Pacote `fixture/` criado com N builders | ✅ Concluído |
| 4 | Padronização | >90% com `@DisplayName` e nomenclatura correta | ✅ Concluído |
| 5 | Integração | 0 IDs hardcoded sem setup explícito | ✅ Concluído (CDU-01 a CDU-21) |
| 6 | Cobertura | Relatório HTML visível, gate configurado | ✅ Concluído |
| 7 | Qualidade | >30% redução em testes duplicados via parametrização | Pendente |

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

**Data**: 21 de dezembro de 2025 (Atualizado em Execução)

**Mudanças Realizadas**:
1. ✅ **Sprint 0:** Validada baseline (98 testes iniciais, 1 LENIENT).
2. ✅ **Sprint 1:** Removidos 15 arquivos de testes boilerplate (DTOs/Models), reduzindo para 83 arquivos.
3. ✅ **Sprint 2:** Removido `Strictness.LENIENT` de `ProcessoServiceTest.java`. Corrigido stub de `save` para `saveAndFlush`.
4. ✅ **Sprint 3:** Criado pacote `sgc.fixture` com 6 builders (Processo, Subprocesso, Mapa, Atividade, Unidade, Usuario). Refatorados `ProcessoServiceTest`, `SubprocessoServiceTest` e `AtividadeServiceTest` para usar fixtures.
5. ✅ Adicionado contexto específico do SGC em cada sprint
6. ✅ Substituído comando genérico "mvn test" por Gradle correto
7. ✅ Documentada estrutura real de 9 módulos
8. ✅ Adicionadas estatísticas verificadas (98 testes, 478 @DisplayName, etc)
9. ✅ Incluído detalhe que JaCoCo já está configurado (Sprint 6)
10. ✅ Adicionados exemplos de código baseados na arquitetura real
11. ✅ Todos os comandos validados e testados
12. ✅ Referências aos READMEs do projeto
13. ✅ **Sprint 4 (Concluído):** Todos os módulos do backend padronizados (`sgrh`, `unidade`, `painel`, `comum`, `mapa`, `e2e` e `integracao`). Total de `@DisplayName` aumentou para 646.
14. ✅ **Sprint 5 (Piloto CDU-02):** Refatorado `CDU02IntegrationTest` para não depender de IDs do `data.sql`. Criado `AlertaFixture`. Resolvidos problemas de `ObjectOptimisticLockingFailureException` via reset de sequence H2.
15. ✅ **Sprint 5 (Lote 1):** Refatorados `CDU01IntegrationTest` e `CDU03IntegrationTest`. Uso de `jdbcTemplate` para setup de Usuários/Perfis imutáveis e `saveAndFlush` para entidades gerenciadas.
16. ✅ **Sprint 5 (Lote 2):** Verificados e validados `CDU04IntegrationTest` a `CDU08IntegrationTest`. Corrigido uso de ID fixo em `CDU05IntegrationTest`. Confirmado que os testes já utilizam Fixtures e não dependem do seed global (apenas de resets de sequence H2).
17. ✅ **Sprint 5 (Lote 3):** Refatorados `CDU09IntegrationTest`, `CDU10IntegrationTest`, `CDU11IntegrationTest` e `CDU12IntegrationTest`. Criado `CompetenciaFixture` e atualizado `AtividadeFixture` para permitir persistence. Todos os testes validados com sucesso.
18. ✅ **Sprint 5 (Lote 3):** Refatorados `CDU13IntegrationTest`, `CDU14IntegrationTest` e `CDU15IntegrationTest`. Utilizado `JdbcTemplate` para contornar `ObjectOptimisticLockingFailureException` em entidades imutáveis (`VW_UNIDADE`) nos testes transacionais. Corrigida compilação de `MapaFixture`.
19. ✅ **Sprint 5 (Lote 4 - CONCLUÍDO):** Refatorados `CDU16IntegrationTest` a `CDU21IntegrationTest`. Todos os testes agora utilizam Fixtures para criar entidades dinamicamente:
   - **CDU16**: Removido hardcode de Unidade (ID 15). Criadas entidades via Fixtures.
   - **CDU17**: Já estava refatorado com Fixtures e JdbcTemplate.
   - **CDU18**: Removido hardcode de Unidade (ID 11). Criadas entidades via Fixtures.
   - **CDU19**: Removidos hardcodes de Unidades (IDs 6, 9). Criada hierarquia de 2 níveis com Fixtures. Corrigidas assertions hardcoded de siglas.
   - **CDU20**: Removidos hardcodes de Unidades (IDs 2, 6, 8). Criada hierarquia de 3 níveis com Fixtures. Corrigidas assertions hardcoded de siglas.
   - **CDU21**: Removidos hardcodes de Unidades (IDs 3, 4, 5) e Usuários (IDs "1", "2", "3"). Criadas entidades via Fixtures. Atualizados mocks do `SgrhService` para usar dados dinâmicos.
20. ✅ **Sprint 6 (CONCLUÍDO):** Configurada visibilidade de cobertura JaCoCo com relatórios HTML e quality gate:
   - Habilitado relatório HTML do JaCoCo em `backend/build.gradle.kts`.
   - Configurado quality gate com limites mínimos: 60% para branches, 80% para linhas.
   - Estabelecida baseline de cobertura: Linhas 85.9%, Branches 62.1%, Instruções 84.6%, Métodos 83.5%, Classes 91.3%.
   - Adicionada documentação completa de testes no `backend/README.md` incluindo comandos, métricas e estrutura.
   - Integrado quality gate ao `check` task do Gradle.
   - Validações confirmadas: relatório HTML gerado com sucesso, quality gate passando.

**Sprint 5 - Status Final**: ✅ **CONCLUÍDO**. Todos os 30 testes de integração (CDU01-CDU21) estão completamente desacoplados do seed global e utilizam setup programático via Fixtures.

**Sprint 6 - Status Final**: ✅ **CONCLUÍDO**. Visibilidade de cobertura estabelecida com relatórios HTML, quality gates configurados e documentação completa.

**Próximo Passo**: Sprint 7 - Qualidade Avançada.
