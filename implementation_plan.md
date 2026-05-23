# Plano de Melhoria de Cobertura — Testes de Integração vs. Unitários

> [!NOTE]
> Questões abertas respondidas pelo usuário em 23/05/2026. Plano atualizado com as decisões.

## Contexto

O relatório de cobertura exclusiva de integração revela o seguinte cenário:

| Métrica | Global (unit+integ) | Apenas Integração | Queda |
|---|---|---|---|
| Instruções | 99,29% | 76,17% | -23,12% |
| Linhas | 99,67% | 77,76% | -21,91% |
| **Branches** | **99,37%** | **55,91%** | **-43,46%** |

A queda de **43,46 pontos percentuais em branches** é o sinal mais importante. Ela indica que quase metade dos
desvios de fluxo do sistema **só é exercitada em testes unitários com mocks** — muitos dos quais provavelmente
testam cenários impossíveis de atingir via API real. Isso gera uma ilusão de cobertura que não protege o sistema.

---

## Análise: Dois problemas distintos

### Problema 1 — Testes unitários artificiais (cobertura de branches impossível)

Os testes unitários cobrem **43,46% de branches extras** que os testes de integração não alcançam. A maior parte
dessa diferença não vem de comportamentos legítimos testados apenas em unidade — vem de:

1. **Verificações defensivas de nulo desnecessárias** no código de produção que nunca ocorrem via API real.
   O JaCoCo registra um branch para cada `if (x != null)` — se `x` nunca puder ser nulo em produção, é código
   morto disfarçado de proteção.

2. **Testes de reflexão que exercitam métodos privados** diretamente, contornando a API pública e criando
   cenários impossíveis de reproduzir via chamada real. Identificados em **9 arquivos** (49 ocorrências totais):

   | Arquivo de Teste | Ocorrências de Reflexão |
   |---|---|
   | `RelatorioFacadeTest.java` | 15 |
   | `ProcessoServiceTest.java` | 8 |
   | `AtribuicaoTemporariaViewsE2eAspectIntegrationTest.java` | 8 |
   | `SubprocessoNotificacaoServiceTest.java` | 6 |
   | `UnidadeHierarquiaServiceTest.java` | 4 |
   | `LimitadorTentativasLoginTest.java` | 3 |
   | `CadastroFluxoServiceTest.java` | 2 |
   | `AlertaFacadeTest.java` | 2 |
   | `RegistroSseEmitterTest.java` | 1 |

3. **Comentários explícitos de "branch hunting"** — já encontramos em `ProcessoServiceTest.java` e
   `ImpactoMapaServiceTest.java` comentários como `// branch 419 (DIAGNOSTICO)`, `// branch 570 (HOMOLOGAR)`,
   `// branch 157 false` — padrão que indica testes criados para satisfazer uma métrica, não um comportamento.

### Problema 2 — Caminhos felizes ainda não cobertos por integração

Os testes de integração (575 testes) deixam **387 métodos** não cobertos. Há módulos inteiros com cobertura de
integração zero ou muito baixa para funcionalidades de caminho feliz:

| Classe / Módulo | Situação |
|---|---|
| `ValidadorDadosOrganizacionais` | **0% cobertura** — 42 métodos, 1.201 instruções. Funcionalidade de diagnóstico organizacional completamente sem cobertura de integração |
| `AtividadeFacade` | **0% cobertura** — 15 métodos (CRUD de atividades e conhecimentos): criar, atualizar, excluir, listar, obter |
| `ProcessoExclusaoCompletaService` | **0% cobertura** — 6 métodos. Serviço `@Profile("hom")` não testável em `integrationTest` |
| `AlertaFacade` (4 métodos) | `alertasPorUsuario`, `listarNaoLidos`, `obterDataHoraLeitura`, `marcarComoLidos` — funcionalidades de alerta sem cobertura |
| `NotificacaoService` (9 métodos) | `listarPorSubprocesso`, `listarTodasAdmin`, `listarResumoSubprocessosAtivos`, `reenfileirarFalhas`, `reenviarPorCodigo` — sem cobertura |
| `SubprocessoController` (18 métodos) | `listar`, `obterStatus`, `excluir`, `obterContexto*`, `listarAtividadesParaImportacao`, `validarCadastro` + 12 outros |
| `UnidadeHierarquiaService` (16 métodos) | `buscarArvore*`, `buscarSiglas*`, `carregarTitulosResponsavel`, `copiarComResponsavelAtual` |
| `RestExceptionHandler` | Handlers de `HttpMessageNotReadable`, `ConstraintViolation`, `IllegalArgument`, `ErroInterno`, `GenericException` não exercitados |
| `FiltroMonitoramentoHttp` | 8 de 13 métodos sem cobertura: `obterDescricaoHttpAtual`, `registrarJavaLento`, `deveLogarHttp`, `logarJavaLento` |
| `LimitadorTentativasLogin` | `encontrarIpMaisAntigo`, `limparTentativasAntigas` — lógica de segurança sem cobertura de integração |

