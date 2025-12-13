# Plano de Refatoração: Tratamento de Erros (SGC)

**Última atualização:** 2025-12-13
**Projeto:** SGC (`lgalvao/sgc`)
**Escopo:** Backend (Spring Boot) e Frontend (Vue 3 + TS + Pinia + Axios)
**Objetivo:** Simplificar, padronizar e tornar previsível o tratamento de erros, eliminando complexidade, fragmentação, ruído e repetição.

---

## 1. Contexto e Diagnóstico

### 1.1. Problema Identificado

O tratamento de erros no SGC está **funcional**, porém apresenta os seguintes problemas:

- **Fragmentado**: Parte global (interceptor Axios, `RestExceptionHandler`), parte local (try/catch em stores/services, mapeamentos específicos como "404 retorna false")
- **Ruidoso e Repetitivo**: Muitas ações repetem `try/catch` apenas para exibir feedback e re-throw
- **Inconsistente**: Múltiplos formatos/níveis de erro; múltiplos lugares "válidos" para decidir UX do erro
- **Acoplado a Detalhes HTTP**: Regras no interceptor (`[400,404,409,422]`) e regras locais (`AxiosError/404`) acabam virando contrato implícito e espalhado

### 1.2. Evidências Atuais

**Backend:**
- `RestExceptionHandler` já existe e funciona bem como núcleo
- Exceções dispersas: `sgc.comum.erros` + exceções por módulo (`ErroProcesso`, `ErroAlerta`, etc.)
- Payload `ErroApi` existe mas não tem campos estáveis como `code`, `traceId`, ou distinção clara entre erro de usuário vs erro interno

**Frontend:**
- Interceptor Axios com lista hardcoded de status para tratamento local: `[400, 404, 409, 422]`
- Stores repetem padrão: `try { ... } catch (error) { feedbackStore.show(...); throw error; }`
- Composable `useApi` existe mas coexiste com padrões antigos (3 estratégias simultâneas)
- Casos especiais como "404 retorna false" em `mapaService.verificarMapaVigente()`

---

## 2. Visão Alvo (Estado Desejado)

### 2.1. Backend: Contrato Padronizado

**Payload de Erro Único (`ErroApi` Estendido):**

```json
{
  "timestamp": "12-12-2025 19:45:00",
  "status": 422,
  "message": "Mensagem amigável para o usuário",
  "code": "PROCESSO_ESTADO_INVALIDO",
  "details": { "campo": "valor", "motivo": "descrição" },
  "traceId": "abc-123-def"
}
```

**Critérios:**
- `timestamp`: sempre presente (LocalDateTime formatado)
- `status`: sempre presente (HTTP status)
- `message`: sempre presente (user-facing, sanitizada)
- `code`: **novo campo**, sempre presente (machine-friendly, estável, ex: `ENTIDADE_NAO_ENCONTRADA`, `VALIDACAO_FALHOU`)
- `details`: opcional (Map<String, ?> para validação/campos/diagnóstico)
- `traceId`: **novo campo**, opcional (UUID gerado para rastreabilidade/observabilidade)

### 2.2. Frontend: Política Única de UX

**Categorização de Erros:**

1. **Erros Inline/Esperados** (validação, conflito, recurso não encontrado em formulário):
   - Renderizar via `BAlert` (inline na tela)
   - **Não** mostrar toast global

2. **Erros Globais/Inesperados** (500, rede, falhas inesperadas):
   - Toast global via `useFeedbackStore`
   - Logging no console

3. **Erros de Autenticação** (401/403):
   - Toast específico
   - Redirecionamento para `/login`
   - Limpeza de sessão

**Arquitetura Unificada:**

```
API Error → Normalizador de Erro → Tipo (kind) → Decisão de UX
                                        ↓
                      inline | global | auth | network
```

**Normalizador de Erro:**

```typescript
interface NormalizedError {
  kind: 'validation' | 'conflict' | 'notFound' | 'unauthorized' | 'forbidden' | 'network' | 'unexpected';
  message: string;
  code?: string;
  status?: number;
  details?: Record<string, any>;
  subErrors?: Array<{ message?: string; field?: string; }>;
  traceId?: string;
  originalError?: unknown;
}
```

---

## 3. Plano de Execução (Estruturado para Agente IA)

### 3.1. Fase 1: Backend - Estender Contrato de Erro

