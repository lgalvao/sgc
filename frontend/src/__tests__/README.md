# Testes Globais e de Integração

Este diretório contém testes que abrangem múltiplos componentes ou comportamentos globais da aplicação.

## Tipos de Teste

1. **Setup Tests**: Verificam se o ambiente de teste (Vitest, JSDOM) está configurado corretamente.
2. **Global Logic**: Testes para arquivos na raiz de `src`, como `axios-setup.ts` ou `main.ts`.
3. **Cross-Feature Tests**: Cenários que envolvem a interação entre diferentes módulos (ex: router + auth store).

## Observação

Testes específicos de componentes, stores ou services devem residir em suas respectivas pastas locais `__tests__` dentro de seus diretórios de origem.
