import fs from 'fs';
import path from 'path';
import {z} from 'zod';

/**
 * src/validators/mocks.ts
 *
 * Schemas Zod reutilizáveis para validação dos mocks em src/mocks/.
 * Exporta os schemas e uma função de validação que retorna um relatório
 * similar ao usado pelo script CLI `scripts/validar-mocks.js`.
 *
 * Objetivo: permitir que os testes unitários importem e re-utilizem os mesmos
 * schemas, evitando duplicação e mantendo validação consistente.
 */

// Diretório padrão dos mocks (relativo à raiz do projeto)
const DEFAULT_MOCKS_DIR = path.resolve(process.cwd(), 'src', 'mocks');

/* Schemas Zod mínimos por entidade (parciais / permissivos) */
export const processoItemSchema = z.object({
    id: z.number().optional(),
    codigo: z.number().optional(),
    descricao: z.string().optional(),
    tipo: z.string().optional(),
    situacao: z.string().optional(),
    dataLimite: z.string().optional(),
    data_limite: z.string().optional()
  }).catchall(z.unknown());
export const processosSchema = z.array(processoItemSchema);

export const subprocessoItemSchema = z.object({
    id: z.number().optional(),
    idProcesso: z.number().optional(),
    unidade: z.string().optional(),
    situacao: z.string().optional()
}).catchall(z.unknown());
export const subprocessosSchema = z.array(subprocessoItemSchema);

export const mapaItemSchema = z.object({
    id: z.number().optional(),
    unidade: z.string().optional(),
    situacao: z.string().optional(),
    competencias: z.array(z.unknown()).optional()
}).catchall(z.unknown());
export const mapasSchema = z.array(mapaItemSchema);

export const unidadeItemSchema = z.object({
    id: z.number().optional(),
    codigo: z.number().optional(),
    sigla: z.string().optional(),
    nome: z.string().optional()
}).catchall(z.unknown());
export const unidadesSchema = z.array(unidadeItemSchema);

export const servidorItemSchema = z.object({
    id: z.number().optional(),
    nome: z.string().optional(),
    unidade: z.string().optional()
}).catchall(z.unknown());
export const servidoresSchema = z.array(servidorItemSchema);

export const atividadeItemSchema = z.object({
    id: z.number().optional(),
    descricao: z.string().optional(),
    idSubprocesso: z.number().optional(),
    conhecimentos: z.array(z.unknown()).optional()
}).catchall(z.unknown());
export const atividadesSchema = z.array(atividadeItemSchema);

export const analiseItemSchema = z.object({
    id: z.number().optional(),
    idSubprocesso: z.number().optional(),
    dataHora: z.string().optional(),
    resultado: z.string().optional()
}).catchall(z.unknown());
export const analisesSchema = z.array(analiseItemSchema);

export const atribuicaoItemSchema = z.object({
    id: z.number().optional(),
    idServidor: z.number().optional(),
    unidade: z.string().optional(),
    dataInicio: z.string().optional(),
    dataTermino: z.string().optional()
}).catchall(z.unknown());
export const atribuicoesSchema = z.array(atribuicaoItemSchema);

export const alertaItemSchema = z.object({
    id: z.number().optional(),
    unidadeOrigem: z.string().optional(),
    unidadeDestino: z.string().optional(),
    dataHora: z.string().optional(),
    descricao: z.string().optional()
}).catchall(z.unknown());
export const alertasSchema = z.array(alertaItemSchema);

export const alertaServidorItemSchema = z.object({
    id: z.number().optional(),
    idAlerta: z.number().optional(),
    idServidor: z.number().optional(),
    lido: z.boolean().optional(),
    dataLeitura: z.string().nullable().optional()
}).catchall(z.unknown());
export const alertasServidorSchema = z.array(alertaServidorItemSchema);

// Mapeamento de arquivo -> schema exportado para reuso
/**
 * Carrega e parseia um mock JSON do diretório informado.
 */
export function carregarMock(nomeArquivo: string, mocksDir = DEFAULT_MOCKS_DIR): unknown {
    const caminho = path.join(mocksDir, nomeArquivo);
    if (!fs.existsSync(caminho)) {
        throw new Error(`Arquivo não encontrado: ${caminho}`);
    }
    const conteudo = fs.readFileSync(caminho, 'utf-8');
    return JSON.parse(conteudo);
}
