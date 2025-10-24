# CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos

Atores: GESTOR e ADMIN

Pré-condições:
● Usuário logado com perfil GESTOR ou ADMIN
● Processo de revisão iniciado que tenha a unidade como participante
● Subprocesso com revisão do cadastro de atividades e conhecimentos já disponibilizada, e com localização atual na
unidade do usuário.

Fluxo principal:

1. No Painel, o usuário clica no processo de revisão.
2. O sistema exibe a tela Detalhes do processo.
3. Usuário clica na unidade subordinada cujo cadastro de atividades deseja validar.
4. O sistema exibe a tela Detalhes do subprocesso com os dados da unidade selecionada.
5. Usuário clica no card Atividades e conhecimentos.
6. O sistema apresenta as atividades e conhecimentos da unidade na tela Atividades e conhecimentos, com os botões:

   6.1. Impactos no mapa;
   6.2. Histórico de análise;
   6.3. Devolver para ajustes; e
   6.4. Registrar aceite, caso o perfil seja GESTOR ou Homologar, caso o perfil seja ADMIN.

7. O botão Impactos no mapa, poderá ser usado pelo usuário para verificar as competências da unidade que serão
   impactadas pela alteração realizada no cadastro de atividades e conhecimentos (ver caso de uso Verificar impactos no
   mapa de competências).
8. Se o usuário clicar no botão Histórico de análise, o sistema mostra, em tela modal, os dados das análises prévias
   registradas para o cadastro de atividades desde a última disponibilização. As análises deverão ser apresentadas em
   uma pequena tabela com data/hora, sigla da unidade, resultado ('Devolução' ou 'Aceite') e observações. Essas
   informações poderão ser usadas como subsídio para a realização da análise pela unidade atual.
9. Usuário analisa as informações obtidas através dos botões Impactos no mapa e Histórico de análise e opta por
   aceitar/homologar ou devolver o cadastro da unidade para ajustes.
10. Se optar por devolver para ajustes:

    10.1. Usuário clica em Devolver para ajustes.
    10.2. O sistema abre tela modal (título Devolução) com a pergunta 'Confirma a devolução do cadastro para ajustes?',
    um campo para preenchimento de uma observação (opcional) e os botões Confirmar ou Cancelar.
    10.3. Caso o usuário escolha o botão Cancelar, o sistema interrompe a operação de devolução do cadastro,
    permanecendo na mesma tela.
    10.4. O usuário opcionalmente informa a observação e escolhe Confirmar.
    10.5. O sistema registra uma análise de cadastro para o subprocesso com as informações:

    10.5.1. Data/hora: Data/hora atual
    10.5.2. Unidade: [SIGLA_UNIDADE_ANALISE]
    10.5.3. Resultado: 'Devolução'
    10.5.4. Observação: A observação da janela modal, caso tenha sido fornecida.

    10.6. O sistema identifica a unidade de devolução como sendo a unidade de origem da última movimentação do
    subprocesso.
    10.7. O sistema registra uma movimentação para o subprocesso com os campos:

    10.7.1. Data/hora: Data/hora atual
    10.7.2. Unidade origem: [SIGLA_UNIDADE_ANALISE]
    10.7.3. Unidade destino: [SIGLA_UNIDADE_DEVOLUCAO]
    10.7.4. Descrição: 'Devolução do cadastro de atividades e conhecimentos para ajustes'

    10.8. Se a unidade de devolução for a própria unidade do subprocesso, o sistema altera a situação do subprocesso
    para 'Revisão do cadastro em andamento' e apaga a data/hora de conclusão da etapa 1 do subprocesso da unidade.
    10.9. O sistema envia notificação por e-mail para a unidade de devolução:

    ```
    Assunto: SGC: Cadastro de atividades e conhecimentos da [SIGLA_UNIDADE_SUBPROCESSO] devolvido para ajustes
    Prezado(a) responsável pela [SIGLA_UNIDADE_DEVOLUCAO],
    
    O cadastro de atividades e conhecimentos da [SIGLA_UNIDADE_SUBPROCESSO] no processo [DESCRIÇÃO DO PROCESSO] foi
    devolvido para ajustes.

    Acompanhe o processo no sistema de Gestão de Competências: [URL_SISTEMA].
    ```

    10.10. O sistema cria internamente um alerta:

    10.10.1. Descrição: "Cadastro de atividades e conhecimentos da unidade [SIGLA_UNIDADE_SUBPROCESSO] devolvido para
    ajustes"
    10.10.2. Processo: [DESCRIÇÃO DO PROCESSO]
    10.10.3. Data/hora: Data/hora atual
    10.10.4. Unidade de origem: [SIGLA_UNIDADE_ANALISE]
    10.10.5. Unidade de destino: [SIGLA_UNIDADE_DEVOLUCAO].

    10.11. O sistema mostra a mensagem "Devolução realizada" e redireciona para o Painel.

