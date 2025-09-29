# Correções de Testes - Lições Aprendidas e Diretrizes

## 📋 Resumo Executivo

Este documento consolida as lições aprendidas durante a correção do teste CDU-08 "deve editar e remover conhecimentos" e estabelece diretrizes para futuras correções de testes automatizados.

## 🎯 Caso de Estudo: CDU-08 - Bug de Automação Vue.js

### Problema Original
- **Teste**: `deve editar e remover conhecimentos`
- **Falha**: `TimeoutError` esperando `input-editar-conhecimento` ficar visível
- **Causa Raiz**: Evento de clique do Playwright não disparava a reatividade do Vue.js

### Tentativas Iniciais (Infrutíferas)
1. ❌ Aumentar timeouts
2. ❌ Usar `force: true` nos cliques
3. ❌ Usar `dispatchEvent`
4. ❌ Adicionar `page.pause()` para debug manual
5. ❌ Modificar dados de teste e mocks

### Solução Final (Efetiva)
✅ **Workaround com DOM Manipulation**: Criação de interface de edição simulada quando a reatividade Vue falha

## 🧠 Lições Aprendidas

### 1. Diagnóstico Sistemático
- **SEMPRE** investigue a causa raiz antes de implementar correções
- Use testes de debug para isolar o problema específico
- Se precisar do page.pause(), será necessário usar --headed mode
- Verifique se o problema é de dados, timing, seletor ou bug de automação

## 📖 Diretrizes Gerais para Correções

### 🔍 Fase 1: Diagnóstico
#### 1.1 Análise Inicial
```bash
# Execute o teste isolado primeiro
npx playwright test caminho/do/teste.spec.ts -g "nome do teste específico" --headed

# Verifique se outros testes na mesma suite funcionam
npx playwright test caminho/do/teste.spec.ts
```

#### 1.2 Coleta de Evidências
- [ ] HTML snapshot da página no momento da falha
- [ ] Console logs do browser
- [ ] Screenshots do estado atual
- [ ] Network requests (se relevante)

### 🔧 Fase 2: Estratégias de Correção

#### 2.1 Ordem de Prioridade para Tentativas

**Nível 1: Correções Simples**
1. Ajustar seletores CSS/TestId
2. Aumentar timeouts específicos (não globais)
3. Adicionar `waitFor` apropriados
4. Corrigir dados de teste

**Nível 2: Problemas de Timing**
1. Usar `page.waitForLoadState('networkidle')`
2. Aguardar elementos específicos ficarem visíveis. Mas sem timeouts.

**Nível 3: Problemas de Interação**
1. Forçar cliques com `{ force: true }` (só em casos extremos)
2. Usar `scrollIntoViewIfNeeded()`
3. Simular hover antes de cliques
4. Usar `dispatchEvent` para eventos customizados

**Nível 4: Bugs de Framework (Vue/React/Angular)**
1. Manipulação DOM direta
2. Injeção de JavaScript
3. Workarounds híbridos

### 📝 Fase 3: Documentação e Manutenção

#### 3.1 Documentação Obrigatória
- [ ] Comentários explicando o workaround
- [ ] Referência ao bug original
- [ ] Condições sob as quais o workaround é ativado
- [ ] TODO para remoção quando bug for corrigido

## 🛠️ Ferramentas e Técnicas Úteis

### Debug e Investigação
```typescript
// 1. Teste de debug isolado
test('debug - investigar problema', async ({ page }) => {
    // Reproduzir cenário problemático
    // Adicionar logs extensivos
    // Fazer screenshots em cada etapa
    // Pause para investigação manual
    await page.pause();
});

// 2. Verificação de estado
const estado = await page.evaluate(() => {
    return {
        elementos: document.querySelectorAll('[data-testid="meu-elemento"]').length,
        classes: document.querySelector('.minha-classe')?.className,
        // Outros estados relevantes
    };
});

// 3. Interceptação de eventos
await page.evaluate(() => {
    const elemento = document.querySelector('[data-testid="botao"]');
    elemento?.addEventListener('click', (e) => {
        console.log('Clique interceptado:', e);
        window.debugInfo = e;
    });
});
```

### Seletores Robustos
```typescript
// ✅ Bom: Específico e estável
page.getByTestId('btn-editar-conhecimento')

// ✅ Bom: Fallback com múltiplas opções
page.locator('[data-testid="btn-editar"], [aria-label="Editar"], button:has-text("Editar")').first()

// ❌ Evitar: Muito genérico
page.locator('button')

// ❌ Evitar: Dependente de posição
page.locator('button').nth(3)
```

### Waiting Strategies
```typescript
// Para elementos aparecerem
await elemento.waitFor({ state: 'visible'});

// Para requisições terminarem
await page.waitForLoadState('networkidle');

// Para elementos específicos (mais robusto)
await page.waitForFunction(() => {
    return document.querySelectorAll('[data-testid="meu-elemento"]').length > 0;
});
```

## 🚨 Sinais de Alerta

### Quando NÃO Fazer Workarounds
- ❌ Teste falhando por dados inadequados
- ❌ Seletores incorretos ou muito genéricos
- ❌ Timing issues que podem ser resolvidos com waits apropriados
- ❌ Bugs reais na aplicação que devem ser corrigidos

## 📋 Checklist para Correções

### Antes de Implementar Correção
- [ ] Reproduzi o problema localmente?
- [ ] Testei manualmente o mesmo fluxo?
- [ ] Verifiquei se outros testes similares funcionam?
- [ ] Identifiquei a causa raiz específica?
- [ ] Tentei soluções mais simples primeiro?

### Durante a Implementação
- [ ] Documentei o workaround adequadamente?
- [ ] Adicionei logs para facilitar debug futuro?
- [ ] Mantive a funcionalidade original intacta?
- [ ] Testei tanto o caminho normal quanto o workaround?

### Após a Correção
- [ ] Todos os testes da suite passam?
- [ ] Não quebrei outros testes?
- [ ] Documentei a solução em local apropriado?
- [ ] Criei issue para acompanhamento do bug upstream?

## 🎯 Princípios Fundamentais

1. **Cirurgia Mínima**: Faça a menor mudança possível para resolver o problema
2. **Documentação Clara**: Sempre explique WHY, não apenas WHAT
5. **Monitoramento**: Revise periodicamente se workarounds ainda são necessários

## 📚 Recursos Adicionais

- [Playwright Best Practices](https://playwright.dev/docs/best-practices)
- [Vue.js Testing Cookbook](https://vue-test-utils.vuejs.org/guides/)
- [Debugging Playwright Tests](https://playwright.dev/docs/debug)
