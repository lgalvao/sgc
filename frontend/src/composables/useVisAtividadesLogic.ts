import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { storeToRefs } from "pinia";
import { useAtividadesStore } from "@/stores/atividades";
import { useUnidadesStore } from "@/stores/unidades";
import { useProcessosStore } from "@/stores/processos";
import { useSubprocessosStore } from "@/stores/subprocessos";
import { usePerfilStore } from "@/stores/perfil";
import { useAnalisesStore } from "@/stores/analises";
import { useMapasStore } from "@/stores/mapas";
import {
    type AceitarCadastroRequest,
    type DevolverCadastroRequest,
    type HomologarCadastroRequest,
    Perfil,
    SituacaoSubprocesso,
    TipoProcesso,
    type Unidade,
} from "@/types/tipos";

export function useVisAtividadesLogic(props: { codProcesso: number | string; sigla: string }) {
    const router = useRouter();
    const atividadesStore = useAtividadesStore();
    const unidadesStore = useUnidadesStore();
    const processosStore = useProcessosStore();
    const subprocessosStore = useSubprocessosStore();
    const perfilStore = usePerfilStore();
    const analisesStore = useAnalisesStore();
    const mapasStore = useMapasStore();

    const { impactoMapa } = storeToRefs(mapasStore);
    const loadingImpacto = ref(false);

    const mostrarModalImpacto = ref(false);
    const mostrarModalValidar = ref(false);
    const mostrarModalDevolver = ref(false);
    const mostrarModalHistoricoAnalise = ref(false);
    const observacaoValidacao = ref("");
    const observacaoDevolucao = ref("");

    const loadingValidacao = ref(false);
    const loadingDevolucao = ref(false);

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

    onMounted(async () => {
        await processosStore.buscarProcessoDetalhe(codProcesso.value);
        if (codSubprocesso.value) {
            await atividadesStore.buscarAtividadesParaSubprocesso(codSubprocesso.value);
        }
    });

    async function confirmarValidacao() {
        if (!codSubprocesso.value || !perfilSelecionado.value) return;

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
                    mostrarModalValidar.value = false;
                    observacaoValidacao.value = "";
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
                    mostrarModalValidar.value = false;
                    observacaoValidacao.value = "";
                    await router.push({ name: "Painel" });
                }
            }
        } finally {
            loadingValidacao.value = false;
        }
    }

    async function confirmarDevolucao() {
        if (!codSubprocesso.value || !perfilSelecionado.value) return;
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
                mostrarModalDevolver.value = false;
                observacaoDevolucao.value = "";
                await router.push("/painel");
            }
        } finally {
            loadingDevolucao.value = false;
        }
    }

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

    async function abrirModalHistoricoAnalise() {
        if (codSubprocesso.value) {
            await analisesStore.buscarAnalisesCadastro(codSubprocesso.value);
        }
        mostrarModalHistoricoAnalise.value = true;
    }

    return {
        atividades,
        siglaUnidade,
        nomeUnidade,
        isRevisao,
        isHomologacao,
        podeVerImpacto,
        codSubprocesso,
        impactoMapa,
        loadingImpacto,
        mostrarModalImpacto,
        historicoAnalises,
        mostrarModalHistoricoAnalise,
        mostrarModalValidar,
        loadingValidacao,
        observacaoValidacao,
        mostrarModalDevolver,
        loadingDevolucao,
        observacaoDevolucao,
        perfilSelecionado,
        Perfil,
        abrirModalImpacto,
        fecharModalImpacto: () => (mostrarModalImpacto.value = false),
        abrirModalHistoricoAnalise,
        fecharModalHistoricoAnalise: () => (mostrarModalHistoricoAnalise.value = false),
        validarCadastro: () => (mostrarModalValidar.value = true),
        devolverCadastro: () => (mostrarModalDevolver.value = true),
        confirmarValidacao,
        confirmarDevolucao,
        fecharModalValidar: () => {
            mostrarModalValidar.value = false;
            observacaoValidacao.value = "";
        },
        fecharModalDevolver: () => {
            mostrarModalDevolver.value = false;
            observacaoDevolucao.value = "";
        },
    };
}