11. Se optar por aceitar (perfil GESTOR):

    11.1. Usuário clica em Registrar aceite.
    11.2. O sistema abre um diálogo modal (título Aceite) com a pergunta 'Confirma o aceite da revisão do cadastro de
    atividades?', um campo para preenchimento de uma observação opcional e os botões Confirmar ou Cancelar.
    11.3. Caso o usuário escolha o botão Cancelar, o sistema interrompe a operação de aceite, permanecendo na mesma
    tela.
    11.4. O usuário opcionalmente informa a observação e escolhe Confirmar.
    11.5. O sistema registra uma análise de cadastro para o subprocesso com as informações:

    11.5.1. Data/hora: Data/hora atual
    11.5.2. Unidade: [SIGLA_UNIDADE_ANALISE]
    11.5.3. Resultado: 'Aceite'
    11.5.4. Observação: A observação da janela modal, caso tenha sido fornecida.

    11.6. O sistema registra uma movimentação para o subprocesso com os campos:

    11.6.1. Data/hora: Data/hora atual
    11.6.2. Unidade origem: [SIGLA_UNIDADE_ANALISE]
    11.6.3. Unidade destino: [SIGLA_UNIDADE_SUPERIOR]
    11.6.4. Descrição: 'Revisão do cadastro de atividades e conhecimentos aceita'

    11.7. O sistema envia notificação por e-mail para a unidade superior:
    ```
    Assunto: SGC: Revisão do cadastro de atividades e conhecimentos da [SIGLA_UNIDADE_SUBPROCESSO] submetido para
    análise
    ```
    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],
    A revisão do cadastro de atividades e conhecimentos da [SIGLA_UNIDADE_SUBPROCESSO] no processo [DESCRIÇÃO_PROCESSO]
    foi submetida para análise por essa unidade.
    A análise já pode ser realizada no O sistema de Gestão de Competências ([URL_SISTEMA]).
    11.8. O sistema cria internamente um alerta:

    11.8.1. Descrição: "Revisão do cadastro de atividades e conhecimentos da unidade [SIGLA_UNIDADE_SUBPROCESSO]
    submetida para análise"
    11.8.2. Processo: [DESCRIÇÃO DO PROCESSO]
    11.8.3. Data/hora: Data/hora atual
    11.8.4. Unidade de origem: [SIGLA_UNIDADE_ANALISE]
    11.8.5. Unidade de destino: [SIGLA_UNIDADE_SUPERIOR].

    11.9. O sistema mostra a mensagem "Aceite registrado" e redireciona para o Painel.

12. Se optar por homologar (perfil ADMIN):

    12.1. Usuário escolhe Homologar.
    12.2. Se o sistema não detectar nenhum impacto no mapa de competências da unidade:

    12.2.1. O sistema abre um diálogo de confirmação (título Homologação do mapa de competências) com a pergunta "A
    revisão do cadastro não produziu nenhum impacto no mapa de competência da unidade". Confirma a manutenção do mapa de
    competências vigente?" e os botões Confirmar ou Cancelar.
    12.2.2. Caso o usuário escolha o botão Cancelar, o sistema interrompe a operação de homologação, permanecendo na
    mesma tela.
    12.2.3. Usuário escolhe Confirmar.
    12.2.4. O sistema altera a situação do subprocesso da unidade para 'Mapa homologado'.

    12.3. Caso contrário (impactos detectados):

    12.3.1. O sistema abre um diálogo de confirmação (título Homologação do cadastro de atividades e conhecimentos) com
    a pergunta "Confirma a homologação do cadastro de atividades e conhecimentos?" e os botões Confirmar ou Cancelar.
    12.3.2. Caso o usuário escolha o botão Cancelar, o sistema interrompe a operação de homologação do cadastro,
    permanecendo na mesma tela.
    12.3.3. Usuário escolhe Confirmar.
    12.3.4. O sistema registra uma movimentação para o subprocesso com os campos:
    - Data/hora: Data/hora atual
    - Unidade origem: 'SEDOC'
    - Unidade destino: 'SEDOC'
    - Descrição: 'Cadastro de atividades e conhecimentos homologado'
    
    12.3.5. O sistema altera a situação do subprocesso da unidade para 'Revisão do cadastro homologada'.

    12.4. O sistema mostra a mensagem "Homologação efetivada" e redireciona para a tela Detalhes do subprocesso.