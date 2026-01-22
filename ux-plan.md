# UX/UI Consistency & Refactoring Plan

This plan outlines comprehensive steps to standardize the application's User Experience and User Interface, addressing inconsistencies in modals, views, and component usage. It is designed to be executed by AI agents in sequential sprints.

> **Last Updated:** Janeiro 2026  
> **Status:** Analysis Complete | Implementation Pending  
> **Codebase:** 44 Vue components, 18 views, 4 composables  
> **BootstrapVueNext:** v0.42.0

## 📊 Executive Summary

**Current State:**
- 23 inline `BModal` instances creating 200+ lines of duplicated code
- Inconsistent page header patterns across 18 views
- Mixed error handling approaches (3 different patterns)
- Manual loading states in 15+ components
- No standardized layout wrapper components

**Target State:**
- Single source of truth for modal confirmations
- Consistent PageHeader component across all views
- Standardized error display patterns
- Reusable loading button component
- 30% reduction in template code
- Improved accessibility and UX consistency

---

## 🔍 Comprehensive Findings

### 1. Modal Usage Analysis (CRITICAL Priority)

#### Quantitative Data:
| Pattern | Count | Files Affected | Lines of Code |
|---------|-------|----------------|---------------|
| **ModalConfirmacao** (Reusable) | 4 views | CadMapa, ProcessoView, CadAtividades, CadAtribuicao | ~120 lines |
| **Inline BModal** | 23 instances | 15 files | ~460 lines |
| **Specialized Modals** | 9 components | Various | ~900 lines |

#### Specific Issues Found:

**ConfiguracoesView.vue:**
- Lines 112-140: Inline modal for user removal
- Lines 145-175: Inline modal for role change
- **Issue:** Both could use ModalConfirmacao
- **Impact:** 56 lines of duplicated footer logic

**VisMapa.vue:**
- 5 inline BModals for different actions
- **Issue:** Mixed `hide-footer` usage with custom footers
- **Impact:** Inconsistent button order, mixed styling

**CadProcesso.vue:**
- Lines 141-177: Inline confirmation modal
- Lines 180-207: Inline removal modal
- **Issue:** Reimplements exact same pattern as ModalConfirmacao
- **Impact:** 54 lines that could be 8 lines

**RelatoriosView.vue:**
- 3 inline modals for different report types
- **Issue:** Each has slightly different button styling
- **Impact:** Inconsistent UX across report dialogs

#### Modal Property Inconsistencies:

| Property | Pattern A | Pattern B | Pattern C |
|----------|-----------|-----------|-----------|
| `fade` | `:fade="false"` | Not specified (true) | `:fade="true"` |
| `centered` | Always used | Sometimes | Rarely |
| `hide-footer` | Yes + template | No (default footer) | Yes (no custom) |
| `header-bg-variant` | Used in AceitarMapaModal | Not used | Not used |

**Recommendation:** Standardize on `:fade="false"` (performance), always `centered`, always custom footer template.

---

### 2. View Layout Patterns (HIGH Priority)

#### Page Header Analysis (All 18 Views):

| View | Title Element | Title Class | Button Container | Issues |
|------|---------------|-------------|------------------|--------|
| PainelView.vue | `div` | `display-6 mb-0` | `d-flex gap-2` | ✓ Good pattern |
| CadProcesso.vue | `h2` | None | `d-flex justify-content-between` | Inconsistent |
| CadMapa.vue | `div` | `display-6 mb-3` | `d-flex gap-2` | Mixed spacing |
| VisMapa.vue | `div` | `display-6` | `d-flex gap-2` | ✓ Good pattern |
| CadAtividades.vue | `h1` + class | `display-6 mb-0` | `d-flex gap-2` | Wrong semantic tag |
| VisAtividades.vue | `h2` | `mb-0` | `d-flex gap-2` | No size class |
| ProcessoView.vue | `div` | `display-6 mb-0` | `d-flex gap-2` | ✓ Good pattern |
| SubprocessoView.vue | `h3` | `mb-3` | `d-flex gap-2` | Wrong hierarchy |
| ConfiguracoesView.vue | `h5` (in card) | `mb-0` | `d-flex justify-content-between` | Card-based pattern |
| RelatoriosView.vue | `div` | `display-6 mb-4` | Grid layout | Different approach |

**Key Findings:**
- **3 different heading tags** used: `h1`, `h2`, `h3`, plus `div` with classes
- **4 different spacing patterns**: `mb-0`, `mb-3`, `mb-4`, no margin
- **2 button layout patterns**: `d-flex gap-2` vs `d-flex justify-content-between`
- **Semantic issue:** Using `div` instead of proper heading tags hurts accessibility

**Recommendation:**
- Standardize on `<h2 class="mb-3">` for all page titles
- Use `d-flex justify-content-between align-items-center mb-3` for header row
- Create PageHeader component to enforce this

---

### 3. Button & Loading States (MEDIUM Priority)

#### Loading State Patterns Found:

**Pattern A - Full Implementation (CadProcesso.vue):**
```vue
<BButton :disabled="isLoading" variant="primary">
  <BSpinner v-if="isLoading" small class="me-1" />
  <i v-else class="bi bi-save me-1" aria-hidden="true"></i>
  {{ isLoading ? 'Salvando...' : 'Salvar' }}
</BButton>
```
**Files using this:** 8 views
**Lines per button:** ~7 lines

**Pattern B - Minimal (DisponibilizarMapaModal.vue):**
```vue
<BButton :disabled="loading">
  <BSpinner v-if="loading" small />
  Salvar
</BButton>
```
**Files using this:** 6 components
**Lines per button:** ~4 lines

