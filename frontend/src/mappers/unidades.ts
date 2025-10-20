
import {mapVWUsuarioToServidor} from '@/mappers/servidores';
import {Unidade, UnidadeSnapshot} from '@/types/tipos';

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



export function mapUnidadeSnapshot(obj: any): UnidadeSnapshot {
  return {
    codigo: obj.codigo ?? obj.id ?? 0,
    nome: obj.nome ?? obj.nome_unidade ?? '',
    sigla: obj.sigla ?? obj.sigla_unidade ?? obj.unidade ?? '',
    filhas: Array.isArray(obj.filhas) ? obj.filhas.map(mapUnidadeSnapshot) : []
  };
}

export function mapUnidade(obj: any): Unidade {
  return {
    codigo: obj.id ?? obj.codigo ?? obj.codigo_unidade ?? 0,
    sigla: obj.sigla ?? obj.sigla_unidade ?? '',
    tipo: obj.tipo ?? obj.tipo_unidade ?? '',
    nome: obj.nome ?? obj.nome_unidade ?? '',
    idServidorTitular: obj.idServidorTitular ?? obj.id_servidor_titular ?? obj.titular_id ?? 0,
    responsavel: obj.responsavel ? {
      codigo: obj.responsavel.codigo ?? 0,
      nome: obj.responsavel.nome ?? '',
      tituloEleitoral: obj.responsavel.tituloEleitoral ?? '',
      unidade: obj.responsavel.unidade ?? {} as Unidade,
      email: obj.responsavel.email ?? '',
      ramal: obj.responsavel.ramal ?? '',
      usuarioTitulo: obj.responsavel.usuarioTitulo ?? '',
      unidadeCodigo: obj.responsavel.unidadeCodigo ?? 0,
      idServidor: obj.responsavel.idServidorResponsavel ?? 0,
      tipo: obj.responsavel.tipo ?? '',
      dataInicio: obj.responsavel.dataInicio ?? '',
      dataFim: obj.responsavel.dataFim ?? null,
    } : null,
    filhas: Array.isArray(obj.filhas) ? obj.filhas.map(mapUnidade) : []
  };
}

export function mapUnidadesArray(arr: any[] = []): Unidade[] {
  return arr.map(mapUnidade);
}

