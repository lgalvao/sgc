# Plano de racionalizacao de desempenho

## Papel

Documento central da iniciativa. Deve explicar a direcao e os criterios de decisao, nao acompanhar rodada a rodada.

O acompanhamento operacional fica em [plano-racionalizacao-tracking.md](plano-racionalizacao-tracking.md).

## Objetivo

Entregar a mesma experiencia funcional com menos trabalho total:

- menos chamadas redundantes no frontend;
- menos recomposicao de contexto;
- menos round-trips sem ganho funcional;
- menos custo sincronico em workflows.

O foco e jornada real medida, nao micro-otimizacao isolada.

## Estado confirmado

- A descoberta generica esta encerrada; novas acoes devem partir de fluxo concreto.
- O painel nao e mais frente aberta.
- `detalhes` de processo ja foi separado de `contexto-completo` quanto a elegiveis e acoes em bloco.
- `contexto-completo` ainda sustenta `ProcessoDetalheView` para acoes em bloco e permissao de finalizacao.
- O contexto inicial de subprocesso ja tem helper compartilhado no frontend.
- `SubprocessoConsultaService` ja delega parte da montagem de contexto, mas ainda concentra permissoes e resposta.
- `MapaVisualizacaoView` ja teve sugestoes/historico e acoes de analise simplificados.
- A abertura de visualizacao de mapa foi medida em `CDU-18` com `SGC_MONITORAMENTO=sim`: o contrato atual em dois endpoints (`contexto-edicao` e `mapa-visualizacao`) custou dezenas de milissegundos por request no cenario E2E, sem evidencia suficiente para criar um BFF unico.
- O tracing de workflow e acao em bloco ja existe e depende de monitoramento ativo na requisicao.
- A importacao de atividades passou a retornar resposta incremental e nao precisa mais recompor `contexto-cadastro-atividades` apos o `POST`.
- As stores de dedupe/cache de processo e subprocesso ja têm cobertura direcionada para dedupe, erro e invalidacao.
- A varredura por scripts apontou dividas amplas que devem ser tratadas por fatias pequenas: nullability defensiva em DTOs/backend, `id` legado fora das excecoes de framework e `any` em testes frontend.

## Diretrizes

1. Priorizar jornada real medida antes de suite ampla.
2. Atacar apenas itens vivos no tracking.
3. Preferir eliminar chamada, recomposicao ou efeito sincronico antes de otimizar query.
4. Consolidar contrato somente quando a tela depender de dados estaveis em conjunto.
5. Evitar cache de contexto quando o dado muda com workflow; usar dedupe concorrente quando bastar.
6. Medir antes e depois com o mesmo cenario monitorado.
7. Nao fundir endpoints por conveniencia se a medicao nao demonstrar ganho material.
8. Tratar dividas transversais por hotspot e contrato, nao por substituicao mecanica em massa.
9. Atualizar este plano apenas quando mudar direcao, prioridade ou criterio de sucesso.

## Frentes abertas

- **Processo:** decidir se `contexto-completo` deve continuar carregando acoes em bloco/elegiveis no primeiro load.
- **Subprocesso:** reduzir recomposicao de `contexto-edicao` e continuar quebrando responsabilidades do hotspot de consulta.
- **Workflow:** medir p50/p95/pico por acao e remover custo sincronico evitavel.
- **Mapa visualizacao:** manter dois endpoints por ora; avaliar somente se condicionais de acao devem vir prontas do backend.
- **Contratos:** reduzir nullability defensiva e nomenclatura `id` legado apenas onde houver contrato de dominio claro para `codigo`.
- **QA:** recuperar o dashboard para verde atacando lacunas residuais de cobertura frontend com menor risco.

## Criterio de sucesso

A iniciativa fecha quando as jornadas prioritarias fizerem menos chamadas, processo/subprocesso pararem de recompor contexto sem necessidade, workflows criticos perderem custo sincronico evitavel e os contratos ficarem mais previsiveis para evolucao.
