# Relatório de Status - Refatoração de Testes JUnit (Sprint 5)

**Data do Relatório:** 22 de dezembro de 2025  
**Responsável:** Análise Automatizada  
**Referência:** SPRINT_JUNIT_REFACTORING.md - Sprint 5 (Desacoplamento de Integração)

---

## 📊 Resumo Executivo

A Sprint 5 da refatoração JUnit tem como objetivo desacoplar os testes de integração (CDU) do arquivo seed global `data.sql`, eliminando dependências de IDs hardcoded e tornando os testes isolados e paralelizáveis.

### Status Geral

| Lote | CDUs | Status | Testes Passando | Observações |
|------|------|--------|-----------------|-------------|
| **Piloto** | CDU-02 | ✅ Concluído | Sim | Refatorado com sucesso |
| **Lote 1** | CDU-01, CDU-03 | ✅ Concluído | Sim | Refatorado com sucesso |
| **Lote 2** | CDU-04 a CDU-08 | ✅ Concluído | Sim | Refatorado com sucesso |
| **Lote 3** | CDU-09 a CDU-15 | ⚠️ Concluído com Issues | CDU-01 a CDU-13: Sim<br>CDU-14, CDU-15: Não | CDU-14 e CDU-15 têm falhas de lógica de negócio |
| **Lote 4** | CDU-16 a CDU-21 | ❌ Pendente | N/A | Ainda não refatorado |

---

## ✅ CDU-01 a CDU-13: REFATORAÇÃO COMPLETA E VALIDADA

### Verificação de Fixtures

Todos os testes CDU-01 a CDU-13 foram verificados e confirmados:

✅ **Uso de Fixtures:**
- `UnidadeFixture` - Criação dinâmica de unidades
- `UsuarioFixture` - Criação dinâmica de usuários
- `ProcessoFixture` - Criação dinâmica de processos
- `SubprocessoFixture` - Criação dinâmica de subprocessos
- `CompetenciaFixture` - Criação dinâmica de competências
- `AtividadeFixture` - Criação dinâmica de atividades
- `AlertaFixture` - Criação dinâmica de alertas (CDU-02)

✅ **Eliminação de IDs Hardcoded:**
- Nenhum uso de `findById(1L)`, `findById(99L)`, etc. sem criação explícita
- IDs dinâmicos gerados via `saveAndFlush` ou `JdbcTemplate` com IDs controlados

✅ **Estratégias de Criação:**
- **CDU-01, CDU-02:** JdbcTemplate para usuários/perfis + Fixtures para outras entidades
- **CDU-03 a CDU-08:** Fixtures com `saveAndFlush`
- **CDU-09 a CDU-12:** Fixtures com criação explícita de Mapas
- **CDU-13, CDU-14:** JdbcTemplate para VW_UNIDADE (@Immutable) + Fixtures para outras entidades

### Testes Executados com Sucesso

```bash
✅ CDU01IntegrationTest - 100% passing
✅ CDU02IntegrationTest - 100% passing
✅ CDU03IntegrationTest - 100% passing
✅ CDU04IntegrationTest - 100% passing
✅ CDU05IntegrationTest - 100% passing
✅ CDU06IntegrationTest - 100% passing
✅ CDU07IntegrationTest - 100% passing
✅ CDU08IntegrationTest - 100% passing
✅ CDU09IntegrationTest - 100% passing
✅ CDU10IntegrationTest - 100% passing
✅ CDU11IntegrationTest - 100% passing
✅ CDU12IntegrationTest - 100% passing
✅ CDU13IntegrationTest - 100% passing
```

**Comando de Verificação:**
```bash
./gradlew :backend:test --tests "sgc.integracao.CDU01IntegrationTest" \
  --tests "sgc.integracao.CDU02IntegrationTest" \
  --tests "sgc.integracao.CDU03IntegrationTest" \
  # ... até CDU13
```

**Resultado:** BUILD SUCCESSFUL (todos os lotes 1, 2 e 3 até CDU-13)

---

## ⚠️ CDU-14 e CDU-15: REFATORADOS MAS COM FALHAS DE TESTE