**Pattern C - No Loading State:**
```vue
<BButton :disabled="someCondition">Ação</BButton>
```
**Files using this:** 5 views
**Issue:** No visual feedback during async operations

#### Icon Spacing Analysis:
- **Consistent:** `me-1` used in 95% of buttons with icons ✓
- **Rare:** `me-2` used in 3 buttons (should be `me-1`)
- **Missing:** 7 icon-only buttons lack `aria-label`

**Impact:**
- ~15 components × ~3 loading buttons = 45 button declarations
- Average 7 lines per button = **315 lines of duplicated loading logic**
- Could be reduced to ~135 lines with LoadingButton component (57% reduction)

---

### 4. Form Patterns & Validation (MEDIUM Priority)

#### Validation Feedback Methods:

| Method | Usage Count | Files | Consistency |
|--------|-------------|-------|-------------|
| `BFormInvalidFeedback` | 12 instances | CadProcesso, CriarCompetenciaModal, DisponibilizarMapaModal | ✓ Recommended |
| `BAlert` (danger) | 8 instances | CadAtribuicao, ConclusaoDiagnostico | ✓ For form-level errors |
| `div.text-danger.small` | 6 instances | CadMapa, CadAtividades | ❌ Inconsistent |
| `small.text-danger` | 3 instances | DisponibilizarMapaModal | ❌ Mixed with others |

**Specific Issues:**

**CadAtribuicao.vue (Lines 34-52):**
```vue
<!-- Field 1 -->
<div class="text-danger small mt-1">{{ error1 }}</div>

<!-- Field 2 -->
<BFormInvalidFeedback>{{ error2 }}</BFormInvalidFeedback>

<!-- Field 3 -->
<small class="text-danger">{{ error3 }}</small>
```
**Problem:** 3 different patterns in one file!

**CadProcesso.vue (Lines 5-17):**
- BAlert placed **before** form (outside form context)
- Should be inside `<BForm>` for semantic correctness

**Recommendation:**
- **Always use `BFormInvalidFeedback`** for field-level errors
- **Always use `BAlert`** for form-level errors (multiple fields)
- Never use raw `div` or `small` with `text-danger`

---

### 5. Error Handling Patterns (MEDIUM Priority)

#### Current Approaches:

**Approach A - Local State + BAlert:**
```vue
<script setup>
const localError = ref<string | null>(null)
</script>
<template>
  <BAlert v-if="localError" variant="danger">{{ localError }}</BAlert>
</template>
```
**Used in:** 6 views
**Good for:** Expected errors, validation issues

**Approach B - Global Toast (feedbackStore):**
```ts
feedbackStore.adicionarMensagem('Erro ao salvar', 'danger')
```
**Used in:** 4 views
**Good for:** Success feedback, unexpected errors

**Approach C - Inline Error Display:**
```vue
<div v-if="error" class="text-danger">{{ error }}</div>
```
**Used in:** 3 views
**Issue:** No dismiss mechanism, not accessible

**Inconsistency Example:**
- **CadProcesso:** Uses local BAlert for all errors
- **VisMapa:** Uses toast for some errors, BAlert for others
- **ConfiguracoesView:** Uses inline div for errors

**Recommendation:**
- **Field errors:** BFormInvalidFeedback
- **Form-level errors:** Local BAlert
- **Success/Info:** Toast (global)
- **Unexpected API errors:** Toast (global)

---

### 6. Accessibility Issues (HIGH Priority)

#### Current Issues Found:

**Missing ARIA Labels:**
- 7 icon-only buttons without `aria-label`
- 12 decorative icons without `aria-hidden="true"`

**Example from TabelaProcessos.vue:**
```vue
<!-- ❌ Bad -->
<BButton size="sm">
  <i class="bi bi-pencil"></i>
</BButton>

<!-- ✅ Good -->
<BButton size="sm" aria-label="Editar processo">
  <i class="bi bi-pencil" aria-hidden="true"></i>
</BButton>
```

**Focus Management:**
- ModalConfirmacao: ✓ Auto-focuses Cancel for danger variant
- Custom modals: ❌ No focus management
- Form errors: ❌ No auto-focus on first error

**Keyboard Navigation:**
- ✓ All BButton components are keyboard-accessible
- ❌ Some custom click handlers on `div` elements (not accessible)
- ❌ Table rows use `@row-clicked` but missing `tabindex` and `role`

**Semantic HTML:**
- ❌ Using `div` for page titles instead of `h2`
- ❌ Some buttons implemented as `div` with click handlers
- ✓ Forms use proper `label` and `BFormGroup`

**Impact:**
- Screen reader users cannot understand icon-only buttons
- Keyboard users cannot navigate to some interactive elements
- Page structure is not properly communicated to assistive tech

---

## 🚀 Enhanced Refactoring Roadmap

### Sprint 0: Documentation & Analysis ✅ COMPLETE

**Completed:**
- [x] Comprehensive codebase analysis
- [x] BootstrapVueNext 0.42 best practices research
- [x] Enhanced design-guidelines.md
- [x] Enhanced ux-plan.md with findings
- [x] Component inventory
- [x] Anti-pattern documentation

**Deliverables:**
- Updated design-guidelines.md (3500+ lines)
- Updated ux-plan.md (this document)
- Decision trees and usage guides

---

### Sprint 1: Foundations & Layout Components

**Goal:** Establish layout components and CSS standards.

**Priority:** HIGH | **Effort:** Medium | **Impact:** High

#### Tasks:

1. **Update `style.css` with Design Tokens** ✅
   - Add CSS variables for consistent spacing
   - Define semantic color overrides (if needed)
   - Add utility class helpers
   - **Files:** `frontend/src/style.css`
   - **Lines:** +30 lines

