# Plano de Alinhamento e Execução

Este plano tem como objetivo abordar os pontos levantados no documento `alignment.md`, focando na correção de bugs, validações faltantes e padronização da API.

## Passo 1: Correção de Validação (CDU-09/10)
- [x] **Modificar `SubprocessoCadastroController` ou `SubprocessoWorkflowService`**
    - [x] Adicionar validação no método `disponibilizarCadastro` (e `disponibilizarRevisao`) para impedir a ação caso a lista de atividades esteja vazia.
    - [x] Atualmente, valida apenas "atividade sem conhecimento". Deve validar também `atividades.isEmpty()`.
- [x] **Criar/Atualizar Testes**
    - [x] Verificar `SubprocessoWorkflowServiceTest` (ou similar) e adicionar caso de teste para tentativa de disponibilizar com 0 atividades.

## Passo 2: Padronização da API e Remoção de Redundância (CDU-15/17)
- [x] **Remover Endpoint Redundante**
    - [x] Remover `SubprocessoMapaController.disponibilizarMapa` (o método que mapeia para `POST /{codigo}/disponibilizar`).
    - [x] Verificar se há uso no frontend deste endpoint e redirecionar para `SubprocessoValidacaoController.disponibilizarMapa` (`POST /{codigo}/disponibilizar-mapa`).
- [x] **Padronizar Verbos HTTP**
    - [x] Em `SubprocessoMapaController`, os métodos `atualizarCompetencia` (PUT) e `removerCompetencia` (DELETE) devem ser removidos ou marcados como deprecated, priorizando `POST .../atualizar` e `POST .../remover`.
    - [x] Confirmar se o frontend está usando as versões POST. Se estiver usando PUT/DELETE, atualizar o frontend primeiro ou manter compatibilidade temporária (mas o plano é executar, então vamos tentar migrar).

## Passo 3: Melhoria de UX no Painel (CDU-02)
- [x] **Destaque Visual em Alertas**
    - [x] Modificar `frontend/src/components/painel/TabelaAlertas.vue`.
    - [x] Adicionar classe CSS ou estilo para deixar em negrito (bold) as linhas de alertas não lidos (`alerta.lido === false`). (Já estava implementado).

## Passo 4: Registro de Sugestões Tratadas (CDU-20)
- [x] **Adicionar Flag de Tratamento**
    - [x] No backend, identificar onde as sugestões são armazenadas (provavelmente na entidade `Mapa` ou `Subprocesso` ou uma entidade `Sugestao`).
    - [x] Adicionar campo `sugestaoTratada` (boolean) ou similar.
    - [x] Adicionar endpoint/lógica para marcar como tratada (pode ser implícito ao "Submeter Mapa Ajustado" ou explícito).
    - [x] *Análise prévia necessária*: Verificar onde `sugestoes` (texto) é salvo. Se for apenas um campo texto no Mapa, talvez precise de uma estrutura melhor ou apenas um flag `sugestoesTratadas` no `Mapa` ou `Subprocesso`.

## Passo 5: Snapshot Hierárquico (CDU-03/04/05)
- [x] **Investigação e Implementação Mínima**
    - [x] O problema é que o processo liga-se a `Unidade` e `Subprocesso` liga-se a `Unidade`. Se a `Unidade` mudar de nome ou hierarquia, o histórico muda.
    - [x] Criar uma entidade `SubprocessoUnidadeSnapshot` ou adicionar campos no `Subprocesso` para armazenar `nomeUnidade`, `siglaUnidade`, `hierarquiaUnidade` (string representativa) no momento da criação.
    - [x] Preencher esses dados em `ProcessoService.iniciarProcesso...`.
    - [x] Alterar DTOs de visualização para usar esses dados "congelados" em vez da entidade `Unidade` atual, se o processo estiver em andamento/finalizado.

## Passo 6: Verificação de Testes
- [x] Executar testes afetados após cada mudança.
- [x] Garantir que nenhum teste existente quebre (exceto se estiver testando comportamento errado).

---
**Observações:**
- A autenticação/login será ignorada conforme instrução.
- O foco é corrigir a lógica de negócio e conformidade com requisitos.
