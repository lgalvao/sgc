import type {Ref} from "vue";
import type {ContextoSubprocesso} from "@/stores/subprocessoStoreHelpers";

export type ChaveCarregamento =
    | "EDICAO_CODIGO"
    | "EDICAO_PROCESSO_UNIDADE"
    | "CADASTRO_CODIGO"
    | "CADASTRO_PROCESSO_UNIDADE";

export type ConfiguracaoContexto<T extends ContextoSubprocesso> = {
    tipoCodigo: ChaveCarregamento;
    tipoProcessoUnidade: ChaveCarregamento;
    contextoRef: Ref<T | null>;
    contextoInvalidoRef: Ref<boolean>;
    contextoSessaoRef: Ref<string | null>;
    codigosPorProcessoUnidade: Map<string, number>;
    buscarPorCodigo: (codigoSubprocesso: number) => Promise<T>;
    buscarPorProcessoEUnidade: (codProcesso: number, siglaUnidade: string) => Promise<T>;
    registrar: (contexto: T) => void;
    mensagemCodigo: (codigoSubprocesso: number) => string;
    mensagemProcessoUnidade: (codProcesso: number, siglaUnidade: string) => string;
};
