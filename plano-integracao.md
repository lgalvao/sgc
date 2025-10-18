# Plano de Integração Frontend-Backend: Guia para Agente de IA

**Nota de Verificação:** Este documento foi revisado e atualizado. As seções marcadas como `[VERIFICADO]` foram confirmadas como corretamente implementadas. As seções marcadas como `[VERIFICADO E CORRIGIDO]` foram ajustadas para alinhar o frontend com o backend. As seções marcadas como `[INCOMPLETO]` indicam funcionalidades que não puderam ser totalmente integradas devido a limitações ou ausência de APIs no backend.

Este documento serve como um guia passo a passo para um agente de IA integrar o frontend (Vue.js) com o backend (Spring Boot) do sistema SGC. O foco está nos ajustes necessários no frontend e em potenciais modificações nos DTOs e controllers do backend para facilitar a integração.

**Princípios Orientadores para o Agente:**
*   **Clareza e Qualidade:** A prioridade na definição dos nomes e estruturas será a clareza e a qualidade geral da integração, e não a primazia de um lado (frontend ou backend) sobre o outro.
*   **Análise de Contexto:** Antes de qualquer modificação, utilize ferramentas como `read_file`, `glob` e `search_file_content` para analisar o código existente, convenções, estilo e padrões do projeto. Mantenha a consistência com o código adjacente.
*   **Verificação Contínua:** Após cada etapa de implementação, execute os testes relevantes (unitários, E2E) e verifique os resultados. Utilize `run_shell_command` para executar comandos de teste e `read_file` para inspecionar logs ou saídas.
*   **Iteração:** Este plano é um guia. Esteja preparado para refinar a abordagem ou solicitar esclarecimentos se encontrar ambiguidades ou desafios inesperados.

## 1. Análise das APIs do Backend

A seguir, uma lista dos principais endpoints e DTOs de entrada/saída identificados nos controllers do backend. Utilize esta seção como referência para entender as interações necessárias.

### Módulo Alerta (`/api/alertas`)
*   `POST /{id}/marcar-como-lido`: Marca um alerta como lido.
    *   **Entrada:** `id` (PathVariable)
    *   **Saída:** `ResponseEntity<?>` (mensagem de sucesso)
    *   **DTOs:** Nenhum DTO de corpo de requisição/resposta explícito.

### Módulo Análise (`/api/subprocessos/{id}/analises-cadastro`, `/api/subprocessos/{id}/analises-validacao`)
*   `GET /{id}/historico-cadastro`: Lista histórico de análises de cadastro.
    *   **Entrada:** `id` (PathVariable - subprocesso)
    *   **Saída:** `List<AnaliseHistoricoDto>`
*   `GET /{id}/historico-validacao`: Lista histórico de análises de validação.
    *   **Entrada:** `id` (PathVariable - subprocesso)
    *   **Saída:** `List<AnaliseValidacaoHistoricoDto>`

### Módulo Atividade (`/api/atividades`)
*   `GET /`: Lista todas as atividades.
    *   **Saída:** `List<AtividadeDto>`
*   `GET /{idAtividade}`: Obtém atividade por ID.
    *   **Saída:** `AtividadeDto`
*   `POST /`: Cria nova atividade.
    *   **Entrada:** `AtividadeDto`
    *   **Saída:** `AtividadeDto`
*   `PUT /{id}`: Atualiza atividade.
    *   **Entrada:** `id` (PathVariable), `AtividadeDto`
    *   **Saída:** `AtividadeDto`
*   `DELETE /{id}`: Exclui atividade.
    *   **Saída:** `ResponseEntity<Void>`
*   `GET /{atividadeId}/conhecimentos`: Lista conhecimentos de uma atividade.
    *   **Saída:** `List<ConhecimentoDto>`
*   `POST /{atividadeId}/conhecimentos`: Cria novo conhecimento para atividade.
    *   **Entrada:** `atividadeId` (PathVariable), `ConhecimentoDto`
    *   **Saída:** `ConhecimentoDto`
*   `PUT /{atividadeId}/conhecimentos/{conhecimentoId}`: Atualiza conhecimento.
    *   **Entrada:** `atividadeId`, `conhecimentoId` (PathVariable), `ConhecimentoDto`
    *   **Saída:** `ConhecimentoDto`
*   `DELETE /{atividadeId}/conhecimentos/{conhecimentoId}`: Exclui conhecimento.
    *   **Saída:** `ResponseEntity<Void>`
