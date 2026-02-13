# E2E Test Investigation - Learnings and Fixes

Testes E2E (cdu-xx) falhando após refatorações no projeto. Esta investigação identificou problemas de isolamento, permissões de acesso e estabilidade de seletores.

## Descobertas Principais

### 1. Isolamento de Estado e describe.serial()

**Problema Identificado:**
`test.describe.serial()` não preserva o estado do banco de dados se as fixtures executarem `resetDatabase()` em cada `test()`.

**Solução:**
- Consolidar fluxos dependentes em um **único bloco `test()`** ou garantir que o reset de DB ocorra apenas uma vez por arquivo/trabalhador.
- A fixture `complete-fixtures.ts` foi aprimorada para evitar resets redundantes dentro do mesmo processo do worker.

### 2. Regras de Transição de Situação (Backend)

**Problema Identificado (CDU-10):**
O subprocesso de Revisão inicia em `NAO_INICIADO`. A ação `EDITAR_REVISAO_CADASTRO` exigia que a situação já fosse `REVISAO_CADASTRO_EM_ANDAMENTO`, impedindo que o titular da unidade iniciasse a revisão.

**Solução:**
Atualizar `SubprocessoAccessPolicy.java` para permitir `EDITAR_REVISAO_CADASTRO` também na situação `NAO_INICIADO`.

### 3. Violações de Strict Mode (Playwright)

**Problema Identificado:**
Locatores genéricos como `page.getByText(/Sucesso/i)` falham se existirem múltiplos elementos (ex: cabeçalho e corpo do Toast).

**Solução:**
- Usar `.first()` em mensagens de toast: `expect(page.getByText(/.../).first()).toBeVisible()`.
- Usar seletores mais específicos (TestIDs) sempre que possível.

### 4. Intercepção de Eventos por Elementos Fixos (Navbar)

**Problema Identificado:**
O botão "Voltar" ou botões no topo da página eram interceptados pela `navbar sticky-top`, impedindo o clique normal do Playwright.

**Solução:**
- Usar `{force: true}` no clique quando a intercepção for puramente visual/layout e não funcional.
- Alternativa: Chamar `scrollIntoViewIfNeeded()` ou usar locatários que garantam que o elemento não está sob a navbar.

### 5. Timeouts em Workflows Longos

**Problema Identificado:**
Workflows complexos (como o fluxo completo de revisão no CDU-10) podem exceder o timeout padrão de 30s-45s, mesmo que cada passo individual seja rápido.

**Solução:**
- Aumentar o timeout especificamente para o teste longo usando `test.setTimeout(60000)`.
- Manter o timeout padrão baixo para falhar rápido em testes unitários/curtos.

### 6. Strict Mode Violations em Tabelas

**Problema Identificado:**
Seletores como `getByRole('row', {name: 'SECAO_221'})` podem encontrar múltiplas linhas se houver múltiplos processos listados para a mesma unidade (ex: Mapeamento e Revisão).

**Solução:**
- Usar locators mais específicos ou filters: `page.getByRole('row', {name: UNIDADE_ALVO}).first()` (se a ordem garantir) ou filtrar pelo contexto do subprocesso.
- Encapsular a navegação em helpers robustos (`acessarSubprocessoAdmin`) que lidam com essas ambiguidades.

### 7. Estado de Login em Tests Serial

**Problema Identificado:**
Em suites `test.describe.serial`, o estado de autenticação pode não persistir ou ser o esperado entre os testes se não for explicitamente gerenciado, especialmente quando diferentes atores interagem.

**Solução:**
- Adicionar `await login(...)` explícito no início de cada bloco `test()` que requer um usuário específico, para garantir o contexto correto.

### 8. Mensagens de Sucesso (Toasts) e Strict Mode

**Problema Identificado:**
Ao validar mensagens de feedback (Toasts), é comum ocorrer violação de "strict mode" pois o texto pode existir também no corpo da página ou haver múltiplos toasts.

