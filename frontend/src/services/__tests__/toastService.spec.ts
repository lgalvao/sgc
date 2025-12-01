import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ToastService } from '../toastService';
import { useFeedbackStore } from '@/stores/feedback';
import { createPinia, setActivePinia } from 'pinia';

describe('toastService', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it('sucesso should call store.show with success variant', () => {
    const store = useFeedbackStore();
    const showSpy = vi.spyOn(store, 'show');

    ToastService.sucesso('Success Title', 'Success Message');

    expect(showSpy).toHaveBeenCalledWith('Success Title', 'Success Message', 'success');
  });

  it('erro should call store.show with danger variant', () => {
    const store = useFeedbackStore();
    const showSpy = vi.spyOn(store, 'show');

    ToastService.erro('Error Title', 'Error Message');

    expect(showSpy).toHaveBeenCalledWith('Error Title', 'Error Message', 'danger');
  });

  it('aviso should call store.show with warning variant', () => {
    const store = useFeedbackStore();
    const showSpy = vi.spyOn(store, 'show');

    ToastService.aviso('Warning Title', 'Warning Message');

    expect(showSpy).toHaveBeenCalledWith('Warning Title', 'Warning Message', 'warning');
  });

  it('info should call store.show with info variant', () => {
    const store = useFeedbackStore();
    const showSpy = vi.spyOn(store, 'show');

    ToastService.info('Info Title', 'Info Message');

    expect(showSpy).toHaveBeenCalledWith('Info Title', 'Info Message', 'info');
  });
});
