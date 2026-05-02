import {
    adicionarCompetencia as adicionarCompetenciaService,
    aceitarValidacao as aceitarValidacaoService,
    atualizarCompetencia as atualizarCompetenciaService,
    devolverValidacao as devolverValidacaoService,
    disponibilizarMapa as disponibilizarMapaService,
    homologarValidacao as homologarValidacaoService,
    removerCompetencia as removerCompetenciaService,
    salvarMapaAjuste,
    salvarMapaCompleto,
    validarMapa as validarMapaService,
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
import {useMapas} from "@/composables/useMapas";

export function useFluxoMapa() {
    const {lastError, clearError} = useErrorHandler();
    const {carregando, erro, executar} = useAsyncAction();
    const mapasStore = useMapas();

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

    async function removerAtividadeDaCompetencia(codSubprocesso: number, codCompetencia: number, codAtividade: number): Promise<MapaCompleto | undefined> {
        return executar(async () => {
            const mapa = typeof mapasStore.obterMapaCompletoCache === "function"
                ? mapasStore.obterMapaCompletoCache(codSubprocesso)
                : mapasStore.mapaCompleto.value;
            const competencia = mapa?.competencias.find(c => c.codigo === codCompetencia);
            if (!competencia) throw new Error("Competência não encontrada.");

            const atividadesCodigos = (competencia.atividades || [])
                .map(a => a.codigo)
                .filter(id => id !== codAtividade);

            const request: SalvarCompetenciaRequest = {
                descricao: competencia.descricao,
                atividadesCodigos
            };

            return await atualizarCompetenciaService(codSubprocesso, codCompetencia, request);
        }, "Erro ao remover atividade da competência.");
    }

    async function validarMapa(codSubprocesso: number): Promise<void> {
        await executar(async () => {
            await validarMapaService(codSubprocesso);
        }, "Erro ao validar mapa.");
    }

    async function aceitarMapa(codSubprocesso: number, dados: { observacao: string }): Promise<void> {
        await executar(async () => {
            await aceitarValidacaoService(codSubprocesso, { texto: dados.observacao });
        }, "Erro ao aceitar mapa.");
    }

    async function homologarMapa(codSubprocesso: number, dados: { observacao: string }): Promise<void> {
        await executar(async () => {
            await homologarValidacaoService(codSubprocesso, { texto: dados.observacao });
        }, "Erro ao homologar mapa.");
    }

    async function devolverMapa(codSubprocesso: number, dados: { justificativa: string }): Promise<void> {
        await executar(async () => {
            await devolverValidacaoService(codSubprocesso, dados);
        }, "Erro ao devolver mapa.");
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
        removerAtividadeDaCompetencia,
        validarMapa,
        aceitarMapa,
        homologarMapa,
        devolverMapa
    };
}
