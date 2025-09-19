# Análise de cobertura dos testes E2E (spec/cdu) por Caso de Uso (reqs/cdu-xx.md)

Objetivo: verificar se cada teste em [`spec/cdu`](spec/cdu) cobre os fluxos e regras dos respectivos CDUs em [`reqs`](reqs).

Fontes analisadas
- Requisitos: [`reqs/cdu-01.md`](reqs/cdu-01.md) … [`reqs/cdu-21.md`](reqs/cdu-21.md), diagrama [`reqs/estados-mapeamento.md`](reqs/estados-mapeamento.md)
- Testes E2E: [`spec/cdu`](spec/cdu), auxiliares [`spec/cdu/auxiliares-*.ts`](spec/cdu), constantes [`spec/cdu/constantes-teste.ts`](spec/cdu/constantes-teste.ts)

Legenda de status
- Completo: cobre fluxos principais e alternativos relevantes com asserts significativos
- Parcial: cobre fluxo “feliz” e/ou parte das variações; faltam ramos/validações críticas do CDU
- Ausente: não há teste mapeado ao CDU

Resumo executivo
- Total CDUs: 21
- Cobertura por arquivo em spec/cdu:
  - Completo ou quase: CDU-02, CDU-03, CDU-05, CDU-10, CDU-15, CDU-16, CDU-17, CDU-18, CDU-19, CDU-20, CDU-21
  - Parcial: CDU-01, CDU-04, CDU-06, CDU-07, CDU-08, CDU-09, CDU-11, CDU-12, CDU-13, CDU-14
  - Ausente: —
- Gaps recorrentes:
  - Confirmações de efeitos de negócio (movimentações, e-mails, alertas) raramente assertadas, exceto em CDU-21.
  - Branches condicionais (ex.: “sem impactos” vs “com impactos”) em geral testados apenas em um lado.
  - Ordenações/marcações requeridas nos requisitos nem sempre validadas.

Detalhamento por CDU

CDU-01 – Login e estrutura
- Teste: [`spec/cdu/cdu-01.spec.ts`](spec/cdu/cdu-01.spec.ts)
- Coberto:
  - Carregamento da tela de login; erro “Usuário não encontrado”; navbar por perfil; logout.
- Faltante no escopo do CDU:
  - Mensagem exata “Título ou senha inválidos” (requisito). Footer (versão/SESEL-COSIS/TRE-PE) não verificado.
  - Seleção perfil/unidade quando múltiplos pares (fluxo expandido) não é exercitado.
- Status: Parcial

CDU-02 – Painel
- Teste: [`spec/cdu/cdu-02.spec.ts`](spec/cdu/cdu-02.spec.ts)
- Coberto:
  - Seções Processos/Alertas por perfil; “Criar processo” e visibilidade de “Criado” apenas para ADMIN; ordenação por Descrição; filtro por unidade; navegação; ordem inicial de Alertas por data/hora desc.
- Faltante:
  - Ordenação por cabeçalho “Processo” na seção Alertas (regra 3.2 do CDU); marcação de alertas como lidos na “primeira visualização”.
- Status: Parcial

CDU-03 – Manter processo
- Teste: [`spec/cdu/cdu-03.spec.ts`](spec/cdu/cdu-03.spec.ts)
- Coberto:
  - Acesso à tela; validações de campos; criar/editar/remover; iniciar com confirmação; regras da árvore (selecionar/deselecionar subárvore, estado intermediário).
  - Regra 2.3.2.5: seleção de unidade “INTEROPERACIONAL” sem propagar para subordinadas (ex.: STIC marcado e COSIS não marcado).
- Faltante:
  - —
- Status: Completo

CDU-04 – Iniciar mapeamento
- Teste: [`spec/cdu/cdu-04.spec.ts`](spec/cdu/cdu-04.spec.ts)
- Coberto:
  - Fluxo positivo com modal/confirmar; cancelar modal.
- Faltante:
  - Verificar criação de subprocessos, cópia da hierarquia, registro de movimentações e alertas; criação de “mapa vazio” por unidade (passo 10); e-mails efetivos (apenas texto do modal).
- Status: Parcial

CDU-05 – Iniciar revisão
- Teste: [`spec/cdu/cdu-05.spec.ts`](spec/cdu/cdu-05.spec.ts)
- Coberto:
  - Navegar ao “Criado”; modal com textos; valida pré-condições antes do modal; cancelar/confirmar; sucesso com redirecionamento.
- Faltante:
  - Asserts sobre criação de subprocessos; cópia de mapas vigentes; movimentações/alertas/e-mails (apenas feedback visual).
- Status: Completo (fluxo principal bem coberto; efeitos backoffice não assertados)

CDU-06 – Detalhar processo
- Teste: [`spec/cdu/cdu-06.spec.ts`](spec/cdu/cdu-06.spec.ts)
- Coberto:
  - Visualização de detalhes; abertura de unidade.
- Faltante:
  - Botões “Aceitar/Homologar em bloco”; edição de prazos/situação por ADMIN via Subprocesso.
- Status: Parcial

CDU-07 – Detalhar subprocesso
- Teste: [`spec/cdu/cdu-07.spec.ts`](spec/cdu/cdu-07.spec.ts)
- Coberto:
  - Acesso e render básico.
- Faltante:
  - Conteúdo do header (titular/responsável, e-mails/ramais), cards por tipo de processo, situação/localização atual, prazos.
- Status: Parcial

CDU-08 – Manter atividades/conhecimentos
- Teste: [`spec/cdu/cdu-08.spec.ts`](spec/cdu/cdu-08.spec.ts)
- Coberto:
  - Navegação; botão Impacto (revisão); CRUD completo; mudança de situação para “em andamento”; exibir botão Disponibilizar; valida “atividade vazia”; cancelar edição.
- Faltante:
  - Importação: seleção processo/unidade, seleção múltipla, deduplicação e mensagens (13.7.1/13.7.2) – apenas abertura do modal foi testada.
- Status: Parcial

CDU-09 – Disponibilizar cadastro (Mapeamento)
- Teste: [`spec/cdu/cdu-09.spec.ts`](spec/cdu/cdu-09.spec.ts)
- Coberto:
  - Exibir histórico (modal) se houver; disponibilizar após ter conhecimentos; redirecionamento.
- Faltante:
  - Caso negativo “atividade sem conhecimento” (coberto no CDU-10, mas não aqui); registro de movimentação/e-mail/alerta; limpeza de histórico após disponibilização.
- Status: Parcial

CDU-10 – Disponibilizar revisão do cadastro
- Teste: [`spec/cdu/cdu-10.spec.ts`](spec/cdu/cdu-10.spec.ts)
- Coberto:
  - Sucesso; bloqueio para atividades sem conhecimento; bloqueio se situação incorreta; histórico modal aberto/fechado.
- Faltante:
  - Registro de movimentação/e-mail/alerta; limpeza de histórico pós-disponibilização (há sucesso, mas sem assert da limpeza).
- Status: Completo (fluxos principais e negativos cobertos)

CDU-11 – Visualizar cadastro (somente leitura)
- Teste: [`spec/cdu/cdu-11.spec.ts`](spec/cdu/cdu-11.spec.ts)
- Coberto:
  - ADMIN e GESTOR: navegação até `/vis-cadastro` com assert explícito de somente-leitura (ausência de controles de editar/remover/adicionar) e verificação do cabeçalho (sigla/nome).
  - CHEFE e SERVIDOR: navegação até `/cadastro` da própria unidade e verificação de lista de atividades e conhecimentos.
- Faltante:
  - Validações de habilitação/fluxo condicionado por situação (ex.: card visível após disponibilização).
  - Assertes adicionais de estrutura por atividade/linhas de conhecimentos quando aplicável.
- Status: Parcial

CDU-12 – Verificar impactos no mapa
- Teste: [`spec/cdu/cdu-12.spec.ts`](spec/cdu/cdu-12.spec.ts)
- Coberto:
  - Caso “sem impactos”: exibe notificação informativa e não abre o modal.
  - Caso “com impactos”: abre modal e apresenta título “Competências impactadas”; valida ausência da mensagem de “Nenhuma...”.
