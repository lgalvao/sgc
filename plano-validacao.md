# Plano de validação e UX

## 1) Contexto e objetivo

Este documento consolida os padrões de validação hoje existentes no SGC, destaca inconsistências de experiência e propõe um plano prático para atingir consistência geral sem perder ergonomia em fluxos específicos.

O acompanhamento operacional da execução fica em [`tracking-validacao.md`](tracking-validacao.md).

Objetivo central:

- reduzir surpresa para usuárias(os) sobre **quando** um erro aparece;
- padronizar **onde** o erro aparece (campo, bloco, topo, toast);
- padronizar **como** a ação principal é bloqueada/liberada;
- preservar exceções intencionais quando elas melhoram UX.

---

## 2) Metodologia de análise

A análise foi feita sobre `frontend/src` com foco em:

- formulários e modais com campos obrigatórios;
- componentes com `BFormInvalidFeedback`, `BAlert`, `AppAlert`, `notify`;
- regras de bloqueio via `:disabled`;
- gatilhos de validação (`validacaoSubmetida`, `watch`, submissão, blur).

Amostra principal analisada:

- `ProcessoCadastroView` + `useProcessoForm` + `ProcessoFormFields`;
- `CadastroView` + `CadAtividadeForm` + `AtividadeItem`;
- `AtribuicaoTemporariaView`;
- `ParametrosView`;
- `CadastroVisualizacaoView`;
- `DisponibilizarMapaModal` e `CriarCompetenciaModal`;
- infraestrutura comum: `useFormErrors`, `apiError`.

---

## 3) Padrões encontrados (estado atual)

## Padrão A — validação no envio com erro inline por campo (progressiva)

### Como aparece

- `validacaoSubmetida` controla a exibição de mensagens de obrigatoriedade;
- mensagens sob o campo via `BFormInvalidFeedback`;
- em geral, erro só aparece após tentativa de ação.

### Onde ocorre

- `AtribuicaoTemporariaView` (usuário, datas e justificativa);
- `ParametrosView` (parâmetros numéricos);
- `CadAtividadeForm` (campo nova atividade);
- `CadastroVisualizacaoView` no modal de devolução.

### Observação de UX

É um padrão bom para evitar “gritar erro cedo demais”.

---

## Padrão B — bloqueio preventivo do botão por validade local (pré-envio)

### Como aparece

- botão principal desabilitado enquanto condições mínimas não são atendidas;
- geralmente combinado com validação no envio.

### Onde ocorre

- `ProcessoCadastroView` (`isFormInvalid` vindo de `useProcessoForm`);
- `AtribuicaoTemporariaView` (`formularioValido`);
- `CadastroView` (habilitação da disponibilização depende de estado e conteúdo);
- modais de mapa (`salvamentoDesabilitado`, `acao-desabilitada`, `ok-disabled`).

### Observação de UX

Reduz chamadas inválidas, mas sem uma regra única de feedback pode gerar “botão morto” sem explicação suficiente.

---

## Padrão C — validação reativa durante digitação (watch)

### Como aparece

- `watch` limpa erro ao editar;
- em alguns casos aplica regra de domínio imediatamente (ex.: data futura).
- também aparece de forma simples para limpar estado de erro após correção de campo curto.

### Onde ocorre

- `useProcessoForm` (data inválida/futura + limpeza de erros ao mudar campos);
- `CadAtividadeForm` (limpa a tentativa inválida quando o texto passa a ter conteúdo);
- `DisponibilizarMapaModal` (data futura e comparação com última data limite).

### Observação de UX

Bom para regras objetivas de formato/faixa, desde que mensagem seja específica.

---

## Padrão D — validação/orquestração de backend com mapeamento para campo + erro geral

### Como aparece

- normalização de erro (`normalizeError`);
- erros por campo (`erros`) mapeados com `useFormErrors`;
- erros sem campo exibidos em alerta global do formulário (`AppAlert`/`BAlert`).

### Onde ocorre

- `ProcessoCadastroView` (`handleApiErrors`);
- fluxo de cadastro de atividades (`validarCadastro` no `CadastroView` + erros por atividade e erro global).

### Observação de UX

Este é o padrão tecnicamente mais robusto, mas ainda não está generalizado para todos os formulários.

