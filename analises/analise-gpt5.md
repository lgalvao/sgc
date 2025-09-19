# Análise de requisitos vs implementação (SGC - TRE-PE)

Objetivo: analisar os requisitos funcionais em reqs/ (CDUs 01–21 e diagrama de estados) e verificar, no protótipo atual, o que está implementado e o que ainda falta. A validação foi realizada confrontando os CDUs com as views, componentes e stores principais.

Fontes de verdade no código:
- Rotas: [src/router.ts](src/router.ts)
- Views: [src/views/Login.vue](src/views/Login.vue), [src/views/Painel.vue](src/views/Painel.vue), [src/views/Processo.vue](src/views/Processo.vue), [src/views/Subprocesso.vue](src/views/Subprocesso.vue), [src/views/CadProcesso.vue](src/views/CadProcesso.vue), [src/views/CadAtividades.vue](src/views/CadAtividades.vue), [src/views/CadMapa.vue](src/views/CadMapa.vue), [src/views/VisAtividades.vue](src/views/VisAtividades.vue), [src/views/VisMapa.vue](src/views/VisMapa.vue), [src/views/Relatorios.vue](src/views/Relatorios.vue), [src/views/Historico.vue](src/views/Historico.vue)
- Componentes: [src/components/Navbar.vue](src/components/Navbar.vue), [src/components/BarraNavegacao.vue](src/components/BarraNavegacao.vue), [src/components/TreeTable.vue](src/components/TreeTable.vue), [src/components/SubprocessoHeader.vue](src/components/SubprocessoHeader.vue), [src/components/SubprocessoCards.vue](src/components/SubprocessoCards.vue), [src/components/ImpactoMapaModal.vue](src/components/ImpactoMapaModal.vue), [src/components/HistoricoAnaliseModal.vue](src/components/HistoricoAnaliseModal.vue), [src/components/DisponibilizarMapaModal.vue](src/components/DisponibilizarMapaModal.vue), [src/components/AceitarMapaModal.vue](src/components/AceitarMapaModal.vue)
- Stores: [src/stores/processos.ts](src/stores/processos.ts), [src/stores/subprocessos.ts](src/stores/subprocessos.ts), [src/stores/mapas.ts](src/stores/mapas.ts), [src/stores/atividades.ts](src/stores/atividades.ts), [src/stores/analises.ts](src/stores/analises.ts), [src/stores/alertas.ts](src/stores/alertas.ts), [src/stores/perfil.ts](src/stores/perfil.ts), [src/stores/revisao.ts](src/stores/revisao.ts)
- Diagrama de estados: [reqs/estados-mapeamento.md](reqs/estados-mapeamento.md)

Resumo executivo
- Cobertura por CDU (visão geral): 19/21 completos ou muito próximos; 2 com lacunas/parcialidades.
- Itens transversais pendentes: footer com versão (CDU-01), regras de ordenação/marcação na tabela de Alertas (CDU-02), seleção “interoperacional com filhos não selecionados” (CDU-03), criação do mapa vazio no início de mapeamento (CDU-04), checagens de importação sem duplicidade (CDU-08).
- Integrações externas (autenticação/SGRH/e-mail) são simuladas, como esperado do protótipo.

Mapa de cobertura por CDU

CDU-01 – Realizar login e exibir estrutura das telas
- Implementado:
  - Login com seleção de perfil/unidade quando múltiplos vínculos: [src/views/Login.vue](src/views/Login.vue)
  - Navbar com itens Painel, Minha Unidade, Relatórios e Histórico e engrenagem para ADMIN: [src/components/Navbar.vue](src/components/Navbar.vue)
  - Estrutura de páginas e breadcrumbs: [src/App.vue](src/App.vue), [src/components/BarraNavegacao.vue](src/components/BarraNavegacao.vue)
- Divergências/pontos a ajustar:
  - Mensagem de falha de autenticação: requisito “Título ou senha inválidos”; implementação usa “Usuário não encontrado” e não valida senha de fato (protótipo).
  - Rodapé com versão e “Desenvolvido por SESEL/COSIS/TRE-PE” ausente (não há footer específico).
  - No topo, exibição “[Perfil] – [Sigla unidade]” está interativa (abre seletor) e não “texto fixo” como no requisito.
- Status: Parcial (funcionalidade OK; mensagens/footer/ux diferem do requisito)

CDU-02 – Visualizar Painel
- Implementado:
  - Seções Processos (ordenável) e Alertas: [src/views/Painel.vue](src/views/Painel.vue), [src/components/TabelaProcessos.vue](src/components/TabelaProcessos.vue)
  - Botão “Criar processo” para ADMIN: [src/views/Painel.vue](src/views/Painel.vue)
  - Itens da tabela clicáveis com rotas conforme perfil: [src/views/Painel.vue](src/views/Painel.vue), [src/router.ts](src/router.ts)
