import type {ContextoEdicaoSubprocesso} from '@/types/tipos';

interface DependenciasContextoSubprocesso {
  garantirContextoEdicao: (codigoSubprocesso: number) => Promise<ContextoEdicaoSubprocesso | null>;
  garantirContextoEdicaoPorProcessoEUnidade: (
    codigoProcesso: number,
    siglaUnidade: string,
  ) => Promise<{codigo: number; contexto: ContextoEdicaoSubprocesso} | null>;
}

interface ParametrosCarregarContextoSubprocesso {
  codProcesso: number;
  siglaUnidade: string;
  codSubprocessoQuery: unknown;
  store: DependenciasContextoSubprocesso;
}

export interface ResultadoContextoSubprocesso {
  codigo: number;
  contexto: ContextoEdicaoSubprocesso;
}

function extrairCodigoSubprocesso(codSubprocessoQuery: unknown): number | null {
  const codigoSubprocesso = Number(codSubprocessoQuery);
  if (!Number.isFinite(codigoSubprocesso) || codigoSubprocesso <= 0) {
    return null;
  }
  return codigoSubprocesso;
}

export async function carregarContextoSubprocessoInicial({
  codProcesso,
  siglaUnidade,
  codSubprocessoQuery,
  store,
}: ParametrosCarregarContextoSubprocesso): Promise<ResultadoContextoSubprocesso | null> {
  const codigoSubprocessoQuery = extrairCodigoSubprocesso(codSubprocessoQuery);

  if (codigoSubprocessoQuery) {
    const contexto = await store.garantirContextoEdicao(codigoSubprocessoQuery);
    if (contexto) {
      return {
        codigo: codigoSubprocessoQuery,
        contexto,
      };
    }
  }

  return store.garantirContextoEdicaoPorProcessoEUnidade(codProcesso, siglaUnidade);
}
