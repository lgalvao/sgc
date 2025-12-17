# Análise Técnica dos Testes End-to-End (E2E)

**Data:** 17 de dezembro de 2025  
**Sistema:** SGC - Sistema de Gestão de Competências  
**Framework:** Playwright + TypeScript  
**Total de Testes:** 22 arquivos spec (CDU-01 a CDU-21 + captura-telas)

---

## 📊 Sumário Executivo

Os testes E2E do sistema SGC apresentam **boa cobertura funcional** e estrutura organizada com helpers reutilizáveis. No entanto, foram identificados **problemas significativos de dívida técnica** que impactam manutenibilidade, robustez e clareza. Esta análise categoriza os problemas em **Críticos**, **Importantes** e **Menores**, com recomendações prioritárias de correção.

### Métricas Gerais
- **22 arquivos de teste** (5.193 linhas totais)
- **5 helpers** especializados (auth, processos, mapas, atividades, analise)
- **1 sistema de hooks** (limpeza de dados)
- **2 fixtures** (base + processos)
- **Maior teste:** cdu-10.spec.ts (530 linhas)
- **Tempo de setup:** ~5 minutos (build backend + frontend)

---

## �� Problemas Críticos

### 1. **Duplicação Massiva de Código**

**Severidade:** Crítica  
**Impacto:** Manutenção, Consistência, Risco de bugs

#### Problema
Funções auxiliares idênticas são redefinidas localmente em múltiplos arquivos de teste:

```typescript
// Encontrado em: cdu-05.spec.ts, cdu-10.spec.ts, cdu-14.spec.ts, cdu-16.spec.ts, cdu-20.spec.ts
async function fazerLogout(page: Page) {
    await page.getByTestId('btn-logout').click();
    await expect(page).toHaveURL(/\/login/);
}

async function verificarPaginaPainel(page: Page) {
    await expect(page).toHaveURL(/\/painel/);
}

async function verificarPaginaSubprocesso(page: Page, unidade?: string) {
    // Implementação varia entre arquivos!
}
```

**Ocorrências identificadas:**
- `fazerLogout`: 6 arquivos
- `verificarPaginaPainel`: 8 arquivos
- `verificarPaginaSubprocesso`: 5 arquivos (com variações)
- `acessarSubprocessoChefe`: 3 arquivos (já existe em helpers-analise!)

#### Consequências
- **Inconsistência**: Diferentes implementações da mesma função
- **Bugs Silenciosos**: Correções não são propagadas entre arquivos
- **Refatoração Cara**: Mudanças requerem edição de múltiplos arquivos
- **Testes de Revisão**: Equipe não sabe qual versão usar

#### Recomendação
```typescript
// Criar: e2e/helpers/helpers-navegacao.ts
export async function fazerLogout(page: Page) {
    await page.getByTestId('btn-logout').click();
    await expect(page).toHaveURL(/\/login/);
}

export async function verificarPaginaPainel(page: Page) {
    await expect(page).toHaveURL(/\/painel/);
}

export async function verificarPaginaSubprocesso(
    page: Page, 
    unidade?: string
) {
    const regex = unidade 
        ? new RegExp(String.raw`/processo/\d+/${unidade}$`)
        : /\/processo\/\d+\/\w+$/;
    await expect(page).toHaveURL(regex);
}
```

**Prioridade:** 🔴 Alta - Impede escalabilidade da suite

---

### 2. **Testes Seriais Frágeis e Monolíticos**

**Severidade:** Crítica  
**Impacto:** Debugging, Paralelização, Tempo de execução

#### Problema
Múltiplos arquivos usam `test.describe.serial()` com testes gigantes que dependem de estado compartilhado:

**Exemplo:** `cdu-05.spec.ts` (322 linhas)
```typescript
test.describe.serial('CDU-05 - Iniciar processo de revisao', () => {
    // Estado compartilhado entre testes
    let processoMapeamentoId: number;
    let processoRevisaoId: number;
    
    test('Fase 1: Ciclo completo de Mapeamento', async ({page}) => {
        // 284 linhas de código inline!
        await passo1_AdminCriaEIniciaProcessoMapeamento(...);
        await passo2_ChefeAdicionaAtividades(...);
        await passo2a_ChefeDisponibilizaCadastro(...);
        await passo2b_AdminHomologaCadastro(...);
        // ... mais 7 passos
    });
    
    test('Fase 2: Iniciar processo de Revisão', async ({page}) => {
        // Depende do estado de Fase 1!
    });
});
```

