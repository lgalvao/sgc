<template>
  <div class="container mt-4">
    <h2 class="display-6 mb-4">Relatórios</h2>
    <div class="row">
      <div class="col-md-4 mb-4">
        <div class="card h-100" style="cursor: pointer;" @click="navegarPara('/relatorios/mapas-vigentes')">
          <div class="card-body">
            <h5 class="card-title">Mapas Vigentes</h5>
            <p class="card-text">Visualize os mapas de competências atualmente vigentes em todas as unidades.</p>
          </div>
        </div>
      </div>
      <div class="col-md-4 mb-4">
        <div class="card h-100" style="cursor: pointer;" @click="navegarPara('/relatorios/diagnostico-gaps')">
          <div class="card-body">
            <h5 class="card-title">Diagnósticos de Gaps</h5>
            <p class="card-text">Analise os gaps de competências identificados nos processos de diagnóstico.</p>
          </div>
        </div>
      </div>
      <div class="col-md-4 mb-4">
        <div class="card h-100" style="cursor: pointer;" @click="navegarPara('/relatorios/andamento-geral')">
          <div class="card-body">
            <h5 class="card-title">Andamento Geral</h5>
            <p class="card-text">Acompanhe o andamento de todos os processos de mapeamento e revisão.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>

function navegarPara(rota: string) {
  console.log(`Navegando para ${rota}...`);
}
</script>

<style scoped>
.card:hover {
  background-color: #f8f9fa;
  border-color: #0d6efd;
}
</style>
