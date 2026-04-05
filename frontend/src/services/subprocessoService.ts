import type {
    Analise,
    Atividade,
    AtividadeImpactada,
    CompetenciaImpactada,
    ContextoEdicaoSubprocesso,
    DisponibilizarMapaRequest,
    ImpactoMapa,
    MapaAjuste,
    MapaCompleto,
    MapaVisualizacao,
    SalvarAjustesRequest,
    SalvarCompetenciaRequest,
    SalvarMapaRequest,
    SubprocessoStatus,
    SubprocessoDetalheResponse,
    ValidacaoCadastro
} from "@/types/tipos";
import apiClient from "../axios-setup";

interface ImportarAtividadesRequest {
    codSubprocessoOrigem: number;
    codigosAtividades?: number[];
}

interface BuscarSubprocessoPorProcessoEUnidadeResponse {
    codigo: number;
}

interface ImpactoMapaResponse {
    temImpactos: boolean;
    inseridas?: AtividadeImpactada[];
    removidas?: AtividadeImpactada[];
    alteradas?: AtividadeImpactada[];
    competenciasImpactadas?: CompetenciaImpactada[];
    totalInseridas?: number;
    totalRemovidas?: number;
    totalAlteradas?: number;
    totalCompetenciasImpactadas?: number;
}

export async function importarAtividades(
    codSubprocessoDestino: number,
    codSubprocessoOrigem: number,
    codigosAtividades?: number[],
): Promise<{aviso?: string}> {
    const request: ImportarAtividadesRequest = {
        codSubprocessoOrigem: codSubprocessoOrigem,
        ...(codigosAtividades && codigosAtividades.length > 0 ? {codigosAtividades} : {}),
    };
    const response = await apiClient.post<{message: string; aviso?: string}>(
        `/subprocessos/${codSubprocessoDestino}/importar-atividades`,
        request,
    );
    return {aviso: response.data.aviso};
}

export async function listarAtividades(codSubprocesso: number): Promise<Atividade[]> {
    const response = await apiClient.get<ContextoEdicaoSubprocesso>(`/subprocessos/${codSubprocesso}/contexto-edicao`);
    return response.data.atividadesDisponiveis;
}

export async function listarAtividadesParaImportacao(codSubprocesso: number): Promise<Atividade[]> {
    const response = await apiClient.get<Atividade[]>(`/subprocessos/${codSubprocesso}/atividades-importacao`);
    return response.data;
}

export async function validarCadastro(codSubprocesso: number): Promise<ValidacaoCadastro> {
    const response = await apiClient.get<ValidacaoCadastro>(`/subprocessos/${codSubprocesso}/validar-cadastro`);
    return response.data;
}

export async function obterStatus(codSubprocesso: number): Promise<SubprocessoStatus> {
    const response = await apiClient.get<SubprocessoStatus>(`/subprocessos/${codSubprocesso}/status`);
    return response.data;
}

export async function buscarSubprocessoDetalhe(
    codSubprocesso: number,
    perfil: string,
    unidadeCodigo: number,
): Promise<SubprocessoDetalheResponse> {
    const response = await apiClient.get<SubprocessoDetalheResponse>(`/subprocessos/${codSubprocesso}`, {
        params: {perfil, unidadeUsuario: unidadeCodigo},
    });
    return response.data;
}

export async function buscarContextoEdicao(
    codSubprocesso: number,
    perfil: string,
    unidadeCodigo: number,
): Promise<ContextoEdicaoSubprocesso> {
    const response = await apiClient.get<ContextoEdicaoSubprocesso>(`/subprocessos/${codSubprocesso}/contexto-edicao`, {
        params: {perfil, unidadeUsuario: unidadeCodigo},
    });
    return response.data;
}

export async function buscarSubprocessoPorProcessoEUnidade(
    codProcesso: number,
    siglaUnidade: string,
): Promise<BuscarSubprocessoPorProcessoEUnidadeResponse> {
    const response = await apiClient.get<BuscarSubprocessoPorProcessoEUnidadeResponse>("/subprocessos/buscar", {
        params: {codProcesso, siglaUnidade},
    });
    return response.data;
}

// Mapa / Competencias

export async function obterMapaVisualizacao(
    codSubprocesso: number,
): Promise<MapaVisualizacao> {
    const response = await apiClient.get<MapaVisualizacao>(
        `/subprocessos/${codSubprocesso}/mapa-visualizacao`,
    );
    return response.data;
}

export async function verificarImpactosMapa(codSubprocesso: number): Promise<ImpactoMapa> {
    const response = await apiClient.get<ImpactoMapaResponse>(`/subprocessos/${codSubprocesso}/impactos-mapa`);
    const data = response.data;

    return {
        temImpactos: data.temImpactos,
        atividadesInseridas: data.inseridas ?? [],
        atividadesRemovidas: data.removidas ?? [],
        atividadesAlteradas: data.alteradas ?? [],
        competenciasImpactadas: data.competenciasImpactadas ?? [],
        totalAtividadesInseridas: data.totalInseridas ?? 0,
        totalAtividadesRemovidas: data.totalRemovidas ?? 0,
        totalAtividadesAlteradas: data.totalAlteradas ?? 0,
        totalCompetenciasImpactadas: data.totalCompetenciasImpactadas ?? 0
    };
}

export async function obterMapaCompleto(codSubprocesso: number): Promise<MapaCompleto> {
    const response = await apiClient.get<MapaCompleto>(`/subprocessos/${codSubprocesso}/mapa-completo`);
    return response.data;
}

export async function salvarMapaCompleto(
    codSubprocesso: number,
    data: SalvarMapaRequest,
): Promise<MapaCompleto> {
    const response = await apiClient.post<MapaCompleto>(
        `/subprocessos/${codSubprocesso}/mapa-completo`,
        data,
    );
    return response.data;
}

export async function obterMapaAjuste(codSubprocesso: number): Promise<MapaAjuste> {
    const response = await apiClient.get<MapaAjuste>(`/subprocessos/${codSubprocesso}/mapa-ajuste`);
    return response.data;
}

export async function salvarMapaAjuste(
    codSubprocesso: number,
    data: SalvarAjustesRequest,
): Promise<void> {
    await apiClient.post(
        `/subprocessos/${codSubprocesso}/mapa-ajuste/atualizar`,
        data,
    );
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

// Acoes em Bloco

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

// Analises / Historico

export const listarAnalisesCadastro = async (
    codSubprocesso: number,
): Promise<Analise[]> => {
    const response = await apiClient.get<Analise[]>(
        `/subprocessos/${codSubprocesso}/historico-cadastro`,
    );
    return response.data;
};

export const listarAnalisesValidacao = async (
    codSubprocesso: number,
): Promise<Analise[]> => {
    const response = await apiClient.get(
        `/subprocessos/${codSubprocesso}/historico-validacao`,
    );
    return response.data as Analise[];
};
