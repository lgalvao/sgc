---
name: simplificacao-codigo
description: Use quando o objetivo for simplificar código no SGC com foco sistêmico, sem quebrar contratos, requisitos ou regras de acesso. Indicado para consolidar duplicações internas, reduzir acoplamento acidental, endurecer bordas HTTP, estreitar superfícies de services/composables/stores, eliminar efeitos colaterais escondidos, remover código morto e explicitar dependências em backend Java/Spring e frontend Vue/TypeScript.
---

# Simplificação Sistêmica no SGC

Use este skill para rodadas de simplificação incremental em código já existente, tratando backend, frontend, integração
e toolkit como partes do mesmo sistema.

Ele não serve apenas para "deixar código menor". Ele existe para reduzir dívida estrutural cara, especialmente quando o
problema real está em uma destas fronteiras:

- backend com baixa coesão;
- contratos HTTP frouxos;
- frontend compensando deriva de payload;
- regra duplicada entre camadas;
- tratamento de erro espalhado;
- auditorias manuais que deveriam virar comando recorrente no `sgc.js`.

## Fontes de verdade

Antes de simplificar, confirme as restrições nestas fontes:

- `specs`
- `specs/design/acesso.md`
- `AGENTS.md`

No SGC, requisitos e regras de acesso têm precedência sobre preferência de refatoração.

## Objetivo

Reduzir complexidade acidental preservando:

- comportamento funcional;
- aderência aos requisitos em `specs`;
- contratos HTTP;
- contratos de integração entre backend e frontend;
- DTOs externos;
- lazy loading protegido por DTO no backend;
- regras de acesso e permissões;
- textos e fluxos relevantes de UI;
- estabilidade de testes;
- governança de qualidade já capturada no toolkit.

## Princípios

1. Simplificar a menor fronteira segura. Prefira começar por duplicação interna, helpers privados, comandos ou fluxos
   concentrados antes de mexer em contratos externos.

2. Tornar dependências explícitas. Uma regra, tela ou serviço não deve funcionar por contexto herdado ou efeito
   colateral implícito.

3. Reduzir superfícies. Se um service, facade, store ou composable expõe mais estado ou mais operações do que o uso real
   exige, estreite essa interface.

4. Eliminar efeitos colaterais escondidos. Prefira retorno explícito de dados, comandos claros e sincronização feita no
   ponto de uso.

5. Preservar fronteiras úteis. DTO, facade, service, composable ou store só devem sumir quando forem redundantes de
   verdade, não apenas porque parecem "finos".

6. Apagar código morto assim que ele ficar órfão. Depois de remover um acoplamento, procure métodos, campos, mocks,
   stubs e testes sem uso real.

7. Validar em passos pequenos. Faça uma mudança curta, valide, registre aprendizado, então siga.

8. Preferir simplificação local antes de abstração compartilhada. Se a lógica ainda pertence claramente a uma única tela
   ou fluxo, prefira helper local de view ou componente local de apresentação antes de inventar camada genérica.

9. Corrigir o ponto de verdade mais próximo da regra. Se a inconsistência nasce no backend, não estabilize isso com
   heurística no frontend. Se a regra é de borda, trate a borda. Se a dor é recorrente, transforme a auditoria em
   ferramenta.

10. Usar o toolkit como mecanismo de recorrência. Quando uma simplificação revela um problema estrutural repetível,
    avalie se ele deve virar comando no `sgc.js`, budget, auditoria ou snapshot.

11. Preferir integração de ferramenta OSS à reinvenção. Se o problema já é bem resolvido por ferramenta madura do
    ecossistema, o papel do SGC é orquestrar, calibrar e consolidar no toolkit, não reconstruir o motor do zero.

## Guardrails do SGC

### Backend

- Não remover DTOs mecanicamente.
- Não expor entidade JPA por conveniência.
- Não colapsar camadas só porque parecem verbosas.
- Não mover controller para acesso direto a repositório quando houver regra de negócio, segurança, transação ou montagem
  de resposta.
