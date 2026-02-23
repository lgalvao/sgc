# Controle de Acesso e Regras de Negócio - SGC

Este documento consolida as regras de acesso e atividades permitidas para cada perfil e contexto do subprocesso.

## Resumo Executivo: Visão vs. Execução

O sistema de acesso do SGC baseia-se em um conjunto simples, mas rígido, de regras de fluxo de trabalho ditadas por dois eixos distintos: **Hierarquia** e **Situação/Localização**.

A regra de ouro que norteia toda a arquitetura de acesso é a separação entre *pertencimento* e *localização*:
1. **Unidade Responsável (Quem é o dono):** Define as **Regras de Visualização (Leitura)**. Um usuário pode visualizar qualquer subprocesso que pertença à sua unidade ou às unidades subordinadas a ele, independentemente de onde o subprocesso esteja "estacionado" no fluxo de trabalho.
2. **Localização Atual (Com quem está a bola):** Define as **Regras de Execução (Escrita)**. Como um documento físico tramitando entre mesas, um usuário **só pode executar ações** (editar, aceitar, devolver, homologar) se o subprocesso estiver fisicamente "na sua mesa" (localizado na sua unidade). 
   * *Exemplo Clássico:* Nem mesmo um ADMIN global pode alterar ou aprovar um rascunho de um CHEFE se o CHEFE não tiver apertado o botão "Disponibilizar" para enviar o subprocesso adiante.

Essa dicotomia permite substituir interceptadores de segurança complexos por duas checagens lógicas diretas e universais.

## Perfis de Acesso

*   **ADMIN**: Administrador do sistema (lotado, independentemente do real usuário logado, na unidade raiz - sigla 'ADMIN').
    *   *Responsabilidades*: Criar e iniciar processos, editar processos (desde que não tenham sido iniciados), criar e ajustar mapas de competências, homologar cadastros, criar outros administradores, homologar mapas, homologar diagnósticos, editar configurações do sistema.
    *   *Escopo*: Todo o sistema, para visualização.

*   **GESTOR**: Responsável por unidade intermediária ou interoperacional (ex: Coordenador).
    *   *Responsabilidades*: Visualizar e validar informações das unidades subordinadas, submeter para análise superior, devolver para ajustes (com ou sem observações).
    *   *Escopo*: Unidades sob sua hierarquia (recursivamente)

*   **CHEFE**: Responsável por unidade operacional ou interoperacional.
    *   *Responsabilidades*: Cadastrar atividades e conhecimento, submeter para validação superior.
    *   *Escopo*: Sua unidade de responsabilidade.

*   **SERVIDOR**: Servidor lotado em unidade operacional/interoperacional.
    *   *Responsabilidades*: Participar de processos de diagnóstico (autoavaliação).
    *   *Escopo*: Sua própria avaliação no contexto de sua unidade.

## Conceito: Localização do Subprocesso

Cada subprocesso pertence a uma **Unidade responsável** (quem executa o trabalho) e possui uma **Localização atual** (onde o processo está "parado" aguardando uma ação).

*   **Unidade responsável**: É a unidade dona do subprocesso (ex: SECAO_111). Define quem tem visibilidade sobre o item e pode cadastrar atividades e conhecimentos para ela (quando o subprocesso estiver na situação pertinente).

*   **Localização atual**: É a unidade onde o fluxo de trabalho se encontra no momento (ex: SECAO_111, COORD_11, SECRETARIA_1 ADMIN). Define quem pode executar ações de alteração de estado. A localização muda conforme as ações de envio (Disponibilizar, Aceitar, Devolver etc.). Essencialmente, qualquer ação que gera uma movimentação muda a localização atual de um subprocesso 

## Regras Gerais de Acesso

O sistema aplica regras distintas para visualizar informações e para executar ações que alteram o estado dos processos e subprocessos.

### 1. Visualização (Leitura)
A permissão de visualização é baseada na **Hierarquia da Unidade Responsável**. Se o usuário logado tiver permissão sobre a unidade responsável pelo subprocesso, poderá vê-lo, independentemente de onde o subprocesso esteja localizado.

*   **ADMIN**: Visualiza todos os subprocessos de todas as unidades.
*   **GESTOR**: Visualiza subprocessos da sua própria unidade e de todas as unidades subordinadas.
*   **CHEFE**: Visualiza apenas os subprocessos da sua unidade.

### 2. Execução (Escrita e Movimentação)
A permissão para alterar dados ou mudar a situação do subprocesso (ex: Homologar, Devolver, Validar) é estritamente baseada na **Localização Atual**.

