# Plano: unificar views de Cadastro e Mapa

## Objetivo

Consolidar cada dominio em uma unica view de referencia:

- `CadastroView.vue` para atividades e conhecimentos;
- `MapaView.vue` para mapa de competencias.

O modo da tela deve ser definido por permissao, situacao do subprocesso e dados carregados, nao pelo nome da rota.

## Estado atual resumido

- `CadastroView.vue` ja e a view unica de cadastro.
- `MapaView.vue` ja e a view unica de mapa.
- `CadastroVisualizacaoView.vue` e `MapaVisualizacaoView.vue` foram removidas.
- `SubprocessoCards.vue` navega pelos cards de dominio, sem separar modo de edicao e visualizacao.
- `processo.routes.ts` trabalha com `/cadastro` e `/mapa`.
- O contrato unitario do frontend ja foi ajustado para o modelo sem `vis-*`.
- Os helpers e as principais specs E2E ja foram migrados para cards unificados e validacoes semanticas.
- O maior bloco pendente agora esta na regressao E2E mais ampla e em textos/comentarios residuais.

## Diretrizes

1. Uma tela por dominio.
   Nao reintroduzir componentes, rotas ou nomenclaturas que tratem leitura e edicao como telas distintas.

2. Permissao decide acao.
   A tela unica deve continuar exibindo ou ocultando mutacoes e acoes de analise com base em `useAcesso` e no estado real do subprocesso.

3. Acoes compartilhadas ficam em um unico lugar.
   Historico, impacto, devolucao, aceite, homologacao, validacao e sugestoes nao devem voltar a existir em templates paralelos.

4. Leitura e edicao podem divergir no corpo, nao no destino.
   Quando necessario, manter corpos distintos dentro da mesma view (`corpoEdicao` e `corpoLeitura`), mas sem bifurcar navegacao.

5. Nao carregar DTO pesado sem necessidade.
   Em mapa, leitura continua usando `mapa-visualizacao`; edicao continua usando `contexto-edicao` e `mapa-completo`.

6. Nomear pelo comportamento.
   Preferir nomes como `modoSomenteLeitura`, `podeEditar`, `acoesAnalise`, `corpoLeitura` e `corpoEdicao`.

7. E2E deve validar capacidade operacional.
   O contrato novo dos testes precisa verificar dados e acoes disponiveis/ausentes, e nao mais qual rota ou card "de visualizacao" abriu.

## Fora de escopo

- fundir endpoints backend de edicao e visualizacao;
- criar um BFF amplo para unificar todos os contextos;
- alterar regras de acesso;
- criar redirects permanentes para `vis-*`;
- redesenhar toda a experiencia visual sem necessidade funcional.

## Proximos passos

### 1. Fechar o contrato E2E de Cadastro

- manter `e2e/helpers/helpers-atividades.ts` orientado a acoes disponiveis/ausentes;
- evitar reintroduzir `card-subprocesso-atividades-vis`;
- ao tocar em novas specs, preferir asserts de capacidade operacional em vez de "modo";
- usar `e2e/cdu-05.spec.ts`, `e2e/cdu-09.spec.ts`, `e2e/cdu-13.spec.ts`, `e2e/cdu-07.spec.ts` e `e2e/jornada.spec.ts` como referencia do contrato atualizado.

### 2. Fechar o contrato E2E de Mapa

- manter `e2e/helpers/helpers-mapas.ts` orientado a manutencao x leitura na mesma tela;
- evitar reintroduzir `card-subprocesso-mapa-visualizacao` e `card-subprocesso-mapa-edicao`;
- validar modo editavel por botoes de manutencao;
- validar modo somente leitura por ausencia de mutacoes e presenca de acoes de analise;
- usar `e2e/cdu-19.spec.ts`, `e2e/cdu-20.spec.ts`, `e2e/cdu-21.spec.ts` e `e2e/jornada.spec.ts` como referencia do contrato atualizado.

### 3. Revisar pontos de clareza visual ainda em aberto

- cadastro: confirmar se edicao inline continua clara;
- mapa: confirmar se associacao de atividades e manutencao de competencias continuam claras;
- se houver conflito entre leitura e manutencao, preferir modal ou painel dedicado.

### 4. Fechar documentacao residual

- manter `etc/reqs/design/breadcrumbs.md` coerente com o contrato atual;
- atualizar comentarios e descricoes de testes que ainda falem em "view de visualizacao".

### 5. Executar regressao E2E ampla

- rerodar um lote mais amplo alem dos arquivos ja migrados;
- priorizar specs nao exercitadas nesta rodada de migracao;
- tratar novas falhas como divergencia semantica, fixture/estado ou permissao, nunca como "falta de timeout".

## Riscos ativos

1. E2E continuar acoplado ao contrato antigo.
   Mitigacao: migrar helpers primeiro e depois revisar specs.

2. Regressao de permissao mascarada pela tela unica.
   Mitigacao: continuar testando ausencia de botoes de mutacao quando o perfil ou a situacao nao permitem edicao.

3. Tela unica ficar operacionalmente confusa.
   Mitigacao: separar visualmente manutencao e analise, mesmo quando convivem na mesma view.
