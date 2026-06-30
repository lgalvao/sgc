---
name: limpeza-estrutural-frontend
description: Use quando o objetivo for reduzir cruft e complexidade acidental no frontend do SGC com cortes pequenos, budgets/waivers ratchetáveis, validação focada e remoção imediata de compatibilidades e sobras obsoletas.
---

# Limpeza Estrutural do Frontend no SGC

Use este skill quando a pergunta real for "qual é o menor corte seguro para deixar o frontend mais coeso, uniforme e
simples sem quebrar contrato?".

Ele é focado em rodadas incrementais de limpeza estrutural no frontend Vue/TypeScript do SGC, guiadas por evidência do
gate de cruft e por hotspots reais do código.

## Fontes de verdade

Antes de simplificar, confirme as restrições nestas fontes:

- `AGENTS.md`
- `specs/design/acesso.md`
- `toolkit/qualidade/frontend-cruft/latest`
- `toolkit/qualidade/frontend-arquitetura/latest`
- `toolkit/qualidade/frontend-arquitetura/acoes-backend-waivers.json`
- testes e2e/unitários relacionados ao fluxo tocado

No SGC, requisitos, contratos de UI e regras de acesso têm precedência sobre qualquer redução de cruft.

## Quando usar

Use este skill quando houver:

- views grandes demais misturando apresentação, ações, modais e navegação;
- services/stores/composables com superfície larga ou pass-through demais;
- arquivos no waiver de cruft que já parecem prontos para um corte menor;
- defensividade excessiva, compatibilidades artificiais ou código morto após refatorações;
- necessidade de ratchetar o baseline removendo waivers obsoletos.

Não use este skill quando:

- o problema principal for performance medida em fluxo real; nesse caso prefira `otimizacao-por-monitoramento`;
- a tarefa exigir simplificação full-stack de regra de negócio atravessando backend e frontend; nesse caso prefira
  `simplificacao-codigo`.

## Objetivo

Reduzir complexidade acidental no frontend preservando:

- comportamento funcional;
- contratos HTTP e de navegação;
- textos e fluxos relevantes de UI;
- `data-testid` semânticos já consumidos por testes;
- estabilidade de typecheck, lint, testes unitários e e2e relevantes.

## Princípios

1. Escolher a menor fronteira segura. Comece por blocos visuais ou fluxos locais coesos: header, ações, filtros, modais,
   seções de tela, helpers privados.

2. Não deslocar o problema de camada. Se a lógica é local de uma view, prefira componente da própria feature ou helper
   local da view antes de criar um novo composable/store que só muda a violação de lugar.

3. Preservar contratos úteis. Não renomeie `data-testid`, props públicas, barrels ou rotas sem evidência de que o
   contrato antigo realmente ficou sem uso.

4. Remover compatibilidade artificial. Se a decisão da rodada for simplificar, não mantenha alias, nomes paralelos,
   branches legados ou defaults silenciosos só para acomodar código velho.

5. Apagar sobra imediatamente. Remova waiver obsoleto, teste sem sentido, helper órfão e código morto na mesma rodada em
   que eles deixarem de ser necessários.

6. Validar em passos pequenos. Faça um corte curto, rode validação focada, estabilize, só então passe ao próximo
   hotspot.

## Loop padrão

### 1. Ler o baseline real

Comece por:

```bash
node toolkit/sgc.js frontend cruft validar
node toolkit/sgc.js frontend arquitetura validar
```

Use o resultado para descobrir:

- qual arquivo está acima do target;
- em qual camada está o excesso;
- se há waiver obsoleto que já pode cair;
- se a mudança proposta pode criar uma violação nova em outra camada.
- se o frontend está calculando habilitação/exibição de ação que deve vir do backend.

Se tocar em `data-testid`, rode também:

```bash
node toolkit/sgc.js frontend test-ids listar-duplicados
```

### 2. Classificar o hotspot

Classifique o alvo em uma destas categorias:

