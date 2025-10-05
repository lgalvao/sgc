# plano de implementação backend para requisitos

Objetivo: produzir um plano técnico e executável para implementar no backend os casos de uso descritos em [`reqs/`](reqs:1) e nos arquivos individuais como [`reqs/cdu-01.md`](reqs/cdu-01.md:1) ... [`reqs/cdu-21.md`](reqs/cdu-21.md:1).

1. Resumo executivo

- Escopo: análise completa dos CDUs listados em [`reqs/`](reqs:1); definição de endpoints, DTOs, entidades, repositórios, serviços, eventos assíncronos, regras de estado e plano de testes.
- Premissas:
  - Integração de autenticação via serviço externo (Sistema Acesso) conforme [`reqs/cdu-01.md`](reqs/cdu-01.md:1).
  - Modelo de dados base descrito em [`reqs/modelo-dados.md`](reqs/modelo-dados.md:1).

2. Visão geral da arquitetura de backend

- Camadas: Controller (REST), Service (regras de negócio), Repository (Spring Data JPA), Domain (JPA entities), DTO/mappers, Events (application events) e Async Workers (fila simples usando Spring @Async ou Spring Events).
- Persistência: manter esquema conforme [`reqs/modelo-dados.md`](reqs/modelo-dados.md:1). Novos campos/tabelas serão adicionados com migrações (Flyway/liquibase).
- Integrações:
  - Sistema Acesso (autenticação) — client HTTP.
  - Serviço de e‑mail (SMTP ou provider) — adapter de notificação.
- Eventos assíncronos:
  - eventos: ProcessCreatedEvent, SubprocessCreatedEvent, MovementRecordedEvent, MapDisponibilizedEvent, AlertCreatedEvent.
  - Implementação: publicar ApplicationEvent + listener executando envio de e‑mail e criação de ALERTA em transação separada.

3. Lista de CDUs (resumo por CDU)

- CDU-01: Login e estrutura
  - Requisito: [`reqs/cdu-01.md`](reqs/cdu-01.md:1)
  - Endpoints:
    - POST /api/auth/login  (body: LoginRequest {titulo:string, senha:string, unidadeId?:number, perfil?:string}) -> LoginResponse {token, perfis:[{perfil,unidadeId}], unidades:[]}
  - DTOs:
    - LoginRequest {titulo:String, senha:String}
    - LoginResponse {token:String, perfis:List<PerfilUnidadeDTO>}
    - PerfilUnidadeDTO {perfil:String, unidadeCodigo:Long, sigla:String}
  - Entidades/Model: integração com VW_USUARIO_PERFIL_UNIDADE (não alterar DB; mapear via repository read-only).
  - Repositórios: UserProfileViewRepository (read-only).
  - Serviços: AuthService -> valida via Sistema Acesso client; consulta perfis/vw-views.
  - Validações: @NotBlank em LoginRequest.
  - Eventos: LoginSuccessEvent (audit).
  - Tests: unit AuthService (mock Sistema Acesso), integration MockMvc login flow.
  - Prioridade: Alta. Est. Esforço: Pequeno.
  - Dependências: nenhum.

- CDU-02: Visualizar Painel
  - Requisito: [`reqs/cdu-02.md`](reqs/cdu-02.md:1)
  - Endpoints:
    - GET /api/painel/processos?perfil=...&unidade=: retorna listagem filtrada (ProcessoSummaryDTO)
    - GET /api/painel/alertas: retorna alertas do usuário/unidade (AlertDTO)
  - DTOs:
    - ProcessoSummaryDTO {codigo:Long, descricao:String, tipo:String, unidadesParticipantes:List<String>, situacao:String}
    - AlertDTO {codigo:Long, dataHora:ISO8601, processoDescricao:String, unidadeOrigemSigla:String, descricao:String, lido:Boolean}
  - Entidades/Model: usar PROCESSO, UNIDADE_PROCESSO, ALERTA, ALERTA_USUARIO.
  - Repositórios: ProcessoRepository, AlertaRepository, AlertaUsuarioRepository, UnidadeProcessoRepository.
  - Serviços: PainelService -> aplicar regras de visibilidade (unidades subordinadas).
  - Regras/Validações: listar apenas 'Criado' para ADMIN.
  - Eventos: none.
  - Prioridade: Alta. Esforço: Médio.
  - Dependências: CDU-01 (auth).

