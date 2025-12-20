# Planejamento Frontend - Sprint 02: Expansão e Limpeza Lógica

## Objetivo
Expandir o padrão BFF para a visualização de processos, eliminar lógica complexa de travessia de árvore no cliente e padronizar o tratamento de erros em todas as telas principais.

## Tarefas de Execução

### 1. [Backend] Criar Endpoint BFF para Visualização de Processo
**Contexto:** `ProcessoView.vue` realiza múltiplas chamadas para montar o estado do processo, estatísticas e unidades.
**Ação:** Implementar endpoint `GET /api/processos/{id}/contexto-completo`.
**Detalhes Técnicos:**
- **Response:** `ProcessoContextoDto` com dados do processo, resumo de unidades participantes e permissões calculadas.
- **Service:** Adicionar método em `ProcessoService` ou criar facade específica.
**Arquivo(s):**
- `backend/src/main/java/sgc/processo/ProcessoControle.java`
- `backend/src/main/java/sgc/processo/dtos/ProcessoContextoDto.java`
**Definição de Pronto (DoD):**
- Endpoint funcional retornando dados agregados.

### 2. [Frontend] Refatorar `ProcessoView.vue` (BFF)
**Contexto:** Simplificar carregamento da tela de detalhes do processo.
**Ação:**
- Consumir o novo endpoint `contexto-completo`.
- Remover chamadas individuais sequenciais.
**Arquivo(s):**
- `frontend/src/views/ProcessoView.vue`
**Definição de Pronto (DoD):**
- Tela carrega em uma única requisição.

### 3. [Frontend] Eliminar `useSubprocessoResolver`
**Contexto:** O frontend atualmente navega recursivamente na árvore de unidades para achar um subprocesso, o que é ineficiente e acoplado.
**Ação:**
- Identificar usos de `useSubprocessoResolver`.
- Substituir por chamada direta ao endpoint existente: `GET /api/subprocessos?processo={id}&unidade={sigla}` (Store: `subprocessosStore.buscarSubprocessoPorProcessoEUnidade`).
- Excluir o arquivo do composable.
**Arquivo(s):**
- `frontend/src/composables/useSubprocessoResolver.ts` (DELETAR)
- Views que o utilizam (ex: `CadMapa.vue`, etc).
**Definição de Pronto (DoD):**
- Funcionalidade de encontrar subprocesso mantém-se inalterada.
- Código de travessia de árvore removido do cliente.

### 4. [Frontend] Expandir `useFormErrors`
**Contexto:** Aplicar o padrão de tratamento de erros criado na Sprint 1 nas demais telas.
**Ação:** Refatorar views para usar o composable `useFormErrors`.
**Views Alvo:**
- `frontend/src/views/CadProcesso.vue`
- `frontend/src/views/UnidadeView.vue`
**Definição de Pronto (DoD):**
- Remoção de lógica duplicada de `if (field === 'x') ...`.
- Validação visual continua funcionando.

## Verificação Final da Sprint
- [ ] `npm run typecheck` passa.
- [ ] Navegação na árvore de processos e subprocessos funciona sem erros.
- [ ] Validação de formulários consistente em todas as telas refatoradas.
