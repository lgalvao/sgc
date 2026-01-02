# Alinhamento de Implementação: CDU-22 a CDU-36

Este documento apresenta a análise de implementação dos casos de uso (CDU) 22 a 36, verificando se:
1. Todos os requisitos estão implementados completamente pelo frontend e backend
2. Existe teste de integração backend que cobre o caso de uso
3. O teste e2e correspondente fornece cobertura abrangente

## Resumo Executivo

| CDU | Nome | Backend | Frontend | Teste Integração | Teste E2E | Status |
|-----|------|---------|----------|------------------|-----------|--------|
| 22 | Aceitar cadastros em bloco | ✅ | ✅ | ✅ | ✅ | **COMPLETO** |
| 23 | Homologar cadastros em bloco | ✅ | ✅ | ✅ | ✅ | **COMPLETO** |
| 24 | Disponibilizar mapas em bloco | ✅ | ✅ | ✅ | ✅ | **COMPLETO** |
| 25 | Aceitar validação de mapas em bloco | ✅ | ✅ | ✅ | ✅ | **COMPLETO** |
| 26 | Homologar validação de mapas em bloco | ✅ | ✅ | ✅ | ✅ | **COMPLETO** |
| 27 | Alterar data limite | ✅ | ✅ | ✅ | ✅ | **COMPLETO** |
| 28 | Manter atribuição temporária | ✅ | ✅ | ✅ | ✅ | **COMPLETO** |
| 29 | Consultar histórico de processos | ✅ | ✅ | ✅ | ✅ | **COMPLETO** |
| 30 | Manter Administradores | ⚠️ | ⚠️ | ❌ | ✅ | **PARCIAL** |
| 31 | Configurar sistema | ✅ | ✅ | ✅ | ✅ | **COMPLETO** |
| 32 | Reabrir cadastro | ✅ | ✅ | ❌ | ✅ | **PARCIAL** |
| 33 | Reabrir revisão de cadastro | ✅ | ✅ | ❌ | ✅ | **PARCIAL** |
| 34 | Enviar lembrete de prazo | ✅ | ✅ | ❌ | ✅ | **PARCIAL** |
| 35 | Gerar relatório de andamento | ✅ | ✅ | ❌ | ✅ | **PARCIAL** |
| 36 | Gerar relatório de mapas | ✅ | ✅ | ❌ | ✅ | **PARCIAL** |

**Legenda:**
- ✅ = Implementado/Presente
- ⚠️ = Parcialmente implementado
- ❌ = Ausente

---

## Análise Detalhada por CDU

### CDU-22: Aceitar cadastros em bloco

**Ator:** GESTOR

**Status:** ✅ **COMPLETO**

#### Implementação Backend
- ✅ Controller: `SubprocessoCadastroController.aceitarCadastroEmBloco()`
- ✅ Service: `SubprocessoCadastroWorkflowService.aceitarCadastroEmBloco()`
- ✅ Endpoint: `POST /api/subprocessos/{codigo}/cadastro/aceitar-em-bloco`

#### Implementação Frontend
- ✅ Service: `subprocessoService.aceitarCadastroEmBloco()`
- ✅ View: Integrado em `ProcessoView.vue`
- ✅ Modal de confirmação com seleção de unidades

#### Testes
- ✅ Teste de integração backend: `CDU22IntegrationTest.java`
- ✅ Teste E2E: `cdu-22.spec.ts`

#### Requisitos Implementados
- ✅ Botão "Aceitar cadastro em bloco" exibido quando há unidades elegíveis
- ✅ Modal com lista de unidades subordinadas (checkboxes selecionados por padrão)
- ✅ Registro de análise de cadastro para cada unidade
- ✅ Registro de movimentação
- ✅ Criação de alertas
- ✅ Envio de notificação por e-mail
- ✅ Mensagem de confirmação e redirecionamento

---

### CDU-23: Homologar cadastros em bloco