2. **Create `PageHeader` Component** ✅
   - **Location:** `frontend/src/components/layout/PageHeader.vue`
   - **Props:**
     - `title` (required, string)
     - `subtitle` (optional, string)
   - **Slots:**
     - `actions` (for buttons)
     - `default` (for subtitle override)
   - **Features:**
     - Responsive layout (stacks on mobile)
     - Consistent spacing (`mb-3`)
     - Semantic HTML (`h2` for title)
   - **Lines:** ~60 lines (template + script + tests)

3. **Create `AppContainer` Component** (Optional)
   - **Location:** `frontend/src/components/layout/AppContainer.vue`
   - **Features:**
     - Wraps `BContainer` with default `mt-4`
     - Responsive padding
   - **Lines:** ~20 lines
   - **Note:** May be unnecessary if PageHeader is sufficient

#### Testing:
- Unit test for PageHeader props and slots (Vitest)
- Visual verification in Storybook or dev environment

#### Expected Outcome:
- Reusable PageHeader component ready for adoption
- CSS variables documented in style.css
- Foundation for Sprint 2

---

### Sprint 2: Modal Standardization (CRITICAL)

**Goal:** Eliminate inline modals, standardize all confirmations.

**Priority:** CRITICAL | **Effort:** High | **Impact:** Very High

#### Phase 2.1: Enhance ModalConfirmacao

**Tasks:**
1. **Add `loading` prop to ModalConfirmacao**
   - Automatically disable buttons during async operations
   - Show spinner on Confirm button when loading
   - **Files:** `frontend/src/components/ModalConfirmacao.vue`
   - **Lines:** +15 lines

2. **Add `okTitle` and `cancelTitle` props**
   - Allow customizing button text
   - Default: "Confirmar" / "Cancelar"
   - **Lines:** +5 lines

3. **Improve TypeScript types**
   - Define proper interfaces for props
   - Add JSDoc comments
   - **Lines:** +10 lines

**Updated ModalConfirmacao API:**
```vue
<ModalConfirmacao
  v-model="showModal"
  titulo="Remover Processo"
  mensagem="Esta ação não pode ser desfeita."
  variant="danger"
  ok-title="Remover"
  :loading="isRemoving"
  @confirmar="handleRemover"
/>
```

#### Phase 2.2: Replace Inline Modals (Priority Order)

**HIGH Priority (Duplicate Confirmation Logic):**

| File | Lines | Modals | Replacement | Effort | Impact |
|------|-------|--------|-------------|--------|--------|
| CadProcesso.vue | 141-207 | 2 inline | ModalConfirmacao × 2 | Medium | High |
| ConfiguracoesView.vue | 112-175 | 2 inline | ModalConfirmacao × 2 | Medium | High |
| VisMapa.vue | Multiple | 5 inline | ModalConfirmacao × 3, Keep 2 custom | High | Very High |
| RelatoriosView.vue | Multiple | 3 inline | ModalConfirmacao × 3 | Medium | Medium |

**MEDIUM Priority (Custom Content, but standardizable):**

| File | Lines | Issue | Solution | Effort |
|------|-------|-------|----------|--------|
| VisAtividades.vue | Multiple | 2 inline with forms | Create dedicated modal components | Medium |
| CadAtribuicao.vue | Multiple | Confirmation with extra info | Use ModalConfirmacao with slot | Low |
| UnidadeView.vue | Multiple | Delete confirmation | ModalConfirmacao | Low |

**LOW Priority (Keep as-is, complex custom logic):**

| File | Reason | Action |
|------|--------|--------|
| SubprocessoModal.vue | Complex form modal | Keep, but standardize footer |
| CriarCompetenciaModal.vue | Complex form modal | Keep, but standardize footer |
| DisponibilizarMapaModal.vue | Multi-step process | Keep, but standardize footer |

#### Phase 2.3: Standardize Custom Modal Footers

**Task:** Ensure all custom modals follow footer pattern.

**Pattern:**
```vue
<template #footer>
  <BButton variant="secondary" @click="fechar">
    <i class="bi bi-x-circle me-1" aria-hidden="true"></i>
    Cancelar
  </BButton>
  <BButton variant="primary" :disabled="!isValid" @click="salvar">
    <BSpinner v-if="salvando" small class="me-1" />
    <i v-else class="bi bi-check-circle me-1" aria-hidden="true"></i>
    {{ salvando ? 'Salvando...' : 'Salvar' }}
  </BButton>
</template>
```

**Files to Update:**
- AceitarMapaModal.vue (footer order is correct, but icon inconsistent)
- SubprocessoModal.vue (add loading state to button)
- ImportarAtividadesModal.vue (standardize button icons)

#### Expected Outcome:
- 23 inline modals → 8 ModalConfirmacao instances + 6 standardized custom modals
- **Code reduction:** ~200 lines removed
- **Consistency:** All confirmations look/behave identically
- **Accessibility:** Proper focus management in all modals

---

### Sprint 3: Button & Loading Standardization

**Goal:** Create reusable loading button, eliminate manual loading logic.

**Priority:** MEDIUM | **Effort:** Medium | **Impact:** High

#### Option A: Create LoadingButton Component (Recommended)

**Component:** `frontend/src/components/ui/LoadingButton.vue`

```vue
<template>
  <BButton
    v-bind="$attrs"
    :disabled="loading || disabled"
    @click="$emit('click', $event)"
  >
    <BSpinner v-if="loading" small class="me-1" />
    <i v-else-if="icon" :class="`bi bi-${icon} me-1`" aria-hidden="true"></i>
    <slot>{{ loadingText && loading ? loadingText : text }}</slot>
  </BButton>
</template>

<script setup lang="ts">
interface Props {
  loading?: boolean
  disabled?: boolean
  icon?: string
  text?: string
  loadingText?: string
}

withDefaults(defineProps<Props>(), {
  loading: false,
  disabled: false,
})

defineEmits<{
  click: [event: MouseEvent]
}>()
</script>
```

