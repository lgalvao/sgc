# Diretório de Stores (Pinia)

Este diretório contém as _stores_ de gerenciamento de estado da aplicação, utilizando [Pinia](https://pinia.vuejs.org/), a solução oficial de gerenciamento de estado para o Vue 3.

## Objetivo

As _stores_ são responsáveis por centralizar o **estado global** da aplicação. Elas guardam dados que precisam ser compartilhados, acessados ou modificados por múltiplos componentes, independentemente de sua posição na árvore de componentes. Isso evita o problema de _"prop drilling"_ (passar `props` por vários níveis de componentes aninhados).

## Estrutura de uma Store

Uma _store_ Pinia é definida usando a função `defineStore` e possui três conceitos principais:

1.  **`state`**: Uma função que retorna o estado inicial. É o coração da _store_, onde os dados são efetivamente armazenados de forma reativa.
2.  **`getters`**: Propriedades computadas para a _store_. São como os `computed` dos componentes e são usados para derivar um estado a partir do `state` principal (e.g., filtrar uma lista, verificar se um usuário está autenticado).
3.  **`actions`**: Métodos que podem ser chamados para interagir com a _store_. Elas são equivalentes aos `methods` dos componentes e são o local ideal para colocar a lógica de negócio e executar operações assíncronas, como chamadas de API, que resultarão em uma alteração do estado (mutações).

## Quando Usar uma Store?

- **Dados do Usuário Autenticado**: Informações sobre o usuário logado, seu token de acesso e suas permissões.
- **Estado de UI Global**: Controle de temas (claro/escuro), estado de um _sidebar_ (aberto/fechado).
- **Dados Compartilhados entre Rotas**: Informações que precisam persistir enquanto o usuário navega por diferentes páginas da aplicação.
- **Cache de Dados da API**: Armazenar dados que são frequentemente acessados para evitar chamadas de API repetitivas.

## Convenção

Cada arquivo neste diretório define uma única _store_ e é nomeado de acordo com o domínio que gerencia, com o sufixo `Store`. Exemplo: `authStore.ts`, `mapaStore.ts`. A função exportada segue a convenção `use<Nome>Store`.