### Problemas Identificados e Corrigidos

#### 1. Erros de Compilação (CORRIGIDOS)

**Problema:** `MapaFixture.mapaPadrao()` estava sendo chamado sem o parâmetro `Subprocesso` obrigatório.

**Arquivos Afetados:**
- `CDU14IntegrationTest.java:181`
- `CDU15IntegrationTest.java:85`

**Solução Aplicada:**
```java
// ANTES (incorreto - causava erro de compilação)
Mapa mapaVigente = MapaFixture.mapaPadrao();
mapaVigente.setCodigo(null);
mapaVigente = mapaRepo.save(mapaVigente);

// DEPOIS (correto)
Mapa mapaVigente = new Mapa();
mapaVigente = mapaRepo.save(mapaVigente);
```

**Justificativa:** Os mapas são criados ANTES dos subprocessos nestes testes, portanto não há como passar um Subprocesso para o MapaFixture. A solução é criar o Mapa diretamente com `new Mapa()`.

#### 2. Violação de Chave Primária em CDU-15 (CORRIGIDO)

**Problema:** CDU-15 usava `UnidadeFixture.unidadePadrao()` com `setCodigo(null)`, causando conflito com IDs do `data.sql` (ex: ID 8).

**Solução Aplicada:**
```java
// ANTES (causava ConstraintViolationException)
Unidade unidade = UnidadeFixture.unidadePadrao();
unidade.setCodigo(null);
unidade = unidadeRepo.save(unidade);

// DEPOIS (usa JdbcTemplate com ID controlado)
Long idUnidade = 5000L;
String sqlInsertUnidade = "INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, unidade_superior_codigo, titulo_titular) VALUES (?, ?, ?, ?, ?, ?, ?)";
jdbcTemplate.update(sqlInsertUnidade, idUnidade, "Unidade CDU-15", "U15", "OPERACIONAL", "ATIVA", null, null);
Unidade unidade = unidadeRepo.findById(idUnidade).orElseThrow();
```

**Justificativa:** VW_UNIDADE é uma entidade `@Immutable`, então a estratégia correta é usar `JdbcTemplate` com IDs altos (5000L) para evitar conflitos com `data.sql`.

### Falhas de Teste Remanescentes

Após as correções de compilação e setup, os testes agora EXECUTAM mas FALHAM com erros de lógica de negócio:

#### CDU-14 (10 testes falhando)
- **Erro Comum:** `Status expected:<200> but was:<409>` (Conflict)
- **Teste Exemplo:** `criarEIniciarProcessoDeRevisao()` retorna 409 em vez de 200
- **Causa Provável:** Validações de negócio (processo já existe? unidade já tem processo ativo?)

#### CDU-15 (6 testes falhando)
- **Erro Comum:** `Status expected:<200> but was:<403>` (Forbidden)
- **Teste Exemplo:** `deveAdicionarCompetencia()` retorna 403 em vez de 200
- **Causa Provável:** Problemas de autenticação/autorização com `@WithMockGestor`

### ⚠️ Análise de Impacto

**Questão:** Estas falhas são pré-existentes ou foram introduzidas pela refatoração?

**Evidências:**
1. Os erros são de **lógica de negócio** (403 Forbidden, 409 Conflict), não de setup de dados
2. CDU-14 e CDU-15 estavam marcados como "Concluídos" no SPRINT_JUNIT_REFACTORING.md
3. Não há registro recente de alterações em CDU-14/CDU-15 além da refatoração do Lote 3

**Recomendação:** Investigar se estes testes passavam ANTES da refatoração do Lote 3. Se sim, a refatoração introduziu uma regressão. Se não, são bugs pré-existentes.

---

## ❌ CDU-16 a CDU-21: NÃO REFATORADOS

### Análise de Dependências Hardcoded

Os seguintes testes ainda dependem diretamente de IDs do `data.sql`:

#### CDU-16: Ajustar mapa de competências
```java
Unidade unidade = unidadeRepo.findById(15L).orElseThrow(); // HARDCODED
```
**Dependências:** ID 15 deve existir em `data.sql`