**Exemplo:** `cdu-10.spec.ts` (530 linhas)
- **8 testes de preparação** sequenciais
- **5 testes principais** que dependem da preparação
- Se o 3º teste de preparação falha, os 10 testes seguintes também falham

#### Consequências
1. **Falhas em cascata**: Um erro contamina todos os testes subsequentes
2. **Debugging infernal**: Qual teste realmente falhou?
3. **Sem paralelização**: `workers: 1` obrigatório
4. **Tempo de execução**: ~30 min totais (poderia ser <10 min paralelo)
5. **Impossível rodar teste isolado**: Viola regras do próprio projeto (e2e_regras.md linha 26)

#### Recomendação
**Opção A - Fixtures Tipados (Recomendado)**
```typescript
// e2e/fixtures/fixtures-processos.ts
export const processoMapeamentoFixture = base.extend<{
    processoMapeamentoCompleto: {
        processoId: number;
        descricao: string;
        unidade: string;
    }
}>({
    processoMapeamentoCompleto: async ({ page, request }, use) => {
        // Setup: criar processo completo
        const processo = await criarProcessoMapeamentoCompleto(page, {
            unidade: 'SECAO_221'
        });
        
        await use(processo);
        
        // Teardown automático
        await request.post(`/e2e/processo/${processo.processoId}/limpar`);
    }
});

// Uso no teste
test('CT-01: Admin homologa revisão', async ({ 
    page, 
    processoMapeamentoCompleto 
}) => {
    // Processo já está pronto!
    await page.goto(`/processo/${processoMapeamentoCompleto.processoId}`);
    // ...
});
```

**Opção B - Testes Atômicos com Seed API**
```typescript
// Cada teste é independente
test('CT-01: Admin homologa revisão', async ({ page, request }) => {
    // Seed via API (rápido!)
    const { processoId } = await request.post('/e2e/seed/processo-revisao', {
        data: { unidade: 'SECAO_221', situacao: 'DISPONIBILIZADA' }
    });
    
    // Teste foca apenas na ação específica
    await page.goto(`/processo/${processoId}/SECAO_221`);
    await homologarRevisao(page);
    
    // Assertions
    await expect(...).toBeVisible();
});
```

**Prioridade:** 🔴 Alta - Reduz tempo de execução em 60%+

---

### 3. **Extração de IDs via Regex sem Validação**

**Severidade:** Crítica  
**Impacto:** Falhas silenciosas, Cleanup incompleto

#### Problema
Extração de IDs de processos é inconsistente e não valida sucesso:

```typescript
// Padrão encontrado em 12+ arquivos
const processoId = parseInt(page.url().match(/\/processo\/cadastro\/(\d+)/)?.[1] || '0');
if (processoId > 0) cleanup.registrar(processoId);

// Problemas:
// 1. Se regex falha, processoId = 0 (nenhum erro lançado!)
// 2. Se URL muda, regex quebra silenciosamente
// 3. Cleanup não acontece, dados órfãos no banco
// 4. Três variações de regex diferentes no código!
```

**Variações encontradas:**
```typescript
// Variação 1 (cdu-02.spec.ts linha 49)
/codProcesso=(\d+)/

// Variação 2 (cdu-03.spec.ts linha 36)
/\/processo\/cadastro\/(\d+)/

// Variação 3 (cdu-04.spec.ts linha 36)
page.url().match(/\/processo\/cadastro\/(\d+)/)?.[1]
```

