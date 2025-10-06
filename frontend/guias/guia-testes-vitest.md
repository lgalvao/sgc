# Guia de Testes Unitários com Vitest e Vue Test Utils

Este guia detalha as melhores práticas e padrões para a criação de testes unitários em componentes Vue, stores Pinia e outras lógicas no contexto do sistema atual, utilizando Vitest como framework de teste e `@vue/test-utils` para componentes Vue.

## 1. Introdução

**Vitest** é um framework de teste rápido e moderno, otimizado para projetos baseados em Vite. Ele oferece uma API familiar (inspirada em Jest) e excelente integração com TypeScript.

**@vue/test-utils** é a biblioteca oficial para testar componentes Vue. Ela fornece Metodos para montar e interagir com componentes Vue de forma isolada.

## 2. Estrutura de um Teste Unitário

Um arquivo de teste unitário geralmente segue a seguinte estrutura:

```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
// Importações do componente a ser testado e suas dependências

describe('NomeDoComponente.vue', () => {
  let pinia: ReturnType<typeof createPinia>;

  beforeEach(() => {
    // Configurações que precisam ser executadas antes de cada teste
    pinia = createPinia();
    setActivePinia(pinia);
    vi.clearAllMocks(); // Limpa mocks entre os testes
    // Reset de estados de stores ou mocks específicos
  });

  it('deve renderizar corretamente', () => {
    // Lógica do teste
    const wrapper = mount(NomeDoComponente, {
      props: { /* ... */ },
      global: { /* ... */ },
    });
    expect(wrapper.exists()).toBe(true);
  });

  // Outros testes...
});
```

*   **`describe(name, fn)`**: Agrupa testes relacionados.
*   **`it(name, fn)`** (ou `test(name, fn)`): Define um caso de teste individual.
*   **`expect(value)`**: Inicia uma asserção.
*   **`vi`**: Objeto global do Vitest para mocking e spying.
*   **`beforeEach(fn)`**: Hook executado antes de cada teste dentro do `describe`. Ideal para configurar o ambiente de teste e garantir o isolamento entre os testes.

## 3. Montagem de Componentes Vue

Utilizamos a função `mount` do `@vue/test-utils` para renderizar componentes Vue.

```typescript
import { mount } from '@vue/test-utils';
import MyComponent from './MyComponent.vue';

const wrapper = mount(MyComponent, {
  props: {
    // Props passadas para o componente
    myProp: 'valor',
  },
  global: {
    // Configurações globais para o ambiente de teste
    plugins: [pinia, router], // Plugins como Pinia e Vue Router
    stubs: {
      // Substitui componentes filhos por stubs (componentes vazios ou mocks)
      RouterLink: {
        template: '<a :href="resolvedTo"><slot /></a>',
        props: ['to'],
        setup(props) {
          const router = useRouter();
          const resolvedTo = computed(() => {
            if (typeof props.to === 'string') {
              return props.to;
            }
            const resolved = router.resolve(props.to);
            return resolved.fullPath;
          });
          return { resolvedTo };
        },
      },
      // Outros componentes que não precisam ser testados em detalhes
      // 'AnotherComponent': true, // Renderiza um stub básico
    },
    // provide: { /* ... */ }, // Valores fornecidos via provide/inject
  },
});

// Aguardar a próxima atualização do DOM para garantir que o componente reagiu a mudanças
await wrapper.vm.$nextTick();
```

*   **`props`**: Objeto para passar props para o componente.
*   **`global.plugins`**: Permite instalar plugins Vue (como Pinia e Vue Router) no ambiente de teste.
*   **`global.stubs`**: Essencial para isolar o componente que está sendo testado. Substitui componentes filhos por versões simplificadas ou mocks, evitando que seus comportamentos interfiram no teste do componente pai.
*   **`wrapper.vm.$nextTick()`**: Garante que o DOM foi atualizado após mudanças reativas no componente.

## 4. Mocks e Spies

Mocks e Spies são cruciais para isolar a unidade de código que está sendo testada, controlando o comportamento de suas dependências.

### 4.1. `vi.mock` para Módulos

Usado para substituir módulos inteiros (como stores Pinia, composables, serviços).

