# Resumo dos Problemas nos Testes Unitários

Durante a execução dos testes unitários, foram identificados problemas persistentes em dois arquivos de teste principais, que parecem estar relacionados a uma configuração fundamental incorreta no ambiente de teste.

## 1. `frontend/src/stores/__tests__/subprocessos.spec.ts`

- **Erro Recorrente**: `AssertionError: expected "spy" to be called with arguments: [ 1 ]`
- **Causa Provável**: Os testes para as ações da `useSubprocessosStore` (ex: `disponibilizarCadastro`, `devolverCadastro`, etc.) dependem do estado de outra store, a `useProcessosStore`. Especificamente, eles precisam acessar `processosStore.processoDetalhe.codigo` para chamar a ação `fetchProcessoDetalhe` com o ID correto. As tentativas de mockar essa dependência não foram bem-sucedidas, pois o spy para `fetchProcessoDetalhe` nunca é chamado com o argumento esperado. Isso sugere que o estado mockado da `processosStore` não está sendo injetado ou lido corretamente dentro das ações da `subprocessosStore` no ambiente de teste.

## 2. `frontend/src/stores/__tests__/impacto.spec.ts`

- **Erro Recorrente**: `TypeError: Cannot read properties of undefined (reading 'fetchProcessoDetalhe')`
- **Causa Provável**: Similar ao problema anterior, os testes para a lógica de impacto dependem de múltiplas stores (`useProcessosStore`, `useMapasStore`, etc.). O erro indica que a instância da `processosStore` está `undefined` no momento em que o teste tenta acessá-la. Isso aponta para um problema na forma como as stores mockadas são injetadas e disponibilizadas para outras stores durante a execução dos testes do Vitest. A configuração do `beforeEach` parece estar correta, mas a injeção de dependência entre as stores no Pinia não está funcionando como esperado no ambiente de teste.

## Conclusão

Ambos os problemas indicam uma falha na configuração do ambiente de teste do Vitest para lidar com a injeção de dependências entre stores Pinia. A abordagem de mock para as stores precisa ser reavaliada para garantir que o estado e as ações de uma store mockada estejam acessíveis para outra store que dependa dela.