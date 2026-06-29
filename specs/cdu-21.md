# CDU-21 - Finalizar processo de mapeamento ou de revisão

## Atores

- ADMIN

## Pré-condições

- Login realizado com perfil ADMIN
- Existência de processo de mapeamento ou de revisão na situação 'Em andamento'

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de mapeamento ou de revisão com situação 'Em andamento'.

2. O sistema mostra a tela `Detalhes do processo`.

3. O usuário aciona `Finalizar`.

4. O sistema verifica se todos os subprocessos das unidades operacionais e interoperacionais participantes estão na
   situação 'Mapa homologado'.

5. Caso negativo, o sistema exibe a mensagem "Não é possível finalizar o processo enquanto houver unidades com mapa não
   homologado", interrompe a operação e permanece na mesma tela.

6. Caso positivo, o sistema mostra diálogo com título "Finalização de processo" e mensagem "Confirma a finalização do
   processo [DESCRICAO_PROCESSO]? Essa ação tornará vigentes os mapas de competências homologados e notificará todas as
   unidades participantes do processo.", além de botões `Cancelar` e `Finalizar`.

7. O usuário aciona `Finalizar`.

8. O sistema define os mapas de competências dos subprocessos como os mapas de competências vigentes das respectivas
   unidades.

9. O sistema muda a situação do processo para 'Finalizado' e envia notificações por e-mail para todas as unidades
   participantes, como a seguir:

   9.1. Unidades operacionais e interoperacionais deverão receber um e-mail segundo o modelo:

   ```text
   Assunto: SGC: Finalização de processo de [TIPO_PROCESSO]

   Prezado(a) responsável pela [SIGLA_UNIDADE],

   Comunicamos a finalização do processo [DESCRIÇÃO_PROCESSO] para a sua unidade.

   Já é possível visualizar o seu mapa de competências atualizado através do menu 
   "Minha unidade" do Sistema de Gestão de Competências ([URL_SISTEMA]).
   ```

   9.2. Unidades intermediárias e interoperacionais deverão receber um e-mail com informações consolidadas das unidades
   operacionais e interoperacionais subordinadas a elas, segundo o modelo:

   ```text
   Assunto: SGC: Finalização do processo de [TIPO_PROCESSO] em unidades subordinadas

   Prezado(a) responsável pela [SIGLA_UNIDADE],

   Comunicamos a finalização do processo [DESCRIÇÃO_PROCESSO] para as unidades [SIGLAS_UNIDADES_SUBORDINADAS].

   Já é possível visualizar os mapas de competências atualizados destas unidades através do menu 
   "Minha unidade" do Sistema de Gestão de Competências ([URL_SISTEMA]).
   ```

   Em ambos os modelos, [TIPO_PROCESSO] será "mapeamento" ou "revisão", conforme o caso.

10. O sistema redireciona para o `Painel`, mostrando a mensagem "Processo finalizado".