#### CDU-17: Consultar detalhes de competências
```java
unidade = unidadeRepo.findById(8L).orElseThrow(); // SEDESENV - HARDCODED
subprocesso = subprocessoRepo.findById(1700L).orElseThrow(); // HARDCODED
mapa = mapaRepo.findById(1700L).orElseThrow(); // HARDCODED
atividadeRepo.findById(17001L).orElseThrow(); // HARDCODED
competenciaRepo.findById(17001L).orElseThrow(); // HARDCODED
```
**Dependências:** IDs 8, 1700, 17001 devem existir em `data.sql` com dados específicos

#### CDU-18: Manter atividades
```java
unidade = unidadeRepo.findById(11L).orElseThrow(); // HARDCODED
```
**Dependências:** ID 11 deve existir em `data.sql`

#### CDU-19: Estrutura de unidades
```java
unidadeSuperior = unidadeRepo.findById(6L).orElseThrow(); // COSIS - HARDCODED
unidade = unidadeRepo.findById(9L).orElseThrow(); // SEDIA - HARDCODED
```
**Dependências:** IDs 6 e 9 devem existir em `data.sql` com hierarquia específica

#### CDU-20: Hierarquia de unidades
```java
unidadeSuperiorSuperior = unidadeRepo.findById(2L).orElseThrow(); // STIC - HARDCODED
unidadeSuperior = unidadeRepo.findById(6L).orElseThrow(); // COSIS - HARDCODED
Unidade unidade = unidadeRepo.findById(8L).orElseThrow(); // SEDESENV - HARDCODED
```
**Dependências:** IDs 2, 6, 8 devem existir em `data.sql` com hierarquia 2 → 6 → 8

#### CDU-21: Vinculação de usuários
```java
Unidade unidadeIntermediaria = unidadeRepo.findById(3L).orElseThrow(); // HARDCODED
unidadeOperacional1 = unidadeRepo.findById(5L).orElseThrow(); // HARDCODED
unidadeOperacional2 = unidadeRepo.findById(4L).orElseThrow(); // HARDCODED
Usuario titularIntermediaria = usuarioRepo.findById("1").orElseThrow(); // HARDCODED
Usuario titularOp1 = usuarioRepo.findById("2").orElseThrow(); // HARDCODED
Usuario titularOp2 = usuarioRepo.findById("3").orElseThrow(); // HARDCODED
```
**Dependências:** IDs de unidades 3, 4, 5 e usuários "1", "2", "3" devem existir em `data.sql`

### Estratégia de Refatoração Recomendada

Para cada CDU-16 a CDU-21, seguir o padrão estabelecido em CDU-13 a CDU-15:

1. **Para VW_UNIDADE (@Immutable):** Usar `JdbcTemplate` com IDs altos (ex: 6000L+)
   ```java
   Long idUnidade = 6000L;
   String sql = "INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, ...) VALUES (?, ?, ?, ?, ?, ...)";
   jdbcTemplate.update(sql, idUnidade, "Nome", "SIGLA", "OPERACIONAL", "ATIVA", ...);
   Unidade unidade = unidadeRepo.findById(idUnidade).orElseThrow();
   ```

2. **Para VW_USUARIO (@Immutable):** Usar `JdbcTemplate` com IDs únicos
   ```java
   String tituloEleitoral = "999999999999";
   String sql = "INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, ...) VALUES (?, ?, ?, ...)";
   jdbcTemplate.update(sql, tituloEleitoral, "Nome", "email@example.com", ...);
   Usuario usuario = usuarioRepo.findById(tituloEleitoral).orElseThrow();
   ```

3. **Para entidades gerenciadas (Processo, Subprocesso, Mapa, etc.):** Usar Fixtures
   ```java
   Processo processo = ProcessoFixture.processoPadrao();
   processo.setCodigo(null);
   processo = processoRepo.save(processo);
   ```

4. **Adicionar @Autowired JdbcTemplate** nas classes de teste que precisarem

---

## 📈 Métricas de Progresso

