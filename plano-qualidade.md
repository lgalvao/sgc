# Plano de Qualidade do Sistema

Este é o plano principal de continuidade do trabalho de qualidade estrutural do SGC.

Ele deve conter apenas o que ainda falta fazer, em ordem de prioridade.

Documento complementar:

- [Plano de Qualidade - Diretrizes](/Users/leonardo/sgc/etc/docs/plano-qualidade-diretrizes.md)

## Objetivo

Elevar a clareza e a consistência do sistema inteiro, com foco em:

- borda HTTP explícita;
- integração backend/frontend previsível;
- redução de acoplamento estrutural;
- redução de responsabilidades misturadas;
- critérios estáveis para evolução futura.

## Critério de priorização

Uma frente sobe de prioridade quando reduz pelo menos um destes riscos:

- contrato HTTP ambíguo ou frouxo;
- vazamento de domínio para a API;
- service/controller/facade com responsabilidades demais;
- tratamento de erro incoerente;
- duplicação conceitual;
- oscilação entre dois padrões concorrentes.

## Estado alvo

O trabalho estará suficientemente maduro quando:

- contratos HTTP forem explícitos e previsíveis;
- o frontend deixar de compensar contrato ruim com fallback silencioso;
- módulos centrais do backend tiverem papéis mais nítidos;
- `JsonView` não orientar mais código novo de API;
- testes e documentação passarem a segurar a direção sem depender de memória oral.

## Frentes pendentes

### Frente 1. Fechar o restante da borda HTTP do backend

Objetivo:

- eliminar os contratos de API que ainda expõem enums, tipos ou shapes internos de `model`.

Alvos prioritários:

- [backend/src/main/java/sgc/processo/dto/ProcessoDetalheDto.java](/Users/leonardo/sgc/backend/src/main/java/sgc/processo/dto/ProcessoDetalheDto.java)
- [backend/src/main/java/sgc/processo/dto/ProcessoResumoDto.java](/Users/leonardo/sgc/backend/src/main/java/sgc/processo/dto/ProcessoResumoDto.java)
- [backend/src/main/java/sgc/subprocesso/dto/MovimentacaoDto.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/dto/MovimentacaoDto.java)
- [backend/src/main/java/sgc/alerta/dto/NotificacaoSubprocessoResumoDto.java](/Users/leonardo/sgc/backend/src/main/java/sgc/alerta/dto/NotificacaoSubprocessoResumoDto.java)
- [backend/src/main/java/sgc/organizacao/dto/AtribuicaoDto.java](/Users/leonardo/sgc/backend/src/main/java/sgc/organizacao/dto/AtribuicaoDto.java)
- [backend/src/main/java/sgc/seguranca/dto/EntrarResponse.java](/Users/leonardo/sgc/backend/src/main/java/sgc/seguranca/dto/EntrarResponse.java)
- [backend/src/main/java/sgc/seguranca/dto/PerfilUnidadeDto.java](/Users/leonardo/sgc/backend/src/main/java/sgc/seguranca/dto/PerfilUnidadeDto.java)

Saída esperada:

- DTOs HTTP sem dependência direta de `..model..`;
- mapeamento concentrado em mappers ou services de visualização;
- contratos mais estáveis para o frontend.

### Frente 2. Migrar o legado de `JsonView` que ainda influencia API

Objetivo:

- impedir que o legado de serialização condicional continue determinando a forma dos contratos.

Alvos prioritários:

- [backend/src/main/java/sgc/configuracoes/model/Configuracao.java](/Users/leonardo/sgc/backend/src/main/java/sgc/configuracoes/model/Configuracao.java)
- contratos correlatos de `organizacao` ainda apoiados em `JsonView`
- respostas de `mapa` que ainda dependem de views mesmo já estando em DTO

Saída esperada:

- `configuracoes` no mesmo padrão dos módulos já migrados;
- documentação e testes cobrindo a direção sem exceção implícita;
- `JsonView` restrito a legado controlado e `e2e`.