- CDU-03: Manter processo (criar/editar/remover)
  - Requisito: [`reqs/cdu-03.md`](reqs/cdu-03.md:1)
  - Endpoints:
    - POST /api/processos   -> CreateProcessRequest {descricao,tipo,dataLimiteEtapa1,List<unidadeCodigos>} -> ProcessDTO
    - PUT /api/processos/{id} -> UpdateProcessRequest ...
    - DELETE /api/processos/{id}
    - GET /api/processos/{id}
  - DTOs:
    - CreateProcessRequest {descricao:String@Size(1,255), tipo:Enum, dataLimiteEtapa1:Date, unidades:List<Long>}
    - ProcessDTO {codigo,descricao,tipo,situacao,dataCriacao}
  - Entidades/Model: PROCESSO (usar colunas existentes). Necessário: persistir snapshot de unidades em UNIDADE_PROCESSO (inserir registros ao iniciar processo) — CDU-04 handles snapshot; for create keep process in 'CRIADO'.
  - Repositórios: ProcessoRepository, UnidadeProcessoRepository.
  - Serviços: ProcessoService (validações: descrição obrigatória; pelo menos uma unidade; para REVISÃO/DIAGNOSTICO só unidades com mapa vigente).
  - Validações: Bean Validation e consulta UNIDADE_MAPA para restrição.
  - Eventos: ProcessCreatedEvent (on create when started).
  - Tests: unit/service validations; integration MockMvc create/edit/delete.
  - Prioridade: Alta. Esforço: Médio.
  - Dependências: CDU-01, CDU-02.

- CDU-04: Iniciar processo de mapeamento
  - Requisito: [`reqs/cdu-04.md`](reqs/cdu-04.md:1)
  - Endpoint:
    - POST /api/processos/{id}/iniciar?tipo=MAPEAMENTO
  - Ações de backend:
    - Validar existência e situação 'CRIADO'.
    - Registrar snapshot na tabela UNIDADE_PROCESSO (copiar árvore de unidades participantes).
    - Mudar situacao PROCESSO-> 'EM_ANDAMENTO'.
    - Criar SUBPROCESSO para cada unidade operacional ou interoperacional; inicializar campos (data_limite_etapa1, situacao_id='Nao iniciado'/cod equivalente).
    - Criar MAPA vazio e vincular ao subprocesso (criar MAPA and set subprocesso.mapa_codigo).
    - Registrar MOVIMENTACAO inicial para cada subprocesso (origem SEDOC).
    - Enfileirar envio de e‑mails e criação de ALERTA por unidade (publish ProcessStartedEvent).
  - Entidades/Model: SUBPROCESSO (usar colunas existentes), MAPA, MOVIMENTACAO, ALERTA.
  - Repositórios: SubprocessoRepository, MapaRepository, MovimentacaoRepository, AlertaRepository.
  - Serviços: ProcessoService.startMappingProcess(id).
  - Validações: verificar unidades não participando de processo ativo do mesmo tipo (reuse query).
  - Events: ProcessStartedEvent listeners: EmailNotificationService, AlertService.
  - Tests: integration test for starting process , assert subprocessos, mapas, movimentacoes e alertas criados; mock email.
  - Prioridade: Alta. Esforço: Alto.
  - Dependências: CDU-03.

