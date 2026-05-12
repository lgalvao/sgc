<script lang="ts" setup>
import {computed, onBeforeUnmount, watch} from "vue";
import {Editor, EditorContent} from "@tiptap/vue-3";
import StarterKit from "@tiptap/starter-kit";
import {htmlTemConteudo, normalizarHtmlEditor} from "@/utils/textoFormatado";

const props = withDefaults(defineProps<{
    modelValue: string;
    desabilitado?: boolean;
    rotulo?: string;
    dataTestid?: string;
    minimoAltura?: string;
    alvoDataTestid?: "editor" | "espelho";
}>(), {
    desabilitado: false,
    rotulo: "Editor de texto",
    dataTestid: "editor-texto-rico-conteudo",
    minimoAltura: "12rem",
    alvoDataTestid: "espelho",
});

const emit = defineEmits<{
    "update:modelValue": [valor: string];
}>();

const editor = new Editor({
    extensions: [
        StarterKit.configure({
            heading: false,
            blockquote: false,
            code: false,
            codeBlock: false,
            horizontalRule: false,
        }),
    ],
    editorProps: {
        attributes: {
            class: "editor-texto-rico__conteudo form-control",
            role: "textbox",
        },
    },
    content: normalizarHtmlEditor(props.modelValue),
    editable: !props.desabilitado,
    onUpdate: ({editor: editorAtual}) => {
        emit("update:modelValue", editorAtual.isEmpty ? "" : normalizarHtmlEditor(editorAtual.getHTML()));
    },
});

watch(() => props.modelValue, (valorAtual) => {
    const conteudoAtual = normalizarHtmlEditor(editor.getHTML());
    const proximoConteudo = normalizarHtmlEditor(valorAtual);
    if (conteudoAtual === proximoConteudo) {
        return;
    }
    editor.commands.setContent(proximoConteudo, {emitUpdate: false});
}, {immediate: true});

watch(() => props.desabilitado, (desabilitado) => {
    editor.setEditable(!desabilitado);
});

onBeforeUnmount(() => {
    editor.destroy();
});

const acoes = computed(() => ([
    {
        codigo: "bold",
        icone: "bi-type-bold",
        rotulo: "Negrito",
        ativa: () => editor.isActive("bold"),
        executar: () => editor.chain().focus().toggleBold().run(),
    },
    {
        codigo: "italic",
        icone: "bi-type-italic",
        rotulo: "Itálico",
        ativa: () => editor.isActive("italic"),
        executar: () => editor.chain().focus().toggleItalic().run(),
    },
    {
        codigo: "bulletList",
        icone: "bi-list-ul",
        rotulo: "Lista",
        ativa: () => editor.isActive("bulletList"),
        executar: () => editor.chain().focus().toggleBulletList().run(),
    },
    {
        codigo: "orderedList",
        icone: "bi-list-ol",
        rotulo: "Lista numerada",
        ativa: () => editor.isActive("orderedList"),
        executar: () => editor.chain().focus().toggleOrderedList().run(),
    },
]));

const classesEditor = computed(() => ({
    "is-invalid": props.desabilitado === false && !htmlTemConteudo(props.modelValue) && false,
}));

const dataTestidEspelho = computed(() => props.alvoDataTestid === "espelho" ? props.dataTestid : undefined);
const dataTestidEditor = computed(() => props.alvoDataTestid === "editor" ? props.dataTestid : `${props.dataTestid}-editor`);

function executarAcao(executar: () => boolean) {
    if (!props.desabilitado) {
        executar();
    }
}

function atualizarPorEspelho(event: Event) {
    emit("update:modelValue", (event.target as HTMLTextAreaElement).value);
}

function sincronizarPorDom(event: Event) {
    const alvo = event.target;
    if (!(alvo instanceof HTMLDivElement)) {
        return;
    }

    const html = normalizarHtmlEditor(alvo.innerHTML);
    if (html === normalizarHtmlEditor(props.modelValue)) {
        return;
    }

    emit("update:modelValue", html);
}
</script>

<template>
    <div class="editor-texto-rico">
        <textarea
            :value="modelValue"
            :data-testid="dataTestidEspelho"
            aria-hidden="true"
            class="editor-texto-rico__espelho"
            tabindex="-1"
            @input="atualizarPorEspelho"
        />
        <div class="editor-texto-rico__barra" role="toolbar">
            <button
                v-for="acao in acoes"
                :key="acao.codigo"
                :aria-label="acao.rotulo"
                :class="['btn btn-sm', acao.ativa() ? 'btn-secondary' : 'btn-outline-secondary']"
                :disabled="desabilitado"
                :title="acao.rotulo"
                type="button"
                @click="executarAcao(acao.executar)"
            >
                <i :class="['bi', acao.icone]" aria-hidden="true"/>
            </button>
        </div>

        <EditorContent
            :editor="editor"
            :aria-label="rotulo"
            :class="classesEditor"
            :data-testid="dataTestidEditor"
            :style="{ '--editor-minimo-altura': minimoAltura }"
            @input.capture="sincronizarPorDom"
        />
    </div>
</template>

<style scoped>
.editor-texto-rico {
    display: flex;
    flex-direction: column;
    min-height: 0;
    border: 1px solid var(--bs-border-color-translucent);
    border-radius: 0.75rem;
    background: var(--bs-body-bg);
    overflow: hidden;
}

.editor-texto-rico__espelho {
    position: absolute;
    width: 1px;
    height: 1px;
    padding: 0;
    margin: -1px;
    overflow: hidden;
    clip: rect(0, 0, 0, 0);
    white-space: nowrap;
    border: 0;
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

.editor-texto-rico :deep(.editor-texto-rico__conteudo) {
    min-height: var(--editor-minimo-altura);
    border: 0;
    border-radius: 0;
    padding: 1rem;
    line-height: 1.6;
    overflow-y: auto;
    background: var(--bs-body-bg);
}

.editor-texto-rico :deep(.editor-texto-rico__conteudo:focus) {
    box-shadow: none;
    outline: none;
}

.editor-texto-rico :deep(.editor-texto-rico__conteudo p) {
    margin-bottom: 0.65rem;
}

.editor-texto-rico :deep(.editor-texto-rico__conteudo ul),
.editor-texto-rico :deep(.editor-texto-rico__conteudo ol) {
    margin-bottom: 0.75rem;
    padding-left: 1.3rem;
}

.editor-texto-rico :deep(.editor-texto-rico__conteudo p:last-child),
.editor-texto-rico :deep(.editor-texto-rico__conteudo ul:last-child),
.editor-texto-rico :deep(.editor-texto-rico__conteudo ol:last-child) {
    margin-bottom: 0;
}
</style>
