# Diretório de Componentes

Este diretório contém todos os componentes Vue 3 reutilizáveis da aplicação. Os componentes são os blocos de construção fundamentais da interface do usuário (UI).

## Filosofia

A principal filosofia é a componentização. Cada componente deve ser o mais autônomo e reutilizável possível. Eles são projetados seguindo a [API de Composição (Composition API)](https://vuejs.org/guide/introduction.html#composition-api) do Vue 3, utilizando a tag `<script setup>`.

## Estrutura

Os componentes são organizados em subdiretórios baseados na funcionalidade ou no domínio de negócio ao qual pertencem. Por exemplo:

- `components/layout/`: Componentes estruturais como `Header`, `Sidebar`, `Footer`.
- `components/formularios/`: Componentes de formulário genéricos como `InputTexto`, `Botao`, `SelectPersonalizado`.
- `components/tabelas/`: Componentes para exibição de dados tabulares.
- `components/comuns/`: Componentes de propósito geral, como `Modal`, `Spinner`, `Alerta`.

## Tipos de Componentes

1.  **Componentes de Apresentação (Dumb Components)**:
    - Recebem dados via `props` e emitem eventos via `emits`.
    - Não possuem lógica de negócio ou estado próprio complexo.
    - São focados exclusivamente na UI e na interação do usuário.
    - Exemplo: um componente de botão que apenas emite um evento de clique.

2.  **Componentes de Contêiner (Smart Components)**:
    - Podem conter lógica de negócio, interagir com as _stores_ (Pinia) ou usar _composables_.
    - Orquestram múltiplos componentes de apresentação.
    - Geralmente estão mais próximos das `views` (páginas) na hierarquia.

Manter essa separação ajuda na testabilidade e na reutilização dos componentes de apresentação.