*   **DTOs:** `AtividadeDto`, `ConhecimentoDto`.

### Módulo Competência (`/api/competencias`)
*   `GET /`: Lista todas as competências.
    *   **Saída:** `List<CompetenciaDto>`
*   `GET /{id}`: Obtém competência por ID.
    *   **Saída:** `CompetenciaDto`
*   `POST /`: Cria nova competência.
    *   **Entrada:** `CompetenciaDto`
    *   **Saída:** `CompetenciaDto`
*   `PUT /{id}`: Atualiza competência.
    *   **Entrada:** `id` (PathVariable), `CompetenciaDto`
    *   **Saída:** `CompetenciaDto`
*   `DELETE /{id}`: Exclui competência.
    *   **Saída:** `ResponseEntity<Void>`
*   `GET /{idCompetencia}/atividades`: Lista atividades vinculadas a uma competência.
    *   **Saída:** `List<CompetenciaAtividade>` (entidade `CompetenciaAtividade` diretamente)
*   `POST /{idCompetencia}/atividades`: Vincula atividade a competência.
    *   **Entrada:** `idCompetencia` (PathVariable), `VinculoAtividadeReq` (contém `idAtividade`)
    *   **Saída:** `ResponseEntity<?>` (`CompetenciaAtividade`)
*   `DELETE /{idCompetencia}/atividades/{idAtividade}`: Desvincula atividade de competência.
    *   **Saída:** `ResponseEntity<Void>`
*   **DTOs:** `CompetenciaDto`, `VinculoAtividadeReq`.

### Módulo Painel (`/api/painel`)
*   `GET /processos`: Lista processos para o painel.
    *   **Entrada:** `perfil`, `unidade` (RequestParam), `Pageable`
    *   **Saída:** `Page<ProcessoResumoDto>`
*   `GET /alertas`: Lista alertas para o painel.
    *   **Entrada:** `usuarioTitulo`, `unidade` (RequestParam), `Pageable`
    *   **Saída:** `Page<AlertaDto>`
*   **DTOs:** `ProcessoResumoDto`, `AlertaDto`.

### Módulo Mapa (`/api/mapas`)
*   `GET /`: Lista todos os mapas.
    *   **Saída:** `List<MapaDto>`
*   `GET /{id}`: Obtém mapa por ID.
    *   **Saída:** `MapaDto`
*   `POST /`: Cria novo mapa.
    *   **Entrada:** `MapaDto`
    *   **Saída:** `MapaDto`
*   `PUT /{id}`: Atualiza mapa.
    *   **Entrada:** `id` (PathVariable), `MapaDto`
    *   **Saída:** `MapaDto`
*   `DELETE /{id}`: Exclui mapa.
    *   **Saída:** `ResponseEntity<Void>`
*   **DTOs:** `MapaDto`.

### Módulo Processo (`/api/processos`)
*   `POST /`: Cria novo processo.
    *   **Entrada:** `CriarProcessoReq`
    *   **Saída:** `ProcessoDto`
*   `GET /{id}`: Obtém processo por ID.
    *   **Saída:** `ProcessoDto`
*   `PUT /{id}`: Atualiza processo.
    *   **Entrada:** `id` (PathVariable), `AtualizarProcessoReq`
    *   **Saída:** `ProcessoDto`
*   `DELETE /{id}`: Exclui processo.
    *   **Saída:** `ResponseEntity<Void>`
*   `GET /{id}/detalhes`: Obtém detalhes completos de um processo.
    *   **Saída:** `ProcessoDetalheDto`
*   `POST /{id}/iniciar`: Inicia um processo.
    *   **Entrada:** `id` (PathVariable), `tipo` (RequestParam), `List<Long> unidades` (RequestBody)
    *   **Saída:** `ResponseEntity<ProcessoDto>`
*   `POST /{id}/finalizar`: Finaliza um processo.
    *   **Entrada:** `id` (PathVariable)
    *   **Saída:** `ResponseEntity<?>`
*   **DTOs:** `CriarProcessoReq`, `AtualizarProcessoReq`, `ProcessoDto`, `ProcessoDetalheDto`.

### Módulo Usuário (`/api/usuarios`)
*   `POST /autenticar`: Autentica usuário.
    *   **Entrada:** `AutenticacaoRequest`
    *   **Saída:** `ResponseEntity<Boolean>`
