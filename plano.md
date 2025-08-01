Sistema de Gestão de Competências

# Plano de Desenvolvimento do Protótipo SGC

Este documento detalha o plano de desenvolvimento para o protótipo do Sistema de Gestão de Competências (SGC), com base na análise das especificações completas (`cdus-hoje.md`) e do estado atual do protótipo.

## 1. Análise da Situação Atual do Protótipo

O protótipo do SGC evoluiu significativamente, cobrindo os fluxos principais de login, navegação e visualização de detalhes de processos e unidades. As principais conquistas e o status de implementação dos CDUs são:

*   **CDU-001 - Realizar login e exibir estrutura das telas:** **Largamente implementado.** O login com seleção de par "perfil-unidade" e a exibição na Navbar estão funcionais. A estrutura básica das telas (Navbar, Conteúdo, Rodapé) está presente.
*   **CDU-002 - Visualizar Painel:** **Parcialmente implementado.**
    *   Seções "Processos Ativos" e "Alertas" estão presentes.
    *   Tabela de processos: colunas básicas e ordenação implementadas. O filtro de processos por unidade do usuário/subordinadas e a lógica de agregação de "Unidades Participantes" não estão totalmente implementados.
    *   Tabela de alertas: colunas e formatação de data implementadas. A gestão de alertas (marcar como lido, negrito) não está implementada.
*   **CDU-003 - Manter processo:** **Basicamente não implementado.** A tela de cadastro (`CadProcesso.vue`) existe, mas a lógica completa de criação, edição e remoção (incluindo a complexa seleção de unidades com checkboxes hierárquicos e validações) não está implementada.
*   **CDU-004 - Iniciar processo de mapeamento:** **Basicamente não implementado.** A mudança de status do processo para "Em andamento" está implementada, mas a criação de processos de unidade internos, envio de notificações e criação de alertas não estão.
*   **CDU-005 - Detalhar processo:** **Largamente implementado.**
    *   Para perfis CHEFE/SERVIDOR (`DetalhesUnidadeProcesso.vue`): exibição detalhada do responsável e cards dinâmicos/clicáveis baseados no tipo de processo (Mapeamento/Revisão e Diagnóstico) estão implementados. O status "Não disponibilizado" para alguns cards é um placeholder.
    *   Para perfis ADMIN/GESTOR (`DetalhesProcesso.vue`): exibição de dados do processo, botão "Finalizar processo" (ADMIN) e subárvore de unidades participantes com situação/data limite estão implementados.
*   **CDU-006 - Manter atividades e conhecimentos:** **Basicamente não implementado.** A tela (`CadAtividades.vue`) existe, mas a funcionalidade de CRUD de atividades/conhecimentos, importação e disponibilização não está implementada.
*   **CDU-007 - Disponibilizar cadastro para validação:** **Não implementado.**
*   **CDU-008 - Validar cadastro:** **Não implementado.**
*   **CDU-010 - Visualizar mapa de competências:** **Basicamente não implementado.** A tela (`CadMapaVisualizacao.vue`) existe, mas a exibição do mapa em si não está implementada.
*   **CDU-011 - Manter mapa de competências:** **Não implementado.**
*   **CDU-012 - Disponibilizar mapa de competências:** **Não implementado.**
*   **CDU-013 - Validar mapa de competências (CHEFE):** **Não implementado.**
*   **CDU-014 - Validar mapa de competências (GESTOR):** **Não implementado.**
*   **CDU-015 - Consultar histórico de processos:** **Basicamente não implementado.** A tela (`HistoricoProcessos.vue`) existe, mas a listagem de processos finalizados e o botão "Mapa" não estão implementados.
*   **CDU-016 - Manter atribuição temporária:** **Basicamente não implementado.** O botão "Criar atribuição" está presente, mas a funcionalidade de cadastro/edição/remoção não está implementada.
*   **CDU-017 a CDU-026:** **Não implementados.**

## 2. Próximos Passos Detalhados

O plano a seguir prioriza a completude das funcionalidades existentes e a implementação de requisitos pendentes, seguindo uma ordem lógica de dependências e complexidade.

### 2.1. Refinamento da Core Functionality e Integração de Dados

