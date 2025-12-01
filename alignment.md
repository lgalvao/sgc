# Alinhamento da Implementação com Requisitos

Este documento apresenta uma análise detalhada da implementação atual do Sistema de Gestão de Competências (SGC) em comparação com as especificações de Caso de Uso (CDU).

## CDU-01 - Realizar login e exibir estrutura das telas

**Resumo:** O sistema deve autenticar usuários via API externa (Acesso TRE-PE), determinar perfis com base no SGRH, permitir seleção de perfil/unidade se houver múltiplos, e exibir a estrutura padrão (navbar/footer).

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `UsuarioController`, `UsuarioService`, `SgrhService`, `Usuario`, `Perfil`.
    *   Frontend: `LoginView.vue`, `MainNavbar.vue`, `App.vue`, `perfil` store.

*   **Pronto:**
    *   Fluxo de login em duas etapas (credenciais -> seleção de perfil).
    *   Exibição da estrutura de telas (Navbar, Conteúdo, Rodapé).
    *   Determinação de perfis/unidades (mockada, mas funcional logicamente).
    *   Redirecionamento baseado na seleção.

*   **Faltante:**
    *   Integração real com API "Acesso TRE-PE" (atualmente simulada/mockada).
    *   Integração real com sistema SGRH (atualmente consulta tabelas locais populadas via seed).

*   **Incorreto:**
    *   `SgrhService.toUsuarioDto` define um cargo fixo ("Analista Judiciário") hardcoded.
    *   A lógica de rodapé está em `App.vue` e não encapsulada em componente de layout específico, embora funcional.

## CDU-02 - Visualizar Painel

**Resumo:** Exibir painel com "Processos Ativos" e "Alertas". Filtragem por unidade do usuário. Ordenação e destaques visuais (negrito para não lidos).

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `PainelController`, `PainelService`, `ProcessoRepo`, `AlertaRepo`.
    *   Frontend: `PainelView.vue`, `TabelaProcessos.vue`, `TabelaAlertas.vue`.

*   **Pronto:**
    *   Endpoints para listar processos e alertas.
    *   Lógica de links de destino dinâmicos baseados no perfil (Admin -> Cadastro, Gestor -> Detalhes, etc.).
    *   Frontend consome e exibe as tabelas.

*   **Faltante:**
    *   Destaque visual (negrito) para alertas não lidos na `TabelaAlertas.vue` (necessita verificação visual, mas a propriedade `dataHoraLeitura` é passada).

*   **Incorreto:**
    *   **Filtragem de Processos:** O requisito exige "apenas os processos que incluam entre as unidades participantes a unidade do usuário e/ou suas unidades subordinadas". O `PainelService` implementa isso, mas `ProcessoController.listarAtivos` (usado em outros lugares) não. É crítico garantir que o Painel use o endpoint correto (`/api/painel/processos`). O frontend usa `painelService`, então está correto, mas a API genérica `/api/processos/ativos` expõe dados excessivos.
    *   **Lista Textual de Unidades:** O requisito pede "apenas as unidades de nível mais alto abaixo da unidade raiz que possuam todas as suas unidades subordinadas participando". A implementação atual (`PainelService.paraProcessoResumoDto`) apenas concatena as siglas de *todas* as unidades participantes, gerando uma lista longa e poluída, violando a regra de negócio de apresentação.

## CDU-03 - Manter processo

**Resumo:** CRUD de processos (Mapeamento, Revisão, Diagnóstico). Validações de criação e edição. Exclusão lógica/física de processos 'Criado'. Início direto de processo.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `ProcessoController`, `ProcessoService`, `Processo`.
    *   Frontend: `CadProcesso.vue`.

*   **Pronto:**
    *   Operações CRUD básicas (Criar, Ler, Atualizar, Apagar).
    *   Validação de campos obrigatórios.
    *   Restrição de edição/remoção apenas para estado 'Criado'.

*   **Faltante:**
    *   **Snapshot da Árvore de Unidades:** O sistema não armazena uma cópia da hierarquia das unidades. Ele vincula as entidades `Unidade` atuais. Se a hierarquia mudar no futuro (SGRH), o histórico do processo será alterado, violando o requisito de preservação da representação hierárquica do momento da criação.

*   **Incorreto:**
    *   **Fluxo Alternativo (Iniciar):** O requisito permite clicar em "Iniciar Processo" durante a criação (antes de salvar). A implementação em `CadProcesso.vue` bloqueia essa ação com um alerta "Você precisa salvar o processo antes de poder iniciá-lo", obrigando um fluxo de dois passos (Salvar -> Redireciona Painel -> Entrar de novo -> Iniciar) que frustra a experiência e contradiz o requisito.

## CDU-04 - Iniciar processo de mapeamento

**Resumo:** Transição de 'Criado' para 'Em andamento'. Criação de subprocessos, mapas vazios e alertas/emails.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `ProcessoService`, `EventoProcessoListener`, `SubprocessoRepo`, `AlertaService`, `NotificacaoEmailService`.

*   **Pronto:**
    *   Mudança de estado.
    *   Criação de subprocessos e mapas.
    *   Geração de eventos de domínio.

*   **Incorreto:**
    *   **Emails para Unidades Intermediárias:** O requisito exige um e-mail consolidado ("Início de processo... nas unidades [SIGLAS_UNIDADES_SUBORDINADAS]"). A implementação atual (`EventoProcessoListener`) envia um e-mail genérico, sem listar as unidades subordinadas, utilizando o mesmo template para todos os tipos de unidade.
    *   **Snapshot da Hierarquia:** Como mencionado no CDU-03, a falta de persistência da estrutura hierárquica (snapshot) afeta a integridade histórica deste caso de uso.

