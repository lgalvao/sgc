import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ToastService, registerToast } from '../toastService';

describe('toastService', () => {
  let mockToastInstance: any;

  beforeEach(() => {
    mockToastInstance = {
      show: vi.fn(),
    };
    // Reset the singleton (though registerToast overwrites it)
    registerToast(mockToastInstance);
  });

  it('sucesso should call toastInstance.show with success variant', () => {
    ToastService.sucesso('Success Title', 'Success Message');
    expect(mockToastInstance.show).toHaveBeenCalledWith({
      title: 'Success Title',
      body: 'Success Message',
      props: { variant: 'success', value: true },
    });
  });

  it('erro should call toastInstance.show with danger variant', () => {
    ToastService.erro('Error Title', 'Error Message');
    expect(mockToastInstance.show).toHaveBeenCalledWith({
      title: 'Error Title',
      body: 'Error Message',
      props: { variant: 'danger', value: true },
    });
  });

  it('aviso should call toastInstance.show with warning variant', () => {
    ToastService.aviso('Warning Title', 'Warning Message');
    expect(mockToastInstance.show).toHaveBeenCalledWith({
      title: 'Warning Title',
      body: 'Warning Message',
      props: { variant: 'warning', value: true },
    });
  });

  it('info should call toastInstance.show with info variant', () => {
    ToastService.info('Info Title', 'Info Message');
    expect(mockToastInstance.show).toHaveBeenCalledWith({
      title: 'Info Title',
      body: 'Info Message',
      props: { variant: 'info', value: true },
    });
  });

  it('should handle missing toastInstance gracefully', () => {
    // Reset instance to null/undefined indirectly by registering undefined
    // Or in a real scenario, if it wasn't registered.
    // Since we can't easily unregister, we can register null (if typescript allows) or undefined.
    // The type definition says `instance: any`, so we can pass null.
    registerToast(null);

    // Should not throw
    expect(() => ToastService.sucesso('Title', 'Message')).not.toThrow();
  });
});
