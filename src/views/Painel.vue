<template>
  <div class="container mt-4">
    <h2>Painel</h2>
    <!-- Removido botão e mensagens de depuração -->
    <p v-if="perfil.value === 'SEDOC'">Visão geral dos processos, unidades e pendências do sistema.</p>
    <p v-else-if="perfil.value === 'GESTOR'">Acompanhe os processos e pendências das suas unidades subordinadas.</p>
    <p v-else-if="perfil.value === 'CHEFE'">Acompanhe o status do cadastro da sua unidade e acesse rapidamente suas principais funções.</p>

    <!-- SEDOC -->
    <div v-if="perfil.value === 'SEDOC'" class="row mt-4">
      <div class="col-md-3 mb-3">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title">Processos</h5>
            <p class="card-text">Total: <span class="badge bg-secondary">3</span><br>Em andamento: <span class="badge bg-warning text-dark">2</span><br>Finalizados: <span class="badge bg-success">1</span></p>
            <router-link to="/processos" class="btn btn-outline-primary btn-sm">Ver Processos</router-link>
          </div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title">Pendências por Unidade</h5>
            <ul class="list-group list-group-flush">
              <li class="list-group-item d-flex justify-content-between align-items-center">COSIS <span class="badge bg-warning text-dark">Aguardando</span></li>
              <li class="list-group-item d-flex justify-content-between align-items-center">SESEL <span class="badge bg-success">Finalizado</span></li>
              <li class="list-group-item d-flex justify-content-between align-items-center">SEDESENV <span class="badge bg-danger">Devolvido</span></li>
            </ul>
          </div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title">Notificações</h5>
            <ul class="list-unstyled mb-0">
              <li><span class="badge bg-info text-dark">Novo</span> Prazo para COSIS: 10/07</li>
              <li><span class="badge bg-danger">!</span> SEDESENV devolvido para ajustes</li>
            </ul>
          </div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card h-100">
          <div class="card-body d-flex flex-column justify-content-between">
            <h5 class="card-title">Ações rápidas</h5>
            <router-link to="/processos" class="btn btn-primary btn-sm mb-2">Criar Processo</router-link>
            <router-link to="/mapas" class="btn btn-outline-primary btn-sm mb-2">Ver Mapas</router-link>
            <router-link to="/diagnostico" class="btn btn-outline-primary btn-sm">Ver Diagnóstico</router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- GESTOR -->
    <div v-else-if="perfil.value === 'GESTOR'" class="row mt-4">
      <div class="col-md-4 mb-3">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title">Processos das Subordinadas</h5>
            <ul class="list-group list-group-flush">
              <li class="list-group-item">Processo 2024/1 <span class="badge bg-warning text-dark">Em andamento</span></li>
              <li class="list-group-item">Processo 2023/2 <span class="badge bg-success">Finalizado</span></li>
            </ul>
            <router-link to="/processos" class="btn btn-outline-primary btn-sm mt-2">Ver Processos</router-link>
          </div>
        </div>
      </div>
      <div class="col-md-4 mb-3">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title">Pendências</h5>
            <ul class="list-group list-group-flush">
              <li class="list-group-item d-flex justify-content-between align-items-center">SESEL <span class="badge bg-warning text-dark">Aguardando validação</span></li>
              <li class="list-group-item d-flex justify-content-between align-items-center">COSIS <span class="badge bg-success">Validado</span></li>
            </ul>
          </div>
        </div>
      </div>
      <div class="col-md-4 mb-3">
        <div class="card h-100">
          <div class="card-body d-flex flex-column justify-content-between">
            <h5 class="card-title">Ações rápidas</h5>
            <router-link to="/processos" class="btn btn-primary btn-sm mb-2">Validar Cadastros</router-link>
            <router-link to="/mapas" class="btn btn-outline-primary btn-sm mb-2">Ver Mapas</router-link>
            <router-link to="/diagnostico" class="btn btn-outline-primary btn-sm">Ver Diagnóstico</router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- CHEFE -->
    <div v-else-if="perfil.value === 'CHEFE'" class="row mt-4">
      <div class="col-md-4 mb-3">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title">Status do Cadastro</h5>
            <p class="card-text">Situação: <span class="badge bg-warning text-dark">Em andamento</span></p>
            <router-link to="/processos/1/unidade/1/atividades" class="btn btn-primary btn-sm">Acessar Cadastro</router-link>
          </div>
        </div>
      </div>
      <div class="col-md-4 mb-3">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title">Mapa de Competências</h5>
            <p class="card-text">Visualize o mapa de competências da sua unidade (atual e anteriores).</p>
            <router-link to="/mapas" class="btn btn-outline-primary btn-sm">Ver Mapa</router-link>
          </div>
        </div>
      </div>
      <div class="col-md-4 mb-3">
        <div class="card h-100">
          <div class="card-body">
            <h5 class="card-title">Notificações</h5>
            <ul class="list-unstyled mb-0">
              <li><span class="badge bg-info text-dark">Novo</span> Prazo para finalizar cadastro: 10/07</li>
              <li><span class="badge bg-danger">!</span> Cadastro devolvido para ajustes</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- Bloco de fallback para valores inesperados de perfil -->
    <div v-else>
      <p>Nenhum painel para o perfil: [{{ perfil.value }}]</p>
    </div>
  </div>
</template>

<script setup>
import { usePerfil } from '../composables/usePerfil'
const perfil = usePerfil()
</script> 