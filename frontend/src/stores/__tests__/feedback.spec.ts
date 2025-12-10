import {createPinia, setActivePinia} from 'pinia';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {useFeedbackStore} from '@/stores/feedback';

describe('Feedback Store', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.restoreAllMocks();
    });

    it('deve ter o estado inicial correto', () => {
        const store = useFeedbackStore();
        expect(store.currentFeedback).toEqual({
            title: '',
            message: '',
            variant: 'info',
            show: false
        });
    });

    it('deve mostrar feedback corretamente', () => {
        const store = useFeedbackStore();
        store.show('Sucesso', 'Operação realizada', 'success');

        expect(store.currentFeedback).toEqual({
            title: 'Sucesso',
            message: 'Operação realizada',
            variant: 'success',
            show: true,
            autoHideDelay: 5000
        });
    });

    it('deve fechar feedback', () => {
        const store = useFeedbackStore();
        store.show('Info', 'Teste');
        expect(store.currentFeedback.show).toBe(true);

        store.close();
        expect(store.currentFeedback.show).toBe(false);
    });

    it('deve fechar automaticamente após o delay', () => {
        const store = useFeedbackStore();
        store.show('Info', 'Teste', 'info', 3000);

        expect(store.currentFeedback.show).toBe(true);

        vi.advanceTimersByTime(3000);

        expect(store.currentFeedback.show).toBe(false);
    });

    it('deve limpar timer anterior ao mostrar novo feedback', () => {
        const store = useFeedbackStore();

        // Primeiro feedback que fecharia em 5s
        store.show('Primeiro', 'Msg 1', 'info', 5000);

        // Avança 2s
        vi.advanceTimersByTime(2000);

        // Mostra segundo feedback que fecha em 5s
        store.show('Segundo', 'Msg 2', 'warning', 5000);

        // Avança mais 3s (total 5s desde o primeiro). O primeiro DEVERIA fechar aqui se não fosse cancelado.
        vi.advanceTimersByTime(3000);

        // Como foi cancelado, não deve ter fechado ainda (pois o segundo só fecha daqui a 2s)
        expect(store.currentFeedback.title).toBe('Segundo');
        expect(store.currentFeedback.show).toBe(true);

        // Avança mais 2s (total 5s desde o segundo)
        vi.advanceTimersByTime(2000);
        expect(store.currentFeedback.show).toBe(false);
    });

    it('não deve setar timeout se delay for 0', () => {
        const store = useFeedbackStore();

        store.show('Fixo', 'Não some', 'danger', 0);

        vi.advanceTimersByTime(10000);
        expect(store.currentFeedback.show).toBe(true);
    });
});
