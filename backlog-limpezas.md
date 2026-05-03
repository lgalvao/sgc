# Backlog de limpezas do frontend

## Estado atual (auditado em 2026-05-03 — terceira varredura)

- Gate de cruft: **ok**, 0 violações
- Lint: **0 erros, 0 warnings**
- Testes unitários: **1247 testes** em **151 arquivos**, todos passando
- `fallow dead-code`: **zerado**
- `fallow health`: **98 A** (subiu de 90.9 após cobertura dos hotspots)
- `fallow dupes`: **6 clone groups**, ~0.2% (houve melhora)

## Já concluído

### Infra e baseline

- Configuração do `fallow` em `frontend/.fallowrc.jsonc`
- Estabilização do loop de validação
- Lint zerado (0 erros, 0 warnings)

### Remoção de arquivos completamente mortos

- `frontend/src/services/alertaService.ts`
- `frontend/src/services/diagnosticoService.ts`
- `frontend/src/utils/csv.ts`, `validators.ts`, `styleUtils.ts`
- `frontend/src/constants/index.ts`, `situacoes.ts`
- `frontend/src/types/dtos.ts` (tipos migrados para módulos naturais)
- Dependências órfãs: `papaparse`, `@types/papaparse`, `zod`

### Cortes pequenos

- Poda de `MainNavbar.vue`
- Consolidação dos modais de observação de cadastro
- Limpeza de exports em `frontend/src/utils/index.ts`
- Consolidação dos modais do mapa em `frontend/src/components/mapa/modais/`
- Remoção de `BFormRadioGroup` não usado em `FeedbackModal.vue`
- `useFeedback.ts`: `console.error` → `logger.error`; linhas em branco vazias removidas
- `subprocessoAcoesAdministrativas.ts`: `confirmarEnviarLembrete` era `async` sem `await` e retornava `true` inconsistentemente — corrigido
- `AtribuicaoTemporariaView.vue`: `codUnidade = computed(() => props.codUnidade)` era ruído puro — removido
- `AtribuicaoTemporariaView.vue`: double null-check em `usuarioSelecionado` após `validarSubmissao` — removido
- `ProcessoCadastroView.vue`: alias `Processo as ProcessoModel` desnecessário — removido

### Fatiamentos estruturais concluídos

- `dateUtils.ts` → `frontend/src/utils/date/`
- `apiError.ts` → `frontend/src/utils/apiError/`
- `processoService.ts` → `frontend/src/services/processo/`
- `useAcesso.ts` → `frontend/src/composables/acesso/`
- `stores/subprocesso.ts` → `frontend/src/stores/subprocesso/`

### Cobertura de testes adicionada

- `frontend/src/utils/date/parsing.ts` — 19 testes, todos os branches cobertos
- `frontend/src/components/mapa/modais/CompetenciaEdicaoModal.vue` — 11 testes
- `frontend/src/components/processo/processoAcoes.ts` — 14 testes

---

## Pendente — achados da varredura de 2026-05-03

### P0 — Ruído e inconsistências simples (corte seguro, sem risco)

#### `NotificacoesAdminView.vue` (490 LOC)

- `TIPOS_NOTIFICACAO_LABELS`, `statusLabel`, `statusVariant` e `prioridadeStatus` são dicionários de constantes de domínio embutidos diretamente na view
- `statusLabel` e `statusVariant` fazem lookup separado no mesmo conjunto de keys — podem ser unificados em `obterStatusNotificacao(): { label, variant }`
- Candidato a extrair para `notificacaoService.ts` ou arquivo de constantes de domínio
- **Ação:** extrair as 4 funções/dicionários para `frontend/src/services/notificacaoService.ts` ou `notificacaoLabels.ts`

#### `AtribuicaoTemporariaView.vue` — `defineExpose` excessivo

- Expõe 14 itens internos (`router`, `notify`, `clear`, `validacaoSubmetida`, etc.) — cheiro de teste que acessa internals em vez de usar a API pública
- **Ação:** verificar se os testes unitários consomem esses campos; se sim, reescrever os testes para usar a interface pública; se não, remover o `defineExpose`

#### `useFeedback.ts` — `usuarioEmail: ''` fixo

- Linha 49: `usuarioEmail: ''` é sempre string vazia — campo sem uso real
- **Ação:** verificar se o backend usa o campo; se não, remover do `MetadadosFeedback`

