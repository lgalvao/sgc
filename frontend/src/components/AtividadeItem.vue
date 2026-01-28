<template>
  <BCard 
    class="mb-3 atividade-card" 
    :class="{'atividade-com-erro': !!erroValidacao}"
    no-body
  >
    <BCardBody class="py-2 position-relative">
      <div
          class="card-title d-flex align-items-center atividade-titulo-card"
          :class="{'atividade-hover-row': !emEdicao}"
      >
        <InlineEditor
          :model-value="atividade.descricao"
          :can-edit="podeEditar"
          aria-label="Editar atividade"
          test-id-input="inp-editar-atividade"
          test-id-save="btn-salvar-edicao-atividade"
          test-id-cancel="btn-cancelar-edicao-atividade"
          test-id-edit="btn-editar-atividade"
          @update:model-value="(val) => $emit('atualizar-atividade', val)"
          @edit-start="emEdicao = true"
          @edit-end="emEdicao = false"
        >
          <strong
              class="atividade-descricao"
              data-testid="cad-atividades__txt-atividade-descricao"
          >{{ atividade?.descricao }}</strong>

          <template #extra-actions>
            <BButton
                class="ms-1"
                data-testid="btn-remover-atividade"
                size="sm"
                title="Remover"
                :aria-label="'Remover atividade: ' + atividade.descricao"
                variant="outline-danger"
                @click="$emit('remover-atividade')"
            >
              <i aria-hidden="true" class="bi bi-trash" />
            </BButton>
          </template>
        </InlineEditor>
      </div>

      <!-- Mensagem de erro inline -->
      <BAlert
          v-if="erroValidacao"
          :model-value="true"
          variant="danger"
          class="mt-2 py-2 mb-0"
          data-testid="atividade-erro-validacao"
      >
        <i aria-hidden="true" class="bi bi-exclamation-circle-fill me-2"/>
        {{ erroValidacao }}
      </BAlert>

      <div class="mt-3 ms-3">
        <!-- Label com asterisco para conhecimentos obrigatórios -->
        <div class="mb-2 text-muted small">
          <strong>Conhecimentos <span class="text-danger">*</span></strong>
          <span class="ms-1">(adicione pelo menos um)</span>
        </div>

        <div
            v-for="conhecimento in atividade.conhecimentos"
            :key="conhecimento.codigo"
            class="d-flex align-items-center mb-2 group-conhecimento position-relative conhecimento-hover-row"
        >
          <InlineEditor
            :model-value="conhecimento.descricao"
            :can-edit="podeEditar"
            size="sm"
            aria-label="Editar conhecimento"
            test-id-input="inp-editar-conhecimento"
            test-id-save="btn-salvar-edicao-conhecimento"
            test-id-cancel="btn-cancelar-edicao-conhecimento"
            test-id-edit="btn-editar-conhecimento"
            @update:model-value="(val) => $emit('atualizar-conhecimento', conhecimento.codigo, val)"
          >
            <span data-testid="cad-atividades__txt-conhecimento-descricao">{{ conhecimento?.descricao }}</span>

            <template #extra-actions>
              <BButton
                  class="ms-1"
                  data-testid="btn-remover-conhecimento"
                  size="sm"
                  title="Remover"
                  :aria-label="'Remover conhecimento: ' + conhecimento.descricao"
                  variant="outline-danger"
                  @click="$emit('remover-conhecimento', conhecimento.codigo)"
              >
                <i aria-hidden="true" class="bi bi-trash" />
              </BButton>
            </template>
          </InlineEditor>
        </div>
        <BForm
            v-if="podeEditar"
            class="row g-2 align-items-center"
            data-testid="form-novo-conhecimento"
            @submit.prevent="adicionarConhecimento"
        >
          <BCol>
            <BFormInput
                v-model="novoConhecimento"
                aria-label="Novo conhecimento"
                :class="{ 'border-danger': !!erroValidacao }"
                data-testid="inp-novo-conhecimento"
                placeholder="Novo conhecimento"
                size="sm"
                type="text"
            />
          </BCol>
          <BCol cols="auto">
            <BButton
                aria-label="Adicionar conhecimento"
                data-testid="btn-adicionar-conhecimento"
                size="sm"
                title="Adicionar Conhecimento"
                type="submit"
                variant="outline-secondary"
            >
              <i
                  aria-hidden="true"
                  class="bi bi-plus-lg"
              />
            </BButton>
          </BCol>
        </BForm>
      </div>
    </BCardBody>
  </BCard>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BCard, BCardBody, BCol, BForm, BFormInput} from "bootstrap-vue-next";
import {ref} from "vue";
import type {Atividade} from "@/types/tipos";
import InlineEditor from "@/components/common/InlineEditor.vue";

interface Props {
  atividade: Atividade;
  podeEditar: boolean;
  erroValidacao?: string;
}

defineProps<Props>();

const emit = defineEmits<{
  (e: "atualizar-atividade", novaDescricao: string): void;
  (e: "remover-atividade"): void;
  (e: "adicionar-conhecimento", descricao: string): void;
  (e: "atualizar-conhecimento", conhecimentoCodigo: number, novaDescricao: string): void;
  (e: "remover-conhecimento", conhecimentoCodigo: number): void;
}>();

// Estado de Edição da Atividade
const emEdicao = ref(false);

// Novo Conhecimento
const novoConhecimento = ref("");

function adicionarConhecimento() {
  const descricao = novoConhecimento.value.trim();
  if (descricao) {
    emit("adicionar-conhecimento", descricao);
    novoConhecimento.value = "";
  }
}
</script>

<style scoped>
.atividade-edicao-input {
  flex-grow: 1;
  min-width: 0;
}

.atividade-card {
  transition: box-shadow 0.2s;
}

.atividade-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.07);
}

.atividade-com-erro {
  border-left: 4px solid var(--bs-danger) !important;
  box-shadow: 0 2px 8px 0 rgba(var(--bs-danger-rgb), 0.1) !important;
}

.botoes-acao-atividade {
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
  top: 0.5rem;
  right: 0.5rem;
  z-index: 10;
}

.botoes-acao {
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
  position: relative;
  z-index: 1;
}

/* Show buttons when card is hovered or has focus */
.atividade-card:hover .botoes-acao-atividade,
.atividade-card:focus-within .botoes-acao-atividade,
.conhecimento-hover-row:hover .botoes-acao,
.conhecimento-hover-row:focus-within .botoes-acao {
  opacity: 1;
  pointer-events: auto;
}

.botao-acao {
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  font-size: 1.1rem;
  border-width: 2px;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
  margin-left: 0;
  margin-right: 0;
  position: relative;
  z-index: 2;
}

.botao-acao:focus,
.botao-acao:hover {
  background: var(--bs-primary-bg-subtle);
  box-shadow: 0 0 0 2px var(--bs-primary);
}

.fade-group {
  transition: opacity 0.2s;
}

.atividade-descricao {
  word-break: break-word;
  max-width: 100%;
  display: inline-block;
}

.conhecimento-hover-row:hover span {
  font-weight: bold;
}

.atividade-hover-row:hover .atividade-descricao {
  font-weight: bold;
}

.atividade-titulo-card {
  background: var(--bs-light);
  border-bottom: 1px solid var(--bs-border-color);
  padding: 0.5rem 0.75rem;
  margin-left: -0.75rem;
  margin-right: -0.75rem;
  margin-top: -0.5rem;
  border-top-left-radius: 0.375rem;
  border-top-right-radius: 0.375rem;
}

</style>
