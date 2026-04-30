<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.administracao.TITULO">
      <template #actions>
        <div class="d-flex gap-2">
          <BButton
              data-testid="btn-abrir-modal-add-admin"
              variant="outline-primary"
              @click="abrirModalAdicionarAdmin"
          >
            <i aria-hidden="true" class="bi bi-person-plus me-1"></i> {{ TEXTOS.administracao.BOTAO_ADICIONAR }}
          </BButton>
        </div>
      </template>
    </PageHeader>

    <div v-if="carregandoAdmins" class="text-center py-4">
      <BSpinner :label="TEXTOS.comum.CARREGANDO" variant="primary" />
    </div>

    <BAlert v-else-if="erroAdmins" :model-value="true" variant="danger">
      {{ erroAdmins }}
    </BAlert>

    <div v-else-if="administradores.length === 0">
      <EmptyState
          :description="TEXTOS.administracao.EMPTY_DESCRIPTION"
          icon="bi-people"
          :title="TEXTOS.administracao.EMPTY_TITLE"
      />
    </div>

    <BTable
        v-else
        :fields="camposAdmins"
        :items="administradores"
        striped
        hover
        responsive
    >
      <template #cell(acoes)="{ item }">
        <div class="text-end">
          <LoadingButton
              :loading="removendoAdmin === item.tituloEleitoral"
              icon="trash"
              size="sm"
              variant="link"
              class="text-secondary"
              :title="TEXTOS.comum.BOTAO_REMOVER"
              @click="confirmarRemocao(item)"
          />
        </div>
      </template>
    </BTable>

    <!-- Modal: Adicionar administrador -->
    <ModalConfirmacao
        v-model="mostrarModalAdicionarAdmin"
        :auto-close="false"
        :loading="adicionandoAdmin"
        :ok-title="TEXTOS.comum.BOTAO_CRIAR"
        :titulo="TEXTOS.administracao.MODAL_ADICIONAR_TITULO"
        variant="success"
        @confirmar="adicionarAdmin"
        @shown="() => inputTituloRef?.focus()"
    >
      <BAlert v-if="erroAdicionarAdmin" :model-value="true" class="mb-3" variant="danger">
        {{ erroAdicionarAdmin }}
      </BAlert>
      <BFormGroup
          label-for="tituloEleitoral"
          class="mb-3"
      >
        <template #label>
          {{ TEXTOS.administracao.LABEL_TITULO }} <span aria-hidden="true" class="text-danger">*</span>
        </template>
        <div class="campo-usuario">
          <BFormInput
              id="tituloEleitoral"
              ref="inputTituloRef"
              v-model="termoUsuario"
              aria-required="true"
              autocomplete="off"
              :state="mensagemErroNovoAdmin ? false : null"
              data-testid="input-busca-usuario"
              :placeholder="TEXTOS.administracao.PLACEHOLDER_TITULO"
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
              Buscando administradores...
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
              Nenhum usuário encontrado.
            </div>
          </div>
        </div>
        <BFormInvalidFeedback :state="mensagemErroNovoAdmin ? false : null">
          {{ mensagemErroNovoAdmin }}
        </BFormInvalidFeedback>
      </BFormGroup>
    </ModalConfirmacao>

    <!-- Modal: Remover administrador -->
    <ModalConfirmacao
        v-model="mostrarModalRemoverAdmin"
        :auto-close="false"
        :loading="removendoAdmin !== null"
        :ok-title="TEXTOS.comum.BOTAO_REMOVER"
        :titulo="TEXTOS.administracao.MODAL_REMOVER_TITULO"
        variant="danger"
        @confirmar="removerAdmin"
    >
      <BAlert v-if="erroRemoverAdmin" :model-value="true" class="mb-3" variant="danger">
        {{ erroRemoverAdmin }}
      </BAlert>
      <p v-if="adminParaRemover">
        {{ TEXTOS.administracao.MODAL_REMOVER_PERGUNTA(adminParaRemover.nome) }}
      </p>
    </ModalConfirmacao>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, nextTick, onBeforeUnmount, onMounted, ref, watch} from 'vue';
