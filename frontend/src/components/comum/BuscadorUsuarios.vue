<template>
  <div class="campo-usuario">
    <BFormInput
        :id="id"
        ref="inputRef"
        v-model="termo"
        aria-required="true"
        autocomplete="off"
        :state="state"
        :data-testid="dataTestid || 'input-busca-usuario'"
        :placeholder="placeholder || 'Pesquise...'"
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
        Buscando usuários...
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
          {{ usuario.nome }} <span class="text-muted small">({{ usuario.tituloEleitoral }})</span>
        </BListGroupItem>
      </BListGroup>
      <div v-else class="p-2 text-muted small">
        Nenhum usuário encontrado.
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, nextTick, onBeforeUnmount, ref, watch} from 'vue';
import {BFormInput, BListGroup, BListGroupItem, BSpinner} from 'bootstrap-vue-next';
import {pesquisarUsuarios} from '@/services/usuarioService';
import type {UsuarioPesquisa} from '@/types/tipos';
import {logger} from '@/utils';
import {useNotification} from '@/composables/useNotification';

const termo = defineModel<string>('termo', { default: '' });
const selecionado = defineModel<string | null>('selecionado', { default: null });

defineProps<{
  id?: string;
  placeholder?: string;
  state?: boolean | null;
  dataTestid?: string;
}>();

const { notify } = useNotification();
const inputRef = ref<InstanceType<typeof BFormInput> | null>(null);

const usuariosEncontrados = ref<UsuarioPesquisa[]>([]);
const pesquisandoUsuarios = ref(false);
const mostrarResultadosUsuarios = ref(false);
const indiceUsuarioDestacado = ref(-1);

let timeoutPesquisaUsuarios: ReturnType<typeof setTimeout> | null = null;
let timeoutOcultarResultadosUsuarios: ReturnType<typeof setTimeout> | null = null;

const termoPesquisaMinimaAtingida = computed(() => termo.value.trim().length >= 2);

watch(usuariosEncontrados, (usuarios) => {
  indiceUsuarioDestacado.value = usuarios.length > 0 ? 0 : -1;
});

onBeforeUnmount(() => {
  limparTimeoutsUsuarios();
});

function aoAlterarTermoUsuario(valor: string | number | null) {
  termo.value = valor == null ? "" : String(valor);
  mostrarResultadosUsuarios.value = podeExibirResultadosPesquisa();
  indiceUsuarioDestacado.value = -1;
  atualizarUsuarioSelecionadoPorNome(termo.value);
  cancelarPesquisaUsuariosPendente();

  if (!devePesquisarUsuarios()) {
    limparResultadosPesquisaUsuarios();
    return;
  }

  agendarPesquisaUsuarios();
}

function atualizarUsuarioSelecionadoPorNome(nome: string) {
  const usuario = usuariosEncontrados.value.find((item) => item.nome === nome.trim());
  selecionado.value = usuario?.tituloEleitoral ?? null;
}

function selecionarUsuario(usuario: UsuarioPesquisa) {
  selecionado.value = usuario.tituloEleitoral;
  termo.value = usuario.nome;
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
  elemento?.scrollIntoView({ block: "nearest" });
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
  return termoPesquisaMinimaAtingida.value && Boolean(termo.value.trim());
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
    usuariosEncontrados.value = await pesquisarUsuarios(termo.value.trim());
    atualizarUsuarioSelecionadoPorNome(termo.value);
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

function focus() {
  inputRef.value?.$el?.focus();
}

defineExpose({
  focus,
  limparResultadosPesquisaUsuarios,
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
