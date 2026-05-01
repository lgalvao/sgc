import {
    aceitarCadastro as serviceAceitarCadastro,
    aceitarRevisaoCadastro as serviceAceitarRevisaoCadastro,
    cancelarInicioRevisaoCadastro as serviceCancelarInicioRevisaoCadastro,
    devolverCadastro as serviceDevolverCadastro,
    devolverRevisaoCadastro as serviceDevolverRevisaoCadastro,
    disponibilizarCadastro as serviceDisponibilizarCadastro,
    disponibilizarRevisaoCadastro as serviceDisponibilizarRevisaoCadastro,
    homologarCadastro as serviceHomologarCadastro,
    homologarRevisaoCadastro as serviceHomologarRevisaoCadastro,
    iniciarRevisaoCadastro as serviceIniciarRevisaoCadastro,
} from "@/services/cadastroService";
import {
    alterarDataLimiteSubprocesso as serviceAlterarDataLimite,
    reabrirCadastro as serviceReabrirCadastro,
    reabrirRevisaoCadastro as serviceReabrirRevisaoCadastro,
    validarCadastro as serviceValidarCadastro,
} from "@/services/subprocessoService";
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
        return withErrorHandling(async () => serviceValidarCadastro(codigoSubprocesso));
    }

    async function disponibilizarCadastro(codigoSubprocesso: number, isRevisao = false) {
        return executarAcaoWorkflow(
            () => isRevisao ? serviceDisponibilizarRevisaoCadastro(codigoSubprocesso) : serviceDisponibilizarCadastro(codigoSubprocesso),
            {
                mensagemSucesso: isRevisao
                    ? TEXTOS.sucesso.REVISAO_CADASTRO_ATIVIDADES_DISPONIBILIZADA
                    : TEXTOS.sucesso.CADASTRO_ATIVIDADES_DISPONIBILIZADO,
                redirecionarParaPainel: true,
                invalidarCaches: {incluirPainel: true}
            }
        );
    }

    async function iniciarRevisaoCadastro(codigoSubprocesso: number) {
        return executarAcaoWorkflow(async () => {
            await serviceIniciarRevisaoCadastro(codigoSubprocesso);
            await subprocessoStore.garantirContextoCadastroAtividades(codigoSubprocesso, true);
        });
    }

    async function cancelarInicioRevisaoCadastro(codigoSubprocesso: number) {
        return executarAcaoWorkflow(async () => {
            await serviceCancelarInicioRevisaoCadastro(codigoSubprocesso);
            await subprocessoStore.garantirContextoCadastroAtividades(codigoSubprocesso, true);
        });
    }

    async function devolverCadastro(codigoSubprocesso: number, req: DevolverCadastroRequest, isRevisao = false) {
        return executarAcaoWorkflow(
            () => isRevisao ? serviceDevolverRevisaoCadastro(codigoSubprocesso, req) : serviceDevolverCadastro(codigoSubprocesso, req),
            {
                mensagemSucesso: TEXTOS.sucesso.DEVOLUCAO_REALIZADA,
                redirecionarParaPainel: true,
                invalidarCaches: {incluirPainel: true}
            }
        );
    }

    async function aceitarCadastro(codigoSubprocesso: number, req: AceitarCadastroRequest, isRevisao = false, options?: {
        mensagemSucesso?: string
    }) {
        return executarAcaoWorkflow(
            () => isRevisao ? serviceAceitarRevisaoCadastro(codigoSubprocesso, req) : serviceAceitarCadastro(codigoSubprocesso, req),
            {
                mensagemSucesso: options?.mensagemSucesso || TEXTOS.sucesso.ACEITE_REGISTRADO,
                redirecionarParaPainel: true,
                invalidarCaches: {incluirPainel: true}
            }
        );
    }

    async function homologarCadastro(codigoSubprocesso: number, req: HomologarCadastroRequest, isRevisao = false, options?: {
        mensagemSucesso?: string;
        redirecionarParaPainel?: boolean;
        redirecionarPara?: import("vue-router").RouteLocationRaw
    }) {
        const redirecionarParaPainel = options?.redirecionarParaPainel ?? true;
        return executarAcaoWorkflow(
            () => isRevisao ? serviceHomologarRevisaoCadastro(codigoSubprocesso, req) : serviceHomologarCadastro(codigoSubprocesso, req),
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
            await serviceAlterarDataLimite(codigoSubprocesso, dados);
        });
    }

    async function reabrirCadastro(codigoSubprocesso: number, justificativa: string, isRevisao = false) {
        const msg = isRevisao ? TEXTOS.subprocesso.SUCESSO_REVISAO_REABERTA : TEXTOS.subprocesso.SUCESSO_CADASTRO_REABERTO;
        return executarAcaoWorkflow(
            () => isRevisao ? serviceReabrirRevisaoCadastro(codigoSubprocesso, justificativa) : serviceReabrirCadastro(codigoSubprocesso, justificativa),
            {
                mensagemSucesso: msg,
            }
        );
    }

    return {
        lastError,
        clearError,
        validarCadastro,
        disponibilizarCadastro,
        iniciarRevisaoCadastro,
        cancelarInicioRevisaoCadastro,
        devolverCadastro,
        aceitarCadastro,
        homologarCadastro,
        alterarDataLimiteSubprocesso,
        reabrirCadastro,
    };
}