#### Cobertura de branches por módulo (integração):

| Módulo | Branch% |
|---|---|
| `sgc/organizacao` (model) | **10,8%** |
| `sgc/comum/erros` | **14,3%** |
| `sgc/mapa` | **16,7%** |
| `sgc/comum/util` | **18,8%** |
| `sgc/alerta/dto` | **0,0%** |
| `sgc/seguranca/login` | **38,5%** |
| `sgc/subprocesso/model` | **42,9%** |
| `sgc/organizacao/service` | **50,7%** |
| `sgc/feedback` | **50,0%** |

---

## Decisões tomadas

> [!NOTE]
> **`ValidadorDadosOrganizacionais`** → **Adicionar testes de integração.**
> A classe é chamada por `UnidadeController.GET /api/unidades/diagnostico-organizacional` e serve para
> apontar problemas no cadastro de unidades (ex: unidade sem responsável). É funcionalidade ativa e
> relevante para os usuários — portanto deve ser coberta por testes de integração.

> [!NOTE]
> **`ProcessoExclusaoCompletaService` (`@Profile("hom")`)** → **Testes unitários com mocks de `JdbcTemplate`.**
> O serviço é usado pelo próprio usuário de testes para apagar processos criados durante os testes.
> Criar `ProcessoExclusaoCompletaServiceTest` com mocks de `JdbcTemplate` e `CacheManager`, validando
> a sequência de DELETEs e a chamada ao `limparCaches()`.

> [!NOTE]
> **Verificações defensivas de nulo em `ProcessoService`** → **Guiar-se pelo modelo JPA e DDL.**
> Investigação confirmou que **algumas verificações são legítimas**: `Subprocesso.mapa` é nullable no DDL
> (`mapa_vigente_codigo` sem `NOT NULL`) e `data_limite_etapa2` também é nullable no modelo.
> **Regra de ouro:** se o campo é `NOT NULL` no banco, a verificação de nulo no Java é código morto
> e o teste unitário que a exercita deve ser removido. Se o nulo for real, é erro grave — lançar exceção,
> não fazer fallback silencioso.

---

## Plano de Execução

A abordagem é dividida em **2 frentes paralelas** por prioridade:

---

### Frente A — Eliminar cobertura artificial nos testes unitários

Objetivo: remover testes que usam reflexão para testar métodos privados onde o comportamento já é coberto via
API pública, ou onde o cenário testado é impossível de atingir em produção.

#### A1. `RelatorioFacadeTest.java` (15 ocorrências de reflexão)
**Risco: MÉDIO** — Reflexão em `compararSegmentosTexto`, `formatarSituacaoPdf`, `adicionarIdentificacaoUnidadeSemMapa`,
`adicionarListaUnidadesSemMapa`, `filtrarUnidadesExibidas`, `ehTextoSecretaria`, `separarSegmentos`.

Ação: Para cada método privado, verificar se existe um teste de integração em `RelatorioFacade` (cobertura de integração: 58,8% branch) que já exercite o mesmo caminho. Se sim, remover o teste de reflexão. Se não, adicionar teste via endpoint de relatório.

#### A2. `ProcessoServiceTest.java` (8 ocorrências)
**Risco: ALTO** — Reflexão em `efetivarInicioSubprocessos`, `carregarUnidadesPorCodigo`, `criarNotificacoesInicioProcesso`, `isSituacaoCadastro`.

Os comentários `// branch 419`, `// branch 570`, `// branch 441` já indicam testes criados para satisfazer
o JaCoCo. Ação: remover esses testes e verificar se os branches reais são cobertos pelos testes de integração
existentes (CDU10-CDU15 cobrem fluxos de processo).

