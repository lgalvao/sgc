# Relatório de Uso do BootstrapVueNext (BVN) no Frontend SGC

**Data:** Março de 2026  
**Versão BVN no projeto:** `^0.43.9`  
**Arquivos vue analisados:** 48 componentes e views  

---

## Sumário executivo

O frontend do SGC utiliza o BootstrapVueNext (BVN) como framework de UI, mas de forma inconsistente: enquanto alguns arquivos
fazem uso exemplar dos componentes BVN, outros contornam completamente o framework — em especial `ModalAcaoBloco.vue`, que usa a API
JavaScript do Bootstrap diretamente. Há também padrões recorrentes de HTML Bootstrap bruto que deveriam ser substituídos por
componentes BVN equivalentes, como badges, alertas, spinners, labels de formulário e listas.

O código é sólido e funcional; as oportunidades de melhoria são principalmente de consistência, manutenibilidade e aproveitamento
das abstrações que o BVN oferece.

---

## Escopo da Análise

Todos os arquivos `.vue` em `frontend/src/` foram analisados. O foco foi em:

1. Elementos HTML Bootstrap brutos onde BVN oferece componente equivalente
2. Uso de API JavaScript do Bootstrap (`bootstrap` JS) onde BVN gerencia o ciclo de vida
3. Padrões BVN disponíveis e documentados que não estão sendo aproveitados

> **Fonte BVN:** Componentes verificados em `bootstrap-vue-next/src/components/index.ts` e documentação do repositório oficial.
> BVN expõe: `BAlert`, `BAvatar`, `BBadge`, `BBreadcrumb`, `BButton`, `BCard`, `BCarousel`, `BCollapse`, `BContainer`,
> `BDropdown`, `BForm`, `BFormCheckbox`, `BFormFile`, `BFormGroup`, `BFormInput`, `BFormRadio`, `BFormSelect`,
> `BFormSpinbutton`, `BFormTags`, `BFormTextarea`, `BInputGroup`, `BListGroup`, `BListGroupItem`, `BModal`, `BNav`,
> `BNavbar`, `BOffcanvas`, `BOverlay`, `BPagination`, `BPlaceholder`, `BPopover`, `BProgress`, `BSpinner`, `BTable`,
> `BTabs`, `BToast`, `BTooltip`, além de sub-componentes (`BFormText`, `BFormFloatingLabel`, `BFormRow`, etc.)

---

## Inventário de Problemas

### Gravidade 1 — Crítico (ignora completamente o BVN)

#### Problema 1.1 — `ModalAcaoBloco.vue`: Modal Bootstrap puro com API JS direta

**Arquivo:** `frontend/src/components/processo/ModalAcaoBloco.vue`

**O problema:** Este componente é o único no projeto que ignora completamente o BVN. Ele implementa um modal usando HTML Bootstrap bruto e inicializa o modal com a API JavaScript do Bootstrap diretamente via `import { Modal } from 'bootstrap'` e `new Modal(modalElement.value)`. Isso cria vários problemas:

- Gerenciamento de ciclo de vida manual e frágil (o `Modal` bootstrap JS não se integra com o reativo Vue)  
- Impossibilidade de usar eventos Vue (`v-model`, `@hidden`, etc.)  
- Duplicação de lógica que o `BModal` já oferece
- Raw HTML aninhado (`modal-dialog`, `modal-content`, `modal-header`, etc.)
- Botões com `data-bs-dismiss="modal"` (bypassa Vue completamente)

**Prova — código atual (linhas 1–74):**

```html
<!-- ModalAcaoBloco.vue - ATUAL -->
<div :id="id" ref="modalElement" aria-hidden="true" class="modal fade" tabindex="-1">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">{{ titulo }}</h5>
        <button aria-label="Fechar" class="btn-close" data-bs-dismiss="modal" type="button"></button>
      </div>
      <div class="modal-body">
        <p class="mb-3">{{ texto }}</p>
        <div v-if="erro" class="alert alert-danger mb-3">
          {{ erro }}
        </div>
        <div class="table-responsive" style="max-height: 300px; overflow-y: auto;">
          <table class="table table-sm table-hover">
            <thead>
              <tr>
                <th style="width: 40px">
                  <input :checked="todosSelecionados" class="form-check-input" type="checkbox" @change="toggleTodos">
                </th>
                <th>Sigla</th> <th>Nome</th> <th>Situação</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="unidade in unidades" :key="unidade.codigo">
                <td><input v-model="selecionadosLocal" :value="unidade.codigo" class="form-check-input" type="checkbox"></td>
                ...
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" data-bs-dismiss="modal" type="button">Cancelar</button>
        <button class="btn btn-primary" type="button" @click="confirmar">
          <!-- <output> é semanticamente incorreto aqui — deve ser <span> ou BSpinner -->
          <output v-if="processando" class="spinner-border spinner-border-sm me-2"></output>
          {{ rotuloBotao }}
        </button>
      </div>
    </div>
  </div>
</div>
```

