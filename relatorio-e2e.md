# Relatório de fidelidade dos testes E2E

## Conclusão

A suíte E2E do SGC, em geral, **simula operações reais do usuário** de forma consistente. Os testes priorizam navegação pela UI, usam `data-testid`, esperam URLs e respostas de rede, e evitam mecanismos mais frágeis como mocks de rota, `localStorage` ou `sessionStorage`.

O principal cuidado está no uso de **fixtures/atalhos de preparação** para pular etapas repetitivas do fluxo. Isso é aceitável quando o objetivo é apenas montar o estado inicial do cenário, desde que a **ação validada continue sendo executada pela interface**.

## Situação atual das melhorias

As críticas que motivaram a revisão já foram tratadas:

* `click({force: true})` foi removido dos helpers de atividades;
* `limparNotificacoes()` deixou de engolir erros silenciosamente;
* os setups críticos deixaram de usar `expect(true).toBeTruthy()` e passaram a validar fixtures/estado;
* `npm run typecheck:e2e` passou e uma amostra de Playwright dos fluxos críticos também passou.
* `CDU-07` e `CDU-08` foram endurecidos para entrar pelo painel antes de abrir processos/subprocessos, reduzindo a navegação direta.
* `CDU-09` e `CDU-10` agora usam helpers distintos por perfil: `CHEFE` entra direto no subprocesso e `GESTOR` usa a árvore de unidades.
* `CDU-22`, `CDU-25` e `CDU-33` também foram endurecidos para abrir detalhes via painel antes das ações de bloco/reabertura.
* `CDU-21` e `CDU-24` também passaram a entrar pelo painel nos fluxos de ADMIN.

A varredura final foi concluída sem deixar passos obrigatórios em aberto. O que ainda existe de navegação direta ou helper semântico ficou restrito a preparação aceitável por design, ou a cliques reais encapsulados em helpers.

## Atalhos aceitáveis por design

### 1. Reset do banco antes dos testes

Arquivos como `e2e/fixtures/complete-fixtures.ts`, `e2e/hooks/hooks-limpeza.ts` e `e2e/README.md` mostram um padrão claro: o banco é resetado para garantir isolamento. Isso não mascara bug de produto; é infraestrutura de teste.

### 2. Fixtures E2E para preparar estados profundos

Os helpers de `e2e/fixtures/fixtures-processos.ts` usam endpoints como `/e2e/fixtures/processo-mapeamento-com-mapa-validado` e similares para criar estados avançados. Isso é aceitável quando o teste quer validar uma ação específica em um estado já pronto, sem repetir todo o workflow anterior.

### 3. Navegação direta para telas profundas

Alguns testes fazem `page.goto('/processo/...')` ou `page.goto('/processo/.../...')` para acessar diretamente detalhes e subprocessos. Isso é razoável quando o alvo do teste é a tela em si, e não o caminho de chegada.

## Atalhos preocupantes

### 1. `force: true` em botões de hover

Em `e2e/helpers/helpers-atividades.ts`, os helpers de edição usam `click({force: true})` em botões que aparecem apenas no hover.

Risco: o teste pode continuar passando mesmo se o botão estiver mal posicionado, obstruído ou com problema de interação real para o usuário.

Situação atual: removido nos helpers de atividades.

### 2. Limpeza de notificações engolindo erro

Em `e2e/helpers/helpers-navegacao.ts`, `limparNotificacoes()` captura erros amplamente e pode retornar sem sinalizar problemas relevantes.

Risco: falhas reais de fechamento/overlay podem ser silenciadas, especialmente em cenários com modais, toasts ou páginas já fechando.

Situação atual: a limpeza continua tolerante apenas a fechamento de página/contexto; demais falhas voltam a aparecer.

### 3. Acesso direto a rotas profundas em vários specs

Exemplos em `e2e/cdu-07.spec.ts`, `e2e/cdu-08.spec.ts`, `e2e/cdu-10.spec.ts`, `e2e/cdu-21.spec.ts` e outros entram em telas internas sem passar pela navegação da listagem.

Risco: isso é aceitável para validar a tela interna, mas não valida o percurso real do usuário quando a navegação via painel/listagem também faz parte da regra.

