---
name: simplificacao-codigo
description: Use quando o objetivo for simplificar código no SGC de forma full-stack, sem quebrar contratos, requisitos ou regras de acesso. Indicado para consolidar duplicações internas, reduzir acoplamento acidental, estreitar superfícies de services/composables/stores, eliminar efeitos colaterais escondidos, remover código morto e explicitar dependências em backend Java/Spring e frontend Vue/TypeScript.
---

# Simplificação Full-Stack no SGC

Use este skill para rodadas de simplificação incremental em código já existente, tratando backend e frontend como partes do mesmo fluxo de negócio.

## Fontes de verdade

Antes de simplificar, confirme as restrições nestas fontes:

- `etc/reqs`
- `etc/docs/regras-acesso.md`
- `AGENTS.md`
- `plano-simplificacao.md`, se existir

No SGC, requisitos e regras de acesso têm precedência sobre preferência de refatoração.

## Objetivo

Reduzir complexidade acidental preservando:

- comportamento funcional;
- aderência aos requisitos em `etc/reqs`;
- contratos HTTP;
- DTOs externos;
- lazy loading protegido por DTO no backend;
- regras de acesso e permissões;
- textos e fluxos relevantes de UI;
- estabilidade de testes.

## Princípios

1. Simplificar a menor fronteira segura.
Prefira começar por duplicação interna, helpers privados, comandos ou fluxos concentrados antes de mexer em contratos externos.

2. Tornar dependências explícitas.
Uma regra, tela ou serviço não deve funcionar por contexto herdado ou efeito colateral implícito.

3. Reduzir superfícies.
Se um service, facade, store ou composable expõe mais estado ou mais operações do que o uso real exige, estreite essa interface.

4. Eliminar efeitos colaterais escondidos.
Prefira retorno explícito de dados, comandos claros e sincronização feita no ponto de uso.

5. Preservar fronteiras úteis.
DTO, facade, service, composable ou store só devem sumir quando forem redundantes de verdade, não apenas porque parecem "finos".

6. Apagar código morto assim que ele ficar órfão.
Depois de remover um acoplamento, procure métodos, campos, mocks, stubs e testes sem uso real.

7. Validar em passos pequenos.
Faça uma mudança curta, valide, registre aprendizado, então siga.

## Guardrails do SGC

### Backend

- Não remover DTOs mecanicamente.
- Não expor entidade JPA por conveniência.
- Não colapsar camadas só porque parecem verbosas.
- Não mover controller para acesso direto a repositório quando houver regra de negócio, segurança, transação ou montagem de resposta.
- Não simplificar permissão sem confronto explícito com `etc/docs/regras-acesso.md`.
- Em `subprocesso`, simplifique antes duplicações de busca, validação e contexto; evite fusões amplas de serviço.
- Prefira helpers privados, `command`/DTO interno e centralização de leitura antes de criar abstrações novas.
- Se houver muitos parâmetros, use objeto de transporte, em linha com `AGENTS.md`.
- Se a simplificação alterar contrato interno real, atualize os testes.
- Se Gradle falhar só ao armazenar cache, repita sem cache antes de tratar como regressão de código.

### Frontend

- Não manter estado global só por conveniência.
- Não deixar composable escrever em store paralelo sem necessidade explícita.
- Não usar store singleton como ponte implícita entre uma action recém-chamada e a leitura imediata da mesma tela.
- Não tratar service, composable e store como camadas obrigatórias se uma delas só repassa chamadas sem agregar contrato.
- Se uma única view consome o estado, prefira sincronização local ou contexto explícito.
- Não criar wrapper visual novo sem ganho claro de contrato, acessibilidade, responsividade ou padronização.
- Preserve textos, navegação e comportamento exigidos por `etc/reqs`.

## Heurísticas Full-Stack

### Backend: bons alvos

- métodos de service que repetem a mesma busca, validação ou montagem de contexto;
- facades ou services pass-through sem regra real;
- branches longos variando só por enum, flag ou tipo de fluxo;
- validações de negócio duplicadas em mais de um ponto do workflow;
- consultas iguais espalhadas em services diferentes;
- testes que ainda mockam collaborators antigos após uma consolidação.

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
- estado exposto mas sem uso em produção;
- services ou wrappers que só repassam chamada sem agregar contrato;
- testes que continuam presos ao fluxo implícito anterior.

### Frontend: alvos ruins

- store global criado só para evitar passar dado explicitamente;
- composable que acumula estado de múltiplas telas sem necessidade real;
- remoção de componente visual compartilhado que ainda protege contrato de UI;
- refatoração “estética” que espalha regra de negócio pela view;
- simplificação que muda textos, navegação ou permissões fora do que `etc/reqs` permite.

## Fluxo recomendado

1. Mapear o acoplamento real.
- Quem lê o estado ou a regra.
- Quem escreve ou decide.
- Se a dependência é explícita, herdada ou duplicada.

2. Classificar o alvo.
- duplicação interna;
- superfície larga demais;
- estado global desnecessário;
- efeito colateral escondido;
- wrapper fino;
- código morto.

3. Escolher o menor corte seguro.
- Uma fronteira por vez.
- Não misturar simplificação estrutural com mudança de regra.

4. Tornar o fluxo explícito.
- Se dois métodos diferem só por contexto, extraia o contexto.
- Se várias validações repetem a mesma coleta, centralize a coleta.
- Se uma função altera outro store por dentro, prefira fazê-la retornar o dado.
- Se uma tela depende de contexto herdado, prefira buscar uma referência explícita.
- Se uma action busca um detalhe e a tela precisa dele imediatamente, prefira retornar esse detalhe em vez de depender de leitura posterior do store global.
- Se uma recarga automática não tem consumidor real além do recurso local afetado, remova o efeito colateral e recarregue só o que a tela usa.

5. Validar logo após cada bloco.

### Comandos úteis do SGC

Backend:

```bash
./gradlew :backend:compileTestJava
./gradlew :backend:test --tests "sgc.algum.pacote.AlgumTeste"
./gradlew --no-configuration-cache :backend:compileTestJava
```

Frontend:

```bash
npx vitest run <arquivos> --reporter=dot --no-color
npm run typecheck
npm run lint
```

6. Registrar aprendizado.
- Atualize `plano-simplificacao.md` quando a rodada gerar critério novo ou novo mapa de risco.

## Perguntas de decisão

- Esta classe ou camada agrega contrato real ou só repassa chamadas?
- Esta duplicação está no backend, no frontend ou atravessa a fronteira entre os dois?
- Esta dependência precisa mesmo ser global?
- Esta regra pode ser centralizada sem mudar o contrato externo?
- A leitura do fluxo ficou mais curta?
- Há algum collaborator implícito que pode virar dado explícito?
- Existe código morto liberado por esta simplificação?
- A mudança continua alinhada com `etc/reqs` e `etc/docs/regras-acesso.md`?

## Saída esperada

Ao usar este skill, entregue:

- simplificação concreta no código;
- validação focada;
- aprendizado curto da rodada;
- próximo alvo natural.
