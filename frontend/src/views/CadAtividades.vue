<template>
  <BContainer class="mt-4">
    <div class="d-flex align-items-center mb-3">
      <BButton
          aria-label="Voltar"
          class="p-0 me-3 text-decoration-none"
          data-testid="btn-cad-atividades-voltar"
          title="Voltar"
          variant="link"
          @click="router.back()"
      >
        <i aria-hidden="true" class="bi bi-arrow-left fs-4"/>
      </BButton>
      <div class="fs-5 d-flex align-items-center gap-2">
        <span>{{ sigla }} - {{ nomeUnidade }}</span>
        <span
            v-if="subprocesso"
            :class="badgeClass(subprocesso.situacao)"
            class="badge fs-6"
            data-testid="cad-atividades__txt-badge-situacao"
        >{{ subprocesso.situacaoLabel || situacaoLabel(subprocesso.situacao) }}</span>
      </div>
    </div>

    <div class="d-flex justify-content-between align-items-center mb-3">
      <h1 class="mb-0 display-6">
        Atividades e conhecimentos
      </h1>

      <div class="d-flex gap-2">
        <BDropdown
            v-if="codSubprocesso && (podeVerImpacto || isChefe)"
            data-testid="btn-mais-acoes"
            text="Mais ações"
            variant="outline-secondary"
            class="me-2"
        >
          <BDropdownItem
              v-if="podeVerImpacto"
              data-testid="cad-atividades__btn-impactos-mapa"
              @click="abrirModalImpacto"
          >
            <i class="bi bi-arrow-right-circle me-2"/> Impacto no mapa
          </BDropdownItem>
          <BDropdownItem
              v-if="isChefe"
              data-testid="btn-cad-atividades-historico"
              @click="abrirModalHistorico"
          >
            <i class="bi bi-clock-history me-2"/> Histórico de análise
          </BDropdownItem>
          <BDropdownItem
              v-if="isChefe"
              data-testid="btn-cad-atividades-importar"
              @click="mostrarModalImportar = true"
          >
            <i class="bi bi-upload me-2"/> Importar atividades
          </BDropdownItem>
        </BDropdown>

        <BButton
            v-if="!!permissoes?.podeDisponibilizarCadastro"
            :disabled="loadingValidacao"
            data-testid="btn-cad-atividades-disponibilizar"
            title="Disponibilizar"
            variant="success"
            @click="disponibilizarCadastro"
        >
          <span
              v-if="loadingValidacao"
              aria-hidden="true"
              class="spinner-border spinner-border-sm me-1"
              role="status"
          />
          <i
              v-else
              class="bi bi-check-lg me-1"
          />
          {{ loadingValidacao ? 'Validando...' : 'Disponibilizar' }}
        </BButton>
      </div>
    </div>

    <BAlert
        v-if="erroGlobal"
        :model-value="true"
        class="mb-4"
        dismissible
        variant="danger"
        @dismissed="erroGlobal = null"
    >
      {{ erroGlobal }}
    </BAlert>

    <BForm
        class="row g-2 align-items-center mb-4"
        data-testid="form-nova-atividade"
        @submit.prevent="adicionarAtividade"
    >
      <BCol>
        <BFormInput
            ref="inputNovaAtividadeRef"
            v-model="novaAtividade"
            aria-label="Nova atividade"
            data-testid="inp-nova-atividade"
            :disabled="loadingAdicionar"
            placeholder="Nova atividade"
            type="text"
            required
        />
      </BCol>
      <BCol cols="auto">
        <BButton
            aria-label="Adicionar atividade"
            :disabled="!codSubprocesso || !permissoes?.podeEditarMapa || loadingAdicionar || !novaAtividade.trim()"
            data-testid="btn-adicionar-atividade"
            size="sm"
            title="Adicionar atividade"
            type="submit"
            variant="outline-primary"
        >
          <span
              v-if="loadingAdicionar"
              class="spinner-border spinner-border-sm"
              role="status"
              aria-hidden="true"
          />
          <i
              v-else
              aria-hidden="true"
              class="bi bi-plus-lg"
          />
        </BButton>
      </BCol>
    </BForm>

    <!-- Empty State -->
    <EmptyState
        v-if="atividades.length === 0"
        icon="bi-list-check"
        title="Lista de atividades"
        :description="`Não há atividades cadastradas. Utilize o campo acima para adicionar uma nova atividade${isChefe ? ' ou importe de outro processo' : ''}.`"
        data-testid="cad-atividades-empty-state"
    >
      <BButton
          v-if="isChefe"
          variant="outline-primary"
          size="sm"
          data-testid="btn-empty-state-importar"
          @click="mostrarModalImportar = true"
      >
        <i class="bi bi-upload me-2"/> Importar atividades
      </BButton>
    </EmptyState>

    <div
        v-for="(atividade, idx) in atividades"
        :key="atividade.codigo || idx"
        :ref="el => setAtividadeRef(atividade.codigo, el)"
    >
      <AtividadeItem
          :atividade="atividade"
          :pode-editar="!!permissoes?.podeEditarMapa"
          :erro-validacao="obterErroParaAtividade(atividade.codigo)"
          @atualizar-atividade="(desc) => salvarEdicaoAtividade(atividade.codigo, desc)"
          @remover-atividade="() => removerAtividade(idx)"
          @adicionar-conhecimento="(desc) => adicionarConhecimento(idx, desc)"
          @atualizar-conhecimento="(idC, desc) => salvarEdicaoConhecimento(atividade.codigo, idC, desc)"
          @remover-conhecimento="(idC) => removerConhecimento(idx, idC)"
      />
    </div>

    <ImportarAtividadesModal
        :cod-subprocesso-destino="codSubprocesso"
        :mostrar="mostrarModalImportar"
        @fechar="mostrarModalImportar = false"
        @importar="handleImportAtividades"
    />

    <ImpactoMapaModal
        v-if="codSubprocesso"
        :impacto="impactoMapa"
        :loading="loadingImpacto"
        :mostrar="mostrarModalImpacto"
        @fechar="fecharModalImpacto"
    />

    <ConfirmacaoDisponibilizacaoModal
        :mostrar="mostrarModalConfirmacao"
        :is-revisao="!!isRevisao"
        @fechar="mostrarModalConfirmacao = false"
        @confirmar="confirmarDisponibilizacao"
    />

    <HistoricoAnaliseModal
        :historico="historicoAnalises"
        :mostrar="mostrarModalHistorico"
        @fechar="mostrarModalHistorico = false"
    />

    <ModalConfirmacao
        v-model="mostrarModalConfirmacaoRemocao"
        :titulo="dadosRemocao?.tipo === 'atividade' ? 'Remover Atividade' : 'Remover Conhecimento'"
        :mensagem="dadosRemocao?.tipo === 'atividade' ? 'Confirma a remoção desta atividade e todos os conhecimentos associados?' : 'Confirma a remoção deste conhecimento?'"
        variant="danger"
        @confirmar="confirmarRemocao"
    />

  </BContainer>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BCol, BContainer, BDropdown, BDropdownItem, BForm, BFormInput} from "bootstrap-vue-next";