import {BAlert, BButton, BFormGroup, BFormInput, BFormInvalidFeedback, BListGroup, BListGroupItem, BSpinner, BTable} from 'bootstrap-vue-next';
import {useRouter} from 'vue-router';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import ModalConfirmacao from '@/components/comum/ModalConfirmacao.vue';
import LoadingButton from '@/components/comum/LoadingButton.vue';
import {
  adicionarAdministrador,
  type AdministradorDto,
  listarAdministradores,
  removerAdministrador
} from '@/services/administradorService';
import {pesquisarUsuarios} from '@/services/usuarioService';
import type {UsuarioPesquisa} from '@/types/tipos';
import {logger} from '@/utils';
import {normalizeError} from '@/utils/apiError';
import {useNotification} from '@/composables/useNotification';
import {useValidacaoFormulario} from '@/composables/useValidacaoFormulario';
import {TEXTOS} from '@/constants/textos';
import {useAsyncAction} from '@/composables/useAsyncAction';

const {notify} = useNotification();
const {carregando: carregandoAdmins, erro: erroAdmins, executarSilencioso} = useAsyncAction();
const router = useRouter();

const administradores = ref<AdministradorDto[]>([]);
const removendoAdmin = ref<string | null>(null);
const mostrarModalAdicionarAdmin = ref(false);
const mostrarModalRemoverAdmin = ref(false);
const adminParaRemover = ref<AdministradorDto | null>(null);
const usuarioSelecionado = ref<string | null>(null);
const termoUsuario = ref('');
const usuariosEncontrados = ref<UsuarioPesquisa[]>([]);
const pesquisandoUsuarios = ref(false);
const mostrarResultadosUsuarios = ref(false);
const indiceUsuarioDestacado = ref(-1);

let timeoutPesquisaUsuarios: ReturnType<typeof setTimeout> | null = null;
let timeoutOcultarResultadosUsuarios: ReturnType<typeof setTimeout> | null = null;

const termoPesquisaMinimaAtingida = computed(() => termoUsuario.value.trim().length >= 2);

