# Backlog de limpezas do frontend

## Estado atual (auditado em 2026-05-03 — segunda varredura)

- Gate de cruft: **ok**
- Lint: **0 erros, 0 warnings** (após remoção de `BFormRadioGroup` em `FeedbackModal.vue`)
- Testes unitários: **1186 testes** em **146 arquivos**, todos passando
- `fallow dead-code`: **zerado**
- `fallow health`: **90.9**
- `fallow dupes`: **6 clone groups**, **153 linhas**, **0.8%**

## Ja concluido de verdade

### Infra e baseline

- configuração do `fallow` em `frontend/.fallowrc.jsonc`
- estabilização do loop de validação

### Remoção de arquivos completamente mortos

- `frontend/src/services/alertaService.ts`
- `frontend/src/services/diagnosticoService.ts`
- `frontend/src/utils/csv.ts`
- `frontend/src/utils/validators.ts`
- `frontend/src/constants/index.ts`
- `frontend/src/constants/situacoes.ts`
- `frontend/src/utils/styleUtils.ts`
- remoção de dependências órfãs: `papaparse`, `@types/papaparse`, `zod`
- `frontend/src/types/dtos.ts` — removido; tipos migrados para módulos naturais

### Cortes pequenos concluídos

- poda de `frontend/src/components/layout/MainNavbar.vue`
- consolidação dos modais de observação de cadastro
- limpeza de exports em `frontend/src/utils/index.ts`
- consolidação dos modais do mapa em `frontend/src/components/mapa/modais/`
- remoção de `BFormRadioGroup` não usado em `FeedbackModal.vue`

### Fatiamentos estruturais concluídos

- `dateUtils.ts` → `frontend/src/utils/date/`
- `apiError.ts` → `frontend/src/utils/apiError/`
- `processoService.ts` → `frontend/src/services/processo/`
- `useAcesso.ts` → `frontend/src/composables/acesso/`
- `stores/subprocesso.ts` → `frontend/src/stores/subprocesso/`

### Baseline do `fallow dead-code` zerado

### Lint zerado (0 erros, 0 warnings)

## Pendente real

### Prioridade 1 — hotspot `usePerfil.ts` ⭐

- `frontend/src/composables/usePerfil.ts`
- 38 LOC, 7 dependentes diretos
- fallow classifica como "high impact · effort:medium"
- ação: avaliar superfície, estreitar o que não é contrato real

### Prioridade 2 — testes faltantes em hotspots

- `frontend/src/utils/date/parsing.ts`
  - funções `analisarData` e `analisarStringData` sem cobertura de testes
  - adicionar testes antes de modificar

- `frontend/src/components/mapa/modais/CompetenciaEdicaoModal.vue`
  - 3 funções complexas sem cobertura de testes

- `frontend/src/components/processo/processoAcoes.ts`
  - 2 funções complexas sem cobertura de testes

### Prioridade 3 — duplicações reais (fallow dupes — 6 grupos, 153 linhas)

#### Fácil (corte claro e seguro)

- `frontend/src/services/relatoriosService.ts`
  - 6 linhas internas duplicadas: lógica de blob/download idêntica em dois métodos
  - extrair função privada `baixarPdf`

#### Moderado

- `frontend/src/components/mapa/modais/MapaDevolucaoModal.vue`
  x `frontend/src/components/mapa/modais/MapaSugestoesEnvioModal.vue`
  - 22 linhas (script completo); diferem em variante, label e textos

- `frontend/src/services/processo/types.ts`
  x `frontend/src/types/processo.ts`
  - 14 linhas; overlap entre `ProcessoDetalheResponseBackend` e `Processo`

#### Com cuidado

- `frontend/src/views/ProcessoCadastroView.vue`
  x `frontend/src/views/UnidadesView.vue`
  - 18 linhas; só mexer se cair junto com refatoração maior das views

- `frontend/src/components/unidade/ArvoreUnidades.vue`
  x `frontend/src/views/ProcessoCadastroView.vue`
  - 9 linhas

### Prioridade 4 — waivers estruturais ainda vivos

#### Views

- `frontend/src/views/MapaView.vue` (waiver 814)
- `frontend/src/views/CadastroView.vue` (waiver 707)
- `frontend/src/views/NotificacoesAdminView.vue` (waiver 490)
- `frontend/src/views/ProcessoCadastroView.vue` (waiver 470)
- `frontend/src/views/AtribuicaoTemporariaView.vue` (waiver 312)

#### Componentes

- `frontend/src/components/unidade/ArvoreUnidades.vue` (waiver 471)
- `frontend/src/components/comum/TreeTable.vue` (waiver 360)
- `frontend/src/components/atividades/ImportarAtividadesModal.vue` (waiver 321)
- `frontend/src/components/atividades/AtividadeItem.vue` (waiver 303)
- `frontend/src/components/processo/ModalAcaoBloco.vue` (waiver 234)
- `frontend/src/components/processo/ProcessoFormFields.vue` (waiver 229, atual 228 — waiver obsoleto)
- `frontend/src/components/processo/SubprocessoCards.vue` (waiver 216)
- `frontend/src/components/unidade/UnidadeTreeNode.vue` (waiver 203)
- `frontend/src/components/comum/InlineEditor.vue` (waiver 197)
- `frontend/src/components/mapa/CompetenciaCard.vue` (waiver 191)
- `frontend/src/components/comum/TreeRowItem.vue` (waiver 181)

#### Composables / stores / outros

- `frontend/src/stores/mapas.ts` (waiver 184)
- `frontend/src/stores/perfil.ts` (waiver 155)
- `frontend/src/composables/useFluxoSubprocesso.ts` (waiver duplicado: 174 e 312)
- `frontend/src/composables/useMapaCompetenciasMutacoes.ts` (waiver 154)
- `frontend/src/composables/useBreadcrumbs.ts` (waiver 141)
- `frontend/src/composables/useMapaSugestoes.ts` (waiver 132)
- `frontend/src/composables/useCadastroRevisaoSemMudancas.ts` (waiver 130)
- `frontend/src/composables/useFluxoMapa.ts` (waiver 122)
- `frontend/src/axios-setup.ts` (waiver 258)
- `frontend/src/constants/textos.ts` (waiver 450)
- `frontend/src/utils/treeUtils.ts` (waiver 153)
- `frontend/src/App.vue` (waiver 165)

## Guardrails das próximas rodadas

- manter contratos públicos e `data-testid`
- não recriar regra de acesso no frontend
- preferir helper local ou componente local antes de abrir nova camada
- apagar sobra liberada na mesma rodada
- manter tudo em **português brasileiro**
- sempre validar a rodada com o menor conjunto suficiente

## Ordem recomendada de execução

1. estreitar `usePerfil.ts` (maior ROI, hotspot confirmado)
2. extrair `baixarPdf` em `relatoriosService.ts` (6 linhas, risco zero)
3. adicionar testes para `date/parsing.ts` e `CompetenciaEdicaoModal.vue`
4. corrigir entrada duplicada de `useFluxoSubprocesso.ts` no arquivo de waivers
5. remover waiver obsoleto de `ProcessoFormFields.vue` (arquivo abaixo do limite)
6. atacar duplicações moderadas e waivers grandes

## Comandos úteis

```bash
node etc/scripts/sgc.js frontend cruft validar
npx fallow dead-code -r frontend
npx fallow dupes -r frontend
npx fallow health -r frontend
npm --prefix frontend run typecheck
npm --prefix frontend run test:unit
npx eslint frontend/src --ext .vue,.ts
```
