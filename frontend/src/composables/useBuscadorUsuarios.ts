import {computed, onBeforeUnmount, ref, watch, type Ref} from 'vue';
import {pesquisarUsuarios} from '@/services/usuarioService';
import type {UsuarioPesquisa} from '@/types/tipos';
import {logger} from '@/utils';
import {useNotification} from '@/composables/useNotification';

export function useBuscadorUsuarios(
    termo: Ref<string>,
    selecionado: Ref<string | null>
) {
    const {notify} = useNotification();
    const usuariosEncontrados = ref<UsuarioPesquisa[]>([]);
    const pesquisandoUsuarios = ref(false);
    const mostrarResultadosUsuarios = ref(false);
    const indiceUsuarioDestacado = ref(-1);

    let timeoutPesquisa: ReturnType<typeof setTimeout> | null = null;
    let timeoutOcultar: ReturnType<typeof setTimeout> | null = null;

    const termoPesquisaMinimaAtingida = computed(() => termo.value.trim().length >= 2);

    watch(usuariosEncontrados, (usuarios) => {
        indiceUsuarioDestacado.value = usuarios.length > 0 ? 0 : -1;
    });

    onBeforeUnmount(() => {
        if (timeoutPesquisa) clearTimeout(timeoutPesquisa);
        if (timeoutOcultar) clearTimeout(timeoutOcultar);
    });

    const atualizarUsuarioSelecionadoPorNome = (nome: string) => {
        const u = usuariosEncontrados.value.find((item) => item.nome === nome.trim());
        selecionado.value = u?.tituloEleitoral ?? null;
    };

    const limparResultados = () => {
        usuariosEncontrados.value = [];
        pesquisandoUsuarios.value = false;
        mostrarResultadosUsuarios.value = false;
        indiceUsuarioDestacado.value = -1;
    };

    async function executarPesquisa() {
        pesquisandoUsuarios.value = true;
        try {
            usuariosEncontrados.value = await pesquisarUsuarios(termo.value.trim());
            atualizarUsuarioSelecionadoPorNome(termo.value);
        } catch (error) {
            limparResultados();
            logger.error("Erro ao pesquisar usuários:", error);
            notify("Erro ao pesquisar usuários", 'danger');
        } finally {
            pesquisandoUsuarios.value = false;
        }
    }

    const aoAlterarTermo = (valor: string | number | null) => {
        termo.value = valor == null ? "" : String(valor);
        mostrarResultadosUsuarios.value = termoPesquisaMinimaAtingida.value;
        indiceUsuarioDestacado.value = -1;
        atualizarUsuarioSelecionadoPorNome(termo.value);
        if (timeoutPesquisa) clearTimeout(timeoutPesquisa);

        if (!termoPesquisaMinimaAtingida.value || !termo.value.trim()) {
            limparResultados();
            return;
        }

        timeoutPesquisa = setTimeout(() => executarPesquisa(), 300);
    };

    const selecionarUsuario = (usuario: UsuarioPesquisa) => {
        selecionado.value = usuario.tituloEleitoral;
        termo.value = usuario.nome;
        mostrarResultadosUsuarios.value = false;
        indiceUsuarioDestacado.value = -1;
    };

    const agendarOcultacao = () => {
        if (timeoutOcultar) clearTimeout(timeoutOcultar);
        timeoutOcultar = setTimeout(() => {
            mostrarResultadosUsuarios.value = false;
            indiceUsuarioDestacado.value = -1;
        }, 150);
    };

    const calcularProximoIndice = (deslocamento: 1 | -1) => {
        const ultimo = usuariosEncontrados.value.length - 1;
        if (deslocamento === 1) return indiceUsuarioDestacado.value < ultimo ? indiceUsuarioDestacado.value + 1 : 0;
        return indiceUsuarioDestacado.value > 0 ? indiceUsuarioDestacado.value - 1 : ultimo;
    };

    return {
        usuariosEncontrados, pesquisandoUsuarios, mostrarResultadosUsuarios, indiceUsuarioDestacado, termoPesquisaMinimaAtingida,
        aoAlterarTermo, selecionarUsuario, agendarOcultacao, limparResultados, calcularProximoIndice,
    };
}