*   **Regra de Ouro**: O usuário só pode executar ações no subprocesso, se o subprocesso estiver **localizado na sua unidade de trabalho atual** -- mesmo para o perfil ADMIN.
*   **ADMIN**: O perfil de Administrador **não isenta** o usuário desta regra. Para que um ADMIN possa homologar um cadastro, o cadastro deve ter sido enviado (disponibilizado/aceito) até chegar à unidade do ADMIN (Raiz). O ADMIN não pode intervir em um processo que ainda está localizado na unidade do Chefe.

## Detalhamento por Caso de Uso (CDU)

As seções a seguir detalham os atores, pré-condições e ações permitidas para cada Caso de Uso.

### CDU-01 - Realizar login e exibir estrutura das telas
**Atores**: Qualquer usuário autenticado (ADMIN, GESTOR, CHEFE, SERVIDOR).
**Ações**:
*   Realizar login via credenciais de rede.
*   Selecionar perfil e unidade de trabalho (se houver múltiplos).
*   Visualizar menu e estrutura de telas correspondente ao perfil.

### CDU-02 - Visualizar Painel
**Atores**: Todos os perfis.
**Ações**:
*   **Todos**: 
    *   Visualizar tabela de processos ativos (limitado à hierarquia do usuário) e alertas.
*   **ADMIN**:
    *   Visualizar processos na situação 'Criado'.
    *   Visualizar/clicar no botão `Criar processo` (leva ao CDU-03).
    *   Visualizar/clicar em processo 'Criado' para editar (leva ao CDU-03).
*   **ADMIN/GESTOR**: Clicar em processos 'Em andamento'/'Finalizado' para ver detalhes do processo (CDU-06).
*   **CHEFE/SERVIDOR**: Clicar em processos 'Em andamento'/'Finalizado' para ver detalhes do subprocesso da própria unidade (CDU-07).

### CDU-03 - Manter processo
**Atores**: ADMIN.
**Ações**:
*   Criar novo processo (Mapeamento, Revisão, Diagnóstico).
*   Editar processo (apenas se situação 'Criado').
*   Remover processo (apenas se situação 'Criado').
*   Selecionar unidades participantes (árvore hierárquica).

### CDU-04 - Iniciar processo de mapeamento
**Atores**: ADMIN.
**Ações**:
*   Iniciar processo na situação 'Criado'.
*   *Efeito*: Muda situação para 'Em andamento', cria subprocessos 'Não iniciado' para unidades, envia notificações.

### CDU-05 - Iniciar processo de revisão
**Atores**: ADMIN.
**Ações**:
*   Iniciar processo de revisão na situação 'Criado'.
*   *Efeito*: Cria cópia do mapa vigente, cria subprocessos, envia notificações.

### CDU-06 - Detalhar processo
**Atores**: ADMIN, GESTOR.
**Ações**:
*   Visualizar dados do processo e lista de unidades participantes (subárvore).
*   **ADMIN**:
    *   Alterar data limite de unidade específica.
    *   Alterar situação do subprocesso de unidade específica (ex: reabrir cadastro).
    *   Finalizar processo.
*   **ADMIN/GESTOR**:
    *   Acessar `Aceitar/Homologar cadastro em bloco` (se houver pendências).
    *   Acessar `Aceitar/Homologar mapa em bloco` (se houver pendências).
    *   Clicar em unidade para ver detalhes do subprocesso (CDU-07).

### CDU-07 - Detalhar subprocesso
**Atores**: Todos os perfis.
**Ações**:
*   Visualizar dados da unidade, movimentações e prazos.
*   **CHEFE**: Acesso ao card `Atividades e conhecimentos` sempre habilitado.
*   **ADMIN**: Acesso ao card `Mapa de competências` habilitado após homologação.

### CDU-08 - Manter cadastro de atividades e conhecimentos
**Atores**: CHEFE.
**Pré-condições**: Situação 'Não iniciado', 'Cadastro em andamento' ou 'Revisão... em andamento'. Unidade do usuário.
**Ações**:
*   Adicionar, editar, remover atividades e conhecimentos.
*   Importar atividades de processos anteriores.
*   *Nota*: Salvamento é automático.

