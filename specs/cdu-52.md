# CDU-52 - Finalizar processo de diagnóstico

**Ator:** ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN.
- Existência de processo de diagnóstico na situação `Em andamento`.

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico com situação `Em andamento`.

2. O sistema mostra a tela `Detalhes do processo`.

3. O usuário clica no botão `Finalizar`.

4. O sistema verifica se todos os subprocessos das unidades operacionais e interoperacionais participantes estão na
   situação `Homologado`.

5. Caso exista ao menos uma unidade ainda não homologada, o sistema exibe a mensagem `Não é possível finalizar o
   processo enquanto houver unidades com diagnóstico ainda não homologado`.

6. Caso todas as unidades estejam homologadas, o sistema mostra diálogo de confirmação com:
    - título "Finalização de processo";
    - mensagem:
        - "Confirma a finalização do processo [DESCRICAO_PROCESSO]? Essa ação encerrará o ciclo de diagnóstico e
          notificará todas as unidades participantes do processo.";
    - botões `Cancelar` e `Finalizar`.

7. O usuário clica em `Finalizar`.

8. O sistema muda a situação do processo para `Finalizado` 

9. O sistema envia notificações por e-mail para todas as unidades participantes, como a seguir:

   9.1. Unidades operacionais e interoperacionais deverão receber um e-mail segundo o modelo:

   ```text
   Assunto: SGC: Finalização do processo [DESCRICAO_PROCESSO]

   Prezado(a) responsável pela [SIGLA_UNIDADE],

   Comunicamos a finalização do processo [DESCRICAO_PROCESSO] para a sua unidade.

   Os resultados consolidados do diagnóstico já podem ser consultados no Sistema de Gestão de Competências ([URL_SISTEMA]).
   ```

   9.2. Unidades intermediárias deverão receber um e-mail com informações consolidadas das unidades operacionais e
   interoperacionais subordinadas a elas, segundo o modelo:

   ```text
   Assunto: SGC: Finalização do processo [DESCRICAO_PROCESSO] em unidades subordinadas

   Prezado(a) responsável pela [SIGLA_UNIDADE],

   Comunicamos a finalização do processo [DESCRICAO_PROCESSO] para as unidades [SIGLAS_UNIDADES_SUBORDINADAS].

   Os resultados consolidados do diagnóstico destas unidades já podem ser consultados no Sistema de Gestão de Competências ([URL_SISTEMA]).
   ```

10. O sistema redireciona para o `Painel`, mostrando a mensagem `Processo finalizado`.