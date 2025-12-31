# CDU-26 - Homologar validação de mapas de competências em bloco

**Ator:** ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN.
- Processo de mapeamento ou de revisão iniciado que tenha a unidade como participante.
- Subprocesso nas situações 'Mapa validado' ou 'Mapa com sugestões' e com localização atual na unidade do usuário.

## Fluxo principal

1. No Painel, o usuário acessa um processo de mapeamento ou revisão em andamento.

2. O sistema mostra tela Detalhes do processo.

3. O sistema identifica que existem unidades subordinadas com subprocessos elegíveis para homologação em bloco do mapa de competências (de acordo com as pré-condições do caso de uso).

4. Na seção de unidades participantes, abaixo da árvore de unidades, sistema mostra o botão `Homologar mapa de competências em bloco`.

5. O usuário clica no botão `Homologar mapa de competências em bloco`.

6. O sistema abre modal de confirmação, com os elementos a seguir:

   - Título "Homologação de mapa em bloco";
   - Texto "Selecione abaixo as unidades cujos mapas deverão ser homologados:";
   - Lista das unidades operacionais ou interoperacionais subordinadas cujos mapas poderão ser homologados, sendo apresentados, para cada unidade, um checkbox (selecionado por padrão), a sigla e o nome; e
   - Botão `Cancelar` e botão `Homologar`.

7. Caso o usuário escolha o botão `Cancelar`, o sistema interrompe a operação, permanecendo na tela Detalhes do processo.

8. O usuário clica em `Homologar`.

9. O sistema atua, para cada unidade selecionada, da seguinte forma:

   9.1. O sistema registra uma movimentação para o subprocesso:

        - `Data/hora`: [Data/hora atual]
        - `Unidade origem`: "SEDOC"
        - `Unidade destino`: "SEDOC"
        - `Descrição`: "Mapa de competências homologado"

   9.2. O sistema altera a situação do subprocesso da unidade para 'Mapa homologado'.

   9.3. O sistema cria internamente um alerta:

        - `Descrição`: "Mapa de competências da unidade [SIGLA_UNIDADE_SUBPROCESSO] homologado"
        - `Processo`: [DESCRIÇÃO_PROCESSO]
        - `Data/hora`: [Data/hora atual]
        - `Unidade de origem`: SEDOC
        - `Unidade de destino`: [SIGLA_UNIDADE_SUBPROCESSO]

   9.4. O sistema envia notificação por e-mail para a unidade do subprocesso:

    ```text
    Assunto: SGC: Mapa de competências homologado

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUBPROCESSO],

    O mapa de competências da sua unidade foi homologado no processo [DESCRIÇÃO_PROCESSO].

    Acompanhe o processo no Sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

10. O sistema mostra mensagem de confirmação: "Mapas de competências homologados em bloco" e redireciona para o Painel.