*   `POST /autorizar`: Autoriza usuário (retorna perfis/unidades).
    *   **Entrada:** `Long tituloEleitoral` (RequestBody)
    *   **Saída:** `ResponseEntity<List<PerfilUnidade>>`
*   `POST /entrar`: Registra entrada do usuário no sistema.
    *   **Entrada:** `EntrarRequest`
    *   **Saída:** `ResponseEntity<Void>`
*   **DTOs:** `AutenticacaoRequest`, `EntrarRequest`, `PerfilUnidade`.

### Módulo Subprocesso (`/api/subprocessos`)

#### SubprocessoCadastroControle
*   `GET /{id}/historico-cadastro`: Histórico de análises de cadastro.
    *   **Saída:** `List<AnaliseHistoricoDto>`
*   `POST /{id}/disponibilizar`: Disponibiliza cadastro de atividades.
    *   **Saída:** `ResponseEntity<RespostaDto>`
*   `POST /{id}/disponibilizar-revisao`: Disponibiliza revisão do cadastro.
    *   **Saída:** `ResponseEntity<RespostaDto>`
*   `GET /{id}/cadastro`: Obtém cadastro de subprocesso.
    *   **Saída:** `SubprocessoCadastroDto`
*   `POST /{id}/devolver-cadastro`: Devolve cadastro de atividades.
    *   **Entrada:** `DevolverCadastroReq`
*   `POST /{id}/aceitar-cadastro`: Aceita cadastro de atividades.
    *   **Entrada:** `AceitarCadastroReq`
*   `POST /{id}/homologar-cadastro`: Homologa cadastro de atividades.
    *   **Entrada:** `HomologarCadastroReq`
*   `POST /{id}/devolver-revisao-cadastro`: Devolve revisão do cadastro.
    *   **Entrada:** `DevolverCadastroReq`
*   `POST /{id}/aceitar-revisao-cadastro`: Aceita revisão do cadastro.
    *   **Entrada:** `AceitarCadastroReq`
*   `POST /{id}/homologar-revisao-cadastro`: Homologa revisão do cadastro.
    *   **Entrada:** `HomologarCadastroReq`
*   `POST /{id}/importar-atividades`: Importa atividades de outro subprocesso.
    *   **Entrada:** `ImportarAtividadesRequest`
    *   **Saída:** `Map<String, String>`
*   **DTOs:** `AnaliseHistoricoDto`, `RespostaDto`, `SubprocessoCadastroDto`, `DevolverCadastroReq`, `AceitarCadastroReq`, `HomologarCadastroReq`, `ImportarAtividadesRequest`.

#### SubprocessoMapaControle
*   `GET /{id}/impactos-mapa`: Verifica impactos no mapa de competências.
    *   **Saída:** `ImpactoMapaDto`
*   `GET /{id}/mapa-visualizacao`: Obtém mapa formatado para visualização.
    *   **Saída:** `MapaVisualizacaoDto`
*   `PUT /{id}/mapa-completo`: Salva mapa completo.
    *   **Entrada:** `SalvarMapaRequest`
    *   **Saída:** `MapaCompletoDto`
*   `GET /{id}/mapa-ajuste`: Obtém mapa para ajuste.
    *   **Saída:** `MapaAjusteDto`
*   `PUT /{id}/mapa-ajuste`: Salva ajustes no mapa.
    *   **Entrada:** `SalvarAjustesReq`
*   **DTOs:** `ImpactoMapaDto`, `MapaCompletoDto`, `MapaVisualizacaoDto`, `SalvarMapaRequest`, `MapaAjusteDto`, `SalvarAjustesReq`.

#### SubprocessoValidacaoControle
*   `POST /{id}/disponibilizar-mapa`: Disponibiliza mapa de competências.
    *   **Entrada:** `DisponibilizarMapaReq`
    *   **Saída:** `ResponseEntity<RespostaDto>`
*   `POST /{id}/apresentar-sugestoes`: Apresenta sugestões para o mapa.
    *   **Entrada:** `ApresentarSugestoesReq`
*   `POST /{id}/validar-mapa`: Valida mapa de competências.
*   `GET /{id}/sugestoes`: Obtém sugestões.
    *   **Saída:** `SugestoesDto`
*   `GET /{id}/historico-validacao`: Histórico de análises de validação.
    *   **Saída:** `List<AnaliseValidacaoHistoricoDto>`
*   `POST /{id}/devolver-validacao`: Devolve validação do mapa.
    *   **Entrada:** `DevolverValidacaoReq`
