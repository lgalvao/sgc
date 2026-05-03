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
        @blur="agendarOcultacao"
        @focus="mostrarResultadosUsuarios = termoPesquisaMinimaAtingida"
        @keydown="aoPressionarTeclaUsuario"
        @update:model-value="aoAlterarTermo"
    />
    <div v-if="mostrarResultadosUsuarios && termoPesquisaMinimaAtingida" class="usuario-dropdown overflow-auto" data-testid="lista-usuarios-pesquisa">
      <div v-if="pesquisandoUsuarios" class="p-2 text-muted small"><BSpinner aria-hidden="true" class="me-2" small />Buscando usuários...</div>
      <BListGroup v-else-if="usuariosEncontrados.length > 0" flush>
        <BListGroupItem
            v-for="(u, i) in usuariosEncontrados" :id="`opcao-usuario-${u.tituloEleitoral}`" :key="u.tituloEleitoral" action button
            :active="indiceUsuarioDestacado === i" :data-testid="`opcao-usuario-${u.tituloEleitoral}`" @mousedown.prevent="selecionarUsuario(u)"
        >{{ u.nome }} <span class="text-muted small">({{ u.tituloEleitoral }})</span></BListGroupItem>
      </BListGroup>
      <div v-else class="p-2 text-muted small">Nenhum usuário encontrado.</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {nextTick, ref} from 'vue';
import {BFormInput, BListGroup, BListGroupItem, BSpinner} from 'bootstrap-vue-next';
import {useBuscadorUsuarios} from '@/composables/useBuscadorUsuarios';

const termo = defineModel<string>('termo', { default: '' });
const selecionado = defineModel<string | null>('selecionado', { default: null });
defineProps<{ id?: string; placeholder?: string; state?: boolean | null; dataTestid?: string; }>();

const inputRef = ref<InstanceType<typeof BFormInput> | null>(null);
const {
  usuariosEncontrados, pesquisandoUsuarios, mostrarResultadosUsuarios, indiceUsuarioDestacado, termoPesquisaMinimaAtingida,
  aoAlterarTermo, selecionarUsuario, agendarOcultacao, limparResultados, calcularProximoIndice
} = useBuscadorUsuarios(termo, selecionado);

async function destacarUsuario(indice: number) {
  indiceUsuarioDestacado.value = indice;
  await nextTick();
  const u = usuariosEncontrados.value[indice];
  if (u) document.getElementById(`opcao-usuario-${u.tituloEleitoral}`)?.scrollIntoView({ block: "nearest" });
}

async function aoPressionarTeclaUsuario(evento: KeyboardEvent) {
  if (!mostrarResultadosUsuarios.value || usuariosEncontrados.value.length === 0) {
    if (evento.key === "ArrowDown" && termoPesquisaMinimaAtingida.value) mostrarResultadosUsuarios.value = true;
    return;
  }
  switch (evento.key) {
    case "ArrowDown": evento.preventDefault(); await destacarUsuario(calcularProximoIndice(1)); break;
    case "ArrowUp": evento.preventDefault(); await destacarUsuario(calcularProximoIndice(-1)); break;
    case "Enter": if (indiceUsuarioDestacado.value >= 0) { evento.preventDefault(); selecionarUsuario(usuariosEncontrados.value[indiceUsuarioDestacado.value]!); } break;
    case "Escape": mostrarResultadosUsuarios.value = false; break;
  }
}

defineExpose({ focus: () => inputRef.value?.$el?.focus(), limparResultadosPesquisaUsuarios: limparResultados });
</script>

<style scoped>
.campo-usuario { position: relative; }
.usuario-dropdown { position: absolute; top: calc(100% + 0.25rem); left: 0; right: 0; z-index: 20; background: var(--bs-body-bg); border: 1px solid var(--bs-border-color); border-radius: var(--bs-border-radius); box-shadow: var(--bs-box-shadow-sm); }
.usuario-dropdown :deep(.list-group-item.active) { color: var(--bs-secondary-text-emphasis); background-color: var(--bs-secondary-bg-subtle); border-color: var(--bs-secondary-border-subtle); }
</style>
