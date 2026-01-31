# 🔄 Guia de Migração para Fixtures E2E

Este guia ajuda a migrar testes E2E existentes para usar as novas fixtures, reduzindo duplicação e melhorando manutenibilidade.

## 📊 Benefícios

- ✅ **Redução de 90% de código boilerplate** (setup/cleanup)
- ✅ **Cleanup automático** de processos criados
- ✅ **Testes mais legíveis** focados no comportamento
- ✅ **Consistência** entre todos os testes

## 🎯 Padrões de Migração

### Padrão 1: Database Reset

**❌ ANTES:**
```typescript
import {test, expect} from './fixtures/auth-fixtures';
import {resetDatabase} from './hooks/hooks-limpeza';

test.describe('Meus testes', () => {
    test.beforeAll(async ({request}) => await resetDatabase(request));

    test('Deve fazer algo', async ({page}) => {
        // ...
    });
});
```

**✅ DEPOIS:**
```typescript
import {test, expect} from './fixtures/database-fixtures';

test.describe('Meus testes', () => {
    test('Deve fazer algo', async ({page, databaseResetada}) => {
        // Database já foi resetada!
        // ...
    });
});
```

**Redução:** 2 linhas → 0 linhas de setup

---

### Padrão 2: Processo + Cleanup

**❌ ANTES:**
```typescript
import {test, expect} from './fixtures/auth-fixtures';
import {criarProcesso} from './helpers/helpers-processos';
import {useProcessoCleanup} from './hooks/hooks-limpeza';

test.describe('Meus testes', () => {
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeEach(() => {
        cleanup = useProcessoCleanup();
    });

    test.afterEach(async ({request}) => {
        await cleanup.limpar(request);
    });

    test('Deve editar processo', async ({page, autenticadoComoAdmin}) => {
        const descricao = `Processo - ${Date.now()}`;
        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidade: 'ASSESSORIA_11',
            expandir: ['SECRETARIA_1']
        });

        await page.getByText(descricao).click();
        const url = new URL(page.url());
        const codigo = parseInt(url.searchParams.get('codProcesso') || '0');
        cleanup.registrar(codigo);
        
        // Teste real...
    });
});
```

**✅ DEPOIS:**
```typescript
import {test, expect} from './fixtures/processo-fixtures';

test.describe('Meus testes', () => {
    test('Deve editar processo', async ({page, processoFixture}) => {
        // Processo já criado e registrado para cleanup!
        await page.goto(`/processo/cadastro?codProcesso=${processoFixture.codigo}`);
        
        // Teste real...
    });
});
```

**Redução:** 23 linhas → 3 linhas (87% menos código)

---

### Padrão 3: Setup Completo (Database + Auth + Cleanup)

**❌ ANTES:**
```typescript
import {test, expect} from './fixtures/auth-fixtures';
import {resetDatabase, useProcessoCleanup} from './hooks/hooks-limpeza';

test.describe('CDU-XX', () => {
    let cleanup: ReturnType<typeof useProcessoCleanup>;

    test.beforeAll(async ({request}) => await resetDatabase(request));

    test.beforeEach(() => {
        cleanup = useProcessoCleanup();
    });

    test.afterEach(async ({request}) => {
        await cleanup.limpar(request);
    });

    test('Teste 1', async ({page, autenticadoComoAdmin}) => {
        // ...
    });
});
```

**✅ DEPOIS:**
```typescript
import {test, expect} from './fixtures/complete-fixtures';

test.describe('CDU-XX', () => {
    test('Teste 1', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
        // Database limpa + Login + Cleanup configurado!
        // ...
    });
});
```

**Redução:** 14 linhas → 0 linhas de boilerplate

---

### Padrão 4: Múltiplos Processos