```typescript
// Script - ATUAL (anti-padrão: Bootstrap JS diretamente)
import { Modal } from 'bootstrap';
onMounted(() => {
  if (modalElement.value) {
    modalInstance.value = new Modal(modalElement.value);
  }
});
function abrir() { modalInstance.value?.show(); }
function fechar() { modalInstance.value?.hide(); }
```

**Como deve ficar — com BVN:**

```html
<!-- ModalAcaoBloco.vue - PROPOSTO -->
<BModal
    v-model="mostrar"
    :title="titulo"
    size="lg"
    centered
    @hide="fechar"
>
  <p class="mb-3">{{ texto }}</p>

  <BAlert v-if="erro" :model-value="true" variant="danger" class="mb-3">
    {{ erro }}
  </BAlert>

  <div v-if="mostrarDataLimite" class="mb-3">
    <BFormGroup label="Data limite" label-for="dataLimiteBloco" label-class="required">
      <InputData id="dataLimiteBloco" v-model="dataLimite" ... />
    </BFormGroup>
  </div>

  <BTable
      :items="unidades"
      :fields="campos"
      small
      hover
      responsive
      sticky-header="300px"
  >
    <template #head(selecao)>
      <BFormCheckbox :model-value="todosSelecionados" @change="toggleTodos" />
    </template>
    <template #cell(selecao)="{ item }">
      <BFormCheckbox v-model="selecionadosLocal" :value="item.codigo" :disabled="processando" />
    </template>
  </BTable>

  <template #footer>
    <BButton variant="secondary" :disabled="processando" @click="fechar">Cancelar</BButton>
    <BButton variant="primary" :disabled="processando || selecionadosLocal.length === 0" @click="confirmar">
      <BSpinner v-if="processando" small class="me-2" aria-hidden="true" />
      {{ rotuloBotao }}
    </BButton>
  </template>
</BModal>
```

```typescript
// Script - PROPOSTO (sem Bootstrap JS, sem ref para elemento DOM)
import { BAlert, BButton, BFormCheckbox, BModal, BSpinner, BTable } from 'bootstrap-vue-next';

const mostrar = ref(false);
function abrir() { mostrar.value = true; }
function fechar() { mostrar.value = false; }
```

**Impacto da mudança:** Remove a dependência de `import { Modal } from 'bootstrap'`, elimina ~60 linhas de HTML
Bootstrap bruto, integra corretamente com o sistema reativo do Vue.

---

### Gravidade 2 — Alto (padrão recorrente em múltiplos arquivos)

#### Problema 2.1 — Spinners de carregamento brutos em vez de `BSpinner`

**Arquivos afetados:**

| Arquivo | Linha | Código atual |
|---------|-------|-------------|
| `ModalConfirmacao.vue` | 29 | `<output class="spinner-border spinner-border-sm me-1" />` |
| `HistoricoAnaliseModal.vue` | 13 | `<output class="spinner-border text-primary">` |
| `ImpactoMapaModal.vue` | 11 | `<output class="spinner-border text-primary">` |
| `AdministradoresView.vue` | 17 | `<div class="spinner-border text-primary" role="status">` |
| `ParametrosView.vue` | 6 | `<div class="spinner-border text-primary" role="status">` |

Note que o `BSpinner` já é usado corretamente em outros arquivos (`AceitarMapaModal.vue`, `SubprocessoView.vue`), tornando essa inconsistência ainda mais notável.

> **Problema extra de semântica HTML:** Além de não usar BVN, vários casos utilizam o elemento `<output>` para o spinner.
> O `<output>` é semanticamente reservado para **resultados de cálculos ou ações de formulário** (ex: `<output for="a b">resultado</output>`).
> Para indicadores de carregamento, o correto é `<span>` ou `<div>` (com `role="status"`). O `BSpinner` resolve isso automaticamente.

**Prova — `ModalConfirmacao.vue` (linha 29–31):**

