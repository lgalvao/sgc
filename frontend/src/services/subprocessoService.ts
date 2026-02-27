import type {
    AnaliseCadastro,
    AnaliseValidacao,
    Atividade,
    DisponibilizarMapaRequest,
    ImpactoMapa,
    MapaAjuste,
    MapaCompleto,
    MapaVisualizacao,
    SubprocessoDetalhe,
    ValidacaoCadastro,
    SalvarCompetenciaRequest
} from "@/types/tipos";
import {getOrNull} from "@/utils/apiError";
import apiClient from "../axios-setup";

interface ImportarAtividadesRequest {
    codSubprocessoOrigem: number;
}

// ------------------------------------------------------------------------------------------------
// Subprocesso Services
// ------------------------------------------------------------------------------------------------

export async function importarAtividades(
    codSubprocessoDestino: number,
    codSubprocessoOrigem: number,
): Promise<void> {
    const request: ImportarAtividadesRequest = {
        codSubprocessoOrigem: codSubprocessoOrigem,
    };
    await apiClient.post(
        `/subprocessos/${codSubprocessoDestino}/importar-atividades`,
        request,
    );
}

export async function listarAtividades(codSubprocesso: number): Promise<Atividade[]> {
    const response = await apiClient.get<any>(`/subprocessos/${codSubprocesso}/contexto-edicao`);
    // Assuming backend returns Atividade[] compatible structure in atividadesDisponiveis
    // or we accept that strict typing might require adjustment if backend DTO differs significantly
    return response.data.atividadesDisponiveis as Atividade[];
}

export async function validarCadastro(codSubprocesso: number): Promise<ValidacaoCadastro> {
    const response = await apiClient.get<ValidacaoCadastro>(`/subprocessos/${codSubprocesso}/validar-cadastro`);
    return response.data;
}

export async function obterStatus(codSubprocesso: number): Promise<any> {
    const response = await apiClient.get<any>(`/subprocessos/${codSubprocesso}/status`);
    return response.data;
}

export async function buscarSubprocessoDetalhe(
    codSubprocesso: number,
    perfil: string,
    unidadeCodigo: number,
) {
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}`, {
        params: {perfil, unidadeUsuario: unidadeCodigo},
    });
    return response.data as SubprocessoDetalhe;
}

export async function buscarContextoEdicao(
    codSubprocesso: number,
    perfil: string,
    unidadeCodigo: number,
) {
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}/contexto-edicao`, {
        params: {perfil, unidadeUsuario: unidadeCodigo},
    });
    return response.data;
}

export async function buscarSubprocessoPorProcessoEUnidade(
    codProcesso: number,
    siglaUnidade: string,
) {
    const response = await apiClient.get("/subprocessos/buscar", {
        params: {codProcesso, siglaUnidade},
    });
    return response.data;
}

// ------------------------------------------------------------------------------------------------
// Mapa / Competencias
// ------------------------------------------------------------------------------------------------

export async function obterMapaVisualizacao(
    codSubprocesso: number,
): Promise<MapaVisualizacao> {
    const response = await apiClient.get<MapaVisualizacao>(
        `/subprocessos/${codSubprocesso}/mapa-visualizacao`,
    );
    return response.data;
}

export async function verificarImpactosMapa(codSubprocesso: number): Promise<ImpactoMapa> {
    const response = await apiClient.get<any>(`/subprocessos/${codSubprocesso}/impactos-mapa`);
    const data = response.data;

    // Mapeamento manual para garantir compatibilidade com a interface do frontend
    return {
        temImpactos: data.temImpactos,
        atividadesInseridas: data.inseridas || [],
        atividadesRemovidas: data.removidas || [],
        atividadesAlteradas: data.alteradas || [],
        competenciasImpactadas: data.competenciasImpactadas || [],
        totalAtividadesInseridas: data.totalInseridas || 0,
        totalAtividadesRemovidas: data.totalRemovidas || 0,
        totalAtividadesAlteradas: data.totalAlteradas || 0,
        totalCompetenciasImpactadas: data.totalCompetenciasImpactadas || 0
    };
}

export async function obterMapaCompleto(codSubprocesso: number): Promise<MapaCompleto> {
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}/mapa-completo`);
    return response.data as MapaCompleto;
}

export async function salvarMapaCompleto(
    codSubprocesso: number,
    data: any,
): Promise<MapaCompleto> {
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/mapa-completo`,
        data,
    );
    return response.data as MapaCompleto;
}