*   `POST /{id}/aceitar-validacao`: Aceita validação do mapa.
*   `POST /{id}/homologar-validacao`: Homologa validação do mapa.
*   `POST /{id}/submeter-mapa-ajustado`: Submete mapa ajustado.
    *   **Entrada:** `SubmeterMapaAjustadoReq`
*   **DTOs:** `DisponibilizarMapaReq`, `ApresentarSugestoesReq`, `SugestoesDto`, `AnaliseValidacaoHistoricoDto`, `DevolverValidacaoReq`, `SubmeterMapaAjustadoReq`.

## 2. Mapeamento de Dados (Frontend)

A pasta `frontend/src/mappers` será estendida para incluir funções de mapeamento para os DTOs do backend. Cada função `map*DtoTo*Model` converterá um DTO do backend para um modelo de dados do frontend, e vice-versa quando necessário (para requisições `POST`/`PUT`).

*   **`frontend/src/mappers/alertas.ts`**: Mapear `AlertaDto` para o modelo `Alerta` do frontend.
*   **`frontend/src/mappers/analises.ts`**: Mapear `AnaliseHistoricoDto` e `AnaliseValidacaoHistoricoDto` para o modelo `AnaliseValidacao` do frontend.
*   **`frontend/src/mappers/atividades.ts`**: Mapear `AtividadeDto` e `ConhecimentoDto` para os modelos `Atividade` e `Conhecimento` do frontend.
*   **`frontend/src/mappers/competencias.ts`**: Mapear `CompetenciaDto` e `CompetenciaAtividade` para o modelo `Competencia` do frontend.
*   **`frontend/src/mappers/mapas.ts`**: Mapear `MapaDto`, `MapaCompletoDto`, `ImpactoMapaDto`, `MapaAjusteDto`, `MapaVisualizacaoDto` para o modelo `Mapa` do frontend.
*   **`frontend/src/mappers/processos.ts`**: Mapear `ProcessoDto`, `ProcessoDetalheDto`, `ProcessoResumoDto` para o modelo `Processo` do frontend.
*   **`frontend/src/mappers/subprocessos.ts`**: Mapear `SubprocessoDto`, `SubprocessoDetalheDto`, `SubprocessoCadastroDto`, `SugestoesDto` para o modelo `Subprocesso` do frontend.
*   **`frontend/src/mappers/sgrh.ts`**: Mapear `AutenticacaoRequest`, `EntrarRequest`, `PerfilUnidade`, `ResponsavelDto`, `UnidadeDto`, `UsuarioDto` para os modelos correspondentes do frontend.

## 3. Serviços de API (Frontend)

Serão criados serviços na pasta `frontend/src/services` para encapsular as chamadas Axios. Cada serviço será responsável por interagir com um módulo específico do backend.

*   **Estrutura:** Cada serviço terá métodos correspondentes aos endpoints do backend, utilizando `apiClient` (configurado em `axios-setup.ts`).
*   **Mapeamento:** Os dados de requisição e resposta serão mapeados usando as funções do diretório `mappers`.
*   **Tratamento de Erros:** Os serviços incluirão blocos `try-catch` para lidar com erros de rede e respostas HTTP com status de erro, lançando exceções ou retornando valores padrão.

**Exemplos de Serviços:**
*   `frontend/src/services/alertaService.ts`
*   `frontend/src/services/analiseService.ts`
*   `frontend/src/services/atividadeService.ts`
*   `frontend/src/services/competenciaService.ts`
*   `frontend/src/services/painelService.ts`
*   `frontend/src/services/mapaService.ts`
*   `frontend/src/services/processoService.ts`
*   `frontend/src/services/usuarioService.ts`
*   `frontend/src/services/subprocessoService.ts`

## 4. Gerenciamento de Estado (Frontend)

As stores Pinia existentes (`alertas`, `atividades`, `atribuicoes`, `mapas`, `processos`, `servidores`, `subprocessos`, `unidades`) serão atualizadas para:
*   **Substituir Mocks:** Remover os dados mockados (`mocks/*.json`) e substituí-los por chamadas aos novos serviços de API.
*   **Centralizar Lógica:** As actions das stores serão responsáveis por chamar os serviços de API, processar as respostas (via mappers) e atualizar o estado reativo.
*   **Otimização:** Implementar cache simples ou estratégias de revalidação de dados conforme a necessidade para evitar requisições desnecessárias.

## 5. Componentes Vue (Frontend)

