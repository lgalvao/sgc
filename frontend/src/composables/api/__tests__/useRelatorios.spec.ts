import {beforeEach, describe, expect, it, vi} from 'vitest';
import {useRelatorios} from '../useRelatorios';
import apiClient from '@/axios-setup';

vi.mock('@/axios-setup', () => ({
  default: {
    get: vi.fn(),
  },
}));

describe('useRelatorios', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    
    // Mock global URL methods
    global.URL.createObjectURL = vi.fn(() => 'blob:http://localhost/mock-url');
    global.URL.revokeObjectURL = vi.fn();
    
    // Mock document.createElement and link behavior
    const mockLink = {
      href: '',
      setAttribute: vi.fn(),
      click: vi.fn(),
      remove: vi.fn(),
    } as unknown as HTMLAnchorElement;
    
    vi.spyOn(document, 'createElement').mockReturnValue(mockLink);
    vi.spyOn(document.body, 'appendChild').mockImplementation(() => document.body);
  });

  it('deve obter o relatorio de andamento', async () => {
    const mockData = [{ id: 1, nome: 'Teste' }];
    (apiClient.get as any).mockResolvedValueOnce({ data: mockData });

    const { obterRelatorioAndamento } = useRelatorios();
    const result = await obterRelatorioAndamento(123);

    expect(apiClient.get).toHaveBeenCalledWith('/relatorios/andamento/123');
    expect(result).toEqual(mockData);
  });

  it('deve fazer o download do relatorio de andamento em pdf', async () => {
    const mockBlobData = new Blob(['pdf content']);
    (apiClient.get as any).mockResolvedValueOnce({ data: mockBlobData });

    const { downloadRelatorioAndamentoPdf } = useRelatorios();
    await downloadRelatorioAndamentoPdf(123);

    expect(apiClient.get).toHaveBeenCalledWith('/relatorios/andamento/123/exportar', {
      responseType: 'blob'
    });
    
    expect(global.URL.createObjectURL).toHaveBeenCalled();
    expect(document.createElement).toHaveBeenCalledWith('a');
  });

  it('deve fazer o download do relatorio de mapas em pdf sem codigoUnidade', async () => {
    const mockBlobData = new Blob(['pdf content']);
    (apiClient.get as any).mockResolvedValueOnce({ data: mockBlobData });

    const { downloadRelatorioMapasPdf } = useRelatorios();
    await downloadRelatorioMapasPdf(123);

    expect(apiClient.get).toHaveBeenCalledWith('/relatorios/mapas/123/exportar', {
      responseType: 'blob'
    });
  });

  it('deve fazer o download do relatorio de mapas em pdf com codigoUnidade', async () => {
    const mockBlobData = new Blob(['pdf content']);
    (apiClient.get as any).mockResolvedValueOnce({ data: mockBlobData });

    const { downloadRelatorioMapasPdf } = useRelatorios();
    await downloadRelatorioMapasPdf(123, 456);

    expect(apiClient.get).toHaveBeenCalledWith('/relatorios/mapas/123/exportar?codigoUnidade=456', {
      responseType: 'blob'
    });
  });
});
