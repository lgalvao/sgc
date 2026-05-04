import {describe, expect, it} from 'vitest';
import {
    calcularAssinaturaCadastro,
    formatDate,
    formatSituacaoProcesso,
    formatSituacaoSubprocesso,
    formatTipoProcesso
} from '@/utils';
import {SituacaoProcesso, SituacaoSubprocesso, TipoProcesso} from '@/types/tipos';

describe('formatters', () => {
    describe('formatDate', () => {
        it('deve retornar string vazia para data nula ou indefinida', () => {
            expect(formatDate(null)).toBe('');
            expect(formatDate(undefined)).toBe('');
            expect(formatDate('')).toBe('');
        });

        it('deve formatar data valida sem hora', () => {
            const date = new Date('2024-01-15T10:30:00Z');
            expect(formatDate(date, false)).toMatch(/15\/01\/2024/);
        });

        it('deve formatar data valida com hora', () => {
            const date = new Date('2024-01-15T10:30:00');
            const formatado = formatDate(date, true);
            expect(formatado).toMatch(/15\/01\/2024/);
            expect(formatado).toMatch(/10:30/);
        });

        it('deve formatar data invalida como string vazia', () => {
            expect(formatDate('data-invalida')).toBe('');
        });
    });

    describe('calcularAssinaturaCadastro', () => {
        it('deve retornar string vazia para lista nula ou vazia', () => {
            expect(calcularAssinaturaCadastro(null as any)).toBe('');
            expect(calcularAssinaturaCadastro([])).toBe('');
        });

        it('deve calcular assinatura para uma atividade', () => {
            const atividades = [
                {
                    descricao: ' Atividade 1 ',
                    conhecimentos: [
                        {descricao: ' C2 '},
                        {descricao: ' C1 '}
                    ]
                }
            ] as any;

            const assinatura = calcularAssinaturaCadastro(atividades);
            // "Atividade 1" + \u0002 + "C1" + \u0001 + "C2"
            expect(assinatura).toBe('Atividade 1\u0002C1\u0001C2');
        });

        it('deve calcular assinatura para múltiplas atividades ordenadas', () => {
            const atividades = [
                {descricao: 'B', conhecimentos: []},
                {descricao: 'A', conhecimentos: []}
            ] as any;

            const assinatura = calcularAssinaturaCadastro(atividades);
            expect(assinatura).toBe('A\u0002\u0003B\u0002');
        });
    });

    describe('formatSituacaoProcesso', () => {
        it('deve retornar string vazia para valor nulo/indefinido', () => {
            expect(formatSituacaoProcesso(null)).toBe('');
            expect(formatSituacaoProcesso(undefined)).toBe('');
        });

        it('deve retornar a label correta', () => {
            expect(formatSituacaoProcesso(SituacaoProcesso.CRIADO)).toBe('Criado');
            expect(formatSituacaoProcesso(SituacaoProcesso.EM_ANDAMENTO)).toBe('Em andamento');
            expect(formatSituacaoProcesso(SituacaoProcesso.FINALIZADO)).toBe('Finalizado');
        });

        it('deve retornar o proprio valor se nao houver label', () => {
            expect(formatSituacaoProcesso('OUTRA_SITUACAO')).toBe('OUTRA_SITUACAO');
        });
    });

    describe('formatTipoProcesso', () => {
        it('deve retornar string vazia para valor nulo/indefinido', () => {
            expect(formatTipoProcesso(null)).toBe('');
            expect(formatTipoProcesso(undefined)).toBe('');
        });

        it('deve retornar a label correta', () => {
            expect(formatTipoProcesso(TipoProcesso.MAPEAMENTO)).toBe('Mapeamento');
            expect(formatTipoProcesso(TipoProcesso.REVISAO)).toBe('Revisão');
            expect(formatTipoProcesso(TipoProcesso.DIAGNOSTICO)).toBe('Diagnóstico');
        });

        it('deve retornar o proprio valor se nao houver label', () => {
            expect(formatTipoProcesso('OUTRO_TIPO')).toBe('OUTRO_TIPO');
        });
    });

    describe('formatSituacaoSubprocesso', () => {
        it('deve retornar string vazia para valor nulo/indefinido', () => {
            expect(formatSituacaoSubprocesso(null)).toBe('');
            expect(formatSituacaoSubprocesso(undefined)).toBe('');
        });

        it('deve retornar a label correta', () => {
            expect(formatSituacaoSubprocesso(SituacaoSubprocesso.NAO_INICIADO)).toBe('Não iniciado');
            expect(formatSituacaoSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)).toBe('Cadastro em andamento');
            expect(formatSituacaoSubprocesso(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO)).toBe('Mapa homologado');
        });

        it('deve retornar o proprio valor se nao houver label', () => {
            expect(formatSituacaoSubprocesso('OUTRA_SITUACAO_SUB')).toBe('OUTRA_SITUACAO_SUB');
        });
    });
});
