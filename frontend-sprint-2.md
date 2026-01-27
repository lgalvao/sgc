# 🎨 Sprint 2 - Consolidação Frontend

**Duração Estimada:** 3-5 dias  
**Objetivo:** Frontend mais consistente, menos requisições HTTP, código DRY  
**Foco:** Eliminar duplicação de código e otimizar comunicação com backend

---

## 📋 Sumário de Ações

| # | Ação                                                           | Prioridade | Esforço  | Impacto  | Arquivos                               |
|---|----------------------------------------------------------------|------------|----------|----------|----------------------------------------|
| 2 | Criar composable `useErrorHandler` para stores                 | 🔴 Alta    | 🟡 Médio | 🔴 Alto  | 14 arquivos (~500 linhas economizadas) |
| 4 | Consolidar queries duplicadas (AtividadeRepo, CompetenciaRepo) | 🔴 Alta    | 🟡 Médio | 🟠 Médio | 2 arquivos (~20 linhas)                |
| 5 | Backend retornar dados completos (eliminar cascata de reloads) | 🔴 Alta    | 🔴 Alto  | 🔴 Alto  | 6 controllers, 6 stores (~50 linhas)   |

**Resultado Esperado:** Redução de 25-40% nas requisições HTTP, código frontend mais limpo e manutenível.

---

## 🎯 Ação #2: Criar Composable useErrorHandler

### Contexto

Todos os 13 stores do frontend implementam o mesmo padrão de tratamento de erro, com código duplicado em **~104 blocos**
similares. Cada método assíncrono repete a mesma lógica:

1. Limpar erro anterior (`lastError.value = null`)
2. Executar lógica em try/catch
3. Capturar erro e normalizar (`lastError.value = normalizeError(error)`)
4. Re-lançar erro

Isso viola o princípio DRY e dificulta manutenção.

### Problema Identificado

**Arquivos Afetados:** 13 stores

- `processos.ts`
- `subprocessos.ts`
- `atividades.ts`
- `usuarios.ts`
- `unidades.ts`
- `mapas.ts`
- `competencias.ts`
- E outros...

**Código Duplicado (Padrão Repetido ~104 vezes):**

```typescript
// Exemplo em processos.ts
async function buscarProcessos(filtro: FiltroProcesso) {
  lastError.value = null;  // ← Repetido
  try {
    const response = await processoService.listarProcessos(filtro);
    processos.value = response.items;
    return response;
  } catch (error) {
    lastError.value = normalizeError(error);  // ← Repetido
    throw error;  // ← Repetido
  }
}

// Exemplo em atividades.ts (IDÊNTICO)
async function buscarAtividades(codSubprocesso: number) {
  lastError.value = null;  // ← Repetido
  try {
    const response = await atividadeService.listar(codSubprocesso);
    atividades.value = response;
    return response;
  } catch (error) {
    lastError.value = normalizeError(error);  // ← Repetido
    throw error;  // ← Repetido
  }
}

// ... repetido em TODOS os stores
```

**Contagem:**

- **13 stores** × ~8 métodos async por store = **~104 blocos idênticos**
- Estimativa: **~500 linhas de código duplicado**

### Solução

**Criar composable centralizado:**

**Arquivo:** `/frontend/src/composables/useErrorHandler.ts` (NOVO)

```typescript
import { ref } from 'vue';
import { normalizeError, type NormalizedError } from '@/utils/apiError';

/**
 * Composable para tratamento centralizado de erros em stores.
 * 
 * Elimina duplicação de código de error handling em todos os stores,
 * fornecendo uma API consistente para gerenciar erros.
 * 
 * @example
 * ```typescript
 * export const useMyStore = defineStore('myStore', () => {
 *   const { lastError, clearError, withErrorHandling } = useErrorHandler();
 *   
 *   async function buscarDados() {
 *     return withErrorHandling(async () => {
 *       const dados = await apiService.buscar();
 *       // ... processar dados
 *       return dados;
 *     });
 *   }
 *   
 *   return { lastError, clearError, buscarDados };
 * });
 * ```

*/
export function useErrorHandler() {
const lastError = ref<NormalizedError | null>(null);

/**

* Limpa o último erro armazenado.
  */
  function clearError() {
  lastError.value = null;
  }

/**

* Executa uma função assíncrona com tratamento automático de erros.
*
* @param fn - Função assíncrona a ser executada
* @param onError - Callback opcional executado quando ocorre erro
* @returns Promise com resultado da função
* @throws Re-lança o erro após tratamento
  */
  async function withErrorHandling<T>(
  fn: () => Promise<T>,
  onError?: (error: NormalizedError) => void
  ): Promise<T> {
  lastError.value = null;
  try {
  return await fn();
  } catch (error) {
  const normalized = normalizeError(error);
  lastError.value = normalized;

  if (onError) {
  onError(normalized);
  }

  throw error;
  }
  }

return {
lastError,
clearError,
withErrorHandling
};
}

```

