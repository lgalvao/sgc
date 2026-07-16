# CDU-07 - Detalhar subprocesso de mapeamento ou revisão

## Atores

- ADMIN
- GESTOR
- CHEFE
- SERVIDOR

## Pré-condições

- Usuário ter feito login (qualquer perfil).
- Ao menos um subprocesso na situação 'Em andamento'.

## Fluxo principal

1. No `Painel`, o usuário aciona um processo de mapeamento/revisão em andamento.

2. O sistema mostra a tela `Detalhes do processo`.

3. O usuário aciona uma unidade.

4. O sistema mostra a tela `Detalhes do subprocesso` para a unidade. A tela será composta por quatro seções --
   referenciadas aqui como `Dados do subprocesso`, `Dados da unidade`, `Movimentações do processo` e
   `Elementos do subprocesso`.

   4.1. Na seção `Dados do subprocesso` (sem título) serão apresentadas as informações:
    - `Situação`: Descrição da situação do subprocesso da unidade (ver [_intro_situacoes.md])
    - `Localização atual`: Unidade de destino da última movimentação do subprocesso da unidade.
    - `Prazo para conclusão (etapa atual)`: Data limite da última etapa do subprocesso ainda não concluída na unidade.

   4.2. Na seção `Dados da unidade` (sem título) serão apresentadas as informações:
    - Sigla e nome da unidade, destacados.

    - Titular (exibido apenas se não for o responsável):
        - [Nome do titular]
        - Ramal: [Ramal do servidor, no SGRH]
        - E-mail: [Endereço de e-mail do servidor, no SGRH]

    - Responsável:
        - [Nome do responsável]
        - [Tipo da responsabilidade]: com estes valores possíveis:
            - "Titular"; ou
            - "Substituição (até :DATA_TERMINO_SUBST:)"; ou
            - "Atrib. temporária (até :DATA_TERMINO_ATRIB:)".
        - Ramal: [Ramal do responsável, no SGRH]
        - E-mail: [Endereço de e-mail do responsável, no SGRH]

   4.3. Na seção `Movimentações do processo` (título "Movimentações"), é apresentada uma tabela com as movimentações
   pelas quais o subprocesso passou, com os campos:
    - `Data/hora`
    - `Origem`
    - `Destino`
    - `Descrição`
      Essas informações deverão ser apresentadas em ordem decrescente de data/hora.

   4.4. Na seção `Elementos do subprocesso` (sem título), serão apresentados cards acionáveis com informação variável em
   função do tipo do subprocesso, os quais darão acesso às telas específicas. As regras de exibição e habilitação dos
   cards são definidas em função do tipo do processo e do perfil ativo, além da situação e localização do subprocesso:

    - O card `Atividades e conhecimentos` (descrição "Cadastro de atividades e conhecimentos da unidade") dá acesso ao
      cadastro; estará sempre habilitado para o perfil CHEFE. Para os demais perfis, a habilitação acontecerá apenas
      após a disponibilização do cadastro pelo CHEFE (ou seja, situação 'Cadastro disponibilizado' em diante).

    - O card `Mapa de competências`  (descrição "Mapa de competências da unidade") dá acesso ao mapa de competências;
      será habilitado inicialmente apenas para o perfil ADMIN após a homologação do cadastro (ou da revisão do
      cadastro). Com a disponibilização do mapa, este card será habilitado para os demais perfis, sempre em modo somente
      leitura.
