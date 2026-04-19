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

## P2: fragmentacao da visualizacao de mapa

**Confirmado**

- `MapaVisualizacaoView` carrega contexto inicial de subprocesso e depois chama `GET /subprocessos/{codigo}/mapa-visualizacao`.
- O contexto inicial fornece permissoes e dados do subprocesso; o endpoint de mapa fornece competencias, atividades sem competencia e sugestoes.
- A tela ainda combina esses contratos para montar a experiencia final.

**Falta fazer**

- Medir a abertura de visualizacao de mapa com monitoramento para quantificar os dois requests.
- Decidir se a separacao deve continuar por clareza de contrato ou se cabe BFF unico para a tela.
- Evitar fusao de endpoints se o ganho medido for pequeno ou se aumentar acoplamento do backend.

**Conclui quando**

- A tela tiver contrato de carga inicial justificado por medicao: dois endpoints claros ou um BFF unico com ganho comprovado.

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
