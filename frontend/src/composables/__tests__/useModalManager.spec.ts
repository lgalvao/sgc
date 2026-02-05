import {describe, expect, it, vi} from 'vitest';
import {useModalManager} from '@/composables/useModalManager';
import {logger} from '@/utils';

vi.mock('@/utils', () => ({
    logger: {
        warn: vi.fn(),
        error: vi.fn(),
        info: vi.fn()
    }
}));

describe('useModalManager', () => {
    it('deve inicializar as modals corretamente', () => {
        const { modals } = useModalManager(['modal1', 'modal2']);
        expect(modals.modal1.value).toEqual({ isOpen: false });
        expect(modals.modal2.value).toEqual({ isOpen: false });
    });

    it('deve abrir uma modal com dados', () => {
        const { open, isOpen, getData } = useModalManager(['modal1']);
        const data = { id: 1 };

        open('modal1', data);

        expect(isOpen('modal1')).toBe(true);
        expect(getData('modal1')).toEqual(data);
    });

    it('deve fechar uma modal e limpar dados', () => {
        const { open, close, isOpen, getData } = useModalManager(['modal1']);
        open('modal1', { id: 1 });

        close('modal1');

        expect(isOpen('modal1')).toBe(false);
        expect(getData('modal1')).toBeUndefined();
    });

    it('deve alternar o estado da modal', () => {
        const { toggle, isOpen } = useModalManager(['modal1']);

        toggle('modal1');
        expect(isOpen('modal1')).toBe(true);

        toggle('modal1');
        expect(isOpen('modal1')).toBe(false);
    });

    it('deve fechar todas as modals', () => {
        const { open, closeAll, isOpen } = useModalManager(['m1', 'm2']);
        open('m1');
        open('m2');

        closeAll();

        expect(isOpen('m1')).toBe(false);
        expect(isOpen('m2')).toBe(false);
    });

    it('deve logar aviso se tentar abrir modal não registrada', () => {
        const { open } = useModalManager(['m1']);
        open('m2');
        expect(logger.warn).toHaveBeenCalledWith(expect.stringContaining('Modal "m2" não foi registrada'));
    });

    it('deve logar aviso se tentar fechar modal não registrada', () => {
        const { close } = useModalManager(['m1']);
        close('m2');
        expect(logger.warn).toHaveBeenCalledWith(expect.stringContaining('Modal "m2" não foi registrada'));
    });

    it('deve logar aviso se tentar alternar modal não registrada', () => {
        const { toggle } = useModalManager(['m1']);
        toggle('m2');
        expect(logger.warn).toHaveBeenCalledWith(expect.stringContaining('Modal "m2" não foi registrada'));
    });

    it('getData deve retornar undefined para modal não registrada', () => {
        const { getData } = useModalManager(['m1']);
        expect(getData('m2')).toBeUndefined();
    });

    it('isOpen deve retornar false para modal não registrada', () => {
        const { isOpen } = useModalManager(['m1']);
        expect(isOpen('m2')).toBe(false);
    });
});
