# CDU-24 - Disponibilizar mapas de competências em bloco

## Atores

- ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN.
- Ao menos um subprocesso na situação 'Mapa criado' (Mapeamento), ou 'Mapa ajustado' (Revisão).

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de mapeamento/revisão em andamento.

2. O sistema mostra a tela `Detalhes do processo`, incluindo o botão `Disponibilizar mapas em bloco`.

3. O usuário aciona `Disponibilizar mapas em bloco`.

4. O sistema identifica as unidades aptas à disponibilização do mapa; ou seja, com subprocesso nas situações 'Mapa
   criado' ou 'Mapa ajustado'. Depois abre um modal com título "Disponibilização de mapas em bloco" e texto
   "Selecione as unidades cujos mapas deverão ser disponibilizados:", além dos elementos:
    - siglas das unidades operacionais/interoperacionais aptas, com um checkbox (selecionado por padrão) para cada
      unidade;
    - Campo `Data limite`
    - Botões `Cancelar` e `Disponibilizar em bloco`.

5. O usuário aciona `Disponibilizar em bloco`.

6. O sistema verifica se todas as competências dos mapas dos subprocessos das unidades selecionadas estão associadas a
   ao menos uma atividade dos cadastros das unidades, e, em sentido oposto, se todas as atividades foram associadas a ao
   menos uma competência do mapa da unidade.

7. Caso negativo, o sistema interrompe a operação e permanece na tela `Detalhes do processo`, informando a mensagem de
   erro: "Não é possível realizar a disponibilização em bloco dos mapas de competências das unidades :
   LISTA_UNIDADES_SELECIONADAS:. Realize a disponibilização individual do mapa de cada unidade para obter mais
   detalhes."

8. Caso positivo, o sistema atua, para cada unidade selecionada, da seguinte forma:

   8.1. Preenche o campo de data limite da Etapa 2 do subprocesso da unidade, com o valor informado no campo `Data limite`.

   8.2. Altera a situação do subprocesso da unidade para 'Mapa disponibilizado'.

   8.3. Registra uma movimentação para o subprocesso:
    - `Descrição`: 'Mapa disponibilizado para validação'
    - `Data/hora`: Data/hora atual
    - `Unidade origem`: ADMIN
    - `Unidade destino`: :UNIDADE_SUBPROCESSO:

   8.4. Envia uma notificação por e-mail para a unidade do subprocesso:
   ```text
   Assunto: SGC: Mapa de competências disponibilizado

   Prezado(a) responsável pela :SIGLA_UNIDADE_SUBPROCESSO:,

   O mapa de competências de sua unidade foi disponibilizado no contexto do processo :DESCRICAO_PROCESSO:.

   A validação do mapa já pode ser realizada no Sistema de Gestão de Competências (:URL_SISTEMA:). 

   O prazo para conclusão desta etapa do processo é :DATA_LIMITE:.
   ```

   8.5. O sistema cria um alerta:
    - `Descrição`: "Mapa de competências disponibilizado para validação"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: Data/hora atual
    - `Unidade de origem`: ADMIN
    - `Unidade de destino`: :UNIDADE_SUBPROCESSO:.

   8.6. O sistema exclui as sugestões apresentadas do mapa de competência do subprocesso da unidade.

9. O sistema redireciona para o `Painel` e mostra o *toast* "Mapas disponibilizados em bloco".
