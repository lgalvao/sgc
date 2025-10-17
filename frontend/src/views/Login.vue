<template>
  <div class="login-bg">
    <div class="login-center-wrapper">
      <div class="card login-card p-4 shadow-lg">
        <h2 class="mb-2 text-center" data-testid="titulo-sgc">SGC</h2>
        <h5 class="mb-4 text-center text-muted" data-testid="subtitulo-sistema">
          Sistema de Gestão de Competências
        </h5>
        <form class="p-0" data-testid="form-login" @submit.prevent="handleLogin">
          <!-- Etapa 1: Credenciais -->
          <div v-if="!perfilStore.loginResponse">
            <div class="mb-3">
              <label class="form-label" for="titulo" data-testid="label-titulo">
                <i class="bi bi-person-circle me-2" />
                Título eleitoral
              </label>
              <input
                id="titulo"
                v-model="titulo"
                autocomplete="username"
                class="form-control"
                placeholder="Digite seu título"
                type="text"
                data-testid="input-titulo"
                :disabled="perfilStore.autenticando"
              />
            </div>
            <div class="mb-3">
              <label class="form-label" for="senha" data-testid="label-senha">
                <i class="bi bi-key me-2" />
                Senha
              </label>
              <input
                id="senha"
                v-model="senha"
                autocomplete="current-password"
                class="form-control"
                placeholder="Digite sua senha"
                type="password"
                data-testid="input-senha"
                :disabled="perfilStore.autenticando"
              />
            </div>
          </div>

          <!-- Etapa 2: Seleção de Perfil/Unidade -->
          <div
            v-if="perfilStore.loginResponse && perfilStore.loginResponse.pares.length > 1"
            class="mb-3"
            data-testid="secao-perfil-unidade"
          >
            <label class="form-label" for="par" data-testid="label-perfil-unidade"
              >Selecione o Perfil e a Unidade</label
            >
            <select
              id="par"
              v-model="parSelecionado"
              class="form-select"
              data-testid="select-perfil-unidade"
              :disabled="perfilStore.autenticando"
            >
              <option
                v-for="par in perfilStore.loginResponse.pares"
                :key="par.perfil + par.unidade"
                :value="par"
              >
                {{ par.perfil }} - {{ par.unidade }}
              </option>
            </select>
          </div>

          <!-- Botão de Ação -->
          <button
            class="btn btn-primary w-100 login-btn"
            type="submit"
            data-testid="botao-entrar"
            :disabled="perfilStore.autenticando"
          >
            <span
              v-if="perfilStore.autenticando"
              class="spinner-border spinner-border-sm me-2"
              role="status"
              aria-hidden="true"
            ></span>
            <i v-else class="bi bi-box-arrow-in-right me-2" />
            {{
              perfilStore.loginResponse && perfilStore.loginResponse.pares.length > 1
                ? 'Confirmar'
                : 'Entrar'
            }}
          </button>

          <!-- Exibição de Erro -->
          <div v-if="perfilStore.erroAutenticacao" class="text-danger mt-3 text-center" data-testid="alerta-erro">
            {{ perfilStore.erroAutenticacao }}
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { usePerfilStore } from '@/stores/perfil'
import { useNotificacoesStore } from '@/stores/notificacoes'
import { Perfil } from '@/types/tipos'

interface PerfilUnidadeDto {
  perfil: Perfil
  unidade: string
}

const router = useRouter()
const perfilStore = usePerfilStore()
const notificacoesStore = useNotificacoesStore()

const titulo = ref(import.meta.env.DEV ? '123456789012' : '') // Título de teste
const senha = ref(import.meta.env.DEV ? 'senha' : '') // Senha de teste
const parSelecionado = ref<PerfilUnidadeDto | null>(null)

// Observa a resposta do login para pré-selecionar o primeiro par
watch(
  () => perfilStore.loginResponse,
  (newResponse) => {
    if (newResponse && newResponse.pares.length > 1) {
      parSelecionado.value = newResponse.pares[0]
    }
  }
)

// Observa o estado de autenticação para redirecionar
watch(
  () => perfilStore.estaAutenticado,
  (autenticado) => {
    if (autenticado) {
      router.push('/painel')
    }
  }
)

const handleLogin = async () => {
  // Se ainda não autenticou, chama a primeira etapa
  if (!perfilStore.loginResponse) {
    if (!titulo.value) {
      perfilStore.erroAutenticacao = 'Por favor, preencha o título eleitoral.';
      return
    }
    try {
      await perfilStore.autenticar({ tituloEleitoral: titulo.value, senha: senha.value })
    } catch {
      // O erro já é tratado no store, apenas evitamos o fluxo de sucesso
    }
  } else {
    // Se já autenticou e precisa escolher um par, chama a segunda etapa
    if (parSelecionado.value) {
      try {
        await perfilStore.entrar(parSelecionado.value)
      } catch {
        // O erro já é tratado no store
      }
    }
  }
}

// Limpa o estado do store de perfil ao montar a página de login
onMounted(() => {
  if (perfilStore.estaAutenticado) {
    perfilStore.logout()
  }
})
</script>

<style scoped>
.login-bg {
  min-height: 100vh;
  width: 100vw;
  background: linear-gradient(
    120deg,
    var(--bs-primary-bg-subtle) 0%,
    var(--bs-info-bg-subtle) 50%,
    var(--bs-light) 100%
  );
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
  border: none;
  background: var(--bs-body-bg);
}

.login-btn {
  transition: background 0.2s;
  font-weight: 500;
}

.login-btn:hover {
  background: var(--bs-primary);
}
</style>