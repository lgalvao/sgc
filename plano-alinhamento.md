# Plano de Alinhamento: Requisitos vs. Implementação e Testes

Este documento tem como objetivo analisar e documentar o alinhamento entre os requisitos de cada caso de uso (CDU), a implementação ponta a ponta (E2E) e os testes de integração do backend.

A análise será realizada para cada um dos 21 casos de uso definidos, verificando se os testes existentes cobrem os principais fluxos e validações especificados nos documentos de requisitos.

## Resumo do Alinhamento

| CDU | Título | Teste E2E | Teste de Integração | Alinhamento | Observações Principais |
|---|---|:---:|:---:|---|---|
| 01 | Realizar Login | ✅ | ❌ | **Fraco** | Ausência de teste de integração para a lógica de login. |
| 02 | Visualizar Painel | ✅ | ❌ | **Fraco** | Ausência de teste de integração para as consultas do painel. |
| 03 | Manter Processo | ✅ | ✅ | **Excelente** | Cobertura sólida e completa. |
| 04 | Iniciar Processo de Mapeamento | ✅ | ✅ | **Excelente** | Cobertura sólida e completa dos efeitos colaterais. |
| 05 | Iniciar Processo de Revisão | ✅ | ✅ | **Bom** | Teste de integração não valida a cópia do mapa. |
| 06 | Detalhar Processo | ✅ | ✅ | **Parcial** | Faltam testes para ações condicionais (finalizar, homologar). |
| 07 | Detalhar Subprocesso | ✅ | ✅ | **Fraco** | Faltam testes para a lógica de visibilidade condicional dos cards. |
| 08 | Manter Cadastro | ✅ | ✅ | **Parcial** | Funcionalidade de importação e variações de revisão não testadas. |
| 09 | Disponibilizar Cadastro | ✅ | ✅ | **Bom** | Não testa a exibição do histórico de análise. |
| 10 | Disponibilizar Revisão Cadastro| ✅ | ✅ | **Bom** | Não testa a exibição do histórico de análise. |
| 11 | Visualizar Cadastro | ✅ | ✅ | **Excelente** | Cobertura sólida e completa. |
| 12 | Verificar Impactos no Mapa | ✅ | ✅ | **Excelente** | Cobertura sólida e completa, especialmente no backend. |
| 13 | Analisar Cadastro | ✅ | ✅ | **Excelente** | Cobertura sólida e completa. |
| 14 | Analisar Revisão Cadastro | ✅ | ❌ | **Crítico** | Ausência de teste de integração para lógica complexa. |
| 15 | Manter Mapa | ✅ | ✅ | **Bom** | Divergência na estratégia de salvamento testada (individual vs. lote). |
| 16 | Ajustar Mapa | ✅ | ✅ | **Parcial** | Teste de integração muito limitado, não cobre o ajuste do mapa. |
| 17 | Disponibilizar Mapa | ✅ | ✅ | **Excelente** | Cobertura sólida e completa. |
| 18 | Visualizar Mapa | ✅ | ✅ | **Excelente** | Cobertura sólida e completa. |
| 19 | Validar Mapa | ✅ | ⚠️ | **Crítico** | Teste de integração desalinhado, testa o CDU-20. |
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
  - **Status:** (Pendente, Em Análise, Concluído)
  - **Observações:** Quaisquer notas adicionais.

---

## Análise Detalhada dos Casos de Uso

### CDU-01: Realizar login e exibir estrutura das telas
- **Requisito:** `reqs/cdu-01.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-01.spec.ts`
- **Teste de Integração:** `N/A`
- **Análise:** O teste E2E cobre os fluxos principais de login, como sucesso, falha e logout. Também verifica a estrutura da tela para os perfis `ADMIN` e `SERVIDOR`. No entanto, o cenário em que um usuário tem múltiplos perfis/unidades e precisa escolher um não é explicitamente testado. A maior lacuna é a ausência de um teste de integração no backend para validar a lógica de autenticação e autorização.
- **Status:** Concluído
- **Observações:** A ausência do teste de integração do backend é um ponto crítico a ser resolvido.

