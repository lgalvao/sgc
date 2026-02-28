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
            SGC
          </h1>
          <p
              class="h5 mb-4 text-center text-muted"
              data-testid="txt-login-subtitulo"
          >
            Sistema de Gestão de Competências
          </p>
          <BForm
              class="p-0"
              data-testid="form-login"
              @submit.prevent="handleLogin"
          >
            <div class="mb-3">
              <label
                  class="form-label"
                  data-testid="lbl-login-usuario"
                  for="titulo"
              >
                <i
                    aria-hidden="true"
                    class="bi bi-person-circle me-2"
                />
                Título eleitoral <span aria-hidden="true" class="text-danger">*</span>
              </label>
              <!-- eslint-disable vuejs-accessibility/no-autofocus -->
              <BFormInput
                  id="titulo"
                  v-model="titulo"
                  :disabled="loginStep > 1"
                  autocomplete="username"
                  autofocus
                  data-testid="inp-login-usuario"
                  inputmode="numeric"
                  name="titulo"
                  placeholder="Digite seu título"
                  required
                  type="text"
              />
              <!-- eslint-enable vuejs-accessibility/no-autofocus -->
            </div>
            <div class="mb-3">
              <label
                  class="form-label"
                  data-testid="lbl-login-senha"
                  for="senha"
              >
                <i
                    aria-hidden="true"
                    class="bi bi-key me-2"
                />
                Senha <span aria-hidden="true" class="text-danger">*</span>
              </label>
              <BInputGroup>
                <BFormInput
                    id="senha"
                    v-model="senha"
                    :autocomplete="showPassword ? 'off' : 'current-password'"
                    :disabled="loginStep > 1"
                    :type="showPassword ? 'text' : 'password'"
                    data-testid="inp-login-senha"
                    name="senha"
                    placeholder="Digite sua senha"
                    required
                    @keydown="verificarCapsLock"
                    @keyup="verificarCapsLock"
                />
                <template #append>
                  <BButton
                      :aria-label="showPassword ? 'Ocultar senha' : 'Mostrar senha'"
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
              <div
                  v-if="capsLockAtivado"
                  class="text-warning small mt-1 d-flex align-items-center"
                  data-testid="alert-caps-lock"
                  role="alert"
              >
                <i
                    aria-hidden="true"
                    class="bi bi-exclamation-triangle-fill me-1"
                />
                Caps Lock ativado
              </div>
            </div>

            <div
                v-if="loginStep === 2 && perfisUnidadesDisponiveis.length > 1"
                class="mb-3"
                data-testid="sec-login-perfil"
            >
              <label
                  class="form-label"
                  data-testid="lbl-login-perfil"
                  for="par"
              >
                Selecione o perfil e a unidade
              </label>
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
                    -- Selecione uma opção --
                  </BFormSelectOption>
                </template>
              </BFormSelect>
            </div>

            <LoadingButton
                :loading="isLoading"
                aria-label="Entrar"
                class="w-100"
                data-testid="btn-login-entrar"
                icon="box-arrow-in-right"
                loading-text="Entrando..."
                text="Entrar"
                type="submit"
                variant="primary"
            />
          </BForm>
        </BCard>
      </BCol>
    </BRow>
  </BContainer>
</template>

<script lang="ts" setup>
import {
  BButton,
  BCard,
  BCol,
  BContainer,
  BForm,
  BFormInput,
  BFormSelect,
  BFormSelectOption,
  BInputGroup,
  BRow,
} from "bootstrap-vue-next";
import {computed, ref, watch} from "vue";
import {useRouter} from "vue-router";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import {logger} from "@/utils";
import type {PerfilUnidade} from "@/services/usuarioService";

import {usePerfilStore} from "@/stores/perfil";
import {useFeedbackStore} from "@/stores/feedback";

const router = useRouter();
const perfilStore = usePerfilStore();
const feedbackStore = useFeedbackStore();

const titulo = ref(import.meta.env.DEV ? "1" : "");
const senha = ref(import.meta.env.DEV ? "123" : "");
const loginStep = ref(1);
const parSelecionado = ref<PerfilUnidade | null>(null);
const isLoading = ref(false);
const showPassword = ref(false);
const capsLockAtivado = ref(false);

const perfisUnidadesDisponiveis = computed(() => perfilStore.perfisUnidades);

const perfisUnidadesOptions = computed(() => {
  return perfilStore.perfisUnidades.map((par) => ({
    value: par,
    text: `${par.perfil} - ${par.unidade.sigla}`,
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
    feedbackStore.show("Dados incompletos", "Por favor, preencha título e senha.", "danger");
    return;
  }

  isLoading.value = true;
  try {
    const sucessoAutenticacao = await perfilStore.loginCompleto(titulo.value, senha.value);

    if (sucessoAutenticacao) {
      await handlePostAuth();
    } else {
      feedbackStore.show("Erro no login", "Título ou senha inválidos.", "danger");
    }
  } catch (error: any) {
    logger.error("Erro no login:", error);
    if (error.response?.status === 404 || error.response?.status === 401) {
      feedbackStore.show("Erro no login", "Título ou senha inválidos.", "danger");
    } else {
      feedbackStore.show("Erro no sistema", "Ocorreu um erro ao tentar realizar o login.", "danger");
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
    feedbackStore.show("Perfis indisponíveis", "Nenhum perfil/unidade disponível para este usuário.", "danger");
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
      feedbackStore.show("Erro", "Falha ao selecionar o perfil.", "danger");
    } finally {
      isLoading.value = false;
    }
  } else {
    feedbackStore.show("Seleção necessária", "Por favor, selecione um perfil.", "danger");
  }
};
</script>
