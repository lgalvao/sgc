import {computed, type ComputedRef} from 'vue';
import type {PermissoesSubprocesso} from '@/types/tipos';
import {TEXTOS} from '@/constants/textos';
import {TEXTOS_SUCESSO_SUBPROCESSO} from '@/constants/textos-subprocesso';
import type {AcaoPrincipalMapa} from './tipos';

export function usarAcessoMapa(
    permissoes: ComputedRef<PermissoesSubprocesso>
) {
    const podeAnalisarMapa = computed(() =>
        permissoes.value.podeDevolverMapa ||
        permissoes.value.podeAceitarMapa ||
        permissoes.value.podeHomologarMapa
    );

    const mostrarApresentarSugestoes = computed(() => permissoes.value.podeApresentarSugestoes);
    const mostrarValidarMapa = computed(() => permissoes.value.podeValidarMapa);
    const mostrarDisponibilizarMapa = computed(() => permissoes.value.podeDisponibilizarMapa);
    const mostrarDevolverMapa = computed(() => permissoes.value.podeDevolverMapa);

    const acaoPrincipalMapa = computed<AcaoPrincipalMapa | null>(() => {
        if (permissoes.value.podeHomologarMapa) {
            return {
                codigo: 'HOMOLOGAR',
                mostrar: true,
                habilitar: permissoes.value.podeHomologarMapa && permissoes.value.habilitarHomologarMapa,
                rotuloBotao: TEXTOS.mapa.LABEL_HOMOLOGAR,
                mensagemSucesso: TEXTOS.mapa.SUCESSO_HOMOLOGACAO,
            };
        }

        if (permissoes.value.podeAceitarMapa) {
            return {
                codigo: 'ACEITAR',
                mostrar: true,
                habilitar: permissoes.value.podeAceitarMapa && permissoes.value.habilitarAceitarMapa,
                rotuloBotao: TEXTOS.mapa.LABEL_REGISTRAR_ACEITE,
                mensagemSucesso: TEXTOS_SUCESSO_SUBPROCESSO.ACEITE_REGISTRADO,
            };
        }

        return null;
    });

    return {
        podeAnalisarMapa,
        mostrarApresentarSugestoes,
        mostrarValidarMapa,
        mostrarDisponibilizarMapa,
        mostrarDevolverMapa,
        acaoPrincipalMapa
    };
}
