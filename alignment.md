# Alinhamento da Implementação com Requisitos

Este documento detalha o estado atual da implementação do Sistema de Gestão de Competências (SGC) em relação a cada Caso
de Uso (CDU) especificado em `/reqs`.

## CDU-01 - Realizar login e exibir estrutura das telas

**Resumo:**
O sistema deve autenticar usuários integrando-se (simuladamente) com "Acesso TRE-PE" e "SGRH". Deve determinar perfis e
unidades do usuário. Se houver múltiplos perfis, deve permitir a seleção (Contexto). Após autenticação, deve exibir a
estrutura padrão (navbar, menu, rodapé).

**Implementação:**

* **Código Utilizado:**
    * Backend: `UsuarioController`, `UsuarioService`, `SgrhService`, `Usuario`, `Perfil`.
    * Frontend: `LoginView.vue`, `MainNavbar.vue`, `App.vue`, `perfil` store.

* **Pronto:**
    * Fluxo de login em duas etapas: Autenticação (simulada via título eleitoral) e Autorização/Seleção de Perfil.
    * Recuperação de dados do usuário e perfis via `SgrhService` (mockado).
    * Estrutura visual básica (Navbar, Conteúdo, Rodapé) implementada em `App.vue` e `MainNavbar.vue`.
    * Persistência do contexto de segurança (JWT/Sessão) no frontend.

* **Faltante:**
    * Integração real com serviços externos (TRE-PE/SGRH). Atualmente depende de mocks e dados "seed".

* **Incorreto:**
    * `SgrhService.toUsuarioDto` define um cargo fixo ("Analista Judiciário") hardcoded no backend, ignorando dados
      reais do SGRH mockado.

## CDU-02 - Visualizar Painel

**Referência**: [reqs/CDU-02.md]()

**Resumo:**
Tela inicial pós-login. Deve exibir "Processos Ativos" e "Alertas" filtrados para a unidade do usuário (ou hierarquia,
dependendo da regra).

**Implementação:**

* **Código Utilizado:**
    * Backend: `PainelController`, `PainelService`, `ProcessoRepo`, `AlertaRepo`.
    * Frontend: `PainelView.vue`, `TabelaProcessos.vue`, `TabelaAlertas.vue`.

* **Pronto:**
    * Endpoints `/api/painel/processos` e `/api/painel/alertas` implementados.
    * Redirecionamento dinâmico nos cards de processo (Admin -> Detalhes Processo, Unidade -> Detalhes Subprocesso).

* **Faltante:**
    * Destaque visual (negrito) para alertas não lidos na `TabelaAlertas.vue`.

* **Incorreto / Observação:** O `PainelService.paraProcessoResumoDto` não simplesmente concatena todas as siglas; implementa as funções `formatarUnidadesParticipantes` e `selecionarIdsVisiveis` que tentam escolher as siglas de unidades de nível superior visíveis (determinando candidatos com `todasSubordinadasParticipam`). Essa lógica parece atender ao requisito, mas precisa de cobertura de testes e revisão de casos de borda para garantir conformidade com a regra de exibir apenas unidades de nível superior imediato abaixo da raiz quando apropriado.
    * **Filtragem de Processos:** A filtragem hierárquica está implementada (obterIdsUnidadesSubordinadas) — porém é recomendável adicionar testes de integração para validar regras de escopo (unidade + subordinadas) em conjuntos de dados complexos.

## CDU-03 - Manter processo

