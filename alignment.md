# Alinhamento da Implementação com Requisitos

Este documento detalha o estado atual da implementação do Sistema de Gestão de Competências (SGC) em relação a cada Caso de Uso (CDU) especificado em `/reqs`.

## CDU-01 - Realizar login e exibir estrutura das telas

**Resumo:**
O sistema deve autenticar usuários integrando-se (simuladamente) com "Acesso TRE-PE" e "SGRH". Deve determinar perfis e unidades do usuário. Se houver múltiplos perfis, deve permitir a seleção (Contexto). Após autenticação, deve exibir a estrutura padrão (navbar, menu, rodapé).

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `UsuarioController`, `UsuarioService`, `SgrhService`, `Usuario`, `Perfil`.
    *   Frontend: `LoginView.vue`, `MainNavbar.vue`, `App.vue`, `perfil` store.

*   **Pronto:**
    *   Fluxo de login em duas etapas: Autenticação (simulada via título eleitoral) e Autorização/Seleção de Perfil.
    *   Recuperação de dados do usuário e perfis via `SgrhService` (mockado).
    *   Estrutura visual básica (Navbar, Conteúdo, Rodapé) implementada em `App.vue` e `MainNavbar.vue`.
    *   Persistência do contexto de segurança (JWT/Sessão) no frontend.

*   **Faltante:**
    *   Integração real com serviços externos (TRE-PE/SGRH). Atualmente depende de mocks e dados "seed".

*   **Incorreto:**
    *   `SgrhService.toUsuarioDto` define um cargo fixo ("Analista Judiciário") hardcoded no backend, ignorando dados reais do SGRH mockado.

## CDU-02 - Visualizar Painel

**Resumo:**
Tela inicial pós-login. Deve exibir "Processos Ativos" e "Alertas" filtrados para a unidade do usuário (ou hierarquia, dependendo da regra).

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `PainelController`, `PainelService`, `ProcessoRepo`, `AlertaRepo`.
    *   Frontend: `PainelView.vue`, `TabelaProcessos.vue`, `TabelaAlertas.vue`.

*   **Pronto:**
    *   Endpoints `/api/painel/processos` e `/api/painel/alertas` implementados.
    *   Redirecionamento dinâmico nos cards de processo (Admin -> Detalhes Processo, Unidade -> Detalhes Subprocesso).

*   **Faltante:**
    *   Destaque visual (negrito) para alertas não lidos na `TabelaAlertas.vue`.

*   **Incorreto:**
    *   **Lista de Unidades:** O `PainelService.paraProcessoResumoDto` concatena siglas de *todas* as unidades participantes, violando a regra de exibir apenas unidades de nível superior imediato abaixo da raiz que possuam todas as suas subordinadas participando.
    *   **Filtragem de Processos:** É necessário garantir que a lógica de filtragem hierárquica (processos da unidade ou subordinadas) esteja rigorosamente aplicada no repositório/serviço, o que é complexo de validar apenas com análise estática.

## CDU-03 - Manter processo

**Resumo:**
CRUD de Processos (Mapeamento/Revisão/Diagnóstico) por perfil ADMIN. Regras de criação, edição e exclusão (apenas 'Criado').

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `ProcessoController`, `ProcessoService`, `Processo`.
    *   Frontend: `CadProcesso.vue`.

*   **Pronto:**
    *   Endpoints CRUD padrão (`GET`, `POST` para criar/atualizar/excluir).
    *   Validações básicas de campos obrigatórios e datas.
    *   Restrição de alteração apenas para status 'CRIADO'.

*   **Incorreto:**
    *   **Fluxo de "Iniciar" na criação:** O frontend bloqueia o botão "Iniciar Processo" até que o processo seja salvo pela primeira vez, obrigando um fluxo não intuitivo (Salvar -> Voltar -> Iniciar), enquanto o requisito sugere um fluxo contínuo.
    *   **Snapshot Hierárquico:** O backend apenas vincula as entidades `Unidade` atuais ao `Processo`. Não é criado um "snapshot" da estrutura organizacional daquele momento. Se a hierarquia de unidades mudar no futuro, o histórico do processo será corrompido.

## CDU-04 - Iniciar processo de mapeamento

**Resumo:**
Transição de 'Criado' para 'Em andamento'. Criação de subprocessos (Unidades), Mapas vazios e envio de notificações/alertas.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `ProcessoService`, `SubprocessoService`, `SubprocessoRepo`.

*   **Pronto:**
    *   Mudança de status do processo.
    *   Geração de Subprocessos para unidades participantes.
    *   Criação de Mapas vazios associados.