**Usage:**
```vue
<LoadingButton
  variant="primary"
  :loading="isSalvando"
  icon="save"
  text="Salvar"
  loading-text="Salvando..."
  @click="salvar"
/>
```

**Benefits:**
- **15 components** × **3 buttons** = 45 buttons
- **7 lines → 3 lines** per button
- **180 lines saved** across codebase

**Files to Update (Priority Order):**
1. CadProcesso.vue (3 loading buttons)
2. VisMapa.vue (4 loading buttons)
3. CadMapa.vue (2 loading buttons)
4. ConfiguracoesView.vue (2 loading buttons)
5. DisponibilizarMapaModal.vue (1 loading button)
6. All other views with loading buttons (gradual adoption)

#### Option B: Enhance BButton Usage (Alternative)

If LoadingButton is deemed over-engineering:
- Create composable `useLoadingButton`
- Standardize template pattern across all files
- Less code reduction, but simpler approach

#### Expected Outcome:
- Consistent loading button behavior
- Reduced template code
- Easier to maintain loading states

---

### Sprint 4: View Refactoring & PageHeader Adoption

**Goal:** Apply PageHeader component, extract form logic to composables.

**Priority:** MEDIUM | **Effort:** High | **Impact:** High

#### Phase 4.1: Adopt PageHeader Component

**Migration Order (by view complexity):**

**Tier 1 - Simple Views (No complex header logic):**
1. PainelView.vue
2. HistoricoView.vue
3. RelatoriosView.vue

**Tier 2 - Standard CRUD Views:**
4. ProcessoView.vue
5. UnidadeView.vue
6. CadMapa.vue

**Tier 3 - Complex Views:**
7. VisMapa.vue
8. CadAtividades.vue
9. VisAtividades.vue
10. SubprocessoView.vue

**Template Pattern:**

**Before:**
```vue
<BContainer class="mt-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h2 class="mb-0">Título</h2>
    <div class="d-flex gap-2">
      <BButton variant="primary">Ação</BButton>
    </div>
  </div>
  <!-- content -->
</BContainer>
```

**After:**
```vue
<BContainer class="mt-4">
  <PageHeader title="Título">
    <template #actions>
      <BButton variant="primary">Ação</BButton>
    </template>
  </PageHeader>
  <!-- content -->
</BContainer>
```

**Impact per view:**
- **Lines removed:** 5-8 lines
- **Total:** 18 views × 6 lines average = **~108 lines removed**

#### Phase 4.2: Extract Form Logic to Composables

**Target Views with Heavy Form Logic:**

**CadProcesso.vue:**
- Current: 250 lines script section
- Extract to: `composables/useProcessoForm.ts`
- Logic to extract:
  - Form field refs (descricao, tipo, dataLimite, unidadesSelecionadas)
  - Validation logic (isFormValid computed)
  - Field error management
  - Form data construction
- **Reduction:** ~80 lines in view → 100 lines in composable
- **Benefit:** Reusable logic, testable in isolation

**CadAtividades.vue:**
- Extract to: `composables/useAtividadeForm.ts`
- **Reduction:** ~60 lines

**DisponibilizarMapaModal.vue:**
- Extract to: `composables/useDisponibilizacaoForm.ts`
- **Reduction:** ~40 lines

**Composable Pattern:**
```ts
// composables/useProcessoForm.ts
export function useProcessoForm(initialData?: Processo) {
  const descricao = ref(initialData?.descricao ?? '')
  const tipo = ref(initialData?.tipo ?? 'MAPEAMENTO')
  const dataLimite = ref(initialData?.dataLimite ?? '')
  const unidadesSelecionadas = ref<number[]>(initialData?.unidades ?? [])

  const { fieldErrors, setFieldError, clearErrors } = useFormErrors()

  const isFormValid = computed(() => {
    return descricao.value.trim() !== '' &&
           tipo.value !== '' &&
           dataLimite.value !== '' &&
           unidadesSelecionadas.value.length > 0
  })

  function construirRequest(): CriarProcessoRequest {
    return {
      descricao: descricao.value,
      tipo: tipo.value as TipoProcesso,
      dataLimite: dataLimite.value,
      unidadesCodigos: unidadesSelecionadas.value,
    }
  }

  function limpar() {
    descricao.value = ''
    tipo.value = 'MAPEAMENTO'
    dataLimite.value = ''
    unidadesSelecionadas.value = []
    clearErrors()
  }

  return {
    // State
    descricao,
    tipo,
    dataLimite,
    unidadesSelecionadas,
    fieldErrors,
    // Computed
    isFormValid,
    // Actions
    setFieldError,
    clearErrors,
    construirRequest,
    limpar,
  }
}
```

#### Expected Outcome:
- All views use PageHeader (consistent structure)
- Heavy form logic extracted to composables
- Views are more declarative, less imperative
- **Total code reduction:** ~250 lines
- Better testability (composables can be unit tested)

---

### Sprint 5: Accessibility & Refinement

**Goal:** Fix accessibility issues, add missing ARIA labels.

**Priority:** HIGH | **Effort:** Medium | **Impact:** Very High

#### Tasks:

1. **Add ARIA labels to icon-only buttons**
   - **Files:** TabelaProcessos, TabelaAlertas, ProcessoView, etc.
   - **Count:** 7 buttons need `aria-label`
   - **Example:**
     ```vue
     <BButton size="sm" aria-label="Editar processo" @click="editar">
       <i class="bi bi-pencil" aria-hidden="true"></i>
     </BButton>
     ```