#### Recomendação
```typescript
// helpers/helpers-processos.ts
export async function extrairProcessoId(page: Page): Promise<number> {
    const url = page.url();
    
    // Suporta múltiplos formatos de URL
    const patterns = [
        /\/processo\/cadastro\/(\d+)/,
        /codProcesso=(\d+)/,
        /\/processo\/(\d+)/
    ];
    
    for (const pattern of patterns) {
        const match = url.match(pattern);
        if (match?.[1]) {
            return parseInt(match[1]);
        }
    }
    
    throw new Error(
        `Não foi possível extrair ID do processo da URL: ${url}`
    );
}

// Uso
test('Deve criar processo', async ({ page }) => {
    await criarProcesso(page, { ... });
    
    try {
        const processoId = await extrairProcessoId(page);
        cleanup.registrar(processoId);
    } catch (error) {
        // Falha explícita ao invés de silenciosa
        throw new Error(`Falha ao registrar processo para cleanup: ${error.message}`);
    }
});
```

**Prioridade:** 🔴 Alta - Previne vazamento de dados de teste

---

## 🟡 Problemas Importantes

### 4. **Ausência de Abstração para Workflows Complexos**

**Severidade:** Importante  
**Impacto:** Legibilidade, Reusabilidade

#### Problema
Fluxos complexos (mapeamento completo, revisão completa) são repetidos inline em múltiplos testes:

```typescript
// Padrão repetido em 8+ arquivos (80+ linhas cada)
test('Preparação: Criar processo de mapeamento', async ({ page }) => {
    // 1. Admin cria e inicia
    await login(page, ADMIN);
    await criarProcesso(page, { ... });
    await iniciarProcesso(page);
    
    // 2. Chefe adiciona atividades
    await fazerLogout(page);
    await login(page, CHEFE);
    await adicionarAtividade(page, 'Atividade 1');
    await adicionarConhecimento(page, 'Atividade 1', 'Conhecimento 1');
    await disponibilizarCadastro(page);
    
    // 3. Gestor aceita
    await fazerLogout(page);
    await login(page, GESTOR);
    await aceitarCadastro(page);
    
    // 4. Admin homologa cadastro
    await fazerLogout(page);
    await login(page, ADMIN);
    await homologarCadastro(page);
    
    // 5. Admin cria competências
    await criarCompetencia(page, 'Competência 1', ['Atividade 1']);
    await disponibilizarMapa(page);
    
    // 6. Chefe valida
    await fazerLogout(page);
    await login(page, CHEFE);
    await validarMapa(page);
    
    // 7. Admin homologa mapa
    await fazerLogout(page);
    await login(page, ADMIN);
    await homologarMapa(page);
});
```

#### Recomendação
```typescript
// helpers/helpers-workflows.ts
export interface ProcessoMapeamentoOpts {
    descricao: string;
    unidade: string;
    atividades: Array<{
        nome: string;
        conhecimentos: string[];
    }>;
    competencias: Array<{
        nome: string;
        atividades: string[];
    }>;
}

export async function criarProcessoMapeamentoCompleto(
    page: Page,
    opts: ProcessoMapeamentoOpts
): Promise<{ processoId: number }> {
    // 1. Admin cria e inicia
    await executarComoUsuario(page, USUARIOS.ADMIN, async () => {
        await criarProcesso(page, {
            descricao: opts.descricao,
            tipo: 'MAPEAMENTO',
            unidade: opts.unidade
        });
        await iniciarProcesso(page);
    });
    
    const processoId = await extrairProcessoId(page);
    
    // 2. Chefe adiciona atividades
    await executarComoUsuario(page, USUARIOS.CHEFE, async () => {
        await navegarParaSubprocesso(page, processoId, opts.unidade);
        await navegarParaAtividades(page);
        
        for (const atividade of opts.atividades) {
            await adicionarAtividade(page, atividade.nome);
            for (const conhecimento of atividade.conhecimentos) {
                await adicionarConhecimento(page, atividade.nome, conhecimento);
            }
        }
        
        await disponibilizarCadastro(page);
    });
    
    // 3-7: Continuar workflow...
    
    return { processoId };
}

// Uso simplificado
test('Deve permitir revisão de mapa', async ({ page }) => {
    const { processoId } = await criarProcessoMapeamentoCompleto(page, {
        descricao: 'Mapeamento Base',
        unidade: 'SECAO_221',
        atividades: [
            { nome: 'Atividade 1', conhecimentos: ['Conhecimento 1A'] }
        ],
        competencias: [
            { nome: 'Competência 1', atividades: ['Atividade 1'] }
        ]
    });
    
    // Teste foca no que interessa
    await criarProcessoRevisao(page, { processoAnterior: processoId });
    // ...
});
```