- CDU-05: Iniciar processo de revisão
  - Requisito: [`reqs/cdu-05.md`](reqs/cdu-05.md:1)
  - Endpoint:
    - POST /api/processos/{id}/iniciar?tipo=REVISAO
  - Ações:
    - Similar a CDU-04, porém copia o mapa vigente por unidade (snapshot do MAPA + atividades + conhecimentos) e vincula à SUBPROCESSO (criar novos MAPA com conteúdo copiado).
    - Criar movimentações, alertas e e‑mails conforme modelo.
  - Repos/Serviços: MapCopyService (clonar mapas, atividades, conhecimentos).
  - Tests: integração que verifica cópia do mapa e criação de subprocessos.
  - Prioridade: Alta. Esforço: Alto.
  - Dependências: CDU-03, CDU-04 (similar).

- CDU-06: Detalhar processo
  - Requisito: [`reqs/cdu-06.md`](reqs/cdu-06.md:1)
  - Endpoint:
    - GET /api/processos/{id}/detalhes?perfil=...
  - Response: ProcessDetailDTO {dados do processo, lista hierárquica de unidades participantes com status de subprocesso e dataLimite}
  - Repos: ProcessoRepository, UnidadeProcessoRepository, SubprocessoRepository.
  - Serviço: ProcessoService.getDetails(id, perfil, unidadeUsuario)
  - Tests: MockMvc detail responses para diferentes perfis.
  - Prioridade: Alta. Esforço: Médio.
  - Dependências: CDU-03, CDU-04/05.

- CDU-07: Detalhar subprocesso
  - Requisito: [`reqs/cdu-07.md`](reqs/cdu-07.md:1)
  - Endpoint:
    - GET /api/subprocessos/{id}
  - Response: SubprocessoDetailDTO {unidade, responsavel, situacao, localizacaoAtual, prazoEtapaAtual, movimentacoes[], elementosDoProcesso[]}
  - Repos: SubprocessoRepository, MovimentacaoRepository, MapaRepository.
  - Serviços: SubprocessoService.getDetails(id, perfil)
  - Tests: integration and unit.
  - Prioridade: Alta. Esforço: Médio.
  - Dependências: CDU-04/05.

- CDU-08: Manter cadastro de atividades e conhecimentos
  - Requisito: [`reqs/cdu-08.md`](reqs/cdu-08.md:1)
  - Endpoints:
    - POST /api/subprocessos/{id}/atividades -> ActivityDTO (cria atividade + conhecimentos)
    - PUT /api/atividades/{id}
    - DELETE /api/atividades/{id}
    - POST /api/atividades/{id}/conhecimentos
    - PUT /api/conhecimentos/{id}
    - DELETE /api/conhecimentos/{id}
    - POST /api/subprocessos/{id}/importar-atividades {processoId, unidadeCodigo, atividadeIds[]}
  - DTOs: ActivityCreateDTO {descricao, conhecimentos:List<ConhecimentoDTO>}, ConhecimentoDTO {descricao}
  - Entidades: ATIVIDADE, CONHECIMENTO (usar MAPA referencia). Garantir vínculo ATIVIDADE.mapa_codigo ao mapa do subprocesso.
  - Repositórios: AtividadeRepository, ConhecimentoRepository.
  - Serviços: AtividadeService (salvamento automático após cada ação), ImportService (importar atividades com verificação de duplicatas por descricao).
  - Validações: atividade.descricao não vazia; conhecimento obrigatório ao disponibilizar (CDU-09).
  - Events: ActivityChangedEvent -> atualiza marcações e versão do mapa do subprocesso.
  - Tests: unit AtividadeService; integration fluxo adicionar/editar/remover; importar atividades.
  - Prioridade: Alta. Esforço: Alto.
  - Dependências: CDU-04/05, CDU-07.

