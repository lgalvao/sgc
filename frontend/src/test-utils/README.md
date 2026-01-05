# Diretório de Test Utils

Este diretório contém utilitários específicos para facilitar a escrita de testes unitários (Vitest) e de componentes.

## Conteúdo Típico

- **`renderUtils.ts`**: Wrappers customizados para o `mount` do Vue Test Utils que já injetam plugins comuns (Pinia, Router, BootstrapVueNext) para evitar repetição em cada teste.
- **`mockFactories.ts`**: Funções geradoras de dados fake (fixtures) para testes. Ex: `criarProcessoMock()`.

## Exemplo de Factory

```typescript
export function criarProcessoMock(overrides?: Partial<Processo>): Processo {
  return {
    codigo: 1,
    titulo: 'Processo Teste',
    situacao: 'EM_ANDAMENTO',
    ...overrides
  };
}
```