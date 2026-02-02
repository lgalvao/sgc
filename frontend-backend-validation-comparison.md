# Frontend vs Backend Validation Audit

## Backend Validations (DTOs)
| File | Line | Annotation | Code |
|------|------|------------|------|
| CriarAnaliseRequest.java | 8 | @Size | `@Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")` |
| CriarAnaliseRequest.java | 11 | @Size | `@Size(max = 20, message = "Sigla da unidade deve ter no máximo 20 caracteres")` |
| CriarAnaliseRequest.java | 14 | @Size | `@Size(max = 12, message = "Título do usuário deve ter no máximo 12 caracteres")` |
| CriarAnaliseRequest.java | 17 | @Size | `@Size(max = 500, message = "Motivo deve ter no máximo 500 caracteres")` |
| ParametroRequest.java | 12 | @NotNull | `@NotNull(message = "O código do parâmetro é obrigatório")` |
| ParametroRequest.java | 15 | @NotBlank | `@NotBlank(message = "A chave não pode estar vazia")` |
| ParametroRequest.java | 16 | @Size | `@Size(max = 50, message = "A chave deve ter no máximo 50 caracteres")` |
| ParametroRequest.java | 21 | @NotBlank | `@NotBlank(message = "O valor não pode estar vazio")` |
| AtualizarAtividadeRequest.java | 12 | @NotBlank | `@NotBlank(message = "Descrição não pode ser vazia")` |
| AtualizarConhecimentoRequest.java | 12 | @NotBlank | `@NotBlank(message = "Descrição não pode ser vazia")` |
| CompetenciaMapaDto.java | 21 | @NotBlank | `@NotBlank(message = "Descrição da competência é obrigatória") @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres") @SanitizarHtml String descricao,` |
| CompetenciaMapaDto.java | 23 | @NotEmpty | `@NotEmpty(message = "Lista de atividades não pode ser vazia") List<Long> atividadesCodigos) {` |
| CriarAtividadeRequest.java | 13 | @NotNull | `@NotNull(message = "Código do mapa é obrigatório")` |
| CriarAtividadeRequest.java | 16 | @NotBlank | `@NotBlank(message = "Descrição não pode ser vazia")` |
| CriarConhecimentoRequest.java | 13 | @NotNull | `@NotNull(message = "Código da atividade é obrigatório")` |
| CriarConhecimentoRequest.java | 16 | @NotBlank | `@NotBlank(message = "Descrição não pode ser vazia")` |
| SalvarMapaRequest.java | 16 | @Nullable | `@Nullable @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres") @SanitizarHtml String observacoes,` |
| CriarAtribuicaoTemporariaRequest.java | 13 | @Size | `@Size(max = 500, message = "A justificativa deve ter no máximo 500 caracteres")` |
| AcaoEmBlocoRequest.java | 11 | @NotEmpty | `@NotEmpty(message = "Pelo menos uma unidade deve ser selecionada")` |
| AcaoEmBlocoRequest.java | 14 | @NotNull | `@NotNull(message = "A ação deve ser informada")` |
| AtualizarProcessoRequest.java | 19 | @NotBlank | `@NotBlank(message = "Preencha a descrição") @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres") @SanitizarHtml String descricao,` |
| AtualizarProcessoRequest.java | 21 | @NotNull | `@NotNull(message = "Tipo do processo é obrigatório") TipoProcesso tipo,` |
| AtualizarProcessoRequest.java | 23 | @NotNull | `@NotNull(message = "Preencha a data limite") @Future(message = "A data limite deve ser futura") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime dataLimiteEtapa1,` |
| AtualizarProcessoRequest.java | 25 | @NotEmpty | `@NotEmpty(message = "Pelo menos uma unidade participante deve ser incluída.") List<Long> unidades) {` |
| CriarProcessoRequest.java | 17 | @NotBlank | `@NotBlank(message = "Preencha a descrição") @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres") @SanitizarHtml String descricao,` |
| CriarProcessoRequest.java | 19 | @NotNull | `@NotNull(message = "Tipo do processo é obrigatório") TipoProcesso tipo,` |
| CriarProcessoRequest.java | 21 | @NotNull | `@NotNull(message = "Preencha a data limite") @Future(message = "A data limite deve ser futura") @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime dataLimiteEtapa1,` |
| CriarProcessoRequest.java | 23 | @NotEmpty | `@NotEmpty(message = "Pelo menos uma unidade participante deve ser incluída.") List<Long> unidades) {` |
| EnviarLembreteRequest.java | 11 | @NotNull | `@NotNull(message = "O código da unidade é obrigatório") Long unidadeCodigo) {` |
| IniciarProcessoRequest.java | 9 | @NotNull | `@NotNull(message = "O tipo do processo é obrigatório")` |
| AutenticarRequest.java | 16 | @NotNull | `@NotNull(message = "A senha é obrigatória.")` |
| AutenticarRequest.java | 17 | @Size | `@Size(max = 64, message = "A senha deve ter no máximo 64 caracteres.")` |
| EntrarRequest.java | 15 | @NotNull | `@NotNull(message = "O perfil é obrigatório.") @Size(max = 50, message = "O perfil deve ter no máximo 50 caracteres.") String perfil,` |
| EntrarRequest.java | 17 | @NotNull | `@NotNull(message = "O código da unidade é obrigatório.") Long unidadeCodigo) {` |
| AceitarCadastroRequest.java | 12 | @NotBlank | `@NotBlank(message = "As observações são obrigatórias") @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres") String observacoes) {` |
| AlterarDataLimiteRequest.java | 13 | @NotNull | `@NotNull(message = "A nova data limite é obrigatória")` |
| ApresentarSugestoesRequest.java | 15 | @NotBlank | `@NotBlank(message = "As sugestões são obrigatórias") @Size(max = 1000, message = "As sugestões devem ter no máximo 1000 caracteres") @SanitizarHtml String sugestoes) {` |
| CompetenciaRequest.java | 14 | @NotBlank | `@NotBlank(message = "A descrição da competência é obrigatória") String descricao,` |
| CompetenciaRequest.java | 16 | @NotEmpty | `@NotEmpty(message = "A competência deve ter pelo menos uma atividade associada") List<Long> atividadesIds) {` |
| CriarSubprocessoRequest.java | 17 | @NotNull | `@NotNull(message = "O código do processo é obrigatório") Long codProcesso,` |
| CriarSubprocessoRequest.java | 19 | @NotNull | `@NotNull(message = "O código da unidade é obrigatório") Long codUnidade,` |
| DevolverCadastroRequest.java | 13 | @NotBlank | `@NotBlank(message = "As observações são obrigatórias") @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres") String observacoes) {` |
| DevolverValidacaoRequest.java | 13 | @NotBlank | `@NotBlank(message = "A justificativa é obrigatória") @Size(max = 500, message = "A justificativa deve ter no máximo 500 caracteres") @SanitizarHtml String justificativa) {` |
| DisponibilizarMapaRequest.java | 14 | @NotNull | `@NotNull(message = "A data limite para validação é obrigatória.") @Future(message = "A data limite para validação deve ser uma data futura.") LocalDate dataLimite,` |
| HomologarCadastroRequest.java | 12 | @Size | `@Size(max = 500, message = "Observações devem ter no máximo 500 caracteres") String observacoes) {` |
| ImportarAtividadesRequest.java | 12 | @NotNull | `@NotNull(message = "O código do subprocesso de origem é obrigatório") Long codSubprocessoOrigem) {` |
| ProcessarEmBlocoRequest.java | 16 | @NotBlank | `@NotBlank(message = "A ação é obrigatória")` |
| ProcessarEmBlocoRequest.java | 19 | @NotEmpty | `@NotEmpty(message = "Pelo menos um subprocesso deve ser selecionado")` |
| ReabrirProcessoRequest.java | 12 | @NotBlank | `@NotBlank(message = "Justificativa é obrigatória")` |
| ReabrirProcessoRequest.java | 13 | @Size | `@Size(max = 500, message = "Justificativa deve ter no máximo 500 caracteres")` |
| SalvarAjustesRequest.java | 14 | @NotEmpty | `@NotEmpty(message = "A lista de competências não pode ser vazia") List<CompetenciaAjusteDto> competencias) {` |
| SubmeterMapaAjustadoRequest.java | 19 | @NotBlank | `@NotBlank(message = "A justificativa é obrigatória")` |
| SubmeterMapaAjustadoRequest.java | 20 | @Size | `@Size(max = 500, message = "A justificativa deve ter no máximo 500 caracteres")` |

