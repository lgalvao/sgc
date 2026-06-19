# CDU-23 - Homologar cadastros em bloco

**Ator:** ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN
- Existência de processo de mapeamento ou revisão em andamento com pelo menos uma unidade subordinada cujo subprocesso
  tenha localização atual na unidade do usuário e a situação:
    - 'Cadastro disponibilizado', para processos de mapeamento; ou
    - 'Revisão do cadastro disponibilizada', para processos de revisão.

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de mapeamento ou revisão em andamento.

2. O sistema mostra a tela `Detalhes do processo`.

3. O sistema identifica se existem unidades subordinadas com subprocessos elegíveis para homologação em bloco do
   cadastro de atividades (de acordo com as pré-condições) e exibe, na seção `Unidades participantes`, abaixo da árvore
   de unidades, o botão `Homologar em bloco`.

4. O usuário clica no botão `Homologar em bloco`.

5. O sistema abre um modal de confirmação, com os elementos a seguir:
    - Título: "Homologação de cadastro em bloco";
    - Texto: "Selecione as unidades para homologar o cadastro:";
    - Lista das unidades operacionais ou interoperacionais subordinadas cujos cadastros estão aptos a homologar, com um
      checkbox (selecionado por padrão) para cada unidade, além de sigla e nome da unidade;
    - Botões `Cancelar` e `Homologar`.

6. O usuário clica em `Homologar`.

7. O sistema atua, para cada unidade selecionada, da seguinte forma:

   7.1. O sistema registra uma movimentação para o subprocesso da unidade:
    - `Data/hora`: [Data/hora atual]
    - `Unidade origem`: "ADMIN"
    - `Unidade destino`: "ADMIN"
    - `Descrição`: "Cadastro homologado"

   7.2. O sistema altera a situação do subprocesso da unidade para 'Cadastro homologado'.

8. O sistema mostra mensagem de confirmação: "Cadastros homologados em bloco" e permanece na tela
   `Detalhes do processo`.