**Resumo:**
CRUD de Processos (Mapeamento/Revisão/Diagnóstico) por perfil ADMIN. Regras de criação, edição e exclusão (apenas '
Criado').

**Implementação:**

* **Código Utilizado:**
    * Backend: `ProcessoController`, `ProcessoService`, `Processo`.
    * Frontend: `CadProcesso.vue`.

* **Pronto:**
    * Endpoints CRUD padrão (`GET`, `POST` para criar/atualizar/excluir).
    * Validações básicas de campos obrigatórios e datas.
    * Restrição de alteração apenas para status 'CRIADO'.

* **Incorreto:**
    * **Fluxo de "Iniciar" na criação:** O frontend bloqueia o botão "Iniciar Processo" até que o processo seja salvo
      pela primeira vez, obrigando um fluxo não intuitivo (Salvar -> Voltar -> Iniciar), enquanto o requisito sugere um
      fluxo contínuo.
    * **Snapshot Hierárquico:** O backend apenas vincula as entidades `Unidade` atuais ao `Processo`. Não é criado um "
      snapshot" da estrutura organizacional daquele momento. Se a hierarquia de unidades mudar no futuro, o histórico do
      processo será corrompido.

## CDU-04 - Iniciar processo de mapeamento

**Resumo:**
Transição de 'Criado' para 'Em andamento'. Criação de subprocessos (Unidades), Mapas vazios e envio de
notificações/alertas.

**Implementação:**

* **Código Utilizado:**
    * Backend: `ProcessoService`, `SubprocessoService`, `SubprocessoRepo`.

* **Pronto:**
    * Mudança de status do processo.
    * Geração de Subprocessos para unidades participantes.
    * Criação de Mapas vazios associados.

* **Faltante:**
    * **Snapshot Hierárquico:** Mesmo problema do CDU-03. A hierarquia vigente no momento do início não é preservada.
    * **E-mails Específicos:** A lógica de envio de e-mail (via `NotificacaoEmailService` ou Listener) precisa
      distinguir corretamente unidades "Intermediárias" para enviar a lista consolidada de unidades subordinadas. A
      implementação atual tende a ser genérica.

## CDU-05 - Iniciar processo de revisão

**Resumo:**
Similar ao CDU-04, mas copia o mapa *vigente* da unidade para o novo subprocesso.

**Implementação:**

* **Código Utilizado:**
    * Backend: `ProcessoService`, `CopiaMapaService`.

* **Pronto:**
    * Lógica de cópia profunda (`CopiaMapaService`) implementada para duplicar Mapa, Competências, Atividades e
      Conhecimentos.
    * Validação de existência de mapa vigente antes de iniciar.

* **Incorreto:**
    * Mesmas questões de Snapshot e Notificações do CDU-04.

## CDU-06 - Detalhar processo

**Resumo:**
Visualização dos dados do processo e lista de unidades participantes com seus status (subprocessos).

**Implementação:**

* **Código Utilizado:**
    * Backend: `ProcessoController` (`obterProcesso`), `ProcessoService`.
    * Frontend: `ProcessoView.vue`, `ProcessoDetalhes.vue`.

* **Pronto:**
    * Exibição dos dados do processo.
    * Listagem de unidades com progresso/situação.
    * Botões de ação (Finalizar, Editar - dependendo do estado).

## CDU-07 - Detalhar subprocesso

**Resumo:**
Visão detalhada do trabalho de uma unidade específica dentro do processo. Acesso aos cards "Atividades", "Mapa", "
Equipe".

**Implementação:**

* **Código Utilizado:**
    * Backend: `SubprocessoCrudController` (`obterDetalhes`), `SubprocessoDtoService`.
    * Frontend: `SubprocessoView.vue`.

* **Pronto:**
    * Recuperação de dados do subprocesso.
    * Cards de navegação para as etapas do workflow.
    * Exibição de histórico de movimentações (`TabelaMovimentacoes.vue`).

## CDU-08 - Manter cadastro de atividades e conhecimentos

**Resumo:**
Unidade (Chefe) cadastra suas Atividades e Conhecimentos. Funcionalidade de importar de outros processos.

**Implementação:**

* **Código Utilizado:**
    * Backend: `AtividadeController`, `AtividadeService`, `ConhecimentoRepo`.
    * Frontend: `CadAtividades.vue`.

* **Pronto:**
    * CRUD de Atividades e Conhecimentos.
    * Endpoint e modal de importação de atividades (`ImportarAtividadesModal.vue`).

* **Faltante:**
    * Validações de negócio mais complexas no frontend para evitar "Atividade sem conhecimento" antes mesmo de tentar
      disponibilizar.

## CDU-09 - Disponibilizar cadastro de atividades e conhecimentos

**Resumo:**
Finalização da etapa de cadastro pela Unidade. Valida se há atividades sem conhecimento. Envia para análise.

**Implementação:**

* **Código Utilizado:**
    * Backend: `SubprocessoCadastroController` (`disponibilizarCadastro`).

* **Pronto:**
    * Transição de estado.
    * Registro de movimentação.

* **Incorreto:**
    * **Validação de Lista Vazia:** O método `disponibilizarCadastro` verifica se há "atividades sem conhecimento", mas
      não verifica se há **zero atividades** cadastradas. O requisito exige "Pelo menos uma atividade cadastrada".

## CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos

**Resumo:**
Idêntico ao CDU-09, mas aplicado ao fluxo de "Revisão" (quando o processo é de revisão ou houve devolução).

**Implementação:**

* **Código Utilizado:**
    * Backend: `SubprocessoCadastroController` (`disponibilizarRevisao`).

* **Pronto:**
    * Lógica análoga ao CDU-09 implementada.
    * Mesmo erro de validação (lista vazia permitida).

## CDU-11 - Visualizar cadastro de atividades e conhecimentos

**Resumo:**
Visualização apenas leitura do cadastro (para unidades superiores ou consulta).

**Implementação:**

* **Código Utilizado:**
    * Backend: `SubprocessoCadastroController` (`obterCadastro`).
    * Frontend: `VisAtividades.vue`.

* **Pronto:**
    * Exibição hierárquica de atividades e conhecimentos.
    * Botões de ação (Aceitar/Devolver) visíveis apenas para perfis autorizados (Gestor/Admin) nessa tela ou tela
      derivada.

## CDU-12 - Verificar impactos no mapa de competências

**Resumo:**
Verificar como as mudanças nas atividades afetam o mapa de competências vigente.

**Implementação:**

* **Código Utilizado:**
    * Backend: `SubprocessoMapaController` (`verificarImpactos`), `ImpactoMapaService`.
    * Frontend: `ImpactoMapaModal.vue`.

* **Pronto:**
    * Algoritmo de comparação (Atividades removidas/adicionadas -> Competências impactadas).
    * Retorno de DTO com listas de impactos.

## CDU-13 - Analisar cadastro de atividades e conhecimentos

**Resumo:**
Unidade Superior (Gestor) ou SEDOC (Admin) analisa o cadastro disponibilizado. Pode Aceitar, Devolver ou Homologar.

**Implementação:**

* **Código Utilizado:**
    * Backend: `SubprocessoCadastroController`, `SubprocessoWorkflowService`.
    * Frontend: `CadAtividades.vue` (modo análise) ou `VisAtividades.vue`.

* **Pronto:**
    * Endpoints para `devolverCadastro`, `aceitarCadastro`, `homologarCadastro`.
    * Registro de `Analise` (Entidade) com histórico.

## CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos

**Resumo:**
Análise focada no fluxo de revisão.

**Implementação:**

* **Código Utilizado:**
    * Backend: `SubprocessoCadastroController` (endpoints `...-revisao-cadastro`).

* **Pronto:**
    * Endpoints específicos implementados (`aceitarRevisaoCadastro`, etc.).
    * Fluxo de trabalho coerente com o CDU-13.

## CDU-15 - Manter mapa de competências

**Resumo:**
SEDOC (Admin) cria/edita o mapa de competências, criando competências e associando-as às atividades cadastradas pela
unidade.

**Implementação:**

* **Código Utilizado:**
    * Backend: `SubprocessoMapaController`, `CompetenciaService`.
    * Frontend: `CadMapa.vue`, `CriarCompetenciaModal.vue`.

* **Pronto:**
    * Criação de competências.
    * Associação N:N (lógica) entre Competência e Atividade.

* **Incorreto:**
    * **Verbos HTTP:** O controlador utiliza `PUT` e `DELETE` (`@PutMapping`, `@DeleteMapping`) para competências. O
      padrão do projeto exige uso exclusivo de `POST` (ex: `POST .../excluir`).

## CDU-16 - Ajustar mapa de competências

**Resumo:**
SEDOC realiza ajustes no mapa após receber sugestões ou devoluções da unidade.

**Implementação:**

* **Código Utilizado:**
    * Backend: `SubprocessoMapaController` (`salvarAjustesMapa`).

* **Pronto:**
    * Endpoint para salvar ajustes.
    * Reuso da lógica de manutenção de mapa.

## CDU-17 - Disponibilizar mapa de competências

**Resumo:**
SEDOC finaliza a elaboração do mapa e o envia para validação da Unidade.

**Implementação:**

* **Código Utilizado:**
    * Backend: `SubprocessoValidacaoController` (`disponibilizarMapa`).

* **Pronto:**
    * Transição de estado para 'Mapa disponibilizado'.
    * Notificações.

* **Incorreto:**
    * Existe um endpoint redundante em `SubprocessoMapaController` (`disponibilizar`), criando ambiguidade sobre qual
      deve ser chamado pelo frontend.

## CDU-18 - Visualizar mapa de competências

**Resumo:**
Unidade visualiza o mapa proposto pela SEDOC.

**Implementação:**

* **Código Utilizado:**
    * Backend: `SubprocessoMapaController` (`obterMapaVisualizacao`).
    * Frontend: `VisMapa.vue`.

* **Pronto:**
    * DTO otimizado para visualização hierárquica (Mapa -> Competência -> Atividade -> Conhecimento).

## CDU-19 - Validar mapa de competências

**Resumo:**
Unidade avalia o mapa. Pode Validar (Aceitar) ou Apresentar Sugestões.

**Implementação:**

* **Código Utilizado:**
    * Backend: `SubprocessoValidacaoController`.
    * Frontend: `VisMapa.vue` (ações).

* **Pronto:**
    * Endpoints `validarMapa` e `apresentarSugestoes`.
    * Registro de sugestões em campo texto formatado no Mapa.

## CDU-20 - Analisar validação de mapa de competências

**Resumo:**
SEDOC/Gestor analisa a resposta da unidade (Validação ou Sugestões). Pode Aceitar, Devolver ou Homologar.

**Implementação:**

* **Código Utilizado:**
    * Backend: `SubprocessoValidacaoController`.

* **Pronto:**
    * Endpoints de workflow (`aceitarValidacao`, `devolverValidacao`, `homologarValidacao`, `submeterMapaAjustado`).

* **Faltante:**
    * Controle explícito de que sugestões foram "lidas" ou "tratadas". O sistema confia no julgamento humano do operador
      da SEDOC.

## CDU-21 - Finalizar processo de mapeamento ou de revisão

**Resumo:**
Encerramento global do processo. Tornar mapas vigentes.

**Implementação:**

* **Código Utilizado:**
    * Backend: `ProcessoService` (`finalizar`).

* **Pronto:**
    * Verificação de que todos os subprocessos estão homologados (`validarTodosSubprocessosHomologados`).
    * Atualização da vigência (`tornarMapasVigentes` -> `unidade.setMapaVigente`).
    * Envio de notificações finais.
    * Bloqueio de finalização se houver pendências.

---
**Análise detalhada (atualizado 2025-12-08T01:18:53Z):**

Abaixo está uma análise CDU-a-CDU com referências ao código (arquivo:linha quando pertinente) e recomendações prioritárias.

CDU-01 (Login / estrutura de telas)
- Implementação: backend `sgc/sgrh/UsuarioController.java`, `sgc/sgrh/service/SgrhService.java`; frontend `frontend/src/views/LoginView.vue`, `perfil` store.
- Pronto: autenticação simulada e seleção de perfil suportadas.
- Faltante/Incorreto: `SgrhService.toUsuarioDto` define cargo hardcoded (backend/src/main/java/sgc/sgrh/service/SgrhService.java:~220). Substituir por mapeamento real quando integrar com o SGRH.

CDU-02 (Painel)
- Implementação: `backend/src/main/java/sgc/painel/PainelService.java` e `PainelController` + frontend `PainelView.vue`.
- Pronto: endpoints `/api/painel/processos` e `/api/painel/alertas` existem.
- Observação: lógica de apresentação de unidades usa `formatarUnidadesParticipantes` / `selecionarIdsVisiveis` (PainelService.java) que tenta cumprir a regra de mostrar unidades 'visíveis'; porém faltam testes de integração para validar casos hierárquicos complexos e exibir destaque visual para alertas não lidos no frontend.

CDU-03 (Manter Processo)
- Implementação: `ProcessoController` / `ProcessoService` (backend/src/.../ProcessoService.java).
- Pronto: CRUD e restrição de edição apenas em situação CRIADO.
- Inconsistência UX: Frontend exige salvar antes de 'Iniciar' processo; considerar ajuste para permitir fluxo contínuo.
- Risco: participantes vinculados diretamente — não há snapshot da hierarquia ao criar o processo (ver CDU-04/05).

CDU-04 (Iniciar mapeamento)
- Implementação: `ProcessoService.iniciarProcessoMapeamento` cria subprocessos e mapas vazios (ProcessoService.java).
- Faltante: não há snapshot da hierarquia/unidades no momento do início; recomenda-se gravar referência imutável (snapshot) para histórico.
- Notificações: uso de `NotificacaoEmailService` é suportado com mock (`NotificacaoEmailServiceMock`) — lógica de direcionamento de e-mails para unidades intermediárias precisa ser revisada (`sgc/subprocesso/service/SubprocessoNotificacaoService.java`).

CDU-05 (Iniciar revisão)
- Implementação: `iniciarProcessoRevisao` copia mapa vigente via `CopiaMapaService` (ProcessoService.java).
- Pronto: cópia profunda implementada; valida existência de mapa vigente.
- Faltante: snapshot de unidades e tratamento fino de notificações.

CDU-06 (Detalhar processo)
- Implementação: `ProcessoController.obterDetalhes` + frontend `ProcessoView.vue`.
- Pronto: dados e lista de unidades com situação exibidos.

CDU-07 (Detalhar subprocesso)
- Implementação: `SubprocessoCrudController` / `SubprocessoDtoService` / `SubprocessoView.vue`.
- Pronto: cards de Atividades/Mapa/Equipe e histórico de movimentações presentes.

CDU-08 (Manter atividades e conhecimentos)
- Implementação: `AtividadeController`, `AtividadeService`, `ConhecimentoRepo` e frontend `CadAtividades.vue`.
- Pronto: CRUD e modal de importação.
- Faltante: validações preventivas no frontend para evitar atividades sem conhecimento.

CDU-09 / CDU-10 (Disponibilizar cadastro / revisão)
- Implementação: `SubprocessoCadastroController.disponibilizarCadastro` e `disponibilizarRevisao` (backend/src/.../SubprocessoCadastroController.java).
- Pronto: transições e registro de movimentação.
- Incorreto: valida apenas "atividades sem conhecimento" mas não valida se existem zero atividades; acrescentar verificação "pelo menos uma atividade cadastrada".

CDU-11 (Visualizar cadastro)
- Implementação: `SubprocessoCadastroController.obterCadastro` e `VisAtividades.vue`.
- Pronto: exibição hierárquica e controles de ação condicionais.

CDU-12 (Verificar impactos no mapa)
- Implementação: `SubprocessoMapaController.verificarImpactos` e `ImpactoMapaService`.
- Pronto: algoritmo de comparação implementado.

CDU-13 / CDU-14 (Analisar cadastro / revisão)
- Implementação: endpoints de workflow em `SubprocessoCadastroController` e `SubprocessoWorkflowService`.
- Pronto: aceitar, devolver, homologar e registro de análise.

CDU-15 (Manter mapa)
- Implementação: `SubprocessoMapaController`, `MapaService`, `CompetenciaService`, frontend `CadMapa.vue`.
- Pronto: criação e associação N:N entre Competência e Atividade.
- Incorreto: coexistem endpoints PUT/DELETE e equivalentes POST de compatibilidade (`SubprocessoMapaController.java`) — sugerir padronizar conforme convenção do projeto (usar POST para atualizar/remover ou migrar frontend para RESTful verbs e documentar).

CDU-16 (Ajustar mapa)
- Implementação: `salvarAjustesMapa` em `SubprocessoMapaController` e `SubprocessoMapaService`.
- Pronto: endpoint para salvar ajustes presente.

CDU-17 (Disponibilizar mapa)
- Implementação: `SubprocessoMapaController.disponibilizarMapa` e `SubprocessoValidacaoController.disponibilizarMapa` (atenção: endpoints redundantes).
- Incorreto: duplicidade de endpoints cria ambiguidade; consolidar e atualizar frontend.

CDU-18 (Visualizar mapa)
- Implementação: `obterMapaVisualizacao` (SubprocessoMapaController) e frontend `VisMapa.vue`.
- Pronto: DTO otimizado para visualização implementado.

CDU-19 (Validar mapa)
- Implementação: `SubprocessoValidacaoController` e frontend `VisMapa.vue` ações.
- Pronto: endpoints `validarMapa` e `apresentarSugestoes` implementados; sugestões armazenadas em campo texto.

CDU-20 (Analisar validação)
- Implementação: workflows em `SubprocessoValidacaoController`.
- Faltante: controle explícito de marcação/consumo de sugestões lidas por SEDOC; sugere campo/flag para rastrear tratamento.

CDU-21 (Finalizar processo)
- Implementação: `ProcessoService.finalizar` (ProcessoService.java) — valida homologação de subprocessos e chama `tornarMapasVigentes`.
- Pronto: vigência atualizada (`unidade.setMapaVigente`), datas e notificações finais enviadas.
- Observação: tornarMapasVigentes grava `unidade.setMapaVigente(mapa)` — como não há snapshot separado, o histórico pode referenciar objetos que mudam com o tempo; recomenda-se persistir um snapshot/cópia histórica do relacionamento (ex: MapaVigenteHistorico).

Recomendações e prioridades
1) Snapshot da hierarquia e atribuições ao iniciar processos (CDU-03/CDU-04/CDU-05): implementar entidades de snapshot ou copiar informações essenciais ao Subprocesso.
2) Validações de borda (CDU-09/10): bloquear disponibilização se não houver ao menos uma atividade cadastrada.
3) Padronização de APIs (CDU-15/CDU-17): escolher entre endpoints RESTful (PUT/DELETE) ou convenção do projeto (POST .../remover) e refletir no frontend; remover endpoints redundantes.
4) Testes de integração: criar cenários para PainelService (seleção de siglas) e fluxos de notificação para validar regras hierárquicas e envios de e-mail reais vs mocks.
5) Registro/consumo de sugestões (CDU-20): adicionar flag 'sugestaoTratada' ou similar para rastrear tratamento.

Se desejar, prossigo com: (A) gerar um diff detalhado por arquivo com trechos relevantes; (B) abrir PR com ajustes mínimos (ex: validação de "pelo menos uma atividade"); ou (C) implementar snapshot mínimo no backend para processos de mapeamento/revisão. Indique a opção.