```typescript
// Exemplo de mock de store Pinia
import { useUnidadesStore } from '@/stores/unidades';
const mockUnidadesStore = {
  pesquisarUnidade: vi.fn(), // Mock de uma função da store
  // Outras propriedades ou Metodos da store
};
vi.mock('@/stores/unidades', () => ({
  useUnidadesStore: () => mockUnidadesStore,
}));

// Exemplo de mock de composable
import { usePerfil } from '@/composables/usePerfil';
import { ref } from 'vue';
const mockServidorLogadoRef = ref({});
vi.mock('@/composables/usePerfil', () => ({
  usePerfil: () => ({
    servidorLogado: mockServidorLogadoRef,
    perfilSelecionado: ref(''),
    // ...
  }),
}));

// Mock parcial de módulo (mantendo partes originais)
vi.mock('@/stores/revisao', async (importOriginal) => {
  const mod = await importOriginal(); // Importa o módulo original
  return {
    ...mod, // Mantém todas as exportações originais
    useRevisaoStore: () => mockRevisaoStore, // Sobrescreve apenas a store
  };
});
```

*   **Importante**: As definições dos objetos mock (ex: `mockUnidadesStore`) devem vir *antes* dos blocos `vi.mock` para evitar `ReferenceError`, pois `vi.mock` é içado para o topo do arquivo.
*   Use `ref` em mocks de composables para simular reatividade.

### 4.2. `vi.spyOn` para Metodos

Monitora chamadas a Metodos existentes sem alterar sua implementação original, ou permite alterar temporariamente a implementação.

```typescript
import { useRouter } from 'vue-router';
// ...
const router = useRouter();
const pushSpy = vi.spyOn(router, 'push'); // Espiona o Metodo 'push' do router

// No teste, você pode verificar se foi chamado:
expect(pushSpy).toHaveBeenCalledWith('/painel');
```

### 4.3. Controle de Mocks

*   **`mockImplementation(fn)`**: Define uma nova implementação para a função mockada.
*   **`mockReturnValue(value)`**: Faz com que a função mockada retorne um valor específico.
*   **`mockClear()`**: Limpa o histórico de chamadas de um mock/spy. Use em `beforeEach` para garantir testes isolados.

### 4.4. Mocks de Dados

Crie objetos de dados mockados para simular respostas de APIs ou estados de stores, garantindo que seus testes sejam previsíveis e independentes de dados reais.

```typescript
const mockUnidade: Unidade = {
  id: 1,
  sigla: 'UNID1',
  nome: 'Unidade 1',
} as Unidade; // Use type assertion para tipagem
```

## 5. Interação com Componentes

O `@vue/test-utils` oferece Metodos para encontrar elementos no DOM do componente e simular interações do usuário.

```typescript
// Encontrar elementos
const button = wrapper.find('button.btn-primary'); // Por seletor CSS
const input = wrapper.find('[data-testid="my-input"]'); // Por data-testid (recomendado)
const allItems = wrapper.findAll('.list-item'); // Encontrar múltiplos elementos

// Simular eventos
await button.trigger('click'); // Simula um clique
await input.setValue('novo valor'); // Define o valor de um input

// Acessar a instância do componente
const vm = wrapper.vm as MyComponentInstance; // Para acessar Metodos ou computed properties
expect(vm.myComputedProperty).toBe('expected');
```

*   **`data-testid`**: É a forma **recomendada** de selecionar elementos para testes, pois é menos suscetível a mudanças na estrutura ou estilo do HTML.

## 6. Asserções Comuns

Vitest oferece uma rica API de asserções.