- Não simplificar permissão sem confronto explícito com `specs/design/acesso.md`.
- Não manter DTO público importando `model.*` quando o endpoint é parte da aplicação e não fixture técnica de E2E.
- Em `subprocesso`, simplifique antes duplicações de busca, validação e contexto; evite fusões amplas de serviço.
- Prefira helpers privados, `command`/DTO interno e centralização de leitura antes de criar abstrações novas.
- Se o frontend estiver reconstruindo regra de acesso, workflow ou disponibilidade por falta de sinal no DTO, prefira
  completar o contrato backend em vez de espalhar heurística no cliente.
- Se um service/controlador/facade virou hub com responsabilidades demais, prefira quebrar por caso de uso real, não por
  camada cosmética nem por contagem de linhas.
- Se a simplificação tocar controller ou DTO, confirme explicitamente se o retorno continua sendo contrato HTTP estável
  e separado do domínio.
- Se houver muitos parâmetros, use objeto de transporte, em linha com `AGENTS.md`.
- Se a simplificação alterar contrato interno real, atualize os testes.
- Se Gradle falhar só ao armazenar cache, repita sem cache antes de tratar como regressão de código.

### Frontend

- Não manter estado global só por conveniência.
- Não deixar composable escrever em store paralelo sem necessidade explícita.
- Não usar store singleton como ponte implícita entre uma action recém-chamada e a leitura imediata da mesma tela.
- Não tratar service, composable e store como camadas obrigatórias se uma delas só repassa chamadas sem agregar
  contrato.
- Se uma única view consome o estado, prefira sincronização local ou contexto explícito.
- Se a lógica continua sendo propriedade de uma única view, prefira extrair para `src/views/*.ts` ou componente local
  coeso em vez de criar composable global ou helper “utilitário” artificial.
- Não criar wrapper visual novo sem ganho claro de contrato, acessibilidade, responsividade ou padronização.
- Não colapsar visibilidade e habilitação da UI na mesma regra. Se a pessoa usuária pode realizar a ação, mas o
  workflow, a localização, a permissão contextual ou o carregamento impedem a execução agora, o controle deve continuar
  visível e ficar desabilitado.
- Se o backend não separar claramente "pode mostrar" de "pode executar", prefira endurecer o DTO/contrato na borda a
  recriar regra de permissão no frontend.
- Não manter tipos frouxos, `Partial<>`, `[key: string]: unknown`, `!`, `as` e defaults silenciosos em respostas
  centrais só para tolerar deriva de payload.
- Não reintroduzir `Perfil`, `isAdmin`, `isChefe`, `isGestor` ou equivalentes para decidir UI quando o backend já
  entrega permissões estruturadas.
- Não manter API pública de store, composable ou view apenas para sustentar testes antigos; ajuste ou apague os testes
  quando a superfície de produção encolher.
- Preserve textos, navegação e comportamento exigidos por `specs`.

### Integração Backend/Frontend

- Não aceitar "funciona porque o frontend corrige" como estado final.
- Não estabilizar drift de contrato com fallback silencioso quando o backend pode produzir dado correto.
- Não duplicar regra de workflow, elegibilidade, visibilidade ou permissão nas duas camadas sem motivo documentado.
- Se a mesma normalização aparece em service, store e view, isso é sinal de contrato frouxo ou borda mal definida.
- Se um contrato é crítico e recorrente, considere extração para OpenAPI, geração de tipos ou auditoria automatizada.

### Toolkit e Ferramentas

- Não criar script local ad hoc quando o problema merece comando nomeado, ajuda, saída JSON e reuso no `sgc.js`.
- Não esconder heurística importante em prompt ou memória quando ela pode virar auditoria reproduzível.
- Não reconstruir do zero o que ferramentas como `ArchUnit`, `Semgrep`, `openapi-diff`, `openapi-typescript`,
  `Schemathesis`, `jQAssistant` ou `OpenRewrite` já resolvem bem.
- O toolkit deve orquestrar, consolidar e contextualizar a análise para o SGC.

## Heurísticas Full-Stack

### Backend: bons alvos

- métodos de service que repetem a mesma busca, validação ou montagem de contexto;
- facades ou services pass-through sem regra real;
- controllers grandes misturando consulta, workflow, manutenção, contexto e administração;
- branches longos variando só por enum, flag ou tipo de fluxo;
- validações de negócio duplicadas em mais de um ponto do workflow;
- consultas iguais espalhadas em services diferentes;
- testes que ainda mockam collaborators antigos após uma consolidação;
- DTOs públicos que ainda importam tipos `model.*`;
- montagem de resposta HTTP misturada à coleta de domínio e à decisão de permissão.