**Objetivo:** Tornar `ErroApi` explícito, versionado e previsível.

#### Tarefa 1.1: Adicionar campos `code` e `traceId` a `ErroApi`

- **Arquivo:** `backend/src/main/java/sgc/comum/erros/ErroApi.java`
- **Ação:**
  - Adicionar campo `private String code;` (obrigatório)
  - Adicionar campo `private String traceId;` (opcional)
  - Atualizar construtores para aceitar `code` e `traceId`
  - Manter retrocompatibilidade: criar construtor sem `code` que usa valor padrão genérico

```java
public ErroApi(HttpStatusCode status, String message, String code) {
    this(status, message);
    this.code = code;
}

public ErroApi(HttpStatusCode status, String message, String code, String traceId) {
    this(status, message, code);
    this.traceId = traceId;
}
```

**Testes:**
- Verificar que JSON serializado inclui `code` e `traceId` (quando fornecidos)
- Testes unitários de `ErroApi` com diferentes combinações de parâmetros

#### Tarefa 1.2: Criar Interface `ErroNegocio` para Exceções de Domínio

- **Arquivo:** `backend/src/main/java/sgc/comum/erros/ErroNegocio.java` (novo)
- **Ação:**
  - Criar interface com métodos:
    - `String getCode();` - código da exceção (ex: "PROCESSO_FINALIZADO")
    - `HttpStatus getStatus();` - status HTTP apropriado
    - `Map<String, ?> getDetails();` - detalhes opcionais
  - Documentar que todas as exceções de negócio devem implementar esta interface

```java
package sgc.comum.erros;

import org.springframework.http.HttpStatus;
import java.util.Map;

/**
 * Interface para exceções de negócio que seguem o contrato padronizado.
 * Toda exceção de domínio deve implementar esta interface para garantir
 * que o RestExceptionHandler possa extrair informações de forma uniforme.
 */
public interface ErroNegocio {
    String getCode();
    HttpStatus getStatus();
    default Map<String, ?> getDetails() { return null; }
}
```

**Testes:**
- Mock de exceção implementando interface
- Validar que `RestExceptionHandler` consegue extrair código e status

#### Tarefa 1.3: Criar Classe Base `ErroNegocioBase` (Opcional, Facilita Implementação)

- **Arquivo:** `backend/src/main/java/sgc/comum/erros/ErroNegocioBase.java` (novo)
- **Ação:**
  - Classe abstrata que implementa `ErroNegocio`
  - Estende `RuntimeException`
  - Armazena `code`, `status`, `details`

```java
package sgc.comum.erros;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import java.util.Map;

@Getter
public abstract class ErroNegocioBase extends RuntimeException implements ErroNegocio {
    private final String code;
    private final HttpStatus status;
    private final Map<String, ?> details;

    protected ErroNegocioBase(String message, String code, HttpStatus status) {
        this(message, code, status, null);
    }

    protected ErroNegocioBase(String message, String code, HttpStatus status, Map<String, ?> details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details;
    }
}
```

#### Tarefa 1.4: Migrar Exceções Existentes para Implementar `ErroNegocio`

- **Arquivos afetados:**
  - `sgc.comum.erros.ErroEntidadeNaoEncontrada`
  - `sgc.comum.erros.ErroValidacao`
  - `sgc.comum.erros.ErroAccessoNegado`
  - `sgc.comum.erros.ErroSituacaoInvalida`
  - `sgc.processo.erros.ErroProcesso`
  - `sgc.alerta.erros.ErroAlerta`
  - E outras exceções de módulos

**Ação (exemplo para `ErroEntidadeNaoEncontrada`):**

```java
public class ErroEntidadeNaoEncontrada extends ErroNegocioBase {
    public ErroEntidadeNaoEncontrada(String entidade, Object id) {
        super(
            String.format("%s com identificador %s não foi encontrada.", entidade, id),
            "ENTIDADE_NAO_ENCONTRADA",
            HttpStatus.NOT_FOUND
        );
    }
}
```

**Estratégia de Migração:**
- Migrar uma exceção por vez
- Manter testes passando
- Atualizar handlers no `RestExceptionHandler` para usar `ErroNegocio` (próxima tarefa)

**Testes:**
- Para cada exceção migrada, verificar que `getCode()`, `getStatus()` funcionam
- Testes de integração (WebMvc) validando payload com `code`

#### Tarefa 1.5: Atualizar `RestExceptionHandler` para Usar `ErroNegocio`

