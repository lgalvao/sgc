import type { ProcessoDetalhe, Unidade } from "@/types/types";

export const mockUnidade: Unidade = {
    codigo: 1,
    nome: 'Unidade Mock',
    sigla: 'UNID',
    unidadeSuperior: null,
};

export const mockProcessoDetalhe: ProcessoDetalhe = {
    codigo: 1,
    tipo: 'MAPEAMENTO',
    situacao: 'EM_ANDAMENTO',
    unidade: mockUnidade,
    unidadesParticipantes: [mockUnidade],
    dataCriacao: '2024-01-01',
    dataFinalizacao: null,
    subprocessos: [],
};