```html
<!-- ATUAL — dois problemas: elemento <output> semanticamente errado + classe Bootstrap bruta -->
<output v-if="loading" aria-hidden="true" class="spinner-border spinner-border-sm me-1" />
```

**Como deve ficar:**

```html
<!-- PROPOSTO — BSpinner renderiza <span role="status"> com semântica correta -->
<BSpinner v-if="loading" small class="me-1" aria-hidden="true" />
```

**Prova — `AdministradoresView.vue` (linhas 16–20):**

```html
<!-- ATUAL -->
<div v-if="carregandoAdmins" class="text-center py-4">
  <div class="spinner-border text-primary" role="status">
    <span class="visually-hidden">Carregando...</span>
  </div>
</div>
```

**Como deve ficar:**

```html
<!-- PROPOSTO -->
<div v-if="carregandoAdmins" class="text-center py-4">
  <BSpinner label="Carregando..." variant="primary" />
</div>
```

O `BSpinner` trata automaticamente o `aria-label` via a prop `label`, dispensando o `<span class="visually-hidden">`.

---

#### Problema 2.2 — Alertas brutos em vez de `BAlert`

**Arquivos afetados:**

| Arquivo | Código atual |
|---------|-------------|
| `ModalAcaoBloco.vue` | `<div v-if="erro" class="alert alert-danger mb-3">` |
| `ModalMapaDisponibilizar.vue` | `<div v-if="fieldErrors?.generic" class="alert alert-danger mb-3">` |
| `CriarCompetenciaModal.vue` | `<div v-if="fieldErrors?.generic" class="alert alert-danger mb-4">` |

O `BAlert` já é usado corretamente em muitos outros lugares do projeto. A inconsistência está apenas nesses três casos.

**Prova — `ModalMapaDisponibilizar.vue` (linha 15):**

```html
<!-- ATUAL -->
<div v-if="fieldErrors?.generic" class="alert alert-danger mb-3">
  {{ fieldErrors.generic }}
</div>
```

**Como deve ficar:**

```html
<!-- PROPOSTO -->
<BAlert v-if="fieldErrors?.generic" :model-value="true" variant="danger" class="mb-3">
  {{ fieldErrors.generic }}
</BAlert>
```

**Prova — `CriarCompetenciaModal.vue` (linha 11):**

```html
<!-- ATUAL -->
<div v-if="fieldErrors?.generic" class="alert alert-danger mb-4">
  {{ fieldErrors.generic }}
</div>
```

**Como deve ficar:**

```html
<!-- PROPOSTO -->
<BAlert v-if="fieldErrors?.generic" :model-value="true" variant="danger" class="mb-4">
  {{ fieldErrors.generic }}
</BAlert>
```

---

#### Problema 2.3 — Badges brutos em vez de `BBadge`

O `BBadge` existe no BVN mas não é usado em nenhum lugar do projeto. Há pelo menos 4 ocorrências de `<span class="badge ...">`.

**Arquivos afetados:**

| Arquivo | Linha | Código atual |
|---------|-------|-------------|
| `CompetenciaCard.vue` | 57 | `<span class="badge bg-secondary ms-2">` |
| `CriarCompetenciaModal.vue` | 56 | `<span class="badge bg-secondary ms-2">` |
| `MapaVisualizacaoView.vue` | 105 | `<span class="badge bg-white text-dark border fw-normal py-1 px-2">` |
| `CadastroView.vue` | 19–20 | `<span :class="badgeClass(...)" class="badge fs-6">` |

**Prova — `CompetenciaCard.vue` (linhas 55–60):**

```html
<!-- ATUAL -->
<span
    v-if="(atividade.conhecimentos?.length ?? 0) > 0"
    v-b-tooltip.html.top="getConhecimentosTooltip(atividade.codigo)"
    class="badge bg-secondary ms-2"
    data-testid="cad-mapa__txt-badge-conhecimentos-1"
>
  {{ atividade.conhecimentos.length }}
</span>
```

**Como deve ficar:**

```html
<!-- PROPOSTO -->
<BBadge
    v-if="(atividade.conhecimentos?.length ?? 0) > 0"
    v-b-tooltip.html.top="getConhecimentosTooltip(atividade.codigo)"
    variant="secondary"
    class="ms-2"
    data-testid="cad-mapa__txt-badge-conhecimentos-1"
>
  {{ atividade.conhecimentos.length }}
</BBadge>
```