- Faltante:
  - Assert detalhado das seções/listas: “Atividades inseridas” e “Competências impactadas” (conteúdo, ícones/tipos, origem por alteração/remoção) e fechamento consistente do modal.
- Status: Parcial

CDU-13 – Analisar cadastro (Mapeamento)
- Teste: [`spec/cdu/cdu-13.spec.ts`](spec/cdu/cdu-13.spec.ts)
- Coberto:
  - Histórico; devolução (GESTOR/ADMIN); aceite (GESTOR); homologação (ADMIN) com variação de botões “Homologar” vs “Validar->Homologar”.
- Faltante:
  - E-mails e alertas; movimentações com campos completos; devolução para a própria unidade reabrindo etapa/limpando datas; encadeamento unidade-superior após aceite.
- Status: Parcial

CDU-14 – Analisar revisão do cadastro
- Teste: [`spec/cdu/cdu-14.spec.ts`](spec/cdu/cdu-14.spec.ts)
- Coberto:
  - Botões corretos por perfil; devolução/aceite com mensagens; histórico modal.
- Faltante:
  - Botão “Impactos no mapa” no contexto de revisão (uso e assert do conteúdo); homologação ADMIN com branch “sem impactos” vs “com impactos”.
- Status: Parcial

CDU-15 – Manter mapa de competências
- Teste: [`spec/cdu/cdu-15.spec.ts`](spec/cdu/cdu-15.spec.ts)
- Coberto:
  - Presença de controles; criar/editar/excluir competências; UI e interações.
- Faltante:
  - Badge de conhecimentos e tooltip (lista de conhecimentos por atividade); conferência de que situação muda de “Cadastro homologado” para “Mapa criado”.
- Status: Completo (funcionalidade central coberta; pequenos detalhes de UI não validados)

CDU-16 – Ajustar mapa (Revisão)
- Teste: [`spec/cdu/cdu-16.spec.ts`](spec/cdu/cdu-16.spec.ts)
- Coberto:
  - Botão Impactos; abrir modal; criar/editar/excluir; tentativas de disponibilização e validações de associação; fluxo de disponibilização integrado.
- Faltante:
  - Assert da exigência de associar “todas as atividades” especificamente apontando atividades pendentes (feedback detalhado); nenhum assert de efeitos secundários (histórico/sugestões).
- Status: Completo (bom escopo; mensagens detalhadas não validadas)

CDU-17 – Disponibilizar mapa
- Teste: [`spec/cdu/cdu-17.spec.ts`](spec/cdu/cdu-17.spec.ts)
- Coberto:
  - Modal título/campos; obrigatoriedade de data; preenchimento; processar/cancelar disponibilização.
- Faltante:
  - Validações de regras 8 e 9 (todas comp. com atividades e todas atividades associadas a alguma comp.) como bloqueio; registros de e-mail/alerta/histórico limpo.
- Status: Completo (fluxo principal do modal coberto; regras de consistência não assertadas)

CDU-18 – Visualizar mapa
- Teste: [`spec/cdu/cdu-18.spec.ts`](spec/cdu/cdu-18.spec.ts)
- Coberto:
  - Navegações por perfil; presença de título, unidade, blocos de competência, atividades e (quando existirem) conhecimentos; ausência de botões de ação para SERVIDOR.
- Faltante:
  - Verificações estruturais completas (ex.: todos conhecimentos por atividade); nada crítico.
- Status: Completo

CDU-19 – Validar mapa (CHEFE)
- Teste: [`spec/cdu/cdu-19.spec.ts`](spec/cdu/cdu-19.spec.ts)
- Coberto:
  - Botões Apresentar Sugestões/Validar; histórico condicional; apresentar sugestões (confirmar e cancelar); validar (confirmar e cancelar).
- Faltante:
  - Mudanças de situação (“Mapa com sugestões” / “Mapa validado”), registro de movimentação, e-mails/alertas.
- Status: Completo (fluxos principais; efeitos backoffice não assertados)