### Cobertura da Refatoração (Sprint 5)

| Métrica | Valor Atual | Meta Sprint 5 |
|---------|-------------|---------------|
| **Testes de Integração Totais** | 21 (CDU-01 a CDU-21) | 21 |
| **Testes Refatorados** | 15 (CDU-01 a CDU-15) | 21 |
| **Testes Passando** | 13 (CDU-01 a CDU-13) | 21 |
| **Testes com Fixtures** | 15 | 21 |
| **Testes sem IDs Hardcoded** | 13 | 21 |
| **Percentual Concluído** | 61.9% (13/21) | 100% |
| **Percentual Refatorado** | 71.4% (15/21) | 100% |

### IDs Hardcoded Eliminados

```bash
# CDU-01 a CDU-13: 0 IDs hardcoded (exceto em setup JDBC controlado)
# CDU-14 a CDU-15: 0 IDs hardcoded (refatorados)
# CDU-16 a CDU-21: ~20 IDs hardcoded remanescentes
```

### Uso de Fixtures

```bash
# Fixtures criadas e em uso:
✅ UnidadeFixture      - 15/21 testes (71%)
✅ UsuarioFixture      - 15/21 testes (71%)
✅ ProcessoFixture     - 15/21 testes (71%)
✅ SubprocessoFixture  - 15/21 testes (71%)
✅ CompetenciaFixture  - 10/21 testes (48%)
✅ AtividadeFixture    - 10/21 testes (48%)
✅ AlertaFixture       - 2/21 testes (10%)
✅ MapaFixture         - 0/21 testes (não usado - ver nota)
```

**Nota sobre MapaFixture:** O `MapaFixture.mapaPadrao(Subprocesso)` existe mas não é amplamente usado porque muitos testes criam o Mapa ANTES do Subprocesso, usando `new Mapa()` diretamente.

---

## 🐛 Issues Identificados

### Issue #1: CDU-14 e CDU-15 falhando após refatoração
- **Severidade:** Alta
- **Tipo:** Possível Regressão ou Bug Pré-existente
- **Descrição:** Após refatoração, testes executam mas falham com erros 403/409
- **Próximo Passo:** Verificar histórico de CI/testes antes da refatoração do Lote 3

### Issue #2: MapaFixture.mapaPadrao() não utilizado
- **Severidade:** Baixa (Limpeza de Código)
- **Tipo:** Design de Fixture
- **Descrição:** A fixture exige Subprocesso, mas testes criam Mapa primeiro
- **Sugestão:** Considerar adicionar `MapaFixture.novo()` sem parâmetros

### Issue #3: 6 testes CDU ainda acoplados ao data.sql
- **Severidade:** Média (Objetivo da Sprint 5)
- **Tipo:** Trabalho Pendente
- **Descrição:** CDU-16 a CDU-21 ainda dependem de IDs do seed global
- **Próximo Passo:** Executar Lote 4 da Sprint 5

---

## 🎯 Critérios de Aceite da Sprint 5

Status dos critérios definidos em `sprint-05-desacoplamento-integracao.md`:

| Critério | Status | Observação |
|----------|--------|-----------|
| ✅ Nenhum teste depende de IDs hardcoded sem criação explícita | ⚠️ Parcial | CDU-01 a CDU-15: OK<br>CDU-16 a CDU-21: Pendente |
| ✅ `./gradlew :backend:test` passa sem erros | ❌ Não | CDU-14 e CDU-15 falhando |
| ✅ Testes podem rodar em qualquer ordem | ⚠️ Parcial | CDU-01 a CDU-13: OK<br>CDU-14+: Desconhecido |
| ✅ Cada teste é autossuficiente | ⚠️ Parcial | CDU-01 a CDU-15: OK<br>CDU-16 a CDU-21: Não |

---

## 📋 Recomendações

### Imediatas (Curto Prazo)

1. **Investigar CDU-14 e CDU-15:**
   - Comparar com versão antes da refatoração do Lote 3
   - Verificar logs de CI/testes históricos
   - Se pré-existente: abrir issue separado
   - Se regressão: reverter e re-refatorar com cuidado

