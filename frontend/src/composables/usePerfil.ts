import {computed} from "vue";
import {usePerfilStore} from "@/stores/perfil";
import {Perfil} from "@/types/tipos";

export function usePerfil() {
    const perfilStore = usePerfilStore();

    const perfilSelecionado = computed(() => perfilStore.perfilSelecionado);

    const unidadeSelecionada = computed(() => perfilStore.unidadeSelecionadaSigla);
    const isAdmin = computed(() => perfilStore.perfilSelecionado === Perfil.ADMIN);
    const podeVerRelatorios = computed(() =>
        perfilStore.perfilSelecionado === Perfil.ADMIN ||
        perfilStore.perfilSelecionado === Perfil.GESTOR
    );
    const mostrarCriarProcesso = computed(() => perfilStore.permissoesSessao?.mostrarCriarProcesso === true);
    const mostrarArvoreCompletaUnidades = computed(() => perfilStore.permissoesSessao?.mostrarArvoreCompletaUnidades === true);
    const mostrarCtaPainelVazio = computed(() => perfilStore.permissoesSessao?.mostrarCtaPainelVazio === true);
    const mostrarDiagnosticoOrganizacional = computed(() => perfilStore.permissoesSessao?.mostrarDiagnosticoOrganizacional === true);
    const mostrarMenuConfiguracoes = computed(() => perfilStore.permissoesSessao?.mostrarMenuConfiguracoes === true);
    const mostrarMenuAdministradores = computed(() => perfilStore.permissoesSessao?.mostrarMenuAdministradores === true);
    const mostrarCriarAtribuicaoTemporaria = computed(() => perfilStore.permissoesSessao?.mostrarCriarAtribuicaoTemporaria === true);

    return {
        perfilSelecionado,
        unidadeSelecionada,
        isAdmin,
        podeVerRelatorios,
        mostrarCriarProcesso,
        mostrarArvoreCompletaUnidades,
        mostrarCtaPainelVazio,
        mostrarDiagnosticoOrganizacional,
        mostrarMenuConfiguracoes,
        mostrarMenuAdministradores,
        mostrarCriarAtribuicaoTemporaria,
    };
}
