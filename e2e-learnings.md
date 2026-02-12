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

### Analisar Resultados com grep

## Observações Finais

1. **Nunca aumentar timeouts sem motivo**: Se um elemento não aparece em 15s em um banco H2 local, o problema é de lógica ou permissão.
2. **Uso de steps**: Use `test.step()` para organizar logs dentro de um teste consolidado longo.
3. **Limpeza de Toasts**: Use o helper `limparNotificacoes(page)` antes de ações críticas de navegação se toasts estiverem bloqueando a tela.