**Prova — `MapaVisualizacaoView.vue` (linhas 103–109) — badge com estilo personalizado:**

```html
<!-- ATUAL -->
<span
    v-for="c in atividade.conhecimentos"
    :key="c.codigo"
    class="badge bg-white text-dark border fw-normal py-1 px-2"
    data-testid="txt-conhecimento-item"
>
  <i aria-hidden="true" class="bi bi-book me-1 text-info"/>
  {{ c.descricao }}
</span>
```

**Como deve ficar (usando BBadge com bg-variant/text-variant):**

```html
<!-- PROPOSTO -->
<BBadge
    v-for="c in atividade.conhecimentos"
    :key="c.codigo"
    bg-variant="white"
    text-variant="dark"
    class="border fw-normal py-1 px-2"
    data-testid="txt-conhecimento-item"
>
  <i aria-hidden="true" class="bi bi-book me-1 text-info"/>
  {{ c.descricao }}
</BBadge>
```

**Prova — `CadastroView.vue` (badge de situação com classe dinâmica):**

```html
<!-- ATUAL -->
<span
    :class="badgeClass(subprocesso.situacao)"
    class="badge fs-6"
    data-testid="cad-atividades__txt-badge-situacao"
>{{ formatSituacaoSubprocesso(subprocesso.situacao) }}</span>
```

**Como deve ficar:**

```html
<!-- PROPOSTO -->
<BBadge
    :variant="badgeVariant(subprocesso.situacao)"
    class="fs-6"
    data-testid="cad-atividades__txt-badge-situacao"
>{{ formatSituacaoSubprocesso(subprocesso.situacao) }}</BBadge>
```

> **Nota:** A função `badgeClass` retorna strings como `badge bg-success text-white`. Ao migrar para `BBadge`, a lógica deve ser adaptada para retornar apenas o nome da variante (ex: `'success'`), usando a prop `variant`.

---

#### Problema 2.4 — Listas Bootstrap brutas em vez de `BListGroup`/`BListGroupItem`

**Arquivo afetado:** `frontend/src/components/mapa/ImpactoMapaModal.vue`

Este arquivo usa extensivamente `<ul class="list-group">` e `<li class="list-group-item">` em quatro seções distintas.
O BVN oferece `BListGroup` e `BListGroupItem` como componentes dedicados.

**Prova — seção "Atividades inseridas" (linhas 30–53):**

```html
<!-- ATUAL -->
<ul class="list-group" data-testid="lista-atividades-inseridas">
  <li
      v-for="ativ in impacto.atividadesInseridas"
      :key="ativ.codigo"
      class="list-group-item"
  >
    <strong>{{ ativ.descricao }}</strong>
    ...
  </li>
</ul>
```

**Como deve ficar:**

```html
<!-- PROPOSTO -->
<BListGroup data-testid="lista-atividades-inseridas">
  <BListGroupItem
      v-for="ativ in impacto.atividadesInseridas"
      :key="ativ.codigo"
  >
    <strong>{{ ativ.descricao }}</strong>
    ...
  </BListGroupItem>
</BListGroup>
```

**Para a seção de competências impactadas com `list-group-flush` (linhas 111–122):**

```html
<!-- ATUAL -->
<ul class="list-group list-group-flush">
  <li class="list-group-item text-muted small">...</li>
</ul>
```

**Como deve ficar:**

```html
<!-- PROPOSTO -->
<BListGroup flush>
  <BListGroupItem class="text-muted small">...</BListGroupItem>
</BListGroup>
```

---

### Gravidade 3 — Médio (inconsistências pontuais que degradam uniformidade)

#### Problema 3.1 — Tabelas brutas em vez de `BTable`

**Arquivos afetados:**

| Arquivo | Linhas | Situação |
|---------|--------|---------|
| `AdministradoresView.vue` | 34–63 | `<table class="table table-striped table-hover">` |
| `TreeTable.vue` | 33–34 | `<table class="table table-striped table-hover m-0">` |
| `ModalAcaoBloco.vue` | 29–55 | `<table class="table table-sm table-hover">` (já coberto em 1.1) |

**Prova — `AdministradoresView.vue` (linhas 34–63):**