Os componentes Vue (`views` e `components`) serão adaptados para:
*   **Consumir Stores:** Utilizar as stores Pinia atualizadas para obter e manipular dados.
*   **Interagir com Stores:** Enviar requisições de alteração de estado (e.g., criar, atualizar, excluir) através das actions das stores, em vez de manipular dados localmente ou via mocks.
*   **Remover Lógica de Mock:** Eliminar qualquer lógica de mock ou dados estáticos que atualmente simulam a interação com o backend.
*   **Adaptação da UI:** Ajustar a interface do usuário para refletir os dados reais do backend e as interações assíncronas.

## 6. Autenticação e Autorização

*   **Fluxo de Login:** O componente `Login.vue` será modificado para:
    1.  Chamar `/api/usuarios/autenticar` com `tituloEleitoral` e `senha`.
    2.  Se autenticado, chamar `/api/usuarios/autorizar` com `tituloEleitoral` para obter a lista de `PerfilUnidade`.
    3.  Permitir que o usuário selecione um `PerfilUnidade` (se houver múltiplos).
    4.  Chamar `/api/usuarios/entrar` com o `EntrarRequest` contendo o `tituloEleitoral`, `perfil` e `unidadeCodigo` selecionados.
    5.  Armazenar o `servidorId`, `perfilSelecionado` e `unidadeSelecionada` no `perfilStore` e no `localStorage`.
*   **Headers de Autorização:** O `axios-setup.ts` será configurado para incluir um token de autorização (se o backend implementar JWT ou similar) em todas as requisições, obtido do `perfilStore`.
*   **Controle de Acesso na UI:** Componentes Vue utilizarão o `perfilSelecionado` do `perfilStore` para habilitar/desabilitar funcionalidades ou exibir/esconder elementos da UI, alinhado com as anotações `@PreAuthorize` do backend.

## 7. Tratamento de Erros

*   **Interceptor Axios:** Será implementado um interceptor global no `axios-setup.ts` para:
    *   Capturar respostas HTTP com status de erro (4xx, 5xx).
    *   Extrair mensagens de erro do corpo da resposta (o backend retorna `ErroApi`).
    *   Utilizar a `notificacoesStore` para exibir mensagens de erro amigáveis ao usuário.
    *   Lidar com erros específicos, como 401 (Não Autorizado) redirecionando para a tela de login.
*   **Tratamento Específico:** Em casos onde a lógica de negócio exige tratamento de erro diferenciado (e.g., validações de formulário), os serviços de API e as actions das stores podem implementar tratamento de erro mais granular.

## 8. Ajustes no Backend (DTOs e Controllers)

Embora o foco seja no frontend, alguns ajustes no backend podem otimizar a integração:

*   **`AlertaControle.java` - `marcarComoLido`:**
    *   **Ajuste:** O `usuarioTitulo` está hardcoded como `"USUARIO_ATUAL"`.
    *   **Proposta:** Modificar para obter o `usuarioTitulo` do contexto de segurança (`@AuthenticationPrincipal UserDetails userDetails`) ou de um parâmetro de requisição, para que o frontend possa enviar o ID do usuário logado.
*   **Padronização de DTOs:** Revisar todos os DTOs para garantir que os nomes dos campos sejam consistentes e sigam as convenções do frontend (e.g., `camelCase`). Se houver campos como `codigo` e `id` para o mesmo propósito, padronizar para um deles.
*   **Consistência de Respostas:** Garantir que as respostas de erro (`ErroApi`) sejam sempre consistentes em sua estrutura para facilitar o tratamento no frontend.
*   **Paginação:** Para endpoints que retornam listas grandes (e.g., `/api/painel/processos`, `/api/painel/alertas`), garantir que a paginação seja implementada de forma eficiente e que o frontend possa consumir os metadados de paginação (total de páginas, total de elementos, etc.).

## 9. Abordagem em Etapas Coesas e Adaptação de Testes E2E

Para garantir uma integração gradual e gerenciável, o processo será dividido em etapas coesas, focando em funcionalidades ou módulos específicos. Em cada etapa, os testes de integração E2E (`@frontend/e2e/**`) serão adaptados para refletir a transição dos mocks para as chamadas reais ao backend. Os testes E2E existentes servirão como uma excelente referência para os fluxos de usuário, mas precisarão ser ajustados onde dependerem diretamente de detalhes dos mocks.

### Etapa 0: Configuração Base e Tratamento de Erros

