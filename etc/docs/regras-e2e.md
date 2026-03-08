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

## Fixtures e preparação de estado

- **Prefira fixtures de backend para preparar estados profundos**: Se o teste precisa chegar a `MAPA_DISPONIBILIZADO`, `MAPA_VALIDADO` ou outro estado tardio apenas para capturar uma tela, use endpoint E2E/fixure em vez de montar todo o workflow pela UI.
- **Fixtures devem reduzir custo estrutural, não esconder comportamento**: Use fixture para pular preparo repetitivo. A ação que está sendo validada no teste deve continuar sendo exercitada pela UI.
- **Fixtures devem retornar o código do processo**: Isso permite navegar direto para `/processo/{codigo}` ou `/processo/{codigo}/{sigla}` sem depender da listagem do painel.
- **Quando a listagem não é o alvo do teste, não dependa dela**: Em cenários preparados por fixture, navegar diretamente pela URL do subprocesso é mais estável e mais rápido do que localizar a linha da tabela.
- **Processo de revisão exige mapa vigente real da unidade**: Para fixtures de revisão/diagnóstico, não basta colocar o subprocesso em uma situação avançada. Se a regra de negócio exigir `mapa vigente`, a preparação precisa materializar esse estado da mesma forma que a aplicação faz em produção.
- **`MAPA_HOMOLOGADO` não equivale automaticamente a mapa vigente**: Em mapeamento, o mapa só passa a ser vigente de forma confiável após a finalização do processo anterior. Se a validação da revisão falhar com erro de `unidade sem mapa vigente`, investigue o estado de `unidade_mapa`, não timing.
- **Quando o estado alvo é terminal, prefira endpoint dedicado**: Se o objetivo do teste é validar uma ação final, como reabertura ou permissão liberada apenas após homologação final, crie uma fixture específica para esse estado em vez de encadear vários passos de UI num `serial`.
- **Toda nova fixture E2E deve nascer com teste de backend**: Ao adicionar endpoint de fixture, cubra-o em teste integrado do backend antes de confiar nele na suíte Playwright. Isso reduz depuração cruzada entre backend e E2E.

## Helpers Disponíveis

Os helpers estão organizados em arquivos especializados no diretório `e2e/helpers/`:

| Arquivo                 | Responsabilidade                                                                                                     |
|-------------------------|----------------------------------------------------------------------------------------------------------------------|
| `helpers-auth.ts`       | Login e credenciais (`login`, `loginComPerfil`, `autenticar`, `USUARIOS`)                                            |
| `helpers-navegacao.ts`  | Navegação entre páginas (`fazerLogout`, `limparNotificacoes`, `verificarPaginaPainel`, `navegarParaSubprocesso`)      |
| `helpers-processos.ts`  | Criar e verificar processos (`criarProcesso`, `extrairProcessoId`, `verificarProcessoNaTabela`, `verificarDetalhesProcesso`, `verificarDetalhesSubprocesso`) |
| `helpers-atividades.ts` | Atividades, conhecimentos e impactos (`adicionarAtividade`, `adicionarConhecimento`, `editarAtividade`, `removerAtividade`, `disponibilizarCadastro`, `abrirModalImpactoEdicao`, `abrirModalImpactoVisualizacao`, `verificarBotaoImpactoAusenteEdicao`, `verificarBotaoImpactoAusenteDireto`) |
| `helpers-mapas.ts`      | Competências e mapas (`navegarParaMapa`, `criarCompetencia`, `editarCompetencia`, `excluirCompetenciaConfirmando`, `disponibilizarMapa`) |
| `helpers-analise.ts`    | Análise de cadastro — navegação (`acessarSubprocessoGestor`, `acessarSubprocessoChefeDireto`, `acessarSubprocessoAdmin`), aceite (`aceitarCadastroMapeamento`, `aceitarRevisao`), devolução (`devolverCadastroMapeamento`, `devolverRevisao`, `cancelarDevolucao`), homologação (`homologarCadastroMapeamento`, `homologarCadastroRevisaoComImpacto`, `cancelarHomologacao`), histórico (`abrirHistoricoAnalise`, `abrirHistoricoAnaliseVisualizacao`, `fecharHistoricoAnalise`) |

**IMPORTANTE**: Sempre use os helpers centralizados ao invés de definir funções locais nos arquivos de teste.

### Estratégias de Espera

- ✅ USE `waitForResponse()` para operações de API
- ✅ USE `waitForURL()` para navegação (ou os helpers semânticos `esperarPagina...`)
- ✅ USE `waitFor()` para elementos do DOM
- ✅ USE `expect().toHaveURL()` para verificar navegação
- ❌ NUNCA use `waitForTimeout()` em testes funcionais (permitido apenas em `captura-telas.spec.ts` para animações)
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
- **Helpers de Navegação Semântica**: Evite repetir expressões regulares de URL nos testes. Use e mantenha os helpers em `helpers-navegacao.ts` (ex: `esperarPaginaDetalhesProcesso(page, id)`).
- **Prefira papel e escopo semântico em modais**:
  - Use `page.getByRole('dialog')` em vez de `.modal-content` quando possível.
  - Ao buscar botões de modal, faça a busca dentro do dialog.
