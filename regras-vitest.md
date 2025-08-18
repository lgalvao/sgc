# Regras e Boas Práticas para Testes Unitários com Vitest

Este documento sumariza os aprendizados e as boas práticas observadas durante a refatoração e depuração dos testes unitários do sistema, com foco em componentes Vue 3, Vue Router e Pinia.

## 1. Estrutura de Testes

Os testes são organizados usando blocos `describe` para agrupar testes relacionados a uma funcionalidade ou componente específico, e blocos `it` para casos de teste individuais. O `beforeEach` é amplamente utilizado para configurar o ambiente de teste antes de cada teste, garantindo isolamento e consistência.

## 2. Mocks e Stubs

### 2.1. Vue Router
*   **Uso de Instância Real:** Para testes de navegação e interação com rotas, é preferível usar uma instância real do `vue-router` (`createRouter`, `createWebHistory`). Isso garante que o comportamento de roteamento seja o mais próximo possível do ambiente de produção.
*   **Espionagem de Métodos:** Utilize `vi.spyOn(router, 'push')` para verificar se as navegações foram chamadas corretamente. Lembre-se de limpar o spy (`pushSpy.mockClear()`) após a navegação inicial do teste e antes da ação que você realmente quer testar.
*   **`router.isReady()`:** Sempre aguarde a prontidão do router (`await router.isReady()`) após uma navegação (`router.push`) para garantir que todas as operações assíncronas do router foram concluídas antes de interagir com o componente.

### 2.2. Pinia Stores
*   **Inicialização:** As stores Pinia devem ser inicializadas em cada `beforeEach` usando `setActivePinia(createPinia())` e obtendo a instância da store (`useMinhaStore()`).
*   **Reset de Estado:** Para garantir o isolamento dos testes, o estado da store deve ser resetado em cada `beforeEach`. Para stores com estado simples, `store.$reset()` pode ser suficiente. Para stores que carregam dados de mocks JSON ou têm inicialização de estado mais complexa, use `store.$patch()` ou atribuição direta (`store.prop = valor`) para redefinir o estado com dados frescos.
*   **Espionagem de Ações:** Para verificar se as ações das stores foram chamadas, utilize `vi.spyOn(storeInstance, 'actionName')`.
*   **Mocks Consistentes:** Ao mockar stores ou composables que retornam funções ou objetos reativos (como `ref`s), certifique-se de que os mocks sejam consistentes entre os testes. Se um mock retorna uma nova instância de `vi.fn()` a cada chamada, os spies podem não capturar as chamadas corretas. É melhor definir os `vi.fn()` globalmente e atribuí-los ao mock.

### 2.3. Composables
*   Composables que retornam valores reativos (ex: `ref`) devem ser mockados de forma que seus valores possam ser manipulados diretamente nos testes (ex: `mockRef.value = 'novoValor'`).

### 2.4. `sessionStorage` e `localStorage`
*   Mocks para `sessionStorage` e `localStorage` (`vi.spyOn(window.localStorage, 'setItem')`, `vi.spyOn(window.localStorage, 'getItem')`, etc.) são essenciais para isolar os testes de efeitos colaterais no armazenamento do navegador. Considere criar um mock global para `localStorage` que simule seu comportamento e permita limpar o estado entre os testes.

### 2.5. Stubs de Componentes
*   Use stubs (`stubs: { Componente: { template: '...' } }`) para isolar o componente em teste de seus filhos. No entanto, tenha cuidado ao stubbar componentes que têm comportamento intrínseco (ex: `RouterLink`). Se o comportamento real do componente stubbado for crucial para o teste (como a navegação do `RouterLink`), é melhor não stubbá-lo.

### 2.6. Mocking de Importações JSON
*   **Problema de Hoisting:** `vi.mock` é içado para o topo do arquivo. Se você tentar mockar um módulo JSON usando uma variável definida no mesmo arquivo, mas *após* a chamada `vi.mock`, ocorrerá um `ReferenceError`.
*   **Solução:** Defina os dados mockados diretamente dentro da função de fábrica do `vi.mock`. Isso garante que os dados estejam disponíveis quando o mock é processado.
    ```typescript
    // Exemplo:
    vi.mock('../../mocks/meu-mock.json', () => ({
      default: [
        { id: 1, nome: 'Item Mockado' },
        { id: 2, nome: 'Outro Item' }
      ]
    }));
    ```
*   **Mocks Dinâmicos/Re-mocking:** Para cenários onde você precisa de diferentes dados mockados para o mesmo módulo JSON em testes específicos, use `vi.doMock` e `vi.importActual` dentro do teste ou de um `beforeEach` aninhado. Lembre-se de que `vi.importActual` deve ser usado para obter a versão "real" do módulo após o mock ser aplicado.

## 3. Lidando com Assincronicidade