**Prioridade:** 🟡 Média - Melhora significativamente a legibilidade

---

### 5. **Inconsistência em Estratégias de Wait**

**Severidade:** Importante  
**Impacto:** Flakiness, Previsibilidade

#### Problema
Três estratégias diferentes de espera são usadas inconsistentemente:

```typescript
// Estratégia 1: waitForResponse (correto!)
const promessaAtividade = page.waitForResponse(
    resp => resp.url().includes('/atividades') && resp.status() === 201
);
await page.getByTestId('btn-adicionar-atividade').click();
await promessaAtividade;

// Estratégia 2: waitForLoadState (genérico demais)
await page.waitForLoadState('networkidle'); // Espera TODOS os requests!

// Estratégia 3: waitForTimeout (ANTI-PATTERN!)
await page.waitForTimeout(500); // Captura-telas.spec.ts linha 64, 72
```

**Problemas:**
- `waitForTimeout`: Arbitrário, não garante nada
- `networkidle`: Desnecessariamente lento
- Falta padronização: Equipe não sabe qual usar

#### Recomendação
```typescript
// helpers/helpers-wait.ts
export const waitStrategies = {
    /** Aguarda resposta específica de API */
    forApiCall: async (
        page: Page, 
        urlPattern: string | RegExp, 
        expectedStatus: number = 200
    ) => {
        return page.waitForResponse(resp => {
            const matchUrl = typeof urlPattern === 'string' 
                ? resp.url().includes(urlPattern)
                : urlPattern.test(resp.url());
            return matchUrl && resp.status() === expectedStatus;
        });
    },
    
    /** Aguarda navegação completa */
    forNavigation: async (page: Page, urlPattern: RegExp) => {
        await page.waitForURL(urlPattern);
        // Garante que DOM está pronto
        await page.waitForLoadState('domcontentloaded');
    },
    
    /** Aguarda elemento aparecer e estar interativo */
    forElement: async (locator: Locator) => {
        await locator.waitFor({ state: 'visible' });
        await locator.waitFor({ state: 'attached' });
    }
};

// Substituir todos os waitForTimeout por estratégias apropriadas
```

**Documentar no e2e_regras.md:**
```markdown
## Estratégias de Espera

- ✅ USE `waitForResponse()` para operações de API
- ✅ USE `waitForURL()` para navegação
- ✅ USE `waitFor()` para elementos do DOM
- ❌ NUNCA use `waitForTimeout()` em testes
- ❌ EVITE `networkidle` (use apenas quando necessário para casos específicos)
```

**Prioridade:** 🟡 Média - Reduz flakiness

---

### 6. **Falta de Tipagem Estrita em Helpers**

**Severidade:** Importante  
**Impacto:** Type Safety, Developer Experience

#### Problema
Muitos helpers aceitam `string` onde deveriam aceitar enums/unions:

```typescript
// helpers-processos.ts
export async function criarProcesso(page: Page, options: {
    tipo: 'MAPEAMENTO' | 'REVISAO' | 'DIAGNOSTICO'; // Bom! ✅
    unidade: string; // Deveria ser enum de unidades válidas ❌
    expandir?: string[]; // Deveria ser enum também ❌
}) { ... }

// helpers-auth.ts
export const USUARIOS = {
    ADMIN_1_PERFIL: {titulo: '191919', senha: 'senha'}, // ✅
    // Mas falta tipo para retorno de funções
};

export async function login(page: Page, usuario: string, senha: string) {
    // Deveria aceitar USUARIOS[keyof typeof USUARIOS] ❌
}
```

