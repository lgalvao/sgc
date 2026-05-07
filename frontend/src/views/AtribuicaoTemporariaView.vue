<template>
  <LayoutPadrao>
    <div class="col-lg-8 col-md-9 col-12">
      <PageHeader :title="TEXTOS.atribuicaoTemporaria.TITULO">
        <template v-if="unidade" #default>
          {{ unidade.sigla }}
        </template>
        <template v-else-if="carregandoInicial" #default>
          <BSpinner small variant="secondary"/>
        </template>
        <template #actions>
          <BButton :to="`/unidade/${props.codUnidade}`" variant="outline-secondary">
            <i class="bi bi-arrow-left me-1"/> {{ TEXTOS.comum.BOTAO_VOLTAR }}
          </BButton>
        </template>
      </PageHeader>

      <AppAlert
          v-if="notificacao"
          :dispensavel="notificacao.dispensavel ?? true"
          :mensagem="notificacao.mensagem"
          :variante="notificacao.variante"
          @dismissed="clear()"
      />

      <BAlert
          v-if="erroFormulario"
          :model-value="true"
          class="mt-3"
          dismissible
          variant="danger"
          @dismissed="erroFormulario = ''"
      >
        {{ erroFormulario }}
      </BAlert>

      <CarregamentoPagina v-if="carregandoInicial && !unidade"/>

      <BForm v-else class="mt-4" @submit.prevent="criarAtribuicao">
        <BFormGroup
            class="mb-3"
            label-for="usuario"
        >
          <template #label>
            {{ TEXTOS.atribuicaoTemporaria.LABEL_USUARIO }} <span aria-hidden="true" class="text-danger">*</span>
          </template>
          <template #description>
            {{ TEXTOS.atribuicaoTemporaria.AJUDA_PESQUISA_USUARIO }}
          </template>
          <BuscadorUsuarios
              id="usuario"
              ref="inputUsuarioRef"
              v-model:selecionado="usuarioSelecionado"
              v-model:termo="termoUsuario"
              :placeholder="TEXTOS.atribuicaoTemporaria.SELECIONE_USUARIO"
              :state="mensagemErroUsuario ? false : null"
          />
          <BFormInvalidFeedback :state="mensagemErroUsuario ? false : null">
            {{ mensagemErroUsuario }}
          </BFormInvalidFeedback>
        </BFormGroup>

        <BRow>
          <BCol class="mb-3" md="6">
            <BFormGroup label-for="dataInicio">
              <template #label>
                {{ TEXTOS.atribuicaoTemporaria.LABEL_DATA_INICIO }} <span
                  aria-hidden="true"
                  class="text-danger">*</span>
              </template>
              <InputData
                  id="dataInicio"
                  v-model="dataInicio"
                  :min="obterHojeFormatado()"
                  :state="mensagemErroDataInicio ? false : null"
                  data-testid="input-data-inicio"
                  max="2099-12-31"
              />
              <BFormInvalidFeedback :state="mensagemErroDataInicio ? false : null">
                {{ mensagemErroDataInicio }}
              </BFormInvalidFeedback>
            </BFormGroup>
          </BCol>

          <BCol class="mb-3" md="6">
            <BFormGroup label-for="dataTermino">
              <template #label>
                {{ TEXTOS.atribuicaoTemporaria.LABEL_DATA_TERMINO }} <span
                  aria-hidden="true"
                  class="text-danger">*</span>
              </template>
              <InputData
                  id="dataTermino"
                  v-model="dataTermino"
                  :min="dataInicio || obterHojeFormatado()"
                  :state="mensagemErroDataTermino ? false : null"
                  data-testid="input-data-termino"
                  max="2099-12-31"
              />
              <BFormInvalidFeedback :state="mensagemErroDataTermino ? false : null">
                {{ mensagemErroDataTermino }}
              </BFormInvalidFeedback>
            </BFormGroup>
          </BCol>
        </BRow>

        <BFormGroup
            class="mb-3"
            label-for="justificativa"
        >
          <template #label>
            {{ TEXTOS.atribuicaoTemporaria.LABEL_JUSTIFICATIVA }} <span aria-hidden="true" class="text-danger">*</span>
          </template>
          <BFormTextarea
              id="justificativa"
              v-model="justificativa"
              :state="mensagemErroJustificativa ? false : null"
              data-testid="textarea-justificativa"
          />
          <BFormInvalidFeedback :state="mensagemErroJustificativa ? false : null">
            {{ mensagemErroJustificativa }}
          </BFormInvalidFeedback>
        </BFormGroup>

        <div class="d-flex justify-content-end gap-2 mt-4">
          <BButton
              :disabled="isLoading"
              class="btn-acao-footer"
              data-testid="btn-cancelar-atribuicao"
              variant="outline-secondary"
              @click="router.push(`/unidade/${props.codUnidade}`)"
          >
            {{ TEXTOS.comum.BOTAO_CANCELAR }}
          </BButton>
          <LoadingButton
              :disabled="isLoading"
              :loading="isLoading"
              :loading-text="TEXTOS.atribuicaoTemporaria.CRIANDO"
              :text="TEXTOS.comum.BOTAO_CRIAR"
              class="btn-acao-footer"
              data-testid="cad-atribuicao__btn-criar-atribuicao"
              variant="success"
              @click="criarAtribuicao"
          />
        </div>
      </BForm>
    </div>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {
  BAlert,
  BButton,
  BCol,
  BForm,
  BFormGroup,
  BFormInvalidFeedback,
  BFormTextarea,
  BRow,
  BSpinner
} from "bootstrap-vue-next";
import {computed, onActivated, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {logger} from "@/utils";
import {normalizarErro} from "@/utils/apiError";
import type {Unidade} from "@/types/tipos";
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import InputData from "@/components/comum/InputData.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import BuscadorUsuarios from "@/components/comum/BuscadorUsuarios.vue";
import {useNotification} from "@/composables/useNotification";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {usePerfil} from "@/composables/usePerfil";
import {useUnidadeStore} from "@/stores/unidade";
import {TEXTOS} from "@/constants/textos";
import {obterHojeFormatado} from "@/utils/date";
import {criarAtribuicaoTemporaria} from "@/services/atribuicaoTemporariaService";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import {useOrganizacaoStore} from "@/stores/organizacao";

const props = defineProps<{ codUnidade: number }>();

const router = useRouter();
const {notificacao, notify, clear} = useNotification();
const {mostrarDiagnosticoOrganizacional} = usePerfil();
const unidadeStore = useUnidadeStore();
const organizacaoStore = useOrganizacaoStore();

const unidade = ref<Unidade | null>(null);
const usuarioSelecionado = ref<string | null>(null);
const termoUsuario = ref("");
const dataInicio = ref("");
const dataTermino = ref("");
const justificativa = ref("");
const isLoading = ref(false);
const carregandoInicial = ref(true);
const inputUsuarioRef = ref<InstanceType<typeof BuscadorUsuarios> | null>(null);

const erroUsuario = ref("");
const erroFormulario = ref("");
const {
  resetarValidacao,
  deveExibirErro,
  validarSubmissao,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const formularioValido = computed(() => {
  return Boolean(
      usuarioSelecionado.value
      && dataInicio.value
      && dataTermino.value
      && justificativa.value.trim()
  );
});

const mensagemErroUsuario = computed(() => {
  if (erroUsuario.value) return erroUsuario.value;
  if (deveExibirErro(!usuarioSelecionado.value)) {
    return TEXTOS.atribuicaoTemporaria.ERRO_SELECIONE_USUARIO;
  }
  return "";
});

const mensagemErroDataInicio = computed(() =>
    deveExibirErro(!dataInicio.value) ? "Informe a data de início." : "",
);

const mensagemErroDataTermino = computed(() =>
    deveExibirErro(!dataTermino.value) ? "Informe a data de término." : "",
);

const mensagemErroJustificativa = computed(() =>
    deveExibirErro(!justificativa.value.trim()) ? "Informe a justificativa." : "",
);

async function carregarDados() {
  // Se já temos a unidade correta carregada, evitamos o spinner pesado
  if (unidade.value?.codigo === props.codUnidade && unidadeStore.cacheUnidades.has(props.codUnidade)) {
    carregandoInicial.value = false;
    return;
  }

  carregandoInicial.value = true;
  try {
    unidade.value = await unidadeStore.obterUnidade(props.codUnidade);
  } catch (error) {
    erroUsuario.value = TEXTOS.atribuicaoTemporaria.ERRO_CARREGAR;
    logger.error(error);
  } finally {
    carregandoInicial.value = false;
  }
}

onMounted(carregarDados);
onActivated(carregarDados);

async function criarAtribuicao() {
  const unidadeAtual = unidade.value;
  if (!unidadeAtual) throw new Error('Invariante violada: unidade não carregada');
  erroUsuario.value = "";
  erroFormulario.value = "";

  if (!validarSubmissao(formularioValido.value)) {
    await focarPrimeiroErroInvalido();
    return;
  }

  const tituloEleitoralUsuario = usuarioSelecionado.value!;

  isLoading.value = true;

  try {
    await criarAtribuicaoTemporaria(unidadeAtual.codigo, {
      tituloEleitoralUsuario,
      dataInicio: dataInicio.value,
      dataTermino: dataTermino.value,
      justificativa: justificativa.value
    });
    await organizacaoStore.recarregarDiagnostico(mostrarDiagnosticoOrganizacional.value);

    notify(TEXTOS.atribuicaoTemporaria.SUCESSO, 'success');
    resetarFormularioAtribuicao();
  } catch (error) {
    logger.error(error);
    erroFormulario.value = normalizarErro(error).mensagem || TEXTOS.atribuicaoTemporaria.ERRO_CRIAR;
  } finally {
    isLoading.value = false;
  }
}

function resetarFormularioAtribuicao() {
  usuarioSelecionado.value = null;
  termoUsuario.value = "";

  dataInicio.value = "";
  dataTermino.value = "";
  justificativa.value = "";
  resetarValidacao();
  erroFormulario.value = "";
}


</script>

<style scoped>
.btn-acao-cabecalho,
.btn-acao-footer {
  min-width: 8rem;
  justify-content: center;
}

.usuario-resultados {
  max-height: 16rem;
}

</style>
