import { describe, expect, it } from 'vitest';
import {
    mapMapaDtoToModel,
    mapMapaCompletoDtoToModel,
    mapImpactoMapaDtoToModel,
    mapMapaAjusteDtoToModel
} from '../mapas';

describe('mappers/mapas.ts', () => {
    describe('mapMapaDtoToModel', () => {
        it('mapeia todos os campos do DTO para o modelo', () => {
            const dto = {
                codigo: 1,
                codProcesso: 10,
                unidade: { sigla: 'TEST' },
                situacao: 'ATIVO',
                dataCriacao: '2023-01-01',
                dataDisponibilizacao: '2023-01-02',
                dataFinalizacao: '2023-01-03',
                competencias: [{ codigo: 1 }],
                descricao: 'Descrição teste'
            };

            const result = mapMapaDtoToModel(dto);

            expect(result.codigo).toBe(1);
            expect(result.codProcesso).toBe(10);
            expect(result.unidade).toEqual({ sigla: 'TEST' });
            expect(result.situacao).toBe('ATIVO');
            expect(result.dataCriacao).toBe('2023-01-01');
            expect(result.dataDisponibilizacao).toBe('2023-01-02');
            expect(result.dataFinalizacao).toBe('2023-01-03');
            expect(result.competencias).toHaveLength(1);
            expect(result.descricao).toBe('Descrição teste');
        });

        it('retorna array vazio quando competencias é undefined', () => {
            const dto = { codigo: 1 };
            const result = mapMapaDtoToModel(dto);
            expect(result.competencias).toEqual([]);
        });
    });

    describe('mapMapaCompletoDtoToModel', () => {
        it('mapeia todos os campos incluindo competencias', () => {
            const dto = {
                codigo: 1,
                subprocessoCodigo: 10,
                observacoes: 'Obs teste',
                situacao: 'ATIVO',
                competencias: [
                    { codigo: 1, descricao: 'Comp 1', atividadesCodigos: [1, 2] },
                    { codigo: 2, descricao: 'Comp 2', atividadesCodigos: [] }
                ]
            };

            const result = mapMapaCompletoDtoToModel(dto);

            expect(result.codigo).toBe(1);
            expect(result.subprocessoCodigo).toBe(10);
            expect(result.observacoes).toBe('Obs teste');
            expect(result.situacao).toBe('ATIVO');
            expect(result.competencias).toHaveLength(2);
            expect(result.competencias[0].codigo).toBe(1);
            expect(result.competencias[0].descricao).toBe('Comp 1');
            expect(result.competencias[0].atividadesAssociadas).toEqual([1, 2]);
        });

        it('retorna valores padrão quando campos são undefined', () => {
            const dto = { codigo: 1 };
            const result = mapMapaCompletoDtoToModel(dto);
            expect(result.competencias).toEqual([]);
            expect(result.situacao).toBe('');
        });

        it('retorna array vazio para atividadesCodigos quando undefined', () => {
            const dto = {
                codigo: 1,
                competencias: [
                    { codigo: 1, descricao: 'Comp 1' } // sem atividadesCodigos
                ]
            };

            const result = mapMapaCompletoDtoToModel(dto);
            expect(result.competencias[0].atividadesAssociadas).toEqual([]);
        });
    });

    describe('mapImpactoMapaDtoToModel', () => {
        it('mapeia todos os campos de impacto', () => {
            const dto = {
                temImpactos: true,
                totalAtividadesInseridas: 5,
                totalAtividadesRemovidas: 2,
                totalAtividadesAlteradas: 3,
                totalCompetenciasImpactadas: 1,
                atividadesInseridas: [{ codigo: 1, descricao: 'Nova' }],
                atividadesRemovidas: [{ codigo: 2, descricao: 'Removida' }],
                atividadesAlteradas: [{ codigo: 3, descricao: 'Alterada' }],
                competenciasImpactadas: [
                    { codigo: 1, descricao: 'Comp', atividadesAfetadas: [1], tipoImpacto: 'ADICIONADA' }
                ]
            };

            const result = mapImpactoMapaDtoToModel(dto);

            expect(result.temImpactos).toBe(true);
            expect(result.totalAtividadesInseridas).toBe(5);
            expect(result.totalAtividadesRemovidas).toBe(2);
            expect(result.totalAtividadesAlteradas).toBe(3);
            expect(result.totalCompetenciasImpactadas).toBe(1);
            expect(result.atividadesInseridas).toHaveLength(1);
            expect(result.atividadesRemovidas).toHaveLength(1);
            expect(result.atividadesAlteradas).toHaveLength(1);
            expect(result.competenciasImpactadas).toHaveLength(1);
            expect(result.competenciasImpactadas[0].tipoImpacto).toBe('ADICIONADA');
        });

        it('retorna arrays vazios quando campos são undefined', () => {
            const dto = { temImpactos: false };
            const result = mapImpactoMapaDtoToModel(dto);
            expect(result.atividadesInseridas).toEqual([]);
            expect(result.atividadesRemovidas).toEqual([]);
            expect(result.atividadesAlteradas).toEqual([]);
            expect(result.competenciasImpactadas).toEqual([]);
        });

        it('retorna array vazio para atividadesAfetadas quando undefined', () => {
            const dto = {
                temImpactos: true,
                competenciasImpactadas: [
                    { codigo: 1, descricao: 'Comp', tipoImpacto: 'REMOVIDA' } // sem atividadesAfetadas
                ]
            };

            const result = mapImpactoMapaDtoToModel(dto);
            expect(result.competenciasImpactadas[0].atividadesAfetadas).toEqual([]);
        });
    });

    describe('mapMapaAjusteDtoToModel', () => {
        it('mapeia todos os campos de ajuste', () => {
            const dto = {
                codigo: 1,
                descricao: 'Ajuste teste',
                competencias: [{ codigo: 1 }, { codigo: 2 }]
            };

            const result = mapMapaAjusteDtoToModel(dto);

            expect(result.codigo).toBe(1);
            expect(result.descricao).toBe('Ajuste teste');
            expect(result.competencias).toHaveLength(2);
        });

        it('retorna array vazio quando competencias é undefined', () => {
            const dto = { codigo: 1, descricao: 'Teste' };
            const result = mapMapaAjusteDtoToModel(dto);
            expect(result.competencias).toEqual([]);
        });
    });
});
