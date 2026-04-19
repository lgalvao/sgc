# Tracking de racionalizacao

## Uso

Este arquivo acompanha somente o que ainda falta fazer. Ao concluir um item, remover ou substituir por uma nova pendencia real.

## P0: matriz de custo de workflow

**Confirmado**

- Ha tracing para `registrarTransicao`: `persistir-transicao` e `notificar-transicao`.
- Ha tracing por etapa para disponibilizacao de mapa e `acao-em-bloco`.
- Os traces dependem de `X-Monitoramento-Ativo: true`.

**Falta fazer**

- Coletar em homologacao, na mesma janela e com o mesmo perfil.
- Consolidar p50, p95 e pico por acao: `iniciar`, `acao-em-bloco`, `finalizar`, disponibilizacao de cadastro, disponibilizacao de mapa e acoes de validacao.
- Separar custo de validacao, persistencia, notificacao e e-mail.
- Escolher a primeira transicao candidata a desacoplamento de efeito colateral.

**Conclui quando**

- Houver ordem de ataque baseada em custo observado e frequencia de uso.

## P1: escopo de `contexto-completo`

**Confirmado**

- `detalhes` usa `obterDetalhesCompleto(codigo, false)`.
- `contexto-completo` usa `obterDetalhesCompleto(codigo, true)`.
- `ProcessoDetalheView` ainda depende de `contexto-completo` para acoes em bloco e permissao de finalizacao.

**Falta fazer**

- Medir ganho real de `detalhes` depois da separacao.
- Decidir se acoes em bloco/elegiveis podem ser carregados sob demanda.
- Avaliar se `podeFinalizar` deve continuar no mesmo contrato.

**Conclui quando**

- Houver decisao clara entre manter o BFF atual ou dividir carga incremental.

## P1: contexto de subprocesso

**Confirmado**

- `carregarContextoSubprocessoInicial` centraliza query e fallback por `processo+unidade`.
- Stores fazem dedupe de chamadas concorrentes, mas nao reutilizam contexto entre ativacoes.
- `SubprocessoContextoConsultaService` ja absorveu parte da montagem de contexto.

**Falta fazer**

- Medir quais telas ainda repetem `contexto-edicao` e busca por `processo+unidade`.
- Decidir se ha reuso seguro dentro de uma unica ativacao de tela.
- Continuar reduzindo permissoes e montagem de resposta dentro de `SubprocessoConsultaService`.
- Remover fallback implicito que esconda erro de integracao.

**Conclui quando**

- A abertura/retomada de subprocesso usar uma rota canonica sem chamadas equivalentes no mesmo fluxo.

## P2: acoes em `MapaVisualizacaoView`

**Confirmado**

- Sugestoes/historico ja nao bloqueiam abertura de modal.
- Acoes de analise ja estao em `useMapaAcoesAnalise`.
- A tela ainda tem condicionais de exibicao baseadas no contexto recebido.

**Falta fazer**

- Identificar condicionais que duplicam regra do backend.
- Avaliar lista de acoes pronta para renderizacao.
- Implementar apenas se reduzir ramificacao real na tela.

**Conclui quando**

- A tela coordenar interacao/estado visual sem reconstruir regra de negocio de acao.

## P2: contratos defensivos e `id` legado

**Confirmado**

- `node etc/scripts/sgc.js codigo smells auditar --json --sem-gravar` apontou pontuacao critica: 103 `@Nullable` em DTOs, 213 checks explicitos de `null` no backend e 417 usos de `any` em testes frontend.
- Hotspots iniciais: `UnidadeDto`, `ValidadorDadosOrganizacionais`, `ProcessoService`, `AtualizarSubprocessoRequest`, `ProcessoDetalheDto` e `SubprocessoResumoDto`.
- `node etc/scripts/sgc.js codigo id-legado identificar --output /tmp/sgc-id-legado-report.txt` encontrou 2097 ocorrencias brutas; ha muito ruido por `getByTestId`, DOM e excecoes de framework.

**Falta fazer**

- Separar o relatorio de `id` legado entre ruido aceito, contrato externo e dominio que deve migrar para `codigo`.
- Escolher uma fatia pequena de DTOs/backend para reduzir `@Nullable` e checks defensivos sem quebrar serializacao nem compatibilidade de API.
- Reduzir `any` em testes frontend apenas nos arquivos que bloquearem manutencao real ou cobertura, evitando refatoracao cosmetica ampla.

**Conclui quando**

- Houver uma lista curta de migracoes por contrato, com criterio de aceite e sem substituicoes mecanicas em massa.

## P2: dashboard de QA frontend

**Confirmado**

- `npm run qa:dashboard` gerou snapshot em `etc/qa-dashboard/runs/2026-04-19T16-51-22Z/`.
- O dashboard ficou vermelho somente por `frontend-cobertura`: statements 94,58%, branches 87,57%, functions 94,42%.
- `node etc/scripts/sgc.js frontend cobertura priorizar-defensivos` apontou lacunas residuais pequenas em `SubprocessoView.vue`, `useAcesso.ts`, `ArvoreUnidades.vue`, `usePerfil.ts` e `DisponibilizarMapaModal.vue`.
- Apos testes direcionados em `usePerfil`, `useAcesso` e `DisponibilizarMapaModal`, `npm run coverage:unit --prefix frontend` ficou em statements 94,64%, branches 87,73%, functions 94,54%; o bloqueio real continua sendo branch coverage global.

**Falta fazer**

- Atacar primeiro arquivos com alto impacto de branches, nao apenas lacunas residuais pequenas.
- Manter os testes direcionados ja adicionados, pois removeram parte da fila de defensivos.
- Atualizar testes sem mascarar comportamento nem relaxar thresholds.
- Rodar `npm run qa:dashboard` apos a correção para confirmar retorno a verde.

**Conclui quando**

- O snapshot mais recente do dashboard ficar verde sem reaproveitar relatorios antigos manualmente.