#### `useFluxoSubprocesso.ts` — boolean flag `isRevisao`

- 5 funções recebem `isRevisao = false` como parâmetro booleano
- Padrão de boolean flag que inverte comportamento interno — viola o princípio de uma responsabilidade por função
- Cada função faz `isRevisao ? serviceFooRevisao(...) : serviceFoo(...)` — duplicação de lógica de despacho
- **Ação:** separar em funções nomeadas (`disponibilizarCadastro` / `disponibilizarRevisaoCadastro`) ou usar overload explícito

### P1 — Complexidade real confirmada pelo fallow

#### `apiError/normalizer.ts` — CRAP 380, cyclomatic 19

- `normalizarErro` tem 19 caminhos de execução em 23 linhas
- Cada branch é essencial (cancelamento, erro de rede, resposta HTTP, Error nativo, desconhecido)
- **Recomendação:** extrair cada case para função privada nomeada (`normalizarCancelamento`, `normalizarErroRede`, `normalizarRespostaHttp`, `normalizarErroGenerico`)
- Não toca no contrato público — só reorganiza internamente
- Adicionar testes unitários antes de modificar (não há spec hoje)

#### `ProcessoFormFields.vue:focarPrimeiroErro` — CRAP 156, cyclomatic 12

- Função de 27 linhas com 12 paths — lógica de foco sequencial em campos do formulário
- **Ação:** verificar se pode ser substituída por `useValidacaoFormulario().focarPrimeiroErroInvalido` que já existe globalmente

#### `axios-setup.ts:handleResponseError` — CRAP 156, cyclomatic 12

- 38 linhas, concentra autenticação + sessão expirada + notificação global + redirect
- Já tem waiver (258 linhas)
- **Ação:** extrair `tratarSessaoExpirada` e `tratarErroAutenticacao` como funções privadas nomeadas

#### `ProcessoCadastroView.vue:handleApiErrors` — CRAP 156, cyclomatic 12

- Função de 35 linhas que centraliza erros de campo + erros genéricos + logging + scroll
- **Ação:** já identificado no backlog anterior; extrair `aplicarErrosCampo` e `notificarErrosGenericos`

### P2 — Defensividade excessiva

#### `ProcessoCadastroView.vue` linha 292

- `if (!processo) { return; }` logo após `await processoService.obterDetalhesProcesso(...)` — o serviço nunca retorna null segundo o tipo (`Promise<Processo>`)
- **Ação:** remover o guard defensivo ou tipar corretamente o retorno

#### `ProcessoCadastroView.vue` linha 455

- `if (!processoEditando.value) { limparCampos(); }` dentro de um bloco que só executa se `processoEditando.value` existia (linha 447: `if (processoEditando.value)`)
- Branch morto — a condição interna nunca é verdadeira nesse contexto
- **Ação:** remover o `if` interno; chamar `limparCampos()` diretamente

#### `subprocessoAcoesAdministrativas.ts` linha 69

- `if (habilitarAlterarDataLimite.value)` antes de abrir o modal — mas o botão que chama isso já tem `:disabled="!habilitarAlterarDataLimite"` no template
- Guard duplo — o estado de UI já controla o acesso
- **Ação:** avaliar se o guard extra é necessário (e.g., ataques diretos via keyboard); se não, remover

### P3 — Inconsistências de padrão

#### `NotificacoesAdminView.vue` — textos hardcoded no script

- Linha 432: `"E-mail recolocado na fila de envio"` e linha 437: `"Erro ao reenviar e-mail"` — não usam `TEXTOS`
- **Ação:** mover para `TEXTOS.administracao.*` ou constante local

#### `useFluxoSubprocesso.ts` — imports com alias `service*`

- 11 imports com alias `as service*` (`aceitarCadastro as serviceAceitarCadastro`, etc.)
- Padrão verboso — o namespace `service` não agrega informação já que estão em `cadastroService`
- **Ação:** importar com `* as cadastroService` e `* as subprocessoService` para tornar o código mais legível

#### `useFeedback.ts` — `montarMetadados` exposta implicitamente

- `montarMetadados` é função privada mas o composable tem apenas 4 exports — ela fica "escondida" dentro da closure sem nome descritivo no retorno
- Padrão ok, mas `usuarioEmail: ''` (linha 49) é campo morto que deveria ser removido

