# Plano de Correção de Tipos

Este plano detalha os passos para corrigir os erros de tipagem no frontend da aplicação.

## Etapa 1: Correção das Definições de Tipos

1.  **Adicionar tipos de requisição ausentes:**
    -   `AceitarCadastroRequest`
    -   `DevolverCadastroRequest`
    -   `HomologarCadastroRequest`
2.  **Adicionar tipos do store de mapas ausentes:**
    -   `SalvarMapaRequest`
    -   `SalvarAjustesRequest`
    -   `MapaCompleto`
    -   `MapaAjuste`
    -   `ImpactoMapa`
3.  **Corrigir interfaces existentes:**
    -   Em `AtribuicaoTemporaria`, corrigir `idServidor` para `servidor`.
    -   Em `Subprocesso`, adicionar `dataFimEtapa1` e `dataLimiteEtapa2`.
    -   Em `UnidadeParticipante`, adicionar `situacao` e `sugestoes`.

## Etapa 2: Atualização dos Stores Pinia

1.  **Adicionar getter `getMapaByUnidadeId` ao store `mapas`:**
    -   Este getter é necessário para o componente `Unidade.vue`.
2.  **Adicionar actions de validação ao store `subprocessos`:**
    -   `homologarValidacao`
    -   `aceitarValidacao`
    -   `devolverValidacao`

## Etapa 3: Correção dos Componentes Vue

1.  **Corrigir `Subprocesso.vue`:**
    -   Importar `SituacaoSubprocesso`.
    -   Corrigir acessos a propriedades e chamadas de métodos.
2.  **Corrigir `Unidade.vue`:**
    -   Corrigir acessos a propriedades e chamadas de métodos.
3.  **Corrigir `VisAtividades.vue`:**
    -   Corrigir acessos a propriedades e chamadas de métodos.
4.  **Corrigir `VisMapa.vue`:**
    -   Corrigir acessos a propriedades e chamadas de métodos.

## Etapa 4: Finalização

1.  Executar `npm run typecheck` para garantir que todos os erros foram resolvidos.
2.  Executar os testes unitários e de E2E para garantir que as correções não introduziram regressões.
3.  Submeter o pull request final para revisão.
