# CDU-13 - Analisar cadastro de atividades e conhecimentos

Atores: GESTOR e ADMIN

Pré-condições:
● Usuário logado com perfil GESTOR ou ADMIN
● Processo de mapeamento iniciado que tenha a unidade como participante
● Subprocesso com cadastro de atividades e conhecimentos já disponibilizado, e com localização atual na unidade do
usuário.

Fluxo principal:

1. No Painel, o usuário clica no processo de mapeamento.
2. O sistema exibe a tela Detalhes do processo.
3. Usuário clica na unidade subordinada cujo cadastro de atividades deseja validar.
4. O sistema exibe a tela Detalhes do subprocesso com os dados da unidade selecionada.
5. Usuário clica no card Atividades e conhecimentos.
6. O sistema apresenta as atividades e conhecimentos da unidade na tela Atividades e conhecimentos, com os botões:
   6.1. Histórico de análise;
   6.2. Devolver para ajustes; e
   6.3. Registrar aceite, caso o perfil seja GESTOR ou Homologar, caso o perfil seja ADMIN.
7. Se o usuário clicar no botão Histórico de análise, o sistema mostra, em tela modal, os dados das análises prévias
   registradas para o cadastro de atividades desde a última disponibilização. As análises deverão ser apresentadas em
   uma pequena tabela com data/hora, sigla da unidade, resultado ('Devolução' ou 'Aceite') e observações. Essas
   informações poderão ser usadas como subsídio para a realização da análise pela unidade atual.
8. O usuário analisa as informações e opta por aceitar/homologar ou devolver o cadastro para ajustes.
9. Se optar por devolver para ajustes:
   9.1. Usuário clica em Devolver para ajustes.
   9.2. O sistema abre tela modal (título Devolução) com a pergunta 'Confirma a devolução do cadastro para ajustes?', um
   campo para preenchimento de uma observação (opcional) e os botões Confirmar ou Cancelar.
   9.3. Caso o usuário escolha o botão Cancelar, o sistema interrompe a operação de devolução do cadastro, permanecendo
   na mesma tela.
   9.4. O usuário opcionalmente informa a observação e escolhe Confirmar.
   9.5. O sistema registra uma análise de cadastro para o subprocesso com as informações:
   9.5.1. Data/hora: Data/hora atual
   9.5.2. Unidade: [SIGLA_UNIDADE_ANALISE]
   9.5.3. Resultado: 'Devolução'
   9.5.4. Observação: A observação da janela modal, caso tenha sido fornecida.
   9.6. O sistema identifica a unidade de devolução como sendo a unidade de origem da última movimentação do
   subprocesso.
   9.7. O sistema registra uma movimentação para o subprocesso com os campos:
   9.7.1. Data/hora: Data/hora atual
   9.7.2. Unidade origem: [SIGLA_UNIDADE_ANALISE]
   9.7.3. Unidade destino: [SIGLA_UNIDADE_DEVOLUCAO]
   9.7.4. Descrição: 'Devolução do cadastro de atividades e conhecimentos para ajustes'
   9.8. Se a unidade de devolução for a própria unidade do subprocesso, o sistema altera a situação do subprocesso
   para 'Cadastro em andamento' e apaga a data/hora de conclusão da etapa 1 do subprocesso da unidade.
   9.9. O sistema envia notificação por e-mail para a unidade de devolução:
   Assunto: SGC: Cadastro de atividades e conhecimentos da [SIGLA_UNIDADE_SUBPROCESSO] devolvido para ajustes
   Prezado(a) responsável pela [SIGLA_UNIDADE_DEVOLUCAO],
   O cadastro de atividades e conhecimentos da [SIGLA_UNIDADE_SUBPROCESSO] no processo [DESCRIÇÃO_PROCESSO] foi
   devolvido para ajustes.
   Acompanhe o processo no O sistema de Gestão de Competências: [URL_SISTEMA].
   9.10. O sistema cria internamente um alerta:
   9.10.1. Descrição: "Cadastro de atividades e conhecimentos da unidade [SIGLA_UNIDADE_SUBPROCESSO] devolvido para
   ajustes"
   9.10.2. Processo: [DESCRIÇÃO DO PROCESSO]
   9.10.3. Data/hora: Data/hora atual
   9.10.4. Unidade de origem: [SIGLA_UNIDADE_ANALISE]
   9.10.5. Unidade de destino: [SIGLA_UNIDADE_DEVOLUCAO].
   9.11. O sistema mostra a mensagem "Devolução realizada" e redireciona para o Painel.
