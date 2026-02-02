import {computed, type ComputedRef, onMounted, ref, type Ref} from "vue";
import {useRouter} from "vue-router";
import {storeToRefs} from "pinia";
import {useAtividadesStore} from "@/stores/atividades";
import {useUnidadesStore} from "@/stores/unidades";
import {useProcessosStore} from "@/stores/processos";
import {usePerfilStore} from "@/stores/perfil";
import {useAnalisesStore} from "@/stores/analises";
import {useMapasStore} from "@/stores/mapas";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {
    type AceitarCadastroRequest,
    type AnaliseCadastro,
    type AnaliseValidacao,
    type Atividade,
    type DevolverCadastroRequest,
    type HomologarCadastroRequest,
    type ImpactoMapa,
    Perfil,
    SituacaoSubprocesso,
    TipoProcesso,
    type Unidade,
} from "@/types/tipos";

type Analise = AnaliseCadastro | AnaliseValidacao;

export interface UseVisAtividades {
    // Estado
    atividades: ComputedRef<Atividade[]>;
    siglaUnidade: ComputedRef<string>;
    nomeUnidade: ComputedRef<string>;
    isRevisao: ComputedRef<boolean>;
    isHomologacao: ComputedRef<boolean>;
    podeVerImpacto: ComputedRef<boolean>;
    codSubprocesso: ComputedRef<number | undefined>;
    historicoAnalises: ComputedRef<Analise[]>;
    perfilSelecionado: ComputedRef<Perfil | null>;
    Perfil: typeof Perfil;

    // Modais
    impactoMapa: Ref<ImpactoMapa | null>;
    loadingImpacto: Ref<boolean>;
    mostrarModalImpacto: Ref<boolean>;
    mostrarModalHistoricoAnalise: Ref<boolean>;
    mostrarModalValidar: Ref<boolean>;
    mostrarModalDevolver: Ref<boolean>;
    observacaoValidacao: Ref<string>;
    observacaoDevolucao: Ref<string>;

    // Ações de validação/devolução
    loadingValidacao: Ref<boolean>;
    loadingDevolucao: Ref<boolean>;

    // Métodos
    abrirModalImpacto: () => Promise<void>;
    fecharModalImpacto: () => void;
    abrirModalHistoricoAnalise: () => Promise<void>;
    fecharModalHistoricoAnalise: () => void;
    validarCadastro: () => void;
    devolverCadastro: () => void;
    confirmarValidacao: () => Promise<void>;
    confirmarDevolucao: () => Promise<void>;
    fecharModalValidar: () => void;
    fecharModalDevolver: () => void;
}

/**
 * Composable unificado para visualização de atividades.
 * 
 * Consolida funcionalidades de:
 * - Estado do subprocesso e atividades
 * - Validação e homologação de cadastro
 * - Devolução de cadastro
 * - Gerenciamento de modais
 * - Visualização de impacto no mapa
 */