**Uso em Store (Exemplo Simplificado):**

```typescript
// atividades.ts - ANTES (com duplicação)
export const useAtividadesStore = defineStore("atividades", () => {
  const atividades = ref<Atividade[]>([]);
  const lastError = ref<NormalizedError | null>(null);

  async function buscarAtividades(codSubprocesso: number) {
    lastError.value = null;  // ❌ Duplicado
    try {
      const response = await atividadeService.listar(codSubprocesso);
      atividades.value = response;
      return response;
    } catch (error) {
      lastError.value = normalizeError(error);  // ❌ Duplicado
      throw error;  // ❌ Duplicado
    }
  }

  return { atividades, lastError, buscarAtividades };
});

// atividades.ts - DEPOIS (sem duplicação)
export const useAtividadesStore = defineStore("atividades", () => {
  const atividades = ref<Atividade[]>([]);
  const { lastError, clearError, withErrorHandling } = useErrorHandler();

  async function buscarAtividades(codSubprocesso: number) {
    return withErrorHandling(async () => {  // ✅ Centralizado
      const response = await atividadeService.listar(codSubprocesso);
      atividades.value = response;
      return response;
    });
  }

  return { atividades, lastError, clearError, buscarAtividades };
});
```

### Passos para Execução por IA

1. **Criar composable useErrorHandler:**
   ```bash
   # Verificar se pasta composables existe
   ls -la /home/runner/work/sgc/sgc/frontend/src/composables/
   
   # Criar arquivo
   create /home/runner/work/sgc/sgc/frontend/src/composables/useErrorHandler.ts
   ```

2. **Verificar se há index.ts em composables:**
   ```bash
   view /home/runner/work/sgc/sgc/frontend/src/composables/index.ts
   ```

3. **Adicionar export em index.ts (se existir):**
   ```bash
   edit /home/runner/work/sgc/sgc/frontend/src/composables/index.ts
   # Adicionar: export { useErrorHandler } from './useErrorHandler';
   ```

4. **Listar todos os stores para refatorar:**
   ```bash
   ls -la /home/runner/work/sgc/sgc/frontend/src/stores/*.ts
   ```

5. **Refatorar cada store (ITERATIVO - um por vez):**

   Para cada store:

   a. **Ver conteúdo do store:**
   ```bash
   view /home/runner/work/sgc/sgc/frontend/src/stores/atividades.ts
   ```

   b. **Adicionar import do useErrorHandler:**
   ```typescript
   import { useErrorHandler } from '@/composables/useErrorHandler';
   ```

   c. **Substituir declaração de lastError:**
   ```diff
   - const lastError = ref<NormalizedError | null>(null);
   + const { lastError, clearError, withErrorHandling } = useErrorHandler();
   ```

   d. **Refatorar cada método assíncrono:**
   ```diff
   - async function buscarAtividades(codSubprocesso: number) {
   -   lastError.value = null;
   -   try {
   -     const response = await atividadeService.listar(codSubprocesso);
   -     atividades.value = response;
   -     return response;
   -   } catch (error) {
   -     lastError.value = normalizeError(error);
   -     throw error;
   -   }
   - }
   
   + async function buscarAtividades(codSubprocesso: number) {
   +   return withErrorHandling(async () => {
   +     const response = await atividadeService.listar(codSubprocesso);
   +     atividades.value = response;
   +     return response;
   +   });
   + }
   ```

   e. **Atualizar return do store para incluir clearError:**
   ```diff
   - return { atividades, lastError, buscarAtividades };
   + return { atividades, lastError, clearError, buscarAtividades };
   ```

6. **Stores a refatorar (ordem sugerida):**
    - `atividades.ts` (primeiro, como exemplo)
    - `processos.ts`
    - `subprocessos.ts`
    - `mapas.ts`
    - `competencias.ts`
    - `usuarios.ts`
    - `unidades.ts`
    - `perfil.ts`
    - Outros stores restantes

