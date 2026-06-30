import {computed, onActivated, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {usePerfilStore} from "@/stores/perfil";
import {usePainelQuery} from "@/composables/usePainelQuery";
import {usePerfil} from "@/composables/usePerfil";
import {useToast} from "@/composables/useToast";
import {usePainelStore} from "@/stores/painel";
import type {Alerta, ProcessoResumo} from "@/types/tipos";
import * as painelService from "@/services/painelService";
import {TEXTOS} from "@/constants/textos";
import {formatarDataHoraBR, logger} from "@/utils";
import {normalizarErro} from "@/utils/apiError";

export function usePainelTela() {
    const perfilStore = usePerfilStore();
    const perfil = usePerfil();
    const painelStore = usePainelStore();
    const painelQuery = usePainelQuery();
    const {exibirPendente} = useToast();
    const carregandoPainel = ref(true);
    const exibindoCarregamentoPainel = computed(() => carregandoPainel.value || painelStore.precisaRecarregar);
    const router = useRouter();

    const criterio = ref<keyof ProcessoResumo>("descricao");
    const asc = ref(true);

    const processosOrdenados = computed(() => {
        const campo = criterio.value;
        const direcao = asc.value ? 1 : -1;
        return (painelQuery.data.value?.processos ?? []).toSorted((a, b) => {
            const va = a[campo] ?? "";
            const vb = b[campo] ?? "";
            if (va < vb) return -1 * direcao;
            if (va > vb) return 1 * direcao;
            return 0;
        });
    });

    const alertas = computed(() => painelQuery.data.value?.alertas ?? []);
    const carregamentoInicialConcluido = ref(false);

    function registrarFalhaBackground(contexto: string, error: unknown) {
        logger.warn(contexto, normalizarErro(error).mensagem);
    }

    function executarEmBackground<T>(acao: () => Promise<T>, contextoFalha: string) {
        void acao().catch((error: unknown) => {
            registrarFalhaBackground(contextoFalha, error);
        });
    }

    async function carregarDados() {
        const unidadeCodigo = perfilStore.unidadeSelecionada;
        if (!perfil.perfilSelecionado.value || !unidadeCodigo) {
            painelStore.marcarRecarregado();
            carregandoPainel.value = false;
            return;
        }

        carregandoPainel.value = true;
        try {
            const {data: bootstrap} = await painelQuery.refetch();
            if (bootstrap) {
                const codigosNaoLidos = bootstrap.alertas
                    .filter((a: Alerta) => !a.dataHoraLeitura && !painelStore.isMarcadoComoLido(a.codigo))
                    .map((a: Alerta) => a.codigo);
                if (codigosNaoLidos.length > 0) {
                    painelStore.registrarLeitura(codigosNaoLidos);
                    executarEmBackground(
                        () => painelService.marcarAlertasLidos(codigosNaoLidos),
                        "Falha ao marcar alertas como lidos em background:",
                    );
                }
            }
        } finally {
            painelStore.marcarRecarregado();
            carregandoPainel.value = false;
        }
    }

    function exibirToastPendente() {
        return exibirPendente();
    }

    onMounted(async () => {
        exibirToastPendente();
        await carregarDados();
        carregamentoInicialConcluido.value = true;
    });

    onActivated(async () => {
        if (!carregamentoInicialConcluido.value) return;
        exibirToastPendente();
        if (painelStore.precisaRecarregar) {
            await carregarDados();
            return;
        }
        executarEmBackground(
            () => painelQuery.refresh(),
            "Falha na recarga em background do painel:",
        );
    });

    function ordenarPor(campo: keyof ProcessoResumo) {
        if (criterio.value === campo) {
            asc.value = !asc.value;
        } else {
            criterio.value = campo;
            asc.value = true;
        }
    }

    function abrirDetalhesProcesso(processo: ProcessoResumo | undefined) {
        if (processo && processo.linkDestino) {
            void router.push(processo.linkDestino);
        }
    }

    const camposAlertas = [
        {
            key: "dataHora",
            label: TEXTOS.painel.CAMPOS_ALERTAS.DATA_HORA,
            sortable: false,
            formatter: ({value}: { value: unknown }) => {
                return (typeof value === "string" || value instanceof Date) ? formatarDataHoraBR(value) : "";
            }
        },
        {key: "mensagem", label: TEXTOS.painel.CAMPOS_ALERTAS.DESCRICAO},
        {
            key: "processo",
            label: TEXTOS.painel.CAMPOS_ALERTAS.PROCESSO,
            sortable: false,
            formatter: ({value}: { value: unknown }) => {
                if (typeof value === "string" && value.trim().length > 0) {
                    return value;
                }
                return "-";
            }
        },
        {key: "origem", label: TEXTOS.painel.CAMPOS_ALERTAS.ORIGEM},
    ];

    const rowClassAlerta = (item: Alerta | null, type = "row") => {
        if (!item || type !== "row") return "";
        return item.dataHoraLeitura ? "" : "fw-bold";
    };

    const rowAttrAlerta = (item: Alerta | null, type = "row") => {
        if (item && type === "row") {
            return {'data-testid': `row-alerta-${item.codigo}`};
        }
        return {};
    };

    return {
        perfil,
        router,
        criterio,
        asc,
        processosOrdenados,
        alertas,
        carregandoPainel: exibindoCarregamentoPainel,
        camposAlertas,
        rowClassAlerta,
        rowAttrAlerta,
        ordenarPor,
        abrirDetalhesProcesso,
        carregarDados
    };
}
