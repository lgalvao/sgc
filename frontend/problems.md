# Problemas Conhecidos

## Testes Frontend

### TabelaProcessos.spec.ts
O teste `deve emitir o evento ordenar ao receber o evento sort-changed` emite um warning:
`[Vue warn]: Component emitted event "sort-changed" but it is neither declared in the emits option nor as an "onSortChanged" prop.`

Isso ocorre porque o evento é disparado diretamente na instância do componente `BTable` (ou wrapper) que pode não ter esse evento explicitamente declarado nas definições do componente mockado ou original no contexto do teste. Como o teste passa e verifica a emissão do evento pai corretamente, isso foi mantido como dívida técnica.

### ProcessoView.spec.ts
O Code Review apontou uma possível regressão crítica onde os testes falhariam por não atualizar as chamadas de `createWrapper`. No entanto, a verificação local (`npm run test:unit src/views/__tests__/ProcessoView.spec.ts`) confirmou que todos os testes passaram e as chamadas foram devidamente atualizadas para `const { wrapper: w } = createWrapper(); wrapper = w;`.
