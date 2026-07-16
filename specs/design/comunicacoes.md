# Comunicações de Workflow

Este documento consolida as regras transversais de destinatários de movimentações, alertas e notificações por e-mail nos
fluxos de subprocessos do SGC.

Os casos de uso continuam sendo a fonte primária do comportamento funcional de cada ação. Este documento formaliza as
regras comuns de comunicação, com o objetivo de reduzir ambiguidades de implementação e permitir auditoria dos
destinatários.

## Conceitos

- **Unidade do subprocesso**: unidade participante responsável pelo trabalho principal do subprocesso. Pode ser
  operacional, interoperacional ou outro tipo de unidade participante previsto no processo.
- **Unidade de análise**: unidade onde o subprocesso está localizado e que executa a ação de analisar, aceitar,
  homologar ou devolver.
- **Unidade superior**: próxima unidade acima na cadeia hierárquica da unidade de referência indicada no caso de uso.
  Pode ser intermediária, interoperacional ou `ADMIN`.
- **Unidade de devolução**: unidade de origem da última movimentação do subprocesso.
- **Destino da movimentação**: unidade de destino registrada na movimentação gerada pela ação.
- **Destino do alerta**: unidade de destino registrada no alerta gerado pela ação.
- **Destino da notificação**: unidade que recebe o e-mail gerado pela ação.
- **Comunicação consolidada**: notificação ou alerta único que resume uma ação realizada sobre múltiplos subprocessos.

## Regras gerais

1. Cada ação de workflow deve definir explicitamente o destino da movimentação, do alerta e da notificação.
2. Nos fluxos de destinatário único, alerta e notificação devem ser paralelos à movimentação.
3. Não deve haver alerta nem notificação para a própria unidade que executou a ação.
4. A unidade `ADMIN` deve ser tratada como qualquer outra unidade destinatária quando o subprocesso chega a ela para
   análise.
5. Quando `ADMIN` devolve, reabre ou pratica ação descendente, a comunicação deve ir para a unidade destinatária da
   ação, conforme o caso de uso.
6. Homologações não geram alertas nem notificações.
7. Comunicações consolidadas só podem existir nos casos listados neste documento. Elas não podem ser inferidas a partir
   da movimentação.
8. Finalização de processo e início de processo são comunicações de processo, não exceções das regras de homologação ou
   análise de subprocesso.

## Fluxos de Destinatário Único

| Fluxo              | Ação                                            | Destino da movimentação | Destino do alerta      | Destino da notificação | Fonte  |
|--------------------|-------------------------------------------------|-------------------------|------------------------|------------------------|--------|
| Mapeamento         | Disponibilizar cadastro (`CHEFE`)               | Unidade superior        | Unidade superior       | Unidade superior       | CDU-09 |
| Mapeamento         | Devolver cadastro (`ADMIN`/`GESTOR`)            | Unidade de devolução    | Unidade de devolução   | Unidade de devolução   | CDU-13 |
| Mapeamento         | Aceitar cadastro (`GESTOR`)                     | Unidade superior        | Unidade superior       | Unidade superior       | CDU-13 |
| Mapeamento         | Homologar cadastro (`ADMIN`)                    | Unidade ADMIN           | Não gera               | Não gera               | CDU-13 |
| Revisão            | Disponibilizar revisão de cadastro (`CHEFE`)    | Unidade superior        | Unidade superior       | Unidade superior       | CDU-10 |
| Revisão            | Devolver revisão de cadastro (`ADMIN`/`GESTOR`) | Unidade de devolução    | Unidade de devolução   | Unidade de devolução   | CDU-14 |
| Revisão            | Aceitar revisão de cadastro (`GESTOR`)          | Unidade superior        | Unidade superior       | Unidade superior       | CDU-14 |
| Revisão            | Homologar cadastro com impacto (`ADMIN`)        | Unidade ADMIN           | Não gera               | Não gera               | CDU-14 |
| Mapeamento/Revisão | Disponibilizar mapa (`ADMIN`)                   | Unidade do subprocesso  | Unidade do subprocesso | Unidade do subprocesso | CDU-17 |
| Mapeamento/Revisão | Disponibilizar mapas em bloco (`ADMIN`)         | Unidade do subprocesso  | Unidade do subprocesso | Unidade do subprocesso | CDU-24 |
| Mapeamento/Revisão | Devolver validação de mapa (`ADMIN`)            | Unidade de devolução    | Unidade de devolução   | Unidade de devolução   | CDU-20 |
| Mapeamento/Revisão | Aceitar validação de mapa (`GESTOR`)            | Unidade superior        | Unidade superior       | Unidade superior       | CDU-20 |
| Mapeamento/Revisão | Homologar validação de mapa (`ADMIN`)           | Unidade ADMIN           | Não gera               | Não gera               | CDU-20 |
| Mapeamento/Revisão | Homologar validação de mapas em bloco (`ADMIN`) | Unidade ADMIN           | Não gera               | Não gera               | CDU-26 |
| Diagnóstico        | Devolver diagnóstico (`ADMIN`/`GESTOR`)         | Unidade de devolução    | Unidade de devolução   | Unidade de devolução   | CDU-50 |
| Diagnóstico        | Aceitar diagnóstico (`GESTOR`)                  | Unidade superior        | Unidade superior       | Unidade superior       | CDU-50 |
| Diagnóstico        | Homologar diagnóstico (`ADMIN`)                 | Unidade ADMIN           | Não gera               | Não gera               | CDU-50 |
| Diagnóstico        | Homologar diagnósticos em bloco (`ADMIN`)       | Unidade ADMIN           | Não gera               | Não gera               | CDU-52 |

## Comunicações Consolidadas

Os casos abaixo possuem comunicações consolidadas explicitamente previstas nos casos de uso. Essas comunicações devem
ser implementadas como regras próprias, sem reaproveitar heurísticas genéricas como "notificar a unidade de destino" ou
"notificar a unidade superior".

| Fluxo              | Ação                                           | Comunicação principal | Comunicação consolidada                                              | Fonte  |
|--------------------|------------------------------------------------|-----------------------|----------------------------------------------------------------------|--------|
| Mapeamento/Revisão | Aceitar cadastros em bloco (`GESTOR`)          | Unidade superior      | Notificação consolidada para a Unidade superior                      | CDU-22 |
| Mapeamento/Revisão | Aceitar validação de mapas em bloco (`GESTOR`) | Unidade superior      | Notificação consolidada para a Unidade superior                      | CDU-25 |
| Diagnóstico        | Aceitar diagnósticos em bloco (`GESTOR`)       | Unidade superior      | Notificação consolidada e alerta consolidado para a Unidade superior | CDU-51 |

## Consequências para implementação e testes

1. Fluxos de destinatário único devem usar a mesma resolução de destino para movimentação, alerta e notificação.
2. Comunicações consolidadas devem ter métodos, nomes e testes próprios, em vez de surgirem por efeito colateral da
   movimentação.
3. Testes unitários e integrados devem validar os destinatários exatos de alertas e notificações, não apenas a
   existência de registros.
4. Testes de regressão devem garantir que nenhuma ação gere alerta ou notificação para a própria unidade que executou a
   ação.
5. Testes de homologação devem garantir ausência de alertas e notificações.
