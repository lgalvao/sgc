# Plano de Reorganização de Testes - SGC

Este plano foi criado para ser executado por agentes de IA (ou desenvolvedores humanos) de forma incremental. Cada sprint é autônomo, pode ser executado em uma sessão de trabalho, e deve manter a cobertura de 100% após sua conclusão.

---

## Objetivo Geral

Consolidar os **31 arquivos fragmentados** (`*CoverageTest`, `*GapTest`) nos arquivos principais de teste, eliminando redundância, padronizando estilo e melhorando a manutenibilidade **sem perder cobertura**.

---

## ⚠️ SALVAGUARDAS CRÍTICAS — LEIA ANTES DE EXECUTAR

### Princípio Fundamental: MESCLAR, NÃO DELETAR

O objetivo **não é deletar arquivos**, mas sim **consolidar testes**. Nenhum caso de teste deve ser perdido. O fluxo obrigatório é:

```
┌─────────────────────────────────────────────────────────────────────────┐
│  FLUXO SEGURO PARA CADA ARQUIVO:                                        │
│                                                                         │
│  1. CHECKPOINT: Rodar jacocoTestReport e ANOTAR a cobertura             │
│                                                                         │
│  2. ANALISAR: Identificar TODOS os casos de teste no arquivo satélite   │
│                                                                         │
│  3. COPIAR: Mover os casos para o arquivo principal (com @Nested se     │
│             necessário para organização)                                │
│                                                                         │
│  4. VERIFICAR RÁPIDO: Rodar testes do PACOTE afetado (não todos!)       │
│                                                                         │
│  5. REMOVER ARQUIVO SATÉLITE (agora vazio/redundante)                   │
│                                                                         │
│  6. AO FINAL DO SPRINT: Rodar :backend:test completo + jacocoTestReport │
│     → A cobertura DEVE ser >= ao checkpoint do passo 1                  │
│     → Se cair QUALQUER linha ou branch, REVERTER e investigar           │
└─────────────────────────────────────────────────────────────────────────┘
```

### Estratégia de Testes em Dois Níveis

Para evitar rodar 1700+ testes a cada pequena mudança:

| Momento                        | Comando                                                    | Quando Usar                          |
| :----------------------------- | :--------------------------------------------------------- | :----------------------------------- |
| **Verificação Rápida**         | `:backend:unitTest --tests "sgc.pacote.*"`                | Após mesclar CADA arquivo            |
| **Verificação Final (Sprint)** | `:backend:test`                                           | Ao FINALIZAR o sprint                |
| **Cobertura Completa**         | `:backend:jacocoTestReport`                               | Apenas no início e fim do sprint     |

**Comandos Úteis por Módulo**:

```bash
# Sprint 1: subprocesso.model
./gradlew :backend:unitTest --tests "sgc.subprocesso.model.*"

# Sprint 2: subprocesso.service (SubprocessoFacade)
./gradlew :backend:unitTest --tests "sgc.subprocesso.service.SubprocessoFacade*"

# Sprint 3: subprocesso.service.workflow
./gradlew :backend:unitTest --tests "sgc.subprocesso.service.workflow.*"

# Sprint 4: subprocesso.service.crud
./gradlew :backend:unitTest --tests "sgc.subprocesso.service.crud.*"

# Sprint 5: processo.service
./gradlew :backend:unitTest --tests "sgc.processo.service.*"

# Sprint 6: mapa.service
./gradlew :backend:unitTest --tests "sgc.mapa.service.*"

# Sprint 7: organizacao
./gradlew :backend:unitTest --tests "sgc.organizacao.*"

# Sprint 8: seguranca
./gradlew :backend:unitTest --tests "sgc.seguranca.*"
```

### Regras Invioláveis

1.  **UM ARQUIVO POR VEZ**: Nunca mescle múltiplos arquivos satélite simultaneamente. Faça um, verifique, só então prossiga.

2.  **ROLLBACK IMEDIATO**: Se a cobertura cair em qualquer métrica (linhas OU branches), reverta a mudança com `git checkout` antes de continuar.

3.  **DOCUMENTAR CADA TESTE MOVIDO**: No `test-organization-tracking.md`, liste os nomes dos métodos de teste que foram movidos de cada arquivo.

