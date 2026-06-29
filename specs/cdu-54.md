# CDU-54 - Gerar relatório de gaps de diagnóstico

## Atores

- ADMIN
- GESTOR

## Pré-condições

- Usuário autenticado com perfil ADMIN ou GESTOR.
- Existência de processo de diagnóstico finalizados

## Fluxo principal

1. O usuário acessa `Relatórios` na barra de navegação.

2. O sistema mostra os cards de relatórios disponíveis.

3. O usuário aciona o card `Gaps de diagnóstico`.

4. O sistema mostra a tela `Relatório de gaps de diagnóstico`, contendo:
    - seletor de processo de diagnóstico;
    - botões `Todas`, `Limpar`, `Gerar` e `PDF`.
    - árvore de unidades participantes do processo, com esses filtros:
        - para o perfil ADMIN, todas as unidades participantes do processo selecionado;
        - para o perfil GESTOR, apenas a unidade do usuário e suas subordinadas, recursivamente.

6. O usuário seleciona o processo, depois seleciona unidades na árvore e aciona `Gerar`.

7. O sistema calcula o gap individual de cada avaliação elegível como `Importância - Domínio`. 7.1. Quando a importância
   ou o domínio estiverem marcados como `NA` a avaliação correspondente não entra nos cálculos consolidados do
   relatório.

8. O sistema processa os dados e mostra, logo abaixo dos controles, uma prévia agregada contendo, para cada unidade
   selecionada:
    - sigla da unidade
    - nome da unidade
    - para cada competência:
        - descrição da competência
        - média de importância considerada no agrupamento
        - média de domínio considerada no agrupamento
        - gap médio consolidado
        - quantidade de servidores considerada no cálculo

9. Caso não existam dados elegíveis para o escopo selecionado, o sistema mostra a mensagem `Não há dados de gap para o
   filtro informado.`

10. O usuário aciona o botão `PDF`.

11. O sistema gera arquivo PDF com os dados especificados acima, precedidos de cabeçalho formal com brasão, nome do
    sistema, data/hora da geração e filtros aplicados. O nome do arquivo deve seguir o padrão
    `sgc-rel-gaps-diagnostico-YYYY-MM-DD.pdf`.

## Observações
- O relatório oficial de gaps deve ser agregado e sem nomes de servidores.
