import {vueTest as test} from './support/vue-specific-setup';
import {
    esperarElementoVisivel,
    loginComoChefe,
    SELETORES,
    criarProcessoCompleto,
    gerarNomeUnico,
    irParaSubprocesso,
    loginComoGestor,
    disponibilizarCadastro,
} from './helpers';
import { aceitarCadastro } from '@/services/cadastroService';
import { verificarCardAcaoVisivel, verificarCardAcaoInvisivel } from './helpers/verificacoes/verificacoes-ui';
import * as cadastroService from '@/services/cadastroService';

test.describe('CDU-07: Detalhar subprocesso', () => {
    let processo: any;
    const siglaUnidade = 'STIC'; // Assumindo uma unidade padrão para o teste

    test.beforeEach(async ({page}) => {
        const nomeProcesso = gerarNomeUnico('PROCESSO CDU-07');
        processo = await criarProcessoCompleto(page, nomeProcesso, 'MAPEAMENTO', '2025-12-31', [1]); // Unidade 1 = SEDOC
    });

    test('deve mostrar detalhes do subprocesso para CHEFE', async ({page}) => {
        await loginComoChefe(page);
        await irParaSubprocesso(page, processo.processo.codigo, siglaUnidade);

        await esperarElementoVisivel(page, SELETORES.SUBPROCESSO_HEADER);
        await esperarElementoVisivel(page, SELETORES.PROCESSO_INFO);
    });

    test('CHEFE deve ver card de Cadastro de atividades em CADASTRO_EM_ANDAMENTO', async ({page}) => {
        await loginComoChefe(page);
        await irParaSubprocesso(page, processo.processo.codigo, siglaUnidade);

        await verificarCardAcaoVisivel(page, 'Cadastro de atividades');
        await verificarCardAcaoInvisivel(page, 'Mapa de competências');
    });

    test('CHEFE deve ver card de Mapa de competências em MAPA_EM_ANDAMENTO', async ({page}) => {
        // Simular que o cadastro foi disponibilizado e aceito, avançando para MAPA_EM_ANDAMENTO
        await cadastroService.disponibilizarCadastro(processo.subprocessos[0].codigo);
        await cadastroService.aceitarCadastro(processo.subprocessos[0].codigo, {observacoes: 'Teste'});

        await loginComoChefe(page);
        await irParaSubprocesso(page, processo.processo.codigo, siglaUnidade);

        await verificarCardAcaoInvisivel(page, 'Cadastro de atividades');
        await verificarCardAcaoVisivel(page, 'Mapa de competências');
    });

    test('GESTOR deve ver card de Análise de cadastro em CADASTRO_DISPONIBILIZADO', async ({page}) => {
        // Simular que o cadastro foi disponibilizado
        await cadastroService.disponibilizarCadastro(processo.subprocessos[0].codigo);

        await loginComoGestor(page);
        await irParaSubprocesso(page, processo.processo.codigo, siglaUnidade);

        await verificarCardAcaoVisivel(page, 'Análise de cadastro');
        await verificarCardAcaoInvisivel(page, 'Cadastro de atividades');
        await verificarCardAcaoInvisivel(page, 'Mapa de competências');
    });
});