**Ator:** ADMIN

**Status:** ✅ **COMPLETO**

#### Implementação Backend
- ✅ Controller: `SubprocessoCadastroController.homologarCadastroEmBloco()`
- ✅ Service: `SubprocessoCadastroWorkflowService.homologarCadastroEmBloco()`
- ✅ Endpoint: `POST /api/subprocessos/{codigo}/cadastro/homologar-em-bloco`

#### Implementação Frontend
- ✅ Service: `subprocessoService.homologarCadastroEmBloco()`
- ✅ View: Integrado em `ProcessoView.vue`
- ✅ Modal de confirmação com seleção de unidades

#### Testes
- ✅ Teste de integração backend: `CDU23IntegrationTest.java`
- ✅ Teste E2E: `cdu-23.spec.ts`

#### Requisitos Implementados
- ✅ Botão "Homologar cadastro em bloco" exibido quando há unidades elegíveis
- ✅ Modal com lista de unidades subordinadas
- ✅ Registro de movimentação
- ✅ Alteração da situação para 'Cadastro homologado'
- ✅ Criação de alertas
- ✅ Envio de notificação por e-mail
- ✅ Mensagem de confirmação

---

### CDU-24: Disponibilizar mapas de competências em bloco

**Ator:** ADMIN

**Status:** ✅ **COMPLETO**

#### Implementação Backend
- ✅ Controller: `SubprocessoMapaController.disponibilizarMapaEmBloco()`
- ✅ Service: `SubprocessoMapaWorkflowService.disponibilizarMapaEmBloco()`
- ✅ Endpoint: `POST /api/subprocessos/{codigo}/mapa/disponibilizar-em-bloco`

#### Implementação Frontend
- ✅ Service: `subprocessoService.disponibilizarMapaEmBloco()`
- ✅ View: Integrado em `ProcessoView.vue`
- ✅ Modal com campo de data limite

#### Testes
- ✅ Teste de integração backend: `CDU24IntegrationTest.java`
- ✅ Teste E2E: `cdu-24.spec.ts`

#### Requisitos Implementados
- ✅ Botão "Disponibilizar mapas de competência em bloco"
- ✅ Modal com lista de unidades e campo de data limite
- ✅ Validação de associação competências ↔ atividades
- ✅ Registro de disponibilização
- ✅ Alteração da situação para 'Mapa disponibilizado'
- ✅ Registro de movimentação
- ✅ Notificações por e-mail (unidade e superiores)
- ✅ Criação de alertas
- ✅ Exclusão de sugestões e histórico de análise

---

### CDU-25: Aceitar validação de mapas de competências em bloco

**Ator:** GESTOR

**Status:** ✅ **COMPLETO**

#### Implementação Backend
- ✅ Controller: `SubprocessoValidacaoController.aceitarValidacaoEmBloco()`
- ✅ Service: `SubprocessoValidacaoWorkflowService.aceitarValidacaoEmBloco()`
- ✅ Endpoint: `POST /api/subprocessos/{codigo}/validacao/aceitar-em-bloco`

#### Implementação Frontend
- ✅ Service: `subprocessoService.aceitarValidacaoEmBloco()`
- ✅ View: Integrado em `ProcessoView.vue`
- ✅ Modal de confirmação

#### Testes
- ✅ Teste de integração backend: `CDU25IntegrationTest.java`
- ✅ Teste E2E: `cdu-25.spec.ts`

#### Requisitos Implementados
- ✅ Botão "Aceitar mapa de competências em bloco"
- ✅ Modal com lista de unidades subordinadas
- ✅ Registro de análise de validação
- ✅ Registro de movimentação
- ✅ Criação de alertas
- ✅ Envio de notificação por e-mail
- ✅ Mensagem de confirmação

---

### CDU-26: Homologar validação de mapas de competências em bloco

**Ator:** ADMIN

**Status:** ✅ **COMPLETO**

