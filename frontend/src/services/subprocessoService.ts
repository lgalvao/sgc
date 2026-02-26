import type {
    AnaliseCadastro,
    AnaliseValidacao,
    Atividade,
    AtividadeOperacaoResponse,
    Competencia,
    CompetenciaCompleta,
    CompetenciaImpactada,
    Conhecimento,
    CriarConhecimentoRequest,
    DisponibilizarMapaRequest,
    ImpactoMapa,
    Mapa,
    MapaAjuste,
    MapaCompleto,
    MapaVisualizacao,
    SubprocessoDetalhe,
    SubprocessoStatus,
    ValidacaoCadastro
} from "@/types/tipos";
import type {
    AtividadeDto,
    AtividadeImpactadaDto,
    AtividadeOperacaoResponseDto,
    ConhecimentoDto,
    ImpactoMapaDto,
    SubprocessoSituacaoDto
} from "@/types/dtos";
import {getOrNull} from "@/utils/apiError";
import apiClient from "../axios-setup";

interface ImportarAtividadesRequest {
    codSubprocessoOrigem: number;
}

// ------------------------------------------------------------------------------------------------
// Mappers Internos (formerly in /mappers)
// ------------------------------------------------------------------------------------------------

export function mapMapaDtoToModel(dto: any): Mapa {
    return {
        codigo: dto.codigo,
        codProcesso: dto.codProcesso,
        unidade: dto.unidade,
        situacao: dto.situacao,
        dataCriacao: dto.dataCriacao,
        dataDisponibilizacao: dto.dataDisponibilizacao,
        dataFinalizacao: dto.dataFinalizacao,
        competencias: dto.competencias || [],
        descricao: dto.descricao,
    };
}

