# Planejamento Frontend - Sprint 01: Fundação e BFF

## Objetivo
Estabelecer a base para a otimização de performance e manutenibilidade do frontend, focando na eliminação de "API Chaining" na tela crítica `CadMapa.vue` através do padrão BFF (Backend For Frontend) e na padronização de tratamento de erros.

## Tarefas de Execução

### 1. [Backend] Criar Endpoint BFF para Contexto de Edição
**Contexto:** A tela `CadMapa.vue` realiza 5 requisições sequenciais/paralelas para carregar. Precisamos de um único endpoint agregado.
**Ação:** Implementar o endpoint `GET /api/subprocessos/{id}/contexto-edicao`.
**Detalhes Técnicos:**
- **DTO de Resposta:** `ContextoEdicaoDto` contendo:
  - `UnidadeDto unidade`
  - `SubprocessoDetalheDto subprocesso`
  - `MapaCompletoDto mapa` (pode ser null)
  - `List<AtividadeDto> atividadesDisponiveis`
- **Service:** Criar `SubprocessoContextoService` (facade de leitura).
- **Controller:** `SubprocessoControle` (ou criar `SubprocessoContextoControle`).
- **Transação:** `@Transactional(readOnly = true)`.
**Arquivo(s) Sugeridos:**
- `backend/src/main/java/sgc/subprocesso/SubprocessoContextoService.java`
- `backend/src/main/java/sgc/subprocesso/dtos/ContextoEdicaoDto.java`
- `backend/src/main/java/sgc/subprocesso/SubprocessoControle.java`
**Definição de Pronto (DoD):**
- Endpoint retorna JSON com todos os dados necessários em < 200ms.
- Testes de integração cobrindo cenários de sucesso e erro (404).

### 2. [Frontend] Criar Composable `useFormErrors`
**Contexto:** A lógica de mapeamento de erros de validação (`subErrors`) é duplicada em várias views.
**Ação:** Criar um composable reutilizável para gerenciar erros de formulário.
**Especificação:**
- Deve aceitar lista inicial de campos.
- Deve expor método `setFromNormalizedError(error: NormalizedError)`.
- Deve limpar erros automaticamente antes de setar novos.
**Arquivo(s):**
- `frontend/src/composables/useFormErrors.ts`
**Definição de Pronto (DoD):**
- Teste unitário (`frontend/src/composables/__tests__/useFormErrors.spec.ts`) cobrindo mapeamento de campos e limpeza.

### 3. [Frontend] Refatorar `CadMapa.vue` (Consumo BFF)
**Contexto:** A view atualmente faz 5 chamadas. Deve fazer apenas 1 para o novo endpoint.
**Ação:**
- Atualizar `subprocessosStore` (ou criar `contextoStore`) para consumir o novo endpoint.
- Substituir as chamadas de `unidadesStore`, `mapasStore`, `atividadesStore` no `onMounted` pela nova chamada única.
- Popular os dados locais da view a partir da resposta única.
**Arquivo(s):**
- `frontend/src/views/CadMapa.vue`
- `frontend/src/stores/subprocessos.ts` (ou nova store)
**Definição de Pronto (DoD):**
- Tela carrega com apenas 1 requisição XHR visível no Network tab.
- Todos os dados (mapa, atividades, cabeçalho) são renderizados corretamente.
- Sem erros no console.

### 4. [Frontend] Refatorar `CadMapa.vue` (Tratamento de Erros)
**Contexto:** Substituir a lógica manual de `handleApiErrors` pelo novo composable.
**Ação:**
- Importar `useFormErrors`.
- Remover função `handleApiErrors`.
- Ligar o `setFromNormalizedError` no `catch` das ações de salvar.
- Atualizar o template para usar o objeto `errors` retornado pelo composable.
**Arquivo(s):**
- `frontend/src/views/CadMapa.vue`
**Definição de Pronto (DoD):**
- Erros de validação do backend continuam aparecendo nos campos corretos.
- Código da view reduzido em ~40-50 linhas.

## Verificação Final da Sprint
- [ ] Executar `npm run typecheck` sem erros.
- [ ] Executar `npm run test:unit` (garantir que testes existentes não quebraram).
- [ ] Verificar fluxo manual: Abrir tela de edição de mapa -> Carregamento rápido -> Tentar salvar inválido -> Ver erros.
