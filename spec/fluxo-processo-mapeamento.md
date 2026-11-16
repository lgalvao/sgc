# Fluxo de Criação de Processo de Mapeamento no SGC

## Visão Geral

O processo de criação de um novo processo de mapeamento no SGC envolve a seleção de unidades participantes, respeitando
a hierarquia organizacional e garantindo a validação adequada dos campos obrigatórios.

## Exibição Hierárquica das Unidades

- As unidades são exibidas em formato de árvore, com indentação visual baseada em Bootstrap (`ms-*`).
- A hierarquia é construída no backend, utilizando um DTO (`UnidadeDto`) que contém o ID, nome, ID da unidade superior e
  lista de subordinadas.
- O template Thymeleaf renderiza a árvore de forma recursiva, facilitando a visualização da estrutura organizacional.

## Seleção em Cascata

- Ao marcar uma unidade superior, todas as subordinadas diretas e indiretas são automaticamente marcadas.
- Ao desmarcar uma unidade superior, todas as subordinadas são desmarcadas.
- Essa lógica é implementada via JavaScript simples, que utiliza atributos `data-id` e `data-parent` nos checkboxes para
  identificar relações de hierarquia.
- O evento de seleção é recursivo, garantindo que múltiplos níveis de hierarquia sejam respeitados.

## Validação de Obrigatoriedade

- O sistema exige que pelo menos uma unidade participante seja selecionada para permitir a criação do processo.
- A validação é feita tanto no frontend (JS/HTML5) quanto no backend (Spring/Thymeleaf).
- Caso nenhuma unidade seja selecionada, o formulário é bloqueado e uma mensagem de erro é exibida acima da árvore de
  unidades.

## Feedback Visual

- Utiliza classes do Bootstrap (`is-invalid`, `alert alert-danger`) para destacar campos obrigatórios não preenchidos.
- O feedback é imediato ao tentar submeter o formulário sem unidades marcadas.
- O botão de submit possui `data-testid` para facilitar a automação de testes.

## Restrições Técnicas Adotadas

- Uso mínimo de JavaScript, apenas para seleção em cascata e feedback visual.
- Não é utilizado AJAX nem REST para atualização de dados na tela.
- Máximo aproveitamento dos utilitários do Bootstrap para layout e feedback visual.
- Templates e lógica de exibição priorizam Thymeleaf e HTML puro.

## Pontos de Atenção e Lições dos Testes

- O DTO deve conter o campo de referência ao superior (`idUnidadeSuperior`) para que o JS funcione corretamente.
- O evento de seleção em cascata depende do correto preenchimento dos atributos `data-id` e `data-parent`.
- Testes automatizados com Playwright são essenciais para garantir o funcionamento da seleção em cascata e do feedback
  visual.
- É importante garantir que o JS do template está sendo carregado e executado corretamente no navegador.
- O uso de logs do console do navegador pode ajudar a depurar problemas de integração JS/HTML.
- O timeout dos testes deve ser ajustado conforme a performance esperada do sistema (no SGC, 3 segundos são
  suficientes).

---

Este documento serve como referência para desenvolvedores e analistas que desejam entender ou evoluir o fluxo de criação
de processos de mapeamento no SGC.