2. **Add `aria-hidden="true"` to decorative icons**
   - **Count:** 12 icons need attribute
   - **Files:** All components with icons

3. **Improve focus management in forms**
   - Auto-focus first field on mount
   - Auto-focus first error field on validation failure
   - **Files:** CadProcesso, CadMapa, CadAtividades

4. **Fix semantic HTML issues**
   - Replace `div` with heading tags where appropriate
   - Ensure proper heading hierarchy (h1 → h2 → h3)
   - **Impact:** Better screen reader navigation

5. **Improve table accessibility**
   - Add `role="button"` to clickable rows
   - Add `tabindex="0"` to interactive rows
   - Add keyboard handlers (Enter/Space)
   - **Files:** TabelaProcessos, TabelaAlertas, TreeTableView

6. **Test with screen reader**
   - Manual testing with NVDA/JAWS
   - Ensure all interactions are accessible
   - Document any remaining issues

#### Expected Outcome:
- WCAG 2.1 Level AA compliance
- All interactive elements keyboard-accessible
- Proper ARIA labels and semantic HTML
- Screen reader friendly interface

---

### Sprint 6: Testing & Documentation

**Goal:** Ensure consistency is maintained through tests.

**Priority:** MEDIUM | **Effort:** Medium | **Impact:** Medium

#### Unit Tests (Vitest):

**New Components to Test:**
- PageHeader.vue
  - Props rendering
  - Slots rendering
  - Responsive behavior
- LoadingButton.vue (if created)
  - Loading state
  - Icon rendering
  - Event emission

**Enhanced Tests:**
- ModalConfirmacao.vue
  - New `loading` prop
  - New `okTitle`/`cancelTitle` props
  - Focus management

**Snapshot Tests:**
- PageHeader with different props
- ModalConfirmacao with different variants
- LoadingButton states

#### E2E Tests (Playwright):

**Consistency Checks:**
1. **Page Header Test:**
   ```ts
   test('all views have consistent page headers', async ({ page }) => {
     const views = ['/painel', '/processos/novo', '/mapa/123']
     for (const view of views) {
       await page.goto(view)
       await expect(page.locator('h2')).toBeVisible()
       await expect(page.locator('.mb-3')).toHaveClass(/mb-3/)
     }
   })
   ```

2. **Modal Consistency Test:**
   ```ts
   test('all confirmation modals have consistent structure', async ({ page }) => {
     // Open various delete/confirm modals
     // Check button order, text, variants
   })
   ```

3. **Accessibility Test:**
   ```ts
   test('interactive elements are keyboard accessible', async ({ page }) => {
     await page.goto('/painel')
     await page.keyboard.press('Tab') // Should focus first button
     // Continue tabbing through all interactive elements
   })
   ```

#### Documentation Updates:

**Files to Update:**
- README.md (usage examples for new components)
- AGENTS.md (frontend patterns section)
- design-guidelines.md (add new components)
- Component JSDoc comments

#### Expected Outcome:
- Comprehensive test coverage for new components
- E2E tests prevent regression
- Updated documentation for maintainability

---

---

## 📈 Impact Metrics & Success Criteria

### Code Quality Metrics

**Before Refactoring:**
| Metric | Value |
|--------|-------|
| Inline BModal instances | 23 |
| Duplicated modal code (lines) | ~460 |
| Views with custom headers | 18 |
| Manual loading buttons | 45 |
| Inconsistent error displays | 17 |
| Accessibility violations | 19 |
| Total template lines | ~6,200 |

**After Refactoring (Target):**
| Metric | Target | Improvement |
|--------|--------|-------------|
| Inline BModal instances | 0 | -23 (100%) |
| Duplicated modal code | ~0 | -460 lines |
| Views with PageHeader | 18 | +18 |
| LoadingButton usage | 45 | Standardized |
| Consistent error display | All | 100% |
| Accessibility violations | 0 | -19 (100%) |
| Total template lines | ~5,400 | -800 lines (13%) |

### User Experience Metrics

**Consistency:**
- Modal dialog UX: 100% consistent
- Page header structure: 100% consistent
- Loading feedback: 100% consistent
- Error messaging: 100% consistent

**Accessibility:**
- WCAG 2.1 Level AA compliance: 100%
- Keyboard navigation: 100% functional
- Screen reader compatibility: Excellent

**Performance:**
- Modal fade disabled: Faster perception
- Reduced template complexity: Easier Vue compilation
- Better code splitting: Smaller bundle sizes

---

## 🧪 Testing Strategy

### Unit Tests (Vitest)

**Coverage Targets:**
- New components: 90%+ coverage
- Modified components: No regression
- Composables: 95%+ coverage

**Test Files to Create:**
```
frontend/src/components/layout/__tests__/
  ├── PageHeader.spec.ts
  └── PageHeader.snapshot.ts

frontend/src/components/ui/__tests__/
  └── LoadingButton.spec.ts

frontend/src/composables/__tests__/
  ├── useProcessoForm.spec.ts
  ├── useAtividadeForm.spec.ts
  └── useNotifications.spec.ts

frontend/src/components/__tests__/
  └── ModalConfirmacao.enhanced.spec.ts
```

**Example Test:**
```ts
// PageHeader.spec.ts
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PageHeader from '../PageHeader.vue'

describe('PageHeader', () => {
  it('renders title prop correctly', () => {
    const wrapper = mount(PageHeader, {
      props: { title: 'Test Title' }
    })
    expect(wrapper.find('h2').text()).toBe('Test Title')
  })

  it('renders actions slot', () => {
    const wrapper = mount(PageHeader, {
      props: { title: 'Test' },
      slots: {
        actions: '<button>Action</button>'
      }
    })
    expect(wrapper.find('button').text()).toBe('Action')
  })

  it('applies consistent spacing classes', () => {
    const wrapper = mount(PageHeader, {
      props: { title: 'Test' }
    })
    const header = wrapper.find('.d-flex')
    expect(header.classes()).toContain('mb-3')
    expect(header.classes()).toContain('justify-content-between')
  })
})
```

