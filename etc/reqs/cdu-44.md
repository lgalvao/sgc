# CDU-44 - Aprovar avaliação de consenso

Ator: SERVIDOR

Maturidade: Alta

Base principal: Fluxo narrado e validado na reunião, confirmado pelas respostas do usuário.

## Pré-condições

- Login realizado com perfil SERVIDOR
- Existência de avaliação de consenso criada para o próprio servidor

## Fluxo principal

1. O usuário acessa a sua avaliação de consenso.

2. Estando de acordo com as informações apresentadas, o usuário clica em `Aprovar consenso`.

3. O sistema altera a situação da avaliação individual para `Consenso aprovado`.

4. O sistema envia notificação por e-mail para o responsável pela unidade:

    ```text
    Assunto: SGC: Avaliação de consenso de [NOME_SERVIDOR] aprovada

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

    O servidor [NOME_SERVIDOR] aprovou a avaliação de consenso do processo [DESCRICAO_PROCESSO].

    Acompanhe o processo no Sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

5. O sistema cria internamente um alerta com:
    - `Descrição`: "Avaliação de consenso de [NOME_SERVIDOR] aprovada"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUBPROCESSO]

6. O sistema mostra a mensagem `Consenso aprovado`.
