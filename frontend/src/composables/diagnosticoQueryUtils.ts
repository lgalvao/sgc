import {usePerfilStore} from '@/stores/perfil';

export const CHAVE_DIAGNOSTICO = 'diagnostico-competencias';

type ContextoSessaoDiagnostico = readonly [string, string, string];

export function criarContextoSessaoDiagnostico(perfilStore: ReturnType<typeof usePerfilStore>): ContextoSessaoDiagnostico {
    return [
        perfilStore.usuarioCodigo ?? 'anon',
        String(perfilStore.perfilSelecionado ?? 'sem-perfil'),
        String(perfilStore.unidadeSelecionada ?? 'sem-unidade'),
    ] as const;
}

export function possuiCodSubprocessoValido(codSubprocesso: number) {
    return codSubprocesso > 0;
}

export function possuiSessaoDiagnostico(perfilStore: ReturnType<typeof usePerfilStore>) {
    return !!perfilStore.usuarioCodigo;
}

export function habilitarQueryDiagnostico(
    perfilStore: ReturnType<typeof usePerfilStore>,
    codSubprocesso: number,
    condicaoExtra = true,
) {
    return possuiSessaoDiagnostico(perfilStore) && possuiCodSubprocessoValido(codSubprocesso) && condicaoExtra;
}
