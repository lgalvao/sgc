# CDU-44 - Realizar autoavaliação

## Atores

- SERVIDOR

## Pré-condições

- Login realizado com perfil SERVIDOR
- Processo de diagnóstico em andamento, com participação da unidade do servidor

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento;

2. O sistema mostra a tela `Detalhes do subprocesso`, conforme o caso de uso [CDU-42.md](cdu-42.md)`.

3. O usuário aciona o card `Autoavaliação`.

4. O sistema mostra a tela `Autoavaliação de diagnóstico`, contendo:
    - Na barra de botões do cabeçalho, o botão `Concluir autoavaliação`;
    - Uma tabela com as competências vigentes da unidade; para cada competência:
        - descrição da competência;
        - controle `Atividade e conhecimentos` para expandir/recolher uma região com as atividades e conhecimentos
          associados à competência; a região deve iniciar recolhida;
        - campo `Importância`, com opções `NA` e números de `1` a `6`;
        - campo `Domínio`, com opções `NA` e os números de `1` a `6`;
    - **IMPORTANTE**: Se o servidor já tiver concluído a autoavaliação, o sistema mostra os elementos acima apenas em
      modo somente-leitura e desabilita o botão `Concluir autoavaliação`, e o **caso de uso termina**.

5. O usuário atribui valores de domínio e de importância para todas as competências.

6. O sistema, durante a edição, salva automaticamente cada alteração realizada, sem necessidade de ação explícita de
   salvamento.

7. O usuário clica em `Concluir autoavaliação`.

8. O sistema verifica se todas as competências tiveram seus campos `Importância` e `Domínio` preenchidos.

   8.1. Caso exista competência com valores sem preencher, o sistema mostra a mensagem
   "Preencha importância e domínio para todas as competências." e interrompe a conclusão.

   8.2. Caso tudo esteja preenchido, o sistema mostra uma tela de confirmação: "Confirma a conclusão da autoavaliação?",
   com botões `Cancelar` e `Concluir`

   8.3. feita a confirmação, o sistema altera a situação do servidor para 'Autoavaliação concluída'.

9. O sistema envia uma notificação por e-mail para o responsável pela unidade do subprocesso, com este modelo:

   ```text
   Assunto: SGC: Autoavaliação concluída: :NOME_SERVIDOR: 

   Prezado(a) responsável pela :SIGLA_UNIDADE_SUBPROCESSO:,

   O servidor :NOME_SERVIDOR: concluiu a autoavaliação no processo :DESCRICAO_PROCESSO:.

   A análise já pode ser realizada no Sistema de Gestão de Competências (SGC): (:URL_SISTEMA:).
   ```

10. O sistema cria internamente um alerta com estes campos/valores:
    - `Descrição`: "Autoavaliação concluída: :NOME_SERVIDOR:"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: :SIGLA_UNIDADE_SUBPROCESSO:
    - `Unidade de destino`: :SIGLA_UNIDADE_SUBPROCESSO:

11. O sistema redireciona para a tela `Detalhes do subprocesso` e mostra um *toast* com a mensagem "Autoavaliação
    concluída".