*   **2.1.1. Completude dos Cards em `DetalhesUnidadeProcesso.vue`:**
    *   **Objetivo:** Tornar os cards de "Atividades e Conhecimentos" e "Diagnóstico" totalmente funcionais e com status dinâmicos, conforme as situações definidas.
    *   **Ações:**
        *   **Mock de Dados:** Criar mocks de dados para `atividadesConhecimentosStatus.json` e `diagnosticoStatus.json` que armazenem o status por unidade e por processo.
        *   **Stores:** Criar `useAtividadesConhecimentosStatusStore` e `useDiagnosticoStatusStore` para gerenciar esses dados.
        *   **`DetalhesUnidadeProcesso.vue`:** Importar as novas stores, usar seus getters para determinar o status dos cards e ajustar a exibição e navegabilidade.

*   **2.1.2. Filtragem e Agregação de Processos no Painel (`CDU-002`):**
    *   **Objetivo:** Implementar a lógica de filtragem de processos por unidade do usuário/subordinadas e a agregação de "Unidades Participantes" na tabela do Painel.
    *   **Ações:**
        *   **`Painel.vue`:** Refinar a `computed` `processosFiltrados` para aplicar o filtro de unidades. Implementar a lógica para a coluna "Unidades Participantes" exibir apenas as unidades de nível mais alto que satisfaçam a condição de agregação.

*   **2.1.3. Gestão de Alertas no Painel (`CDU-002`):**
    *   **Objetivo:** Implementar a funcionalidade de marcar alertas como lidos e exibi-los em negrito.
    *   **Ações:**
        *   **Mock de Dados:** Adicionar um campo `lido` aos alertas em `alertas.json`.
        *   **Store:** Adicionar uma ação na `useAlertasStore` para marcar um alerta como lido.
        *   **`Painel.vue`:** Adicionar lógica para aplicar a classe CSS de negrito e chamar a ação de marcar como lido ao visualizar o alerta.

### 2.2. Implementação de Funcionalidades de Manutenção (CRUD)

*   **2.2.1. Manter Processo (`CDU-003`):**
    *   **Objetivo:** Implementar o CRUD completo de processos, incluindo a complexa seleção de unidades.
    *   **Ações:**
        *   **`CadProcesso.vue`:**
            *   Implementar os campos de formulário e as validações.
            *   Desenvolver a árvore de unidades com checkboxes e a lógica de seleção/desseleção hierárquica (seleção automática de filhos/pais, estado intermediário).
            *   Implementar a lógica para os botões "Salvar", "Remover" e "Iniciar processo".
            *   Integrar com `useProcessosStore` para adicionar, atualizar e remover processos.

*   **2.2.2. Manter Atribuição Temporária (`CDU-016`):**
    *   **Objetivo:** Implementar o CRUD de atribuições temporárias.
    *   **Ações:**
        *   **`CadAtribuicao.vue`:** Implementar o formulário para seleção de servidor, datas e justificativa. Integrar com `useAtribuicaoTemporariaStore` para adicionar, editar e remover atribuições.

*   **2.2.3. Manter Atividades e Conhecimentos (`CDU-006`):**
    *   **Objetivo:** Implementar o CRUD de atividades e conhecimentos.
    *   **Ações:**
        *   **`CadAtividades.vue`:** Implementar a adição, edição e remoção de atividades e conhecimentos. Desenvolver a lógica de importação de atividades de outras unidades.
        *   **Store:** Criar uma store para gerenciar atividades e conhecimentos.

### 2.3. Fluxos de Processo e Validação

*   **2.3.1. Iniciar Processo de Mapeamento (`CDU-004`):**
    *   **Objetivo:** Completar a funcionalidade de iniciar um processo de mapeamento.
    *   **Ações:**
        *   **`DetalhesProcesso.vue` / `CadProcesso.vue`:** Chamar a lógica de inicialização.
        *   **Store:** Implementar a criação de processos de unidade internos (se necessário, uma nova store para `processosDeUnidadeInternos`).
        *   **Notificações/Alertas:** (Para o protótipo, simular com `console.log` ou mocks simples, pois o envio de e-mails está fora do escopo do frontend).

