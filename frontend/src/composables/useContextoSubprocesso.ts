import type {ContextoEdicaoSubprocesso} from '@/types/tipos';
import type {NormalizedError} from '@/utils/apiError';

interface DependenciasContextoSubprocesso {
  garantirContextoEdicao: (codigoSubprocesso: number) => Promise<ContextoEdicaoSubprocesso | null>;
  garantirContextoEdicaoPorProcessoEUnidade: (
    codigoProcesso: number,
    siglaUnidade: string,
  ) => Promise<{codigo: number; contexto: ContextoEdicaoSubprocesso} | null>;
  erroIntegracaoContexto?: NormalizedError | null;
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

export type DiagnosticoCarregamentoContextoSubprocesso =
  | { tipo: 'sucesso'; resultado: ResultadoContextoSubprocesso }
  | { tipo: 'erroIntegracao'; erro: NormalizedError }
  | { tipo: 'ausencia' };

export async function carregarContextoSubprocessoInicial({
  codProcesso,
  siglaUnidade,
  store,
}: ParametrosCarregarContextoSubprocesso): Promise<ResultadoContextoSubprocesso | null> {
  return store.garantirContextoEdicaoPorProcessoEUnidade(codProcesso, siglaUnidade);
}

export async function diagnosticarCarregamentoContextoSubprocessoInicial({
  codProcesso,
  siglaUnidade,
  store,
}: ParametrosCarregarContextoSubprocesso): Promise<DiagnosticoCarregamentoContextoSubprocesso> {
  const resultado = await carregarContextoSubprocessoInicial({
    codProcesso,
    siglaUnidade,
    store,
  });

  if (resultado) {
    return {tipo: 'sucesso', resultado};
  }

  if (store.erroIntegracaoContexto) {
    return {tipo: 'erroIntegracao', erro: store.erroIntegracaoContexto};
  }

  return {tipo: 'ausencia'};
}