---

## Padrão E — erro em bloco/alerta em vez de erro por campo

### Como aparece

- `BAlert`/`AppAlert` para erro geral;
- em alguns pontos sem indicação explícita do campo causador.

### Onde ocorre

- `LoginView` (faltou preencher -> notificação global);
- `CadastroView` (erro global de validação e alerta por atividade);
- `DisponibilizarMapaModal` e `CriarCompetenciaModal` (campo + `generic`);
- `SubprocessoView` no modal de reabertura (justificativa obrigatória bloqueia a ação e ainda usa notificação, sem feedback inline no campo);
- vários views administrativos com erros de carregamento.

### Observação de UX

Adequado para falha sistêmica/integracional; inadequado como único canal para erro de preenchimento simples.

---

## 4) Inconsistências principais

1. **Momento de validação não uniforme**

- Em parte das telas, erro de obrigatoriedade aparece apenas após envio;
- em outras, a ação já nasce bloqueada sem indicar claramente o porquê;
- em outras, regra reativa aparece durante edição.

Impacto: curva de aprendizado por tela, em vez de padrão do sistema.

2. **Canal de erro misturado sem hierarquia clara**

- campo (`BFormInvalidFeedback`), alerta de bloco (`BAlert`), alerta estruturado (`AppAlert`) e toast/notify convivem sem matriz única de decisão.

Impacto: a mesma classe de problema aparece em lugares diferentes.

3. **Semântica distinta para erros de validação de backend**

- em processo existe tratamento forte de `erros`;
- em outros fluxos a mensagem volta mais global ou específica por domínio (atividade) sem contrato visual padronizado.

Impacto: inconsistência entre telas que parecem equivalentes.

4. **Divergência na visualização de erro por lista/coleção**

- em atividades, erro pode ser por card e com scroll automático;
- em outros cenários de coleção, o comportamento equivalente não é padrão.

Impacto: UX excelente em um fluxo e mediana em outro parecido.

5. **Bloqueio preventivo sem explicação contextual**

- Há botões principais/modal que ficam desabilitados por pré-condições locais sem resumo visível da pendência;
- exemplos confirmados: disponibilização de cadastro/mapa, criação de competência e reabertura de subprocesso.

Impacto: reduz envio inválido, mas pode deixar a pessoa inferir o motivo do bloqueio pela tela.

6. **Padrão “blur para validar” praticamente ausente**

- não há validação de campo orientada a `blur` como padrão do sistema;
- o `blur` encontrado em atribuição temporária é para comportamento de busca/autocomplete, não para validar domínio.

Impacto: a regra percebida por usuárias(os) não bate com hipótese de “validar no blur”.

---

## 5) Diretriz alvo de consistência (proposta)

Definir um **contrato único de validação** com 4 camadas:

1. **Camada de elegibilidade da ação (pré-envio):**

- botão principal pode ficar desabilitado por condições mínimas objetivas (vazio, formato incompleto, loading, permissão);
- sempre exibir ajuda contextual curta quando desabilitado (texto auxiliar próximo ao botão ou resumo de pendências).

2. **Camada de validação de campo (no envio + progressiva):**

- obrigatoriedade e regras de domínio aparecem no campo após primeira tentativa;
- após a primeira tentativa, revalidar em tempo real os campos tocados/editados;
- usar sempre `:state` + `BFormInvalidFeedback` para campo.

3. **Camada de validação estrutural/global de formulário:**

- erro sem `field` (backend) ou conflito global: `AppAlert` no topo do formulário;
- evitar toast para erro de preenchimento que a pessoa consegue corrigir na mesma tela.

4. **Camada de falha sistêmica/integracional:**

- manter `notify`/alerta global para rede, autorização e erro inesperado;
- manter log técnico no `logger`.

---

## 6) Padrão recomendado por tipo de cenário

## 6.1 Formulário simples (campos escalares)

Ex.: parâmetros, login, atribuição.

- `validacaoSubmetida` padrão;
- `BFormInvalidFeedback` por campo obrigatório;
- `LoadingButton` desabilitado por validade mínima + loading;
- `AppAlert` apenas para erro global não mapeável a campo.

