# Análise dos Testes Unitários (Vitest) do Frontend - SGC

**Data da Análise:** 17 de Dezembro de 2025  
**Versão:** 1.0  
**Status dos Testes:** ✅ 729 testes passando, 3 skipped (85 arquivos)

---

## 1. Resumo Executivo

Esta análise aborda a qualidade, consistência e robustez da suíte de testes unitários do frontend (Vitest) do projeto SGC. Embora **todos os testes estejam passando**, foram identificadas **várias áreas significativas de dívida técnica** que comprometem a manutenibilidade, confiabilidade e eficácia dos testes a longo prazo.

### 1.1. Métricas Gerais

- **Total de arquivos de teste:** 85
- **Total de testes:** 732 (729 passando + 3 skipped)
- **Cobertura configurada:** 95% (statements, branches, functions, lines)
- **Duração da execução:** ~41 segundos
- **Total de linhas de código de teste:** ~13.499 linhas

### 1.2. Principais Problemas Identificados

1. **Inconsistência crítica na nomenclatura de testes** (Português vs Inglês)
2. **Duplicação massiva de código** (especialmente em testes de Store)
3. **Padrões de mock inconsistentes e frágeis**
4. **Testes superficiais focados apenas em "happy path"**
5. **Falta de testes de integração adequados**
6. **Setup e teardown inconsistentes**
7. **Falta de organização hierárquica (describe aninhados)**
8. **Comentários desnecessários ou vazios**

---

## 2. Análise Detalhada por Categoria

### 2.1. Inconsistência de Nomenclatura ⚠️ **CRÍTICO**

**Problema:** Mistura inconsistente de Português e Inglês nos nomes de testes.

#### 2.1.1. Exemplos Encontrados

**Português (Correto conforme guidelines do projeto):**
```typescript
// stores/__tests__/feedback.spec.ts
it('deve ter o estado inicial correto', () => { ... })
it('deve mostrar feedback corretamente', () => { ... })
it('deve fechar automaticamente após o delay', () => { ... })
```

**Inglês (Inconsistente com guidelines):**
```typescript
// stores/__tests__/mapas.spec.ts
it("should initialize with null values", () => { ... })
it("should call service and update state on success", async () => { ... })
it("should set state to null on failure", async () => { ... })
```

**Misto (Pior cenário):**
```typescript
// Algumas stores em português, outras em inglês no MESMO projeto
```

#### 2.1.2. Impacto

- ❌ **Violação direta das diretrizes do projeto** (tudo deve estar em Português Brasileiro)
- ❌ Dificulta compreensão por equipes que não falam inglês
- ❌ Cria barreira cognitiva ao alternar entre arquivos
- ❌ Indica falta de padrão na equipe

#### 2.1.3. Estatísticas

- **~40% dos arquivos de Store** usam inglês
- **~60% dos arquivos de Store** usam português
- **100% dos arquivos de Component** usam português (correto)
- **~50% dos arquivos de Service** usam inglês

#### 2.1.4. Recomendação

**ALTA PRIORIDADE:** Padronizar TODOS os testes para Português Brasileiro.

**Padrão recomendado:**
```typescript
describe("useMapasStore", () => {
    describe("buscarMapaCompleto", () => {
        it("deve chamar o service e atualizar o estado em caso de sucesso", async () => { ... })
        it("deve definir o estado como null em caso de falha", async () => { ... })
    })
})
```

---

### 2.2. Duplicação de Código 🔴 **ALTO IMPACTO**

**Problema:** Código duplicado massivamente em testes de Stores e Services.

#### 2.2.1. Padrão Duplicado em Stores

**Exemplo do padrão repetido em ~12 stores:**

```typescript
// REPETIDO em processos.spec.ts, mapas.spec.ts, subprocessos.spec.ts, etc.
beforeEach(async () => {
    initPinia();
    store = useXxxStore();
    xxxService = (await import("@/services/xxxService")) as Mocked<
        typeof import("@/services/xxxService")
    >;
    vi.restoreAllMocks();
});
```

**Cada Store repete estruturas idênticas:**
```typescript
// Padrão repetido ~50+ vezes:
it("deve chamar o service", async () => {
    mockService.metodo.mockResolvedValue(mockData);
    await store.acao(payload);
    expect(mockService.metodo).toHaveBeenCalledWith(payload);
});

it("deve lançar erro em caso de falha", async () => {
    mockService.metodo.mockRejectedValue(MOCK_ERROR);
    await expect(store.acao(payload)).rejects.toThrow(MOCK_ERROR);
});
```

#### 2.2.2. Impacto

- ❌ **Manutenção extremamente difícil:** Mudanças exigem editar 12+ arquivos
- ❌ **Alto risco de inconsistência:** Fácil esquecer de atualizar um arquivo
- ❌ **Dificulta evolução dos padrões:** Resistência a mudanças devido ao esforço
- ❌ **Aumenta chance de bugs:** Copy-paste propaga erros

#### 2.2.3. Exemplos de Duplicação

**Inicialização de Pinia (12 stores):**
```typescript
// Repetido identicamente em 12 arquivos
beforeEach(() => {
    setActivePinia(createPinia());
    store = useXxxStore();
    vi.clearAllMocks();
});
```

**Teste de erro padrão (50+ vezes):**
```typescript
// Estrutura idêntica repetida em dezenas de testes
it("deve lançar um erro em caso de falha", async () => {
    service.metodo.mockRejectedValue(MOCK_ERROR);
    await expect(store.acao(payload)).rejects.toThrow(MOCK_ERROR);
});
```