4.  **NUNCA ASSUMA REDUNDÂNCIA**: Mesmo que dois testes pareçam idênticos, verifique se cobrem branches diferentes. Use o relatório JaCoCo para confirmar.

5.  **COMMITS ATÔMICOS**: Faça um commit após cada mesclagem bem-sucedida, antes de passar para o próximo arquivo.

### Como Verificar se um Teste é Único

Para determinar se um teste no arquivo satélite é único ou redundante:

1.  **Comentar temporariamente** o teste no arquivo satélite.
2.  Rodar `./gradlew :backend:unitTest --tests "sgc.pacote.ClasseTest"` (do arquivo principal).
3.  Rodar `./gradlew :backend:jacocoTestReport`.
4.  Verificar o relatório HTML em `build/reports/jacoco/test/html/`.
5.  Se alguma linha/branch ficou descoberta, o teste é **único** e DEVE ser movido.
6.  Se a cobertura permanece igual, o teste pode ser **redundante** (mas ainda assim, mova-o para consolidação — não delete sem mover).

---

## Pré-Requisitos para Cada Sprint

Antes de iniciar qualquer sprint, o agente deve:

1.  **Criar checkpoint de cobertura** (use o script utilitário!):
    ```bash
    cd /Users/leonardo/sgc/backend && node etc/scripts/super-cobertura.cjs --run
    # Ou manualmente: ./gradlew :backend:jacocoTestReport
    # Anotar: Linhas: X%, Branches: Y%
    ```
2.  **Confirmar que os testes do módulo passam**:
    ```bash
    # Exemplo para Sprint 1:
    ./gradlew :backend:unitTest --tests "sgc.subprocesso.model.*"
    ```
3.  **Atualizar `test-organization-tracking.md`** com o status do sprint e o checkpoint de cobertura.

---

## Validação Pós-Sprint

Após cada sprint, o agente deve:

1.  **Rodar testes COMPLETOS do backend** (apenas 1x por sprint):
    ```bash
    cd /Users/leonardo/sgc && ./gradlew :backend:test
    ```
2.  **Verificar cobertura completa** (usando script utilitário):
    ```bash
    cd /Users/leonardo/sgc/backend && node etc/scripts/super-cobertura.cjs
    # Gera: cobertura_lacunas.json com detalhes de linhas/branches faltantes
    ```
3.  **Comparar com checkpoint**: A cobertura DEVE ser >= ao checkpoint inicial.
4.  **Atualizar `test-organization-tracking.md`** com o resultado e a lista de testes movidos.

---

## Scripts Utilitários Disponíveis

O projeto possui scripts em `/backend/etc/scripts/` que facilitam a análise. **USE-OS!**

| Script                        | Comando                                                   | Descrição                                              |
| :---------------------------- | :-------------------------------------------------------- | :----------------------------------------------------- |
| `super-cobertura.cjs`         | `node etc/scripts/super-cobertura.cjs --run`              | Roda JaCoCo e mostra lacunas por classe/linha          |
| `verificar-cobertura.cjs`     | `node etc/scripts/verificar-cobertura.cjs --missed`       | Ranking de arquivos com mais linhas/branches perdidos  |
| `analisar-cobertura.cjs`      | `node etc/scripts/analisar-cobertura.cjs`                 | Tabela detalhada de cobertura por arquivo              |
| `gerar-plano-cobertura.cjs`   | `node etc/scripts/gerar-plano-cobertura.cjs`              | Gera plano para preencher lacunas de cobertura         |

**Exemplo de uso durante refatoração**:

```bash
# 1. Antes de começar, verificar estado atual
cd /Users/leonardo/sgc/backend
node etc/scripts/super-cobertura.cjs --run

# 2. Após mesclar, verificar se algo ficou descoberto
node etc/scripts/verificar-cobertura.cjs --missed sgc.subprocesso.model

# 3. Se houver lacunas, detalhar
node etc/scripts/verificar-cobertura.cjs --missed --simple | grep SituacaoSubprocesso
```

---

## Simplificação de Tags (Sprint Futuro)

### Problema Atual