- **Seletor por `data-testid` errado é defeito do teste, não de timing**:
  - Exemplo prático: `sel-login-perfil` é o seletor correto; `sel-perfil` não é.
  - Em formulários sem `data-testid`, use label real (`getByLabel(...)`) em vez de inventar um test id.

## Nuances de Autenticação

O comportamento do login varia conforme o tipo de unidade do usuário:

- **Unidades Operacionais/Intermediárias**: Geralmente possuem apenas um perfil e o sistema loga diretamente (Use `login`).
- **Unidades Interoperacionais**: Podem acumular perfis (ex: Chefe e Gestor), exigindo a escolha em um dropdown após a senha (Use `loginComPerfil`).
- **Troca de Usuário em Testes Seriais**: Ao usar `test.describe.serial()`, o estado do navegador é mantido entre os cenários. Se o teste N+1 precisa de um usuário diferente do teste N, chame o helper `login` ou `loginComPerfil` explicitamente no início do cenário, mesmo que já use uma fixture. Isso garante a navegação para `/login` e a troca efetiva da sessão.
- **Dica**: Se o teste falhar esperando pelo seletor de perfil, verifique se o usuário em questão realmente possui múltiplos perfis no `seed.sql`.

### Hierarquia de Unidades

Ao testar visibilidade de processos para Gestores ou Chefes, **consulte sempre o `e2e/setup/seed.sql`**. Se a unidade do processo não for subordinada à unidade do usuário testado, o sistema (corretamente) não exibirá o registro, causando falha no teste por "elemento não encontrado".

## Notificações: toast vs alert

- **Toast e alert não são equivalentes**:
  - `toast` deve ser validado só quando o fluxo realmente o produz de forma estável, tipicamente após mutação seguida de navegação.
  - `AppAlert` tende a ser o mecanismo correto para erros e avisos inline.
- **Se a aplicação redireciona para o painel após a ação, prefira validar a navegação/estado final** em vez de texto transitório.
- **Após refatorações de notificação, revise helpers compartilhados**:
  - Um helper que antes esperava sucesso em toast pode passar a estar errado para metade dos fluxos.
- **Ruído esperado deve ser reconhecido como tal**:
  - No teste de login inválido, `401` no backend e no network log é esperado.
  - Isso não deve ser tratado como falha funcional do cenário.

## Debugging de Testes E2E

### Investigar a causa raiz primeiro

Quando um teste falha por um elemento estar "obstruído" ou "não encontrado", **investigue por que** antes de adicionar workarounds. Exemplos:

- Se um toast está bloqueando um botão, verifique se o toast está configurado corretamente (auto-hide, posição). O problema pode ser no frontend, não no teste.
- Se um modal ainda está aberto, verifique se a ação anterior realmente completou. O problema pode ser que o teste não esperou a ação terminar.

### Datas e Localização

Sempre use `.toLocaleDateString('pt-BR')` ao comparar datas geradas dinamicamente nos testes (como `Date.now() + dias`) com o conteúdo da tela. O sistema utiliza o padrão brasileiro, e falhas de asserção ocorrem se o teste comparar formatos ISO ou americanos com o que está renderizado.

### Captura de IDs de Processos

**Evite capturar IDs de URLs de criação/edição** logo após salvar. O redirecionamento para o Painel pode ocorrer antes que a URL mude para o formato com ID, ou o ID pode não estar presente na URL de sucesso.
- ✅ PADRÃO SEGURO: Após criar o processo e voltar ao Painel, localize o registro pela descrição, clique para entrar nos detalhes e então use `await extrairProcessoId(page)` na página de detalhes.

### Ler os logs do Playwright com atenção

O Playwright fornece logs detalhados no "Call log" de cada erro. Eles mostram o elemento exato, atributos e o motivo da falha. Leia esses logs **antes** de tentar corrigir. A resposta geralmente está lá.

### Diferenciar falha real de ruído

- Um teste pode passar e ainda gerar logs de browser warning/error.
- Antes de “limpar os warnings”, classifique:
  - erro esperado do cenário negativo;
  - warning de framework disparado por erro já tratado;
  - defeito real novo.
- Não silencie logs reais só para deixar a saída bonita.

### Ler os `error-context.md` gerados

Testes que falham geram um snapshot do DOM em `error-context.md`. Esse snapshot mostra a **estrutura real** do DOM no momento da falha — use-o para verificar se seus seletores CSS estão corretos.

### `force: true` vs `dispatchEvent`

