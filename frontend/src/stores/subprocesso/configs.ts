import type {Ref} from "vue";
import type {ContextoCadastroAtividadesSubprocesso, ContextoEdicaoSubprocesso} from "@/types/tipos";
import {
    buscarContextoCadastroAtividades as serviceBuscarContextoCadastroAtividades,
    buscarContextoCadastroAtividadesPorProcessoEUnidade as serviceBuscarContextoCadastroAtividadesPorProcessoEUnidade,
    buscarContextoEdicao as serviceBuscarContextoEdicao,
    buscarContextoEdicaoPorProcessoEUnidade as serviceBuscarContextoEdicaoPorProcessoEUnidade,
} from "@/services/subprocessoService";
import type {ConfiguracaoContexto} from "./tipos";

export function criarConfigs(
    contextoEdicao: Ref<ContextoEdicaoSubprocesso | null>,
    contextoEdicaoInvalido: Ref<boolean>,
    codigosEdicaoPorProcessoUnidade: Map<string, number>,
    registrarContextoEdicao: (c: ContextoEdicaoSubprocesso) => void,
    contextoCadastro: Ref<ContextoCadastroAtividadesSubprocesso | null>,
    contextoCadastroInvalido: Ref<boolean>,
    codigosCadastroPorProcessoUnidade: Map<string, number>,
    registrarContextoCadastro: (c: ContextoCadastroAtividadesSubprocesso) => void,
) {
    const configEdicao: ConfiguracaoContexto<ContextoEdicaoSubprocesso> = {
        tipoCodigo: "EDICAO_CODIGO",
        tipoProcessoUnidade: "EDICAO_PROCESSO_UNIDADE",
        contextoRef: contextoEdicao,
        contextoInvalidoRef: contextoEdicaoInvalido,
        codigosPorProcessoUnidade: codigosEdicaoPorProcessoUnidade,
        buscarPorCodigo: serviceBuscarContextoEdicao,
        buscarPorProcessoEUnidade: serviceBuscarContextoEdicaoPorProcessoEUnidade,
        registrar: registrarContextoEdicao,
        mensagemCodigo: (codigo) => `Falha grave ao localizar o subprocesso ${codigo}.`,
        mensagemProcessoUnidade: (codProcesso, sigla) =>
            `Falha grave ao resolver subprocesso do processo ${codProcesso} para a unidade ${sigla}.`,
    };

    const configCadastro: ConfiguracaoContexto<ContextoCadastroAtividadesSubprocesso> = {
        tipoCodigo: "CADASTRO_CODIGO",
        tipoProcessoUnidade: "CADASTRO_PROCESSO_UNIDADE",
        contextoRef: contextoCadastro,
        contextoInvalidoRef: contextoCadastroInvalido,
        codigosPorProcessoUnidade: codigosCadastroPorProcessoUnidade,
        buscarPorCodigo: serviceBuscarContextoCadastroAtividades,
        buscarPorProcessoEUnidade: serviceBuscarContextoCadastroAtividadesPorProcessoEUnidade,
        registrar: registrarContextoCadastro,
        mensagemCodigo: (codigo) => `Falha grave ao carregar o cadastro do subprocesso ${codigo}.`,
        mensagemProcessoUnidade: (codProcesso, sigla) =>
            `Falha grave ao resolver o cadastro do processo ${codProcesso} para a unidade ${sigla}.`,
    };

    return {configEdicao, configCadastro};
}
