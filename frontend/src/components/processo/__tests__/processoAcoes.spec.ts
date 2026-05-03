import {describe, expect, it} from 'vitest';
import {obterIdBotaoAcaoProcesso, obterTestIdBotaoAcaoProcesso} from '../processoAcoes';

describe('processoAcoes', () => {
    describe('obterIdBotaoAcaoProcesso', () => {
        it.each([
            ['aceitar-cadastro', 'btn-aceitar-bloco'],
            ['aceitar-mapa', 'btn-aceitar-mapas-bloco'],
            ['homologar-cadastro', 'btn-homologar-bloco'],
            ['homologar-mapa', 'btn-homologar-mapas-bloco'],
            ['disponibilizar-mapa', 'btn-disponibilizar-bloco'],
        ])('mapeia %s → %s', (entrada, esperado) => {
            expect(obterIdBotaoAcaoProcesso(entrada)).toBe(esperado);
        });

        it('usa o padrão btn-{codigo} para ações não mapeadas', () => {
            expect(obterIdBotaoAcaoProcesso('outra-acao')).toBe('btn-outra-acao');
        });

        it('usa o padrão para string vazia', () => {
            expect(obterIdBotaoAcaoProcesso('')).toBe('btn-');
        });
    });

    describe('obterTestIdBotaoAcaoProcesso', () => {
        it.each([
            ['aceitar-cadastro', 'btn-processo-aceitar-bloco'],
            ['aceitar-mapa', 'btn-processo-aceitar-mapas-bloco'],
            ['homologar-cadastro', 'btn-processo-homologar-bloco'],
            ['homologar-mapa', 'btn-processo-homologar-mapas-bloco'],
            ['disponibilizar-mapa', 'btn-processo-disponibilizar-bloco'],
        ])('mapeia %s → %s', (entrada, esperado) => {
            expect(obterTestIdBotaoAcaoProcesso(entrada)).toBe(esperado);
        });

        it('usa o padrão btn-processo-{codigo} para ações não mapeadas', () => {
            expect(obterTestIdBotaoAcaoProcesso('outra-acao')).toBe('btn-processo-outra-acao');
        });

        it('usa o padrão para string vazia', () => {
            expect(obterTestIdBotaoAcaoProcesso('')).toBe('btn-processo-');
        });
    });
});