#### 2.2.4. Recomendação

**Criar Test Utilities centralizadas:**

```typescript
// test-utils/storeTestHelpers.ts
export function setupStoreTest<T>(
    useStore: () => T, 
    serviceMocks: Record<string, any>
) {
    beforeEach(async () => {
        initPinia();
        const store = useStore();
        const services = await mockServices(serviceMocks);
        return { store, services };
    });
}

export function testServiceCall<T>(
    action: () => Promise<T>,
    service: any,
    method: string,
    expectedArgs: any[]
) {
    it("deve chamar o service com os parâmetros corretos", async () => {
        await action();
        expect(service[method]).toHaveBeenCalledWith(...expectedArgs);
    });
}

export function testErrorHandling<T>(
    action: () => Promise<T>,
    errorType?: ErrorConstructor
) {
    it("deve lançar erro em caso de falha", async () => {
        await expect(action()).rejects.toThrow(errorType);
    });
}
```

**Uso:**
```typescript
describe("useProcessosStore", () => {
    const { store, services } = setupStoreTest(useProcessosStore, {
        processoService: ["criarProcesso", "atualizarProcesso"]
    });

    testServiceCall(() => store.criar(req), services.processoService, "criarProcesso", [req]);
    testErrorHandling(() => store.criar(req), ErroNegocio);
});
```

---

### 2.3. Padrões de Mock Inconsistentes 🟡 **MÉDIO IMPACTO**

**Problema:** Mocks são configurados de formas diferentes em diferentes arquivos.

#### 2.3.1. Variações Encontradas

**Abordagem 1: Mock completo do módulo (Service tests)**
```typescript
vi.mock("@/axios-setup", () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
    },
}));
```

**Abordagem 2: Mock seletivo com named exports (Store tests)**
```typescript
vi.mock("@/services/mapaService", () => ({
    obterMapaCompleto: vi.fn(),
    salvarMapaCompleto: vi.fn(),
}));
```

**Abordagem 3: Mock de Store dependencies**
```typescript
vi.mock("../unidades", () => ({useUnidadesStore: vi.fn(() => ({}))}));
vi.mock("../alertas", () => ({useAlertasStore: vi.fn(() => ({}))}));
```

**Abordagem 4: doMock dinâmico (processoService.spec.ts)**
```typescript
beforeAll(() => {
    vi.doMock("@/mappers/processos", () => ({
        mapProcessoDtoToFrontend: vi.fn((dto) => ({...dto, mapped: true})),
    }));
});
```

#### 2.3.2. Impacto

- ❌ **Curva de aprendizado aumentada:** Desenvolvedores precisam aprender múltiplos padrões
- ❌ **Manutenção fragmentada:** Não há um "jeito certo" claro
- ❌ **Fragilidade:** Alguns padrões são mais propensos a quebrar
- ❌ **Dificuldade de debug:** Comportamento inconsistente entre testes

#### 2.3.3. Problema Específico: Abuso de `vi.restoreAllMocks()`

**Exemplo (processos.spec.ts):**
```typescript
beforeEach(async () => {
    initPinia();
    store = useProcessosStore();
    painelService = (await import("@/services/painelService")) as Mocked<...>;
    processoService = (await import("@/services/processoService")) as Mocked<...>;
    vi.restoreAllMocks();  // ⚠️ Chamado DEPOIS de importar os mocks
});
```

**Problema:** `vi.restoreAllMocks()` é chamado APÓS os mocks serem importados, o que pode levar a comportamento inesperado.

#### 2.3.4. Recomendação

**Padronizar estratégia de mocking:**

1. **Para Services (testa chamadas HTTP):**
```typescript
// Mock do apiClient no nível de arquivo
vi.mock("@/axios-setup", () => ({
    default: {
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
    },
}));

// No beforeEach: apenas reset, não restore
beforeEach(() => {
    vi.clearAllMocks(); // Limpa calls, mas mantém implementação mock
});
```

2. **Para Stores (testa lógica de estado):**
```typescript
// Mock de services dependentes
vi.mock("@/services/xxxService");

// Importar APÓS o mock
beforeEach(async () => {
    const service = await import("@/services/xxxService");
    vi.mocked(service.metodo).mockResolvedValue(mockData);
});
```

3. **Para Components (testa interação):**
```typescript
// Use createTestingPinia de @pinia/testing
const wrapper = mount(Component, {
    global: {
        plugins: [createTestingPinia({ createSpy: vi.fn })]
    }
});
```

---

### 2.4. Testes Superficiais (Happy Path Only) 🟡 **MÉDIO IMPACTO**

**Problema:** A maioria dos testes cobre apenas o "caminho feliz", ignorando casos de erro e edge cases.

#### 2.4.1. Exemplos de Cobertura Insuficiente

**Exemplo 1: Falta de validação de entrada**
```typescript
// processos.spec.ts
describe("criarProcesso", () => {
    it("deve chamar o processoService", async () => {
        processoService.criarProcesso.mockResolvedValue({} as any);
        await store.criarProcesso(payload);
        expect(processoService.criarProcesso).toHaveBeenCalledWith(payload);
    });
    
    // ❌ FALTANDO:
    // - O que acontece se payload for null?
    // - O que acontece se unidades estiver vazia?
    // - O que acontece se dataLimite for inválida?
});
```