CDU-20 – Analisar validação (GESTOR/ADMIN)
- Teste: [`spec/cdu/cdu-20.spec.ts`](spec/cdu/cdu-20.spec.ts)
- Coberto:
  - Botões e ações para GESTOR (devolver/aceitar) e ADMIN (homologar); ver sugestões quando aplicável; histórico modal.
- Faltante:
  - E-mails/alertas; efeitos de devolver para a própria unidade (reset de dataFimEtapa2 e mudança de situação); branchs completos quando “com sugestões”.
- Status: Completo (fluxos principais; efeitos backoffice não assertados)

CDU-21 – Finalizar processo
- Teste: [`spec/cdu/cdu-21.spec.ts`](spec/cdu/cdu-21.spec.ts)
- Coberto:
  - Navegação; impedir finalização se há unidades não-homologadas; modal de confirmação; cancelar e confirmar; mapas marcados como vigentes; mensagens; e-mails simulados; finalizado no Painel; cobertura para mapeamento e revisão.
- Faltante:
  - E-mails consolidados por tipo de unidade (conteúdo específico por perfil/agrupamento) – parcialmente observado via “notificação de e-mail visível”.
- Status: Completo

Recomendações para elevar cobertura a 100% por CDU
1) Expandir `cdu-11.spec.ts` (já criado) para cobrir:
   - Garantir “somente leitura” para ADMIN/GESTOR (ausência de controles de edição/remoção).
   - Asserts do cabeçalho (sigla e nome) e estrutura por atividade/linhas de conhecimentos.
   - Navegação por perfil conforme requisito (ADMIN/GESTOR via processo/unidade subordinada; CHEFE/SERVIDOR na própria unidade) com verificações de habilitação por situação.

2) CDU-02 – Alertas:
   - Testar ordenação por cabeçalho “Processo” (critério composto descrito no CDU) e marcar “não lido → lido” na “primeira visualização”.

3) CDU-03 – Árvore de unidades:
   - Concluído: Regra 2.3.2.5 coberta em [`spec/cdu/cdu-03.spec.ts`](spec/cdu/cdu-03.spec.ts). Manter casos avançados conforme surgirem.

4) CDU-04/05 – Início:
   - Após confirmar, validar:
     - Criação de subprocessos por unidade operacional/interoperacional.
     - Movimentações iniciais (“Processo iniciado”) e alertas por tipo de unidade.
     - No mapeamento, criação do “mapa vazio”.

5) CDU-08 – Importação:
   - Percorrer todo fluxo de modal (listar processos finalizados → unidades → selecionar atividades) e validar deduplicação e mensagens de itens ignorados.

6) CDU-12 – Impactos:
   - Criar caso com alterações/remoções: listar “atividades inseridas” e “competências impactadas” com ícones/tipos; fechar modal.

7) CDU-13/14/17/19/20 – Efeitos de análise e disponibilizações:
   - Verificar side-effects:
     - Situações atualizadas (ex.: “Cadastro em andamento”, “Cadastro disponibilizado”, “Mapa validado”, “Mapa com sugestões”, “Revisão do cadastro homologada”, etc.).
     - Movimentações com origem/destino/descrição corretos.
     - E-mails/alertas (simulados) presentes e com textos esperados.
     - Resets de datas (ex.: devolução para própria unidade limpa dataFimEtapaN).
     - Branchs de impactos (sem impactos → homologa mapa vigente; com impactos → homologa cadastro e vai a “Revisão do cadastro homologada”).

8) CDU-15 – UI detalhada:
   - Badge de nº de conhecimentos e tooltip com a lista.

9) Test data e helpers:
   - Expandir “DADOS_TESTE” em [`spec/cdu/constantes-teste.ts`](spec/cdu/constantes-teste.ts) para compor cenários de impactos e situações específicas de subprocessos para cobrir branches.

Conclusão
- A suíte E2E atual cobre bem os fluxos principais dos CDUs, especialmente Painel, processos, disponibilizações e finalização. 
- Para aderência estrita aos requisitos, recomenda-se estender asserts aos efeitos de negócio (situações, movimentações, e-mails/alertas), completar ramos condicionais (impactos, devoluções à própria unidade) e incluir o teste ausente do CDU-11.