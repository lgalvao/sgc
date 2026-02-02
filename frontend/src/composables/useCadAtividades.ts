import { computed, nextTick, onMounted, ref, type ComputedRef, type Ref } from "vue";
import { useRouter, type Router } from "vue-router";
import { storeToRefs } from "pinia";
import { useAtividadeForm } from "@/composables/useAtividadeForm";
import { useAtividadesStore } from "@/stores/atividades";
import { useSubprocessosStore } from "@/stores/subprocessos";
import { useMapasStore } from "@/stores/mapas";
import { useUnidadesStore } from "@/stores/unidades";
import { useAnalisesStore } from "@/stores/analises";
import { useFeedbackStore } from "@/stores/feedback";
import { usePerfil } from "@/composables/usePerfil";
import type {
    Atividade,
    AnaliseCadastro,
    AnaliseValidacao,
    Conhecimento,
    CriarConhecimentoRequest,
    ErroValidacao,
    ImpactoMapa,
    SubprocessoDetalhe,
    SubprocessoPermissoes,
} from "@/types/tipos";
import { Perfil, SituacaoSubprocesso, TipoProcesso } from "@/types/tipos";
import logger from "@/utils/logger";

type Analise = AnaliseCadastro | AnaliseValidacao;
type DadosRemocao = { tipo: "atividade" | "conhecimento"; index: number; conhecimentoCodigo?: number } | null;

export interface UseCadAtividades {
    // Estado
    router: Router;
    isChefe: ComputedRef<boolean>;
    codSubprocesso: Ref<number | null>;
    subprocesso: ComputedRef<SubprocessoDetalhe | null>;
    nomeUnidade: ComputedRef<string>;
    permissoes: ComputedRef<SubprocessoPermissoes | null>;
    atividades: ComputedRef<Atividade[]>;
    isRevisao: ComputedRef<boolean>;
    historicoAnalises: ComputedRef<Analise[]>;
    podeVerImpacto: ComputedRef<boolean>;
    impactoMapa: Ref<ImpactoMapa | null>;

    // Formulário de nova atividade
    novaAtividade: Ref<string>;
    loadingAdicionar: Ref<boolean>;

    // Modais
    mostrarModalImpacto: Ref<boolean>;
    mostrarModalImportar: Ref<boolean>;
    mostrarModalConfirmacao: Ref<boolean>;
    mostrarModalHistorico: Ref<boolean>;
    mostrarModalConfirmacaoRemocao: Ref<boolean>;
    dadosRemocao: Ref<DadosRemocao>;
    loadingImpacto: Ref<boolean>;

    // Validação
    loadingValidacao: Ref<boolean>;
    errosValidacao: Ref<ErroValidacao[]>;
    erroGlobal: Ref<string | null>;

    // CRUD de Atividades
    adicionarAtividade: () => Promise<boolean>;
    removerAtividade: (idx: number) => void;
    confirmarRemocao: () => Promise<void>;
    salvarEdicaoAtividade: (codigo: number, descricao: string) => Promise<void>;

    // CRUD de Conhecimentos
    adicionarConhecimento: (idx: number, descricao: string) => Promise<void>;
    removerConhecimento: (idx: number, conhecimentoCodigo: number) => void;
    salvarEdicaoConhecimento: (atividadeCodigo: number, conhecimentoCodigo: number, descricao: string) => Promise<void>;

    // Importação
    handleImportAtividades: () => Promise<void>;

    // Validação e Disponibilização
    obterErroParaAtividade: (atividadeCodigo: number) => string | undefined;
    setAtividadeRef: (atividadeCodigo: number, el: unknown) => void;
    disponibilizarCadastro: () => Promise<void>;
    confirmarDisponibilizacao: () => Promise<void>;

    // Modais
    abrirModalHistorico: () => Promise<void>;
    abrirModalImpacto: () => void;
    fecharModalImpacto: () => void;
}

/**
 * Composable unificado para gerenciamento do cadastro de atividades.
 * 
 * Consolida funcionalidades de:
 * - Estado do subprocesso e atividades
 * - CRUD de atividades e conhecimentos
 * - Validação e disponibilização do cadastro
 * - Gerenciamento de modais
 * - Importação de atividades
 */
