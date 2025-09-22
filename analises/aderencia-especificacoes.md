# Relatório de aderência de implementação aos Casos de Uso (CDU-01 a CDU-21)

Objetivo: avaliar a aderência da implementação do SGC aos requisitos definidos em [reqs](reqs), destacando o que está implementado e o que ainda falta.

Fontes analisadas
- Requisitos: [reqs/cdu-01.md](reqs/cdu-01.md) … [reqs/cdu-21.md](reqs/cdu-21.md), diagrama [reqs/estados-mapeamento.md](reqs/estados-mapeamento.md)
- Implementação: stores Pinia (ex.: [src/stores/processos.ts](src/stores/processos.ts), [src/stores/alertas.ts](src/stores/alertas.ts), [src/stores/mapas.ts](src/stores/mapas.ts)), views (ex.: [src/views/CadProcesso.vue](src/views/CadProcesso.vue), [src/views/Painel.vue](src/views/Painel.vue), [src/views/CadAtividades.vue](src/views/CadAtividades.vue), [src/views/CadMapa.vue](src/views/CadMapa.vue), [src/views/VisAtividades.vue](src/views/VisAtividades.vue)), componentes e utilitários.

Legenda de status
- Aderente: fluxos principais e alternativos relevantes implementados com efeitos de negócio essenciais (situações, movimentações, alertas, e-mails simulados).
- Parcial: fluxos “felizes” e/ou parte das variações implementadas; faltam ramos/validações críticas ou efeitos de negócio exigidos no CDU.
- Pendente: requisito ainda não implementado.

Resumo executivo
- Total CDUs: 21
- Situação atual (implementação):
  - Aderente ou quase: CDU-01, CDU-02, CDU-03, CDU-04, CDU-05, CDU-07, CDU-08, CDU-09, CDU-10, CDU-12, CDU-13, CDU-14, CDU-15, CDU-16, CDU-17, CDU-18, CDU-19, CDU-20, CDU-21
  - Parcial: CDU-06, CDU-11
- Melhorias recentes relevantes:
  - CDU-01 (Login/estrutura): inclusão de rodapé padrão com versão (lida de package.json) e crédito “Desenvolvido por SESEL/COSIS/TRE-PE” em [src/App.vue](src/App.vue); padronização da mensagem de erro para “Título ou senha inválidos” em [src/views/Login.vue](src/views/Login.vue). Seleção de perfil/unidade quando múltiplos pares já contemplada.
  - CDU-02 (Painel): visibilidade de processos “Criado” restrita a ADMIN no composable [src/composables/useProcessosFiltrados.ts](src/composables/useProcessosFiltrados.ts); manutenção da ordenação inicial por Data/Hora desc e ordenação por coluna “Processo” em [src/views/Painel.vue](src/views/Painel.vue).
  - CDU-04/05 (Início de processos): criação de “mapa vazio” no Mapeamento e cópia do mapa vigente na Revisão mantidas em [src/views/CadProcesso.vue](src/views/CadProcesso.vue) e [src/stores/mapas.ts](src/stores/mapas.ts); registro de movimentações/alertas/e-mails mantido.
  - CDU-08 (Importação): deduplicação de atividades e conhecimentos, e relatório de itens ignorados (regras 13.7.1/13.7.2) centralizados no store [src/stores/atividades.ts](src/stores/atividades.ts) e integrados à UI em [src/views/CadAtividades.vue](src/views/CadAtividades.vue).
  - CDU-09/10 (Disponibilização): padronização de mensagens e verificação de situação condicional ao tipo do processo (Mapeamento/Revisão), bloqueio para atividades sem conhecimento, side-effects completos (movimentação/e-mail/alerta) e limpeza de histórico ao disponibilizar em [src/views/CadAtividades.vue](src/views/CadAtividades.vue).
  - CDU-19/20 (Validação/Análise): consolidação dos efeitos de negócio no store [src/stores/processos.ts](src/stores/processos.ts) — validação/homologação/devolução com atualização de situação, reset de datas (ex.: dataFimEtapa2), limpeza de histórico de análises e registro de movimentações (ADMIN ao homologar também registra movimentação). Teste unitário ajustado para refletir a movimentação na homologação ADMIN.

Detalhamento por CDU

CDU-01 – Login e estrutura
- Implementado:
  - Tela de login; navbar por perfil; logout; seleção de perfil/unidade quando existirem múltiplos pares; rodapé com versão e crédito; mensagem padronizada “Título ou senha inválidos”.
- Lacunas:
  - —
- Status: Aderente

CDU-02 – Painel
- Implementado:
  - Seções Processos/Alertas por perfil; “Criar processo” e visibilidade de “Criado” apenas para ADMIN; ordenação por Descrição; filtro por unidade; navegação; ordem inicial de Alertas por data/hora decrescente; ordenação por coluna “Processo”; marcação de alerta como lido ao clicar na linha ([src/views/Painel.vue](src/views/Painel.vue)).
- Lacunas:
  - Eventual ajuste fino do critério composto “Processo” se especificado por múltiplos campos.
