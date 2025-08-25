# Regras e Boas Práticas para Testes Unitários com Vitest

Este documento sumariza os aprendizados e as boas práticas observadas durante a refatoração e depuração dos testes
unitários do sistema, com foco em componentes Vue 3, Vue Router e Pinia.

## 1. Estrutura de Testes

Os testes são organizados usando blocos `describe` para agrupar testes relacionados a uma funcionalidade ou componente
específico, e blocos `it` para casos de teste individuais. O `beforeEach` é amplamente utilizado para configurar o
ambiente de teste antes de cada teste, garantindo isolamento e consistência.

## 2. Mocks e Stubs

### 2.1. Vue Router

* **Uso de Instância Real:** Para testes de navegação e interação com rotas, é preferível usar uma instância real do
  `vue-router` (`createRouter`, `createWebHistory`). Isso garante que o comportamento de roteamento seja o mais próximo
  possível do ambiente de produção.
* **Espionagem de Métodos:** Utilize `vi.spyOn(router, 'push')` para verificar se as navegações foram chamadas
  corretamente. Lembre-se de limpar o spy (`pushSpy.mockClear()`) após a navegação inicial do teste e antes da ação que
  você realmente quer testar.
* **`router.isReady()`:** Sempre aguarde a prontidão do router (`await router.isReady()`) após uma navegação (
  `router.push`) para garantir que todas as operações assíncronas do router foram concluídas antes de interagir com o
  componente.

### 2.2. Pinia Stores

* **Inicialização:** As stores Pinia devem ser inicializadas em cada `beforeEach` usando `setActivePinia(createPinia())`
  e obtendo a instância da store (`useMinhaStore()`).
* **Reset de Estado:** Para garantir o isolamento dos testes, o estado da store deve ser resetado em cada `beforeEach`.
  Para stores com estado simples, `store.$reset()` pode ser suficiente. Para stores que carregam dados de mocks JSON ou
  têm inicialização de estado mais complexa, use `store.$patch()` ou atribuição direta (`store.prop = valor`) para
  redefinir o estado com dados frescos.
* **Espionagem de Ações:** Para verificar se as ações das stores foram chamadas, utilize
  `vi.spyOn(storeInstance, 'actionName')`.
* **Mocks Consistentes:** Ao mockar stores ou composables que retornam funções ou objetos reativos (como `ref`s),
  certifique-se de que os mocks sejam consistentes entre os testes. Se um mock retorna uma nova instância de `vi.fn()` a
  cada chamada, os spies podem não capturar as chamadas corretas. É melhor definir os `vi.fn()` globalmente e
  atribuí-los ao mock.
* **Reatividade de Mocks:** Ao mockar valores reativos (ex: `ref`) que afetam a renderização condicional (`v-if`),
  garanta que o mock seja definido *antes* da montagem do componente ou que o componente seja explicitamente atualizado
  (`await wrapper.vm.$nextTick()`) após a alteração do mock. Em casos persistentes, considere passar o valor mockado
  diretamente como prop para o componente em teste, se aplicável.

### 2.3. Composables

* Composables que retornam valores reativos (ex: `ref`) devem ser mockados de forma que seus valores possam ser
  manipulados diretamente nos testes (ex: `mockRef.value = 'novoValor'`).

### 2.4. `sessionStorage` e `localStorage`

* Mocks para `sessionStorage` e `localStorage` (`vi.spyOn(window.localStorage, 'setItem')`,
  `vi.spyOn(window.localStorage, 'getItem')`, etc.) são essenciais para isolar os testes de efeitos colaterais no
  armazenamento do navegador. Considere criar um mock global para `localStorage` que simule seu comportamento e permita
  limpar o estado entre os testes.
* **Ordem de Mocking:** Ao mockar `localStorage` ou `sessionStorage` e testar stores que os utilizam, certifique-se de
  que o `vi.spyOn` seja configurado *antes* da instância da store ser criada no teste, para que o mock seja efetivo.

### 2.5. Stubs de Componentes

* Use stubs (`stubs: { Componente: { template: '...' } }`) para isolar o componente em teste de seus filhos. No entanto,
  tenha cuidado ao stubbar componentes que têm comportamento intrínseco (ex: `RouterLink`). Se o comportamento real do
  componente stubbado for crucial para o teste (como a navegação do `RouterLink`), é melhor não stubbá-lo.

## 3. Lidando com Assincronicidade

