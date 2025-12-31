# CDU-23 - Homologar cadastros em bloco

**Ator:** ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN
- Existência de processo de mapeamento ou revisão em andamento com pelo menos uma unidade subordinada cujo subprocesso tenha localização atual na unidade do usuário e a situação:
  - 'Cadastro disponibilizado', para processos de mapeamento; ou
  - 'Revisão do cadastro disponibilizada', para processos de revisão.

## Fluxo principal

1. No Painel, o usuário acessa um processo de mapeamento ou revisão em andamento.

2. O sistema mostra a tela Detalhes do processo.

3. O sistema identifica que existem unidades subordinadas com subprocessos elegíveis para homologação em bloco do cadastro de atividades (de acordo com as pré-condições do caso de uso) e exibe, na seção Unidades Participantes, abaixo da árvore de unidades, o botão `Homologar cadastro em bloco`.

4. O usuário clica no botão `Homologar cadastro em bloco`.

5. O sistema abre modal de confirmação, com os elementos a seguir:

   - Título "Homologação de cadastro em bloco";
   - Texto "Selecione abaixo as unidades cujos cadastros deverão ser homologados:";
   - Lista das unidades operacionais ou interoperacionais subordinadas cujos cadastros poderão ser homologados, sendo apresentados, para cada unidade, um checkbox (selecionado por padrão), a sigla e o nome;
   - Botões `Cancelar` e `Homologar`.

6. Caso o usuário escolha o botão `Cancelar`, o sistema interrompe a operação, permanecendo na tela Detalhes do processo.

7. O usuário clica em `Homologar`.

8. O sistema atua, para cada unidade selecionada, da seguinte forma:

   8.1. O sistema registra uma movimentação para o subprocesso:

        - `Data/hora`: [Data/hora atual]
        - `Unidade origem`: "SEDOC"
        - `Unidade destino`: "SEDOC"
        - `Descrição`: "Cadastro de atividades e conhecimentos homologado"

   8.2. O sistema altera a situação do subprocesso da unidade para 'Cadastro homologado'.

9. O sistema mostra mensagem de confirmação: "Cadastros homologados em bloco" e permanece na tela Detalhes do processo.
