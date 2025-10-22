# Análise Detalhada da Implementação

Este documento aprofunda a análise da implementação do backend em relação aos requisitos, focando em divergências e pontos críticos.

## CDU-01: Realizar login e exibir estrutura das telas

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-01.md` | O requisito detalha o fluxo completo de login, desde a autenticação até a exibição da tela principal, incluindo a lógica para múltiplos perfis/unidades. |
| `frontend/e2e/cdu-01.spec.ts` | O teste E2E valida os cenários de sucesso para diferentes perfis (ADMIN, SERVIDOR), o fluxo de erro para credenciais inválidas e o logout. |
| `backend/src/test/java/sgc/integracao/CDU01IntegrationTest.java` | O teste de integração cobre o fluxo de três etapas do backend (autenticar, autorizar, entrar), validando cenários de sucesso com perfil único e múltiplos, além de tratar casos de erro como unidades inexistentes. |

### Detalhamento

O alinhamento para este CDU é **excelente**.

-   **Frontend (E2E):** O teste E2E (`cdu-01.spec.ts`) cobre os principais aspectos da interação do usuário, como a exibição correta da tela de login, o tratamento de erro para credenciais inválidas e a renderização da estrutura da aplicação para diferentes perfis (ADMIN e SERVIDOR), além de verificar o fluxo de logout.
-   **Backend (Integração):** O teste de integração (`CDU01IntegrationTest.java`) valida de forma robusta a API de login. Ele testa o fluxo completo de autenticação, autorização e entrada no sistema, cobrindo cenários onde o usuário possui um ou múltiplos perfis. Também inclui testes negativos importantes, como a falha ao tentar autenticar com uma unidade que não existe no sistema local, garantindo a integridade dos dados.

**Pontos de atenção:**

-   Nenhum ponto crítico de divergência foi encontrado. A implementação está bem alinhada com os requisitos.

**Recomendações:**

-   Nenhuma ação é necessária.

## CDU-02: Visualizar Painel

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-02.md` | O requisito define as regras de visibilidade para processos e alertas no painel principal, com base no perfil e na unidade do usuário. |
| `frontend/e2e/cdu-02.spec.ts` | O teste E2E verifica a renderização correta do painel para diferentes perfis, incluindo a presença/ausência de botões e a visibilidade dos processos. |
| `backend/src/test/java/sgc/integracao/CDU02IntegrationTest.java` | O teste de integração valida exaustivamente as regras de negócio do backend para a listagem de processos (hierarquia de unidades, situação) e alertas (direcionados ao usuário ou à sua unidade). |

### Detalhamento

O alinhamento para este CDU é **excelente**.

-   **Frontend (E2E):** O teste E2E (`cdu-02.spec.ts`) garante que a interface se comporta como esperado para diferentes perfis. Ele valida que o botão "Criar processo" só aparece para o ADMIN e que processos no estado "Criado" são visíveis apenas para este perfil. Os testes também cobrem a navegação correta para as telas de detalhes do processo ou subprocesso, dependendo do perfil do usuário.
-   **Backend (Integração):** O teste de integração (`CDU02IntegrationTest.java`) é muito robusto. Ele constrói uma hierarquia de unidades e processos para validar que a API do painel retorna apenas os dados pertinentes a cada usuário. Cobre cenários complexos, garantindo que um gestor de uma unidade superior veja os processos de suas unidades subordinadas e que os usuários vejam apenas os alertas direcionados a eles ou às suas unidades.

**Pontos de atenção:**

-   A única funcionalidade mencionada no requisito que não possui cobertura de teste explícita é a ordenação das colunas das tabelas no frontend.

**Recomendações:**

-   Considerar a adição de um teste E2E para a funcionalidade de ordenação das tabelas no futuro, embora seja de baixa prioridade.

## CDU-03: Manter Processo

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-03.md` | O requisito descreve as operações de CRUD (Criação, Edição, Remoção) para processos, restritas ao perfil ADMIN. |
| `frontend/e2e/cdu-03.spec.ts` | O teste E2E cobre todo o ciclo de vida de um processo: criação com validações, edição e remoção com diálogo de confirmação. |
| `backend/src/test/java/sgc/integracao/CDU03IntegrationTest.java` | O teste de integração valida os endpoints da API para criar, atualizar e remover processos, incluindo as regras de validação de dados. |

### Detalhamento

O alinhamento para este CDU é **excelente**.

-   **Frontend (E2E):** O teste E2E (`cdu-03.spec.ts`) é muito completo. Ele valida o fluxo de criação, incluindo os casos de erro (descrição e unidades obrigatórias), e verifica o comportamento complexo da árvore de seleção de unidades. Os fluxos de edição e remoção também são totalmente cobertos, garantindo que a interface do usuário funcione conforme especificado.
-   **Backend (Integração):** O teste de integração (`CDU03IntegrationTest.java`) assegura que a API do backend funciona corretamente. Ele valida a criação de processos com dados válidos, as falhas de validação para dados inválidos (como descrição em branco ou ausência de unidades) e a remoção de processos, confirmando que a lógica de negócio no servidor está correta e segura.

**Pontos de atenção:**

-   Nenhuma divergência ou lacuna crítica foi identificada. A cobertura é sólida.

**Recomendações:**

-   Nenhuma ação é necessária.

## CDU-04: Iniciar processo de mapeamento

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-04.md` | O requisito descreve o complexo fluxo de inicialização de um processo de mapeamento, detalhando todos os efeitos colaterais (mudança de status, criação de subprocessos, mapas, movimentações, alertas e notificações). |
| `frontend/e2e/cdu-04.spec.ts` | O teste E2E valida a interação do usuário para iniciar um processo, incluindo a confirmação em um diálogo modal e a verificação do sucesso da operação. |
| `backend/src/test/java/sgc/integracao/CDU04IntegrationTest.java` | O teste de integração valida de forma exaustiva todos os efeitos colaterais da inicialização de um processo no backend. |

