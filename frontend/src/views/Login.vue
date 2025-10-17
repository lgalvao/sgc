<template>
  <div class="login-bg">
    <div class="login-center-wrapper">
      <div class="card login-card p-4 shadow-lg">
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
        <form
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
            <input
              id="titulo"
              v-model="titulo"
              :disabled="loginStep > 1"
              autocomplete="username"
              class="form-control"
              placeholder="Digite seu título"
              type="text"
              data-testid="input-titulo"
            >
          </div>
          <div class="mb-3">
            <label
              class="form-label"
              for="senha"
              data-testid="label-senha"
            >
              <i class="bi bi-key me-2" />
              Senha</label>
            <input
              id="senha"
              v-model="senha"
              :disabled="loginStep > 1"
              autocomplete="current-password"
              class="form-control"
              placeholder="Digite sua senha"
              type="password"
              data-testid="input-senha"
            >
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
            <select
              id="par"
              v-model="parSelecionado"
              class="form-select"
              data-testid="select-perfil-unidade"
            >
              <option
                v-for="par in perfilStore.perfisUnidades"
                :key="par.perfil + par.unidade.sigla"
                :value="par"
              >
                {{ par.perfil }} - {{ par.unidade.sigla }}
              </option>
            </select>
          </div>

          <button
            class="btn btn-primary w-100 login-btn"
            type="submit"
            data-testid="botao-entrar"
          >
            <i class="bi bi-box-arrow-in-right me-2" />
            Entrar
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, ref, watch} from 'vue'
import {useRouter} from 'vue-router'
import {usePerfilStore} from '@/stores/perfil'
import {useNotificacoesStore} from '@/stores/notificacoes'
import {PerfilUnidade} from '../mappers/sgrh';

const router = useRouter()
const perfilStore = usePerfilStore()
const notificacoesStore = useNotificacoesStore()

const titulo = ref(import.meta.env.DEV ? '1' : '')
const senha = ref(import.meta.env.DEV ? '123' : '')
const loginStep = ref(1)
const parSelecionado = ref<PerfilUnidade | null>(null)

const perfisUnidadesDisponiveis = computed(() => perfilStore.perfisUnidades);

watch(perfisUnidadesDisponiveis, (newVal) => {
  if (newVal.length > 0) {
    parSelecionado.value = newVal[0];
  }
});

const handleLogin = async () => {
  if (loginStep.value === 1) {
    if (!titulo.value || !senha.value) {
      notificacoesStore.erro(
        'Dados incompletos',
        'Por favor, preencha título e senha.'
      );
      return
    }

    const sucessoAutenticacao = await perfilStore.loginCompleto(titulo.value, senha.value);

    if (sucessoAutenticacao) {
      if (perfilStore.perfisUnidades.length > 1) {
        loginStep.value = 2;
      } else if (perfilStore.perfisUnidades.length === 1) {
        router.push('/painel');
      } else {
        notificacoesStore.erro(
          'Perfis indisponíveis',
          'Nenhum perfil/unidade disponível para este usuário.'
        );
      }
    } else {
      notificacoesStore.erro(
        'Falha na autenticação',
        'Título ou senha inválidos.'
      );
    }
  } else {
    if (parSelecionado.value) {
      await perfilStore.selecionarPerfilUnidade(Number(titulo.value), parSelecionado.value);
      router.push('/painel');
    }
  }
}
</script>
