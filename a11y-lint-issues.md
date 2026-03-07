# Accessibility Lint Issues

There are multiple occurrences of the `vuejs-accessibility/label-has-for` error remaining after the fix run. These are largely due to BootstrapVueNext form elements mapping and how the rule interprets them. According to the prompt, we should document remaining manual issues.

- `frontend/src/components/atividades/AtividadeItem.vue`: Form label must have an associated control
- `frontend/src/components/atividades/CadAtividadeForm.vue`: Form label must have an associated control
- `frontend/src/components/atividades/ImportarAtividadesModal.vue` (3 occurrences)
- `frontend/src/components/comum/InlineEditor.vue`
- `frontend/src/components/mapa/AceitarMapaModal.vue`
- `frontend/src/components/mapa/CriarCompetenciaModal.vue` (2 occurrences)
- `frontend/src/components/mapa/ModalMapaDisponibilizar.vue` (2 occurrences)
- `frontend/src/components/processo/ProcessoFormFields.vue` (3 occurrences)
- `frontend/src/components/processo/SubprocessoModal.vue`
- `frontend/src/components/unidade/UnidadeTreeNode.vue`
- `frontend/src/views/AtribuicaoTemporariaView.vue` (4 occurrences)
- `frontend/src/views/CadastroVisualizacaoView.vue` (2 occurrences)
- `frontend/src/views/LoginView.vue` (3 occurrences)
- `frontend/src/views/MapaVisualizacaoView.vue` (3 occurrences)
- `frontend/src/views/RelatoriosView.vue` (3 occurrences)
- `frontend/src/views/SubprocessoView.vue`