### CDU-02: Visualizar Painel
- **Requisito:** `reqs/cdu-02.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-02.spec.ts`
- **Teste de Integração:** `N/A`
- **Análise:** Os testes E2E validam a visibilidade de componentes por perfil (ex: botão "Criar processo" para ADMIN), a navegação para diferentes telas a partir da tabela de processos e a estrutura da tabela de alertas. A lógica de exibição de processos (apenas da unidade do usuário e subordinadas) também é testada. No entanto, a funcionalidade de ordenação das tabelas de processos e alertas não é coberta. Novamente, falta o teste de integração do backend para garantir a corretude das consultas que alimentam o painel.
- **Status:** Concluído
- **Observações:** A ausência do teste de integração e da cobertura de ordenação das tabelas são as principais lacunas.

### CDU-03: Manter Processo
- **Requisito:** `reqs/cdu-03.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-03.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU03IntegrationTest.java`
- **Análise:** Este caso de uso apresenta um bom alinhamento. Os testes E2E cobrem os fluxos de criação, edição e remoção de processos, incluindo validações de formulário (descrição e unidades obrigatórias) e o comportamento da árvore de seleção de unidades. Os testes de integração do backend complementam bem, validando as regras de negócio no servidor, como a criação com dados válidos, falhas de validação e a remoção de processos.
- **Status:** Concluído
- **Observações:** O alinhamento é sólido. Nenhuma lacuna crítica foi identificada.

### CDU-04: Iniciar Processo de Mapeamento
- **Requisito:** `reqs/cdu-04.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-04.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU04IntegrationTest.java`
- **Análise:** Alinhamento excelente. O teste E2E valida o fluxo de interação do usuário para iniciar um processo, incluindo a confirmação e o cancelamento. O teste de integração do backend é muito completo, verificando todos os efeitos colaterais críticos: a mudança de status do processo, a criação de subprocessos para as unidades corretas, a criação de mapas de competências vazios, o registro da movimentação inicial e a geração de alertas e notificações para os diferentes tipos de unidades.
- **Status:** Concluído
- **Observações:** Cobertura muito boa. A combinação dos testes E2E e de integração garante que tanto a interface quanto a lógica de negócio complexa estão funcionando conforme o especificado.

### CDU-05: Iniciar Processo de Revisão
- **Requisito:** `reqs/cdu-05.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-05.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU05IntegrationTest.java`
- **Análise:** O alinhamento é bom, mas com uma ressalva importante. O teste E2E cobre o fluxo de início e cancelamento do processo, que é similar ao do CDU-04. O teste de integração do backend valida corretamente os casos de sucesso e falha, incluindo a regra de negócio de que uma unidade precisa ter um mapa vigente para participar de uma revisão. **No entanto, o teste de sucesso (`testIniciarProcessoRevisao_sucesso`) apenas verifica se a requisição retorna `200 OK`, sem validar se uma cópia do mapa de competências existente foi de fato criada para o novo subprocesso, conforme exigido pelo requisito.**
- **Status:** Concluído
- **Observações:** A principal lacuna é a falta de uma asserção no teste de integração para verificar se a cópia do mapa de competências foi criada durante o início do processo de revisão.

### CDU-06: Detalhar Processo
- **Requisito:** `reqs/cdu-06.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-06.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU06IntegrationTest.java`
- **Análise:** A cobertura deste caso de uso é parcial. Os testes E2E e de integração validam o fluxo básico de visualização dos detalhes de um processo e de suas unidades participantes. No entanto, funcionalidades importantes especificadas no requisito não são testadas: a visibilidade do botão "Finalizar processo" para o perfil ADMIN e, mais criticamente, a lógica de exibição dos botões de "Aceitar/Homologar em bloco" para o perfil GESTOR, que depende do estado dos subprocessos.
- **Status:** Concluído
- **Observações:** A principal lacuna é a ausência de testes para as ações condicionais disponíveis na tela de detalhes (finalizar e homologar em bloco).

### CDU-07: Detalhar Subprocesso
- **Requisito:** `reqs/cdu-07.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-07.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU07IntegrationTest.java`
- **Análise:** A cobertura deste caso de uso é fraca. O teste E2E apenas verifica se a página de detalhes do subprocesso é carregada, sem validar o conteúdo. O teste de integração do backend valida o retorno de informações básicas (nome da unidade, situação, movimentação). As principais lacunas são: a ausência de validação das informações detalhadas do responsável pela unidade, a falta de testes para a tabela completa de movimentações e, mais importante, a ausência total de testes para a lógica condicional de habilitação dos cards de "Atividades e conhecimentos" e "Mapa de competências", que depende do perfil do usuário e do estado do subprocesso.
- **Status:** Concluído
- **Observações:** Cobertura muito baixa. A lógica de negócio mais complexa e crítica (visibilidade condicional dos cards de ação) não está testada.