### E2E Tests (Playwright)

**Test Files to Create:**
```
e2e/tests/
  ├── consistency/
  │   ├── page-headers.spec.ts
  │   ├── modal-structure.spec.ts
  │   └── loading-states.spec.ts
  └── accessibility/
      ├── keyboard-navigation.spec.ts
      └── screen-reader.spec.ts
```

**Example E2E Test:**
```ts
// page-headers.spec.ts
import { test, expect } from '@playwright/test'

const routes = [
  '/painel',
  '/processos/novo',
  '/mapa/visualizar',
  '/atividades',
  '/configuracoes'
]

test.describe('Page Header Consistency', () => {
  for (const route of routes) {
    test(`${route} has standard PageHeader`, async ({ page }) => {
      await page.goto(route)
      
      // Check h2 exists (semantic HTML)
      const heading = page.locator('h2').first()
      await expect(heading).toBeVisible()
      
      // Check spacing
      const container = page.locator('.container').first()
      await expect(container).toHaveClass(/mt-4/)
      
      // Check header structure
      const headerRow = page.locator('.d-flex.justify-content-between').first()
      await expect(headerRow).toBeVisible()
    })
  }
})
```

### Visual Regression Testing

**Tool:** Playwright or Percy

**Snapshots to Create:**
- All modal variants (primary, danger, warning)
- PageHeader with/without actions
- LoadingButton in all states
- Form validation states

---

## 🚧 Migration Strategy

### Phase 1: Preparation (Week 1)
- Create new components (PageHeader, LoadingButton)
- Write comprehensive tests
- Update documentation
- Get team review and approval

### Phase 2: Pilot (Week 2)
- Migrate 3 simple views (PainelView, HistoricoView, RelatoriosView)
- Replace 5 inline modals with ModalConfirmacao
- Collect feedback
- Adjust approach if needed

### Phase 3: Rollout (Week 3-4)
- Migrate remaining views (batch by complexity)
- Replace all inline modals
- Extract form logic to composables
- Continuous testing and validation

### Phase 4: Refinement (Week 5)
- Fix accessibility issues
- Add missing ARIA labels
- E2E test coverage
- Final documentation update

### Phase 5: Verification (Week 6)
- Full regression testing
- Accessibility audit
- Performance benchmarking
- Team training on new patterns

---

## 🎯 File-by-File Refactoring Checklist

### Views (18 files)

**PainelView.vue** [PRIORITY: HIGH]
- [x] Replace header with PageHeader component
- [ ] No inline modals (✓ already clean)
- [ ] Check button spacing (✓ uses gap-2)
- [ ] Verify accessibility
- **Effort:** Low | **Impact:** High (most visible page)

**CadProcesso.vue** [PRIORITY: CRITICAL]
- [ ] Replace 2 inline modals with ModalConfirmacao
- [ ] Replace header with PageHeader
- [ ] Extract form logic to useProcessoForm composable
- [ ] Add loading buttons (3 buttons)
- [ ] Fix alert placement (move inside form)
- [ ] Add focus management on validation errors
- **Effort:** High | **Impact:** Very High

**VisMapa.vue** [PRIORITY: CRITICAL]
- [ ] Replace 3 inline confirmation modals with ModalConfirmacao
- [ ] Keep 2 custom modals (complex logic), but standardize footer
- [ ] Replace header with PageHeader
- [ ] Standardize button loading states (4 buttons)
- [ ] Verify accessibility
- **Effort:** Very High | **Impact:** Very High

**CadMapa.vue** [PRIORITY: HIGH]
- [ ] Replace header with PageHeader
- [ ] Add loading buttons (2 buttons)
- [ ] Verify form validation consistency
- [ ] Check accessibility
- **Effort:** Medium | **Impact:** High

**CadAtividades.vue** [PRIORITY: HIGH]
- [ ] Replace header with PageHeader (fix h1 → h2)
- [ ] Extract form logic to useAtividadeForm
- [ ] Standardize error display (fix mixed patterns)
- [ ] Add loading buttons
- **Effort:** High | **Impact:** High

**VisAtividades.vue** [PRIORITY: MEDIUM]
- [ ] Replace header with PageHeader (fix h2 sizing)
- [ ] Replace 2 inline modals
- [ ] Check accessibility
- **Effort:** Medium | **Impact:** Medium

**ConfiguracoesView.vue** [PRIORITY: HIGH]
- [ ] Replace 2 inline modals with ModalConfirmacao
- [ ] Standardize card-based header (may need custom approach)
- [ ] Add loading buttons
- [ ] Fix error display
- **Effort:** Medium | **Impact:** Medium

**ProcessoView.vue** [PRIORITY: HIGH]
- [ ] Replace header with PageHeader
- [ ] Verify ModalConfirmacao usage (✓ already uses it)
- [ ] Check button spacing
- **Effort:** Low | **Impact:** High

**SubprocessoView.vue** [PRIORITY: MEDIUM]
- [ ] Replace header with PageHeader (fix h3 → h2)
- [ ] Check modal usage
- [ ] Verify accessibility
- **Effort:** Low | **Impact:** Medium

**RelatoriosView.vue** [PRIORITY: MEDIUM]
- [ ] Adapt PageHeader to card-based layout
- [ ] Replace 3 inline modals
- [ ] Standardize button states
- **Effort:** Medium | **Impact:** Low