## Frontend Validations (Vue)
| File | Line | Type | Code |
|------|------|------|------|
| AceitarMapaModal.vue | 38 | disabled_check | `:disabled="loading"` |
| AceitarMapaModal.vue | 47 | disabled_check | `:disabled="loading"` |
| CriarCompetenciaModal.vue | 80 | disabled_check | `:disabled="atividadesSelecionadas.length === 0 \|\| !novaCompetencia.descricao"` |
| DisponibilizarMapaModal.vue | 20 | vue_rule | `v-model="dataLimiteValidacao"` |
| DisponibilizarMapaModal.vue | 58 | disabled_check | `:disabled="loading"` |
| DisponibilizarMapaModal.vue | 67 | disabled_check | `:disabled="!dataLimiteValidacao"` |
| ImportarAtividadesModal.vue | 21 | disabled_check | `<fieldset :disabled="importando">` |
| ImportarAtividadesModal.vue | 60 | disabled_check | `:disabled="!processoSelecionado"` |
| ImportarAtividadesModal.vue | 111 | disabled_check | `:disabled="importando"` |
| ImportarAtividadesModal.vue | 118 | disabled_check | `:disabled="!atividadesSelecionadas.length \|\| importando"` |
| ModalAcaoBloco.vue | 17 | html_required | `<label for="dataLimiteBloco" class="form-label required">Data Limite</label>` |
| ModalAcaoBloco.vue | 18 | html_required | `<input id="dataLimiteBloco" v-model="dataLimite" type="date" class="form-control" required>` |
| ModalAcaoBloco.vue | 26 | disabled_check | `<input type="checkbox" class="form-check-input" :checked="todosSelecionados" :disabled="processando"...` |
| ModalAcaoBloco.vue | 36 | disabled_check | `<input v-model="selecionadosLocal" type="checkbox" class="form-check-input" :value="unidade.codigo" ...` |
| ModalAcaoBloco.vue | 47 | disabled_check | `<button type="button" class="btn btn-secondary" data-bs-dismiss="modal" :disabled="processando">Canc...` |
| ModalAcaoBloco.vue | 48 | disabled_check | `<button type="button" class="btn btn-primary" :disabled="processando \|\| selecionadosLocal.length ===...` |
| ModalConfirmacao.vue | 24 | disabled_check | `:disabled="loading"` |
| ModalConfirmacao.vue | 32 | disabled_check | `:disabled="loading \|\| okDisabled"` |
| SubprocessoModal.vue | 29 | disabled_check | `:disabled="loading"` |
| SubprocessoModal.vue | 36 | disabled_check | `:disabled="!novaDataLimite \|\| !isDataValida \|\| loading"` |
| UnidadeTreeNode.vue | 25 | disabled_check | `:disabled="!isHabilitado(unidade)"` |
| LoadingButton.vue | 4 | disabled_check | `:disabled="loading \|\| disabled"` |
| AutoavaliacaoDiagnostico.vue | 9 | disabled_check | `:disabled="!podeConcluir"` |
| CadAtividades.vue | 91 | disabled_check | `:disabled="loadingAdicionar"` |
| CadAtividades.vue | 100 | disabled_check | `:disabled="!codSubprocesso \|\| !permissoes?.podeEditarMapa \|\| !novaAtividade.trim()"` |
| CadAtribuicao.vue | 88 | disabled_check | `:disabled="isLoading"` |
| CadMapa.vue | 24 | disabled_check | `:disabled="competencias.length === 0"` |
| CadProcesso.vue | 25 | disabled_check | `:disabled="isFormInvalid \|\| isLoading"` |
| CadProcesso.vue | 34 | disabled_check | `:disabled="isFormInvalid"` |
| CadProcesso.vue | 46 | disabled_check | `:disabled="isLoading"` |
| CadProcesso.vue | 56 | disabled_check | `:disabled="isLoading"` |
| ConclusaoDiagnostico.vue | 44 | disabled_check | `:disabled="!botaoHabilitado"` |
| LoginView.vue | 48 | disabled_check | `:disabled="loginStep > 1"` |
| LoginView.vue | 75 | disabled_check | `:disabled="loginStep > 1"` |
| LoginView.vue | 85 | disabled_check | `:disabled="loginStep > 1"` |
| MonitoramentoDiagnostico.vue | 12 | disabled_check | `:disabled="!todosConcluiramAutoavaliacao"` |
| MonitoramentoDiagnostico.vue | 19 | disabled_check | `:disabled="!diagnostico?.podeSerConcluido"` |
| VisAtividades.vue | 93 | vue_rule | `v-model="mostrarModalValidar"` |
| VisAtividades.vue | 108 | vue_rule | `v-model="observacaoValidacao"` |
| VisMapa.vue | 207 | vue_rule | `v-model="mostrarModalValidar"` |
