import * as cadastroService from "@/services/cadastroService";
import {TEXTOS_SUCESSO_ATIVIDADES} from "@/constants/textos-atividades";
import {TEXTOS_SUCESSO_SUBPROCESSO} from "@/constants/textos-subprocesso";
import type {HomologarCadastroRequest} from "@/types/tipos";
import type {RouteLocationRaw} from "vue-router";
import type {ExecucaoFluxoSubprocesso} from "@/composables/useFluxoSubprocessoExecucao";

interface OpcoesMensagemSucesso {
    mensagemSucesso?: string;
}

interface OpcoesHomologacao extends OpcoesMensagemSucesso {
    redirecionarParaPainel?: boolean;
    redirecionarPara?: RouteLocationRaw;
}

function workflowPainel(codigoSubprocesso: number, mensagemSucesso: string) {
    return {
        atualizarFluxoMapa: codigoSubprocesso,
        mensagemSucesso,
        redirecionarParaPainel: true,
    };
}

function workflowRecarga(codigoSubprocesso: number, tipo: "cadastro" | "edicao") {
    return {
        recarregarContexto: {codigoSubprocesso, tipo} as const,
    };
}

function workflowHomologacao(
    codigoSubprocesso: number,
    mensagemSucesso: string,
    options?: OpcoesHomologacao,
) {
    const redirecionarParaPainel = options?.redirecionarParaPainel ?? true;
    return {
        mensagemSucesso,
        redirecionarPara: options?.redirecionarPara,
        redirecionarParaPainel,
        atualizarFluxoMapa: redirecionarParaPainel ? codigoSubprocesso : undefined,
    };
}

export function useFluxoCadastroSubprocesso(execucao: Pick<ExecucaoFluxoSubprocesso, "executarAcaoWorkflow">) {
    const {executarAcaoWorkflow} = execucao;

    const disponibilizarCadastro = (codigoSubprocesso: number) =>
        executarAcaoWorkflow(
            () => cadastroService.disponibilizarCadastro(codigoSubprocesso),
            workflowPainel(codigoSubprocesso, TEXTOS_SUCESSO_ATIVIDADES.CADASTRO_ATIVIDADES_DISPONIBILIZADO),
        );

    const disponibilizarRevisaoCadastro = (codigoSubprocesso: number) =>
        executarAcaoWorkflow(
            () => cadastroService.disponibilizarRevisaoCadastro(codigoSubprocesso),
            workflowPainel(codigoSubprocesso, TEXTOS_SUCESSO_ATIVIDADES.REVISAO_CADASTRO_ATIVIDADES_DISPONIBILIZADA),
        );

    const iniciarRevisaoCadastro = (codigoSubprocesso: number) =>
        executarAcaoWorkflow(
            () => cadastroService.iniciarRevisaoCadastro(codigoSubprocesso),
            workflowRecarga(codigoSubprocesso, "cadastro"),
        );

    const cancelarInicioRevisaoCadastro = (codigoSubprocesso: number) =>
        executarAcaoWorkflow(
            () => cadastroService.cancelarInicioRevisaoCadastro(codigoSubprocesso),
            workflowRecarga(codigoSubprocesso, "cadastro"),
        );

    const devolverCadastro = (codigoSubprocesso: number, payload: Parameters<typeof cadastroService.devolverCadastro>[1]) =>
        executarAcaoWorkflow(
            () => cadastroService.devolverCadastro(codigoSubprocesso, payload),
            workflowPainel(codigoSubprocesso, TEXTOS_SUCESSO_SUBPROCESSO.DEVOLUCAO_REALIZADA),
        );

    const devolverRevisaoCadastro = (
        codigoSubprocesso: number,
        payload: Parameters<typeof cadastroService.devolverRevisaoCadastro>[1],
    ) =>
        executarAcaoWorkflow(
            () => cadastroService.devolverRevisaoCadastro(codigoSubprocesso, payload),
            workflowPainel(codigoSubprocesso, TEXTOS_SUCESSO_SUBPROCESSO.DEVOLUCAO_REALIZADA),
        );

    const aceitarCadastro = (
        codigoSubprocesso: number,
        payload: Parameters<typeof cadastroService.aceitarCadastro>[1],
        options?: OpcoesMensagemSucesso,
    ) =>
        executarAcaoWorkflow(
            () => cadastroService.aceitarCadastro(codigoSubprocesso, payload),
            workflowPainel(codigoSubprocesso, options?.mensagemSucesso || TEXTOS_SUCESSO_SUBPROCESSO.ACEITE_REGISTRADO),
        );

    const aceitarRevisaoCadastro = (
        codigoSubprocesso: number,
        payload: Parameters<typeof cadastroService.aceitarRevisaoCadastro>[1],
        options?: OpcoesMensagemSucesso,
    ) =>
        executarAcaoWorkflow(
            () => cadastroService.aceitarRevisaoCadastro(codigoSubprocesso, payload),
            workflowPainel(codigoSubprocesso, options?.mensagemSucesso || TEXTOS_SUCESSO_SUBPROCESSO.ACEITE_REGISTRADO),
        );

    const homologarCadastro = (
        codigoSubprocesso: number,
        req: HomologarCadastroRequest,
        options?: OpcoesHomologacao,
    ) =>
        executarAcaoWorkflow(
            () => cadastroService.homologarCadastro(codigoSubprocesso, req),
            workflowHomologacao(
                codigoSubprocesso,
                options?.mensagemSucesso || TEXTOS_SUCESSO_SUBPROCESSO.HOMOLOGACAO_EFETIVADA,
                options,
            ),
        );

    const homologarRevisaoCadastro = (
        codigoSubprocesso: number,
        req: HomologarCadastroRequest,
        options?: OpcoesHomologacao,
    ) =>
        executarAcaoWorkflow(
            () => cadastroService.homologarRevisaoCadastro(codigoSubprocesso, req),
            workflowHomologacao(
                codigoSubprocesso,
                options?.mensagemSucesso || TEXTOS_SUCESSO_SUBPROCESSO.HOMOLOGACAO_EFETIVADA,
                options,
            ),
        );

    return {
        aceitarCadastro,
        aceitarRevisaoCadastro,
        cancelarInicioRevisaoCadastro,
        devolverCadastro,
        devolverRevisaoCadastro,
        disponibilizarCadastro,
        disponibilizarRevisaoCadastro,
        homologarCadastro,
        homologarRevisaoCadastro,
        iniciarRevisaoCadastro,
    };
}
