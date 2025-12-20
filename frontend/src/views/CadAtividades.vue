<template>
  <BContainer class="mt-4">
    <div class="d-flex align-items-center mb-3">
      <BButton
          class="p-0 me-3 text-decoration-none"
          data-testid="btn-cad-atividades-voltar"
          variant="link"
          @click="router.back()"
      >
        <i class="bi bi-arrow-left fs-4"/>
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
        <BButton
            v-if="podeVerImpacto"
            data-testid="cad-atividades__btn-impactos-mapa"
            variant="outline-secondary"
            @click="abrirModalImpacto"
        >
          <i class="bi bi-arrow-right-circle me-2"/>Impacto no mapa
        </BButton>
        <BButton
            v-if="isChefe && codSubprocesso"
            data-testid="btn-cad-atividades-historico"
            variant="outline-info"
            @click="abrirModalHistorico"
        >
          Histórico de análise
        </BButton>
        <BButton
            v-if="isChefe"
            data-testid="btn-cad-atividades-importar"
            title="Importar"
            variant="outline-primary"
            @click="mostrarModalImportar = true"
        >
          Importar atividades
        </BButton>
        <BButton
            v-if="!!permissoes?.podeDisponibilizarCadastro"
            data-testid="btn-cad-atividades-disponibilizar"
            title="Disponibilizar"
            variant="outline-success"
            @click="disponibilizarCadastro"
        >
          Disponibilizar
        </BButton>
      </div>
    </div>

    <BForm
        class="row g-2 align-items-center mb-4"
        data-testid="form-nova-atividade"
        @submit.prevent="adicionarAtividade"
    >
      <BCol>
        <BFormInput
            v-model="novaAtividade"
            aria-label="Nova atividade"
            data-testid="inp-nova-atividade"
            placeholder="Nova atividade"
            type="text"
        />
      </BCol>
      <BCol cols="auto">
        <BButton
            :disabled="!codSubprocesso || !permissoes?.podeEditarMapa"
            data-testid="btn-adicionar-atividade"
            size="sm"
            title="Adicionar atividade"
            type="submit"
            variant="outline-primary"
        >
          <i
              class="bi bi-save"
          />
        </BButton>
      </BCol>
    </BForm>

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
        :cod-subprocesso="codSubprocesso"
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
        :mostrar="mostrarModalHistorico"
        :cod-subprocesso="codSubprocesso"
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
import {BButton, BCol, BContainer, BForm, BFormInput} from "bootstrap-vue-next";
import {computed, nextTick, onMounted, ref, toRef} from "vue";
import {useRouter} from "vue-router";
import {badgeClass, situacaoLabel} from "@/utils";
import ImpactoMapaModal from "@/components/ImpactoMapaModal.vue";
import ImportarAtividadesModal from "@/components/ImportarAtividadesModal.vue";
import HistoricoAnaliseModal from "@/components/HistoricoAnaliseModal.vue";
import ConfirmacaoDisponibilizacaoModal from "@/components/ConfirmacaoDisponibilizacaoModal.vue";
import ModalConfirmacao from "@/components/ModalConfirmacao.vue";
import {usePerfil} from "@/composables/usePerfil";
import {useAtividadesStore} from "@/stores/atividades";
import {useMapasStore} from "@/stores/mapas";
import AtividadeItem from "@/components/AtividadeItem.vue";
import {useFeedbackStore} from "@/stores/feedback";
import {useProcessosStore} from "@/stores/processos";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useUnidadesStore} from "@/stores/unidades";
import {
  type Atividade,
  type Conhecimento,
  type CriarAtividadeRequest,
  type CriarConhecimentoRequest,
  type ErroValidacao,
  Perfil,
  SituacaoSubprocesso,
  type SubprocessoPermissoes,
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

const mapasStore = useMapasStore();

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

const processoAtual = computed(() => processosStore.processoDetalhe);
const isRevisao = computed(
    () => processoAtual.value?.tipo === TipoProcesso.REVISAO,
);

const novaAtividade = ref("");

// Modais
const mostrarModalImpacto = ref(false);
const mostrarModalImportar = ref(false);
const mostrarModalConfirmacao = ref(false);
const mostrarModalHistorico = ref(false);
const mostrarModalConfirmacaoRemocao = ref(false);
const dadosRemocao = ref<{ tipo: 'atividade' | 'conhecimento', index: number, idConhecimento?: number } | null>(null);

const errosValidacao = ref<ErroValidacao[]>([]);
const podeVerImpacto = computed(() => !!permissoes.value?.podeVisualizarImpacto);


// CRUD Atividades (mantido local por enquanto)
async function adicionarAtividade() {
  if (novaAtividade.value?.trim() && codMapa.value && codSubprocesso.value) {
    const request: CriarAtividadeRequest = {
      descricao: novaAtividade.value.trim(),
    };
    const status = await atividadesStore.adicionarAtividade(
        codSubprocesso.value,
        codMapa.value,
        request,
    );
    novaAtividade.value = "";
    if (status) {
      processosStore.atualizarStatusSubprocesso(codSubprocesso.value, status);
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

  const { tipo, index, idConhecimento } = dadosRemocao.value;

  if (tipo === 'atividade') {
    const atividadeRemovida = atividades.value[index];
    const status = await atividadesStore.removerAtividade(
        codSubprocesso.value,
        atividadeRemovida.codigo,
    );
    if (status) {
      processosStore.atualizarStatusSubprocesso(codSubprocesso.value, status);
    }
  } else if (tipo === 'conhecimento' && idConhecimento !== undefined) {
    const atividade = atividades.value[index];
    const status = await atividadesStore.removerConhecimento(
        codSubprocesso.value,
        atividade.codigo,
        idConhecimento,
    );
    if (status) {
      processosStore.atualizarStatusSubprocesso(codSubprocesso.value, status);
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
    const status = await atividadesStore.adicionarConhecimento(
        codSubprocesso.value,
        atividade.codigo,
        request,
    );
    if (status) {
      processosStore.atualizarStatusSubprocesso(codSubprocesso.value, status);
    }
  }
}

function removerConhecimento(idx: number, idConhecimento: number) {
  if (!codSubprocesso.value) return;
  dadosRemocao.value = { tipo: 'conhecimento', index: idx, idConhecimento };
  mostrarModalConfirmacaoRemocao.value = true;
}

async function salvarEdicaoConhecimento(atividadeId: number, conhecimentoId: number, descricao: string) {
  if (!codSubprocesso.value) return;

  if (descricao.trim()) {
    const conhecimentoAtualizado: Conhecimento = {
      id: conhecimentoId,
      descricao: descricao.trim(),
    };
    const status = await atividadesStore.atualizarConhecimento(
        codSubprocesso.value,
        atividadeId,
        conhecimentoId,
        conhecimentoAtualizado,
    );
    if (status) {
      processosStore.atualizarStatusSubprocesso(codSubprocesso.value, status);
    }
  }
}

async function salvarEdicaoAtividade(id: number, descricao: string) {
  if (descricao.trim() && codSubprocesso.value) {
    const atividadeOriginal = atividades.value.find((a) => a.codigo === id);
    if (atividadeOriginal) {
      const atividadeAtualizada: Atividade = {
        ...atividadeOriginal,
        descricao: descricao.trim(),
      };
      const status = await atividadesStore.atualizarAtividade(
          codSubprocesso.value,
          id,
          atividadeAtualizada,
      );
      if (status) {
        processosStore.atualizarStatusSubprocesso(codSubprocesso.value, status);
      }
    }
  }
}

async function handleImportAtividades() {
  mostrarModalImportar.value = false;
  if (codSubprocesso.value) {
    await atividadesStore.buscarAtividadesParaSubprocesso(codSubprocesso.value);
    const status = await subprocessoService.obterStatus(codSubprocesso.value);
    processosStore.atualizarStatusSubprocesso(codSubprocesso.value, status);
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
    if (erro.atividadeId) {
      mapa.set(erro.atividadeId, erro.mensagem);
    }
  });
  return mapa;
});

function obterErroParaAtividade(atividadeId: number): string | undefined {
  return mapaErros.value.get(atividadeId);
}

const atividadeRefs = new Map<number, any>();

function setAtividadeRef(atividadeId: number, el: any) {
  if (el) {
    atividadeRefs.set(atividadeId, el);
  }
}

function scrollParaPrimeiroErro() {
  if (errosValidacao.value.length > 0 && errosValidacao.value[0].atividadeId) {
    const primeiraAtividadeComErro = atividadeRefs.get(errosValidacao.value[0].atividadeId);
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
  } else {
    console.error('[CadAtividades] ERRO: Subprocesso não encontrado!');
  }
});

function abrirModalHistorico() {
  mostrarModalHistorico.value = true;
}

function abrirModalImpacto() {
  mostrarModalImpacto.value = true;
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
    try {
      const resultado = await subprocessoService.validarCadastro(codSubprocesso.value);
      if (resultado.valido) {
        mostrarModalConfirmacao.value = true;
      } else {
        errosValidacao.value = resultado.erros;
        await nextTick();
        scrollParaPrimeiroErro();
      }
    } catch {
      feedbackStore.show("Erro na validação", "Não foi possível validar o cadastro.", "danger");
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