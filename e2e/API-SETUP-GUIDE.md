# API Setup Helpers - Guia de Uso

Este documento demonstra como usar os helpers de API para preparar cenários de teste E2E.

## Pré-requisitos

1. Backend rodando com profile `e2e`:
   ```bash
   ./gradlew :backend:bootRun --args='--spring.profiles.active=e2e'
   ```

2. Os endpoints `/api/test/*` estarão disponíveis apenas com o profile `e2e` ativo.

## Importação

```typescript
import { garantirUsuario, garantirUnidade, garantirProcesso, setupCenarioCompleto } from './helpers';
```

## Exemplos de Uso

### 1. Criar um Usuário de Teste

```typescript
import { test } from '@playwright/test';
import { garantirUsuario } from './helpers';

test('meu teste', async ({ page }) => {
  // Garante que o usuário existe antes de começar o teste
  await garantirUsuario(page, {
    tituloEleitoral: 77777,
    nome: 'Teste Gestor',
    email: 'teste.gestor@test.com',
    ramal: '5555',
    unidadeCodigo: 2,
    perfis: ['GESTOR', 'CHEFE']
  });

  // Agora pode fazer login com este usuário
  // ... resto do teste
});
```

### 2. Criar uma Unidade de Teste

```typescript
await garantirUnidade(page, {
  codigo: 9998,
  nome: 'Unidade de Teste E2E',
  sigla: 'UTE2E',
  tipo: 'OPERACIONAL',
  unidadeSuperiorCodigo: 2
});
```

### 3. Criar um Processo de Teste

```typescript
const processoId = await garantirProcesso(page, {
  descricao: 'Mapeamento de Competências 2025',
  tipo: 'MAPEAMENTO',
  situacao: 'CRIADO',
  dataLimite: '2025-12-31T23:59:59',
  unidadesCodigos: [2, 3, 8]
});

console.log(`Processo criado com ID: ${processoId}`);
```

### 4. Setup Completo de Cenário

Para casos mais complexos, use `setupCenarioCompleto`:

```typescript
import { test } from '@playwright/test';
import { setupCenarioCompleto, loginComoGestor } from './helpers';

test('teste com cenário completo', async ({ page }) => {
  // Prepara todo o cenário de uma vez
  const { processoId } = await setupCenarioCompleto(page, {
    usuario: {
      tituloEleitoral: 66666,
      nome: 'Gestor Regional',
      email: 'gestor.regional@test.com',
      ramal: '4444',
      unidadeCodigo: 8,
      perfis: ['GESTOR']
    },
    unidades: [
      {
        codigo: 9997,
        nome: 'Região Norte',
        sigla: 'RN',
        tipo: 'INTERMEDIARIA',
        unidadeSuperiorCodigo: 2
      },
      {
        codigo: 9996,
        nome: 'Seção Norte A',
        sigla: 'SNA',
        tipo: 'OPERACIONAL',
        unidadeSuperiorCodigo: 9997
      }
    ],
    processo: {
      descricao: 'Revisão Regional Norte',
      tipo: 'REVISAO',
      situacao: 'CRIADO',
      unidadesCodigos: [9997, 9996]
    }
  });

  console.log(`Cenário criado. Processo ID: ${processoId}`);

  // Agora pode executar o teste com os dados preparados
  // ... resto do teste
});
```

## Idempotência

Todos os endpoints são idempotentes. Se você chamar novamente com os mesmos dados:

- **Usuários**: Identificados por `tituloEleitoral`
- **Unidades**: Identificadas por `codigo`
- **Processos**: Identificados por `descricao`

Exemplo:
```typescript
// Primeira chamada
await garantirUsuario(page, { tituloEleitoral: 12345, ... });
// Retorna: { created: true, message: "Usuário criado" }

// Segunda chamada (mesmos dados)
await garantirUsuario(page, { tituloEleitoral: 12345, ... });
// Retorna: { created: false, message: "Usuário já existia" }
```

## Valores Válidos para Enums

### Tipo de Unidade
- `INTEROPERACIONAL`
- `INTERMEDIARIA`
- `OPERACIONAL`

### Tipo de Processo
- `MAPEAMENTO`
- `REVISAO`
- `DIAGNOSTICO`

### Situação de Processo
- `CRIADO`
- `EM_ANDAMENTO`
- `FINALIZADO`

### Perfis de Usuário
- `ADMIN`
- `GESTOR`
- `CHEFE`
- `SERVIDOR`

## Dicas de Uso

1. **Use em `test.beforeEach`**: Para garantir que os dados estejam sempre disponíveis
   ```typescript
   test.beforeEach(async ({ page }) => {
     await setupCenarioCompleto(page, { ... });
   });
   ```

2. **Códigos únicos**: Use códigos altos (ex: 9000+) para evitar conflitos com dados do `data.sql`

3. **Cleanup não é necessário**: O banco H2 é limpo automaticamente entre execuções completas dos testes

4. **Performance**: API setup é muito mais rápido que criar dados via UI

## Exemplo Completo

```typescript
import { test, expect } from '@playwright/test';
import { setupCenarioCompleto, loginComoGestor } from './helpers';

test.describe('CDU-XX: Meu Caso de Uso', () => {
  let processoId: number;

  test.beforeEach(async ({ page }) => {
    // Prepara cenário antes de cada teste
    const result = await setupCenarioCompleto(page, {
      usuario: {
        tituloEleitoral: 55555,
        nome: 'Gestor Teste CDU-XX',
        email: 'gestor.cdu-xx@test.com',
        ramal: '3333',
        unidadeCodigo: 2,
        perfis: ['GESTOR']
      },
      processo: {
        descricao: 'Processo CDU-XX',
        tipo: 'MAPEAMENTO',
        situacao: 'CRIADO',
        unidadesCodigos: [2, 3]
      }
    });

    processoId = result.processoId!;
  });

  test('deve fazer algo com o processo', async ({ page }) => {
    // Login
    await loginComoGestor(page);

    // Navega para o processo
    await page.goto(`/processo/${processoId}`);

    // Suas verificações...
    expect(processoId).toBeGreaterThan(0);
  });
});
```

## Troubleshooting

### Erro: "Cannot find module"
Certifique-se de que os helpers estão exportados corretamente em `e2e/helpers/index.ts`.

### Erro: 404 ao chamar /api/test/*
Verifique se o backend está rodando com `--spring.profiles.active=e2e`.

### Erro: "Value not permitted for column"
Verifique se está usando os valores corretos dos enums (veja seção "Valores Válidos").

### Processo não aparece na UI
Certifique-se de que as `unidadesCodigos` existem no banco (use `data.sql` ou crie com `garantirUnidade`).