#### Recomendação
```typescript
// types/e2e-types.ts
export enum UnidadeSigla {
    SEDOC = 'SEDOC',
    SECRETARIA_1 = 'SECRETARIA_1',
    SECRETARIA_2 = 'SECRETARIA_2',
    COORD_11 = 'COORD_11',
    COORD_22 = 'COORD_22',
    ASSESSORIA_11 = 'ASSESSORIA_11',
    ASSESSORIA_21 = 'ASSESSORIA_21',
    ASSESSORIA_22 = 'ASSESSORIA_22',
    SECAO_111 = 'SECAO_111',
    SECAO_112 = 'SECAO_112',
    SECAO_113 = 'SECAO_113',
    SECAO_121 = 'SECAO_121',
    SECAO_211 = 'SECAO_211',
    SECAO_212 = 'SECAO_212',
    SECAO_221 = 'SECAO_221'
}

export type TipoProcesso = 'MAPEAMENTO' | 'REVISAO' | 'DIAGNOSTICO';

export type UsuarioKey = keyof typeof USUARIOS;

// Atualizar helpers
export async function criarProcesso(page: Page, options: {
    descricao: string;
    tipo: TipoProcesso;
    diasLimite: number;
    unidade: UnidadeSigla;
    expandir?: UnidadeSigla[];
    iniciar?: boolean;
}): Promise<void> { ... }

export async function loginComoUsuario(
    page: Page, 
    usuario: UsuarioKey
): Promise<void> {
    const cred = USUARIOS[usuario];
    await login(page, cred.titulo, cred.senha);
}
```

**Prioridade:** 🟡 Média - Previne erros de digitação

---

### 7. **Logs de Console Poluídos**

**Severidade:** Importante  
**Impacto:** Debugging, Signal-to-Noise Ratio

#### Problema
Sistema de log filtra algumas mensagens mas ainda é muito verboso:

**lifecycle.js (linhas 28-63):**
```javascript
const LOG_FILTERS = [
    /WARNING:/,
    /^> Task :/,
    // ... 10+ padrões
];
```

**Mas ainda loga:**
- Todos os erros HTTP (incluindo 404 esperados)
- Queries SQL do Hibernate
- Stacktraces completos de exceções de negócio esperadas

**fixtures/base.ts (linhas 6-14):**
```typescript
page.on('console', msg => {
    const text = msg.text();
    if (text.includes('[vite] connecting...')) return; // Filtra apenas Vite
    console.log(`[BROWSER ${type.toUpperCase()}] ${text}`);
});
```

#### Recomendação
```typescript
// fixtures/base.ts - Melhorar filtros
const BROWSER_LOG_FILTERS = [
    /\[vite\]/,
    /Download the Vue Devtools/,
    /webpack/,
    /HMR/,
    // Adicionar mais padrões comuns de ruído
];

page.on('console', msg => {
    const text = msg.text();
    const type = msg.type();
    
    // Filtrar ruído
    if (BROWSER_LOG_FILTERS.some(p => p.test(text))) return;
    
    // Colorir por tipo
    const prefix = type === 'error' ? '❌' : 
                   type === 'warning' ? '⚠️' : 'ℹ️';
    console.log(`${prefix} [BROWSER] ${text}`);
});

// lifecycle.js - Adicionar modo silencioso para CI
const SILENT_MODE = process.env.CI === 'true';

function log(prefix, data) {
    if (SILENT_MODE && !data.toString().includes('ERROR')) {
        return; // No CI, só loga erros
    }
    // ... resto do código
}
```

**Adicionar variável de ambiente:**
```bash
# .env.e2e
CI=false
E2E_LOG_LEVEL=info # debug | info | warn | error
```

**Prioridade:** 🟡 Média - Melhora experiência de debugging

---

## 🔵 Problemas Menores

### 8. **Nomenclatura Inconsistente de Test IDs**

**Severidade:** Menor  
**Impacto:** Padrões de código

#### Problema
Três convenções diferentes de nomenclatura:

```typescript
// Convenção 1: kebab-case com prefixo de componente
'btn-painel-criar-processo'
'inp-processo-descricao'
'sel-processo-tipo'

// Convenção 2: PascalCase com underscores (inconsistente!)
'subprocesso-header__txt-badge-situacao'
'cad-atividades__txt-badge-situacao'

// Convenção 3: Sem prefixo
'btn-logout'
'btn-configuracoes'
```