- Divergências/pontos a ajustar:
  - Processos na situação “Criado” deveriam aparecer apenas para ADMIN. O filtro atual ([src/composables/useProcessosFiltrados.ts](src/composables/useProcessosFiltrados.ts)) não os exclui de forma explícita para outros perfis.
  - Alertas: requisito pede ordem decrescente por data/hora por padrão e ordenação pelo cabeçalho “Processo”. A tabela de Alertas não implementa ordenação por cabeçalho nem garante a ordem inicial (marcações de leitura ocorrem no clique).
- Status: Parcial

CDU-03 – Manter processo
- Implementado:
  - Formulário com descrição, tipo, data limite e árvore com checkboxes tri-state: [src/views/CadProcesso.vue](src/views/CadProcesso.vue)
  - Validações: descrição, data, ao menos uma unidade; revisão/diagnóstico exigem mapa vigente; diagnóstico exige servidores na unidade.
  - Editar/Remover apenas para “Criado” por via de navegação (botão Remover é exibido quando vindo de processo “Criado”): [src/views/CadProcesso.vue](src/views/CadProcesso.vue)
- Divergência específica:
  - Regra 2.3.2.5 (unidade interoperacional selecionável sem seus subordinados): o comportamento atual seleciona/deseleciona sempre a subárvore inteira (toggle), não permitindo selecionar apenas a raiz interoperacional isoladamente.
- Status: Parcial

CDU-04 – Iniciar processo de mapeamento
- Implementado:
  - Confirmação, mudança para “Em andamento”, criação de subprocessos “Não iniciado”, movimentações e alertas para operacionais, intermediárias e interoperacionais (duplo alerta): [src/views/CadProcesso.vue](src/views/CadProcesso.vue), [src/stores/processos.ts](src/stores/processos.ts)
  - E-mails simulados: [src/stores/notificacoes.ts](src/stores/notificacoes.ts)
- Lacuna:
  - Criação de “mapa vazio” para cada subprocesso participante (passo 10) não é explicitamente realizada para Mapeamento.
- Status: Parcial

CDU-05 – Iniciar processo de revisão
- Implementado:
  - Cópia do mapa vigente por unidade participante, subprocessos criados, movimentações, alertas e e-mails diferenciados: [src/views/CadProcesso.vue](src/views/CadProcesso.vue), [src/stores/processos.ts](src/stores/processos.ts), [src/stores/mapas.ts](src/stores/mapas.ts)
- Status: Completo

CDU-06 – Detalhar processo
- Implementado:
  - Tabela hierárquica de unidades com situação, data limite e unidade atual; botões de ação em bloco (Aceitar/Homologar); Finalizar processo (ADMIN): [src/views/Processo.vue](src/views/Processo.vue), [src/components/TreeTable.vue](src/components/TreeTable.vue)
  - ADMIN pode alterar data limite do subprocesso (modal em Subprocesso): [src/views/Subprocesso.vue](src/views/Subprocesso.vue), [src/stores/processos.ts](src/stores/processos.ts)
- Status: Completo

CDU-07 – Detalhar subprocesso
- Implementado:
  - Dados da unidade (titular/responsável), situação/localização, movimentações (ordem decrescente) e cards de acesso (Atividades/Mapa/Diagnóstico/Ocupações): [src/views/Subprocesso.vue](src/views/Subprocesso.vue), [src/components/SubprocessoHeader.vue](src/components/SubprocessoHeader.vue), [src/components/SubprocessoCards.vue](src/components/SubprocessoCards.vue)
- Status: Completo

CDU-08 – Manter cadastro de atividades e conhecimentos
- Implementado:
  - CRUD de atividades/conhecimentos com edição/remoção e auto-save (store), botão Impacto (em revisão), importação de atividades: [src/views/CadAtividades.vue](src/views/CadAtividades.vue), [src/components/ImpactoMapaModal.vue](src/components/ImpactoMapaModal.vue), [src/components/ImportarAtividadesModal.vue](src/components/ImportarAtividadesModal.vue)
- Lacuna:
  - Importação: regras 13.7.1 e 13.7.2 (evitar duplicatas e avisar) não estão evidentes (não há validação explícita de duplicidade ao importar).
- Status: Parcial

