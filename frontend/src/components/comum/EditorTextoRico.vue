<script lang="ts" setup>
import {computed, ref, watch} from 'vue'

const props = withDefaults(defineProps<{
  modelValue: string
  desabilitado?: boolean
  rotulo?: string
}>(), {
  desabilitado: false,
  rotulo: 'Editor de texto',
})

const emit = defineEmits<{
  'update:modelValue': [valor: string]
}>()

const editorRef = ref<HTMLDivElement | null>(null)
const emFoco = ref(false)

watch(() => props.modelValue, (valorAtual) => {
  const editor = editorRef.value
  if (!editor || emFoco.value) {
    return
  }

  if (editor.innerHTML !== valorAtual) {
    editor.innerHTML = valorAtual
  }
}, {immediate: true})

const acoes = computed(() => ([
  {comando: 'bold', icone: 'bi-type-bold', rotulo: 'Negrito'},
  {comando: 'italic', icone: 'bi-type-italic', rotulo: 'Itálico'},
  {comando: 'underline', icone: 'bi-type-underline', rotulo: 'Sublinhado'},
  {comando: 'insertUnorderedList', icone: 'bi-list-ul', rotulo: 'Lista'},
  {comando: 'insertOrderedList', icone: 'bi-list-ol', rotulo: 'Lista numerada'},
]))

function sincronizarConteudo() {
  emit('update:modelValue', editorRef.value?.innerHTML ?? '')
}

function executarComando(comando: string) {
  editorRef.value?.focus()
  document.execCommand(comando)
  sincronizarConteudo()
}

function aoFocar() {
  emFoco.value = true
}

function aoPerderFoco() {
  emFoco.value = false
  sincronizarConteudo()
}
</script>

<template>
  <div class="editor-texto-rico">
    <div class="editor-texto-rico__barra" role="toolbar">
      <button
          v-for="acao in acoes"
          :key="acao.comando"
          :aria-label="acao.rotulo"
          :disabled="desabilitado"
          :title="acao.rotulo"
          class="btn btn-sm btn-outline-secondary"
          type="button"
          @click="executarComando(acao.comando)"
      >
        <i :class="['bi', acao.icone]" aria-hidden="true"/>
      </button>
    </div>

    <div
        ref="editorRef"
        :aria-label="rotulo"
        :contenteditable="!desabilitado"
        class="editor-texto-rico__conteudo form-control"
        data-testid="feedback-nota"
        role="textbox"
        @blur="aoPerderFoco"
        @focus="aoFocar"
        @input="sincronizarConteudo"
    />
  </div>
</template>

<style scoped>
.editor-texto-rico {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  border: 1px solid var(--bs-border-color-translucent);
  border-radius: 0.75rem;
  background: var(--bs-body-bg);
  overflow: hidden;
}

.editor-texto-rico__barra {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  padding: 0.6rem 0.75rem;
  border-bottom: 1px solid var(--bs-border-color-translucent);
  background: var(--bs-tertiary-bg);
}

.editor-texto-rico__barra .btn {
  width: 2rem;
  height: 2rem;
  padding: 0;
  border-radius: 0.55rem;
}

.editor-texto-rico__conteudo {
  flex: 1 1 auto;
  min-height: 16rem;
  border: 0;
  border-radius: 0;
  padding: 1rem;
  line-height: 1.6;
  white-space: normal;
  overflow-y: auto;
  background: var(--bs-body-bg);
}

.editor-texto-rico__conteudo:focus {
  box-shadow: none;
  outline: none;
}

.editor-texto-rico__conteudo :deep(p) {
  margin-bottom: 0.65rem;
}

.editor-texto-rico__conteudo :deep(ul),
.editor-texto-rico__conteudo :deep(ol) {
  margin-bottom: 0.75rem;
  padding-left: 1.3rem;
}
</style>