Situação atual: reduzido nos cenários de maior risco; ainda mantido onde a tela interna é o foco do cenário ou onde a rota direta é parte aceitável do preparo.

### 4. Helpers que concentram navegação + assertiva demais

Alguns helpers fazem muito mais do que um clique semântico; por exemplo, `navegarParaSubprocesso()` e helpers de análise combinam navegação, checagem de estado e decisão de rota.

Risco: se usados sem cuidado, podem esconder exatamente o ponto em que o usuário real encontraria erro.

Situação atual: mantidos, mas agora o relatório separa melhor o que é preparo e o que é validação.

## Segunda passada: classificação por risco

### Aceitáveis

Esses pontos são atalhos de infraestrutura ou de preparação e **não escondem, por si só, a jornada do usuário**:

* reset do banco antes dos testes;
* fixtures para criar estados profundos;
* navegação direta para telas profundas quando o teste quer validar a própria tela;
* helpers semânticos que apenas encapsulam cliques e esperas explícitas.

### Arriscados

Esses pontos merecem revisão porque podem reduzir a cobertura do comportamento real, mas **não invalidam a suíte inteira**:

* `page.goto('/processo/...')` em specs que poderiam validar primeiro o acesso pelo painel;
* `executarComo()` em `e2e/helpers/helpers-auth.ts`, porque centraliza login e logout e pode ocultar ausência de validação intermediária;
* novos contextos de browser em testes que poderiam reutilizar a mesma jornada quando a troca de papel não é o foco principal;
* asserções muito centradas em estado final sem checar a transição que o usuário realmente executa.

### Críticos

Esses pontos têm maior chance de mascarar bug real e são os primeiros candidatos a endurecimento:

* `click({force: true})` em interações de hover;
* `limparNotificacoes()` engolindo erros amplamente;
* testes de setup com `expect(true).toBeTruthy()` como única validação;
* specs que dependem de fixture em estado final e validam apenas o que acontece depois, sem exercitar a criação do estado pela UI.

## O que a suíte faz bem

### 1. Prefere UI real para ações de negócio

Os testes validam fluxos completos como:

* criar processo;
* abrir subprocesso;
* adicionar, editar e remover atividades;
* importar atividades;
* aceitar, devolver e homologar cadastro/mapa;
* enviar lembrete.

### 2. Usa esperas corretas

A suíte favorece `waitForURL`, `waitForResponse` e `expect(...).toBeVisible()` em vez de `waitForTimeout()`. Isso é um bom sinal de robustez e reduz falsos positivos.

### 3. Asserções sobre estado final

Vários testes verificam o estado final da interface e do processo, não apenas um toast transitório. Isso é bom porque valida o efeito real da ação.

## Recomendações concretas

1. **Reduzir `force: true` ao mínimo necessário** e manter testes de hover focados em visibilidade/interação real quando possível.

2. **Trocar capturas amplas de erro por tratamento específico** em helpers de limpeza, para não esconder regressões de UI.

3. **Separar melhor o que é preparo e o que é validação**: fixture pode criar estado, mas o teste deve continuar exercitando a ação principal pela UI.

4. **Evitar usar navegação direta como substituto da jornada** quando o requisito inclui passagem pela listagem/painel.

5. **Manter specs críticos com pelo menos um fluxo integral por papel** para garantir que login, painel, listagem e detalhe continuem integrados.

## Veredito

A suíte está **bem orientada a comportamento real** e usa atalhos de forma razoável na maior parte dos casos. Os principais riscos não estão em mocks ou em banco falso, mas em helpers que podem suavizar demais falhas de interação.

Resumo da segunda passada:

* **Aceitável**: reset/fixtures de preparação e helpers semânticos.
* **Arriscado**: navegação direta e abstração excessiva de login/jornada.
* **Crítico**: force click, limpeza permissiva e testes vazios de setup.

Se eu tivesse que priorizar uma revisão adicional, começaria por:

1. `e2e/helpers/helpers-atividades.ts`
2. `e2e/helpers/helpers-navegacao.ts`
3. specs que entram direto em rotas profundas sem validar a jornada completa

No estado atual, essa priorização virou apenas uma fila de melhorias opcionais, não uma pendência bloqueadora.