## CDU-05 - Iniciar processo de revisão

**Resumo:** Similar ao CDU-04, mas copia o mapa vigente da unidade em vez de criar um vazio. Valida existência de mapa vigente.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `ProcessoService`, `CopiaMapaService`, `EventoProcessoListener`.

*   **Pronto:**
    *   Cópia profunda de mapa (Atividades, Conhecimentos, Competências).
    *   Validação de mapa vigente pré-existente.

*   **Incorreto:**
    *   Mesmos problemas do CDU-04 (Emails genéricos e falta de Snapshot hierárquico).

## CDU-06 - Realizar cadastro

**Resumo:** Unidade (Chefe) cadastra Atividades e Conhecimentos. Salva rascunho. Conclui (Disponibiliza) cadastro.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoCadastroController`, `AtividadeController`, `SubprocessoService`.
    *   Frontend: `CadAtividades.vue`.

*   **Pronto:**
    *   CRUD de Atividades e Conhecimentos.
    *   Fluxo de disponibilização.
    *   Importação de atividades.

*   **Incorreto:**
    *   **Validação de Atividade Mínima:** O requisito exige "Pelo menos uma atividade cadastrada". A validação em `SubprocessoCadastroController.disponibilizarCadastro` verifica apenas se existem "atividades sem conhecimento". Se a lista de atividades for vazia, a validação passa, permitindo enviar um subprocesso sem nenhuma atividade, o que é um erro de lógica grave.

## CDU-07 - Analisar cadastro

**Resumo:** Unidade Superior ou SEDOC analisa o cadastro disponibilizado. Pode Devolver (ajustes) ou Validar/Homologar.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoCadastroController`, `SubprocessoWorkflowService`.

*   **Pronto:**
    *   Ações de aceitar, devolver e homologar implementadas.
    *   Registro de histórico de análise (`AnaliseService`).
    *   Controle de permissões (Gestor/Admin).

*   **Faltante:**
    *   Não detectadas falhas óbvias na análise estática.

## CDU-08 a CDU-11 (Fluxos de Revisão e Ajuste de Cadastro)

**Resumo:** Ciclos de correção e reanálise do cadastro de atividades.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoCadastroController` (endpoints de revisão).

*   **Pronto:**
    *   Implementação espelha a lógica de análise de cadastro inicial, suportando os estados de revisão.

## CDU-12 - Disponibilizar mapa para validação

**Resumo:** SEDOC (Admin) disponibiliza o mapa criado/ajustado para a unidade validar.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoValidacaoController`, `SubprocessoMapaController`.

*   **Pronto:**
    *   Endpoint para disponibilizar mapa.

*   **Incorreto:**
    *   **Duplicidade de Endpoints:** Existem endpoints concorrentes para "disponibilizar mapa" em `SubprocessoMapaController` e `SubprocessoValidacaoController`, o que pode causar confusão sobre qual é o correto para o fluxo da SEDOC versus fluxo da Unidade.

## CDU-15 - Manter mapa

**Resumo:** SEDOC cria/edita competências e as associa a atividades.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoMapaController`, `MapaService`.

*   **Pronto:**
    *   CRUD de competências.
    *   Associação com atividades.

*   **Incorreto:**
    *   **Métodos HTTP:** O `SubprocessoMapaController` utiliza métodos `PUT` e `DELETE` (`@PutMapping`, `@DeleteMapping`) para gerenciar competências. Isso viola a diretiva arquitetural do projeto que exige o uso exclusivo de `GET` e `POST`.

## CDU-16 a CDU-20 (Validação e Ajuste de Mapa)

**Resumo:** Unidade avalia impactos, valida o mapa ou sugere alterações. SEDOC ajusta e submete novamente.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `SubprocessoValidacaoController`, `SubprocessoMapaController`.

*   **Pronto:**
    *   Cálculo de impactos (`ImpactoMapaService`).
    *   Fluxo de sugestões e validação.
    *   Ajuste de mapa pela SEDOC.

*   **Faltante:**
    *   Validação estrita de que todas as sugestões foram "tratadas" (aceitas ou rejeitadas) antes de re-submeter não parece explícita, dependendo do analista verificar manualmente.

## CDU-21 - Finalizar processo

**Resumo:** Encerramento do processo. Mapas tornam-se vigentes. Notificações finais.

**Implementação:**

*   **Código Utilizado:**
    *   Backend: `ProcessoController`, `ProcessoService`.

*   **Pronto:**
    *   Validação de homologação de todos os subprocessos.
    *   Atualização do mapa vigente nas unidades.
    *   Envio de notificações de finalização.

## Questões Transversais (Cross-Cutting)

*   **Segurança:** A maioria dos endpoints possui anotações `@PreAuthorize` ou verificação via `@AuthenticationPrincipal`, mas a consistência varia. O endpoint `PainelController.listarProcessos` faz filtragem correta no Service, mas endpoints genéricos de `ProcessoController` podem expor dados se não protegidos adequadamente.
*   **Snapshot de Dados:** A falta de tabelas de histórico para a estrutura organizacional (`Unidade`) é um risco arquitetural para a integridade dos dados históricos dos processos (Requisito de preservação da hierarquia).
*   **Padronização de API:** Violação do padrão POST/GET em `SubprocessoMapaController`.

## Conclusão

A implementação cobre a maior parte dos fluxos funcionais ("Caminho Feliz"), mas falha em detalhes críticos de requisitos de negócio (validação de atividade mínima, conteúdo de e-mails, regras de exibição no painel) e requisitos não-funcionais/arquiteturais (snapshot de hierarquia, padronização HTTP). Ações corretivas são necessárias nos controladores e serviços identificados.
