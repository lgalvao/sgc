import { APIRequestContext } from '@playwright/test';
import { DADOS_TESTE, URLS } from '../dados/constantes-teste';

export async function ensureUser(request: APIRequestContext, user: { titulo?: string | number; username?: string; nome?: string; displayName?: string; perfis?: string[]; unidade?: number | string }) {
  const titulo = (user.titulo ?? user.username ?? user.displayName ?? '0').toString();
  const base = URLS.API_BASE || 'http://localhost:10000/api';
  const resp = await request.get(`${base}/sgrh/usuarios/${titulo}`);
  if (resp.ok()) return await resp.json();

  // Create via test endpoint
  const create = await request.post(`${base.replace(/\/api$/, '')}/api/test/usuarios`, { data: {
    titulo: titulo,
    nome: user.nome ?? user.displayName ?? `Usuario ${titulo}`,
    unidade: user.unidade ?? DADOS_TESTE.UNIDADES.STIC
  }});
  if (!create.ok()) throw new Error(`Falha ao criar usuário ${titulo}: ${create.status()}`);
  return await create.json();
}

export async function ensureUnidade(request: APIRequestContext, codigo: number | string) {
  const base = URLS.API_BASE || 'http://localhost:10000/api';
  const resp = await request.get(`${base}/unidades/${codigo}`);
  if (resp.ok()) return await resp.json();
  const create = await request.post(`${base.replace(/\/api$/, '')}/api/test/unidades`, { data: { codigo, nome: `UNIT-${codigo}`, sigla: `U${codigo}` }});
  if (!create.ok()) throw new Error(`Falha ao criar unidade ${codigo}: ${create.status()}`);
  return await create.json();
}

export async function ensureProcesso(request: APIRequestContext, processo: { descricao: string; tipo?: string; situacao?: string; dataLimite?: string; unidades?: Array<number | string> }) {
  const base = URLS.API_BASE || 'http://localhost:10000/api';
  const resp = await request.get(`${base.replace(/\/api$/, '')}/api/test/processos?descricao=${encodeURIComponent(processo.descricao)}`);
  if (resp.ok()) return await resp.json();
  const create = await request.post(`${base.replace(/\/api$/, '')}/api/test/processos`, { data: processo });
  if (!create.ok()) throw new Error(`Falha ao criar processo ${processo.descricao}: ${create.status()}`);
  return await create.json();
}

export async function prepareDefaultScenario(request: APIRequestContext) {
  await ensureUnidade(request, DADOS_TESTE.UNIDADES.STIC);
  await ensureUser(request, { titulo: DADOS_TESTE.USUARIOS?.ADMIN?.titulo ?? '6', nome: DADOS_TESTE.USUARIOS?.ADMIN?.nome, unidade: DADOS_TESTE.UNIDADES.STIC });
}