import type {
    Atividade,
    AtividadeOperacaoResponse,
    Conhecimento,
    CriarConhecimentoRequest,
} from "@/types/tipos";
import type {AtividadeDto, AtividadeOperacaoResponseDto, ConhecimentoDto} from "@/types/dtos";
import apiClient from "@/axios-setup";

export async function listarAtividades(): Promise<Atividade[]> {
    const response = await apiClient.get<AtividadeDto[]>("/atividades");
    return response.data.map(mapAtividade);
}

export async function obterAtividadePorCodigo(codAtividade: number): Promise<Atividade> {
    const response = await apiClient.get<AtividadeDto>(`/atividades/${codAtividade}`);
    return mapAtividade(response.data);
}

export async function criarAtividade(
    request: any,
    codMapa: number,
): Promise<AtividadeOperacaoResponse> {
    const requestDto = {
        ...request,
        mapaCodigo: codMapa,
    };
    const response = await apiClient.post<AtividadeOperacaoResponseDto>("/atividades", requestDto);
    return mapAtividadeOperacaoResponse(response.data);
}

export async function atualizarAtividade(
    codAtividade: number,
    request: Atividade,
): Promise<AtividadeOperacaoResponse> {
    const payload = {
        descricao: request.descricao,
    };
    const response = await apiClient.post<AtividadeOperacaoResponseDto>(
        `/atividades/${codAtividade}/atualizar`,
        payload,
    );
    return mapAtividadeOperacaoResponse(response.data);
}

export async function excluirAtividade(codAtividade: number): Promise<AtividadeOperacaoResponse> {
    const response = await apiClient.post<AtividadeOperacaoResponseDto>(`/atividades/${codAtividade}/excluir`);
    return mapAtividadeOperacaoResponse(response.data);
}

export async function listarConhecimentos(
    codAtividade: number,
): Promise<Conhecimento[]> {
    const response = await apiClient.get<ConhecimentoDto[]>(
        `/atividades/${codAtividade}/conhecimentos`,
    );
    return response.data.map(mapConhecimento);
}

export async function criarConhecimento(
    codAtividade: number,
    request: CriarConhecimentoRequest,
): Promise<AtividadeOperacaoResponse> {
    const requestDto = {
        descricao: request.descricao,
        atividadeCodigo: codAtividade,
    };
    const response = await apiClient.post<AtividadeOperacaoResponseDto>(
        `/atividades/${codAtividade}/conhecimentos`,
        requestDto,
    );
    return mapAtividadeOperacaoResponse(response.data);
}

export async function atualizarConhecimento(
    codAtividade: number,
    codConhecimento: number,
    request: Conhecimento,
): Promise<AtividadeOperacaoResponse> {
    const payload = {
        codigo: request.codigo,
        atividadeCodigo: codAtividade,
        descricao: request.descricao,
    };
    const response = await apiClient.post<AtividadeOperacaoResponseDto>(
        `/atividades/${codAtividade}/conhecimentos/${codConhecimento}/atualizar`,
        payload,
    );
    return mapAtividadeOperacaoResponse(response.data);
}

export async function excluirConhecimento(
    codAtividade: number,
    codConhecimento: number,
): Promise<AtividadeOperacaoResponse> {
    const response = await apiClient.post<AtividadeOperacaoResponseDto>(
        `/atividades/${codAtividade}/conhecimentos/${codConhecimento}/excluir`,
    );
    return mapAtividadeOperacaoResponse(response.data);
}

// Helpers internos para substituição de mappers
function mapAtividade(dto: any): Atividade {
    return {
        codigo: dto.codigo,
        descricao: dto.descricao,
        conhecimentos: (dto.conhecimentos || []).map(mapConhecimento),
    };
}

function mapConhecimento(dto: any): Conhecimento {
    return {
        codigo: dto.codigo,
        descricao: dto.descricao,
    };
}

function mapAtividadeOperacaoResponse(dto: AtividadeOperacaoResponseDto): AtividadeOperacaoResponse {
    return {
        atividade: dto.atividade ? mapAtividade(dto.atividade) : null,
        subprocesso: dto.subprocesso || { codigo: 0, situacao: 'NAO_INICIADO' as any },
        atividadesAtualizadas: (dto.atividadesAtualizadas || []).map(mapAtividade),
        permissoes: dto.permissoes,
    };
}
