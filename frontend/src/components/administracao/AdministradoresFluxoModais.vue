<template>
  <ModalConfirmacao
      :model-value="mostrarModalAdicionarAdmin"
      :auto-close="false"
      :loading="adicionandoAdmin"
      ok-title="Adicionar"
      :titulo="TEXTOS.administracao.MODAL_ADICIONAR_TITULO"
      variant="success"
      @confirmar="$emit('adicionarAdmin')"
      @shown="$emit('modalAdicionarExibido')"
      @update:model-value="$emit('update:mostrarModalAdicionarAdmin', $event)"
  >
    <BAlert v-if="erroAdicionarAdmin" :model-value="true" class="mb-3" dismissible variant="danger">
      {{ erroAdicionarAdmin }}
    </BAlert>
    <BFormGroup class="mb-3">
      <BuscadorUsuarios
          id="tituloEleitoral"
          ref="inputTituloRef"
          :selecionado="usuarioSelecionado"
          :termo="termoUsuario"
          :placeholder="TEXTOS.administracao.PLACEHOLDER_TITULO"
          :state="mensagemErroNovoAdmin ? false : null"
          @update:selecionado="$emit('update:usuarioSelecionado', $event)"
          @update:termo="$emit('update:termoUsuario', $event)"
          @keydown.enter.prevent="$emit('adicionarAdmin')"
      />
      <BFormInvalidFeedback :state="mensagemErroNovoAdmin ? false : null">
        {{ mensagemErroNovoAdmin }}
      </BFormInvalidFeedback>
    </BFormGroup>
  </ModalConfirmacao>

  <ModalConfirmacao
      :model-value="mostrarModalRemoverAdmin"
      :auto-close="false"
      :loading="removendoAdmin !== null"
      :ok-title="TEXTOS.comum.BOTAO_REMOVER"
      :titulo="TEXTOS.administracao.MODAL_REMOVER_TITULO"
      variant="danger"
      @confirmar="$emit('removerAdmin')"
      @update:model-value="$emit('update:mostrarModalRemoverAdmin', $event)"
  >
    <BAlert v-if="erroRemoverAdmin" :model-value="true" class="mb-3" dismissible variant="danger">
      {{ erroRemoverAdmin }}
    </BAlert>
    <p v-if="adminParaRemover">
      {{ TEXTOS.administracao.MODAL_REMOVER_PERGUNTA(adminParaRemover.nome) }}
    </p>
  </ModalConfirmacao>
</template>

<script lang="ts" setup>
import {BAlert, BFormGroup, BFormInvalidFeedback} from 'bootstrap-vue-next';
import ModalConfirmacao from '@/components/comum/ModalConfirmacao.vue';
import BuscadorUsuarios from '@/components/comum/BuscadorUsuarios.vue';
import {TEXTOS} from '@/constants/textos';
import type {AdministradorDto} from '@/services/administradorService';

defineProps<{
  adicionandoAdmin: boolean;
  adminParaRemover: AdministradorDto | null;
  erroAdicionarAdmin: string;
  erroRemoverAdmin: string;
  inputTituloRef: unknown;
  mensagemErroNovoAdmin: string;
  mostrarModalAdicionarAdmin: boolean;
  mostrarModalRemoverAdmin: boolean;
  removendoAdmin: string | null;
  termoUsuario: string;
  usuarioSelecionado: string | null;
}>();

defineEmits<{
  (e: 'adicionarAdmin'): void;
  (e: 'modalAdicionarExibido'): void;
  (e: 'removerAdmin'): void;
  (e: 'update:mostrarModalAdicionarAdmin', valor: boolean): void;
  (e: 'update:mostrarModalRemoverAdmin', valor: boolean): void;
  (e: 'update:termoUsuario', valor: string): void;
  (e: 'update:usuarioSelecionado', valor: unknown): void;
}>();
</script>