export async function obterMapaAjuste(codSubprocesso: number): Promise<MapaAjuste> {
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}/mapa-ajuste`);
    return response.data as MapaAjuste;
}

export async function salvarMapaAjuste(
    codSubprocesso: number,
    data: any,
): Promise<void> {
    await apiClient.post(
        `/subprocessos/${codSubprocesso}/mapa-ajuste/atualizar`,
        data,
    );
}

export async function verificarMapaVigente(
    codigoUnidade: number,
): Promise<boolean> {
    const result = await getOrNull(async () => {
        const response = await apiClient.get(
            `/unidades/${codigoUnidade}/mapa-vigente`,
        );
        return response.data.temMapaVigente;
    });
    return result ?? false;
}

export async function disponibilizarMapa(
    codSubprocesso: number,
    data: DisponibilizarMapaRequest,
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/disponibilizar-mapa`, data);
}

export async function adicionarCompetencia(
    codSubprocesso: number,
    competencia: SalvarCompetenciaRequest,
): Promise<MapaCompleto> {
    const requestBody = {
        descricao: competencia.descricao,
        atividadesIds: competencia.atividadesIds,
    };
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/competencia`,
        requestBody,
    );
    return response.data as MapaCompleto;
}

export async function atualizarCompetencia(
    codSubprocesso: number,
    codCompetencia: number,
    competencia: SalvarCompetenciaRequest,
): Promise<MapaCompleto> {
    const requestBody = {
        descricao: competencia.descricao,
        atividadesIds: competencia.atividadesIds,
    };
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/competencia/${codCompetencia}`,
        requestBody,
    );
    return response.data as MapaCompleto;
}

export async function removerCompetencia(
    codSubprocesso: number,
    codCompetencia: number,
): Promise<MapaCompleto> {
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/competencia/${codCompetencia}/remover`,
    );
    return response.data as MapaCompleto;
}

// ------------------------------------------------------------------------------------------------
// Acoes em Bloco
// ------------------------------------------------------------------------------------------------

export async function aceitarCadastroEmBloco(
    codSubprocesso: number,
    payload: { unidadeCodigos: number[] }
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/aceitar-cadastro-bloco`, {
        acao: 'ACEITAR',
        subprocessos: payload.unidadeCodigos
    });
}

export async function homologarCadastroEmBloco(
    codSubprocesso: number,
    payload: { unidadeCodigos: number[] }
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/homologar-cadastro-bloco`, {
        acao: 'HOMOLOGAR',
        subprocessos: payload.unidadeCodigos
    });
}

export async function aceitarValidacaoEmBloco(
    codSubprocesso: number,
    payload: { unidadeCodigos: number[] }
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/aceitar-validacao-bloco`, {
        acao: 'ACEITAR_VALIDACAO',
        subprocessos: payload.unidadeCodigos
    });
}

export async function homologarValidacaoEmBloco(
    codSubprocesso: number,
    payload: { unidadeCodigos: number[] }
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/homologar-validacao-bloco`, {
        acao: 'HOMOLOGAR_VALIDACAO',
        subprocessos: payload.unidadeCodigos
    });
}

export async function disponibilizarMapaEmBloco(
    codSubprocesso: number,
    payload: { unidadeCodigos: number[]; dataLimite?: string }
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/disponibilizar-mapa-bloco`, {
        acao: 'DISPONIBILIZAR',
        subprocessos: payload.unidadeCodigos,
        dataLimite: payload.dataLimite
    });
}

// ------------------------------------------------------------------------------------------------
// Analises / Historico
// ------------------------------------------------------------------------------------------------

export const listarAnalisesCadastro = async (
    codSubprocesso: number,
): Promise<AnaliseCadastro[]> => {
    const response = await apiClient.get(
        `/subprocessos/${codSubprocesso}/historico-cadastro`,
    );
    return response.data as AnaliseCadastro[];
};

export const listarAnalisesValidacao = async (
    codSubprocesso: number,
): Promise<AnaliseValidacao[]> => {
    const response = await apiClient.get(
        `/subprocessos/${codSubprocesso}/historico-validacao`,
    );
    return response.data as AnaliseValidacao[];
};