- CDU-09: Disponibilizar cadastro de atividades e conhecimentos
  - Requisito: [`reqs/cdu-09.md`](reqs/cdu-09.md:1)
  - Endpoint:
    - POST /api/subprocessos/{id}/disponibilizar-cadastro {confirm:true}
  - Ações:
    - Validar que cada atividade tem >=1 conhecimento; se falhar, retornar 400 com lista de atividades faltantes.
    - Alterar situacao do subprocesso para 'Cadastro disponibilizado'.
    - Registrar MOVIMENTACAO (origem: unidade subprocesso, destino: unidade superior).
    - Criar ALERTA e enviar e‑mail para unidade superior.
    - Definir data_fim_etapa1 = now.
    - Apagar histórico de análise do cadastro (ANALISE_CADASTRO entries).
  - Tests: integration happy path and failure path (missing conhecimentos).
  - Prioridade: Alta. Esforço: Médio.
  - Dependências: CDU-08.

- CDU-10: Disponibilizar revisão do cadastro
  - Requisito: [`reqs/cdu-10.md`](reqs/cdu-10.md:1)
  - Endpoint:
    - POST /api/subprocessos/{id}/disponibilizar-revisao
  - Ações: similar a CDU-09 mas situacao -> 'Revisão do cadastro disponibilizada' and event types ANALISE_CADASTRO reset.
  - Tests: integração.
  - Prioridade: Média. Esforço: Médio.
  - Dependências: CDU-08, CDU-05.

- CDU-11: Visualizar cadastro
  - Requisito: [`reqs/cdu-11.md`](reqs/cdu-11.md:1)
  - Endpoint:
    - GET /api/subprocessos/{id}/cadastro
  - Response: CadastroDTO {atividades:List<ActivityDTO)}
  - Repos: AtividadeRepository, ConhecimentoRepository.
  - Tests: MockMvc render dados.
  - Prioridade: Alta. Esforço: Pequeno.
  - Dependências: CDU-08/09.

- CDU-12: Verificar impactos no mapa de competências
  - Requisito: [`reqs/cdu-12.md`](reqs/cdu-12.md:1)
  - Endpoint:
    - POST /api/subprocessos/{id}/impactos
  - Ações:
    - Comparar mapa vigente (UNIDADE_MAPA -> MAPA -> ATIVIDADE/CONHECIMENTO) com mapa em revisão/subprocesso.
    - Gerar ImpactReportDTO {atividadesInseridas, competenciasImpactadas:[{competenciaId, impactos:List<{atividade, tipo, detalhe}>}]}
  - Serviços: ImpactAnalysisService (deterministic compare, no persistence).
  - Tests: unit comparators e integração com datasets.
  - Prioridade: Média. Esforço: Médio.
  - Dependências: CDU-08, CDU-15/16.

- CDU-13: Analisar cadastro de atividades e conhecimentos
  - Requisito: [`reqs/cdu-13.md`](reqs/cdu-13.md:1)
  - Endpoints:
    - POST /api/subprocessos/{id}/analises-cadastro -> {resultado: 'ACEITE'|'DEVOLUCAO', observacao?:String}
  - Ações:
    - Registrar registro em ANALISE_CADASTRO com data/hora, unidade e resultado.
    - Registrar MOVIMENTACAO conforme regra.
    - Se devolução e destino for própria unidade, alterar situacao para 'Cadastro em andamento' e limpar data_fim_etapa1.
    - Enviar e‑mail e criar ALERTA.
  - Tests: integração para aceite e devolução.
  - Prioridade: Alta. Esforço: Médio.
  - Dependências: CDU-09.

- CDU-14: Analisar revisão de cadastro
  - Requisito: [`reqs/cdu-14.md`](reqs/cdu-14.md:1)
  - Endpoint:
    - POST /api/subprocessos/{id}/analises-revisao -> similar CDU-13
  - Ações: registrar ANALISE_CADASTRO, movimentacao, possivel homologacao (ADMIN path).
  - Tests: integração.
  - Prioridade: Média. Esforço: Médio.
  - Dependências: CDU-10, CDU-12.

