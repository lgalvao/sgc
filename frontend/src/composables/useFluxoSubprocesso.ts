import {
    aceitarCadastro as serviceAceitarCadastro,
    aceitarRevisaoCadastro as serviceAceitarRevisaoCadastro,
    devolverCadastro as serviceDevolverCadastro,
    devolverRevisaoCadastro as serviceDevolverRevisaoCadastro,
    disponibilizarCadastro as serviceDisponibilizarCadastro,
    disponibilizarRevisaoCadastro as serviceDisponibilizarRevisaoCadastro,
    homologarCadastro as serviceHomologarCadastro,
    homologarRevisaoCadastro as serviceHomologarRevisaoCadastro,
} from "@/services/cadastroService";
import {validarCadastro as serviceValidarCadastro} from "@/services/subprocessoService";
import {
    alterarDataLimiteSubprocesso as serviceAlterarDataLimite,
    reabrirCadastro as serviceReabrirCadastro,
    reabrirRevisaoCadastro as serviceReabrirRevisaoCadastro,
} from "@/services/processoService";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useProcessos} from "@/composables/useProcessos";
import {useSubprocessosStore} from "@/stores/subprocessos";
import type {
    AceitarCadastroRequest,
    DevolverCadastroRequest,
    HomologarCadastroRequest,
} from "@/types/tipos";

async function recarregarProcessoAtual() {
    const processos = useProcessos();
    if (processos.processoDetalhe.value) {
        await processos.buscarProcessoDetalhe(processos.processoDetalhe.value.codigo);
    }
}

export function useFluxoSubprocesso() {
    const {lastError, clearError, withErrorHandling} = useErrorHandler();
    const subprocessosStore = useSubprocessosStore();

    async function executarAcao(
        acao: () => Promise<void>,
        codigoSubprocesso?: number,
        recarregarSubprocesso = false,
    ): Promise<boolean> {
        try {
            await withErrorHandling(async () => {
                await acao();
                await recarregarProcessoAtual();

                if (recarregarSubprocesso && codigoSubprocesso) {
                    await subprocessosStore.buscarSubprocessoDetalhe(codigoSubprocesso);
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

    async function disponibilizarCadastro(codigoSubprocesso: number) {
        return executarAcao(() => serviceDisponibilizarCadastro(codigoSubprocesso));
    }

    async function disponibilizarRevisaoCadastro(codigoSubprocesso: number) {
        return executarAcao(() => serviceDisponibilizarRevisaoCadastro(codigoSubprocesso));
    }

    async function devolverCadastro(codigoSubprocesso: number, req: DevolverCadastroRequest) {
        return executarAcao(() => serviceDevolverCadastro(codigoSubprocesso, req));
    }

    async function devolverRevisaoCadastro(codigoSubprocesso: number, req: DevolverCadastroRequest) {
        return executarAcao(() => serviceDevolverRevisaoCadastro(codigoSubprocesso, req));
    }

    async function aceitarCadastro(codigoSubprocesso: number, req: AceitarCadastroRequest) {
        return executarAcao(() => serviceAceitarCadastro(codigoSubprocesso, req));
    }

    async function aceitarRevisaoCadastro(codigoSubprocesso: number, req: AceitarCadastroRequest) {
        return executarAcao(() => serviceAceitarRevisaoCadastro(codigoSubprocesso, req));
    }

    async function homologarCadastro(codigoSubprocesso: number, req: HomologarCadastroRequest) {
        return executarAcao(() => serviceHomologarCadastro(codigoSubprocesso, req), codigoSubprocesso, true);
    }

    async function homologarRevisaoCadastro(codigoSubprocesso: number, req: HomologarCadastroRequest) {
        return executarAcao(() => serviceHomologarRevisaoCadastro(codigoSubprocesso, req), codigoSubprocesso, true);
    }

    async function alterarDataLimiteSubprocesso(codigoSubprocesso: number, dados: {novaData: string}) {
        return withErrorHandling(async () => {
            await serviceAlterarDataLimite(codigoSubprocesso, dados);
            await recarregarProcessoAtual();
            await subprocessosStore.buscarSubprocessoDetalhe(codigoSubprocesso);
        });
    }

    async function reabrirCadastro(codigoSubprocesso: number, justificativa: string) {
        return executarAcao(() => serviceReabrirCadastro(codigoSubprocesso, justificativa), codigoSubprocesso, true);
    }

    async function reabrirRevisaoCadastro(codigoSubprocesso: number, justificativa: string) {
        return executarAcao(() => serviceReabrirRevisaoCadastro(codigoSubprocesso, justificativa), codigoSubprocesso, true);
    }

    return {
        lastError,
        clearError,
        validarCadastro,
        disponibilizarCadastro,
        disponibilizarRevisaoCadastro,
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
