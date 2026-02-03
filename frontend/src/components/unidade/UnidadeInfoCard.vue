<template>
  <BCard no-body class="mb-4 shadow-sm">
    <BCardBody>
      <BRow>
        <BCol md="6">
          <div data-testid="unidade-titular-info">
            <h5 class="mb-1">
              Titular: {{ titularDetalhes ? titularDetalhes.nome : 'Não informado' }}
            </h5>
            <div v-if="titularDetalhes" class="d-flex flex-column">
              <span v-if="titularDetalhes.ramal">
                Ramal: <a :href="`tel:${titularDetalhes.ramal}`" :aria-label="`Ligar para ${titularDetalhes.ramal}`">{{ titularDetalhes.ramal }}</a>
              </span>
              <span v-if="titularDetalhes.email">
                E-mail: <a :href="`mailto:${titularDetalhes.email}`" :aria-label="`Enviar e-mail para ${titularDetalhes.email}`">{{ titularDetalhes.email }}</a>
              </span>
            </div>
          </div>
        </BCol>
        <BCol md="6" class="border-start">
          <div data-testid="unidade-responsavel-info">
            <h5 class="mb-1">
              Responsável: {{ unidade.responsavel ? unidade.responsavel.nome : 'Não informado' }}
            </h5>
            <p v-if="unidade.responsavel" class="mb-0 text-muted small">Titular ou substituto legal</p>
          </div>
        </BCol>
      </BRow>
    </BCardBody>
  </BCard>
</template>

<script lang="ts" setup>
import {BCard, BCardBody, BCol, BRow} from "bootstrap-vue-next";
import type {Unidade, Usuario} from "@/types/tipos";

defineProps<{
  unidade: Unidade;
  titularDetalhes: Usuario | null;
}>();
</script>