- CDU-15: Manter mapa de competências
  - Requisito: [`reqs/cdu-15.md`](reqs/cdu-15.md:1)
  - Endpoints:
    - POST /api/subprocessos/{id}/mapa/competencias -> CompetenciaCreateDTO {descricao, atividadeIds[]}
    - PUT /api/competencias/{id}
    - DELETE /api/competencias/{id}
    - GET /api/subprocessos/{id}/mapa
  - DTOs: CompetenciaDTO {id, descricao, atividadeIds:List<Long>}
  - Entidades: COMPETENCIA, COMPETENCIA_ATIVIDADE (existentes).
  - Services: MapaService (criar/editar/remover competencia; manter integridade: associar todas atividades não associadas).
  - Tests: unit MapaService; integration edição/disponibilização.
  - Prioridade: Alta. Esforço: Alto.
  - Dependências: CDU-13, CDU-08.

- CDU-16: Ajustar mapa de competências
  - Requisito: [`reqs/cdu-16.md`](reqs/cdu-16.md:1)
  - Endpoint:
    - POST /api/subprocessos/{id}/mapa/impactos -> reuse /impactos and editing endpoints in CDU-15
  - Actions: permitir edição baseada em ImpactReport; garantir que atividades sem competência sejam associadas.
  - Tests: integration.
  - Prioridade: Média. Esforço: Médio.
  - Dependências: CDU-12, CDU-15.

- CDU-17: Disponibilizar mapa de competências
  - Requisito: [`reqs/cdu-17.md`](reqs/cdu-17.md:1)
  - Endpoint:
    - POST /api/subprocessos/{id}/mapa/disponibilizar {dataLimiteValidacao:Date, observacoes?:String}
  - Actions:
    - Validar associações (todas competências com >=1 atividade e todas atividades com >=1 competencia).
    - Alterar situacao para 'Mapa disponibilizado', set data_limite_etapa2, registrar MOVIMENTACAO (SEDOC->subprocesso), notificar via e‑mail e criar ALERTA, limpar sugestões e historico de analise do mapa.
  - Tests: integration happy/failure.
  - Prioridade: Alta. Esforço: Médio.
  - Dependências: CDU-15.

- CDU-18: Visualizar mapa de competências
  - Requisito: [`reqs/cdu-18.md`](reqs/cdu-18.md:1)
  - Endpoint:
    - GET /api/subprocessos/{id}/mapa/visualizar
  - Response: MapaVisualDTO {competencias:[{descricao, atividades:[{descricao, conhecimentos[]}]}]}
  - Tests: MockMvc.
  - Prioridade: Alta. Esforço: Pequeno.
  - Dependências: CDU-17.

- CDU-19: Validar mapa de competências
  - Requisito: [`reqs/cdu-19.md`](reqs/cdu-19.md:1)
  - Endpoint:
    - POST /api/subprocessos/{id}/mapa/validar {acao:'VALIDAR'|'SUGESTAO', observacoes?:String}
  - Actions:
    - Para 'SUGESTAO': salvar sugestoes no MAPA.sugestoes_apresentadas, alterar situacao para 'Mapa com sugestões', criar MOVIMENTACAO, notificar, criar ALERTA.
    - Para 'VALIDAR': alterar situacao para 'Mapa validado', criar MOVIMENTACAO, notificar, criar ALERTA.
    - Registrar data_fim_etapa2 = now e limpar historico de analise.
  - Tests: integration.
  - Prioridade: Alta. Esforço: Médio.
  - Dependências: CDU-17, CDU-18.

- CDU-20: Analisar validação de mapa
  - Requisito: [`reqs/cdu-20.md`](reqs/cdu-20.md:1)
  - Endpoint:
    - POST /api/subprocessos/{id}/analises-mapa {resultado:'ACEITE'|'DEVOLUCAO', observacao?:String}
  - Actions:
    - Registrar ANALISE_VALIDACAO, movimentacao, se devolução e destino igual unidade, set situacao -> 'Mapa disponibilizado' e limpar data_fim_etapa2.
    - Para aceite: se gestor registar, encaminhar para unidade superior; se ADMIN homologar -> 'Mapa homologado'.
    - Notificações e alertas conforme regras.
  - Tests: integration.
  - Prioridade: Alta. Esforço: Médio.
  - Dependências: CDU-17, CDU-19.

