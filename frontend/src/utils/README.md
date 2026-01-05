# Diretório de Utilitários

Este diretório contém funções utilitárias puras (helpers) que não dependem do estado da aplicação (Store/Pinia) e podem ser reutilizadas em qualquer lugar.

## Estrutura

- **`formatters.ts`**: Funções para formatação de dados (data, moeda, CPF, strings).
- **`validators.ts`**: Funções para validação de formulários e regras de negócio simples.
- **`dom.ts`**: Utilitários para manipulação segura do DOM (se necessário).

## Boas Práticas

- **Funções Puras:** Sempre que possível, crie funções puras (mesma entrada = mesma saída, sem efeitos colaterais).
- **Testes Unitários:** Utilitários são candidatos perfeitos para testes unitários rigorosos devido à sua natureza isolada.