Testes em ambientes Vue são inerentemente assíncronos devido ao ciclo de atualização do DOM e operações de
roteamento/store.

* **`await wrapper.vm.$nextTick()`:** Essencial para aguardar a próxima atualização do ciclo de renderização do Vue após
  interações que modificam o DOM ou o estado reativo.
* **`await router.isReady()`:** Garante que o Vue Router concluiu todas as operações de navegação pendentes.
* **`vi.useFakeTimers()` e `vi.runAllTimers()`:** Use com cautela. Se o código em teste utiliza `setTimeout` ou
  `setInterval`, `vi.useFakeTimers()` pode ser necessário. No entanto, ele pode interferir em eventos e microtasks. Use
  `vi.runAllTimers()` para avançar todos os timers falsos e `vi.useRealTimers()` para limpar após o teste. Se não for
  estritamente necessário, evite-o para simplificar o ambiente de teste.

## 4. Boas Práticas de Refatoração de Testes

* **Isolamento:** Cada teste deve focar em uma única funcionalidade. O `beforeEach` é fundamental para configurar um
  ambiente limpo para cada teste.
* **Clareza e Descrição:** Nomes de testes devem ser descritivos e indicar claramente o que está sendo testado e o
  resultado esperado (ex: `deve navegar para o painel se não houver crumbs anteriores`).
* **Reutilização:** Crie funções auxiliares (ex: `mountComponent`) para encapsular lógicas de configuração repetitivas.
* **Limpeza de Mocks/Spies:** Utilize `spy.mockClear()` ou `vi.clearAllMocks()` (com cautela) para garantir que os mocks
  e spies sejam redefinidos antes de cada teste, evitando vazamentos de estado entre eles.
* **Verificação Contínua:** Execute os testes frequentemente durante o processo de refatoração. Isso ajuda a identificar
  e isolar problemas rapidamente.
* **Cuidado com `vi.mock`:** Entenda o escopo e o comportamento de `vi.mock`. Mocks definidos no nível superior do
  arquivo de teste afetam todas as chamadas subsequentes. Se precisar de mocks dinâmicos por teste, manipule os valores
  de `ref`s ou `vi.fn()`s globais.
* **Testar Interações Reais:** Sempre que possível, teste as interações do usuário (ex:
  `wrapper.find('button').trigger('click')`) em vez de chamar métodos internos do componente diretamente, a menos que
  seja para depuração.

## 5. Lições Aprendidas (Erros Comuns)

* **Duplicação de Testes:** Copiar e colar blocos de teste pode levar a duplicação e dificultar a manutenção. Use
  funções auxiliares e `beforeEach` para evitar isso.
* **Mocks Inconsistentes:** Mocks que geram novas instâncias de funções mockadas a cada chamada podem levar a spies que
  não capturam as chamadas esperadas. Use mocks globais consistentes.
* **`pushSpy` Capturando Navegação Inicial:** Ao espionar `router.push`, certifique-se de limpar o spy *após* a
  navegação inicial do teste e *antes* da ação que você realmente quer verificar.
* **`vi.useFakeTimers()` Interferindo em Eventos:** `vi.useFakeTimers()` pode interferir na propagação de eventos ou na
  execução de microtasks/macrotasks. Use-o apenas quando estritamente necessário e sempre com `vi.runAllTimers()` e
  `vi.useRealTimers()`.
* **Stubbing de `RouterLink` que Impede a Navegação:** Stubbar `RouterLink` com um `<a>` simples impede que o
  `router.push` seja chamado. Se o teste depende da navegação do `RouterLink`, não o stubbe ou crie um stub mais
  sofisticado que simule o comportamento de navegação.
* **Erros de Sintaxe Silenciosos:** Pequenos erros de sintaxe (ex: chaves desbalanceadas) podem causar falhas de
  transformação que são difíceis de depurar. Use um editor com linting e formatação automática.
* **Problemas de Hoisting em Mocks JSON:** Variáveis definidas no mesmo arquivo que um `vi.mock` e usadas dentro da
  função de fábrica do mock podem não estar definidas devido ao içamento. A solução é definir os dados mockados
  diretamente dentro da função de fábrica do `vi.mock`.
* **Mutação de Estado de Store em Mocks:** Ao mockar dados para inicializar o estado de uma store, certifique-se de que
  os dados sejam clonados profundamente (ex: `JSON.parse(JSON.stringify(mockData))`) antes de serem atribuídos ao estado
  da store. Isso evita que modificações no estado da store em um teste afetem os dados mockados originais, causando
  efeitos colaterais em outros testes.