*   **Faltante:**
    *   **Snapshot Hierárquico:** Mesmo problema do CDU-03. A hierarquia vigente no momento do início não é preservada.
    *   **E-mails Específicos:** A lógica de envio de e-mail (via `NotificacaoEmailService` ou Listener) precisa distinguir corretamente unidades "Intermediárias" para enviar a lista consolidada de unidades subordinadas. A implementação atual tende a ser genérica.

## CDU-05 - Iniciar processo de revisão

**Resumo:**
Similar ao CDU-04, mas copia o mapa *vigente* da unidade para o novo subprocesso.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `ProcessoService`, `CopiaMapaService`.

*   **Pronto:**
    *   Lógica de cópia profunda (`CopiaMapaService`) implementada para duplicar Mapa, Competências, Atividades e Conhecimentos.
    *   Validação de existência de mapa vigente antes de iniciar.

*   **Incorreto:**
    *   Mesmas questões de Snapshot e Notificações do CDU-04.

## CDU-06 - Detalhar processo

**Resumo:**
Visualização dos dados do processo e lista de unidades participantes com seus status (subprocessos).

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `ProcessoController` (`obterProcesso`), `ProcessoService`.
    *   Frontend: `ProcessoView.vue`, `ProcessoDetalhes.vue`.

*   **Pronto:**
    *   Exibição dos dados do processo.
    *   Listagem de unidades com progresso/situação.
    *   Botões de ação (Finalizar, Editar - dependendo do estado).

## CDU-07 - Detalhar subprocesso

**Resumo:**
Visão detalhada do trabalho de uma unidade específica dentro do processo. Acesso aos cards "Atividades", "Mapa", "Equipe".

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoCrudController` (`obterDetalhes`), `SubprocessoDtoService`.
    *   Frontend: `SubprocessoView.vue`.

*   **Pronto:**
    *   Recuperação de dados do subprocesso.
    *   Cards de navegação para as etapas do workflow.
    *   Exibição de histórico de movimentações (`TabelaMovimentacoes.vue`).

## CDU-08 - Manter cadastro de atividades e conhecimentos

**Resumo:**
Unidade (Chefe) cadastra suas Atividades e Conhecimentos. Funcionalidade de importar de outros processos.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `AtividadeController`, `AtividadeService`, `ConhecimentoRepo`.
    *   Frontend: `CadAtividades.vue`.

*   **Pronto:**
    *   CRUD de Atividades e Conhecimentos.
    *   Endpoint e modal de importação de atividades (`ImportarAtividadesModal.vue`).

*   **Faltante:**
    *   Validações de negócio mais complexas no frontend para evitar "Atividade sem conhecimento" antes mesmo de tentar disponibilizar.

## CDU-09 - Disponibilizar cadastro de atividades e conhecimentos

**Resumo:**
Finalização da etapa de cadastro pela Unidade. Valida se há atividades sem conhecimento. Envia para análise.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoCadastroController` (`disponibilizarCadastro`).

*   **Pronto:**
    *   Transição de estado.
    *   Registro de movimentação.

*   **Incorreto:**
    *   **Validação de Lista Vazia:** O método `disponibilizarCadastro` verifica se há "atividades sem conhecimento", mas não verifica se há **zero atividades** cadastradas. O requisito exige "Pelo menos uma atividade cadastrada".

## CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos

**Resumo:**
Idêntico ao CDU-09, mas aplicado ao fluxo de "Revisão" (quando o processo é de revisão ou houve devolução).

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoCadastroController` (`disponibilizarRevisao`).

*   **Pronto:**
    *   Lógica análoga ao CDU-09 implementada.
    *   Mesmo erro de validação (lista vazia permitida).

## CDU-11 - Visualizar cadastro de atividades e conhecimentos

**Resumo:**
Visualização apenas leitura do cadastro (para unidades superiores ou consulta).

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoCadastroController` (`obterCadastro`).
    *   Frontend: `VisAtividades.vue`.

*   **Pronto:**
    *   Exibição hierárquica de atividades e conhecimentos.
    *   Botões de ação (Aceitar/Devolver) visíveis apenas para perfis autorizados (Gestor/Admin) nessa tela ou tela derivada.

## CDU-12 - Verificar impactos no mapa de competências

**Resumo:**
Verificar como as mudanças nas atividades afetam o mapa de competências vigente.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoMapaController` (`verificarImpactos`), `ImpactoMapaService`.
    *   Frontend: `ImpactoMapaModal.vue`.

*   **Pronto:**
    *   Algoritmo de comparação (Atividades removidas/adicionadas -> Competências impactadas).
    *   Retorno de DTO com listas de impactos.

## CDU-13 - Analisar cadastro de atividades e conhecimentos

**Resumo:**
Unidade Superior (Gestor) ou SEDOC (Admin) analisa o cadastro disponibilizado. Pode Aceitar, Devolver ou Homologar.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoCadastroController`, `SubprocessoWorkflowService`.
    *   Frontend: `CadAtividades.vue` (modo análise) ou `VisAtividades.vue`.

