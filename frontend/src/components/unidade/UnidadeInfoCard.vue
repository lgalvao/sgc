<template>
  <BCard class="mb-4 shadow-sm" no-body>
    <BCardBody>
      <BRow>
        <BCol md="6">
          <div data-testid="unidade-titular-info">
            <h5 class="mb-1">
              Titular: {{ titularDetalhes ? titularDetalhes.nome : 'Não informado' }}
            </h5>
            <div v-if="titularDetalhes" class="d-flex flex-column">
              <span v-if="titularDetalhes.ramal">
                Ramal: <a
                  :aria-label="`Ligar para ${titularDetalhes.ramal}`"
                  :href="`tel:${titularDetalhes.ramal}`"
              >{{ titularDetalhes.ramal }}</a>
              </span>
              <span v-if="titularDetalhes.email">
                E-mail: <a
                  :aria-label="`Enviar e-mail para ${titularDetalhes.email}`"
                  :href="`mailto:${titularDetalhes.email}`"
              >{{ titularDetalhes.email }}</a>
              </span>
            </div>
          </div>
        </BCol>
        <BCol class="border-start" md="6">
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
