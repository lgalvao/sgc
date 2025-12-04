# Fixtures e Hooks para Testes E2E

Este diretório contém utilidades para melhorar isolamento, desempenho e manutenibilidade dos testes E2E.

## 📁 Estrutura

```
e2e/
├── fixtures/           # Criação rápida de dados via API
│   └── processo-fixtures.ts
├── hooks/              # Gerenciamento de ciclo de vida dos testes
│   └── cleanup-hooks.ts
└── helpers/            # Helpers de interação com UI (existentes)
    ├── auth.ts
    ├── processo-helpers.ts
    └── atividade-helpers.ts
```

## 🎯 Fixtures vs Helpers

### Fixtures (API)
- **Propósito**: Criar dados diretamente via API backend
- **Vantagem**: ⚡ Muito mais rápido que navegação UI
- **Uso**: Setup inicial de testes
- **Exemplo**: `criarProcessoFixture()`

### Helpers (UI)
- **Propósito**: Interagir com a interface do usuário
- **Vantagem**: Testa o fluxo completo do usuário
- **Uso**: Testes de fluxo end-to-end
- **Exemplo**: `criarProcesso()` (navega pela UI)

## 📚 Guia de Uso

### 1. Reset de Banco de Dados

Use `resetDatabase()` no `beforeAll` de cada suite de testes:

```typescript
import { resetDatabase } from './hooks/cleanup-hooks';

test.describe('Minha Suite de Testes', () => {
    test.beforeAll(async ({ request }) => {
        await resetDatabase(request);
    });
    
    test('Meu teste', async ({ page }) => {
        // Banco está no estado limpo do seed.sql
    });
});
```

### 2. Cleanup Automático

Use `useProcessoCleanup()` para remover dados criados durante testes:

```typescript
import { useProcessoCleanup } from './hooks/cleanup-hooks';
import { criarProcesso } from './helpers/processo-helpers';

test.describe('Testes com Cleanup', () => {
    let cleanup: ReturnType<typeof useProcessoCleanup>;
    
    test.beforeEach(() => {
        cleanup = useProcessoCleanup();
    });
    
    test.afterEach(async ({ request }) => {
        await cleanup.limpar(request);
    });
    
    test('Cria e limpa processo', async ({ page }) => {
        // Criar via UI
        await criarProcesso(page, { ... });
        
        // Capturar ID do processo da URL
        const processoId = parseInt(page.url().match(/\/processo\/(\d+)/)?.[1] || '0');
        
        // Registrar para cleanup automático
        cleanup.registrar(processoId);
        
        // Teste continua...
        // Ao final, afterEach remove o processo automaticamente
    });
});
```

### 3. Fixtures para Setup Rápido

⚠️ **NOTA**: Os endpoints de fixtures ainda não estão implementados no backend.  
Veja o plano de implementação em `melhorias-e2e.md`.

Quando disponíveis, use assim:

```typescript
import { criarProcessoFixture } from './fixtures/processo-fixtures';

test('Teste que precisa de processo já criado', async ({ page, request }) => {
    // Criar processo via API (rápido, sem navegar UI)
    const processo = await criarProcessoFixture(request, {
        unidade: 'ASSESSORIA_11',
        iniciar: true  // Já criado e iniciado!
    });
    
    // Ir direto para a tela de interesse
    await page.goto(`/processo/${processo.codigo}`);
    
    // Testar funcionalidade específica
    await expect(page.getByText('Em andamento')).toBeVisible();
    
    // Cleanup
    await request.post(`http://localhost:10000/e2e/processo/${processo.codigo}/limpar`);
});
```

### 4. Padrão Completo (Recomendado)

Combine reset + cleanup + fixtures:

```typescript
import { resetDatabase, useProcessoCleanup } from './hooks/cleanup-hooks';
import { criarProcessoFixture } from './fixtures/processo-fixtures';

