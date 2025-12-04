# Melhorias e Padronização dos Testes E2E

**Data da Análise:** 2025-12-04  
**Versão:** 1.0

---

## 📋 Sumário Executivo

Este documento apresenta uma análise detalhada dos testes end-to-end (E2E) do projeto SGC, identificando problemas de **interferência de dados**, **falta de padronização** e **oportunidades de melhoria**. 

### Principais Achados:

1. **Interferência de Dados**: Testes compartilham banco de dados sem isolamento adequado
2. **Inconsistência no Reset**: Apenas 2 de 9 arquivos utilizam reset de banco
3. **Dependências Sequenciais**: Alguns testes dependem de execução ordenada
4. **Endpoints E2E Limitados**: Faltam operações de limpeza granular
5. **Falta de Fixtures**: Ausência de dados pré-configurados reutilizáveis

---

## 🔍 Análise Detalhada

### 1. Problemas de Interferência de Dados

#### 1.1 Testes sem Isolamento

**Problema:** A maioria dos testes cria dados (processos, atividades, mapas) mas não os limpa após execução.

**Evidências:**

- **cdu-01.spec.ts**: ✅ Sem criação de dados (apenas login)
- **cdu-02.spec.ts**: ❌ Cria processos sem limpeza
  ```typescript
  const descricaoProcesso = `Processo E2E - ${Date.now()}`;
  await criarProcesso(page, { descricao: descricaoProcesso, ... });
  // Sem cleanup! Processo permanece no banco
  ```

- **cdu-03.spec.ts**: ❌ Cria e edita processos sem limpeza
  ```typescript
  test('Deve editar um processo existente', async ({ page }) => {
      const descricaoOriginal = `Processo para Edição - ${Date.now()}`;
      await criarProcesso(page, { ... });
      // Edição, mas sem cleanup
  });
  ```

- **cdu-04.spec.ts**: ❌ Cria processos e subprocessos sem limpeza

- **cdu-05.spec.ts**: ❌ Cria ciclo completo de mapeamento sem limpeza
  - **Crítico**: Teste "Fase 2" depende que "Fase 1" tenha executado completamente
  - Utiliza variáveis compartilhadas (`timestamp`, `descProcMapeamento`)

- **cdu-06.spec.ts**: ❌ Cria processos sem limpeza

- **cdu-07.spec.ts**: ❌ Cria processos e subprocessos sem limpeza

- **cdu-08.spec.ts**: ✅ Usa `beforeAll` com reset
  ```typescript
  test.beforeAll(async ({ request }) => {
      const response = await request.post('http://localhost:10000/e2e/reset-database');
      expect(response.ok()).toBeTruthy();
  });
  ```

- **cdu-09.spec.ts**: ⚠️ Usa reset mas compartilha processo entre testes
  ```typescript
  test.beforeAll(async ({ request }) => {
      // Reset database
  });
  test('Preparacao: Admin cria e inicia processo', ...); // Cria processo
  test('Cenario 1: ...', ...); // Usa o mesmo processo
  test('Cenario 2: ...', ...); // Usa o mesmo processo
  ```

#### 1.2 Impacto da Interferência

1. **Flakiness**: Testes podem falhar aleatoriamente se executados em ordem diferente
2. **Poluição de Dados**: Banco acumula processos, subprocessos, mapas, atividades
3. **Efeitos Colaterais**: Um teste pode afetar o resultado de outro
4. **Difícil Depuração**: Falhas são difíceis de reproduzir isoladamente

### 2. Inconsistências de Padronização

#### 2.1 Reset de Banco de Dados

| Arquivo | Usa Reset? | beforeAll/beforeEach | Observações |
|---------|------------|---------------------|-------------|
| cdu-01 | ❌ Não | - | Apenas login (OK) |
| cdu-02 | ❌ Não | beforeEach (goto login) | Cria dados sem limpar |
| cdu-03 | ❌ Não | beforeEach (login admin) | Cria dados sem limpar |
| cdu-04 | ❌ Não | beforeEach (login admin) | Cria dados sem limpar |
| cdu-05 | ❌ Não | - | Testes sequenciais dependentes |
| cdu-06 | ❌ Não | - | Cria dados sem limpar |
| cdu-07 | ❌ Não | - | Cria dados sem limpar |
| cdu-08 | ✅ Sim | beforeAll (reset) | **Bom exemplo** |
| cdu-09 | ✅ Sim | beforeAll (reset) | Mas compartilha estado |