**Solução:**
- Utilizar `.first()` para focar no primeiro elemento encontrado, assumindo que é o mais relevante ou o único visível naquele contexto imediato.
- Exemplo: `await expect(page.getByText(/Sucesso/i).first()).toBeVisible();`

### 9. Navegação Direta vs Painel

**Aprendizado:**
Depender de encontrar o processo na tabela do painel pode ser frágil devido a paginação ou ordenação, especialmente quando múltiplos testes rodam e criam dados.

**Recomendação:**
- Capturar o ID do processo na criação (`extrairProcessoId`).
- Navegar diretamente via URL: `await page.goto('/processo/' + processoId);`.
- Isso garante que o teste acesse o recurso correto sem "adivinhar" sua posição na lista.

## Problemas Resolvidos

### 1. Início de Revisão Bloqueado
Corrigida a política de acesso que impedia o CHEFE de editar um cadastro de revisão ainda não iniciado.

### 2. Erros de Sintaxe na Infra de Teste
Corrigido erro no `e2e/lifecycle.js` que impedia a subida do servidor durante a filtragem de logs.

### 3. Estabilidade do CDU-10
Consolidação do workflow em passos lógicos e tratamento de fechamento de modais de histórico para evitar sobreposição de elementos.

## Comandos Úteis

### Executar Testes
```bash
# Teste único (arquivo completo)
npx playwright test e2e/cdu-XX.spec.ts --reporter=list

# Com saída capturada para análise profunda
npx playwright test e2e/cdu-XX.spec.ts --reporter=list > test_output.txt 2>&1
```
## Observações Finais

1. **Nunca aumentar timeouts sem motivo**: Se um elemento não aparece em 15s em um banco H2 local, o problema é de lógica ou permissão.
2. **Uso de steps**: Use `test.step()` para organizar logs dentro de um teste consolidado longo.
3. **Limpeza de Toasts**: Use o helper `limparNotificacoes(page)` antes de ações críticas de navegação se toasts estiverem bloqueando a tela.

### 14. Logout em fluxos longos pode falhar por estado de UI

**Problema Identificado:**
Em alguns fluxos (ex.: CDU-09/CDU-12), o botão de logout pode não estar disponível ou clicável no instante da troca de ator (overlay/toast/transição de rota), gerando timeout.

**Solução aplicada:**
- `fazerLogout` ficou resiliente: tenta logout pelo menu quando disponível e, se não houver redirecionamento, navega explicitamente para `/login`.

### 15. Suites seriais ainda exigem autenticação explícita por teste

**Problema Identificado:**
Mesmo com estado de banco preservado por arquivo, o contexto de autenticação pode não ser o esperado entre testes (novo contexto/página).

**Solução aplicada:**
- Adicionar fixture de autenticação no próprio teste (`autenticadoComoAdmin`) ou `login(...)` explícito no início de cada caso dependente.

### 16. Helpers de card devem garantir entrada no subprocesso

**Problema Identificado:**
Vários cenários falhavam por permanecer em `/processo/{codigo}` (detalhes do processo), onde os cards do subprocesso não existem.

**Solução aplicada:**
- Em `helpers-atividades.ts` e `helpers-mapas.ts`, o guard de contexto agora tenta selecionar a unidade pela primeira `row` com `cell` visível antes de validar o card.

### 17. Evitar `@Builder` em request record com desserialização Jackson

**Problema Identificado:**
Fluxos de devolução de revisão (`/devolver-revisao-cadastro`) geraram 500 com `NoClassDefFoundError` para `DevolverCadastroRequestBuilder`.

**Solução aplicada:**
- Removido `@Builder` de `DevolverCadastroRequest` (record request), mantendo desserialização direta do Jackson e eliminando a falha em runtime.

## Atualização Contínua - 2026-02-12

### 10. Perfil de GESTOR deve refletir a hierarquia do caso

**Problema Identificado:**
Alguns cenários de revisão/aceite usavam `GESTOR_COORD` (COORD_11), mas o subprocesso testado era da árvore `COORD_22` (`SECAO_221`), causando processo ausente no painel ou 403.