- Status: Aderente

CDU-03 – Manter processo
- Implementado:
  - Acesso à tela; validações; criar/editar/remover; iniciar com confirmação; árvore com seleção/deseleção de subárvore e estado intermediário; regra 2.3.2.5 (INTEROPERACIONAL não propaga).
- Lacunas:
  - —
- Status: Aderente

CDU-04 – Iniciar mapeamento
- Implementado:
  - Confirmação; mudança para “Em andamento”; criação de subprocessos por unidade participante; snapshot de unidades participantes no processo (unidadesSnapshot); criação de mapa de competências vazio por unidade; registro de movimentação “Processo iniciado” (SEDOC → unidade); envio de e-mails simulados (operacionais/interoperacionais e intermediárias com consolidação); criação de alertas correspondentes (origem SEDOC).
  - Referências: [src/views/CadProcesso.vue](src/views/CadProcesso.vue), [src/stores/alertas.ts](src/stores/alertas.ts), [src/stores/notificacoes.ts](src/stores/notificacoes.ts), [src/stores/processos.ts](src/stores/processos.ts), [src/stores/mapas.ts](src/stores/mapas.ts).
- Lacunas:
  - Snapshot de hierarquia armazenado como lista de siglas (string[]). Se necessário preservar a estrutura hierárquica completa, evoluir para um snapshot aninhado (árvore) com metadados.
- Status: Aderente (com observação de modelagem do snapshot)

CDU-05 – Iniciar revisão
- Implementado:
  - Confirmação; mudança para “Em andamento”; criação de subprocessos por unidade participante; snapshot de unidades; cópia do mapa vigente por unidade com vínculo ao subprocesso (idMapaCopiado); registro de movimentação “Processo iniciado”; e-mails/alertas análogos ao mapeamento.
  - Referências: [src/views/CadProcesso.vue](src/views/CadProcesso.vue), [src/stores/mapas.ts](src/stores/mapas.ts), [src/stores/processos.ts](src/stores/processos.ts).
- Lacunas:
  - Mesmo apontamento do snapshot hierárquico do CDU-04 (se requerido).
- Status: Aderente (com observação de modelagem do snapshot)

CDU-06 – Detalhar processo
- Implementado:
  - Visualização de detalhes; abertura de unidade.
- Lacunas:
  - Ações de “Aceitar/Homologar em bloco” e edição de prazos/situação por ADMIN via Subprocesso ainda não estão integradas em uma única tela de processo.
- Status: Parcial

CDU-07 – Detalhar subprocesso
- Implementado:
  - Acesso e render básico; header com titular/responsável, e-mails/ramais; cards por tipo de processo; situação/localização atual e prazos de forma consolidada.
- Lacunas:
  - —
- Status: Aderente

CDU-08 – Manter atividades/conhecimentos
- Implementado:
  - Navegação; impacto no contexto de revisão; CRUD completo; mudança automática para “em andamento”; exibição de botão Disponibilizar; validação de “atividade vazia”; cancelar edição. Importação: listar processos finalizados, escolher unidade, selecionar múltiplas atividades; deduplicação de atividades e conhecimentos; relatório de itens ignorados (13.7.1/13.7.2) centralizado no store e exibido na UI.
  - Referências: [src/components/ImportarAtividadesModal.vue](src/components/ImportarAtividadesModal.vue), [src/stores/atividades.ts](src/stores/atividades.ts), [src/views/CadAtividades.vue](src/views/CadAtividades.vue).
- Lacunas:
  - —
- Status: Aderente

CDU-09 – Disponibilizar cadastro (Mapeamento)
- Implementado:
  - Verificações de situação condicional ao tipo (Mapeamento), checagem explícita e bloqueio para atividades sem conhecimento, disponibilização após conhecimentos, side-effects (movimentação/e-mail/alerta) e limpeza do histórico de análise ao disponibilizar em [src/views/CadAtividades.vue](src/views/CadAtividades.vue), com mensagens padronizadas.
- Lacunas:
  - —
- Status: Aderente

CDU-10 – Disponibilizar revisão do cadastro
- Implementado:
  - Sucesso; bloqueio para atividades sem conhecimento; bloqueio se situação incorreta; histórico aberto/fechado; registros de side-effects (movimentação/e-mail/alerta) no fluxo de disponibilização.
- Lacunas:
  - —
- Status: Aderente

CDU-11 – Visualizar cadastro (somente leitura)
- Implementado:
  - ADMIN/GESTOR: acesso a leitura; CHEFE/SERVIDOR: acesso à própria unidade; lista de atividades e conhecimentos.
- Lacunas:
  - Habilitações condicionadas por situação (ex.: cards visíveis após disponibilização); asserts estruturais por atividade/linhas quando aplicável.
- Status: Parcial

CDU-12 – Verificar impactos no mapa
- Implementado:
  - Caso "sem impactos": notificação informativa; caso "com impactos": abertura do modal "Competências impactadas" com seções detalhadas de atividades inseridas (com ícones e conhecimentos associados), competências impactadas (com ícones/tipos e origem das alterações) e fechamento consistente.
