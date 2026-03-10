# Regras de Negócio do SGC

Este documento consolida todas as regras de negócio do Sistema de Gestão de Competências (SGC), extraídas sistematicamente dos requisitos funcionais. As regras estão organizadas em categorias temáticas; cada regra referencia a(s) fonte(s) de onde foi extraída.

---

## Índice

1. [Estrutura Organizacional e Tipos de Unidade](#1-estrutura-organizacional-e-tipos-de-unidade)
2. [Perfis de Usuário e Permissões](#2-perfis-de-usuário-e-permissões)
3. [Autenticação e Login](#3-autenticação-e-login)
4. [Processos — Criação e Ciclo de Vida](#4-processos--criação-e-ciclo-de-vida)
5. [Subprocessos — Situações e Fluxo](#5-subprocessos--situações-e-fluxo)
6. [Cadastro de Atividades e Conhecimentos](#6-cadastro-de-atividades-e-conhecimentos)
7. [Mapa de Competências](#7-mapa-de-competências)
8. [Validação em Bloco](#8-validação-em-bloco)
9. [Notificações e Alertas](#9-notificações-e-alertas)
10. [Painel e Visibilidade de Processos](#10-painel-e-visibilidade-de-processos)
11. [Histórico de Processos](#11-histórico-de-processos)
12. [Relatórios](#12-relatórios)
13. [Administração do Sistema](#13-administração-do-sistema)
14. [Atribuição Temporária de Responsabilidade](#14-atribuição-temporária-de-responsabilidade)
15. [Integração com Sistemas Externos (SGRH e CORAU)](#15-integração-com-sistemas-externos-sgrh-e-corau)
16. [Views do Sistema](#16-views-do-sistema)

---

## 1. Estrutura Organizacional e Tipos de Unidade

**RN-01.01** — A árvore de unidades organizacionais está subordinada a uma unidade virtual raiz de sigla **ADMIN** (código fixo 1), que não existe no SGRH e é criada artificialmente pelo sistema.
> Fonte: `_intro.md`, `_intro-glossario.md` (Unidade raiz), `views/view-03-unidade.md` (RN-VIEW03-01)

**RN-01.02** — As unidades são classificadas em quatro tipos funcionais:
- **OPERACIONAL**: Unidade-folha com 2 ou mais servidores lotados e sem unidades subordinadas.
- **INTERMEDIÁRIA**: Unidade com unidades subordinadas, mas com apenas um servidor lotado (o titular).
- **INTEROPERACIONAL**: Unidade com unidades subordinadas e com mais de um servidor lotado (acumula papel de chefia e gestão).
- **SEM_EQUIPE**: Unidade sem unidades subordinadas e com menos de 2 servidores lotados. Não participa de nenhum processo.
> Fonte: `_intro.md` (linhas 5–8), `_intro-glossario.md`, `views/view-03-unidade.md` (RN-VIEW03-03)

**RN-01.03** — A classificação do tipo de unidade é calculada automaticamente a partir dos dados do SGRH: quantidade de servidores lotados e existência de unidades subordinadas. A regra inclui servidores de unidades filhas únicas (sem subfilhas) na contagem da unidade superior.
> Fonte: `views/view-03-unidade.md` (RN-VIEW03-03, RN-VIEW03-06)

**RN-01.04** — Unidades do tipo **SEM_EQUIPE** não participam de processos de mapeamento, revisão ou diagnóstico e não possuem responsável atribuído pelo sistema.
> Fonte: `views/view-03-unidade.md` (RN-VIEW03-03), `views/view-05-responsabilidade.md` (RN-VIEW05-01)

**RN-01.05** — Unidades do tipo **INTERMEDIÁRIA** não cadastram atividades e conhecimentos; seu papel no sistema é exclusivamente o de validar os cadastros e mapas das unidades subordinadas.
> Fonte: `_intro.md`, `_intro-glossario.md`, `cdu-04.md` (passo 9)

**RN-01.06** — As **Centrais de Atendimento ao Eleitor (CAEs)** têm como unidade superior, na hierarquia do SGC, a Zona Eleitoral sob sua responsabilidade vigente, conforme dados do SIGMA (esquema CORAU). Se não houver zona atribuída, a unidade superior fica nula.
> Fonte: `views/view-02-zona-resp-central.md` (RN-VIEW02-01), `views/view-03-unidade.md` (RN-VIEW03-04)

**RN-01.07** — Ao iniciar um processo, o sistema armazena uma cópia profunda (snapshot) da árvore de unidades participantes vigente no momento do início, preservando a hierarquia mesmo que posteriormente haja mudanças organizacionais no SGRH.
> Fonte: `cdu-04.md` (passo 7), `cdu-05.md` (passo 7), `views/view-03-unidade.md` (CU-VIEW03-04)

**RN-01.08** — Unidades extintas (situação iniciada com 'E' no SGRH) são consideradas **INATIVAS** e não aparecem na árvore de unidades ativa, não podendo participar de processos.
> Fonte: `views/view-03-unidade.md` (RN-VIEW03-07)

---

## 2. Perfis de Usuário e Permissões

**RN-02.01** — O sistema opera com quatro perfis de usuário: **ADMIN**, **GESTOR**, **CHEFE** e **SERVIDOR**. Um mesmo usuário pode acumular múltiplos perfis em diferentes unidades.
> Fonte: `_intro.md` (linhas 21–28), `cdu-01.md` (passos 7–8)

**RN-02.02** — O perfil **ADMIN** é atribuído manualmente a servidores (geralmente da SEDOC) cadastrados na tabela ADMINISTRADOR. Usuários ADMIN atuam no contexto da unidade raiz virtual ADMIN. O ADMIN não é "todo-poderoso": a maioria das ações de escrita só pode ser realizada quando o subprocesso está localizado na unidade ADMIN.
> Fonte: `_intro.md`, `_intro-glossario.md` (Perfil ADMIN), `views/view-06-usuario-perfil-unidade.md` (RN-VIEW06-02)

**RN-02.03** — O perfil **GESTOR** é atribuído automaticamente ao responsável (titular, substituto ou atribuído temporariamente) de uma unidade do tipo **INTERMEDIÁRIA** ou **INTEROPERACIONAL**.
> Fonte: `_intro.md`, `cdu-01.md` (passo 7), `views/view-06-usuario-perfil-unidade.md` (RN-VIEW06-03)

**RN-02.04** — O perfil **CHEFE** é atribuído automaticamente ao responsável de uma unidade do tipo **OPERACIONAL** ou **INTEROPERACIONAL**.
> Fonte: `_intro.md`, `cdu-01.md` (passo 7), `views/view-06-usuario-perfil-unidade.md` (RN-VIEW06-04)

**RN-02.05** — O perfil **SERVIDOR** é atribuído automaticamente a todo servidor lotado em uma unidade operacional ou interoperacional que não seja o responsável pela unidade. Servidores participam apenas de processos de diagnóstico.
> Fonte: `_intro.md`, `cdu-01.md` (passo 7), `views/view-06-usuario-perfil-unidade.md` (RN-VIEW06-05)

**RN-02.06** — O responsável de uma unidade **INTEROPERACIONAL** acumula os perfis **GESTOR** e **CHEFE**, podendo escolher entre eles no momento de login.
> Fonte: `_intro.md`, `views/view-06-usuario-perfil-unidade.md` (RN-VIEW06-04, RN-VIEW06-06)

**RN-02.07** — Os perfis são dinâmicos: ao mudar de responsável (por fim de substituição, atribuição temporária ou mudança de titular no SGRH), os perfis são automaticamente reatribuídos sem intervenção manual (isso porque os dados de perfil são determinados através de views baseadas no SGRH e em dados do SGC, sendo atualizadas automaticamente no Oracle). A perda de lotação ativa implica perda imediata de todos os perfis.
> Fonte: `views/view-06-usuario-perfil-unidade.md` (Mudanças Dinâmicas), `views/view-05-responsabilidade.md`

**RN-02.08** — O perfil **GESTOR** não cadastra nenhuma informação no sistema; ele apenas valida, devolve ou encaminha subprocessos.
> Fonte: `_intro.md` (descrição do perfil GESTOR)

**RN-02.09** — O cadastro de atividades e conhecimentos é responsabilidade exclusiva do perfil **CHEFE** e restrito a sua unidade organizacional. Nem mesmo o ADMIN pode realizar essa operação.
> Fonte: `_intro.md`, `_intro-glossario.md` (Atividade), `cdu-08.md`

**RN-02.10** — A identificação única dos usuários no sistema é o **título de eleitor**, que é permanente e utilizado como chave primária e no processo de login.
> Fonte: `views/view-04-usuario.md` (RN-VIEW04-01)

---

## 3. Autenticação e Login

**RN-03.01** — A autenticação é realizada através do **Sistema Acesso AD** do TRE-PE, usando número do título de eleitor e senha de rede. O SGC não gerencia senhas.
> Fonte: `cdu-01.md` (passo 4), `_intro-glossario.md` (Acesso TRE-PE)

**RN-03.02** — Após autenticação bem-sucedida, o sistema consulta os dados de lotação e de responsabilidades do usuário nas views do banco de dados para determinar os pares perfil/unidade disponíveis.
> Fonte: `cdu-01.md` (passos 6–7)

**RN-03.03** — Se o usuário autenticado possuir apenas um perfil e uma unidade, o sistema seleciona automaticamente esse par perfil/unidade sem mostrar um passo adicional de seleção .
> Fonte: `cdu-01.md` (passo 8.1)

**RN-03.04** — Se o usuário possuir múltiplos perfis ou unidades, o sistema mostra um campo obrigatório para seleção, para que o usuario escolha com qual perfil/unidade deseja atuar na sessão.
> Fonte: `cdu-01.md` (passos 8.2–8.4)

**RN-03.05** — Somente usuários com lotação ativa no SGRH e credenciais válidas podem acessar o sistema. A perda da lotação ativa (no SGRH) implica perda imediata do acesso.
> Fonte: `cdu-01.md` (pré-condições), `views/view-04-usuario.md` (RN-VIEW04-02)

**RN-03.06** — Caso o usuário não seja autenticado pelas credenciais fornecidas, o sistema exibe a mensagem: **"Título ou senha inválidos."**
> Fonte: `cdu-01.md` (passo 5)

---

## 4. Processos — Criação e Ciclo de Vida

**RN-04.01** — O sistema suporta três tipos de processos: **Mapeamento**, **Revisão** e **Diagnóstico**. O tipo não pode ser alterado após a criação do processo.
> Fonte: `_intro.md` (linhas 16–19), `cdu-03.md` (passo 3 — edição)

**RN-04.02** — Um processo pode ser criado apenas pelo perfil **ADMIN**.
> Fonte: `cdu-03.md`

**RN-04.03** — Um processo passa pelas seguintes situações: **'Criado'** → **'Em andamento'** → **'Finalizado'**.
> Fonte: `_intro.md` (Situações de Processos)

**RN-04.04** — Processos na situação **'Criado'** podem ser editados ou removidos. Após iniciados (situação 'Em andamento' ou 'Finalizado'), não podem ser mais editados nem removidos.
> Fonte: `cdu-03.md`, `cdu-04.md` (passo 4)

**RN-04.05** — A remoção de um processo só é possível quando ele está na situação **'Criado'**.
> Fonte: `cdu-03.md` (Remoção de processo)

**RN-04.06** — Para processos do tipo **Revisão** ou **Diagnóstico**, só podem ser selecionadas unidades que já possuam **mapa de competências vigente** (ou seja, que já passaram por pelo menos um processo de mapeamento concluído). Caso contrário, o sistema exibe: *"Não é possível incluir em processos de revisão ou diagnóstico, unidades que ainda não passaram por processo de mapeamento."*
> Fonte: `cdu-03.md` (passo 4.3)

**RN-04.07** — Uma unidade **não pode participar de dois processos ativos do mesmo tipo simultaneamente**. A lista de unidades disponíveis para seleção deve marcar como não selecionáveis as unidades já participando de processo ativo do mesmo tipo.
> Fonte: `cdu-03.md` (passo 2 — campo Unidades participantes), `design/arvore-unidades.md` (Elegibilidade)

**RN-04.08** — Unidades do tipo **INTERMEDIÁRIA** nunca participam diretamente de um processo como unidade com subprocesso; elas participam apenas no papel de validadoras na hierarquia. Na seleção de unidades, o comportamento da árvore filtra automaticamente as INTERMEDIÁRIAS antes do envio ao backend (porém mostra as unidades para facilitar o entendi,m.
> Fonte: `cdu-04.md` (passo 9), `design/arvore-unidades.md` (Filtro Transparente)

**RN-04.09** — Ao se iniciar um processo de qualquer tipo, o sistema notifica por e-mail e cria alertas para todas as unidades participantes (operacionais e interoperacionais recebem notificação de início; intermediárias recebem notificação consolidada sobre suas subordinadas).
> Fonte: `cdu-04.md` (passos 12–13), `cdu-05.md` (passos 12–13)

**RN-04.10** — Ao se iniciar um processo de **Mapeamento**, o sistema cria, para cada unidade operacional/interoperacional participante, um subprocesso na situação **'Não iniciado'** e um mapa de competências vazio vinculado a esse subprocesso.
> Fonte: `cdu-04.md` (passos 9–10)

**RN-04.11** — Ao se iniciar um processo de **Revisão**, o sistema cria, para cada unidade operacional/interoperacional participante, um subprocesso na situação **'Não iniciado'** e copia o mapa de competências vigente da unidade (com todos seus dados de atividades e conhecimentos) para esse subprocesso.
> Fonte: `cdu-05.md` (passos 9–10)

**RN-04.12** — Um processo pode ser **finalizado** pelo ADMIN somente quando **todos** os subprocessos das unidades participantes estiverem na situação **'Mapa homologado'**. Caso contrário, o sistema mostra: *"Não é possível finalizar o processo enquanto houver unidades com mapa de competências ainda não homologado."*
> Fonte: `cdu-21.md` (passos 4–5)

**RN-04.13** — Ao finalizar um processo de mapeamento ou de revisão, os mapas de competências dos subprocessos tornam-se os **mapas vigentes** das respectivas unidades, e todas as unidades participantes são notificadas por e-mail.
> Fonte: `cdu-21.md` (passos 8–9)

**RN-04.14** — Processos são considerados **ativos** enquanto não finalizados, ou enquanto finalizados há menos de `DIAS_INATIVACAO_PROCESSO` dias (configuração do sistema, padrão 10 dias). Após esse prazo, o processo fica **inativo** e disponível apenas em consulta via a tela `Histórico de processos`.
> Fonte: `_intro-glossario.md` (Processos ativos/inativos), `cdu-31.md`

---

## 5. Subprocessos — Situações e Fluxo

**RN-05.01** — Cada processo possui um subprocesso por unidade operacional ou interoperacional participante. O subprocesso, entre outras coisas, registra o progresso da unidade dentro do processo.
> Fonte: `_intro.md`, `_intro-glossario.md` (Subprocesso)

**RN-05.02** — A **localização atual** do subprocesso é determinada pela unidade de destino da última movimentação registrada. Ela é fundamental para determinar quem pode atuar no subprocesso em cada momento.
> Fonte: `_intro-glossario.md` (Localização atual de subprocesso), `cdu-07.md` (passo 2.1.5)

**RN-05.03** — As situações de subprocessos de **Mapeamento** são, em ordem de progressão: 
`'Não iniciado'` → `'Cadastro em andamento'` → `'Cadastro disponibilizado'` → `'Cadastro homologado'` → `'Mapa criado'` → `'Mapa disponibilizado'` → (`'Mapa com sugestões'` ou) `'Mapa validado'` → `'Mapa homologado'`.
> Fonte: `_intro.md` (Situações de subprocessos de Mapeamento)

**RN-05.04** — As situações de subprocessos de **Revisão** são, em ordem de progressão: `'Não iniciado'` → `'Revisão do cadastro em andamento'` → `'Revisão do cadastro disponibilizada'` → `'Revisão do cadastro homologada'` → `'Mapa ajustado'` → `'Mapa disponibilizado'` → (`'Mapa com sugestões'` ou) `'Mapa validado'` → `'Mapa homologado'`.
> Fonte: `_intro.md` (Situações de subprocessos de Revisão)

**RN-05.05** — As situações de subprocessos de **Diagnóstico** são: `'Não iniciado'` → `'Autoavaliação em andamento'` → `'Monitoramento'` → `'Concluído'`.
> Fonte: `_intro.md` (Situações de subprocessos de Diagnóstico)

**RN-05.06** — Toda transição de estado do subprocesso gera um registro de **movimentação** com: data/hora, unidade origem, unidade destino e descrição da ação realizada. As movimentações são apresentadas em ordem decrescente de data/hora.
> Fonte: `cdu-07.md` (seção 2.2), `cdu-04.md` (passo 11), `cdu-09.md` (passo 11)

**RN-05.07** — O ADMIN pode **alterar a data limite** de qualquer subprocesso em andamento. A alteração gera notificação por e-mail e alerta para a unidade do subprocesso.
> Fonte: `cdu-27.md`

**RN-05.08** — O ADMIN pode **reabrir o cadastro** de um subprocesso de Mapeamento, desde que tenha passado da situação 'Mapa homologado', retornando-o para 'Cadastro em andamento'. A reabertura exige **justificativa** obrigatória e notifica a unidade e suas superiores.
> Fonte: `cdu-32.md`

**RN-05.09** — O ADMIN pode **reabrir a revisão de cadastro** de um subprocesso de Revisão, que tenha passado da situacao 'Mapa homologado', retornando-o para 'Revisão do cadastro em andamento'. A reabertura exige **justificativa** obrigatória e notifica a unidade e suas superiores.
> Fonte: `cdu-33.md`

**RN-05.10** — No processo de **devolução** para ajustes (seja de cadastro ou de mapa), o sistema identifica a unidade de devolução como sendo a **unidade de origem da última movimentação** do subprocesso.
> Fonte: `cdu-13.md` (passo 9.6), `cdu-14.md` (passo 10.6), `cdu-20.md` (passo 8.6)

**RN-05.11** — Se a unidade de devolução for a **própria unidade do subprocesso**, a situação retorna para 'Cadastro em andamento' (mapeamento) ou 'Revisão do cadastro em andamento' (revisão) e a data de conclusão da etapa correspondente é apagada.
> Fonte: `cdu-13.md` (passo 9.8), `cdu-14.md` (passo 10.8), `cdu-20.md` (passo 8.8)

---

## 6. Cadastro de Atividades e Conhecimentos

**RN-06.01** — O cadastro de atividades e conhecimentos só pode ser criado ou editado pelo perfil **CHEFE**, e somente quando o subprocesso da unidade está na localização atual da própria unidade e nas situações: 'Não iniciado', 'Cadastro em andamento' (mapeamento) ou 'Revisão do cadastro em andamento' (revisão).
> Fonte: `cdu-08.md` (pré-condições)

**RN-06.02** — Uma **atividade** é uma ação desempenhada pela unidade. Uma atividade deve ter **pelo menos um conhecimento** associado para que o cadastro possa ser disponibilizado.
> Fonte: `_intro-glossario.md` (Atividade, Conhecimento), `cdu-09.md` (passo 7), `cdu-10.md` (passo 7)

**RN-06.03** — O cadastro é salvo **automaticamente** após cada ação de criação, edição ou exclusão de atividade ou conhecimento, sem necessidade de ação adicional do usuário.
> Fonte: `cdu-08.md` (passo 15.1)

**RN-06.04** — Na primeira ação de criação, edição ou importação de atividades/conhecimentos, se o subprocesso ainda estiver 'Não iniciado', o sistema altera automaticamente a situação para **'Cadastro em andamento'** (mapeamento) ou **'Revisão do cadastro em andamento'** (revisão).
> Fonte: `cdu-08.md` (passo 14)

**RN-06.05** — O CHEFE pode **importar atividades** de processos de mapeamento ou revisão finalizados, de qualquer unidade. Devem ser exibidos **todos** os processos finalizados, de todas as unidades, independentemente da hierarquia.
> Fonte: `cdu-08.md` (passo 13.1), memória armazenada (CDU-08)

**RN-06.06** — Na importação, são copiadas apenas as atividades cujas descrições **não coincidam** com nenhuma atividade já cadastrada na unidade. Se houver coincidência, o sistema informa sobre as atividades não importadas, mas prossegue sem erro.
> Fonte: `cdu-08.md` (passos 13.7.1–13.7.2)

**RN-06.07** — Ao **disponibilizar** o cadastro, o sistema bloqueia a edição e habilita a análise pelas unidades superiores. A disponibilização registra uma movimentação com destino para a unidade superior hierárquica.
> Fonte: `cdu-09.md` (passos 10–14), `cdu-10.md` (passos 10–14)

**RN-06.08** — O CHEFE pode visualizar o **histórico de análise** do cadastro (resultado e observações das análises feitas pelas unidades superiores desde a última disponibilização) para subsidiar ajustes antes de nova disponibilização.
> Fonte: `cdu-09.md` (passo 5.1), `cdu-10.md` (passo 5.1)

**RN-06.09** — Nos processos de **revisão**, o CHEFE pode verificar o **impacto das alterações** no mapa de competências vigente antes de disponibilizar o cadastro revisado. O sistema compara as atividades/conhecimentos do mapa vigente com os do subprocesso e identifica competências impactadas.
> Fonte: `cdu-08.md` (passo 5), `cdu-12.md`

**RN-06.10** — O cadastro de atividades e conhecimentos pode ser **visualizado** (somente leitura) por qualquer perfil, desde que o cadastro já tenha sido disponibilizado pelo CHEFE.
> Fonte: `cdu-11.md`

---

## 7. Mapa de Competências

**RN-07.01** — O mapa de competências é criado e mantido exclusivamente pelo perfil **ADMIN**, a partir das atividades e conhecimentos cadastrados pelas unidades.
> Fonte: `_intro.md`, `_intro-glossario.md` (Competência, Mapa de competências), `cdu-15.md`

**RN-07.02** — Uma **competência** deve estar associada a pelo menos uma atividade do cadastro da unidade. Não é possível disponibilizar o mapa se alguma competência não tiver associação com atividade.
> Fonte: `cdu-17.md` (passo 8), `cdu-24.md` (passo 8)

**RN-07.03** — Todas as **atividades** do cadastro da unidade devem estar associadas a pelo menos uma competência. Não é possível disponibilizar o mapa se alguma atividade não tiver sido associada a uma competência.
> Fonte: `cdu-17.md` (passo 9), `cdu-24.md` (passo 8)

**RN-07.04** — Nos processos de **revisão**, ao ajustar o mapa, o ADMIN deve associar a competências **todas as atividades ainda não associadas** que tenham sido inseridas no cadastro revisado.
> Fonte: `cdu-16.md` (passo 9.1)

**RN-07.05** — Ao **disponibilizar** o mapa, o ADMIN deve informar obrigatoriamente uma **data limite para validação** (etapa 2 do subprocesso) e opcionalmente uma observação.
> Fonte: `cdu-17.md` (passo 10)

**RN-07.06** — Ao disponibilizar o mapa, o sistema **exclui automaticamente** as sugestões eventualmente apresentadas anteriormente para aquele mapa no subprocesso.
> Fonte: `cdu-17.md` (passo 19), `cdu-24.md` (passo 10.6)

**RN-07.07** — O perfil **CHEFE** pode: (a) **validar** o mapa sem ressalvas, ou (b) **apresentar sugestões** (em campo de texto formatado obrigatório) antes de submeter para análise das unidades superiores.
> Fonte: `cdu-19.md` (passos 4–5)

**RN-07.08** — Ao apresentar sugestões, a situação do subprocesso muda para **'Mapa com sugestões'**. Se a unidade superior ou o ADMIN devolver, a situação pode retornar para 'Mapa disponibilizado' (para a unidade do subprocesso) ou para 'Mapa com sugestões' (para unidade intermediária).
> Fonte: `cdu-19.md` (passo 4.3), `_intro.md` (diagrama de estados)

**RN-07.09** — O perfil **GESTOR** pode aceitar (encaminhar para o nível superior) ou devolver a validação do mapa para ajustes. O GESTOR não pode homologar.
> Fonte: `cdu-20.md` (passos 9, 8)

**RN-07.10** — A **homologação** do mapa é ação exclusiva do **ADMIN**. Ao homologar, o subprocesso passa para 'Mapa homologado'.
> Fonte: `cdu-20.md` (passo 10)

**RN-07.11** — Nos processos de **revisão**, se a revisão do cadastro não produziu nenhum impacto no mapa de competências, o ADMIN pode confirmar a manutenção do mapa vigente, alterando diretamente a situação do subprocesso para 'Mapa homologado' sem passar pelas etapas de ajuste e disponibilização.
> Fonte: `cdu-14.md` (passo 12.2)

**RN-07.12** — O mapa de competências pode ser **visualizado** (somente leitura) por qualquer perfil, desde que o mapa já tenha sido disponibilizado pelo ADMIN.
> Fonte: `cdu-18.md`

**RN-07.13** — Ao verificar **impactos no mapa** (em processos de revisão), o sistema compara o mapa vigente da unidade com o mapa do subprocesso e identifica: atividades inseridas e competências impactadas por remoção ou alteração de atividades.
> Fonte: `cdu-12.md` (passos 5–7)

---

## 8. Validação em Bloco

**RN-08.01** — O perfil **GESTOR** pode aceitar, em uma única operação, os cadastros de múltiplas unidades subordinadas cujos subprocessos estejam na situação 'Cadastro disponibilizado' (mapeamento) ou 'Revisão do cadastro disponibilizada' (revisão) e com localização atual na unidade do GESTOR.
> Fonte: `cdu-22.md`

**RN-08.02** — O perfil **ADMIN** pode homologar, em uma única operação, os cadastros de múltiplas unidades cujos subprocessos estejam nas situações 'Cadastro disponibilizado' (mapeamento) ou 'Revisão do cadastro disponibilizada' (revisão) e com localização atual na unidade ADMIN.
> Fonte: `cdu-23.md`

**RN-08.03** — O perfil **ADMIN** pode disponibilizar mapas de competências em bloco para unidades cujos subprocessos estejam nas situações 'Mapa criado' (mapeamento) ou 'Mapa ajustado' (revisão). A disponibilização em bloco exige uma **data limite** obrigatória comum a todas as unidades selecionadas. Se alguma unidade não passar na validação (competências sem atividade ou atividades sem competência), a operação é interrompida para todas, com orientação para fazer disponibilização individual.
> Fonte: `cdu-24.md`

**RN-08.04** — O perfil **GESTOR** pode aceitar, em bloco, a validação de mapas de unidades cujos subprocessos estejam nas situações 'Mapa validado' ou 'Mapa com sugestões' e com localização atual na unidade do GESTOR.
> Fonte: `cdu-25.md`

**RN-08.05** — O perfil **ADMIN** pode homologar, em bloco, os mapas de unidades cujos subprocessos estejam nas situações 'Mapa validado' ou 'Mapa com sugestões' e com localização atual na unidade ADMIN.
> Fonte: `cdu-26.md`

**RN-08.06** — Nas operações em bloco, o sistema apresenta por padrão **todos os itens selecionados**, permitindo ao usuário desmarcar individualmente os que não deseja incluir antes de confirmar a operação.
> Fonte: `cdu-22.md` (passo 5), `cdu-23.md` (passo 5), `cdu-24.md` (passo 5), `cdu-25.md` (passo 5), `cdu-26.md` (passo 6)

---

## 9. Notificações e Alertas

**RN-09.01** — O sistema envia **notificações por e-mail** em todos os eventos significativos do fluxo: início de processo, disponibilização de cadastro, aceite/devolução/homologação de cadastro, disponibilização de mapa, sugestões ao mapa, validação e homologação de mapa, finalização do processo, reabertura de cadastro e lembretes de prazo.
> Fonte: `cdu-04.md`, `cdu-05.md`, `cdu-09.md`, `cdu-10.md`, `cdu-13.md`, `cdu-14.md`, `cdu-17.md`, `cdu-19.md`, `cdu-20.md`, `cdu-21.md`, `cdu-27.md`, `cdu-28.md`, `cdu-32.md`, `cdu-33.md`, `cdu-34.md`

**RN-09.02** — Além do e-mail, o sistema cria internamente **alertas** correspondentes a cada evento, vinculados à unidade de destino (ou ao usuário destinatário no caso de atribuições temporárias).
> Fonte: `cdu-04.md` (passo 13), `cdu-09.md` (passo 13), `cdu-13.md` (passo 9.10), e outros CDUs

**RN-09.03** — Alertas não visualizados pelo usuário são exibidos em **negrito** no Painel. Na primeira visualização, o alerta é marcado como lido para aquele usuário específico.
> Fonte: `cdu-02.md` (passo 3.2)

**RN-09.04** — Os alertas são exibidos no Painel ordenados primariamente pelo processo (asc/desc) e secundariamente pela data/hora (desc).
> Fonte: `cdu-02.md` (passo 3.2)

**RN-09.05** — O **ADMIN** pode enviar manualmente um **lembrete de prazo** para unidades com pendências, gerando e-mail, alerta e registrando uma movimentação interna no subprocesso.
> Fonte: `cdu-34.md`

**RN-09.06** — As notificações de início e conclusão de processo são enviadas de forma diferenciada: unidades operacionais/interoperacionais recebem e-mail específico para a sua unidade; unidades intermediárias e interoperacionais recebem e-mail consolidado com a lista de suas subordinadas participantes.
> Fonte: `cdu-04.md` (passos 12.1–12.2), `cdu-05.md` (passos 12.1–12.2), `cdu-21.md` (passos 9.1–9.2)

**RN-09.07** — Ao disponibilizar o mapa, além da unidade do subprocesso, todas as **unidades superiores hierárquicas** também são notificadas por e-mail sobre a disponibilização.
> Fonte: `cdu-17.md` (passos 16–17), `cdu-24.md` (passo 10.7)

---

## 10. Painel e Visibilidade de Processos

**RN-10.01** — O Painel exibe apenas os processos que incluam a unidade do usuário e/ou suas unidades subordinadas entre as participantes.
> Fonte: `cdu-02.md` (passo 2)

**RN-10.02** — Processos na situação **'Criado'** são exibidos no Painel **apenas** para o perfil ADMIN.
> Fonte: `cdu-02.md` (passo 2.2)

**RN-10.03** — Clicar em um processo na situação 'Em andamento' ou 'Finalizado' leva: (a) ao perfil ADMIN ou GESTOR, para a tela **Detalhes do processo**; (b) ao perfil CHEFE ou SERVIDOR, para a tela **Detalhes do subprocesso** da sua unidade.
> Fonte: `cdu-02.md` (passo 2.2)

**RN-10.04** — A coluna **'Unidades participantes'** no Painel exibe apenas as unidades de nível mais alto da hierarquia que tenham todas as suas subordinadas participando do processo. Por exemplo, se apenas as seções de uma coordenadoria participam, aparece apenas o nome da coordenadoria.
> Fonte: `cdu-02.md` (passo 2.1), `cdu-29.md` (passo 2)

---

## 11. Histórico de Processos

**RN-11.01** — O **Histórico de processos** é acessível pelos perfis ADMIN, GESTOR e CHEFE (não pelo SERVIDOR).
> Fonte: `cdu-29.md` (pré-condições)

**RN-11.02** — O Histórico exibe apenas processos com situação **'Finalizado'**. Processos inativos (finalizados há mais de `DIAS_INATIVACAO_PROCESSO` dias) só aparecem no Histórico, não mais no Painel.
> Fonte: `cdu-29.md`, `_intro-glossario.md` (Processos ativos/inativos)

**RN-11.03** — O Histórico de processos permite apenas **consulta**, sem nenhum botão de ação ou possibilidade de alteração.
> Fonte: `cdu-29.md` (passo 4)

---

## 12. Relatórios

**RN-12.01** — O relatório de **andamento de processo** lista todas as unidades participantes de um processo com: situação atual do subprocesso, data da última movimentação, responsável e titular (se diferente do responsável). Pode ser exportado em PDF.
> Fonte: `cdu-35.md`

**RN-12.02** — O relatório de **mapas** permite exportar em PDF o consolidado de mapas de competências (competências, atividades e conhecimentos) de um processo, com filtro opcional por unidade.
> Fonte: `cdu-36.md`

**RN-12.03** — Apenas o perfil **ADMIN** acessa os relatórios.
> Fonte: `cdu-35.md`, `cdu-36.md` (Ator: ADMIN)

---

## 13. Administração do Sistema

**RN-13.01** — O perfil **ADMIN** pode gerenciar a lista de administradores do sistema: adicionar um novo administrador (informando o título eleitoral) e remover um administrador existente.
> Fonte: `cdu-30.md`

**RN-13.02** — O sistema **não permite** que um ADMIN remova a si mesmo nem que remova o único administrador do sistema.
> Fonte: `cdu-30.md` (passo 12)

**RN-13.03** — O perfil **ADMIN** pode configurar dois parâmetros do sistema: `DIAS_INATIVACAO_PROCESSO` (dias após finalização para considerar processo inativo; padrão: 10) e `DIAS_ALERTA_NOVO` (dias após criação de alerta para deixar de marcá-lo como novo). O efeito das alterações é imediato.
> Fonte: `cdu-31.md`

---

## 14. Atribuição Temporária de Responsabilidade

**RN-14.01** — O perfil **ADMIN** pode criar atribuições temporárias de responsabilidade para servidores de uma unidade, definindo obrigatoriamente: servidor, data de início, data de término e justificativa.
> Fonte: `cdu-28.md` (passos 5–7)

**RN-14.02** — Uma atribuição temporária **sobrepõe** qualquer outra forma de responsabilidade (substituição formal do SGRH ou titularidade), sendo a de maior prioridade na determinação do responsável efetivo de uma unidade.
> Fonte: `_intro-glossario.md` (Atribuição temporária), `views/view-05-responsabilidade.md` (RN-VIEW05-02)

**RN-14.03** — O usuário que recebe uma atribuição temporária passa a ter os direitos do perfil **CHEFE** durante o período da atribuição. A atribuição tem prioridade sobre os dados de titularidade lidos do SGRH.
> Fonte: `cdu-28.md` (passo 11)

**RN-14.04** — Ao criar uma atribuição temporária, o sistema notifica por e-mail o servidor atribuídoe cria um alerta pessoal endereçado a ele.
> Fonte: `cdu-28.md` (passos 9–10)

**RN-14.05** — A hierarquia de precedência para determinação do responsável efetivo de uma unidade é: (1) **Atribuição temporária** (maior prioridade, cadastrada no SGC) > (2) **Substituição formal** (do SGRH) > (3) **Titularidade** (menor prioridade, do SGRH).
> Fonte: `views/view-05-responsabilidade.md` (RN-VIEW05-02, RN-VIEW05-03)

**RN-14.06** — As atribuições temporárias são vigentes quando a data atual estiver no intervalo `[data_inicio, data_termino]` (inclusive). A verificação de vigência usa `SYSDATE BETWEEN TRUNC(data_inicio) AND TRUNC(data_termino + 1)`.
> Fonte: `views/view-05-responsabilidade.md` (RN-VIEW05-04)

---

## 15. Integração com Sistemas Externos (SGRH e CORAU)

**RN-15.01** — O sistema consome dados do **SGRH (SRH2)** em tempo real via views de banco de dados. Não há sincronização periódica; toda consulta reflete o estado atual do SGRH.
> Fonte: `views/views-indice.md` (Sincronização e Atualização)

**RN-15.02** — O identificador único dos usuários no sistema é o **título de eleitor** (`num_tit_ele` no SGRH), não a matrícula funcional.
> Fonte: `views/view-04-usuario.md` (RN-VIEW04-01)

**RN-15.03** — O sistema considera apenas servidores com **lotação ativa** no SGRH (campo `dt_fim_lotacao IS NULL`). Servidores sem lotação ativa perdem acesso imediatamente.
> Fonte: `views/view-04-usuario.md` (RN-VIEW04-02)

**RN-15.04** — Servidores lotados em unidades do tipo **SEM_EQUIPE** têm como **unidade de competência** a unidade superior (operacional ou interoperacional) àquela SEM_EQUIPE, com exceções específicas para unidades diretamente subordinadas à raiz ADMIN (Gabinete da Presidência → ASPRE; demais → SEDOC).
> Fonte: `views/view-04-usuario.md` (RN-VIEW04-03)

**RN-15.05** — A responsabilidade de um servidor pelo SGRH pode ser de dois tipos: **titularidade** (titular formal do cargo comissionado da unidade) ou **substituição** (substituição formal registrada em `QFC_SUBST_COM`).
> Fonte: `views/view-05-responsabilidade.md` (RN-VIEW05-03, RN-VIEW05-04)

**RN-15.06** — Mudanças organizacionais (criação/extinção de unidades, mudança de lotações, nomeação/dispensa de titulares) no SGRH refletem **imediatamente** no SGC para novos processos. Processos em andamento usam o snapshot preservado no momento do início.
> Fonte: `views/view-03-unidade.md` (Manutenção e Sincronização)

---

## 16. Views do Sistema

**RN-16.01 (VW_VINCULACAO_UNIDADE)** — Esta view rastreia o histórico de sucessões de unidades organizacionais (extinções e reestruturações), permitindo identificar qual unidade atual sucedeu unidades históricas. É usada para auditoria de processos históricos e migração de dados.
> Fonte: `views/view-01-vinculacao-unidade.md`

**RN-16.02 (VW_ZONA_RESP_CENTRAL)** — Esta view mapeia as Centrais de Atendimento ao Eleitor (CAEs) para as Zonas Eleitorais sob sua responsabilidade vigente, integrando dados do SGRH e do CORAU. É essencial para determinar a posição hierárquica das CAEs na árvore de unidades.
> Fonte: `views/view-02-zona-resp-central.md`

**RN-16.03 (VW_UNIDADE)** — View principal de unidades do sistema. Consolida dados do SGRH, enriquece com classificação de tipo (RAIZ, SEM_EQUIPE, OPERACIONAL, INTEROPERACIONAL, INTERMEDIÁRIA), titularidade, situação e hierarquia ajustada (incluindo a unidade virtual ADMIN e o ajuste de CAEs).
> Fonte: `views/view-03-unidade.md`

**RN-16.04 (VW_USUARIO)** — View com dados de todos os servidores com lotação ativa no TRE-PE, incluindo nome, e-mail, ramal, unidade de lotação e unidade de competência (calculada). É a base para autenticação, determinação de perfis e envio de notificações.
> Fonte: `views/view-04-usuario.md`

**RN-16.05 (VW_RESPONSABILIDADE)** — View que consolida as responsabilidades vigentes de cada unidade (titularidade, substituição ou atribuição temporária), respeitando a hierarquia de precedência. Unidades sem responsável definido aparecem com campos nulos, indicando situação irregular.
> Fonte: `views/view-05-responsabilidade.md`

**RN-16.06 (VW_USUARIO_PERFIL_UNIDADE)** — View que é o coração do sistema de autorização. Estabelece o mapeamento completo entre usuários, perfis (ADMIN, GESTOR, CHEFE, SERVIDOR) e unidades. É consultada em cada login e em todas as validações de permissão.
> Fonte: `views/view-06-usuario-perfil-unidade.md`

---

## Referência das Fontes

| Arquivo                                   | Conteúdo                                                                                      |
|-------------------------------------------|-----------------------------------------------------------------------------------------------|
| `_intro.md`                               | Introdução geral, tipos de processo, situações de processos e subprocessos, perfis de usuário |
| `_intro-glossario.md`                     | Glossário de termos do sistema                                                                |
| `cdu-01.md`                               | Login e estrutura de telas                                                                    |
| `cdu-02.md`                               | Visualizar Painel                                                                             |
| `cdu-03.md`                               | Manter processo (criação, edição, remoção)                                                    |
| `cdu-04.md`                               | Iniciar processo de mapeamento                                                                |
| `cdu-05.md`                               | Iniciar processo de revisão                                                                   |
| `cdu-06.md`                               | Detalhar processo                                                                             |
| `cdu-07.md`                               | Detalhar subprocesso                                                                          |
| `cdu-08.md`                               | Manter cadastro de atividades e conhecimentos                                                 |
| `cdu-09.md`                               | Disponibilizar cadastro de atividades (mapeamento)                                            |
| `cdu-10.md`                               | Disponibilizar revisão do cadastro de atividades                                              |
| `cdu-11.md`                               | Visualizar cadastro de atividades e conhecimentos                                             |
| `cdu-12.md`                               | Verificar impactos no mapa de competências                                                    |
| `cdu-13.md`                               | Analisar cadastro de atividades (mapeamento)                                                  |
| `cdu-14.md`                               | Analisar revisão de cadastro de atividades                                                    |
| `cdu-15.md`                               | Manter mapa de competências (mapeamento)                                                      |
| `cdu-16.md`                               | Ajustar mapa de competências (revisão)                                                        |
| `cdu-17.md`                               | Disponibilizar mapa de competências                                                           |
| `cdu-18.md`                               | Visualizar mapa de competências                                                               |
| `cdu-19.md`                               | Validar mapa de competências (CHEFE)                                                          |
| `cdu-20.md`                               | Analisar validação de mapa de competências (GESTOR/ADMIN)                                     |
| `cdu-21.md`                               | Finalizar processo de mapeamento ou revisão                                                   |
| `cdu-22.md`                               | Aceitar cadastros em bloco (GESTOR)                                                           |
| `cdu-23.md`                               | Homologar cadastros em bloco (ADMIN)                                                          |
| `cdu-24.md`                               | Disponibilizar mapas de competências em bloco (ADMIN)                                         |
| `cdu-25.md`                               | Aceitar validação de mapas em bloco (GESTOR)                                                  |
| `cdu-26.md`                               | Homologar validação de mapas em bloco (ADMIN)                                                 |
| `cdu-27.md`                               | Alterar data limite de subprocesso                                                            |
| `cdu-28.md`                               | Manter atribuição temporária                                                                  |
| `cdu-29.md`                               | Consultar histórico de processos                                                              |
| `cdu-30.md`                               | Manter administradores                                                                        |
| `cdu-31.md`                               | Configurar sistema                                                                            |
| `cdu-32.md`                               | Reabrir cadastro                                                                              |
| `cdu-33.md`                               | Reabrir revisão de cadastro                                                                   |
| `cdu-34.md`                               | Enviar lembrete de prazo                                                                      |
| `cdu-35.md`                               | Gerar relatório de andamento                                                                  |
| `cdu-36.md`                               | Gerar relatório de mapas                                                                      |
| `views/views-indice.md`                   | Índice das views do sistema                                                                   |
| `views/view-01-vinculacao-unidade.md`     | VW_VINCULACAO_UNIDADE                                                                         |
| `views/view-02-zona-resp-central.md`      | VW_ZONA_RESP_CENTRAL                                                                          |
| `views/view-03-unidade.md`                | VW_UNIDADE                                                                                    |
| `views/view-04-usuario.md`                | VW_USUARIO                                                                                    |
| `views/view-05-responsabilidade.md`       | VW_RESPONSABILIDADE                                                                           |
| `views/view-06-usuario-perfil-unidade.md` | VW_USUARIO_PERFIL_UNIDADE                                                                     |
| `design/arvore-unidades.md`               | Especificação da árvore de seleção de unidades                                                |
| `design/breadcrumbs.md`                   | Especificação da barra de navegação/breadcrumbs                                               |