CDU-09 – Disponibilizar cadastro de atividades e conhecimentos (Mapeamento)
- Implementado:
  - Verificação de atividades sem conhecimentos; confirmação; alteração de situação para “Cadastro disponibilizado”; movimentação; e-mail; alerta; data/hora fim etapa 1; exclusão do histórico de análise; redirecionamento: [src/views/CadAtividades.vue](src/views/CadAtividades.vue), [src/stores/analises.ts](src/stores/analises.ts), [src/stores/processos.ts](src/stores/processos.ts), [src/stores/alertas.ts](src/stores/alertas.ts)
- Status: Completo

CDU-10 – Disponibilizar revisão do cadastro (Revisão)
- Implementado (espelho do CDU-09 com textos/estado próprios): [src/views/CadAtividades.vue](src/views/CadAtividades.vue)
- Status: Completo

CDU-11 – Visualizar cadastro de atividades e conhecimentos
- Implementado:
  - Visualização somente leitura por atividade e seus conhecimentos, com acesso condicionado via Processo/Subprocesso conforme perfil: [src/views/VisAtividades.vue](src/views/VisAtividades.vue), [src/router.ts](src/router.ts)
- Status: Completo

CDU-12 – Verificar impactos no mapa de competências
- Implementado:
  - Modal consolidando atividades inseridas e competências impactadas (alterações/remoções), disponível nos contextos previstos: [src/components/ImpactoMapaModal.vue](src/components/ImpactoMapaModal.vue), [src/stores/revisao.ts](src/stores/revisao.ts)
- Status: Completo

CDU-13 – Analisar cadastro de atividades e conhecimentos (Mapeamento)
- Implementado:
  - Histórico de análise; devolução/aceite/homologação com registros, e-mails e alertas; ações em bloco no processo: [src/views/VisAtividades.vue](src/views/VisAtividades.vue), [src/views/Processo.vue](src/views/Processo.vue), [src/stores/analises.ts](src/stores/analises.ts), [src/stores/processos.ts](src/stores/processos.ts)
- Status: Completo

CDU-14 – Analisar revisão de cadastro (Revisão)
- Implementado:
  - Igual ao CDU-13, acrescido do botão/fluxo “Impactos no mapa” e lógica de homologação condicional por ADMIN: [src/views/VisAtividades.vue](src/views/VisAtividades.vue), [src/components/ImpactoMapaModal.vue](src/components/ImpactoMapaModal.vue), [src/stores/processos.ts](src/stores/processos.ts)
- Status: Completo

CDU-15 – Manter mapa de competências
- Implementado:
  - CRUD de competências; associação a atividades; badges de conhecimentos; disponibilização: [src/views/CadMapa.vue](src/views/CadMapa.vue), [src/components/CriarCompetenciaModal.vue](src/components/CriarCompetenciaModal.vue)
- Status: Completo

CDU-16 – Ajustar mapa de competências (Revisão)
- Implementado:
  - Edição baseada em impactos; garantir associação total de atividades a competências: [src/views/CadMapa.vue](src/views/CadMapa.vue)
- Status: Completo

CDU-17 – Disponibilizar mapa de competências
- Implementado:
  - Validações (todas competências com atividades e todas as atividades associadas a alguma competência), modal com data limite e observações, mudança de situação para “Mapa disponibilizado”, e-mails/alertas e limpeza do histórico/sugestões: [src/components/DisponibilizarMapaModal.vue](src/components/DisponibilizarMapaModal.vue), [src/stores/processos.ts](src/stores/processos.ts), [src/stores/alertas.ts](src/stores/alertas.ts)
- Status: Completo

CDU-18 – Visualizar mapa de competências
- Implementado:
  - Visualização de competências → atividades → conhecimentos, para todos os perfis, via Subprocesso: [src/views/VisMapa.vue](src/views/VisMapa.vue), [src/router.ts](src/router.ts)
- Status: Completo

CDU-19 – Validar mapa de competências (CHEFE)
- Implementado:
  - Apresentar sugestões (texto formatado), validar (modal de confirmação), histórico quando aplicável; mudança para “Mapa com sugestões”/“Mapa validado”, e-mails/alertas e movimentações: [src/views/VisMapa.vue](src/views/VisMapa.vue), [src/stores/processos.ts](src/stores/processos.ts)
- Status: Completo

CDU-20 – Analisar validação de mapa de competências (GESTOR/ADMIN)
- Implementado:
  - Ver sugestões; histórico; devolução/aceite/homologação; efeitos sobre situação e data fim da etapa 2 quando devolvido à própria unidade: [src/views/VisMapa.vue](src/views/VisMapa.vue), [src/stores/analises.ts](src/stores/analises.ts), [src/stores/processos.ts](src/stores/processos.ts)
