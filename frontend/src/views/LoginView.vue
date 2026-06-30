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
              @submit.prevent="aoLogar"
          >
            <LoginCredenciaisCampos
                :caps-lock-ativado="capsLockAtivado"
                :carregando="carregando"
                :login-bloqueado="selecionandoPerfil"
                :mostrar-acao-trocar-titulo-eleitoral="selecionandoPerfil"
                :mensagem-erro-senha="mensagemErroSenha"
                :mensagem-erro-titulo="mensagemErroTitulo"
                :senha="senha"
                :show-password="showPassword"
                :titulo="titulo"
                @trocar-titulo-eleitoral="trocarTituloEleitoral"
                @toggle-senha="alternarVisibilidadeSenha"
                @update:senha="senha = $event"
                @update:titulo="titulo = $event"
                @verificar-caps-lock="verificarCapsLock"
            />

            <Transition name="login-autorizacao">
              <div
                  v-if="mostrarBlocoAutorizacao"
                  class="login-autorizacao-bloco"
              >
                <LoginPerfilSelect
                    :mensagem-erro-perfil="mensagemErroPerfil"
                    :mostrar="true"
                    :par-selecionado="parSelecionado"
                    :perfis-unidades-options="perfisUnidadesOptions"
                    @update:par-selecionado="parSelecionado = $event"
                />
              </div>
            </Transition>

            <LoadingButton
                :aria-label="TEXTOS.comum.BOTAO_ENTRAR"
                :loading="carregando"
                :loading-text="TEXTOS.login.ENTRANDO"
                :text="TEXTOS.comum.BOTAO_ENTRAR"
                class="w-100"
                data-testid="btn-login-entrar"
                icon="box-arrow-in-right"
                type="submit"
                variant="primary"
            />
            <Alerta
                v-if="notificacao"
                :chave="notificacao.chave"
                :dispensavel="notificacao.dispensavel ?? true"
                :mensagem="notificacao.mensagem"
                :variante="notificacao.variante"
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
import {BCard, BCol, BContainer, BForm, BRow,} from "bootstrap-vue-next";
import {computed, ref, watch} from "vue";
import {useRouter} from "vue-router";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import Alerta from "@/components/comum/Alerta.vue";
import LoginCredenciaisCampos from "@/components/login/LoginCredenciaisCampos.vue";
import LoginPerfilSelect from "@/components/login/LoginPerfilSelect.vue";
import {logger} from "@/utils";
import {normalizarErro} from "@/utils/apiError";
import type {PerfilUnidade} from "@/types/autenticacao";
import {TEXTOS} from "@/constants/textos";

import {usePerfilStore} from "@/stores/perfil";
import {useNotification} from "@/composables/useNotification";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";

const router = useRouter();
const perfilStore = usePerfilStore();
const {notificacao, notify, clear} = useNotification();

const titulo = ref("");
const senha = ref("");
const loginStep = ref(1);
const parSelecionado = ref<PerfilUnidade | null>(null);
const carregando = ref(false);
const showPassword = ref(false);
const capsLockAtivado = ref(false);

