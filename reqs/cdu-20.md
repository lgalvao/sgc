# CDU-20 - Analisar validação de mapa de competências

Ator: GESTOR e ADMIN

Pré-condições:

- Usuário logado com perfil GESTOR ou ADMIN.
- Processo de mapeamento ou de revisão iniciado que tenha a unidade como participante.
- Subprocesso nas situações 'Mapa validado' ou 'Mapa com sugestões' e com localização atual na unidade do usuário.

Fluxo principal:

1. No Painel, usuário escolhe um processo e na tela Detalhes do processo clica em uma unidade com situação 'Mapa validado' ou 'Mapa com sugestões'.

2. O sistema mostra a tela `Detalhes do subprocesso`.

3. Usuário clica no card `Mapa de Competências`.

4. O sistema apresenta o mapa de competências da unidade na tela `Visualização de mapa`, com os botões:

   - `Histórico de análise`;
   - `Devolver para ajustes`;
   - `Registrar aceite`, caso o perfil seja GESTOR ou `Homologar`, caso o perfil seja ADMIN.

5. Caso a situação do subprocesso seja 'Mapa com sugestões', a tela Visualização de mapa incluirá ainda, antes do botão `Histórico de análise`, o botão `Ver sugestões`, a partir do qual será possível visualizar, em uma tela modal, as sugestões registradas para o mapa no subprocesso da unidade.

6. Se o usuário clicar no botão `Histórico de análise`, o sistema mostra, em tela modal, os dados das análises prévias registradas para a validação do mapa. As análises deverão ser apresentadas em uma pequena tabela com data/hora, sigla da unidade, resultado ('Devolução' ou 'Aceite') e observações. Essas informações poderão ser usadas como subsídio para a realização da análise pela unidade atual.

7. O usuário analisa as informações e opta por aceitar/homologar ou devolver a validação para ajustes.

8. Se optar por devolver para ajustes:

   8.1. Usuário clica em Devolver para ajustes.

   8.2. O sistema abre tela modal (título Devolução) com a pergunta 'Confirma a devolução da validação do mapa para ajustes?', um campo para preenchimento de uma observação (opcional) e os botões Confirmar ou Cancelar.

   8.3. Caso o usuário escolha o botão Cancelar, o sistema interrompe a operação de devolução, permanecendo na mesma tela.

   8.4. O usuário opcionalmente informa a observação e escolhe Confirmar.

   8.5. O sistema registra uma análise de validação para o subprocesso com:

   - Data/hora: Data/hora atual
   - Unidade: [SIGLA_UNIDADE_ANALISE]
   - Resultado: 'Devolução'
   - Observação: A observação da janela modal, caso tenha sido fornecida.

   8.6. O sistema identifica a unidade de devolução como sendo a unidade de origem da última movimentação do subprocesso.

   8.7. O sistema registra uma movimentação para o subprocesso com:

   - Descrição: 'Devolução da validação do mapa de competências para ajustes'
   - Data/hora: Data/hora atual
   - Unidade origem: [SIGLA_UNIDADE_ANALISE]
   - Unidade destino: [SIGLA_UNIDADE_DEVOLUCAO]

   8.8. Se a unidade de devolução for a própria unidade do subprocesso, o sistema altera a situação do subprocesso para 'Mapa disponibilizado' e apaga a data/hora de conclusão da etapa 2 do subprocesso da unidade.

   8.9. O sistema envia notificação por e-mail para a unidade de devolução:
   Assunto: SGC: Validação do mapa de competências da [SIGLA_UNIDADE_SUBPROCESSO] devolvida para ajustes

   ```text
   Prezado(a) responsável pela [SIGLA_UNIDADE_DEVOLUCAO],

   A validação do mapa de competências da [SIGLA_UNIDADE_SUBPROCESSO] no processo [DESCRICAO_PROCESSO] foi devolvida para ajustes.

   Acompanhe o processo no O sistema de Gestão de Competências: [URL_SISTEMA].
   ```

   8.10. O sistema cria internamente um alerta com:

   - Descrição: "Cadastro de atividades e conhecimentos da unidade [SIGLA_UNIDADE_SUBPROCESSO] devolvido para ajustes"
   - Processo: [DESCRICAO_PROCESSO]
   - Data/hora: Data/hora atual
   - Unidade de origem: [SIGLA_UNIDADE_ANALISE]
   - Unidade de destino: [SIGLA_UNIDADE_DEVOLUCAO].

   8.11. O sistema mostra a mensagem "Devolução realizada" e redireciona para o Painel.

9. Se optar por aceitar (perfil GESTOR):

   9.1. Usuário clica em Registrar aceite.

   9.2. O sistema abre um diálogo modal (título Aceite) com a pergunta 'Confirma o aceite da validação do mapa de competências?', um campo para preenchimento de uma observação opcional e os botões Confirmar ou Cancelar.

   9.3. Caso o usuário escolha o botão Cancelar, o sistema interrompe a operação de aceite, permanecendo na mesma tela.

   9.4. O usuário opcionalmente informa a observação e escolhe Confirmar.

   9.5. O sistema registra uma análise de validação para o subprocesso com:

   - Data/hora: Data/hora atual
   - Unidade: [SIGLA_UNIDADE_ANALISE]
   - Resultado: 'Aceite'
   - Observação: A observação da janela modal, caso tenha sido fornecida.

   9.6. O sistema registra uma movimentação para o subprocesso com:

   - Data/hora: Data/hora atual
   - Unidade origem: [SIGLA_UNIDADE_ANALISE]
   - Unidade destino: [SIGLA_UNIDADE_SUPERIOR]
   - Descrição: 'Mapa de competências validado'

   9.7. O sistema envia notificação por e-mail para a unidade superior:

   ```text
   Assunto: SGC: Validação do mapa de competências da [SIGLA_UNIDADE_SUBPROCESSO] submetida para análise 
   
   Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

   A validação do mapa de competências da [SIGLA_UNIDADE_SUBPROCESSO] no processo [DESCRICAO_PROCESSO] foi submetida para análise por essa unidade. 
   
   A análise já pode ser realizada no O sistema de Gestão de Competências ([URL_SISTEMA]).
   ```

   9.8. O sistema cria internamente um alerta com:

   - Descrição: "Validação do mapa de competências da [SIGLA_UNIDADE_SUBPROCESSO] submetida para análise"
   - Processo: [DESCRICAO_PROCESSO]
   - Data/hora: Data/hora atual
   - Unidade de origem: [SIGLA_UNIDADE_ANALISE]
   - Unidade de destino: [SIGLA_UNIDADE_SUPERIOR].

   9.9. O sistema mostra a mensagem "Aceite registrado" e redireciona para o Painel.

10. Se optar por homologar (perfil ADMIN):

    10.1. Usuário escolhe `Homologar`.

    10.2. O sistema abre um diálogo de confirmação (título Homologação) com a pergunta 'Confirma a homologação do mapa de competências?' e os botões `Confirmar` ou `Cancelar`.

    10.3. Caso o usuário escolha o botão `Cancelar`, o sistema interrompe a operação de homologação, permanecendo na mesma tela.

    10.4. Usuário escolhe `Confirmar`.

    10.5. O sistema altera a situação do subprocesso da unidade para 'Mapa homologado'.

    10.6. O sistema mostra a mensagem "Homologação efetivada" e redireciona para o Painel.
