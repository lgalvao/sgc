# CDU-04 - Iniciar processo de mapeamento

Ator: ADMIN

Pré-condições:

- Existência de ao menos um processo de mapeamento na situação 'Criado'

Fluxo principal:

1. No Painel, ADMIN clica em um processo de mapeamento que esteja na situação 'Criado'.

2. O sistema muda para a tela `Cadastro de processo`. Os campos aparecem preenchidos com as informações do processo selecionado.

3. ADMIN clica no botão Iniciar processo.

4. O sistema mostra modal de confirmação: "Ao iniciar o processo, não será mais possível editá-lo ou removê-lo e todas as unidades participantes serão notificadas por e-mail.", com botões `Confirmar` e `Cancelar`.

5. Caso o usuário `Cancelar`, o sistema interrompe a operação de iniciação do processo, permanecendo na mesma tela.

6. ADMIN confirma.

7. O sistema armazena internamente uma cópia da árvore de unidades participantes e a vincula com o processo, a fim de preservar a representação hierárquica vigente no momento do início do processo.

8. O sistema muda a situação do processo de mapeamento para 'Em andamento';

9. O sistema cria internamente um subprocesso para cada unidade participante, que seja do tipo Operacional ou Interoperacional, com os seguintes campos e valores iniciais:

    - `Data limite etapa 1`: Data copiada da data limite da etapa inicial do processo.
    - `Situação`: 'Não iniciado'
    - `Observações`: Campo de texto formatado para registro de informações futuras pelo perfil ADMIN.
    - `Sugestões`: Campo de texto formatado para registro de sugestões futuras pelas unidades.

10. O sistema cria internamente um mapa de competências vazio (sem competências) e o vincula ao subprocesso da unidade.

11. O sistema registra uma movimentação para cada subprocesso criado com os campos:
    - `Data/hora`: Data/hora atual
    - `Unidade origem`: ADMIN
    - `Unidade destino`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Descrição`: 'Processo iniciado'

12. O sistema envia notificações por e-mail para todas as unidades participantes.

13. 12.1. Unidades operacionais e interoperacionais deverão receber um e-mail segundo o modelo:
    ```text

        Assunto: SGC: Início de processo de mapeamento de competências

        Prezado(a) responsável pela [SIGLA_UNIDADE],

        Comunicamos o início do processo [DESCRICAO_PROCESSO] para a sua unidade.

        Já é possível realizar o cadastro de atividades e conhecimentos no O sistema de Gestão de
        Competências ([URL_SISTEMA]).

        O prazo para conclusão desta etapa do processo é [DATA_LIMITE].
        ```

    12.2. Unidades intermediárias e interoperacionais deverão receber um e-mail com informações consolidadas das unidades operacionais e interoperacionais subordinadas a elas, segundo o modelo:

        ```text

        Assunto: SGC: Início de processo de mapeamento de competências em unidades subordinadas
        
        Prezado(a) responsável pela [SIGLA_UNIDADE],
        
        Comunicamos o início do processo [DESCRICAO_PROCESSO] nas unidades [SIGLAS_UNIDADES_SUBORDINADAS]. Estas unidades já podem iniciar o cadastro de atividades e conhecimentos. À medida que estes cadastros forem sendo disponibilizados, será possível visualizar e realizar a sua validação.

        O prazo para conclusão desta etapa do processo é [DATA_LIMITE]. Acompanhe o processo no O sistema de Gestão de Competências: [URL_SISTEMA].
        ```

14. O sistema cria internamente alertas para todas as unidades participantes.

    13.1. Para cada unidade **operacional** será criado um alerta com:

    - `Descrição`: "Início do processo"
    - `Processo`: [DESCRICAO_PROCESSO]
    - Data/hora: [Data/hora atual]
    - Unidade de origem: ADMIN
    - Unidade de destino: [SIGLA_UNIDADE].

    13.2. Para cada unidade **intermediária** será criado um alerta com:

    - Descrição: "Início do processo em unidade(s) subordinada(s)"
    - Processo: [DESCRICAO_PROCESSO]
    - Data/hora: [Data/hora atual]
    - Unidade de origem: ADMIN
    - Unidade de destino: [SIGLA_UNIDADE].

    13.3. Para cada unidade **interoperacional** serão criados dois alertas: um de unidade operacional e outro de unidade intermediária, como especificado acima.