- **Arquivo:** `backend/src/main/java/sgc/comum/erros/RestExceptionHandler.java`
- **Ação:**
  - Criar handler genérico para `ErroNegocio`:
  
  ```java
  @ExceptionHandler(ErroNegocio.class)
  protected ResponseEntity<Object> handleErroNegocio(ErroNegocio ex) {
      String traceId = UUID.randomUUID().toString();
      log.warn("[{}] Erro de negócio ({}): {}", traceId, ex.getCode(), ex.getMessage());
      
      ErroApi erroApi = new ErroApi(
          ex.getStatus(),
          sanitizar(ex.getMessage()),
          ex.getCode(),
          traceId
      );
      
      if (ex.getDetails() != null && !ex.getDetails().isEmpty()) {
          erroApi.setDetails(ex.getDetails());
      }
      
      return buildResponseEntity(erroApi);
  }
  ```
  
  - Remover handlers específicos individuais (ou mantê-los como fallback temporário)
  - Atualizar handlers de exceções genéricas (IllegalArgumentException, IllegalStateException) para incluir `code` genérico

**Testes:**
- Testes WebMvc verificando que erros retornam com `code` e `traceId`
- Validar logs incluindo `traceId`

#### Tarefa 1.6: Padronizar Logging (Severidade por Tipo)

- **Arquivo:** `backend/src/main/java/sgc/comum/erros/RestExceptionHandler.java`
- **Ação:**
  - Erros esperados (validação, 404, conflito de estado) → `WARN` sem stacktrace
  - Erros inesperados (500, IllegalArgumentException não esperado) → `ERROR` com stacktrace
  - Incluir `traceId` em todos os logs para correlação

```java
private void logErro(ErroNegocio ex, String traceId) {
    if (ex.getStatus().is4xxClientError()) {
        log.warn("[{}] Erro de cliente ({}): {}", traceId, ex.getCode(), ex.getMessage());
    } else {
        log.error("[{}] Erro do servidor ({}): {}", traceId, ex.getCode(), ex.getMessage(), ex);
    }
}
```

#### Tarefa 1.7: Documentar Contrato no Swagger/OpenAPI

- **Arquivo:** Classe de configuração Swagger (se existir, ex: `ConfigSwagger.java`)
- **Ação:**
  - Adicionar exemplo de resposta de erro em endpoints críticos
  - Documentar schema de `ErroApi` com todos os campos

```java
@Schema(description = "Resposta de erro padronizada")
public class ErroApi {
    @Schema(example = "12-12-2025 19:45:00")
    private LocalDateTime timestamp;
    
    @Schema(example = "422")
    private int status;
    
    @Schema(example = "A operação não pode ser executada no estado atual.")
    private String message;
    
    @Schema(example = "PROCESSO_ESTADO_INVALIDO")
    private String code;
    
    // ...
}
```

**Testes:**
- Validar que documentação OpenAPI inclui schema de erro
- Gerar JSON do Swagger e verificar campos

---

### 3.2. Fase 2: Frontend - Criar Normalizador de Erro

**Objetivo:** Centralizar lógica de interpretação de erros da API.

#### Tarefa 2.1: Criar `apiError.ts` com Tipos e Normalizador

- **Arquivo:** `frontend/src/utils/apiError.ts` (novo)
- **Ação:**
  - Definir interfaces para erro da API e erro normalizado
  - Implementar função `normalizeError(err: unknown): NormalizedError`

