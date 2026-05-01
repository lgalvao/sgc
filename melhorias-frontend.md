# Melhorias do frontend

Arquivo reduzido ao backlog real que ainda está pendente no checkout atual.

## Pendências principais

### 1. Política de cache e invalidação ainda não está formalizada

A base foi estabilizada: `processo`, `subprocesso`, `painel`, `historico`, `unidade` e parte das views já trabalham melhor com validade explícita. Ainda falta fechar a política como regra simples e uniforme.

Pendências:

- definir a política por recurso: `painel`, `historico`, `processo`, `subprocesso`, `mapa`, `unidade`, `organizacao`;
- documentar para cada recurso: escopo, chave, TTL ou invalidação explícita, e se a tela deve fazer refresh local ou esperar reativação;
- fechar os fluxos adjacentes de `unidade` e `organizacao` com a mesma semântica;
- decidir se `historico` continua sem TTL ou se deve seguir política parecida com `painel`.

### 2. `MapaView` ainda tem carregamento sobreposto em modo somente leitura

`useMapaOrquestracao.ts` ainda pode carregar contexto com mapa e depois buscar `mapa-visualizacao`.

Pendências:

- comparar os contratos reais de `contexto-edicao`, `mapa-completo` e `mapa-visualizacao`;
- remover a chamada extra se o contexto já trouxer o suficiente;
- se a leitura exigir payload próprio, explicitar isso no contrato e evitar carregar mapa completo no caminho de leitura;
- medir antes/depois com monitoramento.

### 3. `MapaView` ainda concentra o fluxo de sugestões

As mutações de competências já saíram da view. O bloco de sugestões ainda está embutido nela.

Pendências:

- extrair `useMapaSugestoes` para abrir, carregar, editar e submeter sugestões;
- manter retorno explícito do fluxo, sem esconder atualização em store por dentro;
- reduzir `defineExpose` conforme os testes deixarem de depender de detalhe interno.

### 4. Regra de domínio ainda vaza no mapper do frontend

`frontend/src/services/subprocessoService.ts` ainda faz `etapaAtual ?? 1`.

Pendências:

- confirmar o contrato backend para `etapaAtual`;
- remover o fallback do frontend quando o backend devolver o campo corretamente;
- revisar outros defaults silenciosos no mapper.

### 5. Contratos frouxos e defensividade excessiva

Ainda há mistura de `null`, `false`, estado em store e toast como formas de erro, além de alguns `casts` em fronteiras de API.

Pendências:

- reduzir a mistura de canais de erro em operações assíncronas;
- priorizar correção de fronteiras com `as unknown` em services/stores;
- evitar retorno silencioso de lista vazia quando isso muda o significado da tela.

### 6. `useAcesso.ts` continua grande demais

Ainda concentra permissões, habilitações e visibilidade de vários domínios.

Pendências:

- separar por domínio (`cadastro`, `mapa`, `subprocesso/admin`, `sessão`);
- preferir consumir ações prontas vindas do backend quando já existirem no payload;
- evitar rederivar regra de negócio no frontend.

### 7. Relatórios e importação ainda podem justificar payload agregado

`RelatorioMapasView.vue` e `ImportarAtividadesModal.vue` ainda são candidatos a reduzir coreografia de chamadas.

Pendências:

- medir esses fluxos primeiro;
- só criar endpoint agregado se a repetição estiver comprovada;
- remover etapas intermediárias no frontend apenas depois do contrato novo existir.

### 8. Organização de testes ainda reflete acúmulo histórico

Ainda existem testes duplicados por localização e arquivos `Coverage`/`Uncovered` que já não representam mais uma fase transitória clara.

Pendências:

- escolher um padrão de localização para testes de componente;
- consolidar `Coverage`/`Uncovered` quando o contrato da tela estiver estável;
- fazer isso em fatias pequenas, sempre com a suíte afetada rodando.

## Ordem prática

1. Fechar a política de validade/invalidação para `organizacao` e `mapas`.
2. Medir `MapaView` em modo somente leitura.
3. Extrair `useMapaSugestoes`.
4. Remover `etapaAtual ?? 1`.
5. Consolidar testes duplicados quando essas superfícies estabilizarem.