## 6.2 Formulário com backend rico em suberros

Ex.: processo.

- manter `normalizeError` + `useFormErrors`;
- mapear aliases de campo quando backend divergir (`dataLimiteEtapa1` -> `dataLimite`);
- foco no primeiro campo inválido;
- `AppAlert` para erros genéricos sem `field`.

## 6.3 Lista editável (cards/itens)

Ex.: atividades e conhecimentos.

- erro de item deve aparecer no próprio card;
- erro global complementar apenas quando não houver item associado;
- scroll para primeiro item inválido;
- evitar toast como único canal de validação de item.

## 6.4 Modal com campos obrigatórios

Ex.: disponibilizar mapa, criar competência, devolução.

- manter confirmação desabilitada por critérios mínimos;
- mensagens inline no campo dentro do modal;
- erro `generic` no topo do modal quando aplicável;
- resetar estado ao abrir/fechar modal de forma previsível.

---

## 7) Casos em que vale quebrar a consistência (intencionalmente)

Estas exceções são saudáveis quando documentadas:

1. **Segurança/autenticação (Login):**

- pode priorizar mensagem global curta para evitar vazar regra de credencial;
- ainda assim, vale manter indicação de obrigatoriedade local para campos vazios.

2. **Fluxo de workflow com validação de negócio agregada:**

- quando o backend retorna problemas distribuídos (ex.: várias atividades), pode haver combinação de erro global + erro por item + scroll.

3. **Validação temporal crítica (datas):**

- feedback reativo imediato é melhor que esperar submit, pois reduz tentativa e erro.

4. **Ações irreversíveis ou operacionais (modais de confirmação):**

- botão pode iniciar desabilitado até informação mínima obrigatória (ex.: justificativa), mesmo antes de “submeter”.

---

## 8) Plano de implementação para convergir o frontend

## Fase 0 — Governança (rápida)

1. Criar guia de decisão “onde mostrar erro” no frontend (README de arquitetura UI).
2. Formalizar matriz de canais:
   - campo (`BFormInvalidFeedback`),
   - bloco (`AppAlert`),
   - toast (`notify`) apenas sistêmico.

## Fase 1 — Base compartilhada (concluída)

1. Evoluir `useFormErrors` para suportar:
   - erro global padronizado (`generic`),
   - utilitário de “primeiro erro”.
   - contrato direto em português, sem aliases permanentes.
2. Criar um pequeno composable de orquestração para formulários (`useValidacaoFormulario`) com:
   - `validacaoSubmetida`,
   - `podeSubmeter`,
   - `resumoPendencias` opcional.

## Fase 2 — Padronização por módulo (concluída)

1. **Processo**: manter como referência e extrair padrão reutilizável.
2. **Atribuição/Parâmetros/Login**: alinhados à mesma hierarquia visual de erro (campo > bloco > toast), preservando a exceção saudável de autenticação.
3. **Cadastro de atividades**: convergido para o mesmo padrão-base, combinando erro local inline, erro global contextual e erro por item sem falhas silenciosas relevantes.
4. **Modais de mapa**: estrutura de erro inline e `generic` já convergida; manter como referência para os fluxos restantes.

## Fase 3 — Acessibilidade e microcopy (majoritariamente concluída)

1. Garantir que todos os campos obrigatórios tenham um indicador visual claro (asterisco `*` ou equivalente) no label, antes mesmo da submissão.
2. Garantir foco no primeiro erro em todos os formulários principais.
3. Revisar linguagem das mensagens para padrão único (tom e ação esperada).
4. Garantir que campos inválidos tenham associação semântica com mensagem (ARIA/feedback), fechando os pontos residuais de componentes reutilizáveis.

## Fase 4 — Testes de regressão de UX (em andamento)

1. Criar suíte mínima de testes de comportamento de validação:
   - submit com campos vazios;
   - correção progressiva após primeira tentativa;
   - erro de backend por campo e erro global;
   - botão desabilitado/habilitado em transições.
2. Incluir cenários de modal e lista por item.

---

## 9) Checklist prático de consistência (para PR futuro)

Antes de concluir qualquer ajuste de formulário, validar:

- [ ] Existe critério claro e visível para botão desabilitado?
- [ ] Erro de campo aparece inline e no momento correto?
- [ ] Erro global sem campo aparece em `AppAlert`/bloco, não só em toast?
- [ ] Falha sistêmica está separada de falha de preenchimento?
- [ ] Há foco/rolagem para primeiro erro relevante?
- [ ] Mensagens estão em português claro e orientadas à ação?

---

## 10) Conclusão objetiva

O frontend já convergiu os principais formulários e modais para uma base compartilhada de validação (`useValidacaoFormulario`, foco no primeiro erro e feedback inline/contextual). O fluxo de cadastro de atividades, antes o principal hotspot remanescente, também já foi alinhado ao padrão-base. A convergência agora deve priorizar:

1. uma hierarquia fixa de exibição de erro;
2. um contrato comum de estado de validação;
3. exceções explícitas para login, datas críticas e validações agregadas por item.

Com isso, a UX fica previsível sem perder flexibilidade nos fluxos complexos.

---

## 11) Matriz completa dos formulários e views (frontend)

A matriz abaixo cobre todas as views em `frontend/src/views` e os formulários/componentes de entrada associados.

Legenda rápida:

- **Gatilho**: quando a validação acontece (submit, reativo, pré-condição);
- **Feedback**: como o erro é exibido (inline, alerta, toast, item);
- **Bloqueio**: se ação principal é desabilitada preventivamente;
- **Risco de inconsistência**: baixo / médio / alto para futura padronização.

| View / Formulário | Gatilho de validação predominante | Feedback de erro | Bloqueio preventivo | Risco de inconsistência |
|---|---|---|---|---|
| `LoginView` | Submit (checagem local de vazio) + resposta de backend | `notify`/`AppAlert` + alerta contextual de Caps Lock | Não bloqueia por vazio; apenas por etapa/loading | **Médio** |
| `ProcessoCadastroView` + `ProcessoFormFields` + `useProcessoForm` | Reativo (`watch` p/ data) + backend (`erros`) + submit | Inline por campo + `AppAlert` para erro global estruturado + foco no inválido | Sim (`isFormInvalid`) | **Baixo** (referência atual) |
| `CadastroView` + `CadAtividadeForm` + `AtividadeItem` | Submit (adicionar), validação de domínio via endpoint (`validar-cadastro`) e regras locais por item/edição inline | Erro por item (card), erro global (`BAlert`), erros inline locais e notificações sistêmicas | Sim apenas por estado de workflow, loading e elegibilidade real da disponibilização | **Baixo/Médio** |
| `CadastroVisualizacaoView` (modais de validar/devolver) | Submit no modal (devolução exige texto) | Inline em `BFormInvalidFeedback` + `notify` apenas para falhas sistêmicas/fluxo | Não depende mais de bloqueio preventivo do confirmar para a obrigatoriedade do campo | **Baixo/Médio** |
| `MapaView` (edição mapa) | Ações de workflow + submit em modais com `useValidacaoFormulario` + validação backend | Inline nos modais, `BAlert` local e `notify` sistêmico | Sim em ações de topo por loading/estado de workflow | **Baixo/Médio** |
| `MapaVisualizacaoView` (sugestões/validação/devolução) | Submit em modais + regras por estado de workflow | Inline/contextual nos modais + `notify` sistêmico | Sem `ok-disabled` residual para obrigatoriedade dos campos migrados | **Baixo/Médio** |
| `SubprocessoView` (alterar prazo/reabrir/lembrete) | Submit em modais + validação backend | `AppAlert` + `BAlert` + feedback inline nos campos obrigatórios | Sim em modais por loading/estado, sem bloqueio silencioso para justificativa | **Baixo/Médio** |
| `AtribuicaoTemporariaView` | Submit com `useValidacaoFormulario` + preenchimento mínimo | Inline por campo + `notify`/`AppAlert` | Sim (`formularioValido`) | **Baixo** |
| `ParametrosView` | Submit com `useValidacaoFormulario` | Inline por campo + `AppAlert`/`notify` | Não bloqueia por inválido antes do submit (valida ao salvar) | **Baixo** |
| `AdministradoresView` | Submit em modal com `useValidacaoFormulario` | Inline por campo no modal + `BAlert` contextual + `notify` de sucesso | Não bloqueia por vazio; valida no clique da ação | **Baixo** |
| `NotificacoesAdminView` | Ações explícitas (reenvio) | `BAlert` + `AppAlert` + `notify` | Sim (carregamento) | **Baixo** |
| `LimpezaProcessosView` | Submit local antes da confirmação | Inline por campo + `AppAlert`/`notify` para operação sistêmica + alerta fixo informativo | Não desabilita por valor inválido; valida no clique e só então abre confirmação | **Baixo** |
| `RelatorioAndamentoView` | Submit com seleção obrigatória do processo | Inline por campo + `notify` sistêmico | Sim apenas durante carregamento | **Baixo** |
| `RelatorioMapasView` | Submit com seleção obrigatória do processo | Inline por campo + `notify` sistêmico | Sim apenas durante carregamento | **Baixo** |
| `HistoricoView` | Sem formulário de entrada | Log local em falha de carga | N/A | **Baixo** |
| `PainelView` | Sem formulário clássico | Toast/empty state/lista | N/A | **Baixo** |
| `RelatoriosView` | Navegação por cards, sem formulário | N/A | N/A | **Baixo** |
| `UnidadesView`, `UnidadeView`, `ProcessoDetalheView`, `ErroGeralView` | Sem formulário clássico | Alertas/estados de tela | N/A | **Baixo** |

