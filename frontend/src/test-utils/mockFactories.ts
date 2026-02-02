/**
 * Factory functions for creating mock objects in tests
 */
import { ProcessoResumo, SituacaoProcesso, TipoProcesso } from '@/types/tipos';

/**
 * Creates a mock ProcessoResumo object with default values
 */
export function createMockProcessoResumo(overrides: Partial<ProcessoResumo> = {}): ProcessoResumo {
  return {
    codigo: 1,
    descricao: 'Processo Teste',
    tipo: TipoProcesso.MAPEAMENTO,
    tipoLabel: 'Mapeamento',
    situacao: SituacaoProcesso.EM_ANDAMENTO,
    situacaoLabel: 'Em andamento',
    dataLimite: new Date().toISOString(),
    dataLimiteFormatada: '31/12/2023',
    dataCriacao: new Date().toISOString(),
    unidadeCodigo: 1,
    unidadeNome: 'Unidade Teste',
    unidadesParticipantes: 'UT1, UT2',
    ...overrides
  };
}
