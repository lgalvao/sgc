import {ref, type ComputedRef, type Ref} from "vue";
import type {Competencia, MapaCompleto, SalvarCompetenciaRequest} from "@/types/tipos";
import type {NormalizedError} from "@/utils/apiError";
import {normalizeError} from "@/utils/apiError";
import logger from "@/utils/logger";

interface FluxoMapaCompetencias {
    lastError: Ref<unknown>;
    clearError: () => void;
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
    aplicarErroNormalizado: (error: NormalizedError | null) => void;
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

    function handleErrors() {
        aplicarErroNormalizado(fluxoMapa.lastError.value as NormalizedError | null);
    }

    function abrirModalCriarNovaCompetencia(competenciaParaEditar: Competencia | null = null) {
        mostrarModalCriarNovaCompetencia.value = true;
        clearErrors();
        fluxoMapa.clearError();
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
                if (competenciaSendoEditada.value) {
                    sincronizarMapa(await fluxoMapa.atualizarCompetencia(codigo, competenciaSendoEditada.value.codigo, request));
                } else {
                    sincronizarMapa(await fluxoMapa.adicionarCompetencia(codigo, request));
                }
                fecharModalCriarNovaCompetencia();
            } catch (error) {
                logger.error(error);
                handleErrors();
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
                sincronizarMapa(await fluxoMapa.removerCompetencia(codigo, competenciaParaExcluir.value!.codigo));
                mostrarModalExcluirCompetencia.value = false;
            } catch (error) {
                logger.error(error);
                notify(normalizeError(error).message, "danger");
            } finally {
                loadingExclusao.value = false;
            }
        });
    }

    async function removerAtividadeAssociada(competenciaId: number, codigoAtividade: number) {
        await executarComSubprocesso(async (codigo) => {
            try {
                sincronizarMapa(await fluxoMapa.removerAtividadeDaCompetencia(codigo, competenciaId, codigoAtividade));
            } catch (error) {
                logger.error(error);
                notify(normalizeError(error).message, "danger");
            }
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
