<template>
    <BModal
        :fade="false"
        :header-bg-variant="perfil === 'ADMIN' ? 'success' : 'primary'"
        :model-value="mostrarModal"
        :title="tituloModal"
        centered
        header-text-variant="white"
        hide-footer
        @hide="$emit('fecharModal')"
    >
        <div data-testid="body-aceite-mapa">
            <p v-if="perfil === 'ADMIN'">
                {{ corpoModal }}
            </p>
            <div v-else class="mb-3">
                <label class="form-label" for="observacao-textarea">
                    Observações <span class="text-muted small">(opcional)</span>
                </label>
                <BFormTextarea
                    id="observacao-textarea"
                    v-model="observacao"
                    data-testid="inp-aceite-mapa-obs"
                    placeholder="Digite suas observações sobre o mapa..."
                    rows="4"
                />
                <div class="form-text">
                    As observações serão registradas junto com a validação do
                    mapa.
                </div>
            </div>
        </div>

        <template #footer>
            <BButton
                data-testid="btn-aceite-mapa-cancelar"
                variant="secondary"
                @click="$emit('fecharModal')"
            >
                <i class="bi bi-x-circle me-1" />
                Cancelar
            </BButton>
            <BButton
                data-testid="btn-aceite-mapa-confirmar"
                variant="success"
                @click="$emit('confirmarAceitacao', observacao)"
            >
                <i class="bi bi-check-circle me-1" />
                Aceitar
            </BButton>
        </template>
    </BModal>
</template>

<script lang="ts" setup>
import {BButton, BFormTextarea, BModal} from "bootstrap-vue-next";
import {computed, ref} from "vue";

interface Props {
    mostrarModal: boolean;
    perfil?: string;
}

const props = defineProps<Props>();

defineEmits<{
    fecharModal: [];
    confirmarAceitacao: [observacao: string];
}>();

const observacao = ref("");

const tituloModal = computed(() => {
    return props.perfil === "ADMIN"
        ? "Homologação"
        : "Aceitar Mapa de Competências";
});

const corpoModal = computed(() => {
    return props.perfil === "ADMIN"
        ? "Confirma a homologação do mapa de competências?"
        : "Observações";
});
</script>

<style scoped>
.form-control:focus {
    border-color: var(--bs-success);
    box-shadow: 0 0 0 0.2rem rgba(var(--bs-success-rgb), 0.25);
}
</style>
