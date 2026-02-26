import type {
    Atividade,
    Competencia,
    MapaCompleto,
    ValidacaoCadastro,
    DisponibilizarMapaRequest,
    ImpactoMapa,
    MapaAjuste,
    MapaVisualizacao,
    AnaliseCadastro,
    AnaliseValidacao,
    CompetenciaImpactada,
    CompetenciaCompleta,
    SubprocessoDetalhe,
    Unidade
} from "@/types/tipos";
import {getOrNull} from "@/utils/apiError";
import apiClient from "../axios-setup";

interface ImportarAtividadesRequest {
    codSubprocessoOrigem: number;
}


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
    return (response.data.atividadesDisponiveis || []);
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
): Promise<SubprocessoDetalhe> {
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}`, {
        params: {perfil, unidadeUsuario: unidadeCodigo},
    });
    return mapSubprocessoDetalhe(response.data);
}

export async function buscarContextoEdicao(
    codSubprocesso: number,
    perfil: string,
    unidadeCodigo: number,
) {
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}/contexto-edicao`, {
        params: {perfil, unidadeUsuario: unidadeCodigo},
    });
    const data = response.data;
    if (data.detalhes || data.subprocesso) {
        data.detalhes = mapSubprocessoDetalhe(data.detalhes || data);
    }
    if (data.mapa) {
        data.mapa = mapMapaCompleto(data.mapa);
    }
    if (data.unidade) {
        data.unidade = mapUnidade(data.unidade);
    }
    return data;
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

export async function adicionarCompetencia(
    codSubprocesso: number,
    competencia: Competencia,
): Promise<MapaCompleto> {
    const requestBody = {
        descricao: competencia.descricao,
        atividadesIds: competencia.atividadesAssociadas,
    };
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/competencia`,
        requestBody,
    );
    return mapMapaCompleto(response.data);
}

export async function atualizarCompetencia(
    codSubprocesso: number,
    competencia: Competencia,
): Promise<MapaCompleto> {
    const requestBody = {
        descricao: competencia.descricao,
        atividadesIds: competencia.atividadesAssociadas,
    };
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/competencia/${competencia.codigo}`,
        requestBody,
    );
    return mapMapaCompleto(response.data);
}

export async function removerCompetencia(
    codSubprocesso: number,
    codCompetencia: number,
): Promise<MapaCompleto> {
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/competencia/${codCompetencia}/remover`,
    );
    return mapMapaCompleto(response.data);
}

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

// Métodos vindos do mapaService
export async function obterMapaVisualizacao(
    codSubprocesso: number,
): Promise<MapaVisualizacao> {
    const response = await apiClient.get<MapaVisualizacao>(
        `/subprocessos/${codSubprocesso}/mapa-visualizacao`,
    );
    return response.data;
}

export async function verificarImpactosMapa(codSubprocesso: number): Promise<ImpactoMapa> {
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}/impactos-mapa`);
    const dto = response.data;
    const inseridas = dto.inseridas || [];
    const removidas = dto.removidas || [];
    const alteradas = dto.alteradas || [];
    const competencias = dto.competenciasImpactadas || [];

    return {
        temImpactos: dto.temImpactos,
        totalAtividadesInseridas: inseridas.length,
        totalAtividadesRemovidas: removidas.length,
        totalAtividadesAlteradas: alteradas.length,
        totalCompetenciasImpactadas: competencias.length,
        atividadesInseridas: inseridas,
        atividadesRemovidas: removidas,
        atividadesAlteradas: alteradas,
        competenciasImpactadas: competencias.map(
            (c: any): CompetenciaImpactada => ({
                codigo: c.codigo,
                descricao: c.descricao,
                atividadesAfetadas: c.atividadesAfetadas || [],
                tiposImpacto: c.tiposImpacto,
            }),
        ),
    };
}

export async function obterMapaCompleto(codSubprocesso: number): Promise<MapaCompleto> {
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}/mapa-completo`);
    return mapMapaCompleto(response.data);
}

export async function salvarMapaCompleto(
    codSubprocesso: number,
    data: any,
): Promise<MapaCompleto> {
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/mapa-completo`,
        data,
    );
    return mapMapaCompleto(response.data);
}

export async function obterMapaAjuste(codSubprocesso: number): Promise<MapaAjuste> {
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}/mapa-ajuste`);
    return response.data;
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

// Métodos vindos do analiseService
export async function listarAnalisesCadastro(
    codSubprocesso: number,
): Promise<AnaliseCadastro[]> {
    const response = await apiClient.get(
        `/subprocessos/${codSubprocesso}/historico-cadastro`,
    );
    return response.data || [];
}

export async function listarAnalisesValidacao(
    codSubprocesso: number,
): Promise<AnaliseValidacao[]> {
    const response = await apiClient.get(
        `/subprocessos/${codSubprocesso}/historico-validacao`,
    );
    return response.data || [];
}

// Helpers internos para substituição de mappers
function mapMapaCompleto(dto: any): MapaCompleto {
    return {
        codigo: dto.codigo,
        subprocessoCodigo: dto.subprocessoCodigo,
        observacoes: dto.observacoes,
        competencias: (dto.competencias || []).map(
            (c: any): CompetenciaCompleta => ({
                codigo: c.codigo,
                descricao: c.descricao,
                atividadesAssociadas: c.atividadesCodigos || (c.atividades || []).map((a: any) => a.codigo) || [],
                atividades: (c.atividades || []).map((a: any) => ({
                    codigo: a.codigo,
                    descricao: a.descricao,
                    conhecimentos: (a.conhecimentos || []).map((k: any) => ({
                        codigo: k.codigo,
                        descricao: k.descricao,
                    })),
                })),
            }),
        ),
        situacao: dto.situacao || "",
    };
}

function mapSubprocessoDetalhe(dto: any): SubprocessoDetalhe {
    if (!dto) {
        throw new Error("DTO de detalhes do subprocesso é nulo ou indefinido.");
    }

    const sp = dto.subprocesso || dto || {};
    const unidadeDefault = { codigo: 0, nome: 'Não informada', sigla: 'N/I' };
    const unidade = mapUnidade(sp.unidade || dto.unidade || unidadeDefault);

    return {
        codigo: sp.codigo || dto.codigo || 0,
        unidade: unidade,
        titular: dto.titular || null,
        responsavel: dto.responsavel || null,
        situacao: sp.situacao || dto.situacao || 'NAO_INICIADO',
        localizacaoAtual: dto.localizacaoAtual || '',
        processoDescricao: (sp.processo?.descricao) || dto.processoDescricao || '',
        tipoProcesso: (sp.processo?.tipo) || dto.tipoProcesso || 'MAPEAMENTO',
        prazoEtapaAtual: sp.dataLimiteEtapa1 || dto.prazoEtapaAtual || '',
        isEmAndamento: typeof sp.isEmAndamento === 'boolean' ? sp.isEmAndamento : (dto.isEmAndamento || false),
        etapaAtual: sp.etapaAtual || dto.etapaAtual || 1,
        movimentacoes: dto.movimentacoes || [],
        elementosProcesso: dto.elementosProcesso || [],
        permissoes: dto.permissoes || {},
    } as SubprocessoDetalhe;
}

function mapUnidade(dto: any): Unidade {
    if (!dto) return dto;
    return {
        ...dto,
        filhas: (dto.subunidades || dto.filhas || []).map(mapUnidade)
    };
}