- `click({force: true})` — Pula as verificações de "actionability" mas ainda dispara o evento nas coordenadas do elemento. **Não garante** que o handler do Vue será acionado se o elemento clicado (ex: `<li>`) não é o mesmo que recebe o evento (ex: `<a>` dentro do `<li>`).
- `dispatchEvent('click')` — Dispara o evento DOM diretamente no elemento, ignorando completamente sobreposições visuais. **Garante** que o handler será acionado. Usar para casos como o `fazerLogout`, onde um toast pode sobrepor o botão.

### Rodar os testes localmente

Não tente adivinhar o problema — rode o teste e leia a saída:

```bash
npx playwright test e2e/cdu-XX.spec.ts > resultado.txt 2>&1
```

## Verificar as pré-condições de estado antes de testar uma ação

Se um teste espera que um botão esteja visível/habilitado mas ele não está, **verifique se o subprocesso está na situação correta para habilitar aquela ação**. O backend determina as permissões com base no estado do subprocesso (ex: `podeReabrirCadastro` requer `>= MAPA_HOMOLOGADO`). 

**Ação:** Leia a lógica de permissões em `SubprocessoService.java` (método `construirPermissoes`) e confira se o fluxo no teste atinge a situação mínima exigida.

### Regras práticas observadas nesta sessão

- Se o cenário preparou apenas cadastro disponibilizado/homologado, botões de ação em bloco de mapa devem estar **ocultos**, não “talvez visíveis”.
- `btnReabrirCadastro` só deve ser esperado quando a situação já passou de homologação do mapa.
- Em testes de relatórios, valide o botão de exportação dentro do dialog correto.
- Em testes de gestão de unidades, expanda toda a hierarquia necessária antes de esperar a unidade filha.
- Se o fluxo de revisão precisa apenas chegar ao ponto de `podeReabrirRevisao`, a preparação ideal é fixture em `REVISAO_MAPA_HOMOLOGADO`, não workflow completo por UI.
- Quando um teste serial estoura o timeout e o erro final aparece em `page.goto('/login')`, suspeite primeiro de orçamento gasto no preparo anterior, e não de problema no login.

## Testes que revelam bugs no frontend

Nem sempre a falha é do teste — o teste pode estar correto e revelando um **bug real do frontend**. Exemplos encontrados:

- **`isProcessoFinalizado` excessivamente restritivo**: `SubprocessoView.vue` e `SubprocessoCards.vue` tratavam `MAPA_HOMOLOGADO` como "processo finalizado", escondendo botões.
- **Busca flat em árvore hierárquica**: `MapaVisualizacaoView.vue` usava `.find()` flat, falhando em unidades aninhadas.

**Ação:** Quando um teste falha em um fluxo que deveria funcionar, investigue também o código do frontend — não apenas o teste.

## Sempre assertar mensagens de sucesso após ações mutantes

Se um passo de preparação faz uma ação (validar mapa, aceitar, homologar), **adicione uma assertiva para a mensagem de sucesso** (ex: `await expect(page.getByText(/Mapa validado/i).first()).toBeVisible()`). Sem isso, erros passam despercebidos — o teste avança mas a ação não foi efetivada no backend.

**Atualização importante**: se a mensagem não é estável após refatoração de notificações, substitua essa assertiva por uma validação de:
- navegação correta;
- estado do subprocesso;
- botão/permissão esperado na tela seguinte.

## Logs do backend nos resultados dos testes

Os logs do backend aparecem na saída dos testes (prefixados com `[WebServer] [BACKEND]`). Use `grep` para verificar se as chamadas de backend esperadas realmente aconteceram:

```

## Lições aprendidas em correções recentes

- **Evite locators com prefixo de tag desnecessário**:
    - ❌ `page.locator('table[data-testid="tbl-processos"]')` falha se o `data-testid` for movido para uma `div` de wrapper (comum em componentes de terceiros como `BTable`).
    - ✅ USE `page.locator('[data-testid="tbl-processos"]')` para ser agnóstico à tag.
- **H2: Cuidado com `TRUNCATE` e `REFERENTIAL_INTEGRITY FALSE`**:
    - Em bancos H2, o comando `TRUNCATE TABLE` pode corromper `CHECK` constraints (especialmente em visualizações simuladas como tabelas no schema de teste) se a integridade referencial estiver desabilitada no momento.
    - Se encontrar `JdbcSQLIntegrityConstraintViolationException: Check constraint invalid` após um reset de banco, substitua o `TRUNCATE` por `DELETE FROM`.
- **Navegação pós-criação**:
    - Após criar um registro (`page.getByTestId('btn-...-salvar').click()`), certifique-se de que o teste aguarda a volta para a página esperada (`/painel`) antes de tentar interagir com a tabela. O uso de `await expect(page).toHaveURL(...)` ou `page.waitForURL(...)` é essencial.
- **Robustez em Clicks de Linha**:
    - Antes de clicar em uma linha da tabela (`tabela.locator('tr').filter(...).click()`), prefira adicionar um `await linha.waitFor({ state: 'visible' })`. Em ambientes de teste, a linha pode estar presente no DOM mas ainda não "clicável" devido a re-renderizações do Vue/Bootstrap.