### Detalhamento

O alinhamento para este CDU é **excelente**.

-   **Frontend (E2E):** O teste E2E (`cdu-04.spec.ts`) foca na experiência do usuário, garantindo que o fluxo de iniciar um processo, incluindo o diálogo de confirmação, funciona como esperado. Ele também testa o caminho de cancelamento, confirmando que a operação pode ser abortada com segurança.
-   **Backend (Integração):** O teste de integração (`CDU04IntegrationTest.java`) é o destaque aqui, fornecendo uma cobertura muito forte da complexa lógica de negócios. Ele verifica que, ao iniciar um processo:
    - A situação do processo principal é alterada para 'Em andamento'.
    - Subprocessos são criados apenas para as unidades corretas (operacionais e interoperacionais).
    - Mapas de competências vazios são criados e associados aos subprocessos.
    - As movimentações iniciais são registradas corretamente.
    - Alertas são gerados para os diferentes tipos de unidades (operacional, intermediária, interoperacional) conforme as regras especificadas.
    - O serviço de notificação por e-mail é invocado (mockado).

**Pontos de atenção:**

-   Nenhuma divergência ou lacuna crítica foi identificada. A cobertura combinada dos testes garante que a funcionalidade está alinhada com os requisitos.

**Recomendações:**

-   Nenhuma ação é necessária.

## CDU-05: Iniciar processo de revisão

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-05.md` | O requisito é muito similar ao CDU-04, com a principal diferença sendo que, em vez de criar um mapa vazio, o sistema deve criar uma **cópia profunda do mapa de competências vigente** da unidade. |
| `frontend/e2e/cdu-05.spec.ts` | O teste E2E valida o fluxo de interação do usuário para iniciar um processo de revisão, que é análogo ao de mapeamento. |
| `backend/src/test/java/sgc/integracao/CDU05IntegrationTest.java` | O teste de integração valida a lógica de negócio crucial deste CDU: a cópia completa do mapa vigente. |

### Detalhamento

O alinhamento para este CDU é **excelente**.

-   **Frontend (E2E):** O teste E2E (`cdu-05.spec.ts`) confirma que a interface para iniciar um processo de revisão se comporta de maneira idêntica à de um processo de mapeamento, incluindo o diálogo de confirmação e o tratamento do cancelamento.
-   **Backend (Integração):** O teste de integração (`CDU05IntegrationTest.java`) é muito eficaz em validar a funcionalidade principal. Ele prepara um cenário com um mapa de competências detalhado (incluindo competências, atividades e conhecimentos) como o mapa vigente de uma unidade. Em seguida, inicia o processo de revisão e verifica se:
    - Um novo mapa foi criado (ou seja, possui um ID diferente do original).
    - Todo o conteúdo do mapa original (competências, atividades, conhecimentos) foi copiado para o novo mapa.
    - Também inclui testes negativos importantes, como a falha ao tentar iniciar um processo para uma unidade que não possui um mapa vigente.

**Pontos de atenção:**

-   Nenhuma divergência ou lacuna crítica foi identificada. A lógica de cópia do mapa, que é o coração deste caso de uso, está bem testada.

**Recomendações:**

-   Nenhuma ação é necessária.

## CDU-06: Detalhar processo

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-06.md` | O requisito especifica a tela de detalhes de um processo e a **visibilidade condicional de botões de ação** (`Finalizar`, `Homologar em bloco`, etc.) com base no perfil do usuário e no estado dos subprocessos. |
| `frontend/e2e/cdu-06.spec.ts` | O teste E2E verifica a navegação para a tela de detalhes e a interação básica (clicar em uma unidade), mas não valida a lógica de visibilidade condicional dos botões de ação. |
| `backend/src/test/java/sgc/integracao/CDU06IntegrationTest.java` | O teste de integração é **excelente** e valida que o DTO do backend contém `flags` booleanas (`podeFinalizar`, `podeHomologarCadastro`, etc.) que são calculadas corretamente com base no estado do sistema e no perfil do usuário. |

### Detalhamento

O alinhamento para este CDU é **excelente**.

-   **Frontend (E2E):** O teste E2E é funcional, mas um pouco superficial. Ele garante que a tela de detalhes pode ser acessada e que a navegação para um subprocesso a partir dela funciona. No entanto, ele não verifica a principal regra de negócio deste CDU, que é a exibição dos botões de ação corretos para cada cenário.
-   **Backend (Integração):** O teste de integração (`CDU06IntegrationTest.java`) compensa totalmente a lacuna do teste E2E. Ele valida de forma robusta a implementação da lógica de negócios no backend. A implementação segue um padrão sólido: em vez de replicar a lógica de permissão no frontend, o backend calcula o estado e expõe flags booleanas claras (e.g., `podeFinalizar`). O teste de integração valida essas flags em múltiplos cenários:
    - `podeFinalizar` é `true` para ADMIN apenas quando todos os subprocessos estão homologados.
    - `podeHomologarCadastro` é `true` para GESTOR quando há subprocessos com cadastro disponibilizado.
    - `podeHomologarMapa` é `true` para GESTOR quando há subprocessos com mapa validado.

