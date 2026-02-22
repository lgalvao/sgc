import type {SubprocessoDetalhe} from "@/types/tipos";

/**
 * Mapeia o DTO SubprocessoDetalheResponse do backend para a interface SubprocessoDetalhe do frontend.
 * Resolve o descompasso entre as estruturas aninhadas do servidor e as achatadas do cliente.
 */
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