### Backend: alvos ruins

- remoção mecânica de DTO;
- controller falando direto com repo quando o fluxo tem regra, permissão ou transação;
- fusão ampla de services de workflow só para "diminuir número de classes";
- troca simultânea de contrato HTTP, regra de negócio e arquitetura sem isolamento;
- retorno de entidade para evitar mapper.

### Frontend: bons alvos

- views com repetição de `loading`, erro, modal, redirect ou sincronização de dados;
- composables que atualizam outros stores por dentro;
- actions que já carregam um recurso, mas obrigam a view a reler um singleton para usar o resultado;
- stores finas com um único consumidor real;
- stores cuja superfície pública cresceu só para apoiar testes ou chamadas redundantes;
- estado exposto mas sem uso em produção;
- services ou wrappers que só repassam chamada sem agregar contrato;
- DTOs/permissões que já distinguem capacidade e habilitação, mas cujo frontend ainda trata isso como uma única flag;
- contratos de resposta com opcionais demais para dados que deveriam ser obrigatórios;
- mapeadores que preenchem ausência de backend com enum default, string vazia ou `0`;
- views grandes que ainda misturam orquestração de fluxo e seções visuais que podem virar componente local de
  apresentação;
- arquivos centrais cujo código pode ser fatiado por responsabilidade real sem alterar o contrato externo;
- testes que continuam presos ao fluxo implícito anterior.

### Frontend: alvos ruins

- store global criado só para evitar passar dado explicitamente;
- composable que acumula estado de múltiplas telas sem necessidade real;
- remoção de componente visual compartilhado que ainda protege contrato de UI;
- refatoração “estética” que espalha regra de negócio pela view;
- simplificação que faz ação desabilitada sumir quando o contrato correto de UX pede ação visível e indisponível;
- compensar DTO fraco com heurística local de perfil, situação ou localização no frontend em vez de corrigir a borda;
- extração para helper/composable compartilhado quando o código continua com um único consumidor claro;
- mover blocos grandes para outro arquivo sem reduzir superfície, acoplamento ou responsabilidade real;
- simplificação que muda textos, navegação ou permissões fora do que `specs` permite.

### Integração: bons alvos

- backend entregando dado incompleto que o frontend recompõe de forma defensiva;
- tipos frontend mais permissivos que o contrato backend real;
- divergência entre nomes de campos, nulabilidade e obrigatoriedade;
- duplicação de decisão entre permissão backend e habilitação frontend;
- contratos de contexto grandes demais, porém ainda mal definidos.

### Integração: alvos ruins

- aceitar fallback silencioso só porque evita quebrar teste antigo;
- gerar tipagem manual paralela se a fonte de verdade do contrato já pode ser publicada;
- tratar drift estrutural com comentário, convenção verbal ou prompt em vez de ajuste de contrato ou auditoria.

### Toolkit: bons alvos

- auditoria manual repetida em várias rodadas;
- detecção de smell estrutural com critério objetivo;
- comparação backend/frontend que pode emitir Markdown e JSON;
- validação diff-aware que pode virar gate ou snapshot;
- integração leve de ferramenta OSS com boa relação sinal/ruído.

### Toolkit: alvos ruins

- script opaco sem saída estruturada;
- score mágico sem hotspot legível;
- ferramenta nova sem encaixe claro em comando, relatório ou decisão de equipe;
- duplicar analisador de terceiros só para manter tudo "caseiro".

## Fluxo recomendado

1. Mapear o acoplamento real.

- Quem lê o estado ou a regra.
- Quem escreve ou decide.
- Se a dependência é explícita, herdada ou duplicada.
- Se o problema está na lógica local, na borda HTTP, na integração ou no toolkit.

1. Classificar o alvo.

- duplicação interna;
- superfície larga demais;
- estado global desnecessário;
- efeito colateral escondido;
- wrapper fino;
- contrato frouxo na borda;
- regra de permissão reconstruída no cliente;
- código morto;
- baixa coesão de backend;
- deriva de integração;
- heurística que deveria virar auditoria.

1. Escolher o menor corte seguro.

