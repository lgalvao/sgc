# Auditoria de consistência de idioma (inglês vs português)

Gerado em: 2026-05-20T19:55:14.720Z Base: /home/runner/work/sgc/sgc

## Indicadores

- Membros com nome inglês: 240
- Campos com `id`/`*Id`: 0
- Parâmetros com `id`/`*Id`: 2
- **Score total (menor = melhor):** 242

## Distribuição por prefixo inglês

| Prefixo/Tipo | Ocorrências |
|--------------|-------------|
| set          | 84          |
| get          | 46          |
| is           | 39          |
| create       | 30          |
| handle       | 16          |
| parse        | 10          |
| reset        | 4           |
| toggle       | 3           |
| update       | 3           |
| has          | 2           |
| delete       | 1           |
| build        | 1           |
| add          | 1           |

## Top 20 arquivos com mais membros em inglês

- **backend/src/main/java/sgc/subprocesso/service/SubprocessoConsultaService.java** (6): isMesmaUnidadeAlvo,
  isUnidadeAlvoNaHierarquiaUsuario, isChefe, isGestor, isAdmin, isGestorOuAdmin
- **frontend/src/axios-setup.ts** (6): setRouter, isMonitoramentoSolicitadoPorSessao, isMonitoramentoSolicitadoPorUrl,
  isMonitoramentoAtivo, isErroCanceladoHttp, isRequisicaoPermitidaDuranteTransicao
- **backend/src/main/java/sgc/processo/service/ProcessoService.java** (5): isSituacaoCadastroDisponibilizado,
  isSituacaoMapaAceitavel, isSituacaoMapaHomologavel, isUnidadeAdmin, isSituacaoCadastro
- **toolkit/projeto/arvore-linhas.js** (5): getFiles, buildTree, getTreeConnectors, parseArgs, isTestFile
- **backend/src/main/java/sgc/e2e/E2eController.java** (4): resetDatabase, getSeedResource, setSituacaoProcesso,
  setSituacaoSubprocesso
- **backend/src/main/java/sgc/organizacao/model/Usuario.java** (4): getUnidadeCodigo, getAuthorities, getPassword,
  getUsername
- **backend/src/main/java/sgc/subprocesso/model/Subprocesso.java** (4): getAtividades, setSituacao, setSituacaoForcada,
  isEmAndamento
- **backend/src/main/java/sgc/subprocesso/service/SubprocessoAcessoService.java** (4): isSituacaoMapeamentoAPartirDe,
  isSituacaoRevisaoAPartirDe, isFluxoMapeamento, isFluxoRevisao
- **backend/src/test/java/sgc/comum/util/FiltroMonitoramentoHttpTest.java** (4): getAttribute, getAttributeNames,
  getSessionId, getSessionMutex
- **frontend/src/components/unidade/useArvoreSelecao.ts** (4): isChecked, isHabilitado, getEstadoSelecao,
  updateAncestors
- **frontend/src/composables/useCacheSync.ts** (4): handleAtualizacaoCache, handleErro, handlePageHide, handlePageShow
- **frontend/src/utils/statusHelpers.ts** (4): getProcessoBadgeVariant, getSubprocessoBadgeVariant,
  getNotificacaoStatusInfo, getNotificacaoBadgeVariant
- **frontend/src/views/ __tests__/CadProcesso.spec.ts** (4): createWrapper, createWrapperCobertura, setProcessando,
  setErro
- **backend/src/main/java/sgc/comum/erros/ErroNegocio.java** (3): getCode, getStatus, getMessage
- **backend/src/main/java/sgc/organizacao/service/HierarquiaService.java** (3): isResponsavel, isSubordinada,
  isSuperiorImediata
- **toolkit/backend/java-corrigir-fqn.js** (3): parseImports, getInsertPosition, parseArgs
- **frontend/src/views/CadastroView.vue** (3): handleImportAtividades, setAtividadeRef, handleAdicionarAtividade
- **backend/src/main/java/sgc/alerta/model/Alerta.java** (2): getOrigemSiglaSintetica, getMensagemSintetica
- **backend/src/main/java/sgc/comum/util/MonitoramentoProperties.java** (2): isMonitoramentoHttpAtivo,
  isMonitoramentoJavaLentoAtivo
- **backend/src/main/java/sgc/processo/model/Processo.java** (2): getCodigosParticipantes, getSiglasParticipantes

## Campos com `id`/`*Id` (deveriam usar `codigo`)

- Nenhum encontrado ✅
