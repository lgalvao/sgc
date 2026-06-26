# CDU-46 - Aprovar avaliação de consenso

Ator: SERVIDOR

## Pré-condições

- Login realizado com perfil SERVIDOR
- Existência de avaliação de consenso criada para o usuário, para todas as competências da unidade.
- Subprocesso localizado na unidade do usuário

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do subprocesso`, conforme o caso de uso [CDU-42.md](cdu-42.md), além dos botões
   `Voltar` e `Aprovar consenso`, na barra de botões do cabeçalho.

3. O usuário aciona o card `Avaliação de consenso`.

4. O sistema mostra a tela `Avaliação de consenso`, com uma grade contendo as descrições das competências vigentes da
   unidade e os valores de importância, domínio e consenso para cada competência. Todo os valores são somente-leitura.
   Abaixo um exemplo de grade:

   | Competência | Importância | Domínio | Consenso 
                        | :---- | :---: | :---: | :---:
   | Desc. competência 1 | 4 | 1 | 3
   | Desc. competência 2 | NA | NA | NA
   | Desc. competência 3 | 3 | 2 | 3

   4.1. Se o usuário já tiver aprovado a avaliação de consenso, o sistema mostra um aviso fixo: "A avaliação já foi
   aprovada." e desabilita o botão `Aprovar consenso` e o *caso de uso termina*.

5. Se o usuário acionar o botão `Aprovar consenso`.

   5.1. O sistema mostra uma tela de confirmação: "Confirma a aprovação do cosenso?"
   com botões `Cancelar` e `Aprovar consenso`
   
   5.2. Feita a confirmação, o sistema altera a situação do servidor para 'Avaliação de consenso aprovada'.

6. O sistema envia uma notificação por e-mail ao responsável pela unidade do subprocesso, com este modelo:

   ```text
   Assunto: SGC: Avaliação de consenso aprovada: [NOME_SERVIDOR]

   Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

   O servidor [NOME_SERVIDOR] aprovou a avaliação de consenso do processo [DESCRICAO_PROCESSO].

   Acompanhe o processo no Sistema de Gestão de Competências (SGC): ([URL_SISTEMA]).
   ```

7. O sistema cria internamente um alerta com estes campos/valores:
    - `Descrição`: "Avaliação de consenso aprovada: [NOME_SERVIDOR]"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUBPROCESSO]

8. O sistema redireciona para a tela `Detalhes do subprocesso` e mostra um *toast* com mensagem "Avaliação de consenso
   aprovada".