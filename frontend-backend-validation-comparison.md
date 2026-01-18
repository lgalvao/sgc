# Frontend vs Backend Validation Audit

## Backend Validations (DTOs)
| File | Line | Annotation | Code |
|------|------|------------|------|
| CriarAnaliseRequest.java | 8 | @Size | `@Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")` |
| CriarAnaliseRequest.java | 11 | @Size | `@Size(max = 20, message = "Sigla da unidade deve ter no máximo 20 caracteres")` |
| CriarAnaliseRequest.java | 14 | @Size | `@Size(max = 12, message = "Título do usuário deve ter no máximo 12 caracteres")` |
| CriarAnaliseRequest.java | 17 | @Size | `@Size(max = 500, message = "Motivo deve ter no máximo 500 caracteres")` |
| AutenticarRequest.java | 16 | @NotNull | `@NotNull(message = "O título é obrigatório.")` |
| AutenticarRequest.java | 17 | @Size | `@Size(max = 12, message = "O título deve ter no máximo 12 caracteres.")` |
| AutenticarRequest.java | 20 | @NotNull | `@NotNull(message = "A senha é obrigatória.")` |
| AutenticarRequest.java | 21 | @Size | `@Size(max = 64, message = "A senha deve ter no máximo 64 caracteres.")` |
| EntrarRequest.java | 16 | @NotNull | `@NotNull(message = "O título eleitoral é obrigatório.")` |
| EntrarRequest.java | 17 | @Size | `@Size(max = 20, message = "O título eleitoral deve ter no máximo 20 caracteres.")` |
| EntrarRequest.java | 20 | @NotNull | `@NotNull(message = "O perfil é obrigatório.")` |
| EntrarRequest.java | 21 | @Size | `@Size(max = 50, message = "O perfil deve ter no máximo 50 caracteres.")` |
| EntrarRequest.java | 24 | @NotNull | `@NotNull(message = "O código da unidade é obrigatório.")` |
| CompetenciaRequest.java | 18 | @NotBlank | `@NotBlank(message = "A descrição da competência é obrigatória")` |
| CompetenciaRequest.java | 21 | @NotEmpty | `@NotEmpty(message = "A competência deve ter pelo menos uma atividade associada")` |
| CriarSubprocessoRequest.java | 21 | @NotNull | `@NotNull(message = "O código do processo é obrigatório")` |
| CriarSubprocessoRequest.java | 24 | @NotNull | `@NotNull(message = "O código da unidade é obrigatório")` |
| DevolverCadastroRequest.java | 19 | @Size | `@Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")` |
| ReabrirProcessoRequest.java | 13 | @NotBlank | `@NotBlank(message = "A justificativa é obrigatória")` |
| DisponibilizarMapaRequest.java | 19 | @NotNull | `@NotNull(message = "A data limite para validação é obrigatória.")` |
| ImportarAtividadesRequest.java | 19 | @NotNull | `@NotNull(message = "O código do subprocesso de origem é obrigatório")` |
| SalvarAjustesRequest.java | 21 | @NotNull | `@NotNull(message = "A lista de competências é obrigatória")` |
| DevolverValidacaoRequest.java | 19 | @NotBlank | `@NotBlank(message = "A justificativa é obrigatória")` |
| HomologarCadastroRequest.java | 19 | @Size | `@Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")` |
| ApresentarSugestoesRequest.java | 21 | @NotBlank | `@NotBlank(message = "As sugestões são obrigatórias")` |
| SubmeterMapaAjustadoRequest.java | 20 | @NotBlank | `@NotBlank(message = "O campo 'observacoes' é obrigatório.")` |
| SubmeterMapaAjustadoRequest.java | 24 | @NotNull | `@NotNull(message = "O campo 'dataLimiteEtapa2' é obrigatório.")` |
| AlterarDataLimiteRequest.java | 17 | @NotNull | `@NotNull(message = "A nova data limite é obrigatória")` |
| AceitarCadastroRequest.java | 18 | @Size | `@Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")` |
| CriarProcessoRequest.java | 25 | @NotBlank | `@NotBlank(message = "Preencha a descrição")` |
| CriarProcessoRequest.java | 29 | @NotNull | `@NotNull(message = "Tipo do processo é obrigatório")` |
| CriarProcessoRequest.java | 32 | @NotNull | `@NotNull(message = "Preencha a data limite")` |
| CriarProcessoRequest.java | 37 | @NotEmpty | `@NotEmpty(message = "Pelo menos uma unidade participante deve ser incluída.")` |
| IniciarProcessoRequest.java | 9 | @NotNull | `@NotNull(message = "O tipo do processo é obrigatório")` |
| EnviarLembreteRequest.java | 15 | @NotNull | `@NotNull(message = "O código da unidade é obrigatório")` |
| AtualizarProcessoRequest.java | 26 | @NotBlank | `@NotBlank(message = "Preencha a descrição")` |
| AtualizarProcessoRequest.java | 30 | @NotNull | `@NotNull(message = "Tipo do processo é obrigatório")` |
| AtualizarProcessoRequest.java | 33 | @NotNull | `@NotNull(message = "Preencha a data limite")` |
| AtualizarProcessoRequest.java | 38 | @NotEmpty | `@NotEmpty(message = "Pelo menos uma unidade participante deve ser incluída.")` |
| AtualizarConhecimentoRequest.java | 12 | @NotBlank | `@NotBlank(message = "Descrição não pode ser vazia")` |
| CriarConhecimentoRequest.java | 13 | @NotNull | `@NotNull(message = "Código da atividade é obrigatório")` |
| CriarConhecimentoRequest.java | 16 | @NotBlank | `@NotBlank(message = "Descrição não pode ser vazia")` |
| AtualizarAtividadeRequest.java | 12 | @NotBlank | `@NotBlank(message = "Descrição não pode ser vazia")` |
| CriarAtividadeRequest.java | 13 | @NotNull | `@NotNull(message = "Código do mapa é obrigatório")` |
| CriarAtividadeRequest.java | 16 | @NotBlank | `@NotBlank(message = "Descrição não pode ser vazia")` |
| SalvarMapaRequest.java | 27 | @NotNull | `@NotNull(message = "Lista de competências não pode ser nula")` |
| CompetenciaMapaDto.java | 28 | @NotBlank | `@NotBlank(message = "Descrição da competência é obrigatória")` |
| CompetenciaMapaDto.java | 34 | @NotNull | `@NotNull(message = "Lista de atividades não pode ser nula")` |