**Solução:**
- Usar `GESTOR_COORD_22` nos cenários que operam sobre `SECAO_221`.
- Revisar fixtures de autenticação por cenário para garantir aderência à unidade-alvo.

### 11. Navegação direta para subprocesso pode falhar por contexto de autorização

**Problema Identificado:**
Em alguns fluxos de CHEFE, abrir URL direta de subprocesso (`/processo/{codigo}/{sigla}`) resultou em erro de autorização/contexto (`403`) durante carregamento.

**Solução:**
- Preferir navegação de usuário real via painel (`clicar no processo`) quando o acesso direto não for estável.
- Manter captura de `codigo` para cenários onde a navegação direta é comprovadamente estável.

### 12. Bug de backend em busca de subprocesso por sigla

**Problema Identificado:**
Para CHEFE em alguns fluxos (CDU-11, CDU-14 revisão, CDU-17), a chamada `GET /api/subprocessos/buscar?codProcesso={codigo}&siglaUnidade=SECAO_221` retorna `404 ENTIDADE_NAO_ENCONTRADA` mesmo com participante criado e processo iniciado.

**Evidência:**
- Mensagem backend: `Subprocesso com codigo '[processo, 18]' não encontrado`.
- Impacto direto: card `card-subprocesso-atividades` não carrega e os passos de cadastro ficam bloqueados.

**Conclusão:**
Este ponto é bug do sistema (backend/consulta de subprocesso), não instabilidade de seletor.

### 13. Mudança de elegibilidade de unidades no cadastro de processo

**Problema Identificado:**
Em cenários de criação (ex.: CDU-25 e CDU-34), `SECAO_221` e `COORD_22` aparecem `disabled` no tree de unidades, deixando `Salvar/Iniciar` desabilitado.

**Solução aplicada nos testes:**
- Ajustar cenários para unidade elegível (`ASSESSORIA_22`) e ator compatível (`CHEFE_ASSESSORIA_22`) quando o objetivo do caso de teste não depende estritamente da `SECAO_221`.

### 14. Painel vazio/inconsistente para perfis com processos iniciados

**Problema Identificado:**
Mesmo após criação e início de processo com participante confirmado em log backend, alguns perfis (CHEFE/GESTOR) não enxergam o processo no painel (`Nenhum processo encontrado`).

**Evidência:**
- Backend registra criação/início com participante.
- UI do ator mostra painel vazio no passo seguinte.
- Impacta fluxos de preparação em CDU-11, CDU-17 e CDU-20.

### 15. Detalhes do processo ficam em carregamento infinito

**Problema Identificado:**
Em alguns cenários, a rota de detalhes (`/processo/{codigo}` ou `/processo/{codigo}/{sigla}`) permanece em `Carregando detalhes...` sem renderizar cards/tabela esperados.

**Conclusão:**
Quando isso ocorre com dados válidos e atores corretos, tratar como bug funcional do sistema e não como problema de timeout/seletor.

### 16. Bloqueio atual concentrado em visibilidade para GESTOR

**Status da rodada `cdu-20/cdu-25/cdu-34` (`test_output_fix7.txt`):**
- 13 passed
- 2 failed
- 4 did not run

**Falhas remanescentes:**
1. `CDU-20` cenário 1 (`GESTOR_COORD_22`) não encontra o processo no painel após mapa validado.
2. `CDU-25` cenário 1 (`GESTOR_COORD_22`) não consegue abrir o processo no painel (timeout no clique pelo texto).

**Leitura técnica:**
- Os ajustes de navegação (evitar `goto /processo/{codigo}` em etapas de preparo) reduziram falhas de carregamento infinito.
- O bloqueio restante ficou isolado no comportamento de listagem/visibilidade de processo para GESTOR, com indício forte de regra de negócio/hierarquia no backend.

### 17. Correções backend aplicadas para reduzir inconsistências de painel/subprocesso

