# CDU-53 - Gerar relatório de gaps de diagnóstico

**Ator:** ADMIN, GESTOR

## Descrição

Permite a geração de relatório consolidado de gaps de competência a partir dos dados de diagnóstico, sem identificação
 nominal de servidores.

## Pré-condições

- Usuário autenticado com perfil ADMIN ou GESTOR.
- Existência de processo de diagnóstico finalizado, ou de subprocessos homologados com dados consolidados disponíveis
  para o escopo selecionado.

## Fluxo principal

1. O usuário acessa `Relatórios` na barra de navegação.

2. O sistema mostra os cards de relatórios disponíveis.

3. O usuário aciona o card `Gaps de diagnóstico`.

4. O sistema mostra a tela `Relatório de gaps de diagnóstico`, contendo:
   - seletor de processo de diagnóstico;
   - árvore de unidades participantes do processo;
   - botões `Todas`, `Limpar`, `Gerar` e `PDF`.

5. O sistema aplica as seguintes restrições de escopo:
   - para o perfil ADMIN, a árvore pode conter todas as unidades participantes do processo selecionado;
   - para o perfil GESTOR, a árvore deve conter apenas a unidade do usuário e suas subordinadas, recursivamente.

6. O usuário seleciona o processo e as unidades desejadas na árvore e aciona o botão `Gerar`.

7. O sistema calcula o gap individual de cada avaliação elegível como `Importância - Domínio`.

   7.1. Quando a importância ou o domínio estiverem marcados como `NA`, ou não houver valor informado, a avaliação não
   entra nos cálculos consolidados do relatório.

8. O sistema processa os dados e mostra, logo abaixo dos controles, uma prévia agregada contendo, para cada unidade
   selecionada:
   - unidade (sigla e nome);
   - para cada competência:
     - descrição da competência;
     - média de importância considerada no agrupamento;
     - média de domínio considerada no agrupamento;
     - gap médio consolidado;
     - quantidade de servidores considerada no cálculo.

9. Caso não existam dados elegíveis para o escopo selecionado, o sistema mostra a mensagem `Não há dados de gap para o
   filtro informado.`

10. O usuário aciona o botão `PDF`.

11. O sistema gera arquivo PDF com os dados especificados acima, precedidos de cabeçalho formal com brasão, nome do
    sistema, data/hora da geração e filtros aplicados. O nome do arquivo deve seguir o padrão
    `sgc-rel-gaps-diagnostico-YYYY-MM-DD.pdf`.

## Observações

- O relatório oficial de gaps deve ser agregado e sem nomes de servidores.
- Consulta nominal, se necessária, deve ocorrer dentro do sistema e sob regra de acesso, não em relatório institucional
  exportável.
