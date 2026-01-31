<template>
  <div data-testid="header-subprocesso">
    <PageHeader
        :title="unidadeSigla"
        :subtitle="unidadeNome"
        title-test-id="subprocesso-header__txt-header-unidade"
    >
      <template #actions>
        <!-- Botão para alterar data limite -->
        <BButton
            v-if="podeAlterarDataLimite"
            data-testid="btn-alterar-data-limite"
            variant="outline-primary"
            @click="handleAlterarDataLimite"
        >
          <i aria-hidden="true" class="bi bi-calendar me-1"/>
          Alterar data limite
        </BButton>

        <!-- CDU-32/33: Botão para reabrir cadastro/revisão -->
        <BButton
            v-if="podeReabrirCadastro"
            data-testid="btn-reabrir-cadastro"
            variant="outline-warning"
            @click="handleReabrirCadastro"
        >
          <i aria-hidden="true" class="bi bi-arrow-counterclockwise me-1"/>
          Reabrir cadastro
        </BButton>

        <BButton
            v-if="podeReabrirRevisao"
            data-testid="btn-reabrir-revisao"
            variant="outline-warning"
            @click="handleReabrirRevisao"
        >
          <i aria-hidden="true" class="bi bi-arrow-counterclockwise me-1"/>
          Reabrir Revisão
        </BButton>

        <!-- CDU-34: Botão para enviar lembrete -->
        <BButton
            v-if="podeEnviarLembrete"
            data-testid="btn-enviar-lembrete"
            variant="outline-info"
            @click="handleEnviarLembrete"
        >
          <i aria-hidden="true" class="bi bi-bell me-1"/>
          Enviar lembrete
        </BButton>
      </template>
    </PageHeader>

    <BCard class="mb-4" no-body data-testid="header-subprocesso-details">
      <BCardBody>
        <p
            class="text-muted small mb-3"
            data-testid="txt-header-processo"
        >
          Processo: {{ processoDescricao }}
        </p>

        <p>
          <span class="fw-bold me-1">Situação:</span>
          <span
              data-testid="subprocesso-header__txt-situacao"
          >{{ situacao }}</span>
        </p>

        <p><strong>Titular:</strong> {{ titularNome }}</p>
        <p class="ms-3">
          <i aria-hidden="true" class="bi bi-telephone-fill me-2"/>{{ titularRamal }}
          <i aria-hidden="true" class="bi bi-envelope-fill ms-3 me-2"/>{{ titularEmail }}
        </p>

        <template v-if="responsavelNome && responsavelNome !== titularNome">
          <p><strong>Responsável:</strong> {{ responsavelNome }}</p>
          <p class="ms-3">
            <i aria-hidden="true" class="bi bi-telephone-fill me-2"/>{{ responsavelRamal }}
            <i aria-hidden="true" class="bi bi-envelope-fill ms-3 me-2"/>{{ responsavelEmail }}
          </p>
        </template>
      </BCardBody>
    </BCard>
  </div>
</template>

<script lang="ts" setup>
import {BButton, BCard, BCardBody} from "bootstrap-vue-next";
import PageHeader from "@/components/layout/PageHeader.vue";

interface Props {
  processoDescricao: string;
  unidadeSigla: string;
  unidadeNome: string;
  situacao: string;
  titularNome: string;
  titularRamal: string;
  titularEmail: string;
  responsavelNome?: string;
  responsavelRamal?: string;
  responsavelEmail?: string;
  podeAlterarDataLimite: boolean;
  podeReabrirCadastro?: boolean;
  podeReabrirRevisao?: boolean;
  podeEnviarLembrete?: boolean;
}

defineProps<Props>();

const emit = defineEmits({
  alterarDataLimite: null,
  reabrirCadastro: null,
  reabrirRevisao: null,
  enviarLembrete: null,
});

const handleAlterarDataLimite = () => {
  emit("alterarDataLimite");
};

const handleReabrirCadastro = () => {
  emit("reabrirCadastro");
};

const handleReabrirRevisao = () => {
  emit("reabrirRevisao");
};

const handleEnviarLembrete = () => {
  emit("enviarLembrete");
};
</script>