- Status: Completo

CDU-21 – Finalizar processo de mapeamento ou de revisão
- Implementado:
  - Verificação de pré-condição (todos os subprocessos operacionais/interoperacionais com “Mapa homologado”), definição de mapas vigentes, e-mails diferenciados por tipo de unidade, alertas e mensagem final: [src/views/Processo.vue](src/views/Processo.vue), [src/stores/mapas.ts](src/stores/mapas.ts), [src/stores/processos.ts](src/stores/processos.ts), [src/stores/alertas.ts](src/stores/alertas.ts)
- Status: Completo

Alinhamento com o diagrama de estados (estados-mapeamento.md)
- O ciclo de Cadastro (Não iniciado → Em andamento → Disponibilizado → Homologado) e de Mapa (Criado → Disponibilizado → Validado/Com sugestões → Homologado) está refletido nas transições implementadas em [src/stores/processos.ts](src/stores/processos.ts) e nas views [src/views/CadAtividades.vue](src/views/CadAtividades.vue), [src/views/VisMapa.vue](src/views/VisMapa.vue), [src/views/CadMapa.vue](src/views/CadMapa.vue).
- Ponto específico: no início de Mapeamento (CDU-04), a criação do “mapa vazio” por subprocesso ainda não é consistente; isso afeta a aderência completa ao estado “Mapa criado” após “Cadastro homologado” quando o mapa ainda não existe materialmente.
- As devoluções e revalidações entre unidades (origem/destino, unidadeAnterior/unidadeAtual, dataFimEtapa2 reset quando devolvido à própria unidade) foram implementadas conforme decisões do diagrama.

Gaps transversais e ajustes recomendados
1) CDU-01
- Implementar rodapé padrão com versão e “Desenvolvido por SESEL/COSIS/TRE-PE” (ex.: em [src/App.vue](src/App.vue)).
- Ajustar mensagens de erro de login para refletir “Título ou senha inválidos” e validar a senha no protótipo ou registrar observação de limitação.
- Tornar o “[Perfil] – [Sigla]” não interativo (ou manter a conveniência e documentar divergência do requisito).

2) CDU-02
- Filtrar processos em situação “Criado” para que apenas ADMIN visualize (ajuste em [src/composables/useProcessosFiltrados.ts](src/composables/useProcessosFiltrados.ts)).
- Implementar ordenação da tabela de Alertas por cabeçalhos (especialmente “Processo”) e garantir ordem decrescente por data/hora inicialmente em [src/views/Painel.vue](src/views/Painel.vue); marcar como “lido” na primeira visualização (carregamento) ou documentar o critério (no clique).

3) CDU-03 (Árvore de unidades)
- Permitir, quando a raiz for “interoperacional”, selecionar apenas a raiz sem propagar obrigatoriamente para filhos (regra 2.3.2.5). Ajustar a lógica de toggle em [src/views/CadProcesso.vue](src/views/CadProcesso.vue) (e/ou reutilizar um componente genérico como [src/components/UnidadeTreeItem.vue](src/components/UnidadeTreeItem.vue)).

4) CDU-04
- Ao iniciar mapeamento, criar um mapa vazio por subprocesso (ex.: ação em [src/stores/mapas.ts](src/stores/mapas.ts) invocada no fluxo de início em [src/views/CadProcesso.vue](src/views/CadProcesso.vue)).

5) CDU-08 (Importação)
- Implementar checagem e bloqueio de importação de atividades duplicadas e mensagens detalhando as que foram ignoradas (regras 13.7.1 e 13.7.2) em [src/components/ImportarAtividadesModal.vue](src/components/ImportarAtividadesModal.vue) / [src/views/CadAtividades.vue](src/views/CadAtividades.vue).

6) Integrações (fora do escopo do protótipo, mas necessárias para produção)
- Autenticação (Sistema Acesso do TRE-PE), SGRH, persistência real e envio de e-mails (hoje simulados) – manter documentado no README e backlog técnico.

Conclusão
- A implementação atual cobre, com alto grau de fidelidade, os fluxos de Mapeamento e Revisão descritos nos CDUs, inclusive os ciclos de disponibilização, análise (aceite/devolução/homologação), validação de mapas, sugestões e finalização de processos.
- As principais lacunas mapeadas são pontuais (rodapé/informações estáticas, ordenação/marcações nos alertas, um caso específico de seleção em árvore, criação de mapa vazio no início do mapeamento e de-duplicação na importação).
- Recomenda-se priorizar os ajustes dos itens 1–5 para alinhar 100% com os requisitos e documentar oficialmente as funcionalidades simuladas do protótipo (integrações externas).