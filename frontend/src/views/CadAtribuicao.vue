<template>
  <div class="container mt-4">
    <h2>Criar atribuição temporária</h2>
    <div class="card mb-4 mt-4">
      <div class="card-body">
        <h5 class="card-title mb-3">
          {{ unidade?.sigla }} - {{ unidade?.nome }}
        </h5>
        <form @submit.prevent="criarAtribuicao">
          <div class="mb-3">
            <label
              class="form-label"
              for="servidor"
            >Servidor</label>
            <select
              id="servidor"
              v-model="servidorSelecionado"
              class="form-select"
              data-testid="select-servidor"
              required
            >
              <option
                :value="null"
                disabled
              >
                Selecione um servidor
              </option>
              <option
                v-for="servidor in servidores"
                :key="servidor.codigo"
                :value="servidor.codigo"
              >
                {{ servidor.nome }}
              </option>
            </select>
            <div
              v-if="erroServidor"
              class="text-danger small mt-1"
            >
              {{ erroServidor }}
            </div>
          </div>

          <div class="mb-3">
            <label
              class="form-label"
              for="dataTermino"
            >Data de término</label>
            <input
              id="dataTermino"
              v-model="dataTermino"
              class="form-control"
              data-testid="input-data-termino"
              required
              type="date"
            >
          </div>

          <div class="mb-3">
            <label
              class="form-label"
              for="justificativa"
            >Justificativa</label>
            <textarea
              id="justificativa"
              v-model="justificativa"
              class="form-control"
              data-testid="textarea-justificativa"
              required
            />
          </div>
          <button
            class="btn btn-primary"
            data-testid="btn-criar-atribuicao"
            type="submit"
          >
            Criar
          </button>
          <button
            class="btn btn-secondary ms-2"
            data-testid="btn-cancelar-atribuicao"
            type="button"
            @click="router.push(`/unidade/${sigla}`)"
          >
            Cancelar
          </button>
        </form>

        <div
          v-if="sucesso"
          class="alert alert-success mt-3"
        >
          Atribuição criada!
        </div>
        <div
          v-if="erroApi"
          class="alert alert-danger mt-3"
        >
          {{ erroApi }}
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { buscarUnidadePorSigla } from '@/services/unidadesService'
import { buscarUsuariosPorUnidade } from '@/services/usuarioService'
import { criarAtribuicaoTemporaria } from '@/services/atribuicaoTemporariaService'
import type { Unidade, Usuario } from '@/types/tipos'

const props = defineProps<{ sigla: string }>()

const router = useRouter()
const sigla = computed(() => props.sigla)

const unidade = ref<Unidade | null>(null)
const servidores = ref<Usuario[]>([])
const servidorSelecionado = ref<string | null>(null)
const dataTermino = ref('')
const justificativa = ref('')

const sucesso = ref(false)
const erroServidor = ref('')
const erroApi = ref('')

onMounted(async () => {
  try {
    unidade.value = await buscarUnidadePorSigla(sigla.value)
    if (unidade.value) {
      servidores.value = await buscarUsuariosPorUnidade(unidade.value.codigo)
    }
  } catch (error) {
    erroServidor.value = 'Falha ao carregar dados da unidade ou servidores.'
    console.error(error)
  }
})

async function criarAtribuicao() {
  if (!unidade.value || !servidorSelecionado.value) {
    return
  }

  erroApi.value = ''
  sucesso.value = false

  try {
    await criarAtribuicaoTemporaria(unidade.value.codigo, {
      tituloEleitoralServidor: servidorSelecionado.value,
      dataTermino: dataTermino.value,
      justificativa: justificativa.value,
    })
    sucesso.value = true
    // Reset form
    servidorSelecionado.value = null
    dataTermino.value = ''
    justificativa.value = ''
  } catch (error) {
    erroApi.value = 'Falha ao criar atribuição. Tente novamente.'
    console.error(error)
  }
}
</script>