#### Implementação Backend
- ✅ Controller: `SubprocessoValidacaoController.homologarValidacaoEmBloco()`
- ✅ Service: `SubprocessoValidacaoWorkflowService.homologarValidacaoEmBloco()`
- ✅ Endpoint: `POST /api/subprocessos/{codigo}/validacao/homologar-em-bloco`

#### Implementação Frontend
- ✅ Service: `subprocessoService.homologarValidacaoEmBloco()`
- ✅ View: Integrado em `ProcessoView.vue`
- ✅ Modal de confirmação

#### Testes
- ✅ Teste de integração backend: `CDU26IntegrationTest.java`
- ✅ Teste E2E: `cdu-26.spec.ts`

#### Requisitos Implementados
- ✅ Botão "Homologar mapa de competências em bloco"
- ✅ Modal com lista de unidades subordinadas
- ✅ Registro de movimentação
- ✅ Alteração da situação para 'Mapa homologado'
- ✅ Criação de alertas
- ✅ Envio de notificação por e-mail
- ✅ Mensagem de confirmação e redirecionamento

---

### CDU-27: Alterar data limite de subprocesso

**Ator:** ADMIN

**Status:** ✅ **COMPLETO**

#### Implementação Backend
- ✅ Controller: `SubprocessoCrudController.alterarDataLimite()`
- ✅ Service: `SubprocessoService.alterarDataLimite()`
- ✅ Endpoint: `POST /api/subprocessos/{codigo}/data-limite`

#### Implementação Frontend
- ✅ Service: Integrado em `subprocessoService`
- ✅ View: Integrado em `SubprocessoView.vue`
- ✅ Modal "Alterar data limite" com campo de data

#### Testes
- ✅ Teste de integração backend: `CDU27IntegrationTest.java`
- ✅ Teste E2E: `cdu-27.spec.ts`

#### Requisitos Implementados
- ✅ Botão "Alterar data limite" na tela de detalhes do subprocesso
- ✅ Modal com campo de data preenchido com a data atual
- ✅ Atualização da data limite
- ✅ Envio de notificação por e-mail
- ✅ Criação de alerta
- ✅ Mensagem de confirmação

---

### CDU-28: Manter atribuição temporária

**Ator:** ADMIN

**Status:** ✅ **COMPLETO**

#### Implementação Backend
- ✅ Controller: `UnidadeController.criarAtribuicaoTemporaria()`
- ✅ Service: `UnidadeService.criarAtribuicaoTemporaria()`
- ✅ Endpoint: `POST /api/unidades/{codUnidade}/atribuicoes-temporarias`
- ✅ Model: `AtribuicaoTemporaria`, `AtribuicaoTemporariaRepo`

#### Implementação Frontend
- ✅ Service: `atribuicaoTemporariaService.criarAtribuicaoTemporaria()`
- ✅ View: Integrado em view de unidade
- ✅ Modal com campos: servidor, datas, justificativa

#### Testes
- ✅ Teste de integração backend: `CDU28IntegrationTest.java`
- ✅ Teste E2E: `cdu-28.spec.ts`

#### Requisitos Implementados
- ✅ Botão "Criar atribuição" na página de detalhes da unidade
- ✅ Modal com dropdown pesquisável de servidores
- ✅ Campos obrigatórios: servidor, data início, data término, justificativa
- ✅ Registro da atribuição temporária
- ✅ Envio de notificação por e-mail
- ✅ Criação de alerta
- ✅ Prioridade sobre dados de titularidade do SGRH

---

### CDU-29: Consultar histórico de processos

**Ator:** ADMIN/GESTOR/CHEFE

**Status:** ✅ **COMPLETO**

#### Implementação Backend
- ✅ Controller: `ProcessoController.buscarProcessosFinalizados()`
- ✅ Service: `ProcessoService` com listagem de processos finalizados
- ✅ Endpoint: `GET /api/processos/finalizados`

