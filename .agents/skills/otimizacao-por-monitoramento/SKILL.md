---
name: otimizacao-por-monitoramento
description: Use quando o objetivo for identificar e reduzir gargalos reais no SGC a partir de um cenário monitorado. Indicado para fluxos E2E com `SGC_MONITORAMENTO=sim`, análise por arquivo único de log, comparação antes/depois e otimizações incrementais em backend Java/Spring e frontend Vue/TypeScript.
---

# Otimização por Monitoramento no SGC

Use este skill quando a pergunta real for "onde o fluxo está gastando tempo ou round-trips?" e a resposta precise vir de evidência de execução, não de inspeção estática isolada.

## Fontes de verdade

Antes de otimizar, confirme as restrições nestas fontes:

- `etc/reqs`
- `etc/docs/regras-acesso.md`
- `AGENTS.md`
- o cenário E2E relacionado

No SGC, requisito e regra de acesso têm precedência sobre qualquer corte de performance.

## Quando usar

Use este skill quando houver:

- percepção de lentidão em um caso de uso;
- suspeita de chamadas repetidas;
- suspeita de sobreposição entre telas, contextos ou endpoints;
- dúvida se o gargalo está no backend, no frontend ou na navegação;
- necessidade de provar ganho com antes/depois.

Não use este skill quando o problema já for evidentemente local e determinístico, como um algoritmo interno isolado sem dependência de fluxo real.

## Objetivo

Encontrar o menor corte seguro que reduz:

- latência no caminho crítico;
- round-trips redundantes;
- recargas completas desnecessárias;
- efeitos colaterais síncronos fora do que o fluxo precisa;
- consultas de resolução repetidas quando a referência já existe.

Preserve:

- comportamento funcional;
- contratos exigidos por `etc/reqs`;
- regras de acesso;
- textos e navegação relevantes;
- estabilidade de testes.

## Método padrão

### 1. Medir o cenário real

Rode o cenário com monitoramento ligado e saída redirecionada para um único arquivo:

```bash
SGC_MONITORAMENTO=sim SGC_MONITORAMENTO_TEMPO_MINIMO_HTTP_MS=0 SGC_MONITORAMENTO_TEMPO_MINIMO_JAVA_MS=0 npx playwright test e2e/arquivo.spec.ts --reporter=dot > /tmp/arquivo-monitorado.log 2>&1
```

> [!IMPORTANT]
> **Use o nível INFO**: Por padrão, o monitoramento pode estar em `WARN`. Para ver todas as chamadas HTTP e Java, garanta que o nível está em `info` (padrão se omitido).
> **Zere os thresholds**: O threshold padrão de 100ms/500ms oculta chamadas rápidas que, se repetidas, causam impacto. Use `0` para auditoria completa.

Se o caso de uso depender de preparação serial, rode o arquivo inteiro. Se houver um cenário isolável sem perder fidelidade, rode o recorte mínimo suficiente.

### 1.1 Ativar telemetria do frontend quando o alvo inclui navegação/round-trip do navegador

No lifecycle E2E atual do SGC, o frontend pode subir com `VITE_MONITORAMENTO_MODO='off'`.
Se a hipótese envolver custo percebido no frontend, sequência de chamadas, navegação SPA ou round-trips redundantes, **não aceite isso como limitação do cenário**.

Antes de tirar conclusões:

1. alinhe o ambiente para subir o frontend monitorado;
2. garanta que o artefato único realmente capture também a telemetria HTTP do navegador;
3. só então compare antes/depois.

Em outras palavras: se a skill for usada para performance de frontend, o agente deve **ativar o monitoramento do Vite/navegador primeiro** em vez de analisar apenas o lado backend e chamar isso de suficiente.

### 2. Analisar um artefato só

Use apenas o arquivo redirecionado do E2E como fonte de análise.

Não misture:

- saída solta do terminal;
- `server.log`;
- memória do que "pareceu lento".

Filtre com `rg`.

Exemplos:

```bash
rg "^http " /tmp/arquivo-monitorado.log
rg "^java " /tmp/arquivo-monitorado.log
rg "^(http|java) " /tmp/arquivo-monitorado.log
rg "subprocessos/buscar|contexto-edicao|contexto-cadastro-atividades|mapa-visualizacao" /tmp/arquivo-monitorado.log
rg "processos/401|subprocessos/401" /tmp/arquivo-monitorado.log
```

Para contar repetição:

```bash
rg "subprocessos/buscar\\?codProcesso=401" /tmp/arquivo-monitorado.log | wc -l
```

### 3. Classificar o tipo de desperdício

Classifique cada achado em uma destas categorias:

- hotspot síncrono: request realmente lenta no caminho crítico;
- lookup redundante: resolve código ou contexto que já poderia estar explícito;
- contexto fragmentado: sequência de endpoints pequenos para montar uma tela;
- refresh pós-mutação: recarga completa após operação que já devolve estado suficiente;
- efeito colateral síncrono: alerta, e-mail, template ou hierarquia dentro do request crítico;
- ruído de teste: timeout ou espera de URL incompatível com o contrato atual.