```html
<!-- ATUAL -->
<div v-else class="table-responsive">
  <table class="table table-striped table-hover">
    <thead>
      <tr>
        <th>Nome</th>
        <th>Título eleitoral</th>
        <th>Matrícula</th>
        <th>Unidade</th>
        <th class="text-end">Ações</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="admin in administradores" :key="admin.tituloEleitoral">
        <td>{{ admin.nome }}</td>
        <td>{{ admin.tituloEleitoral }}</td>
        <td>{{ admin.matricula }}</td>
        <td>{{ admin.unidadeSigla }}</td>
        <td class="text-end">
          <LoadingButton ... />
        </td>
      </tr>
    </tbody>
  </table>
</div>
```

**Como deve ficar:**

```html
<!-- PROPOSTO -->
<BTable
    v-else
    :fields="campos"
    :items="administradores"
    striped
    hover
    responsive
>
  <template #cell(acoes)="{ item }">
    <div class="text-end">
      <LoadingButton
          :loading="removendoAdmin === item.tituloEleitoral"
          icon="trash"
          size="sm"
          text="Remover"
          variant="outline-danger"
          @click="confirmarRemocao(item)"
      />
    </div>
  </template>
</BTable>
```

```typescript
// Script - adicionar definição dos campos
const campos = [
  { key: 'nome', label: 'Nome' },
  { key: 'tituloEleitoral', label: 'Título eleitoral' },
  { key: 'matricula', label: 'Matrícula' },
  { key: 'unidadeSigla', label: 'Unidade' },
  { key: 'acoes', label: 'Ações', thClass: 'text-end' },
];
```

> **Nota sobre `TreeTable.vue`:** Este componente implementa uma tabela em árvore customizada com suporte a expansão de linhas.
> A migração para `BTable` é possível usando o slot `row-expansion` do BVN, mas a complexidade é maior. Recomenda-se tratar
> separadamente após os casos mais simples.

---

#### Problema 3.2 — Entradas de formulário e labels brutas em vez de `BFormGroup`

O `BFormGroup` do BVN encapsula o padrão `<label> + <input> + feedback`, evitando repetição. Ele já é usado exemplarmente
em `ProcessoFormFields.vue` e `SubprocessoModal.vue`, mas está ausente em outros formulários.

**Arquivos afetados com `<label class="form-label">` brutos:**

| Arquivo | Quantidade de labels brutas |
|---------|---------------------------|
| `LoginView.vue` | 3 labels (`titulo`, `senha`, `perfil`) |
| `AtribuicaoTemporariaView.vue` | 3 labels (`usuario`, `dataInicio`, `dataTermino`, `justificativa`) |
| `ParametrosView.vue` | 2 labels (`diasInativacao`, `diasAlertaNovo`) |
| `ModalMapaDisponibilizar.vue` | 2 labels (`dataLimite`, `observacoes`) |
| `AceitarMapaModal.vue` | 1 label (`observacao-textarea`) |
| `MapaVisualizacaoView.vue` | 3 labels em modais |
| `AdministradoresView.vue` | 1 label no modal de adição |

**Prova — `ParametrosView.vue` (linhas 25–45):**

```html
<!-- ATUAL -->
<div class="mb-3">
  <label class="form-label" for="diasInativacao">
    Dias para inativação... <span aria-hidden="true" class="text-danger">*</span>
  </label>
  <input
      id="diasInativacao"
      v-model="form.diasInativacao"
      aria-describedby="diasInativacaoHelp"
      class="form-control"
      min="1"
      required
      type="number"
  />
  <div id="diasInativacaoHelp" class="form-text">
    Dias depois da finalização de um processo...
  </div>
</div>
```

**Como deve ficar:**

```html
<!-- PROPOSTO -->
<BFormGroup
    label-for="diasInativacao"
    class="mb-3"
>
  <template #label>
    Dias para inativação... <span aria-hidden="true" class="text-danger">*</span>
  </template>
  <template #description>
    Dias depois da finalização de um processo...
  </template>
  <BFormInput
      id="diasInativacao"
      v-model="form.diasInativacao"
      min="1"
      required
      type="number"
  />
</BFormGroup>
```

> O slot `#description` do `BFormGroup` renderiza automaticamente o texto de ajuda com a classe `form-text`, eliminando a necessidade do `<div class="form-text">` e do `aria-describedby` manual.

**Prova — `ParametrosView.vue` usa `<form>` bruto:**

```html
<!-- ATUAL -->
<form @submit.prevent="salvar">
```

**Como deve ficar:**

```html
<!-- PROPOSTO -->
<BForm @submit.prevent="salvar">
```

---

#### Problema 3.3 — Grid Bootstrap bruto em vez de `BRow`/`BCol`

