<template>
  <BContainer class="d-flex align-items-center justify-content-center min-vh-100">
    <BRow class="w-100 justify-content-center">
      <BCol lg="5" md="7" sm="12">
        <BCard class="shadow-lg p-4 text-center" no-body>
          <div class="p-4">
            <h1 class="h3 mb-3" data-testid="txt-erro-geral-titulo">
              {{ TEXTOS.erroGeral.TITULO }}
            </h1>
            <p class="text-muted mb-4" data-testid="txt-erro-geral-descricao">
              {{ TEXTOS.erroGeral.DESCRICAO }}
            </p>
            <BButton to="/login" variant="primary" data-testid="btn-erro-geral-voltar">
              {{ TEXTOS.erroGeral.ACAO }}
            </BButton>
          </div>
        </BCard>
      </BCol>
    </BRow>
  </BContainer>
</template>

<script setup lang="ts">
import {BButton, BCard, BCol, BContainer, BRow} from "bootstrap-vue-next";
import {TEXTOS} from "@/constants/textos";
</script>