*   **2.3.2. Disponibilizar e Validar Cadastro (`CDU-007`, `CDU-008`):**
    *   **Objetivo:** Implementar o fluxo de disponibilização e validação de cadastro de atividades/conhecimentos.
    *   **Ações:**
        *   **`CadAtividades.vue`:** Implementar o botão "Disponibilizar" e a lógica de validação.
        *   **Telas de Validação:** Criar ou adaptar telas para GESTOR/ADMIN validarem/devolverem cadastros.
        *   **Stores:** Atualizar o status do processo de unidade e a localização.

*   **2.3.3. Manter e Visualizar Mapa de Competências (`CDU-010`, `CDU-011`, `CDU-012`):**
    *   **Objetivo:** Implementar o CRUD e a visualização de mapas de competências.
    *   **Ações:**
        *   **`CadMapa.vue`:** Implementar a criação de competências, associação com atividades/conhecimentos e geração do mapa.
        *   **`CadMapaVisualizacao.vue`:** Implementar a exibição do mapa hierárquico.
        *   **Stores:** Criar stores para gerenciar competências e mapas.

### 2.4. Implementação de Telas e Funcionalidades Pendentes

*   **2.4.1. Consultar Histórico de Processos (`CDU-015`):**
    *   **Objetivo:** Exibir uma tabela de processos finalizados.
    *   **Ações:**
        *   **`HistoricoProcessos.vue`:** Implementar a listagem e o filtro de processos finalizados. Adicionar o botão "Mapa" para visualização.

*   **2.4.2. Iniciar Processo de Revisão (`CDU-017`):**
    *   **Objetivo:** Implementar a funcionalidade de iniciar um processo de revisão.
    *   **Ações:** Similar ao `CDU-004`, com a lógica de cópia de mapas e notificações.

*   **2.4.3. Revisar e Validar Mapa de Competências (`CDU-018`, `CDU-019`, `CDU-020`):**
    *   **Objetivo:** Implementar os fluxos de revisão e validação de mapas revisados.
    *   **Ações:** Desenvolver as telas e lógicas para esses fluxos complexos.

*   **2.4.4. Outros CDUs (`CDU-021` a `CDU-026`):**
    *   **Objetivo:** Implementar as funcionalidades restantes.
    *   **Ações:** Implementar conforme a necessidade e prioridade, criando telas e lógicas específicas.

### 2.5. Melhorias de UX e Consistência

*   **2.5.1. Seletor de Perfil no Navbar:**
    *   **Objetivo:** Permitir que o usuário troque de perfil/unidade diretamente pelo Navbar, sem precisar deslogar.
    *   **Ações:**
        *   **`Navbar.vue`:** Modificar o seletor de perfil para listar todos os pares "perfil - unidade" disponíveis para o `servidorLogado` (similar à lógica de login). Ao selecionar um novo par, atualizar `perfilStore.perfilSelecionado` e `perfilStore.unidadeSelecionada`.

*   **2.5.2. "Contato" e "Mapa Vigente" em `DetalhesUnidade.vue`:**
    *   **Objetivo:** Exibir informações mais realistas para "Contato" e "Mapa vigente".
    *   **Ações:**
        *   **Mock de Dados:** Adicionar um campo `contato` ao `unidades.json` para algumas unidades.
        *   **`DetalhesUnidade.vue`:** Refinar a exibição do "Mapa vigente" para mostrar mais detalhes do mapa (e.g., data de criação, versão) se aplicável, além da situação.

### 2.6. Refatoração e Limpeza de Código

*   **2.6.1. Refatoração da lógica de `getPerfisEUnidades` em `Login.vue`:**
    *   **Objetivo:** Melhorar a legibilidade e manutenção da função.
    *   **Ações:** Revisar a função, possivelmente dividindo-a em funções menores ou utilizando abordagens mais funcionais para clareza.

## 3. Verificação e Testes

Após cada etapa de implementação, realizar as seguintes verificações:

*   **Testes Manuais:** Navegar pela aplicação e testar as funcionalidades implementadas.
*   **Console do Navegador:** Verificar se há erros ou avisos no console.
*   **Consistência de Dados:** Assegurar que os dados exibidos são consistentes com os mocks e as regras de negócio.

Este plano será atualizado conforme o progresso e novas necessidades surgirem.