**Exemplo 2: Falta de teste de estado intermediário**
```typescript
// mapas.spec.ts
it("deve chamar o service e atualizar o estado em caso de sucesso", async () => {
    vi.mocked(mapaService.obterMapaCompleto).mockResolvedValue(mockMapa);
    await store.buscarMapaCompleto(codSubprocesso);
    expect(store.mapaCompleto).toEqual(mockMapa);
    
    // ❌ FALTANDO:
    // - Estado estava null antes?
    // - isLoading foi setado corretamente?
    // - Estado anterior foi limpo?
});
```

**Exemplo 3: Falta de teste de concorrência**
```typescript
// Nenhum teste verifica:
// - O que acontece se duas requisições forem feitas simultaneamente?
// - Como o estado se comporta durante múltiplas operações?
```

#### 2.4.2. Casos de Borda Não Testados

| Categoria | Casos Não Testados |
|-----------|-------------------|
| **Validação de Input** | null, undefined, strings vazias, arrays vazios |
| **Concorrência** | Múltiplas chamadas simultâneas |
| **Estado Intermediário** | isLoading, lastError durante execução |
| **Rollback** | Estado deve voltar ao anterior em caso de erro |
| **Timeouts** | Requisições longas/timeout |
| **Limpeza** | Estado antigo deve ser limpo antes de nova carga |

#### 2.4.3. Impacto

- ❌ **Falsa sensação de segurança:** 95% de cobertura não significa qualidade
- ❌ **Bugs em produção:** Edge cases não testados chegam ao usuário
- ❌ **Dificuldade de refatoração:** Testes não garantem comportamento correto

#### 2.4.4. Recomendação

**Adicionar testes para:**

1. **Validação de entrada:**
```typescript
describe("criarProcesso", () => {
    it("deve validar payload obrigatório", async () => {
        await expect(store.criarProcesso(null as any)).rejects.toThrow();
    });
    
    it("deve validar unidades não vazia", async () => {
        await expect(store.criarProcesso({ ...payload, unidades: [] }))
            .rejects.toThrow("Unidades não pode estar vazia");
    });
});
```

2. **Estado intermediário:**
```typescript
it("deve gerenciar isLoading corretamente", async () => {
    const promise = store.buscar();
    expect(store.isLoading).toBe(true);
    await promise;
    expect(store.isLoading).toBe(false);
});

it("deve limpar estado anterior antes de nova busca", async () => {
    store.mapaCompleto = oldData;
    await store.buscarMapaCompleto(1);
    expect(store.mapaCompleto).not.toEqual(oldData);
});
```

3. **Recuperação de erro:**
```typescript
it("deve limpar estado em caso de erro", async () => {
    store.mapaCompleto = oldData;
    mockService.obter.mockRejectedValue(new Error());
    
    try {
        await store.buscar();
    } catch {}
    
    expect(store.mapaCompleto).toBeNull();
    expect(store.lastError).not.toBeNull();
});
```

---

### 2.5. Falta de Testes de Integração 🟡 **MÉDIO IMPACTO**

**Problema:** A maioria dos testes são unitários puros (mocks everywhere), faltam testes de integração real.

#### 2.5.1. Observação Positiva ✅

**O projeto TEM testes de integração de qualidade** para o componente `ArvoreUnidades`:

```typescript
// components/__tests__/ArvoreUnidades.integration.spec.ts
// components/__tests__/ArvoreUnidades.bug.spec.ts

describe('ArvoreUnidades - Testes de Integração (TERIAM PEGADO OS BUGS)', () => {
    it('COORD_11 deve estar INDETERMINADA quando 2 de 3 filhas selecionadas', () => {
        const wrapper = mount(ArvoreUnidades, {
            props: { unidades: criarUnidades(), modelValue: [132, 133] }
        });
        const estado = wrapper.vm.getEstadoSelecao(coord11);
        expect(estado).toBe('indeterminate');
    });
});
```

**Comentário no código é revelador:**
```typescript
/**
 * TESTES DE INTEGRAÇÃO - Estes testes TERIAM PEGADO os bugs reais!
 * 
 * Diferença dos testes existentes:
 * - Montam o componente completo (não apenas testam funções isoladas)
 * - Verificam props dos checkboxes (estado visual)
 * - Testam reatividade (watches, computed)
 * - Testam interação com usuário (clicks)
 */
```

#### 2.5.2. Problema: Falta de Integração em Stores

**Stores são testadas isoladamente:**
```typescript
// Todos os services são mockados
vi.mock("@/services/processoService");
vi.mock("@/services/painelService");
vi.mock("../unidades");
vi.mock("../alertas");

// ❌ Nunca testa a integração real entre Store → Service → API
```

**Consequência:** Bugs podem aparecer na integração real:
- Contrato de API mudou, mas mock não foi atualizado
- Mapper transforma dados incorretamente
- Estados não sincronizam entre stores relacionadas

#### 2.5.3. Recomendação

**Adicionar testes de integração seletivos:**