**Recomendação:** Todos os testes que criam dados devem usar reset ou cleanup.

#### 2.2 Padrões de Nomenclatura

**Inconsistências encontradas:**

1. **Descrições de processos**:
   - `Processo E2E - ${Date.now()}`
   - `Processo para Edição - ${Date.now()}`
   - `Processo CDU-06 ${timestamp}`
   - `Processo CDU-08 Map ${timestamp}`

2. **Test steps**: 
   - Alguns testes usam `test.step()` (cdu-08, cdu-09) ✅
   - Outros não usam (cdu-02, cdu-03, cdu-04) ❌

3. **Helpers**:
   - `auth.ts`: Bem estruturado com funções `login()`, `loginComPerfil()`
   - `processo-helpers.ts`: Bom, mas poderia incluir cleanup
   - `atividade-helpers.ts`: Bom, modular

**Recomendação:** Padronizar nomenclatura e uso de `test.step()` para melhor legibilidade.

### 3. Dependências Sequenciais

#### 3.1 CDU-05: Testes Acoplados

```typescript
const timestamp = Date.now(); // Variável de escopo de arquivo
const descProcMapeamento = `Mapeamento Setup ${timestamp}`;
const descProcRevisao = `Revisão Teste ${timestamp}`;

test('Fase 1: Ciclo completo de Mapeamento', async ({page}) => {
    await passo1_AdminCriaEIniciaProcessoMapeamento(page, descProcMapeamento);
    // ... 5 passos sequenciais
});

test('Fase 2: Iniciar processo de Revisão', async ({page}) => {
    // Este teste ASSUME que Fase 1 criou um mapa vigente
    await criarProcesso(page, { tipo: 'REVISAO', ... });
});
```

**Problema:** Se "Fase 2" executar antes de "Fase 1" (ou se "Fase 1" falhar), "Fase 2" pode falhar.

**Recomendação:** 
- Opção 1: Combinar em um único teste
- Opção 2: Usar `test.describe.serial()` para garantir ordem
- Opção 3: Fase 2 deve criar seu próprio mapa vigente

#### 3.2 CDU-09: Estado Compartilhado

```typescript
const descProcesso = `Processo CDU-09 ${timestamp}`; // Variável de arquivo

test('Preparacao: Admin cria e inicia processo', ...);
test('Cenario 1: ...', ...); // Usa descProcesso
test('Cenario 2: ...', ...); // Usa descProcesso
test('Cenario 3: ...', ...); // Usa descProcesso
```

**Problema:** Testes não são independentes. Se "Preparacao" falhar, todos falham.

**Recomendação:**
- Usar `test.describe.serial()` + `test.beforeAll()` para setup compartilhado
- OU: Cada teste deve criar seus próprios dados

### 4. Limitações dos Endpoints E2E

#### 4.1 Endpoints Atuais

**Backend: `sgc.e2e.E2eController`**

```java
@PostMapping("/reset-database")
public void resetDatabase() throws SQLException {
    // Trunca TODAS as tabelas
    // Recarrega seed.sql
}

@PostMapping("/processo/{codigo}/limpar")
public void limparProcessoComDependentes(@PathVariable Long codigo) {
    // Remove processo e TODOS os dados relacionados
}
```

**Análise:**

✅ **Pontos Positivos:**
- `reset-database`: Garante estado limpo inicial
- `limpar-processo`: Remove cascata de dados

❌ **Pontos Negativos:**
- **Reset completo é pesado**: Trunca e recarrega tudo
- **Falta granularidade**: Não há como limpar apenas subprocessos, mapas, atividades
- **Sem endpoint para criar fixtures**: Testes precisam criar via UI

#### 4.2 Endpoints Sugeridos

Para melhorar isolamento e desempenho, adicionar:

