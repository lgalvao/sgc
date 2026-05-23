import * as cadastroService from "@/services/cadastroService";
import * as subprocessoService from "@/services/subprocessoService";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useToastStore} from "@/stores/toast";
import {useRouter} from "vue-router";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_SUCESSO_ATIVIDADES} from "@/constants/textos-atividades";
import {TEXTOS_SUCESSO_SUBPROCESSO} from "@/constants/textos-subprocesso";
import type {AceitarCadastroRequest, DevolverCadastroRequest, HomologarCadastroRequest,} from "@/types/tipos";
import type {RouteLocationRaw} from "vue-router";

interface WorkflowOptions {
    mensagemSucesso?: string;
    redirecionarParaPainel?: boolean;
    invalidarCaches?: {
        incluirPainel?: boolean;
        incluirMapas?: boolean;
        codigoSubprocessoMapa?: number;
    };
    redirecionarPara?: RouteLocationRaw;
    recarregarContexto?: {
        codigoSubprocesso: number;
        tipo: "cadastro" | "edicao";
    };
}

interface OpcoesMensagemSucesso {
    mensagemSucesso?: string;
}

interface OpcoesHomologacao extends OpcoesMensagemSucesso {
    redirecionarParaPainel?: boolean;
    redirecionarPara?: RouteLocationRaw;
}

function invalidarPainelEMapa(codigoSubprocesso: number) {
    return {
        incluirPainel: true,
        incluirMapas: true,
        codigoSubprocessoMapa: codigoSubprocesso,
    };
}

function workflowPainel(codigoSubprocesso: number, mensagemSucesso: string): WorkflowOptions {
    return {
        mensagemSucesso,
        redirecionarParaPainel: true,
        invalidarCaches: invalidarPainelEMapa(codigoSubprocesso),
    };
}

function workflowRecargaContexto(codigoSubprocesso: number, tipo: "cadastro" | "edicao"): WorkflowOptions {
    return {
        recarregarContexto: {codigoSubprocesso, tipo},
    };
}

function workflowHomologacao(codigoSubprocesso: number, mensagemSucesso: string, options?: OpcoesHomologacao): WorkflowOptions {
    const redirecionarParaPainel = options?.redirecionarParaPainel ?? true;
    return {
        mensagemSucesso,
        redirecionarParaPainel,
        redirecionarPara: options?.redirecionarPara,
        invalidarCaches: redirecionarParaPainel ? invalidarPainelEMapa(codigoSubprocesso) : undefined,
    };
}

async function executarRecargaContexto(
    subprocessoStore: ReturnType<typeof useSubprocessoStore>,
    codigoSubprocesso: number,
    tipo: "cadastro" | "edicao",
) {
    if (tipo === "cadastro") {
        await subprocessoStore.garantirContextoCadastroAtividades(codigoSubprocesso, true);
        return;
    }

    await subprocessoStore.garantirContextoEdicao(codigoSubprocesso, true);
}