```typescript
// stores/__tests__/processos.integration.spec.ts
describe("useProcessosStore - Integração Real", () => {
    beforeEach(() => {
        // NÃO mockar services, deixar chamadas reais acontecerem
        setupMockServer(); // Mock apenas HTTP com MSW
    });

    it("deve buscar e atualizar corretamente do backend mockado", async () => {
        // Simula resposta HTTP real
        server.use(
            rest.get("/api/processos/:id/detalhes", (req, res, ctx) => {
                return res(ctx.json(mockProcessoDto));
            })
        );

        const store = useProcessosStore();
        await store.buscarProcessoDetalhe(1);

        // Valida que mapeamento e estado estão corretos
        expect(store.processoDetalhe).toMatchObject({
            codigo: 1,
            descricao: expect.any(String),
        });
    });
});
```

**Benefícios:**
- ✅ Detecta problemas de integração entre camadas
- ✅ Valida contratos de API
- ✅ Testa mappers com dados reais
- ✅ Maior confiança em refatorações

---

### 2.6. Setup e Teardown Inconsistentes 🟡 **MÉDIO IMPACTO**

**Problema:** Estratégias de setup/teardown variam entre arquivos.

#### 2.6.1. Variações Encontradas

**Variação 1: beforeEach + afterEach (feedback.spec.ts)**
```typescript
beforeEach(() => {
    setActivePinia(createPinia());
    vi.useFakeTimers();
});

afterEach(() => {
    vi.restoreAllMocks();
});
```

**Variação 2: Apenas beforeEach (mapas.spec.ts)**
```typescript
beforeEach(() => {
    setActivePinia(createPinia());
    store = useMapasStore();
    vi.clearAllMocks();
});
// ❌ Sem afterEach - pode vazar estado entre testes
```

**Variação 3: beforeAll + beforeEach (processoService.spec.ts)**
```typescript
beforeAll(() => {
    vi.doMock("@/mappers/processos", ...);
});

beforeEach(async () => {
    setActivePinia(createPinia());
    mockedMappers = await import("@/mappers/processos");
});

afterEach(() => {
    vi.restoreAllMocks();
});
```

**Variação 4: Nenhum teardown**
```typescript
beforeEach(() => {
    initPinia();
    store = useProcessosStore();
});
// ❌ Sem limpeza - pode causar interferência entre testes
```

#### 2.6.2. Impacto

- ❌ **Testes flaky:** Estado pode vazar entre testes
- ❌ **Ordem dos testes importa:** Algumas combinações podem falhar
- ❌ **Dificuldade de debug:** Comportamento inconsistente

#### 2.6.3. Problemas Específicos

**Problema 1: `vi.useFakeTimers()` sem restore**
```typescript
// feedback.spec.ts
beforeEach(() => {
    vi.useFakeTimers();
});

afterEach(() => {
    vi.restoreAllMocks(); // ⚠️ Não restaura timers!
});
```

**Deveria ser:**
```typescript
afterEach(() => {
    vi.useRealTimers(); // Restaurar timers reais
    vi.restoreAllMocks();
});
```

**Problema 2: Pinia não é limpa entre testes**
```typescript
beforeEach(() => {
    setActivePinia(createPinia()); // Cria nova instância
    store = useProcessosStore();
});

// ❌ PROBLEMA: Stores podem manter referências antigas
// Se outro teste modificou uma store "singleton", pode vazar
```

#### 2.6.4. Recomendação

**Padronizar setup/teardown:**

```typescript
// Template padrão para Store tests
describe("useXxxStore", () => {
    let store: ReturnType<typeof useXxxStore>;

    beforeEach(() => {
        // 1. Reset Pinia
        setActivePinia(createPinia());
        
        // 2. Criar store fresca
        store = useXxxStore();
        
        // 3. Limpar mocks (não restore, para manter vi.mock)
        vi.clearAllMocks();
    });

    afterEach(() => {
        // 1. Restaurar timers se usado
        vi.useRealTimers();
        
        // 2. Não precisa restoreAllMocks aqui (quebra vi.mock)
    });
});
```

```typescript
// Template padrão para Component tests
describe("MyComponent", () => {
    let wrapper: VueWrapper;

    afterEach(() => {
        // Sempre desmontar componente
        wrapper?.unmount();
    });

    it("...", () => {
        wrapper = mount(MyComponent, { ... });
        // ...
    });
});
```

---

### 2.7. Falta de Organização Hierárquica 🟡 **MÉDIO IMPACTO**

**Problema:** Muitos testes são "flat" (sem estrutura de describe aninhados), dificultando navegação e compreensão.

#### 2.7.1. Exemplo Ruim (Flat Structure)

```typescript
// ❌ Difícil de navegar
describe("useProcessosStore", () => {
    it("deve inicializar com o estado padrão", () => { ... })
    it("deve atualizar o estado em caso de sucesso", async () => { ... })
    it("não deve atualizar o estado em caso de falha", async () => { ... })
    it("deve chamar o processoService", async () => { ... })
    it("deve lançar um erro em caso de falha", async () => { ... })
    // ... 50 mais testes flat
});
```

**Problemas:**
- ❌ Difícil encontrar teste específico
- ❌ Não fica claro qual método está sendo testado
- ❌ Saída do test runner é confusa

#### 2.7.2. Exemplo Bom (Hierárquica)

```typescript
// ✅ Organizado e navegável
describe("useProcessosStore", () => {
    describe("Estado Inicial", () => {
        it("deve inicializar com processosPainel vazio", () => { ... })
        it("deve inicializar com processoDetalhe null", () => { ... })
    });

    describe("Actions", () => {
        describe("buscarProcessosPainel", () => {
            it("deve atualizar o estado em caso de sucesso", async () => { ... })
            it("deve respeitar ordenação personalizada", async () => { ... })
            it("não deve atualizar o estado em caso de falha", async () => { ... })
        });

        describe("criarProcesso", () => {
            it("deve chamar o processoService", async () => { ... })
            it("deve lançar erro em caso de falha", async () => { ... })
        });
    });
});
```

