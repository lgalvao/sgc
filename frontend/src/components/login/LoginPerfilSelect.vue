<script setup lang="ts">
import {
  BFormGroup,
  BFormInvalidFeedback,
  BFormSelect,
  BFormSelectOption,
} from "bootstrap-vue-next";
import type {PerfilUnidade} from "@/types/autenticacao";
import {TEXTOS} from "@/constants/textos";

interface PerfilOption {
  value: PerfilUnidade;
  text: string;
}

interface Props {
  mostrar: boolean;
  parSelecionado: PerfilUnidade | string | null;
  perfisUnidadesOptions: PerfilOption[];
  mensagemErroPerfil: string;
}

defineProps<Props>();

const emit = defineEmits<{
  (e: "update:parSelecionado", valor: PerfilUnidade | null): void;
}>();

function atualizarPerfilSelecionado(valor: PerfilUnidade | PerfilUnidade[] | string | null) {
  if (valor && typeof valor === "object" && !Array.isArray(valor)) {
    emit("update:parSelecionado", valor);
    return;
  }

  emit("update:parSelecionado", null);
}
</script>

<template>
  <BFormGroup
      v-if="mostrar"
      label-for="par"
      class="mb-3"
      data-testid="sec-login-perfil"
  >
    <template #label>
      {{ TEXTOS.login.SELECAO_PERFIL }} <span aria-hidden="true" class="text-danger">*</span>
    </template>
    <BFormSelect
        id="par"
        :model-value="parSelecionado"
        :options="perfisUnidadesOptions"
        :state="mensagemErroPerfil ? false : null"
        data-testid="sel-login-perfil"
        text-field="text"
        value-field="value"
        @update:model-value="atualizarPerfilSelecionado"
    >
      <template #first>
        <BFormSelectOption
            :value="null"
            disabled
        >
          {{ TEXTOS.login.SELECIONE_OPCAO }}
        </BFormSelectOption>
      </template>
    </BFormSelect>
    <BFormInvalidFeedback :state="mensagemErroPerfil ? false : null">
      {{ mensagemErroPerfil }}
    </BFormInvalidFeedback>
  </BFormGroup>
</template>