#### A3. `SubprocessoNotificacaoServiceTest.java` (6 ocorrências)
Reflexão em `obterTemplateObrigatorio` e `criarAssunto`. Verificar se os testes de integração de email
(`SubprocessoServiceEmailIntegrationTest`) já exercitam esses caminhos.

#### A4. `UnidadeHierarquiaServiceTest.java` (4 ocorrências)
Reflexão em `copiarArvore`. Verificar cobertura via API pública (`buscarArvore*`).

#### A5. `LimitadorTentativasLoginTest.java` (3 ocorrências)
Reflexão para testar `encontrarIpMaisAntigo` e acessar campo interno `tentativasPorIp`. O construtor
package-private já existe — substituir por teste via construtor, não via reflexão.

#### A6. `AlertaFacadeTest.java` e `CadastroFluxoServiceTest.java` (2+2 ocorrências)
Reflexão em `obterUnidadeObrigatoria` e `normalizarTexto`. Avaliar se o caminho público já cobre.

#### A7. `AtribuicaoTemporariaViewsE2eAspectIntegrationTest.java` (8 ocorrências)
Reflexão em `buscarValorObrigatorio`, `removerPerfisDeResponsabilidadeAnteriores`, `restaurarResponsabilidadeTitular`.
Esses são métodos privados do aspecto — verificar se existe CDU que exercite o fluxo completo de atribuição temporária.

---

### Frente B — Adicionar cobertura de integração nos caminhos felizes descobertos

Objetivo: cobrir com testes de integração os fluxos de caminho feliz que atualmente só existem em unitários
(ou que não existem em nenhum teste).

#### B1. `AtividadeFacade` — Prioridade ALTA (0% cobertura, 182 linhas de código de produção)

Adicionar testes em `CDU` existente (verificar qual CDU cobre CRUD de atividades — possivelmente CDU06 ou CDU07)
ou criar `AtividadeFacadeIntegrationTest`:
- `criarAtividade` com dados válidos → verifica que a atividade existe no banco
- `atualizarAtividade` → verifica campos alterados
- `excluirAtividade` → verifica remoção
- `criarConhecimento` / `atualizarConhecimento` / `excluirConhecimento`
- `listarConhecimentosPorAtividade`

#### B2. `AlertaFacade` — Prioridade ALTA (45,3% branch, 4 métodos de caminho feliz não cobertos)

Os CDU de alerta (CDU20-CDU22 ou similar) precisam ser verificados. Adicionar cenários:
- `alertasPorUsuario` — listar alertas de um usuário com alertas existentes
- `listarNaoLidos` — usuário com alertas não lidos
- `marcarComoLidos` — marcar um alerta como lido

#### B3. `NotificacaoService` (admin) — Prioridade MÉDIA

`listarTodasAdmin`, `listarResumoSubprocessosAtivos`, `reenfileirarFalhasDefinitivasPorSubprocesso`, `reenviarPorCodigo`:
Adicionar testes de integração para esses fluxos administrativos de notificação.

#### B4. `SubprocessoController` (18 métodos não cobertos) — Prioridade ALTA

Os testes de integração `SubprocessoFluxoIntegrationTest`, `SubprocessoServiceIntegrationTest` existem mas
não cobrem vários endpoints do controller. Verificar lacunas e adicionar cenários para:
- `listar` (GET de listagem)
- `obterStatus`
- `excluir` (DELETE/excluir endpoint)
- `obterContextoCadastroAtividades`
- `validarCadastro`
- `iniciarRevisaoCadastro` / `cancelarInicioRevisaoCadastro`
- `obterMapaCompleto` / `salvarMapaCompleto`
- `criarAnaliseCadastro` / `criarAnaliseValidacao`

#### B5. `UnidadeHierarquiaService` (16 métodos não cobertos) — Prioridade MÉDIA

Adicionar testes de integração para os endpoints de hierarquia que exercitem:
- `buscarArvoreComElegibilidade` (predicate variant)
- `buscarSiglasSubordinadas`
- `buscarSiglaSuperior`
- `carregarTitulosResponsavel`

#### B6. `RestExceptionHandler` — Prioridade MÉDIA (7,7% branch de integração)

Adicionar testes de integração que provoquem cada tipo de exceção via HTTP real:
- `ConstraintViolationException` — enviar payload que viola `@NotNull`/`@Size`
- `HttpMessageNotReadable` — enviar JSON malformado no body
- `IllegalArgumentException` — entrada inválida que não é validada pelo Bean Validation
- `ErroInterno` — simular erro de serviço
- Rota não encontrada (`NoResourceFoundException`)

