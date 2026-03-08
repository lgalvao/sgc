# Regras para corrigir problemas em testes end to end (e2e)

Se um testes end to end falhar, geralmente será por uma dessas causas:

- As expectativas do teste estão erradas.
- Dados nao estão presentes no banco de dados.
- Elementos esperados não estão sendo mostrados porque alguma validação falhou no backend.
- A funcionalidade ainda não foi implementada completamente ou corretamente.
- Um elemento transiente (toast, modal) está sobrepondo o alvo do click. Neste caso, investigue se o componente frontend está configurado corretamente (auto-hide, posição).

Nunca será porque um elemento não teve tempo de carregar ou renderizar. Então **aumentar o timeout NAO RESOLVERÁ NADA**! Esse sistema está rodando localmente, com um banco H2 em memória. Tudo é rápido. Se um elemento nao aparece, é por alguns dos motivos indicados acima.

Os testes e2e estão sendo usados para confirmar a implementação das funcionalidades do sistema. Portanto, se um teste falhar, isso será um sinal de que devemos investigar as causas indicadas acima -- e corrigir o problema usando com base a saída dos testes.

Ao rodar os testes e2e, tanto o frontend como o backend serão construídos e executados, e os logs de ambos serão mostrados durante os testes. Então nao se preocupe em rodar o backend ou frontend separadamente.

Os testes que falharem geram arquivos `error-context.md`, com a situacao da tela no momento da falha -- nao deixe de ler esses arquivos.

## Regras para execução de testes E2E

- **NUNCA rode apenas um cenário isolado**: Muitos testes usam `test.describe.serial()`, o que significa que os cenários dependem da execução sequencial dos anteriores. Rodar um cenário isolado causará falhas.
- **Sempre redirecione a saída para um arquivo**: Use `> resultado.txt 2>&1` ao rodar testes E2E para capturar toda a saída (stdout e stderr) em um arquivo de texto.
- **Use grep para analisar resultados**: Após redirecionar para arquivo, use `grep` para filtrar e analisar partes específicas da saída, como erros, logs do backend, ou mensagens específicas.
- **Não misture variáveis ao depurar**: Se o objetivo é corrigir semântica de um arquivo legado ou `serial`, rode com `--workers=1` primeiro. Valide o paralelismo só depois que o arquivo estiver verde isoladamente.
- **Paralelismo atual comprovado**: A arquitetura E2E já suporta frontend único + backend por worker. Até o momento, o nível validado de forma estável é **2 workers**. Não assuma que `4` ou `8` estão prontos sem prova.

## Paralelismo e infraestrutura

- **Frontend não precisa subir por worker**: O modelo viável é um frontend único compartilhado e um backend isolado por worker.
- **Isolamento por worker precisa ser explícito**: O backend, banco em memória e portas devem ser separados por índice de worker. Se o roteamento do worker estiver errado, a interferência aparece como falha funcional aleatória.
- **Banco por worker não implica múltiplos `seed.sql`**: O `seed.sql` é único. O que muda é a instância do banco em memória por worker, não o conteúdo do seed.
- **Não tente provar escalabilidade por intuição**: Se `2 workers` funcionam, isso sugere que `4` podem funcionar, mas não prova. Gargalos de bootstrap, portas, arquivos temporários e serviços compartilhados só aparecem quando testados.

## Fixtures e preparação de estado (State-Jumping)