export function useCadAtividades(props: { codProcesso: number | string; sigla: string }): UseCadAtividades {
    // Stores
    const router = useRouter();
    const atividadesStore = useAtividadesStore();
    const unidadesStore = useUnidadesStore();
    const subprocessosStore = useSubprocessosStore();
    const analisesStore = useAnalisesStore();
    const mapasStore = useMapasStore();
    const feedbackStore = useFeedbackStore();
    const { impactoMapa } = storeToRefs(mapasStore);

    // Estado base
    const { perfilSelecionado } = usePerfil();
    const isChefe = computed(() => perfilSelecionado.value === Perfil.CHEFE);
    const codSubprocesso = ref<number | null>(null);

    // Computeds do subprocesso
    const codMapa = computed(() => mapasStore.mapaCompleto?.codigo || null);
    const subprocesso = computed(() => subprocessosStore.subprocessoDetalhe);
    const nomeUnidade = computed(() => unidadesStore.unidade?.nome || "");
    const permissoes = computed(() => subprocesso.value?.permissoes || null);
    const isRevisao = computed(() => subprocesso.value?.tipoProcesso === TipoProcesso.REVISAO);
    const podeVerImpacto = computed(() => permissoes.value?.podeVisualizarImpacto ?? false);

    // Atividades e análises
    const atividades = computed(() => {
        if (codSubprocesso.value === null) return [];
        return atividadesStore.obterAtividadesPorSubprocesso(codSubprocesso.value);
    });

    const historicoAnalises = computed(() => {
        if (!codSubprocesso.value) return [];
        return analisesStore.obterAnalisesPorSubprocesso(codSubprocesso.value);
    });

    // Formulário de nova atividade
    const { novaAtividade, loadingAdicionar, adicionarAtividade: adicionarAtividadeAction } = useAtividadeForm();

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

    // ========== CRUD de Atividades ==========

    async function adicionarAtividade(): Promise<boolean> {
        if (codMapa.value && codSubprocesso.value) {
            return await adicionarAtividadeAction(codSubprocesso.value, codMapa.value);
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

    // ========== CRUD de Conhecimentos ==========

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

    // ========== Importação ==========

    async function handleImportAtividades() {
        mostrarModalImportar.value = false;
        if (codSubprocesso.value) {
            await atividadesStore.buscarAtividadesParaSubprocesso(codSubprocesso.value);
        }
        feedbackStore.show("Importação Concluída", "As atividades foram importadas para o seu mapa.", "success");
    }

    // ========== Validação ==========

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
                if (resultado && resultado.valido) {
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

    // ========== Modais ==========

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

    // ========== Inicialização ==========

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

    return {
        // Estado
        router,
        isChefe,
        codSubprocesso,
        subprocesso,
        nomeUnidade,
        permissoes,
        atividades,
        isRevisao,
        historicoAnalises,
        podeVerImpacto,
        impactoMapa,

        // Formulário
        novaAtividade,
        loadingAdicionar,

        // Modais
        mostrarModalImpacto,
        mostrarModalImportar,
        mostrarModalConfirmacao,
        mostrarModalHistorico,
        mostrarModalConfirmacaoRemocao,
        dadosRemocao,
        loadingImpacto,

        // Validação
        loadingValidacao,
        errosValidacao,
        erroGlobal,

        // CRUD Atividades
        adicionarAtividade,
        removerAtividade,
        confirmarRemocao,
        salvarEdicaoAtividade,

        // CRUD Conhecimentos
        adicionarConhecimento,
        removerConhecimento,
        salvarEdicaoConhecimento,

        // Importação
        handleImportAtividades,

        // Validação e Disponibilização
        obterErroParaAtividade,
        setAtividadeRef,
        disponibilizarCadastro,
        confirmarDisponibilizacao,

        // Modais
        abrirModalHistorico,
        abrirModalImpacto,
        fecharModalImpacto,
    };
}