### CDU-08: Manter Cadastro de Atividades e Conhecimentos
- **Requisito:** `reqs/cdu-08.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-08.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU08IntegrationTest.java`
- **Análise:** A cobertura para o CRUD básico de atividades e conhecimentos é boa. Tanto o teste E2E quanto o de integração validam os fluxos de adicionar, editar e remover atividades e conhecimentos. No entanto, funcionalidades complexas e importantes especificadas nos requisitos não são testadas: a funcionalidade de "Importar atividades" de outros processos, a exibição do botão "Impacto no mapa" durante processos de revisão e a mudança automática de status do subprocesso para "Cadastro em andamento" após a primeira alteração.
- **Status:** Concluído
- **Observações:** A principal lacuna é a ausência total de testes para a funcionalidade de importação de atividades e para as variações de tela de um processo de revisão.

### CDU-09: Disponibilizar Cadastro de Atividades e Conhecimentos
- **Requisito:** `reqs/cdu-09.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-09.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU09IntegrationTest.java`
- **Análise:** Alinhamento muito bom. O teste E2E valida a regra de negócio principal (não permitir disponibilizar com atividades sem conhecimentos). O teste de integração do backend é robusto, cobrindo o fluxo de sucesso com todos os seus efeitos (mudança de status, movimentação, alerta, notificação), o caso de falha de validação e um teste de segurança para garantir que apenas o chefe da unidade correta pode executar a ação. A única lacuna identificada é a ausência de um teste para a exibição do botão "Histórico de análise" quando um cadastro é devolvido.
- **Status:** Concluído
- **Observações:** Cobertura sólida, com exceção da funcionalidade de visualização do histórico de análises.

### CDU-10: Disponibilizar Revisão do Cadastro de Atividades e Conhecimentos
- **Requisito:** `reqs/cdu-10.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-10.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU10IntegrationTest.java`
- **Análise:** O alinhamento é muito bom e segue o mesmo padrão do CDU-09. Este caso de uso é funcionalmente idêntico ao anterior, mas para processos de "Revisão". Os testes E2E e de integração cobrem adequadamente o fluxo principal, a validação de atividades incompletas e os efeitos colaterais (mudança de status para 'REVISAO_CADASTRO_DISPONIBILIZADA', movimentação, etc.). Assim como no CDU-09, a única funcionalidade não testada é a exibição do "Histórico de análise" para cadastros devolvidos.
- **Status:** Concluído
- **Observações:** Cobertura sólida, com a mesma pequena lacuna do CDU-09 referente ao histórico de análises.

### CDU-11: Visualizar Cadastro de Atividades e Conhecimentos
- **Requisito:** `reqs/cdu-11.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-11.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU11IntegrationTest.java`
- **Análise:** Alinhamento excelente. O teste E2E valida de forma eficaz o requisito de "somente leitura", verificando que diferentes perfis (ADMIN, GESTOR, outro CHEFE) podem ver os dados, mas não os controles de edição. O teste de integração do backend é muito completo, cobrindo o acesso para todos os perfis, a estrutura detalhada da resposta JSON e vários casos de borda (subprocesso não encontrado, mapa sem atividades, etc.).
- **Status:** Concluído
- **Observações:** Cobertura muito boa. A combinação dos testes garante que a visualização de dados funciona corretamente para todos os perfis e em diferentes cenários.

### CDU-12: Verificar Impactos no Mapa de Competências
- **Requisito:** `reqs/cdu-12.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-12.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU12IntegrationTest.java`
- **Análise:** Alinhamento muito forte, impulsionado por um teste de integração de alta qualidade. O teste E2E verifica os cenários básicos (nenhum impacto e exibição do modal). O teste de integração do backend é excelente e cobre exaustivamente a lógica de comparação: detecta corretamente atividades inseridas, removidas e alteradas; identifica as competências impactadas; e valida os casos de borda. Além disso, possui uma suíte de testes dedicada para o controle de acesso, garantindo que cada perfil só pode acessar a funcionalidade no estado correto do subprocesso.
- **Status:** Concluído
- **Observações:** Cobertura excelente. A lógica de negócio complexa está muito bem testada no backend.