- **Prefira fixtures de backend para preparar estados profundos**: Se o teste precisa chegar a `MAPA_DISPONIBILIZADO`, `MAPA_VALIDADO` ou outro estado tardio apenas para capturar uma tela, use endpoint E2E/fixure (via `E2eController`) em vez de montar todo o workflow pela UI. Isso é o conceito de **State-Jumping**.
- **Fixtures devem reduzir custo estrutural, não esconder comportamento**: Use fixture para pular preparo repetitivo. A ação que está sendo validada no teste deve continuar sendo exercitada pela UI.
- **Fixtures devem retornar o código do processo**: Isso permite navegar direto para `/processo/{codigo}` ou `/processo/{codigo}/{sigla}` sem depender da listagem do painel.
- **Quando a listagem não é o alvo do teste, não dependa dela**: Em cenários preparados por fixture, navegar diretamente pela URL do subprocesso é mais estável e mais rápido do que localizar a linha da tabela.
- **Processo de revisão exige mapa vigente real da unidade**: Para fixtures de revisão/diagnóstico, não basta colocar o subprocesso em uma situação avançada. Se a regra de negócio exigir `mapa vigente`, a preparação precisa materializar esse estado (processo anterior finalizado) da mesma forma que a aplicação faz em produção.
- **`MAPA_HOMOLOGADO` não equivale automaticamente a mapa vigente**: Em mapeamento, o mapa só passa a ser vigente de forma confiável após a finalização do processo anterior. Se a validação da revisão falhar com erro de `unidade sem mapa vigente`, investigue o estado de `unidade_mapa`, não timing.
- **Quando o estado alvo é terminal, prefira endpoint dedicado**: Se o objetivo do teste é validar uma ação final, como reabertura ou permissão liberada apenas após homologação final, crie uma fixture específica para esse estado em vez de encadear vários passos de UI num `serial`.
- **Toda nova fixture E2E deve nascer com teste de backend**: Ao adicionar endpoint de fixture, cubra-o em teste integrado do backend antes de confiar nele na suíte Playwright. Isso reduz depuração cruzada entre backend e E2E.

## Helpers Disponíveis

Os helpers estão organizados em arquivos especializados no diretório `e2e/helpers/`:

| Arquivo                 | Responsabilidade                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
|-------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `helpers-auth.ts`       | Login e credenciais (`login`, `loginComPerfil`, `autenticar`, `USUARIOS`)                                                                                                                                                                                                                                                                                                                                                                                                        |
| `helpers-navegacao.ts`  | Navegação entre páginas (`fazerLogout`, `limparNotificacoes`, `verificarPaginaPainel`, `navegarParaSubprocesso`)                                                                                                                                                                                                                                                                                                                                                                 |
| `helpers-processos.ts`  | Criar e verificar processos (`criarProcesso`, `extrairProcessoId`, `verificarProcessoNaTabela`, `verificarDetalhesProcesso`, `verificarDetalhesSubprocesso`)                                                                                                                                                                                                                                                                                                                     |
| `helpers-atividades.ts` | Atividades, conhecimentos e impactos (`adicionarAtividade`, `adicionarConhecimento`, `editarAtividade`, `removerAtividade`, `disponibilizarCadastro`, `abrirModalImpactoEdicao`, `abrirModalImpactoVisualizacao`, `verificarBotaoImpactoAusenteEdicao`, `verificarBotaoImpactoAusenteDireto`)                                                                                                                                                                                    |
| `helpers-mapas.ts`      | Competências e mapas (`navegarParaMapa`, `criarCompetencia`, `editarCompetencia`, `excluirCompetenciaConfirmando`, `disponibilizarMapa`)                                                                                                                                                                                                                                                                                                                                         |
| `helpers-analise.ts`    | Análise de cadastro — navegação (`acessarSubprocessoGestor`, `acessarSubprocessoChefeDireto`, `acessarSubprocessoAdmin`), aceite (`aceitarCadastroMapeamento`, `aceitarRevisao`), devolução (`devolverCadastroMapeamento`, `devolverRevisao`, `cancelarDevolucao`), homologação (`homologarCadastroMapeamento`, `homologarCadastroRevisaoComImpacto`, `cancelarHomologacao`), histórico (`abrirHistoricoAnalise`, `abrirHistoricoAnaliseVisualizacao`, `fecharHistoricoAnalise`) |

**IMPORTANTE**: Sempre use os helpers centralizados ao invés de definir funções locais nos arquivos de teste.

### Estratégias de Espera

- ✅ USE `waitForResponse()` para operações de API
- ✅ USE `waitForURL()` para navegação (ou os helpers semânticos `esperarPagina...`)
- ✅ USE `waitFor()` para elementos do DOM
- ✅ USE `expect().toHaveURL()` para verificar navegação
- ❌ NUNCA use `waitForTimeout()` em testes funcionais (permitido apenas em `captura.spec.ts` para animações)
- ❌ NÃO resolva tempo de execução alto com timeout maior. Se um teste isolado estoura `20s`, o normal é quebrá-lo em testes menores ou introduzir fixture de preparo.

## Antipadrões que devem ser evitados

- **Nunca use `if (await locator.isVisible().catch(() => false))`**:
  - Isso engole erro real.
  - Isso transforma falha em silêncio.
  - Isso costuma produzir teste que “passa” sem validar nada.