#### Implementação Frontend
- ✅ Service: `processoService.buscarProcessosFinalizados()`
- ✅ View: `HistoricoView.vue`
- ✅ Tabela com processos finalizados
- ✅ Navegação para detalhes do processo (somente leitura)

#### Testes
- ✅ Teste de integração backend: `CDU29IntegrationTest.java`
- ✅ Teste E2E: `cdu-29.spec.ts`

#### Requisitos Implementados
- ✅ Opção "Histórico" na barra de navegação
- ✅ Tabela com processos finalizados
- ✅ Colunas: Processo, Tipo, Finalizado em, Unidades participantes
- ✅ Navegação para detalhes do processo
- ✅ Tela de detalhes sem permitir mudanças ou botões de ação

---

### CDU-30: Manter Administradores

**Ator:** ADMIN

**Status:** ⚠️ **PARCIAL**

#### Implementação Backend
- ⚠️ Model: `Administrador`, `AdministradorRepo`, `AdministradorDto` existem
- ❌ **FALTA**: Controller específico para CRUD de administradores
- ❌ **FALTA**: Endpoints REST para adicionar/remover administradores
- ⚠️ Lógica de autorização existe no `UsuarioService`

#### Implementação Frontend
- ❌ **FALTA**: Service específico para administradores
- ❌ **FALTA**: Componente/view para gerenciar administradores
- ⚠️ A view `ConfiguracoesView.vue` existe mas não tem seção de administradores

#### Testes
- ❌ **FALTA**: Teste de integração backend
- ✅ Teste E2E: `cdu-30.spec.ts` existe mas aguarda implementação

#### Requisitos Faltantes
- ❌ Endpoint `GET /api/administradores` para listar
- ❌ Endpoint `POST /api/administradores` para adicionar
- ❌ Endpoint `DELETE /api/administradores/{codigo}` para remover
- ❌ Frontend: Seção de administradores na tela de configurações
- ❌ Frontend: Modal para adicionar administrador com busca de usuário
- ❌ Frontend: Confirmação para remover administrador
- ❌ Validação: Administrador não pode remover a si mesmo

**Ação Necessária:**
1. Criar `AdministradorController` com endpoints CRUD
2. Implementar service layer para gerenciar administradores
3. Adicionar seção de administradores em `ConfiguracoesView.vue`
4. Criar teste de integração `CDU30IntegrationTest.java`

---

### CDU-31: Configurar sistema

**Ator:** ADMIN

**Status:** ✅ **COMPLETO**

#### Implementação Backend
- ✅ Controller: `ConfiguracaoController`
- ✅ Service: `ParametroService`
- ✅ Endpoints: `GET /api/configuracoes`, `POST /api/configuracoes`
- ✅ Model: `Parametro`

#### Implementação Frontend
- ✅ Store: `configuracoes.ts`
- ✅ View: `ConfiguracoesView.vue`
- ✅ Campos editáveis para DIAS_INATIVACAO_PROCESSO e DIAS_ALERTA_NOVO

#### Testes
- ✅ Teste de integração backend: `CDU31IntegrationTest.java`
- ✅ Teste E2E: `cdu-31.spec.ts`

#### Requisitos Implementados
- ✅ Botão de configurações (engrenagem) na barra de navegação
- ✅ Tela de configurações com valores atuais
- ✅ Campos editáveis para as configurações
- ✅ Botão "Salvar"
- ✅ Mensagem de confirmação
- ✅ Efeito imediato das configurações

---

### CDU-32: Reabrir cadastro

**Ator:** ADMIN

**Status:** ⚠️ **PARCIAL**

#### Implementação Backend
- ✅ Service: `SubprocessoService.reabrirCadastro()`
- ⚠️ **FALTA**: Endpoint REST exposto via controller
- ✅ Lógica de negócio implementada

#### Implementação Frontend
- ✅ Service: `processoService.reabrirCadastro()`
- ✅ Component: `SubprocessoHeader.vue` com evento de reabertura
- ✅ Modal com campo de justificativa