**UnidadeView.vue** [PRIORITY: LOW]
- [ ] Replace header with PageHeader
- [ ] Replace inline modal
- **Effort:** Low | **Impact:** Low

**HistoricoView.vue** [PRIORITY: LOW]
- [ ] Replace header with PageHeader
- [ ] Verify consistency
- **Effort:** Very Low | **Impact:** Low

**CadAtribuicao.vue** [PRIORITY: MEDIUM]
- [ ] Replace header with PageHeader
- [ ] Fix mixed error display patterns
- [ ] Standardize with ModalConfirmacao
- **Effort:** Medium | **Impact:** Medium

**LoginView.vue** [PRIORITY: LOW]
- [ ] No header needed (login page)
- [ ] Verify form validation display
- **Effort:** Very Low | **Impact:** Low

**AutoavaliacaoDiagnostico.vue** [PRIORITY: LOW]
- [ ] Replace header with PageHeader
- [ ] Check form patterns
- **Effort:** Low | **Impact:** Low

**MonitoramentoDiagnostico.vue** [PRIORITY: LOW]
- [ ] Replace header with PageHeader
- **Effort:** Low | **Impact:** Low

**OcupacoesCriticasDiagnostico.vue** [PRIORITY: LOW]
- [ ] Replace header with PageHeader
- **Effort:** Low | **Impact:** Low

**ConclusaoDiagnostico.vue** [PRIORITY: LOW]
- [ ] Replace header with PageHeader
- [ ] Check form validation
- **Effort:** Low | **Impact:** Low

---

### Components (Modal-specific)

**ModalConfirmacao.vue** [PRIORITY: CRITICAL]
- [ ] Add `loading` prop
- [ ] Add `okTitle` / `cancelTitle` props
- [ ] Add proper TypeScript types
- [ ] Update tests
- [ ] Update documentation
- **Effort:** Low | **Impact:** Very High

**AceitarMapaModal.vue** [PRIORITY: MEDIUM]
- [ ] Standardize footer button order (✓ already correct)
- [ ] Add consistent icons
- [ ] Verify accessibility
- **Effort:** Low | **Impact:** Low

**SubprocessoModal.vue** [PRIORITY: LOW]
- [ ] Standardize footer pattern
- [ ] Add loading state to save button
- **Effort:** Low | **Impact:** Low

**DisponibilizarMapaModal.vue** [PRIORITY: MEDIUM]
- [ ] Standardize footer
- [ ] Fix error display
- [ ] Add loading button
- **Effort:** Low | **Impact:** Low

**CriarCompetenciaModal.vue** [PRIORITY: LOW]
- [ ] Standardize footer
- [ ] Verify loading state
- **Effort:** Very Low | **Impact:** Low

**ImportarAtividadesModal.vue** [PRIORITY: LOW]
- [ ] Standardize footer icons
- **Effort:** Very Low | **Impact:** Low

---

### Components (Layout)

**PageHeader.vue** [PRIORITY: CRITICAL]
- [ ] Create component
- [ ] Write tests
- [ ] Add to component library
- **Effort:** Low | **Impact:** Very High

**LoadingButton.vue** [PRIORITY: HIGH]
- [ ] Create component (optional, evaluate first)
- [ ] Write tests
- [ ] Add to component library
- **Effort:** Low | **Impact:** High

---

### Composables

**useProcessoForm.ts** [PRIORITY: HIGH]
- [ ] Create composable
- [ ] Extract logic from CadProcesso
- [ ] Write comprehensive tests
- **Effort:** Medium | **Impact:** High

**useAtividadeForm.ts** [PRIORITY: MEDIUM]
- [ ] Create composable
- [ ] Extract logic from CadAtividades
- [ ] Write tests
- **Effort:** Medium | **Impact:** Medium

**useNotifications.ts** [PRIORITY: LOW]
- [ ] Wrapper around useToast
- [ ] Standardize toast options
- **Effort:** Very Low | **Impact:** Low

---

## 🧹 Tech Debt Payoff Summary

### Code Reduction

**Template Code:**
- Inline modals removed: ~460 lines
- Manual headers removed: ~108 lines
- Manual loading buttons: ~180 lines
- **Total template reduction:** ~748 lines (12% of total)

**Script Code:**
- Form logic to composables: -180 lines in views, +250 in composables
- **Net:** +70 lines, but better organization

**Total Project:**
- **Lines removed:** ~750
- **Lines added:** ~400 (new components + composables + tests)
- **Net reduction:** ~350 lines
- **Complexity reduction:** Significant (centralized patterns)

### Maintainability Improvements

**Single Source of Truth:**
- Modal confirmations: 1 component instead of 23 implementations
- Page headers: 1 component instead of 18 implementations
- Loading buttons: 1 component instead of 45 implementations

**Change Impact:**
- Change modal button color: 1 file instead of 23
- Change page header spacing: 1 file instead of 18
- Change loading indicator: 1 file instead of 45

**Testing:**
- Test modal behavior: 1 component test instead of 23
- Test page headers: 1 component test + 18 E2E checks
- Regression prevention: Automated instead of manual

### Developer Experience

**Onboarding:**
- New developers learn 1 pattern instead of 23 variations
- Clear documentation in design-guidelines.md
- Examples in all components

**Productivity:**
- PageHeader: 3 lines instead of 8
- Modal confirmation: 5 lines instead of 25
- Loading button: 3 lines instead of 7
- **Average time saved per feature:** 15-20 minutes

**Code Reviews:**
- Consistent patterns easier to review
- Violations caught by linter/tests
- Less subjective discussion

---

## 🧪 Testing the Refactor

### Manual Verification Checklist

**Modals:**
- [ ] Open a "Delete" modal → Check: Red theme, Cancel focused
- [ ] Open a "Save" modal → Check: Blue theme, proper focus
- [ ] Test ESC key → Modal closes
- [ ] Test backdrop click → Modal behavior consistent
- [ ] Test keyboard navigation → Tab order correct

