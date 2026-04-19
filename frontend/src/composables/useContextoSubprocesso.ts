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
  store: DependenciasContextoSubprocesso;
}

export interface ResultadoContextoSubprocesso {
  codigo: number;
  contexto: ContextoEdicaoSubprocesso;
}

export async function carregarContextoSubprocessoInicial({
  codProcesso,
  siglaUnidade,
  store,
}: ParametrosCarregarContextoSubprocesso): Promise<ResultadoContextoSubprocesso | null> {
  return store.garantirContextoEdicaoPorProcessoEUnidade(codProcesso, siglaUnidade);
}