7. **Executar testes após cada store refatorado:**
   ```bash
   cd /home/runner/work/sgc/sgc
   npm run typecheck
   npm run lint
   npm run test:unit
   ```

8. **Validação final:**
   ```bash
   # Verificar que não há mais duplicação de error handling
   grep -r "lastError.value = normalizeError" frontend/src/stores/ --include="*.ts"
   
   # Resultado esperado: Nenhuma ocorrência
   ```

### Critérios de Validação

- ✅ Arquivo `useErrorHandler.ts` criado em `/frontend/src/composables/`
- ✅ Todos os 13 stores refatorados
- ✅ Nenhuma duplicação de `lastError.value = null` ou `lastError.value = normalizeError(error)`
- ✅ Todos os stores exportam `clearError`
- ✅ TypeCheck passa
- ✅ Lint passa
- ✅ Testes unitários passam
- ✅ Testes E2E passam

### Benefícios

- 🟢 Redução de **~500 linhas** de código duplicado
- 🟢 Tratamento de erro **consistente** em todo o frontend
- 🟢 **Manutenibilidade** melhorada (mudanças em um lugar só)
- 🟢 **Testabilidade** melhorada (composable isolado pode ser testado)
- 🟢 Código mais **limpo e legível**

---

## 🎯 Ação #4: Consolidar Queries Duplicadas (Backend)

### Contexto

Os repositórios `AtividadeRepo` e `CompetenciaRepo` têm queries similares para buscar entidades por código de mapa, com
pequenas variações. Essas variações poderiam ser consolidadas usando `@EntityGraph` ou métodos mais consistentes.

### Problema Identificado

**Arquivos Afetados:**

- `/backend/src/main/java/sgc/mapa/model/AtividadeRepo.java`
- `/backend/src/main/java/sgc/mapa/model/CompetenciaRepo.java`

**Queries Duplicadas:**

**AtividadeRepo.java:**

```java
// Método 1 (linha ~20)
@Query("""
    SELECT a FROM Atividade a
    LEFT JOIN FETCH a.competencias
    WHERE a.mapa.codigo = :codigoMapa
    """)
List<Atividade> findByMapaCodigo(@Param("codigoMapa") Long codigoMapa);

// Método 2 (linha ~30) - DUPLICAÇÃO com pequena variação
@Query("""
    SELECT a FROM Atividade a
    LEFT JOIN FETCH a.conhecimentos
    WHERE a.mapa.codigo = :codigoMapa
    """)
List<Atividade> findByMapaCodigoWithConhecimentos(@Param("codigoMapa") Long codigoMapa);

// Problema: Duas queries quase idênticas, diferem apenas no JOIN
```

**CompetenciaRepo.java:**

```java
// Similar pattern
@Query("""
    SELECT c FROM Competencia c
    LEFT JOIN FETCH c.atividades
    WHERE c.mapa.codigo = :codigoMapa
    """)
List<Competencia> findByMapaCodigo(@Param("codigoMapa") Long codigoMapa);

// Outro método com variação
@Query("""
    SELECT c FROM Competencia c
    WHERE c.mapa.codigo = :codigoMapa
    """)
List<Competencia> findByMapaCodigoSimple(@Param("codigoMapa") Long codigoMapa);
```

### Solução

**Opção A - Usar @EntityGraph (RECOMENDADA):**

```java
// AtividadeRepo.java - CONSOLIDADO
public interface AtividadeRepo extends JpaRepository<Atividade, Long> {

    // Método base (sem fetch)
    List<Atividade> findByMapaCodigo(Long codigoMapa);
    
    // Com competências (usando @EntityGraph)
    @EntityGraph(attributePaths = {"competencias"})
    List<Atividade> findWithCompetenciasByMapaCodigo(Long codigoMapa);
    
    // Com conhecimentos (usando @EntityGraph)
    @EntityGraph(attributePaths = {"conhecimentos"})
    List<Atividade> findWithConhecimentosByMapaCodigo(Long codigoMapa);
    
    // Com ambos (se necessário)
    @EntityGraph(attributePaths = {"competencias", "conhecimentos"})
    List<Atividade> findWithAllByMapaCodigo(Long codigoMapa);
}
```

**Benefícios:**

- ✅ Menos código (Spring Data deriva implementação)
- ✅ Mais flexível (@EntityGraph permite combinações)
- ✅ Mais consistente (padrão do Spring Data)

**Opção B - Query Method Derivation (Mais simples):**

