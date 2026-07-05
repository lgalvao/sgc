# CDU-41 - Iniciar processo de diagnóstico

## Atores

- ADMIN

## Pré-condições

- Login realizado com perfil ADMIN
- Existência de processo de diagnóstico na situação 'Criado'

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação 'Criado'.

2. O sistema muda para a tela `Cadastro de processo`. Os campos aparecem preenchidos com os dados do processo
   selecionado.

3. O usuário aciona `Iniciar`.

4. O sistema mostra uma tela de confirmação, com texto "Ao iniciar o processo, não será mais possível editá-lo ou
   removê-lo e todas as unidades participantes serão notificadas por e-mail.", além de botões `Iniciar` e `Cancelar`.

5. O usuário aciona em `Iniciar processo`, dentro da tela de confirmação.

6. O sistema armazena internamente uma cópia (*snapshot*) da árvore de unidades participantes, incluindo todos os
   servidores lotados em cada unidade participante; depois vincula essa cópia com o processo.

7. O sistema muda a situação do processo para 'Em andamento'.

8. O sistema cria um subprocesso para cada uma das unidades operacionais ou interoperacionais participantes, com os
   seguintes campos/valores iniciais:
    - `Data limite etapa 1`: data copiada da data limite do processo;
    - `Situação`: 'Não iniciado';

   **IMPORTANTE**: O diagnóstico deve ser feito sobre o **mapa vigente da unidade**. O início do processo de diagnóstico
   não cria cópia do mapa nem novo mapa vinculado ao subprocesso. As competências consideradas no diagnóstico são as do
   mapa vigente da unidade, no momento do início do processo.

9. O sistema registra uma movimentação para cada subprocesso criado, com estes campos/valores:
    - `Descrição`: "Processo iniciado".
    - `Data/hora`: :DATA_HORA:;
    - `Unidade origem`: ADMIN;
    - `Unidade destino`: :SIGLA_UNIDADE_SUBPROCESSO:;

10. O sistema envia notificações por e-mail para todas as unidades participantes.

- Unidades operacionais e interoperacionais deverão receber este e-mail

   ```text
   Assunto: SGC: Início de processo de diagnóstico de competências
   
   Prezado(a) responsável pela :SIGLA_UNIDADE:,
   
   Comunicamos o início do processo :DESCRICAO_PROCESSO: para a sua unidade.
   
   Já é possível realizar o diagnóstico de competências no Sistema de Gestão de Competências (SGC) (:URL_SISTEMA:).
   
   O prazo para conclusão do diagnóstico é :DATA_LIMITE:.
   ```

- Unidades intermediárias e interoperacionais deverão receber um e-mail com informações consolidadas das unidades
  operacionais e interoperacionais subordinadas a elas, segundo o modelo:
    ```text
    Assunto: SGC: Início de processo de diagnóstico de competências em unidades subordinadas
    
    Prezado(a) responsável pela :SIGLA_UNIDADE:,
    
    Comunicamos o início do processo :DESCRICAO_PROCESSO: nas unidades :SIGLAS_UNIDADES_SUBORDINADAS:. 
    Essas unidades já podem iniciar o diagnóstico de competências. À medida que os diagnósticos forem sendo concluídos, será possível acompanhar e realizar a análise.
    
    O prazo para conclusão do processo é :DATA_LIMITE:.
    
    Acompanhe o processo no Sistema de Gestão de Competências (SGC):(:URL_SISTEMA:).
    ```

13. O sistema cria internamente alertas para todas as unidades participantes.

- Para cada unidade operacional será criado um alerta com:
    - `Descrição`: "Início do processo"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: :DATA_HORA:
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: :SIGLA_UNIDADE:

- Para a unidade intermediária imediatamente superior à unidade participante, será criado também um alerta com:
    - `Descrição`: "Início do processo"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: :DATA_HORA:
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: :SIGLA_UNIDADE_SUPERIOR:

- Para cada unidade interoperacional serão criados dois alertas: um de unidade operacional e outro de unidade
  intermediária, como especificado acima.

14. O sistema cria alertas individuais para todos os servidores de todas as unidades participantes, com exceção dos
    servidores responsáveis pelas unidades, com os campos/valores:
    - `Descrição`: "Início do processo"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: :DATA_HORA:
    - `Unidade de origem`: ADMIN
    - `Usuário de destino`: :TITULO_USUARIO:
