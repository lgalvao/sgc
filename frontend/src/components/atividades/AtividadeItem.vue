<template>
  <BCard
      :class="{'atividade-com-erro': !!erroValidacao}"
      class="mb-3 atividade-card"
      data-testid="cad-atividades__card-atividade"
      no-body
  >
    <BCardBody class="py-2 position-relative">
      <BCardTitle
          :class="{'atividade-hover-row': !emEdicao}"
          :data-testid="!emEdicao ? 'cad-atividades__hover-row' : undefined"
          class="d-flex align-items-center atividade-titulo-card"
      >
        <InlineEditor
            :can-edit="podeEditar"
            :edit-enabled="habilitarEdicao"
            :model-value="atividade.descricao"
            aria-label="Editar atividade"
            mensagem-erro-obrigatoria="Informe a atividade."
            test-id-cancelar="btn-cancelar-edicao-atividade"
            test-id-editar="btn-editar-atividade"
            test-id-input="inp-editar-atividade"
            test-id-salvar="btn-salvar-edicao-atividade"
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
                :aria-label="'Remover atividade: ' + atividade.descricao"
                :disabled="!habilitarEdicao"
                class="btn-compacto"
                data-testid="btn-remover-atividade"
                size="sm"
                title="Remover"
                variant="outline-secondary"
                @click="$emit('remover-atividade')"
            >
              <i aria-hidden="true" class="bi bi-trash"/>
            </BButton>
          </template>
        </InlineEditor>
      </BCardTitle>

      <!-- Mensagem de erro inline -->
      <BAlert
          v-if="erroValidacao"
          :model-value="true"
          class="mt-2 py-2 mb-0"
          data-testid="atividade-erro-validacao"
          variant="danger"
      >
        <i aria-hidden="true" class="bi bi-exclamation-circle-fill me-2"/>
        {{ erroValidacao }}
      </BAlert>

      <div class="mt-3 ms-3">
        <BForm
            v-if="podeEditar"
            class="mb-3"
            data-testid="form-novo-conhecimento"
            @submit.prevent="adicionarConhecimento"
        >
          <BRow class="g-2 align-items-center">
            <BCol>
              <BFormInput
                  v-model="novoConhecimento"
                  :disabled="!habilitarEdicao"
                  :state="mensagemErroNovoConhecimento ? false : null"
                  aria-label="Novo conhecimento"
                  data-testid="inp-novo-conhecimento"
                  placeholder="Novo conhecimento"
                  size="sm"
                  type="text"
              />
              <BFormInvalidFeedback :state="mensagemErroNovoConhecimento ? false : null">
                {{ mensagemErroNovoConhecimento }}
              </BFormInvalidFeedback>
            </BCol>
            <BCol cols="auto">
              <BButton
                  :disabled="!habilitarEdicao"
                  aria-label="Adicionar conhecimento"
                  class="btn-compacto"
                  data-testid="btn-adicionar-conhecimento"
                  size="sm"
                  title="Adicionar conhecimento"
                  type="submit"
                  variant="outline-secondary"
              >
                <i
                    aria-hidden="true"
                    class="bi bi-plus-lg"
                />
              </BButton>
            </BCol>
          </BRow>
        </BForm>

        <div
            v-for="conhecimento in atividade.conhecimentos"
            :key="conhecimento.codigo"
            class="d-flex align-items-center mb-2 group-conhecimento position-relative conhecimento-hover-row"
            data-testid="cad-atividades__item-conhecimento"
        >
          <InlineEditor
              :can-edit="podeEditar"
              :edit-enabled="habilitarEdicao"
              :model-value="conhecimento.descricao"
              aria-label="Editar conhecimento"
              mensagem-erro-obrigatoria="Informe o conhecimento."
              size="sm"
              test-id-cancelar="btn-cancelar-edicao-conhecimento"
              test-id-editar="btn-editar-conhecimento"
              test-id-input="inp-editar-conhecimento"
              test-id-salvar="btn-salvar-edicao-conhecimento"
              @update:model-value="(val) => $emit('atualizar-conhecimento', conhecimento.codigo, val)"
          >
            <span data-testid="cad-atividades__txt-conhecimento-descricao">{{ conhecimento?.descricao }}</span>

            <template #extra-actions>
              <BButton
                  :aria-label="'Remover conhecimento: ' + conhecimento.descricao"
                  :disabled="!habilitarEdicao"
                  class="btn-compacto"
                  data-testid="btn-remover-conhecimento"
                  size="sm"
                  title="Remover"
                  variant="outline-secondary"
                  @click="$emit('remover-conhecimento', conhecimento.codigo)"
              >
                <i aria-hidden="true" class="bi bi-trash"/>
              </BButton>
            </template>
          </InlineEditor>
        </div>
      </div>
    </BCardBody>
  </BCard>