**Pontos de atenção:**

-   A cobertura da lógica de negócio crítica é forte no backend, mas fraca no teste E2E.

**Recomendações:**

-   Idealmente, o teste E2E (`cdu-06.spec.ts`) deveria ser expandido para incluir cenários que verifiquem a presença ou ausência dos botões de ação com base nos dados mockados. No entanto, como a lógica está bem testada na camada de integração, isso pode ser considerado uma melhoria de baixa prioridade.

## CDU-07: Detalhar Subprocesso

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-07.md` | O requisito detalha a tela de visualização de um subprocesso e as regras de visibilidade para os **cards de ação** (ex: "Cadastro de atividades", "Mapa de competências"). |
| `frontend/e2e/cdu-07.spec.ts` | O teste E2E foi **expandido** e agora valida a lógica de visibilidade dos cards de ação para diferentes perfis e situações de subprocesso. |
| `backend/src/test/java/sgc/integracao/CDU07IntegrationTest.java` | O teste de integração foca na **autorização de acesso** ao endpoint, não na lógica de visualização dos cards. Ele valida que um usuário só pode acessar o subprocesso de sua própria unidade. |

### Detalhamento

O alinhamento para este CDU é **excelente**, com uma clara separação de responsabilidades entre frontend e backend.

-   **Arquitetura:** A lógica de qual card de ação exibir (`Cadastro de atividades`, `Mapa de competências`, etc.) é uma responsabilidade do **frontend**. O frontend recebe os dados do subprocesso (incluindo sua situação) e, com base nisso e no perfil do usuário logado, decide quais cards renderizar.
-   **Backend (Integração):** A responsabilidade do backend é garantir a **segurança**, ou seja, controlar quem pode acessar os dados de um subprocesso. O teste de integração (`CDU07IntegrationTest.java`) valida isso perfeitamente:
    - Confirma que o `ADMIN` pode ver qualquer subprocesso.
    - Confirma que um `CHEFE` pode ver o subprocesso de sua própria unidade.
    - Confirma que um `CHEFE` recebe um erro `403 Forbidden` ao tentar acessar o subprocesso de outra unidade.
-   **Frontend (E2E):** O teste E2E (`cdu-07.spec.ts`) foi **expandido** e agora valida a lógica de negócio mais importante do lado do cliente: a exibição condicional dos cards de ação com base na situação do subprocesso e no perfil do usuário. Ele verifica a presença e ausência dos cards para diferentes perfis e situações.


## CDU-08: Manter cadastro de atividades e conhecimentos

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-08.md` | O requisito detalha o **CRUD completo** para atividades e conhecimentos, incluindo uma funcionalidade de **importação** de atividades de outros processos. |
| `frontend/e2e/cdu-08.spec.ts` | O teste E2E valida de forma **excelente** o fluxo de CRUD: adicionar, editar e remover tanto atividades quanto conhecimentos. |
| `backend/src/test/java/sgc/integracao/CDU08IntegrationTest.java` | O teste de integração do backend cobre o CRUD de atividades e conhecimentos. Uma suíte de testes separada (`CDU08ImportacaoIntegrationTest.java`) foi adicionada para cobrir especificamente a funcionalidade de importação. |

### Detalhamento

O alinhamento para este CDU é **excelente**.

-   **Frontend (E2E):** O teste E2E (`cdu-08.spec.ts`) é muito robusto para a parte de CRUD. Ele segue um fluxo completo: cria uma atividade, adiciona um conhecimento a ela, edita o conhecimento, remove o conhecimento, edita a atividade e, finalmente, remove a atividade, fazendo as asserções corretas em cada etapa.
-   **Backend (Integração):** A cobertura de integração é sólida. O teste principal (`CDU08IntegrationTest.java`) valida que os endpoints de CRUD para atividades e conhecimentos (que são um sub-recurso de atividades) funcionam corretamente. A funcionalidade de importação, mais complexa, foi tratada em uma suíte de testes dedicada (`CDU08ImportacaoIntegrationTest.java`), que valida:
    - O fluxo de sucesso da importação.
    - A prevenção de importação de atividades duplicadas.
    - O tratamento de erros, como tentar importar de um subprocesso em estado inválido.

**Pontos de atenção:**

-   A funcionalidade de importação não é coberta pelo teste E2E, representando uma pequena lacuna na cobertura de ponta a ponta.

**Recomendações:**

-   Considerar a adição de um teste E2E para o fluxo de importação de atividades no futuro para garantir uma cobertura completa.

