# CDU-42 - Realizar autoavaliação de diagnóstico

Ator: SERVIDOR

## Pré-condições

- Login realizado com perfil SERVIDOR
- Processo de diagnóstico em andamento com participação da unidade do servidor

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação 'Em andamento'.

2. O sistema mostra a tela `Detalhes do subprocesso` da unidade do servidor.

3. O usuário clica no card `Autoavaliação`.

4. O sistema apresenta a tela `Autoavaliação de diagnóstico`, contendo a lista das competências vigentes da unidade e, para cada competência:
   - descrição da competência;
   - botão `Atividade e conhecimentos`, que permite mostrar as atividades e conhecimentos associados à competência;
   - campo `Importância`, com opções `NA` e os números de `1` a `6`;
   - campo `Domínio`, com opções `NA` e os números de `1` a `6`.

6. O usuário escolhe os valores desejados para cada uma das competências.

7. O sistema, durante a edição, salva automaticamente cada alteração realizada, sem necessidade de ação explícita de salvamento.

8. O usuário clica em `Concluir autoavaliação`.

9. O sistema verifica se todas as competências tiveram seus campos `Importância` e `Domínio` preenchidos. 
      
   9.1. caso exista competência com valores sem preencher o sistema mostra a mensagem `Preencha importância e domínio para todas as competências.` e interrompe a conclusão da autoavaliação. 
      
   9.2. Caso tudo estiver preenchido, o sistema mostra uma tela de confirmação: "Confirma a conclusão da autoavaliação?", com botões `Confirmar` e `Cancelar`; uma vez confirmado, altera a situação da avaliação individual do servidor para 'Autoavaliação concluída'.

10. O sistema envia uma notificação por e-mail para o responsável pela unidade, com este modelo:

    ```text
    Assunto: SGC: Autoavaliação de [NOME_SERVIDOR] submetida para análise

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

    O servidor [NOME_SERVIDOR] concluiu a autoavaliação no processo [DESCRICAO_PROCESSO].

    A análise já pode ser realizada no Sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

11. O sistema cria internamente um alerta com:
    - `Descrição`: "Autoavaliação de [NOME_SERVIDOR] submetida para análise"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUBPROCESSO]

12. O sistema redireciona para a tela `Detalhes do subprocesso` e mostra a mensagem `Autoavaliação concluída`.