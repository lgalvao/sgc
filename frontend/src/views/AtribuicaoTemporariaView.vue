<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregandoInicial && !unidade"/>

    <div v-else class="col-lg-8 col-md-9 col-12">
      <PageHeader
          :title="tituloPagina"
          actions-test-id="atribuicao-view__acoes"
          title-test-id="atribuicao-view__titulo"
      >
        <template v-if="unidade" #default>
          <span data-testid="atribuicao-view__sigla">{{ unidade.sigla }}</span>
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

      <BForm class="mt-4" @submit.prevent="salvarAtribuicao">
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
                  :min="dataMinimaTermino"
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
          <EditorTextoRico
              id="justificativa"
              v-model="justificativa"
              data-testid="textarea-justificativa"
              minimo-altura="10rem"
              rotulo="Justificativa"
          />
          <BFormInvalidFeedback :state="mensagemErroJustificativa ? false : null">
            {{ mensagemErroJustificativa }}
          </BFormInvalidFeedback>
        </BFormGroup>

        <div class="d-flex justify-content-end gap-2 mt-4">
          <BButton
              v-if="modoEdicao"
              :disabled="isLoading"
              class="btn-acao-footer"
              data-testid="btn-remover-atribuicao"
              variant="outline-danger"
              @click="mostrarModalRemocao = true"
          >
            {{ TEXTOS.atribuicaoTemporaria.BOTAO_REMOVER }}
          </BButton>
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
              :loading-text="textoBotaoSalvando"
              :text="textoBotaoSalvar"
              class="btn-acao-footer"
              data-testid="cad-atribuicao__btn-salvar-atribuicao"
              variant="success"
              @click="salvarAtribuicao"
          />
        </div>
      </BForm>

      <ModalConfirmacao
          v-model="mostrarModalRemocao"
          :loading="isLoading"
          :auto-close="false"
          :ok-title="TEXTOS.comum.BOTAO_REMOVER"
          :titulo="TEXTOS.atribuicaoTemporaria.MODAL_REMOVER_TITULO"
          test-id-confirmar="btn-confirmar-remover-atribuicao"
          variant="danger"
          @confirmar="removerAtribuicao"
      >
        <p class="mb-0">{{ TEXTOS.atribuicaoTemporaria.MODAL_REMOVER_TEXTO }}</p>
      </ModalConfirmacao>
    </div>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BCol, BForm, BFormGroup, BFormInvalidFeedback, BRow} from "bootstrap-vue-next";
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
import EditorTextoRico from "@/components/comum/EditorTextoRico.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import {useNotification} from "@/composables/useNotification";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {usePerfil} from "@/composables/usePerfil";
import {useUnidadeStore} from "@/stores/unidade";
import {TEXTOS} from "@/constants/textos";
import {obterHojeFormatado} from "@/utils/date";
import {
  type AtribuicaoTemporaria,
  atualizarAtribuicaoTemporaria,
  buscarAtribuicoesTemporariasPorUnidade,
  criarAtribuicaoTemporaria,
  removerAtribuicaoTemporaria
} from "@/services/atribuicaoTemporariaService";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import {useOrganizacaoStore} from "@/stores/organizacao";

const props = defineProps<{ codUnidade: number }>();

const router = useRouter();
const {notificacao, notify, clear} = useNotification();
const {mostrarDiagnosticoOrganizacional} = usePerfil();
const unidadeStore = useUnidadeStore();
const organizacaoStore = useOrganizacaoStore();

const unidade = ref<Unidade | null>(null);
const atribuicoes = ref<AtribuicaoTemporaria[]>([]);
const usuarioSelecionado = ref<string | null>(null);
const termoUsuario = ref("");
const dataInicio = ref("");
const dataTermino = ref("");
const justificativa = ref("");
const isLoading = ref(false);
const carregandoInicial = ref(true);
const inputUsuarioRef = ref<InstanceType<typeof BuscadorUsuarios> | null>(null);
const carregamentoInicialConcluido = ref(false);
const mostrarModalRemocao = ref(false);