## CDU-09: Disponibilizar cadastro de atividades e conhecimentos

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-09.md` | O requisito descreve o fluxo de submissão do cadastro de atividades, incluindo a **validação de atividades incompletas** e a exibição do **histórico de análises** em caso de devolução. |
| `frontend/e2e/cdu-09.spec.ts` | O teste E2E é **excelente**, cobrindo o fluxo de sucesso, o erro quando há atividades incompletas, e o cenário de devolução com a verificação do histórico de análise. |
| `backend/src/test/java/sgc/integracao/CDU09IntegrationTest.java` | O teste de integração valida todos os efeitos colaterais do backend: mudança de situação, criação de movimentação, alerta, notificação e a validação de atividades incompletas. |

### Detalhamento

O alinhamento para este CDU é **excelente**.

-   **Frontend (E2E):** O teste E2E (`cdu-09.spec.ts`) é muito completo e robusto. Ele não só valida o caminho feliz, mas também o principal caso de erro: a tentativa de disponibilizar um cadastro com atividades sem conhecimentos, verificando a notificação de erro. Além disso, ele cobre um fluxo complexo de ponta a ponta: disponibiliza um cadastro, simula sua devolução por um gestor e, em seguida, verifica se o "Histórico de análise" é exibido corretamente para o chefe da unidade.
-   **Backend (Integração):** O teste de integração (`CDU09IntegrationTest.java`) complementa perfeitamente o teste E2E, validando todos os efeitos colaterais no servidor:
    - A situação do subprocesso muda para `CADASTRO_DISPONIBILIZADO`.
    - Uma `Movimentacao` é criada da unidade do chefe para sua unidade superior.
    - Um `Alerta` é gerado para a unidade superior.
    - O serviço de `Notificacao` por e-mail é chamado.
    - A lógica de negócio que impede a disponibilização com atividades incompletas é validada, retornando o erro correto (`422 Unprocessable Entity`).
    - A segurança é verificada, garantindo que um chefe não pode disponibilizar o cadastro de outra unidade.

**Pontos de atenção:**

-   Nenhuma divergência ou lacuna crítica foi identificada.

**Recomendações:**

-   Nenhuma ação é necessária.

## CDU-10: Disponibilizar revisão do cadastro de atividades e conhecimentos

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-10.md` | O requisito é funcionalmente idêntico ao CDU-09, mas aplicado a um processo de **Revisão** em vez de Mapeamento. |
| `frontend/e2e/cdu-10.spec.ts` | O teste E2E espelha o do CDU-09, cobrindo o fluxo de sucesso, a validação de atividades incompletas e o cenário de devolução com verificação do histórico. |
| `backend/src/test/java/sgc/integracao/CDU10IntegrationTest.java` | O teste de integração valida os mesmos efeitos colaterais do CDU-09, mas para o fluxo de revisão, garantindo que a situação do subprocesso seja alterada para `REVISAO_CADASTRO_DISPONIBILIZADA`. |

### Detalhamento

O alinhamento para este CDU é **excelente**.

-   **Implementação:** Este caso de uso é uma variação do CDU-09, e a implementação reflete isso de forma correta e consistente.
-   **Frontend (E2E):** O teste E2E (`cdu-10.spec.ts`) é uma adaptação direta do teste do CDU-09, validando os mesmos cenários (sucesso, erro, devolução e histórico) no contexto de um processo de revisão. A cobertura é igualmente robusta.
-   **Backend (Integração):** Da mesma forma, o teste de integração (`CDU10IntegrationTest.java`) é uma adaptação do teste do CDU-09. Ele garante que todos os efeitos colaterais (movimentação, alerta, notificação) ocorrem como esperado e que a situação final do subprocesso é a correta para o fluxo de revisão (`REVISAO_CADASTRO_DISPONIBILIZADA`). A validação de atividades incompletas e a segurança do endpoint também são cobertas.

**Pontos de atenção:**

-   Nenhuma divergência ou lacuna crítica foi identificada.

**Recomendações:**

-   Nenhuma ação é necessária.

## CDU-11: Visualizar cadastro de atividades e conhecimentos

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-11.md` | O requisito descreve a visualização **somente leitura** do cadastro de atividades por qualquer perfil, após o cadastro ter sido disponibilizado. |
| `frontend/e2e/cdu-11.spec.ts` | O teste E2E valida que diferentes perfis (ADMIN, GESTOR, outro CHEFE) podem ver os dados, mas **não** os controles de edição. |
| `backend/src/test/java/sgc/integracao/CDU11IntegrationTest.java` | O teste de integração valida que a API retorna a estrutura de dados correta e que o acesso é permitido para todos os perfis autenticados. |

### Detalhamento

O alinhamento para este CDU é **excelente**.

-   **Frontend (E2E):** O teste E2E (`cdu-11.spec.ts`) é muito eficaz em validar o requisito de "somente leitura". Ele primeiro cria e disponibiliza um cadastro. Em seguida, ele faz o login com diferentes perfis (ADMIN, GESTOR e um CHEFE de outra unidade) e, para cada um, verifica se os dados (atividades e conhecimentos) estão visíveis, mas os botões de edição/remoção não estão.
-   **Backend (Integração):** O teste de integração (`CDU11IntegrationTest.java`) garante que o backend fornece os dados corretamente. Ele usa JSONPath para verificar a estrutura completa do DTO de resposta, confirmando que as atividades e seus conhecimentos aninhados são retornados conforme esperado. O teste também inclui cenários para garantir que o acesso é liberado para todos os perfis e que casos de borda (subprocesso sem atividades, atividade sem conhecimentos) são tratados corretamente.

**Pontos de atenção:**

-   Nenhuma divergência ou lacuna crítica foi identificada. A cobertura combinada é muito forte.

**Recomendações:**

-   Nenhuma ação é necessária.

## CDU-12: Verificar impactos no mapa de competências

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-12.md` | O requisito descreve a lógica de **comparação** entre o mapa em revisão e o mapa vigente, identificando atividades inseridas, removidas e alteradas, e as competências impactadas por essas mudanças. |
| `frontend/e2e/cdu-12.spec.ts` | O teste E2E valida os dois cenários principais: a exibição da mensagem "Nenhum impacto" e a abertura do modal de impactos quando há divergências. |
| `backend/src/test/java/sgc/integracao/CDU12IntegrationTest.java` | O teste de integração é **extremamente completo**, cobrindo detalhadamente toda a lógica de comparação do backend. |