```java
// 1. Limpeza granular
@PostMapping("/subprocesso/{codigo}/limpar")
public void limparSubprocesso(@PathVariable Long codigo);

@PostMapping("/mapa/{codigo}/limpar")
public void limparMapa(@PathVariable Long codigo);

// 2. Criação de fixtures via API (mais rápido que UI)
@PostMapping("/fixtures/processo-mapeamento")
public ProcessoDto criarProcessoMapeamento(@RequestBody FixtureRequest request);

@PostMapping("/fixtures/processo-mapeamento-completo")
public ProcessoCompletoDto criarProcessoMapeamentoCompleto();
// Retorna processo + subprocesso + mapa + atividades

// 3. Query de estado para validações
@GetMapping("/estado/processo/{codigo}")
public EstadoProcessoDto obterEstadoProcesso(@PathVariable Long codigo);
```

**Benefícios:**
- ⚡ Testes mais rápidos (menos navegação UI)
- 🎯 Melhor isolamento (cleanup granular)
- 🧪 Fixtures reutilizáveis
- 🔍 Validações mais robustas

### 5. Oportunidades de Melhoria

#### 5.1 Sistema de Fixtures

**Problema:** Cada teste recria dados via UI (lento e frágil).

**Solução:** Criar fixtures reutilizáveis.

**Exemplo:**

```typescript
// e2e/fixtures/processo-fixtures.ts
export async function criarProcessoFixture(request: RequestContext, options: {
    tipo: 'MAPEAMENTO' | 'REVISAO',
    unidade: string,
    situacao?: 'CRIADO' | 'EM_ANDAMENTO' | 'FINALIZADO'
}): Promise<ProcessoFixture> {
    const response = await request.post('http://localhost:10000/e2e/fixtures/processo', {
        data: options
    });
    return response.json();
}

// Uso no teste
test('Deve validar processo finalizado', async ({ page, request }) => {
    const processo = await criarProcessoFixture(request, {
        tipo: 'MAPEAMENTO',
        unidade: 'ASSESSORIA_11',
        situacao: 'FINALIZADO' // Já cria finalizado!
    });
    
    await page.goto(`/processo/${processo.codigo}`);
    // Testar visualização de processo finalizado
});
```

#### 5.2 Hooks de Cleanup

**Solução:** Padronizar cleanup com hooks.

```typescript
// e2e/hooks/cleanup-hooks.ts
export function useProcessoCleanup() {
    const processosParaLimpar: number[] = [];
    
    return {
        registrar: (codigo: number) => processosParaLimpar.push(codigo),
        limpar: async (request: RequestContext) => {
            for (const codigo of processosParaLimpar) {
                await request.post(`http://localhost:10000/e2e/processo/${codigo}/limpar`);
            }
        }
    };
}

