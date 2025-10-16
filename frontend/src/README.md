# Diretório `src`

Este diretório é o coração da aplicação Vue 3, contendo todo o código-fonte, componentes, lógica de negócio e estilos. A estrutura é organizada para promover a modularidade, reutilização de código e manutenibilidade.

## Estrutura de Pastas

A seguir, uma descrição detalhada de cada subdiretório principal:

### `components/`

Contém componentes Vue reutilizáveis que são usados em várias partes da aplicação. Exemplos incluem botões, modais, campos de formulário e painéis de informação. O objetivo é encapsular a lógica e a estilização em blocos independentes e fáceis de usar.

### `composables/`

Armazena as funções "composables" do Vue 3 (Composition API). Elas encapsulam e reutilizam lógica com estado, como manipulação de dados de formulários, controle de estado de UI (ex: abrir/fechar um modal) ou integração com APIs externas.

### `constants/`

Define constantes globais usadas em toda a aplicação, como chaves de armazenamento local, nomes de eventos ou valores fixos de configuração. Centralizar constantes aqui evita a duplicação de "valores mágicos" no código.

### `mappers/`

Contém funções responsáveis por transformar (mapear) os dados recebidos da API (DTOs - Data Transfer Objects) para os modelos ou tipos de dados usados no frontend, e vice-versa. Isso desacopla a camada de visualização da estrutura de dados do backend.

### `mocks/`

Inclui dados e simulações de serviços (mock services) para uso em testes (unitários, de componentes e E2E) e no desenvolvimento local. Permite desenvolver e testar a interface do usuário de forma isolada, sem depender de um backend funcional.

### `stores/`

Define os "stores" do Pinia, o gerenciador de estado oficial do Vue. Cada arquivo representa um _store_ específico (ex: `usuarioStore`, `subprocessoStore`), que gerencia uma parte do estado global da aplicação de forma centralizada e reativa.

### `test/`

Contém os arquivos de teste unitário e de componente, utilizando Vitest. A estrutura de pastas dentro de `test/` geralmente espelha a de `src/`, facilitando a localização dos testes correspondentes a cada componente ou função.

### `types/`

Armazena as definições de tipos e interfaces TypeScript usadas na aplicação. Tipar os dados (ex: `Usuario`, `Processo`, DTOs) melhora a segurança do código, a autocompletação do editor e a clareza geral do projeto.

### `utils/`

Coleção de funções utilitárias puras e genéricas que podem ser usadas em qualquer parte do sistema. Exemplos incluem formatação de datas, cálculos simples ou manipulação de strings.

### `validators/`

Contém a lógica de validação de formulários. As funções aqui definidas podem ser usadas em conjunto com bibliotecas de formulário (como VeeValidate) ou em lógicas manuais para garantir que os dados inseridos pelo usuário sejam válidos.

### `views/`

Representa as páginas principais da aplicação. Cada arquivo `.vue` neste diretório é um componente de nível superior associado a uma rota específica (definida em `router.ts`). As _views_ orquestram a exibição de vários componentes menores para construir uma página completa.

## Arquivos Principais

-   **`App.vue`**: O componente raiz da aplicação. Ele serve como o layout principal que envolve todas as outras views.
-   **`main.ts`**: O ponto de entrada da aplicação. É aqui que a instância do Vue é criada, plugins (como Pinia e Vue Router) são instalados e a aplicação é montada no DOM.
-   **`router.ts`**: Define as rotas da aplicação, mapeando cada URL a um componente de _view_ correspondente.
-   **`style.css`**: Arquivo de estilos globais que se aplicam a toda a aplicação.