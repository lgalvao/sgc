# 🚨 Estratégia de Tratamento de Erros - Frontend SGC

**Data de Criação:** 2026-01-31  
**Status:** Padrão Oficial

---

## 📋 Resumo Executivo

Este documento define o **padrão oficial** para tratamento e exibição de erros no frontend do SGC. O objetivo é garantir uma **experiência de usuário consistente** e **código manutenível**.

---

## 🎯 Princípios Norteadores

1. **Consistência:** Mesmos tipos de erro sempre exibidos da mesma forma
2. **Clareza:** Mensagens de erro compreensíveis para o usuário final
3. **Contextualização:** Erros exibidos próximos ao contexto onde ocorreram
4. **Não-intrusividade:** Não bloquear a interface desnecessariamente

---

## 📊 Estratégia por Tipo de Erro

### 1. Erros de Negócio (Validações)

**Quando usar:** Erros retornados pela API com status 400, 422 (validação de negócio)

**Como exibir:** `<BAlert>` inline no contexto da ação

**Exemplo:**
```vue
<template>
  <BAlert 
    v-if="processosStore.lastError" 
    :model-value="true" 
    variant="danger"
    dismissible
    @dismissed="processosStore.clearError()"
  >
    {{ processosStore.lastError.message }}
    <div v-if="processosStore.lastError.details">
      <small>Detalhes: {{ processosStore.lastError.details }}</small>
    </div>
  </BAlert>
</template>
```

**Justificativa:**
- Erros de negócio são **esperados** e fazem parte do fluxo normal
- Usuário precisa ver o erro **no contexto** da ação (ex: formulário)
- Não deve bloquear outras partes da interface

---

### 2. Erros de Sistema/Infraestrutura

**Quando usar:** Erros 500, timeout, problemas de conexão, erros inesperados

**Como exibir:** Toast global via `useFeedbackStore().show()`

**Exemplo:**
```typescript
import { useFeedbackStore } from '@/stores/feedback';

try {
  await processosStore.finalizarProcesso(codigo);
  feedbackStore.show(
    'Sucesso',
    'Processo finalizado com sucesso.',
    'success'
  );
} catch (error: any) {
  feedbackStore.show(
    'Erro ao finalizar',
    error.message || 'Erro inesperado no sistema. Tente novamente.',
    'danger'
  );
}
```

**Justificativa:**
- Erros de sistema são **inesperados** e graves
- Toast global garante visibilidade independente do contexto
- Não polui a interface com alertas permanentes

---

### 3. Erros de Autorização

**Quando usar:** Erros 401, 403 (não autenticado, sem permissão)

**Como exibir:** Modal de erro (casos críticos) ou BAlert inline (casos menores)

**Exemplo:**
```vue
<!-- Para casos onde o usuário pode continuar usando a aplicação -->
<BAlert variant="warning" :model-value="true">
  <i class="bi bi-shield-exclamation" aria-hidden="true"/>
  Você não tem permissão para realizar esta ação.
</BAlert>

<!-- Para casos onde o usuário precisa tomar ação -->
<ModalConfirmacao
  v-model="mostrarErroAutorizacao"
  titulo="Acesso Negado"
  variant="danger"
>
  Sua sessão expirou. Faça login novamente.
</ModalConfirmacao>
```

**Justificativa:**
- Erros de autorização podem bloquear funcionalidades inteiras
- Modal garante que o usuário veja e tome ação
- BAlert inline para casos onde há funcionalidades alternativas

---

### 4. Confirmações de Ações Destrutivas

**Quando usar:** Exclusão, cancelamento, finalização de processos

**Como exibir:** Modal de confirmação **antes** da ação

**Exemplo:**
```vue
<ModalConfirmacao
  v-model="mostrarModalExclusao"
  titulo="Confirmar Exclusão"
  variant="danger"
  test-id-confirmar="btn-confirmar-exclusao"
  test-id-cancelar="btn-cancelar-exclusao"
  @confirmar="executarExclusao"
>
  <BAlert :fade="false" :model-value="true" variant="warning">
    <i class="bi bi-exclamation-triangle" aria-hidden="true"/>
    Tem certeza que deseja excluir <strong>{{ item.nome }}</strong>?
    Esta ação não pode ser desfeita.
  </BAlert>
</ModalConfirmacao>
```

**Justificativa:**
- Previne ações acidentais
- Dá ao usuário chance de reconsiderar
- Claramente separa confirmação de erro

---

## 🛠️ Componentes e Ferramentas

### Componentes Disponíveis

1. **`<BAlert>`** (BootstrapVueNext)
   - Erros inline
   - Variantes: `danger`, `warning`, `info`, `success`
   - Sempre incluir `dismissible` para erros

2. **`<ModalConfirmacao>`** (Componente customizado)
   - Confirmações de ações
   - Erros que bloqueiam funcionalidades

3. **`useFeedbackStore()`** (Pinia Store)
   - Toast global
   - Método: `show(titulo, mensagem, variant)`

### Composables

1. **`useErrorHandler()`**
   - Normalização de erros
   - Tracking de último erro
   - Método `withErrorHandling()`

---

## 📐 Matriz de Decisão

