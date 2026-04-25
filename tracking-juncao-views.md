# Tracking: junção das views de Cadastro e Mapa

## Status geral

Status: em andamento

Foco atual: consolidar o contrato E2E e encerrar ajustes de clareza visual e documentacao residual.

## Estado consolidado

### Entregue

- [x] Cadastro unificado em `CadastroView.vue`.
- [x] Mapa unificado em `MapaView.vue`.
- [x] Views legadas `CadastroVisualizacaoView.vue` e `MapaVisualizacaoView.vue` removidas.
- [x] Navegacao principal sem cards/rotas separados por modo.
- [x] Breadcrumbs alinhados ao contrato atual.
- [x] Frontend unitario ajustado ao modelo sem `vis-*`.
- [x] Helpers E2E de cadastro e mapa migrados para o contrato semantico.
- [x] Specs E2E principais migradas para cards unificados e acoes disponiveis/ausentes.

### Em aberto

- [ ] Confirmar se cadastro precisa de mais separacao visual entre edicao e analise.
- [ ] Confirmar se mapa precisa de mais separacao visual entre manutencao e analise.
- [ ] Limpar comentarios e textos residuais que ainda descrevem "view de visualizacao".
- [ ] Rodar regressao E2E mais ampla fora do lote principal da migracao.

## Diretrizes para o proximo ciclo

1. Prioridade de execucao:
   helpers E2E -> specs E2E -> ajustes de clareza visual -> documentacao residual.

2. Contrato novo dos testes:
   validar tela do dominio, dados presentes e acoes disponiveis/ausentes; nao validar mais o antigo "destino de visualizacao".

3. Regras de seguranca funcional:
   a unificacao nao pode liberar botoes de mutacao fora das permissoes previstas.

4. Compatibilidade:
   nao recriar `vis-*`, aliases ou redirects permanentes so para acomodar testes antigos.

## Proximos passos objetivos

### E2E de Cadastro

- [x] Atualizar `e2e/helpers/helpers-atividades.ts`.
- [x] Revisar `e2e/cdu-05.spec.ts`.
- [x] Revisar `e2e/cdu-09.spec.ts`.
- [x] Revisar `e2e/cdu-13.spec.ts`.
- [x] Revisar `e2e/cdu-07.spec.ts`.
- [x] Revisar `e2e/jornada.spec.ts` no fluxo de cadastro.

### E2E de Mapa

- [x] Atualizar `e2e/helpers/helpers-mapas.ts`.
- [x] Revisar `e2e/cdu-19.spec.ts`.
- [x] Revisar `e2e/cdu-20.spec.ts`.
- [x] Revisar `e2e/cdu-21.spec.ts`.
- [x] Revisar `e2e/jornada.spec.ts` no fluxo de mapa.

### Regressao ampla

- [ ] Revisar `e2e/regressao-cache-sessao.spec.ts`.
- [ ] Rodar um lote E2E mais amplo cobrindo specs ainda nao exercitadas nesta migracao.

### Fechamento

- [ ] Rodar E2E principal de cadastro.
- [ ] Rodar E2E principal de mapa.
- [ ] Registrar falhas relevantes encontradas.
- [ ] Atualizar documentacao final remanescente.

## Arquivos-chave do proximo passo

- `e2e/helpers/helpers-atividades.ts`
- `e2e/helpers/helpers-mapas.ts`
- `e2e/cdu-05.spec.ts`
- `e2e/cdu-09.spec.ts`
- `e2e/cdu-13.spec.ts`
- `e2e/cdu-19.spec.ts`
- `e2e/cdu-20.spec.ts`
- `e2e/cdu-21.spec.ts`
- `e2e/jornada.spec.ts`
- `e2e/regressao-cache-sessao.spec.ts`

## Observacoes

- O tracking deve registrar daqui para frente apenas estado atual, diretrizes e proximos passos.
- Historico detalhado de validacoes executadas nao precisa permanecer aqui, salvo quando houver bloqueio ainda ativo.