#### Testes
- ❌ **FALTA**: Teste de integração backend
- ✅ Teste E2E: `cdu-32.spec.ts`

#### Requisitos Faltantes
- ❌ Endpoint `POST /api/subprocessos/{codigo}/reabrir-cadastro` no controller
- ❌ Teste de integração `CDU32IntegrationTest.java`

#### Requisitos Implementados
- ✅ Lógica de reabertura no service
- ✅ Alteração da situação para MAPEAMENTO_CADASTRO_EM_ANDAMENTO
- ✅ Registro de movimentação
- ✅ Envio de notificações
- ✅ Criação de alertas

**Ação Necessária:**
1. Adicionar endpoint em `SubprocessoCrudController` ou controller apropriado
2. Criar teste de integração `CDU32IntegrationTest.java`

---

### CDU-33: Reabrir revisão de cadastro

**Ator:** ADMIN

**Status:** ⚠️ **PARCIAL**

#### Implementação Backend
- ✅ Service: `SubprocessoService.reabrirRevisaoCadastro()`
- ⚠️ **FALTA**: Endpoint REST exposto via controller
- ✅ Lógica de negócio implementada

#### Implementação Frontend
- ✅ Service: `processoService.reabrirRevisaoCadastro()`
- ✅ Component: `SubprocessoHeader.vue` com evento de reabertura
- ✅ Modal com campo de justificativa

#### Testes
- ❌ **FALTA**: Teste de integração backend
- ✅ Teste E2E: `cdu-33.spec.ts`

#### Requisitos Faltantes
- ❌ Endpoint `POST /api/subprocessos/{codigo}/reabrir-revisao-cadastro` no controller
- ❌ Teste de integração `CDU33IntegrationTest.java`

#### Requisitos Implementados
- ✅ Lógica de reabertura no service
- ✅ Alteração da situação para REVISAO_CADASTRO_EM_ANDAMENTO
- ✅ Registro de movimentação
- ✅ Envio de notificações
- ✅ Criação de alertas

**Ação Necessária:**
1. Adicionar endpoint em `SubprocessoCrudController` ou controller apropriado
2. Criar teste de integração `CDU33IntegrationTest.java`

---

### CDU-34: Enviar lembrete de prazo

**Ator:** Sistema/ADMIN

**Status:** ⚠️ **PARCIAL**

#### Implementação Backend
- ✅ Controller: `ProcessoController.enviarLembrete()`
- ✅ Service: `ProcessoService.enviarLembrete()`
- ✅ Endpoint: `POST /api/processos/{codigo}/enviar-lembrete`

#### Implementação Frontend
- ✅ Service: `processoService.enviarLembrete()`
- ✅ Integração na interface

#### Testes
- ❌ **FALTA**: Teste de integração backend
- ✅ Teste E2E: `cdu-34.spec.ts`

#### Requisitos Faltantes
- ❌ Teste de integração `CDU34IntegrationTest.java`

#### Requisitos Implementados
- ✅ Opção "Enviar Lembrete" no sistema
- ✅ Envio de e-mail para responsáveis
- ✅ Registro de movimentação
- ✅ Criação de alerta
- ✅ Mensagem de confirmação

**Ação Necessária:**
1. Criar teste de integração `CDU34IntegrationTest.java`

---

### CDU-35: Gerar relatório de andamento

**Ator:** ADMIN

**Status:** ⚠️ **PARCIAL**

#### Implementação Backend
- ✅ Controller: `RelatorioController.gerarRelatorioAndamento()`
- ✅ Service: `RelatorioService.gerarRelatorioAndamento()`
- ✅ Endpoint: `GET /api/relatorios/andamento/{codProcesso}`
- ✅ Geração de PDF

#### Implementação Frontend
- ✅ View: `RelatoriosView.vue`
- ✅ Seleção de processo
- ✅ Botão para gerar PDF

