<template>
  <div class="container mt-4">
    <h2 class="mb-4">Relatórios</h2>
    <div class="row">
      <div class="col-md-4 mb-4">
        <div class="card h-100" @click="navegarPara('/relatorios/mapas-vigentes')" style="cursor: pointer;">
          <div class="card-body">
            <h5 class="card-title">Mapas Vigentes</h5>
            <p class="card-text">Visualize os mapas de competências atualmente vigentes em todas as unidades.</p>
          </div>
        </div>
      </div>
      <div class="col-md-4 mb-4">
        <div class="card h-100" @click="navegarPara('/relatorios/diagnostico-gaps')" style="cursor: pointer;">
          <div class="card-body">
            <h5 class="card-title">Diagnósticos de Gaps</h5>
            <p class="card-text">Analise os gaps de competências identificados nos processos de diagnóstico.</p>
          </div>
        </div>
      </div>
      <div class="col-md-4 mb-4">
        <div class="card h-100" @click="navegarPara('/relatorios/andamento-geral')" style="cursor: pointer;">
          <div class="card-body">
            <h5 class="card-title">Andamento Geral</h5>
            <p class="card-text">Acompanhe o andamento de todos os processos de mapeamento e revisão.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

function navegarPara(rota) {
  // Por enquanto, apenas navega para uma rota genérica.
  // A implementação específica de cada relatório será feita no futuro.
  // router.push(rota);
  console.log(`Navegando para ${rota}...`);
}
</script>

<style scoped>
.card:hover {
  background-color: #f8f9fa;
  border-color: #0d6efd;
}
</style>