- view monolítica;
- componente com responsabilidades misturadas;
- service/store/composable com superfície larga;
- barrel ou arquivo agregador pesado;
- código morto ou compatibilidade residual;
- teste acumulado de transição histórica.

### 3. Escolher o corte

Prefira, nesta ordem:

1. extrair bloco visual coeso;
2. extrair helper local de fluxo;
3. fatiar tipos/barrels por responsabilidade;
4. remover fallback defensivo ou compatibilidade artificial;
5. consolidar testes duplicados ou órfãos.

Só crie novo composable quando ele agregar contrato real e reutilização clara.

### 4. Preservar contratos sem espalhar exceções

Se um `data-testid` ou nome público já é contrato de e2e, restaure ou preserve esse contrato.

Se o mesmo nome colidir com outro contexto, prefira:

- manter o nome histórico no ponto público;
- renomear o elemento interno/local;
- evitar allowlists novas no gate quando a colisão puder ser resolvida no código.

### 5. Ratchetar o baseline

Se o arquivo voltou ao target, remova o waiver na mesma rodada.

Não deixe:

- waiver obsoleto;
- budget relaxado sem necessidade;
- arquivo novo acima do target sem justificativa real.

## Heurísticas que já se mostraram úteis

### Bons alvos

- views com `PageHeader` cheio de ações e branching;
- wrappers que acumulam vários modais distintos;
- arquivos de tipos agregando responsabilidades demais;
- telas de relatório misturando filtro, navegação e resultado;
- stores que servem só como ponte implícita para leitura imediata da própria tela;
- testes `Coverage`/`Uncovered` que ainda refletem transições antigas.

### Maus alvos

- abstração genérica criada só para "organizar";
- novo composable para lógica usada por uma única view sem contrato claro;
- renomeação estética de `data-testid` sem varrer impactos em e2e;
- fallback silencioso que encobre contrato frouxo;
- compatibilidade temporária que fica permanente.

## Guardrails específicos do SGC

- Mantenha tudo em português brasileiro.
- Use `codigo`, não `id`, em novos contratos internos.
- Evite overengineering: menos camadas, mas só quando a leitura realmente melhora.
- Não tente "recuperar" erro irrecuperável de backend no frontend; preserve o padrão fail-fast.
- Não duplique regra de acesso no frontend se o backend já decide habilitação.
- Não introduza cálculos locais de `pode*`, `habilitar*`, `mostrar*` ou `exibir*` para botões, cards ou ações. Use
  flags/action models vindos do backend; o gate `frontend arquitetura validar` bloqueia regressões novas.
- Não mantenha código marcado como legado/depreciado se a dependência interna sumiu.

## Validação mínima por rodada

Depois de cada corte, rode o menor conjunto suficiente entre:

```bash
npx vitest run <arquivos>
npm run typecheck
npm run lint
node toolkit/sgc.js frontend cruft validar
node toolkit/sgc.js frontend arquitetura validar
node toolkit/sgc.js frontend test-ids listar-duplicados
npx playwright test <arquivo-e2e>
```

Regras práticas:

- sempre rode `frontend cruft validar`;
- rode `frontend arquitetura validar` se tocar em ações, botões, permissões ou fluxos de navegação;
- rode `test-ids-duplicados` se tocar em seletores;
- rode Playwright focado se tocar em contrato de navegação ou tela coberta por e2e.

## Perguntas de decisão

- Este corte reduz leitura real ou só redistribui linhas?
- A lógica é realmente compartilhada ou é só local da view?
- O contrato público mudou sem necessidade?
- O novo arquivo caiu numa camada mais crítica do gate?
- Há waiver que já pode ser removido agora?
- Existe sobra liberada pela mudança que deve ser apagada já?

## Saída esperada

Ao usar este skill, entregue:

- simplificação concreta no código;
- validação focada da rodada;
- baseline ratchetado quando aplicável;
- aprendizado curto do corte;
- próximo hotspot natural do frontend.
