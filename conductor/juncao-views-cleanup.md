# Plano de Ação: Conclusão da Junção de Views (Cleanup & E2E)

## Objetivo
Finalizar a consolidação das views de Cadastro e Mapa, removendo nomenclaturas residuais de "visualização" em toda a base de código e consolidando testes E2E isolados. O intuito é manter o repositório enxuto e estritamente coerente com a nova arquitetura de views únicas por domínio (uma tela por domínio independente do modo leitura/edição).

## Key Files & Context
- `e2e/regressao-cache-sessao.spec.ts` (arquivo a ser excluído)
- `e2e/cdu-07.spec.ts` (receberá o cenário de regressão)
- `frontend/src/constants/textos.ts` (limpeza de variáveis de texto)
- `frontend/src/components/processo/SubprocessoCards.vue` (uso dos textos unificados)
- Helpers E2E (`helpers-atividades.ts`, `helpers-analise.ts`, `helpers-mapas.ts`)
- Múltiplas specs E2E e documentos de requisitos (`etc/reqs/*.md`) que ainda citam "visualização".

## Implementation Steps

### 1. Consolidação do E2E de Regressão de Cache
- Transferir o cenário de teste presente em `e2e/regressao-cache-sessao.spec.ts` para o escopo do arquivo `e2e/cdu-07.spec.ts`, que já é focado no detalhamento e permissionamento de subprocessos.
- Excluir o arquivo standalone `e2e/regressao-cache-sessao.spec.ts`.

### 2. Unificação de Textos de Interface
- Modificar `frontend/src/constants/textos.ts` para remover as chaves `VISUALIZACAO_CADASTRO_TEXTO` e `MAPA_VISUALIZACAO_TEXTO`.
- Em `frontend/src/components/processo/SubprocessoCards.vue`, consolidar a exibição de textos dos cards para não mais bifurcar entre "Cadastro" e "Visualização do cadastro". Utilizaremos `ATUALIZACAO_CADASTRO_TEXTO` e `MAPA_TEXTO` unificados, reforçando que o destino é único.

### 3. Remoção de Nomenclaturas Residuais nos Helpers E2E
- Refatorar `e2e/helpers/helpers-atividades.ts`: renomear métodos como `navegarParaAtividadesVisualizacao` e `abrirModalImpactoVisualizacao` eliminando o sufixo "Visualizacao" e adaptando para o comportamento único da tela.
- Refatorar `e2e/helpers/helpers-analise.ts`: renomear `abrirHistoricoAnaliseVisualizacao` e `verificarAcoesAnaliseCadastroVisualizacao`.
- Ajustar `e2e/helpers/helpers-mapas.ts` para remover menções a visualização nos comentários.

### 4. Atualização das Specs E2E
- Substituir os asserts em specs (`cdu-07.spec.ts`, `jornada.spec.ts`, `captura.spec.ts`, etc) que buscam por textos de "Visualização das atividades..." nos cards, usando o texto consolidado.
- Atualizar todas as importações e chamadas de helpers renomeados em todas as specs E2E impactadas.

### 5. Limpeza de Documentação
- Atualizar arquivos Markdown em `etc/reqs/` (como `cdu-07.md`, `cdu-18.md`, `cdu-19.md`, `cdu-20.md`) substituindo menções a telas específicas de "Visualização de mapa" para a tela única de "Mapa de competências" em modo somente leitura.

## Verification & Testing
- Validar a refatoração executando a suíte E2E nas specs alteradas (particularmente `cdu-07` e `jornada`) para atestar que os asserts de cards e a navegação permanecem verdes.
- Rodar validações estáticas (`npm run quality:all` ou correspondente) para garantir que a remoção das chaves em `textos.ts` não deixou referências quebradas na base TypeScript.