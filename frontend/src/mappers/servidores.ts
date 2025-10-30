import {Servidor} from '@/types/tipos';

/**
 * src/mappers/servidores.ts
 *
 * Mappers para converter objeto VW_USUARIO / JSON do backend para o tipo
 * frontend `Servidor`.
 *
 * Observações:
 * - O backend pode expor `titulo` (string) como identificador do usuário
 *   (`usuario_titulo`). Quando `titulo` for numérico, será convertido para
 *   `id` numérico. Caso contrário, tenta usar `id` fornecido; se nenhum
 *   identificador numérico estiver disponível, retorna `id = 0` (considere
 *   ajustar para sua estratégia de identificação real).
 * - Campos alternativos são suportados (nome_completo, unidade_codigo, etc).
 */

export function mapVWUsuarioToServidor(vw: any): Servidor {
  const candidateId =
    vw?.id ??
    vw?.codigo ?? // Adicionado para considerar vw.codigo
    (typeof vw?.titulo === 'string' && /^\d+$/.test(vw.titulo) ? Number(vw.titulo) : undefined) ??
    undefined;
  const codigo = Number(candidateId ?? 0);

  return {
    codigo,
    nome: vw?.nome ?? vw?.nome_completo ?? vw?.nome_usuario ?? '',
    unidade: vw?.unidade ?? vw?.unidade_sigla ?? vw?.unidade_codigo ?? '',
    email: vw?.email ?? null,
    ramal: vw?.ramal ?? vw?.ramal_telefone ?? null,
    tituloEleitoral: vw?.titulo_eleitoral ?? vw?.titulo ?? ''
  };
}

export function mapVWUsuariosArray(arr: any[] = []): Servidor[] {
  return arr.map(mapVWUsuarioToServidor);
}