**Arquivos afetados:**

| Arquivo | Código atual |
|---------|-------------|
| `AtribuicaoTemporariaView.vue` | `<div class="row">` / `<div class="col-md-6 mb-3">` |
| `AtividadeItem.vue` | `<div class="row g-2 align-items-center">` |
| `CadAtividadeForm.vue` | Usa `BForm class="row g-2"` — correto, mas sem BRow |

**Prova — `AtribuicaoTemporariaView.vue` (linhas 46–73):**

```html
<!-- ATUAL -->
<div class="row">
  <div class="col-md-6 mb-3">
    <label class="form-label" for="dataInicio">Data de Início</label>
    <InputData id="dataInicio" v-model="dataInicio" ... />
  </div>
  <div class="col-md-6 mb-3">
    <label class="form-label" for="dataTermino">Data de Término</label>
    <InputData id="dataTermino" v-model="dataTermino" ... />
  </div>
</div>
```

**Como deve ficar:**

```html
<!-- PROPOSTO -->
<BRow>
  <BCol md="6" class="mb-3">
    <BFormGroup label="Data de Início" label-for="dataInicio">
      <InputData id="dataInicio" v-model="dataInicio" ... />
    </BFormGroup>
  </BCol>
  <BCol md="6" class="mb-3">
    <BFormGroup label="Data de Término" label-for="dataTermino">
      <InputData id="dataTermino" v-model="dataTermino" ... />
    </BFormGroup>
  </BCol>
</BRow>
```

---

#### Problema 3.4 — Botões brutos em vez de `BButton`

**Arquivos afetados:**

| Arquivo | Código atual | Motivo |
|---------|-------------|--------|
| `ArvoreUnidades.vue` | `<button class="btn btn-sm btn-outline-primary">` (2 botões) | Botões de selecionar/desmarcar todas |
| `TreeRowItem.vue` | `<button class="btn btn-link ...">` | Toggle de expandir linha |

**Prova — `ArvoreUnidades.vue` (linhas 4–16):**

```html
<!-- ATUAL -->
<button
    aria-label="Selecionar todas as unidades elegíveis"
    class="btn btn-sm btn-outline-primary"
    type="button"
    @click="selecionarTodas"
>
  <i aria-hidden="true" class="bi bi-check-all me-1"/>
</button>
<button
    aria-label="Desmarcar todas as unidades"
    class="btn btn-sm btn-outline-secondary"
    type="button"
    @click="deselecionarTodas"
>
  <i aria-hidden="true" class="bi bi-x-lg me-1"/>
</button>
```

**Como deve ficar:**

```html
<!-- PROPOSTO -->
<BButton
    aria-label="Selecionar todas as unidades elegíveis"
    size="sm"
    variant="outline-primary"
    @click="selecionarTodas"
>
  <i aria-hidden="true" class="bi bi-check-all me-1"/>
</BButton>
<BButton
    aria-label="Desmarcar todas as unidades"
    size="sm"
    variant="outline-secondary"
    @click="deselecionarTodas"
>
  <i aria-hidden="true" class="bi bi-x-lg me-1"/>
</BButton>
```

---

#### Problema 3.5 — `<input class="form-control">` bruto em vez de `BFormInput`

**Arquivo afetado:** `AdministradoresView.vue` (linhas 83–93), dentro do modal de adição de administrador.

**Prova:**

```html
<!-- ATUAL -->
<input
    id="usuarioTitulo"
    ref="inputTituloRef"
    v-model="novoAdminTitulo"
    class="form-control"
    maxlength="12"
    placeholder="Digite o título eleitoral"
    required
    type="text"
    @keydown.enter.prevent="adicionarAdmin"
/>
```

**Como deve ficar:**

```html
<!-- PROPOSTO -->
<BFormInput
    id="usuarioTitulo"
    ref="inputTituloRef"
    v-model="novoAdminTitulo"
    maxlength="12"
    placeholder="Digite o título eleitoral"
    required
    type="text"
    @keydown.enter.prevent="adicionarAdmin"
/>
```

> **Nota:** O `ref` precisa ser atualizado para `ref<InstanceType<typeof BFormInput>>` e o `.focus()` chamado via `inputTituloRef.value?.focus()` (o BFormInput expõe `focus()` no elemento interno).

---

### Gravidade 4 — Baixo (polimento e boas práticas)

#### Problema 4.1 — Texto de ajuda de formulário bruto em vez de `BFormText`

