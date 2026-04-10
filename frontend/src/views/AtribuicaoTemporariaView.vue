<template>
  <LayoutPadrao>
    <div class="col-lg-8 col-md-9 col-12">
      <PageHeader :title="TEXTOS.atribuicaoTemporaria.TITULO">
        <template v-if="unidade" #default>
          {{ unidade.sigla }}
        </template>
        <template #actions>
          <BButton
              class="btn-acao-cabecalho"
              :disabled="isLoading"
              data-testid="btn-cancelar-atribuicao"
              type="button"
              variant="outline-secondary"
              @click="router.push(`/unidade/${codUnidade}`)"
          >
            {{ TEXTOS.comum.BOTAO_CANCELAR }}
          </BButton>
          <LoadingButton
              class="btn-acao-cabecalho"
              :loading="isLoading"
              data-testid="cad-atribuicao__btn-criar-atribuicao"
              :loading-text="TEXTOS.atribuicaoTemporaria.CRIANDO"
              :text="TEXTOS.comum.BOTAO_CRIAR"
              type="button"
              variant="primary"
              @click="criarAtribuicao"
          />
        </template>
      </PageHeader>
      <AppAlert
          v-if="notificacao"
          :dismissible="notificacao.dismissible ?? true"
          :message="notificacao.message"
          :variant="notificacao.variant"
          @dismissed="clear()"
      />
      <BForm class="mt-4" @submit.prevent="criarAtribuicao">
        <BFormGroup
            :label="TEXTOS.atribuicaoTemporaria.LABEL_USUARIO"
            label-for="usuario"
            class="mb-3"
        >
          <template #description>
            {{ TEXTOS.atribuicaoTemporaria.AJUDA_PESQUISA_USUARIO }}
          </template>
          <div class="campo-usuario">
            <BFormInput
                id="usuario"
                v-model="termoUsuario"
                aria-required="true"
                autocomplete="off"
                :state="mensagemErroUsuario ? false : null"
                data-testid="input-busca-usuario"
                :placeholder="TEXTOS.atribuicaoTemporaria.SELECIONE_USUARIO"
                type="text"
                @blur="agendarOcultacaoResultadosUsuarios"
                @focus="mostrarResultadosUsuarios = termoPesquisaMinimaAtingida"
                @keydown="aoPressionarTeclaUsuario"
                @update:model-value="aoAlterarTermoUsuario"
            />
            <div
                v-if="mostrarResultadosUsuarios && termoPesquisaMinimaAtingida"
                class="usuario-dropdown overflow-auto"
                data-testid="lista-usuarios-pesquisa"
            >
              <div v-if="pesquisandoUsuarios" class="p-2 text-muted small">
                <BSpinner aria-hidden="true" class="me-2" small />
                {{ TEXTOS.atribuicaoTemporaria.BUSCANDO_USUARIOS }}
              </div>
              <BListGroup v-else-if="usuariosEncontrados.length > 0" flush>
                <BListGroupItem
                    v-for="(usuario, indice) in usuariosEncontrados"
                    :id="`opcao-usuario-${usuario.tituloEleitoral}`"
                    :key="usuario.tituloEleitoral"
                    action
                    button
                    :active="indiceUsuarioDestacado === indice"
                    :data-testid="`opcao-usuario-${usuario.tituloEleitoral}`"
                    @mousedown.prevent="selecionarUsuario(usuario)"
                >
                  {{ usuario.nome }}
                </BListGroupItem>
              </BListGroup>
              <div v-else class="p-2 text-muted small">
                {{ TEXTOS.atribuicaoTemporaria.NENHUM_USUARIO_ENCONTRADO }}
              </div>
            </div>
          </div>
          <BFormInvalidFeedback :state="mensagemErroUsuario ? false : null">
            {{ mensagemErroUsuario }}
          </BFormInvalidFeedback>
        </BFormGroup>

        <BRow>
          <BCol md="6" class="mb-3">
            <BFormGroup :label="TEXTOS.atribuicaoTemporaria.LABEL_DATA_INICIO" label-for="dataInicio">
              <InputData
                  id="dataInicio"
                  v-model="dataInicio"
                  :state="mensagemErroDataInicio ? false : null"
                  data-testid="input-data-inicio"
                  max="2099-12-31"
                  :min="obterHojeFormatado()"
              />
              <BFormInvalidFeedback :state="mensagemErroDataInicio ? false : null">
                {{ mensagemErroDataInicio }}
              </BFormInvalidFeedback>
            </BFormGroup>
          </BCol>

          <BCol md="6" class="mb-3">
            <BFormGroup :label="TEXTOS.atribuicaoTemporaria.LABEL_DATA_TERMINO" label-for="dataTermino">
              <InputData
                  id="dataTermino"
                  v-model="dataTermino"
                  :state="mensagemErroDataTermino ? false : null"
                  data-testid="input-data-termino"
                  max="2099-12-31"
                  :min="dataInicio || obterHojeFormatado()"
              />
              <BFormInvalidFeedback :state="mensagemErroDataTermino ? false : null">
                {{ mensagemErroDataTermino }}
              </BFormInvalidFeedback>
            </BFormGroup>
          </BCol>
        </BRow>

        <BFormGroup
            :label="TEXTOS.atribuicaoTemporaria.LABEL_JUSTIFICATIVA"
            label-for="justificativa"
            class="mb-3"
        >
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
      </BForm>
    </div>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {
  BButton,
  BCol,
  BForm,
  BFormGroup,
  BFormInput,
  BFormInvalidFeedback,
  BFormTextarea,
  BListGroup,
  BListGroupItem,
  BRow,
  BSpinner
} from "bootstrap-vue-next";
import {computed, nextTick, onBeforeUnmount, onMounted, ref, watch} from "vue";
import {useRouter} from "vue-router";
import {logger} from "@/utils";
import type {Unidade, UsuarioPesquisa} from "@/types/tipos";
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import InputData from "@/components/comum/InputData.vue";
import {useNotification} from "@/composables/useNotification";
import {TEXTOS} from "@/constants/textos";
import {obterHojeFormatado} from "@/utils/dateUtils";
import {buscarUnidadePorCodigo as buscarUnidadeServico} from "@/services/unidadeService";
import {pesquisarUsuarios} from "@/services/usuarioService";
import {criarAtribuicaoTemporaria} from "@/services/atribuicaoTemporariaService";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";