### CDU-09 - Disponibilizar cadastro de atividades e conhecimentos
**Atores**: CHEFE.
**Pré-condições**: Processo Mapeamento. Situação 'Cadastro em andamento'.
**Ações**:
*   Visualizar histórico de análise (se houve devolução).
*   Disponibilizar cadastro (envia para unidade superior/ADMIN).
*   *Efeito*: Situação muda para 'Cadastro disponibilizado'.

### CDU-10 - Disponibilizar revisão do cadastro de atividades e conhecimentos
**Atores**: CHEFE.
**Pré-condições**: Processo Revisão. Situação 'Revisão do cadastro em andamento'.
**Ações**:
*   Visualizar histórico de análise.
*   Disponibilizar revisão (envia para unidade superior/ADMIN).
*   *Efeito*: Situação muda para 'Revisão do cadastro disponibilizada'.

### CDU-11 - Visualizar cadastro de atividades e conhecimentos
**Atores**: Todos os perfis.
**Ações**:
*   Visualizar lista de atividades e conhecimentos (somente leitura).

### CDU-12 - Verificar impactos no mapa de competências
**Atores**: CHEFE, GESTOR, ADMIN.
**Pré-condições**:
*   **CHEFE**: Situação 'Revisão do cadastro em andamento'.
*   **GESTOR**: Situação 'Revisão do cadastro disponibilizada' (na sua unidade).
*   **ADMIN**: Situação 'Revisão do cadastro homologada' ou 'Mapa ajustado'.
**Ações**:
*   Visualizar comparação (diff) entre mapa vigente e revisão atual (inclusões, remoções, alterações).

### CDU-13 - Analisar cadastro de atividades e conhecimentos
**Atores**: GESTOR, ADMIN.
**Pré-condições**: Processo Mapeamento. Situação 'Cadastro disponibilizado'. Localização na unidade do usuário.
**Ações**:
*   Visualizar histórico de análise.
*   **Devolver para ajustes** (envia de volta para unidade inferior).
*   **GESTOR**: Registrar aceite (envia para unidade superior).
*   **ADMIN**: Homologar (finaliza cadastro, muda para 'Cadastro homologado').

### CDU-14 - Analisar revisão de cadastro de atividades e conhecimentos
**Atores**: GESTOR, ADMIN.
**Pré-condições**: Processo Revisão. Situação 'Revisão... disponibilizada'. Localização na unidade do usuário.
**Ações**:
*   Verificar impactos no mapa (CDU-12).
*   Visualizar histórico de análise.
*   **Devolver para ajustes** (envia de volta para unidade inferior).
*   **GESTOR**: Registrar aceite (envia para unidade superior).
*   **ADMIN**: Homologar (finaliza revisão, muda para 'Revisão... homologada').

### CDU-15 - Manter mapa de competências
**Atores**: ADMIN.
**Pré-condições**: Situação 'Cadastro homologado' ou 'Mapa criado'.
**Ações**:
*   Criar, editar, excluir competências.
*   Associar atividades às competências.
*   Disponibilizar (leva ao CDU-17).

### CDU-16 - Ajustar mapa de competências
**Atores**: ADMIN.
**Pré-condições**: Processo Revisão. Situação 'Revisão... homologada' ou 'Mapa ajustado'.
**Ações**:
*   Verificar impactos no mapa.
*   Ajustar competências/atividades (mesmo fluxo de CDU-15).
*   Disponibilizar (leva ao CDU-17).

### CDU-17 - Disponibilizar mapa de competências
**Atores**: ADMIN.
**Pré-condições**: Situação 'Mapa criado' ou 'Mapa ajustado'.
**Ações**:
*   Validar mapa (regras de consistência).
*   Definir data limite para validação.
*   Disponibilizar (envia para unidades subordinadas).
*   *Efeito*: Muda situação para 'Mapa disponibilizado'.

### CDU-18 - Visualizar mapa de competências
**Atores**: Todos os perfis.
**Ações**:
*   Visualizar mapa (competências, atividades, conhecimentos).

### CDU-19 - Validar mapa de competências
**Atores**: CHEFE.
**Pré-condições**: Situação 'Mapa disponibilizado'.
**Ações**:
*   Visualizar mapa.
*   Apresentar sugestões (muda para 'Mapa com sugestões', envia para superior).
*   Validar (muda para 'Mapa validado', envia para superior).
*   Visualizar histórico de análise (se houve devolução).

