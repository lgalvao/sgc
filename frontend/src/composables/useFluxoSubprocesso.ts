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
    };
    redirecionarPara?: import("vue-router").RouteLocationRaw;
}

export function useFluxoSubprocesso() {
    const {lastError, clearError, withErrorHandling} = useErrorHandler();
    const subprocessoStore = useSubprocessoStore();
    const toastStore = useToastStore();
    const router = useRouter();
    const {invalidarCachesSubprocesso} = useInvalidacaoNavegacao();

    async function executarAcaoWorkflow(
        acao: () => Promise<unknown>,
        options: WorkflowOptions = {}
    ): Promise<boolean> {
        try {
            await withErrorHandling(async () => {
                await acao();

                if (options.mensagemSucesso) {
                    toastStore.setPending(options.mensagemSucesso);
                }

                if (options.invalidarCaches) {
                    invalidarCachesSubprocesso(options.invalidarCaches);
                }

                if (options.redirecionarParaPainel) {
                    await router.push("/painel");
                } else if (options.redirecionarPara) {
                    await router.push(options.redirecionarPara);
                }
            });

            return true;
        } catch {
            return false;
        }
    }

    async function validarCadastro(codigoSubprocesso: number) {
        return withErrorHandling(async () => subprocessoService.validarCadastro(codigoSubprocesso));
    }

    async function disponibilizarCadastro(codigoSubprocesso: number) {
        return executarAcaoWorkflow(
            () => cadastroService.disponibilizarCadastro(codigoSubprocesso),
            {
                mensagemSucesso: TEXTOS.sucesso.CADASTRO_ATIVIDADES_DISPONIBILIZADO,
                redirecionarParaPainel: true,
                invalidarCaches: {incluirPainel: true}
            }
        );
    }

    async function disponibilizarRevisaoCadastro(codigoSubprocesso: number) {
        return executarAcaoWorkflow(
            () => cadastroService.disponibilizarRevisaoCadastro(codigoSubprocesso),
            {
                mensagemSucesso: TEXTOS.sucesso.REVISAO_CADASTRO_ATIVIDADES_DISPONIBILIZADA,
                redirecionarParaPainel: true,
                invalidarCaches: {incluirPainel: true}
            }
        );
    }

    async function iniciarRevisaoCadastro(codigoSubprocesso: number) {
        return executarAcaoWorkflow(async () => {
            await cadastroService.iniciarRevisaoCadastro(codigoSubprocesso);
            await subprocessoStore.garantirContextoCadastroAtividades(codigoSubprocesso, true);
        });
    }

    async function cancelarInicioRevisaoCadastro(codigoSubprocesso: number) {
        return executarAcaoWorkflow(async () => {
            await cadastroService.cancelarInicioRevisaoCadastro(codigoSubprocesso);
            await subprocessoStore.garantirContextoCadastroAtividades(codigoSubprocesso, true);
        });
    }

    async function devolverCadastro(codigoSubprocesso: number, req: DevolverCadastroRequest) {
        return executarAcaoWorkflow(
            () => cadastroService.devolverCadastro(codigoSubprocesso, req),
            {
                mensagemSucesso: TEXTOS.sucesso.DEVOLUCAO_REALIZADA,
                redirecionarParaPainel: true,
                invalidarCaches: {incluirPainel: true}
            }
        );
    }

    async function devolverRevisaoCadastro(codigoSubprocesso: number, req: DevolverCadastroRequest) {
        return executarAcaoWorkflow(
            () => cadastroService.devolverRevisaoCadastro(codigoSubprocesso, req),
            {
                mensagemSucesso: TEXTOS.sucesso.DEVOLUCAO_REALIZADA,
                redirecionarParaPainel: true,
                invalidarCaches: {incluirPainel: true}
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
                invalidarCaches: {incluirPainel: true}
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
                invalidarCaches: {incluirPainel: true}
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
                invalidarCaches: redirecionarParaPainel ? {incluirPainel: true} : {}
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
                invalidarCaches: redirecionarParaPainel ? {incluirPainel: true} : {}
            }
        );
    }

    async function alterarDataLimiteSubprocesso(codigoSubprocesso: number, dados: {novaData: string}) {
        return withErrorHandling(async () => {
            await subprocessoService.alterarDataLimiteSubprocesso(codigoSubprocesso, dados);
        });
    }

    async function reabrirCadastro(codigoSubprocesso: number, justificativa: string) {
        return executarAcaoWorkflow(
            () => subprocessoService.reabrirCadastro(codigoSubprocesso, justificativa),
            {
                mensagemSucesso: TEXTOS.subprocesso.SUCESSO_CADASTRO_REABERTO,
            }
        );
    }

    async function reabrirRevisaoCadastro(codigoSubprocesso: number, justificativa: string) {
        return executarAcaoWorkflow(
            () => subprocessoService.reabrirRevisaoCadastro(codigoSubprocesso, justificativa),
            {
                mensagemSucesso: TEXTOS.subprocesso.SUCESSO_REVISAO_REABERTA,
            }
        );
    }

    return {
        lastError,
        clearError,
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
