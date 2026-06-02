# Continuidade do Trabalho de Qualidade do Sistema

Este documento existe para evitar regressão de direção.

Ele deve permitir que qualquer agente ou desenvolvedor entre no projeto sem contexto prévio e continue o trabalho de qualidade do SGC sem:

- reinterpretar errado o objetivo;
- reintroduzir padrões que já foram considerados problemáticos;
- confundir “passar na auditoria” com melhoria real;
- voltar a usar atalhos que aumentam ambiguidade estrutural.

## 1. Objetivo real

O objetivo deste trabalho não é melhorar score de auditoria, cobertura nominal ou aparência arquitetural.

O objetivo real é aumentar:

- clareza;
- consistência;
- previsibilidade;
- capacidade de evolução segura;
- qualidade da integração backend/frontend.

Se uma mudança melhora métrica mas piora entendimento, ela está na direção errada.

## 2. Princípio central

A regra principal é:

> em caso de dúvida, preferir contrato explícito, responsabilidade explícita e dependência explícita.

Aplicações práticas:

- preferir DTO explícito a reaproveitamento de entidade;
- preferir mapper explícito a lógica de montagem escondida no DTO;
- preferir contrato estável a enum de domínio vazando para a API;
- preferir classe com responsabilidade clara a hub “conveniente”;
- preferir documentação curta e inequívoca a convenção implícita.

## 3. O que já foi decidido e não deve ser reinterpretado

### 3.1. Borda HTTP deve ser explícita

A direção oficial do backend é:

- controllers recebem request DTO;
- controllers respondem response DTO;
- `@RequestBody` não recebe entidade JPA;
- DTO HTTP não deve expor tipos de `..model..` diretamente;
- a tradução entre domínio e API deve ficar em mapper ou service de visualização/contexto.

Referência:

- [Borda HTTP do backend](/Users/leonardo/sgc/etc/docs/backend-borda-http.md)
- [ADR 0001 - Contratos HTTP Explícitos no Backend](/Users/leonardo/sgc/etc/docs/adr/0001-contratos-http-explicitos.md)

### 3.2. `JsonView` não é o padrão da borda

`JsonView` foi identificado como herança de uma direção anterior, baseada em reaproveitamento excessivo da mesma estrutura para múltiplos contratos.

Direção atual:

- não usar `JsonView` como mecanismo principal de montagem de contrato HTTP nos módulos já migrados;
- `JsonView` pode continuar existindo em legado e no adapter `e2e`, mas não deve orientar código novo;
- se houver dúvida entre reaproveitar um objeto com `JsonView` ou declarar contrato explícito, a decisão correta é declarar contrato explícito.

Referência:

- [ADR 0002 - Conter JsonView no Legado de Model e no Adapter E2E](/Users/leonardo/sgc/etc/docs/adr/0002-conter-jsonview-no-adapter-e2e.md)

### 3.3. Melhorar qualidade não é “burlar auditoria”

Auditorias, grep, Semgrep, ArchUnit e scripts existem para ajudar a localizar dívida, não para ditar soluções cosméticas.

Exemplos do que não fazer:

- trocar import por FQN só para a regra parar de acusar;
- mover lógica de lugar sem reduzir acoplamento real;
- fragmentar classe só para cair abaixo de limite artificial;
- manter fallback silencioso só para evitar quebra de teste;
- adaptar teste para aceitar contrato pior.

A pergunta correta é sempre:

> a mudança deixou o sistema mais claro, mais consistente e menos acoplado?

## 4. Guardrails já codificados

Hoje o repositório já possui proteção explícita para essa direção.

### 4.1. ArchUnit

Arquivo:

- [ArchConsistencyTest.java](/Users/leonardo/sgc/backend/src/test/java/sgc/arquitetura/ArchConsistencyTest.java)

Regras relevantes já existentes:

- controllers não expõem entidades JPA;
- controllers não recebem entidades JPA em `@RequestBody`;
- controllers não expõem tipos internos de `model` no contrato HTTP;
- controllers da aplicação não usam `@JsonView` fora do adapter `e2e`;
- DTOs dos módulos já migrados (`processo`, `subprocesso`, `seguranca`, `alerta`) não devem usar `@JsonView`.

### 4.2. Documentação

Arquivos de referência:

- [backend-borda-http.md](/Users/leonardo/sgc/etc/docs/backend-borda-http.md)
- [backend/README.md](/Users/leonardo/sgc/backend/README.md)
- [subprocesso/README.md](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/README.md)
- [Plano de Qualidade do Sistema](/Users/leonardo/sgc/plano-qualidade.md)

## 5. Como continuar sem risco de inverter a direção

Antes de começar qualquer rodada, siga esta ordem:

1. Ler este documento.
2. Ler [backend-borda-http.md](/Users/leonardo/sgc/etc/docs/backend-borda-http.md).
3. Conferir as regras atuais em [ArchConsistencyTest.java](/Users/leonardo/sgc/backend/src/test/java/sgc/arquitetura/ArchConsistencyTest.java).
4. Medir o problema real antes de editar.
5. Escolher o corte com maior ganho estrutural, não o mais fácil.