#### Testes
- ❌ **FALTA**: Teste de integração backend
- ✅ Teste E2E: `cdu-35.spec.ts`

#### Requisitos Faltantes
- ❌ Teste de integração `CDU35IntegrationTest.java`

#### Requisitos Implementados
- ✅ Opção "Andamento de processo" em Relatórios
- ✅ Seleção de processo
- ✅ Relatório com colunas especificadas
- ✅ Exportação para PDF
- ✅ Download do arquivo

**Ação Necessária:**
1. Criar teste de integração `CDU35IntegrationTest.java`

---

### CDU-36: Gerar relatório de mapas

**Ator:** ADMIN

**Status:** ⚠️ **PARCIAL**

#### Implementação Backend
- ✅ Controller: `RelatorioController.gerarRelatorioMapas()`
- ✅ Service: `RelatorioService.gerarRelatorioMapas()`
- ✅ Endpoint: `GET /api/relatorios/mapas/{codProcesso}?codUnidade=`
- ✅ Geração de PDF

#### Implementação Frontend
- ✅ View: `RelatoriosView.vue`
- ✅ Seleção de processo (obrigatório)
- ✅ Seleção de unidade (opcional)
- ✅ Botão para gerar PDF

#### Testes
- ❌ **FALTA**: Teste de integração backend
- ✅ Teste E2E: `cdu-36.spec.ts`

#### Requisitos Faltantes
- ❌ Teste de integração `CDU36IntegrationTest.java`

#### Requisitos Implementados
- ✅ Opção "Mapas" em Relatórios
- ✅ Filtros: Processo (obrigatório) e Unidade (opcional)
- ✅ Relatório PDF com estrutura especificada
- ✅ Para cada mapa: Unidade, Competências, Atividades, Conhecimentos
- ✅ Download do arquivo

**Ação Necessária:**
1. Criar teste de integração `CDU36IntegrationTest.java`

---

## Resumo de Pendências

### Críticas (Impedem uso completo da funcionalidade)

**CDU-30: Manter Administradores**
- ❌ Criar controller REST com endpoints CRUD
- ❌ Implementar service layer completo
- ❌ Adicionar UI no frontend (seção em ConfiguracoesView)
- ❌ Criar teste de integração

### Importantes (Funcionalidade implementada, falta apenas teste)

**CDU-32: Reabrir cadastro**
- ❌ Expor endpoint REST no controller
- ❌ Criar teste de integração

**CDU-33: Reabrir revisão de cadastro**
- ❌ Expor endpoint REST no controller
- ❌ Criar teste de integração

**CDU-34: Enviar lembrete de prazo**
- ❌ Criar teste de integração

**CDU-35: Gerar relatório de andamento**
- ❌ Criar teste de integração

**CDU-36: Gerar relatório de mapas**
- ❌ Criar teste de integração

---

## Conclusão

**Casos de uso com implementação completa:** 9/15 (60%)
- CDU-22, CDU-23, CDU-24, CDU-25, CDU-26, CDU-27, CDU-28, CDU-29, CDU-31

**Casos de uso com implementação parcial:** 6/15 (40%)
- CDU-30 (falta backend controller e frontend UI)
- CDU-32, CDU-33 (falta expor endpoint)
- CDU-34, CDU-35, CDU-36 (falta apenas teste de integração)

**Todos os casos de uso possuem testes E2E implementados.**

A maioria dos casos de uso está completamente implementada. As pendências principais são:
1. **CDU-30** requer implementação completa do CRUD de administradores
2. **CDU-32 e CDU-33** precisam apenas que os endpoints sejam expostos nos controllers
3. **CDU-34, CDU-35 e CDU-36** estão funcionalmente completos, faltando apenas testes de integração backend

Todos os casos de uso possuem cobertura de testes E2E, o que garante validação end-to-end das funcionalidades implementadas.
