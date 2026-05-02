# Melhorias do frontend

Arquivo de backlog atualizado com o estado real do código, refletindo as necessidades de refatoração, resolução de código morto, inconsistências e defensividade excessiva.

## Pendências principais

### 1. `MapaView` ainda tem carregamento sobreposto em modo somente leitura

`useMapaOrquestracao.ts` ainda carrega o contexto de edição (`garantirContextoEdicao`) com mapa embutido e depois busca novamente `mapa-visualizacao` via `obterMapaVisualizacao`.

Pendências:
- comparar os contratos reais de `contexto-edicao`, `mapa-completo` e `mapa-visualizacao`;
- remover a chamada extra se o contexto inicial já trouxer o suficiente;
- se a leitura exigir payload próprio, explicitar isso no contrato e evitar carregar mapa completo no caminho de leitura;
- medir antes/depois com monitoramento.

### 2. `MapaView` ainda concentra o fluxo de sugestões

As mutações de competências já saíram da view, mas o bloco de sugestões ainda está fortemente embutido nela (métodos `carregarSugestoesParaVisualizacao`, `sincronizarSugestoesMapa`, `verSugestoes`, etc.).

Pendências:
- extrair `useMapaSugestoes` para abrir, carregar, editar e submeter sugestões;
- manter retorno explícito do fluxo, sem esconder atualização em store por dentro;
- reduzir o uso de `defineExpose` conforme a dependência de detalhes internos nos testes for diminuída.

### 3. Regra de domínio ainda vaza no mapper do frontend

`frontend/src/services/subprocessoService.ts` ainda faz fallback silencioso usando `etapaAtual ?? 1`.

Pendências:
- confirmar o contrato backend para `etapaAtual`;
- remover o fallback do frontend se o backend puder/dever garantir a resposta correta;
- revisar outros defaults silenciosos (ex: strings vazias em unidades) nos mappers.

### 4. Contratos frouxos e defensividade excessiva (Tipagem forçada)

Há uma mistura considerável de abordagens de erro (`null`, `false`, stores de erro, e exceptions), além de excesso de buracos na tipagem. O código conta com cerca de **98 tipagens forçadas usando `as unknown`** em stores (ex: `perfil.ts`), services (ex: `usuarioService.ts`) e fortemente nos testes.

Pendências:
- reduzir a mistura de canais de erro em operações assíncronas;
- revisar as fronteiras de API (responses/stores) para remover e refatorar os usages de `as unknown`;
- evitar o retorno silencioso de lista vazia ou valores default que encubram falhas de integração com a API.

### 5. `useAcesso.ts` continua grande demais e com boilerplate desnecessário

O arquivo centraliza permissões, habilitações e visibilidade e acumula quase 200 linhas apenas exportando dezenas de `computed properties` que repassam (`pass-through`) a flag correspondente calculada e devolvida pelo backend no objeto `permissoes`. 

Pendências:
- separar por domínio (`cadastro`, `mapa`, `subprocesso/admin`, `sessão`);
- preferir consumir o objeto de permissões de forma mais direta, eliminando as redundâncias;
- evitar a re-derivação de regras de negócio no frontend (verificações híbridas com `isAdmin` ou `isChefe` para funções que o backend deveria habilitar/desabilitar diretamente).

### 6. Relatórios e importação ainda podem justificar payload agregado

`RelatorioMapasView.vue` e `ImportarAtividadesModal.vue` ainda são candidatos a reduzir coreografia de chamadas.

Pendências:
- medir esses fluxos primeiro;
- só criar endpoint agregado se a repetição estiver comprovada;
- remover etapas intermediárias no frontend apenas depois do contrato novo existir.

### 7. Organização de testes ainda reflete acúmulo histórico

Ainda há uma separação não-padrão de testes duplicados indicando transições, como pastas/arquivos `*Coverage.spec.ts` coexistindo com testes antigos `*Uncovered.spec.ts`.

Pendências:
- unificar os arquivos de teste duplicados (consolidando Coverage e Uncovered);
- adotar um padrão definitivo de localização dos testes de componente;
- fazer as migrações em fatias pequenas, garantindo que as suítes rodem e passem a cada etapa.

## Ordem prática

1. Medir `MapaView` em modo somente leitura.
2. Extrair `useMapaSugestoes`.
3. Remover o cast e mitigação `etapaAtual ?? 1`.
4. Simplificar o boilerplate do `useAcesso.ts` e refatorar os usos nocivos de `as unknown` nos contratos.
5. Consolidar testes duplicados remanescentes de transições anteriores.