```typescript
/**
 * Payload de erro retornado pela API (backend)
 */
export interface ApiErrorPayload {
  timestamp?: string;
  status?: number;
  message?: string;
  code?: string;
  details?: Record<string, any>;
  traceId?: string;
  subErrors?: Array<{
    object?: string;
    field?: string;
    rejectedValue?: any;
    message?: string;
  }>;
}

/**
 * Categorias de erro para decisão de UX
 */
export type ErrorKind = 
  | 'validation'      // 400, 422 - erro de validação de dados
  | 'notFound'        // 404 - recurso não encontrado
  | 'conflict'        // 409 - conflito de estado
  | 'unauthorized'    // 401 - não autenticado
  | 'forbidden'       // 403 - sem permissão
  | 'network'         // Erro de rede
  | 'unexpected';     // 500 ou erro desconhecido

/**
 * Erro normalizado para consumo pelo frontend
 */
export interface NormalizedError {
  kind: ErrorKind;
  message: string;
  code?: string;
  status?: number;
  details?: Record<string, any>;
  subErrors?: Array<{ message?: string; field?: string; }>;
  traceId?: string;
  originalError?: unknown;
}

/**
 * Normaliza um erro (axios, Error, ou unknown) em uma estrutura previsível
 */
export function normalizeError(err: unknown): NormalizedError {
  // Erro de rede (sem response)
  if (isAxiosError(err) && !err.response) {
    return {
      kind: 'network',
      message: 'Não foi possível conectar ao servidor. Verifique sua conexão.',
      originalError: err
    };
  }

  // Erro HTTP com resposta da API
  if (isAxiosError(err) && err.response) {
    const { status, data } = err.response;
    // data can be unknown, cast to ApiErrorPayload if it matches
    const payload = (data || {}) as ApiErrorPayload;

    return {
      kind: mapStatusToKind(status),
      message: payload?.message || 'Erro desconhecido.',
      code: payload?.code,
      status: status,
      details: payload?.details,
      subErrors: payload?.subErrors,
      traceId: payload?.traceId,
      originalError: err
    };
  }

  // Erro genérico (Error, string, etc.)
  if (err instanceof Error) {
    return {
      kind: 'unexpected',
      message: err.message || 'Erro inesperado.',
      originalError: err
    };
  }

  // Fallback
  return {
    kind: 'unexpected',
    message: 'Erro desconhecido.',
    originalError: err
  };
}

/**
 * Mapeia status HTTP para categoria de erro
 */
function mapStatusToKind(status: number): ErrorKind {
  if (status === 400 || status === 422) return 'validation';
  if (status === 401) return 'unauthorized';
  if (status === 403) return 'forbidden';
  if (status === 404) return 'notFound';
  if (status === 409) return 'conflict';
  if (status >= 500) return 'unexpected';
  return 'unexpected';
}

/**
 * Type guard para AxiosError
 */
function isAxiosError(error: unknown): error is import('axios').AxiosError {
  return (
    error !== null &&
    typeof error === 'object' &&
    'isAxiosError' in error &&
    (error as any).isAxiosError === true
  );
}
```

**Testes:**
- Testes unitários com Vitest para cada tipo de erro (rede, 400, 401, 404, 409, 422, 500)
- Validar que `kind` é mapeado corretamente
- Validar que campos do backend (`code`, `traceId`, `details`) são preservados

#### Tarefa 2.2: Criar Helper `notifyError()`

- **Arquivo:** `frontend/src/utils/apiError.ts` (adicionar)
- **Ação:**
  - Função para exibir toast global baseado no `kind` do erro

```typescript
import { useFeedbackStore } from '@/stores/feedback';

/**
 * Exibe notificação de erro global (toast) baseado no tipo de erro.
 * Use para erros que devem ser mostrados globalmente (não inline).
 */
export function notifyError(normalized: NormalizedError): void {
  const feedbackStore = useFeedbackStore();

  // Títulos padrão por tipo
  const titles: Record<ErrorKind, string> = {
    validation: 'Erro de Validação',
    notFound: 'Não Encontrado',
    conflict: 'Conflito',
    unauthorized: 'Não Autorizado',
    forbidden: 'Acesso Negado',
    network: 'Erro de Rede',
    unexpected: 'Erro Inesperado'
  };

  const title = titles[normalized.kind];
  feedbackStore.show(title, normalized.message, 'danger');
}

/**
 * Decide se o erro deve ser notificado globalmente ou tratado inline.
 * Erros inline: validação, notFound, conflict (contexto de formulário)
 * Erros globais: unauthorized, network, unexpected
 */
export function shouldNotifyGlobally(normalized: NormalizedError): boolean {
  return ['unauthorized', 'forbidden', 'network', 'unexpected'].includes(normalized.kind);
}
```

**Testes:**
- Mock de `useFeedbackStore`
- Validar que `notifyError` chama `feedbackStore.show` com parâmetros corretos
- Validar lógica de `shouldNotifyGlobally`

---

### 3.3. Fase 3: Frontend - Refatorar Interceptor Axios

**Objetivo:** Remover lista hardcoded de status; usar normalizador; manter 401 como caso especial.

#### Tarefa 3.1: Atualizar `axios-setup.ts`