**Arquivos afetados:**

| Arquivo | Código atual |
|---------|-------------|
| `ParametrosView.vue` | `<div id="diasInativacaoHelp" class="form-text">` (2 ocorrências) |
| `AceitarMapaModal.vue` | `<div class="form-text">` |

O BVN oferece `BFormText` para este caso. Como mencionado no Problema 3.2, o slot `#description` do `BFormGroup` é a solução
mais elegante quando o campo já usa `BFormGroup`.

**Prova — `AceitarMapaModal.vue` (linhas 26–29):**

```html
<!-- ATUAL -->
<div class="form-text">
  As observações serão registradas junto com a validação do mapa.
</div>
```

**Como deve ficar (isolado, fora de BFormGroup):**

```html
<!-- PROPOSTO -->
<BFormText>
  As observações serão registradas junto com a validação do mapa.
</BFormText>
```

---

#### Problema 4.2 — `<div class="card-title">` bruto em vez de `BCardTitle`

**Arquivos afetados:**

| Arquivo | Código atual |
|---------|-------------|
| `AtividadeItem.vue` | `<div class="card-title d-flex align-items-center ...">` |
| `CompetenciaCard.vue` | `<div class="card-title fs-5 mb-0">` dentro de `BCardHeader` |
| `AtribuicaoTemporariaView.vue` | `<h5 class="card-title mb-3">` dentro de `BCardBody` |

**Prova — `AtividadeItem.vue` (linhas 9–11):**

```html
<!-- ATUAL -->
<div class="card-title d-flex align-items-center atividade-titulo-card">
  ...
</div>
```

**Como deve ficar:**

```html
<!-- PROPOSTO (BCardTitle aceita classes e é um wrapper semântico) -->
<BCardTitle class="d-flex align-items-center atividade-titulo-card">
  ...
</BCardTitle>
```

---

#### Problema 4.3 — Aviso de Caps Lock com div bruta em vez de `BAlert`

**Arquivo afetado:** `LoginView.vue` (linhas ~100–107)

**Prova:**

```html
<!-- ATUAL -->
<div
    v-if="capsLockAtivado"
    class="text-warning small mt-1 d-flex align-items-center"
    data-testid="alert-caps-lock"
    role="alert"
>
  <i aria-hidden="true" class="bi bi-exclamation-triangle-fill me-1"/>
  Caps Lock ativado
</div>
```

**Como deve ficar:**

```html
<!-- PROPOSTO -->
<BAlert
    :model-value="capsLockAtivado"
    variant="warning"
    class="small mt-1 py-1 px-2"
    data-testid="alert-caps-lock"
>
  <i aria-hidden="true" class="bi bi-exclamation-triangle-fill me-1"/>
  Caps Lock ativado
</BAlert>
```

---

## Tabela de Priorização

| # | Problema | Arquivos | Gravidade | Esforço | Benefício |
|---|----------|----------|-----------|---------|-----------|
| 1.1 | `ModalAcaoBloco.vue` completo (Bootstrap JS + HTML bruto) | 1 | 🔴 Crítico | Alto | Muito alto |
| 2.1 | Spinners brutos (`spinner-border`) | 5 | 🟠 Alto | Baixo | Alto |
| 2.2 | Alertas brutos (`alert alert-danger`) | 3 | 🟠 Alto | Baixo | Alto |
| 2.3 | Badges brutos (`<span class="badge">`) | 4 | 🟠 Alto | Baixo | Médio |
| 2.4 | Listas brutas (`list-group`) em `ImpactoMapaModal` | 1 | 🟠 Alto | Médio | Médio |
| 3.1 | Tabelas brutas (`<table class="table">`) | 2 | 🟡 Médio | Médio | Alto |
| 3.2 | Labels e inputs brutos (sem `BFormGroup`) | 7 | 🟡 Médio | Médio | Alto |
| 3.3 | Grid bruto (`<div class="row/col-*">`) | 2 | 🟡 Médio | Baixo | Médio |
| 3.4 | Botões brutos (`<button class="btn">`) | 2 | 🟡 Médio | Baixo | Baixo |
| 3.5 | `<input class="form-control">` bruto | 1 | 🟡 Médio | Baixo | Médio |
| 4.1 | `<div class="form-text">` bruto | 2 | 🟢 Baixo | Baixo | Baixo |
| 4.2 | `card-title` bruto em vez de `BCardTitle` | 3 | 🟢 Baixo | Baixo | Baixo |
| 4.3 | Div de Caps Lock bruta | 1 | 🟢 Baixo | Baixo | Baixo |