### 4. Escolher o menor corte seguro

Prefira, nesta ordem:

1. remover lookup redundante;
2. reaproveitar contexto já carregado;
3. substituir refresh completo por atualização incremental;
4. tirar efeito colateral caro do request síncrono;
5. só então atacar micro-otimização interna.

### 5. Validar com o mesmo cenário

Depois de cada rodada:

- rode o mesmo cenário;
- compare contagem de endpoints repetidos;
- compare a request lenta principal;
- confirme que o comportamento do caso de uso continua válido.

## Heurísticas SGC

### Bons alvos no frontend

- navegação que conhece `codSubprocesso`, mas ainda chama `subprocessos/buscar`;
- views que montam tela com múltiplos contextos sobrepostos;
- cards ou tabelas que descartam referência já disponível;
- reload completo depois de importação, inclusão, aceite ou homologação;
- helpers E2E que falham por esperar URL antiga após melhoria do contrato.

### Bons alvos no backend

- transição de workflow com notificação síncrona;
- preparação pesada de alerta/e-mail/template no request do usuário;
- montagem duplicada de contexto para a mesma tela;
- busca repetida da mesma entidade em sequência curta.

### Sinais de que não é o foco certo

- CRUD principal já está rápido e o custo está na navegação;
- request lenta única é aceitável e rara, mas a tela faz várias chamadas pequenas repetidas;
- o problema percebido vem do teste, não do fluxo real.

## Padrões que já se mostraram úteis

### Toasts e Persistência em Navegação

Se a ação do usuário dispara um redirecionamento (ex: `router.push('/painel')`), a mensagem de sucesso disparada na View original será destruída com ela.
No SGC, use o `ToastStore` para mensagens que devem sobreviver à navegação:

- ✅ USE: `toastStore.setPending(mensagem)` antes do redirecionamento.
- ✅ VERIFIQUE: A View de destino (ex: `PainelView`) deve consumir isso no `onMounted/onActivated` via `exibirToastPendente()`.

### Compatibilidade E2E (BOrchestrator)

O sistema de testes Playwright do SGC utiliza seletores semânticos para toasts.
O componente `BOrchestrator` no `App.vue` **DEVE** ter a classe `.orchestrator-container`. Se for removida, o helper `verificarToast` falhará com "element not found".

### Otimização de Payload (Bloat e Redundância)

Identifique JSONs que carregam o "passado" desnecessariamente:

- **Movimentações**: Em contextos de edição/mapeamento, limite o histórico para as 10-15 mais recentes. O histórico completo deve ser sob demanda.
- **Atividades e Conhecimentos**: Evite enviar a árvore completa de conhecimentos em listagens onde apenas o ID/Descrição da atividade é necessário (ex: seleção de atividades para competência).
- **Duplicação**: Se o `mapa` já traz atividades associadas, o campo `atividadesDisponiveis` não deve repetir os dados completos dessas mesmas atividades.

### Tornar referência explícita

Se a navegação já conhece o subprocesso, carregue essa referência no destino.

Prefira:

- `codSubprocesso` explícito no destino;
- leitura direta do contexto por código;
- links e cards preservando a referência.

Evite:

- resolver de novo por `processo + unidade` quando isso já foi descoberto antes.

### Atualizar localmente após mutação

Se a operação já pode devolver estado suficiente, use esse retorno para atualizar a tela.

Evite:

- `POST` seguido de `GET` completo só para reconstruir a mesma tela.

### Tirar peso do request crítico

Se o usuário espera a conclusão da transição, remova do caminho síncrono o que não precisa estar lá.

Exemplos típicos:

- envio de notificação;
- preparação de templates;
- resolução de destinatários;
- criação de efeitos colaterais que podem rodar após commit.

## Guardrails

- Não invente gargalo; prove pelo log.
- Não troque contrato externo só para reduzir uma chamada pequena sem necessidade real.
- Não mantenha camada de compatibilidade se a decisão da rodada for simplificar o contrato.
- Não use duas fontes de log para defender a mesma conclusão.
- Não confunda melhoria de teste com melhoria de produto.
- Não remova verificação de permissão em nome de performance.

## Perguntas de decisão

- Este request é realmente lento ou só aparece muito?
- Essa chamada existe porque o dado não existe, ou porque a navegação o perdeu?
- O fluxo precisa desse `GET` completo ou ele é só recomposição preguiçosa?
- O backend está fazendo trabalho essencial ao usuário ou efeito colateral incidental?
- A melhor próxima mudança corta o maior desperdício com o menor risco?

## Saída esperada

Ao usar este skill, entregue:

- cenário monitorado executado com saída em arquivo único;
- achados principais com evidência objetiva do log;
- otimização concreta no código;
- validação antes/depois;
- próximo alvo natural, se ainda houver desperdício remanescente.