#### B7. `FiltroMonitoramentoHttp` — Prioridade BAIXA (infraestrutura)

A maioria dos métodos não cobertos são de logging/monitoramento. Verificar se é possível exercitar via teste
de integração que faça requisições HTTP lentas o suficiente para disparar `deveLogarHttp` e `logarJavaLento`.

#### B8. `SituacaoSubprocesso.transicaoDiagnostico` — Prioridade MÉDIA

Este método (`transicaoDiagnostico`) não é coberto pela integração. Verificar qual CDU representa o
fluxo de diagnóstico e adicionar um cenário que exercite essa transição específica.

#### B9. `ValidadorDadosOrganizacionais` — Prioridade ALTA (decisão: teste de integração)

Endpoint: `GET /api/unidades/diagnostico-organizacional` (em `UnidadeController`), acessível por admin.
A classe valida consistências do cadastro organizacional lido das views (unidades sem responsável,
perfis inválidos, usuários duplicados, etc.). Criar `ValidadorDadosOrganizacionaisIntegrationTest`:
- Cenário com cadastro válido → `diagnosticar()` retorna lista vazia de violações
- Cenário com unidade sem responsável → retorna violação com mensagem adequada
- Cenário com usuário referenciado inexistente → retorna violação
- Verificar que o endpoint retorna HTTP 200 com o DTO esperado

#### B10. `ProcessoExclusaoCompletaService` — Prioridade MÉDIA (decisão: teste unitário)

Criar `ProcessoExclusaoCompletaServiceTest` com mocks:
- `processoRepo.existsById()` retorna `true` → verifica sequência completa de `jdbcTemplate.update()` na ordem correta
- `existsById()` retorna `false` → verifica lançamento de `ErroEntidadeNaoEncontrada`
- Verificar que `cacheManager.getCache(*)` e `cache.clear()` são invocados ao final
- Cenário `tabelaExiste()` retorna `false` → os `DELETE` condicionais não são executados

---

## Ordem de Execução Recomendada

1. **Sprint 1** (impacto imediato em branches):
   - A2: Remover branch-hunting em `ProcessoServiceTest` (reduz ruído nos unitários)
   - A1: Avaliar reflexão em `RelatorioFacadeTest` (maior número de ocorrências)
   - B1: Adicionar cobertura de `AtividadeFacade` (0% cobertura, funcionalidade completa)
   - B6: `RestExceptionHandler` (aumenta branch de integração com testes simples e diretos)

2. **Sprint 2**:
   - A3, A5: `SubprocessoNotificacaoServiceTest`, `LimitadorTentativasLoginTest`
   - B2: `AlertaFacade` (4 métodos de caminho feliz)
   - B4: `SubprocessoController` (18 métodos — maior lacuna absoluta)

3. **Sprint 3**:
   - B5: `UnidadeHierarquiaService`
   - B3: `NotificacaoService` admin
   - B8: `SituacaoSubprocesso.transicaoDiagnostico`
   - A4, A6, A7: ocorrências menores de reflexão

4. **Sprint 4** (itens com decisão agora fechada):
   - B9: `ValidadorDadosOrganizacionais` → teste de integração via `GET /api/unidades/diagnostico-organizacional`
   - B10: `ProcessoExclusaoCompletaService` → teste unitário com mocks de `JdbcTemplate` e `CacheManager`

---

## Verificação

Ao final de cada sprint:

```bash
# Rodar testes de integração e regenerar relatório
./gradlew --no-daemon --no-configuration-cache :backend:integrationTest jacocoIntegrationTestReport

# Verificar cobertura de branches específicos
# (comparar com baselines deste documento)

# Rodar testes unitários para garantir que nenhuma remoção quebrou algo
./gradlew --no-daemon --no-configuration-cache :backend:test
```

**Baseline atual (pré-execução)**:
- Branches de integração: **55,91%** (1.329 / 2.377)
- Métodos de integração: **78,20%** (1.388 / 1.775)
- Classes de integração: **88,41%** (267 / 302)

**Meta após Sprint 1**: branches de integração ≥ 62%
**Meta após Sprint 2**: branches de integração ≥ 68%
**Meta após Sprint 3**: branches de integração ≥ 72%
