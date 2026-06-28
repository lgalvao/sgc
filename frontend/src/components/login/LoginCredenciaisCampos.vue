<script lang="ts" setup>
import {BButton, BFormGroup, BFormInput, BFormInvalidFeedback, BInputGroup,} from "bootstrap-vue-next";
import {TEXTOS} from "@/constants/textos";
import Alerta from "@/components/comum/Alerta.vue";

interface Props {
  titulo: string;
  senha: string;
  loginBloqueado: boolean;
  mostrarAcaoTrocarTituloEleitoral?: boolean;
  carregando: boolean;
  showPassword: boolean;
  capsLockAtivado: boolean;
  mensagemErroTitulo: string;
  mensagemErroSenha: string;
}

withDefaults(defineProps<Props>(), {
  mostrarAcaoTrocarTituloEleitoral: false
});

const emit = defineEmits<{
  (e: "update:titulo", valor: string): void;
  (e: "update:senha", valor: string): void;
  (e: "toggle-senha"): void;
  (e: "verificar-caps-lock", event: KeyboardEvent): void;
  (e: "trocar-titulo-eleitoral"): void;
}>();

function atualizarTitulo(valor: string | number | null) {
  emit(
      "update:titulo",
      typeof valor === "string" ? valor : typeof valor === "number" ? String(valor) : "",
  );
}

function atualizarSenha(valor: string | number | null) {
  emit(
      "update:senha",
      typeof valor === "string" ? valor : typeof valor === "number" ? String(valor) : "",
  );
}
</script>

<template>
  <BFormGroup
      class="mb-3"
      label-for="titulo"
  >
    <template #label>
      <i
          aria-hidden="true"
          class="bi bi-person-circle me-2"
      />
      {{ TEXTOS.login.LABEL_USUARIO }} <span aria-hidden="true" class="text-danger">*</span>
    </template>
    <BInputGroup>
      <!-- eslint-disable vuejs-accessibility/no-autofocus -->
      <BFormInput
          id="titulo"
          :disabled="carregando"
          :model-value="titulo"
          :placeholder="TEXTOS.login.PLACEHOLDER_USUARIO"
          :readonly="loginBloqueado"
          :state="mensagemErroTitulo ? false : null"
          aria-required="true"
          autocomplete="username"
          autofocus
          data-testid="inp-login-usuario"
          inputmode="numeric"
          name="titulo"
          type="text"
          @update:model-value="atualizarTitulo"
      />
      <!-- eslint-enable vuejs-accessibility/no-autofocus -->
      <template #append>
        <BButton
            v-if="mostrarAcaoTrocarTituloEleitoral"
            :aria-label="'Trocar usuário'"
            :disabled="carregando"
            :title="'Trocar usuário'"
            class="login-input-acao"
            data-testid="btn-login-trocar-titulo-eleitoral"
            variant="link"
            @click="emit('trocar-titulo-eleitoral')"
        >
          <i aria-hidden="true" class="bi bi-arrow-repeat"/>
        </BButton>
      </template>
    </BInputGroup>
    <BFormInvalidFeedback :state="mensagemErroTitulo ? false : null">
      {{ mensagemErroTitulo }}
    </BFormInvalidFeedback>
  </BFormGroup>

  <BFormGroup
      class="mb-3"
      label-for="senha"
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
          :autocomplete="showPassword ? 'off' : 'current-password'"
          :disabled="carregando"
          :model-value="senha"
          :placeholder="TEXTOS.login.PLACEHOLDER_SENHA"
          :readonly="loginBloqueado"
          :state="mensagemErroSenha ? false : null"
          :type="showPassword ? 'text' : 'password'"
          aria-required="true"
          data-testid="inp-login-senha"
          name="senha"
          @keydown="emit('verificar-caps-lock', $event)"
          @keyup="emit('verificar-caps-lock', $event)"
          @update:model-value="atualizarSenha"
      />
      <template #append>
        <BButton
            :aria-label="showPassword ? TEXTOS.login.OCULTAR_SENHA : TEXTOS.login.MOSTRAR_SENHA"
            :disabled="carregando"
            class="login-input-acao"
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
    <Alerta
        v-if="capsLockAtivado"
        class="small mt-1 py-1 px-2 mb-0"
        data-testid="alert-caps-lock"
        variante="warning"
    >
      <i
          aria-hidden="true"
          class="bi bi-exclamation-triangle-fill me-1"
      />
      {{ TEXTOS.login.CAPS_LOCK }}
    </Alerta>
  </BFormGroup>
</template>

<style scoped>
.login-input-acao {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 2.75rem;
  padding-inline: 0.75rem;
  color: var(--bs-secondary-color);
  background-color: var(--bs-tertiary-bg);
  border: 1px solid var(--bs-border-color);
  border-left: 0;
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  text-decoration: none;
}

.login-input-acao:hover,
.login-input-acao:focus-visible {
  color: var(--bs-body-color);
  background-color: var(--bs-secondary-bg);
}

.login-input-acao:disabled {
  color: var(--bs-secondary-color);
  background-color: var(--bs-tertiary-bg);
  opacity: 1;
}
</style>