```java
// AtividadeRepo.java - SUPER SIMPLES
public interface AtividadeRepo extends JpaRepository<Atividade, Long> {

    // Spring Data deriva a query automaticamente
    List<Atividade> findByMapaCodigo(Long codigoMapa);
    
    // Para casos com fetch, usar @EntityGraph
    @EntityGraph(attributePaths = {"competencias"})
    List<Atividade> findByMapaCodigo(Long codigoMapa);  // Sobrecarga
}
```

### Passos para Execução por IA

1. **Localizar arquivos:**
   ```bash
   view /home/runner/work/sgc/sgc/backend/src/main/java/sgc/mapa/model/AtividadeRepo.java
   view /home/runner/work/sgc/sgc/backend/src/main/java/sgc/mapa/model/CompetenciaRepo.java
   ```

2. **Identificar todas as queries `findByMapaCodigo*`:**
   ```bash
   grep -n "findByMapaCodigo" backend/src/main/java/sgc/mapa/model/AtividadeRepo.java
   grep -n "findByMapaCodigo" backend/src/main/java/sgc/mapa/model/CompetenciaRepo.java
   ```

3. **Verificar onde cada método é usado:**
   ```bash
   grep -r "findByMapaCodigo" backend/src/main/java/sgc/ --include="*.java"
   grep -r "findByMapaCodigoWithConhecimentos" backend/src/main/java/sgc/ --include="*.java"
   ```

4. **Refatorar AtividadeRepo:**
   ```bash
   edit /home/runner/work/sgc/sgc/backend/src/main/java/sgc/mapa/model/AtividadeRepo.java
   # Substituir queries @Query por @EntityGraph
   ```

5. **Refatorar CompetenciaRepo:**
   ```bash
   edit /home/runner/work/sgc/sgc/backend/src/main/java/sgc/mapa/model/CompetenciaRepo.java
   # Consolidar queries similares
   ```

6. **Atualizar chamadas nos Services:**
   ```bash
   # Se necessário, atualizar nomes de métodos nos services
   ```

7. **Executar testes:**
   ```bash
   cd /home/runner/work/sgc/sgc
   ./gradlew :backend:test --tests "*Atividade*"
   ./gradlew :backend:test --tests "*Competencia*"
   ```

### Critérios de Validação

- ✅ Menos queries @Query duplicadas
- ✅ Uso de @EntityGraph onde apropriado
- ✅ Código mais consistente e idiomático
- ✅ Testes passam
- ✅ Performance igual ou melhor

---

## 🎯 Ação #5: Backend Retornar Dados Completos (Eliminar Cascata)

### Contexto

Atualmente, após ações de workflow (criar, atualizar, deletar), o frontend faz **3 requisições** em cascata:

1. Ação principal (POST/PUT/DELETE)
2. Recarregar lista afetada (GET)
3. Recarregar detalhes relacionados (GET)

Isso é **ineficiente** e causa **latência perceptível** na UI. O backend deveria retornar os dados completos na primeira
resposta.

### Problema Identificado

**Arquivos Afetados (Frontend):**

- `/frontend/src/stores/atividades.ts` - 6 métodos com cascata
- `/frontend/src/stores/processos.ts` - ~4 métodos com cascata
- `/frontend/src/stores/subprocessos.ts` - ~5 métodos com cascata

**Exemplo - Cascata de 3 Requisições:**

```typescript
// atividades.ts - ANTES (com cascata)
async function adicionarAtividade(
  codSubprocesso: number,
  codMapa: number,
  request: CriarAtividadeRequest
) {
  lastError.value = null;
  try {
    // ❌ REQUISIÇÃO 1: Criar atividade
    const response = await atividadeService.criarAtividade(request, codMapa);
    
    // ❌ REQUISIÇÃO 2: Recarregar lista de atividades
    await buscarAtividadesParaSubprocesso(codSubprocesso);
    
    // ❌ REQUISIÇÃO 3: Recarregar detalhes do subprocesso
    const subprocessosStore = useSubprocessosStore();
    await subprocessosStore.buscarSubprocessoDetalhe(codSubprocesso);
    
    return response.subprocesso;
  } catch (error) {
    lastError.value = normalizeError(error);
    throw error;
  }
}
```

**Sequência Temporal (Atual):**

```
t=0ms:   POST /api/atividades (criar atividade)
  ↓
t=120ms: Response recebida
  ↓
t=121ms: GET /api/subprocessos/456/atividades (recarregar lista)
  ↓
t=250ms: Response recebida
  ↓
t=251ms: GET /api/subprocessos/456/detalhes (recarregar detalhes)
  ↓
t=380ms: CONCLUÍDO (total: 380ms, 3 requests)
```

