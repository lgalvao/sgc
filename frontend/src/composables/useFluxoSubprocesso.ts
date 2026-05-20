import * as cadastroService from "@/services/cadastroService";
import * as subprocessoService from "@/services/subprocessoService";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useToastStore} from "@/stores/toast";
import {useRouter} from "vue-router";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import {TEXTOS} from "@/constants/textos";
import type {AceitarCadastroRequest, DevolverCadastroRequest, HomologarCadastroRequest,} from "@/types/tipos";

interface WorkflowOptions {
    mensagemSucesso?: string;
    redirecionarParaPainel?: boolean;
    invalidarCaches?: {
        incluirPainel?: boolean;
        incluirMapas?: boolean;
        codigoSubprocessoMapa?: number;
    };
    redirecionarPara?: import("vue-router").RouteLocationRaw;
    recarregarContexto?: {
        codigoSubprocesso: number;
        tipo: "cadastro" | "edicao";
    };
}

export function useFluxoSubprocesso() {
    const {ultimoErro, limparErro, executarComTratamentoDeErros} = useErrorHandler();
    const subprocessoStore = useSubprocessoStore();
    const toastStore = useToastStore();
    const router = useRouter();
    const {invalidarCachesSubprocesso} = useInvalidacaoNavegacao();

    async function executarAcaoWorkflow(
        acao: () => Promise<unknown>,
        options: WorkflowOptions = {}
    ): Promise<boolean> {
        try {
            await executarComTratamentoDeErros(async () => {
                await acao();

                if (options.mensagemSucesso) {
                    toastStore.setPending(options.mensagemSucesso);
                }

                if (options.invalidarCaches) {
                    invalidarCachesSubprocesso(options.invalidarCaches);
                }

                if (options.redirecionarParaPainel) {
                    subprocessoStore.limparContextoAtual();
                    await router.push("/painel");
                } else if (options.redirecionarPara) {
                    subprocessoStore.limparContextoAtual();
                    await router.push(options.redirecionarPara);
                } else if (options.recarregarContexto) {
                    if (options.recarregarContexto.tipo === "cadastro") {
                        await subprocessoStore.garantirContextoCadastroAtividades(
                            options.recarregarContexto.codigoSubprocesso,
                            true,
                        );
                    } else {
                        await subprocessoStore.garantirContextoEdicao(
                            options.recarregarContexto.codigoSubprocesso,
                            true,
                        );
                    }
                }
            });

            return true;
        } catch {
            return false;
        }
    }

    async function validarCadastro(codigoSubprocesso: number) {
        return executarComTratamentoDeErros(async () => subprocessoService.validarCadastro(codigoSubprocesso));
    }

    async function disponibilizarCadastro(codigoSubprocesso: number) {
        return executarAcaoWorkflow(
            () => cadastroService.disponibilizarCadastro(codigoSubprocesso),
            {
                mensagemSucesso: TEXTOS.sucesso.CADASTRO_ATIVIDADES_DISPONIBILIZADO,
                redirecionarParaPainel: true,
                invalidarCaches: {incluirPainel: true, incluirMapas: true, codigoSubprocessoMapa: codigoSubprocesso}
            }
        );
    }

    async function disponibilizarRevisaoCadastro(codigoSubprocesso: number) {
        return executarAcaoWorkflow(
            () => cadastroService.disponibilizarRevisaoCadastro(codigoSubprocesso),
            {
                mensagemSucesso: TEXTOS.sucesso.REVISAO_CADASTRO_ATIVIDADES_DISPONIBILIZADA,
                redirecionarParaPainel: true,
                invalidarCaches: {incluirPainel: true, incluirMapas: true, codigoSubprocessoMapa: codigoSubprocesso}
            }
        );
    }

    async function iniciarRevisaoCadastro(codigoSubprocesso: number) {
        return executarAcaoWorkflow(
            () => cadastroService.iniciarRevisaoCadastro(codigoSubprocesso),
            {
                recarregarContexto: {codigoSubprocesso, tipo: "cadastro"}
            }
        );
    }

    async function cancelarInicioRevisaoCadastro(codigoSubprocesso: number) {
        return executarAcaoWorkflow(
            () => cadastroService.cancelarInicioRevisaoCadastro(codigoSubprocesso),
            {
                recarregarContexto: {codigoSubprocesso, tipo: "cadastro"}
            }
        );
    }

    async function devolverCadastro(codigoSubprocesso: number, req: DevolverCadastroRequest) {
        return executarAcaoWorkflow(
            () => cadastroService.devolverCadastro(codigoSubprocesso, req),
            {
                mensagemSucesso: TEXTOS.sucesso.DEVOLUCAO_REALIZADA,
                redirecionarParaPainel: true,
                invalidarCaches: {incluirPainel: true, incluirMapas: true, codigoSubprocessoMapa: codigoSubprocesso}
            }
        );
    }

    async function devolverRevisaoCadastro(codigoSubprocesso: number, req: DevolverCadastroRequest) {
        return executarAcaoWorkflow(
            () => cadastroService.devolverRevisaoCadastro(codigoSubprocesso, req),
            {
                mensagemSucesso: TEXTOS.sucesso.DEVOLUCAO_REALIZADA,
                redirecionarParaPainel: true,
                invalidarCaches: {incluirPainel: true, incluirMapas: true, codigoSubprocessoMapa: codigoSubprocesso}
            }
        );
    }

    async function aceitarCadastro(codigoSubprocesso: number, req: AceitarCadastroRequest, options?: {
        mensagemSucesso?: string
    }) {
        return executarAcaoWorkflow(
            () => cadastroService.aceitarCadastro(codigoSubprocesso, req),
            {
                mensagemSucesso: options?.mensagemSucesso || TEXTOS.sucesso.ACEITE_REGISTRADO,
                redirecionarParaPainel: true,
                invalidarCaches: {incluirPainel: true, incluirMapas: true, codigoSubprocessoMapa: codigoSubprocesso}
            }
        );
    }

    async function aceitarRevisaoCadastro(codigoSubprocesso: number, req: AceitarCadastroRequest, options?: {
        mensagemSucesso?: string
    }) {
        return executarAcaoWorkflow(
            () => cadastroService.aceitarRevisaoCadastro(codigoSubprocesso, req),
            {
                mensagemSucesso: options?.mensagemSucesso || TEXTOS.sucesso.ACEITE_REGISTRADO,
                redirecionarParaPainel: true,
                invalidarCaches: {incluirPainel: true, incluirMapas: true, codigoSubprocessoMapa: codigoSubprocesso}
            }
        );
    }

    async function homologarCadastro(codigoSubprocesso: number, req: HomologarCadastroRequest, options?: {
        mensagemSucesso?: string;
        redirecionarParaPainel?: boolean;
        redirecionarPara?: import("vue-router").RouteLocationRaw
    }) {
        const redirecionarParaPainel = options?.redirecionarParaPainel ?? true;
        return executarAcaoWorkflow(
            () => cadastroService.homologarCadastro(codigoSubprocesso, req),
            {
                mensagemSucesso: options?.mensagemSucesso || TEXTOS.sucesso.HOMOLOGACAO_EFETIVADA,
                redirecionarParaPainel,
                redirecionarPara: options?.redirecionarPara,
                invalidarCaches: redirecionarParaPainel
                    ? {incluirPainel: true, incluirMapas: true, codigoSubprocessoMapa: codigoSubprocesso}
                    : undefined
            }
        );
    }

    async function homologarRevisaoCadastro(codigoSubprocesso: number, req: HomologarCadastroRequest, options?: {
        mensagemSucesso?: string;
        redirecionarParaPainel?: boolean;
        redirecionarPara?: import("vue-router").RouteLocationRaw
    }) {
        const redirecionarParaPainel = options?.redirecionarParaPainel ?? true;
        return executarAcaoWorkflow(
            () => cadastroService.homologarRevisaoCadastro(codigoSubprocesso, req),
            {
                mensagemSucesso: options?.mensagemSucesso || TEXTOS.sucesso.HOMOLOGACAO_EFETIVADA,
                redirecionarParaPainel,
                redirecionarPara: options?.redirecionarPara,
                invalidarCaches: redirecionarParaPainel
                    ? {incluirPainel: true, incluirMapas: true, codigoSubprocessoMapa: codigoSubprocesso}
                    : undefined
            }
        );
    }

    async function alterarDataLimiteSubprocesso(codigoSubprocesso: number, dados: { novaData: string }) {
        return executarComTratamentoDeErros(async () => {
            await subprocessoService.alterarDataLimiteSubprocesso(codigoSubprocesso, dados);
        });
    }

    async function reabrirCadastro(codigoSubprocesso: number, justificativa: string) {
        return executarAcaoWorkflow(
            () => subprocessoService.reabrirCadastro(codigoSubprocesso, justificativa),
            {
                mensagemSucesso: TEXTOS.subprocesso.SUCESSO_CADASTRO_REABERTO,
                recarregarContexto: {codigoSubprocesso, tipo: "edicao"}
            }
        );
    }

    async function reabrirRevisaoCadastro(codigoSubprocesso: number, justificativa: string) {
        return executarAcaoWorkflow(
            () => subprocessoService.reabrirRevisaoCadastro(codigoSubprocesso, justificativa),
            {
                mensagemSucesso: TEXTOS.subprocesso.SUCESSO_REVISAO_REABERTA,
                recarregarContexto: {codigoSubprocesso, tipo: "edicao"}
            }
        );
    }

    return {
        ultimoErro,
        limparErro,
        validarCadastro,
        disponibilizarCadastro,
        disponibilizarRevisaoCadastro,
        iniciarRevisaoCadastro,
        cancelarInicioRevisaoCadastro,
        devolverCadastro,
        devolverRevisaoCadastro,
        aceitarCadastro,
        aceitarRevisaoCadastro,
        homologarCadastro,
        homologarRevisaoCadastro,
        alterarDataLimiteSubprocesso,
        reabrirCadastro,
        reabrirRevisaoCadastro,
    };
}
