# CDU-05 - Iniciar processo de revisão

Ator: ADMIN

Pré-condições:

- Login realizado com perfil ADMIN
- Existência de processo de revisão na situação 'Criado'

Fluxo principal:

1. No Painel, ADMIN clica em um processo de revisão que esteja na situação 'Criado'.

2. O sistema muda para a tela `Cadastro de processo`, com os campos preenchidos com as informações do processo
   selecionado.

3. ADMIN clica no botão `Iniciar processo`.

4. O sistema mostra diálogo de confirmação: "Ao iniciar o processo, não será mais possível editá-lo ou removê-lo e todas
   as unidades participantes serão notificadas por e-mail.", botões Confirmar e Cancelar.

5. Caso o usuário escolha o botão Cancelar, o sistema interrompe a operação de iniciação do processo, permanecendo na
   mesma tela.

6. ADMIN confirma.

7. O sistema armazena internamente uma cópia da árvore de unidades participantes e a vincula com o processo, a fim de
   preservar a hierarquia de unidades vigente, no momento da iniciação do processo.

8. O sistema muda a situação do processo de revisão para 'Em andamento';

9. O sistema cria internamente um subprocesso para cada unidade operacional ou interoperacional participante, com os
   seguintes campos e valores iniciais:
    - `Data limite etapa 1`: Data copiada da data limite da etapa inicial do processo.
    - `Situação`: 'Não iniciado'
    - `Observações`: Campo de texto formatado reservado para registro de informações futuras pelo perfil ADMIN.
    - `Sugestões`: Campo de texto formatado reservado para registro de sugestões futuras pelas unidades.

10. O sistema cria internamente uma cópia do mapa de competências vigente, juntamente com as respectivas atividades e
    conhecimentos, de cada unidade operacional ou interoperacional participante, vinculando-o ao subprocesso da unidade.

11. O sistema registra uma movimentação para cada subprocesso criado com os campos:
    - `Data/hora`: Data/hora atual
    - `Unidade origem`: 'SEDOC'
    - `Unidade destino`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Descrição`: 'Processo iniciado'

12. O sistema envia notificações por e-mail para todas as unidades participantes.

    12.1. Unidades operacionais e interoperacionais deverão receber um e-mail segundo o modelo:

     ```text
        Assunto: SGC: Início de processo de revisão do mapa de competências
        
        Prezado(a) responsável pela [SIGLA_UNIDADE],
        
        Comunicamos o início do processo [DESCRICAO_PROCESSO] para a sua unidade.
        Já é possível realizar a revisão do seu cadastro de atividades e conhecimentos no O sistema de Gestão de Competências ([URL_SISTEMA]).
        
        O prazo para conclusão desta etapa do processo é [DATA_LIMITE].
     ```

    12.2. Unidades intermediárias e interoperacionais deverão receber um e-mail com informações consolidadas das
    unidades operacionais e interoperacionais subordinadas a elas, segundo o modelo:

      ```text
         Assunto: SGC: Início de processo de mapeamento de competências em unidades subordinadas

         Prezado(a) responsável pela [SIGLA_UNIDADE],
         
         Comunicamos o início do processo [DESCRIÇÃO_PROCESSO] nas unidades [SIGLAS_UNIDADES_SUBORDINADAS]. Estas unidades já podem iniciar a revisão do cadastro de atividades e conhecimentos. À medida que estas revisões forem sendo disponibilizadas, será possível visualizar e realizar a sua validação.

         O prazo para conclusão desta etapa do processo é [DATA_LIMITE].
         
         Acompanhe o processo no sistema de Gestão de Competências: [URL_SISTEMA].
      ```

13. O sistema cria internamente alertas para todas as unidades participantes.
    13.1. Para cada unidade operacional será criado um alerta com estes campos/valores:
    - `Descrição`: "Início do processo"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: SEDOC
    - `Unidade de destino`: [SIGLA_UNIDADE].

    13.2. Para cada unidade intermediária será criado um alerta com estes campos/valores:
    - `Descrição`: "Início do processo em unidade(s) subordinada(s)"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: SEDOC
    - `Unidade de destino`: [SIGLA_UNIDADE].

    13.3. Para cada unidade interoperacional serão criados dois alertas: um de unidade operacional e outro de unidade
    intermediária, como especificado acima.