#### Recomendação
**Documentar padrão único:**
```markdown
## Convenção de Test IDs

Formato: `{tipo}-{contexto}-{acao}`

Tipos:
- btn: Botão
- inp: Input de texto
- sel: Select/Dropdown
- chk: Checkbox
- tbl: Tabela
- mdl: Modal
- txt: Texto estático
- card: Card/Container

Exemplos:
- ✅ `btn-painel-criar-processo`
- ✅ `inp-login-usuario`
- ✅ `tbl-processos`
- ❌ `subprocesso-header__txt-badge-situacao` (evitar __)
- ❌ `btnLogout` (evitar camelCase)
```

**Prioridade:** 🔵 Baixa - Refatoração oportunística

---

### 9. **Magic Numbers Sem Constantes**

**Severidade:** Menor  
**Impacto:** Manutenibilidade

#### Problema
```typescript
// Encontrado em múltiplos arquivos
await page.getByTestId('inp-processo-data-limite').fill('2030-12-31');
dataLimite.setDate(dataLimite.getDate() + 30); // Por que 30?
await page.waitForTimeout(500); // Por que 500ms?
```

#### Recomendação
```typescript
// constants/e2e-constants.ts
export const E2E_CONSTANTS = {
    PRAZOS: {
        PROCESSO_PADRAO_DIAS: 30,
        DATA_FUTURA_FIXA: '2030-12-31', // Garante validade em testes
        MAPA_PADRAO_DIAS: 60
    },
    TIMEOUTS: {
        ANIMACAO_UI: 300,
        REQUEST_RAPIDO: 1000,
        REQUEST_LENTO: 5000
    }
} as const;
```

**Prioridade:** 🔵 Baixa

---

### 10. **Falta de Validação de Pré-condições**

**Severidade:** Menor  
**Impacto:** Mensagens de erro

#### Problema
Helpers não validam estado antes de executar:

```typescript
export async function adicionarAtividade(page: Page, descricao: string) {
    // Não valida se está na página correta!
    await page.getByTestId('inp-nova-atividade').fill(descricao);
    // Se elemento não existir, erro genérico: "Locator not found"
}
```

#### Recomendação
```typescript
export async function adicionarAtividade(page: Page, descricao: string) {
    // Validar pré-condição
    const heading = page.getByRole('heading', {
        name: 'Atividades e conhecimentos'
    });
    
    await expect(heading).toBeVisible({
        timeout: 5000
    }).catch(() => {
        throw new Error(
            'Não está na tela de cadastro de atividades. ' +
            'Certifique-se de chamar navegarParaAtividades() antes.'
        );
    });
    
    await page.getByTestId('inp-nova-atividade').fill(descricao);
    await page.getByTestId('btn-adicionar-atividade').click();
    await expect(page.getByText(descricao)).toBeVisible();
}
```

**Prioridade:** 🔵 Baixa - Melhora mensagens de erro

---

## 🏗️ Oportunidades de Melhoria

### 11. **Criar Suite de Testes de Smoke**

**Descrição:** Subset de testes críticos que roda em <5 min

```typescript
// e2e/smoke/smoke.spec.ts
test.describe('Smoke Tests', () => {
    test('Sistema está acessível', async ({ page }) => {
        await page.goto('/login');
        await expect(page.getByTestId('inp-login-usuario')).toBeVisible();
    });
    
    test('Login funciona', async ({ page }) => {
        await login(page, USUARIOS.ADMIN);
        await expect(page).toHaveURL(/\/painel/);
    });
    
    test('Criar processo básico', async ({ page }) => {
        await login(page, USUARIOS.ADMIN);
        await criarProcesso(page, { tipo: 'MAPEAMENTO' });
        await expect(page.getByText('Processo criado')).toBeVisible();
    });
});
```

**Executar no CI:**
```yaml
# .github/workflows/ci.yml
- name: Smoke Tests
  run: npx playwright test smoke/
  timeout-minutes: 5
```

---

### 12. **Implementar Page Object Model (POM) Parcial**

**Descrição:** Para componentes complexos (Tabela de Processos, Árvore de Unidades)