O projeto usa `@Tag("unit")` em ~100+ testes e `@Tag("integration")` em ~10 testes. Isso é redundante — **se algo não é integração, é unitário por exclusão**.

### Estratégia Proposta

1.  **Manter apenas `@Tag("integration")`** para testes que usam `@SpringBootTest` ou banco de dados.
2.  **Remover todos os `@Tag("unit")`** — testes sem tag são considerados unitários por padrão.
3.  **Manter `@Tag("security")`** se for uma categoria útil para rodar testes de segurança isoladamente.

### Comandos Após Simplificação

```bash
# Rodar APENAS testes unitários (exclui integration)
./gradlew :backend:test -PexcludeTags=integration

# Rodar APENAS testes de integração
./gradlew :backend:test -PincludeTags=integration

# Rodar TODOS os testes
./gradlew :backend:test
```

### Implementação (Sprint 12 - Opcional)

1.  **Criar script para remover `@Tag("unit")`**:
    ```bash
    # Exemplo de busca dos arquivos afetados
    grep -rl '@Tag("unit")' backend/src/test/java/sgc | wc -l
    # Resultado esperado: ~100 arquivos
    ```

2.  **Atualizar `build.gradle.kts`** para configurar filtros por tag:
    ```kotlin
    tasks.withType<Test> {
        useJUnitPlatform {
            if (project.hasProperty("includeTags")) {
                includeTags(project.property("includeTags") as String)
            }
            if (project.hasProperty("excludeTags")) {
                excludeTags(project.property("excludeTags") as String)
            }
        }
    }
    ```

3.  **Verificar que nenhum teste foi perdido** após a remoção.

### Tags a Manter

| Tag             | Uso                                                    |
| :-------------- | :----------------------------------------------------- |
| `integration`   | Testes com `@SpringBootTest`, banco, contexto Spring   |
| `security`      | Testes específicos de segurança (JWT, CORS, etc.)      |
| `slow`          | (Opcional) Testes que demoram mais de 5 segundos       |

### Tags a Remover

| Tag             | Motivo                                                 |
| :-------------- | :----------------------------------------------------- |
| `unit`          | Redundante — é o padrão por exclusão                   |

---

## Sprint 1: Consolidação do Módulo `subprocesso.model`

**Duração Estimada**: 30-45 minutos  
**Arquivos Alvo**:
- `SituacaoSubprocessoTest.java` (destino)
- `SituacaoSubprocessoCoverageTest.java` (origem → deletar)
- `SituacaoSubprocessoGapTest.java` (origem → deletar)

**Tarefas**:

1.  **Analisar** `SituacaoSubprocessoCoverageTest.java` e identificar casos de teste **únicos** (não presentes em `SituacaoSubprocessoTest`).
2.  **Copiar** os casos únicos para `SituacaoSubprocessoTest.java`, organizando-os com `@Nested` classes se apropriado.
3.  **Analisar** `SituacaoSubprocessoGapTest.java`:
    *   O teste usa `ReflectionTestUtils` para testar método privado `podeIniciar`.
    *   **Decisão**: Se a cobertura é alcançada pelos testes do método público `podeTransicionarPara`, o `GapTest` é redundante. Caso contrário, mover para o arquivo principal.
4.  **Eliminar duplicidades**: Se um `@CsvSource` já cobre uma transição, não duplicá-la.
5.  **Padronizar asserções**: Converter qualquer `assertTrue`/`assertFalse` para `assertThat(x).isTrue()`/`assertThat(x).isFalse()`.
6.  **Deletar** `SituacaoSubprocessoCoverageTest.java` e `SituacaoSubprocessoGapTest.java`.
7.  **Rodar testes e verificar cobertura**.

---

## Sprint 2: Consolidação do Módulo `subprocesso.service` (SubprocessoFacade)

**Duração Estimada**: 60-90 minutos  
**Arquivos Alvo**:
- `SubprocessoFacadeTest.java` (destino principal, manter estilo `@Nested`)
- `SubprocessoFacadeCoverageTest.java` (origem → deletar)
- `SubprocessoFacadeComplementaryTest.java` (origem → mesclar e deletar)
- `SubprocessoFacadeBatchUpdateTest.java` (origem → mesclar e deletar)