### CDU-13: Analisar Cadastro de Atividades e Conhecimentos
- **Requisito:** `reqs/cdu-13.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-13.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU13IntegrationTest.java`
- **Análise:** Alinhamento excelente. Este é um caso de uso complexo com múltiplos atores e ações, e está muito bem coberto. O teste E2E valida todos os fluxos de ação principais: devolução (por GESTOR e ADMIN), aceite (por GESTOR) e homologação (por ADMIN). O teste de integração do backend é igualmente completo, com testes aninhados para cada fluxo que verificam detalhadamente os efeitos colaterais: mudanças de status, criação de registros de análise, movimentações corretas entre unidades e a geração de alertas/notificações. O teste de histórico de análise também está presente e correto.
- **Status:** Concluído
- **Observações:** Cobertura exemplar. Nenhuma lacuna foi identificada.

### CDU-21: Finalizar Processo
- **Requisito:** `reqs/cdu-21.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-21.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU21IntegrationTest.java`
- **Análise:** Alinhamento excelente. O teste E2E valida a visibilidade do botão "Finalizar" para o ADMIN, o fluxo de confirmação e o resultado no painel. O teste de integração do backend é muito robusto, com um setup que simula uma hierarquia de unidades. Ele valida os dois principais efeitos da finalização: a mudança de status do processo para 'FINALIZADO' e a promoção dos mapas dos subprocesses para os novos mapas vigentes das unidades. Além disso, utiliza um `ArgumentCaptor` para inspecionar o conteúdo dos e-mails de notificação, garantindo que as mensagens corretas são enviadas para cada tipo de unidade.
- **Status:** Concluído
- **Observações:** Cobertura muito boa. A única pequena lacuna é a ausência de um teste negativo para a regra de que todos os subprocessos devem estar homologados antes da finalização.

### CDU-14: Analisar Revisão de Cadastro de Atividades e Conhecimentos
- **Requisito:** `reqs/cdu-14.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-14.spec.ts`
- **Teste de Integração:** `N/A`
- **Análise:** Alinhamento muito fraco e de alto risco. O teste E2E cobre as ações de "devolver" e "aceitar" pelo GESTOR e a visibilidade dos botões para os diferentes perfis. No entanto, ele não testa o fluxo de "homologação" pelo ADMIN, que é a parte mais complexa e a principal diferença em relação ao CDU-13. A lacuna mais crítica é a ausência total de um teste de integração no backend. Isso significa que toda a lógica do servidor — incluindo a verificação de impactos que bifurca o fluxo de homologação, as mudanças de status, as movimentações e a criação de registros de análise — não está sendo testada.
- **Status:** Concluído
- **Observações:** Risco crítico. A ausência de um teste de integração para um caso de uso com lógica condicional complexa no backend é uma falha grave na cobertura de testes.

### CDU-15: Manter Mapa de Competências
- **Requisito:** `reqs/cdu-15.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-15.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU15IntegrationTest.java`
- **Análise:** O alinhamento é bom, com uma observação sobre a estratégia de implementação. O teste E2E valida corretamente o fluxo de CRUD (criar, editar, excluir) de competências individuais, conforme descrito nos requisitos. O teste de integração do backend, por outro lado, testa uma abordagem de "salvar o mapa inteiro" de uma vez, em vez de operações individuais. Embora as abordagens sejam diferentes, o resultado funcional (manter o mapa) é coberto. Ambos os testes verificam a criação, edição e remoção. O teste de integração também valida a mudança de status do subprocesso para 'MAPA_CRIADO' e a validação de segurança para estados inválidos.
- **Status:** Concluído
- **Observações:** A cobertura funcional é boa. A principal observação é a aparente divergência entre a abordagem de salvamento individual (testada no E2E) e a de salvamento em lote (testada no backend).

