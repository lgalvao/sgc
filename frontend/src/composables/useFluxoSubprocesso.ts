import * as cadastroService from "@/services/cadastroService";
import * as subprocessoService from "@/services/subprocessoService";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useToastStore} from "@/stores/toast";
import type {RouteLocationRaw} from "vue-router";
import {useRouter} from "vue-router";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_SUCESSO_ATIVIDADES} from "@/constants/textos-atividades";
import {TEXTOS_SUCESSO_SUBPROCESSO} from "@/constants/textos-subprocesso";
import type {HomologarCadastroRequest} from "@/types/tipos";

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

type AcaoSemPayload = (codigoSubprocesso: number) => Promise<unknown>;
type AcaoComPayload<T> = (codigoSubprocesso: number, payload: T) => Promise<unknown>;

interface DependenciasFluxoSubprocesso {
    executarAcaoWorkflow: (acao: () => Promise<unknown>, options?: WorkflowOptions) => Promise<boolean>;
    executarComTratamentoDeErros: ReturnType<typeof useErrorHandler>["executarComTratamentoDeErros"];
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

function criarAcaoPainel(
    deps: DependenciasFluxoSubprocesso,
    mensagemSucesso: string,
    acao: AcaoSemPayload,
) {
    return (codigoSubprocesso: number) =>
        deps.executarAcaoWorkflow(
            () => acao(codigoSubprocesso),
            workflowPainel(codigoSubprocesso, mensagemSucesso));
}

function criarAcaoRecarga(
    deps: DependenciasFluxoSubprocesso,
    tipo: "cadastro" | "edicao",
    acao: AcaoSemPayload,
) {
    return (codigoSubprocesso: number) =>
        deps.executarAcaoWorkflow(
            () => acao(codigoSubprocesso),
            workflowRecargaContexto(codigoSubprocesso, tipo),
        );
}

function criarAcaoPainelComPayload<T>(
    deps: DependenciasFluxoSubprocesso,
    obterMensagemSucesso: (options?: OpcoesMensagemSucesso) => string,
    acao: AcaoComPayload<T>,
) {
    return (codigoSubprocesso: number, payload: T, options?: OpcoesMensagemSucesso) =>
        deps.executarAcaoWorkflow(
            () => acao(codigoSubprocesso, payload),
            workflowPainel(codigoSubprocesso, obterMensagemSucesso(options)),
        );
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

    const dependenciasFluxo = {
        executarAcaoWorkflow,
        executarComTratamentoDeErros,
    };
    const disponibilizarCadastro = criarAcaoPainel(
        dependenciasFluxo,
        TEXTOS_SUCESSO_ATIVIDADES.CADASTRO_ATIVIDADES_DISPONIBILIZADO,
        cadastroService.disponibilizarCadastro,
    );
    const disponibilizarRevisaoCadastro = criarAcaoPainel(
        dependenciasFluxo,
        TEXTOS_SUCESSO_ATIVIDADES.REVISAO_CADASTRO_ATIVIDADES_DISPONIBILIZADA,
        cadastroService.disponibilizarRevisaoCadastro,
    );
    const iniciarRevisaoCadastro = criarAcaoRecarga(
        dependenciasFluxo,
        "cadastro",
        cadastroService.iniciarRevisaoCadastro,
    );
    const cancelarInicioRevisaoCadastro = criarAcaoRecarga(
        dependenciasFluxo,
        "cadastro",
        cadastroService.cancelarInicioRevisaoCadastro,
    );
    const devolverCadastro = criarAcaoPainelComPayload(
        dependenciasFluxo,
        () => TEXTOS_SUCESSO_SUBPROCESSO.DEVOLUCAO_REALIZADA,
        cadastroService.devolverCadastro,
    );
    const devolverRevisaoCadastro = criarAcaoPainelComPayload(
        dependenciasFluxo,
        () => TEXTOS_SUCESSO_SUBPROCESSO.DEVOLUCAO_REALIZADA,
        cadastroService.devolverRevisaoCadastro,
    );
    const aceitarCadastro = criarAcaoPainelComPayload(
        dependenciasFluxo,
        (options) => options?.mensagemSucesso || TEXTOS_SUCESSO_SUBPROCESSO.ACEITE_REGISTRADO,
        cadastroService.aceitarCadastro,
    );
    const aceitarRevisaoCadastro = criarAcaoPainelComPayload(
        dependenciasFluxo,
        (options) => options?.mensagemSucesso || TEXTOS_SUCESSO_SUBPROCESSO.ACEITE_REGISTRADO,
        cadastroService.aceitarRevisaoCadastro,
    );

    async function validarCadastro(codigoSubprocesso: number) {
        return executarComTratamentoDeErros(async () => subprocessoService.validarCadastro(codigoSubprocesso));
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