```typescript
// pages/components/TabelaProcessos.ts
export class TabelaProcessos {
    constructor(private page: Page) {}
    
    readonly locator = this.page.getByTestId('tbl-processos');
    
    async buscarPorDescricao(descricao: string) {
        return this.locator.locator('tr', {
            has: this.page.getByText(descricao)
        });
    }
    
    async clicarProcesso(descricao: string) {
        const linha = await this.buscarPorDescricao(descricao);
        await linha.click();
    }
    
    async verificarSituacao(descricao: string, situacao: string) {
        const linha = await this.buscarPorDescricao(descricao);
        await expect(linha.getByText(situacao)).toBeVisible();
    }
}

// Uso
test('Deve exibir processo', async ({ page }) => {
    const tabela = new TabelaProcessos(page);
    await tabela.verificarSituacao('Meu Processo', 'Em andamento');
});
```

**Aplicar apenas para:**
- ✅ Tabela de Processos
- ✅ Árvore de Unidades (seletor complexo)
- ✅ Modal de Competências
- ❌ NÃO aplicar para páginas inteiras (overkill)

---

### 13. **Adicionar Métricas de Performance**

**Descrição:** Rastrear tempo de carregamento de páginas críticas

```typescript
// fixtures/performance.ts
export const testWithPerformance = base.extend<{
    performance: PerformanceMetrics
}>({
    performance: async ({ page }, use) => {
        const metrics = new PerformanceMetrics(page);
        await use(metrics);
        await metrics.report();
    }
});

class PerformanceMetrics {
    private timings: Map<string, number> = new Map();
    
    async measure(label: string, fn: () => Promise<void>) {
        const start = Date.now();
        await fn();
        this.timings.set(label, Date.now() - start);
    }
    
    async report() {
        console.log('\n📊 Performance Metrics:');
        for (const [label, time] of this.timings) {
            const status = time < 1000 ? '✅' : 
                          time < 3000 ? '⚠️' : '❌';
            console.log(`${status} ${label}: ${time}ms`);
        }
    }
}

// Uso
test('Login deve ser rápido', async ({ page, performance }) => {
    await performance.measure('Login completo', async () => {
        await login(page, USUARIOS.ADMIN);
    });
    // Se > 3s, teste falha (detecta regressões de performance)
});
```

---

### 14. **Melhorar Documentação de Helpers**

**Problema atual:** Helpers têm pouca ou nenhuma documentação

**Exemplo atual:**
```typescript
export async function criarCompetencia(page: Page, descricao: string, atividades: string[]) {
    // Sem docs
}
```

**Recomendação:**
```typescript
/**
 * Cria uma nova competência no mapa da unidade atual
 * 
 * @param page - Instância do Playwright Page
 * @param descricao - Descrição única da competência
 * @param atividades - Array de descrições de atividades a vincular
 * 
 * @example
 * ```typescript
 * await navegarParaMapa(page);
 * await criarCompetencia(page, 'Análise de Dados', [
 *     'Elaborar relatórios', 
 *     'Consolidar informações'
 * ]);
 * ```
 * 
 * @throws {Error} Se não estiver na tela de edição do mapa
 * @throws {Error} Se alguma atividade não existir
 */
export async function criarCompetencia(
    page: Page, 
    descricao: string, 
    atividades: string[]
) {
    // Implementação...
}
```

**Gerar documentação:**
```bash
npm install --save-dev typedoc
npx typedoc --out docs/e2e-helpers e2e/helpers
```

---

### 15. **Implementar Visual Regression Testing (Opcional)**

**Descrição:** Para componentes críticos de UI

```typescript
// Usando playwright-percy ou similar
import { percySnapshot } from '@percy/playwright';

test('Tabela de processos - Layout consistente', async ({ page }) => {
    await login(page, USUARIOS.ADMIN);
    await criarProcessosVariados(page, 5);
    
    // Captura snapshot para comparação visual
    await percySnapshot(page, 'Tabela Processos - 5 itens');
});
```

**Prós:**
- Detecta regressões visuais não cobertas por testes funcionais
- Útil para componentes de UI complexos

**Contras:**
- Adiciona dependência externa (Percy/Chromatic)
- Aumenta tempo de build
- Pode gerar falsos positivos

