import {describe, it, expect, vi} from 'vitest';
import {gerarCSV, downloadCSV} from '../csv';

describe('csv utils', () => {
  it('gerarCSV gera string correta e sanitiza valores', () => {
    const dados = [
        { nome: 'João', valor: 100 },
        { nome: '=SOMA(A1:A2)', valor: '+50' }
    ];

    const csv = gerarCSV(dados);
    expect(csv).toContain('"João"');
    expect(csv).toContain('"\'=SOMA(A1:A2)"');
    expect(csv).toContain('"\'+50"');
    expect(csv).toContain('"100"');
  });

  it('gerarCSV retorna string vazia para dados vazios', () => {
    expect(gerarCSV([])).toBe('');
  });

  it('downloadCSV cria link e clica', () => {
    const csv = 'col1,col2\nval1,val2';
    const filename = 'teste.csv';

    // Mock global URL
    global.URL.createObjectURL = vi.fn(() => 'blob:test');

    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {});
    const appendSpy = vi.spyOn(document.body, 'appendChild');

    downloadCSV(csv, filename);

    expect(global.URL.createObjectURL).toHaveBeenCalled();
    expect(appendSpy).toHaveBeenCalled();
    expect(clickSpy).toHaveBeenCalled();
  });
});