- **Arquivo:** `frontend/src/axios-setup.ts`
- **Ação:**
  - Importar `normalizeError`, `notifyError`, `shouldNotifyGlobally`
  - Refatorar `handleResponseError`:

```typescript
import { normalizeError, notifyError, shouldNotifyGlobally } from '@/utils/apiError';

const handleResponseError = (error: any) => {
  const normalized = normalizeError(error);

  // Caso especial: 401 - redirecionar para login
  if (normalized.kind === 'unauthorized') {
    const feedbackStore = useFeedbackStore();
    feedbackStore.show(
      'Não Autorizado',
      'Sua sessão expirou ou você não está autenticado. Faça login novamente.',
      'danger'
    );
    router.push('/login');
    return Promise.reject(error);
  }

  // Decidir se mostra toast global baseado no kind
  if (shouldNotifyGlobally(normalized)) {
    notifyError(normalized);
  }

  // Sempre rejeitar para permitir tratamento local
  return Promise.reject(error);
};
```

**Observação:** A lógica não mostra toast para erros inline (`validation`, `notFound`, `conflict`), mas ainda propaga o erro para tratamento local.

**Testes:**
- Mock de axios com diferentes respostas de erro
- Validar que 401 redireciona
- Validar que erros globais mostram toast
- Validar que erros inline **não** mostram toast global

---

### 3.4. Fase 4: Frontend - Refatorar `useApi`

**Objetivo:** Integrar normalizador; expor erro normalizado.

#### Tarefa 4.1: Atualizar `useApi.ts`

- **Arquivo:** `frontend/src/composables/useApi.ts`
- **Ação:**
  - Usar `normalizeError`
  - Expor `normalizedError` além de `error` (string)

```typescript
import type { AxiosError } from "axios";
import type { Ref } from "vue";
import { ref } from "vue";
import { normalizeError, type NormalizedError } from "@/utils/apiError";

export function useApi<T>(apiCall: (...args: any[]) => Promise<T>) {
  const data: Ref<T | null> = ref(null);
  const isLoading = ref(false);
  const error: Ref<string | null> = ref(null);
  const normalizedError: Ref<NormalizedError | null> = ref(null);

  const execute = async (...args: any[]): Promise<void> => {
    isLoading.value = true;
    error.value = null;
    normalizedError.value = null;
    data.value = null;

    try {
      data.value = await apiCall(...args);
    } catch (err) {
      normalizedError.value = normalizeError(err);
      error.value = normalizedError.value.message; // Retrocompatibilidade
      throw err;
    } finally {
      isLoading.value = false;
    }
  };

  const clearError = () => {
    error.value = null;
    normalizedError.value = null;
  };

  return { data, isLoading, error, normalizedError, execute, clearError };
}
```

**Testes:**
- Validar que `normalizedError` é populado corretamente
- Validar que `error` (string) continua funcionando (retrocompatibilidade)
- Componentes que usam `useApi` podem acessar `normalizedError.value.code`, `.details`, etc.

---

### 3.5. Fase 5: Frontend - Reduzir Repetição em Stores

**Objetivo:** Eliminar try/catch repetitivos; padronizar exibição de erros.

#### Tarefa 5.1: Criar Padrão de Store com Estado de Erro

**Estratégia:** Em vez de `try/catch` com `feedbackStore.show` em cada ação, stores devem:
- Armazenar último erro em `lastError: NormalizedError | null`
- Views/componentes decidem se exibem inline ou global

**Exemplo (refatoração de `mapas.ts`):**

```typescript
import { defineStore } from "pinia";
import { ref } from "vue";
import * as mapaService from "@/services/mapaService";
import { normalizeError, type NormalizedError } from "@/utils/apiError";

export const useMapasStore = defineStore("mapas", () => {
  const mapaCompleto = ref<MapaCompleto | null>(null);
  const lastError = ref<NormalizedError | null>(null);

  async function buscarMapaCompleto(codSubprocesso: number) {
    lastError.value = null;
    try {
      mapaCompleto.value = await mapaService.obterMapaCompleto(codSubprocesso);
    } catch (error) {
      lastError.value = normalizeError(error);
      mapaCompleto.value = null;
      throw error; // Re-throw para componente decidir UX
    }
  }

  function clearError() {
    lastError.value = null;
  }

  return { mapaCompleto, lastError, buscarMapaCompleto, clearError };
});
```

**Componente (View) consome:**

