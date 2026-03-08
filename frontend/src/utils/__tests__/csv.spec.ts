import {describe, expect, it, vi} from 'vitest';
import {downloadCSV, gerarCSV} from '../csv';

describe('csv utils', () => {
  describe('gerarCSV', () => {
    it('deve retornar string vazia para array vazio', () => {
      expect(gerarCSV([])).toBe('');
    });

    it('deve gerar CSV corretamente', () => {
      const data = [
        { nome: 'Teste', idade: 30 },
        { nome: 'João', idade: 25 }
      ];
      const result = gerarCSV(data);
      expect(result).toContain('"nome","idade"');
      expect(result).toContain('"Teste","30"');
      expect(result).toContain('"João","25"');
    });

    it('deve sanitizar valores para prevenir CSV injection', () => {
      const data = [
        { formula: '=1+1', normal: 'texto' },
        { formula: '+A1', normal: 'texto' },
        { formula: '-B2', normal: 'texto' },
        { formula: '@SUM', normal: 'texto' },
      ];
      const result = gerarCSV(data);
      expect(result).toContain(`"'=1+1","texto"`);
      expect(result).toContain(`"'+A1","texto"`);
      expect(result).toContain(`"'-B2","texto"`);
      expect(result).toContain(`"'@SUM","texto"`);
    });

    it('deve lidar com valores nulos ou undefined', () => {
      const data = [
        { nome: 'Teste', valor: null },
        { nome: 'João', valor: undefined }
      ];
      const result = gerarCSV(data);
      expect(result).toContain('"Teste",');
      expect(result).toContain('"João",');
    });
  });

  describe('downloadCSV', () => {
    it('deve criar link e fazer o download', () => {
      const mockLink = {
        download: '',
        href: '',
        setAttribute: vi.fn(),
        click: vi.fn(),
        remove: vi.fn(),
        style: { visibility: '' }
      } as unknown as HTMLAnchorElement;
      
      vi.spyOn(document, 'createElement').mockReturnValue(mockLink);
      global.URL.createObjectURL = vi.fn(() => 'blob:mock');
      vi.spyOn(document.body, 'appendChild').mockImplementation(() => document.body);

      downloadCSV('a,b\n1,2', 'teste.csv');

      expect(document.createElement).toHaveBeenCalledWith('a');
      expect(mockLink.setAttribute).toHaveBeenCalledWith('download', 'teste.csv');
      expect(mockLink.click).toHaveBeenCalled();
      expect(mockLink.remove).toHaveBeenCalled();
    });
  });
});