Testes em ambientes Vue são inerentemente assíncronos devido ao ciclo de atualização do DOM e operações de roteamento/store.
*   **`await wrapper.vm.$nextTick()`:** Essencial para aguardar a próxima atualização do ciclo de renderização do Vue após interações que modificam o DOM ou o estado reativo.
*   **`await router.isReady()`:** Garante que o Vue Router concluiu todas as operações de navegação pendentes.
*   **`vi.useFakeTimers()` e `vi.runAllTimers()`:** Use com cautela. Se o código em teste utiliza `setTimeout` ou `setInterval`, `vi.useFakeTimers()` pode ser necessário. No entanto, ele pode interferir em eventos e microtasks. Use `vi.runAllTimers()` para avançar todos os timers falsos e `vi.useRealTimers()` para limpar após o teste. Se não for estritamente necessário, evite-o para simplificar o ambiente de teste.

## 4. Boas Práticas de Refatoração de Testes

*   **Isolamento:** Cada teste deve focar em uma única funcionalidade. O `beforeEach` é fundamental para configurar um ambiente limpo para cada teste.
*   **Clareza e Descrição:** Nomes de testes devem ser descritivos e indicar claramente o que está sendo testado e o resultado esperado (ex: `deve navegar para o painel se não houver crumbs anteriores`).
*   **Reutilização:** Crie funções auxiliares (ex: `mountComponent`) para encapsular lógicas de configuração repetitivas.
*   **Limpeza de Mocks/Spies:** Utilize `spy.mockClear()` ou `vi.clearAllMocks()` (com cautela) para garantir que os mocks e spies sejam redefinidos antes de cada teste, evitando vazamentos de estado entre eles.
*   **Verificação Contínua:** Execute os testes frequentemente durante o processo de refatoração. Isso ajuda a identificar e isolar problemas rapidamente.
*   **Cuidado com `vi.mock`:** Entenda o escopo e o comportamento de `vi.mock`. Mocks definidos no nível superior do arquivo de teste afetam todas as chamadas subsequentes. Se precisar de mocks dinâmicos por teste, manipule os valores de `ref`s ou `vi.fn()`s globais.
*   **Testar Interações Reais:** Sempre que possível, teste as interações do usuário (ex: `wrapper.find('button').trigger('click')`) em vez de chamar métodos internos do componente diretamente, a menos que seja para depuração.

## 5. Lições Aprendidas (Erros Comuns)

*   **Duplicação de Testes:** Copiar e colar blocos de teste pode levar a duplicação e dificultar a manutenção. Use funções auxiliares e `beforeEach` para evitar isso.
*   **Mocks Inconsistentes:** Mocks que geram novas instâncias de funções mockadas a cada chamada podem levar a spies que não capturam as chamadas esperadas. Use mocks globais consistentes.
*   **`pushSpy` Capturando Navegação Inicial:** Ao espionar `router.push`, certifique-se de limpar o spy *após* a navegação inicial do teste e *antes* da ação que você realmente quer verificar.
*   **`vi.useFakeTimers()` Interferindo em Eventos:** `vi.useFakeTimers()` pode interferir na propagação de eventos ou na execução de microtasks/macrotasks. Use-o apenas quando estritamente necessário e sempre com `vi.runAllTimers()` e `vi.useRealTimers()`.
*   **Stubbing de `RouterLink` que Impede a Navegação:** Stubbar `RouterLink` com um `<a>` simples impede que o `router.push` seja chamado. Se o teste depende da navegação do `RouterLink`, não o stubbe ou crie um stub mais sofisticado que simule o comportamento de navegação.
*   **Erros de Sintaxe Silenciosos:** Pequenos erros de sintaxe (ex: chaves desbalanceadas) podem causar falhas de transformação que são difíceis de depurar. Use um editor com linting e formatação automática.
*   **Problemas de Hoisting em Mocks JSON:** Variáveis definidas no mesmo arquivo que um `vi.mock` e usadas dentro da função de fábrica do mock podem não estar definidas devido ao içamento. A solução é definir os dados mockados diretamente dentro da função de fábrica do `vi.mock`.
*   **Mutação de Estado de Store em Mocks:** Ao mockar dados para inicializar o estado de uma store, certifique-se de que os dados sejam clonados profundamente (ex: `JSON.parse(JSON.stringify(mockData))`) antes de serem atribuídos ao estado da store. Isso evita que modificações no estado da store em um teste afetem os dados mockados originais, causando efeitos colaterais em outros testes.
*   **Inferência de Tipos com `ref([])` e `ref({})`:** Ao declarar `ref`s com arrays ou objetos vazios (`ref([])`, `ref({})`), o TypeScript pode inferir tipos muito genéricos (`never[]`, `{}`). Sempre forneça uma tipagem explícita para esses `ref`s para garantir a segurança de tipo e evitar erros de atribuição.