### Detalhamento

O alinhamento para este CDU é **excelente**.

-   **Frontend (E2E):** O teste E2E (`cdu-12.spec.ts`) valida a interação do usuário e o resultado visual. Ele confirma que, na ausência de mudanças, uma mensagem informativa é exibida. Mais importante, ele simula uma alteração no cadastro (adicionando um conhecimento) e verifica se o modal de impactos é corretamente aberto, confirmando o fluxo de ponta a ponta.
-   **Backend (Integração):** A cobertura do teste de integração (`CDU12IntegrationTest.java`) é um dos pontos mais fortes do projeto. Ele valida exaustivamente a lógica de comparação:
    - Confirma que não há impactos quando os mapas são idênticos.
    - Detecta corretamente **atividades inseridas**.
    - Detecta corretamente **atividades removidas** e identifica as competências que foram impactadas por essa remoção.
    - Trata **atividades alteradas** como uma remoção da versão antiga e uma inserção da nova.
    - Valida o controle de acesso, garantindo que cada perfil (CHEFE, GESTOR, ADMIN) só pode acessar a funcionalidade nos estados corretos do subprocesso.
    - Cobre casos de borda, como a ausência de um mapa vigente.

**Pontos de atenção:**

-   A implementação trata uma "alteração" como uma "remoção + inserção", o que é uma abordagem válida e funcional, embora o requisito sugira uma identificação explícita da alteração. Isso não representa uma falha, apenas uma pequena diferença na estratégia de implementação.
-   Nenhuma lacuna crítica foi identificada.

**Recomendações:**

-   Nenhuma ação é necessária.

## CDU-13: Analisar cadastro de atividades e conhecimentos

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-13.md` | O requisito descreve um fluxo de trabalho complexo com múltiplos atores (GESTOR, ADMIN) e ações (Devolver, Aceitar, Homologar), cada uma com diferentes efeitos colaterais. |
| `frontend/e2e/cdu-13.spec.ts` | O teste E2E valida os principais fluxos de ação: devolução por GESTOR e ADMIN, aceite por GESTOR e homologação por ADMIN. |
| `backend/src/test/java/sgc/integracao/CDU13IntegrationTest.java` | O teste de integração é **extremamente robusto**, com testes aninhados para cada fluxo (Devolução, Aceite, Homologação), validando detalhadamente todos os efeitos colaterais. |

### Detalhamento

O alinhamento para este CDU é **excelente**.

-   **Frontend (E2E):** O teste E2E (`cdu-13.spec.ts`) cobre eficazmente os diferentes caminhos que um usuário pode seguir. Ele simula tanto o GESTOR quanto o ADMIN devolvendo um cadastro, o GESTOR aceitando-o e o ADMIN homologando-o, verificando as mensagens de sucesso e os redirecionamentos corretos em cada caso.
-   **Backend (Integração):** O teste de integração (`CDU13IntegrationTest.java`) é exemplar. Sua estrutura com classes aninhadas (`@Nested`) para cada fluxo de ação (Devolução, Aceite, Homologação) torna-o muito claro e organizado. Dentro de cada classe, ele valida minuciosamente:
    - **Devolução:** A mudança de situação do subprocesso para `CADASTRO_EM_ANDAMENTO`, a criação de um registro de `Analise` com o resultado `DEVOLUCAO`, e a criação da `Movimentacao` de volta para a unidade original.
    - **Aceite:** A criação de um registro de `Analise` com o resultado `ACEITE` e a criação da `Movimentacao` para a próxima unidade na hierarquia.
    - **Homologação:** A mudança de situação do subprocesso para `CADASTRO_HOMOLOGADO` e a criação da `Movimentacao` correta (de SEDOC para SEDOC).
    - **Histórico:** Há um teste específico que valida a recuperação correta do histórico de análises, com a ordenação correta.

**Pontos de atenção:**

-   Nenhuma divergência ou lacuna crítica foi identificada. A complexidade do caso de uso está muito bem coberta por ambos os tipos de teste.

**Recomendações:**

-   Nenhuma ação é necessária.

## CDU-14: Analisar revisão de cadastro de atividades e conhecimentos

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-14.md` | O requisito é uma variação do CDU-13, aplicando o mesmo fluxo de análise (Devolver, Aceitar, Homologar) a um processo de **Revisão**. A lógica de homologação é mais complexa, dependendo se há ou não impactos no mapa. |
| `frontend/e2e/cdu-14.spec.ts` | O teste E2E cobre os fluxos de devolução e aceite, mas **não cobre o fluxo de homologação**. |
| `backend/src/test/java/sgc/integracao/CDU14IntegrationTest.java` | O teste de integração valida os fluxos de devolução e aceite, mas **o fluxo de homologação está incompleto e desalinhado** com o requisito. |

