# Checklist operacional - lote 1 E2E

## Escopo
- CDUs do lote 1: `CDU-13`, `CDU-14`, `CDU-20`.
- Objetivo: fechar lacunas de cobertura E2E com menor risco de retrabalho.
- Estratégia: usar E2E para fluxo visível na UI e teste de integração backend para efeitos internos sem superfície estável.

## Ordem de implementação
1. `CDU-20`
2. `CDU-13`
3. `CDU-14`

## Critério de execução por CDU
- Adicionar pelo menos um cenário de fluxo principal explicitamente alinhado ao requisito.
- Adicionar pelo menos um cenário negativo ou de cancelamento.
- Adicionar pelo menos um assert de efeito colateral observável.
- Se `Data/hora`, movimentação, alerta interno ou e-mail não estiverem visíveis de forma estável na UI, complementar no backend.

## CDU-20
- Arquivo-alvo: `e2e/cdu-20.spec.ts`
- Adicionar assert explícito da tela `Detalhes do subprocesso` antes de entrar no card `Mapa de competências`.
- Criar cenário específico para `Histórico de análise`, validando:
- colunas `Data/hora`, `Unidade`, `Resultado`, `Observação`
- pelo menos uma linha com `Aceite` ou `Devolução`
- Transformar o fluxo de devolução em cenário completo:
- abrir modal
- preencher observação
- confirmar devolução
- validar redirecionamento ao `Painel`
- validar mensagem `Devolução realizada`, se estável
- Adicionar cenário de cancelamento da homologação para perfil `ADMIN`, confirmando permanência na mesma tela.
- Se houver alerta observável na UI, validar efeito colateral no painel da unidade destinatária.
- Se não houver superfície estável para `Data/hora atual` ou movimentação, abrir complemento em teste de integração backend.

## CDU-13
- Arquivo-alvo: `e2e/cdu-13.spec.ts`
- Adicionar cenário explícito de navegação pelo fluxo do requisito:
- `Painel`
- `Detalhes do processo`
- clique na unidade subordinada
- Reforçar `Histórico de análise` com assert de estrutura da tabela, não apenas conteúdo:
- cabeçalhos
- unidade
- resultado
- observação
- Adicionar cenário de cancelamento da devolução, confirmando permanência na tela.
- Adicionar cenário de cancelamento da homologação, confirmando permanência na tela.
- Após `Registrar aceite`, validar redirecionamento ao `Painel` e mensagem `Aceite registrado`, se estável.
- Se a UI expuser movimentação, validar `Data/hora` por regex e `Unidade origem/destino`.
- Se não expuser de forma estável, complementar no backend.

## CDU-14
- Arquivo-alvo: `e2e/cdu-14.spec.ts`
- Tornar `Impactos no mapa` evidência forte:
- abrir modal ou tela de impactos
- validar conteúdo funcional, não apenas presença do botão
- Reforçar `Histórico de análise` com cabeçalhos e linha de devolução.
- Manter o teste de cancelamento da devolução e acrescentar prova de permanência na mesma tela sem alteração indevida de situação.
- Adicionar cenário explícito de aceite com redirecionamento ao `Painel` e mensagem `Aceite registrado`, se estável.
- Verificar se existe fluxo administrado de homologação nesse contexto:
- se existir, adicionar cancelamento da homologação
- se não existir, registrar a limitação no alinhamento
- Se `Data/hora atual`, movimentação ou alerta não estiverem visíveis de forma confiável na UI, complementar no backend.

## Regra prática
- Validar em E2E: botões, modais, navegação, status, tabelas e mensagens estáveis.
- Validar em backend: `Data/hora atual`, movimentações internas, alertas internos e envio de e-mail quando a UI não oferecer evidência objetiva.