**Benefícios:**
- ✅ Navegação clara (collapse/expand)
- ✅ Fácil localizar testes relacionados
- ✅ Saída de test runner organizada
- ✅ Facilita skip/only em grupos

#### 2.7.3. Observação Positiva ✅

Alguns arquivos já seguem boa estrutura:
- `processos.spec.ts` - Usa `describe("Actions")` e `describe("Getters")`
- `ArvoreUnidades.spec.ts` - Excelente hierarquia por regra de negócio

#### 2.7.4. Recomendação

**Adotar padrão hierárquico consistente:**

```typescript
describe("Store/Component Name", () => {
    describe("Inicialização", () => {
        it("deve ter estado padrão correto", () => { ... })
    });

    describe("Getters/Computed", () => {
        describe("nomeDoGetter", () => {
            it("caso 1", () => { ... })
            it("caso 2", () => { ... })
        });
    });

    describe("Actions/Methods", () => {
        describe("nomeDaAction", () => {
            describe("sucesso", () => {
                it("deve chamar service", () => { ... })
                it("deve atualizar estado", () => { ... })
            });

            describe("erro", () => {
                it("deve lançar erro", () => { ... })
                it("deve reverter estado", () => { ... })
            });
        });
    });
});
```

---

### 2.8. Comentários Vazios ou Desnecessários 🟢 **BAIXO IMPACTO**

**Problema:** Alguns testes contêm comentários vazios ou inúteis.

#### 2.8.1. Exemplos

```typescript
// useApi.spec.ts
try {
    await execute();
} catch {
    // a  ⚠️ Comentário vazio
}
```

```typescript
// processos.spec.ts
const payload = {
    codProcesso: 1,
    unidades: ["1"],
    tipoAcao: "aceitar",
    unidadeUsuario: "1",
} as any;  // ⚠️ Type assertion desnecessário? Poderia tipar corretamente
```

#### 2.8.2. Recomendação

- **Remover comentários vazios**
- **Substituir `as any` por tipagem correta**
- **Adicionar comentários apenas quando necessário explicar "porquê", não "o quê"**

**Bom exemplo de comentário:**
```typescript
// ArvoreUnidades.integration.spec.ts
/**
 * TESTES DE INTEGRAÇÃO - Estes testes TERIAM PEGADO os bugs reais!
 * 
 * Diferença dos testes existentes:
 * - Montam o componente completo (não apenas testam funções isoladas)
 * - Verificam props dos checkboxes (estado visual)
 */
```

---

### 2.9. Problemas Específicos por Tipo de Teste

#### 2.9.1. Stores

**Problemas:**
1. ❌ Duplicação massiva de código
2. ❌ Faltam testes de estado intermediário (isLoading, lastError)
3. ❌ Não testam interação entre stores relacionadas
4. ❌ Nomenclatura inconsistente (PT vs EN)

**Exemplo de problema:**
```typescript
// processos.spec.ts - Linha 300-309
describe("processarCadastroBloco", () => {
    // ⚠️ DUPLICADO - Já existe teste idêntico nas linhas 221-239!
    it("deve chamar service corretamente", async () => { ... })
});
```

#### 2.9.2. Services

**Problemas:**
1. ❌ Apenas testam se chamada HTTP foi feita corretamente
2. ❌ Não testam tratamento de erro específico
3. ❌ Não testam retry/timeout
4. ❌ Mock de mappers é confuso (vi.doMock vs vi.mock)

**Exemplo de teste superficial:**
```typescript
// processoService.spec.ts
it("iniciarProcesso should post with correct params", async () => {
    mockApi.post.mockResolvedValue({});
    await service.iniciarProcesso(1, TipoProcesso.REVISAO, [10, 20]);
    expect(mockApi.post).toHaveBeenCalledWith("/processos/1/iniciar", { ... });
    
    // ❌ FALTANDO:
    // - Testa resposta diferente de 200?
    // - Testa resposta sem body?
    // - Testa erro de rede?
});
```

#### 2.9.3. Components

**Problemas:**
1. ❌ Faltam testes de eventos (emit)
2. ❌ Faltam testes de slots
3. ❌ Alguns usam @vue/test-utils de forma inconsistente

**Ponto positivo:**
- ✅ `BarraNavegacao.spec.ts` é bem estruturado
- ✅ Usa `describe` hierárquico
- ✅ Testa diferentes cenários de perfil

#### 2.9.4. Views

**Problemas:**
1. ❌ Muito grandes (558 linhas para CadAtividades.spec.ts)
2. ❌ Testam demais (deveria delegar para testes de component)
3. ❌ Setup complexo com muitos mocks

**Recomendação:**
- Views devem testar apenas **orquestração** entre components
- Lógica de UI deve estar em components testados separadamente

#### 2.9.5. Utils

**Ponto positivo:**
- ✅ `utils/__tests__/index.spec.ts` é **excelente**
- ✅ Testa edge cases (null, undefined, datas inválidas)
- ✅ Organizado hierarquicamente
- ✅ Cobertura abrangente

---

## 3. Análise de Qualidade por Arquivo