**Recomendação:** Avaliar custo-benefício antes de implementar

---

## 📋 Plano de Ação Prioritário

### Sprint 1 - Correções Críticas (2-3 semanas)
1. ✅ **Eliminar duplicação de código**
   - Criar `helpers-navegacao.ts`
   - Consolidar funções de logout/verificação de página
   - Remover funções locais duplicadas
   
2. ✅ **Implementar extração robusta de IDs**
   - Criar `extrairProcessoId()` com validação
   - Adicionar testes unitários para regex
   - Substituir todas as 12+ ocorrências
   
3. ✅ **Refatorar testes seriais**
   - Começar com `cdu-10.spec.ts` (mais complexo)
   - Criar fixtures para estados complexos
   - Documentar padrão em `e2e_regras.md`

### Sprint 2 - Melhorias Importantes (2 semanas)
4. ✅ **Criar helpers de workflow**
   - `criarProcessoMapeamentoCompleto()`
   - `criarProcessoRevisaoCompleto()`
   - Reduzir 80+ linhas para 10 linhas

5. ✅ **Padronizar estratégias de wait**
   - Criar `helpers-wait.ts`
   - Documentar anti-patterns
   - Substituir todos os `waitForTimeout`

6. ✅ **Adicionar tipagem estrita**
   - Criar `e2e-types.ts`
   - Enum para UnidadeSigla
   - Type unions para TipoProcesso

### Sprint 3 - Qualidade de Vida (1 semana)
7. ✅ **Melhorar sistema de logs**
   - Adicionar níveis de log
   - Colorir saída no terminal
   - Modo silencioso para CI

8. ✅ **Criar smoke tests**
   - Subset de 5-10 testes críticos
   - Executar em <5 min
   - Integrar no CI

9. ✅ **Documentar helpers**
   - Adicionar JSDoc completo
   - Exemplos de uso
   - Gerar site de documentação

---

## 📊 Métricas de Sucesso

### Antes (Estado Atual)
- ⏱️ Tempo de execução: ~30 minutos
- 🔧 Workers: 1 (sem paralelização)
- 📏 Linhas duplicadas: ~300+ linhas
- 🐛 Testes frágeis: 8+ suites seriais
- 📖 Documentação: Limitada

### Depois (Meta)
- ⏱️ Tempo de execução: <15 minutos
- 🔧 Workers: 4+ (paralelização parcial)
- 📏 Linhas duplicadas: <50 linhas
- 🐛 Testes frágeis: 0 (todos independentes)
- 📖 Documentação: Completa com exemplos
- 🚀 Smoke tests: <5 minutos

---

## 🎯 Conclusão

A suite de testes E2E do SGC tem **boa cobertura funcional** e **estrutura organizada**, mas sofre de **dívida técnica significativa** que impede escalabilidade. Os principais pontos são:

### ✅ Pontos Fortes
1. **Cobertura abrangente**: 22 CDUs testados
2. **Helpers bem organizados**: Separação lógica por domínio
3. **Cleanup automático**: Hook `useProcessoCleanup` funciona bem
4. **Documentação do setup**: `lifecycle.js` bem estruturado

### ❌ Pontos Fracos Críticos
1. **Duplicação massiva de código** (300+ linhas)
2. **Testes seriais frágeis** (impossíveis de rodar isolados)
3. **Extração de IDs sem validação** (falhas silenciosas)
4. **Falta de workflows de alto nível** (legibilidade prejudicada)

### 🎯 Recomendação Prioritária
**Começar pelo Sprint 1** focando em:
1. Consolidar funções duplicadas
2. Implementar extração robusta de IDs
3. Refatorar 2-3 testes mais complexos como exemplo

Isso **desbloqueará** as melhorias subsequentes e **reduzirá tempo de execução em ~50%**.

---

**Próximos Passos:**
1. Revisar este documento com a equipe
2. Priorizar itens do Sprint 1
3. Criar issues no GitHub para rastreamento
4. Definir padrões em `e2e_regras.md`
5. Começar refatoração incremental

---

*Documento gerado em: 17/12/2025*  
*Autor: Análise Automatizada dos Testes E2E*