---

## Passos de Implementação recomendados

### Fase 1 — Remover dependência do Bootstrap JS (Sprint curto, 1–2 dias)

1. **Reescrever `ModalAcaoBloco.vue`** usando `BModal`, `BTable`, `BFormCheckbox`, `BAlert`, `BButton`, `BSpinner`.
   - Remover `import { Modal } from 'bootstrap'`
   - Substituir o mecanismo de controle por `v-model`
   - Substituir todos os elementos HTML brutos

### Fase 2 — Corrigir inconsistências de alta gravidade (1–2 dias)

2. **Substituir todos os `spinner-border` brutos** por `BSpinner` nos 5 arquivos afetados.
3. **Substituir os `alert alert-danger` brutos** por `BAlert` nos 3 modais afetados.
4. **Substituir os `<span class="badge">` brutos** por `BBadge` nos 4 arquivos afetados.
5. **Substituir listas brutas** (`<ul class="list-group">`) por `BListGroup`/`BListGroupItem` em `ImpactoMapaModal.vue`.

### Fase 3 — Uniformizar formulários e layout (2–3 dias)

6. **Migrar tabela de `AdministradoresView`** para `BTable`.
7. **Migrar labels brutas** para `BFormGroup` com slot `#label` e `#description` nos arquivos identificados.
8. **Migrar `<input class="form-control">` bruto** em `AdministradoresView.vue` para `BFormInput`.
9. **Migrar grid bruto** em `AtribuicaoTemporariaView.vue` e `AtividadeItem.vue` para `BRow`/`BCol`.
10. **Migrar botões brutos** em `ArvoreUnidades.vue` e `TreeRowItem.vue` para `BButton`.

### Fase 4 — Polimento (1 dia)

11. Substituir `<div class="form-text">` por `BFormText` (ou slot `#description` de `BFormGroup`).
12. Substituir `<div class="card-title">` brutos por `BCardTitle`.
13. Migrar aviso de Caps Lock em `LoginView.vue` para `BAlert`.

---

## O que já está bem

Para equilíbrio do diagnóstico, vale destacar o que funciona exemplarmente:

- ✅ **Modals**: `BModal` usado em ~10 componentes; `ModalConfirmacao` e `ModalPadrao` são wrappers reutilizáveis bem feitos
- ✅ **Navegação**: `BNavbar`, `BNavItem`, `BBreadcrumb`, `BBreadcrumbItem` usados corretamente em `MainNavbar.vue` e `BarraNavegacao.vue`
- ✅ **Formulários complexos**: `ProcessoFormFields.vue` usa `BFormGroup` com `#label` personalizado e `BFormInvalidFeedback` — padrão ideal a ser replicado
- ✅ **Tabs**: `BTabs`/`BTab` usados corretamente em `RelatoriosView.vue`
- ✅ **Dropdown**: `BDropdown`/`BDropdownItem` usados corretamente em `CadastroView.vue`
- ✅ **Toasts**: `useToast()` integrado via store
- ✅ **Tooltips**: `v-b-tooltip` usado amplamente e de forma consistente
- ✅ **BSpinner**: Usado corretamente em `AceitarMapaModal.vue` e `SubprocessoView.vue` — é o modelo a seguir
- ✅ **BContainer/BRow/BCol**: Usados em `LoginView.vue` e `SubprocessoCards.vue` — é o modelo a seguir
- ✅ **BAlert**: Usado corretamente na maioria dos arquivos — as exceções são isoladas
- ✅ **BTable**: Usado com slots tipados em `HistoricoAnaliseModal.vue` — é o modelo a seguir

---

## Apêndice: Referências BVN

- Documentação oficial: https://bootstrap-vue-next.github.io/bootstrap-vue-next/
- Repositório: https://github.com/bootstrap-vue-next/bootstrap-vue-next
- Componentes disponíveis: `BBadge`, `BListGroup`, `BListGroupItem`, `BFormGroup`, `BFormText`, `BFormFloatingLabel`, `BSpinner`, `BTable`, `BModal`, `BAlert`, `BButton`, `BRow`, `BCol` e ~40 outros

> **Versão em uso:** `^0.43.9` — BVN ainda está em desenvolvimento ativo (pré-1.0). Verificar breaking changes ao atualizar.
