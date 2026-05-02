import {computed, ref, type ComputedRef, type Ref} from "vue";
import type {Competencia, DisponibilizarMapaRequest, MapaCompleto} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import logger from "@/utils/logger";
import {normalizeError} from "@/utils/apiError";

type DependenciasMapaDisponibilizacao = {
    competencias: ComputedRef<Competencia[]>;
    existeCompetenciaSemAtividade: ComputedRef<boolean>;
    atividadesSemCompetencia: ComputedRef<{ codigo: number }[]>;
    mostrarModalDisponibilizar: Ref<boolean>;
    clearErrors: () => void;
    executarComSubprocesso: (callback: (codigoSubprocesso: number) => Promise<void>) => Promise<void>;
    disponibilizarMapaFluxo: (codigoSubprocesso: number, request: DisponibilizarMapaRequest) => Promise<void>;
    concluirAcaoPainel: (mensagem: string, fecharModal: () => void) => Promise<void>;
    aplicarErroNormalizado: (error: ReturnType<typeof normalizeError> | null) => void;
};

export function useMapaDisponibilizacao({
    competencias,
    existeCompetenciaSemAtividade,
    atividadesSemCompetencia,
    mostrarModalDisponibilizar,
    clearErrors,
    executarComSubprocesso,
    disponibilizarMapaFluxo,
    concluirAcaoPainel,
    aplicarErroNormalizado,
}: DependenciasMapaDisponibilizacao) {
    const notificacaoDisponibilizacao = ref("");
    const erroValidacaoMapa = ref("");
    const loadingDisponibilizacao = ref(false);

    const podeConfirmarDisponibilizacao = computed(() =>
        competencias.value.length > 0 && !existeCompetenciaSemAtividade.value && atividadesSemCompetencia.value.length === 0,
    );

    function limparErroMapa(erroMapa: Ref<string | null>) {
        erroValidacaoMapa.value = "";
        erroMapa.value = null;
    }

    function obterMensagemErroChecklistDisponibilizacao() {
        if (competencias.value.length === 0) return TEXTOS.mapa.ERRO_MAPA_SEM_COMPETENCIAS;
        if (existeCompetenciaSemAtividade.value) return TEXTOS.mapa.ERRO_COMPETENCIA_SEM_ATIVIDADE;
        if (atividadesSemCompetencia.value.length > 0) return TEXTOS.mapa.ERRO_ATIVIDADES_SEM_COMPETENCIA;
        return "";
    }

    function abrirModalDisponibilizar() {
        erroValidacaoMapa.value = "";
        if (!podeConfirmarDisponibilizacao.value) {
            erroValidacaoMapa.value = obterMensagemErroChecklistDisponibilizacao();
            return;
        }
        mostrarModalDisponibilizar.value = true;
        clearErrors();
    }

    function fecharModalDisponibilizar() {
        mostrarModalDisponibilizar.value = false;
        notificacaoDisponibilizacao.value = "";
        clearErrors();
    }

    async function disponibilizarMapa(request: DisponibilizarMapaRequest) {
        if (loadingDisponibilizacao.value) return;
        await executarComSubprocesso(async (codigoSubprocesso) => {
            loadingDisponibilizacao.value = true;
            try {
                await disponibilizarMapaFluxo(codigoSubprocesso, request);
                await concluirAcaoPainel(TEXTOS.sucesso.MAPA_DISPONIBILIZADO, fecharModalDisponibilizar);
            } catch (error) {
                logger.error(error);
                aplicarErroNormalizado(normalizeError(error) as ReturnType<typeof normalizeError>);
            } finally {
                loadingDisponibilizacao.value = false;
            }
        });
    }

    function sincronizarMapaContexto(
        mapaAtualizado: MapaCompleto | null | undefined,
        codigoSubprocesso: number | null,
        definirMapaCompleto: (codigo: number, mapa: MapaCompleto) => void,
        mapaContextoAtual: Ref<{ detalhes: { codigo: number }; mapa: MapaCompleto } | null>,
    ) {
        if (!mapaAtualizado || !codigoSubprocesso) return;
        definirMapaCompleto(codigoSubprocesso, mapaAtualizado);
        if (mapaContextoAtual.value?.detalhes.codigo === codigoSubprocesso) {
            mapaContextoAtual.value.mapa = mapaAtualizado;
        }
    }

    return {
        erroValidacaoMapa,
        loadingDisponibilizacao,
        notificacaoDisponibilizacao,
        abrirModalDisponibilizar,
        fecharModalDisponibilizar,
        disponibilizarMapa,
        limparErroMapa,
        sincronizarMapaContexto,
    };
}
