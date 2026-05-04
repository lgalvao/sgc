import type {Meta, StoryObj} from '@storybook/vue3-vite';
import {ref} from 'vue';
import BuscadorUsuarios from './BuscadorUsuarios.vue';

const meta: Meta<typeof BuscadorUsuarios> = {
    title: 'Comum/BuscadorUsuarios',
    component: BuscadorUsuarios,
    tags: ['autodocs'],
    argTypes: {
        state: {
            control: 'select',
            options: [null, true, false],
        },
        placeholder: {control: 'text'},
    },
};

export default meta;
type Story = StoryObj<typeof BuscadorUsuarios>;

export const Default: Story = {
    tags: ['visual-only'],
    render: () => ({
        components: {BuscadorUsuarios},
        setup() {
            const termo = ref('');
            const selecionado = ref<string | null>(null);
            return {termo, selecionado};
        },
        template: `
      <div style="max-width: 400px; padding: 20px;">
        <label class="form-label">Buscar servidor</label>
        <BuscadorUsuarios v-model:termo="termo" v-model:selecionado="selecionado" />
        <small class="text-muted mt-2 d-block">Selecionado: {{ selecionado ?? 'Nenhum' }}</small>
      </div>
    `,
    }),
};

export const ComPlaceholder: Story = {
    tags: ['visual-only'],
    render: () => ({
        components: {BuscadorUsuarios},
        setup() {
            const termo = ref('');
            const selecionado = ref<string | null>(null);
            return {termo, selecionado};
        },
        template: `
      <div style="max-width: 400px; padding: 20px;">
        <BuscadorUsuarios
          v-model:termo="termo"
          v-model:selecionado="selecionado"
          placeholder="Digite o título eleitoral ou nome..."
        />
      </div>
    `,
    }),
};

export const ComEstadoInvalido: Story = {
    tags: ['visual-only'],
    render: () => ({
        components: {BuscadorUsuarios},
        setup() {
            const termo = ref('');
            const selecionado = ref<string | null>(null);
            return {termo, selecionado};
        },
        template: `
      <div style="max-width: 400px; padding: 20px;">
        <BuscadorUsuarios
          v-model:termo="termo"
          v-model:selecionado="selecionado"
          :state="false"
        />
        <div class="invalid-feedback d-block">Selecione um servidor responsável.</div>
      </div>
    `,
    }),
};