const {
  validarSubmissao,
  resetarValidacao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const selecionandoPerfil = computed(() => loginStep.value > 1);
const mostrarBlocoAutorizacao = computed(() => selecionandoPerfil.value && perfisUnidadesDisponiveis.value.length > 1);
const credenciaisPreenchidas = computed(() => Boolean(titulo.value && senha.value));
const mensagemErroTitulo = computed(() => deveExibirErro(!titulo.value) ? TEXTOS.login.ERRO_CAMPO_TITULO : "");
const mensagemErroSenha = computed(() => deveExibirErro(!senha.value) ? TEXTOS.login.ERRO_CAMPO_SENHA : "");
const mensagemErroPerfil = computed(() =>
    deveExibirErro(!parSelecionado.value) ? TEXTOS.login.ERRO_POR_FAVOR_SELECIONE : ""
);

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

function alternarVisibilidadeSenha() {
  showPassword.value = !showPassword.value;
}

function trocarTituloEleitoral() {
  perfilStore.cancelarFluxoLogin();
  loginStep.value = 1;
  parSelecionado.value = null;
  senha.value = "";
  capsLockAtivado.value = false;
  clear();
  resetarValidacao();
}

const aoLogar = async () => {
  if (!selecionandoPerfil.value) {
    await performInitialLogin();
  } else {
    await performProfileSelection();
  }
};

const performInitialLogin = async () => {
  if (!validarSubmissao(credenciaisPreenchidas.value)) {
    await focarPrimeiroErroInvalido();
    return;
  }

  carregando.value = true;
  try {
    const fluxoLogin = await perfilStore.iniciarLogin(titulo.value, senha.value);

    if (fluxoLogin.autenticado) {
      resetarValidacao();
      await aposAutenticacao();
    } else {
      notify(TEXTOS.login.ERRO_CREDENCIAIS, 'danger');
    }
  } catch (error: unknown) {
    const erroNormalizado = normalizarErro(error);
    if (erroNormalizado.tipo === 'inesperado') {
      logger.error("Erro interno no login:", erroNormalizado.mensagem);
      await router.push("/erro");
      return;
    }
    if (erroNormalizado.tipo !== 'naoEncontrado' && erroNormalizado.tipo !== 'naoAutorizado') {
      logger.error("Erro no login:", error instanceof Error ? error.message : "Erro desconhecido");
    }

    if (erroNormalizado.tipo === 'naoEncontrado' || erroNormalizado.tipo === 'naoAutorizado') {
      notify(TEXTOS.login.ERRO_CREDENCIAIS, 'danger');
    } else {
      notify(TEXTOS.login.ERRO_GENERICO, 'danger');
    }
  } finally {
    carregando.value = false;
  }
};

const aposAutenticacao = async () => {
  if (perfilStore.perfisUnidades.length > 1) {
    loginStep.value = 2;
  } else if (perfilStore.perfisUnidades.length === 1) {
    await router.push("/painel");
  } else {
    notify(TEXTOS.login.ERRO_SEM_AUTORIZACAO, 'danger');
  }
};

const performProfileSelection = async () => {
  if (!validarSubmissao(!!parSelecionado.value)) {
    await focarPrimeiroErroInvalido();
    return;
  }

  carregando.value = true;
  try {
    if (parSelecionado.value) {
      await perfilStore.concluirLoginComPerfil(parSelecionado.value);
      await router.push("/painel");
    } else {
      logger.error("Erro interno ao selecionar perfil: Perfil não selecionado");
      notify(TEXTOS.login.ERRO_GENERICO, 'danger');
    }
  } catch (error) {
    const erroNormalizado = normalizarErro(error);
    if (erroNormalizado.tipo === 'inesperado') {
      logger.error("Erro interno ao selecionar perfil:", erroNormalizado.mensagem);
      await router.push("/erro");
      return;
    }
    logger.error("Erro ao selecionar perfil:", error);
    notify(TEXTOS.login.ERRO_SELECAO_PERFIL, 'danger');
  } finally {
    carregando.value = false;
  }
};
</script>

<style scoped>
.login-autorizacao-bloco {
  overflow: hidden;
}

.login-autorizacao-enter-active,
.login-autorizacao-leave-active {
  transition: max-height 0.28s ease,
  opacity 0.22s ease,
  transform 0.22s ease,
  margin-bottom 0.22s ease;
}

.login-autorizacao-enter-from,
.login-autorizacao-leave-to {
  max-height: 0;
  opacity: 0;
  transform: translateY(-0.35rem);
  margin-bottom: 0;
}

.login-autorizacao-enter-to,
.login-autorizacao-leave-from {
  max-height: 14rem;
  opacity: 1;
  transform: translateY(0);
  margin-bottom: 0;
}
</style>