| Situação | Componente | Exemplo |
|----------|-----------|---------|
| Validação de formulário | `<BAlert>` inline | "CPF inválido" |
| Falha ao salvar dados | `<BAlert>` inline | "Erro ao salvar: campo obrigatório" |
| Erro de conexão | `Toast (feedback)` | "Erro de conexão com servidor" |
| Erro 500 | `Toast (feedback)` | "Erro interno do sistema" |
| Sessão expirada (401) | `Modal` | "Faça login novamente" |
| Sem permissão (403) | `<BAlert>` inline | "Você não tem permissão" |
| Confirmar exclusão | `ModalConfirmacao` | "Deseja excluir?" |
| Confirmar finalização | `ModalConfirmacao` | "Finalizar processo?" |

---

## ✅ Checklist de Implementação

Ao implementar tratamento de erros em uma nova feature:

- [ ] Identificar tipo de erro (negócio, sistema, autorização)
- [ ] Escolher componente apropriado (BAlert, Toast, Modal)
- [ ] Usar `withErrorHandling()` na store para capturar erros
- [ ] Exibir `lastError` da store com `<BAlert>` quando apropriado
- [ ] Incluir botão de dismissal (`dismissible`) em erros não-críticos
- [ ] Adicionar `test-id` para testes E2E
- [ ] Testar fluxo de erro manualmente

---

## 🚫 Anti-Padrões (Evitar)

❌ **NÃO fazer:**

1. **Misturar estratégias sem motivo:**
   ```typescript
   // ❌ ERRADO: Erro de negócio em toast
   toast.error("Campo obrigatório");
   ```

2. **Alerts sem contexto:**
   ```vue
   <!-- ❌ ERRADO: Alert genérico no topo da página -->
   <BAlert variant="danger">Erro</BAlert>
   ```

3. **Console.log em produção:**
   ```typescript
   // ❌ ERRADO: Usar console em vez de logger
   console.error("Erro ao salvar");
   ```

4. **Engolir erros silenciosamente:**
   ```typescript
   // ❌ ERRADO: Catch vazio
   try {
     await salvar();
   } catch (e) {
     // nada
   }
   ```

✅ **FAZER:**

1. **BAlert para erros de negócio:**
   ```vue
   <BAlert 
     v-if="store.lastError" 
     variant="danger" 
     dismissible
     @dismissed="store.clearError()"
   >
     {{ store.lastError.message }}
   </BAlert>
   ```

2. **Toast para erros de sistema:**
   ```typescript
   feedbackStore.show('Erro', error.message, 'danger');
   ```

3. **Logger estruturado:**
   ```typescript
   import { logger } from '@/utils';
   logger.error('Erro ao salvar processo:', error);
   ```

---

## 📚 Exemplos de Código

### Exemplo Completo: View com Tratamento de Erros

```vue
<template>
  <BContainer class="mt-4">
    <!-- Erro de negócio: BAlert inline -->
    <BAlert
      v-if="processosStore.lastError"
      :model-value="true"
      variant="danger"
      dismissible
      @dismissed="processosStore.clearError()"
    >
      {{ processosStore.lastError.message }}
    </BAlert>

    <!-- Conteúdo da view -->
    <BForm @submit.prevent="salvar">
      <!-- ... campos ... -->
      <BButton type="submit" variant="primary">Salvar</BButton>
    </BForm>

    <!-- Confirmação de ação destrutiva -->
    <ModalConfirmacao
      v-model="mostrarModalExclusao"
      titulo="Confirmar Exclusão"
      variant="danger"
      @confirmar="excluir"
    >
      Deseja excluir este item?
    </ModalConfirmacao>
  </BContainer>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useProcessosStore } from '@/stores/processos';
import { useFeedbackStore } from '@/stores/feedback';

const processosStore = useProcessosStore();
const feedbackStore = useFeedbackStore();
const mostrarModalExclusao = ref(false);

async function salvar() {
  try {
    await processosStore.criarProcesso(dados);
    // Sucesso: Toast global
    feedbackStore.show('Sucesso', 'Processo criado com sucesso', 'success');
  } catch (error: any) {
    // Erro de negócio: já capturado em processosStore.lastError
    // Será exibido no BAlert inline
    
    // Erro de sistema: Toast global
    if (error.isSystemError) {
      feedbackStore.show('Erro', 'Erro inesperado. Tente novamente.', 'danger');
    }
  }
}

async function excluir() {
  try {
    await processosStore.removerProcesso(codigo);
    feedbackStore.show('Sucesso', 'Item excluído', 'success');
    mostrarModalExclusao.value = false;
  } catch (error: any) {
    feedbackStore.show('Erro ao excluir', error.message, 'danger');
  }
}
</script>
```

---

## 🔄 Migração de Código Existente

Se encontrar código que não segue este padrão:

1. **Identifique o tipo de erro**
2. **Refatore para o componente correto**
3. **Teste o fluxo de erro**
4. **Atualize testes E2E se necessário**

---

## 📞 Dúvidas?

Em caso de dúvida sobre qual estratégia usar:

1. Consulte a **Matriz de Decisão** acima
2. Verifique exemplos em `ProcessoView.vue`, `SubprocessoView.vue`
3. Pergunte ao time em code review

---

**Última Atualização:** 2026-01-31  
**Responsável:** Time de Desenvolvimento SGC
