# Matriz de E-mails por CDU

Base primária para revisão de conteúdo dos e-mails do SGC: arquivos `etc/reqs/cdu-xx.md`.

Objetivo:
- usar os CDUs como fonte de verdade para assunto e corpo;
- tratar `regras-negocio.md` apenas como consolidação derivada;
- revisar visual e conteúdo separadamente.

## Início e finalização de processo

| Evento | Template atual | Fonte primária |
|---|---|---|
| Início de processo para unidade participante | `email-inicio-processo-consolidado.html` com `isParticipante=true` | `cdu-04.md` item `12.1`, `cdu-05.md` item `12.1` |
| Início de processo consolidado para unidade superior/intermediária | `email-inicio-processo-consolidado.html` com `isParticipante=false` | `cdu-04.md` item `12.2`, `cdu-05.md` item `12.2` |
| Finalização de processo para unidade participante | `processo-finalizado-por-unidade.html` | `cdu-21.md` item `9.1` |
| Finalização de processo consolidada para superiores/intermediárias | `processo-finalizado-unidades-subordinadas.html` | `cdu-21.md` item `9.2` |

## Transições de cadastro

| Evento | Template atual | Fonte primária |
|---|---|---|
| Cadastro disponibilizado | `cadastro-disponibilizado.html` | `cdu-09.md` item `12.4` |
| Cadastro disponibilizado para unidade superior | `cadastro-disponibilizado-superior.html` | `cdu-09.md` item `13` |
| Cadastro devolvido | `cadastro-devolvido.html` | `cdu-13.md` item `9.9` |
| Cadastro devolvido para unidade superior | `cadastro-devolvido-superior.html` | `cdu-13.md` item `10.7` |
| Cadastro aceito | `aceite-cadastro.html` | `cdu-22.md` item `8.4` |
| Cadastro aceito para unidade superior | `aceite-cadastro-superior.html` | `cdu-22.md` item `8.4` |
| Cadastro homologado | Sem template ativo em `TipoTransicao` | `cdu-23.md` item `8.4` |
| Cadastro reaberto | `cadastro-reaberto.html` | `cdu-32.md` |
| Cadastro reaberto para unidade superior | `cadastro-reaberto-superior.html` | `cdu-32.md` |

## Transições de revisão de cadastro

| Evento | Template atual | Fonte primária |
|---|---|---|
| Revisão de cadastro disponibilizada | `disponibilizacao-revisao-cadastro.html` | `cdu-10.md` item `13.4` |
| Revisão de cadastro disponibilizada para unidade superior | `disponibilizacao-revisao-cadastro-superior.html` | `cdu-10.md` item `14` |
| Revisão de cadastro devolvida | `devolucao-revisao-cadastro.html` | `cdu-14.md` item `10.9` |
| Revisão de cadastro devolvida para unidade superior | `devolucao-revisao-cadastro-superior.html` | `cdu-14.md` item `11.7` |
| Revisão de cadastro aceita | `aceite-revisao-cadastro.html` | `cdu-22.md` ou fluxo equivalente de revisão em bloco |
| Revisão de cadastro aceita para unidade superior | `aceite-revisao-cadastro-superior.html` | `cdu-22.md` ou fluxo equivalente de revisão em bloco |
| Revisão de cadastro homologada | Sem template ativo em `TipoTransicao` | `cdu-23.md` ou fluxo equivalente de revisão em bloco |
| Revisão de cadastro reaberta | `revisao-cadastro-reaberta.html` | `cdu-33.md` |
| Revisão de cadastro reaberta para unidade superior | `revisao-cadastro-reaberta-superior.html` | `cdu-33.md` |

## Transições de mapa

| Evento | Template atual | Fonte primária |
|---|---|---|
| Mapa disponibilizado | `mapa-disponibilizado.html` | `cdu-17.md` item `15`, `cdu-24.md` item `9.4` |
| Mapa disponibilizado para unidade superior | `mapa-disponibilizado-superior.html` | `cdu-17.md` item `16`, `cdu-24.md` item `9.7` |
| Sugestões apresentadas para o mapa | `sugestoes-mapa.html` | `cdu-19.md` item `4.4` |
| Sugestões apresentadas para unidade superior | `sugestoes-mapa-superior.html` | `cdu-19.md` item `4.4` |
| Mapa validado | `validacao-mapa.html` | `cdu-19.md` item `5.4` |
| Mapa validado para unidade superior | `validacao-mapa-superior.html` | `cdu-19.md` item `5.4` |
| Validação de mapa devolvida | `devolucao-validacao.html` | `cdu-20.md` item `8.9` |
| Validação de mapa devolvida para unidade superior | `devolucao-validacao-superior.html` | `cdu-20.md` item `9.7` |
| Validação de mapa aceita | `aceite-validacao.html` | `cdu-25.md` item `8.4` |
| Validação de mapa aceita para unidade superior | `aceite-validacao-superior.html` | `cdu-25.md` item `8.4` |
| Mapa homologado | Sem template ativo em `TipoTransicao` | `cdu-26.md` item `9.4` |

## Outros eventos

| Evento | Template atual | Fonte primária |
|---|---|---|
| Alteração de data limite | `data-limite-alterada.html` | `cdu-27.md` item `6` |
| Lembrete de prazo | `lembrete-prazo.html` | `cdu-34.md` item `7` |
| Atribuição temporária | `atribuicao-temporaria.html` | `cdu-28.md` item `9` |

## Atenções encontradas no código

- `processo-iniciado.html`, `processo-finalizado.html`, `email-inicio-processo-operacional.html` e `email-inicio-processo-intermediario.html` existem em `templates/email`, mas o fluxo principal hoje usa:
  - `email-inicio-processo-consolidado.html`;
  - `processo-finalizado-por-unidade.html`;
  - `processo-finalizado-unidades-subordinadas.html`.
- Em `TipoTransicao`, alguns eventos relevantes não têm template ativo (`CADASTRO_HOMOLOGADO`, `REVISAO_CADASTRO_HOMOLOGADA`, `MAPA_HOMOLOGADO`), embora haja CDUs com modelo de e-mail correspondente.
- Próximo passo recomendado:
  1. revisar os assuntos e o corpo dos templates ativos contra os CDUs acima;
  2. identificar templates legados/órfãos;
  3. decidir se os templates sem uso atual devem ser removidos ou religados ao fluxo.
