import {describe, expect, it, vi} from 'vitest';
import {useGerenciadorModals} from '@/composables/useModalManager';
import {logger} from '@/utils';

vi.mock('@/utils', () => ({
    logger: {
        warn: vi.fn(),
        error: vi.fn(),
        info: vi.fn()
    }
}));

describe('useGerenciadorModals', () => {
    it('deve inicializar as modals corretamente', () => {
        const {modals} = useGerenciadorModals(['modal1', 'modal2']);
        expect(modals.modal1.value).toEqual({aberto: false});
        expect(modals.modal2.value).toEqual({aberto: false});
    });

    it('deve abrir uma modal com dados', () => {
        const {abrir, estaAberto, obterDados} = useGerenciadorModals(['modal1']);
        const dados = {id: 1};

        abrir('modal1', dados);

        expect(estaAberto('modal1')).toBe(true);
        expect(obterDados('modal1')).toEqual(dados);
    });

    it('deve fechar uma modal e limpar dados', () => {
        const {abrir, fechar, estaAberto, obterDados} = useGerenciadorModals(['modal1']);
        abrir('modal1', {id: 1});

        fechar('modal1');

        expect(estaAberto('modal1')).toBe(false);
        expect(obterDados('modal1')).toBeUndefined();
    });

    it('deve alternar o estado da modal', () => {
        const {alternar, estaAberto} = useGerenciadorModals(['modal1']);

        alternar('modal1');
        expect(estaAberto('modal1')).toBe(true);

        alternar('modal1');
        expect(estaAberto('modal1')).toBe(false);
    });

    it('deve fechar todas as modals', () => {
        const {abrir, fecharTodos, estaAberto} = useGerenciadorModals(['m1', 'm2']);
        abrir('m1');
        abrir('m2');

        fecharTodos();

        expect(estaAberto('m1')).toBe(false);
        expect(estaAberto('m2')).toBe(false);
    });

    it('deve logar aviso se tentar abrir modal não registrada', () => {
        const {abrir} = useGerenciadorModals(['m1']);
        abrir('m2');
        expect(logger.warn).toHaveBeenCalledWith(expect.stringContaining('Modal "m2" não foi registrada'));
    });

    it('deve logar aviso se tentar fechar modal não registrada', () => {
        const {fechar} = useGerenciadorModals(['m1']);
        fechar('m2');
        expect(logger.warn).toHaveBeenCalledWith(expect.stringContaining('Modal "m2" não foi registrada'));
    });

    it('deve logar aviso se tentar alternar modal não registrada', () => {
        const {alternar} = useGerenciadorModals(['m1']);
        alternar('m2');
        expect(logger.warn).toHaveBeenCalledWith(expect.stringContaining('Modal "m2" não foi registrada'));
    });

    it('obterDados deve retornar undefined para modal não registrada', () => {
        const {obterDados} = useGerenciadorModals(['m1']);
        expect(obterDados('m2')).toBeUndefined();
    });

    it('estaAberto deve retornar false para modal não registrada', () => {
        const {estaAberto} = useGerenciadorModals(['m1']);
        expect(estaAberto('m2')).toBe(false);
    });
});
