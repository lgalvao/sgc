/**
 * API Setup Helpers
 * 
 * Funções auxiliares para preparar cenários de teste via API usando os endpoints /api/test/*
 * Estes endpoints são disponíveis apenas quando o backend está rodando com profile e2e.
 * 
 * IMPORTANTE: Estas funções fazem requisições HTTP diretamente aos endpoints de teste,
 * sem usar a UI do Playwright. Isso permite preparar dados de teste de forma rápida e confiável.
 */

import { Page } from '@playwright/test';

const API_BASE_URL = 'http://localhost:10000/api/test';

/**
 * Dados para criar um usuário de teste
 */
export interface UsuarioTestData {
  tituloEleitoral: number;
  nome: string;
  email: string;
  ramal: string;
  unidadeCodigo: number;
  perfis: string[]; // Ex: ['ADMIN', 'GESTOR']
}

/**
 * Dados para criar uma unidade de teste
 */
export interface UnidadeTestData {
  codigo: number;
  nome: string;
  sigla: string;
  tipo: 'INTEROPERACIONAL' | 'INTERMEDIARIA' | 'OPERACIONAL';
  unidadeSuperiorCodigo?: number | null;
}

/**
 * Dados para criar um processo de teste
 */
export interface ProcessoTestData {
  descricao: string;
  tipo: 'MAPEAMENTO' | 'REVISAO' | 'DIAGNOSTICO';
  situacao: 'EM_ELABORACAO' | 'EM_ANDAMENTO' | 'FINALIZADO';
  dataLimite?: string; // ISO 8601 format
  unidadesCodigos: number[]; // Lista de códigos de unidades participantes
}

/**
 * Garante que um usuário existe no banco de dados
 * 
 * @param page - Instância do Page do Playwright (usado apenas para context de request)
 * @param usuario - Dados do usuário
 * @returns Promise<void>
 * 
 * @example
 * ```ts
 * await garantirUsuario(page, {
 *   tituloEleitoral: 777,
 *   nome: 'Teste Admin',
 *   email: 'teste@test.com',
 *   ramal: '1234',
 *   unidadeCodigo: 2,
 *   perfis: ['ADMIN', 'GESTOR']
 * });
 * ```
 */
export async function garantirUsuario(page: Page, usuario: UsuarioTestData): Promise<void> {
  const response = await page.request.post(`${API_BASE_URL}/usuarios`, {
    data: usuario,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Falha ao criar usuário: ${response.status()} - ${body}`);
  }
}

/**
 * Garante que uma unidade existe no banco de dados
 * 
 * @param page - Instância do Page do Playwright
 * @param unidade - Dados da unidade
 * @returns Promise<void>
 * 
 * @example
 * ```ts
 * await garantirUnidade(page, {
 *   codigo: 999,
 *   nome: 'Unidade Teste',
 *   sigla: 'UTEST',
 *   tipo: 'OPERACIONAL',
 *   unidadeSuperiorCodigo: 2
 * });
 * ```
 */
export async function garantirUnidade(page: Page, unidade: UnidadeTestData): Promise<void> {
  const response = await page.request.post(`${API_BASE_URL}/unidades`, {
    data: unidade,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Falha ao criar unidade: ${response.status()} - ${body}`);
  }
}

/**
 * Garante que um processo existe no banco de dados
 * 
 * @param page - Instância do Page do Playwright
 * @param processo - Dados do processo
 * @returns Promise<number> - Retorna o código do processo criado
 * 
 * @example
 * ```ts
 * const processoId = await garantirProcesso(page, {
 *   descricao: 'Processo de Teste',
 *   tipo: 'MAPEAMENTO',
 *   situacao: 'EM_ELABORACAO',
 *   dataLimite: '2025-12-31T23:59:59',
 *   unidadesCodigos: [2, 3, 8]
 * });
 * ```
 */
export async function garantirProcesso(page: Page, processo: ProcessoTestData): Promise<number> {
  const response = await page.request.post(`${API_BASE_URL}/processos`, {
    data: processo,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Falha ao criar processo: ${response.status()} - ${body}`);
  }

  const result = await response.json();
  return result.codigo; // Retorna o código do processo criado
}

/**
 * Setup completo de um cenário de teste com usuário, unidades e processo
 * 
 * @param page - Instância do Page do Playwright
 * @param config - Configuração do cenário
 * @returns Promise<{ processoId: number }>
 * 
 * @example
 * ```ts
 * const { processoId } = await setupCenarioCompleto(page, {
 *   usuario: {
 *     tituloEleitoral: 888,
 *     nome: 'Gestor Teste',
 *     email: 'gestor.teste@test.com',
 *     ramal: '5555',
 *     unidadeCodigo: 2,
 *     perfis: ['GESTOR']
 *   },
 *   unidades: [
 *     { codigo: 998, nome: 'Unidade A', sigla: 'UNA', tipo: 'OPERACIONAL' },
 *     { codigo: 997, nome: 'Unidade B', sigla: 'UNB', tipo: 'OPERACIONAL' }
 *   ],
 *   processo: {
 *     descricao: 'Mapeamento 2025',
 *     tipo: 'MAPEAMENTO',
 *     situacao: 'EM_ELABORACAO',
 *     unidadesCodigos: [998, 997]
 *   }
 * });
 * ```
 */
export async function setupCenarioCompleto(
  page: Page,
  config: {
    usuario?: UsuarioTestData;
    unidades?: UnidadeTestData[];
    processo?: ProcessoTestData;
  }
): Promise<{ processoId?: number }> {
  // Criar usuário se fornecido
  if (config.usuario) {
    await garantirUsuario(page, config.usuario);
  }

  // Criar unidades se fornecidas
  if (config.unidades && config.unidades.length > 0) {
    for (const unidade of config.unidades) {
      await garantirUnidade(page, unidade);
    }
  }

  // Criar processo se fornecido
  let processoId: number | undefined;
  if (config.processo) {
    processoId = await garantirProcesso(page, config.processo);
  }

  return { processoId };
}
