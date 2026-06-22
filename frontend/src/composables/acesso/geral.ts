import {computed, type ComputedRef} from 'vue';
import type {PermissoesSubprocesso} from '@/types/tipos';

export function usarAcessoGeral(
    permissoes: ComputedRef<PermissoesSubprocesso>,
    ehRevisao: ComputedRef<boolean>
) {
    void ehRevisao;
    const podeVisualizarImpacto = computed(() => permissoes.value.podeVisualizarImpacto);

    const mostrarAlterarDataLimite = computed(() => permissoes.value.podeAlterarDataLimite);
    const mostrarReabrirCadastro = computed(() => permissoes.value.podeReabrirCadastro);
    const mostrarReabrirRevisao = computed(() => permissoes.value.podeReabrirRevisao);
    const mostrarEnviarLembrete = computed(() => permissoes.value.podeEnviarLembrete);

    return {
        podeVisualizarImpacto,
        mostrarAlterarDataLimite,
        mostrarReabrirCadastro,
        mostrarReabrirRevisao,
        mostrarEnviarLembrete,
    };
}