test.describe('Suite Completa', () => {
    let cleanup: ReturnType<typeof useProcessoCleanup>;
    
    // Reset completo antes de todos os testes
    test.beforeAll(async ({ request }) => {
        await resetDatabase(request);
    });
    
    // Inicializar cleanup antes de cada teste
    test.beforeEach(() => {
        cleanup = useProcessoCleanup();
    });
    
    // Limpar dados após cada teste
    test.afterEach(async ({ request }) => {
        await cleanup.limpar(request);
    });
    
    test('Teste isolado 1', async ({ page, request }) => {
        const processo = await criarProcessoFixture(request, { ... });
        cleanup.registrar(processo.codigo);
        // Teste...
    });
    
    test('Teste isolado 2', async ({ page, request }) => {
        const processo = await criarProcessoFixture(request, { ... });
        cleanup.registrar(processo.codigo);
        // Teste independente do anterior
    });
});
```

## 🔧 Implementação de Fixtures Backend

Para habilitar as fixtures, é necessário implementar endpoints no backend:

### Backend: `sgc.e2e.E2eController`

```java
@PostMapping("/fixtures/processo-mapeamento")
public ProcessoDto criarProcessoMapeamento(@RequestBody ProcessoFixtureRequest request) {
    // Implementação em melhorias-e2e.md
}

@PostMapping("/fixtures/processo-revisao")
public ProcessoDto criarProcessoRevisao(@RequestBody ProcessoFixtureRequest request) {
    // Similar ao mapeamento
}

public record ProcessoFixtureRequest(
    String descricao,
    String unidadeSigla,
    Boolean iniciar,
    Integer diasLimite
) {}
```

Veja detalhes completos em `melhorias-e2e.md`, seção "Exemplo 3: Novo Endpoint E2E de Fixture".

## 📖 Exemplos Completos

### Exemplo 1: Teste Simples com Cleanup

```typescript
test('Deve editar processo', async ({ page, request }) => {
    const cleanup = useProcessoCleanup();
    
    try {
        // Criar processo via UI
        const descricao = `Processo Teste ${Date.now()}`;
        await criarProcesso(page, { descricao, ... });
        
        // Capturar ID
        await page.getByText(descricao).click();
        const processoId = parseInt(page.url().match(/\/processo\/cadastro\/(\d+)/)?.[1] || '0');
        cleanup.registrar(processoId);
        
        // Editar processo
        await page.getByTestId('inp-processo-descricao').fill(descricao + ' EDITADO');
        await page.getByTestId('btn-processo-salvar').click();
        
        // Validar
        await expect(page.getByText(descricao + ' EDITADO')).toBeVisible();
    } finally {
        // Garantir cleanup mesmo se teste falhar
        await cleanup.limpar(request);
    }
});
```

### Exemplo 2: Teste com Fixture (quando disponível)

```typescript
test('Deve finalizar processo', async ({ page, request }) => {
    // Setup rápido via API
    const processo = await criarProcessoFixture(request, {
        unidade: 'ASSESSORIA_11',
        tipo: 'MAPEAMENTO',
        iniciar: true
    });
    
    try {
        // Navegar direto para o processo
        await page.goto(`/processo/${processo.codigo}`);
        
        // Finalizar
        await page.getByTestId('btn-processo-finalizar').click();
        await page.getByTestId('btn-finalizar-processo-confirmar').click();
        
        // Validar
        await expect(page.getByText('Finalizado')).toBeVisible();
    } finally {
        // Cleanup
        await request.post(`http://localhost:10000/e2e/processo/${processo.codigo}/limpar`);
    }
});
```

## ✅ Checklist de Boas Práticas

Ao escrever novos testes E2E:

- [ ] Usar `resetDatabase()` no `beforeAll` do describe
- [ ] Usar `useProcessoCleanup()` para gerenciar cleanup
- [ ] Registrar todos os processos/subprocessos criados para cleanup
- [ ] Usar `test.step()` para documentar passos do teste
- [ ] Preferir fixtures (API) para setup quando disponíveis
- [ ] Usar helpers (UI) apenas quando testar fluxo completo
- [ ] Garantir que teste pode rodar isoladamente
- [ ] Garantir que teste pode rodar em qualquer ordem

## 🔗 Referências

- [melhorias-e2e.md](../melhorias-e2e.md) - Análise completa e recomendações
- [cdu-02-melhorado.spec.ts](../cdu-02-melhorado.spec.ts) - Exemplo de teste melhorado
- [Playwright Best Practices](https://playwright.dev/docs/best-practices)