```vue
<template>
  <div>
    <BAlert v-if="mapasStore.lastError" variant="danger" dismissible @dismissed="mapasStore.clearError()">
      {{ mapasStore.lastError.message }}
      <div v-if="mapasStore.lastError.details">
        <small>Detalhes: {{ mapasStore.lastError.details }}</small>
      </div>
    </BAlert>
    <!-- Conteúdo do mapa -->
  </div>
</template>
```

**Ação para Migração:**
- Identificar stores com padrão repetitivo (grep `feedbackStore.show` em stores)
- Migrar um por vez:
  - Adicionar `lastError`
  - Remover `feedbackStore.show` de dentro do catch
  - Deixar componente/view decidir UX

**Stores a Migrar:**
- `stores/mapas.ts` (exemplo acima)
- `stores/processos.ts`
- `stores/subprocessos.ts`
- `stores/atividades.ts`
- Outros conforme necessário

**Testes:**
- Validar que `lastError` é populado em caso de erro
- Validar que componente consegue ler `lastError` e renderizar `BAlert`

#### Tarefa 5.2: Remover `feedbackStore.show` de Stores (Exceto Casos Globais)

**Regra:**
- **Remover** `feedbackStore.show` de operações onde o erro deve ser inline (formulários, modais)
- **Manter** `feedbackStore.show` para sucessos (ex: "Mapa salvo com sucesso!")
- Para erros globais inesperados, interceptor já cuida

**Buscar e Substituir:**
```bash
# Encontrar ocorrências
grep -r "feedbackStore.show" frontend/src/stores/ --include="*.ts" -n

# Para cada ocorrência, avaliar:
# - É feedback de sucesso? → Manter
# - É feedback de erro? → Remover, usar lastError
```

---

### 3.6. Fase 6: Frontend - Casos Especiais (404 = false, etc.)

**Objetivo:** Sistematizar conversões semânticas.

#### Tarefa 6.1: Refatorar `mapaService.verificarMapaVigente()`

- **Arquivo:** `frontend/src/services/mapaService.ts`
- **Situação Atual:**
  ```typescript
  try {
    await obterMapaVigente(codSubprocesso);
    return true;
  } catch (error) {
    if (isAxiosError(error) && error.response?.status === 404) {
      return false;
    }
    throw error;
  }
  ```

**Problema:** Lógica de "404 = false" espalhada.

**Solução:** Criar helpers genéricos para esse padrão.

- **Arquivo:** `frontend/src/utils/apiError.ts` (adicionar)

```typescript
/**
 * Executa uma chamada de API e retorna true se sucesso, false se 404.
 * Outros erros são propagados.
 */
export async function existsOrFalse<T>(
  apiCall: () => Promise<T>
): Promise<boolean> {
  try {
    await apiCall();
    return true;
  } catch (error) {
    const normalized = normalizeError(error);
    if (normalized.kind === 'notFound') {
      return false;
    }
    throw error;
  }
}

/**
 * Executa uma chamada de API e retorna null se 404.
 * Outros erros são propagados.
 */
export async function getOrNull<T>(
  apiCall: () => Promise<T>
): Promise<T | null> {
  try {
    return await apiCall();
  } catch (error) {
    const normalized = normalizeError(error);
    if (normalized.kind === 'notFound') {
      return null;
    }
    throw error;
  }
}
```

**Refatorar `verificarMapaVigente`:**

```typescript
import { existsOrFalse } from '@/utils/apiError';

export async function verificarMapaVigente(codSubprocesso: number): Promise<boolean> {
  return existsOrFalse(() => obterMapaVigente(codSubprocesso));
}
```

**Buscar Outros Casos:**
```bash
grep -r "status === 404" frontend/src/ --include="*.ts" -n
```

**Testes:**
- Validar que `existsOrFalse` retorna `false` para 404, `true` para sucesso
- Validar que outros erros são propagados
- Validar que `getOrNull` retorna `null` para 404

---

### 3.7. Fase 7: Documentação e Limpeza Final

#### Tarefa 7.1: Documentar Padrões em README

- **Arquivo:** `frontend/src/utils/README.md` (atualizar)
- **Ação:**
  - Documentar uso de `normalizeError`, `notifyError`
  - Exemplos de uso em stores e componentes
  - Guia de decisão: quando usar inline vs global

#### Tarefa 7.2: Atualizar `AGENTS.md`