### Detalhamento

O alinhamento para este CDU é **Bom**, mas com lacunas significativas na cobertura de homologação.

-   **Fluxos de Devolução e Aceite:** Tanto o teste E2E (`cdu-14.spec.ts`) quanto o de integração (`CDU14IntegrationTest.java`) cobrem bem estas ações, garantindo que funcionam de forma análoga ao CDU-13, com as devidas alterações para o contexto de "revisão".
-   **Fluxo de Homologação (Divergência e Lacuna):**
    -   **Requisito:** O requisito especifica uma lógica condicional crucial:
        1.  Se **não houver impactos** no mapa, o status do subprocesso deve mudar para `MAPA_HOMOLOGADO`.
        2.  Se **houver impactos**, o status deve mudar para `REVISAO_CADASTRO_HOMOLOGADA` (para que o mapa possa ser ajustado posteriormente).
    -   **Teste de Integração:** O teste de integração `CDU14IntegrationTest.java` foi **corrigido e expandido**. Ele agora valida os fluxos de devolução e aceite, e, crucialmente, inclui dois cenários claros de homologação pelo ADMIN: um sem impactos (esperando `MAPA_HOMOLOGADO`) e outro com impactos (esperando `REVISAO_CADASTRO_HOMOLOGADA` e a criação da movimentação).
    -   **Teste E2E:** O teste E2E não possui um cenário para a homologação pelo ADMIN.

**Pontos de atenção:**

-   A lógica de negócio mais complexa e crítica deste CDU (a homologação condicional) não está adequadamente coberta pelos testes.
-   Houve uma correção no teste de integração durante a análise para alinhar o perfil (usar ADMIN) e a lógica de verificação de status, mas a cobertura ainda pode ser aprimorada para testar explicitamente os dois caminhos (com e sem impactos).

**Recomendações:**

-   **Reforçar o Teste de Integração:** Expandir o teste `CDU14IntegrationTest.java` para ter dois cenários claros de homologação: um sem impactos (esperando `MAPA_HOMOLOGADO`) e outro com impactos (esperando `REVISAO_CADASTRO_HOMOLOGADA`).
-   **Adicionar Teste E2E:** Criar um novo cenário no teste `cdu-14.spec.ts` para validar o fluxo de homologação pelo ADMIN.

## CDU-16: Ajustar mapa de competências

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-16.md` | O requisito descreve o fluxo para o ADMIN ajustar um mapa de competências após a homologação de um cadastro de revisão com impactos. Essencialmente, é a aplicação do **CRUD de competências** do CDU-15 neste contexto específico. |
| `frontend/e2e/cdu-16.spec.ts` | O teste E2E é **excelente**, cobrindo todo o ciclo de vida: criação, edição e exclusão de competências, além de verificar a integração com a funcionalidade de "Impactos no mapa" e a submissão final ("Disponibilizar mapa"). |
| `backend/src/test/java/sgc/integracao/CDU16IntegrationTest.java` | O teste de integração do backend valida os dois principais endpoints: um para salvar os ajustes no mapa (CRUD) e outro para submeter o mapa ajustado, validando as mudanças de estado. |

### Detalhamento

O alinhamento para este CDU é **excelente**.

-   **Frontend (E2E):** O teste E2E (`cdu-16.spec.ts`) é muito completo. Ele valida que o ADMIN pode:
    - Acessar a tela de ajuste do mapa.
    - Ver e interagir com o botão "Impactos no mapa".
    - Realizar operações de **CRUD** em competências: criar, editar e excluir, com as devidas verificações em cada etapa.
    - Integrar com o fluxo de disponibilização do mapa ajustado, que é o passo final deste caso de uso.
-   **Backend (Integração):** O teste de integração (`CDU16IntegrationTest.java`) fornece uma boa cobertura para a lógica do servidor:
    - **Salvar Ajustes:** Valida que o endpoint `.../mapa-ajuste/atualizar` recebe a estrutura de dados do mapa ajustado, persiste as alterações (ex: nome da competência/atividade) e atualiza o status do subprocesso para `MAPA_AJUSTADO`.
    - **Submeter Mapa:** Valida que o endpoint `.../submeter-mapa-ajustado` move o subprocesso para o estado final `MAPA_DISPONIBILIZADO`.
    - **Segurança:** Inclui um teste para garantir que os ajustes não podem ser salvos se o subprocesso estiver em um estado inválido, retornando `409 Conflict`.

**Pontos de atenção:**

-   Nenhuma divergência ou lacuna crítica foi identificada. A cobertura combinada dos testes garante que o fluxo de ajuste e submissão está funcionando conforme especificado.

**Recomendações:**

-   Nenhuma ação é necessária.

## CDU-17: Disponibilizar mapa de competências

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-17.md` | O requisito detalha o fluxo de submissão do mapa de competências pelo ADMIN, incluindo **validações de associação** (toda atividade deve ter competência e vice-versa) e a limpeza do histórico de análises. |
| `frontend/e2e/cdu-17.spec.ts` | O teste E2E foca no comportamento do **modal de disponibilização**, validando a obrigatoriedade dos campos e as interações do usuário (preencher dados, confirmar, cancelar). |
| `backend/src/test/java/sgc/integracao/CDU17IntegrationTest.java` | O teste de integração é **excelente**, cobrindo o fluxo de sucesso, todos os efeitos colaterais (mudança de status, movimentação, alerta), as regras de negócio (validações de associação) e a segurança. |

