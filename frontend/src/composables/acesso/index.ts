import {computed, type Ref, unref} from 'vue';
import {type PermissoesSubprocesso, type SubprocessoDetalhe, TipoProcesso} from '@/types/tipos';
import {criarAcessosPermissao} from '@/composables/acessoPermissoes';
import {PERMISSOES_SUBPROCESSO_VAZIAS} from '@/utils/permissoesSubprocesso';
import {usarAcessoCadastro} from './cadastro';
import {usarAcessoMapa} from './mapa';
import {usarAcessoGeral} from './geral';

export * from './tipos';

/**
 * Composable para acessar permissões calculadas pelo backend.
 * 
 * O backend é a fonte única de verdade para regras de segurança e workflow.
 * Este composable simplifica o acesso a essas regras nos componentes Vue.
 */
export function useAcesso(subprocessoRef: Ref<SubprocessoDetalhe | null> | SubprocessoDetalhe) {
    const obterSubprocesso = () => unref(subprocessoRef);
    const obterPermissoes = () => obterSubprocesso()?.permissoes;
    const ehRevisao = computed(() => obterSubprocesso()?.tipoProcesso === TipoProcesso.REVISAO);
    const permissoes = computed<PermissoesSubprocesso>(() => obterPermissoes() ?? PERMISSOES_SUBPROCESSO_VAZIAS);
    
    // Acessos básicos baseados em flags booleanas simples
    const acessosPermissao = criarAcessosPermissao(permissoes);

    // Acessos fatiados por domínio
    const acessoCadastro = usarAcessoCadastro(permissoes, ehRevisao);
    const acessoMapa = usarAcessoMapa(permissoes);
    const acessoGeral = usarAcessoGeral(permissoes, ehRevisao);

    return {
        ...acessosPermissao,
        ...acessoCadastro,
        ...acessoMapa,
        ...acessoGeral,
    };
}