const erroUsuario = ref("");
const erroFormulario = ref("");
const {
  resetarValidacao,
  deveExibirErro,
  validarSubmissao,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const atribuicaoVigente = computed(() => {
  const agora = new Date();
  return atribuicoes.value.find((atribuicao) => {
    const dataInicioAtribuicao = new Date(atribuicao.dataInicio);
    const dataTerminoAtribuicao = new Date(atribuicao.dataTermino);
    return dataInicioAtribuicao <= agora && dataTerminoAtribuicao >= agora;
  }) ?? null;
});

const modoEdicao = computed(() => Boolean(atribuicaoVigente.value));
const tituloPagina = computed(() => TEXTOS.atribuicaoTemporaria.TITULO);
const textoBotaoSalvar = computed(() =>
    modoEdicao.value ? TEXTOS.comum.BOTAO_SALVAR : TEXTOS.comum.BOTAO_CRIAR
);
const textoBotaoSalvando = computed(() =>
    modoEdicao.value ? TEXTOS.atribuicaoTemporaria.SALVANDO : TEXTOS.atribuicaoTemporaria.CRIANDO
);
const dataMinimaTermino = computed(() => dataInicio.value.length > 0 ? dataInicio.value : obterHojeFormatado());

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

function resetarFormularioAtribuicao() {
  usuarioSelecionado.value = null;
  termoUsuario.value = "";
  dataInicio.value = "";
  dataTermino.value = "";
  justificativa.value = "";
  resetarValidacao();
}

function preencherFormularioComAtribuicaoVigente() {
  const atribuicaoAtual = atribuicaoVigente.value;
  if (!atribuicaoAtual) {
    resetarFormularioAtribuicao();
    return;
  }

  usuarioSelecionado.value = atribuicaoAtual.usuario.tituloEleitoral;
  termoUsuario.value = atribuicaoAtual.usuario.nome;
  dataInicio.value = atribuicaoAtual.dataInicio.slice(0, 10);
  dataTermino.value = atribuicaoAtual.dataTermino.slice(0, 10);
  justificativa.value = atribuicaoAtual.justificativa;
  resetarValidacao();
}

async function carregarDados() {
  carregandoInicial.value = true;
  erroUsuario.value = "";

  try {
    unidade.value = await unidadeStore.recarregarUnidade(props.codUnidade);
    atribuicoes.value = await buscarAtribuicoesTemporariasPorUnidade(props.codUnidade);
    preencherFormularioComAtribuicaoVigente();
  } catch (error) {
    const mensagemErro = normalizarErro(error).mensagem;
    erroUsuario.value = mensagemErro;
    erroFormulario.value = mensagemErro;
    logger.error(error);
  } finally {
    carregandoInicial.value = false;
  }
}

onMounted(async () => {
  await carregarDados();
  carregamentoInicialConcluido.value = true;
});

onActivated(async () => {
  if (!carregamentoInicialConcluido.value) {
    return;
  }
  await carregarDados();
});

async function atualizarCachesPosMutacao() {
  unidade.value = await unidadeStore.recarregarUnidade(props.codUnidade);
  atribuicoes.value = await buscarAtribuicoesTemporariasPorUnidade(props.codUnidade);
  preencherFormularioComAtribuicaoVigente();
}

async function salvarAtribuicao() {
  const unidadeAtual = unidade.value;
  if (!unidadeAtual) throw new Error("Invariante violada: unidade não carregada");

  erroUsuario.value = "";
  erroFormulario.value = "";

  if (!validarSubmissao(formularioValido.value)) {
    await focarPrimeiroErroInvalido();
    return;
  }

  const tituloEleitoralUsuario = usuarioSelecionado.value!;
  const request = {
    tituloEleitoralUsuario,
    dataInicio: dataInicio.value,
    dataTermino: dataTermino.value,
    justificativa: justificativa.value
  };
  const estavaEmEdicao = modoEdicao.value;

  isLoading.value = true;

  try {
    if (atribuicaoVigente.value) {
      await atualizarAtribuicaoTemporaria(unidadeAtual.codigo, atribuicaoVigente.value.codigo, request);
    } else {
      await criarAtribuicaoTemporaria(unidadeAtual.codigo, request);
    }

    await organizacaoStore.recarregarDiagnostico(mostrarDiagnosticoOrganizacional.value);
    await atualizarCachesPosMutacao();
    notify(
        estavaEmEdicao
            ? TEXTOS.atribuicaoTemporaria.SUCESSO_ATUALIZACAO
            : TEXTOS.atribuicaoTemporaria.SUCESSO,
        "success"
    );
  } catch (error) {
    logger.error(error);
    erroFormulario.value = normalizarErro(error).mensagem;
  } finally {
    isLoading.value = false;
  }
}

async function removerAtribuicao() {
  const unidadeAtual = unidade.value;
  const atribuicaoAtual = atribuicaoVigente.value;
  if (!unidadeAtual || !atribuicaoAtual) {
    return;
  }

  erroFormulario.value = "";
  isLoading.value = true;

  try {
    await removerAtribuicaoTemporaria(unidadeAtual.codigo, atribuicaoAtual.codigo);
    await organizacaoStore.recarregarDiagnostico(mostrarDiagnosticoOrganizacional.value);
    await atualizarCachesPosMutacao();
    mostrarModalRemocao.value = false;
    resetarFormularioAtribuicao();
    notify(TEXTOS.atribuicaoTemporaria.SUCESSO_REMOCAO, "success");
  } catch (error) {
    logger.error(error);
    erroFormulario.value = normalizarErro(error).mensagem;
  } finally {
    isLoading.value = false;
  }
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
