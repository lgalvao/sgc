<template>
  <div class="login-bg">
    <div class="login-center-wrapper">
      <div class="card login-card p-4 shadow-lg">
        <h2 class="mb-2 text-center">SGC</h2>
        <h5 class="mb-4 text-center text-muted">Sistema de Gestão de Competências</h5>
        <form class="p-0" @submit.prevent="entrar">
          <div class="mb-3">
            <label class="form-label" for="usuario">Título</label>
            <input id="usuario" v-model="usuario" autocomplete="username" class="form-control" type="text"
              placeholder="Digite seu título" />
          </div>
          <div class="mb-3">
            <label class="form-label" for="senha">Senha</label>
            <input id="senha" v-model="senha" autocomplete="current-password" class="form-control" type="password"
              placeholder="Digite sua senha" />
          </div>
          <button class="btn btn-primary w-100 login-btn" type="submit">Entrar</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const usuario = ref('')
const senha = ref('')

function entrar() {
  router.push('/painel')
}
</script>

<style scoped>
.login-bg {
  min-height: 100vh;
  width: 100vw;
  background: linear-gradient(120deg, #e3f0ff 0%, #c9e7ff 50%, #f6faff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-center-wrapper {
  width: 100vw;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 400px;
  border-radius: 22px;
  border: none;
  background: #fff;
  box-shadow: 0 8px 40px 0 rgba(0, 0, 0, 0.13);
}

.login-btn {
  font-size: 1.15rem;
  padding: 0.75rem 0;
  border-radius: 8px;
  transition: background 0.2s;
  font-weight: 500;
}

.login-btn:hover {
  background: #1769e0;
}

.card {
  box-shadow: 0 6px 32px 0 rgba(0, 0, 0, 0.10);
}
</style>