*   **Objetivo:** Estabelecer a base de comunicação com o backend e um tratamento de erros robusto.
*   **Backend:** `sgc.comum.erros.RestExceptionHandler` (para entender a estrutura de erros).
*   **Frontend:**
    *   `frontend/src/axios-setup.ts`: Configure a instância do Axios para apontar para o backend e implemente um interceptor de requisições/respostas para tratamento global de erros (e.g., 401, 403, 500). **[VERIFICADO]**
    *   `frontend/src/stores/notificacoes.ts`: Utilize a `notificacoesStore` para exibir mensagens de erro amigáveis ao usuário. **[VERIFICADO]**
*   **Testes E2E:**
    *   Crie testes E2E básicos para verificar o interceptor de erros (e.g., simular uma resposta 401 e verificar o redirecionamento para o login).
    *   Adapte testes existentes para verificar a exibição de notificações de erro genéricas.

### Etapa 1: Autenticação e Autorização

*   **Objetivo:** Implementar o fluxo completo de login, seleção de perfil/unidade e persistência do estado de autenticação.
*   **Backend:** `sgc.sgrh.UsuarioControle` (`/api/usuarios/autenticar`, `/api/usuarios/autorizar`, `/api/usuarios/entrar`).
*   **Frontend:**
    *   **Mappers:** Crie/atualize `frontend/src/mappers/sgrh.ts` para mapear `AutenticacaoRequest`, `EntrarRequest`, `PerfilUnidade`, `UsuarioDto`. **[VERIFICADO]**
    *   **Serviços de API:** Crie `frontend/src/services/usuarioService.ts` com métodos para autenticar, autorizar e entrar. **[VERIFICADO]**
    *   **Stores Pinia:** Atualize `frontend/src/stores/perfil.ts` para chamar `usuarioService` e armazenar dados reais. **[VERIFICADO]**
    *   **Componentes Vue:** Modifique `frontend/src/views/Login.vue` para substituir a lógica de mock por chamadas ao `usuarioService`. **[VERIFICADO]**
*   **Testes E2E:**
    *   Adapte `frontend/e2e/cdu/cdu-01.spec.ts` para usar o fluxo de login real, verificando autenticação bem-sucedida, falha de credenciais e logout. **[VERIFICADO]**
    *   Atualize `frontend/e2e/utils/auth.ts` e `frontend/e2e/utils/authHelpers.ts` para usar o fluxo de login real, removendo mocks de `localStorage`.

### Etapa 2: Painel de Controle (Listagem de Processos e Alertas)

*   **Objetivo:** Exibir os processos e alertas do usuário logado no painel principal.
*   **Backend:** `sgc.comum.PainelControle` (`/api/painel/processos`, `/api/painel/alertas`), `sgc.alerta.AlertaControle` (`/api/alertas/{id}/marcar-como-lido`).
*   **Frontend:**
    *   **Mappers:** Crie/atualize `frontend/src/mappers/processos.ts` (mapear `ProcessoResumoDto`), `frontend/src/mappers/alertas.ts` (mapear `AlertaDto`). **[VERIFICADO]**
    *   **Serviços de API:** Crie `frontend/src/services/painelService.ts` (métodos para listar processos e alertas), `frontend/src/services/alertaService.ts` (método para marcar alerta como lido). **[VERIFICADO]**
    *   **Stores Pinia:** Atualize `frontend/src/stores/processos.ts` e `frontend/src/stores/alertas.ts` para buscar dados reais. **[VERIFICADO]**
    *   **Componentes Vue:** Modifique `frontend/src/views/Painel.vue` para substituir mocks por dados das stores atualizadas. **[VERIFICADO]**
*   **Testes E2E:**
    *   Adapte `frontend/e2e/cdu/cdu-02.spec.ts` para verificar a listagem de processos e alertas vindos do backend, ordenação e marcação de alertas como lidos. **[VERIFICADO]**
    *   Atualize `frontend/e2e/visual/02-painel.spec.ts` para capturar telas com dados reais.

### Etapa 3: Manter Processo (CRUD Básico)