- CDU-21: Finalizar processo
  - Requisito: [`reqs/cdu-21.md`](reqs/cdu-21.md:1)
  - Endpoint:
    - POST /api/processos/{id}/finalizar
  - Actions:
    - Verificar todos subprocessos de unidades O/I estão 'Mapa homologado'; se não, retornar 400 com lista.
    - Definir MAPA como vigente em UNIDADE_MAPA para cada unidade (inserir/atualizar).
    - Alterar PROCESSO.situacao -> 'FINALIZADO' e set data_finalizacao.
    - Enviar e‑mails para todas unidades e criar ALERTAS.
  - Tests: integração full flow.
  - Prioridade: Alta. Esforço: Médio.
  - Dependências: CDU-17, CDU-20.

4. Plano de implementação passo-a-passo (sequência recomendada)

Sprint 0 (setup)
- A1. Configurar projeto backend: pacotes, DTOs, mappers, exception handling, security skeleton (auth client stub), email adapter, flyway baseline.
- A2. Criar repositórios para views (VW_USUARIO_PERFIL_UNIDADE) e modelos principais (PROCESSO, SUBPROCESSO).

Sprint 1 (autenticação e painel)
- B1. Implementar CDU-01 (AuthService, /api/auth/login)
- B2. Implementar CDU-02 (Painel endpoints, consulta alertas/processos)

Sprint 2 (processo CRUD + iniciar)
- C1. CDU-03: processos CRUD endpoints and validations
- C2. CDU-04: iniciar processo mapeamento (subprocessos, mapas vazios, movimentacoes, alertas, emails)

Sprint 3 (revisão + detalhes)
- D1. CDU-05: iniciar revisão (mapa copy service)
- D2. CDU-06: detalhar processo, CDU-07: detalhar subprocesso

Sprint 4 (atividades cadastros)
- E1. CDU-08: manter cadastro (atividades/conhecimentos + import)
- E2. CDU-09/10: disponibilizar cadastro e revisão

Sprint 5 (análises e mapas)
- F1. CDU-13/14: analisar cadastro/revisão
- F2. CDU-12/16: impactos + ajustes mapa

Sprint 6 (manter/disponibilizar mapas)
- G1. CDU-15/17: manter mapa e disponibilizar mapa
- G2. CDU-18/19/20: visualizar/validar/analisar validação do mapa

Sprint 7 (finalização & hardening)
- H1. CDU-21: finalizar processo
- H2. Auditoria, performance, testes de contrato e QA checklist

5. Plano de integração com frontend

- Base: endpoints REST JSON; autenticação via token (JWT) retornado por `/api/auth/login`.
- DTOs e exemplos (principais):
  - LoginRequest
    - { "titulo":"000000000001", "senha":"senha" }
  - LoginResponse
    - { "token":"eyJ...", "perfis":[{"perfil":"ADMIN","unidadeCodigo":1,"sigla":"SESEL"}], "unidades":[{"codigo":1,"sigla":"SESEL"}] }
  - CreateProcessRequest
    - { "descricao":"Mapeamento X", "tipo":"MAPEAMENTO", "dataLimiteEtapa1":"2025-10-15", "unidades":[1,2,3] }
  - ProcessDTO (exemplo)
    - { "codigo":12, "descricao":"Mapeamento X", "tipo":"MAPEAMENTO", "situacao":"CRIADO", "dataCriacao":"2025-10-02T10:00:00Z" }
  - ActivityCreateDTO
    - { "descricao":"Atividade A", "conhecimentos":[{"descricao":"Conhecimento 1"}] }

- Contratos: manter campos esperados pelo frontend (`sigla`, `descricao`, `situacao`) e formatos ISO8601 para datas.

6. Plano de testes e checklist de QA

- Testes unitários:
  - Services: validações, regras de negócio (AtividadeService, MapaService, ProcessoService).
