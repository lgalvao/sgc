# Relatório de Análise de Interface (UI) e Experiência do Usuário (UX)

**Data:** 02/01/2026
**Origem:** Análise de screenshots gerados por testes automatizados (E2E)
**Contexto:** Sistema SGC (Sistema de Gestão de Competências)

---

## 1. Visão Geral

A interface do sistema SGC utiliza **Bootstrap 5** de forma consistente, apresentando um layout limpo e funcional. A estrutura de navegação (Navbar superior) e o conteúdo principal (Containers) seguem os padrões esperados. No entanto, foram identificados pontos de melhoria na responsividade, feedback visual e consistência de componentes, além de alguns erros técnicos que impactam a experiência.

## 2. Problemas Técnicos e Bugs Identificados

Durante a execução dos testes e análise dos logs/telas, foram detectados os seguintes problemas que requerem correção prioritária:

### 2.1. Erro de Redirecionamento em Subprocessos (Crítico)
*   **Sintoma:** Ao tentar acessar os detalhes de um subprocesso específico (ex: `/processo/11/SECAO_121`), o sistema redireciona o usuário para a visão geral do processo (`/processo/11`) ou painel.
*   **Evidência:** Falha no teste `10 - Gestão de Subprocessos` (Expect URL matching `/processo/\d+/SECAO_121/`, Received `/processo/11`).
*   **Ação:** Verificar a guarda de rotas (`beforeEach` no Vue Router) e a lógica de montagem de links na tabela de subprocessos.

### 2.2. Aviso de Propriedade Vue (Console)
*   **Sintoma:** `[Vue warn]: Invalid prop: type check failed for prop "perfil". Expected String with value "null", got Null`.
*   **Local:** Componente `ProcessoAcoes`.
*   **Ação:** Ajustar a definição da prop para aceitar `null` ou garantir que o valor padrão seja uma string vazia/indefinida, evitando "sujeira" no console e potenciais comportamentos inesperados.

### 2.3. Endpoint Não Encontrado (404)
*   **Sintoma:** A aplicação tenta buscar administradores em `/api/administradores` e recebe um erro 404.
*   **Impacto:** A lista de administradores na tela de Configurações pode não estar sendo carregada corretamente.
*   **Ação:** Verificar se o endpoint existe no Backend (`AdministradorControle`) ou se a URL no Frontend está correta.

---

## 3. Análise de Responsividade (Mobile)

A análise do screenshot `08-responsividade--04-mobile-375x667.png` revelou problemas significativos na visualização em dispositivos móveis.

*   **Tabelas Cortadas:** A tabela de "Processos" e "Alertas" não possui barra de rolagem horizontal, fazendo com que colunas (como "Situação" e "Origem") sejam cortadas ou fiquem inacessíveis.
    *   **Sugestão:** Envolver todas as tabelas em um contêiner `.table-responsive` do Bootstrap.
*   **Espaçamento Vertical:** Em telas pequenas, o título "Processos" e o botão "Criar processo" ficam visualmente desconexos.
    *   **Sugestão:** Em mobile, empilhar o botão abaixo do título ou usar ícones para economizar espaço.

---

## 4. Melhorias de Interface e Usabilidade

### 4.1. Formulários e Validação
*   **Validação Inline (Screenshot `04-subprocesso--23...`):** O alerta de erro "Esta atividade não possui conhecimentos associados" usa um fundo vermelho claro (`alert-danger`). Embora visível, ocupa muito espaço vertical dentro de cada card de atividade.
    *   **Sugestão:** Utilizar uma borda vermelha no card da atividade (`border-danger`) e um texto de erro menor e mais discreto (ex: `.text-danger.small`) abaixo do título, para reduzir a poluição visual em listas longas.
*   **Campos de Data:** O input de data (`02-painel--03...`) segue o padrão do navegador, que é funcional mas pode ser inconsistente entre browsers. O alinhamento está correto.

### 4.2. Componentes e Cards
*   **Mapa de Competências (`05-mapa--05...`):** Os "cards" de competências ("Desenvolvimento de Software") parecem caixas cinzas simples com botões de ação (lixeira/lápis) flutuando.
    *   **Sugestão:** Utilizar o componente **Card** do Bootstrap completo:
        *   `.card-header`: Para o título da competência e botões de ação.
        *   `.card-body`: Para listar as atividades/conhecimentos internos.
        *   Isso cria uma hierarquia visual mais clara.
*   **Modais (`03-processo--02...`):** O texto nas modais de confirmação está denso.
    *   **Sugestão:** Utilizar negrito para informações chave (como o nome do processo) e garantir margens (`my-3`) para separar parágrafos.

### 4.3. Feedback de "Estado Vazio"
*   **Alertas (`02-painel--06...`):** A mensagem "Nenhum alerta encontrado" está centralizada e clara.
    *   **Sugestão:** Adicionar um ícone (ex: sino cinza desativado) acima do texto para reforçar o contexto visualmente e tornar a tela menos "árida".

---

## 5. Plano de Ação Sugerido

Para elevar a qualidade da UI na próxima iteração, sugere-se a seguinte ordem de prioridade:

1.  **Correção (Bug):** Resolver o redirecionamento da rota de subprocessos e o endpoint 404 de administradores.
2.  **Responsividade:** Aplicar `.table-responsive` em todas as tabelas do sistema.
3.  **Refinamento:** Melhorar o design dos cards no Mapa de Competências para usar a estrutura padrão do Bootstrap.
4.  **Polimento:** Ajustar mensagens de validação e warnings de console (Vue props).

---
*Relatório gerado automaticamente por Jules (AI Agent).*
