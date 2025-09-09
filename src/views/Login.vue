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
            >Título eleitoral</label>
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
            >Senha</label>
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
              v-if="loginStep === 2 && paresDisponiveis.length > 1"
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
                  v-for="par in paresDisponiveis"
                  :key="par.perfil + par.unidade"
                  :value="par"
              >
                {{ par.perfil }} - {{ par.unidade }}
              </option>
            </select>
          </div>

          <button
              class="btn btn-primary w-100 login-btn"
              type="submit"
              data-testid="botao-entrar"
          >
            Entrar
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {useServidoresStore} from '@/stores/servidores'
import {usePerfilStore} from '@/stores/perfil'
import {usePerfil} from '@/composables/usePerfil'
import {useNotificacoesStore} from '@/stores/notificacoes'
import {Perfil, Servidor} from '@/types/tipos';

interface Par {
  perfil: Perfil;
  unidade: string;
}

const router = useRouter()
const servidoresStore = useServidoresStore()
const perfilStore = usePerfilStore()
const notificacoesStore = useNotificacoesStore()
const {getPerfisDoServidor} = usePerfil()

// Credenciais de teste - remover em produção
const titulo = ref(import.meta.env.DEV ? '1' : '')
const senha = ref(import.meta.env.DEV ? '123' : '')
const loginStep = ref(1)
const servidor = ref<Servidor | null | undefined>(null)
const paresDisponiveis = ref<Par[]>([])
const parSelecionado = ref<Par | null>(null)

const handleLogin = () => {
  if (loginStep.value === 1) {
    if (!titulo.value || !senha.value) {
      notificacoesStore.erro(
        'Dados incompletos',
        'Por favor, preencha título e senha.'
      );
      return
    }

    servidor.value = servidoresStore.getServidorById(Number(titulo.value))

    if (servidor.value) {
      paresDisponiveis.value = getPerfisDoServidor(servidor.value.id)

      if (paresDisponiveis.value.length > 1) {
        loginStep.value = 2
        parSelecionado.value = paresDisponiveis.value[0]
      } else if (paresDisponiveis.value.length === 1) {
        finalizarLogin(servidor.value.id, paresDisponiveis.value[0].perfil, paresDisponiveis.value[0].unidade)
      } else {
        notificacoesStore.erro(
          'Perfis indisponíveis',
          'Nenhum perfil/unidade disponível para este usuário.'
        );
      }
    } else {
      notificacoesStore.erro(
        'Usuário não encontrado',
        'Verifique se o título eleitoral está correto.'
      );
    }
  } else {
    if (servidor.value && parSelecionado.value) {
      finalizarLogin(servidor.value.id, parSelecionado.value.perfil, parSelecionado.value.unidade)
    }
  }
}

const finalizarLogin = (idServidor: number, perfil: Perfil, unidadeSigla: string) => {
  perfilStore.setServidorId(idServidor)
  perfilStore.setPerfilUnidade(perfil, unidadeSigla)
  router.push('/painel')
}

</script>

<style scoped>
.login-bg {
  min-height: 100vh;
  width: 100vw;
  background: linear-gradient(120deg, #e3f0ff 0%, #c9e7ff 50%, #f6faff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-center-wrapper {
  width: 100vw;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 400px;
  border-radius: 22px;
  border: none;
  background: #fff;
  box-shadow: 0 8px 40px 0 rgba(0, 0, 0, 0.13);
}

.login-btn {
  font-size: 1.15rem;
  padding: 0.75rem 0;
  border-radius: 8px;
  transition: background 0.2s;
  font-weight: 500;
}

.login-btn:hover {
  background: #1769e0;
}

.card {
  box-shadow: 0 6px 32px 0 rgba(0, 0, 0, 0.10);
}
</style>
