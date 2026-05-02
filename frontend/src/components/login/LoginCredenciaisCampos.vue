<script setup lang="ts">
import {
  BAlert,
  BButton,
  BFormGroup,
  BFormInput,
  BFormInvalidFeedback,
  BInputGroup,
} from "bootstrap-vue-next";
import {TEXTOS} from "@/constants/textos";

interface Props {
  titulo: string;
  senha: string;
  loginBloqueado: boolean;
  isLoading: boolean;
  showPassword: boolean;
  capsLockAtivado: boolean;
  mensagemErroTitulo: string;
  mensagemErroSenha: string;
}

defineProps<Props>();

const emit = defineEmits<{
  (e: "update:titulo", valor: string): void;
  (e: "update:senha", valor: string): void;
  (e: "toggle-senha"): void;
  (e: "verificar-caps-lock", event: KeyboardEvent): void;
}>();

function atualizarTitulo(valor: string | number | null) {
  emit("update:titulo", valor == null ? "" : String(valor));
}

function atualizarSenha(valor: string | number | null) {
  emit("update:senha", valor == null ? "" : String(valor));
}
</script>

<template>
  <BFormGroup
      label-for="titulo"
      class="mb-3"
  >
    <template #label>
      <i
          aria-hidden="true"
          class="bi bi-person-circle me-2"
      />
      {{ TEXTOS.login.LABEL_USUARIO }} <span aria-hidden="true" class="text-danger">*</span>
    </template>
    <!-- eslint-disable vuejs-accessibility/no-autofocus -->
    <BFormInput
        id="titulo"
        :model-value="titulo"
        aria-required="true"
        :readonly="loginBloqueado"
        :disabled="isLoading"
        autocomplete="username"
        autofocus
        data-testid="inp-login-usuario"
        inputmode="numeric"
        name="titulo"
        :placeholder="TEXTOS.login.PLACEHOLDER_USUARIO"
        :state="mensagemErroTitulo ? false : null"
        type="text"
        @update:model-value="atualizarTitulo"
    />
    <!-- eslint-enable vuejs-accessibility/no-autofocus -->
    <BFormInvalidFeedback :state="mensagemErroTitulo ? false : null">
      {{ mensagemErroTitulo }}
    </BFormInvalidFeedback>
  </BFormGroup>

  <BFormGroup
      label-for="senha"
      class="mb-3"
  >
    <template #label>
      <i
          aria-hidden="true"
          class="bi bi-key me-2"
      />
      {{ TEXTOS.login.LABEL_SENHA }} <span aria-hidden="true" class="text-danger">*</span>
    </template>
    <BInputGroup>
      <BFormInput
          id="senha"
          :model-value="senha"
          aria-required="true"
          :autocomplete="showPassword ? 'off' : 'current-password'"
          :readonly="loginBloqueado"
          :disabled="isLoading"
          :state="mensagemErroSenha ? false : null"
          :type="showPassword ? 'text' : 'password'"
          data-testid="inp-login-senha"
          name="senha"
          :placeholder="TEXTOS.login.PLACEHOLDER_SENHA"
          @update:model-value="atualizarSenha"
          @keydown="emit('verificar-caps-lock', $event)"
          @keyup="emit('verificar-caps-lock', $event)"
      />
      <template #append>
        <BButton
            :aria-label="showPassword ? TEXTOS.login.OCULTAR_SENHA : TEXTOS.login.MOSTRAR_SENHA"
            :disabled="isLoading"
            class="text-secondary border-0"
            variant="link"
            @click="emit('toggle-senha')"
        >
          <i
              :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"
              aria-hidden="true"
          />
        </BButton>
      </template>
    </BInputGroup>
    <BFormInvalidFeedback :state="mensagemErroSenha ? false : null">
      {{ mensagemErroSenha }}
    </BFormInvalidFeedback>
    <BAlert
        v-if="capsLockAtivado"
        :model-value="true"
        variant="warning"
        class="small mt-1 py-1 px-2 mb-0"
        data-testid="alert-caps-lock"
    >
      <i
          aria-hidden="true"
          class="bi bi-exclamation-triangle-fill me-1"
      />
      {{ TEXTOS.login.CAPS_LOCK }}
    </BAlert>
  </BFormGroup>
</template>