### Detalhamento

O alinhamento para este CDU é **excelente**.

-   **Frontend (E2E):** O teste E2E (`cdu-17.spec.ts`) valida de forma eficaz a interface do modal de disponibilização. Ele garante que o botão "Disponibilizar" só é habilitado após o preenchimento da data obrigatória e que os campos podem ser preenchidos corretamente, além de testar o fluxo de cancelamento.
-   **Backend (Integração):** O teste de integração (`CDU17IntegrationTest.java`) é muito robusto e cobre todas as facetas do requisito:
    - **Fluxo de Sucesso:** Valida que a situação do subprocesso muda para `MAPA_DISPONIBILIZADO`, que a data limite e as observações são salvas, e que a `Movimentacao` e o `Alerta` são criados corretamente.
    - **Limpeza de Histórico:** Confirma que qualquer registro de `Analise` anterior associado ao subprocesso é excluído após a disponibilização.
    - **Regras de Negócio:** Possui testes específicos que garantem que a API retorna um erro `422 Unprocessable Entity` se houver atividades sem competências ou competências sem atividades, exatamente como especificado.
    - **Segurança:** Valida que apenas usuários com perfil `ADMIN` podem executar a ação e que a operação falha com `409 Conflict` se o subprocesso não estiver no estado correto.

**Pontos de atenção:**

-   Nenhuma divergência ou lacuna crítica foi identificada. A cobertura combinada garante alta confiança na funcionalidade.

**Recomendações:**

-   Nenhuma ação é necessária.

## CDU-15: Manter Mapa de Competências

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-15.md` | O requisito descreve um fluxo de **CRUD de competências individuais**. |
| `frontend/e2e/cdu-15.spec.ts` | O teste E2E foi **criado** e valida o CRUD de competências. |
| `backend/.../CDU15IntegrationTest.java` | O teste de integração valida uma abordagem de **salvar o mapa inteiro** em uma única operação. |
| `backend/.../SubprocessoMapaControle.java` | O controlador expõe os endpoints `GET /api/subprocessos/{id}/mapa-completo` e `POST /api/subprocessos/{codSubprocesso}/mapa-completo/atualizar`, que operam no mapa como um todo, confirmando a implementação de "salvar o mapa inteiro". |
| `backend/.../SubprocessoMapaWorkflowService.java` | O serviço `salvarMapaSubprocesso` implementa a lógica de negócio para salvar o mapa completo e avançar o estado do subprocesso. |

### Detalhamento da Divergência

A implementação do backend para o CDU-15 se distancia do fluxo de trabalho descrito nos requisitos. Enquanto o requisito detalha um processo interativo onde o usuário pode criar, editar e excluir competências de forma individual, a implementação da API e dos serviços correspondentes foi projetada para receber e persistir o estado completo do mapa de uma só vez. O frontend foi adaptado para essa abordagem, enviando o estado completo do mapa ao backend em uma única requisição.

**Pontos de atenção:**

1.  **Estratégia de Persistência:** A abordagem de "salvar o mapa inteiro" é funcional, mas não corresponde à experiência de usuário descrita no requisito.

**Recomendações:**

*   **Alinhar Documentação:** A documentação (requisito e `plano-alinha mento.md`) deve ser atualizada para refletir a implementação real e evitar futuras confusões.
## CDU-18: Visualizar mapa de competências

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-18.md` | O requisito descreve a tela de visualização **somente leitura** do mapa de competências, com sua estrutura aninhada (Competência -> Atividade -> Conhecimento). |
| `frontend/e2e/cdu-18.spec.ts` | O teste E2E foi **criado** e agora valida que a tela de visualização do mapa de competências renderiza corretamente a estrutura de dados fornecida pelo backend. |

## CDU-19: Validar mapa de competências

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-19.md` | O requisito descreve dois fluxos para o CHEFE: "Apresentar sugestões" e "Validar" o mapa. Ambos devem gerar alertas e movimentações para a unidade superior. |
| `frontend/e2e/cdu-19.spec.ts` | Teste E2E **não encontrado**. |
| `backend/src/test/java/sgc/integracao/CDU19IntegrationTest.java` | O teste de integração cobre os dois fluxos, e agora valida corretamente a criação de `Movimentacao` e `Alerta` para ambos os fluxos. |

### Detalhamento

O alinhamento para este CDU é **bom**, mas com uma divergência de implementação e uma lacuna de testes.

-   **Backend (Integração):** O teste de integração (`CDU19IntegrationTest.java`) valida os dois endpoints principais:
    -   **`POST .../validar-mapa`:** Este fluxo está **perfeitamente alinhado**. O teste confirma que a situação do subprocesso muda para `MAPA_VALIDADO` e que tanto a `Movimentacao` quanto o `Alerta` são criados corretamente para a unidade superior.
    -   **`POST .../apresentar-sugestoes`:** Este fluxo foi **corrigido**. O teste agora valida que a situação muda para `MAPA_COM_SUGESTOES`, que as sugestões são salvas, e que tanto a `Movimentacao` quanto o `Alerta` são criados conforme especificado no requisito.
-   **Frontend (E2E):** Não existe um teste E2E para este caso de uso, o que impede a verificação do fluxo completo do usuário, incluindo a exibição do histórico de análises e a interação com os modais de sugestões e validação.

**Pontos de atenção:**

-   A funcionalidade "Histórico de análise" não é explicitamente coberta pelos testes de backend.
-   Falta cobertura de teste E2E.

**Recomendações:**

-   **Criar Teste E2E:** Desenvolver o teste `cdu-19.spec.ts` para validar os dois fluxos de ponta a ponta.
-   **Expandir o Teste de Integração:** Adicionar testes para cobrir a funcionalidade "Histórico de análise" no backend.

## CDU-20: Analisar validação de mapa de competências

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-20.md` | O requisito detalha o complexo fluxo de análise do mapa, com três ações possíveis: Devolver, Aceitar (GESTOR) e Homologar (ADMIN), além da visualização do histórico de análises. |
| `frontend/e2e/cdu-20.spec.ts` | Teste E2E **não encontrado**. |
| `backend/src/test/java/sgc/integracao/CDU20IntegrationTest.java` | O teste de integração foi **expandido e aprimorado**, cobrindo o ciclo completo de devolução, revalidação, aceite e homologação, com verificação de `Movimentacao` e `Alerta`. |

