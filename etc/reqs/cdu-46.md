# CDU-45 - Aprovar avaliação de consenso

Ator: SERVIDOR

## Pré-condições

- Login realizado com perfil SERVIDOR
- Existência de avaliação de consenso criada para o usuário, para todas as competências da unidade.
- Subprocesso localizado na unidade do usuário

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento e o sistema mostra a tela
   `Detalhes do subprocesso`, conforme o caso de uso [CDU-42a.md](cdu-42a.md).`.

2. O usuário aciona o card `Avaliação de consenso`.

3. O sistema mostra a tela `Avaliação de consenso`, com uma grade contendo as descrições das competências vigentes da
   unidade e os valores de consenso, importância e domínio atribuídos para cada competência. Todo os valores são
   somente-leitura. Abaixo um exemplo de grade:

   | Competência | Importância | Domínio |
   | :---- | :---: | :---: |
   | Desc. competência 1 | 4 | 1 |
   | Desc. competência 2 | NA | NA |
   | Desc. competência 3 | 3 | 2 |

Abaixo da grade de competências, o sistema mostra o botão `Aprovar consenso`.

4. O usuário aciona `Aprovar consenso`.

5. O sistema altera a situação individual do servidor para 'Avaliação de consenso aprovada'.

6. O sistema envia uma notificação por e-mail para o responsável pela unidade do subprocesso:
   
   ```text
   Assunto: SGC: Avaliação de consenso de [NOME_SERVIDOR] aprovada

   Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

   O servidor [NOME_SERVIDOR] aprovou a avaliação de consenso do processo [DESCRICAO_PROCESSO].

   Acompanhe o processo no Sistema de Gestão de Competências ([URL_SISTEMA]).
   ```

7. O sistema cria internamente um alerta:
    - `Descrição`: "Avaliação de consenso aprovada: [NOME_SERVIDOR]"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUBPROCESSO]

8. O sistema redireciona para a tela `Detalhes do subprocesso` e mostra a mensagem "Avaliação de consenso aprovada".