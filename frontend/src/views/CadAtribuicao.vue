<template>
  <BContainer class="mt-4">
    <h2>Criar atribuição temporária</h2>
    <BCard class="mb-4 mt-4">
      <BCardBody>
        <h5 class="card-title mb-3">
          {{ unidade?.sigla }} - {{ unidade?.nome }}
        </h5>
        <BForm @submit.prevent="criarAtribuicao">
          <div class="mb-3">
            <label
              class="form-label"
              for="servidor"
            >Servidor</label>
            <BFormSelect
              id="servidor"
              v-model="servidorSelecionado"
              data-testid="select-servidor"
              required
              :options="servidores"
              value-field="codigo"
              text-field="nome"
            >
              <template #first>
                <BFormSelectOption
                  :value="null"
                  disabled
                >
                  Selecione um servidor
                </BFormSelectOption>
              </template>
            </BFormSelect>
            <div
              v-if="erroServidor"
              class="text-danger small mt-1"
            >
              {{ erroServidor }}
            </div>
          </div>

          <div class="mb-3">
            <label
              class="form-label"
              for="dataTermino"
            >Data de término</label>
            <BFormInput
              id="dataTermino"
              v-model="dataTermino"
              data-testid="input-data-termino"
              required
              type="date"
            />
          </div>

          <div class="mb-3">
            <label
              class="form-label"
              for="justificativa"
            >Justificativa</label>
            <BFormTextarea
              id="justificativa"
              v-model="justificativa"
              data-testid="textarea-justificativa"
              required
            />
          </div>
          <BButton
            variant="primary"
            data-testid="btn-criar-atribuicao"
            type="submit"
          >
            Criar
          </BButton>
          <BButton
            variant="secondary"
            class="ms-2"
            data-testid="btn-cancelar-atribuicao"
            type="button"
            @click="router.push(`/unidade/${sigla}`)"
          >
            Cancelar
          </BButton>
        </BForm>

        <BAlert
          v-if="sucesso"
          variant="success"
          class="mt-3"
          :model-value="true"
          :fade="false"
        >
          Atribuição criada!
        </BAlert>
        <BAlert
          v-if="erroApi"
          variant="danger"
          class="mt-3"
          :model-value="true"
          :fade="false"
        >
          {{ erroApi }}
        </BAlert>
      </BCardBody>
    </BCard>
  </BContainer>
</template>

<script lang="ts" setup>
import {
  BAlert,
  BButton,
  BCard,
  BCardBody,
  BContainer,
  BForm,
  BFormInput,
  BFormSelect,
  BFormSelectOption,
  BFormTextarea,
} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {criarAtribuicaoTemporaria} from "@/services/atribuicaoTemporariaService";
import {buscarUnidadePorSigla} from "@/services/unidadesService";
import {buscarUsuariosPorUnidade} from "@/services/usuarioService";
import type {Unidade, Usuario} from "@/types/tipos";

const props = defineProps<{ sigla: string }>();

const router = useRouter();
const sigla = computed(() => props.sigla);

const unidade = ref<Unidade | null>(null);
const servidores = ref<Usuario[]>([]);
const servidorSelecionado = ref<string | null>(null);
const dataTermino = ref("");
const justificativa = ref("");

const sucesso = ref(false);
const erroServidor = ref("");
const erroApi = ref("");

onMounted(async () => {
  try {
    unidade.value = await buscarUnidadePorSigla(sigla.value);
    if (unidade.value) {
      servidores.value = await buscarUsuariosPorUnidade(unidade.value.codigo);
    }
  } catch (error) {
    erroServidor.value = "Falha ao carregar dados da unidade ou servidores.";
    console.error(error);
  }
});

async function criarAtribuicao() {
  if (!unidade.value || !servidorSelecionado.value) {
    return;
  }

  erroApi.value = "";
  sucesso.value = false;

  try {
    await criarAtribuicaoTemporaria(unidade.value.codigo, {
      tituloEleitoralServidor: servidorSelecionado.value,
      dataTermino: dataTermino.value,
      justificativa: justificativa.value,
    });
    sucesso.value = true;
    // Reset form
    servidorSelecionado.value = null;
    dataTermino.value = "";
    justificativa.value = "";
  } catch (error) {
    erroApi.value = "Falha ao criar atribuição. Tente novamente.";
    console.error(error);
  }
}
</script>