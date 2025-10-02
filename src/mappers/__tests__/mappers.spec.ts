import {describe, expect, it} from 'vitest';
import {mapVWUsuariosArray, mapVWUsuarioToServidor} from '@/mappers/servidores';
import {mapUnidade, mapUnidadesArray, mapUnidadeSnapshot} from '@/mappers/unidades';

describe('mappers/servidores', () => {
  it('mapVWUsuarioToServidor maps numeric titulo to id and fields', () => {
    const vw = { titulo: '42', nome: 'Fulano', unidade: 'SESEL', email: 'f@t.br', ramal: '123' };
    const s = mapVWUsuarioToServidor(vw);
    expect(s.id).toBe(42);
    expect(s.nome).toBe('Fulano');
    expect(s.unidade).toBe('SESEL');
    expect(s.email).toBe('f@t.br');
    expect(s.ramal).toBe('123');
  });

  it('mapVWUsuarioToServidor uses id when provided and defaults missing fields', () => {
    const vw = { id: 7, nome: 'Beltrano' };
    const s = mapVWUsuarioToServidor(vw);
    expect(s.id).toBe(7);
    expect(s.nome).toBe('Beltrano');
    expect(s.unidade).toBe('');
  });

  it('mapVWUsuariosArray maps array', () => {
    const arr = [{ id: 1, nome: 'A' }, { titulo: '2', nome: 'B' }];
    const res = mapVWUsuariosArray(arr);
    expect(res.length).toBe(2);
    expect(res[0].id).toBe(1);
    expect(res[1].id).toBe(2);
  });
});

describe('mappers/unidades', () => {
  it('mapUnidade maps fields and responsavel, and recursive filhas', () => {
    const u = {
      id: 10,
      sigla: 'SETEST',
      nome: 'Seção Teste',
      tipo: 'OPERACIONAL',
      idServidorTitular: 99,
      responsavel: {
        idServidorResponsavel: 100,
        tipo: 'Substituição',
        dataInicio: '2025-01-01',
        dataFim: null
      },
      filhas: [
        { id: 11, sigla: 'FILHA', nome: 'Filha', tipo: 'OPERACIONAL', filhas: [] }
      ]
    };

    const mapped = mapUnidade(u);
    expect(mapped.id).toBe(10);
    expect(mapped.sigla).toBe('SETEST');
    expect(mapped.nome).toBe('Seção Teste');
    expect(mapped.idServidorTitular).toBe(99);
    expect(mapped.responsavel).not.toBeNull();
    expect(mapped.responsavel!.idServidor).toBe(100);
    expect(Array.isArray(mapped.filhas)).toBe(true);
    expect(mapped.filhas[0].id).toBe(11);
  });

  it('mapUnidadeSnapshot maps simple snapshot structure recursively', () => {
    const s = { sigla: 'ROOT', tipo: 'OPERACIONAL', filhas: [{ sigla: 'C1', tipo: 'OPERACIONAL', filhas: [] }] };
    const snap = mapUnidadeSnapshot(s);
    expect(snap.sigla).toBe('ROOT');
    expect(snap.filhas.length).toBe(1);
    expect(snap.filhas[0].sigla).toBe('C1');
  });

  it('mapUnidadesArray maps arrays', () => {
    const arr = [{ id: 1, sigla: 'A' }, { id: 2, sigla: 'B' }];
    const res = mapUnidadesArray(arr);
    expect(res.length).toBe(2);
    expect(res[0].sigla).toBe('A');
  });
});