export function mapMapaCompletoDtoToModel(dto: any): MapaCompleto {
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

export function mapImpactoMapaDtoToModel(dto: ImpactoMapaDto): ImpactoMapa {
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
        // Arrays de atividades impactadas - sem mapeamento trivial
        atividadesInseridas: (inseridas as unknown as any[]).map((i: any) => ({ ...i, tipoImpacto: 'INSERIDA' })),
        atividadesRemovidas: (removidas as unknown as any[]).map((i: any) => ({ ...i, tipoImpacto: 'REMOVIDA' })),
        atividadesAlteradas: (alteradas as unknown as any[]).map((i: any) => ({ ...i, tipoImpacto: 'ALTERADA' })),
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

export function mapMapaAjusteDtoToModel(dto: any): MapaAjuste {
    return {
        codigo: dto.codigo,
        descricao: dto.descricao,
        competencias: dto.competencias || [],
    };
}

export function mapAnaliseDtoToModel(dto: any): AnaliseCadastro { // Reusing Type for now
    return {
        dataHora: dto.dataHora,
        observacoes: dto.observacoes || "",
        acao: dto.acao,
        unidadeSigla: dto.unidadeSigla,
        unidadeNome: dto.unidadeNome,
        analistaUsuarioTitulo: dto.analistaUsuarioTitulo,
        motivo: dto.motivo || "",
        tipo: dto.tipo
    };
}

export function mapAnalisesArray(dtos: any[]): AnaliseCadastro[] {
    if (!dtos) return [];
    return dtos.map(mapAnaliseDtoToModel);
}

export function mapAtividadeToModel(dto: AtividadeDto | null | undefined): Atividade | null {
    if (!dto) return null;
    return {
        codigo: dto.codigo,
        descricao: dto.descricao,
        conhecimentos: (dto.conhecimentos || [])
            .map(mapConhecimentoToModel)
            .filter((c): c is Conhecimento => c !== null),
    };
}

// Alias for backwards compatibility
export const mapAtividadeVisualizacaoToModel = mapAtividadeToModel;

export function mapConhecimentoToModel(dto: ConhecimentoDto | null | undefined): Conhecimento | null {
    if (!dto) return null;
    return {
        codigo: dto.codigo,
        descricao: dto.descricao,
    };
}

export const mapConhecimentoVisualizacaoToModel = mapConhecimentoToModel;

export function mapSubprocessoSituacaoToModel(dto: SubprocessoSituacaoDto): SubprocessoStatus {
    return {
        codigo: dto.codigo,
        situacao: dto.situacao,
    };
}

export function mapAtividadeOperacaoResponseToModel(dto: AtividadeOperacaoResponseDto): AtividadeOperacaoResponse {
    return {
        atividade: dto.atividade ? mapAtividadeToModel(dto.atividade) : null,
        subprocesso: mapSubprocessoSituacaoToModel(dto.subprocesso || { codigo: 0, situacao: 'NAO_INICIADO' as any }),
        atividadesAtualizadas: (dto.atividadesAtualizadas || [])
            .map(mapAtividadeToModel)
            .filter((a): a is Atividade => a !== null),
        permissoes: dto.permissoes,
    };
}

export function mapCriarAtividadeRequestToDto(
    request: any,
    codMapa: number,
): any {
    return {
        ...request,
        mapaCodigo: codMapa,
    };
}

export function mapAtualizarAtividadeToDto(request: Atividade): any {
    return {
        descricao: request.descricao,
    };
}

export function mapCriarConhecimentoRequestToDto(
    request: CriarConhecimentoRequest,
    atividadeCodigo: number,
): any {
    return {
        descricao: request.descricao,
        atividadeCodigo,
    };
}

export function mapAtualizarConhecimentoToDto(
    request: Conhecimento,
    atividadeCodigo: number
): any {
    return {
        codigo: request.codigo,
        atividadeCodigo: atividadeCodigo,
        descricao: request.descricao,
    };
}

export function mapSubprocessoDetalheDtoToModel(dto: any): SubprocessoDetalhe {
    if (!dto) {
        throw new Error("DTO de detalhes do subprocesso é nulo ou indefinido.");
    }

    // O backend retorna SubprocessoDetalheResponse que aninha a entidade Subprocesso
    const sp = dto.subprocesso || dto || {};

    // Fallback para unidade para evitar "Cannot read properties of undefined (reading 'sigla')"
    const unidadeDefault = { codigo: 0, nome: 'Não informada', sigla: 'N/I' };
    const unidade = sp.unidade || dto.unidade || unidadeDefault;

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
    return (response.data.atividadesDisponiveis || []).map(mapAtividadeVisualizacaoToModel).filter((a: Atividade | null): a is Atividade => a !== null);
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
    return response.data;
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
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}/impactos-mapa`);
    return mapImpactoMapaDtoToModel(response.data);
}

export async function obterMapaCompleto(codSubprocesso: number): Promise<MapaCompleto> {
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}/mapa-completo`);
    return mapMapaCompletoDtoToModel(response.data);
}

export async function salvarMapaCompleto(
    codSubprocesso: number,
    data: any,
): Promise<MapaCompleto> {
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/mapa-completo`,
        data,
    );
    return mapMapaCompletoDtoToModel(response.data);
}

export async function obterMapaAjuste(codSubprocesso: number): Promise<MapaAjuste> {
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}/mapa-ajuste`);
    return mapMapaAjusteDtoToModel(response.data);
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
    return mapMapaCompletoDtoToModel(response.data);
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
    return mapMapaCompletoDtoToModel(response.data);
}

export async function removerCompetencia(
    codSubprocesso: number,
    codCompetencia: number,
): Promise<MapaCompleto> {
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/competencia/${codCompetencia}/remover`,
    );
    return mapMapaCompletoDtoToModel(response.data);
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
    return mapAnalisesArray(response.data);
};

export const listarAnalisesValidacao = async (
    codSubprocesso: number,
): Promise<AnaliseValidacao[]> => {
    const response = await apiClient.get(
        `/subprocessos/${codSubprocesso}/historico-validacao`,
    );
    return mapAnalisesArray(response.data);
};