- Uma fronteira por vez.
- Não misturar simplificação estrutural com mudança de regra.
- Se a simplificação tocar visibilidade de ação, confirme explicitamente se o comportamento correto é ocultar ou
  desabilitar.
- Se a simplificação tocar contrato HTTP, confirme explicitamente quem é a fonte de verdade e quais consumidores reais
  dependem dele.
- Só considere um arquivo realmente simplificado se a superfície pública, o acoplamento ou a responsabilidade dele
  tiverem diminuído de forma verificável.

1. Tornar o fluxo explícito.

- Se dois métodos diferem só por contexto, extraia o contexto.
- Se várias validações repetem a mesma coleta, centralize a coleta.
- Se uma função altera outro store por dentro, prefira fazê-la retornar o dado.
- Se uma tela depende de contexto herdado, prefira buscar uma referência explícita.
- Se uma action busca um detalhe e a tela precisa dele imediatamente, prefira retornar esse detalhe em vez de depender
  de leitura posterior do store global.
- Se uma recarga automática não tem consumidor real além do recurso local afetado, remova o efeito colateral e
  recarregue só o que a tela usa.
- Se o frontend precisa decidir entre mostrar e habilitar, prefira um contrato explícito com flags separadas em vez de
  inferência implícita no componente.
- Se a borda do service já consegue normalizar DTO, status, permissões ou datas de forma estável, concentre isso ali e
  pare de repetir defaults e defensividade na store e na view.
- Se a mesma checagem estrutural já apareceu mais de uma vez na sessão, avalie transformá-la em auditoria do toolkit.
- Se houver ferramenta OSS madura para o problema, prefira integrar a ferramenta e adicionar adaptação local ao `sgc.js`
  em vez de ampliar heurística caseira sem necessidade.

1. Validar logo após cada bloco.

### Comandos úteis do SGC

Backend:

```bash
./gradlew :backend:compileTestJava
./gradlew :backend:test --tests "sgc.algum.pacote.AlgumTeste"
./gradlew :backend:compileTestJava
node toolkit/sgc.js backend contratos auditar
```

Frontend:

```bash
npx vitest run <arquivos> --reporter=dot --no-color
npm run typecheck
npm run lint
node toolkit/sgc.js frontend arquitetura auditar
node toolkit/sgc.js frontend cruft auditar
node toolkit/sgc.js codigo smells auditar
```

1. Registrar aprendizado.

- Atualize `plano-simplificacao.md` quando a rodada gerar critério novo ou novo mapa de risco.
- Atualize `plano-qualidade.md` quando a rodada alterar direção estrutural, critério de integração ou evolução do
  toolkit.
- Se a rodada revelou problema repetível, proponha comando, budget, waiver ou snapshot correspondente.

## Perguntas de decisão

- Esta classe ou camada agrega contrato real ou só repassa chamadas?
- Esta duplicação está no backend, no frontend ou atravessa a fronteira entre os dois?
- O problema real está na implementação, no contrato, ou na ausência de auditoria reproduzível?
- Esta dependência precisa mesmo ser global?
- Esta regra pode ser centralizada sem mudar o contrato externo?
- O melhor corte aqui é um helper local de view, um componente local de apresentação, ou um contrato compartilhado mais
  forte na borda?
- Este fallback existe por requisito ou só por tolerância a drift?
- Estou mantendo alguma API pública apenas porque os testes antigos se acostumaram com ela?
- Estou simplificando uma regra de UX real ou apenas apagando um estado desabilitado que parecia redundante?
- A distinção entre "ação inexistente para este perfil" e "ação indisponível neste contexto" continua preservada?
- A leitura do fluxo ficou mais curta?
- Há algum collaborator implícito que pode virar dado explícito?
- Existe código morto liberado por esta simplificação?
- Esta auditoria deveria virar comando do `sgc.js`?
- Existe ferramenta OSS melhor do que heurística local para este problema?
- A mudança continua alinhada com `specs` e `specs/design/acesso.md`?

## Saída esperada

Ao usar este skill, entregue:

- simplificação concreta no código;
- validação focada;
- aprendizado curto da rodada;
- próximo alvo natural;
- quando fizer sentido, proposta concreta de evolução do toolkit ou da auditoria associada.
