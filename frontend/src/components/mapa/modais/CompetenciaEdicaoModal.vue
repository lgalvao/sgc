<script lang="ts" setup>
import {BAlert, BFormInvalidFeedback, BFormTextarea} from "bootstrap-vue-next";
import {computed, nextTick, ref, watch} from "vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import CompetenciaAtividadeItem from "./CompetenciaAtividadeItem.vue";
import type {Atividade, Competencia} from "@/types/tipos";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";

const props = defineProps<{
  mostrar: boolean; atividades: Atividade[]; loading?: boolean; competenciaParaEditar?: Competencia | null;
  fieldErrors?: { descricao?: string; atividades?: string; generic?: string; };
}>();

const emit = defineEmits<{ fechar: []; salvar: [c: { descricao: string; atividadesSelecionadas: number[] }]; }>();

const novaComp = ref({descricao: ""});
const selecionadas = ref<number[]>([]);
const editando = ref<Competencia | null>(null);
const inputRef = ref<InstanceType<typeof BFormTextarea> | null>(null);

const {validarSubmissao, resetarValidacao, deveExibirErro, focarPrimeiroErroInvalido} = useValidacaoFormulario();

const mostrarComp = computed({ get: () => props.mostrar, set: (v: boolean) => { if (!v) emit("fechar"); } });
const erroDesc = computed(() => props.fieldErrors?.descricao || (deveExibirErro(!novaComp.value.descricao.trim()) ? "A descrição é obrigatória." : ""));
const erroAtv = computed(() => props.fieldErrors?.atividades || (deveExibirErro(!editando.value && selecionadas.value.length === 0) ? "Selecione ao menos uma atividade." : ""));
const textoAcao = computed(() => editando.value ? "Salvar" : "Criar");

watch(() => props.mostrar, (m) => {
  resetarValidacao();
  if (m && props.competenciaParaEditar) {
    novaComp.value.descricao = props.competenciaParaEditar.descricao;
    selecionadas.value = [...(props.competenciaParaEditar.atividades?.map(a => a.codigo) || [])];
    editando.value = props.competenciaParaEditar;
  } else if (m) {
    novaComp.value.descricao = ""; selecionadas.value = []; editando.value = null;
  }
}, {immediate: true});

function salvar() {
  if (props.loading) return;
  if (!validarSubmissao(novaComp.value.descricao.trim() !== "" && (!!editando.value || selecionadas.value.length > 0))) {
    void focarPrimeiroErroInvalido(); return;
  }
  emit("salvar", { descricao: novaComp.value.descricao, atividadesSelecionadas: selecionadas.value });
}
</script>

<template>
  <ModalPadrao
      v-model="mostrarComp" :loading="loading" :texto-acao-carregando="textoAcao" data-testid="mdl-criar-competencia" tamanho="lg"
      test-codigo-cancelar="btn-criar-competencia-cancelar" test-codigo-confirmar="btn-criar-competencia-salvar"
      :texto-acao="textoAcao" :titulo="editando ? 'Edição de competência' : 'Criação de competência'"
      @confirmar="salvar" @fechar="emit('fechar')" @shown="nextTick(() => inputRef?.$el?.focus())"
  >
    <BAlert v-if="fieldErrors?.generic" :model-value="true" variant="danger" class="mb-4">{{ fieldErrors.generic }}</BAlert>
    <div class="mb-4">
      <h5>Descrição <span aria-hidden="true" class="text-danger">*</span></h5>
      <BFormTextarea id="descricao" ref="inputRef" v-model="novaComp.descricao" :state="erroDesc ? false : null" data-testid="inp-criar-competencia-descricao" placeholder="Descreva a competência" rows="3" />
      <BFormInvalidFeedback :state="erroDesc ? false : null">{{ erroDesc }}</BFormInvalidFeedback>
    </div>
    <div class="mb-4">
      <h5>Atividades <span aria-hidden="true" class="text-danger">*</span></h5>
      <div id="atividades" :class="['d-flex flex-wrap gap-2 p-2 border rounded', { 'border-danger is-invalid': erroAtv }]" tabindex="-1">
        <CompetenciaAtividadeItem v-for="atv in atividades" :key="atv.codigo" v-model="selecionadas" :atividade="atv" :selecionadas="selecionadas" />
      </div>
      <div v-if="erroAtv" class="text-danger small mt-1" data-testid="txt-criar-competencia-pendencia-atividades">{{ erroAtv }}</div>
    </div>
  </ModalPadrao>
</template>
