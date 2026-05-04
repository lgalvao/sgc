import {describe, expect, it} from 'vitest';
import {ref} from 'vue';
import {useAcesso} from '../index';
import {SituacaoSubprocesso, TipoProcesso, type SubprocessoDetalhe} from '@/types/tipos';
import {PERMISSOES_SUBPROCESSO_VAZIAS} from '@/utils/permissoesSubprocesso';

describe('useAcesso composable', () => {
    const mockSubprocesso = (permissoes = {}): SubprocessoDetalhe => ({
        codigo: 1,
        situacao: SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        unidade: {codigo: 1, sigla: 'TEST', nome: 'Teste'},
        titular: null,
        responsavel: null,
        localizacaoAtual: 'Teste',
        processoDescricao: 'Teste',
        dataCriacaoProcesso: '2024-01-01',
        ultimaDataLimiteSubprocesso: '2024-01-01',
        prazoEtapaAtual: '2024-01-01',
        isEmAndamento: true,
        etapaAtual: 1,
        movimentacoes: [],
        elementosProcesso: [],
        permissoes: {
            ...PERMISSOES_SUBPROCESSO_VAZIAS,
            ...permissoes
        }
    });

    it('deve mapear corretamente as flags de permissão do DTO', () => {
        const subprocesso = ref<SubprocessoDetalhe | null>(mockSubprocesso({
            habilitarEditarCadastro: true,
            habilitarDisponibilizarCadastro: true,
            podeEditarCadastro: true
        }));

        const {habilitarEditarCadastro, habilitarDisponibilizarCadastro, podeEditarCadastro} = useAcesso(subprocesso);

        expect(habilitarEditarCadastro.value).toBe(true);
        expect(habilitarDisponibilizarCadastro.value).toBe(true);
        expect(podeEditarCadastro.value).toBe(true);
    });

    it('deve usar permissões vazias (restritivas) se o subprocesso for nulo', () => {
        const subprocesso = ref<SubprocessoDetalhe | null>(null);
        const {habilitarEditarCadastro, podeEditarMapa} = useAcesso(subprocesso);

        expect(habilitarEditarCadastro.value).toBe(false);
        expect(podeEditarMapa.value).toBe(false);
    });

    it('deve reagir a mudanças no subprocesso', () => {
        const subprocesso = ref<SubprocessoDetalhe | null>(mockSubprocesso({habilitarEditarCadastro: false}));
        const {habilitarEditarCadastro} = useAcesso(subprocesso);

        expect(habilitarEditarCadastro.value).toBe(false);

        // Muda o subprocesso para um estado habilitado
        subprocesso.value = mockSubprocesso({habilitarEditarCadastro: true});
        expect(habilitarEditarCadastro.value).toBe(true);
    });

    it('deve manter a regra de ouro: se mesmaUnidade for false, habilitar deve ser false', () => {
        // Nota: O useAcesso apenas reflete o que o backend envia.
        const subprocesso = ref<SubprocessoDetalhe | null>(mockSubprocesso({
            mesmaUnidade: false,
            habilitarEditarCadastro: false
        }));

        const {habilitarEditarCadastro, mesmaUnidade} = useAcesso(subprocesso);

        expect(mesmaUnidade.value).toBe(false);
        expect(habilitarEditarCadastro.value).toBe(false);
    });
});
