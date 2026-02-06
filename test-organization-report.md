# Relatório de Organização de Testes - SGC

Este relatório apresenta uma análise detalhada da estrutura de testes atual do projeto SGC, identificando pontos de fragmentação, desorganização e redundância, apesar da cobertura métrica de 100%.

---

## Sumário Executivo

| Métrica                           | Valor       |
| --------------------------------- | ----------- |
| **Total de Arquivos de Teste**    | 248         |
| **Arquivos `*CoverageTest`**       | 27          |
| **Arquivos `*GapTest`**            | 4           |
| **Arquivos com `@Nested`**         | ~59         |
| **Arquivos usando `assertTrue`**   | ~17         |
| **Arquivos usando `assertThat`**   | ~145+       |

---

## 1. Achados Principais

### 1.1. Fragmentação Excessiva (O Problema dos Sufixos)

A prática de criar novos arquivos de teste para cobrir lacunas específicas de cobertura, em vez de evoluir os testes existentes, resultou em uma **proliferação de 31 arquivos "extras"** (27 `*CoverageTest` + 4 `*GapTest`), representando **12.5% do total de arquivos de teste**.

**Exemplo Crítico: `SubprocessoFacade`**  
Existem pelo menos **4 arquivos** testando a mesma classe:
1.  `SubprocessoFacadeTest.java` (133 linhas): Testes de delegação iniciais.
2.  `SubprocessoFacadeCoverageTest.java` (137 linhas): Caminhos de erro e listas vazias.
3.  `SubprocessoFacadeComplementaryTest.java` (396 linhas): Testes adicionais com `@Nested`.
4.  `SubprocessoFacadeBatchUpdateTest.java` (70 linhas): Operações em bloco.

**Total: 736 linhas de teste para uma única Facade.**

**Outros exemplos de fragmentação:**

| Componente                        | Arquivos de Teste                                                                 | Linhas Totais (aprox.) |
| :-------------------------------- | :-------------------------------------------------------------------------------- | :--------------------- |
| `SituacaoSubprocesso`             | `Test`, `CoverageTest`, `GapTest`                                                 | ~280                   |
| `SubprocessoMapaWorkflowService`  | `Test`, `CoverageTest`                                                            | ~380                   |
| `ProcessoFacade`                  | `BlocoTest`, `CoverageTest`, `CrudTest`, `QueryTest`, `SecurityTest`, `WorkflowTest` | ~640                   |
| `UnidadeFacade`                   | `Test`, `ElegibilidadePredicateTest`, `GapsTest`, `HierarchyTest`                 | ~460                   |
| `MapaManutencaoService`           | `Test` (40KB!), `CoverageTest`                                                    | ~480                   |

---

### 1.2. Inventário Completo de Arquivos Fragmentados

Os 31 arquivos identificados como "satélites" de cobertura são:

**Arquivos `*CoverageTest` (27):**
| Módulo        | Arquivo                                     |
| :------------ | :------------------------------------------ |
| `alerta`      | `AlertaMapperCoverageTest.java`             |
| `comum`       | `GeneralMappersCoverageTest.java`           |
| `e2e`         | `E2eControllerCoverageTest.java`            |
| `mapa`        | `MapaControllerCoverageTest.java`           |
| `mapa`        | `ImpactoMapaServiceCoverageTest.java`       |
| `mapa`        | `MapaImportacaoListenerCoverageTest.java`   |
| `mapa`        | `MapaManutencaoServiceCoverageTest.java`    |
| `mapa`        | `MapaSalvamentoServiceCoverageTest.java`    |
| `organizacao` | `UsuarioFacadeCoverageTest.java`            |
| `organizacao` | `PerfilDtoCoverageTest.java`                |
| `organizacao` | `UsuarioMapperCoverageTest.java`            |
| `organizacao` | `UsuarioCoverageTest.java`                  |
| `processo`    | `ProcessoControllerCoverageTest.java`       |
| `processo`    | `EventoProcessoListenerCoverageTest.java`   |
| `processo`    | `ProcessoDetalheMapperCoverageTest.java`    |
| `processo`    | `ProcessoMapperCoverageTest.java`           |
| `processo`    | `ProcessoFacadeCoverageTest.java`           |
| `seguranca`   | `LoginControllerCoverageTest.java`          |
| `subprocesso` | `SubprocessoCadastroControllerCoverageTest.java` |
| `subprocesso` | `AnaliseValidacaoDtoCoverageTest.java`      |
| `subprocesso` | `MapaAjusteMapperCoverageTest.java`         |
| `subprocesso` | `SubprocessoDetalheMapperCoverageTest.java` |
| `subprocesso` | `SituacaoSubprocessoCoverageTest.java`      |
| `subprocesso` | `SubprocessoFacadeCoverageTest.java`        |
| `subprocesso` | `SubprocessoCrudServiceCoverageTest.java`   |
| `subprocesso` | `SubprocessoValidacaoServiceCoverageTest.java` |
| `subprocesso` | `SubprocessoMapaWorkflowServiceCoverageTest.java` |

**Arquivos `*GapTest` (4):**
| Módulo        | Arquivo                                     |
| :------------ | :------------------------------------------ |
| `organizacao` | `UnidadeFacadeGapsTest.java`                |
| `seguranca`   | `FiltroJwtGapTest.java`                     |
| `seguranca`   | `GerenciadorJwtGapTest.java`                |
| `subprocesso` | `SituacaoSubprocessoGapTest.java`           |

---

### 1.3. Redundância de Casos de Teste

Muitos casos de teste são duplicados entre os arquivos "originais" e os de "cobertura".

