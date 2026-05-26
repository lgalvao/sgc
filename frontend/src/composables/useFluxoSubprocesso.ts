import {useFluxoAdministrativoSubprocesso} from "@/composables/useFluxoAdministrativoSubprocesso";
import {useFluxoCadastroSubprocesso} from "@/composables/useFluxoCadastroSubprocesso";
import {useFluxoSubprocessoExecucao} from "@/composables/useFluxoSubprocessoExecucao";

export function useFluxoSubprocesso() {
    const execucao = useFluxoSubprocessoExecucao();
    const {limparErro, ultimoErro} = execucao;
    const fluxoCadastro = useFluxoCadastroSubprocesso(execucao);
    const fluxoAdministrativo = useFluxoAdministrativoSubprocesso(execucao);

    return {
        ...fluxoCadastro,
        ...fluxoAdministrativo,
        ultimoErro,
        limparErro,
    };
}
