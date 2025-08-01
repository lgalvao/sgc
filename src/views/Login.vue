<template>
  <div class="login-bg">
    <div class="login-center-wrapper">
      <div class="card login-card p-4 shadow-lg">
        <h2 class="mb-2 text-center">SGC</h2>
        <h5 class="mb-4 text-center text-muted">Sistema de Gestão de Competências</h5>
        <form class="p-0" @submit.prevent="handleLogin">
          <div class="mb-3">
            <label class="form-label" for="titulo">Título Eleitoral</label>
            <input id="titulo" v-model="titulo" autocomplete="username" class="form-control" type="text"
                   placeholder="Digite seu título" :disabled="loginStep > 1" />
          </div>
          <div class="mb-3">
            <label class="form-label" for="senha">Senha</label>
            <input id="senha" v-model="senha" autocomplete="current-password" class="form-control" type="password"
                   placeholder="Digite sua senha" :disabled="loginStep > 1" />
          </div>

          <div v-if="loginStep === 2 && paresDisponiveis.length > 1" class="mb-3">
            <label class="form-label" for="par">Selecione seu Perfil e Unidade</label>
            <select id="par" v-model="parSelecionado" class="form-select">
              <option v-for="par in paresDisponiveis" :key="par.perfil + par.unidade" :value="par">
                {{ par.perfil }} - {{ par.unidade }}
              </option>
            </select>
          </div>

          <button class="btn btn-primary w-100 login-btn" type="submit">
            {{ loginStep === 1 ? 'Próximo' : 'Entrar' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import {useServidoresStore} from '../stores/servidores'
import {usePerfilStore} from '../stores/perfil'
import {useUnidadesStore} from '../stores/unidades'
import {useAtribuicaoTemporariaStore} from '../stores/atribuicaoTemporaria'
import {usePerfil} from '../composables/usePerfil'

const router = useRouter()
const servidoresStore = useServidoresStore()
const perfilStore = usePerfilStore()
const unidadesStore = useUnidadesStore()
const atribuicaoTemporariaStore = useAtribuicaoTemporariaStore()
const { setPerfil } = usePerfil()

const titulo = ref('1') // Preenchido para teste com Ana Paula Souza
const senha = ref('123') // Preenchido para teste
const loginStep = ref(1)
const servidor = ref(null)
const paresDisponiveis = ref([])
const parSelecionado = ref(null)

const handleLogin = () => {
  if (loginStep.value === 1) {
    if (!titulo.value || !senha.value) {
      alert('Por favor, preencha título e senha.')
      return
    }

    servidor.value = servidoresStore.getServidorById(Number(titulo.value))

    if (servidor.value) {
      paresDisponiveis.value = getPerfisEUnidades(servidor.value.id)

      if (paresDisponiveis.value.length > 1) {
        loginStep.value = 2
        parSelecionado.value = paresDisponiveis.value[0]
      } else if (paresDisponiveis.value.length === 1) {
        finalizarLogin(servidor.value.id, paresDisponiveis.value[0].perfil, paresDisponiveis.value[0].unidade)
      } else {
        alert('Nenhum perfil/unidade disponível para este usuário.')
      }
    } else {
      alert('Usuário não encontrado.')
    }
  } else {
    finalizarLogin(servidor.value.id, parSelecionado.value.perfil, parSelecionado.value.unidade)
  }
}

const finalizarLogin = (servidorId, perfil, unidadeSigla) => {
  perfilStore.setServidorId(servidorId)
  perfilStore.setPerfilUnidade(perfil, unidadeSigla)
  router.push('/painel')
}

const getPerfisEUnidades = (servidorId) => {
  const { servidoresComPerfil } = usePerfil()
  const atribuicoes = atribuicaoTemporariaStore.getAtribuicoesPorServidor(servidorId)

  const paresDisponiveis = []

  const servidorComPerfil = servidoresComPerfil.value.find(s => s.id === servidorId)
  if (servidorComPerfil) {
    const unidadeTitular = unidadesStore.findUnit(servidorComPerfil.unidade)
    if (unidadeTitular) {
      paresDisponiveis.push({ perfil: servidorComPerfil.perfil, unidade: unidadeTitular.sigla })
    }
  }

  atribuicoes.forEach(atrb => {
    const unidade = unidadesStore.findUnit(atrb.unidade)
    if (unidade) {
      // Adiciona apenas se o par perfil-unidade ainda não existe
      const existe = paresDisponiveis.some(p => p.perfil === 'SERVIDOR' && p.unidade === unidade.sigla)
      if (!existe) {
        paresDisponiveis.push({ perfil: 'SERVIDOR', unidade: unidade.sigla })
      }
    }
  })

  return paresDisponiveis
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