// Uso no teste
test.describe('CDU-02', () => {
    let cleanup: ReturnType<typeof useProcessoCleanup>;
    
    test.beforeEach(() => {
        cleanup = useProcessoCleanup();
    });
    
    test.afterEach(async ({ request }) => {
        await cleanup.limpar(request);
    });
    
    test('Deve criar processo', async ({ page }) => {
        const processo = await criarProcesso(page, { ... });
        cleanup.registrar(processo.codigo); // Auto-cleanup
    });
});
```

#### 5.3 Estratégias de Isolamento

**Três abordagens:**

1. **Reset Total (atual em cdu-08/09)**
   ```typescript
   test.beforeAll(async ({ request }) => {
       await request.post('http://localhost:10000/e2e/reset-database');
   });
   ```
   - ✅ Isolamento completo
   - ❌ Lento (trunca + seed)

2. **Cleanup Seletivo (recomendado)**
   ```typescript
   test.afterEach(async ({ request }) => {
       await request.post(`http://localhost:10000/e2e/processo/${processoId}/limpar`);
   });
   ```
   - ✅ Rápido
   - ✅ Isolamento adequado
   - ❌ Requer registro de IDs

3. **Transações (ideal, mas complexo)**
   - Cada teste roda em transação
   - Rollback ao final
   - Requer mudanças no backend

**Recomendação:** Usar abordagem 2 (Cleanup Seletivo) com hooks.

#### 5.4 Paralelização

**Atual:** `workers: 1` (sequencial)

**Problema:** Testes levam muito tempo.

**Solução com isolamento adequado:**

```typescript
// playwright.config.ts
export default defineConfig({
    workers: process.env.CI ? 2 : 4, // Paralelo em CI e local
    fullyParallel: true,
    // ... rest of config
});
```

**Requisitos para paralelização:**
1. ✅ Reset ou cleanup em cada teste
2. ✅ Sem estado compartilhado entre testes
3. ✅ Dados de seed suficientes para N workers

Com as melhorias propostas, seria possível executar em paralelo com segurança.

---

## 📊 Tabela de Prioridades

| Melhoria | Impacto | Esforço | Prioridade |
|----------|---------|---------|------------|
| Adicionar cleanup em todos os testes | 🔴 Alto | 🟡 Médio | **P0** |
| Padronizar uso de `beforeAll` com reset | 🔴 Alto | 🟢 Baixo | **P0** |
| Criar endpoints E2E granulares | 🟠 Médio | 🟡 Médio | **P1** |
| Implementar sistema de fixtures | 🟠 Médio | 🔴 Alto | **P1** |
| Refatorar CDU-05 (dependências sequenciais) | 🟡 Baixo | 🟢 Baixo | **P2** |
| Refatorar CDU-09 (estado compartilhado) | 🟡 Baixo | 🟢 Baixo | **P2** |
| Habilitar paralelização | 🟠 Médio | 🔴 Alto | **P3** |
| Adicionar `test.step()` consistentemente | 🟢 Baixo | 🟢 Baixo | **P3** |

---

## 🎯 Plano de Ação Recomendado

### Fase 1: Correção Urgente (Sprint 1)

1. **Adicionar cleanup em todos os arquivos de teste**
   - `cdu-02.spec.ts` a `cdu-07.spec.ts`
   - Usar `test.afterEach()` com endpoint `/e2e/processo/{codigo}/limpar`

2. **Padronizar reset de banco**
   - Todos os describes devem ter:
   ```typescript
   test.beforeAll(async ({ request }) => {
       await request.post('http://localhost:10000/e2e/reset-database');
   });
   ```

3. **Corrigir CDU-05 e CDU-09**
   - Usar `test.describe.serial()` OU
   - Tornar testes independentes

### Fase 2: Melhorias de Infraestrutura (Sprint 2)

4. **Criar endpoints E2E adicionais**
   - `/e2e/fixtures/processo-mapeamento`
   - `/e2e/fixtures/processo-revisao`
   - `/e2e/subprocesso/{codigo}/limpar`
   - `/e2e/mapa/{codigo}/limpar`

5. **Implementar sistema de fixtures**
   - Criar `e2e/fixtures/` com helpers
   - Migrar testes para usar fixtures quando apropriado

6. **Criar hooks de cleanup**
   - `e2e/hooks/cleanup-hooks.ts`
   - Migrar testes para usar hooks

### Fase 3: Otimização (Sprint 3)

7. **Habilitar paralelização**
   - Garantir que todos os testes estão isolados
   - Aumentar `workers` para 2-4
   - Adicionar dados de seed suficientes

8. **Padronizar estrutura dos testes**
   - Usar `test.step()` consistentemente
   - Padronizar nomenclatura de processos
   - Criar guia de estilo para testes E2E

---

## 📝 Exemplos de Implementação

### Exemplo 1: CDU-02 com Cleanup

**Antes:**
```typescript
test('Deve criar processo e visualizá-lo na tabela', async ({ page }) => {
    const descricaoProcesso = `Processo E2E - ${Date.now()}`;
    await criarProcesso(page, { descricao: descricaoProcesso, ... });
    await verificarProcessoNaTabela(page, { descricao: descricaoProcesso, ... });
    // SEM CLEANUP - processo permanece no banco
});
```

**Depois:**
```typescript
test.describe('CDU-02 - Visualizar Painel', () => {
    let processoId: number | null = null;

    test.beforeAll(async ({ request }) => {
        // Reset completo para isolamento
        await request.post('http://localhost:10000/e2e/reset-database');
    });

    test.afterEach(async ({ request }) => {
        if (processoId) {
            await request.post(`http://localhost:10000/e2e/processo/${processoId}/limpar`);
            processoId = null;
        }
    });

    test('Deve criar processo e visualizá-lo na tabela', async ({ page }) => {
        const descricaoProcesso = `Processo E2E - ${Date.now()}`;
        
        await criarProcesso(page, { descricao: descricaoProcesso, ... });
        
        // Capturar ID do processo para cleanup
        const url = page.url(); // Ex: /processo/123
        processoId = parseInt(url.match(/\/processo\/(\d+)/)?.[1] || '0');
        
        await verificarProcessoNaTabela(page, { descricao: descricaoProcesso, ... });
    });
});
```

### Exemplo 2: CDU-05 com test.describe.serial

**Antes:**
```typescript
test('Fase 1: Ciclo completo de Mapeamento', async ({page}) => { ... });
test('Fase 2: Iniciar processo de Revisão', async ({page}) => { ... });
// Fase 2 depende de Fase 1 mas ordem não é garantida
```

**Depois:**
```typescript
test.describe.serial('CDU-05 - Iniciar processo de revisao', () => {
    let mapaVigenteCodigo: number;
    
    test.beforeAll(async ({ request }) => {
        await request.post('http://localhost:10000/e2e/reset-database');
    });

    test('Fase 1: Ciclo completo de Mapeamento', async ({page}) => {
        // ... passos de mapeamento
        // Capturar ID do mapa criado
        mapaVigenteCodigo = /* extrair do response ou UI */;
    });

    test('Fase 2: Iniciar processo de Revisão', async ({page}) => {
        // Agora GARANTIDO que Fase 1 executou antes
        expect(mapaVigenteCodigo).toBeDefined();
        // ... criar processo de revisão
    });
});
```

### Exemplo 3: Novo Endpoint E2E de Fixture

**Backend: E2eController.java**
```java
@PostMapping("/fixtures/processo-mapeamento")
public ProcessoDto criarProcessoMapeamento(@RequestBody ProcessoFixtureRequest request) {
    // Validar entrada
    if (request.unidadeSigla() == null) {
        throw new IllegalArgumentException("Unidade é obrigatória");
    }
    
    // Criar processo via service (mais rápido que UI)
    var processo = processoService.criar(
        request.descricao() != null ? request.descricao() : "Processo Fixture E2E",
        TipoProcesso.MAPEAMENTO,
        LocalDate.now().plusDays(30)
    );
    
    // Adicionar unidade
    var unidade = unidadeRepo.findBySigla(request.unidadeSigla())
        .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada"));
    processoService.adicionarUnidade(processo.getCodigo(), unidade.getCodigo());
    
    // Iniciar se solicitado
    if (request.iniciar() != null && request.iniciar()) {
        processoService.iniciar(processo.getCodigo());
    }
    
    return processoMapper.toDto(processo);
}

