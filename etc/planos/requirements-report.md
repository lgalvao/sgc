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

Há indícios concretos de que vários `CDUs` com envio de e-mail ainda têm testes de integração insuficientes para fechar
o contrato de comunicação. O padrão da lacuna recorrente é:

- validação de transição/estado sem validação de `assunto`, `corpo` e notificação enfileirada;
- ausência de diferenciação explícita entre destinatários/papéis quando o requisito prevê múltiplos envios;
- cobertura E2E frequentemente limitada ao fluxo principal, sem observar comunicação visível quando ela existe.

Já existe um apoio inicial para triagem em [auditar-cobertura-notificacoes.mjs](/Users/leonardo/sgc/etc/scripts/auditar-cobertura-notificacoes.mjs:1).

---

## 2. Diagnóstico: refinamentos ainda em aberto

O módulo de Diagnóstico já não é mais uma lacuna estrutural de requisitos. A pendência atual é de refinamento dentro da
própria série `CDU-39` a `CDU-53`.

Pontos ainda explicitamente abertos nos documentos:

- `cdu-39.md`: campos e modelos de comunicação ainda assumidos por paralelismo;
- `cdu-41.md`, `cdu-42.md`, `cdu-43.md`, `cdu-45.md`, `cdu-46.md`: hipóteses operacionais ainda marcadas para
  refinamento;
- `cdu-48.md` a `cdu-53.md`: monitoramento, ocupações críticas e relatórios ainda com recortes mínimos assumidos;
- `_intro-glossario.md`: conceitos de `gap de competência` e `ocupações críticas` ainda marcados para validação.

---

## 3. Prioridade recomendada

### Alta

- Planejar a correção gradual da cobertura de comunicações em testes de integração dos CDUs que enviam e-mail.

### Contínua

- Refinar os CDUs de Diagnóstico à medida que novas validações de negócio ocorrerem, preservando os marcadores
  `PENDÊNCIA DE REFINAMENTO` enquanto houver hipótese ainda não fechada.