* **Inferência de Tipos com `ref([])` e `ref({})`:** Ao declarar `ref`s com arrays ou objetos vazios (`ref([])`,
  `ref({})`), o TypeScript pode inferir tipos muito genéricos (`never[]`, `{}`). Sempre forneça uma tipagem explícita
  para esses `ref`s para garantir a segurança de tipo e evitar erros de atribuição.
* **Declarações de Mocks Duplicadas:** Evite declarar o mesmo objeto mock (ex: `mockAtividadesStore`) múltiplas vezes no
  mesmo escopo. Isso pode causar erros de sintaxe e confusão. Defina-os uma única vez em um escopo apropriado.
* **Gerenciamento Consistente de Dados Mockados:** Garanta que as implementações mockadas de métodos de store (ex:
  `getAtividadesPorSubprocesso`, `setAtividades`) leiam e escrevam consistentemente em uma fonte central de dados
  mockados (ex: `mockAtividadesStore.atividades`). Isso assegura que as alterações feitas por um mock sejam refletidas
  quando outros mocks ou propriedades computadas do componente acessam os dados.
* **Tipagem Correta de Funções Mockadas:** Ao usar `vi.fn()` para métodos de store e acessá-los via `useStore()`, o
  TypeScript pode inferir o tipo da função original, não o tipo do mock. Faça um "cast" explícito para `Mock` (ex:
  `(store.method as Mock).mockImplementation(...)`) para permitir o acesso a métodos específicos de mock como
  `mockImplementation`. Lembre-se de importar `Mock` de `vitest`.
* **Atualizações Assíncronas e `await wrapper.vm.$nextTick()`:** Após realizar ações que modificam dados reativos ou
  acionam re-renderizações de componentes (ex: definir valores de input, disparar eventos, chamar ações de store que
  atualizam o estado), sempre utilize `await wrapper.vm.$nextTick()` para aguardar que o Vue atualize o DOM. Isso
  garante que as asserções sejam feitas contra a UI atualizada.
* **Seleção Precisa de Elementos DOM:** Seja preciso com os seletores de elementos DOM. Por exemplo, quando verificar se
  um botão está habilitado, não use `[disabled="false"]` como seletor de atributo; em vez disso, verifique a ausência do
  atributo `disabled` ou use `:not([disabled])`. Ao disparar submissões de formulário, direcione o próprio elemento
  `form` (ex: `form[aria-label="Adicionar Atividade"]`) em vez de um botão de submit, pois o evento de submit do
  formulário é o que importa.
* **Compatibilidade de Versão ES para Métodos de Array:** Esteja ciente da compatibilidade da versão ES do ambiente
  JavaScript. Se métodos como `.at()` não forem suportados, utilize alternativas mais antigas e compatíveis (ex:
  `[index]`) ou atualize a opção `lib` do compilador em `tsconfig.json` se uma alteração em todo o projeto for
  aceitável. Prefira soluções baseadas em código para correções de teste isoladas.
* **Mocking de Ações Assíncronas de Store:** Ao mockar ações assíncronas de store (ex: `fetchAtividadesPorSubprocesso`),
  certifique-se de que a implementação mockada retorne uma `Promise` (ex: `Promise.resolve(...)`) para simular
  corretamente o comportamento assíncrono.
* **Problemas de Reatividade com Mocks de `ref` e `v-if`:** Em testes de componentes, quando um `v-if` depende de um
  `ref` mockado (ex: de um composable ou store), pode ser necessário garantir que o `ref` seja definido *antes* da
  montagem do componente, ou que o componente seja explicitamente atualizado (`await wrapper.vm.$nextTick()`) após a
  alteração do `ref` mockado para forçar a re-renderização. Em casos persistentes, pode ser necessário investigar o uso
  de `@pinia/testing` para um controle mais direto do estado da store no ambiente de teste.
* **Depuração de Testes Persistentes:** Quando um teste falha de forma persistente e a causa não é óbvia, utilize
  `console.log` para inspecionar o estado dos mocks e do componente em diferentes pontos do teste. Se o problema for
  com a reatividade ou a interação com o ambiente de teste (como JSDOM), considere simplificar o teste ou, como último
  recurso, ignorá-lo temporariamente para permitir que outros testes passem, enquanto aprofunda a investigação em um
  momento oportuno.