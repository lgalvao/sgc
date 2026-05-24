# Checklist de Simplificação do Frontend

Use este checklist em rodadas de simplificação estrutural no frontend do SGC.

O objetivo é reduzir complexidade acidental sem quebrar contrato, esconder regra importante ou trocar clareza por abstração.

## 1. Leitura e estrutura

- O arquivo começa pelo caso de uso principal, não por utilitários secundários.
- A função/composable pública é curta e legível.
- A ordem do arquivo ajuda a leitura de cima para baixo.
- O arquivo é organizado por ações reais do caso de uso, não por helpers técnicos genéricos.
- Helpers privados só existem quando revelam um conceito real.
- Helpers privados usam nomes curtos quando o contexto do arquivo já é suficiente.
- O arquivo não depende de “fábricas” artificiais só para empacotar `Ref`, `loading`, `erro` ou retorno.

## 2. Estado, fluxo e validação

- Estado e fluxo não ficam misturados sem necessidade.
- O estado local não coordena efeitos de UI escondidos.
- `nextTick`, scroll, modal e outros efeitos de render ficam no fluxo, não no estado.
- A validação local retorna resultado semântico explícito.
- O código não usa protocolos implícitos como `null`, `undefined` ou arrays para significados diferentes sem nomear isso.
- O fluxo principal deixa claro quando valida, quando interrompe, quando abre modal e quando aplica erro.
- Se erro, toast, modal ou invalidação só fazem sentido em uma ação específica, eles ficam junto dessa ação.
- A API pública do composable/view reflete as ações da tela, não a mecânica interna.

## 3. Contrato e defensividade

- Não há fallback para estados impossíveis pelo contrato atual.
- Degradação opcional só é aceitável quando o requisito permite explicitamente perder um complemento não crítico sem comprometer o fluxo principal.
- O frontend não inventa mensagem de erro para esconder violação de contrato interno.
- O arquivo não recompõe heurísticas locais quando o contrato já deveria vir explícito de outra borda.
- `try/catch` só permanece quando existe tratamento local real; não apenas log, fallback ou conversão silenciosa de erro.
- A simplificação não muda permissões, visibilidade ou habilitação sem confronto com os requisitos vigentes.
- Se o comportamento depende de situação do subprocesso, a situação esperada está explícita no código.

## 4. Nomes e superfície

- A nomenclatura interna é consistente.
- Nomes paralelos seguem o mesmo padrão verbal.
- Imports de constantes e helpers trazem apenas o que o arquivo realmente usa.
- A API pública do composable/view é coerente com a modelagem interna.
- Não há alias, compatibilidades ou nomes duplicados sem necessidade real.
- Se um helper precisa de muitos parâmetros, isso é sinal de fronteira ruim ou abstração artificial.
- Quando várias ações compartilham muitas dependências locais, prefira um contexto local explícito a passar parâmetros soltos demais.
- Se um helper privado ultrapassa 3 parâmetros, reagrupe em contexto ou opções semânticas em vez de manter assinatura posicional longa.

## 5. Critérios de corte

- A extração reduz responsabilidade real, não apenas redistribui linhas.
- O corte mantém a lógica na camada certa.
- Se a lógica é local da tela, ela permanece local da tela.
- Só vale criar reutilização quando há contrato compartilhado real.
- Se uma extração deixa o fluxo principal mais difícil de seguir, ela provavelmente não valeu a pena.
- Se um helper esconde fluxo importante da ação principal, ele provavelmente piorou o arquivo.

## 6. Validação da rodada

- O hotspot tocado saiu ou melhorou de forma mensurável no lint/cruft.
- Os testes focados do fluxo alterado continuam verdes.
- O diff remove complexidade acidental, não apenas muda a forma dela.
- O resultado final está mais fácil de explicar em poucas frases.

## Sinais de alerta

- Funções demais para coordenar erro, loading, modal e `nextTick`.
- Muitos retornos especiais com significado implícito.
- Defensividade para estados que nunca deveriam acontecer.
- `try/catch` que apenas registra log e devolve valor neutro.
- Funções com nomes longos só para compensar estrutura confusa.
- Helpers privados com “cara de infraestrutura compartilhável”, mas que só existem para reduzir linhas.
- Wrappers genéricos como “executar com loading/erro/toast” que escondem fluxo importante.
- Extrações criadas apenas para satisfazer budget de lint.
- Fluxo principal interrompido por detalhes mecânicos de infraestrutura.