**Page Headers:**
- [ ] Navigate to PainelView → Check alignment and spacing
- [ ] Navigate to CadProcesso → Check: Same alignment and spacing
- [ ] Resize window → Check: Responsive behavior
- [ ] Compare all 18 views → Check: Visual consistency

**Loading States:**
- [ ] Click Save button → Check: Spinner shows, button disables
- [ ] Wait for completion → Check: Spinner hides, button enables
- [ ] Click during loading → Check: No action (disabled)

**Forms:**
- [ ] Submit invalid form → Check: Errors display correctly
- [ ] Fix errors → Check: Error messages clear
- [ ] Submit valid form → Check: Success feedback

**Accessibility:**
- [ ] Navigate with keyboard only → All features accessible
- [ ] Test with screen reader → Proper announcements
- [ ] Check color contrast → WCAG AA compliance

---

## 📝 Documentation Updates Required

### Files to Update:

**frontend/README.md:**
- Add PageHeader usage examples
- Add LoadingButton usage examples
- Add modal patterns section

**AGENTS.md:**
- Update frontend patterns section
- Add composables best practices
- Reference design-guidelines.md

**design-guidelines.md:**
- [x] Already updated with comprehensive guidelines
- [ ] Add component API documentation
- [ ] Add migration examples

**Component JSDoc:**
- [ ] PageHeader.vue
- [ ] LoadingButton.vue
- [ ] Enhanced ModalConfirmacao.vue

---

## 🎓 Training & Knowledge Transfer

### Team Sessions:

**Session 1: New Components Overview** (30 min)
- PageHeader component and usage
- LoadingButton component (if created)
- Enhanced ModalConfirmacao

**Session 2: Composables Pattern** (45 min)
- When to create composables
- Form logic extraction pattern
- Testing composables

**Session 3: Migration Workflow** (30 min)
- How to migrate a view
- Testing checklist
- Code review standards

### Knowledge Base:

**Create Wiki Pages:**
- "How to Add a New View" (with PageHeader template)
- "Modal Patterns" (decision tree)
- "Form Validation Patterns"
- "Accessibility Checklist"

---

## 📊 Success Metrics

### Quantitative KPIs:

**Code Quality:**
- [ ] 100% of views use PageHeader
- [ ] 0 inline BModal for confirmations
- [ ] 0 accessibility violations
- [ ] Test coverage >85%

**Performance:**
- [ ] Modal open time <100ms
- [ ] Page load time unchanged or improved
- [ ] Bundle size reduction (if achieved)

**Developer Productivity:**
- [ ] Time to create new view: -30%
- [ ] Code review time: -20%
- [ ] Bug report rate: -25%

### Qualitative KPIs:

**User Experience:**
- [ ] Consistent modal behavior across app
- [ ] Predictable loading feedback
- [ ] Accessible to keyboard users
- [ ] Screen reader compatible

**Developer Experience:**
- [ ] Positive team feedback
- [ ] Easier onboarding for new devs
- [ ] Less "how do I..." questions

---

## 🚀 Implementation Timeline

### Realistic Timeline (4-6 Weeks)

**Week 1: Foundation**
- Days 1-2: Create PageHeader + tests
- Days 3-4: Create LoadingButton + tests (optional)
- Day 5: Update ModalConfirmacao + tests

**Week 2: Pilot Migration**
- Days 1-2: Migrate 3 simple views
- Day 3: Replace 5 inline modals
- Days 4-5: Testing and adjustments

**Week 3: Main Migration**
- Days 1-3: Migrate 10 views
- Days 4-5: Replace remaining modals

**Week 4: Composables & Logic**
- Days 1-2: Create useProcessoForm
- Days 3-4: Create useAtividadeForm
- Day 5: Extract other form logic

**Week 5: Accessibility**
- Days 1-2: Add ARIA labels
- Days 3-4: Fix semantic HTML
- Day 5: Screen reader testing

**Week 6: Final Polish**
- Days 1-2: E2E tests
- Day 3: Documentation
- Days 4-5: Team training

---

## 🎯 Next Steps

### Immediate Actions:

1. **Get stakeholder approval** for this plan
2. **Prioritize Sprint 1** (Foundations) for implementation
3. **Assign team members** to specific tasks
4. **Set up project tracking** (Jira/GitHub Issues)
5. **Schedule team kickoff** meeting

### Future Considerations:

**Beyond This Plan:**
- Internationalization (i18n) support
- Dark mode implementation
- Advanced form validation library
- Component library documentation site (Storybook)
- Design system tokens for other frameworks

**Monitoring:**
- Track adoption rate of new components
- Monitor user feedback
- Measure performance impact
- Collect developer satisfaction

---

## 📚 References & Resources

### Official Documentation:
- [BootstrapVueNext v0.42 Docs](https://bootstrap-vue-next.github.io/bootstrap-vue-next/)
- [Bootstrap 5.3 Docs](https://getbootstrap.com/docs/5.3/)
- [Vue 3 Composition API](https://vuejs.org/guide/extras/composition-api-faq.html)
- [Pinia Setup Stores](https://pinia.vuejs.org/core-concepts/#setup-stores)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)

### Internal Documentation:
- [design-guidelines.md](./design-guidelines.md) - Comprehensive UI/UX standards
- [AGENTS.md](./AGENTS.md) - Project coding conventions
- [frontend/README.md](./frontend/README.md) - Frontend setup guide

---

**Document Version:** 2.0  
**Last Updated:** Janeiro 2026  
**Authors:** Development Team + AI Analysis  
**Status:** Ready for Implementation Review
