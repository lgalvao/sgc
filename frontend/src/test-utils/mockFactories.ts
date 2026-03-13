/**
 * Factory functions for creating mock objects in tests
 */
import {ProcessoResumo, SituacaoProcesso, TipoProcesso} from '@/types/tipos';

export function createMockProcessoResumo(overrides: Partial<ProcessoResumo> = {}): ProcessoResumo {
    return {
        codigo: 1,
        descricao: 'Processo teste',
        tipo: TipoProcesso.MAPEAMENTO,
        situacao: SituacaoProcesso.EM_ANDAMENTO,
        dataLimite: new Date().toISOString(),
        dataCriacao: new Date().toISOString(),
        unidadeCodigo: 1,
        unidadeNome: 'Unidade teste',
        unidadesParticipantes: 'UT1, UT2',
        ...overrides
    };
}
