# CDU-44 - Realizar autoavaliação

Ator: SERVIDOR

## Pré-condições

- Login realizado com perfil SERVIDOR
- Processo de diagnóstico em andamento com participação da unidade do servidor

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento e o sistema mostra a tela
   `Detalhes do subprocesso`, conforme o caso de uso [CDU-42.md](cdu-42.md)`.

2. O usuário aciona o card `Autoavaliação`.

3. O sistema apresenta a tela `Autoavaliação de diagnóstico`, contendo a lista das competências vigentes da unidade e,
   para cada competência:
    - descrição da competência;
    - um controle de _toggle_ `Atividade e conhecimentos`, que permite mostrar/esconder as atividades e conhecimentos
      associados à competência;
    - campo `Importância`, com opções `NA` e os números de `1` a `6`;
    - campo `Domínio`, com opções `NA` e os números de `1` a `6`;
    - botão `Concluir autoavaliação`.

   **IMPORTANTE**: Se o servidor já tiver concluído a autoavaliação, o sistema mostra os dados acima apenas em modo
   somente-leitura e desabilita o botão  `Concluir autoavaliação`, não se aplicando os passos a seguir.

4. O usuário atribui um valor de domínio e de importância para cada uma das competências.

5. O sistema, durante a edição, salva automaticamente cada alteração realizada, sem necessidade de ação explícita de
   salvamento.

6. O usuário clica em `Concluir autoavaliação`.

7. O sistema verifica se todas as competências tiveram seus campos `Importância` e `Domínio` preenchidos.

   7.1. Caso exista competência com valores sem preencher, o sistema mostra a mensagem
   `Preencha importância e domínio para todas as competências.` e interrompe a conclusão.

   7.2. Caso tudo esteja preenchido, o sistema mostra uma tela de confirmação: "Confirma a conclusão da autoavaliação?",
   com botões `Concluir` e `Cancelar`, e, uma vez confirmado, altera a situação da avaliação do servidor para
   `Autoavaliação concluída`.

8. O sistema envia uma notificação por e-mail para o responsável pela unidade do subprocesso, com este modelo:

   ```text
   Assunto: SGC: Autoavaliação de [NOME_SERVIDOR] submetida para análise

   Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

   O servidor [NOME_SERVIDOR] concluiu a autoavaliação no processo [DESCRICAO_PROCESSO].

   A análise já pode ser realizada no Sistema de Gestão de Competências ([URL_SISTEMA]).
   ```

9. O sistema cria internamente um alerta com:
    - `Descrição`: "Autoavaliação de [NOME_SERVIDOR] submetida para análise"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUBPROCESSO]

10. O sistema redireciona para a tela `Detalhes do subprocesso` e mostra a mensagem `Autoavaliação concluída`.