watch(usuariosEncontrados, (usuarios) => {
  indiceUsuarioDestacado.value = usuarios.length > 0 ? 0 : -1;
});
const {
  validarSubmissao,
  resetarValidacao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const erroAdicionarAdmin = ref('');
const erroRemoverAdmin = ref('');
const adicionandoAdmin = ref(false);
const inputTituloRef = ref<InstanceType<typeof BFormInput> | null>(null);

const camposAdmins = [
  {key: 'nome', label: TEXTOS.administracao.CAMPO_NOME},
  {key: 'tituloEleitoral', label: TEXTOS.administracao.CAMPO_TITULO},
  {key: 'matricula', label: TEXTOS.administracao.CAMPO_MATRICULA},
  {key: 'unidadeSigla', label: TEXTOS.administracao.CAMPO_UNIDADE},
  {key: 'acoes', label: TEXTOS.administracao.CAMPO_ACOES, thClass: 'text-end'},
];

const mensagemErroNovoAdmin = computed(() => {
  return deveExibirErro(!termoUsuario.value.trim()) ? TEXTOS.administracao.ERRO_TITULO_INVALIDO : '';
});

async function carregarAdministradores() {
  await executarSilencioso(async () => {
    administradores.value = await listarAdministradores();
  }, TEXTOS.comum.ERRO_OPERACAO);
}

function abrirModalAdicionarAdmin() {
  termoUsuario.value = '';
  usuarioSelecionado.value = null;
  resetarValidacao();
  erroAdicionarAdmin.value = '';
  mostrarModalAdicionarAdmin.value = true;
}

function fecharModalAdicionarAdmin() {
  mostrarModalAdicionarAdmin.value = false;
  termoUsuario.value = '';
  usuarioSelecionado.value = null;
  resetarValidacao();
  erroAdicionarAdmin.value = '';
  limparResultadosPesquisaUsuarios();
}

async function adicionarAdmin() {
  const adminId = usuarioSelecionado.value || termoUsuario.value.trim();

  if (!validarSubmissao(!!adminId)) {
    await focarPrimeiroErroInvalido();
    return;
  }

  erroAdicionarAdmin.value = '';
  adicionandoAdmin.value = true;
  try {
    await adicionarAdministrador(adminId);
    fecharModalAdicionarAdmin();
    notify(TEXTOS.administracao.SUCESSO_ADICIONADO, 'success');
    await carregarAdministradores();
  } catch (error) {
    erroAdicionarAdmin.value = normalizeError(error).message;
  } finally {
    adicionandoAdmin.value = false;
  }
}

onBeforeUnmount(() => {
  limparTimeoutsUsuarios();
});

function aoAlterarTermoUsuario(valor: string | number | null) {
  termoUsuario.value = valor == null ? "" : String(valor);
  mostrarResultadosUsuarios.value = podeExibirResultadosPesquisa();
  indiceUsuarioDestacado.value = -1;
  atualizarUsuarioSelecionadoPorNome(termoUsuario.value);
  cancelarPesquisaUsuariosPendente();

  if (!devePesquisarUsuarios()) {
    limparResultadosPesquisaUsuarios();
    return;
  }

  agendarPesquisaUsuarios();
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
  if (!podeNavegarResultadosUsuarios()) {
    if (evento.key === "ArrowDown" && termoPesquisaMinimaAtingida.value) {
      mostrarResultadosUsuarios.value = true;
    }
    return;
  }

  switch (evento.key) {
    case "ArrowDown":
      evento.preventDefault();
      await destacarUsuario(calcularProximoIndice(1));
      return;
    case "ArrowUp":
      evento.preventDefault();
      await destacarUsuario(calcularProximoIndice(-1));
      return;
    case "Enter":
      if (indiceUsuarioDestacado.value < 0) return;
      evento.preventDefault();
      selecionarUsuario(usuariosEncontrados.value[indiceUsuarioDestacado.value]!);
      return;
    case "Escape":
      ocultarResultadosUsuarios();
      return;
    default:
      return;
  }
}

function limparTimeoutPesquisaUsuarios() {
  if (timeoutPesquisaUsuarios) {
    clearTimeout(timeoutPesquisaUsuarios);
    timeoutPesquisaUsuarios = null;
  }
}

function cancelarPesquisaUsuariosPendente() {
  limparTimeoutPesquisaUsuarios();
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

function agendarPesquisaUsuarios() {
  timeoutPesquisaUsuarios = setTimeout(async () => {
    await executarPesquisaUsuarios();
  }, 300);
}

function devePesquisarUsuarios() {
  return termoPesquisaMinimaAtingida.value && Boolean(termoUsuario.value.trim());
}

function podeExibirResultadosPesquisa() {
  return termoPesquisaMinimaAtingida.value;
}

function podeNavegarResultadosUsuarios() {
  return mostrarResultadosUsuarios.value && usuariosEncontrados.value.length > 0;
}

async function executarPesquisaUsuarios() {
  pesquisandoUsuarios.value = true;
  try {
    usuariosEncontrados.value = await pesquisarUsuarios(termoUsuario.value.trim());
    atualizarUsuarioSelecionadoPorNome(termoUsuario.value);
  } catch (error) {
    limparResultadosPesquisaUsuarios();
    logger.error("Erro ao pesquisar usuários:", error);
    notify("Erro ao pesquisar usuários", 'danger');
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

async function confirmarRemocao(admin: AdministradorDto) {
  adminParaRemover.value = admin;
  erroRemoverAdmin.value = '';
  mostrarModalRemoverAdmin.value = true;
}

async function removerAdmin() {
  if (!adminParaRemover.value) return;

  erroRemoverAdmin.value = '';
  removendoAdmin.value = adminParaRemover.value.tituloEleitoral;
  try {
    await removerAdministrador(adminParaRemover.value.tituloEleitoral);
    notify(TEXTOS.administracao.SUCESSO_REMOVIDO, 'success');
    await carregarAdministradores();
    mostrarModalRemoverAdmin.value = false;
    adminParaRemover.value = null;
  } catch (error) {
    erroRemoverAdmin.value = normalizeError(error).message;
  } finally {
    removendoAdmin.value = null;
  }
}

onMounted(async () => {
  await carregarAdministradores();
});
</script>

<style scoped>
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