### Frente 3. Reduzir god objects do backend

Objetivo:

- separar responsabilidades reais nos hubs mais caros do sistema.

Alvos prioritários:

- [backend/src/main/java/sgc/processo/service/ProcessoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/processo/service/ProcessoService.java)
- [backend/src/main/java/sgc/subprocesso/SubprocessoController.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/SubprocessoController.java)
- [backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java)
- [backend/src/main/java/sgc/relatorio/RelatorioFacade.java](/Users/leonardo/sgc/backend/src/main/java/sgc/relatorio/RelatorioFacade.java)
- [backend/src/main/java/sgc/e2e/E2eController.java](/Users/leonardo/sgc/backend/src/main/java/sgc/e2e/E2eController.java)

Saída esperada:

- classes menores por responsabilidade real;
- menos dependências por classe;
- menor custo de navegação e manutenção.

### Frente 4. Endurecer a integração frontend/backend

Objetivo:

- fazer o frontend falhar cedo quando o contrato estiver errado, em vez de compensar silenciosamente.

Alvos prioritários:

- serviços TS ainda com tipos frouxos, `as`, `!`, fallbacks implícitos;
- áreas de `subprocesso`, `processo` e autenticação;
- uso progressivo dos tipos gerados por OpenAPI onde isso reduzir ambiguidade real.

Saída esperada:

- menos normalização defensiva no cliente;
- menos defaults silenciosos;
- contratos compartilhados mais claros.

### Frente 5. Reorganizar tratamento de erro e permissões

Objetivo:

- remover ruído e inconsistência entre erro técnico, erro de domínio, cancelamento e regra de acesso.

Alvos prioritários:

- [frontend/src/axios-setup.ts](/Users/leonardo/sgc/frontend/src/axios-setup.ts)
- composables/telas com tratamento paralelo de erro;
- pontos do backend em que resposta de erro ainda mistura camadas;
- fluxos de permissão em `subprocesso`, `processo` e autenticação.

Saída esperada:

- tratamento de erro mais uniforme;
- permissão e falha com semântica mais previsível;
- menos duplicação de comportamento entre telas.

### Frente 6. Reduzir duplicação conceitual

Objetivo:

- consolidar regras equivalentes espalhadas em lugares diferentes.

Alvos prioritários:

- montagem de DTO/resposta em múltiplos services;
- regras de status, elegibilidade e contexto repetidas;
- duplicações em fixtures e suporte `e2e`;
- normalização repetida entre frontend e backend.

Saída esperada:

- menos lógica repetida;
- menos divergência entre fluxos equivalentes;
- base mais fácil de evoluir.

### Frente 7. Continuar codificando a direção em guardrails

Objetivo:

- transformar a direção arquitetural em testes e documentação, não em memória implícita.

Pendências principais:

- expandir ArchUnit conforme módulos forem migrados;
- atualizar READMEs de módulo quando uma frente fechar;
- revisar o toolkit `sgc.js` para manter métricas úteis, sem virar fim em si mesmo.

Saída esperada:

- menor risco de regressão de direção;
- menor chance de agentes futuros retomarem padrão antigo.

## Sequência recomendada

1. Fechar o restante da borda HTTP do backend.
2. Migrar o legado de `JsonView` que ainda influencia API.
3. Reduzir god objects centrais do backend.
4. Endurecer integração frontend/backend nos fluxos mais críticos.
5. Reorganizar tratamento de erro e permissões.
6. Consolidar duplicações conceituais remanescentes.
7. Continuar transformando a direção em testes e documentação.

## Regra operacional

Cada rodada deve:

- atacar um corte semântico real;
- deixar o sistema mais claro do que estava antes;
- validar o subconjunto afetado;
- evitar criação de nova exceção conceitual;
- atualizar teste/documentação quando a direção ficar mais explícita.
