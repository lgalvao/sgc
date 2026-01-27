# Design Guidelines & Standards

This document serves as the source of truth for UI/UX development in the frontend application. It enforces consistency
across Views, Components, and Interactions using **Bootstrap 5.3** and **BootstrapVueNext 0.42**.

> **Last Updated:** Janeiro 2026  
> **BootstrapVueNext Version:** 0.42.0  
> **Vue Version:** 3.5.27  
> **Target:** Clean, minimal, accessible UI with consistent UX patterns

## 📚 Table of Contents

1. [Core Principles](#-core-principles)
2. [Layout & Typography](#-layout--typography)
3. [Components](#-components)
4. [State Management & API](#-state-management--api)
5. [Composables](#-composables)
6. [Accessibility](#-accessibility)
7. [Component Inventory](#-component-inventory)
8. [Anti-Patterns](#-anti-patterns)
9. [Decision Trees](#-decision-trees)

---

## 🎨 Core Principles

1. **Bootstrap First:** Use standard Bootstrap 5 utility classes (`p-3`, `d-flex`, `text-muted`) over custom CSS.
   Leverage Bootstrap's grid, spacing, and flexbox utilities.
2. **Component Reusability:** Never duplicate modal logic or complex layouts. Use shared components. If you write the
   same code twice, create a component.
3. **Feedback:** Every user action (Save, Delete, Load) must provide visual feedback (Spinners, Toasts, Alerts).
4. **Accessibility:** All interactive elements must be keyboard-navigable. Use semantic HTML and ARIA attributes where
   needed.
5. **Minimal Code:** Prefer built-in BootstrapVueNext features over custom implementations. Less code = fewer bugs.
6. **Composition API:** Use `<script setup lang="ts">` for all components. Leverage composables for reusable logic.

---

## 📐 Layout & Typography

### Page Structure

**Standard Pattern (Current Implementation):**

```vue
<template>
  <BContainer class="mt-4">
    <!-- Page Header -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h2 class="mb-0">Título da Página</h2>
      <div class="d-flex gap-2">
        <BButton variant="primary">
          <i class="bi bi-plus-lg me-1" aria-hidden="true"></i>
          Criar Novo
        </BButton>
      </div>
    </div>

    <!-- Alerts (if needed) -->
    <BAlert v-model="showAlert" :variant="alertVariant" dismissible class="mt-3">
      {{ alertMessage }}
    </BAlert>

    <!-- Content Area -->
    <div>
      <!-- Tables, Forms, Cards, etc. -->
    </div>
  </BContainer>
</template>
```

**Future Pattern (with PageHeader component):**

```vue
<template>
  <BContainer class="mt-4">
    <PageHeader
      title="Título da Página"
      subtitle="Descrição opcional do contexto"
    >
      <template #actions>
        <BButton variant="primary" to="/novo">
          <i class="bi bi-plus-lg me-1" aria-hidden="true"></i>
          Criar Novo
        </BButton>
      </template>
    </PageHeader>

    <div class="content-wrapper">
       <!-- Content -->
    </div>
  </BContainer>
</template>
```

### Typography Hierarchy

| Element            | Class                   | Usage                      | Example Files           |
|--------------------|-------------------------|----------------------------|-------------------------|
| **Page Title**     | `h2` ou `display-6`     | Título principal da página | PainelView, CadProcesso |
| **Section Title**  | `h4` ou `h5` com `mb-3` | Seções dentro da página    | VisMapa, CadAtividades  |
| **Subsection**     | `h6`                    | Sub-seções menores         | Raramente usado         |
| **Body Text**      | (default)               | Texto padrão               | Todos                   |
| **Secondary Text** | `text-muted`            | Informação secundária      | Descrições, hints       |
| **Small Text**     | `small` ou `fs-7`       | Textos auxiliares          | Form descriptions       |

**⚠️ Current Inconsistency:**

- Some views use `display-6`, others use `h2` for page titles
- **Recommendation:** Standardize on `h2` for semantic HTML (better for accessibility)
- Use `display-6` only for marketing/landing pages

### Spacing Conventions

- **Container Top Margin:** Always `mt-4` on `BContainer`
- **Header Bottom Margin:** `mb-3` after page header
- **Section Spacing:** `mb-4` or `mb-5` between major sections
- **Form Group Spacing:** `mb-3` on all `BFormGroup`
- **Button Group Gap:** `gap-2` on flex containers

---

## 🧩 Components

### Buttons (`BButton`)

**Variants & Usage:**

```vue
<!-- Primary Action (main workflow action) -->
<BButton variant="primary">
  <i class="bi bi-save me-1" aria-hidden="true"></i>
  Salvar
</BButton>

<!-- Success (final positive action) -->
<BButton variant="success">
  <i class="bi bi-play-fill me-1" aria-hidden="true"></i>
  Iniciar Processo
</BButton>

<!-- Danger (destructive action) -->
<BButton variant="danger">
  <i class="bi bi-trash me-1" aria-hidden="true"></i>
  Remover
</BButton>

<!-- Secondary (cancel, alternative action) -->
<BButton variant="secondary">
  Cancelar
</BButton>

<!-- Outline (less prominent action) -->
<BButton variant="outline-primary">
  <i class="bi bi-pencil me-1" aria-hidden="true"></i>
  Editar
</BButton>

<!-- Link (tertiary action, navigation) -->
<BButton variant="link" to="/painel">
  Voltar
</BButton>
```

**Loading States (Current Pattern):**

```vue
<BButton :disabled="isLoading" variant="primary">
  <BSpinner v-if="isLoading" small class="me-1" />
  <i v-else class="bi bi-save me-1" aria-hidden="true"></i>
  {{ isLoading ? 'Salvando...' : 'Salvar' }}
</BButton>
```

**Icon Guidelines:**

- Always use `me-1` spacing after icon (before text)
- Add `aria-hidden="true"` to decorative icons
- Use Bootstrap Icons (`bi-*`) library consistently
- Icon + text is better than icon-only for clarity

**Button Grouping:**

```vue
<!-- Horizontal group with consistent spacing -->
<div class="d-flex gap-2">
  <BButton variant="primary">Ação Principal</BButton>
  <BButton variant="outline-secondary">Ação Secundária</BButton>
</div>

<!-- Justified layout (action left, cancel right) -->
<div class="d-flex justify-content-between">
  <BButton variant="danger">Remover</BButton>
  <BButton variant="link">Cancelar</BButton>
</div>
```

---

### Modals (`BModal`)

**❌ DO NOT use inline `<BModal>` for standard confirmations.**

**Current Issues Found:**

- 23 inline BModal instances across 15 files
- Inconsistent `fade` prop usage
- Duplicated footer logic
- Mixed button order in footers

**✅ Use `ModalConfirmacao.vue` for confirmations:**

```vue
<template>
  <ModalConfirmacao
    v-model="mostrarModal"
    titulo="Remover Processo"
    mensagem="Remover o processo? Esta ação não poderá ser desfeita."
    variant="danger"
    @confirmar="handleConfirmar"
  />
</template>

<script setup lang="ts">
import ModalConfirmacao from '@/components/ModalConfirmacao.vue'
import { ref } from 'vue'

const mostrarModal = ref(false)

function handleConfirmar() {
  // Execute action
}
</script>
```

**Props da ModalConfirmacao:**

- `titulo` (string): Título do modal
- `mensagem` (string, optional): Mensagem de confirmação
- `variant` (string): `danger`, `warning`, `primary` (default)
- `testIdConfirmar` (string, optional): Test ID para botão confirmar
- `testIdCancelar` (string, optional): Test ID para botão cancelar

**Behavior:**

- Always `centered`
- Auto-focus on **Cancel** button for `variant="danger"` (safety feature)
- ESC key closes modal (accessible)
- Button order: Cancel (left, secondary) | Confirm (right, variant)

**For Complex Modals (Forms, Multi-step):**

```vue
<template>
  <BModal
    v-model="mostrarModal"
    title="Criar Nova Competência"
    centered
    :fade="false"
    hide-footer
  >
    <BFormGroup label="Descrição" class="mb-3">
      <BFormInput v-model="descricao" />
    </BFormGroup>

    <!-- Custom Footer -->
    <template #footer>
      <BButton variant="secondary" @click="fecharModal">
        <i class="bi bi-x-circle me-1" aria-hidden="true"></i>
        Cancelar
      </BButton>
      <BButton variant="primary" :disabled="!isValid" @click="salvar">
        <BSpinner v-if="salvando" small class="me-1" />
        <i v-else class="bi bi-check-circle me-1" aria-hidden="true"></i>
        {{ salvando ? 'Salvando...' : 'Salvar' }}
      </BButton>
    </template>
  </BModal>
</template>
```

**Modal Best Practices (BootstrapVueNext 0.42):**

- Use `v-model` for open/close state (reactive)
- Use `:fade="false"` for better performance (optional, but consistent)
- Use `hide-footer` + `<template #footer>` for custom footers
- Always provide a close mechanism (X button, Cancel, ESC key)
- Use `no-close-on-backdrop` for critical actions only
- Keep modal content focused and concise

---

### Forms

**Standard Form Group:**

```vue
<BFormGroup
  class="mb-3"
  label="Descrição"
  label-for="descricao"
>
  <BFormInput
    id="descricao"
    v-model="descricao"
    :state="fieldErrors.descricao ? false : null"
    placeholder="Descreva o processo"
  />
  <BFormInvalidFeedback :state="fieldErrors.descricao ? false : null">
    {{ fieldErrors.descricao }}
  </BFormInvalidFeedback>
</BFormGroup>
```

**Validation Patterns:**

- Use `useFormErrors` composable for managing field errors
- Set `:state="false"` to show invalid state
- Set `:state="null"` for neutral (no validation yet)
- Set `:state="true"` for valid state (optional, use sparingly)

**Form Loading State:**

```vue
<BForm @submit.prevent="handleSubmit">
  <BFormGroup v-for="field in fields" :key="field.name" class="mb-3">
    <!-- fields -->
  </BFormGroup>

  <!-- Disable ALL inputs during submit -->
  <BButton type="submit" :disabled="isLoading || !isFormValid">
    <BSpinner v-if="isLoading" small class="me-1" />
    {{ isLoading ? 'Salvando...' : 'Salvar' }}
  </BButton>
</BForm>
```

**Error Display Methods:**

| Method                  | Usage              | When to Use                      |
|-------------------------|--------------------|----------------------------------|
| `BFormInvalidFeedback`  | Field-level errors | Input validation errors          |
| `BAlert` (danger)       | Form-level errors  | Multiple errors, API errors      |
| `div.text-danger.small` | ❌ Avoid            | Use BFormInvalidFeedback instead |

---

### Tables

**Standard Table with Empty State:**

```vue
<BTable
  hover
  striped
  responsive
  :items="items"
  :fields="fields"
  @row-clicked="handleRowClick"
>
  <template #cell(acoes)="{ item }">
    <BButton size="sm" variant="outline-primary" @click="editar(item)">
      <i class="bi bi-pencil" aria-hidden="true"></i>
    </BButton>
  </template>

  <!-- Empty State -->
  <template #empty>
    <div class="text-center py-5 text-muted">
      <i class="bi bi-inbox display-4 d-block mb-3" aria-hidden="true"></i>
      <p class="mb-0">Nenhum registro encontrado.</p>
    </div>
  </template>
</BTable>
```

**Table Best Practices:**

- Use `:hover="true"` for clickable rows
- Add `cursor: pointer` styling (already in style.css)
- Always handle empty state with EmptyState component or inline template
- Use `responsive` prop for mobile-friendly tables
- Keep action columns narrow and right-aligned

---

### Alerts (`BAlert`)

**Page-Level Alerts (Local State):**

```vue
<BAlert
  v-model="showAlert"
  :variant="alertVariant"
  dismissible
  fade
  class="mt-3"
>
  <h4 v-if="alertTitle" class="alert-heading">{{ alertTitle }}</h4>
  <p class="mb-0">{{ alertMessage }}</p>
</BAlert>
```

**Variants:**

- `success`: Operação bem-sucedida
- `danger`: Erro crítico
- `warning`: Aviso importante
- `info`: Informação geral

**Toast Notifications (Global, via useToast):**

```ts
import { useToast } from 'bootstrap-vue-next'

const toast = useToast()

// Success
toast.success('Processo criado com sucesso!')

// Error
toast.danger('Erro ao salvar: ' + error.message)

// Info
toast.info('Processando...')
```

**When to Use:**

- **BAlert (local):** Persistent page-level messages, validation summaries
- **Toast (global):** Transient feedback, success/error notifications
- **BFormInvalidFeedback:** Field-level validation errors

---

### Cards (`BCard`)

```vue
<BCard>
  <BCardHeader>
    <h5 class="mb-0">Título do Card</h5>
  </BCardHeader>
  <BCardBody>
    <BCardTitle>Subtítulo</BCardTitle>
    <BCardText>Conteúdo do card...</BCardText>
  </BCardBody>
  <BCardFooter>
    <BButton variant="primary">Ação</BButton>
  </BCardFooter>
</BCard>
```

**Best Practices:**

- Use semantic components (`BCardHeader`, `BCardBody`, `BCardFooter`)
- Avoid nesting cards in cards
- Use cards for grouped content, not as generic containers
- Keep card actions in footer for consistency

---

## 🛠 State Management & API

### Pinia Stores (Setup Stores Pattern)

**Standard Store Structure:**

```ts
// stores/exemplo.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Exemplo } from '@/types/tipos'
import * as exemploService from '@/services/exemploService'

export const useExemploStore = defineStore('exemplo', () => {
  // State
  const exemplos = ref<Exemplo[]>([])
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  // Getters (computed)
  const exemplosPorTipo = computed(() => {
    return (tipo: string) => exemplos.value.filter(e => e.tipo === tipo)
  })

  // Actions
  async function buscarExemplos() {
    isLoading.value = true
    error.value = null
    try {
      exemplos.value = await exemploService.listar()
    } catch (e) {
      error.value = normalizeError(e)
      throw e // Re-throw for component handling
    } finally {
      isLoading.value = false
    }
  }

  return {
    // State
    exemplos,
    isLoading,
    error,
    // Getters
    exemplosPorTipo,
    // Actions
    buscarExemplos,
  }
})
```

**Best Practices:**

- Use **Setup Stores** (composition function style) instead of Options API
- State: `ref()` for primitives and objects
- Getters: `computed()` for derived state
- Actions: async functions for API calls
- Always handle loading and error states
- Re-throw errors for component-level handling

---

### Service Layer Pattern

**View → Store → Service → API**

```ts
// services/exemploService.ts
import api from '@/axios-setup'
import type { Exemplo, CriarExemploRequest } from '@/types/tipos'

export async function listar(): Promise<Exemplo[]> {
  const response = await api.get<Exemplo[]>('/api/exemplos')
  return response.data
}

export async function criar(request: CriarExemploRequest): Promise<Exemplo> {
  const response = await api.post<Exemplo>('/api/exemplos', request)
  return response.data
}
```

**Component Usage:**

```vue
<script setup lang="ts">
import { useExemploStore } from '@/stores/exemplo'
import { storeToRefs } from 'pinia'
import { onMounted } from 'vue'

const exemploStore = useExemploStore()
const { exemplos, isLoading } = storeToRefs(exemploStore)

onMounted(async () => {
  await exemploStore.buscarExemplos()
})
</script>
```

---

### Error Handling

**Component-Level (Local):**

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { useFeedbackStore } from '@/stores/feedback'
import { normalizeError } from '@/utils'

const feedbackStore = useFeedbackStore()
const localError = ref<string | null>(null)

async function salvar() {
  localError.value = null
  try {
    await store.salvar(dados)
    feedbackStore.adicionarMensagem('Salvo com sucesso!', 'success')
  } catch (e) {
    localError.value = normalizeError(e)
    // Show error inline, don't use toast for expected errors
  }
}
</script>

<template>
  <BAlert v-if="localError" variant="danger" dismissible>
    {{ localError }}
  </BAlert>
</template>
```

**Global Feedback (Toast):**

```ts
import { useToast } from 'bootstrap-vue-next'

const toast = useToast()

try {
  await action()
  toast.success('Operação concluída!')
} catch (e) {
  toast.danger('Erro: ' + normalizeError(e))
}
```

**When to Use:**

- **Local BAlert:** Expected errors, validation issues, form-level errors
- **Global Toast:** Success feedback, unexpected errors, background operations
- **BFormInvalidFeedback:** Field-level validation

---

## 🎯 Composables

### Built-in BootstrapVueNext Composables

**useToast (Recommended):**

```ts
import { useToast } from 'bootstrap-vue-next'

export function useNotifications() {
  const toast = useToast()

  function notificarSucesso(mensagem: string) {
    toast.success(mensagem, {
      title: 'Sucesso',
      autoHide: true,
      delay: 3000
    })
  }

  function notificarErro(erro: unknown) {
    toast.danger(normalizeError(erro), {
      title: 'Erro',
      autoHide: true,
      delay: 5000
    })
  }

  return { notificarSucesso, notificarErro }
}
```

**useModal (Programmatic Modals):**

```ts
import { useModal } from 'bootstrap-vue-next'

export function useConfirmDialog() {
  const modal = useModal()

  async function confirmar(titulo: string, mensagem: string): Promise<boolean> {
    return new Promise((resolve) => {
      modal.show({
        title: titulo,
        body: mensagem,
        centered: true,
        onOk: () => resolve(true),
        onCancel: () => resolve(false),
      })
    })
  }

  return { confirmar }
}
```

---

### Custom Composables (Existing)

| Composable       | Purpose                      | Usage                                                                 |
|------------------|------------------------------|-----------------------------------------------------------------------|
| `useFormErrors`  | Manage form field errors     | `const { fieldErrors, setFieldError, clearErrors } = useFormErrors()` |
| `usePerfil`      | Access user profile state    | `const { perfil, unidade } = usePerfil()`                             |
| `useBreadcrumbs` | Manage breadcrumb navigation | `const { setBreadcrumbs } = useBreadcrumbs()`                         |
| `useApi`         | Shared API request wrapper   | Internal use in services                                              |

**Composable Best Practices:**

- **Name with `use` prefix:** `useMyComposable`
- **Return object with named exports:** `return { state, action }`
- **Keep composables focused:** One responsibility per composable
- **Don't call UI methods:** No `toast()` or `alert()` inside composables
- **Return error state:** Let component decide how to display errors
- **Use TypeScript:** Always type parameters and return values

**Example Custom Composable:**

```ts
// composables/useProcessoForm.ts
import { ref, computed } from 'vue'
import type { CriarProcessoRequest } from '@/types/tipos'

export function useProcessoForm() {
  const descricao = ref('')
  const tipo = ref('MAPEAMENTO')
  const dataLimite = ref('')

  const isFormValid = computed(() => {
    return descricao.value.trim() !== '' && dataLimite.value !== ''
  })

  function limparFormulario() {
    descricao.value = ''
    tipo.value = 'MAPEAMENTO'
    dataLimite.value = ''
  }

  function construirRequest(): CriarProcessoRequest {
    return {
      descricao: descricao.value,
      tipo: tipo.value,
      dataLimite: dataLimite.value,
    }
  }

  return {
    descricao,
    tipo,
    dataLimite,
    isFormValid,
    limparFormulario,
    construirRequest,
  }
}
```

---

## ♿ Accessibility

### Keyboard Navigation

- **All interactive elements must be keyboard-accessible**
- Use semantic HTML (`<button>`, `<a>`, `<input>`)
- Test with Tab, Enter, Escape keys
- Ensure logical tab order

### ARIA Attributes

```vue
<!-- Decorative icons (screen readers should skip) -->
<i class="bi bi-save me-1" aria-hidden="true"></i>

<!-- Icon-only buttons (need labels) -->
<BButton aria-label="Editar processo">
  <i class="bi bi-pencil"></i>
</BButton>

<!-- Loading states -->
<BButton :disabled="loading" aria-busy="loading">
  <BSpinner v-if="loading" aria-hidden="true" />
  {{ loading ? 'Salvando...' : 'Salvar' }}
</BButton>

<!-- Dynamic content regions -->
<div role="alert" aria-live="polite">
  {{ statusMessage }}
</div>
```

### Focus Management

**Modal Auto-Focus (Safety Pattern):**

```ts
// ModalConfirmacao.vue already implements this
function onShown() {
  if (props.variant === 'danger' && btnCancelar.value?.$el) {
    btnCancelar.value.$el.focus() // Focus Cancel for destructive actions
  }
}
```

**Form Validation Focus:**

```ts
async function handleSubmit() {
  if (!isFormValid.value) {
    // Focus first invalid field
    const firstError = document.querySelector('.is-invalid')
    if (firstError instanceof HTMLElement) {
      firstError.focus()
    }
  }
}
```

---

## 📦 Component Inventory

### Layout Components

| Component    | Status    | Location                    | Usage                         |
|--------------|-----------|-----------------------------|-------------------------------|
| PageHeader   | ❌ Missing | -                           | Standardize page headers      |
| AppContainer | ❌ Missing | -                           | Wrap BContainer with defaults |
| EmptyState   | ✅ Exists  | `components/EmptyState.vue` | Table empty states            |

### Modal Components

| Component               | Status   | Location                                 | Usage                 |
|-------------------------|----------|------------------------------------------|-----------------------|
| ModalConfirmacao        | ✅ Exists | `components/ModalConfirmacao.vue`        | Generic confirmations |
| SubprocessoModal        | ✅ Exists | `components/SubprocessoModal.vue`        | Create subprocess     |
| AceitarMapaModal        | ✅ Exists | `components/AceitarMapaModal.vue`        | Accept competency map |
| DisponibilizarMapaModal | ✅ Exists | `components/DisponibilizarMapaModal.vue` | Publish map           |
| CriarCompetenciaModal   | ✅ Exists | `components/CriarCompetenciaModal.vue`   | Create competency     |
| ImpactoMapaModal        | ✅ Exists | `components/ImpactoMapaModal.vue`        | View impact report    |

**Issues:**

- 23 inline BModal instances should use ModalConfirmacao
- Specialized modals should extend ModalConfirmacao's footer pattern

### Form Components

| Component            | Status             | Usage                               |
|----------------------|--------------------|-------------------------------------|
| BFormGroup           | ✅ BootstrapVueNext | Form field wrapper                  |
| BFormInput           | ✅ BootstrapVueNext | Text input                          |
| BFormSelect          | ✅ BootstrapVueNext | Dropdown                            |
| BFormTextarea        | ✅ BootstrapVueNext | Multi-line text                     |
| BFormInvalidFeedback | ✅ BootstrapVueNext | Validation errors                   |
| FormField            | ❌ Missing          | Combined input + validation wrapper |

### Navigation Components

| Component      | Status   | Location                        |
|----------------|----------|---------------------------------|
| MainNavbar     | ✅ Exists | `components/MainNavbar.vue`     |
| BarraNavegacao | ✅ Exists | `components/BarraNavegacao.vue` |

### Data Display Components

| Component           | Status   | Location                             |
|---------------------|----------|--------------------------------------|
| TabelaProcessos     | ✅ Exists | `components/TabelaProcessos.vue`     |
| TabelaAlertas       | ✅ Exists | `components/TabelaAlertas.vue`       |
| TabelaMovimentacoes | ✅ Exists | `components/TabelaMovimentacoes.vue` |
| TreeTableView       | ✅ Exists | `components/TreeTableView.vue`       |
| ProcessoCard        | ✅ Exists | `components/ProcessoCard.vue`        |
| CompetenciaCard     | ✅ Exists | `components/CompetenciaCard.vue`     |

---

## ⚠️ Anti-Patterns (Do Not Do This)

### 1. Inline Modals for Generic Confirmations

**❌ Bad (23 instances found):**

```vue
<BModal v-model="showDelete" title="Remover" hide-footer>
  <p>Confirmar remoção?</p>
  <template #footer>
    <BButton @click="showDelete = false">Cancelar</BButton>
    <BButton variant="danger" @click="confirmar">Remover</BButton>
  </template>
</BModal>
```

**✅ Good:**

```vue
<ModalConfirmacao
  v-model="showDelete"
  titulo="Remover Processo"
  mensagem="Confirmar remoção? Esta ação não pode ser desfeita."
  variant="danger"
  @confirmar="handleRemover"
/>
```

---

### 2. Manual Loading States

**❌ Bad:**

```vue
<BButton :disabled="loading1">Ação 1</BButton>
<BButton :disabled="loading2">Ação 2</BButton>
<BButton :disabled="loading3">Ação 3</BButton>
```

**✅ Good (LoadingButton component):**

```vue
<LoadingButton :loading="loading1" @click="action1">Ação 1</LoadingButton>
<LoadingButton :loading="loading2" @click="action2">Ação 2</LoadingButton>
```

---

### 3. Inconsistent Headings

**❌ Bad:**

```vue
<!-- View A -->
<h1>Título</h1>

<!-- View B -->
<div class="display-6">Título</div>

<!-- View C -->
<h3>Título</h3>
```

**✅ Good:**

```vue
<!-- All views -->
<h2 class="mb-3">Título da Página</h2>
```

---

### 4. Hardcoded Colors

**❌ Bad:**

```vue
<div style="color: #333">Texto</div>
<BButton style="background: #28a745">Salvar</BButton>
```

**✅ Good:**

```vue
<div class="text-dark">Texto</div>
<BButton variant="success">Salvar</BButton>
```

---

### 5. Mixed Error Display

**❌ Bad:**

```vue
<!-- File A -->
<div class="text-danger small">{{ error }}</div>

<!-- File B -->
<small class="text-danger">{{ error }}</small>

<!-- File C -->
<BFormInvalidFeedback>{{ error }}</BFormInvalidFeedback>
```

**✅ Good:**

```vue
<!-- Consistent use of BFormInvalidFeedback for field errors -->
<BFormInvalidFeedback :state="hasError ? false : null">
  {{ error }}
</BFormInvalidFeedback>
```

---

### 6. Composables with UI Side Effects

**❌ Bad:**

```ts
export function useSalvar() {
  const toast = useToast()
  
  async function salvar(data) {
    try {
      await api.post('/salvar', data)
      toast.success('Salvo!') // ❌ UI logic in composable
    } catch (e) {
      toast.error('Erro!') // ❌ UI logic in composable
    }
  }
  
  return { salvar }
}
```

**✅ Good:**

```ts
export function useSalvar() {
  const error = ref<string | null>(null)
  const success = ref(false)
  
  async function salvar(data) {
    error.value = null
    success.value = false
    try {
      await api.post('/salvar', data)
      success.value = true // Component decides what to show
    } catch (e) {
      error.value = normalizeError(e) // Return error state
      throw e
    }
  }
  
  return { salvar, error, success }
}

// In component:
const { salvar, error, success } = useSalvar()
watch(success, (val) => {
  if (val) toast.success('Salvo!')
})
watch(error, (val) => {
  if (val) toast.error(val)
})
```

---

## 🌳 Decision Trees

### "Should I create a new modal component?"

```
Is this a simple Yes/No confirmation?
├─ Yes → Use ModalConfirmacao
└─ No → Does it have a form with validation?
    ├─ Yes → Create dedicated modal component
    │         (SubprocessoModal, CriarCompetenciaModal pattern)
    └─ No → Does it show read-only information?
        ├─ Yes → Consider BAlert or inline content instead
        └─ No → Create dedicated modal component
```

---

### "How should I display this error?"

```
Where did the error occur?
├─ Field-level validation
│   └─ Use: BFormInvalidFeedback
├─ Form-level validation (multiple fields)
│   └─ Use: BAlert (danger) at top of form
├─ API error (expected, like 409 conflict)
│   └─ Use: BAlert (danger) inline in context
├─ API error (unexpected, like 500)
│   └─ Use: toast.danger() (global notification)
└─ Background operation success
    └─ Use: toast.success() (global notification)
```

---

### "Which button variant should I use?"

```
What is the action?
├─ Primary workflow action (Save, Create)
│   └─ variant="primary"
├─ Final positive action (Submit, Confirm, Finish)
│   └─ variant="success"
├─ Destructive action (Delete, Remove)
│   └─ variant="danger"
├─ Cancel, Go Back
│   └─ variant="secondary" or variant="link"
├─ Alternative action (Edit, View)
│   └─ variant="outline-primary"
└─ Tertiary navigation
    └─ variant="link"
```

---

## 📚 References

### Official Documentation

- [BootstrapVueNext Docs](https://bootstrap-vue-next.github.io/bootstrap-vue-next/)
- [Bootstrap 5.3 Docs](https://getbootstrap.com/docs/5.3/getting-started/introduction/)
- [Vue 3 Docs](https://vuejs.org/)
- [Pinia Docs](https://pinia.vuejs.org/)

### Component Examples

- See existing views in `frontend/src/views/` for implementation patterns
- Check `frontend/src/components/` for reusable components
- Review `frontend/src/composables/` for logic extraction patterns

---

**Last Updated:** Janeiro 2026  
**Maintained by:** Development Team  
**Questions?** Consult AGENTS.md and GEMINI.md for project-specific guidelines.
