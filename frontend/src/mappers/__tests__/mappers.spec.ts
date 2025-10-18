import {describe, expect, it} from 'vitest';
import { mapAlertaDtoToFrontend } from '../alertas';
import {mapVWUsuariosArray, mapVWUsuarioToServidor} from '@/mappers/servidores';
import {mapUnidade, mapUnidadesArray, mapUnidadeSnapshot} from '@/mappers/unidades';

describe('mappers/alertas', () => {
    it('mapAlertaDtoToFrontend should map all fields correctly', () => {
      const dto = {
        codigo: 1,
        processoCodigo: 10,
        descricao: 'Test Alert',
        dataHora: '2025-10-18T10:00:00',
        unidadeOrigemCodigo: 100,
        unidadeDestinoCodigo: 200,
        usuarioDestinoTitulo: '123456789012',
      };

      const mapped = mapAlertaDtoToFrontend(dto);

      expect(mapped.codigo).toBe(1);
      expect(mapped.processoCodigo).toBe(10);
      expect(mapped.descricao).toBe('Test Alert');
      expect(mapped.dataHora).toBe('2025-10-18T10:00:00');
      expect(mapped.unidadeOrigemCodigo).toBe(100);
      expect(mapped.unidadeDestinoCodigo).toBe(200);
      expect(mapped.usuarioDestinoTitulo).toBe('123456789012');
    });
});

import {
    mapAtividadeDtoToModel,
    mapConhecimentoDtoToModel,
    mapCriarAtividadeRequestToDto,
    mapCriarConhecimentoRequestToDto,
} from '../atividades';

describe('mappers/atividades', () => {
    it('mapAtividadeDtoToModel should map correctly', () => {
        const dto = {
            codigo: 1,
            descricao: 'Atividade Teste',
            conhecimentos: [{ codigo: 101, descricao: 'Conhecimento Teste' }],
        };
        const model = mapAtividadeDtoToModel(dto);
        expect(model.codigo).toBe(1);
        expect(model.descricao).toBe('Atividade Teste');
        expect(model.conhecimentos).toHaveLength(1);
        expect(model.conhecimentos[0].codigo).toBe(101);
    });

    it('mapConhecimentoDtoToModel should map correctly', () => {
        const dto = { codigo: 101, descricao: 'Conhecimento Teste' };
        const model = mapConhecimentoDtoToModel(dto);
        expect(model.codigo).toBe(101);
        expect(model.descricao).toBe('Conhecimento Teste');
    });

    it('mapCriarAtividadeRequestToDto should add idSubprocesso', () => {
        const request = { descricao: 'Nova Atividade' };
        const dto = mapCriarAtividadeRequestToDto(request, 123);
        expect(dto.descricao).toBe('Nova Atividade');
        expect(dto.idSubprocesso).toBe(123);
    });

    it('mapCriarConhecimentoRequestToDto should map correctly', () => {
        const request = { descricao: 'Novo Conhecimento' };
        const dto = mapCriarConhecimentoRequestToDto(request);
        expect(dto.descricao).toBe('Novo Conhecimento');
    });
});

import {
    mapMapaVisualizacaoToAtividades,
    mapMapaDtoToModel,
    mapMapaCompletoDtoToModel,
    mapImpactoMapaDtoToModel,
    mapMapaAjusteDtoToModel,
} from '../mapas';

