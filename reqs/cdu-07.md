# CDU-07 - Detalhar subprocesso

Atores: CHEFE e SERVIDOR

Pré-condições:

- Usuário ter feito login (qualquer perfil).
- Ao menos um subprocesso nas situações 'Em andamento' ou 'Finalizado'.

Fluxo principal:

1. O sistema mostra a tela Detalhes do subprocesso com os dados do subprocesso da unidade do perfil.
2. A tela Detalhes do subprocesso será composta por três seções: Dados da Unidade, Movimentações do Processo e Elementos
   do Processo.

   2.1. Na seção Dados da unidade serão apresentadas as informações:

   2.1.1. (Sem título): Sigla e nome da unidade, destacado.
   2.1.2. Titular (exibido apenas se não for o responsável):

    - [Nome do titular]
    - Ramal: [Ramal do servidor no SGRH]
    - E-mail: [Endereço de e-mail do servidor no SGRH]

   2.1.3. Responsável:

    - [Nome do responsável]
    - [Tipo da responsabilidade]: com valores possíveis:

   ○ "Titular"
   ○ "Substituição (até [DATA_TERMINO_SUBST)"
   ○ "Atrib. temporária (até [DATA_TERMINO_ATRIB])"

    - Ramal: [Ramal do servidor no SGRH]
    - E-mail: [Endereço de e-mail do servidor no SGRH]

   2.1.4. Situação do Subprocesso: Informação descritiva da situação do subprocesso da unidade (ver seção Situações de
   subprocessos).
   2.1.5. Localização atual: Unidade destino da última movimentação do subprocesso da unidade.
   2.1.6. Prazo para conclusão da etapa atual: Data limite da última etapa do subprocesso ainda não concluída na
   unidade.

   2.2. Na seção Movimentações do processo, título "Movimentações", é apresentada uma tabela com as movimentações que o
   subprocesso já teve até o momento, com os campos: Data/hora da movimentação, unidade origem, unidade destino e
   descrição da movimentação. As informações deverão ser apresentadas em ordem decrescente de data/hora.
   2.3. Na seção Elementos do processo (sem título) serão apresentados cards clicáveis com informação variável em função
   do tipo do processo, os quais darão acesso às telas específicas de cada tema.

   2.3.1. Se o processo for dos tipos Mapeamento ou Revisão, a seção apresentará cards para acesso ao cadastro de
   Atividades e conhecimentos (descrição "Cadastro de atividades e conhecimentos da unidade") e ao Mapa de
   competências (descrição "Mapa de competências da unidade").

    - O card Atividades e conhecimentos estará sempre habilitado para usuários com o perfil CHEFE, inclusive com opções
      para alteração do cadastro (ver caso de uso Manter cadastro de atividades e conhecimentos ). Para os demais
      perfis, a
      habilitação acontecerá apenas após a disponibilização do cadastro pelo CHEFE.
    - O card Mapa de Competências será habilitado inicialmente apenas para o perfil ADMIN, após a homologação do
      cadastro
      ou da revisão cadastral. Posteriormente, com a disponibilização do mapa, o card será liberado também para os
      demais
      perfis de usuários.

   2.3.2. Se o processo for do tipo Diagnóstico, a seção apresentará estes cards:

    - Diagnóstico da equipe (descrição "Diagnóstico das competências pelos servidores da unidade")
    - Ocupações críticas (descrição "Identificação das ocupações críticas da unidade").