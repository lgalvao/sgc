# Planejamento Frontend - Sprint 03: Limpeza e Estabilização

## Objetivo
Remover "código defensivo" desnecessário nas Stores (confiando no contrato do Backend), realizar auditoria final de padrões e garantir estabilidade através de testes de regressão.

## Tarefas de Execução

### 1. [Frontend] Auditoria e Limpeza de Stores (Mapas)
**Contexto:** A store `mapas.ts` contém lógica que desconfia da resposta do backend (ex: verifica se ID é 0 e faz re-fetch).
**Ação:**
- Remover verificação `if (mapa.competencias.some(c => !c.codigo))` após salvar.
- Remover chamadas de re-fetch automáticas após mutações (confiar no retorno da mutação).
**Arquivo(s):**
- `frontend/src/stores/mapas.ts`
**Definição de Pronto (DoD):**
- Criação/Edição de competências atualiza a UI instantaneamente sem requisição GET extra.

### 2. [Frontend] Auditoria e Limpeza de Stores (Processos)
**Contexto:** `processos.ts` possui lógica complexa de merge de arrays.
**Ação:** Simplificar atualização de estado local. Substituir o objeto/lista antigo pelo novo retornado pelo backend.
**Arquivo(s):**
- `frontend/src/stores/processos.ts`
**Definição de Pronto (DoD):**
- Código da store simplificado.
- Listagens atualizam corretamente após ações.

### 3. [Frontend/Geral] Documentação Técnica Atualizada
**Contexto:** Os novos padrões (BFF, `useFormErrors`) precisam ser documentados para evitar regressão de práticas.
**Ação:** Atualizar `regras/frontend-padroes.md` (ou criar novo guia).
**Itens a Documentar:**
- Uso obrigatório de `useFormErrors`.
- Preferência por Endpoints Agregados (BFF).
- Proibição de travessia de árvore de dados complexos no cliente.
**Definição de Pronto (DoD):**
- Documentação refletindo o estado atual do código.

### 4. [QA] Testes de Regressão
**Contexto:** Garantir que as refatorações profundas não quebraram fluxos críticos.
**Ação:** Executar (manualmente ou via E2E se disponível) os fluxos principais:
- Criar Processo.
- Criar Mapa.
- Validar Mapa.
**Definição de Pronto (DoD):**
- Sistema estável.

## Verificação Final da Sprint
- [ ] Todas as stores auditadas.
- [ ] Zero chamadas "duplas" (POST seguido imediatamente de GET) no Network tab.