## 6. Critério para escolher a próxima rodada

Escolha trabalhos que reduzam pelo menos um destes problemas:

- vazamento de domínio na borda HTTP;
- falta de clareza de contrato backend/frontend;
- responsabilidades misturadas em controller/service/facade;
- duplicação conceitual de montagem de resposta;
- inconsistência de nomenclatura ou shape entre fluxos equivalentes;
- tratamento de erro ambíguo ou espalhado.

Evite rodadas dominadas por:

- script novo sem ganho estrutural imediato;
- ajuste cosmético de lint;
- micro-refatoração isolada fora de hotspot;
- documentação sem contrapartida prática;
- troca de padrão por preferência pessoal.

## 7. Forma correta de executar uma rodada

### 7.1. Medir antes

Use primeiro sinais existentes do repositório:

- `./gradlew :backend:test --tests 'sgc.arquitetura.ArchConsistencyTest' --console=plain`
- `rg` sobre DTOs, `model`, `JsonView`, controllers e mappers
- testes de integração do módulo afetado

### 7.2. Editar em cortes semânticos

A rodada deve ser pequena o suficiente para validar com segurança, mas grande o suficiente para gerar ganho estrutural real.

Boa unidade de corte:

- um contrato HTTP inteiro;
- um grupo de DTOs correlatos;
- uma responsabilidade concreta extraída de service/facade;
- uma família coerente de inconsistências.

Má unidade de corte:

- “trocar 40 arquivos porque apareceu no grep”;
- “apagar warnings”;
- “passar regra sem entender o papel da classe”.

### 7.3. Validar depois

Sempre validar o subconjunto mais diretamente afetado.

Exemplos:

- testes de arquitetura;
- testes de controller;
- testes de integração do fluxo afetado;
- typecheck do frontend se o contrato compartilhado mudou.

## 8. Heurísticas de decisão

### 8.1. Quando criar mapper

Crie mapper quando:

- o contrato HTTP difere do modelo interno;
- existe conversão de enum para string;
- há resumo de relacionamento;
- o mesmo domínio aparece em mais de um shape;
- o DTO estava com `fromEntity`, `fromLeitura` ou lógica de montagem embutida.

Não crie mapper só por formalismo, quando o objeto já é um request simples e não há tradução relevante.

### 8.2. Quando trocar enum por string no contrato

Troque por `String` quando:

- o frontend só precisa do valor serializado;
- a enum pertence ao domínio interno e não deveria ser importada pelo contrato;
- a mudança reduz acoplamento sem perda de semântica externa.

Mantenha enum no contrato apenas quando ela for, de fato, parte estável e intencional da API.

### 8.3. Quando documentar

Documente quando:

- a decisão pode ser reinterpretada no futuro;
- existe risco de time/agente repetir padrão antigo;
- a regra é simples e recorrente;
- o custo de não documentar é nova oscilação arquitetural.

## 9. O que ainda está em transição

Nem todo o backend já está coerente com a direção nova.

Pontos que ainda exigem continuidade:

- `processo` ainda tem DTOs e detalhes de fluxo com acoplamento residual;
- `subprocesso` avançou bastante, mas ainda possui contratos e DTOs legados a revisar;
- `configuracoes` ainda não está no mesmo patamar da regra nova de `JsonView`;
- `MovimentacaoDto`, `ProcessoDetalheDto`, `ProcessoResumoDto`, `NotificacaoSubprocessoResumoDto` e correlatos continuam sendo bons candidatos de continuidade;
- há legado de `JsonView` em entidades/modelos que não deve ser expandido.

## 10. Regra de ouro para novos agentes

Se você entrou agora no projeto, siga esta instrução como default:

> não invente um novo padrão, não restaure um padrão antigo e não conclua que algo “já funciona” só porque tem teste. Continue a direção já consolidada: contratos explícitos, menos acoplamento, menos ambiguidade e mais consistência sistêmica.

## 11. Comandos úteis

Arquitetura:

```bash
./gradlew :backend:test --tests 'sgc.arquitetura.ArchConsistencyTest' --console=plain
```

Busca de sinais de acoplamento:

```bash
rg -n "JsonView|model\\." backend/src/main/java/sgc -g '*Dto.java' -g '*Response.java'
```

Busca de montagem embutida em DTO:

```bash
rg -n "fromEntity|fromLeitura|fromResumo" backend/src/main/java/sgc -g '*Dto.java' -g '*Response.java'
```

## 12. Resultado esperado ao final

O trabalho estará indo na direção certa quando:

- contratos HTTP puderem ser entendidos sem abrir entidade/modelo;
- fluxos equivalentes usarem o mesmo padrão de borda;
- `JsonView` deixar de orientar código novo;
- controllers e services ficarem semanticamente mais previsíveis;
- a integração frontend/backend quebrar cedo quando o contrato estiver errado, e não tarde demais por compensação silenciosa.
