# CDU-52 - Gerar relatório de gaps de diagnóstico

Ator: ADMIN, GESTOR

## Pré-condições

- Login realizado com perfil ADMIN ou GESTOR
- Existência de processo de diagnóstico na situação 'Finalizado'

## Fluxo principal

1. O usuário acessa `Relatórios` na barra de navegação.

2. O usuário clica no card `Gaps de diagnóstico`.

3. O sistema mostra a tela `Relatório de gaps de diagnóstico`, contendo:
   - seletor de processo de diagnóstico finalizado;
   - campo `Consolidação`, com as opções `Por servidor`, `Por unidade` e `Por competência`;
   - botão `Gerar`;
   - botões de exportação `PDF` e `CSV`.

4. Para o perfil GESTOR, a lista de processos deve ser filtrada para exibir apenas aqueles que envolvam a sua unidade
   ou subordinadas.

5. O usuário seleciona o processo e a forma de consolidação desejada, e clica em `Gerar`.

6. O sistema calcula os gaps usando a fórmula `Importância - Domínio`, desconsiderando dos cálculos consolidados as
   linhas em que `Importância = NA` ou `Domínio = NA`.

   PENDÊNCIA DE REFINAMENTO: esta fórmula foi adotada como base inicial da especificação e ainda precisa de validação
   funcional final com a área de negócio.

7. O sistema apresenta a prévia em tela de acordo com a consolidação escolhida:
   - `Por servidor`: lista os servidores e seus gaps por competência;
   - `Por unidade`: consolida os gaps por unidade participante;
   - `Por competência`: consolida os gaps por competência, considerando o escopo visível ao usuário.

8. O usuário pode exportar a prévia em `PDF` ou `CSV`.

9. O sistema gera o arquivo correspondente, contendo a mesma consolidação visualizada, precedida por cabeçalho formal
   com nome do sistema, data/hora da geração, processo e tipo de consolidação escolhida.

## Observação

PENDÊNCIA DE REFINAMENTO: filtros adicionais, ordenações e outros recortes analíticos ainda não foram detalhados nesta
primeira versão.
