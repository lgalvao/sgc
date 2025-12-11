# CDU-02 - Visualizar Painel

Ator: Usuário (todos os perfis)

## Pré-condição

- Usuário ter feito login (qualquer perfil)

## Fluxo principal

1. O sistema exibe a tela `Painel`, com as seções `Processos ativos` e `Alertas`.

2. Na seção `Processos ativos`, o sistema mostra uma tabela de processos ativos (com título 'Processos'). Devem ser mostrados apenas os processos que incluam entre as unidades participantes a unidade do usuário e/ou suas unidades subordinadas.

   2.1. Campos da tabela:

    - `Descrição`: Descrição dada ao processo no momento do seu cadastro
    - `Tipo`: Tipo do processo ('Mapeamento', 'Revisão' ou 'Diagnóstico')
    - `Unidades Participantes`: Lista textual das unidades, contendo apenas as unidades de nível mais alto abaixo da unidade raiz que possuam todas as suas unidades subordinadas participando do processo. Por exemplo, para uma secretaria com duas coordenadorias A e B, se apenas as seções da coordenadoria B participarem do processo, deverá aparecer apenas o nome da coordenadoria B.
    - `Situação`: Situação do processo ('Criado', 'Em andamento' ou 'Finalizado').

   2.2. Regras de exibição e funcionamento:

    - Processos na situação 'Criado' deverão ser listados apenas se o usuário estiver logado com o perfil ADMIN.
    - Cabeçalhos das colunas deverão ser clicáveis, possibilitando ordenação em ordem crescente e decrescente.
    - Itens da tabela serão clicáveis com estas regras:
        - Para perfil ADMIN, clicar em um processo na situação 'Criado' mostra tela `Cadastro de processo` com os dados do processo (ver caso de uso `Manter processo`).
        - Clicar em processos nas situações 'Em andamento' e 'Finalizado' mostrará as telas Detalhes do processo, caso o perfil logado seja ADMIN ou GESTOR (ver caso de uso `Detalhar processo`), ou `Detalhes do subprocesso`, caso o perfil logado seja CHEFE ou SERVIDOR (ver caso de uso `Detalhar subprocesso`).

   2.3. Caso o usuário logado esteja no perfil ADMIN, no topo da seção de Processos ativos deverá ser exibido o botão `Criar processo` a partir do qual será efetuado o cadastro de novos processos (ver caso de uso `Manter processo`).

3. Na seção `Alertas`, O sistema mostra uma tabela com os alertas registrados pelo sistema que tiverem como destino o usuário logado ou, na ausência desta informação específica, a sua unidade de lotação (título 'Alertas').

   3.1. Campos da tabela:

    - `Descrição`: Descrição do alerta
    - `Data/Hora`: Informação da data e da hora de geração do alerta
    - `Processo`: Descrição do processo a que se refere o alerta
    - `Unidade`: Unidade de origem do alerta

   3.2. Regras de exibição e funcionamento da tabela de alertas:

    - Alertas ainda não visualizados pelo usuário logado serão exibidos em negrito.
    - Na primeira visualização de um ou mais alertas pelo usuário logado, estes alertas deverão ser marcado como visualizado **pelo usuário**, de maneira a serem exibidos sem negrito a partir da próxima visualização pelo mesmo usuário.
    - Os alertas devem estar inicialmente em ordem decrescente por data/hora, podendo-se alternar a ordenação clicando no cabeçalho correspondente.
    - A ordenação deve ser feita tendo como primeiro critério a `Processo` (asc/desc) e em seguida `Data/hora` (desc).
    - O cabeçalho da coluna `Processo` poderá ser clicado para alterar a ordenação dos dados da tabela.