describe('mappers/mapas', () => {
    it('mapMapaVisualizacaoToAtividades should extract and map all activities', () => {
        const dto = {
            competencias: [
                { atividades: [{ codigo: 1, descricao: 'A1', conhecimentos: [] }] },
                { atividades: [{ codigo: 2, descricao: 'A2', conhecimentos: [] }, { codigo: 3, descricao: 'A3', conhecimentos: [] }] },
            ]
        };
        const atividades = mapMapaVisualizacaoToAtividades(dto);
        expect(atividades).toHaveLength(3);
        expect(atividades[0].codigo).toBe(1);
        expect(atividades[2].codigo).toBe(3);
    });

    it('mapMapaDtoToModel should map fields and handle dates', () => {
        const dto = {
            id: 1,
            dataCriacao: '2025-01-01T00:00:00Z',
            dataDisponibilizacao: '2025-01-02T00:00:00Z',
            competencias: [],
        };
        const model = mapMapaDtoToModel(dto);
        expect(model.id).toBe(1);
        expect(model.dataCriacao).toBeInstanceOf(Date);
        expect(model.dataDisponibilizacao).toBeInstanceOf(Date);
        expect(model.dataFinalizacao).toBeNull();
    });

    it('mapMapaCompletoDtoToModel should map nested structures', () => {
        const dto = {
            codigo: 1,
            competencias: [{ codigo: 10, atividades: [{ codigo: 100, conhecimentos: [{ codigo: 1000 }] }] }],
        };
        const model = mapMapaCompletoDtoToModel(dto);
        expect(model.competencias[0].atividades[0].conhecimentos[0].codigo).toBe(1000);
    });

    it('mapImpactoMapaDtoToModel should map impact fields', () => {
        const dto = {
            temImpacto: true,
            competencias: [{ id: 1, atividadesAdicionadas: ['A1'] }],
        };
        const model = mapImpactoMapaDtoToModel(dto);
        expect(model.temImpacto).toBe(true);
        expect(model.competencias[0].atividadesAdicionadas).toContain('A1');
    });

    it('mapMapaAjusteDtoToModel should map ajuste fields', () => {
        const dto = { competencias: [1, 2], sugestoes: 'N/A' };
        const model = mapMapaAjusteDtoToModel(dto);
        expect(model.competencias).toHaveLength(2);
        expect(model.sugestoes).toBe('N/A');
    });
});

import {
    mapProcessoResumoDtoToFrontend,
    mapProcessoDtoToFrontend,
    mapProcessoDetalheDtoToFrontend,
} from '../processos';

describe('mappers/processos', () => {
    it('mapProcessoResumoDtoToFrontend should copy all properties', () => {
        const dto = { id: 1, tipo: 'MAPEAMENTO', situacao: 'EM_ANDAMENTO' };
        const model = mapProcessoResumoDtoToFrontend(dto);
        expect(model).toEqual(dto);
    });

    it('mapProcessoDtoToFrontend should copy all properties', () => {
        const dto = { id: 1, tipo: 'REVISAO', situacao: 'INICIADO', responsavel: 'user' };
        const model = mapProcessoDtoToFrontend(dto);
        expect(model).toEqual(dto);
    });

    it('mapProcessoDetalheDtoToFrontend should map nested structures', () => {
        const dto = {
            id: 1,
            unidades: [{ id: 10, filhos: [{ id: 11 }] }],
            resumoSubprocessos: [{ id: 100 }],
        };
        const model = mapProcessoDetalheDtoToFrontend(dto);
        expect(model.unidades[0].filhos[0].id).toBe(11);
        expect(model.resumoSubprocessos[0].id).toBe(100);
    });
});

import { mapPerfilUnidadeToFrontend, mapUsuarioToFrontend } from '../sgrh';

describe('mappers/sgrh', () => {
    it('mapPerfilUnidadeToFrontend should map correctly', () => {
        const dto = {
            perfil: 'CHEFE',
            unidade: { codigo: 1, nome: 'Unidade Teste', sigla: 'UT' },
            siglaUnidade: 'UT',
        };
        const model = mapPerfilUnidadeToFrontend(dto);
        expect(model.perfil).toBe('CHEFE');
        expect(model.unidade.codigo).toBe(1);
        expect(model.siglaUnidade).toBe('UT');
    });

    it('mapUsuarioToFrontend should map correctly', () => {
        const dto = {
            tituloEleitoral: 123,
            nome: 'Usuário Teste',
            email: 'test@test.com',
            ramal: '1234',
            unidade: { codigo: 1, nome: 'Unidade Teste', sigla: 'UT' },
            perfis: ['CHEFE'],
        };
        const model = mapUsuarioToFrontend(dto);
        expect(model.tituloEleitoral).toBe(123);
        expect(model.nome).toBe('Usuário Teste');
        expect(model.unidade.sigla).toBe('UT');
        expect(model.perfis).toContain('CHEFE');
    });
});

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