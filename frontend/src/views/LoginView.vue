<template>
  <BContainer class="d-flex align-items-center justify-content-center min-vh-100">
    <BRow class="w-100 justify-content-center">
      <BCol
          lg="4"
          md="6"
          sm="12"
      >
        <BCard
            class="shadow-lg p-4"
            no-body
        >
          <h1
              class="h2 mb-2 text-center"
              data-testid="txt-login-titulo"
          >
            {{ TEXTOS.login.TITULO }}
          </h1>
          <p
              class="h5 mb-4 text-center text-muted"
              data-testid="txt-login-subtitulo"
          >
            {{ TEXTOS.login.SUBTITULO }}
          </p>
          <BForm
              class="p-0"
              data-testid="form-login"
              @submit.prevent="handleLogin"
          >
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
                   v-model="titulo"
                   aria-required="true"
                   :disabled="loginStep > 1"
                   autocomplete="username"
                   autofocus
                   data-testid="inp-login-usuario"
                   inputmode="numeric"
                   name="titulo"
                   :placeholder="TEXTOS.login.PLACEHOLDER_USUARIO"
                   type="text"
               />
              <!-- eslint-enable vuejs-accessibility/no-autofocus -->
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
                    v-model="senha"
                    aria-required="true"
                    :autocomplete="showPassword ? 'off' : 'current-password'"
                    :disabled="loginStep > 1"
                    :type="showPassword ? 'text' : 'password'"
                    data-testid="inp-login-senha"
                    name="senha"
                    :placeholder="TEXTOS.login.PLACEHOLDER_SENHA"
                    @keydown="verificarCapsLock"
                    @keyup="verificarCapsLock"
                />
                <template #append>
                  <BButton
                      :aria-label="showPassword ? TEXTOS.login.OCULTAR_SENHA : TEXTOS.login.MOSTRAR_SENHA"
                      :disabled="loginStep > 1"
                      class="text-secondary border-0"
                      variant="link"
                      @click="showPassword = !showPassword"
                  >
                    <i
                        :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"
                        aria-hidden="true"
                    />
                  </BButton>
                </template>
              </BInputGroup>
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

            <BFormGroup
                v-if="loginStep === 2 && perfisUnidadesDisponiveis.length > 1"
                :label="TEXTOS.login.SELECAO_PERFIL"
                label-for="par"
                class="mb-3"
                data-testid="sec-login-perfil"
            >
              <BFormSelect
                  id="par"
                  v-model="parSelecionado"
                  :options="perfisUnidadesOptions"
                  data-testid="sel-login-perfil"
                  text-field="text"
                  value-field="value"
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
            </BFormGroup>

          <LoadingButton
              :loading="isLoading"
              :aria-label="TEXTOS.comum.BOTAO_ENTRAR"
              class="w-100"
              data-testid="btn-login-entrar"
              icon="box-arrow-in-right"
              :loading-text="TEXTOS.login.ENTRANDO"
              :text="TEXTOS.comum.BOTAO_ENTRAR"
              type="submit"
              variant="primary"
          />
          <AppAlert
              v-if="notificacao"
              :dismissible="notificacao.dismissible ?? true"
              :message="notificacao.message"
              :variant="notificacao.variant"
              class="mt-3"
              @dismissed="clear()"
          />
        </BForm>
        </BCard>
      </BCol>
    </BRow>
  </BContainer>
</template>

<script lang="ts" setup>
import {
  BAlert,
  BButton,
  BCard,
  BCol,
  BContainer,
  BForm,
  BFormGroup,
  BFormInput,
  BFormSelect,
  BFormSelectOption,
  BInputGroup,
  BRow,
} from "bootstrap-vue-next";
import {computed, ref, watch} from "vue";
import {useRouter} from "vue-router";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import {logger} from "@/utils";
import {normalizeError} from "@/utils/apiError";
import type {PerfilUnidade} from "@/services/usuarioService";
import {TEXTOS} from "@/constants/textos";

import {usePerfilStore} from "@/stores/perfil";
import {useNotification} from "@/composables/useNotification";

const router = useRouter();
const perfilStore = usePerfilStore();
const {notificacao, notify, clear} = useNotification();

const titulo = ref("");
const senha = ref("");
const loginStep = ref(1);
const parSelecionado = ref<PerfilUnidade | null>(null);
const isLoading = ref(false);
const showPassword = ref(false);
const capsLockAtivado = ref(false);

const perfisUnidadesDisponiveis = computed(() => perfilStore.perfisUnidades);

const perfisUnidadesOptions = computed(() => {
  return perfilStore.perfisUnidades.map((par) => ({
    value: par,
    text: par.perfil === par.unidade.sigla ? par.perfil : `${par.perfil} - ${par.unidade.sigla}`,
  }));
});

watch(perfisUnidadesDisponiveis, (newVal) => {
  if (newVal.length > 0) {
    parSelecionado.value = newVal[0];
  }
});

const verificarCapsLock = (event: KeyboardEvent) => {
  if (event.getModifierState) {
    capsLockAtivado.value = event.getModifierState("CapsLock");
  }
};

const handleLogin = async () => {
  if (loginStep.value === 1) {
    await performInitialLogin();
  } else if (loginStep.value === 2) {
    await performProfileSelection();
  }
};

const performInitialLogin = async () => {
  if (!titulo.value || !senha.value) {
    notify(TEXTOS.login.ERRO_PREENCHIMENTO, 'danger');
    return;
  }

  isLoading.value = true;
  try {
    const sucessoAutenticacao = await perfilStore.loginCompleto(titulo.value, senha.value);

    if (sucessoAutenticacao) {
      await handlePostAuth();
      } else {
        notify(TEXTOS.login.ERRO_CREDENCIAIS, 'danger');
      }
  } catch (error: any) {
    const erroNormalizado = normalizeError(error);
    if (erroNormalizado.kind !== 'notFound' && erroNormalizado.kind !== 'unauthorized') {
      logger.error("Erro no login:", error instanceof Error ? error.message : "Erro desconhecido");
    }

    if (error.response?.status === 404 || error.response?.status === 401) {
      notify(TEXTOS.login.ERRO_CREDENCIAIS, 'danger');
    } else {
      notify(TEXTOS.login.ERRO_GENERICO, 'danger');
    }
  } finally {
    isLoading.value = false;
  }
};

const handlePostAuth = async () => {
  if (perfilStore.perfisUnidades.length > 1) {
    loginStep.value = 2;
  } else if (perfilStore.perfisUnidades.length === 1) {
    await router.push("/painel");
  } else {
    notify(TEXTOS.login.ERRO_SEM_AUTORIZACAO, 'danger');
  }
};

const performProfileSelection = async () => {
  if (parSelecionado.value) {
    isLoading.value = true;
    try {
      await perfilStore.selecionarPerfilUnidade(
          titulo.value,
          parSelecionado.value,
      );
      await router.push("/painel");
    } catch (error) {
      logger.error("Erro ao selecionar perfil:", error);
      notify(TEXTOS.login.ERRO_SELECAO_PERFIL, 'danger');
    } finally {
      isLoading.value = false;
    }
  } else {
    notify(TEXTOS.login.ERRO_POR_FAVOR_SELECIONE, 'danger');
  }
};
</script>
