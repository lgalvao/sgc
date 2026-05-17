# CDU-40 - Realizar autoavaliação de diagnóstico

Ator: SERVIDOR

## Pré-condições

- Login realizado com perfil SERVIDOR
- Processo de diagnóstico em andamento com participação da unidade do servidor
- Servidor participante do snapshot do processo e com avaliação individual não impossibilitada

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação 'Em andamento'.

2. O sistema mostra a tela `Detalhes do subprocesso` da unidade do servidor.

3. O usuário clica no card `Diagnóstico da equipe`.

4. O sistema apresenta a tela `Autoavaliação de diagnóstico`, contendo a lista das competências vigentes da unidade.

5. Para cada competência, o sistema mostra:
   - a descrição da competência;
   - opção para detalhar as atividades e conhecimentos associados à competência;
   - campo `Importância`, com opções `NA`, `1`, `2`, `3`, `4`, `5` e `6`;
   - campo `Domínio`, com opções `NA`, `1`, `2`, `3`, `4`, `5` e `6`.

6. O usuário preenche os valores desejados para todas as competências.

7. O sistema salva automaticamente cada alteração realizada, sem necessidade de ação explícita de salvamento.

8. O usuário clica em `Concluir autoavaliação`.

9. O sistema verifica se todas as competências tiveram seus campos `Importância` e `Domínio` preenchidos.

10. Caso positivo, o sistema altera a situação da avaliação individual do servidor para 'Autoavaliação concluída'.

11. O sistema envia notificação por e-mail para o responsável pela unidade, com este modelo:

    ```text
    Assunto: SGC: Autoavaliação de [NOME_SERVIDOR] submetida para análise

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

    O servidor [NOME_SERVIDOR] concluiu a autoavaliação no processo [DESCRICAO_PROCESSO].

    A análise já pode ser realizada no Sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

12. O sistema cria internamente um alerta com:
    - `Descrição`: "Autoavaliação de [NOME_SERVIDOR] submetida para análise"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUBPROCESSO]

13. O sistema mostra a mensagem `Autoavaliação concluída`.

## Fluxo alternativo

1. No passo 9, caso exista competência sem `Importância` ou `Domínio` preenchidos, o sistema mostra a mensagem
   `Preencha importância e domínio para todas as competências.` e interrompe a conclusão da autoavaliação.
