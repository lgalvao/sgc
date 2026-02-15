<template>
  <LayoutPadrao>
    <PageHeader title="Conclusão do Diagnóstico" />
    <div class="row justify-content-center">
      <div class="col-md-8">
        <BCard class="shadow-sm">
          <div v-if="loading" class="text-center py-5">
            <BSpinner label="Carregando..."/>
          </div>

          <div v-else>
            <div v-if="!diagnostico?.podeSerConcluido" class="alert alert-danger mb-4">
              <i aria-hidden="true" class="bi bi-exclamation-triangle-fill me-2"/>
              {{ diagnostico?.motivoNaoPodeConcluir }}
              <hr>
              <p class="mb-0">Para concluir com pendências, é obrigatório fornecer uma justificativa abaixo.</p>
            </div>

            <div v-else class="alert alert-success mb-4">
              <i aria-hidden="true" class="bi bi-check-circle-fill me-2"/>
              O diagnóstico está completo e pronto para ser concluído.
            </div>

            <BFormGroup
                class="mb-4"
                label="Justificativa / Comentários Finais:"
                label-for="justificativa"
            >
              <BFormTextarea
                  id="justificativa"
                  v-model="justificativa"
                  :state="justificativaValida"
                  placeholder="Insira observações relevantes para a validação superior..."
                  rows="4"
              />
              <BFormInvalidFeedback>
                A justificativa é obrigatória quando existem pendências.
              </BFormInvalidFeedback>
            </BFormGroup>

            <div class="d-flex justify-content-end gap-2">
              <BButton to="/painel" variant="outline-secondary">Cancelar</BButton>
              <BButton
                  :disabled="!botaoHabilitado"
                  data-testid="btn-confirmar-conclusao"
                  variant="success"
                  @click="concluir"
              >
                Confirmar Conclusão
              </BButton>
            </div>
          </div>
        </BCard>
      </div>
    </div>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {
  BButton,
  BCard,
  BFormGroup,
  BFormInvalidFeedback,
  BFormTextarea,
  BSpinner
} from 'bootstrap-vue-next';
import PageHeader from '@/components/layout/PageHeader.vue';
import {useFeedbackStore} from '@/stores/feedback';
import {useDiagnosticosStore} from '@/stores/diagnosticos';
import type {DiagnosticoDto} from '@/services/diagnosticoService';

const route = useRoute();
const router = useRouter();
const feedbackStore = useFeedbackStore();
const diagnosticosStore = useDiagnosticosStore();

const loading = ref(true);
const codSubprocesso = computed(() => Number(route.params.codSubprocesso));
const diagnostico = computed<DiagnosticoDto | null>(() => diagnosticosStore.diagnostico);
const justificativa = ref('');

const justificativaValida = computed(() => {
  if (diagnostico.value?.podeSerConcluido) return null; // Não valida se opcional
  return justificativa.value.trim().length > 10;
});

const botaoHabilitado = computed(() => {
  if (diagnostico.value?.podeSerConcluido) return true;
  return justificativaValida.value === true;
});

onMounted(async () => {
  try {
    loading.value = true;
    await diagnosticosStore.buscarDiagnostico(codSubprocesso.value);
    if (diagnosticosStore.diagnostico?.situacao === 'CONCLUIDO') {
      feedbackStore.show('Aviso', 'Este diagnóstico já foi concluído.', 'warning');
      await router.push('/painel');
    }
  } catch (error) {
    feedbackStore.show('Erro', 'Erro ao carregar dados: ' + error, 'danger');
  } finally {
    loading.value = false;
  }
});

async function concluir() {
  try {
    await diagnosticosStore.concluirDiagnostico(codSubprocesso.value, justificativa.value);
    feedbackStore.show('Sucesso', 'Diagnóstico da unidade concluído com sucesso!', 'success');
    await router.push('/painel');
  } catch (error: any) {
    feedbackStore.show('Erro', error.response?.data?.message || 'Erro ao concluir.', 'danger');
  }
}
</script>