</template>

<script lang="ts" setup>
import {
  BAlert,
  BButton,
  BCard,
  BCardBody,
  BCardTitle,
  BCol,
  BForm,
  BFormInput,
  BFormInvalidFeedback,
  BRow
} from "bootstrap-vue-next";
import {computed, ref, watch} from "vue";
import type {Atividade} from "@/types/tipos";
import InlineEditor from "@/components/comum/InlineEditor.vue";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";

interface Props {
  atividade: Atividade;
  podeEditar: boolean;
  habilitarEdicao?: boolean;
  erroValidacao?: string;
}

const props = withDefaults(defineProps<Props>(), {
  habilitarEdicao: true,
  erroValidacao: "",
});

const emit = defineEmits<{
  (e: "atualizar-atividade", novaDescricao: string): void;
  (e: "remover-atividade"): void;
  (e: "adicionar-conhecimento", descricao: string): void;
  (e: "atualizar-conhecimento", conhecimentoCodigo: number, novaDescricao: string): void;
  (e: "remover-conhecimento", conhecimentoCodigo: number): void;
}>();

const emEdicao = ref(false);

const novoConhecimento = ref("");
const {
  validarSubmissao,
  resetarValidacao,
  deveExibirErro
} = useValidacaoFormulario();

const mensagemErroNovoConhecimento = computed(() => {
  return deveExibirErro(!novoConhecimento.value.trim()) ? "Informe o conhecimento." : "";
});

function adicionarConhecimento() {
  if (!props.habilitarEdicao) {
    return;
  }

  if (!validarSubmissao(!!novoConhecimento.value.trim())) {
    return;
  }

  const descricao = novoConhecimento.value.trim();
  emit("adicionar-conhecimento", descricao);
  novoConhecimento.value = "";
  resetarValidacao();
}

watch(novoConhecimento, (valorAtual, valorAnterior) => {
  if (valorAtual !== valorAnterior && valorAtual.trim()) {
    resetarValidacao();
  }
});
</script>

<style scoped>
.atividade-card {
  transition: box-shadow 0.2s;
}

.atividade-card:hover {
  box-shadow: 0 4px 15px 0 rgba(0, 0, 0, 0.1);
}

[data-bs-theme="dark"] .atividade-card:hover {
  box-shadow: 0 4px 15px 0 rgba(0, 0, 0, 0.5);
}

.atividade-com-erro {
  border-left: 4px solid var(--bs-danger) !important;
  box-shadow: 0 2px 8px 0 rgba(var(--bs-danger-rgb), 0.1) !important;
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

.atividade-descricao {
  overflow-wrap: anywhere;
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
  background: var(--bs-tertiary-bg);
  border-bottom: 1px solid var(--bs-border-color);
  padding: 0.5rem 0.75rem;
  margin-left: -0.75rem;
  margin-right: -0.75rem;
  margin-top: -0.5rem;
  border-top-left-radius: 0.375rem;
  border-top-right-radius: 0.375rem;
}

.group-conhecimento span {
  overflow-wrap: anywhere;
  word-break: break-word;
}

.btn-compacto {
  padding: 0.2rem 0.35rem;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-compacto i {
  font-size: 0.875rem;
}
</style>
