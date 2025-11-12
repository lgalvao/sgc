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
                <div
                  v-if="erroServidor"
                  class="text-danger small"
                >
                  {{ erroServidor }}
                </div>
              </option>
            </select>
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
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue'
import {useRouter} from 'vue-router'
const props = defineProps<{ sigla: string }>()

const router = useRouter()
const sigla = computed(() => props.sigla)

const servidorSelecionado = ref<number | null>(null)
const dataTermino = ref("")
const justificativa = ref("")
const sucesso = ref(false)
const erroServidor = ref("")

</script>