<template>
  <BCard 
    class="mb-3 atividade-card" 
    :class="{'atividade-com-erro': !!erroValidacao}"
    no-body
  >
    <BCardBody class="py-2 position-relative">
      <div
          class="card-title d-flex align-items-center atividade-edicao-row atividade-titulo-card"
          :class="{'atividade-hover-row': !emEdicao}"
      >
        <template v-if="emEdicao">
          <BFormInput
              v-model="atividadeEditada"
              aria-label="Editar atividade"
              class="me-2 atividade-edicao-input"
              data-testid="inp-editar-atividade"
          />
          <BButton
              class="me-1 botao-acao"
              data-testid="btn-salvar-edicao-atividade"
              size="sm"
              title="Salvar"
              variant="outline-success"
              @click="salvarEdicaoAtividade"
          >
            <i class="bi bi-save"/>
          </BButton>
          <BButton
              class="botao-acao"
              data-testid="btn-cancelar-edicao-atividade"
              size="sm"
              title="Cancelar"
              variant="outline-secondary"
              @click="cancelarEdicaoAtividade"
          >
            <i class="bi bi-x"/>
          </BButton>
        </template>

        <template v-else>
          <strong
              class="atividade-descricao"
              data-testid="cad-atividades__txt-atividade-descricao"
          >{{ atividade?.descricao }}</strong>
        </template>
      </div>
      <div
          v-if="podeEditar && !emEdicao"
          class="botoes-acao-atividade position-absolute d-flex"
      >
        <BButton
            class="botao-acao"
            data-testid="btn-editar-atividade"
            size="sm"
            title="Editar"
            variant="outline-primary"
            @click="iniciarEdicaoAtividade"
        >
          <i class="bi bi-pencil"/>
        </BButton>
        <BButton
            class="botao-acao ms-1"
            data-testid="btn-remover-atividade"
            size="sm"
            title="Remover"
            variant="outline-danger"
            @click="$emit('remover-atividade')"
        >
          <i class="bi bi-trash"/>
        </BButton>
      </div>

      <!-- Mensagem de erro inline -->
      <div 
        v-if="erroValidacao" 
        class="alert alert-danger d-flex align-items-center mt-2 mb-0 py-2"
        role="alert"
        data-testid="atividade-erro-validacao"
      >
        <i class="bi bi-exclamation-triangle-fill me-2"></i>
        <span>{{ erroValidacao }}</span>
      </div>

      <div class="mt-3 ms-3">
        <!-- Label com asterisco para conhecimentos obrigatórios -->
        <div class="mb-2 text-muted small">
          <strong>Conhecimentos <span class="text-danger">*</span></strong>
          <span class="ms-1">(adicione pelo menos um)</span>
        </div>

        <div
            v-for="conhecimento in atividade.conhecimentos"
            :key="conhecimento.id"
            class="d-flex align-items-center mb-2 group-conhecimento position-relative conhecimento-hover-row"
        >
          <template v-if="conhecimentoEmEdicao === conhecimento.id">
            <BFormInput
                v-model="conhecimentoEditadoDescricao"
                aria-label="Editar conhecimento"
                class="me-2"
                data-testid="inp-editar-conhecimento"
                size="sm"
            />
            <BButton
                class="me-1 botao-acao"
                data-testid="btn-salvar-edicao-conhecimento"
                size="sm"
                title="Salvar"
                variant="outline-success"
                @click="salvarEdicaoConhecimento(conhecimento.id)"
            >
              <i class="bi bi-save"/>
            </BButton>
            <BButton
                class="botao-acao"
                data-testid="btn-cancelar-edicao-conhecimento"
                size="sm"
                title="Cancelar"
                variant="outline-secondary"
                @click="cancelarEdicaoConhecimento"
            >
              <i class="bi bi-x"/>
            </BButton>
          </template>
          <template v-else>
            <span data-testid="cad-atividades__txt-conhecimento-descricao">{{ conhecimento?.descricao }}</span>
            <div
                v-if="podeEditar"
                class="d-inline-flex align-items-center gap-1 ms-3 botoes-acao fade-group"
            >
              <BButton
                  class="botao-acao"
                  data-testid="btn-editar-conhecimento"
                  size="sm"
                  title="Editar"
                  variant="outline-primary"
                  @click="iniciarEdicaoConhecimento(conhecimento)"
              >
                <i class="bi bi-pencil"/>
              </BButton>
              <BButton
                  class="botao-acao"
                  data-testid="btn-remover-conhecimento"
                  size="sm"
                  title="Remover"
                  variant="outline-danger"
                  @click="$emit('remover-conhecimento', conhecimento.id)"
              >
                <i class="bi bi-trash"/>
              </BButton>
            </div>
          </template>
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
                data-testid="inp-novo-conhecimento"
                placeholder="Novo conhecimento"
                size="sm"
                type="text"
            />
          </BCol>
          <BCol cols="auto">
            <BButton
                data-testid="btn-adicionar-conhecimento"
                size="sm"
                title="Adicionar Conhecimento"
                type="submit"
                variant="outline-secondary"
            >
              <i class="bi bi-save"/>
            </BButton>
          </BCol>
        </BForm>
      </div>
    </BCardBody>
  </BCard>
