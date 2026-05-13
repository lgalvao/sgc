<template>
    <BModal
        :fade="false"
        :model-value="mostrar"
        centered
        data-testid="mdl-historico-analise"
        size="lg"
        title="Histórico de análise"
        @hide="fechar"
    >
        <div data-testid="modal-historico-body">
            <div v-if="loading" class="text-center py-4">
                <BSpinner label="Carregando dados" variant="primary"/>
            </div>
            <template v-else>
                <div
                    v-if="historico.length === 0"
                    class="text-center py-3 text-muted"
                    data-testid="alert-historico-vazio"
                >
                    <i class="bi bi-info-circle fs-4 d-block mb-2"></i>
                    Nenhuma análise registrada para este mapa.
                </div>
                <div v-else>
                    <BTable
                        :fields="fields"
                        :items="historico"
                        hover
                        responsive
                    >
                        <template #head(dataHora)>
                            <span data-testid="header-historico-dataHora">Data/Hora</span>
                        </template>
                        <template #head(unidadeSigla)>
                            <span data-testid="header-historico-unidade">Unidade</span>
                        </template>
                        <template #head(acao)>
                            <span data-testid="header-historico-acao">Ação</span>
                        </template>
                        <template #head(usuarioNome)>
                            <span data-testid="header-historico-usuario">Usuário</span>
                        </template>
                        <template #head(observacoesResumo)>
                            <span data-testid="header-historico-observacao">Observação</span>
                        </template>
                        <template #head(observacoesAcoes)>
                            <span class="text-end d-block">Visualizar</span>
                        </template>
                        <template #cell(dataHora)="{ item, index }">
                            <span :data-testid="`cell-dataHora-${index}`">
                                {{ formatarDataHoraBR((item as Analise).dataHora) }}
                            </span>
                        </template>
                        <template #cell(unidadeSigla)="{ item, index }">
                            <span :data-testid="`cell-unidade-${index}`" :title="(item as Analise).unidadeNome">
                                {{ (item as Analise).unidadeSigla }}
                            </span>
                        </template>
                        <template #cell(acao)="{ item, index }">
                            <span :data-testid="`cell-resultado-${index}`">
                                {{ (item as Analise).acaoDescricao || formatarAcaoAnalise((item as Analise).acao) }}
                            </span>
                        </template>
                        <template #cell(usuarioNome)="{ item, index }">
                            <span
                                :data-testid="`cell-usuario-${index}`"
                                :title="(item as Analise).usuarioNome || (item as Analise).analistaUsuarioTitulo"
                                class="texto-truncado-usuario"
                            >
                                {{ (item as Analise).usuarioNome || (item as Analise).analistaUsuarioTitulo || "-" }}
                            </span>
                        </template>
                        <template #cell(observacoesResumo)="{ item, index }">
                            <span
                                :data-testid="`cell-observacao-${index}`"
                                :title="extrairTextoPlanoHtml((item as Analise).observacoes)"
                                class="texto-truncado-observacao"
                            >
                                {{ resumirObservacao((item as Analise).observacoes) }}
                            </span>
                        </template>
                        <template #cell(observacoesAcoes)="{ item, index }">
                            <div class="text-end">
                                <BButton
                                    v-if="extrairTextoPlanoHtml((item as Analise).observacoes)"
                                    :data-testid="`btn-ver-observacao-${index}`"
                                    size="sm"
                                    variant="outline-secondary"
                                    @click="abrirObservacao((item as Analise).observacoes)"
                                >
                                    <i aria-hidden="true" class="bi bi-eye"/>
                                </BButton>
                                <span v-else class="text-body-secondary">-</span>
                            </div>
                        </template>
                    </BTable>
                </div>
            </template>
        </div>
        <template #footer>
            <div class="d-flex justify-content-end w-100 gap-3 align-items-center">
                <BButton
                    data-testid="btn-modal-fechar"
                    variant="link"
                    class="text-decoration-none text-secondary fw-medium btn-fechar-link"
                    @click="fechar"
                >
                    Fechar
                </BButton>
            </div>
        </template>
    </BModal>

    <ModalVisualizacaoTextoFormatado
        v-model="mostrarObservacao"
        :conteudo="observacaoSelecionada"
        test-id-conteudo="txt-historico-observacao-html"
        titulo="Observação da análise"
        @fechar="mostrarObservacao = false"
    />
</template>

<script lang="ts" setup>
import {ref} from "vue";
import {BButton, BModal, BSpinner, BTable} from "bootstrap-vue-next";
import ModalVisualizacaoTextoFormatado from "@/components/comum/ModalVisualizacaoTextoFormatado.vue";
import type {Analise} from "@/types/tipos";
import {formatarDataHoraBR} from "@/utils/date";
import {extrairTextoPlanoHtml} from "@/utils/textoFormatado";

defineProps<{
    mostrar: boolean;
    historico: Analise[];
    loading?: boolean;
}>();

const emit = defineEmits(["fechar"]);

const fields = [
    {key: "dataHora", label: "Data/Hora"},
    {key: "unidadeSigla", label: "Unidade"},
    {key: "acao", label: "Ação"},
    {key: "usuarioNome", label: "Usuário"},
    {key: "observacoesResumo", label: "Observação"},
    {key: "observacoesAcoes", label: ""},
];

const mostrarObservacao = ref(false);
const observacaoSelecionada = ref("");

function fechar() {
    emit("fechar");
}

function abrirObservacao(observacoes: string | null | undefined) {
    observacaoSelecionada.value = observacoes || "";
    mostrarObservacao.value = true;
}

function resumirObservacao(observacoes: string | null | undefined): string {
    const texto = extrairTextoPlanoHtml(observacoes);
    if (!texto) {
        return "-";
    }
    if (texto.length <= 80) {
        return texto;
    }
    return `${texto.slice(0, 77)}...`;
}

function formatarAcaoAnalise(acao: string | null | undefined): string {
    switch (acao) {
        case "ACEITE_MAPEAMENTO":
        case "ACEITE_REVISAO":
            return "Aceite";
        case "DEVOLUCAO_MAPEAMENTO":
        case "DEVOLUCAO_REVISAO":
            return "Devolução";
        default:
            if (!acao) {
                return "-";
            }
            return acao
                .toLowerCase()
                .split("_")
                .map((parte) => parte.charAt(0).toUpperCase() + parte.slice(1))
                .join(" ");
    }
}
</script>

<style scoped>
.btn-fechar-link {
    padding: 0.375rem 0.75rem;
    transition: all 0.2s;
    border-radius: 0.375rem;
}

.btn-fechar-link:hover {
    color: var(--bs-emphasis-color) !important;
    background-color: var(--bs-secondary-bg);
}

.texto-truncado-usuario {
    display: inline-block;
    max-width: 16rem;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    vertical-align: bottom;
}

.texto-truncado-observacao {
    display: inline-block;
    max-width: 20rem;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    vertical-align: bottom;
}
</style>
