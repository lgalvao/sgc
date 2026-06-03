# CDU-41 - Iniciar processo de diagnóstico

Ator: ADMIN

## Pré-condições

- Login realizado com perfil ADMIN
- Existência de processo de diagnóstico na situação 'Criado'

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação 'Criado'.

2. O sistema muda para a tela `Cadastro de processo`. Os campos aparecem preenchidos com os dados do processo
   selecionado.

3. O usuário clica em `Iniciar`.

4. O sistema mostra uma tela de confirmação, com texto "Ao iniciar o processo, não será mais possível editá-lo ou removê-lo e todas as unidades participantes serão notificadas por e-mail." e botões `Iniciar` e `Cancelar`.
   
5. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação de iniciação do processo, permanecendo na mesma tela.

6. O usuário clica em `Iniciar`, dentro da tela de confirmação.
   
7. O sistema armazena internamente uma cópia (snapshot) da árvore de unidades participantes, incluindo todos os servidores lotados em cada unidade participante, e a vincula com o processo.

8. O sistema muda a situação do processo para 'Em andamento'.

9. O sistema cria um subprocesso para cada uma das unidades operacionais ou interoperacionais participantes, com os seguintes campos e valores iniciais:
   - `Data limite etapa 1`: data copiada da data limite do processo;
   - `Situação`: 'Não iniciado';
    
10. O sistema registra uma movimentação para cada subprocesso criado com:
    - `Data/hora`: data/hora atual;
    - `Unidade origem`: 'ADMIN';
    - `Unidade destino`: [SIGLA_UNIDADE_SUBPROCESSO];
    - `Descrição`: 'Processo iniciado'.

12. O sistema envia notificações por e-mail para todas as unidades participantes.

    12.1. Unidades operacionais e interoperacionais deverão receber um e-mail segundo este modelo:

    ```text
    Assunto: SGC: Início de processo de diagnóstico de competências

    Prezado(a) responsável pela [SIGLA_UNIDADE],

    Comunicamos o início do processo [DESCRICAO_PROCESSO] para a sua unidade.

    Já é possível realizar o diagnóstico de competências no Sistema de Gestão de Competências ([URL_SISTEMA]).

    O prazo para conclusão desta etapa é [DATA_LIMITE].
    ```

    12.2. Unidades intermediárias e interoperacionais deverão receber um e-mail com informações consolidadas das
    unidades operacionais e interoperacionais subordinadas a elas, segundo o modelo:

    ```text
    Assunto: SGC: Início de processo de diagnóstico de competências em unidades subordinadas

    Prezado(a) responsável pela [SIGLA_UNIDADE],

    Comunicamos o início do processo [DESCRICAO_PROCESSO] nas unidades [SIGLAS_UNIDADES_SUBORDINADAS]. Essas unidades já podem iniciar o diagnóstico de competências. À medida que os diagnósticos forem sendo concluídos, será possível acompanhar e realizar a sua análise.

    O prazo para conclusão desta etapa do processo é [DATA_LIMITE].

    Acompanhe o processo no Sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

13. O sistema cria internamente alertas para todas as unidades participantes.

    13.1. Para cada unidade operacional será criado um alerta com:
    - `Descrição`: "Início do processo"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: [SIGLA_UNIDADE]

    13.2. Para cada unidade intermediária imediatamente superior à unidade participante, será criado um alerta com:
    - `Descrição`: "Início do processo em unidade(s) subordinada(s)"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

    [PENDENCIA: Como fica a agregação das unidades? Confirmar com outros CDUs paralelos.] 

  13.3. Para cada unidade interoperacional serão criados dois alertas: um de unidade operacional e outro de unidade intermediária, como especificado acima.