import type {
    DisponibilizarMapaRequest,
    ImpactoMapa,
    MapaAjuste,
    MapaCompleto,
    SalvarAjustesRequest,
    SalvarCompetenciaRequest,
    SalvarMapaRequest,
} from "@/types/tipos";
import apiClient from "../axios-setup";
import {
    caminhoSubprocesso,
    type ImpactoMapaResponse,
    mapearPayloadCompetencia,
    postarAcaoEmBloco,
} from "./subprocessoServiceBase";

const obter = async <T>(caminho: string): Promise<T> => (await apiClient.get<T>(caminho)).data;
const postar = async <T>(caminho: string, payload?: object): Promise<T> => (await apiClient.post<T>(caminho, payload)).data;

export async function obterSugestoesMapa(codSubprocesso: number): Promise<string> { return (await obter<{sugestoes: string}>(caminhoSubprocesso(codSubprocesso, "/sugestoes"))).sugestoes; }
export async function apresentarSugestoes(codSubprocesso: number, dados: { sugestoes: string }): Promise<void> { await postar(caminhoSubprocesso(codSubprocesso, "/apresentar-sugestoes"), {texto: dados.sugestoes}); }

export async function verificarImpactosMapa(codSubprocesso: number): Promise<ImpactoMapa> {
    const data = await obter<ImpactoMapaResponse>(caminhoSubprocesso(codSubprocesso, "/impactos-mapa"));
    return {
        temImpactos: data.temImpactos,
        atividadesInseridas: data.inseridas,
        atividadesRemovidas: data.removidas,
        atividadesAlteradas: data.alteradas,
        competenciasImpactadas: data.competenciasImpactadas,
        totalAtividadesInseridas: data.totalInseridas,
        totalAtividadesRemovidas: data.totalRemovidas,
        totalAtividadesAlteradas: data.totalAlteradas,
        totalCompetenciasImpactadas: data.totalCompetenciasImpactadas,
    };
}

export async function obterMapaCompleto(codSubprocesso: number): Promise<MapaCompleto> { return obter<MapaCompleto>(caminhoSubprocesso(codSubprocesso, "/mapa-completo")); }
export async function salvarMapaCompleto(codSubprocesso: number, data: SalvarMapaRequest): Promise<MapaCompleto> { return postar<MapaCompleto>(caminhoSubprocesso(codSubprocesso, "/mapa-completo"), data); }
export async function obterMapaAjuste(codSubprocesso: number): Promise<MapaAjuste> { return obter<MapaAjuste>(caminhoSubprocesso(codSubprocesso, "/mapa-ajuste")); }
export async function salvarMapaAjuste(codSubprocesso: number, data: SalvarAjustesRequest): Promise<void> { await postar(caminhoSubprocesso(codSubprocesso, "/mapa-ajuste/atualizar"), data); }
export async function disponibilizarMapa(codSubprocesso: number, data: DisponibilizarMapaRequest): Promise<void> { await postar(caminhoSubprocesso(codSubprocesso, "/disponibilizar-mapa"), data); }
export async function validarMapa(codSubprocesso: number): Promise<void> { await postar(caminhoSubprocesso(codSubprocesso, "/validar-mapa")); }
export async function homologarValidacao(codSubprocesso: number, dados: { texto: string }): Promise<void> { await postar(caminhoSubprocesso(codSubprocesso, "/homologar-validacao"), dados); }
export async function aceitarValidacao(codSubprocesso: number, dados: { texto: string }): Promise<void> { await postar(caminhoSubprocesso(codSubprocesso, "/aceitar-validacao"), dados); }
export async function devolverValidacao(codSubprocesso: number, dados: { justificativa: string }): Promise<void> { await postar(caminhoSubprocesso(codSubprocesso, "/devolver-validacao"), dados); }

export async function adicionarCompetencia(
    codSubprocesso: number,
    competencia: SalvarCompetenciaRequest,
): Promise<MapaCompleto> {
    return postar<MapaCompleto>(caminhoSubprocesso(codSubprocesso, "/competencia"), mapearPayloadCompetencia(competencia));
}

export async function atualizarCompetencia(
    codSubprocesso: number,
    codCompetencia: number,
    competencia: SalvarCompetenciaRequest,
): Promise<MapaCompleto> {
    return postar<MapaCompleto>(caminhoSubprocesso(codSubprocesso, `/competencia/${codCompetencia}`), mapearPayloadCompetencia(competencia));
}

export async function removerCompetencia(codSubprocesso: number, codCompetencia: number): Promise<MapaCompleto> {
    return postar<MapaCompleto>(caminhoSubprocesso(codSubprocesso, `/competencia/${codCompetencia}/remover`));
}

export async function aceitarCadastroEmBloco(payload: { unidadeCodigos: number[] }): Promise<void> {
    await postarAcaoEmBloco("aceitar-cadastro-bloco", payload);
}

export async function homologarCadastroEmBloco(payload: { unidadeCodigos: number[] }): Promise<void> {
    await postarAcaoEmBloco("homologar-cadastro-bloco", payload);
}

export async function aceitarValidacaoEmBloco(payload: { unidadeCodigos: number[] }): Promise<void> {
    await postarAcaoEmBloco("aceitar-validacao-bloco", payload);
}

export async function homologarValidacaoEmBloco(payload: { unidadeCodigos: number[] }): Promise<void> {
    await postarAcaoEmBloco("homologar-validacao-bloco", payload);
}

export async function disponibilizarMapaEmBloco(
    payload: { unidadeCodigos: number[]; dataLimite?: string },
): Promise<void> {
    await postarAcaoEmBloco("disponibilizar-mapa-bloco", payload);
}