**Tarefas**:

1.  **Analisar** cada arquivo satélite e identificar os cenários cobertos.
2.  **Planejar estrutura `@Nested`** no arquivo destino:
    ```java
    @Nested class Leitura { /* buscar, listar, obter */ }
    @Nested class Escrita { /* criar, atualizar, excluir */ }
    @Nested class Validacao { /* validarCadastro, validarExistencia */ }
    @Nested class Workflow { /* disponibilizar, aceitar, homologar */ }
    @Nested class Bloco { /* operações em bloco */ }
    @Nested class Permissoes { /* obterPermissoes, obterDetalhes */ }
    ```
3.  **Mover testes** para as `@Nested` classes apropriadas, evitando duplicidade de `verify()`.
4.  **Padronizar mocks**: Usar `@InjectMocks` e `@Mock` consistentemente. Remover mocks não utilizados.
5.  **Deletar** os arquivos satélites.
6.  **Rodar testes e verificar cobertura**.

---

## Sprint 3: Consolidação do Módulo `subprocesso.service.workflow`

**Duração Estimada**: 45-60 minutos  
**Arquivos Alvo**:
- `SubprocessoMapaWorkflowServiceTest.java` (destino)
- `SubprocessoMapaWorkflowServiceCoverageTest.java` (origem → deletar)

**Tarefas**:

1.  Identificar casos únicos em `*CoverageTest`.
2.  Adicionar ao arquivo principal com `@Nested` se necessário.
3.  Remover duplicidades.
4.  Deletar arquivo satélite.
5.  **Rodar testes e verificar cobertura**.

---

## Sprint 4: Consolidação do Módulo `subprocesso.service.crud`

**Duração Estimada**: 30-45 minutos  
**Arquivos Alvo**:
- `SubprocessoCrudServiceTest.java` (destino)
- `SubprocessoCrudServiceCoverageTest.java` (origem → deletar)
- `SubprocessoValidacaoServiceTest.java` (destino)
- `SubprocessoValidacaoServiceCoverageTest.java` (origem → deletar)

**Tarefas**:

1.  Para cada par (Test + CoverageTest):
    *   Mesclar casos únicos.
    *   Deletar arquivo satélite.
2.  **Rodar testes e verificar cobertura**.

---

## Sprint 5: Consolidação do Módulo `processo.service`

**Duração Estimada**: 60 minutos  
**Arquivos Alvo**:
- `ProcessoFacadeBlocoTest.java`, `ProcessoFacadeCoverageTest.java`, `ProcessoFacadeCrudTest.java`, `ProcessoFacadeQueryTest.java`, `ProcessoFacadeSecurityTest.java`, `ProcessoFacadeWorkflowTest.java`

**Decisão Arquitetural**: Consolidar **todos** em um único `ProcessoFacadeTest.java` com `@Nested` classes, ou manter separação semântica (Bloco, Crud, Query, etc)?

**Recomendação**: Manter separação semântica é aceitável **se cada arquivo for bem organizado internamente**. Neste sprint, focar em:

1.  Mesclar `ProcessoFacadeCoverageTest.java` no arquivo mais apropriado (provavelmente `*CrudTest` ou `*QueryTest`).
2.  Deletar `ProcessoFacadeCoverageTest.java`.
3.  **Rodar testes e verificar cobertura**.

---

## Sprint 6: Consolidação do Módulo `mapa.service`

**Duração Estimada**: 45-60 minutos  
**Arquivos Alvo**:
- `MapaManutencaoServiceTest.java` (40KB) + `MapaManutencaoServiceCoverageTest.java`
- `MapaSalvamentoServiceTest.java` + `MapaSalvamentoServiceCoverageTest.java`
- `ImpactoMapaServiceTest.java` + `ImpactoMapaServiceCoverageTest.java`
- `MapaImportacaoListenerCoverageTest.java` (sem par → mesclar em contexto de uso)

**Tarefas**:

1.  Para cada par: mesclar e deletar satélite.
2.  Para `MapaImportacaoListenerCoverageTest.java`: mover para um arquivo de teste do listener ou criar `MapaImportacaoListenerTest.java` se não existir.
3.  **Rodar testes e verificar cobertura**.