async function executarPosAcao(
    options: WorkflowOptions,
    deps: {
        toastStore: ReturnType<typeof useToastStore>;
        subprocessoStore: ReturnType<typeof useSubprocessoStore>;
        router: ReturnType<typeof useRouter>;
        invalidarCachesSubprocesso: ReturnType<typeof useInvalidacaoNavegacao>["invalidarCachesSubprocesso"];
    },
) {
    if (options.mensagemSucesso) {
        deps.toastStore.setPending(options.mensagemSucesso);
    }

    if (options.invalidarCaches) {
        deps.invalidarCachesSubprocesso(options.invalidarCaches);
    }

    if (options.redirecionarParaPainel) {
        deps.subprocessoStore.limparContextoAtual();
        await deps.router.push("/painel");
        return;
    }

    if (options.redirecionarPara) {
        deps.subprocessoStore.limparContextoAtual();
        await deps.router.push(options.redirecionarPara);
        return;
    }

    if (options.recarregarContexto) {
        await executarRecargaContexto(
            deps.subprocessoStore,
            options.recarregarContexto.codigoSubprocesso,
            options.recarregarContexto.tipo,
        );
    }
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
                await executarPosAcao(options, {toastStore, subprocessoStore, router, invalidarCachesSubprocesso});
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
            workflowPainel(codigoSubprocesso, TEXTOS_SUCESSO_ATIVIDADES.CADASTRO_ATIVIDADES_DISPONIBILIZADO)
        );
    }

    async function disponibilizarRevisaoCadastro(codigoSubprocesso: number) {
        return executarAcaoWorkflow(
            () => cadastroService.disponibilizarRevisaoCadastro(codigoSubprocesso),
            workflowPainel(codigoSubprocesso, TEXTOS_SUCESSO_ATIVIDADES.REVISAO_CADASTRO_ATIVIDADES_DISPONIBILIZADA)
        );
    }

    async function iniciarRevisaoCadastro(codigoSubprocesso: number) {
        return executarAcaoWorkflow(
            () => cadastroService.iniciarRevisaoCadastro(codigoSubprocesso),
            workflowRecargaContexto(codigoSubprocesso, "cadastro")
        );
    }

    async function cancelarInicioRevisaoCadastro(codigoSubprocesso: number) {
        return executarAcaoWorkflow(
            () => cadastroService.cancelarInicioRevisaoCadastro(codigoSubprocesso),
            workflowRecargaContexto(codigoSubprocesso, "cadastro")
        );
    }

    async function devolverCadastro(codigoSubprocesso: number, req: DevolverCadastroRequest) {
        return executarAcaoWorkflow(
            () => cadastroService.devolverCadastro(codigoSubprocesso, req),
            workflowPainel(codigoSubprocesso, TEXTOS_SUCESSO_SUBPROCESSO.DEVOLUCAO_REALIZADA)
        );
    }

    async function devolverRevisaoCadastro(codigoSubprocesso: number, req: DevolverCadastroRequest) {
        return executarAcaoWorkflow(
            () => cadastroService.devolverRevisaoCadastro(codigoSubprocesso, req),
            workflowPainel(codigoSubprocesso, TEXTOS_SUCESSO_SUBPROCESSO.DEVOLUCAO_REALIZADA)
        );
    }

    async function aceitarCadastro(codigoSubprocesso: number, req: AceitarCadastroRequest, options?: OpcoesMensagemSucesso) {
        return executarAcaoWorkflow(
            () => cadastroService.aceitarCadastro(codigoSubprocesso, req),
            workflowPainel(codigoSubprocesso, options?.mensagemSucesso || TEXTOS_SUCESSO_SUBPROCESSO.ACEITE_REGISTRADO)
        );
    }

    async function aceitarRevisaoCadastro(codigoSubprocesso: number, req: AceitarCadastroRequest, options?: OpcoesMensagemSucesso) {
        return executarAcaoWorkflow(
            () => cadastroService.aceitarRevisaoCadastro(codigoSubprocesso, req),
            workflowPainel(codigoSubprocesso, options?.mensagemSucesso || TEXTOS_SUCESSO_SUBPROCESSO.ACEITE_REGISTRADO)
        );
    }

    async function homologarCadastro(codigoSubprocesso: number, req: HomologarCadastroRequest, options?: OpcoesHomologacao) {
        return executarAcaoWorkflow(
            () => cadastroService.homologarCadastro(codigoSubprocesso, req),
            workflowHomologacao(
                codigoSubprocesso,
                options?.mensagemSucesso || TEXTOS_SUCESSO_SUBPROCESSO.HOMOLOGACAO_EFETIVADA,
                options,
            )
        );
    }

    async function homologarRevisaoCadastro(codigoSubprocesso: number, req: HomologarCadastroRequest, options?: OpcoesHomologacao) {
        return executarAcaoWorkflow(
            () => cadastroService.homologarRevisaoCadastro(codigoSubprocesso, req),
            workflowHomologacao(
                codigoSubprocesso,
                options?.mensagemSucesso || TEXTOS_SUCESSO_SUBPROCESSO.HOMOLOGACAO_EFETIVADA,
                options,
            )
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
                ...workflowRecargaContexto(codigoSubprocesso, "edicao"),
                mensagemSucesso: TEXTOS.subprocesso.SUCESSO_CADASTRO_REABERTO,
            }
        );
    }

    async function reabrirRevisaoCadastro(codigoSubprocesso: number, justificativa: string) {
        return executarAcaoWorkflow(
            () => subprocessoService.reabrirRevisaoCadastro(codigoSubprocesso, justificativa),
            {
                ...workflowRecargaContexto(codigoSubprocesso, "edicao"),
                mensagemSucesso: TEXTOS.subprocesso.SUCESSO_REVISAO_REABERTA,
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