public record ProcessoFixtureRequest(
    String descricao,
    String unidadeSigla,
    Boolean iniciar
) {}
```

**Frontend: processo-fixtures.ts**
```typescript
export interface ProcessoFixture {
    codigo: number;
    descricao: string;
    situacao: string;
}

export async function criarProcessoFixture(
    request: RequestContext,
    options: {
        unidade: string,
        iniciar?: boolean,
        descricao?: string
    }
): Promise<ProcessoFixture> {
    const response = await request.post('http://localhost:10000/e2e/fixtures/processo-mapeamento', {
        data: {
            unidadeSigla: options.unidade,
            iniciar: options.iniciar ?? false,
            descricao: options.descricao ?? `Fixture E2E ${Date.now()}`
        }
    });
    
    expect(response.ok()).toBeTruthy();
    return await response.json();
}
```

**Uso no teste:**
```typescript
test('Deve exibir detalhes do processo', async ({ page, request }) => {
    // Criar processo via API (mais rápido que navegação UI)
    const processo = await criarProcessoFixture(request, {
        unidade: 'ASSESSORIA_11',
        iniciar: true
    });
    
    // Ir direto para a tela de detalhes
    await page.goto(`/processo/${processo.codigo}`);
    
    // Validar UI
    await expect(page.getByText(processo.descricao)).toBeVisible();
    
    // Cleanup automático
    await request.post(`http://localhost:10000/e2e/processo/${processo.codigo}/limpar`);
});
```

---

## 🔧 Configurações Sugeridas

### playwright.config.ts (Atualizado)

```typescript
export default defineConfig({
    testDir: './e2e',
    timeout: 30_000, // Aumentar para fixtures via API
    workers: 1, // Manter 1 até isolamento estar completo
    fullyParallel: false, // Aguardar isolamento
    expect: { timeout: 5_000 }, // Aumentar de 2s para 5s
    forbidOnly: !!process.env.CI,
    
    reporter: [
        ['dot'],
        ['json', { outputFile: 'test-results/results.json' }],
        ['html', { open: 'never' }] // Adicionar HTML report
    ],
    
    use: {
        baseURL: 'http://localhost:5173',
        trace: 'retain-on-failure', // Habilitar traces
        screenshot: 'only-on-failure',
        video: 'retain-on-failure'
    },
    
    webServer: {
        command: 'node e2e/lifecycle.js',
        url: 'http://localhost:5173',
        reuseExistingServer: true,
        timeout: 300_000,
        stdout: 'pipe',
        stderr: 'pipe',
    },
    
    projects: [{
        name: 'chromium',
        use: {
            ...devices['Desktop Chrome'],
            channel: 'chromium-headless-shell'
        }
    }],
});
```

### .gitignore (Adicionar)

```gitignore
# E2E artifacts
test-results/
playwright-report/
e2e/server.log
```

---

## 📚 Referências e Boas Práticas

### Princípios de Testes E2E

1. **Isolamento (FIRST - Isolated)**
   - Cada teste deve ser independente
   - Não compartilhar estado entre testes
   - Cleanup após execução

2. **Repetibilidade (FIRST - Repeatable)**
   - Mesmos dados de entrada = mesmos resultados
   - Não depender de ordem de execução
   - Não depender de estado externo

3. **Rapidez**
   - Minimizar navegação UI desnecessária
   - Usar fixtures via API quando possível
   - Paralelizar quando seguro

4. **Clareza**
   - Usar `test.step()` para documentar fluxo
   - Nomenclatura descritiva
   - Comentários quando necessário

### Links Úteis

- [Playwright Best Practices](https://playwright.dev/docs/best-practices)
- [Test Isolation](https://playwright.dev/docs/test-isolation)
- [Playwright Fixtures](https://playwright.dev/docs/test-fixtures)

---

## ✅ Checklist de Implementação

### Para cada arquivo de teste:

- [ ] Adicionar `test.beforeAll` com reset de banco (ou justificar ausência)
- [ ] Adicionar `test.afterEach` com cleanup de dados criados
- [ ] Remover dependências sequenciais entre testes
- [ ] Usar `test.step()` para fluxos com múltiplas etapas
- [ ] Validar que testes passam isoladamente
- [ ] Validar que testes passam em qualquer ordem

### Para o backend:

- [ ] Criar endpoints de fixtures básicos
- [ ] Criar endpoints de limpeza granular
- [ ] Adicionar testes unitários para novos endpoints
- [ ] Documentar endpoints no README.md de E2E

### Para a documentação:

- [ ] Criar guia de estilo para testes E2E
- [ ] Documentar padrões de nomenclatura
- [ ] Documentar estratégias de isolamento
- [ ] Atualizar README.md de E2E

---

## 🎓 Conclusão

Os testes E2E do SGC estão funcionais mas sofrem de **problemas de isolamento e padronização** que podem levar a:

- ❌ Testes flaky (falhas intermitentes)
- ❌ Poluição de dados no banco de testes
- ❌ Dificuldade de depuração
- ❌ Impossibilidade de paralelização

Com as melhorias propostas, será possível:

- ✅ Executar testes em qualquer ordem
- ✅ Executar testes em paralelo
- ✅ Depurar falhas facilmente
- ✅ Manter a suite de testes rápida e confiável

**Recomendação:** Implementar o Plano de Ação em 3 fases, priorizando a **Fase 1** (correções urgentes) para estabilizar a base de testes.

---

**Documento elaborado por:** Copilot Agent  
**Revisão recomendada por:** Equipe de Desenvolvimento SGC