**Impacto:**

- ❌ **3 requisições** por ação (ineficiente)
- ❌ **Latência** de ~380ms total
- ❌ **Código complexo** com dependências entre stores

### Solução

**Backend retornar dados completos na primeira resposta:**

**Backend - DEPOIS:**

```java
// AtividadeController.java
@PostMapping("/{codigoMapa}/atividades")
public ResponseEntity<CriarAtividadeResponse> criarAtividade(
    @PathVariable Long codigoMapa,
    @RequestBody @Valid CriarAtividadeRequest request
) {
    var atividade = atividadeFacade.criarAtividade(codigoMapa, request);
    
    // ✅ Retornar dados COMPLETOS (atividade + lista atualizada + detalhes)
    var response = new CriarAtividadeResponse(
        atividade,
        atividadeFacade.buscarAtividadesPorMapa(codigoMapa),  // Lista completa
        subprocessoFacade.buscarDetalhes(atividade.getSubprocessoCodigo())  // Detalhes
    );
    
    return ResponseEntity.ok(response);
}
```

**Frontend - DEPOIS:**

```typescript
// atividades.ts - DEPOIS (SEM cascata)
async function adicionarAtividade(
  codSubprocesso: number,
  codMapa: number,
  request: CriarAtividadeRequest
) {
  return withErrorHandling(async () => {
    // ✅ UMA ÚNICA REQUISIÇÃO - backend retorna tudo
    const response = await atividadeService.criarAtividade(request, codMapa);
    
    // ✅ Atualizar estado local com dados completos
    atividadesPorSubprocesso.value.set(codSubprocesso, response.atividades);
    
    const subprocessosStore = useSubprocessosStore();
    subprocessosStore.atualizarDetalhesLocal(response.subprocessoDetalhes);
    
    return response;
  });
}
```

**Sequência Temporal (Nova):**

```
t=0ms:   POST /api/atividades (criar + retornar tudo)
  ↓
t=150ms: Response completa recebida
  ↓
t=151ms: CONCLUÍDO (total: 150ms, 1 request)

Redução: 380ms → 150ms (60% mais rápido!)
```

### Passos para Execução por IA

**ATENÇÃO:** Esta é a ação mais complexa da Sprint 2. Requer mudanças coordenadas entre backend e frontend.

#### Fase 1: Identificar Endpoints Afetados

1. **Mapear cascatas no frontend:**
   ```bash
   grep -r "buscarSubprocessoDetalhe\|buscarProcessoDetalhes" frontend/src/stores/ --include="*.ts" -B 5 -A 5
   ```

2. **Listar todos os métodos com cascata:**
   ```bash
   # Buscar padrão de múltiplas chamadas await consecutivas
   grep -r "await.*await" frontend/src/stores/ --include="*.ts" -A 2
   ```

3. **Endpoints backend a modificar:**
    - `POST /api/mapas/{id}/atividades` - AtividadeController
    - `PUT /api/mapas/{id}/atividades/{idAtividade}` - AtividadeController
    - `POST /api/atividades/{id}/excluir` - AtividadeController
    - `POST /api/processos` - ProcessoController
    - `POST /api/processos/{id}/iniciar` - ProcessoWorkflowController
    - Outros identificados na análise

#### Fase 2: Criar DTOs de Response Completos (Backend)

1. **Criar DTOs de response:**
   ```bash
   # Exemplo: CriarAtividadeResponse.java
   create /home/runner/work/sgc/sgc/backend/src/main/java/sgc/mapa/dto/CriarAtividadeResponse.java
   ```

   ```java
   public record CriarAtividadeResponse(
       AtividadeDto atividade,
       List<AtividadeDto> atividadesAtualizadas,
       SubprocessoDetalheDto subprocessoDetalhes
   ) {}
   ```

2. **Repetir para outros endpoints.**

#### Fase 3: Atualizar Controllers (Backend)

1. **Modificar cada controller:**
   ```bash
   edit /home/runner/work/sgc/sgc/backend/src/main/java/sgc/mapa/controller/AtividadeController.java
   # Alterar retorno para incluir dados completos
   ```

2. **Executar testes backend:**
   ```bash
   ./gradlew :backend:test
   ```

#### Fase 4: Atualizar Services Frontend