*   **Pronto:**
    *   Endpoints para `devolverCadastro`, `aceitarCadastro`, `homologarCadastro`.
    *   Registro de `Analise` (Entidade) com histórico.

## CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos

**Resumo:**
Análise focada no fluxo de revisão.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoCadastroController` (endpoints `...-revisao-cadastro`).

*   **Pronto:**
    *   Endpoints específicos implementados (`aceitarRevisaoCadastro`, etc.).
    *   Fluxo de trabalho coerente com o CDU-13.

## CDU-15 - Manter mapa de competências

**Resumo:**
SEDOC (Admin) cria/edita o mapa de competências, criando competências e associando-as às atividades cadastradas pela unidade.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoMapaController`, `CompetenciaService`.
    *   Frontend: `CadMapa.vue`, `CriarCompetenciaModal.vue`.

*   **Pronto:**
    *   Criação de competências.
    *   Associação N:N (lógica) entre Competência e Atividade.

*   **Incorreto:**
    *   **Verbos HTTP:** O controlador utiliza `PUT` e `DELETE` (`@PutMapping`, `@DeleteMapping`) para competências. O padrão do projeto exige uso exclusivo de `POST` (ex: `POST .../excluir`).

## CDU-16 - Ajustar mapa de competências

**Resumo:**
SEDOC realiza ajustes no mapa após receber sugestões ou devoluções da unidade.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoMapaController` (`salvarAjustesMapa`).

*   **Pronto:**
    *   Endpoint para salvar ajustes.
    *   Reuso da lógica de manutenção de mapa.

## CDU-17 - Disponibilizar mapa de competências

**Resumo:**
SEDOC finaliza a elaboração do mapa e o envia para validação da Unidade.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoValidacaoController` (`disponibilizarMapa`).

*   **Pronto:**
    *   Transição de estado para 'Mapa disponibilizado'.
    *   Notificações.

*   **Incorreto:**
    *   Existe um endpoint redundante em `SubprocessoMapaController` (`disponibilizar`), criando ambiguidade sobre qual deve ser chamado pelo frontend.

## CDU-18 - Visualizar mapa de competências

**Resumo:**
Unidade visualiza o mapa proposto pela SEDOC.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoMapaController` (`obterMapaVisualizacao`).
    *   Frontend: `VisMapa.vue`.

*   **Pronto:**
    *   DTO otimizado para visualização hierárquica (Mapa -> Competência -> Atividade -> Conhecimento).

## CDU-19 - Validar mapa de competências

**Resumo:**
Unidade avalia o mapa. Pode Validar (Aceitar) ou Apresentar Sugestões.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoValidacaoController`.
    *   Frontend: `VisMapa.vue` (ações).

*   **Pronto:**
    *   Endpoints `validarMapa` e `apresentarSugestoes`.
    *   Registro de sugestões em campo texto formatado no Mapa.

## CDU-20 - Analisar validação de mapa de competências

**Resumo:**
SEDOC/Gestor analisa a resposta da unidade (Validação ou Sugestões). Pode Aceitar, Devolver ou Homologar.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoValidacaoController`.

*   **Pronto:**
    *   Endpoints de workflow (`aceitarValidacao`, `devolverValidacao`, `homologarValidacao`, `submeterMapaAjustado`).

*   **Faltante:**
    *   Controle explícito de que sugestões foram "lidas" ou "tratadas". O sistema confia no julgamento humano do operador da SEDOC.

## CDU-21 - Finalizar processo de mapeamento ou de revisão

**Resumo:**
Encerramento global do processo. Tornar mapas vigentes.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `ProcessoService` (`finalizar`).

*   **Pronto:**
    *   Verificação de que todos os subprocessos estão homologados (`validarTodosSubprocessosHomologados`).
    *   Atualização da vigência (`tornarMapasVigentes` -> `unidade.setMapaVigente`).
    *   Envio de notificações finais.
    *   Bloqueio de finalização se houver pendências.

---
**Observação Geral:** A implementação backend está robusta em termos de cobertura de fluxos ("Caminho Feliz"), mas apresenta desvios arquiteturais (Snapshot de Unidades, Verbos HTTP) e validações de borda (Listas vazias) que precisam ser corrigidos para conformidade total com os requisitos.
