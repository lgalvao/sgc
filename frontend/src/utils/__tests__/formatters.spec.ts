import { describe, it, expect } from 'vitest';
import { formatarSituacaoProcesso, formatarTipoProcesso, limparCpf, formatarCpf } from '../formatters';
import { SituacaoProcesso, TipoProcesso } from '@/types/tipos';

describe('formatters', () => {
    describe('formatarSituacaoProcesso', () => {
        it('deve formatar CRIADO', () => {
            expect(formatarSituacaoProcesso(SituacaoProcesso.CRIADO)).toBe('Criado');
        });
        it('deve formatar FINALIZADO', () => {
            expect(formatarSituacaoProcesso(SituacaoProcesso.FINALIZADO)).toBe('Finalizado');
        });
        it('deve formatar EM_ANDAMENTO', () => {
            expect(formatarSituacaoProcesso(SituacaoProcesso.EM_ANDAMENTO)).toBe('Em andamento');
        });
        it('deve retornar a string original se desconhecido', () => {
            expect(formatarSituacaoProcesso('OUTRO')).toBe('OUTRO');
        });
    });

    describe('formatarTipoProcesso', () => {
        it('deve formatar MAPEAMENTO', () => {
            expect(formatarTipoProcesso(TipoProcesso.MAPEAMENTO)).toBe('Mapeamento');
        });
        it('deve formatar REVISAO', () => {
            expect(formatarTipoProcesso(TipoProcesso.REVISAO)).toBe('Revisão');
        });
        it('deve formatar DIAGNOSTICO', () => {
            expect(formatarTipoProcesso(TipoProcesso.DIAGNOSTICO)).toBe('Diagnóstico');
        });
        it('deve retornar a string original se desconhecido', () => {
            expect(formatarTipoProcesso('OUTRO')).toBe('OUTRO');
        });
    });

    describe('limparCpf', () => {
        it('deve remover caracteres não numéricos', () => {
            expect(limparCpf('123.456.789-00')).toBe('12345678900');
            expect(limparCpf('1a2b3c')).toBe('123');
            expect(limparCpf('')).toBe('');
        });
    });

    describe('formatarCpf', () => {
        it('deve retornar string vazia para entrada vazia', () => {
            expect(formatarCpf('')).toBe('');
        });
        it('deve formatar CPF válido corretamente', () => {
            expect(formatarCpf('12345678900')).toBe('123.456.789-00');
        });
        it('deve retornar limpo se não tiver 11 dígitos', () => {
            expect(formatarCpf('123')).toBe('123');
            expect(formatarCpf('123456789000')).toBe('123456789000');
        });
        it('deve formatar CPF com caracteres extras removidos', () => {
            expect(formatarCpf('123.456.789-00abc')).toBe('123.456.789-00'); // input string with extra chars -> cleaned -> if 11, format.
            // '123.456.789-00abc' cleaned is '12345678900' (if abc ignored by replace \D)
        });
    });
});
