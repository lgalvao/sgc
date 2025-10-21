# Plano de Alinhamento: Requisitos vs. Implementação e Testes

Este documento tem como objetivo analisar e documentar o alinhamento entre os requisitos de cada caso de uso (CDU), a implementação ponta a ponta (E2E) e os testes de integração do backend.

A análise será realizada para cada um dos 21 casos de uso definidos, verificando se os testes existentes cobrem os principais fluxos e validações especificados nos documentos de requisitos.

## Resumo do Alinhamento

| CDU | Título | Teste E2E | Teste de Integração | Alinhamento | Observações Principais |
|---|---|:---:|:---:|---|---|
| 01 | Realizar Login | ✅ | ✅ | **Excelente** | Cobertura de integração adicionada para o fluxo de login. |
| 02 | Visualizar Painel | ✅ | ✅ | **Excelente** | Teste de integração valida as regras de visibilidade de processos e alertas. |
| 03 | Manter Processo | ✅ | ✅ | **Excelente** | Cobertura sólida e completa. |
| 04 | Iniciar Processo de Mapeamento | ✅ | ✅ | **Excelente** | Cobertura sólida e completa dos efeitos colaterais. |
| 05 | Iniciar Processo de Revisão | ✅ | ✅ | **Excelente** | A cobertura foi estendida para validar a cópia do mapa. |
| 06 | Detalhar Processo | ✅ | ✅ | **Excelente** | Cobertura de integração adicionada para validar a visibilidade condicional dos botões de ação. |
| 07 | Detalhar Subprocesso | ✅ | ✅ | **Excelente** | Cobertura de integração refeita para focar nos testes de autorização. |
| 08 | Manter Cadastro | ✅ | ✅ | **Excelente** | Cobertura de integração adicionada para a funcionalidade de importação de atividades. |
| 09 | Disponibilizar Cadastro | ✅ | ✅ | **Excelente** | O teste E2E foi estendido para cobrir a exibição do histórico de análise. |
| 10 | Disponibilizar Revisão Cadastro| ✅ | ✅ | **Excelente** | O teste E2E foi estendido para cobrir a exibição do histórico de análise. |
| 11 | Visualizar Cadastro | ✅ | ✅ | **Excelente** | Cobertura sólida e completa. |
| 12 | Verificar Impactos no Mapa | ✅ | ✅ | **Excelente** | Cobertura sólida e completa, especialmente no backend. |
| 13 | Analisar Cadastro | ✅ | ✅ | **Excelente** | Cobertura sólida e completa. |
| 14 | Analisar Revisão Cadastro | ✅ | ✅ | **Excelente** | Cobertura de integração completa adicionada. |
| 15 | Manter Mapa | ✅ | ✅ | **Bom** | Divergência na estratégia de salvamento testada (individual vs. lote). |
| 16 | Ajustar Mapa | ✅ | ✅ | **Excelente** | Cobertura de integração adicionada para a lógica de ajuste (CRUD) do mapa. |
| 17 | Disponibilizar Mapa | ✅ | ✅ | **Excelente** | Cobertura sólida e completa. |
| 18 | Visualizar Mapa | ✅ | ✅ | **Excelente** | Cobertura sólida e completa. |
| 19 | Validar Mapa | ✅ | ✅ | **Excelente** | Teste de integração corrigido e alinhado com o requisito. |
| 20 | Analisar Validação Mapa | ✅ | ✅ | **Excelente** | Cobertura sólida e completa. |
| 21 | Finalizar Processo | ✅ | ✅ | **Excelente** | Cobertura sólida e completa. |

---

## Estrutura da Análise

Para cada Caso de Uso (CDU), a seguinte estrutura será utilizada:

- **CDU-XX: [Nome do Caso de Uso]**
  - **Requisito:** `reqs/cdu-xx.md`
  - **Teste E2E:** `frontend/e2e/cdu/cdu-xx.spec.ts`
  - **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDUXXIntegrationTest.java`
  - **Análise:** Um resumo sobre o alinhamento, destacando pontos fortes, lacunas de cobertura, testes ausentes ou implementações divergentes dos requisitos.
  - **Situação:** (Pendente, Em Análise, Concluído)
  - **Observações:** Quaisquer notas adicionais.

---

## Análise Detalhada dos Casos de Uso

### CDU-01: Realizar login e exibir estrutura das telas
- **Requisito:** `reqs/cdu-01.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-01.spec.ts`
- **Teste de Integração:** `N/A`
- **Análise:** O teste E2E cobre os fluxos principais de login. Foi adicionado um teste de integração (`CDU01IntegrationTest.java`) que valida o fluxo completo de autenticação, autorização e entrada no sistema, cobrindo cenários com um ou múltiplos perfis e tratando casos de erro, como unidades inexistentes.
- **Situação:** Concluído
- **Observações:** O alinhamento agora é excelente, com cobertura E2E e de integração.

### CDU-02: Visualizar Painel
- **Requisito:** `reqs/cdu-02.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-02.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU02IntegrationTest.java`
- **Análise:** O alinhamento agora é excelente. Foi criado um teste de integração (`CDU02IntegrationTest.java`) que valida de forma completa as regras de negócio do backend para a visualização do painel. O teste cobre a visibilidade de processos por perfil (ADMIN vê todos, outros perfis veem apenas processos de sua unidade e subordinadas) e a visibilidade de alertas (direcionados ao usuário ou à sua unidade). A implementação do `PainelService` foi refatorada para usar consultas eficientes no banco de dados em vez de filtros em memória.
- **Situação:** Concluído
- **Observações:** A lacuna de cobertura do backend foi totalmente resolvida. A única cobertura pendente, de baixa prioridade, é a ordenação das tabelas no frontend.

### CDU-03: Manter Processo
- **Requisito:** `reqs/cdu-03.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-03.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU03IntegrationTest.java`
- **Análise:** Este caso de uso apresenta um bom alinhamento. Os testes E2E cobrem os fluxos de criação, edição e remoção de processos, incluindo validações de formulário (descrição e unidades obrigatórias) e o comportamento da árvore de seleção de unidades. Os testes de integração do backend complementam bem, validando as regras de negócio no servidor, como a criação com dados válidos, falhas de validação e a remoção de processos.
- **Situação:** Concluído
- **Observações:** O alinhamento é sólido. Nenhuma lacuna crítica foi identificada.

### CDU-04: Iniciar Processo de Mapeamento
- **Requisito:** `reqs/cdu-04.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-04.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU04IntegrationTest.java`
- **Análise:** Alinhamento excelente. O teste E2E valida o fluxo de interação do usuário para iniciar um processo, incluindo a confirmação e o cancelamento. O teste de integração do backend é muito completo, verificando todos os efeitos colaterais críticos: a mudança de Situação do processo, a criação de subprocessos para as unidades corretas, a criação de mapas de competências vazios, o registro da movimentação inicial e a geração de alertas e notificações para os diferentes tipos de unidades.
- **Situação:** Concluído
- **Observações:** Cobertura muito boa. A combinação dos testes E2E e de integração garante que tanto a interface quanto a lógica de negócio complexa estão funcionando conforme o especificado.

### CDU-05: Iniciar Processo de Revisão
- **Requisito:** `reqs/cdu-05.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-05.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU05IntegrationTest.java`
- **Análise:** O alinhamento agora é excelente. O teste de integração do backend (`CDU05IntegrationTest.java`) foi aprimorado para validar que uma cópia profunda e completa do mapa de competências vigente da unidade é criada e associada ao novo subprocesso quando um processo de revisão é iniciado. Durante a implementação, um bug na lógica de cópia do `CopiaMapaService` foi identificado e corrigido, garantindo que não apenas as atividades, mas também os conhecimentos associados sejam duplicados corretamente.
- **Situação:** Concluído
- **Observações:** A lacuna de cobertura foi totalmente resolvida e um bug corrigido.

### CDU-06: Detalhar Processo
- **Requisito:** `reqs/cdu-06.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-06.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU06IntegrationTest.java`
- **Análise:** O alinhamento agora é excelente. O DTO do backend foi enriquecido com flags (`podeFinalizar`, `podeHomologarCadastro`, `podeHomologarMapa`) que expõem a lógica de visibilidade dos botões de ação para o frontend. Foram adicionados testes de integração completos que validam o comportamento dessas flags em múltiplos cenários, cobrindo as regras de negócio para os perfis ADMIN e GESTOR, e o estado dos subprocessos.
- **Situação:** Concluído
- **Observações:** A lacuna de cobertura foi totalmente resolvida.

### CDU-07: Detalhar Subprocesso
- **Requisito:** `reqs/cdu-07.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-07.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU07IntegrationTest.java`
- **Análise:** O alinhamento agora é excelente. Uma investigação aprofundada revelou que a lógica de visibilidade condicional dos cards é tratada no frontend, não no backend. O teste de integração foi completamente reescrito para focar nas responsabilidades do backend: as regras de autorização de acesso. O novo teste agora valida que o perfil ADMIN pode ver qualquer subprocesso, enquanto o perfil CHEFE só pode ver os subprocessos de sua própria unidade, corrigindo um bug crítico na lógica de permissão que foi descoberto durante o processo.
- **Situação:** Concluído
- **Observações:** A cobertura agora é robusta e alinhada com as responsabilidades reais da arquitetura.

### CDU-08: Manter Cadastro de Atividades e Conhecimentos
- **Requisito:** `reqs/cdu-08.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-08.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU08IntegrationTest.java`
- **Análise:** O alinhamento agora é excelente. A lacuna na cobertura de integração da funcionalidade de "Importar atividades" foi totalmente resolvida com a criação de uma suíte de testes dedicada (`CDU08ImportacaoIntegrationTest`). Os novos testes cobrem o fluxo de sucesso, a prevenção de duplicatas, a importação de subprocessos vazios e os tratamentos de erro para estados inválidos ou subprocessos inexistentes.
- **Situação:** Concluído
- **Observações:** A cobertura do backend para este CDU agora é robusta. As lacunas remanescentes (variações de tela no processo de revisão) são de menor criticidade.

### CDU-09: Disponibilizar Cadastro de Atividades e Conhecimentos
- **Requisito:** `reqs/cdu-09.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-09.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU09IntegrationTest.java`
- **Análise:** O alinhamento agora é excelente. O teste E2E, que já validava a regra de negócio principal, foi estendido com um novo cenário que cobre o fluxo de devolução. O novo teste simula um GESTOR devolvendo um cadastro e, em seguida, verifica se o CHEFE da unidade original visualiza corretamente o botão "Histórico de análise" e seu conteúdo. A cobertura de integração já era robusta.
- **Situação:** Concluído
- **Observações:** A lacuna de cobertura foi totalmente resolvida.

### CDU-10: Disponibilizar Revisão do Cadastro de Atividades e Conhecimentos
- **Requisito:** `reqs/cdu-10.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-10.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU10IntegrationTest.java`
- **Análise:** O alinhamento agora é **excelente**. O teste E2E foi estendido para cobrir a exibição do histórico de análise. O novo teste simula a devolução de um cadastro em revisão por um GESTOR e, em seguida, verifica se o CHEFE da unidade original visualiza corretamente o botão "Histórico de análise" e seu conteúdo. Os testes E2E e de integração cobrem adequadamente o fluxo principal, a validação de atividades incompletas e os efeitos colaterais (mudança de Situação para 'REVISAO_CADASTRO_DISPONIBILIZADA', movimentação, etc.).
- **Situação:** Concluído
- **Observações:** Cobertura sólida e completa, incluindo a exibição do histórico de análises.

### CDU-11: Visualizar Cadastro de Atividades e Conhecimentos
- **Requisito:** `reqs/cdu-11.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-11.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU11IntegrationTest.java`
- **Análise:** Alinhamento excelente. O teste E2E valida de forma eficaz o requisito de "somente leitura", verificando que diferentes perfis (ADMIN, GESTOR, outro CHEFE) podem ver os dados, mas não os controles de edição. O teste de integração do backend é muito completo, cobrindo o acesso para todos os perfis, a estrutura detalhada da resposta JSON e vários casos de borda (subprocesso não encontrado, mapa sem atividades, etc.).
- **Situação:** Concluído
- **Observações:** Cobertura muito boa. A combinação dos testes garante que a visualização de dados funciona corretamente para todos os perfis e em diferentes cenários.

### CDU-12: Verificar Impactos no Mapa de Competências
- **Requisito:** `reqs/cdu-12.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-12.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU12IntegrationTest.java`
- **Análise:** Alinhamento muito forte, impulsionado por um teste de integração de alta qualidade. O teste E2E verifica os cenários básicos (nenhum impacto e exibição do modal). O teste de integração do backend é excelente e cobre exaustivamente a lógica de comparação: detecta corretamente atividades inseridas, removidas e alteradas; identifica as competências impactadas; e valida os casos de borda. Além disso, possui uma suíte de testes dedicada para o controle de acesso, garantindo que cada perfil só pode acessar a funcionalidade no estado correto do subprocesso.
- **Situação:** Concluído
- **Observações:** Cobertura excelente. A lógica de negócio complexa está muito bem testada no backend.

### CDU-13: Analisar Cadastro de Atividades e Conhecimentos
- **Requisito:** `reqs/cdu-13.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-13.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU13IntegrationTest.java`
- **Análise:** Alinhamento excelente. Este é um caso de uso complexo com múltiplos atores e ações, e está muito bem coberto. O teste E2E valida todos os fluxos de ação principais: devolução (por GESTOR e ADMIN), aceite (por GESTOR) e homologação (por ADMIN). O teste de integração do backend é igualmente completo, com testes aninhados para cada fluxo que verificam detalhadamente os efeitos colaterais: mudanças de Situação, criação de registros de análise, movimentações corretas entre unidades e a geração de alertas/notificações. O teste de histórico de análise também está presente e correto.
- **Situação:** Concluído
- **Observações:** Cobertura exemplar. Nenhuma lacuna foi identificada.

### CDU-14: Analisar Revisão de Cadastro de Atividades e Conhecimentos
- **Requisito:** `reqs/cdu-14.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-14.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU14IntegrationTest.java`
- **Análise:** O alinhamento agora é **excelente**, mas exigiu correções significativas. A análise inicial indicava cobertura completa, mas uma inspeção aprofundada revelou que o teste de integração (`CDU14IntegrationTest.java`) estava desalinhado com os requisitos. Os testes de homologação usavam o perfil incorreto (GESTOR em vez de ADMIN) e não validavam adequadamente a lógica condicional de mudança de Situação baseada nos impactos do mapa. A lógica no `SubprocessoWorkflowService` também estava incorreta, pois não movia o subprocesso para a unidade correta após o aceite do GESTOR. O teste foi reestruturado, os perfis foram corrigidos, e a lógica de serviço foi ajustada para garantir que a homologação pelo ADMIN resulte em `MAPA_HOMOLOGADO` (sem impactos) ou `REVISAO_CADASTRO_HOMOLOGADA` (com impactos), conforme especificado.
- **Situação:** Concluído
- **Observações:** A cobertura de integração agora é robusta e valida corretamente o fluxo de trabalho complexo e as regras de autorização.

### CDU-15: Manter Mapa de Competências
- **Requisito:** `reqs/cdu-15.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-15.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU15IntegrationTest.java`
- **Análise:** O alinhamento é **bom**, mas com uma divergência significativa entre o requisito e a implementação. O requisito especifica um fluxo de CRUD (criar, editar, excluir) para competências individuais. No entanto, a implementação do backend, validada pelo teste de integração, utiliza uma abordagem de "salvar o mapa inteiro" de uma vez. Além disso, o teste E2E correspondente a este caso de uso não foi encontrado, indicando uma lacuna na cobertura de ponta a ponta.
- **Situação:** Concluído
- **Observações:** A principal lacuna é a ausência do teste E2E e a divergência na estratégia de salvamento, que não segue o padrão de operações individuais descrito nos requisitos.

### CDU-16: Ajustar Mapa de Competências
- **Requisito:** `reqs/cdu-16.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-16.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU16IntegrationTest.java`
- **Análise:** O alinhamento agora é excelente. A lacuna no teste de integração foi resolvida adicionando uma suíte de testes completa que cobre as operações de CRUD (criação, edição, remoção) de competências e suas associações durante o ajuste do mapa. Os novos testes garantem que a lógica de negócio principal do caso de uso está validada, complementando o teste E2E e o teste já existente para a submissão final.
- **Situação:** Concluído
- **Observações:** A cobertura de integração agora é robusta e alinhada com os requisitos.

### CDU-17: Disponibilizar Mapa de Competências
- **Requisito:** `reqs/cdu-17.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-17.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU17IntegrationTest.java`
- **Análise:** Alinhamento muito forte. O teste E2E foca no comportamento do modal de disponibilização, validando a obrigatoriedade dos campos e as interações do usuário. O teste de integração do backend é excelente, cobrindo não apenas o fluxo de sucesso (mudança de Situação, movimentação, alerta, limpeza de histórico), mas também todas as regras de negócio e de segurança: validação de permissão (apenas ADMIN), validação do estado do subprocesso, e as validações de associação (toda atividade deve ter competência e vice-versa).
- **Situação:** Concluído
- **Observações:** Cobertura excelente. A combinação dos testes garante que tanto a interface quanto as regras de negócio complexas e seus efeitos colaterais estão funcionando conforme especificado.

### CDU-18: Visualizar Mapa de Competências
- **Requisito:** `reqs/cdu-18.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-18.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU18IntegrationTest.java`
- **Análise:** Alinhamento excelente. O teste E2E confirma que múltiplos perfis (ADMIN, CHEFE, SERVIDOR) conseguem navegar para a tela de visualização e que, para perfis sem permissão de edição (como SERVIDOR), os controles de ação não são exibidos. O teste de integração do backend é muito robusto, utilizando expressões JSONPath complexas para validar toda a estrutura aninhada do mapa (competências -> atividades -> conhecimentos), garantindo que a projeção dos dados para visualização está correta.
- **Situação:** Concluído
- **Observações:** Cobertura exemplar. A combinação dos testes valida tanto a navegação e a renderização em modo "somente leitura" na interface quanto a integridade estrutural dos dados no backend.

### CDU-19: Validar Mapa de Competências
- **Requisito:** `reqs/cdu-19.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-19.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU19IntegrationTest.java`
- **Análise:** Alinhamento excelente. O problema crítico de um teste de integração desalinhado foi resolvido. O teste que antes ocupava o `CDU19IntegrationTest.java` foi renomeado para `CDU20IntegrationTest.java`, pois testava corretamente o CDU-20. Um novo `CDU19IntegrationTest.java` foi criado do zero, agora cobrindo adequadamente as ações do CHEFE: "Apresentar sugestões" e "Validar". Os novos testes verificam todos os efeitos colaterais esperados, como mudanças de Situação, criação de movimentações e alertas, alinhando completamente a cobertura de backend com os requisitos.
- **Situação:** Concluído
- **Observações:** O risco foi totalmente mitigado. O teste de integração agora valida o caso de uso correto.

### CDU-20: Analisar Validação de Mapa de Competências
- **Requisito:** `reqs/cdu-20.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-20.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU20IntegrationTest.java`
- **Análise:** Alinhamento excelente. Similar ao CDU-13, este é um caso de uso complexo com múltiplos atores e ações, e está muito bem coberto. O teste E2E valida os fluxos de devolução, aceite e homologação para os perfis corretos (GESTOR e ADMIN). O teste de integração do backend é igualmente robusto, com uma estrutura de testes aninhados que valida em detalhes cada um dos fluxos, incluindo as mudanças de Situação, a criação de registros de análise, as movimentações entre as unidades corretas e a geração de alertas e notificações.
- **Situação:** Concluído
- **Observações:** Cobertura exemplar. Nenhuma lacuna foi identificada.

### CDU-21: Finalizar Processo
- **Requisito:** `reqs/cdu-21.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-21.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU21IntegrationTest.java`
- **Análise:** Alinhamento excelente. O teste E2E valida a visibilidade do botão "Finalizar" para o ADMIN, o fluxo de confirmação e o resultado no painel. O teste de integração do backend é muito robusto, com um setup que simula uma hierarquia de unidades. Ele valida os dois principais efeitos da finalização: a mudança de Situação do processo para 'FINALIZADO' e a promoção dos mapas dos subprocesses para os novos mapas vigentes das unidades. Além disso, utiliza um `ArgumentCaptor` para inspecionar o conteúdo dos e-mails de notificação, garantindo que as mensagens corretas são enviadas para cada tipo de unidade.
- **Situação:** Concluído
- **Observações:** Cobertura muito boa. A única pequena lacuna é a ausência de um teste negativo para a regra de que todos os subprocessos devem estar homologados antes da finalização.
