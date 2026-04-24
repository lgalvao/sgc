# Plano de validacao e UX

Este documento e um guia vivo das diretrizes de validacao/feedback do SGC e do que ainda falta convergir.

O historico operacional fica resumido em [`tracking-validacao.md`](tracking-validacao.md). As diretrizes de UX complementares seguem em [`ux.md`](ux.md).

## Objetivo

Manter uma UX previsivel para validacao e feedback, reduzindo surpresa sobre:

- **quando** o erro aparece;
- **onde** o erro aparece;
- **como** a pessoa entende o que precisa corrigir;
- **quando** a acao principal deve ficar indisponivel.

## Diretrizes vigentes

### 1. Erro corrigivel nao usa toast

- erro de preenchimento simples deve aparecer no proprio campo com `BFormInvalidFeedback`;
- erro estrutural sem campo deve aparecer em `AppAlert` ou `BAlert` contextual;
- `notify` fica para falha sistemica, integracional ou de autorizacao.

### 2. Validacao acontece no submit, com correcao progressiva

- a obrigatoriedade deve aparecer no clique da acao, nao no carregamento inicial da tela;
- depois da primeira tentativa, a correcao deve limpar ou atualizar o erro local de forma progressiva;
- quando fizer sentido, usar `useValidacaoFormulario` para manter comportamento uniforme.

### 3. Botao desabilitado so para elegibilidade real

- `loading`, permissao, estado de workflow e dependencias objetivas podem desabilitar a acao;
- campo vazio ou invalido, por si so, nao deve virar "botao morto" sem explicacao contextual;
- quando houver pre-condicao mais rica que um campo simples, a pendencia precisa ficar visivel.

### 4. Foco e contexto importam

- apos falha de validacao, focar o primeiro campo invalido;
- em listas editaveis, o erro deve aparecer no item/cartao correspondente;
- em erros estruturais por colecao, manter erro global complementar apenas quando nao houver item unico a destacar.

### 5. Modais com campo obrigatorio nao devem fechar em submit invalido

- o modal precisa permanecer aberto ate a correcao ou cancelamento explicito;
- o estado de validacao deve ser resetado de forma previsivel ao abrir/fechar;
- a mensagem obrigatoria deve ficar no proprio campo, nao apenas no rodape ou em toast.

### 6. Regras reativas devem ser reservadas para casos objetivos

- datas, limites e coerencias temporais podem validar durante a edicao;
- isso nao substitui a validacao semantica no submit;
- sanitizacao nao substitui validacao de dominio.

## Excecoes intencionais

### Login e autenticacao

- erro de credencial, sessao e autorizacao pode continuar global;
- obrigatoriedade local de campos vazios continua desejavel.

### Workflow agregado

- fluxos como cadastro de atividades podem combinar erro por item, erro global e scroll/foco;
- nessas telas, a hierarquia do feedback importa mais do que uma uniformidade artificial.

### Validacoes temporais

- datas podem reagir imediatamente para evitar tentativa e erro desnecessarios;
- ainda assim, o submit invalido nao deve fechar modal nem falhar silenciosamente.

## Estado atual consolidado

Ja estao convergidos ao padrao-base:

- base compartilhada com `useValidacaoFormulario` e foco no primeiro erro;
- administracao, atribuicao temporaria, limpeza e relatorios;
- cadastro de atividades (`CadastroView`, `CadAtividadeForm`, `AtividadeItem`, `InlineEditor`);
- modais criticos de mapa, cadastro e subprocesso que tinham bloqueio preventivo ou falha silenciosa;
- contrato estruturado de erro entre backend e frontend nos fluxos principais tocados.

## Estado da trilha

No momento, **nao ha pendencias abertas nesta trilha de validacao/feedback**.

O hardening final foi concluido com:

- revisao dos modais com campo obrigatorio;
- garantia de `autoClose=false` nos modais que nao podem fechar em submit invalido;
- reforco de cobertura para submit invalido, permanencia aberta e nao disparo de acoes indevidas;
- limpeza da documentacao para manter apenas diretrizes e checklist vivo.

## Fora do escopo deste plano

- o `401` espurio em `/api/subprocessos/contexto-edicao/buscar` continua sendo uma investigacao separada de comportamento de sessao/rota;
- ele nao faz parte da convergencia de UX/validacao em si, embora possa gerar ruido durante E2E.

## Checklist curto para PR futuro

- [ ] erro corrigivel aparece inline ou em alerta contextual, sem toast;
- [ ] submit invalido nao falha silenciosamente;
- [ ] modal com campo obrigatorio permanece aberto em erro corrigivel;
- [ ] botao desabilitado tem motivo objetivo e visivel;
- [ ] primeiro erro relevante recebe foco;
- [ ] mensagem esta em portugues claro e orientada a acao.
