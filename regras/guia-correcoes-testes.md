# Correções de Testes - Lições Aprendidas e Diretrizes (versão Pós-Refatoração)

## 📋 Resumo Executivo
Este guia atualiza e amplia as diretrizes de correção de testes após a refatoração dos testes E2E em camadas (constantes → helpers de domínio → specs semânticos). Inclui aprendizados obtidos ao corrigir várias quebras pós-refatoração, exemplos práticos e recomendações operacionais para evitar regressões e acelerar correções futuras.

## 🔧 Contexto da Refatoração
A refatoração seguida pelo time adotou uma arquitetura em 3 camadas para os E2E:
- Camada 1 — Constantes (seletores, textos, URLs): `e2e/cdu/helpers/dados/constantes-teste.ts`
- Camada 2 — Linguagem de domínio (ações, verificações, navegação): `e2e/cdu/helpers/...`
- Camada 3 — Especificações semânticas (`e2e/cdu/*.spec.ts`)

Princípio chave: testes chamam apenas helpers semânticos da Camada 2; strings de UI e test-ids ficam na Camada 1.

---

## 🧠 Aprendizados Gerais (baseados na correção real)
1. Centralizar TEXTOS/SELETORES evita duplicação e facilita correção: quando um texto de UI muda, ajustar a constante corrige todos os testes que dependem dela.
2. Reexportar novos helpers nos `index.ts` é crítico — esquecer reexport quebra imports nos specs. Sempre atualize os índices junto com o novo helper.
3. Verificações devem ser semânticas e tolerantes:
   - Prefira verificar presença semântica (heading, tabela, estado) em vez de strings exatas.
   - Use regex/`i` case-insensitive e `.first()` para evitar "strict mode violation" quando o DOM contém múltiplas instâncias.
4. Notificações temporárias causam flakes: defina comportamento previsível em ambiente de teste (duracao configurável) ou verifique o conteúdo do elemento que persiste (ex.: modal, lista de alertas).
5. Test-data (mocks) é fonte comum de falsos negativos: garanta que mocks contenham cenários esperados (histórico, unidades, idProcesso).
6. Workarounds (DOM manipulation, JS injection) aceitos como último recurso — documente e marque TODO para remoção.

---

## ✅ Boas Práticas Pós-Refatoração

1. Sempre atualizar:
   - [`e2e/cdu/helpers/dados/constantes-teste.ts`](e2e/cdu/helpers/dados/constantes-teste.ts:1) ao mudar rótulos/ids.
   - `index.ts` de cada pasta (ações/verificações/navegação) ao adicionar helpers.

2. Helpers de verificação:
   - Encapsulem `expect` para evitar `expect` nos specs.
   - Exponham estados (ex.: `verificarCadastroDevolvidoComSucesso(page)`), não detalhes de DOM.
   - Implementem tolerância a variações de UI (plural, capitalização, pequenas reformulações).

3. Helpers de ação:
   - Sempre receber `page: Page` como primeiro argumento.
   - Tratar alternativas (botões com nomes diferentes) internamente.
   - Registrar logs/contexto para facilitar debugging.

4. Seletores e TestIds:
   - Preferir `[data-testid="xxx"]`.
   - Se o componente expõe `data-testid="btn-editar-competencia"`, mantenha a constante `EDITAR_COMPETENCIA: 'btn-editar-competencia'`.
   - Se possível, adicione fallback robusto: `page.locator('[data-testid="x"], button:has-text("Y")').first()`.
   - Se um test precisar de um identificador mais claro e ele não existir no código, crie um novo `data-testid` no componente em Camada 1 (faça essa mudança no código da aplicação, não no teste) e exporte a constante correspondente em `constantes-teste.ts`. Test-ids claros reduzem fragilidade e simplificam correções.

5. Notificações:
   - Em ambiente de teste, permita configurar duração (ex.: via variável de ambiente) para evitar flakes.
   - Verifique o local persistente da informação (ex.: título de notificação ou modal) em vez de seletor CSS transitório.

