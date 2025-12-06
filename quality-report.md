# Relatório de Qualidade do Projeto SGC

Este relatório apresenta os resultados da verificação de qualidade executada utilizando os scripts e tarefas do projeto (`quality-check.sh`).

**Data:** 16 de Dezembro de 2025
**Status Global:** 🔴 FALHA (Devido a erros no Frontend Typecheck)

## 1. Resumo Executivo

A verificação de qualidade foi realizada para Backend e Frontend.
- **Backend:** Testes e ferramentas de análise estática (Checkstyle, PMD, SpotBugs) foram executados. Foram encontradas violações de estilo e boas práticas.
- **Frontend:** Linting passou sem problemas, mas o Typecheck falhou com erros de configuração ou tipagem em componentes Vue, impedindo a validação completa.

---

## 2. Detalhamento Frontend

### 2.1. Typecheck (Typescript)
**Status:** 🔴 FALHA CRÍTICA

O comando `npm run typecheck` (executando `vue-tsc`) falhou e causou um erro interno (crash) durante o processamento.

**Erros Principais:**
- `error TS2306: File '/app/frontend/src/views/CadMapa.vue' is not a module.`
  - Este erro ocorre em múltiplos arquivos (`router.ts`, testes unitários) ao importar `CadMapa.vue`.
  - Indica provável falta de declaração de tipo para arquivos `.vue` (ex: ausência de `shim-vue.d.ts` ou `env.d.ts`) ou erro de sintaxe que impede o parser do `vue-tsc` de reconhecer o componente.
- `TypeError: Cannot read properties of undefined (reading 'length')`
  - Um erro interno no `vue-tsc` ao processar templates, sugerindo que algum componente (possivelmente `CadMapa.vue`) tem uma estrutura que a ferramenta não consegue analisar corretamente.

### 2.2. Lint (ESLint)
**Status:** 🟢 SUCESSO

Não foram encontradas violações de lint no código frontend. O código segue os padrões de formatação e estilo definidos.

### 2.3. Testes Unitários
**Status:** ⚪ EXECUTADOS (Relatório gerado em `frontend/coverage/index.html`)

---

## 3. Detalhamento Backend

### 3.1. Análise Estática (Checkstyle)
**Status:** ⚠️ ALERTA (Violações Encontradas)

Foram identificadas violações de estilo, principalmente relacionadas à organização de imports e Javadoc.

**Violações Comuns:**
- **Ordenação de Imports:** Imports do Java (`java.*`) devem preceder imports do projeto (`sgc.*`).
- **Javadoc:** Tags `<p>` devem ser precedidas por uma linha em branco.

**Exemplo:**
```
Wrong lexicographical order for 'java.io.UnsupportedEncodingException' import. Should be before 'sgc.notificacao.model.NotificacaoRepo'.
```

### 3.2. Análise Estática (PMD)
**Status:** ⚠️ ALERTA (Violações Encontradas)

O PMD identificou problemas de design e boas práticas.

**Principais Problemas:**
- **Acoplamento:** A classe `AlertaService` possui alto acoplamento (CouplingBetweenObjects = 24), excedendo o limite de 20.
- **Tratamento de Exceções:** Captura genérica de `RuntimeException` ou `Exception` em vários pontos.
- **Logs:** Chamadas de log não estão protegidas por guardas (ex: `if (log.isDebugEnabled())`).
- **Nomenclatura:** Classe `Sgc` tem nome muito curto.
- **Design:** Classe utilitária `Sgc` possui construtor público.

### 3.3. Análise de Bugs (SpotBugs)
**Status:** ⚪ EXECUTADO (Relatório em `backend/build/reports/spotbugs/main.html`)

A ferramenta executou com sucesso. Recomenda-se a análise manual do relatório HTML para identificar bugs potenciais, como dereferência de null ou problemas de concorrência.

### 3.4. Testes Automatizados
**Status:** ⚪ EXECUTADOS

Os testes foram executados. Não foram encontradas falhas explícitas nos logs analisados, mas a falha geral do script pode ter mascarado resultados. Verifique `backend/build/reports/tests/test/index.html`.

---

## 4. Análise de Conformidade com Padrões (Regras)

### 4.1. Backend
- **Nomenclatura:** Segue o padrão PascalCase para classes e camelCase para métodos.
- **API REST:** Controladores utilizam `POST` para operações de escrita (criar, atualizar, excluir), conforme documentado nos padrões do projeto (desvio aceito).
- **Idioma:** Código e comentários estão predominantemente em Português Brasileiro.
- **Arquitetura:** Uso correto de DTOs e Services.

### 4.2. Frontend
- **Tecnologia:** Uso correto de Composition API (`<script setup>`) e `BootstrapVueNext`.
- **Componentes:** Nomes em PascalCase e uso de `data-testid` para testes.
- **Estilo:** Código limpo e sem violações de lint.

---

## 5. Sugestões de Melhoria

1.  **Corrigir Configuração TypeScript (Frontend):**
    - Criar ou ajustar o arquivo de declaração de tipos (`env.d.ts`) para garantir que o TypeScript reconheça arquivos `.vue` como módulos.
    - Investigar a causa do crash no `vue-tsc` em `CadMapa.vue`, simplificando o componente temporariamente para isolar o erro.

2.  **Refatoração Backend (PMD/Checkstyle):**
    - **Automatizar Imports:** Configurar a IDE ou o pre-commit para ordenar imports automaticamente conforme as regras do Checkstyle.
    - **Reduzir Acoplamento:** Refatorar `AlertaService` extraindo responsabilidades para novos serviços ou classes auxiliares.
    - **Corrigir Logs:** Adicionar guardas de log ou configurar o PMD para ignorar se estiver usando SLF4J moderno que não exige guardas para strings parametrizadas.

3.  **Renomear Classe Principal:**
    - Renomear `Sgc` para `SgcApplication` para evitar o aviso de "ShortClassName" e deixar mais claro o propósito da classe.

4.  **Integração Contínua:**
    - Configurar o pipeline para falhar apenas se o número de violações exceder um limite, ou corrigir todas as violações atuais para ter um "clean slate".
