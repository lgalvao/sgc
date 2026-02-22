<template>
  <LayoutPadrao>
    <PageHeader title="Atividades e conhecimentos">
      <template #default>
        <div class="d-flex align-items-center gap-2">
          <BButton
              aria-label="Voltar"
              class="p-0 me-2 text-decoration-none text-muted"
              data-testid="btn-cad-atividades-voltar"
              title="Voltar"
              variant="link"
              @click="() => { router.push(`/processo/${props.codProcesso}/${props.sigla}`); }"
          >
            <i aria-hidden="true" class="bi bi-arrow-left"/>
          </BButton>
          <span>{{ sigla }} - {{ nomeUnidade }}</span>
          <span
              v-if="subprocesso"
              :class="badgeClass(subprocesso.situacao)"
              class="badge fs-6"
              data-testid="cad-atividades__txt-badge-situacao"
          >{{ formatSituacaoSubprocesso(subprocesso.situacao) }}</span>
        </div>
      </template>
      <template #actions>
        <BDropdown
            v-if="codSubprocesso && (podeVerImpacto || isChefe)"
            data-testid="btn-mais-acoes"
            text="Mais ações"
            variant="outline-secondary"
            class="me-2"
        >
          <BDropdownItem
              v-if="podeVerImpacto"
              data-testid="cad-atividades__btn-impactos-mapa-edicao"
              @click="abrirModalImpacto"
          >
            <i aria-hidden="true" class="bi bi-arrow-right-circle me-2"/> Impacto no mapa
          </BDropdownItem>
          <BDropdownItem
              v-if="isChefe"
              data-testid="btn-cad-atividades-historico"
              @click="abrirModalHistorico"
          >
            <i aria-hidden="true" class="bi bi-clock-history me-2"/> Histórico de análise
          </BDropdownItem>
          <BDropdownItem
              v-if="isChefe"
              data-testid="btn-cad-atividades-importar"
              @click="mostrarModalImportar = true"
          >
            <i aria-hidden="true" class="bi bi-upload me-2"/> Importar atividades
          </BDropdownItem>
        </BDropdown>

        <LoadingButton
            v-if="!!podeDisponibilizarCadastro"
            :loading="loadingValidacao"
            data-testid="btn-cad-atividades-disponibilizar"
            variant="success"
            icon="check-lg"
            text="Disponibilizar"
            loading-text="Validando..."
            @click="disponibilizarCadastro"
        />
      </template>
    </PageHeader>

    <ErrorAlert
        :error="erroGlobalFormatado"
        @dismiss="erroGlobal = null"
    />

    <CadAtividadeForm
        ref="atividadeFormRef"
        v-model="novaAtividade"
        :disabled="!codSubprocesso || !podeEditarCadastro"
        :erro="erroNovaAtividade"
        :loading="loadingAdicionar"
        @submit="handleAdicionarAtividade"
    />

    <!-- Empty State -->
    <EmptyState
        v-if="atividades?.length === 0"
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
        <i aria-hidden="true" class="bi bi-upload me-2"/> Importar atividades
      </BButton>
    </EmptyState>

    <div
        v-for="(atividade, idx) in atividades"
        :key="atividade.codigo || idx"
        :ref="el => setAtividadeRef(atividade.codigo, el)"
    >
      <AtividadeItem
          :atividade="atividade"
          :pode-editar="!!podeEditarCadastro"
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

  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BButton, BDropdown, BDropdownItem} from "bootstrap-vue-next";