### CDU-16: Ajustar Mapa de Competências
- **Requisito:** `reqs/cdu-16.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-16.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU16IntegrationTest.java`
- **Análise:** A cobertura é parcial. O teste E2E é bom, cobrindo a visibilidade do botão de "Impactos", o CRUD de competências no contexto de um ajuste e a ação final de "Disponibilizar". Contudo, o teste de integração do backend é muito limitado: ele testa apenas a etapa final de submissão do mapa ajustado, validando a mudança de status para 'MAPA_DISPONIBILIZADO'. Ele não testa o processo de ajuste em si (as operações de CRUD no mapa) nem a verificação de impactos no contexto deste caso de uso.
- **Status:** Concluído
- **Observações:** A principal lacuna está no teste de integração, que não valida a lógica central de ajuste do mapa, dependendo implicitamente da cobertura do CDU-15.

### CDU-17: Disponibilizar Mapa de Competências
- **Requisito:** `reqs/cdu-17.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-17.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU17IntegrationTest.java`
- **Análise:** Alinhamento muito forte. O teste E2E foca no comportamento do modal de disponibilização, validando a obrigatoriedade dos campos e as interações do usuário. O teste de integração do backend é excelente, cobrindo não apenas o fluxo de sucesso (mudança de status, movimentação, alerta, limpeza de histórico), mas também todas as regras de negócio e de segurança: validação de permissão (apenas ADMIN), validação do estado do subprocesso, e as validações de associação (toda atividade deve ter competência e vice-versa).
- **Status:** Concluído
- **Observações:** Cobertura excelente. A combinação dos testes garante que tanto a interface quanto as regras de negócio complexas e seus efeitos colaterais estão funcionando conforme especificado.

### CDU-18: Visualizar Mapa de Competências
- **Requisito:** `reqs/cdu-18.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-18.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU18IntegrationTest.java`
- **Análise:** Alinhamento excelente. O teste E2E confirma que múltiplos perfis (ADMIN, CHEFE, SERVIDOR) conseguem navegar para a tela de visualização e que, para perfis sem permissão de edição (como SERVIDOR), os controles de ação não são exibidos. O teste de integração do backend é muito robusto, utilizando expressões JSONPath complexas para validar toda a estrutura aninhada do mapa (competências -> atividades -> conhecimentos), garantindo que a projeção dos dados para visualização está correta.
- **Status:** Concluído
- **Observações:** Cobertura exemplar. A combinação dos testes valida tanto a navegação e a renderização em modo "somente leitura" na interface quanto a integridade estrutural dos dados no backend.

### CDU-19: Validar Mapa de Competências
- **Requisito:** `reqs/cdu-19.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-19.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU19IntegrationTest.java`
- **Análise:** Alinhamento problemático devido a um teste de integração desalinhado. O teste E2E cobre bem o fluxo do CHEFE, validando as ações de "Apresentar sugestões" e "Validar", além da visualização do histórico. No entanto, o teste de integração (`CDU19IntegrationTest.java`) parece testar o caso de uso errado; ele testa as ações de "devolver" e "aceitar" a validação, que pertencem ao GESTOR/ADMIN no CDU-20, e não as ações do CHEFE deste CDU. Isso significa que a lógica de backend para "Validar mapa" e "Apresentar sugestões", incluindo todos os efeitos colaterais (mudança de status, movimentação, alertas), não está coberta por testes de integração.
- **Status:** Concluído
- **Observações:** Risco crítico. O teste de integração do backend não corresponde ao caso de uso, deixando a implementação do servidor sem cobertura.

### CDU-20: Analisar Validação de Mapa de Competências
- **Requisito:** `reqs/cdu-20.md`
- **Teste E2E:** `frontend/e2e/cdu/cdu-20.spec.ts`
- **Teste de Integração:** `backend/src/test/java/sgc/integracao/CDU20IntegrationTest.java`
- **Análise:** Alinhamento excelente. Similar ao CDU-13, este é um caso de uso complexo com múltiplos atores e ações, e está muito bem coberto. O teste E2E valida os fluxos de devolução, aceite e homologação para os perfis corretos (GESTOR e ADMIN). O teste de integração do backend é igualmente robusto, com uma estrutura de testes aninhados que valida em detalhes cada um dos fluxos, incluindo as mudanças de status, a criação de registros de análise, as movimentações entre as unidades corretas e a geração de alertas e notificações.
- **Status:** Concluído
- **Observações:** Cobertura exemplar. Nenhuma lacuna foi identificada.