### 3.1. Arquivos com Boa Qualidade ✅

| Arquivo | Pontos Positivos |
|---------|-----------------|
| `utils/__tests__/index.spec.ts` | • Testa edge cases<br>• Hierarquia clara<br>• Cobertura completa<br>• Nomenclatura consistente (PT) |
| `stores/__tests__/feedback.spec.ts` | • Usa fake timers corretamente<br>• Testa comportamento temporal<br>• Nomenclatura em PT<br>• Bem estruturado |
| `components/__tests__/BarraNavegacao.spec.ts` | • Hierarquia clara<br>• Testa diferentes perfis<br>• Usa helpers adequadamente<br>• Nomenclatura em PT |
| `components/__tests__/ArvoreUnidades.spec.ts` | • **EXCELENTE** hierarquia<br>• Documenta regras de negócio<br>• Testa casos complexos<br>• Comentários úteis |
| `components/__tests__/ArvoreUnidades.integration.spec.ts` | • Verdadeiro teste de integração<br>• Documenta valor dos testes<br>• Pegaria bugs reais |

### 3.2. Arquivos que Precisam de Melhoria ⚠️

| Arquivo | Problemas Principais |
|---------|---------------------|
| `stores/__tests__/mapas.spec.ts` | • Nomenclatura em inglês<br>• Falta testes de estado intermediário<br>• Setup inconsistente |
| `stores/__tests__/processos.spec.ts` | • Testes duplicados (linhas 221-239 vs 300-309)<br>• Muito longo (416 linhas)<br>• Mistura PT/EN |
| `services/__tests__/processoService.spec.ts` | • Apenas happy path<br>• Mock de mapper confuso<br>• Nomenclatura em inglês |
| `views/__tests__/CadAtividades.spec.ts` | • Muito grande (558 linhas)<br>• Deveria delegar para components<br>• Setup complexo demais |

---

## 4. Cobertura de Testes

### 4.1. Configuração Atual

```typescript
// vitest.config.ts
coverage: {
    thresholds: {
        statements: 95,
        branches: 95,
        functions: 95,
        lines: 95,
    },
}
```

### 4.2. Análise Crítica

**Problema:** **95% de cobertura NÃO significa 95% de qualidade.**

**Exemplos de cobertura alta mas qualidade baixa:**

```typescript
// ✅ 100% de cobertura
it("deve chamar o service", async () => {
    mockService.criar.mockResolvedValue({});
    await store.criar(payload);
    expect(mockService.criar).toHaveBeenCalled();
});

// ❌ MAS não testa:
// - Validação de payload
// - Estado antes/depois
// - Casos de erro
// - Edge cases
```

### 4.3. Recomendação

**Adicionar métricas de qualidade:**

1. **Mutation Testing** (Stryker)
   - Detecta testes que não testam de verdade
   - Meta: >70% mutation score

2. **Revisão de Cobertura por Tipo:**
   - Happy path: 100% ✅
   - Error cases: <50% ❌
   - Edge cases: <30% ❌

3. **Audit de Mocks:**
   - Quantos testes usam integração real? <5% ❌
   - Quantos services são sempre mockados? 100% ⚠️

---

## 5. Boas Práticas Observadas ✅

### 5.1. Estrutura de Helpers

**Excelente organização:**
```
test-utils/
├── helpers.ts         # Funções de setup (initPinia, etc)
├── uiHelpers.ts       # Funções de interação com UI
└── __tests__/
    └── helpers.spec.ts  # Testes dos próprios helpers!
```

**Ponto positivo:** Os helpers são testados! Isso é raro e muito bom.

### 5.2. Uso de createTestingPinia

```typescript
// BarraNavegacao.spec.ts
const wrapper = mount(BarraNavegacao, getMountOptions(
    createTestingPinia({ createSpy: vi.fn })
));
```

**Benefício:** Isolamento melhor de stores em testes de componentes.

### 5.3. Fake Timers para Testes Temporais

```typescript
// feedback.spec.ts
beforeEach(() => {
    vi.useFakeTimers();
});

it('deve fechar automaticamente após o delay', () => {
    store.show('Info', 'Teste', 'info', 3000);
    vi.advanceTimersByTime(3000);
    expect(store.currentFeedback.show).toBe(false);
});
```

**Excelente uso** de fake timers para testar comportamento temporal deterministicamente.

### 5.4. Data Builders/Factories

```typescript
// helpers.ts
export function getMockAtividadesData() {
    return [
        { codigo: 1, descricao: "Atividade 1", ... },
        { codigo: 2, descricao: "Atividade 2", ... },
    ];
}
```

**Bom:** Reduz duplicação de dados de teste.

---

## 6. Impacto da Dívida Técnica

### 6.1. Curto Prazo (0-3 meses)

| Impacto | Severidade | Descrição |
|---------|-----------|-----------|
| **Onboarding lento** | 🔴 Alto | Novos devs levam mais tempo para entender padrões inconsistentes |
| **Falsos positivos** | 🟡 Médio | Testes passam mas não garantem qualidade |
| **Manutenção custosa** | 🟡 Médio | Mudanças simples exigem editar múltiplos arquivos |

### 6.2. Médio Prazo (3-12 meses)