- Testes de integração:
  - MockMvc + BD para endpoints críticos: login, criar/iniciar processo, disponibilizar cadastro, analisar, disponibilizar mapa, finalizar processo.
- Testes de contrato:
  - Contratos JSON para endpoints de painel e processos (usando Spring RestDocs ou Pact).
- QA checklist:
  - Validação de permissões por perfil.
  - Fluxos happy path e error path (ex: disponibilizar sem conhecimentos).
  - Verificar envios de e‑mail (SMTP sandbox).

7. Riscos e recomendações

- Migrações de schema: usar Flyway; scripts para popular UNIDADE_MAPA ao finalizar processo.
- Dados legacy: cuidado ao copiar mapas (preservar códigos primários; criar novos códigos no snapshot).
- E‑mails: usar provider com environment flags; enviar apenas em ambiente de produção.
- Concorrência: proteger operações de início e finalização de processo com locks otimistas/pessimistas.

8. Checklist final com milestones

- Milestone 1: Autenticação + Painel (CDU-01, CDU-02)
- Milestone 2: CRUD processo + iniciar mapeamento (CDU-03, CDU-04, CDU-05)
- Milestone 3: Cadastro de atividades + disponibilizar (CDU-08, CDU-09, CDU-10)
- Milestone 4: Análises e mapas (CDU-13..20)
- Milestone 5: Finalização e entrega (CDU-21)

9. Checklist inicial (tarefas executáveis)

- [ ] Criar pacote sgc.auth com controller /api/auth/login e AuthService.
- [ ] Implementar client HTTP para Sistema Acesso (mock + impl real).
- [ ] Criar DTOs: LoginRequest, LoginResponse, PerfilUnidadeDTO.
- [ ] Criar PainelController com endpoints /api/painel/processos e /api/painel/alertas.
- [ ] Criar ProcessoController com endpoints CRUD e iniciar/finalizar.
- [ ] Implementar ProcessoService com validações do CDU-03.
- [ ] Implementar startMappingProcess e startRevisionProcess (CDU-04/05).
- [ ] Criar Subprocesso, Mapa, Movimentacao e Alerta repositories and entities mapping to DB.
- [ ] Implementar AtividadeController e AtividadeService (CDU-08).
- [ ] Implementar import de atividades entre processos.
- [ ] Implementar endpoints de disponibilização (CDU-09/10).
- [ ] Implementar análise de cadastro e validação de mapas (CDU-13..14,19..20).
- [ ] Implementar MapaService (criação/edição/deleção de competências).
- [ ] Implementar ImpactAnalysisService (CDU-12).
- [ ] Implementar email adapter e AlertService (listeners de eventos).
- [ ] Escrever testes unitários para services críticos.
- [ ] Escrever testes de integração MockMvc para fluxos críticos.
- [ ] Escrever scripts Flyway para migrações necessárias.
- [ ] Documentar contratos API em README ou Spring RestDocs.

Observações sobre incertezas e recomendações para esclarecimentos

- Validações de "unidades com mapas vigentes" requerem definição de como considerar interoperacionais; recomendo confirmar regras com analista de domínio.
- Padrão de envio de e‑mail (templates) e URL do sistema devem ser parametrizados via `application.yml`.

Referências

- Requisitos: [`reqs/`](reqs:1)
- Situações: [`reqs/situacoes.md`](reqs/situacoes.md:1)
- Modelo de dados: [`reqs/modelo-dados.md`](reqs/modelo-dados.md:1)

Checklist final (milestones curtinhos)

- [ ] Sprint 0 completo (setup)
- [ ] Sprint 1 completo (CDU-01/02)
- [ ] Sprint 2 completo (CDU-03/04/05)
- [ ] Sprint 3 completo (CDU-06/07)
- [ ] Sprint 4 completo (CDU-08..10)
- [ ] Sprint 5 completo (CDU-12..14)
- [ ] Sprint 6 completo (CDU-15..20)
- [ ] Sprint 7 completo (CDU-21, audit)

Fim.