import {computed, nextTick, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {badgeClass, logger, situacaoLabel} from "@/utils";
import ImpactoMapaModal from "@/components/ImpactoMapaModal.vue";
import ImportarAtividadesModal from "@/components/ImportarAtividadesModal.vue";
import HistoricoAnaliseModal from "@/components/HistoricoAnaliseModal.vue";
import ConfirmacaoDisponibilizacaoModal from "@/components/ConfirmacaoDisponibilizacaoModal.vue";
import ModalConfirmacao from "@/components/ModalConfirmacao.vue";
import EmptyState from "@/components/EmptyState.vue";
import {usePerfil} from "@/composables/usePerfil";
import {useAnalisesStore} from "@/stores/analises";
import {useAtividadesStore} from "@/stores/atividades";
import {useMapasStore} from "@/stores/mapas";
import AtividadeItem from "@/components/AtividadeItem.vue";
import {useFeedbackStore} from "@/stores/feedback";
import {useProcessosStore} from "@/stores/processos";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useUnidadesStore} from "@/stores/unidades";
import {storeToRefs} from "pinia";
import {
  type Atividade,
  type Conhecimento,
  type CriarAtividadeRequest,
  type CriarConhecimentoRequest,
  type ErroValidacao,
  Perfil,
  SituacaoSubprocesso,
  TipoProcesso,
} from "@/types/tipos";
import * as subprocessoService from "@/services/subprocessoService";

const props = defineProps<{
  codProcesso: number | string;
  sigla: string;
}>();

const router = useRouter();
const atividadesStore = useAtividadesStore();
const unidadesStore = useUnidadesStore();
const processosStore = useProcessosStore();
const subprocessosStore = useSubprocessosStore();
const feedbackStore = useFeedbackStore();
const analisesStore = useAnalisesStore();

const mapasStore = useMapasStore();
const {impactoMapa} = storeToRefs(mapasStore);
const loadingImpacto = ref(false);

const {perfilSelecionado} = usePerfil();
const isChefe = computed(() => perfilSelecionado.value === Perfil.CHEFE);

const codProcessoRef = computed(() => Number(props.codProcesso));

const codSubprocesso = ref<number | null>(null);
const codMapa = computed(() => mapasStore.mapaCompleto?.codigo || null);
const subprocesso = computed(() => subprocessosStore.subprocessoDetalhe);
const nomeUnidade = computed(() => unidadesStore.unidade?.nome || "");
const permissoes = computed(() => subprocesso.value?.permissoes || null);

