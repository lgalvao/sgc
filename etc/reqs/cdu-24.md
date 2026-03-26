# CDU-24 - Disponibilizar mapas de competências em bloco

**Ator:** ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN.
- Processo de mapeamento com ao menos uma unidade com subprocesso na situação 'Mapa criado', ou processo de revisão com ao menos uma unidade com subprocesso na situação 'Mapa ajustado'.

## Fluxo principal

1. No `Painel`, o usuário acessa um processo  em andamento, do tipo Mapeamento ou Revisão.

2. O sistema mostra a tela `Detalhes do processo`.

3. O sistema verifica se existem unidades com subprocessos que têm mapas criados ou ajustados mas ainda não disponibilizados e caso positivo habilita  o botão `Disponibilizar mapas em bloco`.

4. O sistema abre um modal de confirmação, com os elementos a seguir:

    - Título: "Disponibilização de mapas em bloco";
    - Texto: "Selecione as unidades cujos mapas deverão ser disponibilizados:";
    - Siglas das unidades operacionais ou interoperacionais cujos mapas de competências poderão ser disponibilizados, com um checkbox (selecionado por padrão) por unidade;
    - Campo de data obrigatório, para a `Data limite` permitida para a validação dos mapas de competências; 
    - Botões `Cancelar` e `Disponibilizar`.

5. Caso o usuário escolha o botão `Cancelar`, o sistema interrompe a operação, permanecendo na tela Detalhes do processo.

6. O usuário clica em `Disponibilizar`.

7. O sistema verifica se todas as competências dos mapas de competências dos subprocessos das unidades selecionadas estão associadas a pelo menos uma atividade dos cadastros das unidades, e, em sentido oposto, se todas as atividades foram associadas a pelo menos uma competência do mapa da unidade.

8. Caso negativo, o sistema interrompe a operação e permanece na tela `Detalhes do processo`, informando a mensagem de erro: "Não é possível realizar a disponibilização em bloco dos mapas de competências das unidades [LISTA_UNIDADES_SELECIONADAS]. Realize a disponibilização individual do mapa de cada unidade para obter mais detalhes."

9. Caso positivo, o sistema atua, para cada unidade selecionada, da seguinte forma:
   
   9.1. O sistema registra a informação "Mapa disponibilizado em bloco" na observação de disponibilização do mapa do subprocesso e a informação do campo Data limite para a validação dos mapas de competências na data limite da etapa 2 do subprocesso da unidade.

   9.2. O sistema altera a situação do subprocesso da unidade para 'Mapa disponibilizado'.

   9.3. O sistema registra uma movimentação para o subprocesso com os campos:

   - `Data/hora`: Data/hora atual
   - `Unidade origem`: ADMIN
   - `Unidade destino`: [SIGLA_UNIDADE_SUBPROCESSO]
   - `Descrição`: 'Disponibilização do mapa de competências'

   9.4. O sistema notifica a unidade do subprocesso quanto à disponibilização, com e-mail no modelo abaixo:

   ```text
   Assunto: SGC: Mapa de competências disponibilizado

   Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

   O mapa de competências de sua unidade foi disponibilizado no contexto do processo [DESCRIÇÃO_PROCESSO].

   A validação deste mapa já pode ser realizada no Sistema de Gestão de Competências (URL_SISTEMA). O prazo para conclusão desta etapa do processo é [DATA_LIMITE].
   ```

   9.5. O sistema cria internamente um alerta:

   - `Descrição`: "Mapa de competências da unidade [SIGLA_UNIDADE_SUBPROCESSO] disponibilizado para análise"
   - `Processo`: [DESCRIÇÃO DO PROCESSO]
   - `Data/hora`: Data/hora atual
   - `Unidade de origem`: ADMIN
   - `Unidade de destino`: [SIGLA_UNIDADE_SUBPROCESSO].

   9.6. O sistema exclui as sugestões apresentadas do mapa de competência do subprocesso da unidade.

   9.7. O sistema agrupa as unidades selecionadas com suas unidades superiores em todos os níveis da hierarquia, notificando estas unidades superiores, com e-mail no modelo abaixo:

   ```text
   Assunto: SGC: Mapas de competências disponibilizados

   Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

   Os mapas de competências das unidades [LISTA_UNIDADE_SUBORDINADAS_SELECIONADAS] foram disponibilizados no contexto do processo [DESCRIÇÃO DO PROCESSO].

   A validação destes mapas já pode ser realizada no Sistema de Gestão de Competências (URL_SISTEMA). O prazo para conclusão desta etapa do processo é [DATA_LIMITE].
   ```

10. O sistema redireciona para o `Painel` e mostra a mensagem de confirmação "Mapas disponibilizados em bloco".