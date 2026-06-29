# CDU-55 - Gerar relatório de situação de capacitação

## Atores

- ADMIN
- GESTOR

## Pré-condições

- Usuário autenticado com perfil ADMIN ou GESTOR.
- Existência de processo de diagnóstico finalizado.

## Fluxo principal

1. O usuário acessa `Relatórios` na barra de navegação.

2. O sistema mostra os cards de relatórios disponíveis.

3. O usuário aciona o card `Situação de capacitação`.

4. O sistema mostra a tela `Relatório de situação de capacitação`, contendo:
    - seletor de processo de diagnóstico;
    - botões `Todas`, `Limpar`, `Gerar` e `PDF`.
    - árvore de unidades participantes do processo, com esses filtros:
        - para perfil ADMIN, todas as unidades participantes do processo selecionado;
        - para perfil GESTOR, apenas a unidade do usuário e suas subordinadas, recursivamente.

5. O usuário seleciona o processo e as unidades desejadas na árvore e aciona o botão `Gerar`.

6. O sistema mostra, logo abaixo dos controles, uma prévia agregada contendo, para cada unidade selecionada:
    - sigla da unidade
    - nome da unidade
    - para cada competência:
        - descrição da competência
        - quantitativo de registros com situação `NA`
        - quantitativo de registros com situação `AC`
        - quantitativo de registros com situação `EC`
        - quantitativo de registros com situação `C`
        - quantitativo de registros com situação `I`
        - quantidade total de servidores considerada no agrupamento

8. Caso não existam dados de situação de capacitação para o escopo selecionado, o sistema mostra a mensagem
   `Não há dados de situação de capacitação para o filtro informado.`

9. O usuário aciona o botão `PDF`.

10. O sistema gera arquivo PDF com os dados especificados acima, precedidos de cabeçalho formal com brasão, nome do
    sistema, data/hora da geração e filtros aplicados. O nome do arquivo deve seguir o padrão
    `sgc-rel-situacao-capacitacao-YYYY-MM-DD.pdf`.

## Observações

- O relatório oficial de situação de capacitação deve ser agregado e sem nomes de servidores.
