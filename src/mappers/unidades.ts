import {parseDate} from '@/utils';
import {Responsavel, Unidade, UnidadeSnapshot} from '@/types/tipos';

/**
 * src/mappers/unidades.ts
 *
 * Mappers para converter objetos vindos de `UNIDADE` / `UNIDADE_PROCESSO` (mocks/visões)
 * para os tipos frontend `Unidade` e `UnidadeSnapshot`.
 *
 * Regras:
 * - aceita campos alternativos: codigo/id, sigla, nome, idServidorTitular / titular_titulo
 * - responsavel pode vir com chaves diferentes; mapeia para `Responsavel` com datas convertidas
 * - filhas (children) são mapeadas recursivamente
 */

function mapResponsavel(obj: any): Responsavel | null {
  if (!obj) return null;
  const idServidor =
    obj.idServidor ?? obj.idServidorResponsavel ?? obj.id_servidor_responsavel ?? null;
  const tipo =
    obj.tipo ?? obj.tipo_responsavel ?? null;
  const dataInicio = obj.dataInicio ?? obj.data_inicio ?? null;
  const dataFim = obj.dataFim ?? obj.data_fim ?? null;

  return {
    idServidor: idServidor ?? 0,
    tipo: (tipo as any) ?? 'Substituição',
    dataInicio: dataInicio ? parseDate(dataInicio) : null,
    dataFim: dataFim ? parseDate(dataFim) : null
  };
}

export function mapUnidadeSnapshot(obj: any): UnidadeSnapshot {
  return {
    sigla: obj.sigla ?? obj.sigla_unidade ?? obj.unidade ?? '',
    tipo: obj.tipo ?? obj.tipo_unidade ?? '',
    filhas: Array.isArray(obj.filhas) ? obj.filhas.map(mapUnidadeSnapshot) : []
  };
}

export function mapUnidade(obj: any): Unidade {
  return {
    id: obj.id ?? obj.codigo ?? obj.codigo_unidade ?? 0,
    sigla: obj.sigla ?? obj.sigla_unidade ?? '',
    tipo: obj.tipo ?? obj.tipo_unidade ?? '',
    nome: obj.nome ?? obj.nome_unidade ?? '',
    idServidorTitular: obj.idServidorTitular ?? obj.id_servidor_titular ?? obj.titular_id ?? 0,
    responsavel: mapResponsavel(obj.responsavel ?? obj.responsavel_titulo ?? null),
    filhas: Array.isArray(obj.filhas) ? obj.filhas.map(mapUnidade) : []
  };
}

export function mapUnidadesArray(arr: any[] = []): Unidade[] {
  return arr.map(mapUnidade);
}