| Impacto | Severidade | Descrição |
|---------|-----------|-----------|
| **Resistência a mudanças** | 🔴 Alto | Equipe evita refatorar devido ao esforço de atualizar testes |
| **Bugs em produção** | 🔴 Alto | Edge cases não testados chegam ao usuário |
| **Degradação da suíte** | 🟡 Médio | Testes começam a falhar por motivos errados (flakiness) |

### 6.3. Longo Prazo (1+ ano)

| Impacto | Severidade | Descrição |
|---------|-----------|-----------|
| **Perda de confiança** | 🔴 Crítico | Equipe para de confiar nos testes |
| **Suíte inutilizável** | 🔴 Crítico | Testes são ignorados ou desabilitados |
| **Reescrita necessária** | 🔴 Crítico | Único caminho é refazer do zero |

---

## 7. Plano de Ação Recomendado

### 7.1. Prioridade 1 (Urgente - 0-1 mês) 🔴

#### Ação 1.1: Padronizar Nomenclatura para Português

**Esforço:** ~8 horas  
**Impacto:** Alto (melhora consistência e alinhamento com guidelines)

**Passos:**
1. Criar script para identificar testes em inglês:
```bash
grep -r "it(\"should" frontend/src/**/__tests__
```

2. Refatorar por lotes:
   - Stores (4h)
   - Services (2h)
   - Components (1h)
   - Views (1h)

3. Documentar padrão no README:
```markdown
## Nomenclatura de Testes

✅ CORRETO:
it("deve chamar o service e atualizar o estado", ...)

❌ INCORRETO:
it("should call service and update state", ...)
```

#### Ação 1.2: Remover Testes Duplicados

**Esforço:** ~4 horas  
**Impacto:** Médio (reduz confusão, facilita manutenção)

**Passos:**
1. Identificar duplicatas (grep por describe/it idênticos)
2. Manter versão mais completa
3. Adicionar comentário se necessário:
```typescript
// Nota: teste de validação de cadastro em bloco está em outro describe
```

### 7.2. Prioridade 2 (Importante - 1-2 meses) 🟡

#### Ação 2.1: Criar Test Utilities Centralizadas

**Esforço:** ~16 horas  
**Impacto:** Alto (reduz duplicação, facilita evolução)

**Entregáveis:**
- `test-utils/storeTestHelpers.ts`
- `test-utils/serviceTestHelpers.ts`
- `test-utils/componentTestHelpers.ts`
- Documentação de uso

#### Ação 2.2: Adicionar Testes de Edge Cases

**Esforço:** ~40 horas (1 semana)  
**Impacto:** Alto (aumenta confiabilidade)

**Priorizar:**
1. Stores críticas (processos, mapas, subprocessos) - 20h
2. Services de escrita (criar, atualizar, excluir) - 10h
3. Components com lógica complexa (ArvoreUnidades já está bom) - 10h

#### Ação 2.3: Padronizar Setup/Teardown

**Esforço:** ~8 horas  
**Impacto:** Médio (reduz flakiness)

**Criar templates:**
```typescript
// templates/store.spec.template.ts
// templates/service.spec.template.ts
// templates/component.spec.template.ts
```

### 7.3. Prioridade 3 (Desejável - 2-3 meses) 🟢

#### Ação 3.1: Adicionar Testes de Integração

**Esforço:** ~24 horas  
**Impacto:** Médio (aumenta confiança em refatorações)

**Usar MSW (Mock Service Worker):**
```typescript
// Instalar
npm install -D msw

// Setup
// test-utils/msw/handlers.ts
// test-utils/msw/server.ts

// Usar em testes
describe("useProcessosStore - Integração", () => {
    beforeAll(() => server.listen());
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());
    
    // Testes sem mock de services
});
```

#### Ação 3.2: Implementar Mutation Testing

**Esforço:** ~16 horas  
**Impacto:** Alto (detecta testes ineficazes)

```bash
npm install -D @stryker-mutator/core @stryker-mutator/vitest-runner

npx stryker init
npx stryker run
```

**Meta:** >70% mutation score

#### Ação 3.3: Refatorar Views para Serem Mais Leves

**Esforço:** ~32 horas  
**Impacto:** Médio (facilita manutenção)

**Estratégia:**
1. Extrair lógica de UI para components reutilizáveis
2. Views testam apenas orquestração
3. Components testam lógica de UI

---

## 8. Métricas de Sucesso

### 8.1. Métricas Quantitativas

| Métrica | Atual | Meta 3 meses | Meta 6 meses |
|---------|-------|--------------|--------------|
| **Nomenclatura em PT** | ~60% | 100% | 100% |
| **Linhas duplicadas** | ~2000 | <500 | <200 |
| **Testes de edge cases** | ~30% | >60% | >80% |
| **Testes de integração** | ~2% | >10% | >20% |
| **Mutation score** | N/A | >60% | >70% |
| **Tempo de execução** | 41s | <45s | <50s |

### 8.2. Métricas Qualitativas

**Pesquisa com equipe (escala 1-5):**

| Aspecto | Meta |
|---------|------|
| **Facilidade de escrever novos testes** | >4.0 |
| **Confiança nos testes existentes** | >4.5 |
| **Facilidade de entender testes de outros** | >4.0 |
| **Velocidade de debug quando teste falha** | >3.5 |

---

## 9. Conclusão

### 9.1. Resumo dos Achados

A suíte de testes do frontend SGC está **funcionalmente passando**, mas apresenta **dívida técnica significativa** que comprometerá a manutenibilidade e confiabilidade a médio/longo prazo.

