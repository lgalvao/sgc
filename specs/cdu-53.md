# CDU-53 - Finalizar processo de diagnóstico

## Atores

- ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN.
- Existência de processo de diagnóstico em andamento.

## Fluxo principal

1. No `Painel`, o usuário aciona um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo`, especificada em [CDU-06 - Detalhar processo](cdu-06.md)

3. O usuário aciona `Finalizar`.

4. O sistema verifica se todos os subprocessos das unidades participantes estão na situação 'Homologado'.
    - Caso exista ao menos uma unidade ainda não homologada, o sistema mostra o alerta "Não é possível finalizar o
      processo: há unidades não homologadas", e interrompe a operação, sem sair da tela.

5. Caso todas as unidades estejam homologadas, o sistema mostra um diálogo de confirmação, com título "Finalização de processo"; "Confirma a finalização do processo :DESCRICAO_PROCESSO:? Essa ação encerrará o processo e notificará todas as unidades participantes." e botões `Cancelar` e `Finalizar`.

6. O usuário aciona `Finalizar`.

7. O sistema muda a situação do processo para 'Finalizado'.

8. Para cada unidade operacional ou interoperacional, o sistema:

   8.1. Cria um alerta voltado à unidade do subprocesso:
    - `Descrição`: "Processo finalizado"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: :DATA_HORA:
    - `Unidade origem`: ADMIN
    - `Unidade de destino`: :SIGLA_UNIDADE_SUBPROCESSO:

   8.2 Envia uma notificação por e-mail à unidade do subprocesso:
      ```text
      Assunto: SGC: Finalização de processo de diagnóstico
    
      Prezado(a) responsável pela :SIGLA_UNIDADE:,
    
      Comunicamos a finalização do processo :DESCRICAO_PROCESSO:.
    
      Os resultados consolidados do diagnóstico já podem ser consultados no 
      Sistema de Gestão de Competências (SGC): (:URL_SISTEMA:).
      ```

9. Para cada unidade intermediária, o sistema:

   9.1. Envia uma notificação por e-mail à unidade, consolidando as unidades subordinadas:
   ```text
       Assunto: SGC: Finalização de processo de diagnóstico em unidades subordinadas

       Prezado(a) responsável pela :SIGLA_UNIDADE:,
       
       Comunicamos a finalização do processo :DESCRICAO_PROCESSO: para as unidades :SIGLAS_UNIDADES_SUBORDINADAS:.
       
       Os resultados do diagnóstico destas unidades podem ser consultados no 
       Sistema de Gestão de Competências (SGC): (:URL_SISTEMA:).
    ```

   9.2. Cria um alerta voltado à unidade, consolidando as unidades subordinadas:
    - `Descrição`: "Processo finalizado em unidades subordinadas"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: [Data/hora atual]
    - `Unidade origem`: ADMIN
    - `Unidade de destino`: :SIGLA_UNIDADE_INTERMEDIARIA:

10. O sistema redireciona para o `Painel` e mostra o *toast* `Processo finalizado`.