- **Arquivo:** `/home/runner/work/sgc/sgc/AGENTS.md`
- **Ação:**
  - Adicionar seção sobre tratamento de erros padronizado
  - Instruir agentes a usar `normalizeError` e `notifyError`
  - Instruir agentes a evitar `feedbackStore.show` em stores (usar `lastError`)

#### Tarefa 7.3: Atualizar `plano-refatoracao-vue.md`

- **Arquivo:** `/home/runner/work/sgc/sgc/plano-refatoracao-vue.md`
- **Ação:**
  - Marcar item "Padronizar tratamento de erros" como concluído
  - Referenciar este plano (`plano-refatoracao-erros.md`)

---

## 4. Ordem de Execução Recomendada

### Sprint 1: Backend (Contrato Estável)
1. [x] Adicionar `code` e `traceId` a `ErroApi`
2. [x] Criar interface `ErroNegocio`
3. [x] Criar classe base `ErroNegocioBase`
4. [x] Migrar 2-3 exceções existentes para `ErroNegocioBase` (piloto)
5. [x] Atualizar `RestExceptionHandler` com handler genérico `ErroNegocio`
6. [x] Validar testes backend (todos devem passar)

### Sprint 2: Backend (Migração Completa)
7. [x] Migrar todas as exceções restantes (Nota: Algumas exceções ainda herdam de `RuntimeException` mas são tratadas corretamente pelo `RestExceptionHandler`. Migração estrutural completa pode ser feita gradualmente.)
8. [x] Padronizar logging com `traceId`
9. [x] Documentar contrato no Swagger
10. [x] Code review e ajustes

### Sprint 3: Frontend (Infraestrutura)
11. [x] Criar `apiError.ts` com normalizador e helpers
12. [x] Criar testes unitários para normalizador
13. [x] Refatorar interceptor Axios
14. [x] Refatorar `useApi` para usar normalizador

### Sprint 4: Frontend (Refatoração de Stores - Piloto)
15. [x] Migrar 2-3 stores (piloto): `mapas`, `processos` (Concluído)
16. [x] Atualizar views/componentes consumidores para usar `lastError` (Concluído: `CadMapa.vue`, `CadProcesso.vue`, `ProcessoView.vue`)
17. [x] Validar testes frontend e E2E (Em progresso - testes frontend verificados)

### Sprint 5: Frontend (Refatoração Completa)
18. [x] Migrar stores restantes (`subprocessos.ts`, `atividades.ts`, etc.)
19. [x] Remover `feedbackStore.show` de erros (manter sucessos)
20. [x] Refatorar casos especiais (404 = false)
21. [x] Validar testes completos (unit + E2E)

### Sprint 6: Documentação e Finalização
22. [ ] Atualizar documentação (READMEs, AGENTS.md)
23. [ ] Code review final
24. [ ] Smoke tests E2E completos
25. [ ] Merge para main

---

## 5. Critérios de Aceite

### Backend
- [x] `ErroApi` inclui `code` e `traceId` (quando aplicável)
- [x] Todas as exceções de negócio implementam `ErroNegocio` (ou são tratadas de forma compatível)
- [x] `RestExceptionHandler` retorna payload consistente para status 400, 404, 409, 422, 500
- [x] Logs incluem `traceId` para correlação
- [x] Testes backend (JUnit) passam 100%
- [x] Documentação Swagger inclui schema de erro com exemplos

### Frontend
- [x] `normalizeError` converte todos os tipos de erro para `NormalizedError`
- [x] Interceptor Axios não usa lista hardcoded de status
- [x] `useApi` expõe `normalizedError` com campos completos
- [x] Stores não repetem `try/catch` com `feedbackStore.show` para erros
- [x] Stores usam `lastError` e componentes decidem UX (inline vs global)
- [x] Casos especiais (404 = false) usam helpers `existsOrFalse`/`getOrNull`
- [x] Testes frontend (Vitest) passam 100%
- [ ] Testes E2E (Playwright) críticos passam (login, CRUD, navegação)

### Geral
- [ ] Sem `window.alert()` ou `window.confirm()` no código (exceto casos justificados)
- [ ] Mensagens de erro user-friendly (não vazam stacktraces ou mensagens técnicas)
- [ ] Documentação atualizada (READMEs, AGENTS.md, plano-refatoracao-vue.md)
- [ ] Code review aprovado por pelo menos 2 revisores

---