</template>

<script lang="ts" setup>
import {BButton, BCard, BCardBody, BCol, BForm, BFormInput} from "bootstrap-vue-next";
import {ref} from "vue";
import type {Atividade, Conhecimento} from "@/types/tipos";

interface Props {
  atividade: Atividade;
  podeEditar: boolean;
  erroValidacao?: string;
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: "atualizar-atividade", novaDescricao: string): void;
  (e: "remover-atividade"): void;
  (e: "adicionar-conhecimento", descricao: string): void;
  (e: "atualizar-conhecimento", idConhecimento: number, novaDescricao: string): void;
  (e: "remover-conhecimento", idConhecimento: number): void;
}>();

// Estado de Edição da Atividade
const emEdicao = ref(false);
const atividadeEditada = ref("");

function iniciarEdicaoAtividade() {
  atividadeEditada.value = props.atividade.descricao;
  emEdicao.value = true;
}

function cancelarEdicaoAtividade() {
  emEdicao.value = false;
  atividadeEditada.value = "";
}

function salvarEdicaoAtividade() {
  const descricao = atividadeEditada.value.trim();
  if (descricao && descricao !== props.atividade.descricao) {
    emit("atualizar-atividade", descricao);
  }
  cancelarEdicaoAtividade();
}

// Estado de Edição de Conhecimento
const conhecimentoEmEdicao = ref<number | null>(null);
const conhecimentoEditadoDescricao = ref("");

function iniciarEdicaoConhecimento(conhecimento: Conhecimento) {
  conhecimentoEmEdicao.value = conhecimento.id;
  conhecimentoEditadoDescricao.value = conhecimento.descricao;
}

function cancelarEdicaoConhecimento() {
  conhecimentoEmEdicao.value = null;
  conhecimentoEditadoDescricao.value = "";
}

function salvarEdicaoConhecimento(id: number) {
  const descricao = conhecimentoEditadoDescricao.value.trim();
  if (descricao) {
    emit("atualizar-conhecimento", id, descricao);
  }
  cancelarEdicaoConhecimento();
}

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
  border: 2px solid var(--bs-danger) !important;
  box-shadow: 0 0 0 0.2rem rgba(var(--bs-danger-rgb), 0.25) !important;
}

.atividade-com-erro .atividade-titulo-card {
  background-color: var(--bs-danger-bg-subtle);
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

/* Show buttons when card is hovered */
.atividade-card:hover .botoes-acao-atividade,
.conhecimento-hover-row:hover .botoes-acao {
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
