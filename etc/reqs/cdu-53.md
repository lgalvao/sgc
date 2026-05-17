# CDU-53 - Gerar relatório de situação de capacitação do diagnóstico

Ator: ADMIN, GESTOR

## Pré-condições

- Login realizado com perfil ADMIN ou GESTOR
- Existência de processo de diagnóstico na situação 'Finalizado'

## Fluxo principal

1. O usuário acessa `Relatórios` na barra de navegação.

2. O usuário clica no card `Situação de capacitação`.

3. O sistema mostra a tela `Relatório de situação de capacitação`, contendo:
   - seletor de processo de diagnóstico finalizado;
   - botão `Gerar`;
   - botões de exportação `PDF` e `CSV`.

4. Para o perfil GESTOR, a lista de processos deve ser filtrada para exibir apenas aqueles que envolvam a sua unidade
   ou subordinadas.

5. O usuário seleciona o processo desejado e clica em `Gerar`.

6. O sistema apresenta uma prévia consolidada por unidade, contendo, para cada unidade participante visível ao usuário:
   - sigla e nome da unidade;
   - relação dos servidores da unidade;
   - para cada servidor, a situação de capacitação registrada em cada competência;
   - totalizadores por competência e por unidade, agrupando os quantitativos de `NA`, `AC`, `EC`, `C` e `I`.

7. O usuário pode exportar a prévia em `PDF` ou `CSV`.

8. O sistema gera o arquivo correspondente, contendo a mesma prévia visualizada, precedida por cabeçalho formal com
   nome do sistema, data/hora da geração e identificação do processo selecionado.

## Observação

PENDÊNCIA DE REFINAMENTO: esta primeira versão especifica apenas a saída consolidada por unidade. Caso a área de
negócio demande outros recortes analíticos, o caso de uso deverá ser expandido.
