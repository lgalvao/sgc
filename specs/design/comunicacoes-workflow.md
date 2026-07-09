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
- **Unidade imediatamente superior**: próxima unidade acima na cadeia hierárquica da unidade de referência indicada no
  caso de uso. Pode ser intermediária, interoperacional ou `ADMIN`.
- **Unidade de devolução**: unidade de origem da última movimentação do subprocesso.
- **Destino da movimentação**: unidade de destino registrada na movimentação gerada pela ação.
- **Destino do alerta**: unidade de destino registrada no alerta gerado pela ação.
- **Destino da notificação**: unidade que recebe o e-mail gerado pela ação.
- **Comunicação adicional**: alerta ou notificação que complementa a comunicação principal e possui destinatário
  explicitamente previsto no caso de uso.
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
7. Destinatários adicionais só podem existir nos casos listados como comunicação adicional ou consolidada. Eles não
   podem ser inferidos a partir da movimentação.
8. Finalização de processo e início de processo são comunicações de processo, não exceções das regras de homologação ou
   análise de subprocesso.

## Fluxos de Destinatário Único

| Fluxo | Ação | Destino da movimentação | Destino do alerta | Destino da notificação | Fonte |
| --- | --- | --- | --- | --- | --- |
| Mapeamento | Disponibilizar cadastro (`CHEFE`) | Unidade imediatamente superior | Unidade imediatamente superior | Unidade imediatamente superior | CDU-09 |
| Mapeamento | Devolver cadastro | Unidade de devolução | Unidade de devolução | Unidade de devolução | CDU-13 |
| Mapeamento | Aceitar cadastro (`GESTOR`) | Unidade imediatamente superior | Unidade imediatamente superior | Unidade imediatamente superior | CDU-13 |
| Mapeamento | Homologar cadastro (`ADMIN`) | `ADMIN` | Não gera | Não gera | CDU-13 |
| Revisão | Disponibilizar revisão de cadastro (`CHEFE`) | Unidade imediatamente superior | Unidade imediatamente superior | Unidade imediatamente superior | CDU-10 |
| Revisão | Devolver cadastro | Unidade de devolução | Unidade de devolução | Unidade de devolução | CDU-14 |
| Revisão | Aceitar revisão de cadastro (`GESTOR`) | Unidade imediatamente superior | Unidade imediatamente superior | Unidade imediatamente superior | CDU-14 |
| Revisão | Homologar cadastro com impacto (`ADMIN`) | `ADMIN` | Não gera | Não gera | CDU-14 |
| Mapeamento/Revisão | Devolver validação de mapa | Unidade de devolução | Unidade de devolução | Unidade de devolução | CDU-20 |
| Mapeamento/Revisão | Aceitar validação de mapa (`GESTOR`) | Unidade imediatamente superior | Unidade imediatamente superior | Unidade imediatamente superior | CDU-20 |
| Mapeamento/Revisão | Homologar validação de mapa (`ADMIN`) | `ADMIN` | Não gera | Não gera | CDU-20 |
| Mapeamento/Revisão | Homologar validação de mapas em bloco (`ADMIN`) | `ADMIN` | Não gera | Não gera | CDU-26 |
| Diagnóstico | Devolver diagnóstico | Unidade de devolução | Unidade de devolução | Unidade de devolução | CDU-50 |
| Diagnóstico | Aceitar diagnóstico (`GESTOR`) | Unidade imediatamente superior | Unidade imediatamente superior | Unidade imediatamente superior | CDU-50 |
| Diagnóstico | Homologar diagnóstico (`ADMIN`) | `ADMIN` | Não gera | Não gera | CDU-50 |
| Diagnóstico | Homologar diagnósticos em bloco (`ADMIN`) | `ADMIN` | Não gera | Não gera | CDU-52 |

## Comunicações Adicionais e Consolidadas

Os casos abaixo possuem destinatários adicionais explicitamente previstos nos casos de uso. Essas comunicações devem ser
implementadas como regras próprias, sem reaproveitar heurísticas genéricas como "notificar a unidade de destino" ou
"notificar a unidade superior".

| Fluxo | Ação | Comunicação principal | Comunicação adicional ou consolidada | Fonte |
| --- | --- | --- | --- | --- |
| Mapeamento/Revisão | Disponibilizar mapa (`ADMIN`) | Unidade do subprocesso | Notificação adicional para unidades superiores da unidade do subprocesso | CDU-17 |
| Mapeamento/Revisão | Disponibilizar mapas em bloco (`ADMIN`) | Unidade do subprocesso, por subprocesso | Notificação consolidada para cada unidade imediatamente superior agrupada | CDU-24 |
| Mapeamento/Revisão | Aceitar cadastros em bloco (`GESTOR`) | Unidade imediatamente superior, por movimentação e alerta | Notificação individual para a unidade do subprocesso e notificação consolidada para a unidade imediatamente superior | CDU-22 |
| Mapeamento/Revisão | Aceitar validação de mapas em bloco (`GESTOR`) | Unidade imediatamente superior, por movimentação e alerta | Notificação individual para a unidade do subprocesso e notificação consolidada para a unidade imediatamente superior | CDU-25 |
| Diagnóstico | Aceitar diagnósticos em bloco (`GESTOR`) | Unidade imediatamente superior, por movimentação | Notificação consolidada e alerta consolidado para a unidade imediatamente superior | CDU-51 |
| Mapeamento | Reabrir cadastro (`ADMIN`) | Unidade do subprocesso | Notificação e alerta adicionais para a unidade imediatamente superior | CDU-32 |
| Revisão | Reabrir revisão de cadastro (`ADMIN`) | Unidade do subprocesso | Notificação e alerta adicionais para a unidade imediatamente superior | CDU-33 |

## Consequências para implementação e testes

1. Fluxos de destinatário único devem usar a mesma resolução de destino para movimentação, alerta e notificação.
2. Comunicações adicionais devem ter métodos, nomes e testes próprios, em vez de surgirem por efeito colateral da
   movimentação.
3. Testes unitários e integrados devem validar os destinatários exatos de alertas e notificações, não apenas a
   existência de registros.
4. Testes de regressão devem garantir que nenhuma ação gere alerta ou notificação para a própria unidade que executou a
   ação.
5. Testes de homologação devem garantir ausência de alertas e notificações.
