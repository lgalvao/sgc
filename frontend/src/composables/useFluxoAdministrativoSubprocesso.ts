import * as subprocessoService from "@/services/subprocessoService";
import {TEXTOS} from "@/constants/textos";
import type {ExecucaoFluxoSubprocesso} from "@/composables/useFluxoSubprocessoExecucao";

function workflowRecargaEdicao(codigoSubprocesso: number, mensagemSucesso: string) {
    return {
        mensagemSucesso,
        recarregarContexto: {
            codigoSubprocesso,
            tipo: "edicao" as const,
        },
    };
}

export function useFluxoAdministrativoSubprocesso(
    execucao: Pick<ExecucaoFluxoSubprocesso, "executarAcaoWorkflow" | "executarComTratamentoDeErros">,
) {
    const {executarAcaoWorkflow, executarComTratamentoDeErros} = execucao;

    async function validarCadastro(codigoSubprocesso: number) {
        return executarComTratamentoDeErros(async () => subprocessoService.validarCadastro(codigoSubprocesso));
    }

    async function alterarDataLimiteSubprocesso(codigoSubprocesso: number, dados: { novaData: string }) {
        return executarComTratamentoDeErros(async () => {
            await subprocessoService.alterarDataLimiteSubprocesso(codigoSubprocesso, dados);
        });
    }

    const reabrirCadastro = (codigoSubprocesso: number, justificativa: string) =>
        executarAcaoWorkflow(
            () => subprocessoService.reabrirCadastro(codigoSubprocesso, justificativa),
            workflowRecargaEdicao(codigoSubprocesso, TEXTOS.subprocesso.SUCESSO_CADASTRO_REABERTO),
        );

    const reabrirRevisaoCadastro = (codigoSubprocesso: number, justificativa: string) =>
        executarAcaoWorkflow(
            () => subprocessoService.reabrirRevisaoCadastro(codigoSubprocesso, justificativa),
            workflowRecargaEdicao(codigoSubprocesso, TEXTOS.subprocesso.SUCESSO_REVISAO_REABERTA),
        );

    return {
        alterarDataLimiteSubprocesso,
        reabrirCadastro,
        reabrirRevisaoCadastro,
        validarCadastro,
    };
}