---

## Sprint 7: Consolidação do Módulo `organizacao`

**Duração Estimada**: 45-60 minutos  
**Arquivos Alvo**:
- `UnidadeFacadeTest.java` + `UnidadeFacadeGapsTest.java` + `UnidadeFacadeElegibilidadePredicateTest.java` + `UnidadeFacadeHierarchyTest.java`
- `UsuarioFacadeTest.java` + `UsuarioFacadeCoverageTest.java`
- `UsuarioCoverageTest.java`, `PerfilDtoCoverageTest.java`, `UsuarioMapperCoverageTest.java`

**Tarefas**:

1.  Mesclar `UnidadeFacadeGapsTest` em `UnidadeFacadeTest`.
2.  **Decisão**: `ElegibilidadePredicateTest` e `HierarchyTest` podem ser mantidos separados se forem logicamente distintos, mas considerar mover como `@Nested` classes.
3.  Mesclar `*CoverageTest` nos arquivos principais.
4.  Deletar arquivos satélites.
5.  **Rodar testes e verificar cobertura**.

---

## Sprint 8: Consolidação do Módulo `seguranca`

**Duração Estimada**: 30 minutos  
**Arquivos Alvo**:
- `FiltroJwtGapTest.java` → mesclar em `FiltroJwtTest.java` (se existir) ou renomear
- `GerenciadorJwtGapTest.java` → mesclar em `GerenciadorJwtTest.java`
- `LoginControllerCoverageTest.java` → mesclar em `LoginControllerTest.java`

**Tarefas**:

1.  Mesclar cada `*GapTest` e `*CoverageTest` no arquivo principal.
2.  Deletar arquivos satélites.
3.  **Rodar testes e verificar cobertura**.

---

## Sprint 9: Consolidação de Módulos Menores

**Duração Estimada**: 30-45 minutos  
**Arquivos Alvo**:
- `alerta/AlertaMapperCoverageTest.java`
- `comum/GeneralMappersCoverageTest.java`
- `e2e/E2eControllerCoverageTest.java`
- `mapa/MapaControllerCoverageTest.java`
- `processo/ProcessoControllerCoverageTest.java`
- `processo/listener/EventoProcessoListenerCoverageTest.java`
- `processo/mapper/ProcessoDetalheMapperCoverageTest.java`
- `processo/mapper/ProcessoMapperCoverageTest.java`
- `subprocesso/SubprocessoCadastroControllerCoverageTest.java`
- `subprocesso/dto/AnaliseValidacaoDtoCoverageTest.java`
- `subprocesso/mapper/MapaAjusteMapperCoverageTest.java`
- `subprocesso/mapper/SubprocessoDetalheMapperCoverageTest.java`

**Tarefas**:

1.  Para cada arquivo satélite, identificar o arquivo principal correspondente.
2.  Se não existir arquivo principal (ex: `MapaAjusteMapperTest.java`), **renomear** o `*CoverageTest` para `*Test`.
3.  Se existir, mesclar e deletar.
4.  **Rodar testes e verificar cobertura**.

---

## Sprint 10: Eliminação do `CoberturaExtraTest.java`

**Duração Estimada**: 45 minutos  
**Arquivo Alvo**: `sgc/CoberturaExtraTest.java`

**Tarefas**:

1.  **Analisar** cada entidade instanciada no arquivo:
    *   `ErroEstadoImpossivel`, `ErroConfiguracao`, `ErroMapaNaoAssociado`, `ErroParametroPainelInvalido` → Mover para testes dos serviços que lançam esses erros.
    *   `ErroNegocioBase`, `ErroNegocio` → Mover para `RestExceptionHandlerTest` ou testes de erro específicos.
    *   `Competencia`, `Conhecimento`, `Unidade` → Já cobertos por testes de domínio? Se sim, remover. Se não, mover.
2.  **Distribuir** cada teste para o contexto apropriado.
3.  **Deletar** `CoberturaExtraTest.java`.
4.  **Rodar testes e verificar cobertura**.

---

## Sprint 11: Padronização de Estilo (Opcional, Baixa Prioridade)