export function useVisAtividades(props: { codProcesso: number | string; sigla: string }): UseVisAtividades {
    // Stores
    const router = useRouter();
    const atividadesStore = useAtividadesStore();
    const unidadesStore = useUnidadesStore();
    const processosStore = useProcessosStore();
    const perfilStore = usePerfilStore();
    const analisesStore = useAnalisesStore();
    const mapasStore = useMapasStore();
    const subprocessosStore = useSubprocessosStore();
    const { impactoMapa } = storeToRefs(mapasStore);

    // Estado base
    const unidadeId = computed(() => props.sigla);
    const codProcesso = computed(() => Number(props.codProcesso));

    const unidade = computed(() => {
        function buscarUnidade(unidades: Unidade[], sigla: string): Unidade | undefined {
            for (const u of unidades) {
                if (u.sigla === sigla) return u;
                if (u.filhas && u.filhas.length) {
                    const encontrada = buscarUnidade(u.filhas, sigla);
                    if (encontrada) return encontrada;
                }
            }
        }
        return buscarUnidade(unidadesStore.unidades as Unidade[], unidadeId.value);
    });

    const siglaUnidade = computed(() => unidade.value?.sigla || unidadeId.value);
    const nomeUnidade = computed(() => (unidade.value?.nome ? `${unidade.value.nome}` : ""));
    const perfilSelecionado = computed(() => perfilStore.perfilSelecionado);

    const subprocesso = computed(() => {
        if (!processosStore.processoDetalhe) return null;
        return processosStore.processoDetalhe.unidades.find((u) => u.sigla === unidadeId.value);
    });

    const isHomologacao = computed(() => {
        if (!subprocesso.value) return false;
        const { situacaoSubprocesso } = subprocesso.value;
        const perfil = perfilSelecionado.value;
        return (
            perfil === Perfil.ADMIN &&
            (situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
                situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO ||
                situacaoSubprocesso === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA ||
                situacaoSubprocesso === SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA)
        );
    });

    const podeVerImpacto = computed(() => {
        if (!subprocesso.value || !perfilSelecionado.value) return false;
        const perfil = perfilSelecionado.value;
        const podeVer = perfil === Perfil.GESTOR || perfil === Perfil.ADMIN;
        const situacaoCorreta =
            subprocesso.value.situacaoSubprocesso === SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO ||
            subprocesso.value.situacaoSubprocesso === SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA;
        return podeVer && situacaoCorreta;
    });

    const codSubprocesso = computed(() => subprocesso.value?.codSubprocesso);

    const atividades = computed(() => {
        if (codSubprocesso.value === undefined) return [];
        return atividadesStore.obterAtividadesPorSubprocesso(codSubprocesso.value) || [];
    });

    const processoAtual = computed(() => processosStore.processoDetalhe);
    const isRevisao = computed(() => processoAtual.value?.tipo === TipoProcesso.REVISAO);

    const historicoAnalises = computed(() => {
        if (!codSubprocesso.value) return [];
        return analisesStore.obterAnalisesPorSubprocesso(codSubprocesso.value);
    });

    // Modais
    const loadingImpacto = ref(false);
    const mostrarModalImpacto = ref(false);
    const mostrarModalValidar = ref(false);
    const mostrarModalDevolver = ref(false);
    const mostrarModalHistoricoAnalise = ref(false);
    const observacaoValidacao = ref("");
    const observacaoDevolucao = ref("");

    // Ações de validação/devolução
    const loadingValidacao = ref(false);
    const loadingDevolucao = ref(false);

    // ========== Modais ==========

    async function abrirModalImpacto() {
        mostrarModalImpacto.value = true;
        if (codSubprocesso.value) {
            loadingImpacto.value = true;
            try {
                await mapasStore.buscarImpactoMapa(codSubprocesso.value);
            } finally {
                loadingImpacto.value = false;
            }
        }
    }

    function fecharModalImpacto() {
        mostrarModalImpacto.value = false;
    }

    async function abrirModalHistoricoAnalise() {
        if (codSubprocesso.value) {
            await analisesStore.buscarAnalisesCadastro(codSubprocesso.value);
        }
        mostrarModalHistoricoAnalise.value = true;
    }

    function fecharModalHistoricoAnalise() {
        mostrarModalHistoricoAnalise.value = false;
    }

    function validarCadastro() {
        mostrarModalValidar.value = true;
    }

    function devolverCadastro() {
        mostrarModalDevolver.value = true;
    }

    function fecharModalValidar() {
        mostrarModalValidar.value = false;
        observacaoValidacao.value = "";
    }

    function fecharModalDevolver() {
        mostrarModalDevolver.value = false;
        observacaoDevolucao.value = "";
    }

    // ========== CRUD - Validação ==========

    async function confirmarValidacao() {
        if (!codSubprocesso.value) return;

        loadingValidacao.value = true;
        try {
            let sucesso: boolean;

            if (isHomologacao.value) {
                const req: HomologarCadastroRequest = {
                    observacoes: observacaoValidacao.value,
                };
                if (isRevisao.value) {
                    sucesso = await subprocessosStore.homologarRevisaoCadastro(codSubprocesso.value, req);
                } else {
                    sucesso = await subprocessosStore.homologarCadastro(codSubprocesso.value, req);
                }

                if (sucesso) {
                    fecharModalValidar();
                    await router.push({
                        name: "Subprocesso",
                        params: {
                            codProcesso: props.codProcesso,
                            siglaUnidade: props.sigla,
                        },
                    });
                }
            } else {
                const req: AceitarCadastroRequest = {
                    observacoes: observacaoValidacao.value,
                };
                if (isRevisao.value) {
                    sucesso = await subprocessosStore.aceitarRevisaoCadastro(codSubprocesso.value, req);
                } else {
                    sucesso = await subprocessosStore.aceitarCadastro(codSubprocesso.value, req);
                }

                if (sucesso) {
                    fecharModalValidar();
                    await router.push({ name: "Painel" });
                }
            }
        } finally {
            loadingValidacao.value = false;
        }
    }

    // ========== CRUD - Devolução ==========

    async function confirmarDevolucao() {
        if (!codSubprocesso.value) return;
        loadingDevolucao.value = true;

        try {
            const req: DevolverCadastroRequest = {
                observacoes: observacaoDevolucao.value,
            };

            let sucesso: boolean;
            if (isRevisao.value) {
                sucesso = await subprocessosStore.devolverRevisaoCadastro(codSubprocesso.value, req);
            } else {
                sucesso = await subprocessosStore.devolverCadastro(codSubprocesso.value, req);
            }

            if (sucesso) {
                fecharModalDevolver();
                await router.push("/painel");
            }
        } finally {
            loadingDevolucao.value = false;
        }
    }

    // ========== Inicialização ==========

    onMounted(async () => {
        await processosStore.buscarProcessoDetalhe(codProcesso.value);
        if (codSubprocesso.value) {
            await atividadesStore.buscarAtividadesParaSubprocesso(codSubprocesso.value);
        }
    });

    return {
        // Estado
        atividades,
        siglaUnidade,
        nomeUnidade,
        isRevisao,
        isHomologacao,
        podeVerImpacto,
        codSubprocesso,
        historicoAnalises,
        perfilSelecionado,
        Perfil,

        // Modais
        impactoMapa,
        loadingImpacto,
        mostrarModalImpacto,
        mostrarModalHistoricoAnalise,
        mostrarModalValidar,
        mostrarModalDevolver,
        observacaoValidacao,
        observacaoDevolucao,

        // Ações
        loadingValidacao,
        loadingDevolucao,

        // Métodos
        abrirModalImpacto,
        fecharModalImpacto,
        abrirModalHistoricoAnalise,
        fecharModalHistoricoAnalise,
        validarCadastro,
        devolverCadastro,
        confirmarValidacao,
        confirmarDevolucao,
        fecharModalValidar,
        fecharModalDevolver,
    };
}
