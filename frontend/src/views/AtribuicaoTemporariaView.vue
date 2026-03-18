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
          <BFormInput
              id="usuario"
              v-model="termoUsuario"
              aria-required="true"
              autocomplete="off"
              :state="mensagemErroUsuario ? false : null"
              data-testid="input-busca-usuario"
              :placeholder="TEXTOS.atribuicaoTemporaria.SELECIONE_USUARIO"
              type="text"
              @focus="mostrarResultadosUsuarios = termoPesquisaMinimaAtingida"
              @update:model-value="aoAlterarTermoUsuario"
          />
          <div
              v-if="mostrarResultadosUsuarios && termoPesquisaMinimaAtingida"
              class="border rounded mt-2 overflow-auto usuario-resultados"
              data-testid="lista-usuarios-pesquisa"
          >
            <div v-if="pesquisandoUsuarios" class="p-3 text-muted small">
              <BSpinner aria-hidden="true" class="me-2" small />
              Buscando usuários...
            </div>
            <template v-else-if="usuariosEncontrados.length > 0">
              <BListGroup flush>
                <BListGroupItem
                    v-for="usuario in usuariosEncontrados"
                    :key="usuario.codigo"
                    action
                    button
                    class="py-2"
                    :data-testid="`opcao-usuario-${usuario.codigo}`"
                    @click="selecionarUsuario(usuario)"
                >
                  <div class="fw-medium">{{ usuario.nome }}</div>
                  <small class="text-muted">{{ usuario.tituloEleitoral }}</small>
                </BListGroupItem>
              </BListGroup>
            </template>
            <div v-else class="p-3 text-muted small">
              Nenhum usuário encontrado.
            </div>
          </div>
          <div
              v-else-if="!termoPesquisaMinimaAtingida"
              class="small text-muted mt-2"
              data-testid="txt-ajuda-busca-usuario"
          >
            Digite pelo menos 2 caracteres para pesquisar por nome.
          </div>
          <div
              v-if="usuarioSelecionadoNome"
              class="small text-muted mt-2"
              data-testid="txt-usuario-selecionado"
          >
            Selecionado: {{ usuarioSelecionadoNome }}
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
                  min="2000-01-01"
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
                  min="2000-01-01"
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
  BListGroup,
  BListGroupItem,
  BSpinner,
  BFormTextarea,
  BRow
} from "bootstrap-vue-next";
import {computed, onBeforeUnmount, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {logger} from "@/utils";
import type {Unidade, Usuario} from "@/types/tipos";
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import InputData from "@/components/comum/InputData.vue";
import {useNotification} from "@/composables/useNotification";
import {TEXTOS} from "@/constants/textos";
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
const usuarioSelecionadoNome = ref("");
const termoUsuario = ref("");
const usuariosEncontrados = ref<Usuario[]>([]);
const pesquisandoUsuarios = ref(false);
const mostrarResultadosUsuarios = ref(false);
const dataInicio = ref("");
const dataTermino = ref("");
const justificativa = ref("");
const isLoading = ref(false);
let timeoutPesquisaUsuarios: ReturnType<typeof setTimeout> | null = null;

const erroUsuario = ref("");
const validacaoSubmetida = ref(false);
const termoPesquisaMinimaAtingida = computed(() => termoUsuario.value.trim().length >= 2);

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
  if (timeoutPesquisaUsuarios) {
    clearTimeout(timeoutPesquisaUsuarios);
  }
});

function aoAlterarTermoUsuario(valor: string | number) {
  termoUsuario.value = String(valor);
  mostrarResultadosUsuarios.value = termoPesquisaMinimaAtingida.value;

  if (usuarioSelecionadoNome.value && termoUsuario.value !== usuarioSelecionadoNome.value) {
    usuarioSelecionado.value = null;
    usuarioSelecionadoNome.value = "";
  }

  if (timeoutPesquisaUsuarios) {
    clearTimeout(timeoutPesquisaUsuarios);
  }

  if (!termoPesquisaMinimaAtingida.value) {
    usuariosEncontrados.value = [];
    pesquisandoUsuarios.value = false;
    return;
  }

  timeoutPesquisaUsuarios = setTimeout(async () => {
    pesquisandoUsuarios.value = true;
    try {
      usuariosEncontrados.value = await pesquisarUsuarios(termoUsuario.value.trim());
    } catch (error) {
      usuariosEncontrados.value = [];
      logger.error("Erro ao pesquisar usuários:", error);
      notify(TEXTOS.atribuicaoTemporaria.ERRO_CARREGAR, 'danger');
    } finally {
      pesquisandoUsuarios.value = false;
    }
  }, 300);
}

function selecionarUsuario(usuario: Usuario) {
  usuarioSelecionado.value = usuario.tituloEleitoral;
  usuarioSelecionadoNome.value = usuario.nome;
  termoUsuario.value = usuario.nome;
  usuariosEncontrados.value = [];
  mostrarResultadosUsuarios.value = false;
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

    usuarioSelecionado.value = null;
    usuarioSelecionadoNome.value = "";
    termoUsuario.value = "";
    usuariosEncontrados.value = [];
    mostrarResultadosUsuarios.value = false;
    dataInicio.value = "";
    dataTermino.value = "";
    justificativa.value = "";
    validacaoSubmetida.value = false;
  } catch (error) {
    logger.error(error);
    notify(TEXTOS.atribuicaoTemporaria.ERRO_CRIAR, 'danger');
  } finally {
    isLoading.value = false;
  }
}

defineExpose({
  router,
  codUnidade,
  unidade,
  usuarioSelecionadoNome,
  termoUsuario,
  usuariosEncontrados,
  usuarioSelecionado,
  dataInicio,
  dataTermino,
  justificativa,
  isLoading,
  erroUsuario,
  selecionarUsuario,
  aoAlterarTermoUsuario,
  criarAtribuicao
});
</script>

<style scoped>
.btn-acao-cabecalho {
  min-width: 8.5rem;
  justify-content: center;
}

.usuario-resultados {
  max-height: 16rem;
}
</style>