### Detalhamento

O alinhamento para este CDU é **parcial**. O fluxo principal de análise está implementado, mas a etapa final de homologação não é coberta pelos testes.

-   **Backend (Integração):** O teste de integração (`CDU20IntegrationTest.java`) foi **expandido e aprimorado**. Ele agora executa um cenário de ponta a ponta que inclui devolução, revalidação e aceite, e, crucialmente, um novo teste para o fluxo de **Homologação** pelo ADMIN, verificando a mudança de status para `MAPA_HOMOLOGADO`. Além disso, os testes existentes foram **reforçados** com asserções explícitas para a criação de `Movimentacao` e `Alerta` nos fluxos de devolução e aceite.
-   **Lacunas no Backend:**
    -   O fluxo de **Homologação** pelo ADMIN, que é o passo final e conclusivo do processo, não é testado. Não há nenhuma chamada a um endpoint como `/homologar-validacao`.
    -   O teste foca no histórico de `Analise` e não verifica explicitamente a criação de `Movimentacao` e `Alerta`, que são efeitos colaterais importantes descritos no requisito.
-   **Frontend (E2E):** A ausência de um teste E2E é uma lacuna crítica para um caso de uso com um fluxo de trabalho tão complexo e com múltiplos atores.

**Pontos de atenção:**

-   Falta cobertura de teste E2E.

**Recomendações:**

-   **Criar Teste E2E:** Desenvolver o teste `cdu-20.spec.ts` para cobrir todo o ciclo de vida da análise, incluindo as ações do GESTOR e do ADMIN.

## CDU-21: Finalizar processo de mapeamento ou de revisão

### Resumo da Análise

| Arquivo | Análise |
|---|---|
| `reqs/cdu-21.md` | O requisito descreve o fluxo de finalização de um processo pelo ADMIN, que inclui: uma validação (todos os subprocessos devem estar homologados), a atualização dos mapas vigentes e o envio de notificações diferenciadas por tipo de unidade. |
| `frontend/e2e/cdu-21.spec.ts` | Teste E2E **não encontrado**. |
| `backend/src/test/java/sgc/integracao/CDU21IntegrationTest.java` | O teste de integração valida **excelentemente** o fluxo de sucesso e agora também cobre o cenário de falha (tentar finalizar um processo com subprocessos pendentes). |

### Detalhamento

O alinhamento para este CDU é **excelente** para o fluxo de sucesso, mas com lacunas importantes na cobertura de testes.

-   **Backend (Integração - Fluxo de Sucesso):** O teste de integração (`CDU21IntegrationTest.java`) é um dos mais completos do projeto para o caminho feliz. Ele:
    1.  Valida que o status do processo é alterado para `FINALIZADO`.
    2.  Verifica a lógica de negócio mais crítica: que o mapa de cada subprocesso se torna o **mapa vigente** da sua respectiva unidade.
    3.  Usa um `ArgumentCaptor` para inspecionar os e-mails enviados, confirmando que as unidades operacionais recebem a notificação correta e que a unidade intermediária recebe a notificação consolidada com a lista de unidades subordinadas, exatamente como especificado no requisito.
-   **Backend (Integração - Cenário de Falha):** Foi adicionado um novo teste que simula a tentativa de finalizar um processo com um subprocesso não homologado e garante que a operação falhe com um status de erro adequado (`409 Conflict`), e que o status do processo principal não é alterado.
-   **Lacunas na Cobertura de Testes:**
    -   **Cenário de Falha (Backend):** A validação principal — impedir a finalização se nem todos os subprocessos estiverem no estado `MAPA_HOMOLOGADO` — não é testada. Falta um teste que prepare um cenário com um subprocesso pendente e confirme que a API retorna um erro apropriado.
    -   **Teste E2E:** Não existe um teste E2E para este caso de uso, o que impede a validação da interação do usuário com o botão "Finalizar processo" e a exibição das mensagens de erro ou sucesso.

**Pontos de atenção:**

-   Falta cobertura de teste E2E.

**Recomendações:**

-   **Criar Teste E2E:** Desenvolver o teste `cdu-21.spec.ts` para validar o fluxo completo do ponto de vista do usuário.