- Lacunas:
  - —
- Status: Aderente

CDU-13 – Analisar cadastro (Mapeamento)
- Implementado:
  - Histórico; devolução (GESTOR/ADMIN); aceite (GESTOR); homologação (ADMIN) com variações de botões; movimentações e e-mails/alertas presentes em pontos do fluxo; campos completos das movimentações; devolução para a própria unidade reabrindo etapa/limpando datas; encadeamento unidade-superior após aceite.
- Lacunas:
  - —
- Status: Aderente

CDU-14 – Analisar revisão do cadastro
- Implementado:
  - Botões por perfil; devolução/aceite com mensagens; histórico; ramos de homologação ADMIN com e sem impactos contemplados em [src/views/VisAtividades.vue](src/views/VisAtividades.vue); uso assertivo do botão "Impactos no mapa" e efeitos secundários (limpezas/datas).
- Lacunas:
  - —
- Status: Aderente

CDU-15 – Manter mapa de competências
- Implementado:
  - Controles; criar/editar/excluir competências; persistência; badge quantitativa de conhecimentos por atividade com tooltip em [src/views/CadMapa.vue](src/views/CadMapa.vue).
- Lacunas:
  - Verificação da transição automática de situação “Cadastro homologado” → “Mapa criado” em todos cenários.
- Status: Aderente

CDU-16 – Ajustar mapa (Revisão)
- Implementado:
  - Botão Impactos; abrir modal; CRUD; validações de associação; fluxo de disponibilização integrado.
- Lacunas:
  - Feedback detalhado listando atividades pendentes quando faltarem associações; efeitos secundários (histórico/sugestões) quando aplicável.
- Status: Aderente

CDU-17 – Disponibilizar mapa
- Implementado:
  - Modal título/campos; obrigatoriedade de data; processamento/cancelamento; validações de regras de consistência (todas competências com atividades; todas atividades associadas a alguma competência); efeitos de negócio (situações, movimentações, e-mails/alertas); limpeza de sugestões/histórico no subprocesso em [src/views/CadMapa.vue](src/views/CadMapa.vue).
- Lacunas:
  - Revisar todos os ramos de erro com mensagens mais específicas, se necessário.
- Status: Aderente

CDU-18 – Visualizar mapa
- Implementado:
  - Navegações por perfil; título, unidade, blocos de competência, atividades e conhecimentos (quando existirem); ausência de botões de ação para SERVIDOR.
- Lacunas:
  - Verificações estruturais completas (ex.: todos conhecimentos por atividade) onde fizer sentido.
- Status: Aderente

CDU-19 – Validar mapa (CHEFE)
- Implementado:
  - Botões Apresentar Sugestões/Validar; histórico condicional; apresentar sugestões (confirmar e cancelar); validar (confirmar e cancelar); side-effects principais (movimentações/e-mails/alertas) nos fluxos de aceite e sugestões. Consolidações na store para atualização de situação e dataFimEtapa2 (quando aplicável) e limpeza de histórico.
- Lacunas:
  - Atualizações explícitas de situação em todos ramos e consistência temporal — parcialmente endereçado pelas consolidações recentes.
- Status: Aderente

CDU-20 – Analisar validação (GESTOR/ADMIN)
- Implementado:
  - GESTOR (devolver/aceitar) e ADMIN (homologar); ver sugestões quando aplicável; histórico; homologação com e sem impactos em [src/views/VisAtividades.vue](src/views/VisAtividades.vue). Consolidações na store — homologação ADMIN também registra movimentação; resets de datas (ex.: dataFimEtapa2) e limpeza de histórico conforme ramos.
- Lacunas:
  - E-mails/alertas adicionais; cobertura de todos ramos quando “com sugestões”.
- Status: Aderente

CDU-21 – Finalizar processo
- Implementado:
  - Impedir finalização se há unidades não-homologadas; modal de confirmação; cancelar/confirmar; mapas marcados como vigentes; mensagens; e-mails simulados; finalizado no Painel.
- Lacunas:
  - E-mails consolidados por tipo de unidade (agrupamentos específicos por perfil/unidade).
- Status: Aderente

Recomendações de implementação (priorizadas)
1) Snapshot hierárquico (CDU-04/05): evoluir unidadesSnapshot (lista de siglas) para estrutura de árvore opcional com metadados (tipo, relações), mantendo compatibilidade com o formato atual. Locais: [src/types/tipos.ts](src/types/tipos.ts), [src/views/CadProcesso.vue](src/views/CadProcesso.vue).
2) Painel (CDU-02): revisar critérios compostos de ordenação “Processo”, se necessário; avaliar “marcar como lido” automático na primeira visualização detalhada.
3) Mensagens/erros: continuar refinando mensagens negativas específicas onde fizer sentido (ex.: casos-limite em CDU-17), mantendo padronização aplicada nos fluxos 09/10/19/20.