2. **Validar Estratégia de JdbcTemplate:**
   - CDU-13, CDU-14, CDU-15 usam JdbcTemplate para VW_UNIDADE
   - Confirmar que esta é a abordagem correta para entidades @Immutable
   - Documentar no AGENTS.md se aprovado

### Médio Prazo

3. **Executar Lote 4 da Sprint 5:**
   - Refatorar CDU-16 a CDU-21 usando padrão estabelecido
   - Usar IDs altos (6000L+) para evitar conflitos com data.sql
   - Validar cada teste individualmente antes de seguir para o próximo

4. **Melhorar Fixtures:**
   - Considerar `MapaFixture.novo()` sem parâmetros
   - Adicionar `UnidadeFixture.comHierarquia()` para testes que precisam de hierarquia

### Longo Prazo

5. **Remover data.sql (Opcional):**
   - Após refatoração completa, considerar remover `data.sql`
   - Cada teste criaria 100% de seus próprios dados
   - Benefício: testes completamente isolados e paralelizáveis

6. **Consolidar Documentação:**
   - Atualizar SPRINT_JUNIT_REFACTORING.md com lições aprendidas
   - Documentar pattern de JdbcTemplate vs Fixtures
   - Criar guia de "Como Criar Novo Teste de Integração"

---

## 📝 Comandos de Verificação

### Verificar CDU-01 a CDU-13 (Confirmado Funcionando)
```bash
./gradlew :backend:test --tests "sgc.integracao.CDU01IntegrationTest" \
  --tests "sgc.integracao.CDU02IntegrationTest" \
  --tests "sgc.integracao.CDU03IntegrationTest" \
  --tests "sgc.integracao.CDU04IntegrationTest" \
  --tests "sgc.integracao.CDU05IntegrationTest" \
  --tests "sgc.integracao.CDU06IntegrationTest" \
  --tests "sgc.integracao.CDU07IntegrationTest" \
  --tests "sgc.integracao.CDU08IntegrationTest" \
  --tests "sgc.integracao.CDU09IntegrationTest" \
  --tests "sgc.integracao.CDU10IntegrationTest" \
  --tests "sgc.integracao.CDU11IntegrationTest" \
  --tests "sgc.integracao.CDU12IntegrationTest" \
  --tests "sgc.integracao.CDU13IntegrationTest"
```
**Resultado Esperado:** BUILD SUCCESSFUL

### Verificar CDU-14 e CDU-15 (Falhando)
```bash
./gradlew :backend:test --tests "sgc.integracao.CDU14IntegrationTest" \
  --tests "sgc.integracao.CDU15IntegrationTest"
```
**Resultado Atual:** 16 tests completed, 16 failed (403 Forbidden, 409 Conflict)

### Verificar IDs Hardcoded em CDU-16 a CDU-21
```bash
grep -n "findById([0-9]" backend/src/test/java/sgc/integracao/CDU{16,17,18,19,20,21}IntegrationTest.java
```

### Verificar Uso de Fixtures
```bash
grep -l "import sgc.fixture" backend/src/test/java/sgc/integracao/CDU*.java
```

---

## 📅 Próximos Passos

1. **[URGENTE]** Resolver falhas em CDU-14 e CDU-15
2. **[ALTA PRIORIDADE]** Refatorar CDU-16 a CDU-21 (Lote 4)
3. **[MÉDIA PRIORIDADE]** Validar todos os 21 testes passando
4. **[BAIXA PRIORIDADE]** Limpar MapaFixture ou adicionar sobrecarga

---

**Conclusão:** A Sprint 5 está **71.4% refatorada** e **61.9% funcional**. CDU-01 a CDU-13 estão completamente desacoplados e passando. CDU-14 e CDU-15 foram refatorados mas têm falhas que precisam investigação. CDU-16 a CDU-21 aguardam refatoração.

---

**Anexos:**
- SPRINT_JUNIT_REFACTORING.md
- sprint-05-desacoplamento-integracao.md
- backend/src/test/resources/data.sql (seed global)