*   **Objetivo:** Implementar a criação, edição, visualização de detalhes e exclusão de processos.
*   **Backend:** `sgc.processo.ProcessoControle` (`/api/processos`).
*   **Frontend:**
    *   **Mappers:** Crie/atualize `frontend/src/mappers/processos.ts` (mapear `CriarProcessoReq`, `AtualizarProcessoReq`, `ProcessoDto`, `ProcessoDetalheDto`). **[VERIFICADO]**
    *   **Serviços de API:** Crie `frontend/src/services/processoService.ts` com métodos para CRUD de processos. **[VERIFICADO]**
    *   **Stores Pinia:** Atualize `frontend/src/stores/processos.ts` para chamar `processoService`. **[VERIFICADO]**
    *   **Componentes Vue:** Modifique `frontend/src/views/CadProcesso.vue` (criar/editar), `frontend/src/views/Processo.vue` (detalhes), `frontend/src/views/Historico.vue` (listagem). **[INCOMPLETO - `Historico.vue` usa workaround por falta de endpoint específico no backend]**
*   **Testes E2E:**
    *   Adapte `frontend/e2e/cdu/cdu-03.spec.ts` e `frontend/e2e/cdu/cdu-06.spec.ts` para testar o CRUD de processos com o backend real. **[VERIFICADO]**
    *   Atualize `frontend/e2e/geral/cad-processo.spec.ts` e `frontend/e2e/visual/03-processos.spec.ts` para usar o backend real. **[VERIFICADO]**

### Etapa 4: Iniciar e Finalizar Processo

*   **Objetivo:** Implementar as ações de iniciar e finalizar um processo.
*   **Backend:** `sgc.processo.ProcessoControle` (`/api/processos/{id}/iniciar`, `/api/processos/{id}/finalizar`).
*   **Frontend:**
    *   **Serviços de API:** Atualize `frontend/src/services/processoService.ts` com métodos para iniciar e finalizar processos. **[VERIFICADO]**
    *   **Stores Pinia:** Atualize `frontend/src/stores/processos.ts` para chamar os serviços. **[VERIFICADO]**
    *   **Componentes Vue:** Modifique `frontend/src/views/CadProcesso.vue` (botão iniciar), `frontend/src/views/Processo.vue` (botão finalizar). **[VERIFICADO]**
*   **Testes E2E:**
    *   Adapte `frontend/e2e/cdu/cdu-04.spec.ts`, `frontend/e2e/cdu/cdu-05.spec.ts` e `frontend/e2e/cdu/cdu-21.spec.ts` para testar o início e finalização de processos. **[VERIFICADO]**

### Etapa 5: Manter Atividades e Conhecimentos

*   **Objetivo:** Gerenciar atividades e conhecimentos dentro de um subprocesso.
*   **Backend:** `sgc.atividade.AtividadeControle` (`/api/atividades`), `sgc.subprocesso.SubprocessoCadastroControle` (`/api/subprocessos/{id}/importar-atividades`).
*   **Frontend:**
    *   **Mappers:** Crie/atualize `frontend/src/mappers/atividades.ts` (mapear `AtividadeDto`, `ConhecimentoDto`). **[VERIFICADO]**
    *   **Serviços de API:** Crie `frontend/src/services/atividadeService.ts` (CRUD de atividades/conhecimentos), atualize `frontend/src/services/subprocessoService.ts` (método para importar atividades). **[VERIFICADO]**
    *   **Stores Pinia:** Atualize `frontend/src/stores/atividades.ts` para chamar os serviços. **[VERIFICADO]**
    *   **Componentes Vue:** Modifique `frontend/src/views/CadAtividades.vue` (edição), `frontend/src/views/VisAtividades.vue` (visualização). **[VERIFICADO]**
*   **Testes E2E:**
    *   Adapte `frontend/e2e/cdu/cdu-08.spec.ts`, `frontend/e2e/cdu/cdu-09.spec.ts`, `frontend/e2e/cdu/cdu-10.spec.ts` e `frontend/e2e/cdu/cdu-11.spec.ts` para testar a manutenção de atividades e conhecimentos.
    *   Atualize `frontend/e2e/visual/04-atividades.spec.ts` para capturar telas com dados reais.

### Etapa 6: Manter Mapa de Competências

