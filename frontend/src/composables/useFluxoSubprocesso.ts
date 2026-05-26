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
    aplicarAtualizacaoNavegacao?: () => void;
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

function workflowPainel(
    codigoSubprocesso: number,
    mensagemSucesso: string,
    atualizarFluxoMapa: (codigoSubprocesso: number) => void,
): WorkflowOptions {
    return {
        mensagemSucesso,
        redirecionarParaPainel: true,
        aplicarAtualizacaoNavegacao: () => atualizarFluxoMapa(codigoSubprocesso),
    };
}

function workflowRecargaContexto(codigoSubprocesso: number, tipo: "cadastro" | "edicao"): WorkflowOptions {
    return {
        recarregarContexto: {codigoSubprocesso, tipo},
    };
}

function workflowHomologacao(
    codigoSubprocesso: number,
    mensagemSucesso: string,
    atualizarFluxoMapa: (codigoSubprocesso: number) => void,
    options?: OpcoesHomologacao,
): WorkflowOptions {
    const redirecionarParaPainel = options?.redirecionarParaPainel ?? true;
    return {
        mensagemSucesso,
        redirecionarParaPainel,
        redirecionarPara: options?.redirecionarPara,
        aplicarAtualizacaoNavegacao: redirecionarParaPainel
            ? () => atualizarFluxoMapa(codigoSubprocesso)
            : undefined,
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

export function useFluxoSubprocesso() {
    const {ultimoErro, limparErro, executarComTratamentoDeErros} = useErrorHandler();
    const subprocessoStore = useSubprocessoStore();
    const toastStore = useToastStore();
    const router = useRouter();
    const {atualizarFluxoMapa} = useInvalidacaoNavegacao();

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

                if (options.aplicarAtualizacaoNavegacao) {
                    options.aplicarAtualizacaoNavegacao();
                }

                if (options.redirecionarParaPainel) {
                    subprocessoStore.limparContextoAtual();
                    await router.push("/painel");
                    return;
                }

                if (options.redirecionarPara) {
                    subprocessoStore.limparContextoAtual();
                    await router.push(options.redirecionarPara);
                    return;
                }

                if (options.recarregarContexto) {
                    await executarRecargaContexto(
                        subprocessoStore,
                        options.recarregarContexto.codigoSubprocesso,
                        options.recarregarContexto.tipo,
                    );
                }
            });

            return true;
        } catch {
            return false;
        }
    }

    const disponibilizarCadastro = (codigoSubprocesso: number) =>
        executarAcaoWorkflow(
            () => cadastroService.disponibilizarCadastro(codigoSubprocesso),
            workflowPainel(codigoSubprocesso, TEXTOS_SUCESSO_ATIVIDADES.CADASTRO_ATIVIDADES_DISPONIBILIZADO, atualizarFluxoMapa),
        );

    const disponibilizarRevisaoCadastro = (codigoSubprocesso: number) =>
        executarAcaoWorkflow(
            () => cadastroService.disponibilizarRevisaoCadastro(codigoSubprocesso),
            workflowPainel(codigoSubprocesso, TEXTOS_SUCESSO_ATIVIDADES.REVISAO_CADASTRO_ATIVIDADES_DISPONIBILIZADA, atualizarFluxoMapa),
        );

    const iniciarRevisaoCadastro = (codigoSubprocesso: number) =>
        executarAcaoWorkflow(
            () => cadastroService.iniciarRevisaoCadastro(codigoSubprocesso),
            workflowRecargaContexto(codigoSubprocesso, "cadastro"),
        );

    const cancelarInicioRevisaoCadastro = (codigoSubprocesso: number) =>
        executarAcaoWorkflow(
            () => cadastroService.cancelarInicioRevisaoCadastro(codigoSubprocesso),
            workflowRecargaContexto(codigoSubprocesso, "cadastro"),
        );

    const devolverCadastro = (codigoSubprocesso: number, payload: Parameters<typeof cadastroService.devolverCadastro>[1]) =>
        executarAcaoWorkflow(
            () => cadastroService.devolverCadastro(codigoSubprocesso, payload),
            workflowPainel(codigoSubprocesso, TEXTOS_SUCESSO_SUBPROCESSO.DEVOLUCAO_REALIZADA, atualizarFluxoMapa),
        );

    const devolverRevisaoCadastro = (codigoSubprocesso: number, payload: Parameters<typeof cadastroService.devolverRevisaoCadastro>[1]) =>
        executarAcaoWorkflow(
            () => cadastroService.devolverRevisaoCadastro(codigoSubprocesso, payload),
            workflowPainel(codigoSubprocesso, TEXTOS_SUCESSO_SUBPROCESSO.DEVOLUCAO_REALIZADA, atualizarFluxoMapa),
        );

    const aceitarCadastro = (
        codigoSubprocesso: number,
        payload: Parameters<typeof cadastroService.aceitarCadastro>[1],
        options?: OpcoesMensagemSucesso,
    ) =>
        executarAcaoWorkflow(
            () => cadastroService.aceitarCadastro(codigoSubprocesso, payload),
            workflowPainel(codigoSubprocesso, options?.mensagemSucesso || TEXTOS_SUCESSO_SUBPROCESSO.ACEITE_REGISTRADO, atualizarFluxoMapa),
        );

    const aceitarRevisaoCadastro = (
        codigoSubprocesso: number,
        payload: Parameters<typeof cadastroService.aceitarRevisaoCadastro>[1],
        options?: OpcoesMensagemSucesso,
    ) =>
        executarAcaoWorkflow(
            () => cadastroService.aceitarRevisaoCadastro(codigoSubprocesso, payload),
            workflowPainel(codigoSubprocesso, options?.mensagemSucesso || TEXTOS_SUCESSO_SUBPROCESSO.ACEITE_REGISTRADO, atualizarFluxoMapa),
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
                atualizarFluxoMapa,
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
                atualizarFluxoMapa,
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
