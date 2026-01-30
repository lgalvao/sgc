import { computed, nextTick, ref, type ComputedRef, type Ref } from "vue";
import type { Router } from "vue-router";
import { useSubprocessosStore } from "@/stores/subprocessos";
import { useFeedbackStore } from "@/stores/feedback";
import type { ErroValidacao, SubprocessoDetalhe } from "@/types/tipos";
import { SituacaoSubprocesso } from "@/types/tipos";
import * as subprocessoService from "@/services/subprocessoService";

export interface CadAtividadesValidacao {
    loadingValidacao: Ref<boolean>;
    errosValidacao: Ref<ErroValidacao[]>;
    erroGlobal: Ref<string | null>;
    mapaErros: ComputedRef<Map<number, string>>;
    obterErroParaAtividade: (atividadeCodigo: number) => string | undefined;
    setAtividadeRef: (atividadeCodigo: number, el: any) => void;
    scrollParaPrimeiroErro: () => void;
    disponibilizarCadastro: (codSubprocesso: number | null, subprocesso: SubprocessoDetalhe | null, isRevisao: boolean, mostrarModal: Ref<boolean>) => Promise<void>;
    confirmarDisponibilizacao: (codSubprocesso: number | null, isRevisao: boolean, mostrarModal: Ref<boolean>, router: Router) => Promise<void>;
}

export function useCadAtividadesValidacao(): CadAtividadesValidacao {
    const subprocessosStore = useSubprocessosStore();
    const feedbackStore = useFeedbackStore();

    const loadingValidacao = ref(false);
    const errosValidacao = ref<ErroValidacao[]>([]);
    const erroGlobal = ref<string | null>(null);

    const atividadeRefs = new Map<number, any>();

    const mapaErros = computed(() => {
        const mapa = new Map<number, string>();
        errosValidacao.value.forEach((erro) => {
            if (erro.atividadeCodigo) {
                mapa.set(erro.atividadeCodigo, erro.mensagem);
            }
        });
        return mapa;
    });

    function obterErroParaAtividade(atividadeCodigo: number): string | undefined {
        return mapaErros.value.get(atividadeCodigo);
    }

    function setAtividadeRef(atividadeCodigo: number, el: any) {
        if (el) {
            atividadeRefs.set(atividadeCodigo, el);
        }
    }

    function scrollParaPrimeiroErro() {
        if (errosValidacao.value.length > 0 && errosValidacao.value[0].atividadeCodigo) {
            const primeiraAtividadeComErro = atividadeRefs.get(errosValidacao.value[0].atividadeCodigo);
            if (primeiraAtividadeComErro) {
                primeiraAtividadeComErro.scrollIntoView({
                    behavior: "smooth",
                    block: "center",
                });
            }
        }
    }

    async function disponibilizarCadastro(codSubprocesso: number | null, subprocesso: SubprocessoDetalhe | null, isRevisao: boolean, mostrarModal: Ref<boolean>) {
        const situacaoEsperada = isRevisao
            ? SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
            : SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO;

        if (subprocesso?.situacao !== situacaoEsperada) {
            feedbackStore.show(
                "Ação não permitida",
                `Ação permitida apenas na situação: "${situacaoEsperada}".`,
                "danger",
            );
            return;
        }

        if (codSubprocesso) {
            loadingValidacao.value = true;
            errosValidacao.value = [];
            erroGlobal.value = null;
            try {
                const resultado = await subprocessoService.validarCadastro(codSubprocesso);
                if (resultado.valido) {
                    mostrarModal.value = true;
                } else {
                    errosValidacao.value = resultado.erros;

                    const erroSemAtividade = resultado.erros.find((e) => !e.atividadeCodigo);
                    if (erroSemAtividade) {
                        erroGlobal.value = erroSemAtividade.mensagem;
                    }

                    await nextTick();
                    scrollParaPrimeiroErro();
                }
            } catch {
                feedbackStore.show("Erro na validação", "Não foi possível validar o cadastro.", "danger");
            } finally {
                loadingValidacao.value = false;
            }
        }
    }

    async function confirmarDisponibilizacao(codSubprocesso: number | null, isRevisao: boolean, mostrarModal: Ref<boolean>, router: Router) {
        if (!codSubprocesso) return;

        let sucesso: boolean;
        if (isRevisao) {
            sucesso = await subprocessosStore.disponibilizarRevisaoCadastro(codSubprocesso);
        } else {
            sucesso = await subprocessosStore.disponibilizarCadastro(codSubprocesso);
        }

        mostrarModal.value = false;
        if (sucesso) {
            await router.push("/painel");
        }
    }

    return {
        loadingValidacao,
        errosValidacao,
        erroGlobal,
        mapaErros,
        obterErroParaAtividade,
        setAtividadeRef,
        scrollParaPrimeiroErro,
        disponibilizarCadastro,
        confirmarDisponibilizacao,
    };
}