*   **Objetivo:** Gerenciar o mapa de competências, incluindo criação, edição, visualização e ações de workflow (disponibilizar, ajustar).
*   **Backend:** `sgc.mapa.MapaControle` (`/api/mapas`), `sgc.subprocesso.SubprocessoMapaControle` (`/api/subprocessos/{id}/mapa-completo`, `/api/subprocessos/{id}/mapa-visualizacao`, `/api/subprocessos/{id}/impactos-mapa`, `/api/subprocessos/{id}/mapa-ajuste`), `sgc.subprocesso.SubprocessoValidacaoControle` (`/api/subprocessos/{id}/disponibilizar-mapa`, `/api/subprocessos/{id}/apresentar-sugestoes`, `/api/subprocessos/{id}/validar-mapa`, `/api/subprocessos/{id}/devolver-validacao`, `/api/subprocessos/{id}/aceitar-validacao`, `/api/subprocessos/{id}/homologar-validacao`, `/api/subprocessos/{id}/submeter-mapa-ajustado`).
*   **Frontend:**
    *   **Mappers:** Crie/atualize `frontend/src/mappers/mapas.ts` (mapear todos os DTOs de mapa). **[VERIFICADO]**
    *   **Serviços de API:** Crie `frontend/src/services/mapaService.ts`, atualize `frontend/src/services/subprocessoService.ts` (métodos relacionados a mapa). **[VERIFICADO]**
    *   **Stores Pinia:** Atualize `frontend/src/stores/mapas.ts` para chamar os serviços. **[VERIFICADO]**
    *   **Componentes Vue:** Modifique `frontend/src/views/CadMapa.vue` (edição), `frontend/src/views/VisMapa.vue` (visualização e ações de workflow). **[INCOMPLETO - `CadMapa.vue` contém lógica de UI complexa e simulações que indicam integração parcial]**
*   **Testes E2E:**
    *   Adapte `frontend/e2e/cdu/cdu-12.spec.ts`, `frontend/e2e/cdu/cdu-15.spec.ts`, `frontend/e2e/cdu/cdu-16.spec.ts`, `frontend/e2e/cdu/cdu-17.spec.ts`, `frontend/e2e/cdu/cdu-18.spec.ts`, `frontend/e2e/cdu/cdu-19.spec.ts` e `frontend/e2e/cdu/cdu-20.spec.ts` para testar o gerenciamento e workflow de mapas.
    *   Atualize `frontend/e2e/geral/cad-mapa.spec.ts` e `frontend/e2e/visual/05-mapas.spec.ts` para usar o backend real.

### Etapa 7: Outras Funcionalidades (Unidades, Atribuição Temporária, Diagnóstico, Relatórios, Configurações)

*   **Objetivo:** Integrar as funcionalidades restantes.
*   **Backend:** `sgc.unidade.UnidadeControle` (se houver), `sgc.sgrh.SgrhService` (para dados de unidades e servidores), `sgc.analise.AnaliseControle`, `sgc.comum.PainelControle` (relatórios), `sgc.comum.config.ConfigAplicacao` (configurações).
*   **Frontend:**
    *   **Mappers:** Crie/atualize `frontend/src/mappers/unidades.ts` e `frontend/src/mappers/servidores.ts`. **[VERIFICADO]**
    *   **Serviços de API:** Crie `frontend/src/services/unidadeService.ts`, `frontend/src/services/servidorService.ts`, `frontend/src/services/analiseService.ts`, etc. **[VERIFICADO E CORRIGIDO - `analiseService.ts` corrigido. `unidadeService` e `servidorService` não puderam ser criados por falta de endpoints no backend.]**
    *   **Stores Pinia:** Atualize `frontend/src/stores/unidades.ts`, `frontend/src/stores/servidores.ts`, `frontend/src/stores/analises.ts` e `frontend/src/stores/configuracoes.ts`. **[VERIFICADO E CORRIGIDO - `analises.ts` verificado. `unidades.ts` e `servidores.ts` permanecem com mocks por falta de API no backend.]**
    *   **Componentes Vue:** Modifique `frontend/src/views/Unidade.vue`, `frontend/src/views/CadAtribuicao.vue`, `frontend/src/views/DiagnosticoEquipe.vue`, `frontend/src/views/OcupacoesCriticas.vue`, `frontend/src/views/Relatorios.vue` e `frontend/src/views/Configuracoes.vue`.
*   **Testes E2E:**
    *   Adapte `frontend/e2e/geral/cad-atribuicao.spec.ts`, `frontend/e2e/geral/configuracoes.spec.ts`, `frontend/e2e/geral/relatorios.spec.ts` e `frontend/e2e/geral/unidades.spec.ts`.
    *   Atualize `frontend/e2e/visual/06-unidades.spec.ts` e `frontend/e2e/visual/07-admin.spec.ts` para capturar telas com dados reais.

Esta abordagem permitirá que o agente trabalhe em blocos menores, testando e validando cada funcionalidade integrada antes de passar para a próxima, minimizando riscos e facilitando a depuração.