6. Timeouts e Esperas — recomendação prática:
   - Não use "aumentar timeouts" como primeira linha de defesa. Em protótipos e durante a maioria das execuções de teste, as ações são instantâneas; usar longos timeouts mascara problemas reais.
   - Só faz sentido recorrer a timeouts maiores quando o próprio código da aplicação implementa delays reais (por exemplo notificações que desaparecem, animações declaradas com delays, processos assíncronos controlados por timers).
   - Prefira estratégias de espera determinísticas (waitForLoadState, waitForFunction, esperar elemento visível) e configurar durações controladas para componentes que realmente dependem de timeout.

---

## 📌 Recomendações Operacionais (Checklists atualizados)

### Antes de editar um spec semântico (Camada 3)
- [ ] Confirmar que as strings necessárias existem em `TEXTOS`.
- [ ] Listar helpers que precisam ser criados/ajustados na Camada 2.
- [ ] Reexportar novas funções nos `index.ts` correspondentes.

### Ao corrigir um teste quebrado
1. Reproduzir localmente com `--headed`.
2. Ler e analisar o arquivo `error-context.md` gerado pelo Playwright para entender o contexto visual exato onde o teste quebrou. Esses snapshots mostram o DOM/estado no momento da falha e são essenciais para diagnóstico rápido.
3. Coletar snapshots, console logs e network.
4. Verificar se o erro é:
   - Dados/mocks → atualize `src/mocks/*` ou `e2e` mocks.
   - Seletores → atualizar `SELETORES/SELETORES_CSS`.
   - Texto → atualizar `TEXTOS`.
   - Tempo/interação → adicionar waits nas Camada 2 (não nos specs).
   - Falha de importação → checar `index.ts` reexports.
5. Modificar helpers (Camada 2) e reexecutar `npx playwright test --last-failed`.

### Para workarounds (somente se necessário)
- Documente a razão no helper com link para issue.
- Marque com TODO e crie issue para remover após correção upstream.

---

## Exemplos práticos (trechos úteis)

- Verificação tolerante de modal:
```ts
// verificarModalHistoricoAnaliseAberto (helper de verificações)
const modal = page.locator(SELETORES_CSS.MODAL_VISIVEL);
await expect(modal).toBeVisible();
await expect(modal.getByRole('heading', { name: /Histórico de Análises?/i }).first()).toBeVisible();
if ((await modal.getByText(/nenhuma análise registrada/i).count()) > 0) return;
await expect(modal.getByText(/data\/hora/i).first()).toBeVisible();
```

- Esperar notificação por texto (fallback genérico):
```ts
// esperarMensagemSucesso
const notificacao = page.locator('.notification', { hasText: mensagem });
await expect(notificacao).toBeVisible();
```

- Ações robustas com fallback de botões:
```ts
// confirmar ação no modal
if ((await modal.getByRole('button', { name: TEXTOS.CONFIRMAR }).count()) > 0) {
  await modal.getByRole('button', { name: TEXTOS.CONFIRMAR }).click();
} else {
  await modal.getByRole('button', { name: /aceitar|confirmar/i }).first().click();
}
```

---

## ✔️ Checklist Final de Aceitação (após correção)
- [ ] Teste reproduzido localmente em modo headed.
- [ ] Evidências coletadas (snapshot, logs, network).
- [ ] Correção aplicada na Camada correta (dados/constantes/helpers).
- [ ] `index.ts` atualizado quando helper novo criado.
- [ ] Tests rodando: `npx playwright test --last-failed` passa.
- [ ] Workarounds documentados com TODO e issue criada.

---

## 🎯 Princípios Reforçados
- Centralize textos e seletores.
- Mantenha helpers semânticos pequenos e resilientes.
- Prefira mudanças nas Camadas 1/2 em vez de tocar specs diretamente.
- Documente tudo: WHY > WHAT.

## 📚 Recursos
- [Playwright Best Practices](https://playwright.dev/docs/best-practices)
- [Vue.js Testing Cookbook](https://vue-test-utils.vuejs.org/guides/)
- [Debugging Playwright Tests](https://playwright.dev/docs/debug)
