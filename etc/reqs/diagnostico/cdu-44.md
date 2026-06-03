# CDU-44 - Aprovar avaliação de consenso

Ator: SERVIDOR

## Pré-condições

- Login realizado com perfil SERVIDOR
- Existência de avaliação de consenso criada para o servidor, para todas as competências 

## Fluxo principal

1. No `Painel`o usuário acessa um processo de diagnóstico em andamento da sua unidade.

2. O sistema mostra a tela `Detalhes do subprocesso` para a unidade, com os detalhes do subprocesso e apenas um card `Avaliações`. 
   
3. O usuário aciona o card `Avaliações`.

4. O sistema mostra uma tabela com as competências e o valor da avaliação de consenso, de importância e domínio para cada competência, com um botão `Aprovar consenso`.
   
5. O usuário clica em `Aprovar consenso`.

3. O sistema altera a situação da avaliação do servidor para 'Avaliação de consenso aprovada'.

4. O sistema envia notificação por e-mail para o responsável pela unidade:

    ```text
    Assunto: SGC: Avaliação de consenso de [NOME_SERVIDOR] aprovada

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

    O servidor [NOME_SERVIDOR] aprovou a avaliação de consenso do processo [DESCRICAO_PROCESSO].

    Acompanhe o processo no Sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

5. O sistema cria internamente um alerta com:
    - `Descrição`: "Avaliação de consenso aprovada: [NOME_SERVIDOR] "
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUBPROCESSO]

6. O sistema redireciona para a tela `Detalhes do subprocesso` e mostra a mensagem "Avaliação de consenso aprovada".