## Frontend Validations (Vue)
| File | Line | Type | Code |
|------|------|------|------|
| VisAtividades.vue | 95 | vue_rule | `v-model="mostrarModalValidar"` |
| VisAtividades.vue | 114 | vue_rule | `v-model="observacaoValidacao"` |
| CadAtividades.vue | 63 | disabled_check | `:disabled="loadingValidacao"` |
| CadAtividades.vue | 105 | disabled_check | `:disabled="loadingAdicionar"` |
| CadAtividades.vue | 114 | disabled_check | `:disabled="!codSubprocesso \|\| !permissoes?.podeEditarMapa \|\| loadingAdicionar \|\| !novaAtividade.trim...` |
| SubprocessoView.vue | 67 | disabled_check | `:disabled="!justificativaReabertura.trim()"` |
| LoginView.vue | 48 | disabled_check | `:disabled="loginStep > 1"` |
| LoginView.vue | 75 | disabled_check | `:disabled="loginStep > 1"` |
| LoginView.vue | 85 | disabled_check | `:disabled="loginStep > 1"` |
| LoginView.vue | 146 | disabled_check | `:disabled="isLoading"` |
| CadMapa.vue | 38 | disabled_check | `:disabled="competencias.length === 0"` |
| AutoavaliacaoDiagnostico.vue | 10 | disabled_check | `:disabled="!podeConcluir"` |
| MonitoramentoDiagnostico.vue | 11 | disabled_check | `:disabled="!todosConcluiramAutoavaliacao"` |
| MonitoramentoDiagnostico.vue | 18 | disabled_check | `:disabled="!diagnostico?.podeSerConcluido"` |
| ConfiguracoesView.vue | 42 | disabled_check | `:disabled="removendoAdmin === admin.tituloEleitoral"` |
| ConfiguracoesView.vue | 115 | disabled_check | `<button type="submit" class="btn btn-success" :disabled="salvando">` |
| ConfiguracoesView.vue | 143 | disabled_check | `<button type="submit" class="btn btn-primary" :disabled="adicionandoAdmin">` |
| ConfiguracoesView.vue | 163 | disabled_check | `:disabled="removendoAdmin !== null"` |
| CadProcesso.vue | 99 | disabled_check | `:disabled="isFormInvalid \|\| isLoading"` |
| CadProcesso.vue | 107 | disabled_check | `:disabled="isFormInvalid \|\| isLoading"` |
| CadProcesso.vue | 120 | disabled_check | `:disabled="isLoading"` |
| CadProcesso.vue | 130 | disabled_check | `:disabled="isLoading"` |
| CadProcesso.vue | 160 | disabled_check | `:disabled="isLoading"` |
| CadProcesso.vue | 168 | disabled_check | `:disabled="isLoading"` |
| CadProcesso.vue | 192 | disabled_check | `:disabled="isLoading"` |
| CadProcesso.vue | 199 | disabled_check | `:disabled="isLoading"` |
| ConclusaoDiagnostico.vue | 43 | disabled_check | `:disabled="!botaoHabilitado"` |
| VisMapa.vue | 221 | vue_rule | `v-model="mostrarModalValidar"` |
| CriarCompetenciaModal.vue | 79 | disabled_check | `:disabled="atividadesSelecionadas.length === 0 \|\| !novaCompetencia.descricao"` |
| ModalAcaoBloco.vue | 17 | html_required | `<label for="dataLimiteBloco" class="form-label required">Data Limite</label>` |
| ModalAcaoBloco.vue | 18 | html_required | `<input id="dataLimiteBloco" v-model="dataLimite" type="date" class="form-control" required>` |
| ModalAcaoBloco.vue | 26 | disabled_check | `<input type="checkbox" class="form-check-input" :checked="todosSelecionados" :disabled="processando"...` |
| ModalAcaoBloco.vue | 36 | disabled_check | `<input v-model="selecionadosLocal" type="checkbox" class="form-check-input" :value="unidade.codigo" ...` |
| ModalAcaoBloco.vue | 47 | disabled_check | `<button type="button" class="btn btn-secondary" data-bs-dismiss="modal" :disabled="processando">Canc...` |
| ModalAcaoBloco.vue | 48 | disabled_check | `<button type="button" class="btn btn-primary" :disabled="processando \|\| selecionadosLocal.length ===...` |
| SubprocessoModal.vue | 35 | disabled_check | `:disabled="!novaDataLimite \|\| !isDataValida"` |
| ImportarAtividadesModal.vue | 21 | disabled_check | `<fieldset :disabled="importando">` |
| ImportarAtividadesModal.vue | 60 | disabled_check | `:disabled="!processoSelecionado"` |
| ImportarAtividadesModal.vue | 116 | disabled_check | `:disabled="!atividadesSelecionadas.length \|\| importando"` |
| DisponibilizarMapaModal.vue | 20 | vue_rule | `v-model="dataLimiteValidacao"` |
| DisponibilizarMapaModal.vue | 58 | disabled_check | `:disabled="loading"` |
| DisponibilizarMapaModal.vue | 66 | disabled_check | `:disabled="!dataLimiteValidacao \|\| loading"` |
| UnidadeTreeNode.vue | 25 | disabled_check | `:disabled="!isHabilitado(unidade)"` |
