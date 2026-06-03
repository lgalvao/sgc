import {type ComputedRef, ref} from "vue";
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
    obterCodigoSubprocessoObrigatorio: () => number;
    competencias: ComputedRef<Competencia[]>;
    fluxoMapa: FluxoMapaCompetencias;
    notify: (mensagem: string, variante: "danger" | "warning" | "success" | "info") => void;
    limparErros: () => void;
    aplicarErroNormalizado: (error: ErroNormalizado | null) => void;
    sincronizarMapa: (mapaAtualizado: MapaCompleto | null | undefined) => void;
}

export function useMapaCompetenciasMutacoes({
                                                obterCodigoSubprocessoObrigatorio,
                                                competencias,
                                                fluxoMapa,
                                                notify,
                                                limparErros,
                                                aplicarErroNormalizado,
                                                sincronizarMapa,
                                            }: UseMapaCompetenciasMutacoesParams) {
    const competenciaSendoEditada = ref<Competencia | null>(null);
    const mostrarModalCriarNovaCompetencia = ref(false);
    const mostrarModalExcluirCompetencia = ref(false);
    const competenciaParaExcluir = ref<Competencia | null>(null);
    const loadingCompetencia = ref(false);
    const loadingExclusao = ref(false);

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

    function tratarErros(error: unknown) {
        aplicarErroNormalizado(normalizarErro(error));
    }

    function abrirModalCriarNovaCompetencia(competenciaParaEditar: Competencia | null = null) {
        mostrarModalCriarNovaCompetencia.value = true;
        limparErros();
        competenciaSendoEditada.value = competenciaParaEditar;
    }

    function abrirModalCriarLimpo() {
        abrirModalCriarNovaCompetencia();
    }

    function fecharModalCriarNovaCompetencia() {
        mostrarModalCriarNovaCompetencia.value = false;
        limparErros();
    }

    function iniciarEdicaoCompetencia(competencia: Competencia) {
        abrirModalCriarNovaCompetencia(competencia);
    }

    async function adicionarCompetenciaEFecharModal(dados: { descricao: string; atividadesSelecionadas: number[] }) {
        if (loadingCompetencia.value) return;

        const codigo = obterCodigoSubprocessoObrigatorio();
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
            }, tratarErros);
        } finally {
            loadingCompetencia.value = false;
        }
    }

    function excluirCompetencia(codigo: number) {
        const competencia = competencias.value.find((item) => item.codigo === codigo);
        if (!competencia) return;

        competenciaParaExcluir.value = competencia;
        mostrarModalExcluirCompetencia.value = true;
    }

    async function confirmarExclusaoCompetencia() {
        const comp = competenciaParaExcluir.value;
        if (!comp) return;

        const codigo = obterCodigoSubprocessoObrigatorio();
        loadingExclusao.value = true;
        try {
            await executarOperacaoCompetencia(async () => {
                sincronizarMapa(await fluxoMapa.removerCompetencia(codigo, comp.codigo));
                mostrarModalExcluirCompetencia.value = false;
            }, (error) => notify(normalizarErro(error).mensagem, "danger"));
        } finally {
            loadingExclusao.value = false;
        }
    }

    async function removerAtividadeAssociada(codigoCompetencia: number, codigoAtividade: number) {
        const competencia = competencias.value.find((item) => item.codigo === codigoCompetencia);
        if (!competencia) {
            return;
        }

        const codigo = obterCodigoSubprocessoObrigatorio();
        await executarOperacaoCompetencia(async () => {
            sincronizarMapa(await fluxoMapa.removerAtividadeDaCompetencia(codigo, codigoCompetencia, codigoAtividade));
        }, (error) => notify(normalizarErro(error).mensagem, "danger"));
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
