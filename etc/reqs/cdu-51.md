# CDU-51 - Finalizar processo de diagnóstico

Ator: ADMIN

Maturidade: Média

Base principal: Respostas do usuário sobre encerramento do processo, complementadas por paralelismo com a finalização de mapeamento e revisão.

## Pré-condições

- Login realizado com perfil ADMIN
- Existência de processo de diagnóstico na situação 'Em andamento'

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação 'Em andamento'.

2. O sistema mostra a tela `Detalhes do processo`.

3. O usuário clica em `Finalizar`.

4. O sistema verifica se todos os subprocessos das unidades participantes estão na situação 'Homologado'.

5. Caso negativo, o sistema mostra a mensagem `Não é possível finalizar o processo enquanto houver unidades com
   diagnóstico ainda não homologado`.

6. Caso positivo, o sistema mostra diálogo de confirmação com:
   - título `Finalização de processo`;
   - mensagem `Confirma a finalização do processo [DESCRICAO_PROCESSO]? Essa ação liberará os relatórios consolidados do diagnóstico.`;
   - botões `Cancelar` e `Finalizar`.

7. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação e permanece na mesma tela.

8. O usuário confirma.

9. O sistema muda a situação do processo para 'Finalizado'.

10. O sistema envia notificações por e-mail para todas as unidades participantes.

    10.1. Unidades operacionais e interoperacionais deverão receber um e-mail segundo este modelo:

    ```text
    Assunto: SGC: Finalização do processo [DESCRICAO_PROCESSO]

    Prezado(a) responsável pela [SIGLA_UNIDADE],

    Comunicamos a finalização do processo [DESCRICAO_PROCESSO] para a sua unidade.

    Os relatórios consolidados do diagnóstico já podem ser consultados no Sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

    10.2. Unidades intermediárias e interoperacionais deverão receber um e-mail com informações consolidadas das
    unidades subordinadas a elas, segundo o modelo:

    ```text
    Assunto: SGC: Finalização do processo [DESCRICAO_PROCESSO] em unidades subordinadas

    Prezado(a) responsável pela [SIGLA_UNIDADE],

    Comunicamos a finalização do processo [DESCRICAO_PROCESSO] para as unidades [SIGLAS_UNIDADES_SUBORDINADAS].

    Os relatórios consolidados do diagnóstico já podem ser consultados no Sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

11. O sistema redireciona para o `Painel` e mostra a mensagem `Processo finalizado`.

## Observação

PENDÊNCIA DE REFINAMENTO: esta primeira versão libera os relatórios apenas na finalização completa do processo, por
paralelismo com os módulos já existentes. Confirmar depois se haverá necessidade de consulta parcial após homologações
individuais de unidade.