- **Nunca use assertiva genérica com `ou` para mascarar estado indefinido**:
  - Se dois estados são possíveis, descubra por que.
  - Se ambos forem realmente válidos, modele isso explicitamente no cenário, não com uma asserção vaga.
- **Não use helpers compartilhados para assertar mensagens genéricas**:
  - Exemplo ruim: helper sempre esperar “mensagem de sucesso”.
  - O helper deve validar só o que o fluxo garante em todos os usos.
- **Não mantenha texto legado em asserções**:
  - Depois de refatorações de notificação, frases como “Cadastro aceito” ou “Mapa disponibilizado” podem deixar de ser estáveis mesmo com a funcionalidade correta.

## Robustez de Seletores e Ambiguidade

Ao testar elementos que contêm texto, especialmente em tabelas ou listas de mensagens:

- **Conheça o Componente antes de interagir**: Não assuma padrões de interação genéricos. 
    - Unidades e processos na árvore usam `TreeTable.vue` e exigem clique no botão de expansão (`btn-toggle-expand-...`).
    - Atividades e Conhecimentos usam cards (`AtividadeItem.vue`) que já exibem os dados aninhados. **Nao tente clicar em botões de expansão que não existem**.
- **Seletores Escopados (Scoped & Semantic)**: Evite `page.getByText('SIGLA')` global se o texto aparecer em múltiplos lugares (breadcrumbs, menus, títulos).
  - ✅ USE: `page.locator('.unidade-sigla').getByText(UNIDADE_ALVO)`
  - ✅ USE: `page.locator('.header-context').getByText('Texto')`
- **Evite Ambiguidade com Modais**: O Playwright falhará em modo estrito se `getByText('Rótulo')` encontrar tanto um botão quanto o título de um modal aberto. 
    - ✅ USE `getByRole('button', { name: 'Rótulo' })` para garantir que está interagindo com o botão e não com o título do modal (`heading`).
- **Cuidado com Substrings**: Se uma mensagem ("Início do processo") é parte de outra ("Início do processo em unidade subordinada"), use filtros combinados para diferenciar:
  ```typescript
  // Seleciona a linha que tem o texto A, mas NÃO tem a palavra B
  await expect(page.locator('tr', {hasText: 'Início do processo'})
      .filter({hasNotText: 'subordinada'})
  ).toBeVisible();
  ```
- **Atenção a Textos Ocultos (Accessibility)**: Alguns componentes injetam texto para leitores de tela. Prefira regex ou filtros de conteúdo em vez de matchers exatos (`exact: true`) em células complexas.
- **Use `data-testid` oficial**: Consulte o componente Vue para usar o ID correto (ex: `inp-editar-atividade`, não `inp-editar-atividade-descricao`).

## Resiliência em Componentes Dinâmicos (Hover/Inline)

Componentes como `InlineEditor` (usados em Atividades) dependem de estados transientes que podem ser instáveis em CI:

- **Botoes que aparecem no Hover**: Use `click({ force: true })` para interagir com botões de edição/remoção que só aparecem visualmente via CSS `:hover`. O Playwright pode falhar ao tentar simular o mouse em ambiente headless.
- **Transição de Estado de Edição**: Ao clicar para editar, a estrutura do DOM do card pode mudar. Busque o input resultante **globalmente na página** (`page.getByTestId('...')`) em vez de restringir a busca dentro do locator do card que disparou o evento.
- **Aguarde a Visibilidade**: Sempre use `await input.waitFor({ state: 'visible' })` após o clique de edição antes de tentar preencher (`fill`).

## Nuances de Autenticação e Jornada do Ator

O comportamento do login varia conforme o tipo de unidade do usuário:

- **Siga o Fluxo do Requisito Rigorosamente**: Se o CDU diz "O usuário clica no Painel", faça o teste clicar no Painel, mesmo que `page.goto()` seja mais rápido. Isso valida permissões e SPAs.
- **Unidades Operacionais/Intermediárias**: Geralmente possuem apenas um perfil e o sistema loga diretamente (Use `login`).
- **Unidades Interoperacionais**: Podem acumular perfis (ex: Chefe e Gestor), exigindo a escolha em um dropdown após a senha (Use `loginComPerfil`).
- **Troca de Usuário em Testes Seriais**: Ao usar `test.describe.serial()`, o estado do navegador é mantido entre os cenários. Se o teste N+1 precisa de um usuário diferente do teste N, chame o helper `login` ou `loginComPerfil` explicitamente no início do cenário, mesmo que já use uma fixture.
- **Dica**: Se o teste falhar esperando pelo seletor de perfil, verifique se o usuário em questão realmente possui múltiplos perfis no `seed.sql`.

