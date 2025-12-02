<template>
  <div class="login-bg">
    <div class="login-center-wrapper">
      <BCard
        class="login-card p-4 shadow-lg"
        no-body
      >
        <h2
          class="mb-2 text-center"
          data-testid="titulo-sgc"
        >
          SGC
        </h2>
        <h5
          class="mb-4 text-center text-muted"
          data-testid="subtitulo-sistema"
        >
          Sistema de Gestão de Competências
        </h5>
        <BForm
          class="p-0"
          data-testid="form-login"
          @submit.prevent="handleLogin"
        >
          <div class="mb-3">
            <label
              class="form-label"
              for="titulo"
              data-testid="label-titulo"
            >
              <i class="bi bi-person-circle me-2" />
              Título eleitoral</label>
            <BFormInput
              id="titulo"
              v-model="titulo"
              :disabled="loginStep > 1"
              autocomplete="username"
              placeholder="Digite seu título"
              type="text"
              data-testid="input-titulo"
            />
          </div>
          <div class="mb-3">
            <label
              class="form-label"
              for="senha"
              data-testid="label-senha"
            >
              <i class="bi bi-key me-2" />
              Senha</label>
            <BFormInput
              id="senha"
              v-model="senha"
              :disabled="loginStep > 1"
              autocomplete="current-password"
              placeholder="Digite sua senha"
              type="password"
              data-testid="input-senha"
            />
          </div>

          <div
            v-if="loginStep === 2 && perfisUnidadesDisponiveis.length > 1"
            class="mb-3"
            data-testid="secao-perfil-unidade"
          >
            <label
              class="form-label"
              for="par"
              data-testid="label-perfil-unidade"
            >Selecione o Perfil e a Unidade</label>
            <BFormSelect
              id="par"
              v-model="parSelecionado"
              :options="perfisUnidadesOptions"
              value-field="value"
              text-field="text"
              data-testid="select-perfil-unidade"
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

          <BButton
            variant="primary"
            class="w-100 login-btn"
            type="submit"
            data-testid="botao-entrar"
            aria-label="Entrar"
          >
            <i class="bi bi-box-arrow-in-right me-2" />
            Entrar
          </BButton>
        </BForm>
      </BCard>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {BButton, BCard, BForm, BFormInput, BFormSelect, BFormSelectOption,} from "bootstrap-vue-next";
import {computed, ref, watch} from "vue";
import {useRouter} from "vue-router";
import type {PerfilUnidade} from "@/mappers/sgrh";

import {usePerfilStore} from "@/stores/perfil";
import {useFeedbackStore} from "@/stores/feedback";

const router = useRouter();
const perfilStore = usePerfilStore();
const feedbackStore = useFeedbackStore();

const titulo = ref(import.meta.env.DEV ? "1" : "");
const senha = ref(import.meta.env.DEV ? "123" : "");
const loginStep = ref(1);
const parSelecionado = ref<PerfilUnidade | null>(null);

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

const handleLogin = async () => {
  if (loginStep.value === 1) {
    if (!titulo.value || !senha.value) {
      feedbackStore.show("Dados incompletos", "Por favor, preencha título e senha.", "danger");
      return;
    }

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
        feedbackStore.show("Falha na autenticação", "Título ou senha inválidos.", "danger");
      }
    } catch (error) {
      console.error("Erro no login:", error);
      feedbackStore.show("Erro no sistema", "Ocorreu um erro ao tentar realizar o login.", "danger");
    }
  } else if (loginStep.value === 2) {
    // Step 2: Profile Selection
    if (parSelecionado.value) {
      try {
        await perfilStore.selecionarPerfilUnidade(
          Number(titulo.value),
          parSelecionado.value,
        );
        await router.push("/painel");
      } catch (error) {
        console.error("Erro ao selecionar perfil:", error);
        feedbackStore.show("Erro", "Falha ao selecionar o perfil.", "danger");
      }
    } else {
      feedbackStore.show("Seleção necessária", "Por favor, selecione um perfil.", "danger");
    }
  }
};
</script>
