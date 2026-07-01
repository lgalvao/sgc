import {computed, onMounted, ref} from "vue";
import {
    adicionarAdministrador,
    type AdministradorDto,
    listarAdministradores,
    removerAdministrador,
} from "@/services/administradorService";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {TEXTOS} from "@/constants/textos";
import {useAsyncAction} from "@/composables/useAsyncAction";
import type BuscadorUsuarios from "@/components/comum/BuscadorUsuarios.vue";

export function useAdministradoresTela() {
    const {carregando: carregandoAdmins, erro: erroAdmins, executar} = useAsyncAction();
    const acaoAdicionar = useAsyncAction();
    const acaoRemover = useAsyncAction();
    const {validarSubmissao, resetarValidacao, deveExibirErro, focarPrimeiroErroInvalido} =
        useValidacaoFormulario();

    const administradores = ref<AdministradorDto[]>([]);
    const carregandoInicial = ref(true);
    const removendoAdmin = ref<string | null>(null);
    const mostrarModalAdicionarAdmin = ref(false);
    const mostrarModalRemoverAdmin = ref(false);
    const adminParaRemover = ref<AdministradorDto | null>(null);
    const usuarioSelecionado = ref<string | null>(null);
    const termoUsuario = ref("");
    const erroAdicionarAdmin = ref("");
    const erroRemoverAdmin = ref("");
    const inputTituloRef = ref<InstanceType<typeof BuscadorUsuarios> | null>(null);

    const mensagemErroNovoAdmin = computed(() =>
        deveExibirErro(!termoUsuario.value.trim()) ? TEXTOS.administracao.ERRO_TITULO_INVALIDO : "",
    );

    async function carregarAdministradores() {
        await executar(
            async () => {
                administradores.value = await listarAdministradores();
            },
            TEXTOS.comum.ERRO_OPERACAO,
            {relancarErro: false},
        );
    }

    function abrirModalAdicionarAdmin() {
        termoUsuario.value = "";
        usuarioSelecionado.value = null;
        resetarValidacao();
        erroAdicionarAdmin.value = "";
        mostrarModalAdicionarAdmin.value = true;
    }

    function fecharModalAdicionarAdmin() {
        mostrarModalAdicionarAdmin.value = false;
        termoUsuario.value = "";
        usuarioSelecionado.value = null;
        resetarValidacao();
        erroAdicionarAdmin.value = "";
    }

    async function adicionarAdmin() {
        const adminId = usuarioSelecionado.value || termoUsuario.value.trim();
        if (!validarSubmissao(!!adminId)) {
            await focarPrimeiroErroInvalido();
            return;
        }
        erroAdicionarAdmin.value = "";
        await acaoAdicionar.executar(
            () => adicionarAdministrador(adminId),
            TEXTOS.comum.ERRO_OPERACAO,
            {
                relancarErro: false,
                aoOcorrerErro: (erro) => {
                    erroAdicionarAdmin.value = erro.mensagem;
                },
                aoSucesso: async () => {
                    fecharModalAdicionarAdmin();
                    await carregarAdministradores();
                },
            },
        );
    }

    async function confirmarRemocao(admin: AdministradorDto) {
        adminParaRemover.value = admin;
        erroRemoverAdmin.value = "";
        mostrarModalRemoverAdmin.value = true;
    }

    async function removerAdmin() {
        if (!adminParaRemover.value) return;
        erroRemoverAdmin.value = "";
        removendoAdmin.value = adminParaRemover.value.tituloEleitoral;
        try {
            await acaoRemover.executar(
                () => removerAdministrador(adminParaRemover.value!.tituloEleitoral),
                TEXTOS.comum.ERRO_OPERACAO,
                {
                    relancarErro: false,
                    aoOcorrerErro: (erro) => {
                        erroRemoverAdmin.value = erro.mensagem;
                    },
                    aoSucesso: async () => {
                        await carregarAdministradores();
                        mostrarModalRemoverAdmin.value = false;
                        adminParaRemover.value = null;
                    },
                },
            );
        } finally {
            removendoAdmin.value = null;
        }
    }

    onMounted(async () => {
        await carregarAdministradores();
        carregandoInicial.value = false;
    });

    return {
        administradores,
        carregandoInicial,
        carregandoAdmins,
        erroAdmins,
        removendoAdmin,
        mostrarModalAdicionarAdmin,
        mostrarModalRemoverAdmin,
        adminParaRemover,
        usuarioSelecionado,
        termoUsuario,
        erroAdicionarAdmin,
        erroRemoverAdmin,
        adicionandoAdmin: acaoAdicionar.carregando,
        inputTituloRef,
        mensagemErroNovoAdmin,
        abrirModalAdicionarAdmin,
        fecharModalAdicionarAdmin,
        adicionarAdmin,
        confirmarRemocao,
        removerAdmin,
    };
}
