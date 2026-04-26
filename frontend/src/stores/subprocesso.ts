import {defineStore} from "pinia";
import {ref} from "vue";
import type {
    ContextoCadastroAtividadesSubprocesso,
    ContextoEdicaoSubprocesso,
    PermissoesSubprocesso,
    SituacaoSubprocesso,
} from "@/types/tipos";
import {
    buscarContextoCadastroAtividades as serviceBuscarContextoCadastroAtividades,
    buscarContextoCadastroAtividadesPorProcessoEUnidade as serviceBuscarContextoCadastroAtividadesPorProcessoEUnidade,
    buscarContextoEdicao as serviceBuscarContextoEdicao,
    buscarContextoEdicaoPorProcessoEUnidade as serviceBuscarContextoEdicaoPorProcessoEUnidade,
} from "@/services/subprocessoService";
import {logger} from "@/utils";
import {type NormalizedError, normalizeError} from "@/utils/apiError";

type ChaveCarregamento =
    | "EDICAO_CODIGO"
    | "EDICAO_PROCESSO_UNIDADE"
    | "CADASTRO_CODIGO"
    | "CADASTRO_PROCESSO_UNIDADE";

export const useSubprocessoStore = defineStore("subprocesso", () => {
    const contextoEdicao = ref<ContextoEdicaoSubprocesso | null>(null);
    const contextoCadastro = ref<ContextoCadastroAtividadesSubprocesso | null>(null);
    const erroIntegracaoContexto = ref<NormalizedError | null>(null);
    const carregamentos = new Map<string, Promise<unknown>>();
    const codigosEdicaoPorProcessoUnidade = new Map<string, number>();
    const codigosCadastroPorProcessoUnidade = new Map<string, number>();

    function gerarChave(tipo: ChaveCarregamento, identificador: number | string): string {
        return `${tipo}:${identificador}`;
    }

    function gerarChaveProcessoUnidade(codigoProcesso: number, siglaUnidade: string): string {
        return `${codigoProcesso}:${siglaUnidade}`;
    }

    function limparContextoAtual(): void {
        contextoEdicao.value = null;
        contextoCadastro.value = null;
    }

    function limparErroIntegracao(): void {
        erroIntegracaoContexto.value = null;
    }

    function invalidar(): void {
        carregamentos.clear();
        codigosEdicaoPorProcessoUnidade.clear();
        codigosCadastroPorProcessoUnidade.clear();
        limparErroIntegracao();
        limparContextoAtual();
    }

    function registrarContextoEdicao(contexto: ContextoEdicaoSubprocesso): void {
        contextoEdicao.value = contexto;
        limparErroIntegracao();
    }

    function registrarContextoCadastro(contexto: ContextoCadastroAtividadesSubprocesso): void {
        contextoCadastro.value = contexto;
        limparErroIntegracao();
    }

    function criarErroSubprocessoNaoEncontrado(mensagemBase: string, erroNormalizado: NormalizedError): NormalizedError {
        return {
            ...erroNormalizado,
            kind: "unexpected",
            message: `${mensagemBase} Isso indica inconsistência interna ou tentativa inválida de acesso.`,
            code: erroNormalizado.code ?? "SUBPROCESSO_NAO_ENCONTRADO_INESPERADO",
        };
    }

    function registrarErroIntegracao(erro: unknown, mensagemBase: string): null {
        const erroNormalizado = normalizeError(erro);
        if (erroNormalizado.code === "REQUEST_CANCELADA") {
            erroIntegracaoContexto.value = erroNormalizado;
            return null;
        }

        logger.error(mensagemBase, erro);
        erroIntegracaoContexto.value = erroNormalizado.kind === "notFound"
            ? criarErroSubprocessoNaoEncontrado(mensagemBase, erroNormalizado)
            : erroNormalizado;
        return null;
    }

    async function executarComDedupe<T>(chave: string, acao: () => Promise<T>): Promise<T> {
        const carregamentoExistente = carregamentos.get(chave);
        if (carregamentoExistente) {
            return carregamentoExistente as Promise<T>;
        }

        const promessa = acao().finally(() => carregamentos.delete(chave));
        carregamentos.set(chave, promessa);
        return promessa;
    }

    async function garantirContextoEdicao(codigoSubprocesso: number, limparAntes = false): Promise<ContextoEdicaoSubprocesso | null> {
        if (limparAntes) {
            limparContextoAtual();
        }

        if (contextoEdicao.value?.detalhes.codigo === codigoSubprocesso) {
            return contextoEdicao.value;
        }

        try {
            const data = await executarComDedupe(
                gerarChave("EDICAO_CODIGO", codigoSubprocesso),
                () => serviceBuscarContextoEdicao(codigoSubprocesso),
            );
            registrarContextoEdicao(data);
            return data;
        } catch (erro) {
            return registrarErroIntegracao(erro, `Falha grave ao localizar o subprocesso ${codigoSubprocesso}.`);
        }
    }

    async function garantirContextoEdicaoPorProcessoEUnidade(
        codProcesso: number,
        siglaUnidade: string,
        limparAntes = false
    ): Promise<{ codigo: number; contexto: ContextoEdicaoSubprocesso } | null> {
        if (limparAntes) {
            limparContextoAtual();
        }

        const chaveProcessoUnidade = gerarChaveProcessoUnidade(codProcesso, siglaUnidade);
        const codigoMapeado = codigosEdicaoPorProcessoUnidade.get(chaveProcessoUnidade);
        if (typeof codigoMapeado === "number") {
            const contexto = await garantirContextoEdicao(codigoMapeado, limparAntes);
            if (contexto) {
                return {codigo: codigoMapeado, contexto};
            }
        }

        try {
            return await executarComDedupe(
                gerarChave("EDICAO_PROCESSO_UNIDADE", chaveProcessoUnidade),
                async () => {
                    const contexto = await serviceBuscarContextoEdicaoPorProcessoEUnidade(codProcesso, siglaUnidade);
                    const codigo = contexto.detalhes.codigo;
                    codigosEdicaoPorProcessoUnidade.set(chaveProcessoUnidade, codigo);
                    registrarContextoEdicao(contexto);
                    return {codigo, contexto};
                },
            );
        } catch (erro) {
            return registrarErroIntegracao(
                erro,
                `Falha grave ao resolver subprocesso do processo ${codProcesso} para a unidade ${siglaUnidade}.`,
            );
        }
    }

    async function garantirContextoCadastroAtividades(
        codigoSubprocesso: number,
        limparAntes = false,
    ): Promise<ContextoCadastroAtividadesSubprocesso | null> {
        if (limparAntes) {
            limparContextoAtual();
        }

        if (contextoCadastro.value?.detalhes.codigo === codigoSubprocesso) {
            return contextoCadastro.value;
        }

        try {
            const data = await executarComDedupe(
                gerarChave("CADASTRO_CODIGO", codigoSubprocesso),
                () => serviceBuscarContextoCadastroAtividades(codigoSubprocesso),
            );
            registrarContextoCadastro(data);
            return data;
        } catch (erro) {
            return registrarErroIntegracao(erro, `Falha grave ao carregar o cadastro do subprocesso ${codigoSubprocesso}.`);
        }
    }

    async function garantirContextoCadastroAtividadesPorProcessoEUnidade(
        codProcesso: number,
        siglaUnidade: string,
        limparAntes = false,
    ): Promise<ContextoCadastroAtividadesSubprocesso | null> {
        if (limparAntes) {
            limparContextoAtual();
        }

        const chaveProcessoUnidade = gerarChaveProcessoUnidade(codProcesso, siglaUnidade);
        const codigoMapeado = codigosCadastroPorProcessoUnidade.get(chaveProcessoUnidade);
        if (typeof codigoMapeado === "number") {
            return garantirContextoCadastroAtividades(codigoMapeado, limparAntes);
        }

        try {
            return await executarComDedupe(
                gerarChave("CADASTRO_PROCESSO_UNIDADE", chaveProcessoUnidade),
                async () => {
                    const contexto = await serviceBuscarContextoCadastroAtividadesPorProcessoEUnidade(codProcesso, siglaUnidade);
                    codigosCadastroPorProcessoUnidade.set(chaveProcessoUnidade, contexto.detalhes.codigo);
                    registrarContextoCadastro(contexto);
                    return contexto;
                },
            );
        } catch (erro) {
            return registrarErroIntegracao(
                erro,
                `Falha grave ao resolver o cadastro do processo ${codProcesso} para a unidade ${siglaUnidade}.`,
            );
        }
    }

    function atualizarStatusLocal(status: {
        codigo: number;
        situacao: SituacaoSubprocesso;
        permissoes?: PermissoesSubprocesso;
    }) {
        if (contextoEdicao.value?.detalhes.codigo === status.codigo) {
            contextoEdicao.value.detalhes.situacao = status.situacao;
            if (status.permissoes) {
                contextoEdicao.value.detalhes.permissoes = status.permissoes;
            }
        }

        if (contextoCadastro.value?.detalhes.codigo === status.codigo) {
            contextoCadastro.value.detalhes.situacao = status.situacao;
            if (status.permissoes) {
                contextoCadastro.value.detalhes.permissoes = status.permissoes;
            }
        }
    }

    return {
        contextoEdicao,
        contextoCadastro,
        erroIntegracaoContexto,
        invalidar,
        limparContextoAtual,
        limparErroIntegracao,
        garantirContextoEdicao,
        garantirContextoEdicaoPorProcessoEUnidade,
        garantirContextoCadastroAtividades,
        garantirContextoCadastroAtividadesPorProcessoEUnidade,
        atualizarStatusLocal,
    };
});
