import {type ComputedRef, ref, type Ref} from "vue";
import type {Competencia, MapaCompleto, SalvarCompetenciaRequest} from "@/types/tipos";
import type {ErroNormalizado} from "@/utils/apiError";
import {normalizarErro} from "@/utils/apiError";
import logger from "@/utils/logger";

interface FluxoMapaCompetencias {
    adicionarCompetencia(codigoSubprocesso: number, request: SalvarCompetenciaRequest): Promise<MapaCompleto | undefined>;

    atualizarCompetencia(codigoSubprocesso: number, codigoCompetencia: number, request: SalvarCompetenciaRequest): Promise<MapaCompleto | undefined>;

    removerCompetencia(codigoSubprocesso: number, codigoCompetencia: number): Promise<MapaCompleto | undefined>;

    removerAtividadeDaCompetencia(codigoSubprocesso: number, codigoCompetencia: number, codigoAtividade: number): Promise<MapaCompleto | undefined>;
}

interface UseMapaCompetenciasMutacoesParams {
    codigoSubprocesso: Ref<number | null>;
    competencias: ComputedRef<Competencia[]>;
    fluxoMapa: FluxoMapaCompetencias;
    notify: (mensagem: string, variante: "danger" | "warning" | "success" | "info") => void;
    clearErrors: () => void;
    aplicarErroNormalizado: (error: ErroNormalizado | null) => void;
    sincronizarMapa: (mapaAtualizado: MapaCompleto | null | undefined) => void;
}

export function useMapaCompetenciasMutacoes({
                                                codigoSubprocesso,
                                                competencias,
                                                fluxoMapa,
                                                notify,
                                                clearErrors,
                                                aplicarErroNormalizado,
                                                sincronizarMapa,
                                            }: UseMapaCompetenciasMutacoesParams) {
    const competenciaSendoEditada = ref<Competencia | null>(null);
    const mostrarModalCriarNovaCompetencia = ref(false);
    const mostrarModalExcluirCompetencia = ref(false);
    const competenciaParaExcluir = ref<Competencia | null>(null);
    const loadingCompetencia = ref(false);
    const loadingExclusao = ref(false);

    async function executarComSubprocesso(callback: (codigoSubprocesso: number) => Promise<void>) {
        const codigo = codigoSubprocesso.value;
        if (!codigo) return;
        await callback(codigo);
    }

    async function executarOperacaoCompetencia(
        operacao: () => Promise<void>,
        tratarErro: (error: unknown) => void
    ): Promise<void> {
        try {
            await operacao();
        } catch (error) {
            logger.error(error);
            tratarErro(error);
        }
    }

    function handleErrors(error: unknown) {
        aplicarErroNormalizado(normalizarErro(error) as ErroNormalizado);
    }

    function abrirModalCriarNovaCompetencia(competenciaParaEditar: Competencia | null = null) {
        mostrarModalCriarNovaCompetencia.value = true;
        clearErrors();
        competenciaSendoEditada.value = competenciaParaEditar;
    }

    function abrirModalCriarLimpo() {
        abrirModalCriarNovaCompetencia();
    }

    function fecharModalCriarNovaCompetencia() {
        mostrarModalCriarNovaCompetencia.value = false;
        clearErrors();
    }

    function iniciarEdicaoCompetencia(competencia: Competencia) {
        abrirModalCriarNovaCompetencia(competencia);
    }

    async function adicionarCompetenciaEFecharModal(dados: { descricao: string; atividadesSelecionadas: number[] }) {
        if (loadingCompetencia.value) return;

        await executarComSubprocesso(async (codigo) => {
            const request: SalvarCompetenciaRequest = {
                descricao: dados.descricao,
                atividadesCodigos: dados.atividadesSelecionadas,
            };

            loadingCompetencia.value = true;
            try {
                await executarOperacaoCompetencia(async () => {
                    if (competenciaSendoEditada.value) {
                        sincronizarMapa(await fluxoMapa.atualizarCompetencia(codigo, competenciaSendoEditada.value.codigo, request));
                    } else {
                        sincronizarMapa(await fluxoMapa.adicionarCompetencia(codigo, request));
                    }
                    fecharModalCriarNovaCompetencia();
                }, handleErrors);
            } finally {
                loadingCompetencia.value = false;
            }
        });
    }

    function excluirCompetencia(codigo: number) {
        const competencia = competencias.value.find((item) => item.codigo === codigo);
        if (!competencia) return;

        competenciaParaExcluir.value = competencia;
        mostrarModalExcluirCompetencia.value = true;
    }

    async function confirmarExclusaoCompetencia() {
        if (!competenciaParaExcluir.value) return;

        await executarComSubprocesso(async (codigo) => {
            loadingExclusao.value = true;
            try {
                await executarOperacaoCompetencia(async () => {
                    sincronizarMapa(await fluxoMapa.removerCompetencia(codigo, competenciaParaExcluir.value!.codigo));
                    mostrarModalExcluirCompetencia.value = false;
                }, (error) => notify(normalizarErro(error).mensagem, "danger"));
            } finally {
                loadingExclusao.value = false;
            }
        });
    }

    async function removerAtividadeAssociada(competenciaId: number, codigoAtividade: number) {
        await executarComSubprocesso(async (codigo) => {
            await executarOperacaoCompetencia(async () => {
                sincronizarMapa(await fluxoMapa.removerAtividadeDaCompetencia(codigo, competenciaId, codigoAtividade));
            }, (error) => notify(normalizarErro(error).mensagem, "danger"));
        });
    }

    function fecharModalExcluirCompetencia() {
        mostrarModalExcluirCompetencia.value = false;
        competenciaParaExcluir.value = null;
    }

    return {
        competenciaSendoEditada,
        mostrarModalCriarNovaCompetencia,
        mostrarModalExcluirCompetencia,
        competenciaParaExcluir,
        loadingCompetencia,
        loadingExclusao,
        abrirModalCriarNovaCompetencia,
        abrirModalCriarLimpo,
        fecharModalCriarNovaCompetencia,
        iniciarEdicaoCompetencia,
        adicionarCompetenciaEFecharModal,
        excluirCompetencia,
        confirmarExclusaoCompetencia,
        removerAtividadeAssociada,
        fecharModalExcluirCompetencia,
    };
}