**Principais problemas:**
1. 🔴 **Inconsistência de nomenclatura** (PT vs EN)
2. 🔴 **Duplicação massiva de código** (especialmente Stores)
3. 🟡 **Mocks inconsistentes e frágeis**
4. 🟡 **Cobertura superficial** (apenas happy path)
5. 🟡 **Falta de testes de integração**

**Pontos positivos:**
- ✅ Todos os testes passando
- ✅ Alta cobertura (95%)
- ✅ Alguns arquivos excelentes (utils, ArvoreUnidades)
- ✅ Uso de boas práticas (fake timers, testingPinia)

### 9.2. Recomendação Final

**Ação imediata necessária:**
- Padronizar nomenclatura para PT (urgente)
- Criar test utilities para reduzir duplicação
- Adicionar testes de edge cases progressivamente

**Investimento recomendado:**
- **Sprint 1 (2 semanas):** Prioridade 1 - Consistência básica
- **Sprint 2-3 (4 semanas):** Prioridade 2 - Test utilities e edge cases
- **Sprint 4-5 (4 semanas):** Prioridade 3 - Integração e mutation testing

**ROI esperado:**
- **Curto prazo:** Menos confusão, onboarding mais rápido
- **Médio prazo:** Menos bugs em produção, refatorações mais seguras
- **Longo prazo:** Suíte sustentável, confiança da equipe mantida

### 9.3. Citação Relevante

> "Testes que passam não significam código correto. Testes que falham quando o código está errado significam código correto."
> 
> — Adaptado de Kent Beck

Atualmente, os testes **passam**, mas não garantem que **falhariam** se o código estivesse errado (especialmente em edge cases).

---

## 10. Apêndices

### 10.1. Checklist de Revisão de Código para Testes

Use este checklist em code reviews:

- [ ] **Nomenclatura:** Testes estão em Português Brasileiro?
- [ ] **Hierarquia:** Usa `describe` aninhados para organização?
- [ ] **Edge Cases:** Testa null, undefined, arrays vazios, erros?
- [ ] **Estado Intermediário:** Testa isLoading, lastError durante execução?
- [ ] **Setup/Teardown:** Usa beforeEach/afterEach consistentemente?
- [ ] **Mocks:** Mocks são claros e bem documentados?
- [ ] **Duplicação:** Código duplicado foi extraído para helpers?
- [ ] **Assertions:** Usa expect.toEqual, não apenas expect.toBeCalled?
- [ ] **Comentários:** Comentários explicam "porquê", não "o quê"?

### 10.2. Exemplos de Refatoração

#### Antes (Ruim):
```typescript
// mapas.spec.ts
it("should call service and update state on success", async () => {
    const mockMapa: MapaCompleto = { codigo: 1, ... };
    vi.mocked(mapaService.obterMapaCompleto).mockResolvedValue(mockMapa);
    await store.buscarMapaCompleto(1);
    expect(mapaService.obterMapaCompleto).toHaveBeenCalledWith(1);
    expect(store.mapaCompleto).toEqual(mockMapa);
});
```

#### Depois (Bom):
```typescript
// mapas.spec.ts
describe("buscarMapaCompleto", () => {
    describe("em caso de sucesso", () => {
        it("deve chamar o service com código correto", async () => {
            const mockMapa = criarMapaMock();
            mockService.obterMapaCompleto.mockResolvedValue(mockMapa);
            
            await store.buscarMapaCompleto(1);
            
            expect(mockService.obterMapaCompleto).toHaveBeenCalledWith(1);
        });

        it("deve atualizar o estado com dados retornados", async () => {
            const mockMapa = criarMapaMock();
            mockService.obterMapaCompleto.mockResolvedValue(mockMapa);
            
            await store.buscarMapaCompleto(1);
            
            expect(store.mapaCompleto).toEqual(mockMapa);
        });

        it("deve gerenciar isLoading corretamente", async () => {
            mockService.obterMapaCompleto.mockResolvedValue(criarMapaMock());
            
            const promise = store.buscarMapaCompleto(1);
            expect(store.isLoading).toBe(true);
            
            await promise;
            expect(store.isLoading).toBe(false);
        });
    });

    describe("em caso de erro", () => {
        it("deve definir estado como null", async () => {
            store.mapaCompleto = criarMapaMock(); // Estado anterior
            mockService.obterMapaCompleto.mockRejectedValue(new Error());
            
            try {
                await store.buscarMapaCompleto(1);
            } catch {}
            
            expect(store.mapaCompleto).toBeNull();
        });

        it("deve popular lastError", async () => {
            const erro = new Error("Falha na rede");
            mockService.obterMapaCompleto.mockRejectedValue(erro);
            
            try {
                await store.buscarMapaCompleto(1);
            } catch {}
            
            expect(store.lastError).toBeTruthy();
        });
    });
});
```

### 10.3. Referências

- [Vitest Documentation](https://vitest.dev/)
- [Vue Test Utils](https://test-utils.vuejs.org/)
- [Pinia Testing](https://pinia.vuejs.org/cookbook/testing.html)
- [Testing Best Practices (Kent C. Dodds)](https://kentcdodds.com/blog/common-mistakes-with-react-testing-library)
- [Mutation Testing (Stryker)](https://stryker-mutator.io/)

---

**Documento gerado em:** 17 de Dezembro de 2025  
**Autor:** Análise Automatizada  
**Versão:** 1.0