const props = defineProps<{ codUnidade: number }>();

const router = useRouter();
const {notificacao, notify, clear} = useNotification();
const codUnidade = computed(() => props.codUnidade);
const unidade = ref<Unidade | null>(null);
const usuarioSelecionado = ref<string | null>(null);
const termoUsuario = ref("");
const usuariosEncontrados = ref<UsuarioPesquisa[]>([]);
const pesquisandoUsuarios = ref(false);
const mostrarResultadosUsuarios = ref(false);
const indiceUsuarioDestacado = ref(-1);
const dataInicio = ref("");
const dataTermino = ref("");
const justificativa = ref("");
const isLoading = ref(false);
let timeoutPesquisaUsuarios: ReturnType<typeof setTimeout> | null = null;
let timeoutOcultarResultadosUsuarios: ReturnType<typeof setTimeout> | null = null;

const erroUsuario = ref("");
const validacaoSubmetida = ref(false);
const termoPesquisaMinimaAtingida = computed(() => termoUsuario.value.trim().length >= 2);

watch(usuariosEncontrados, (usuarios) => {
  indiceUsuarioDestacado.value = usuarios.length > 0 ? 0 : -1;
});

const mensagemErroUsuario = computed(() => {
  if (erroUsuario.value) return erroUsuario.value;
  if (validacaoSubmetida.value && !usuarioSelecionado.value) {
    return TEXTOS.atribuicaoTemporaria.ERRO_SELECIONE_USUARIO;
  }
  return "";
});

const mensagemErroDataInicio = computed(() =>
    validacaoSubmetida.value && !dataInicio.value ? "Informe a data de início." : "",
);

const mensagemErroDataTermino = computed(() =>
    validacaoSubmetida.value && !dataTermino.value ? "Informe a data de término." : "",
);