### 11.1 Matriz dos componentes de formulário reutilizáveis

| Componente | Técnica de validação | Observação para refatoração |
|---|---|---|
| `CadAtividadeForm` | `useValidacaoFormulario` + inline no submit | Já alinhado ao padrão compartilhado; referência para formulários curtos sem pré-bloqueio por erro corrigível. |
| `ProcessoFormFields` | Erro por `fieldErrors` (backend/local), foco no primeiro erro | Bom núcleo para extrair padrão de foco/acessibilidade. |
| `DisponibilizarMapaModal` | `useValidacaoFormulario` + regra reativa de data + erro de backend por campo + `generic` | Virou boa referência de validação em modal com foco na origem do erro. |
| `CriarCompetenciaModal` | `useValidacaoFormulario` + inline por campo + `generic` | Já alinhado ao padrão compartilhado; manter como referência para listas obrigatórias em modal. |
| `InputData` | Componente neutro, recebe `state` externo | Facilita padronização central se houver composable comum. |

### 11.2 Inconsistências adicionais observadas nesta rodada

1. O fluxo de **cadastro de atividades** ainda combina erro por item, erro global e notificações de forma mais rica que formulários simples, mas agora sem falhas silenciosas relevantes.
2. Há variação controlada entre:
   - validar no submit com `useValidacaoFormulario`;
   - validar reativamente por `watch` para regras objetivas (especialmente datas);
   - bloquear ação por loading/estado de workflow, não por erro simples de preenchimento.
3. Telas administrativas, relatórios e cadastro de atividades já migraram para uma base mais uniforme; o trabalho restante está mais ligado a hardening, cobertura e refinamentos visuais.
4. Os modais críticos antes citados como inconsistentes já foram convergidos; o foco agora é reduzir microdiferenças residuais fora do composable compartilhado quando houver ganho claro.

---

## 12) Matriz de endpoints de validação (backend) vinculados ao frontend

Abaixo, uma visão de “entrada da API” para os fluxos de formulário do frontend.

### 12.1 Processo e painel de processo

| Endpoint | DTO/entrada | Tipo de validação no backend | Observação UX/refatoração |
|---|---|---|---|
| `POST /api/processos` | `CriarProcessoRequest` | Bean Validation (`@NotBlank`, `@NotNull`, `@Future`, `@NotEmpty`) + regras de negócio em `ProcessoService` | Base robusta; já alinhada ao frontend com `erros`. |
| `POST /api/processos/{codigo}/atualizar` | `AtualizarProcessoRequest` | Bean Validation + regras de situação (`CRIADO`) + elegibilidade de unidades | Consistente com criação. |
| `POST /api/processos/{codigo}/iniciar` | `IniciarProcessoRequest` | Bean Validation parcial + regras de negócio (situação, unidades, mapa vigente, conflitos) | Endpoint crítico para mensagens de domínio agregadas. |
| `POST /api/processos/{codigo}/acao-em-bloco` | `AcaoEmBlocoRequest` | Bean Validation + validação condicional (`dataLimite` obrigatória para disponibilizar) | Bom exemplo de validação condicional server-side. |
| `POST /api/processos/{codigo}/excluir-completo` (hom) | path variable | Regra ambiental + autorização | Fluxo administrativo especial. |

