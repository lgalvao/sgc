<script lang="ts" setup>
import {BButton, BFormInvalidFeedback, BFormTextarea} from "bootstrap-vue-next";
import {computed, nextTick, ref, watch} from "vue";
import Alerta from "@/components/comum/Alerta.vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import CompetenciaAtividadeItem from "./CompetenciaAtividadeItem.vue";
import type {Atividade, Competencia} from "@/types/tipos";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";

const props = defineProps<{
  mostrar: boolean;
  atividades: Atividade[];
  loading?: boolean;
  competenciaParaEditar?: Competencia | null;
  fieldErrors?: { descricao?: string; atividades?: string; generic?: string; };
}>();

const emit = defineEmits<{ fechar: []; salvar: [c: { descricao: string; atividadesSelecionadas: number[] }]; }>();

const novaComp = ref({descricao: ""});
const selecionadas = ref<number[]>([]);
const editando = ref<Competencia | null>(null);
const inputRef = ref<InstanceType<typeof BFormTextarea> | null>(null);

const {validarSubmissao, resetarValidacao, deveExibirErro, focarPrimeiroErroInvalido} = useValidacaoFormulario();

const mostrarComp = computed({
  get: () => props.mostrar,
  set: (v: boolean) => {
    if (!v) emit("fechar");
  },
});

const erroDesc = computed(() =>
    props.fieldErrors?.descricao || (deveExibirErro(!novaComp.value.descricao.trim()) ? "A descrição é obrigatória." : ""),
);

const erroAtv = computed(() =>
    props.fieldErrors?.atividades || (deveExibirErro(!editando.value && selecionadas.value.length === 0) ? "Selecione ao menos uma atividade." : ""),
);

const textoAcao = computed(() => editando.value ? "Salvar" : "Criar");

watch(() => props.mostrar, (m) => {
  resetarValidacao();
  if (m && props.competenciaParaEditar) {
    novaComp.value.descricao = props.competenciaParaEditar.descricao;
    selecionadas.value = [...(props.competenciaParaEditar.atividades?.map(a => a.codigo) || [])];
    editando.value = props.competenciaParaEditar;
  } else if (m) {
    novaComp.value.descricao = "";
    selecionadas.value = [];
    editando.value = null;
  }
}, {immediate: true});

function salvar() {
  if (props.loading) return;
  if (!validarSubmissao(novaComp.value.descricao.trim() !== "" && (!!editando.value || selecionadas.value.length > 0))) {
    void focarPrimeiroErroInvalido();
    return;
  }
  emit("salvar", {descricao: novaComp.value.descricao, atividadesSelecionadas: selecionadas.value});
}

function selecionarTodasAtividades() {
  selecionadas.value = props.atividades.map((atv) => atv.codigo);
}

function limparSelecaoAtividades() {
  selecionadas.value = [];
}
</script>

<template>
  <ModalPadrao
      v-model="mostrarComp"
      :loading="loading"
      :texto-acao="textoAcao"
      :texto-acao-carregando="textoAcao"
      :titulo="editando ? 'Edição de competência' : 'Criação de competência'"
      data-testid="mdl-criar-competencia"
      tamanho="lg"
      test-id-cancelar="btn-criar-competencia-cancelar"
      test-id-confirmar="btn-criar-competencia-salvar"
      @confirmar="salvar"
      @fechar="emit('fechar')"
      @shown="nextTick(() => inputRef?.$el?.focus())"
  >
    <template #alerta>
      <Alerta
          :mensagem="fieldErrors?.generic"
          data-testid="alert-criar-competencia-erro"
      />
    </template>
    <div class="mb-4">
      <h5>Descrição <span aria-hidden="true" class="text-danger">*</span></h5>
      <BFormTextarea
          id="descricao"
          ref="inputRef"
          v-model="novaComp.descricao"
          :state="erroDesc ? false : null"
          data-testid="inp-criar-competencia-descricao"
          placeholder="Descreva a competência"
          rows="3"
      />
      <BFormInvalidFeedback :state="erroDesc ? false : null">{{ erroDesc }}</BFormInvalidFeedback>
    </div>
    <div class="mb-4">
      <h5>Atividades <span aria-hidden="true" class="text-danger">*</span></h5>
      <div class="d-flex gap-2 mb-2">
        <BButton
            aria-label="Selecionar todas as atividades"
            class="btn-acao-sutil"
            data-testid="btn-competencia-selecionar-todas-atividades"
            size="sm"
            title="Selecionar todas"
            variant="outline-secondary"
            @click="selecionarTodasAtividades"
        >
          <i aria-hidden="true" class="bi bi-check-all"/>
        </BButton>
        <BButton
            aria-label="Desmarcar todas as atividades"
            class="btn-acao-sutil"
            data-testid="btn-competencia-limpar-selecao-atividades"
            size="sm"
            title="Limpar seleção"
            variant="outline-secondary"
            @click="limparSelecaoAtividades"
        >
          <i aria-hidden="true" class="bi bi-x-lg"/>
        </BButton>
      </div>
      <div
          id="atividades"
          :class="['lista-atividades py-1 px-0', { 'border border-danger is-invalid rounded p-2': erroAtv }]"
          tabindex="-1"
      >
        <CompetenciaAtividadeItem
            v-for="atv in atividades"
            :key="atv.codigo"
            v-model="selecionadas"
            :atividade="atv"
            :selecionadas="selecionadas"
        />
      </div>
      <div v-if="erroAtv" class="text-danger small mt-1" data-testid="txt-criar-competencia-pendencia-atividades">
        {{ erroAtv }}
      </div>
    </div>
  </ModalPadrao>
</template>

<style scoped>
.lista-atividades {
  max-height: 24rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.btn-acao-sutil i {
  font-size: 1.1rem;
}
</style>
