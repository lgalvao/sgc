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
*   **Sintoma:** Ao tentar acessar os detalhes de um subprocesso específico (ex: `/processo/11/SECAO_121`), o sistema não carrega corretamente ou retorna para a visão geral.
*   **Evidência:** Falha no teste `10 - Gestão de Subprocessos`.
*   **Análise:** O código não apresenta redirecionamentos explícitos incorretos. A falha provavelmente reside na detecção da navegação nos testes ou no preenchimento de links na tabela.
*   **Ação:** Investigação mantida para validação dinâmica posterior.

### 2.2. Aviso de Propriedade Vue (Console)
*   **Sintoma:** `[Vue warn]: Invalid prop: type check failed for prop "perfil". Expected String with value "null", got Null`.
*   **Local:** Componente `ProcessoAcoes`.
*   **Status:** Confirmado. A prop `perfil` recebe `null` da store mas espera `String`.
*   **Ação:** Ajustar a definição da prop para aceitar `String | null`.

### 2.3. Endpoint Não Encontrado (404)
*   **Sintoma:** A aplicação tenta buscar administradores em `/api/administradores` e recebe um erro 404.
*   **Status:** Confirmado. O endpoint correto no backend é `/api/usuarios/administradores`.
*   **Ação:** Corrigir as URLs no `administradorService.ts`.

---

## 3. Análise de Responsividade (Mobile)

A análise revelou problemas na visualização em dispositivos móveis (375px).

*   **Tabelas Cortadas:** A tabela de "Processos" e "Alertas" pode cortar conteúdo.
    *   **Ação:** Reforçar a responsividade envolvendo as tabelas em containers `.table-responsive` explícitos, além da propriedade `responsive` do componente.
*   **Espaçamento Vertical:** Em telas pequenas, elementos de cabeçalho podem ficar desconexos.

---

## 4. Melhorias de Interface e Usabilidade

### 4.1. Formulários e Validação
*   **Validação Inline:** Mensagens de erro podem ser otimizadas.
*   **Campos de Data:** Input padrão é funcional.

### 4.2. Componentes e Cards
*   **Mapa de Competências:** Os "cards" de competências usam estilos manuais (`competencia-titulo-card`) para simular um cabeçalho.
    *   **Status:** Confirmado. O componente `CompetenciaCard.vue` manipula CSS manualmente.
    *   **Ação:** Refatorar `CompetenciaCard.vue` para utilizar `BCardHeader` nativo do Bootstrap, removendo hacks de CSS e melhorando a semântica.

### 4.3. Feedback de "Estado Vazio"
*   **Alertas:** Mensagem clara.
    *   **Sugestão:** Adicionar ícone.

---

## 5. Plano de Ação (Atualizado)

1.  **Correção (Bug):** Corrigir rotas do `administradorService` (2.3).
2.  **Correção (Bug):** Ajustar tipagem da prop em `ProcessoAcoes` (2.2).
3.  **Refatoração:** Modernizar `CompetenciaCard` com `BCardHeader` (4.2).
4.  **Responsividade:** Aplicar wrapper `.table-responsive` nas tabelas principais (3).

---
*Documento atualizado por Jules (AI Agent) após verificação de código.*