### 12.2 Cadastro de atividades (subprocesso)

| Endpoint | DTO/entrada | Tipo de validação no backend | Observação UX/refatoração |
|---|---|---|---|
| `GET /api/subprocessos/{cod}/validar-cadastro` | sem body | Validação de negócio estruturada (`ValidacaoCadastroDto` com erros por atividade e globais) | Excelente contrato para erro por item no frontend. |
| `POST /api/subprocessos/{cod}/cadastro/disponibilizar` | sem body | Regras de transição/situação no serviço | Poderia evoluir para retornar pendências estruturadas quando inválido; hoje o pré-check estruturado é o `GET validar-cadastro`. |
| `POST /api/subprocessos/{cod}/devolver-cadastro` | `JustificativaRequest` | `@NotBlank` + regras de workflow | Alinha com modal de justificativa obrigatória. |
| `POST /api/subprocessos/{cod}/aceitar-cadastro` e `/homologar-cadastro` | `TextoOpcionalRequest` | texto opcional + regras de transição | Sem erro de campo típico; foco em regra de estado. |
| `POST /api/subprocessos/{cod}/importar-atividades` | `ImportarAtividadesRequest` | `@NotNull` origem + validações de permissão/situação | Retorna aviso de duplicidade (já aproveitado no frontend). |

### 12.3 Mapa e competências

| Endpoint | DTO/entrada | Tipo de validação no backend | Observação UX/refatoração |
|---|---|---|---|
| `POST /api/subprocessos/{cod}/disponibilizar-mapa` | `DisponibilizarMapaRequest` | `@NotNull` + `@Future` para data + regras de situação | Combina bem com validação local de data no modal. |
| `POST /api/subprocessos/{cod}/mapa-completo` | `SalvarMapaRequest` | validação de campos + `@Valid` aninhado + integridade no serviço | Forte para consolidar padrão de erro por campo/lista. |
| `POST /api/subprocessos/{cod}/competencia` | `CriarCompetenciaRequest` | `@NotBlank` descrição + `@NotEmpty` em `atividadesCodigos` + regra de serviço equivalente | Criação agora tem contrato explícito para a coleção obrigatória. |
| `POST /api/subprocessos/{cod}/competencia/{codCompetencia}` | `AtualizarCompetenciaRequest` | `@NotBlank` descrição; `atividadesCodigos` opcional | Divergência criação/edição fica explícita: edição pode manter a lista vazia conforme regra atual. |
| `POST /api/subprocessos/{cod}/submeter-mapa-ajustado` | `SubmeterMapaAjustadoRequest` | `@NotBlank`, `@Size`, nested `@Valid` | Bom para fluxo de justificativa de ajuste. |
| `POST /api/subprocessos/{cod}/validar-mapa`, `.../devolver-validacao`, `.../aceitar-validacao`, `.../homologar-validacao` | `JustificativaRequest`/`TextoOpcionalRequest`/sem body | Regras de transição por situação | Erros predominantemente de negócio/estado. |

### 12.4 Atividades e conhecimentos

| Endpoint | DTO/entrada | Tipo de validação no backend | Observação UX/refatoração |
|---|---|---|---|
| `POST /api/atividades` | `CriarAtividadeRequest` | `@NotNull` mapa + `@NotBlank` descrição | Suporta inline básico. |
| `POST /api/atividades/{cod}/atualizar` | `AtualizarAtividadeRequest` | `@NotBlank` descrição | Simples e consistente. |
| `POST /api/atividades/{cod}/conhecimentos` | `CriarConhecimentoRequest` | `@NotNull` atividade + `@NotBlank` descrição | Simples e consistente. |
| `POST /api/atividades/{cod}/conhecimentos/{k}/atualizar` | `AtualizarConhecimentoRequest` | `@NotBlank` descrição | Simples e consistente. |

