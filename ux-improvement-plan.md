# Plano de Melhorias de UX - SGC
## Documento Orientado para Agentes de IA

**Data de criação:** 2026-02-14  
**Baseado em:** `ux-improvement-report.md`  
**Escopo:** 81 capturas de tela cobrindo autenticação, painel, processos, subprocessos, mapa, navegação, responsividade e relatórios

---

## 📋 Índice

1. [Visão Geral](#1-visão-geral)
2. [Contexto Arquitetural](#2-contexto-arquitetural)
3. [Melhorias Priorizadas](#3-melhorias-priorizadas)
4. [Especificações Técnicas Detalhadas](#4-especificações-técnicas-detalhadas)
5. [Checklist de Implementação](#5-checklist-de-implementação)
6. [Testes e Validação](#6-testes-e-validação)
7. [Referências Técnicas](#7-referências-técnicas)

---

## 1. Visão Geral

### 1.1 Objetivo
Padronizar e melhorar a experiência do usuário (UX) do sistema SGC através de melhorias incrementais, baseadas em evidências visuais concretas, mantendo aderência aos padrões arquiteturais do projeto.

### 1.2 Princípios Orientadores
- **Minimalismo nas mudanças:** Alterações cirúrgicas e precisas
- **Evidência visual:** Cada melhoria baseada em capturas de tela específicas
- **Consistência:** Padrões uniformes entre módulos
- **Acessibilidade:** WCAG 2.1 nível AA como mínimo
- **Responsividade:** Mobile-first com breakpoints bem definidos

### 1.3 Stack Tecnológico
- **Frontend:** Vue 3.5 + TypeScript + BootstrapVueNext
- **Padrão de componentes:** `<script setup lang="ts">`
- **Estado:** Pinia (Setup Stores)
- **Arquitetura:** View → Store → Service → API
- **Testes E2E:** Playwright (`e2e/captura-telas.spec.ts`)

---

## 2. Contexto Arquitetural

### 2.1 Estrutura de Diretórios Frontend
```
frontend/src/
├── components/           # Componentes reutilizáveis (apresentacionais)
├── views/               # Views inteligentes (lógica de negócio)
├── stores/              # Pinia stores (estado global)
├── services/            # Camada de serviços
├── api/                 # Comunicação com backend
├── utils/               # Utilitários (normalizeError, logger)
└── assets/              # Estilos, imagens
```

### 2.2 Convenções de Nomenclatura
- **Componentes Vue:** `PascalCase` (ex: `ProcessoCard.vue`)
- **Arquivos TS:** `camelCase` (ex: `processoService.ts`)
- **Stores:** `use{Nome}Store` (ex: `useProcessoStore`)
- **Idioma:** Português Brasileiro (código, comentários, mensagens)

### 2.3 Padrões de Código Existentes
- **Props/Emits:** Componentes apresentacionais recebem Props e emitem Events
- **Erro Handling:** `normalizeError` em services/stores; componentes usam `BAlert` inline
- **Logging:** `logger.info()`, `logger.warn()`, `logger.error()` (nunca `console.*`)
- **Validação:** Bean Validation no backend; validação inline no frontend

---

## 3. Melhorias Priorizadas

### 3.1 Prioridade Alta (Impacto Imediato)

#### UX-001: Padronizar Rodapé e Semântica de Botões em Modais
**Evidências:** 
- `03-processo--02-modal-iniciar-processo.png`
- `03-processo--04-modal-finalizar-processo.png`
- `05-mapa--07-modal-disponibilizar-mapa.png`
- `14-relatorios--02-modal-relatorio-andamento.png`

**Problema:** Inconsistência na ordem e estilo de botões entre modais críticos.

**Complexidade:** Média  
**Esforço estimado:** 3-4 horas  
**Arquivos afetados:** ~8-12 componentes de modal

#### UX-002: Unificar Padrão de Validação Inline
**Evidências:**
- `03-processo--10-botoes-desativados-form-vazio.png`
- `04-subprocesso--23-validacao-inline-primeira-atividade.png`
- `04-subprocesso--25-detalhe-card-com-erro.png`

**Problema:** Padrão de validação não é uniforme em todos os formulários.

**Complexidade:** Alta  
**Esforço estimado:** 5-6 horas  
**Arquivos afetados:** ~15-20 formulários

#### UX-003: Melhorar Legibilidade de Tabelas
**Evidências:**
- `02-painel--06a-tabela-processos.png`
- `07-estados--03-tabela-com-multiplos-estados.png`
- `12-historico--02-tabela-processos-finalizados.png`

**Problema:** Densidade de informação alta sem diferenciação visual clara.

**Complexidade:** Média  
**Esforço estimado:** 4-5 horas  
**Arquivos afetados:** ~6-8 componentes de tabela

### 3.2 Prioridade Média

#### UX-004: Adicionar Cabeçalho Contextual por Etapa/Perfil
**Evidências:**
- `02-painel--10-painel-gestor.png`
- `02-painel--11-painel-chefe.png`
- `04-subprocesso--01-dashboard-subprocesso.png`

**Complexidade:** Média  
**Esforço estimado:** 3-4 horas  
**Arquivos afetados:** Views principais (~10 arquivos)

#### UX-005: Padronizar Layout Base das Páginas
**Evidências:**
- `06-navegacao--01-menu-principal.png`
- `06-navegacao--05a-barra-lateral.png`

**Complexidade:** Alta  
**Esforço estimado:** 6-8 horas  
**Arquivos afetado:** Template base + todas as views

#### UX-006: Fortalecer Estado Vazio com CTA Orientado
**Evidências:**
- `02-painel--06a-tabela-processos.png` (quando vazio)

**Complexidade:** Baixa  
**Esforço estimado:** 2-3 horas  
**Arquivos afetados:** Componentes de lista/tabela

### 3.3 Prioridade Estrutural (Fundação)

#### UX-007: Criar Design Tokens
**Complexidade:** Alta  
**Esforço estimado:** 8-10 horas  
**Impacto:** Base para todas as outras melhorias

#### UX-008: Definir Regras de Responsividade
**Evidências:**
- `08-responsividade--01-desktop-1920x1080.png`
- `08-responsividade--04-mobile-375x667.png`

**Complexidade:** Alta  
**Esforço estimado:** 6-8 horas  
**Impacto:** Crítico para mobile

#### UX-009: Manter Suíte de Captura como Auditoria Visual
**Complexidade:** Baixa  
**Esforço estimado:** 2 horas (manutenção contínua)  
**Arquivo:** `e2e/captura-telas.spec.ts`

---

## 4. Especificações Técnicas Detalhadas

### 4.1 UX-001: Padronização de Botões em Modais

#### Contexto Técnico
- **Framework:** BootstrapVueNext `<BModal>`
- **Padrão atual:** Inconsistente (botões em ordens diferentes)
- **Padrão desejado:** Fixo e semântico

#### Especificação Visual

```
┌─────────────────────────────────────┐
│ Título do Modal                  [X]│
├─────────────────────────────────────┤
│                                     │
│ Conteúdo do modal                   │
│                                     │
├─────────────────────────────────────┤
│ [Cancelar]        [Confirmar/Ação] │
│  ↑ secundário      ↑ primário       │
└─────────────────────────────────────┘
```

#### Taxonomia de Botões

| Tipo | Variante BootstrapVue | Posição | Uso |
|------|----------------------|---------|-----|
| Primário | `variant="primary"` | Direita | Ação principal (Salvar, Confirmar) |
| Secundário | `variant="secondary"` | Esquerda | Cancelar, Voltar |
| Perigo | `variant="danger"` | Direita | Ações irreversíveis (Excluir, Finalizar) |

#### Template Padrão

```vue
<template>
  <BModal
    v-model="mostrar"
    :title="titulo"
    hide-footer
  >
    <template #default>
      <!-- Conteúdo do modal -->
      <slot />
    </template>
    
    <template #footer>
      <div class="d-flex justify-content-between w-100">
        <BButton 
          variant="secondary" 
          @click="cancelar"
        >
          Cancelar
        </BButton>
        <BButton 
          :variant="varianteBotaoPrincipal" 
          @click="confirmar"
          :disabled="desabilitado"
        >
          {{ textoBotaoPrincipal }}
        </BButton>
      </div>
    </template>
  </BModal>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  mostrar: boolean;
  titulo: string;
  textoBotaoPrincipal?: string;
  acaoPerigosa?: boolean;
  desabilitado?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  textoBotaoPrincipal: 'Confirmar',
  acaoPerigosa: false,
  desabilitado: false,
});

const emit = defineEmits<{
  confirmar: [];
  cancelar: [];
  'update:mostrar': [value: boolean];
}>();

const varianteBotaoPrincipal = computed(() => 
  props.acaoPerigosa ? 'danger' : 'primary'
);

function confirmar() {
  emit('confirmar');
}

function cancelar() {
  emit('cancelar');
  emit('update:mostrar', false);
}
</script>
```

#### Arquivos a Modificar

1. **Criar componente base:** `frontend/src/components/comum/ModalPadrao.vue`
2. **Modificar modais existentes:**
   - `frontend/src/components/processo/ModalIniciarProcesso.vue`
   - `frontend/src/components/processo/ModalFinalizarProcesso.vue`
   - `frontend/src/components/mapa/ModalDisponibilizarMapa.vue`
   - `frontend/src/components/relatorio/ModalRelatorioAndamento.vue`
   - E outros ~8 modais críticos

#### Teste de Validação

```typescript
// e2e/ux/botoes-modais.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Padronização de botões em modais', () => {
  test('modal iniciar processo deve ter botões na ordem correta', async ({ page }) => {
    await page.goto('/processos');
    await page.click('text=Iniciar Processo');
    
    const footer = page.locator('.modal-footer');
    const botoes = footer.locator('button');
    
    // Primeiro botão deve ser Cancelar (secundário)
    await expect(botoes.nth(0)).toHaveText('Cancelar');
    await expect(botoes.nth(0)).toHaveClass(/btn-secondary/);
    
    // Segundo botão deve ser ação primária
    await expect(botoes.nth(1)).toHaveClass(/btn-primary/);
  });
  
  test('modal com ação perigosa deve usar variant danger', async ({ page }) => {
    await page.goto('/processos/123');
    await page.click('text=Finalizar Processo');
    
    const footer = page.locator('.modal-footer');
    const botaoPrincipal = footer.locator('button').nth(1);
    
    await expect(botaoPrincipal).toHaveClass(/btn-danger/);
  });
});
```

---

### 4.2 UX-002: Padrão de Validação Inline

#### Contexto Técnico
- **Validação atual:** Parcialmente implementada em atividades
- **Objetivo:** Padronizar em todos os formulários
- **Integração:** Bean Validation (backend) + validação inline (frontend)

#### Estados de Validação

| Estado | Classe CSS | Ícone | Mensagem |
|--------|-----------|-------|----------|
| Padrão | `form-control` | - | - |
| Foco | `form-control:focus` | - | - |
| Erro | `form-control is-invalid` | ⚠️ | Abaixo do campo |
| Sucesso | `form-control is-valid` | ✓ | - |
| Desabilitado | `form-control:disabled` | - | - |

#### Template de Campo com Validação

```vue
<template>
  <div class="mb-3">
    <label :for="id" class="form-label">
      {{ label }}
      <span v-if="obrigatorio" class="text-danger">*</span>
    </label>
    
    <BFormInput
      :id="id"
      v-model="valor"
      :state="estadoValidacao"
      :placeholder="placeholder"
      :disabled="desabilitado"
      @blur="validar"
    />
    
    <BFormInvalidFeedback :state="estadoValidacao">
      {{ mensagemErro }}
    </BFormInvalidFeedback>
    
    <BFormText v-if="textoAjuda && !mensagemErro">
      {{ textoAjuda }}
    </BFormText>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

interface Props {
  id: string;
  label: string;
  modelValue: string;
  obrigatorio?: boolean;
  placeholder?: string;
  textoAjuda?: string;
  desabilitado?: boolean;
  validacao?: (valor: string) => string | null;
}

const props = withDefaults(defineProps<Props>(), {
  obrigatorio: false,
  desabilitado: false,
});

const emit = defineEmits<{
  'update:modelValue': [value: string];
}>();

const valor = computed({
  get: () => props.modelValue,
  set: (value: string) => emit('update:modelValue', value),
});

const mensagemErro = ref<string | null>(null);
const tocado = ref(false);

const estadoValidacao = computed(() => {
  if (!tocado.value) return null;
  return mensagemErro.value ? false : true;
});

function validar() {
  tocado.value = true;
  
  if (props.obrigatorio && !valor.value.trim()) {
    mensagemErro.value = 'Este campo é obrigatório';
    return;
  }
  
  if (props.validacao) {
    mensagemErro.value = props.validacao(valor.value);
  } else {
    mensagemErro.value = null;
  }
}
</script>
```

#### Resumo de Erros para Formulários Longos

```vue
<template>
  <BAlert v-if="erros.length > 0" variant="danger" class="mb-3">
    <h5 class="alert-heading">Corrija os seguintes erros:</h5>
    <ul class="mb-0">
      <li v-for="erro in erros" :key="erro.campo">
        <a href="#" @click.prevent="focarCampo(erro.campo)">
          {{ erro.mensagem }}
        </a>
      </li>
    </ul>
  </BAlert>
</template>

<script setup lang="ts">
interface Erro {
  campo: string;
  mensagem: string;
}

interface Props {
  erros: Erro[];
}

defineProps<Props>();

function focarCampo(idCampo: string) {
  const elemento = document.getElementById(idCampo);
  if (elemento) {
    elemento.focus();
    elemento.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }
}
</script>
```

#### Arquivos a Criar/Modificar

**Criar:**
1. `frontend/src/components/comum/CampoTexto.vue`
2. `frontend/src/components/comum/ResumoErros.vue`
3. `frontend/src/composables/useValidacao.ts`

**Modificar (aplicar padrão):**
1. `frontend/src/components/processo/FormularioProcesso.vue`
2. `frontend/src/components/subprocesso/FormularioAtividade.vue`
3. `frontend/src/components/mapa/FormularioMapa.vue`
4. Outros ~12-15 formulários

---

### 4.3 UX-003: Melhorar Legibilidade de Tabelas

#### Especificação Visual

**Antes (problema):**
- Densidade alta, contraste baixo
- Status sem diferenciação clara
- Ações em posições inconsistentes

**Depois (solução):**
```
┌────────────────────────────────────────────────────────────┐
│ Processos                                   [+ Novo]       │
├────────┬──────────────┬───────────┬──────────────┬─────────┤
│ Código │ Descrição    │ Status    │ Responsável  │ Ações   │
├────────┼──────────────┼───────────┼──────────────┼─────────┤
│ 001    │ Processo A   │ ⬤ Ativo   │ João Silva   │ ⋮       │
│ 002    │ Processo B   │ ⬤ Parado  │ Maria Santos │ ⋮       │
│ 003    │ Processo C   │ ⬤ Concluí.│ Pedro Costa  │ ⋮       │
└────────┴──────────────┴───────────┴──────────────┴─────────┘

Legenda:
⬤ Ativo     - Verde (#28a745)
⬤ Parado    - Amarelo (#ffc107)
⬤ Concluído - Azul (#007bff)
```

#### Componente de Badge de Status

```vue
<template>
  <span 
    class="badge status-badge" 
    :class="`status-${variantePorStatus}`"
  >
    <span class="status-indicator"></span>
    {{ textoStatus }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';

type StatusProcesso = 'ATIVO' | 'PARADO' | 'CONCLUIDO' | 'CANCELADO';

interface Props {
  status: StatusProcesso;
}

const props = defineProps<Props>();

const mapeamentoStatus: Record<StatusProcesso, { texto: string; variante: string }> = {
  ATIVO: { texto: 'Ativo', variante: 'success' },
  PARADO: { texto: 'Parado', variante: 'warning' },
  CONCLUIDO: { texto: 'Concluído', variante: 'primary' },
  CANCELADO: { texto: 'Cancelado', variante: 'secondary' },
};

const textoStatus = computed(() => mapeamentoStatus[props.status].texto);
const variantePorStatus = computed(() => mapeamentoStatus[props.status].variante);
</script>

<style scoped>
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.25rem 0.75rem;
  font-weight: 500;
  border-radius: 1rem;
}

.status-indicator {
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
  background-color: currentColor;
}

.status-success {
  background-color: #d4edda;
  color: #155724;
}

.status-warning {
  background-color: #fff3cd;
  color: #856404;
}

.status-primary {
  background-color: #cfe2ff;
  color: #084298;
}

.status-secondary {
  background-color: #e2e3e5;
  color: #41464b;
}
</style>
```

#### Template de Tabela Padronizada

```vue
<template>
  <div class="tabela-padrao">
    <!-- Cabeçalho com ação -->
    <div class="tabela-header d-flex justify-content-between align-items-center mb-3">
      <h2 class="h4 mb-0">{{ titulo }}</h2>
      <BButton 
        v-if="mostrarBotaoNovo"
        variant="primary" 
        @click="emit('novo')"
      >
        <i class="bi bi-plus-lg me-1"></i>
        {{ textoBotaoNovo }}
      </BButton>
    </div>
    
    <!-- Estado vazio -->
    <div v-if="itens.length === 0" class="estado-vazio text-center py-5">
      <i class="bi bi-inbox display-1 text-muted"></i>
      <h3 class="h5 mt-3">{{ tituloEstadoVazio }}</h3>
      <p class="text-muted">{{ descricaoEstadoVazio }}</p>
      <BButton 
        v-if="mostrarBotaoNovo"
        variant="primary" 
        @click="emit('novo')"
      >
        {{ textoBotaoNovo }}
      </BButton>
    </div>
    
    <!-- Tabela -->
    <BTable
      v-else
      :items="itens"
      :fields="campos"
      striped
      hover
      responsive
      class="tabela-conteudo"
    >
      <!-- Slot para customização de células -->
      <template v-for="(_, slot) in $slots" #[slot]="scope">
        <slot :name="slot" v-bind="scope" />
      </template>
      
      <!-- Coluna de ações padrão -->
      <template #cell(acoes)="{ item }">
        <BDropdown 
          variant="link" 
          toggle-class="text-decoration-none p-0"
          no-caret
          right
        >
          <template #button-content>
            <i class="bi bi-three-dots-vertical"></i>
          </template>
          <BDropdownItem @click="emit('visualizar', item)">
            <i class="bi bi-eye me-2"></i>Visualizar
          </BDropdownItem>
          <BDropdownItem @click="emit('editar', item)">
            <i class="bi bi-pencil me-2"></i>Editar
          </BDropdownItem>
          <BDropdownDivider />
          <BDropdownItem 
            variant="danger" 
            @click="emit('excluir', item)"
          >
            <i class="bi bi-trash me-2"></i>Excluir
          </BDropdownItem>
        </BDropdown>
      </template>
    </BTable>
  </div>
</template>

<script setup lang="ts">
interface Props {
  titulo: string;
  itens: any[];
  campos: any[];
  mostrarBotaoNovo?: boolean;
  textoBotaoNovo?: string;
  tituloEstadoVazio?: string;
  descricaoEstadoVazio?: string;
}

withDefaults(defineProps<Props>(), {
  mostrarBotaoNovo: true,
  textoBotaoNovo: 'Novo',
  tituloEstadoVazio: 'Nenhum item encontrado',
  descricaoEstadoVazio: 'Comece criando um novo item.',
});

const emit = defineEmits<{
  novo: [];
  visualizar: [item: any];
  editar: [item: any];
  excluir: [item: any];
}>();
</script>

<style scoped>
.tabela-padrao {
  background-color: #fff;
  border-radius: 0.5rem;
  padding: 1.5rem;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}

.tabela-header {
  border-bottom: 1px solid #dee2e6;
  padding-bottom: 1rem;
}

.tabela-conteudo :deep(thead) {
  background-color: #f8f9fa;
  font-weight: 600;
}

.tabela-conteudo :deep(tbody tr:hover) {
  background-color: #f8f9fa;
}

.estado-vazio i {
  opacity: 0.5;
}
</style>
```

#### Arquivos a Criar/Modificar

**Criar:**
1. `frontend/src/components/comum/TabelaPadrao.vue`
2. `frontend/src/components/comum/BadgeStatus.vue`

**Modificar:**
1. `frontend/src/views/painel/PainelView.vue`
2. `frontend/src/components/processo/TabelaProcessos.vue`
3. `frontend/src/components/historico/TabelaHistorico.vue`
4. Outros ~5-6 componentes de tabela

---

### 4.4 UX-004: Cabeçalho Contextual por Etapa/Perfil

#### Especificação

```vue
<template>
  <div class="cabecalho-contextual mb-4">
    <div class="d-flex justify-content-between align-items-start">
      <div>
        <h1 class="h3 mb-1">{{ titulo }}</h1>
        <p class="text-muted mb-2">{{ descricao }}</p>
        
        <!-- Breadcrumb para contexto -->
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb mb-0">
            <li 
              v-for="(item, index) in breadcrumb" 
              :key="index"
              class="breadcrumb-item"
              :class="{ active: index === breadcrumb.length - 1 }"
            >
              <a 
                v-if="index !== breadcrumb.length - 1" 
                :href="item.url"
              >
                {{ item.texto }}
              </a>
              <span v-else>{{ item.texto }}</span>
            </li>
          </ol>
        </nav>
      </div>
      
      <!-- Indicador de perfil/situação -->
      <div v-if="mostrarIndicador" class="indicador-situacao">
        <span class="badge bg-info">
          <i :class="iconeIndicador" class="me-1"></i>
          {{ textoIndicador }}
        </span>
      </div>
    </div>
    
    <!-- Próxima ação recomendada -->
    <BAlert 
      v-if="proximaAcao" 
      variant="info" 
      class="mt-3 mb-0"
      show
    >
      <i class="bi bi-lightbulb me-2"></i>
      <strong>Próximo passo:</strong> {{ proximaAcao }}
    </BAlert>
  </div>
</template>

<script setup lang="ts">
interface BreadcrumbItem {
  texto: string;
  url?: string;
}

interface Props {
  titulo: string;
  descricao?: string;
  breadcrumb?: BreadcrumbItem[];
  mostrarIndicador?: boolean;
  textoIndicador?: string;
  iconeIndicador?: string;
  proximaAcao?: string;
}

withDefaults(defineProps<Props>(), {
  mostrarIndicador: false,
  iconeIndicador: 'bi-info-circle',
});
</script>

<style scoped>
.cabecalho-contextual {
  background-color: #fff;
  border-radius: 0.5rem;
  padding: 1.5rem;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
}

.indicador-situacao .badge {
  font-size: 0.875rem;
  padding: 0.5rem 1rem;
}
</style>
```

#### Uso em Views

```vue
<!-- ProcessoView.vue -->
<template>
  <div class="processo-view">
    <CabecalhoContextual
      titulo="Processo #001"
      descricao="Gestão de processos administrativos"
      :breadcrumb="breadcrumb"
      mostrar-indicador
      texto-indicador="Em Andamento"
      icone-indicador="bi-play-circle"
      proxima-acao="Cadastrar atividades para o subprocesso atual"
    />
    
    <!-- Resto do conteúdo -->
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import CabecalhoContextual from '@/components/comum/CabecalhoContextual.vue';

const route = useRoute();

const breadcrumb = computed(() => [
  { texto: 'Painel', url: '/painel' },
  { texto: 'Processos', url: '/processos' },
  { texto: `Processo #${route.params.id}` },
]);
</script>
```

#### Lógica de Próxima Ação por Perfil

```typescript
// composables/useProximaAcao.ts
import { computed } from 'vue';
import { useAuthStore } from '@/stores/authStore';

interface Processo {
  situacao: string;
  etapa: string;
}

export function useProximaAcao(processo: Processo) {
  const authStore = useAuthStore();
  
  const proximaAcao = computed(() => {
    const perfil = authStore.usuario?.perfil;
    const { situacao, etapa } = processo;
    
    // ADMIN
    if (perfil === 'ADMIN') {
      if (situacao === 'CADASTRO') {
        return 'Cadastre as atividades necessárias e inicie o processo';
      }
      if (situacao === 'EM_ANDAMENTO' && etapa === 'CADASTRO_ATIVIDADES') {
        return 'Complete o cadastro de atividades do subprocesso';
      }
    }
    
    // GESTOR
    if (perfil === 'GESTOR') {
      if (etapa === 'AGUARDANDO_MAPA') {
        return 'Aguarde o mapa de competências ser disponibilizado';
      }
      if (etapa === 'PREENCHIMENTO_MAPA') {
        return 'Preencha o mapa de competências das atividades';
      }
    }
    
    // CHEFE
    if (perfil === 'CHEFE') {
      if (etapa === 'AGUARDANDO_HOMOLOGACAO') {
        return 'Revise e homologue o mapa de competências';
      }
    }
    
    return null;
  });
  
  return { proximaAcao };
}
```

---

### 4.5 UX-007: Design Tokens (Fundação)

#### Estrutura de Design Tokens

```scss
// frontend/src/assets/styles/_tokens.scss

// ========================================
// CORES
// ========================================

// Cores primárias
$cor-primaria: #007bff;
$cor-primaria-hover: #0056b3;
$cor-primaria-ativo: #004085;

// Cores semânticas
$cor-sucesso: #28a745;
$cor-sucesso-claro: #d4edda;
$cor-sucesso-texto: #155724;

$cor-aviso: #ffc107;
$cor-aviso-claro: #fff3cd;
$cor-aviso-texto: #856404;

$cor-erro: #dc3545;
$cor-erro-claro: #f8d7da;
$cor-erro-texto: #721c24;

$cor-info: #17a2b8;
$cor-info-claro: #d1ecf1;
$cor-info-texto: #0c5460;

// Tons de cinza
$cinza-100: #f8f9fa;
$cinza-200: #e9ecef;
$cinza-300: #dee2e6;
$cinza-400: #ced4da;
$cinza-500: #adb5bd;
$cinza-600: #6c757d;
$cinza-700: #495057;
$cinza-800: #343a40;
$cinza-900: #212529;

// ========================================
// TIPOGRAFIA
// ========================================

// Família de fontes
$fonte-base: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
$fonte-mono: SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;

// Tamanhos de fonte
$fonte-xs: 0.75rem;    // 12px
$fonte-sm: 0.875rem;   // 14px
$fonte-base: 1rem;     // 16px
$fonte-lg: 1.125rem;   // 18px
$fonte-xl: 1.25rem;    // 20px
$fonte-2xl: 1.5rem;    // 24px
$fonte-3xl: 1.875rem;  // 30px
$fonte-4xl: 2.25rem;   // 36px

// Pesos de fonte
$peso-normal: 400;
$peso-medio: 500;
$peso-semibold: 600;
$peso-bold: 700;

// ========================================
// ESPAÇAMENTO
// ========================================

$espacamento-xs: 0.25rem;   // 4px
$espacamento-sm: 0.5rem;    // 8px
$espacamento-md: 1rem;      // 16px
$espacamento-lg: 1.5rem;    // 24px
$espacamento-xl: 2rem;      // 32px
$espacamento-2xl: 3rem;     // 48px
$espacamento-3xl: 4rem;     // 64px

// ========================================
// BORDAS
// ========================================

$raio-borda-sm: 0.25rem;    // 4px
$raio-borda-md: 0.375rem;   // 6px
$raio-borda-lg: 0.5rem;     // 8px
$raio-borda-xl: 1rem;       // 16px
$raio-borda-pill: 50rem;    // Pill shape

$largura-borda: 1px;
$largura-borda-grossa: 2px;

// ========================================
// SOMBRAS
// ========================================

$sombra-sm: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
$sombra-md: 0 0.25rem 0.5rem rgba(0, 0, 0, 0.1);
$sombra-lg: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
$sombra-xl: 0 1rem 3rem rgba(0, 0, 0, 0.175);

// ========================================
// BREAKPOINTS (Responsividade)
// ========================================

$breakpoint-xs: 0;
$breakpoint-sm: 576px;      // Small devices (landscape phones)
$breakpoint-md: 768px;      // Medium devices (tablets)
$breakpoint-lg: 992px;      // Large devices (desktops)
$breakpoint-xl: 1200px;     // Extra large devices (large desktops)
$breakpoint-xxl: 1400px;    // Extra extra large devices

// ========================================
// Z-INDEX
// ========================================

$z-dropdown: 1000;
$z-sticky: 1020;
$z-fixed: 1030;
$z-modal-backdrop: 1040;
$z-modal: 1050;
$z-popover: 1060;
$z-tooltip: 1070;

// ========================================
// TRANSIÇÕES
// ========================================

$transicao-rapida: 150ms;
$transicao-base: 300ms;
$transicao-lenta: 500ms;

$easing-base: ease-in-out;
$easing-in: ease-in;
$easing-out: ease-out;
```

#### Uso dos Tokens

```vue
<style scoped lang="scss">
@import '@/assets/styles/tokens';

.meu-componente {
  padding: $espacamento-md;
  border-radius: $raio-borda-lg;
  box-shadow: $sombra-sm;
  transition: all $transicao-base $easing-base;
  
  &:hover {
    box-shadow: $sombra-md;
  }
}

.titulo {
  font-size: $fonte-2xl;
  font-weight: $peso-semibold;
  color: $cinza-900;
  margin-bottom: $espacamento-lg;
}

.badge-sucesso {
  background-color: $cor-sucesso-claro;
  color: $cor-sucesso-texto;
  padding: $espacamento-xs $espacamento-sm;
  border-radius: $raio-borda-pill;
  font-size: $fonte-sm;
  font-weight: $peso-medio;
}
</style>
```

---

### 4.6 UX-008: Regras de Responsividade

#### Breakpoints Oficiais

```typescript
// frontend/src/utils/breakpoints.ts

export const BREAKPOINTS = {
  xs: 0,
  sm: 576,   // Landscape phones
  md: 768,   // Tablets
  lg: 992,   // Desktops
  xl: 1200,  // Large desktops
  xxl: 1400, // Extra large desktops
} as const;

export type Breakpoint = keyof typeof BREAKPOINTS;

/**
 * Verifica se a largura atual está em um breakpoint específico ou maior
 */
export function useBreakpoint(breakpoint: Breakpoint): boolean {
  return window.innerWidth >= BREAKPOINTS[breakpoint];
}

/**
 * Retorna o breakpoint atual
 */
export function getCurrentBreakpoint(): Breakpoint {
  const width = window.innerWidth;
  
  if (width >= BREAKPOINTS.xxl) return 'xxl';
  if (width >= BREAKPOINTS.xl) return 'xl';
  if (width >= BREAKPOINTS.lg) return 'lg';
  if (width >= BREAKPOINTS.md) return 'md';
  if (width >= BREAKPOINTS.sm) return 'sm';
  return 'xs';
}
```

#### Comportamento por Componente

##### Tabelas

```scss
// Desktop (>= md)
.tabela-padrao {
  @media (min-width: $breakpoint-md) {
    // Todas as colunas visíveis
    .coluna-secundaria {
      display: table-cell;
    }
  }
  
  // Tablet (sm a md)
  @media (max-width: $breakpoint-md - 1) {
    // Esconder colunas secundárias
    .coluna-secundaria {
      display: none;
    }
  }
  
  // Mobile (< sm)
  @media (max-width: $breakpoint-sm - 1) {
    // Transformar em cards
    table, thead, tbody, tr, td, th {
      display: block;
    }
    
    thead {
      display: none;
    }
    
    tr {
      margin-bottom: 1rem;
      border: 1px solid $cinza-300;
      border-radius: $raio-borda-md;
      padding: $espacamento-md;
    }
    
    td {
      display: flex;
      justify-content: space-between;
      padding: $espacamento-sm 0;
      
      &::before {
        content: attr(data-label);
        font-weight: $peso-semibold;
        margin-right: $espacamento-sm;
      }
    }
  }
}
```

##### Modais

```scss
.modal {
  // Desktop
  @media (min-width: $breakpoint-lg) {
    .modal-dialog {
      max-width: 600px;
    }
  }
  
  // Tablet
  @media (min-width: $breakpoint-md) and (max-width: $breakpoint-lg - 1) {
    .modal-dialog {
      max-width: 500px;
      margin: 1.75rem auto;
    }
  }
  
  // Mobile
  @media (max-width: $breakpoint-md - 1) {
    .modal-dialog {
      margin: 0.5rem;
      max-height: calc(100vh - 1rem);
    }
    
    .modal-content {
      max-height: calc(100vh - 1rem);
    }
    
    .modal-body {
      overflow-y: auto;
      max-height: calc(100vh - 200px);
    }
  }
}
```

##### Botões de Ação

```vue
<template>
  <div class="acoes-container">
    <!-- Desktop: botões lado a lado -->
    <div class="acoes-desktop d-none d-md-flex">
      <BButton variant="secondary">Cancelar</BButton>
      <BButton variant="primary">Salvar</BButton>
    </div>
    
    <!-- Mobile: menu "Mais ações" -->
    <BDropdown 
      class="acoes-mobile d-md-none" 
      text="Ações" 
      variant="primary"
      block
    >
      <BDropdownItem>Salvar</BDropdownItem>
      <BDropdownItem>Cancelar</BDropdownItem>
    </BDropdown>
  </div>
</template>
```

---

## 5. Checklist de Implementação

### 5.1 Checklist por Melhoria

#### UX-001: Padronização de Botões em Modais
- [ ] Criar `ModalPadrao.vue` com footer padronizado
- [ ] Definir taxonomia de variantes (primary, secondary, danger)
- [ ] Migrar `ModalIniciarProcesso.vue` para usar padrão
- [ ] Migrar `ModalFinalizarProcesso.vue` para usar padrão
- [ ] Migrar `ModalDisponibilizarMapa.vue` para usar padrão
- [ ] Migrar `ModalRelatorioAndamento.vue` para usar padrão
- [ ] Migrar demais modais (~8 arquivos)
- [ ] Criar teste E2E para validar ordem dos botões
- [ ] Atualizar `captura-telas.spec.ts` para validar consistência

#### UX-002: Validação Inline Unificada
- [ ] Criar `CampoTexto.vue` com validação integrada
- [ ] Criar `ResumoErros.vue` para formulários longos
- [ ] Criar composable `useValidacao.ts`
- [ ] Aplicar em `FormularioProcesso.vue`
- [ ] Aplicar em `FormularioAtividade.vue`
- [ ] Aplicar em `FormularioMapa.vue`
- [ ] Aplicar em demais formulários (~12 arquivos)
- [ ] Implementar foco automático no primeiro erro
- [ ] Criar testes unitários para validação
- [ ] Criar teste E2E para fluxo completo de validação

#### UX-003: Tabelas Legíveis
- [ ] Criar `TabelaPadrao.vue` com layout otimizado
- [ ] Criar `BadgeStatus.vue` com cores semânticas
- [ ] Definir paleta de cores para status
- [ ] Aplicar em `TabelaProcessos.vue`
- [ ] Aplicar em `TabelaHistorico.vue`
- [ ] Aplicar em demais tabelas (~6 arquivos)
- [ ] Implementar estado vazio com CTA
- [ ] Testar responsividade (desktop, tablet, mobile)
- [ ] Criar testes de snapshot para badges

#### UX-004: Cabeçalho Contextual
- [ ] Criar `CabecalhoContextual.vue`
- [ ] Criar composable `useProximaAcao.ts`
- [ ] Implementar lógica por perfil (ADMIN, GESTOR, CHEFE)
- [ ] Aplicar em views principais (~10 arquivos)
- [ ] Implementar breadcrumb dinâmico
- [ ] Testar integração com router
- [ ] Validar mensagens por situação/etapa

#### UX-005: Layout Base Padronizado
- [ ] Criar `LayoutPadrao.vue` como template base
- [ ] Definir grid de espaçamentos
- [ ] Padronizar posição de títulos e ações
- [ ] Migrar views para usar layout base
- [ ] Testar consistência visual entre módulos

#### UX-006: Estado Vazio
- [ ] Criar `EstadoVazio.vue` reutilizável
- [ ] Definir mensagens por contexto
- [ ] Aplicar em todas as listas/tabelas
- [ ] Incluir CTAs orientados

#### UX-007: Design Tokens
- [ ] Criar `_tokens.scss` com todas as variáveis
- [ ] Definir cores semânticas
- [ ] Definir escala tipográfica
- [ ] Definir escala de espaçamento
- [ ] Definir sombras e bordas
- [ ] Definir breakpoints oficiais
- [ ] Documentar uso dos tokens
- [ ] Migrar componentes para usar tokens

#### UX-008: Responsividade
- [ ] Criar `breakpoints.ts` com utilitários
- [ ] Definir comportamento de tabelas no mobile
- [ ] Definir comportamento de modais no mobile
- [ ] Definir comportamento de ações no mobile
- [ ] Testar em todos os breakpoints (xs, sm, md, lg, xl, xxl)
- [ ] Atualizar `captura-telas.spec.ts` com mais resoluções

#### UX-009: Suíte de Auditoria Visual
- [ ] Manter `captura-telas.spec.ts` atualizado
- [ ] Adicionar capturas para novos componentes
- [ ] Criar comparação visual automatizada
- [ ] Documentar processo de auditoria

---

## 6. Testes e Validação

### 6.1 Estratégia de Testes

#### Testes Unitários (Vitest)
- **Alvo:** Componentes reutilizáveis
- **Foco:** Props, eventos, validação, lógica
- **Cobertura esperada:** >85%

```typescript
// CampoTexto.spec.ts
import { mount } from '@vue/test-utils';
import { describe, it, expect } from 'vitest';
import CampoTexto from '@/components/comum/CampoTexto.vue';

describe('CampoTexto', () => {
  it('deve mostrar erro quando campo obrigatório está vazio', async () => {
    const wrapper = mount(CampoTexto, {
      props: {
        id: 'teste',
        label: 'Nome',
        modelValue: '',
        obrigatorio: true,
      },
    });
    
    await wrapper.find('input').trigger('blur');
    
    expect(wrapper.find('.invalid-feedback').text()).toBe('Este campo é obrigatório');
    expect(wrapper.find('input').classes()).toContain('is-invalid');
  });
  
  it('deve limpar erro quando campo é preenchido', async () => {
    const wrapper = mount(CampoTexto, {
      props: {
        id: 'teste',
        label: 'Nome',
        modelValue: '',
        obrigatorio: true,
      },
    });
    
    // Trigger erro
    await wrapper.find('input').trigger('blur');
    expect(wrapper.find('input').classes()).toContain('is-invalid');
    
    // Preencher campo
    await wrapper.find('input').setValue('João');
    await wrapper.find('input').trigger('blur');
    
    expect(wrapper.find('input').classes()).toContain('is-valid');
    expect(wrapper.find('.invalid-feedback').exists()).toBe(false);
  });
});
```

#### Testes E2E (Playwright)
- **Alvo:** Fluxos críticos de usuário
- **Foco:** Integração, navegação, validação end-to-end
- **Cobertura:** Cenários de alto impacto

```typescript
// e2e/ux/validacao-formularios.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Validação de formulários', () => {
  test('deve mostrar erro inline ao submeter formulário vazio', async ({ page }) => {
    await page.goto('/processos/novo');
    
    // Tentar submeter sem preencher
    await page.click('button[type="submit"]');
    
    // Verificar resumo de erros no topo
    const resumoErros = page.locator('.alert-danger');
    await expect(resumoErros).toBeVisible();
    await expect(resumoErros).toContainText('Corrija os seguintes erros');
    
    // Verificar erro inline no campo
    const campoNome = page.locator('#nome');
    await expect(campoNome).toHaveClass(/is-invalid/);
    
    const mensagemErro = page.locator('#nome + .invalid-feedback');
    await expect(mensagemErro).toContainText('Este campo é obrigatório');
  });
  
  test('deve focar no primeiro campo com erro ao clicar no resumo', async ({ page }) => {
    await page.goto('/processos/novo');
    await page.click('button[type="submit"]');
    
    // Clicar no link do erro no resumo
    await page.click('.alert-danger ul li:first-child a');
    
    // Verificar se campo recebeu foco
    const campoFocado = await page.evaluate(() => document.activeElement?.id);
    expect(campoFocado).toBe('nome');
  });
});
```

#### Testes Visuais (Captura de Telas)
- **Alvo:** Consistência visual
- **Foco:** Comparação antes/depois
- **Execução:** A cada ciclo de melhoria

```bash
# Gerar capturas atuais
npm run test:e2e:captura

# Comparar com baseline
npm run test:e2e:visual-diff
```

### 6.2 Critérios de Aceitação

Para cada melhoria implementada:

#### Funcional
- ✅ Funcionalidade existente não foi quebrada
- ✅ Nova funcionalidade funciona conforme especificado
- ✅ Validação funciona corretamente
- ✅ Mensagens de erro são claras

#### Visual
- ✅ Layout é consistente entre páginas
- ✅ Cores seguem paleta semântica
- ✅ Espaçamentos seguem escala definida
- ✅ Tipografia é legível e hierárquica

#### Responsividade
- ✅ Funciona em desktop (≥1200px)
- ✅ Funciona em tablet (768px-1199px)
- ✅ Funciona em mobile (≤767px)
- ✅ Não há quebra de layout em nenhum breakpoint

#### Acessibilidade
- ✅ Contraste mínimo 4.5:1 (texto normal)
- ✅ Contraste mínimo 3:1 (texto grande)
- ✅ Navegação por teclado funciona
- ✅ Leitores de tela conseguem navegar
- ✅ Labels associados a campos

#### Performance
- ✅ Não há regressão de performance
- ✅ Componentes não causam re-renders desnecessários
- ✅ Imagens são otimizadas
- ✅ CSS é minificado

---

## 7. Referências Técnicas

### 7.1 Documentação do Projeto
- **Arquitetura:** `/ARCHITECTURE.md`
- **Frontend Patterns:** `/frontend/FRONTEND-PATTERNS.md`
- **Padrões de Código:** `/frontend/CODE-PATTERNS.md`
- **ADRs:** `/docs/adr/`
- **Guias:** `/docs/guias/`

### 7.2 Documentação de Frameworks
- **Vue 3:** https://vuejs.org/guide/
- **TypeScript:** https://www.typescriptlang.org/docs/
- **BootstrapVueNext:** https://bootstrap-vue-next.github.io/bootstrap-vue-next/
- **Pinia:** https://pinia.vuejs.org/
- **Playwright:** https://playwright.dev/

### 7.3 Acessibilidade
- **WCAG 2.1:** https://www.w3.org/WAI/WCAG21/quickref/
- **ARIA Authoring Practices:** https://www.w3.org/WAI/ARIA/apg/

### 7.4 Design System References
- **Material Design:** https://m3.material.io/
- **Human Interface Guidelines:** https://developer.apple.com/design/human-interface-guidelines/
- **Fluent UI:** https://fluent2.microsoft.design/

---

## 8. Notas para Agentes de IA

### 8.1 Ao Implementar Melhorias

1. **Sempre ler o contexto completo:**
   - Evidências visuais mencionadas
   - Arquivos afetados
   - Padrões arquiteturais (ADRs)

2. **Seguir ordem de prioridade:**
   - Alta → Média → Estrutural
   - Dentro de cada nível, começar pelo menor impacto

3. **Fazer mudanças incrementais:**
   - Uma melhoria por vez
   - Testar após cada mudança
   - Commit frequente com `report_progress`

4. **Validar com testes:**
   - Rodar testes unitários: `npm run test:unit`
   - Rodar typecheck: `npm run typecheck`
   - Rodar lint: `npm run lint`
   - Gerar capturas: `npm run test:e2e:captura`

5. **Documentar decisões:**
   - Atualizar `ux-improvement-tracking.md`
   - Registrar problemas encontrados
   - Anotar decisões de design

### 8.2 Comandos Úteis

```bash
# Frontend
cd /home/runner/work/sgc/sgc/frontend
npm run dev              # Servidor de desenvolvimento
npm run typecheck        # Verificar tipos TypeScript
npm run lint             # Linter
npm run lint:fix         # Corrigir problemas de lint
npm run test:unit        # Testes unitários
npm run test:unit:watch  # Testes em modo watch

# E2E
cd /home/runner/work/sgc/sgc
npm run test:e2e:captura      # Gerar capturas de tela
npm run test:e2e -- --ui      # Executar com interface visual

# Backend (se necessário)
cd /home/runner/work/sgc/sgc
./gradlew :backend:test  # Testes backend
```

### 8.3 Padrão de Commits

```
UX-001: Padronizar botões em modal de iniciar processo

- Migrado para usar ModalPadrao.vue
- Ordem fixa: Cancelar (esq) + Confirmar (dir)
- Aplicado variant="primary" para ação principal

Evidências: 03-processo--02-modal-iniciar-processo.png
```

### 8.4 Checklist Antes de Finalizar

- [ ] Código segue convenções do projeto (Português BR)
- [ ] Componentes são reutilizáveis e apresentacionais
- [ ] Validação funciona corretamente
- [ ] Mensagens de erro são claras
- [ ] Responsividade testada (mobile, tablet, desktop)
- [ ] Acessibilidade validada (contraste, navegação por teclado)
- [ ] Testes passam (`npm run test:unit`)
- [ ] Typecheck passa (`npm run typecheck`)
- [ ] Lint passa (`npm run lint`)
- [ ] Capturas atualizadas (`npm run test:e2e:captura`)
- [ ] `ux-improvement-tracking.md` atualizado
- [ ] Commit com `report_progress`

---

**Última atualização:** 2026-02-14  
**Versão:** 1.0.0  
**Responsável:** Sistema de Agentes de IA
