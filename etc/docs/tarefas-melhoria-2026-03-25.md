# Tarefas propostas após varredura rápida do código (2026-03-25)

## 1) Corrigir typo
**Tipo:** typo

**Problema observado**
Há um comentário TODO com erro de digitação (`traduziros simbolos`) no composable de loading.

**Tarefa proposta**
Corrigir o texto para português correto, por exemplo: `// TODO traduzir os símbolos para português`.

**Critério de aceite**
- Não há mais erro ortográfico no comentário.
- O arquivo continua passando no lint/typecheck.

---

## 2) Corrigir bug de tipagem/contrato no mapeamento de subprocesso
**Tipo:** bug

**Problema observado**
A função `mapSubprocessoDetalheDtoToModel(dto: any): SubprocessoDetalhe` retorna `null as any` quando `dto` é nulo.
Isso quebra o contrato da função (retorno não nulo), pode mascarar erro de integração e gerar falhas em tempo de execução quando o chamador assume objeto válido.

**Tarefa proposta**
Refatorar a função para ter contrato explícito e seguro, com uma destas abordagens:
- retornar `SubprocessoDetalhe | null` e ajustar os chamadores; ou
- lançar erro de domínio quando `dto` vier inválido e tratar no fluxo de erro.

**Critério de aceite**
- Não existe mais `null as any` no mapeamento.
- Tipos refletem corretamente o comportamento real.
- Testes cobrindo cenário de DTO ausente/ inválido.

---

## 3) Corrigir discrepância de documentação (comandos de execução/qualidade)
**Tipo:** documentação

**Problema observado**
O README aponta comandos/caminhos inconsistentes com o repositório:
- `node e1e/lifecycle.js` (pasta correta é `e2e/`).
- `./quality-check.sh` na raiz, mas o script existente está em `etc/scripts/quality-check.sh`.

**Tarefa proposta**
Atualizar README para comandos executáveis e alinhados com a estrutura real do projeto.

**Critério de aceite**
- Comandos documentados funcionam quando executados a partir da raiz.
- Caminhos no README batem com arquivos existentes no repositório.

---

## 4) Melhorar teste E2E do CDU-07 (ordenação de movimentações)
**Tipo:** melhoria de teste

**Problema observado**
A análise de alinhamento indica lacuna: o teste não valida a ordem decrescente por data/hora das movimentações.
No `cdu-07.spec.ts`, há validações de presença de linhas e textos, mas não da ordenação temporal.

**Tarefa proposta**
Adicionar asserções para capturar os valores da coluna `Data/hora`, converter para data e validar ordenação decrescente (mais recente primeiro).

**Critério de aceite**
- O teste falha se a ordenação for alterada para crescente/aleatória.
- O teste passa de forma estável em execuções repetidas.
