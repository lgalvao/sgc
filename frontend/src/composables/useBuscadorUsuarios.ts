import {computed, onBeforeUnmount, ref, type Ref, watch} from 'vue';
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

    watch(termo, (novoTermo) => {
        if (!novoTermo?.trim()) {
            limparResultados();
        }
    });

    onBeforeUnmount(() => {
        if (timeoutPesquisa) clearTimeout(timeoutPesquisa);
        if (timeoutOcultar) clearTimeout(timeoutOcultar);
    });

    const atualizarUsuarioSelecionadoPorNome = (nome: string) => {
        const u = usuariosEncontrados.value.find((item) => item.nome === nome.trim());
        const novoSelecionado = u?.tituloEleitoral ?? null;
        if (selecionado.value !== novoSelecionado) {
            selecionado.value = novoSelecionado;
        }
    };

    const limparResultados = () => {
        if (usuariosEncontrados.value.length === 0 && !mostrarResultadosUsuarios.value && !pesquisandoUsuarios.value) return;
        usuariosEncontrados.value = [];
        pesquisandoUsuarios.value = false;
        mostrarResultadosUsuarios.value = false;
        indiceUsuarioDestacado.value = -1;
    };

    async function executarPesquisa() {
        pesquisandoUsuarios.value = true;
        try {
            const resultados = await pesquisarUsuarios(termo.value.trim());
            usuariosEncontrados.value = resultados;
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
        const novoTermo = valor == null ? "" : String(valor);

        termo.value = novoTermo;
        mostrarResultadosUsuarios.value = termoPesquisaMinimaAtingida.value;
        indiceUsuarioDestacado.value = -1;
        atualizarUsuarioSelecionadoPorNome(novoTermo);

        if (timeoutPesquisa) clearTimeout(timeoutPesquisa);

        if (!termoPesquisaMinimaAtingida.value || !novoTermo.trim()) {
            limparResultados();
            return;
        }

        timeoutPesquisa = setTimeout(() => executarPesquisa(), 300);
    };

    const selecionarUsuario = (usuario: UsuarioPesquisa) => {
        if (selecionado.value !== usuario.tituloEleitoral) {
            selecionado.value = usuario.tituloEleitoral;
        }
        if (termo.value !== usuario.nome) {
            termo.value = usuario.nome;
        }
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
        usuariosEncontrados,
        pesquisandoUsuarios,
        mostrarResultadosUsuarios,
        indiceUsuarioDestacado,
        termoPesquisaMinimaAtingida,
        aoAlterarTermo,
        selecionarUsuario,
        agendarOcultacao,
        limparResultados,
        calcularProximoIndice,
    };
}