const atividades = computed({
  get: () => {
    if (codSubprocesso.value === null) return [];
    return atividadesStore.obterAtividadesPorSubprocesso(codSubprocesso.value);
  },
  set: () => {
  },
});

const __ = computed(() => processosStore.processoDetalhe);
const isRevisao = computed(
    () => subprocesso.value?.tipoProcesso === TipoProcesso.REVISAO,
);

const historicoAnalises = computed(() => {
  if (!codSubprocesso.value) return [];
  return analisesStore.obterAnalisesPorSubprocesso(codSubprocesso.value);
});

const novaAtividade = ref("");
const inputNovaAtividadeRef = ref<InstanceType<typeof BFormInput> | null>(null);
const loadingAdicionar = ref(false);
const loadingValidacao = ref(false);

// Modais
const mostrarModalImpacto = ref(false);
const mostrarModalImportar = ref(false);
const mostrarModalConfirmacao = ref(false);
const mostrarModalHistorico = ref(false);
const mostrarModalConfirmacaoRemocao = ref(false);
const dadosRemocao = ref<{ tipo: 'atividade' | 'conhecimento', index: number, conhecimentoCodigo?: number } | null>(null);

const errosValidacao = ref<ErroValidacao[]>([]);
const erroGlobal = ref<string | null>(null);
const podeVerImpacto = computed(() => !!permissoes.value?.podeVisualizarImpacto);


// CRUD Atividades (mantido local por enquanto)
async function adicionarAtividade() {
  if (novaAtividade.value?.trim() && codMapa.value && codSubprocesso.value) {
    loadingAdicionar.value = true;
    try {
      const request: CriarAtividadeRequest = {
        descricao: novaAtividade.value.trim(),
      };
      await atividadesStore.adicionarAtividade(
          codSubprocesso.value,
          codMapa.value,
          request,
      );
      novaAtividade.value = "";
      // UX Improvement: Refocus input to allow rapid entry of multiple activities
      await nextTick();
      inputNovaAtividadeRef.value?.$el?.focus();
    } finally {
      loadingAdicionar.value = false;
    }
  }
}

function removerAtividade(idx: number) {
  if (!codSubprocesso.value) return;
  dadosRemocao.value = { tipo: 'atividade', index: idx };
  mostrarModalConfirmacaoRemocao.value = true;
}

async function confirmarRemocao() {
  if (!dadosRemocao.value || !codSubprocesso.value) return;

  const { tipo, index, conhecimentoCodigo } = dadosRemocao.value;

  try {
    if (tipo === 'atividade') {
      const atividadeRemovida = atividades.value[index];
      await atividadesStore.removerAtividade(
          codSubprocesso.value,
          atividadeRemovida.codigo,
      );
    } else if (tipo === 'conhecimento' && conhecimentoCodigo !== undefined) {
      const atividade = atividades.value[index];
      await atividadesStore.removerConhecimento(
          codSubprocesso.value,
          atividade.codigo,
          conhecimentoCodigo,
      );
    }
    mostrarModalConfirmacaoRemocao.value = false;
    dadosRemocao.value = null;
  } catch (e: any) {
    feedbackStore.show(
        "Erro na remoção",
        e.message || "Não foi possível remover o item.",
        "danger"
    );
    // Fecha modal mesmo com erro para não travar UI (mas mantém visível se erro)
    // O ideal é manter modal se retry for possível, mas aqui assumimos erro fatal ou validação
    mostrarModalConfirmacaoRemocao.value = false;
  }
}

async function adicionarConhecimento(idx: number, descricao: string) {
  if (!codSubprocesso.value) return;
  const atividade = atividades.value[idx];
  if (descricao.trim()) {
    const request: CriarConhecimentoRequest = {
      descricao: descricao.trim(),
    };
    await atividadesStore.adicionarConhecimento(
        codSubprocesso.value,
        atividade.codigo,
        request,
    );
  }
}

function removerConhecimento(idx: number, conhecimentoCodigo: number) {
  if (!codSubprocesso.value) return;
  dadosRemocao.value = { tipo: 'conhecimento', index: idx, conhecimentoCodigo };
  mostrarModalConfirmacaoRemocao.value = true;
}

async function salvarEdicaoConhecimento(atividadeCodigo: number, conhecimentoCodigo: number, descricao: string) {
  if (!codSubprocesso.value) return;

  if (descricao.trim()) {
    const conhecimentoAtualizado: Conhecimento = {
      codigo: conhecimentoCodigo,
      descricao: descricao.trim(),
    };
    await atividadesStore.atualizarConhecimento(
        codSubprocesso.value,
        atividadeCodigo,
        conhecimentoCodigo,
        conhecimentoAtualizado,
    );
  }
}