**Duração Estimada**: 2-3 horas (pode ser dividido)  
**Escopo**: Arquivos que usam `assertTrue`/`assertFalse` do JUnit em vez de AssertJ.

**Arquivos Identificados** (~17):
- `comum/model/EntidadeBaseTest.java`
- `comum/util/SleeperTest.java`
- `subprocesso/model/SituacaoSubprocessoCoverageTest.java` (já será deletado)
- `alerta/AlertaServiceTest.java`
- `seguranca/login/ClienteAcessoAdTest.java`
- `seguranca/acesso/AbstractAccessPolicyTest.java`
- `seguranca/acesso/SubprocessoAccessPolicyTest.java`
- `configuracao/model/ParametroValidationTest.java`
- `organizacao/UsuarioServiceTest.java`
- ...e outros

**Tarefas**:

1.  Para cada arquivo, substituir:
    *   `assertTrue(x)` → `assertThat(x).isTrue()`
    *   `assertFalse(x)` → `assertThat(x).isFalse()`
    *   `assertEquals(a, b)` → `assertThat(b).isEqualTo(a)`
    *   `assertNull(x)` → `assertThat(x).isNull()`
2.  **Rodar testes e verificar cobertura**.

---

## Cronograma Sugerido

| Sprint | Módulo                          | Arquivos a Deletar | Prioridade |
| :----- | :------------------------------ | :----------------- | :--------- |
| 1      | `subprocesso.model`             | 2                  | 🔴 Alta    |
| 2      | `subprocesso.service` (Facade)  | 3                  | 🔴 Alta    |
| 3      | `subprocesso.service.workflow`  | 1                  | 🔴 Alta    |
| 4      | `subprocesso.service.crud`      | 2                  | 🔴 Alta    |
| 5      | `processo.service`              | 1                  | 🟠 Média   |
| 6      | `mapa.service`                  | 4                  | 🟠 Média   |
| 7      | `organizacao`                   | 5                  | 🟡 Baixa   |
| 8      | `seguranca`                     | 3                  | 🟡 Baixa   |
| 9      | Módulos Menores                 | 11                 | 🟡 Baixa   |
| 10     | `CoberturaExtraTest`            | 1                  | 🟠 Média   |
| 11     | Padronização de Estilo          | 0                  | ⚪ Opcional |
| 12     | Simplificação de Tags           | 0 (remove @Tag)    | ⚪ Opcional |

**Total de Arquivos a Mesclar/Deletar**: ~33 (31 satélites + 1 `CoberturaExtraTest` + possíveis renomeações)  
**Total de `@Tag("unit")` a Remover (Sprint 12)**: ~100+ arquivos

---

## Notas para Agentes de IA

1.  **Use os scripts utilitários!**
    ```bash
    cd /Users/leonardo/sgc/backend
    node etc/scripts/super-cobertura.cjs --run   # Para verificar cobertura
    node etc/scripts/verificar-cobertura.cjs --missed  # Para ver lacunas
    ```

2.  **Rode testes por pacote durante a mesclagem**, não todos os 1700+:
    ```bash
    ./gradlew :backend:unitTest --tests "sgc.subprocesso.model.*"
    ```

3.  **Rode `:backend:test` completo apenas 1x ao final de cada sprint**.

4.  **Se um teste falhar após a mesclagem, reverta e analise a causa.** Provavelmente há uma dependência de ordem ou mock não configurado.

5.  **Commits granulares**: Faça um commit após cada arquivo mesclado com sucesso.

6.  **Atualize `test-organization-tracking.md`** ao iniciar e finalizar cada sprint.

7.  **Priorize os sprints 1-4** (módulo `subprocesso`), pois é o core do negócio e tem maior fragmentação.

8.  **Ao remover `@Tag("unit")` (Sprint 12)**, use busca e substituição em massa:
    ```bash
    # Localizar arquivos afetados
    grep -rl '@Tag("unit")' backend/src/test/java/sgc
    
    # Remover a linha inteira (cuidado com múltiplas tags na mesma classe)
    # Melhor fazer manualmente ou com script que preserve @Tag("integration")
    ```

---

**Documento criado em**: 2026-02-06  
**Última atualização**: 2026-02-06

