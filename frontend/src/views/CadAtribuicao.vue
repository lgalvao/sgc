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
                :options="servidores"
                data-testid="select-servidor"
                required
                text-field="nome"
                value-field="codigo"
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
              data-testid="cad-atribuicao__btn-criar-atribuicao"
              type="submit"
              variant="primary"
          >
            Criar
          </BButton>
          <BButton
              class="ms-2"
              data-testid="btn-cancelar-atribuicao"
              type="button"
              variant="secondary"
              @click="router.push(`/unidade/${codUnidade}`)"
          >
            Cancelar
          </BButton>
        </BForm>

        <BAlert
            v-if="sucesso"
            :fade="false"
            :model-value="true"
            class="mt-3"
            variant="success"
        >
          Atribuição criada!
        </BAlert>
        <BAlert
            v-if="erroApi"
            :fade="false"
            :model-value="true"
            class="mt-3"
            variant="danger"
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
import {buscarUnidadePorCodigo} from "@/services/unidadesService";
import {buscarUsuariosPorUnidade} from "@/services/usuarioService";
import type {Unidade, Usuario} from "@/types/tipos";

const props = defineProps<{ codUnidade: number }>();

const router = useRouter();
const codUnidade = computed(() => props.codUnidade);

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
    unidade.value = await buscarUnidadePorCodigo(codUnidade.value);
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