import {computed, nextTick, onMounted, ref, watch} from "vue";
import {useRouter} from "vue-router";
import {storeToRefs} from "pinia";
import {badgeClass} from "@/utils";
import ImpactoMapaModal from "@/components/mapa/ImpactoMapaModal.vue";
import ImportarAtividadesModal from "@/components/atividades/ImportarAtividadesModal.vue";
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import ConfirmacaoDisponibilizacaoModal from "@/components/mapa/ConfirmacaoDisponibilizacaoModal.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import ErrorAlert from "@/components/comum/ErrorAlert.vue";
import AtividadeItem from "@/components/atividades/AtividadeItem.vue";
import CadAtividadeForm from "@/components/atividades/CadAtividadeForm.vue";
import {useAtividadeForm} from "@/composables/useAtividadeForm";
import {useAtividadesStore} from "@/stores/atividades";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useMapasStore} from "@/stores/mapas";
import {useUnidadesStore} from "@/stores/unidades";
import {useAnalisesStore} from "@/stores/analises";
import {useFeedbackStore} from "@/stores/feedback";
import {usePerfil} from "@/composables/usePerfil";
import {useAcesso} from "@/composables/useAcesso";
import type {Atividade, Conhecimento, CriarConhecimentoRequest, ErroValidacao,} from "@/types/tipos";
import {Perfil, SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";
import logger from "@/utils/logger";
import {formatSituacaoSubprocesso} from "@/utils/formatters";

type DadosRemocao = {tipo: "atividade" | "conhecimento"; index: number; conhecimentoCodigo?: number} | null;

const props = defineProps<{
  codProcesso: number | string;
  sigla: string;
}>();

const router = useRouter();
const atividadesStore = useAtividadesStore();
const unidadesStore = useUnidadesStore();
const subprocessosStore = useSubprocessosStore();
const analisesStore = useAnalisesStore();
const mapasStore = useMapasStore();
const feedbackStore = useFeedbackStore();
const {impactoMapa} = storeToRefs(mapasStore);

const {perfilSelecionado} = usePerfil();
const isChefe = computed(() => perfilSelecionado.value === Perfil.CHEFE);
const codSubprocesso = ref<number | null>(null);

const codMapa = computed(() => mapasStore.mapaCompleto?.codigo || null);
const subprocesso = computed(() => subprocessosStore.subprocessoDetalhe);
const nomeUnidade = computed(() => unidadesStore.unidade?.nome || "");
const { podeEditarCadastro, podeDisponibilizarCadastro, podeVisualizarImpacto } = useAcesso(subprocesso);
const isRevisao = computed(() => subprocesso.value?.tipoProcesso === TipoProcesso.REVISAO);
const podeVerImpacto = computed(() => podeVisualizarImpacto.value);

const atividades = computed(() => {
  if (codSubprocesso.value === null) return [];
  return atividadesStore.obterAtividadesPorSubprocesso(codSubprocesso.value);
});

const historicoAnalises = computed(() => {
  if (!codSubprocesso.value) return [];
  return analisesStore.obterAnalisesPorSubprocesso(codSubprocesso.value);
});

const {novaAtividade, loadingAdicionar, adicionarAtividade: adicionarAtividadeAction} = useAtividadeForm();
const erroNovaAtividade = ref<string | null>(null);

// Modais
const mostrarModalImpacto = ref(false);
const mostrarModalImportar = ref(false);
const mostrarModalConfirmacao = ref(false);
const mostrarModalHistorico = ref(false);
const mostrarModalConfirmacaoRemocao = ref(false);
const dadosRemocao = ref<DadosRemocao>(null);
const loadingImpacto = ref(false);

// Validação
const loadingValidacao = ref(false);
const errosValidacao = ref<ErroValidacao[]>([]);
const erroGlobal = ref<string | null>(null);
const atividadeRefs = new Map<number, Element>();

const mapaErros = computed(() => {
  const mapa = new Map<number, string>();
  errosValidacao.value.forEach((erro) => {
    if (erro.atividadeCodigo) {
      mapa.set(erro.atividadeCodigo, erro.mensagem);
    }
  });
  return mapa;
});

const atividadeFormRef = ref<InstanceType<typeof CadAtividadeForm> | null>(null);
const erroGlobalFormatado = computed(() =>
  erroGlobal.value ? {message: erroGlobal.value} : null
);

async function adicionarAtividade(): Promise<boolean> {
  if (codMapa.value && codSubprocesso.value) {
    try {
      const sucesso = await adicionarAtividadeAction(codSubprocesso.value, codMapa.value);
      if (sucesso) erroNovaAtividade.value = null;
      return sucesso;
    } catch {
      erroNovaAtividade.value = atividadesStore.lastError?.message || "Não foi possível adicionar atividade.";
      return false;
    }
  }
  return false;
}

function removerAtividade(idx: number) {
  if (!codSubprocesso.value) return;
  dadosRemocao.value = {tipo: "atividade", index: idx};
  mostrarModalConfirmacaoRemocao.value = true;
}

async function confirmarRemocao() {
  if (!dadosRemocao.value || !codSubprocesso.value) return;

  const {tipo, index, conhecimentoCodigo} = dadosRemocao.value;

  try {
    if (tipo === "atividade") {
      const atividadeRemovida = atividades.value[index];
      await atividadesStore.removerAtividade(codSubprocesso.value, atividadeRemovida.codigo);
    } else if (tipo === "conhecimento" && conhecimentoCodigo !== undefined) {
      const atividade = atividades.value[index];
      await atividadesStore.removerConhecimento(codSubprocesso.value, atividade.codigo, conhecimentoCodigo);
    }
    mostrarModalConfirmacaoRemocao.value = false;
    dadosRemocao.value = null;
  } catch (e: any) {
    feedbackStore.show("Erro na remoção", e.message || "Não foi possível remover o item.", "danger");
    mostrarModalConfirmacaoRemocao.value = false;
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
      await atividadesStore.atualizarAtividade(codSubprocesso.value, codigo, atividadeAtualizada);
    }
  }
}

