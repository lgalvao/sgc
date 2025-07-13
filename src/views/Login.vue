<template>
  <div class="container mt-4">
    <h2 class="mb-4">Login</h2>
    <form class="col-md-4 col-sm-6 col-12 p-0" @submit.prevent="entrar">
      <div class="mb-3">
        <label for="usuario" class="form-label">Usuário</label>
        <input type="text" class="form-control" id="usuario" placeholder="Digite seu usuário" />
      </div>
      <div class="mb-3">
        <label for="senha" class="form-label">Senha</label>
        <input type="password" class="form-control" id="senha" placeholder="Digite sua senha" />
      </div>
      <button type="submit" class="btn btn-primary w-100">Entrar</button>
    </form>
  </div>
</template>

<script setup>
// Página de login fictícia, sem validação ou backend
import { useRouter } from 'vue-router'
const router = useRouter()
function entrar() {
  router.push('/painel')
}
</script> 