10. Se optar por aceitar (perfil GESTOR):
    10.1. Usuário clica em Registrar aceite.
    10.2. O sistema abre um diálogo modal (título Aceite) com a pergunta 'Confirma o aceite do cadastro de atividades?',
    um campo para preenchimento de uma observação opcional e os botões Confirmar ou Cancelar.
    10.3. Caso o usuário escolha o botão Cancelar, o sistema interrompe a operação de aceite, permanecendo na mesma
    tela.
    10.4. O usuário opcionalmente informa a observação e escolhe Confirmar.
    10.5. O sistema registra uma análise de cadastro para o subprocesso:
    10.5.1. Data/hora: Data/hora atual
    10.5.2. Unidade: [SIGLA_UNIDADE_ANALISE]
    10.5.3. Resultado: 'Aceite'
    10.5.4. Observação: A observação da janela modal, caso tenha sido fornecida.
    10.6. O sistema registra uma movimentação para o subprocesso:
    10.6.1. Data/hora: Data/hora atual
    10.6.2. Unidade origem: [SIGLA_UNIDADE_ANALISE]
    10.6.3. Unidade destino: [SIGLA_UNIDADE_SUPERIOR]
    10.6.4. Descrição: 'Cadastro de atividades e conhecimentos aceito'
    10.7. O sistema envia notificação por e-mail para a unidade superior:
    Assunto: SGC: Cadastro de atividades e conhecimentos da [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise
    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],
    O cadastro de atividades e conhecimentos da [SIGLA_UNIDADE_SUBPROCESSO] no processo [DESCRIÇÃO_PROCESSO] foi
    submetido para análise por essa unidade.
    A análise já pode ser realizada no O sistema de Gestão de Competências ([URL_SISTEMA]).
    10.8. O sistema cria internamente um alerta:
    10.8.1. Descrição: "Cadastro de atividades e conhecimentos da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetido para
    análise"
    10.8.2. Processo: [DESCRICA_PROCESSO]
    10.8.3. Data/hora: Data/hora atual
    10.8.4. Unidade de origem: [SIGLA_UNIDADE_ANALISE]
    10.8.5. Unidade de destino: [SIGLA_UNIDADE_SUPERIOR].
    10.9. O sistema mostra a mensagem "Aceite registrado" e redireciona para o Painel.
11. Se optar por homologar (perfil ADMIN):
    11.1. Usuário escolhe Homologar.
    11.2. O sistema abre um diálogo de confirmação (título Homologação do cadastro de atividades e conhecimentos) com a
    pergunta "Confirma a homologação do cadastro de atividades e conhecimentos?" e os botões Confirmar ou Cancelar.
    11.3. Caso o usuário escolha o botão Cancelar, o sistema interrompe a operação de homologação do cadastro,
    permanecendo na mesma tela.
    11.4. Usuário escolhe Confirmar.
    11.5. O sistema registra uma movimentação para o subprocesso:
    11.5.1. Data/hora: Data/hora atual
    11.5.2. Unidade origem: 'SEDOC'
    11.5.3. Unidade destino: 'SEDOC'
    11.5.4. Descrição: 'Cadastro de atividades e conhecimentos homologado'
    11.6. O sistema altera a situação do subprocesso da unidade para 'Cadastro homologado'.
    11.7. O sistema mostra a mensagem "Homologação efetivada" e redireciona para a tela Detalhes do subprocesso.