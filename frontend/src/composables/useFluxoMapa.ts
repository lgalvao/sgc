import {
    adicionarCompetencia as adicionarCompetenciaService,
    atualizarCompetencia as atualizarCompetenciaService,
    disponibilizarMapa as disponibilizarMapaService,
    removerCompetencia as removerCompetenciaService,
    salvarMapaAjuste,
    salvarMapaCompleto,
} from "@/services/subprocessoService";
import type {
    DisponibilizarMapaRequest,
    MapaCompleto,
    SalvarAjustesRequest,
    SalvarCompetenciaRequest,
    SalvarMapaRequest
} from "@/types/tipos";
import {useAsyncAction} from "@/composables/useAsyncAction";
import {useErrorHandler} from "@/composables/useErrorHandler";

export function useFluxoMapa() {
    const {lastError, clearError} = useErrorHandler();
    const {carregando, erro, executar} = useAsyncAction();

    async function salvarMapa(codSubprocesso: number, dados: SalvarMapaRequest): Promise<MapaCompleto | undefined> {
        return executar(async () => {
            return await salvarMapaCompleto(codSubprocesso, dados);
        }, "Erro ao salvar mapa completo.");
    }

    async function salvarAjustes(codSubprocesso: number, request: SalvarAjustesRequest) {
        await executar(async () => {
            await salvarMapaAjuste(codSubprocesso, request);
        }, "Erro ao salvar ajustes do mapa.");
    }

    async function disponibilizarMapa(codSubprocesso: number, request: DisponibilizarMapaRequest) {
        await executar(async () => {
            await disponibilizarMapaService(codSubprocesso, request);
        }, "Erro ao disponibilizar mapa.");
    }

    async function adicionarCompetencia(codSubprocesso: number, competencia: SalvarCompetenciaRequest): Promise<MapaCompleto | undefined> {
        return executar(async () => {
            return await adicionarCompetenciaService(codSubprocesso, competencia);
        }, "Erro ao adicionar competência.");
    }

    async function atualizarCompetencia(codSubprocesso: number, codCompetencia: number, competencia: SalvarCompetenciaRequest): Promise<MapaCompleto | undefined> {
        return executar(async () => {
            return await atualizarCompetenciaService(codSubprocesso, codCompetencia, competencia);
        }, "Erro ao atualizar competência.");
    }

    async function removerCompetencia(codSubprocesso: number, codCompetencia: number): Promise<MapaCompleto | undefined> {
        return executar(async () => {
            return await removerCompetenciaService(codSubprocesso, codCompetencia);
        }, "Erro ao remover competência.");
    }

    return {
        carregando,
        erro,
        lastError,
        clearError,
        salvarMapa,
        salvarAjustes,
        disponibilizarMapa,
        adicionarCompetencia,
        atualizarCompetencia,
        removerCompetencia,
    };
}