const mensagemErroJustificativa = computed(() =>
    validacaoSubmetida.value && !justificativa.value.trim() ? "Informe a justificativa." : "",
);

onMounted(async () => {
  try {
    const response = await buscarUnidadeServico(codUnidade.value);
    unidade.value = response as Unidade;
  } catch (error) {
    erroUsuario.value = TEXTOS.atribuicaoTemporaria.ERRO_CARREGAR;
    logger.error(error);
  }
});

onBeforeUnmount(() => {
  limparTimeoutsUsuarios();
});

function aoAlterarTermoUsuario(valor: string | number | null) {
  termoUsuario.value = valor == null ? "" : String(valor);
  mostrarResultadosUsuarios.value = termoPesquisaMinimaAtingida.value;
  indiceUsuarioDestacado.value = -1;
  atualizarUsuarioSelecionadoPorNome(termoUsuario.value);
  limparTimeoutPesquisaUsuarios();

  if (!termoPesquisaMinimaAtingida.value || !termoUsuario.value.trim()) {
    limparResultadosPesquisaUsuarios();
    return;
  }

  timeoutPesquisaUsuarios = setTimeout(async () => {
    await executarPesquisaUsuarios();
  }, 300);
}

function atualizarUsuarioSelecionadoPorNome(nome: string) {
  const usuario = usuariosEncontrados.value.find((item) => item.nome === nome.trim());
  usuarioSelecionado.value = usuario?.tituloEleitoral ?? null;
}

function selecionarUsuario(usuario: UsuarioPesquisa) {
  usuarioSelecionado.value = usuario.tituloEleitoral;
  termoUsuario.value = usuario.nome;
  ocultarResultadosUsuarios();
}

function agendarOcultacaoResultadosUsuarios() {
  limparTimeoutOcultarResultadosUsuarios();
  timeoutOcultarResultadosUsuarios = setTimeout(() => {
    ocultarResultadosUsuarios();
  }, 150);
}

async function destacarUsuario(indice: number) {
  indiceUsuarioDestacado.value = indice;
  await nextTick();

  const usuario = usuariosEncontrados.value[indice];
  if (!usuario) return;

  const elemento = document.getElementById(`opcao-usuario-${usuario.tituloEleitoral}`);
  elemento?.scrollIntoView({block: "nearest"});
}

async function aoPressionarTeclaUsuario(evento: KeyboardEvent) {
  if (!mostrarResultadosUsuarios.value || usuariosEncontrados.value.length === 0) {
    if (evento.key === "ArrowDown" && termoPesquisaMinimaAtingida.value) {
      mostrarResultadosUsuarios.value = true;
    }
    return;
  }

  if (evento.key === "ArrowDown") {
    evento.preventDefault();
    await destacarUsuario(calcularProximoIndice(1));
    return;
  }

  if (evento.key === "ArrowUp") {
    evento.preventDefault();
    await destacarUsuario(calcularProximoIndice(-1));
    return;
  }

  if (evento.key === "Enter" && indiceUsuarioDestacado.value >= 0) {
    evento.preventDefault();
    selecionarUsuario(usuariosEncontrados.value[indiceUsuarioDestacado.value]!);
    return;
  }

  if (evento.key === "Escape") {
    ocultarResultadosUsuarios();
  }
}

async function criarAtribuicao() {
  const unidadeAtual = unidade.value;
  if (!unidadeAtual) throw new Error('Invariante violada: unidade não carregada');
  validacaoSubmetida.value = true;
  erroUsuario.value = "";

  if (!usuarioSelecionado.value || !dataInicio.value || !dataTermino.value || !justificativa.value.trim()) {
    notify(TEXTOS.atribuicaoTemporaria.ERRO_PREENCHIMENTO, 'danger');
    return;
  }

  isLoading.value = true;

  try {
    await criarAtribuicaoTemporaria(unidadeAtual.codigo, {
      tituloEleitoralUsuario: usuarioSelecionado.value,
      dataInicio: dataInicio.value,
      dataTermino: dataTermino.value,
      justificativa: justificativa.value
    });

    notify(TEXTOS.atribuicaoTemporaria.SUCESSO, 'success');
    resetarFormularioAtribuicao();
  } catch (error) {
    logger.error(error);
    notify(TEXTOS.atribuicaoTemporaria.ERRO_CRIAR, 'danger');
  } finally {
    isLoading.value = false;
  }
}

