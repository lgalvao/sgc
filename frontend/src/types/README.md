# Diretório de Tipos e Interfaces

Centraliza as definições de tipos TypeScript utilizadas em toda a aplicação.

## Arquivos Principais

* **`dtos.ts`**: Define os objetos de transferência de dados (Data Transfer Objects) que refletem exatamente a estrutura enviada/recebida pela API.
* **`tipos.ts`**: Define modelos de domínio internos do frontend, enums, e tipos utilitários complexos.

## Convenções

1. **PascalCase**: Interfaces e Tipos devem usar PascalCase (ex: `ProcessoDetalhado`).
2. **DTOs**: Devem preferencialmente ter o sufixo `Dto` (ex: `UsuarioLoginDto`).
3. **Enums**: Devem ser usados para conjuntos fixos de valores (ex: `SituacaoProcesso`).
