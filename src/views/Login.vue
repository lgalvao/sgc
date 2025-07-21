<template>
  <div class="login-bg">
    <div class="d-flex justify-content-center align-items-center min-vh-100">
      <div class="card login-card p-4 shadow-lg">
        <h2 class="mb-4 text-center">SGC - Login</h2>
        <form class="p-0" @submit.prevent="entrar">
          <div class="mb-3">
            <label class="form-label" for="usuario">Título</label>
            <input id="usuario" v-model="usuario" autocomplete="username" class="form-control"
                   type="text"/>
          </div>
          <div class="mb-3">
            <label class="form-label" for="senha">Senha</label>
            <input id="senha" v-model="senha" autocomplete="current-password" class="form-control"
                   type="password"/>
          </div>
          <button class="btn btn-primary w-100 login-btn" type="submit">Entrar</button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'

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
  background: linear-gradient(135deg, #e3f0ff 0%, #f6faff 100%);
}

.login-card {
  width: 100%;
  max-width: 380px;
  border-radius: 18px;
  border: none;
}

.login-btn {
  font-size: 1.15rem;
  padding: 0.75rem 0;
  border-radius: 8px;
  transition: background 0.2s;
}

.login-btn:hover {
  background: #1769e0;
}

.card {
  box-shadow: 0 6px 32px 0 rgba(0, 0, 0, 0.10);
}
</style>