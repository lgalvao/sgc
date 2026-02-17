import {describe, expect, it} from 'vitest';
import {SituacaoSubprocesso} from '@/types/tipos';
import {
    mapAtividadeOperacaoResponseToModel,
    mapAtividadeToModel,
    mapAtualizarAtividadeToDto,
    mapAtualizarConhecimentoToDto,
    mapConhecimentoToModel,
    mapCriarAtividadeRequestToDto,
    mapCriarConhecimentoRequestToDto,
    mapSubprocessoSituacaoToModel
} from '../atividades';

describe('atividades mappers', () => {
  it('mapAtividadeToModel handles null/undefined', () => {
    expect(mapAtividadeToModel(null)).toBeNull();
    expect(mapAtividadeToModel(undefined)).toBeNull();
  });

  it('mapAtividadeToModel maps correctly', () => {
    const dto = {
      codigo: 1,
      descricao: 'Atividade 1',
      conhecimentos: [{ codigo: 10, descricao: 'C1' }]
    };
    const model = mapAtividadeToModel(dto);
    expect(model).toEqual({
      codigo: 1,
      descricao: 'Atividade 1',
      conhecimentos: [{ codigo: 10, descricao: 'C1' }]
    });
  });

  it('mapConhecimentoToModel handles null/undefined', () => {
    expect(mapConhecimentoToModel(null)).toBeNull();
    expect(mapConhecimentoToModel(undefined)).toBeNull();
  });

  it('mapSubprocessoSituacaoToModel maps correctly', () => {
    const dto = { codigo: 1, situacao: SituacaoSubprocesso.NAO_INICIADO, situacaoLabel: 'L1' };
    expect(mapSubprocessoSituacaoToModel(dto)).toEqual(dto);
  });

  it('mapAtividadeOperacaoResponseToModel maps correctly', () => {
    const dto = {
      atividade: { codigo: 1, descricao: 'A1', conhecimentos: [] },
      subprocesso: { codigo: 1, situacao: SituacaoSubprocesso.NAO_INICIADO, situacaoLabel: 'L1' },
      atividadesAtualizadas: [{ codigo: 2, descricao: 'A2', conhecimentos: [] }]
    };
    const model = mapAtividadeOperacaoResponseToModel(dto as any);
    expect(model.atividade?.codigo).toBe(1);
    expect(model.subprocesso.codigo).toBe(1);
    expect(model.atividadesAtualizadas[0].codigo).toBe(2);
  });

  it('mapAtividadeOperacaoResponseToModel handles null atividade', () => {
    const dto = {
      atividade: null,
      subprocesso: { codigo: 1, situacao: SituacaoSubprocesso.NAO_INICIADO, situacaoLabel: 'L1' },
      atividadesAtualizadas: []
    };
    const model = mapAtividadeOperacaoResponseToModel(dto as any);
    expect(model.atividade).toBeNull();
  });

  it('mapCriarAtividadeRequestToDto adds codMapa', () => {
    const request = { descricao: 'Nova' };
    const dto = mapCriarAtividadeRequestToDto(request, 10);
    expect(dto).toEqual({ descricao: 'Nova', mapaCodigo: 10 });
  });

  it('mapAtualizarAtividadeToDto returns only descricao', () => {
    const request = { codigo: 1, descricao: 'Edit', conhecimentos: [] };
    const dto = mapAtualizarAtividadeToDto(request);
    expect(dto).toEqual({ descricao: 'Edit' });
  });

  it('mapCriarConhecimentoRequestToDto maps correctly', () => {
    const request = { descricao: 'C1' };
    const dto = mapCriarConhecimentoRequestToDto(request, 1);
    expect(dto).toEqual({ descricao: 'C1', atividadeCodigo: 1 });
  });

  it('mapAtualizarConhecimentoToDto maps correctly', () => {
    const request = { codigo: 10, descricao: 'C1' };
    const dto = mapAtualizarConhecimentoToDto(request, 1);
    expect(dto).toEqual({ codigo: 10, atividadeCodigo: 1, descricao: 'C1' });
  });

  it('mapAtividadeToModel filters out null conhecimentos', () => {
    const dto = {
      codigo: 1,
      descricao: 'A1',
      conhecimentos: [null, { codigo: 10, descricao: 'C1' }]
    };
    const model = mapAtividadeToModel(dto as any);
    expect(model?.conhecimentos).toHaveLength(1);
    expect(model?.conhecimentos[0].codigo).toBe(10);
  });

  it('mapAtividadeOperacaoResponseToModel filters out null atividadesAtualizadas', () => {
    const dto = {
      atividade: null,
      subprocesso: { codigo: 1, situacao: SituacaoSubprocesso.NAO_INICIADO, situacaoLabel: 'L1' },
      atividadesAtualizadas: [null, { codigo: 1, descricao: 'A1', conhecimentos: [] }]
    };
    const model = mapAtividadeOperacaoResponseToModel(dto as any);
    expect(model.atividadesAtualizadas).toHaveLength(1);
  });
});