### Hierarquia de Unidades

Ao testar visibilidade de processos para Gestores ou Chefes, **consulte sempre o `e2e/setup/seed.sql`**. Se a unidade do processo não for subordinada à unidade do usuário testado, o sistema (corretamente) não exibirá o registro, causando falha no teste por "elemento não encontrado".

## Notificações: toast vs alert

- **Toast e alert não são equivalentes**:
  - `toast` deve ser validado só quando o fluxo realmente o produz de forma estável, tipicamente após mutação seguida de navegação.
  - `AppAlert` tende a ser o mecanismo correto para erros e avisos inline.
- **Se a aplicação redireciona para o painel após a ação, prefira validar a navegação/estado final** em vez de texto transitório.

## Debugging de Testes E2E

### Investigar a causa raiz primeiro

Quando um teste falha por um elemento estar "obstruído" ou "não encontrado", **investigue por que** antes de adicionar workarounds. Exemplos:

- Se um toast está bloqueando um botão, verifique se o toast está configurado corretamente (auto-hide, posição). O problema pode ser no frontend, não no teste.
- Se um modal ainda está aberto, verifique se a ação anterior realmente completou. O problema pode ser que o teste não esperou a ação terminar.

### Datas e Localização

Sempre use `.toLocaleDateString('pt-BR')` ao comparar datas geradas dinamicamente nos testes (como `Date.now() + dias`) com o conteúdo da tela. O sistema utiliza o padrão brasileiro.

### Captura de IDs de Processos

**Evite capturar IDs de URLs de criação/edição** logo após salvar. O redirecionamento para o Painel pode ocorrer antes que a URL mude para o formato com ID.
- ✅ PADRÃO SEGURO: Após criar o processo e voltar ao Painel, localize o registro pela descrição, clique para entrar nos detalhes e então use `await extrairProcessoId(page)` na página de detalhes.

### `force: true` vs `dispatchEvent`

- `click({force: true})` — Pula as verificações de "actionability" mas ainda dispara o evento nas coordenadas do elemento. Útil para elementos ocultos por hover.
- `dispatchEvent('click')` — Dispara o evento DOM diretamente no elemento, ignorando completamente sobreposições visuais. Usar para casos como o `fazerLogout`, onde um toast pode sobrepor o botão.

### Verificar as pré-condições de estado antes de testar uma ação

Se um teste espera que um botão esteja visível/habilitado mas ele não está, **verifique se o subprocesso está na situação correta**. O backend determina as permissões com base no estado do subprocesso (ex: `podeReabrirCadastro` requer `>= MAPA_HOMOLOGADO`). 

**Ação:** Leia a lógica de permissões em `SubprocessoService.java` (método `construirPermissoes`).

## Testes que revelam bugs no frontend (Histórico)

Nem sempre a falha é do teste — o teste pode estar correto e revelando um **bug real do frontend**. Exemplos encontrados:

- **`isProcessoFinalizado` excessivamente restritivo**: `SubprocessoView.vue` tratava `MAPA_HOMOLOGADO` como "processo finalizado", escondendo botões.
- **Busca flat em árvore hierárquica**: `MapaVisualizacaoView.vue` usava `.find()` flat, falhando em unidades aninhadas.

## Lições aprendidas em correções recentes

- **Evite locators com prefixo de tag desnecessário**:
    - ❌ `page.locator('table[data-testid="tbl-processos"]')` falha se o `data-testid` for movido para uma `div`.
    - ✅ USE `page.locator('[data-testid="tbl-processos"]')` para ser agnóstico à tag.
- **H2: Cuidado com `TRUNCATE` e `REFERENTIAL_INTEGRITY FALSE`**:
    - Se encontrar `JdbcSQLIntegrityConstraintViolationException` após um reset, substitua o `TRUNCATE` por `DELETE FROM`.
- **Navegação pós-criação**: Use `await expect(page).toHaveURL(...)` ou `page.waitForURL(...)` após o `click` de salvar.
