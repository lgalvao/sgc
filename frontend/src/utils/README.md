# Diretório de Utilitários (Utils)

Este diretório contém funções utilitárias genéricas e reutilizáveis que podem ser usadas em qualquer parte da aplicação.

## Objetivo

O objetivo deste diretório é abrigar código que não se encaixa em categorias mais específicas como `composables` (porque não têm estado), `mappers` (porque não transformam modelos de domínio) ou `validators` (porque não validam dados). As funções aqui devem ser, idealmente, **puras** e **sem efeitos colaterais**.

## O que colocar aqui?

- **Formatação de Dados**: Funções para formatar datas, moedas, números, etc. (e.g., `formatarDataParaBR`, `formatarCnpj`).
- **Manipulação de Strings**: Funções para capitalizar texto, truncar strings longas, etc.
- **Cálculos Genéricos**: Funções que realizam cálculos puros e não estão atreladas a uma lógica de negócio específica.
- **Manipulação de Objetos/Arrays**: Funções auxiliares para trabalhar com estruturas de dados de forma mais simples (e.g., `agruparPor`, `removerDuplicados`).

## O que NÃO colocar aqui?

- **Lógica com Estado (Reativa)**: Isso pertence aos `composables`.
- **Lógica de Negócio Específica**: Deve residir em `composables` ou `stores`.
- **Componentes Vue**: Devem estar no diretório `components`.
- **Chamadas de API**: Devem ser abstraídas por `composables` ou `stores`.

## Estrutura

As funções podem ser agrupadas em arquivos por categoria para melhor organização.

- `date.ts`: Funções relacionadas a datas.
- `string.ts`: Funções para manipulação de strings.
- `array.ts`: Funções auxiliares para arrays.

Isso facilita a importação e a descoberta de utilitários existentes.