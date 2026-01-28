import { computed, nextTick, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import { useAtividadesStore } from "@/stores/atividades";
import { useSubprocessosStore } from "@/stores/subprocessos";
import { useMapasStore } from "@/stores/mapas";
import { useFeedbackStore } from "@/stores/feedback";
import { useAnalisesStore } from "@/stores/analises";
import { useUnidadesStore } from "@/stores/unidades";
import { usePerfil } from "@/composables/usePerfil";
import { useAtividadeForm } from "@/composables/useAtividadeForm";
import {
    type Atividade,
    type Conhecimento,
    type CriarConhecimentoRequest,
    type ErroValidacao,
    Perfil,
    SituacaoSubprocesso,
    TipoProcesso,
} from "@/types/tipos";
import * as subprocessoService from "@/services/subprocessoService";
import logger from "@/utils/logger";

export function useCadAtividadesLogic(props: { codProcesso: number | string; sigla: string }) {
    const router = useRouter();
    const atividadesStore = useAtividadesStore();
    const unidadesStore = useUnidadesStore();
    const subprocessosStore = useSubprocessosStore();
    const feedbackStore = useFeedbackStore();
    const analisesStore = useAnalisesStore();
    const mapasStore = useMapasStore();
    const { impactoMapa } = storeToRefs(mapasStore);

    const { perfilSelecionado } = usePerfil();
    const isChefe = computed(() => perfilSelecionado.value === Perfil.CHEFE);
    const codProcessoRef = computed(() => Number(props.codProcesso));

    const codSubprocesso = ref<number | null>(null);
    const codMapa = computed(() => mapasStore.mapaCompleto?.codigo || null);
    const subprocesso = computed(() => subprocessosStore.subprocessoDetalhe);
    const nomeUnidade = computed(() => unidadesStore.unidade?.nome || "");
    const permissoes = computed(() => subprocesso.value?.permissoes || null);
    const isRevisao = computed(() => subprocesso.value?.tipoProcesso === TipoProcesso.REVISAO);

    const atividades = computed(() => {
        if (codSubprocesso.value === null) return [];
        return atividadesStore.obterAtividadesPorSubprocesso(codSubprocesso.value);
    });

    const historicoAnalises = computed(() => {
        if (!codSubprocesso.value) return [];
        return analisesStore.obterAnalisesPorSubprocesso(codSubprocesso.value);
    });

    const { novaAtividade, loadingAdicionar, adicionarAtividade: adicionarAtividadeAction } = useAtividadeForm();

    const loadingValidacao = ref(false);
    const loadingImpacto = ref(false);

    // Modais
    const mostrarModalImpacto = ref(false);
    const mostrarModalImportar = ref(false);
    const mostrarModalConfirmacao = ref(false);
    const mostrarModalHistorico = ref(false);
    const mostrarModalConfirmacaoRemocao = ref(false);
    const dadosRemocao = ref<{ tipo: "atividade" | "conhecimento"; index: number; conhecimentoCodigo?: number } | null>(null);

    const errosValidacao = ref<ErroValidacao[]>([]);
    const erroGlobal = ref<string | null>(null);
    const podeVerImpacto = computed(() => permissoes.value?.podeVisualizarImpacto ?? false);

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
                    behavior: "smooth",
                    block: "center",
                });
            }
        }
    }

    async function adicionarAtividade() {
        if (codMapa.value && codSubprocesso.value) {
            const sucesso = await adicionarAtividadeAction(codSubprocesso.value, codMapa.value);
            if (sucesso) {
                return true;
            }
        }
        return false;
    }

    function removerAtividade(idx: number) {
        if (!codSubprocesso.value) return;
        dadosRemocao.value = { tipo: "atividade", index: idx };
        mostrarModalConfirmacaoRemocao.value = true;
    }

    async function confirmarRemocao() {
        if (!dadosRemocao.value || !codSubprocesso.value) return;

        const { tipo, index, conhecimentoCodigo } = dadosRemocao.value;

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
        dadosRemocao.value = { tipo: "conhecimento", index: idx, conhecimentoCodigo };
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
                await atividadesStore.atualizarAtividade(codSubprocesso.value, codigo, atividadeAtualizada);
            }
        }
    }

    async function handleImportAtividades() {
        mostrarModalImportar.value = false;
        if (codSubprocesso.value) {
            await atividadesStore.buscarAtividadesParaSubprocesso(codSubprocesso.value);
        }
        feedbackStore.show("Importação Concluída", "As atividades foram importadas para o seu mapa.", "success");
    }

    const mapaErros = computed(() => {
        const mapa = new Map<number, string>();
        errosValidacao.value.forEach((erro) => {
            if (erro.atividadeCodigo) {
                mapa.set(erro.atividadeCodigo, erro.mensagem);
            }
        });
        return mapa;
    });

    function obterErroParaAtividade(atividadeCodigo: number): string | undefined {
        return mapaErros.value.get(atividadeCodigo);
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

    async function disponibilizarCadastro() {
        const sub = subprocesso.value;
        const situacaoEsperada = isRevisao.value
            ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
            : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;

        if (sub?.situacao !== situacaoEsperada) {
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
                const resultado = await subprocessoService.validarCadastro(codSubprocesso.value);
                if (resultado.valido) {
                    mostrarModalConfirmacao.value = true;
                } else {
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

    onMounted(async () => {
        const id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(codProcessoRef.value, props.sigla);

        if (id) {
            codSubprocesso.value = id;
            await subprocessosStore.buscarContextoEdicao(id);
        } else {
            logger.error("[CadAtividades] ERRO: Subprocesso não encontrado!");
        }
    });

    return {
        router,
        isChefe,
        codSubprocesso,
        subprocesso,
        nomeUnidade,
        permissoes,
        atividades,
        isRevisao,
        historicoAnalises,
        novaAtividade,
        loadingAdicionar,
        loadingValidacao,
        loadingImpacto,
        impactoMapa,
        mostrarModalImpacto,
        mostrarModalImportar,
        mostrarModalConfirmacao,
        mostrarModalHistorico,
        mostrarModalConfirmacaoRemocao,
        dadosRemocao,
        errosValidacao,
        erroGlobal,
        podeVerImpacto,
        adicionarAtividade,
        removerAtividade,
        confirmarRemocao,
        adicionarConhecimento,
        removerConhecimento,
        salvarEdicaoConhecimento,
        salvarEdicaoAtividade,
        handleImportAtividades,
        obterErroParaAtividade,
        setAtividadeRef,
        abrirModalHistorico,
        abrirModalImpacto,
        disponibilizarCadastro,
        confirmarDisponibilizacao,
        fecharModalImpacto: () => (mostrarModalImpacto.value = false),
    };
}