**Correções implementadas:**
1. `PainelController` agora resolve `unidade` pelo usuário autenticado (unidade ativa/lotação) quando o parâmetro não é enviado para perfis não-ADMIN.
2. `SubprocessoCrudController /api/subprocessos/buscar` ganhou fallback para tentar unidades descendentes quando não existir subprocesso direto para a sigla consultada.

**Validação técnica:**
- Testes backend focados (`PainelControllerTest` e `SubprocessoCrudControllerTest`) passando após ajuste.

**Status funcional:**
- Ainda persistem falhas E2E de visibilidade no painel para GESTOR (`CDU-20` e `CDU-25`), indicando investigação adicional de regra de negócio/hierarquia (não apenas transporte de parâmetro).

### 18. Formulário de atribuição temporária com falha silenciosa

**Sintoma observado em teste manual:**
Ao clicar em "Criar atribuição" sem combinação válida de campos/carregamento, não havia feedback claro para o usuário.

**Correção aplicada:**
- `CadAtribuicao.vue` passou a exibir erro explícito (feedback visual) quando:
  - unidade não foi carregada,
  - usuário não foi selecionado,
  - data de término ou justificativa não foram preenchidas.

### 19. CDU-25 estabilizado por alinhamento hierárquico real do seed

**Problema Identificado:**
`CDU-25` falhava em sequência por combinação ator/unidade incoerente com a árvore e com os dados do `seed.sql`, gerando ausência de processo no painel do GESTOR.

**Correção aplicada:**
- `CDU-25` foi alinhado para fluxo completo em `SECAO_211` (árvore `COORD_21`).
- Atores ajustados para a hierarquia correta do caso:
  - CHEFE: `CHEFE_SECAO_211` (`101010`)
  - GESTOR: `GESTOR_COORD_21` (`999999`)
- Login explícito mantido em cada etapa para evitar estado implícito entre testes serial.

**Validação:**
- `npx playwright test e2e/cdu-25.spec.ts` → **6 passed**
- `npx playwright test e2e/cdu-20.spec.ts e2e/cdu-25.spec.ts e2e/cdu-34.spec.ts` → **19 passed**

### 20. Falhas remanescentes concentradas em busca de subprocesso

**Problema Identificado:**
Após nova rodada dos CDUs com falha, vários fluxos de CHEFE/ADMIN quebram na etapa inicial com:
- `GET /api/subprocessos/buscar?codProcesso={codigo}&siglaUnidade={sigla}`
- resposta `404 ENTIDADE_NAO_ENCONTRADA`
- mensagem: `Subprocesso com codigo '[{processo}, {unidade}]' não encontrado(a)`.

**Impacto observado:**
- Etapas de preparação falham por ausência dos cards de subprocesso (atividades/mapa), gerando efeito cascata nos cenários seguintes.
- Rodada atual dos arquivos críticos ficou em **8 falhas** (CDU-11/12/13/14/15/16/17/18), com padrão consistente.

**Leitura técnica:**
O problema dominante deixou de ser seletor/timeout e passou a ser funcional de backend na resolução processo+unidade para subprocesso em parte dos fluxos.

### 21. Isolamento de cleanup em suíte serial impacta falsos negativos

**Problema Identificado:**
Em fluxos `test.describe.serial`, o cleanup automático por teste removia processo entre etapas de preparação/cenário, causando sintomas semelhantes a bug de backend.

**Correção aplicada:**
- Fixture `complete-fixtures` foi ajustada para não executar limpeza destrutiva entre testes do mesmo arquivo serial.
- Fluxos críticos passaram a manter estado entre etapas como esperado pelo desenho dos CDUs.

**Efeito prático:**
- O erro 404 em `/api/subprocessos/buscar` deixou de ser padrão dominante.
- No trio `CDU-11/13/14`, o status evoluiu para **2 falhas remanescentes** focadas em navegação de detalhe/subprocesso (não mais em ausência de subprocesso por limpeza prematura).
