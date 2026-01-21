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
                Título eleitoral <span class="text-danger" aria-hidden="true">*</span>
              </label>
              <!-- eslint-disable vuejs-accessibility/no-autofocus -->
              <BFormInput
                  id="titulo"
                  v-model="titulo"
                  autocomplete="username"
                  data-testid="inp-login-usuario"
                  :disabled="loginStep > 1"
                  placeholder="Digite seu título"
                  type="text"
                  inputmode="numeric"
                  required
                  autofocus
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
                Senha <span class="text-danger" aria-hidden="true">*</span>
              </label>
              <BInputGroup>
                <BFormInput
                    id="senha"
                    v-model="senha"
                    autocomplete="current-password"
                    data-testid="inp-login-senha"
                    :disabled="loginStep > 1"
                    placeholder="Digite sua senha"
                    :type="showPassword ? 'text' : 'password'"
                    required
                    @keydown="verificarCapsLock"
                    @keyup="verificarCapsLock"
                />
                <template #append>
                  <BButton
                      :aria-label="showPassword ? 'Ocultar senha' : 'Mostrar senha'"
                      :disabled="loginStep > 1"
                      variant="link"
                      class="text-secondary border-0"
                      @click="showPassword = !showPassword"
                  >
                    <i
                        aria-hidden="true"
                        :class="showPassword ? 'bi bi-eye-slash' : 'bi bi-eye'"
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
                  data-testid="sel-login-perfil"
                  :options="perfisUnidadesOptions"
                  text-field="text"
                  value-field="value"
              >
                <template #first>
                  <BFormSelectOption
                      disabled
                      :value="null"
                  >
                    -- Selecione uma opção --
                  </BFormSelectOption>
                </template>
              </BFormSelect>
            </div>

            <BButton
                aria-label="Entrar"
                class="w-100"
                data-testid="btn-login-entrar"
                :disabled="isLoading"
                type="submit"
                variant="primary"
            >
              <BSpinner
                  v-if="isLoading"
                  class="me-2"
                  small
              />
              <i
                  v-else
                  aria-hidden="true"
                  class="bi bi-box-arrow-in-right me-2"
              />
              {{ isLoading ? 'Entrando...' : 'Entrar' }}
            </BButton>
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
  BSpinner
} from "bootstrap-vue-next";
import {computed, ref, watch} from "vue";
import {useRouter} from "vue-router";
import type {PerfilUnidade} from "@/mappers/sgrh";
import {logger} from "@/utils";

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
    if (!titulo.value || !senha.value) {
      feedbackStore.show("Dados incompletos", "Por favor, preencha título e senha.", "danger");
      return;
    }

    isLoading.value = true;
    try {
      const sucessoAutenticacao = await perfilStore.loginCompleto(titulo.value, senha.value);

      if (sucessoAutenticacao) {
        if (perfilStore.perfisUnidades.length > 1) {
          loginStep.value = 2;
          return;
        } else if (perfilStore.perfisUnidades.length === 1) {
          await router.push("/painel");
        } else {
          feedbackStore.show("Perfis indisponíveis", "Nenhum perfil/unidade disponível para este usuário.", "danger");
        }
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
  } else if (loginStep.value === 2) {
    // Step 2: Profile Selection
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
  }
};
</script>