```typescript
expect(wrapper.exists()).toBe(true); // Verifica se o componente existe
expect(wrapper.find('h1').text()).toBe('Título'); // Verifica o texto de um elemento
expect(wrapper.find('.error').exists()).toBe(false); // Verifica a não existência
expect(wrapper.findAll('li')).toHaveLength(3); // Verifica o número de elementos
expect(wrapper.html()).toContain('Texto esperado'); // Verifica se o HTML contém uma string

// Para mocks/spies
expect(myMockFunction).toHaveBeenCalled(); // Verifica se a função foi chamada
expect(myMockFunction).toHaveBeenCalledWith('arg1', 123); // Verifica argumentos
expect(myMockFunction).toHaveBeenCalledTimes(2); // Verifica quantas vezes foi chamada

// Para objetos
expect(myObject).toEqual({ a: 1, b: 2 }); // Comparação profunda de objetos
expect(myObject).objectContaining({ a: 1 }); // Verifica se contém certas propriedades

// Para eventos emitidos
expect(wrapper.emitted('my-event')).toHaveLength(1); // Verifica se o evento foi emitido
expect(wrapper.emitted('my-event')[0]).toEqual(['payload']); // Verifica o payload
```

## 7. Testes Assíncronos

Muitas operações em aplicações Vue são assíncronas (chamadas de API, navegação do router, atualizações reativas).

```typescript
// Navegação do Vue Router
await router.push('/minha-rota');
await router.isReady(); // Aguarda o router resolver a rota

// Atualizações reativas do Vue
await wrapper.vm.$nextTick();

// Atrasos simulados (raramente necessário, mas útil para edge cases de reatividade)
await new Promise(resolve => setTimeout(resolve, 10));
```

## 8. Testando Vue Router

Ao testar componentes que interagem com o Vue Router, é necessário configurar uma instância do router no ambiente de teste.

```typescript
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  { path: '/painel', name: 'Painel', component: { template: '<div>Painel</div>' } },
  { path: '/login', name: 'Login', component: { template: '<div>Login</div>' } },
  // ... outras rotas mockadas
];

let router: ReturnType<typeof createRouter>;

beforeEach(() => {
  router = createRouter({
    history: createWebHistory(),
    routes,
  });
});

// No teste
await router.push('/painel');
await router.isReady();
```

## 9. Testes Parametrizados (`it.each`)

Útil para testar o mesmo cenário com diferentes conjuntos de dados.

```typescript
it.each([
  ['success', 'notification-success', 'bi-check-circle-fill text-success'],
  ['error', 'notification-error', 'bi-exclamation-triangle-fill text-danger'],
])('should render %s notification with correct classes and icon', async (type, cssClass, iconClass) => {
  // Lógica do teste usando type, cssClass, iconClass
});
```

## 10. Testando Funções Utilitárias Puras

Funções que não dependem do DOM ou do estado de componentes podem ser testadas diretamente, sem a necessidade de montar componentes.

```typescript
import { iconeTipo } from '@/utils'; // Sua função utilitária

describe('Funções Utilitárias', () => {
  it('should return correct icon classes for each type', () => {
    expect(iconeTipo('success')).toBe('bi bi-check-circle-fill text-success');
    expect(iconeTipo('error')).toBe('bi bi-exclamation-triangle-fill text-danger');
    // ...
  });
});
```

## 11. Boas Práticas

*   **Isolamento**: Cada teste deve ser independente dos outros. Use `beforeEach` para resetar estados e mocks.
*   **`data-testid`**: Use atributos `data-testid` em seus componentes para selecionar elementos de forma robusta nos testes, em vez de seletores CSS que podem mudar.
*   **Clareza**: Escreva descrições de `describe` e `it` que sejam claras e concisas, explicando o que está sendo testado.
*   **Testar Comportamento, Não Implementação**: Foque em testar o que o componente *faz* (saída, eventos emitidos, mudanças de estado visíveis), não *como* ele faz (detalhes internos de implementação).
*   **Cobertura de Código**: Busque uma boa cobertura de código, mas não como um fim em si. Testes bem escritos são mais importantes do que apenas atingir 100% de cobertura.
*   **Mantenha os Mocks Simples**: Mocks complexos podem ser difíceis de manter e podem introduzir bugs nos próprios testes. Mantenha-os o mais simples possível.
*   **Testes de Snapshot (Opcional)**: Para componentes com UI complexa e estável, testes de snapshot podem ser úteis para garantir que a renderização não mude inesperadamente. No entanto, podem ser frágeis e exigir manutenção frequente.

Este guia serve como um ponto de partida para escrever testes unitários eficazes e manuteníveis no projeto. Ao seguir estas diretrizes, garantimos a qualidade e a estabilidade do nosso código.