### 12.5 Administração, configuração e autenticação

| Endpoint | DTO/entrada | Tipo de validação no backend | Observação UX/refatoração |
|---|---|---|---|
| `POST /api/unidades/{cod}/atribuicoes-temporarias` | `CriarAtribuicaoRequest` | `@TituloEleitoral`, `@NotNull` nas datas, `@NotBlank`/`@Size` na justificativa + regra de coerência de datas no serviço | Contrato de entrada fortalecido; regra cruzada continua no serviço. |
| `POST /api/configuracoes` | `List<ParametroRequest>` | Bean Validation por item (`codigo`, `chave`, `valor`) | Bom backend para exibir erros por índice/campo no frontend. |
| `POST /api/usuarios/login` | `AutenticarRequest` | validação de título/senha + regras de autenticação | Deve seguir política de erro não enumerável. |
| `POST /api/usuarios/entrar` | `EntrarRequest` | `@NotNull` + `@Size` | Consistente. |
| `POST /api/usuarios/administradores` | `AdicionarAdministradorRequest` | `@TituloEleitoral` | Payload saiu de `Map<String,String>` manual e passou a DTO validado. |

---

## 13) O que precisa mudar no backend para viabilizar a refatoração completa

Para a consistência completa de validação/UX no frontend, os principais ajustes de backend são:

### 13.1 Unificar contrato de erro de validação por campo

1. Manter o contrato em português **`erros`/`campo`/`mensagem`**, sem camada permanente de aliases.
2. Garantir que todo endpoint de formulário relevante retorne campos estáveis quando houver erro de entrada.
3. Atualizar `RestExceptionHandler`, tipos do frontend e `useFormErrors` no mesmo corte de contrato.
4. Evitar mensagens agregadas em string única quando existirem múltiplas pendências corrigíveis.

### 13.2 Reduzir validação manual ad hoc em controllers

1. Substituir payloads `Map<String, String>` por DTOs com Bean Validation (`@NotBlank`, `@Size`, etc.).
2. Centralizar validação de formato/obrigatoriedade no nível de DTO sempre que possível.

### 13.3 Fortalecer DTOs com obrigatoriedade explícita

Em especial para entradas hoje frouxas em relação ao comportamento esperado do frontend:

- campos já fortalecidos nesta etapa: atribuição temporária, criação de competência e adição de administrador;
- campos textuais opcionais que em certos fluxos são funcionalmente obrigatórios (decidir por endpoint/ação).

### 13.4 Diferenciar melhor erro de entrada x erro de workflow

1. **Erro de entrada do formulário**: `400` com lista estruturada de erros de campo (`erros`/`campo`/`mensagem`, após a migração).
2. **Erro de situação/workflow/permissão de ação**: `400/409/403` com código semântico estável (`code`) para microcopy consistente no frontend.

### 13.5 Expor validações de “pré-check” estruturadas para ações complexas

Para ações de workflow com múltiplos pré-requisitos (disponibilizar/homologar em bloco etc.), considerar endpoints de pré-validação no mesmo estilo de `validar-cadastro`, retornando lista estruturada de pendências.

### 13.6 Padronizar sanitização + validação semântica

Manter sanitização, mas garantir que ela não substitua validação semântica. Em formulários sensíveis, preservar:

- limites de tamanho,
- obrigatoriedade,
- coerência temporal,
- coerência de relacionamento (ex.: associação atividade/competência).

---

## 14) Backlog sugerido (frontend + backend) para execução incremental

1. **Sprint 1 (contrato de erro):** concluída — contrato em português (`erros.campo`) e DTOs priorizados foram entregues.
2. **Sprint 2 (frontend base):** concluída — `useValidacaoFormulario` e foco no primeiro erro já sustentam a maior parte dos fluxos migrados.
3. **Sprint 3 (fluxos críticos):** concluída — processo, mapa e cadastro de atividades convergiram para o padrão-base.
4. **Sprint 4 (admin e auxiliares):** concluída para os fluxos já mapeados — administradores, atribuição temporária, limpeza e relatórios.
5. **Sprint 5 (hardening):** consolidar regressão de UX, revisar microdiferenças residuais e concluir a auditoria de acessibilidade.
