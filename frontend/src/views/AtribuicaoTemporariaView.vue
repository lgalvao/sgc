<template>
  <LayoutPadrao>
    <PageHeader title="Criar atribuição temporária"/>
    <AppAlert
        v-if="notificacao"
        :dismissible="notificacao.dismissible ?? true"
        :message="notificacao.message"
        :variant="notificacao.variant"
        @dismissed="clear()"
    />
    <BCard class="mb-4 mt-4">
      <BCardBody>
        <BCardTitle class="mb-3">
          {{ unidade?.sigla }} - {{ unidade?.nome }}
        </BCardTitle>
        <BForm @submit.prevent="criarAtribuicao">
          <BFormGroup
              label="Usuário"
              label-for="usuario"
              class="mb-3"
          >
            <BFormSelect
                id="usuario"
                v-model="usuarioSelecionado"
                :options="usuarios"
                :state="erroUsuario ? false : null"
                data-testid="select-usuario"
                required
                text-field="nome"
                value-field="tituloEleitoral"
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
          </BFormGroup>

          <BRow>
            <BCol md="6" class="mb-3">
              <BFormGroup label="Data de Início" label-for="dataInicio">
                <InputData
                    id="dataInicio"
                    v-model="dataInicio"
                    data-testid="input-data-inicio"
                    max="2099-12-31"
                    min="2000-01-01"
                    required
                />
              </BFormGroup>
            </BCol>

            <BCol md="6" class="mb-3">
              <BFormGroup label="Data de Término" label-for="dataTermino">
                <InputData
                    id="dataTermino"
                    v-model="dataTermino"
                    data-testid="input-data-termino"
                    max="2099-12-31"
                    min="2000-01-01"
                    required
                />
              </BFormGroup>
            </BCol>
          </BRow>

          <BFormGroup
              label="Justificativa"
              label-for="justificativa"
              class="mb-3"
          >
            <BFormTextarea
                id="justificativa"
                v-model="justificativa"
                data-testid="textarea-justificativa"
                required
            />
          </BFormGroup>
          <LoadingButton
              :loading="isLoading"
              data-testid="cad-atribuicao__btn-criar-atribuicao"
              loading-text="Criando..."
              text="Criar"
              type="submit"
              variant="primary"
          />
          <BButton
              :disabled="isLoading"
              class="ms-2"
              data-testid="btn-cancelar-atribuicao"
              type="button"
              variant="secondary"
              @click="router.push(`/unidade/${codUnidade}`)"
          >
            Cancelar
          </BButton>
        </BForm>
      </BCardBody>
    </BCard>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {
  BButton,
  BCard,
  BCardBody,
  BCardTitle,
  BCol,
  BForm,
  BFormGroup,
  BFormInvalidFeedback,
  BFormSelect,
  BFormSelectOption,
  BFormTextarea,
  BRow
} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {logger} from "@/utils";
import type {Unidade, Usuario} from "@/types/tipos";
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import InputData from "@/components/comum/InputData.vue";
import {useNotification} from "@/composables/useNotification";
import {buscarUnidadePorCodigo as buscarUnidadeServico} from "@/services/unidadeService";
import {buscarUsuariosPorUnidade} from "@/services/usuarioService";
import {criarAtribuicaoTemporaria} from "@/services/atribuicaoTemporariaService";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";

const props = defineProps<{ codUnidade: number }>();

const router = useRouter();
const {notificacao, notify, clear} = useNotification();
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
    const response = await buscarUnidadeServico(codUnidade.value);
    unidade.value = response as Unidade;
    if (unidade.value) {
      usuarios.value = await buscarUsuariosPorUnidade(unidade.value.codigo);
    }
  } catch (error) {
    erroUsuario.value = "Falha ao carregar dados da unidade ou usuários.";
    logger.error(error);
  }
});

async function criarAtribuicao() {
  const unidadeAtual = unidade.value;
  if (!unidadeAtual) throw new Error('Invariante violada: unidade não carregada');
  if (!usuarioSelecionado.value) {
    erroUsuario.value = "Selecione um usuário para criar a atribuição.";
    notify('Selecione um usuário para criar a atribuição.', 'danger');
    return;
  }
  if (!dataInicio.value || !dataTermino.value || !justificativa.value.trim()) {
    notify('Preencha data de início, data de término e justificativa.', 'danger');
    return;
  }
  erroUsuario.value = "";

  isLoading.value = true;

  try {
    await criarAtribuicaoTemporaria(unidadeAtual.codigo, {
      tituloEleitoralUsuario: usuarioSelecionado.value,
      dataInicio: dataInicio.value,
      dataTermino: dataTermino.value,
      justificativa: justificativa.value
    });

    notify('Atribuição criada', 'success');

    usuarioSelecionado.value = null;
    dataInicio.value = "";
    dataTermino.value = "";
    justificativa.value = "";
  } catch (error) {
    logger.error(error);
    notify('Falha ao criar atribuição. Tente novamente.', 'danger');
  } finally {
    isLoading.value = false;
  }
}

defineExpose({
  router,
  codUnidade,
  unidade,
  usuarios,
  usuarioSelecionado,
  dataInicio,
  dataTermino,
  justificativa,
  isLoading,
  erroUsuario,
  criarAtribuicao
});
</script>