#### `AtribuicaoTemporariaView.vue` — `erroUsuario` com dois papéis

- `erroUsuario` é usado tanto para erro de carregamento inicial (linha 218) quanto como erro de validação de campo de usuário (linha 228: `erroUsuario.value = ""`)
- Responsabilidade dupla — dificulta entender quando o erro é do server vs. da validação
- **Ação:** separar em `erroCarregamento` (para o mounted) e manter `erroUsuario` só para validação de campo

### P4 — Waivers estruturais ainda vivos (para rodadas futuras)

#### Views

- `MapaView.vue` (waiver 814) — já bem fatiado; avaliar extração de `MapaCompetenciasSection`
- `CadastroView.vue` (waiver 707)
- `NotificacoesAdminView.vue` (waiver 490) — candidato a extrair `NotificacoesFiltros` e `NotificacoesTabela`
- `ProcessoCadastroView.vue` (waiver 470)
- `AtribuicaoTemporariaView.vue` (waiver 312)

#### Componentes

- `ArvoreUnidades.vue` (waiver 471)
- `TreeTable.vue` (waiver 360)
- `ImportarAtividadesModal.vue` (waiver 321)
- `AtividadeItem.vue` (waiver 303)
- `ModalAcaoBloco.vue` (waiver 234)
- `ProcessoFormFields.vue` (waiver 229)
- `SubprocessoCards.vue` (waiver 216)
- `UnidadeTreeNode.vue` (waiver 203)
- `InlineEditor.vue` (waiver 197)
- `CompetenciaCard.vue` (waiver 191)
- `TreeRowItem.vue` (waiver 181)

#### Composables / stores / outros

- `useFluxoSubprocesso.ts` (waiver 174) — boolean flags `isRevisao` são o problema real aqui
- `stores/mapas.ts` (waiver 184)
- `stores/perfil.ts` (waiver 155)
- `useMapaCompetenciasMutacoes.ts` (waiver 154)
- `useBreadcrumbs.ts` (waiver 141)
- `useMapaSugestoes.ts` (waiver 132)
- `useCadastroRevisaoSemMudancas.ts` (waiver 130)
- `useFluxoMapa.ts` (waiver 122)
- `axios-setup.ts` (waiver 258)
- `constants/textos.ts` (waiver 450)
- `utils/treeUtils.ts` (waiver 153)
- `App.vue` (waiver 165)

---

## Ordem recomendada de execução

1. **P0 fácil:** `statusLabel` + `statusVariant` → `obterStatusNotificacao` em `NotificacoesAdminView.vue`
2. **P2 imediato:** remover branch morto `if (!processoEditando.value)` em `ProcessoCadastroView.vue`
3. **P2 imediato:** remover guard `if (!processo)` defensivo desnecessário em `carregarProcessoParaEdicao`
4. **P0:** extrair dicionários de notificação para `notificacaoService.ts` ou arquivo de labels
5. **P0:** textos hardcoded em `NotificacoesAdminView.vue` → `TEXTOS.administracao.*`
6. **P3:** imports com alias `service*` em `useFluxoSubprocesso.ts` → `* as cadastroService`
7. **P1:** adicionar testes para `apiError/normalizer.ts` antes de refatorar
8. **P1:** extrair cases de `normalizarErro` em funções privadas nomeadas
9. **P1:** extrair `tratarSessaoExpirada` de `axios-setup.ts`
10. **P3:** separar `erroCarregamento` de `erroUsuario` em `AtribuicaoTemporariaView.vue`
11. **P4:** atacar boolean flags `isRevisao` em `useFluxoSubprocesso.ts`
12. **P4:** waivers grandes (rodadas separadas)

## Guardrails

- Manter contratos públicos e `data-testid`
- Não recriar regra de acesso no frontend
- Preferir helper local antes de nova camada
- Apagar sobra liberada na mesma rodada
- Manter tudo em **português brasileiro**
- Sempre validar com o menor conjunto suficiente

## Comandos úteis

```bash
node etc/scripts/sgc.js frontend cruft validar
npx fallow health -r frontend
npx eslint frontend/src --ext .vue,.ts
npm --prefix frontend run typecheck
npm --prefix frontend run test:unit
pnpm -C frontend exec vitest run <arquivo> --reporter=verbose --no-color
```
