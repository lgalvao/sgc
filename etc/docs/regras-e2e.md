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
- ✅ USE `waitForURL()` para navegação (ou os helpers semânticos `esperarPagina...`)
- ✅ USE `waitFor()` para elementos do DOM
- ✅ USE `expect().toHaveURL()` para verificar navegação
- ❌ NUNCA use `waitForTimeout()` em testes funcionais (permitido apenas em `captura-telas.spec.ts` para animações)

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

## Nuances de Autenticação

O comportamento do login varia conforme o tipo de unidade do usuário:

- **Unidades Operacionais/Intermediárias**: Geralmente possuem apenas um perfil e o sistema loga diretamente (Use `login`).
- **Unidades Interoperacionais**: Podem acumular perfis (ex: Chefe e Gestor), exigindo a escolha em um dropdown após a senha (Use `loginComPerfil`).
- **Troca de Usuário em Testes Seriais**: Ao usar `test.describe.serial()`, o estado do navegador é mantido entre os cenários. Se o teste N+1 precisa de um usuário diferente do teste N, chame o helper `login` ou `loginComPerfil` explicitamente no início do cenário, mesmo que já use uma fixture. Isso garante a navegação para `/login` e a troca efetiva da sessão.
- **Dica**: Se o teste falhar esperando pelo seletor de perfil, verifique se o usuário em questão realmente possui múltiplos perfis no `seed.sql`.

### Hierarquia de Unidades

Ao testar visibilidade de processos para Gestores ou Chefes, **consulte sempre o `e2e/setup/seed.sql`**. Se a unidade do processo não for subordinada à unidade do usuário testado, o sistema (corretamente) não exibirá o registro, causando falha no teste por "elemento não encontrado".

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

## Testes que revelam bugs no frontend

Nem sempre a falha é do teste — o teste pode estar correto e revelando um **bug real do frontend**. Exemplos encontrados:

- **`isProcessoFinalizado` excessivamente restritivo**: `SubprocessoView.vue` e `SubprocessoCards.vue` tratavam `MAPA_HOMOLOGADO` como "processo finalizado", escondendo botões.
- **Busca flat em árvore hierárquica**: `MapaVisualizacaoView.vue` usava `.find()` flat, falhando em unidades aninhadas.

**Ação:** Quando um teste falha em um fluxo que deveria funcionar, investigue também o código do frontend — não apenas o teste.

## Sempre assertar mensagens de sucesso após ações mutantes

Se um passo de preparação faz uma ação (validar mapa, aceitar, homologar), **adicione uma assertiva para a mensagem de sucesso** (ex: `await expect(page.getByText(/Mapa validado/i).first()).toBeVisible()`). Sem isso, erros passam despercebidos — o teste avança mas a ação não foi efetivada no backend.

## Logs do backend nos resultados dos testes

Os logs do backend aparecem na saída dos testes (prefixados com `[WebServer] [BACKEND]`). Use `grep` para verificar se as chamadas de backend esperadas realmente aconteceram:

```bash
grep -i 'subprocesso 202' resultado.txt
```
