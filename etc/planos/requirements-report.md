# Relatório de Situação dos Requisitos — SGC

**Data de atualização:** 2026-05-21  
**Escopo deste backlog:** manter apenas o que ainda falta tratar após a rodada atual de correções dos requisitos.

---

## Sumário executivo

As principais inconsistências documentais já tratadas nesta rodada foram:

- `CDU-01` e `CDU-29`: histórico para `SERVIDOR`, restrição de `Relatórios`, navbar do `ADMIN` e texto fixo do perfil;
- `CDU-05`: assunto do e-mail consolidado de revisão;
- `CDU-10`, `CDU-13`, `CDU-20`, `CDU-32`, `CDU-33` e `CDU-35`: numeração, rótulos de ação, textos de alerta e
  ambiguidade de datas do relatório de andamento.

O backlog abaixo mantém somente o que ainda está pendente.

---

## 1. Verificações ainda pendentes

### 1.1 Tratar a lacuna sistêmica de cobertura de comunicações nos testes

Depois das rodadas mais recentes, a lacuna sistêmica remanescente ficou concentrada principalmente em observabilidade de
comunicação nos testes E2E. Na integração backend, os principais fluxos não ligados a Diagnóstico já têm boa cobertura
de `assunto`, `corpo`, destinatários e fila/outbox.

O padrão residual recorrente é:

- cobertura E2E limitada ao fluxo principal, sem observar a comunicação gerada;
- falta de um mecanismo reutilizável para inspecionar envio de e-mails sem acoplar os cenários ao SMTP de teste;
- oportunidade de validar essas comunicações pela própria view administrativa de notificações.

Já existe um apoio inicial para triagem em [auditar-cobertura-notificacoes.mjs](/Users/leonardo/sgc/etc/scripts/auditar-cobertura-notificacoes.mjs:1).

CDUs fora de Diagnóstico que ainda aparecem com essa lacuna residual de E2E:

- `CDU-13`
- `CDU-14`
- `CDU-20`
- `CDU-21`
- `CDU-28`

---

## 2. Prioridade recomendada

### Alta

- Planejar a correção gradual da cobertura de comunicações em testes de integração dos CDUs que enviam e-mail.
