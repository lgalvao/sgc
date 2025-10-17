import {describe, expect, it} from 'vitest';

/**
 * Testes que validam os arquivos em src/mocks/ usando Zod.
 * O objetivo é garantir que os mocks respeitem um contrato mínimo.
 *
 * O teste é deliberadamente permissivo (campos opcionais) mas exige
 * a presença de campos essenciais como `situacao` onde aplicável.
 */
import {
    alertasSchema,
    alertasServidorSchema,
    analisesSchema,
    atividadesSchema,
    atribuicoesSchema,
    carregarMock,
    mapasSchema,
    processosSchema,
    servidoresSchema,
    subprocessosSchema,
    unidadesSchema
} from '@/validators/mocks';

/* Testes */
describe('Validação dos mocks com Zod', () => {
  it('processos.json deve validar contra o schema mínimo', () => {
    const data = carregarMock('processos.json');
    expect(() => processosSchema.parse(data)).not.toThrow();
  });

  it('subprocessos.json deve validar contra o schema mínimo', () => {
    const data = carregarMock('subprocessos.json');
    expect(() => subprocessosSchema.parse(data)).not.toThrow();
  });

  it('mapas.json deve validar contra o schema mínimo', () => {
    const data = carregarMock('mapas.json');
    expect(() => mapasSchema.parse(data)).not.toThrow();
  });

  it('unidades.json deve validar contra o schema mínimo', () => {
    const data = carregarMock('unidades.json');
    expect(() => unidadesSchema.parse(data)).not.toThrow();
  });

  it('servidores.json deve validar contra o schema mínimo', () => {
    const data = carregarMock('servidores.json');
    expect(() => servidoresSchema.parse(data)).not.toThrow();
  });

  it('atividades.json deve validar contra o schema mínimo', () => {
    const data = carregarMock('atividades.json');
    expect(() => atividadesSchema.parse(data)).not.toThrow();
  });

  it('analises.json deve validar contra o schema mínimo', () => {
    const data = carregarMock('analises.json');
    expect(() => analisesSchema.parse(data)).not.toThrow();
  });

  it('atribuicoes.json deve validar contra o schema mínimo', () => {
    const data = carregarMock('atribuicoes.json');
    expect(() => atribuicoesSchema.parse(data)).not.toThrow();
  });

  it('alertas.json deve validar contra o schema mínimo', () => {
    const data = carregarMock('alertas.json');
    expect(() => alertasSchema.parse(data)).not.toThrow();
  });

  it('alertas-servidor.json deve validar contra o schema mínimo', () => {
    const data = carregarMock('alertas-servidor.json');
    expect(() => alertasServidorSchema.parse(data)).not.toThrow();
  });
});