import {computed, ref, type Ref} from "vue";
import {useDiagnosticoOrganizacionalAlert} from "@/composables/useDiagnosticoOrganizacionalAlert";
import {useNotification} from "@/composables/useNotification";
import type {VarianteAlerta} from "@/composables/useNotification";
import {usePerfil} from "@/composables/usePerfil";
import {useProcessoForm} from "@/composables/useProcessoForm";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {useProcessoCadastroCarga} from "@/composables/useProcessoCadastroCarga";
import {useProcessoMutacoes} from "@/composables/useProcessoMutacoes";
import {ehErroAxios, extrairErrosGenericos, normalizarErro, deveNotificarGlobalmente} from "@/utils/apiError";
import {logger} from "@/utils";
import {listarUnidadesComEquipePropriaSelecionadas} from "@/views/processoCadastroUnidades";
import type {Processo, Unidade} from "@/types/tipos";

type FormFieldsRef = Record<string, unknown>;
type ModalAcaoBlocoRef = Record<string, unknown>;

interface TelaCadastroRefs {
    formFieldsRef: Ref<FormFieldsRef>;
    modalUnidadesComEquipePropriaRef: Ref<ModalAcaoBlocoRef | null>;
}

export function useProcessoCadastroTela({formFieldsRef, modalUnidadesComEquipePropriaRef}: TelaCadastroRefs) {
    const formulario = useProcessoForm();
    
    // Estado local da tela
    const unidades = ref<Unidade[]>([]);
    const isLoadingUnidades = ref(false);
    const isLoadingData = ref(false);
    const mostrarModalConfirmacao = ref(false);
    const mostrarModalRemocao = ref(false);
    const processoEditando = ref<Processo | null>(null);

    const {notificacao, notify, notifyStructured, clear} = useNotification();
    const {mostrarDiagnosticoOrganizacional} = usePerfil();
    const {focarPrimeiroErroInvalido} = useValidacaoFormulario();

    const formData = computed({
        get: () => ({
            descricao: formulario.descricao.value,
            tipo: formulario.tipo.value,
            unidadesSelecionadas: formulario.unidadesSelecionadas.value,
            dataLimite: formulario.dataLimite.value
        }),
        set: (value) => {
            formulario.descricao.value = value.descricao;
            formulario.tipo.value = value.tipo;
            formulario.unidadesSelecionadas.value = value.unidadesSelecionadas;
            formulario.dataLimite.value = value.dataLimite;
        }
    });

    function tratarErrosApi(error: unknown, titulo: string, mensagemPadrao: string) {
        formulario.limparErros();
        clear();

        const erroNormalizado = normalizarErro(error);
        const usarErroEstruturado = ehErroAxios(error) || !!erroNormalizado.erros?.length;

        if (usarErroEstruturado) {
            formulario.aplicarErroNormalizado(erroNormalizado);
            const errosGenericos = extrairErrosGenericos(erroNormalizado);

            if (!formulario.temErros() || errosGenericos.length > 0) {
                notifyStructured(erroNormalizado.mensagem || mensagemPadrao, errosGenericos, {
                    variante: "danger",
                    stackTrace: erroNormalizado.stackTrace || undefined,
                });
                globalThis.scrollTo(0, 0);
            } else {
                void focarPrimeiroErroInvalido();
            }
        } else {
            notify(mensagemPadrao, "danger");
        }

        if (deveNotificarGlobalmente(erroNormalizado)) {
            logger.error(`${titulo}:`, error);
        }
    }

    // Compondo a Carga
    const {carregamentoInicialConcluido, buscarUnidadesParaProcesso} = useProcessoCadastroCarga({
        formulario,
        formFieldsRef,
        unidades,
        isLoadingUnidades,
        isLoadingData,
        processoEditando,
        notify
    });

    const unidadesComEquipePropriaSelecionadas = computed(() =>
        listarUnidadesComEquipePropriaSelecionadas(unidades.value, formulario.unidadesSelecionadas.value)
    );
    const idsUnidadesComEquipePropriaSelecionadas = computed(() =>
        unidadesComEquipePropriaSelecionadas.value.map((unidade) => unidade.codigo)
    );

    // Compondo as Mutações
    const {
        isSaving,
        isStarting,
        isRemoving,
        salvarProcesso,
        confirmarIniciarProcesso,
        confirmarSelecaoUnidadesComEquipePropria,
        confirmarRemocao
    } = useProcessoMutacoes({
        formulario,
        processoEditando,
        unidadesComEquipePropriaSelecionadas,
        idsUnidadesComEquipePropriaSelecionadas,
        mostrarModalConfirmacao,
        mostrarModalRemocao,
        modalUnidadesComEquipePropriaRef,
        tratarErrosApi
    });

    const anyLoading = computed(() => isSaving.value || isStarting.value || isRemoving.value);
    const salvarDesabilitado = computed(() => formulario.isFormInvalid.value || isLoadingData.value || anyLoading.value);
    const iniciarDesabilitado = computed(() => formulario.isFormInvalid.value || isLoadingData.value || anyLoading.value);

    const diagnostico = useDiagnosticoOrganizacionalAlert(unidades, mostrarDiagnosticoOrganizacional);

    return {
        anyLoading,
        buscarUnidadesParaProcesso,
        carregandoDiagnosticoOrganizacional: diagnostico.carregandoDiagnosticoOrganizacional,
        clear,
        confirmarIniciarProcesso,
        confirmarRemocao,
        confirmarSelecaoUnidadesComEquipePropria,
        dataLimite: formulario.dataLimite,
        descricao: formulario.descricao,
        dispensarAlertaDiagnostico: diagnostico.dispensarAlertaDiagnostico,
        exibirAlertaDiagnostico: diagnostico.exibirAlertaDiagnostico,
        fieldErrors: formulario.fieldErrors,
        formData,
        gruposDiagnostico: diagnostico.gruposDiagnostico,
        iniciarDesabilitado,
        isFormInvalid: formulario.isFormInvalid,
        isLoadingData,
        isLoadingUnidades,
        isRemoving,
        isSaving,
        isStarting,
        mostrarModalConfirmacao,
        mostrarModalRemocao,
        notificacao,
        notify,
        notifyStructured,
        processoEditando,
        resumoDiagnostico: diagnostico.resumoDiagnostico,
        salvarDesabilitado,
        salvarProcesso,
        tipo: formulario.tipo,
        unidades,
        unidadesComEquipePropriaSelecionadas,
        unidadesSelecionadas: formulario.unidadesSelecionadas,
        unidadesSemResponsavel: diagnostico.unidadesSemResponsavel,
    };
}