**Exemplo: `SituacaoSubprocesso`**
*   `SituacaoSubprocessoTest.java`:  Tabela de transições via `@CsvSource` com ~73 linhas.
*   `SituacaoSubprocessoCoverageTest.java`: Outra tabela de transições via `@CsvSource` com ~90 linhas. **Muitas são idênticas.**
*   `SituacaoSubprocessoGapTest.java`: Testa o método **privado** `podeIniciar` via `ReflectionTestUtils`. Isso é um *code smell* — se o método privado precisa de teste, ele deveria ser extraído ou os testes do método público deveriam cobri-lo.

---

### 1.4. "Ginástica" de Cobertura (Low-Value Tests)

O arquivo `CoberturaExtraTest.java` (79 linhas) é um exemplo claro de teste criado **apenas para satisfazer métricas**, sem valor de verificação comportamental:

```java
// Trecho de CoberturaExtraTest.java
@Test
void deveInstanciarErros() {
    assertThat(new ErroEstadoImpossivel("msg")).isNotNull();
    assertThat(new ErroConfiguracao("msg")).isNotNull();
    // ...apenas instancia objetos para cobertura
}

@Test
void deveInstanciarModelos() {
    Competencia c = Competencia.builder().descricao("desc").mapa(new Mapa()).build();
    c.setCodigo(1L);
    assertThat(c.getCodigo()).isEqualTo(1L);
    // ...apenas chama getters/setters
}
```

**Se um construtor ou método existe, ele deve ser testado no contexto de seu uso real, não em um arquivo "pega-tudo".**

---

### 1.5. Inconsistência de Estilo

Não há um padrão claro adotado:

| Estilo                              | Uso Atual                               |
| :---------------------------------- | :-------------------------------------- |
| `@Nested` classes                   | ~59 arquivos                            |
| Flat tests (sem `@Nested`)          | ~189 arquivos                           |
| AssertJ (`assertThat`)              | ~145+ arquivos (maioria)                |
| JUnit Assertions (`assertTrue`, etc) | ~17 arquivos (minoria, inconsistente)   |

---

### 1.6. Testes de Integração Pesados (Fragilidade)

Os testes em `sgc.integracao` (45 arquivos CDU) possuem `setUp` acoplados a detalhes de implementação do banco:

```java
// Trecho de CDU01IntegrationTest.java
@BeforeEach
void setUp() {
    try {
        jdbcTemplate.execute("ALTER TABLE SGC.VW_UNIDADE ALTER COLUMN CODIGO RESTART WITH 10000");
    } catch (DataAccessException ignored) {}
    // ...
    jdbcTemplate.update(
        "INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, perfil, unidade_codigo) VALUES (?, ?, ?)",
        usuarioAdmin.getTituloEleitoral(), "ADMIN", unidadeAdmin.getCodigo());
}
```

Isso causa:
*   **Fragilidade**: Renomear uma coluna ou tabela quebra múltiplos testes.
*   **Lentidão**: `@SpringBootTest` é pesado; cada teste inicializa contexto.
*   **Não portabilidade**: Sintaxe de `ALTER COLUMN ... RESTART` é específica do H2.

---

## 2. Impactos na Manutenção

1.  **Dificuldade de Localização**: Quando um comportamento muda, o desenvolvedor precisa procurar em 3 ou 4 arquivos onde a asserção correspondente está.
2.  **Refatoração Dolorosa**: Mudar a assinatura de um método na Facade exige atualizar mocks e verificações em múltiplos arquivos dispersos.
3.  **Falsa Sensação de Segurança**: A cobertura de 100% esconde o fato de que muitos testes validam "se o código chamou o código" em vez de "se o negócio funciona".
4.  **Custo de Onboarding**: Novos desenvolvedores (humanos ou IA) perdem tempo entendendo a estrutura dispersa.

---

## 3. Análise de Severidade por Módulo

| Módulo         | Severidade | Justificativa                                                                 |
| :------------- | :--------- | :---------------------------------------------------------------------------- |
| `subprocesso`  | 🔴 Alta    | Maior fragmentação (44 arquivos, múltiplos `*CoverageTest`). Centro da lógica de negócio. |
| `processo`     | 🟠 Média   | 6 arquivos para `ProcessoFacade`. Lógica crítica de workflow.                 |
| `mapa`         | 🟠 Média   | `MapaManutencaoService` tem arquivo de 40KB + `CoverageTest`.                 |
| `organizacao`  | 🟡 Baixa   | Fragmentação presente, mas menor impacto no core do negócio.                  |
| `seguranca`    | 🟡 Baixa   | `GapTest` para JWT são pequenos e focados.                                    |
| `integracao`   | 🟠 Média   | Setup manual é frágil, mas CDUs são estáveis e bem nomeados.                  |

---

## 4. Recomendações de Melhoria (Resumo)

### Fase 1: Consolidação (Curto Prazo)
*   Mesclar arquivos `*CoverageTest` e `*GapTest` nos seus respectivos arquivos principais (`*Test`).
*   Eliminar duplicidade de casos de teste.
*   Padronizar estilo: `@Nested` para organização, AssertJ para asserções.

### Fase 2: Qualidade (Médio Prazo)
*   Eliminar `CoberturaExtraTest.java` distribuindo seus testes para os contextos apropriados.
*   Focar em testes de domínio, não apenas delegação.

### Fase 3: Infraestrutura (Longo Prazo)
*   Abstrair setup de dados em fixtures reutilizáveis para testes de integração.
*   Adotar convenção de nomenclatura clara (`*Test`, `*IntegrationTest`, `*E2ETest`).

---

## Conclusão

O projeto atingiu a excelência métrica (100%), mas sacrificou a manutenibilidade. A organização atual é um subproduto de uma abordagem "aditiva" à cobertura. É hora de uma "faxina" para consolidar o conhecimento disperso nos testes.