function limparTimeoutPesquisaUsuarios() {
  if (timeoutPesquisaUsuarios) {
    clearTimeout(timeoutPesquisaUsuarios);
    timeoutPesquisaUsuarios = null;
  }
}

function limparTimeoutOcultarResultadosUsuarios() {
  if (timeoutOcultarResultadosUsuarios) {
    clearTimeout(timeoutOcultarResultadosUsuarios);
    timeoutOcultarResultadosUsuarios = null;
  }
}

function limparTimeoutsUsuarios() {
  limparTimeoutPesquisaUsuarios();
  limparTimeoutOcultarResultadosUsuarios();
}

function ocultarResultadosUsuarios() {
  mostrarResultadosUsuarios.value = false;
  indiceUsuarioDestacado.value = -1;
}

function limparResultadosPesquisaUsuarios() {
  usuariosEncontrados.value = [];
  pesquisandoUsuarios.value = false;
  ocultarResultadosUsuarios();
}

async function executarPesquisaUsuarios() {
  pesquisandoUsuarios.value = true;
  try {
    usuariosEncontrados.value = await pesquisarUsuarios(termoUsuario.value.trim());
    atualizarUsuarioSelecionadoPorNome(termoUsuario.value);
  } catch (error) {
    limparResultadosPesquisaUsuarios();
    logger.error("Erro ao pesquisar usuários:", error);
    notify(TEXTOS.atribuicaoTemporaria.ERRO_CARREGAR, 'danger');
  } finally {
    pesquisandoUsuarios.value = false;
  }
}

function calcularProximoIndice(deslocamento: 1 | -1) {
  const ultimoIndice = usuariosEncontrados.value.length - 1;
  if (deslocamento === 1) {
    return indiceUsuarioDestacado.value < ultimoIndice ? indiceUsuarioDestacado.value + 1 : 0;
  }
  return indiceUsuarioDestacado.value > 0 ? indiceUsuarioDestacado.value - 1 : ultimoIndice;
}

function resetarFormularioAtribuicao() {
  usuarioSelecionado.value = null;
  termoUsuario.value = "";
  limparResultadosPesquisaUsuarios();
  dataInicio.value = "";
  dataTermino.value = "";
  justificativa.value = "";
  validacaoSubmetida.value = false;
}

defineExpose({
  router,
  codUnidade,
  unidade,
  termoUsuario,
  usuariosEncontrados,
  usuarioSelecionado,
  mostrarResultadosUsuarios,
  indiceUsuarioDestacado,
  dataInicio,
  dataTermino,
  justificativa,
  isLoading,
  erroUsuario,
  atualizarUsuarioSelecionadoPorNome,
  selecionarUsuario,
  agendarOcultacaoResultadosUsuarios,
  aoPressionarTeclaUsuario,
  aoAlterarTermoUsuario,
  criarAtribuicao,
  notify,
  clear
});
</script>

<style scoped>
.btn-acao-cabecalho {
  min-width: 7rem;
  justify-content: center;
}

.usuario-resultados {
  max-height: 16rem;
}

.campo-usuario {
  position: relative;
}

.usuario-dropdown {
  position: absolute;
  top: calc(100% + 0.25rem);
  left: 0;
  right: 0;
  z-index: 20;
  background: var(--bs-body-bg);
  border: 1px solid var(--bs-border-color);
  border-radius: var(--bs-border-radius);
  box-shadow: var(--bs-box-shadow-sm);
}

.usuario-dropdown :deep(.list-group-item.active) {
  color: var(--bs-secondary-text-emphasis);
  background-color: var(--bs-secondary-bg-subtle);
  border-color: var(--bs-secondary-border-subtle);
}
</style>