async function adicionarConhecimento(idx: number, descricao: string) {
  if (!codSubprocesso.value) return;
  const atividade = atividades.value[idx];
  if (descricao.trim()) {
    const request: CriarConhecimentoRequest = {
      descricao: descricao.trim(),
    };
    await atividadesStore.adicionarConhecimento(codSubprocesso.value, atividade.codigo, request);
  }
}

function removerConhecimento(idx: number, conhecimentoCodigo: number) {
  if (!codSubprocesso.value) return;
  dadosRemocao.value = {tipo: "conhecimento", index: idx, conhecimentoCodigo};
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

async function handleImportAtividades() {
  mostrarModalImportar.value = false;
  if (codSubprocesso.value) {
    await atividadesStore.buscarAtividadesParaSubprocesso(codSubprocesso.value);
  }
  feedbackStore.show("Importação Concluída", "As atividades foram importadas para o seu mapa.", "success");
}

function obterErroParaAtividade(atividadeCodigo: number): string | undefined {
  return mapaErros.value.get(atividadeCodigo);
}

function setAtividadeRef(atividadeCodigo: number, el: unknown) {
  if (el && el instanceof Element) {
    atividadeRefs.set(atividadeCodigo, el);
  }
}

function scrollParaPrimeiroErro() {
  if (errosValidacao.value.length > 0 && errosValidacao.value[0].atividadeCodigo) {
    const primeiraAtividadeComErro = atividadeRefs.get(errosValidacao.value[0].atividadeCodigo);
    if (primeiraAtividadeComErro) {
      primeiraAtividadeComErro.scrollIntoView({
        behavior: "smooth",
        block: "center",
      });
    }
  }
}

async function disponibilizarCadastro() {
  const situacaoEsperada = isRevisao.value
    ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
    : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;

  if (subprocesso.value?.situacao !== situacaoEsperada) {
    feedbackStore.show(
      "Ação não permitida",
      `Ação permitida apenas na situação: "${situacaoEsperada}".`,
      "danger",
    );
    return;
  }

  if (codSubprocesso.value) {
    loadingValidacao.value = true;
    errosValidacao.value = [];
    erroGlobal.value = null;
    try {
      const resultado = await subprocessosStore.validarCadastro(codSubprocesso.value);
      if (resultado?.valido) {
        mostrarModalConfirmacao.value = true;
      } else if (resultado) {
        errosValidacao.value = resultado.erros;

        const erroSemAtividade = resultado.erros.find((e) => !e.atividadeCodigo);
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
    mapasStore.buscarImpactoMapa(codSubprocesso.value).finally(() => (loadingImpacto.value = false));
  }
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
}

async function handleAdicionarAtividade() {
  const sucesso = await adicionarAtividade();
  await nextTick();
  if (sucesso || erroNovaAtividade.value) atividadeFormRef.value?.inputRef?.$el?.focus();
}

onMounted(async () => {
  const codProcessoRef = Number(props.codProcesso);
  const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(codProcessoRef, props.sigla);

  if (id) {
    codSubprocesso.value = id;
    await subprocessosStore.buscarContextoEdicao(id);
  } else {
    logger.error("[CadAtividades] ERRO: Subprocesso não encontrado!");
  }
});

// Auto-focus if empty on load
watch(() => atividades.value?.length, (newLen, oldLen) => {
  if (newLen === 0 && oldLen === undefined) {
    nextTick(() => atividadeFormRef.value?.inputRef?.$el?.focus());
  }
}, {immediate: true});
</script>
