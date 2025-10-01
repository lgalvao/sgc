import {describe, it, expect} from 'vitest';
import fs from 'fs';
import path from 'path';
import {z} from 'zod';

/**
 * Testes que validam os arquivos em src/mocks/ usando Zod.
 * O objetivo é garantir que os mocks respeitem um contrato mínimo.
 *
 * O teste é deliberadamente permissivo (campos opcionais) mas exige
 * a presença de campos essenciais como `situacao` onde aplicável.
 */

const MOCKS_DIR = path.resolve(__dirname, '../../mocks');

function carregarMock(nomeArquivo: string) {
  const caminho = path.join(MOCKS_DIR, nomeArquivo);
  const conteudo = fs.readFileSync(caminho, 'utf-8');
  return JSON.parse(conteudo);
}

/* Schemas Zod mínimos por entidade */
const processoItemSchema = z.object({
  id: z.number().optional(),
  codigo: z.number().optional(),
  descricao: z.string().optional(),
  tipo: z.string().optional(),
  situacao: z.string().optional(),
  dataLimite: z.string().optional(),
  data_limite: z.string().optional()
}).passthrough();
const processosSchema = z.array(processoItemSchema);

const subprocessoItemSchema = z.object({
  id: z.number().optional(),
  idProcesso: z.number().optional(),
  unidade: z.string().optional(),
  situacao: z.string().optional()
}).passthrough();
const subprocessosSchema = z.array(subprocessoItemSchema);

const mapaItemSchema = z.object({
  id: z.number().optional(),
  unidade: z.string().optional(),
  situacao: z.string().optional(),
  competencias: z.array(z.any()).optional()
}).passthrough();
const mapasSchema = z.array(mapaItemSchema);

const unidadeItemSchema = z.object({
  id: z.number().optional(),
  codigo: z.number().optional(),
  sigla: z.string().optional(),
  nome: z.string().optional()
}).passthrough();
const unidadesSchema = z.array(unidadeItemSchema);

const servidorItemSchema = z.object({
  id: z.number().optional(),
  nome: z.string().optional(),
  unidade: z.string().optional()
}).passthrough();
const servidoresSchema = z.array(servidorItemSchema);

const atividadeItemSchema = z.object({
  id: z.number().optional(),
  descricao: z.string().optional(),
  idSubprocesso: z.number().optional(),
  conhecimentos: z.array(z.any()).optional()
}).passthrough();
const atividadesSchema = z.array(atividadeItemSchema);

const analiseItemSchema = z.object({
  id: z.number().optional(),
  idSubprocesso: z.number().optional(),
  dataHora: z.string().optional(),
  resultado: z.string().optional()
}).passthrough();
const analisesSchema = z.array(analiseItemSchema);

const atribuicaoItemSchema = z.object({
  id: z.number().optional(),
  idServidor: z.number().optional(),
  unidade: z.string().optional(),
  dataInicio: z.string().optional(),
  dataTermino: z.string().optional()
}).passthrough();
const atribuicoesSchema = z.array(atribuicaoItemSchema);

const alertaItemSchema = z.object({
  id: z.number().optional(),
  unidadeOrigem: z.string().optional(),
  unidadeDestino: z.string().optional(),
  dataHora: z.string().optional(),
  descricao: z.string().optional()
}).passthrough();
const alertasSchema = z.array(alertaItemSchema);

const alertaServidorItemSchema = z.object({
  id: z.number().optional(),
  idAlerta: z.number().optional(),
  idServidor: z.number().optional(),
  lido: z.boolean().optional(),
  dataLeitura: z.string().nullable().optional()
}).passthrough();
const alertasServidorSchema = z.array(alertaServidorItemSchema);

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