### CDU-20 - Analisar validação de mapa de competências
**Atores**: GESTOR, ADMIN.
**Pré-condições**: Situação 'Mapa validado' ou 'Mapa com sugestões'. Localização na unidade do usuário.
**Ações**:
*   Visualizar mapa e sugestões (se houver).
*   **Devolver para ajustes** (envia de volta para unidade inferior, muda para 'Mapa disponibilizado').
*   **GESTOR**: Registrar aceite (envia para unidade superior).
*   **ADMIN**: Homologar (muda para 'Mapa homologado').

### CDU-21 - Finalizar processo de mapeamento ou de revisão
**Atores**: ADMIN.
**Pré-condições**: Processo 'Em andamento'. Todas as unidades 'Mapa homologado'.
**Ações**:
*   Finalizar processo.
*   *Efeito*: Mapas tornam-se vigentes. Situação muda para 'Finalizado'.

### CDU-22 - Aceitar cadastros em bloco
**Atores**: GESTOR.
**Pré-condições**: Unidades subordinadas com 'Cadastro disponibilizado' ou 'Revisão... disponibilizada' na unidade do usuário.
**Ações**:
*   Selecionar múltiplas unidades.
*   Registrar aceite em bloco.

### CDU-23 - Homologar cadastros em bloco
**Atores**: ADMIN.
**Pré-condições**: Unidades subordinadas com 'Cadastro disponibilizado' ou 'Revisão... disponibilizada' na unidade do usuário (ADMIN).
**Ações**:
*   Selecionar múltiplas unidades.
*   Homologar em bloco (muda para 'Cadastro homologado' ou 'Revisão... homologada').

### CDU-24 - Disponibilizar mapas de competências em bloco
**Atores**: ADMIN.
**Pré-condições**: Unidades com 'Mapa criado' ou 'Mapa ajustado'.
**Ações**:
*   Selecionar múltiplas unidades.
*   Disponibilizar em bloco (valida mapas, define data limite, muda para 'Mapa disponibilizado').

### CDU-25 - Aceitar validação de mapas de competências em bloco
**Atores**: GESTOR.
**Pré-condições**: Unidades com 'Mapa validado' ou 'Mapa com sugestões' na unidade do usuário.
**Ações**:
*   Selecionar múltiplas unidades.
*   Registrar aceite em bloco.

### CDU-26 - Homologar validação de mapas de competências em bloco
**Atores**: ADMIN.
**Pré-condições**: Unidades com 'Mapa validado' ou 'Mapa com sugestões' na unidade do usuário (ADMIN).
**Ações**:
*   Selecionar múltiplas unidades.
*   Homologar em bloco (muda para 'Mapa homologado').

### CDU-27 - Alterar data limite de subprocesso
**Atores**: ADMIN.
**Pré-condições**: Processo não finalizado.
**Ações**:
*   Alterar data limite de um subprocesso específico.

### CDU-28 - Manter atribuição temporária
**Atores**: ADMIN.
**Ações**:
*   Criar atribuição temporária de perfil CHEFE para um servidor em uma unidade.
*   Definir período e justificativa.

### CDU-29 - Consultar histórico de processos
**Atores**: ADMIN, GESTOR, CHEFE.
**Ações**:
*   Visualizar lista de processos finalizados.
*   Ver detalhes de processos finalizados (somente leitura).

### CDU-30 - Manter Administradores
**Atores**: ADMIN.
**Ações**:
*   Adicionar novo administrador (por título eleitoral).
*   Remover administrador existente.

### CDU-31 - Configurar sistema
**Atores**: ADMIN.
**Ações**:
*   Alterar configurações globais (Dias para inativação de processo, Dias para alerta novo).

### CDU-32 - Reabrir cadastro
**Atores**: ADMIN.
**Ações**:
*   Reabrir cadastro de atividades de uma unidade.
*   *Efeito*: Situação muda para 'Cadastro em andamento'. Envia notificação.

### CDU-33 - Reabrir revisão de cadastro
**Atores**: ADMIN.
**Ações**:
*   Reabrir revisão de cadastro de uma unidade.
*   *Efeito*: Situação muda para 'Revisão do cadastro em andamento'. Envia notificação.

### CDU-34 - Enviar lembrete de prazo
**Atores**: ADMIN.
**Ações**:
*   Selecionar unidades com pendências.
*   Enviar e-mail de lembrete de prazo.

### CDU-35 - Gerar relatório de andamento
**Atores**: ADMIN.
**Ações**:
*   Visualizar e exportar (PDF) relatório de andamento de processo.

### CDU-36 - Gerar relatório de mapas
**Atores**: ADMIN.
**Ações**:
*   Visualizar e exportar (PDF) mapas de competências consolidados.
