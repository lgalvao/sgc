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
- ✅ USE `waitForURL()` para navegação
- ✅ USE `waitFor()` para elementos do DOM
- ✅ USE `expect().toHaveURL()` para verificar navegação
- ❌ NUNCA use `waitForTimeout()` em testes funcionais (permitido apenas em `captura-telas.spec.ts` para animações)

## Princípios para Helpers

Helpers devem ser **lineares e assertivos**. Se algo inesperado acontece, o teste deve falhar imediatamente.

- ❌ NUNCA use `.catch(() => false)` ou `try/catch` para engolir erros silenciosamente — isso esconde bugs. Exceção: `.catch(() => {})` no click de toasts que podem auto-fechar entre `isVisible` e `click` é aceitável, pois o toast desaparecer é o resultado desejado.
- ❌ NUNCA use fallbacks com múltiplas estratégias (ex: "se não encontrar X, tenta Y, depois Z") — isso torna os testes não-determinísticos.
- ❌ NUNCA use `if (await element.isVisible())` para decidir qual caminho seguir **quando o teste deveria saber qual caminho está testando**. Exceção: quando a UI legítimamente mostra variantes (ex: botão empty-state vs botão normal em `abrirModalCriarCompetencia`, ou card de edição vs visualização em `navegarParaMapa`), usar `.or()` ou `isVisible()` é aceitável.
- ✅ Use funções separadas para contextos diferentes (ex: `abrirModalImpactoEdicao` vs `abrirModalImpactoVisualizacao`).
- ✅ Se um parâmetro é necessário para navegação determinística, torne-o obrigatório (ex: `siglaUnidade`).

## Debugging de Testes E2E

### Investigar a causa raiz primeiro

Quando um teste falha por um elemento estar "obstruído" ou "não encontrado", **investigue por que** antes de adicionar workarounds. Exemplos:

- Se um toast está bloqueando um botão, verifique se o toast está configurado corretamente (auto-hide, posição). O problema pode ser no frontend, não no teste.
- Se um modal ainda está aberto, verifique se a ação anterior realmente completou. O problema pode ser que o teste não esperou a ação terminar.

### Ler os logs do Playwright com atenção

O Playwright fornece logs detalhados no "Call log" de cada erro. Eles mostram:

- O **elemento exato** que está interceptando (`<button class="btn-close">` from `<div class="orchestrator-container">`)
- Os **atributos do elemento** (ex: `value="3000"` revelou que o prop name estava errado)
- O **número de retries** e o motivo de cada falha

Leia esses logs **antes** de tentar corrigir. A resposta geralmente está lá.

### Ler os `error-context.md` gerados

Testes que falham geram um snapshot do DOM em `error-context.md`. Esse snapshot mostra a **estrutura real** do DOM no momento da falha — use-o para verificar se seus seletores CSS estão corretos.

### `force: true` vs `dispatchEvent`

- `click({force: true})` — Pula as verificações de "actionability" (visibilidade, sobreposição) mas ainda dispara o evento nas coordenadas do elemento. **Não garante** que o handler do Vue será acionado se o elemento clicado (ex: `<li>`) não é o mesmo que recebe o evento (ex: `<a>` dentro do `<li>`).
- `dispatchEvent('click')` — Dispara o evento DOM diretamente no elemento, ignorando completamente sobreposições visuais. **Garante** que o handler será acionado. Usar para casos como o `fazerLogout`, onde um toast pode sobrepor o botão.

### Rodar os testes localmente

Não tente adivinhar o problema — rode o teste e leia a saída:

```bash
npx playwright test e2e/cdu-XX.spec.ts > resultado.txt 2>&1
```

## Verificar as pré-condições de estado antes de testar uma ação

Se um teste espera que um botão esteja visível/habilitado mas ele não está, **verifique se o subprocesso está na situação correta para habilitar aquela ação**. O backend determina as permissões com base no estado do subprocesso (ex: `podeReabrirCadastro` requer `>= MAPA_HOMOLOGADO`). Se o teste só progrediu o subprocesso até `CADASTRO_HOMOLOGADO`, o botão não aparecerá.

**Ação:** Leia a lógica de permissões em `SubprocessoService.java` (método `verificarPermissoes`) e confira se o fluxo no teste atinge a situação mínima exigida. Se não, adicione passos de preparação (ex: criar mapa → disponibilizar → validar → aceitar → homologar).

## Testes que revelam bugs no frontend

Nem sempre a falha é do teste — o teste pode estar correto e revelando um **bug real do frontend**. Exemplos encontrados:

- **`isProcessoFinalizado` excessivamente restritivo**: `SubprocessoView.vue` e `SubprocessoCards.vue` tratavam `MAPA_HOMOLOGADO` como "processo finalizado", escondendo botões que deveriam estar visíveis exatamente nesse estado (como "Reabrir cadastro"). O processo só é realmente finalizado quando `SituacaoProcesso.FINALIZADO`.

- **Busca flat em árvore hierárquica**: `MapaVisualizacaoView.vue` usava `.find()` flat no array `processoDetalhe.unidades`, que só contém nós raiz. Unidades folha (ex: `SECAO_212`) ficam aninhadas em `filhos` e nunca eram encontradas, causando `codSubprocesso = null` e mapa vazio. Corrigido com busca recursiva.

- **Endpoint errado para homologação de mapa em revisão**: `MapaVisualizacaoView.confirmarAceitacao` chamava `homologarRevisaoCadastro` (homologação de *cadastro*) para processos de revisão, quando deveria chamar `homologarValidacao` (homologação de *mapa*). Isso fazia o subprocesso regredir ao estado de cadastro ao invés de avançar para `REVISAO_MAPA_HOMOLOGADO`.

**Ação:** Quando um teste falha em um fluxo que deveria funcionar, investigue também o código do frontend — não apenas o teste.

## Sempre assertar mensagens de sucesso após ações mutantes

Se um passo de preparação faz uma ação (validar mapa, aceitar, homologar), **adicione uma assertiva para a mensagem de sucesso** (ex: `await expect(page.getByText(/Mapa validado/i).first()).toBeVisible()`). Sem isso, erros silenciosos passam despercebidos — o teste avança mas a ação não foi efetivada no backend. Isso é especialmente importante em fluxos com múltiplos passos sequenciais, onde o passo N+1 depende do sucesso do passo N.

## Logs do backend nos resultados dos testes

Os logs do backend aparecem na saída dos testes (prefixados com `[WebServer] [BACKEND]`). Use `grep` para verificar se as chamadas de backend esperadas realmente aconteceram:

```bash
grep -i 'subprocesso 202' resultado.txt
```

Se uma ação que deveria ter sido executada não aparece nos logs (ex: "Validando mapa do subprocesso 202" ausente), isso indica que o frontend não chamou o backend — provavelmente porque `codSubprocesso` era null ou o endpoint errado foi chamado.