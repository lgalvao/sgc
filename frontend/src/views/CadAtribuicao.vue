<template>
  <BContainer class="mt-4">
    <PageHeader title="Criar atribuição temporária" />
    <BCard class="mb-4 mt-4">
      <BCardBody>
        <h5 class="card-title mb-3">
          {{ unidade?.sigla }} - {{ unidade?.nome }}
        </h5>
        <BForm @submit.prevent="criarAtribuicao">
          <div class="mb-3">
            <label
                class="form-label"
                for="usuario"
            >Usuário</label>
            <BFormSelect
                id="usuario"
                v-model="usuarioSelecionado"
                :options="usuarios"
                data-testid="select-usuario"
                required
                text-field="nome"
                value-field="codigo"
                :state="erroUsuario ? false : null"
            >
              <template #first>
                <BFormSelectOption
                    :value="null"
                    disabled
                >
                  Selecione um usuário
                </BFormSelectOption>
              </template>
            </BFormSelect>
            <BFormInvalidFeedback :state="erroUsuario ? false : null">
              {{ erroUsuario }}
            </BFormInvalidFeedback>
          </div>

          <div class="row">
            <div class="col-md-6 mb-3">
              <label class="form-label" for="dataInicio">Data de Início (Opcional)</label>
              <BFormInput
                  id="dataInicio"
                  v-model="dataInicio"
                  data-testid="input-data-inicio"
                  type="date"
              />
              <div class="form-text">Se não informada, será considerada a data atual.</div>
            </div>

            <div class="col-md-6 mb-3">
              <label class="form-label" for="dataTermino">Data de Término</label>
              <BFormInput
                  id="dataTermino"
                  v-model="dataTermino"
                  data-testid="input-data-termino"
                  required
                  type="date"
              />
            </div>
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
          <LoadingButton
              data-testid="cad-atribuicao__btn-criar-atribuicao"
              type="submit"
              variant="primary"
              :loading="isLoading"
              text="Criar"
              loading-text="Criando..."
          />
          <BButton
              class="ms-2"
              data-testid="btn-cancelar-atribuicao"
              type="button"
              variant="secondary"
              :disabled="isLoading"
              @click="router.push(`/unidade/${codUnidade}`)"
          >
            Cancelar
          </BButton>
        </BForm>
      </BCardBody>
    </BCard>
  </BContainer>
</template>

<script lang="ts" setup>
import {
  BButton,
  BCard,
  BCardBody,
  BContainer,
  BForm,
  BFormInput,
  BFormInvalidFeedback,
  BFormSelect,
  BFormSelectOption,
  BFormTextarea,
} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {logger} from "@/utils";
import type {Unidade, Usuario} from "@/types/tipos";
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/ui/LoadingButton.vue";
import {useFeedbackStore} from "@/stores/feedback";
import {useUnidadesStore} from "@/stores/unidades";
import {useUsuariosStore} from "@/stores/usuarios";
import {useAtribuicaoTemporariaStore} from "@/stores/atribuicoes";

const props = defineProps<{ codUnidade: number }>();

const router = useRouter();
const feedbackStore = useFeedbackStore();
const unidadesStore = useUnidadesStore();
const usuariosStore = useUsuariosStore();
const atribuicoesStore = useAtribuicaoTemporariaStore();
const codUnidade = computed(() => props.codUnidade);

const unidade = ref<Unidade | null>(null);
const usuarios = ref<Usuario[]>([]);
const usuarioSelecionado = ref<string | null>(null);
const dataInicio = ref("");
const dataTermino = ref("");
const justificativa = ref("");
const isLoading = ref(false);

const erroUsuario = ref("");

onMounted(async () => {
  try {
    await unidadesStore.buscarUnidadePorCodigo(codUnidade.value);
    unidade.value = unidadesStore.unidade as Unidade;
    if (unidade.value) {
      usuarios.value = await usuariosStore.buscarUsuariosPorUnidade(unidade.value.codigo);
    }
  } catch (error) {
    erroUsuario.value = "Falha ao carregar dados da unidade ou usuários.";
    logger.error(error);
  }
});

async function criarAtribuicao() {
  if (!unidade.value || !usuarioSelecionado.value) {
    return;
  }

  isLoading.value = true;

  try {
    await atribuicoesStore.criarAtribuicaoTemporaria(unidade.value.codigo, {
      tituloEleitoralUsuario: usuarioSelecionado.value,
      dataInicio: dataInicio.value || undefined,
      dataTermino: dataTermino.value,
      justificativa: justificativa.value,
    });

    feedbackStore.show('Sucesso', 'Atribuição criada com sucesso!', 'success');

    usuarioSelecionado.value = null;
    dataInicio.value = "";
    dataTermino.value = "";
    justificativa.value = "";
  } catch (error) {
    logger.error(error);
    feedbackStore.show('Erro', 'Falha ao criar atribuição. Tente novamente.', 'danger');
  } finally {
    isLoading.value = false;
  }
}
</script>
