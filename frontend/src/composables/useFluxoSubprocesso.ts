// @sgc-auditoria ignorar: fachadaPura | Fachada de coordenação deliberada: injeta `execucao` como dependência em useFluxoCadastroSubprocesso e useFluxoAdministrativoSubprocesso, isolando os consumers desse detalhe
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