1. **Atualizar tipos TypeScript:**
   ```typescript
   // atividadeService.ts
   export interface CriarAtividadeResponse {
     atividade: Atividade;
     atividadesAtualizadas: Atividade[];
     subprocessoDetalhes: SubprocessoDetalhado;
   }
   ```

2. **Atualizar chamadas de API:**
   ```bash
   edit /home/runner/work/sgc/sgc/frontend/src/services/atividadeService.ts
   ```

#### Fase 5: Refatorar Stores (Frontend)

1. **Remover cascatas:**
   ```bash
   edit /home/runner/work/sgc/sgc/frontend/src/stores/atividades.ts
   # Remover chamadas sequenciais, usar dados do response
   ```

2. **Repetir para cada store afetado.**

#### Fase 6: Testes Extensivos

```bash
# Backend
./gradlew :backend:test

# Frontend
npm run typecheck
npm run lint
npm run test:unit

# E2E (CRÍTICO para esta ação)
npm run test:e2e
```

### Critérios de Validação

- ✅ Backend retorna dados completos em responses
- ✅ Frontend não faz mais cascatas de reloads
- ✅ Redução de 66% nas requisições HTTP (3 → 1)
- ✅ Latência reduzida em 40-60%
- ✅ Testes E2E passam (sem regressões)
- ✅ Performance melhorada (medida com Playwright)

### Riscos e Mitigações

**🔴 ALTO RISCO:**

- Mudança em 6 controllers e 6 stores
- Potencial para quebrar funcionalidades existentes

**Mitigações:**

1. **Testes E2E OBRIGATÓRIOS** antes e depois
2. **Implementar incrementalmente** (um endpoint por vez)
3. **Validar cada endpoint** antes de prosseguir
4. **Rollback fácil** (commits pequenos e isolados)

---

## 📊 Checklist de Validação da Sprint 2

Após implementar todas as 3 ações, validar:

### Testes Automatizados

- [ ] ✅ Testes unitários backend passam: `./gradlew :backend:test`
- [ ] ✅ Testes unitários frontend passam: `npm run test:unit`
- [ ] ✅ TypeCheck frontend passa: `npm run typecheck`
- [ ] ✅ Lint frontend passa: `npm run lint`
- [ ] ✅ **Testes E2E passam (CRÍTICO):** `npm run test:e2e`

### Validação Manual

- [ ] ✅ Aplicação inicia sem erros
- [ ] ✅ Criar atividade não faz 3 requisições
- [ ] ✅ Atualizar atividade não faz 3 requisições
- [ ] ✅ Deletar atividade não faz 3 requisições
- [ ] ✅ Tratamento de erros consistente em todos os stores
- [ ] ✅ Performance melhorada (latência reduzida)

### Qualidade de Código

- [ ] ✅ Nenhuma duplicação de error handling
- [ ] ✅ Nenhuma cascata de reloads
- [ ] ✅ Composable `useErrorHandler` implementado
- [ ] ✅ Queries consolidadas no backend
- [ ] ✅ DTOs de response completos

### Métricas

- [ ] ✅ Redução de **~500 linhas** (error handling)
- [ ] ✅ Redução de **25-40%** em requisições HTTP
- [ ] ✅ Latência reduzida em **40-60%**

---

## 📈 Métricas de Sucesso

**Antes da Sprint 2:**

- Código duplicado (error handling): ~500 linhas
- Requisições por ação: 3 (cascata)
- Latência por ação: ~380ms
- Queries duplicadas: ~5 ocorrências

**Após a Sprint 2:**

- ✅ Código duplicado: 0 linhas (eliminado ~500 linhas)
- ✅ Requisições por ação: 1 (redução de 66%)
- ✅ Latência por ação: ~150ms (redução de 60%)
- ✅ Queries consolidadas (uso de @EntityGraph)

**Estimativa de Impacto:**

- 🟢 Redução de **~550 linhas** de código
- 🟢 Redução de **25-40%** em requisições HTTP
- 🟢 Performance melhorada em **40-60%**
- 🟢 Código mais **limpo e manutenível**

---

## 🚀 Próximos Passos

Após conclusão da Sprint 2, prosseguir para:

- **Sprint 3:** [backend-sprint-3.md](./backend-sprint-3.md) - Refatoração Backend (God Objects)
- **Sprint 4:** [otimizacoes-sprint-4.md](./otimizacoes-sprint-4.md) - Otimizações Opcionais

---

**Versão:** 1.0  
**Data de Criação:** 26 de Janeiro de 2026  
**Status:** 🔵 Planejada
