# CDU-23 - Homologar cadastros em bloco

## Atores

- ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de mapeamento ou revisão em andamento.

2. O sistema mostra a tela `Detalhes do processo`.

3. O sistema identifica as **unidades aptas** para homologação em bloco: unidades subordinadas com subprocessos
   localizados na unidade do usuário e situação 'Cadastro disponibilizado' (mapeamento), ou 'Revisão do cadastro
   disponibilizada' (revisão).

4. O usuário aciona `Homologar cadastros em bloco`.

5. O sistema abre um modal de confirmação, com os elementos a seguir:
    - Título: `Homologação de cadastros em bloco`;
    - Texto: "Selecione as unidades para homologação do cadastro:";
    - Grade com as unidades aptas, com checkbox (marcado inicialmente) para cada unidade, além de sigla e nome da
      unidade;
    - Botões `Cancelar` e `Homologar em bloco`.

6. O usuário determina as unidades a serem homologadas (marcando ou desmarcando as checkboxes) e aciona
   `Homologar em bloco`.

7. O sistema atua, para cada unidade marcada, da seguinte forma:

   7.1. Registra uma movimentação para o subprocesso da unidade:
    - `Data/hora`: :DATA_HORA:
    - `Unidade origem`: ADMIN
    - `Unidade destino`: ADMIN
    - `Descrição`: "Cadastro homologado"

   7.2. Altera a situação do subprocesso da unidade para 'Cadastro homologado'.

   ** IMPORTANTE: Como a homologação não gera demandas de ações de outras unidades, não são gerados nem alertas nem
   notificações neste caso de uso.

8. O sistema mostra *toast* "Cadastros homologados em bloco" e permanece na tela.
