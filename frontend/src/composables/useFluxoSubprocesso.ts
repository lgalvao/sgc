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
import {validarCadastro as serviceValidarCadastro} from "@/services/subprocessoService";
import {
    alterarDataLimiteSubprocesso as serviceAlterarDataLimite,
    reabrirCadastro as serviceReabrirCadastro,
    reabrirRevisaoCadastro as serviceReabrirRevisaoCadastro,
} from "@/services/processoService";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {useSubprocessos} from "@/composables/useSubprocessos";
import type {AceitarCadastroRequest, DevolverCadastroRequest, HomologarCadastroRequest,} from "@/types/tipos";

export function useFluxoSubprocesso() {
    const {lastError, clearError, withErrorHandling} = useErrorHandler();
    const subprocessos = useSubprocessos();

    async function executarAcao(
        acao: () => Promise<void>,
        codigoSubprocesso?: number,
        recarregarSubprocesso = false,
    ): Promise<boolean> {
        try {
            await withErrorHandling(async () => {
                await acao();

                if (recarregarSubprocesso && codigoSubprocesso) {
                    await subprocessos.buscarSubprocessoDetalhe(codigoSubprocesso);
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

    async function iniciarRevisaoCadastro(codigoSubprocesso: number) {
        return executarAcao(async () => {
            await serviceIniciarRevisaoCadastro(codigoSubprocesso);
            await subprocessos.buscarSubprocessoDetalhe(codigoSubprocesso, false);
        });
    }

    async function cancelarInicioRevisaoCadastro(codigoSubprocesso: number) {
        return executarAcao(async () => {
            await serviceCancelarInicioRevisaoCadastro(codigoSubprocesso);
            await subprocessos.buscarSubprocessoDetalhe(codigoSubprocesso, false);
        });
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
            await subprocessos.buscarSubprocessoDetalhe(codigoSubprocesso);
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