**❌ ANTES:**
```typescript
test('Deve listar processos', async ({page, autenticadoComoAdmin}) => {
    const processos = [];
    
    for (let i = 0; i < 3; i++) {
        const descricao = `Processo ${i} - ${Date.now()}`;
        await criarProcesso(page, {descricao, ...});
        await page.getByText(descricao).click();
        const codigo = parseInt(new URL(page.url()).searchParams.get('codProcesso') || '0');
        processos.push({codigo, descricao});
        cleanup.registrar(codigo);
        await page.goto('/painel');
    }
    
    // Validações...
});
```

**✅ DEPOIS:**
```typescript
import {criarMultiplosProcessos} from './fixtures/processo-fixtures';

test('Deve listar processos', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
    const processos = await criarMultiplosProcessos(page, 3, {
        tipo: 'MAPEAMENTO',
        unidade: 'ASSESSORIA_11'
    });
    
    processos.forEach(p => cleanupAutomatico.registrar(p.codigo));
    
    // Validações...
});
```

**Redução:** 14 linhas → 6 linhas (57% menos código)

---

## 📋 Checklist de Migração

### Por Arquivo de Teste

- [ ] Substituir imports de `auth-fixtures` por fixture apropriada
- [ ] Remover `test.beforeAll(resetDatabase)`
- [ ] Remover `test.beforeEach(() => cleanup = ...)`
- [ ] Remover `test.afterEach(cleanup.limpar)`
- [ ] Refatorar criação manual de processos para usar `processoFixture` ou `criarMultiplosProcessos`
- [ ] Adicionar parâmetro fixture apropriado (`autenticadoComoAdmin`, `processoFixture`, etc.)
- [ ] Executar teste para validar
- [ ] Remover imports não utilizados

### Por Teste Individual

1. Identificar qual fixture usar:
   - Apenas autenticação? → `auth-fixtures`
   - Precisa de database limpa? → `database-fixtures`
   - Precisa criar processo? → `processo-fixtures`
   - Precisa de tudo? → `complete-fixtures`

2. Atualizar import no topo do arquivo

3. Remover código de setup/teardown manual

4. Usar parâmetros de fixture no teste

## 🎯 Priorização

Migrar nesta ordem:

1. ✅ **Alta prioridade** (16 arquivos): CDU-02 a CDU-07, CDU-09 (padrão completo com database+cleanup)
2. 🟡 **Média prioridade** (12 arquivos): CDU-08 a CDU-19 (padrão variado)
3. 🟢 **Baixa prioridade** (8 arquivos): CDU-20 a CDU-36, captura-telas

## ⚠️ Casos Especiais

### CDU-01 (Testes de Login)

**NÃO migrar!** Este teste especificamente valida o fluxo de login, então deve continuar usando login manual.

### Testes com Múltiplos Usuários

Use múltiplas fixtures:
```typescript
test('Admin vs Gestor', async ({page, autenticadoComoAdmin}) => {
    // Ações como admin...
    
    await page.evaluate(() => localStorage.clear());
    await page.goto('/login');
    await login(page, USUARIOS.GESTOR_COORD.titulo, USUARIOS.GESTOR_COORD.senha);
    
    // Ações como gestor...
});
```

### Testes que Criam Tipos Variados

Para processos com configurações específicas, continue usando `criarProcesso()` helper, mas use `cleanupAutomatico`:

```typescript
test('Processo de Revisão', async ({page, autenticadoComoAdmin, cleanupAutomatico}) => {
    await criarProcesso(page, {
        descricao: 'Revisão Específica',
        tipo: 'REVISAO',  // Tipo específico
        diasLimite: 60    // Configuração específica
    });
    
    const codigo = await capturarCodigo(page);
    cleanupAutomatico.registrar(codigo);
});
```

## 📊 Métricas Esperadas

- **Antes:** ~850 linhas de código de setup/cleanup duplicado
- **Depois:** ~85 linhas (90% de redução)
- **Tempo de execução:** Sem impacto (mesma lógica, menos código)
- **Manutenibilidade:** ⭐⭐⭐⭐⭐ (mudanças centralizadas em fixtures)