async function salvarEdicaoAtividade(codigo: number, descricao: string) {
  if (descricao.trim() && codSubprocesso.value) {
    const atividadeOriginal = atividades.value.find((a) => a.codigo === codigo);
    if (atividadeOriginal) {
      const atividadeAtualizada: Atividade = {
        ...atividadeOriginal,
        descricao: descricao.trim(),
      };
      await atividadesStore.atualizarAtividade(
          codSubprocesso.value,
          codigo,
          atividadeAtualizada,
      );
    }
  }
}

async function handleImportAtividades() {
  mostrarModalImportar.value = false;
  if (codSubprocesso.value) {
    await atividadesStore.buscarAtividadesParaSubprocesso(codSubprocesso.value);
  }
  feedbackStore.show(
      "Importação Concluída",
      "As atividades foram importadas para o seu mapa.",
      "success"
  );
}

// Mapeamento de erros
const mapaErros = computed(() => {
  const mapa = new Map<number, string>();
  errosValidacao.value.forEach(erro => {
    if (erro.atividadeCodigo) {
      mapa.set(erro.atividadeCodigo, erro.mensagem);
    }
  });
  return mapa;
});

function obterErroParaAtividade(atividadeCodigo: number): string | undefined {
  return mapaErros.value.get(atividadeCodigo);
}

const atividadeRefs = new Map<number, any>();

function setAtividadeRef(atividadeCodigo: number, el: any) {
  if (el) {
    atividadeRefs.set(atividadeCodigo, el);
  }
}

function scrollParaPrimeiroErro() {
  if (errosValidacao.value.length > 0 && errosValidacao.value[0].atividadeCodigo) {
    const primeiraAtividadeComErro = atividadeRefs.get(errosValidacao.value[0].atividadeCodigo);
    if (primeiraAtividadeComErro) {
      primeiraAtividadeComErro.scrollIntoView({
        behavior: 'smooth',
        block: 'center'
      });
    }
  }
}


onMounted(async () => {
  const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(
      codProcessoRef.value,
      props.sigla,
  );

  if (id) {
    codSubprocesso.value = id;
    await subprocessosStore.buscarContextoEdicao(id);

    // UX Improvement: Auto-focus input if list is empty to encourage starting
    if (atividades.value.length === 0) {
      await nextTick();
      inputNovaAtividadeRef.value?.$el?.focus();
    }
  } else {
    logger.error('[CadAtividades] ERRO: Subprocesso não encontrado!');
  }
});

async function abrirModalHistorico() {
  if (codSubprocesso.value) {
    await analisesStore.buscarAnalisesCadastro(codSubprocesso.value);
  }
  mostrarModalHistorico.value = true;
}

function abrirModalImpacto() {
  mostrarModalImpacto.value = true;
  if (codSubprocesso.value) {
    loadingImpacto.value = true;
    mapasStore.buscarImpactoMapa(codSubprocesso.value)
        .finally(() => loadingImpacto.value = false);
  }
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
}

async function disponibilizarCadastro() {
  const sub = subprocesso.value;
  const situacaoEsperada = isRevisao.value
      ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
      : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;

  if (!sub || sub.situacao !== situacaoEsperada) {
    feedbackStore.show(
        "Ação não permitida",
        `Ação permitida apenas na situação: "${situacaoEsperada}".`,
        "danger"
    );
    return;
  }

  if (codSubprocesso.value) {
    loadingValidacao.value = true;
    errosValidacao.value = [];
    erroGlobal.value = null;
    try {
      const resultado = await subprocessoService.validarCadastro(codSubprocesso.value);
      if (resultado.valido) {
        mostrarModalConfirmacao.value = true;
      } else {
        errosValidacao.value = resultado.erros;

        // Identificar erro global
        const erroSemAtividade = resultado.erros.find(e => !e.atividadeCodigo);
        if (erroSemAtividade) {
          erroGlobal.value = erroSemAtividade.mensagem;
        }

        await nextTick();
        scrollParaPrimeiroErro();
      }
    } catch {
      feedbackStore.show("Erro na validação", "Não foi possível validar o cadastro.", "danger");
    } finally {
      loadingValidacao.value = false;
    }
  }
}

async function confirmarDisponibilizacao() {
  if (!codSubprocesso.value) return;

  let sucesso: boolean;
  if (isRevisao.value) {
    sucesso = await subprocessosStore.disponibilizarRevisaoCadastro(codSubprocesso.value);
  } else {
    sucesso = await subprocessosStore.disponibilizarCadastro(codSubprocesso.value);
  }

  mostrarModalConfirmacao.value = false;
  if (sucesso) {
    await router.push("/painel");
  }
}
</script>