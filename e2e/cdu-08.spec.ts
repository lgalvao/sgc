import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import * as AtividadeHelpers from './helpers/helpers-atividades.js';
import {fazerLogout} from './helpers/helpers-navegacao.js';
import {criarProcessoFinalizadoFixture, criarProcessoFixture} from './fixtures/fixtures-processos.js';

test.describe('CDU-08 - Manter cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'ASSESSORIA_11';
    const CHEFE_UNIDADE = USUARIOS.CHEFE_ASSESSORIA_11.titulo;
    const SENHA_CHEFE = USUARIOS.CHEFE_ASSESSORIA_11.senha;

    test('Cenário 1: Processo de Mapeamento (Fluxo Completo + Importação + Auto-save)', async ({
                                                                                       page,
                                                                                       autenticadoComoAdmin,
                                                                                       request
                                                                                   }) => {
        const timestamp = Date.now();
        const descricaoProcesso = `Processo CDU-08 Map ${timestamp}`;
        const processoOrigemDescricao = `Processo Base FINALIZADO ${timestamp}`;
        let processoOrigemId: number;
        let processoAlvoId: number;

        await test.step('1. Setup: Criar Processo Origem e Mapeamento Alvo', async () => {
            
            // Criar Processo Finalizado via Fixture (para importação)
            const procOrigem = await criarProcessoFinalizadoFixture(request, {
                unidade: 'SECRETARIA_1',
                descricao: processoOrigemDescricao
            });
            processoOrigemId = procOrigem.codigo;

            const processoAlvo = await criarProcessoFixture(request, {
                unidade: UNIDADE_ALVO,
                descricao: descricaoProcesso,
                iniciar: true,
                diasLimite: 30
            });
            processoAlvoId = processoAlvo.codigo;

            await fazerLogout(page);
        });

        await test.step('2. Acessar tela de Atividades', async () => {
            await login(page, CHEFE_UNIDADE, SENHA_CHEFE);
            await page.goto(`/processo/${processoAlvoId}/${UNIDADE_ALVO}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoAlvoId}/${UNIDADE_ALVO}$`));
            await AtividadeHelpers.navegarParaAtividades(page);
        });

        await test.step('3. Importar Atividades (Fluxo Positivo e Negativo)', async () => {
            const atividadeA = `Atividade Origem A - ${processoOrigemId}`;
            const atividadeB = `Atividade Origem B - ${processoOrigemId}`;
            
            // Importar ambas as atividades com sucesso
            await AtividadeHelpers.importarAtividades(page, processoOrigemDescricao, 'SECRETARIA_1', [atividadeA, atividadeB]);

            // Tentar importar de novo e esperar o erro (Fluxo Negativo / Regra de Duplicidade)
            await AtividadeHelpers.importarAtividadesComErroDuplicidade(page, processoOrigemDescricao, 'SECRETARIA_1', [atividadeA]);
        });

        const atividadeManual = `Atividade Manual ${timestamp}`;

        await test.step('4. Adicionar Manualmente e Validar Auto-save', async () => {
            await AtividadeHelpers.adicionarAtividade(page, atividadeManual);
            await AtividadeHelpers.verificarSituacaoSubprocesso(page, 'Cadastro em andamento');

            const conhecimento1 = `Conhecimento Manual ${timestamp}`;
            await AtividadeHelpers.adicionarConhecimento(page, atividadeManual, conhecimento1);

            // Recarregar a página para atestar que os dados estão sendo persistidos
            await page.reload();

            // Ao recarregar, tudo o que foi inserido precisa estar lá
            await expect(page.getByText(atividadeManual, { exact: true }).first()).toBeVisible();
            await expect(page.locator('.group-conhecimento', { hasText: conhecimento1 }).first()).toBeVisible();
            await expect(page.getByText(`Atividade Origem A - ${processoOrigemId}`, { exact: true }).first()).toBeVisible();
        });

        await test.step('5. Editar e Remover', async () => {
            const atividadeEditada = `${atividadeManual} EDITADA`;
            await AtividadeHelpers.editarAtividade(page, atividadeManual, atividadeEditada);

            const conhecimento1 = `Conhecimento Manual ${timestamp}`;
            const conhecimento1Editado = `${conhecimento1} EDITADO`;

            await AtividadeHelpers.editarConhecimento(page, atividadeEditada, conhecimento1, conhecimento1Editado);
            await AtividadeHelpers.removerConhecimento(page, atividadeEditada, conhecimento1Editado);
            await AtividadeHelpers.removerAtividade(page, atividadeEditada);
        });

        await test.step('6. Verificar Ausência de Botão de Impacto', async () => {
            await AtividadeHelpers.verificarBotaoImpactoAusenteEdicao(page);
        });

        await test.step('7. Disponibilizar', async () => {
            await AtividadeHelpers.disponibilizarCadastro(page);
            await expect(page).toHaveURL(/\/painel/);
        });
    });

    test('Cenário 2: Processo de Revisão (Botão Impacto)', async ({page, request}) => {
        const timestamp = Date.now();
        const descricao = `Processo CDU-08 Rev ${timestamp}`;
        const UNIDADE_REVISAO = 'ASSESSORIA_12';
        const CHEFE_REVISAO = USUARIOS.CHEFE_ASSESSORIA_12.titulo;
        const SENHA_REVISAO = USUARIOS.CHEFE_ASSESSORIA_12.senha;
        let processoRevisaoId: number;

        await test.step('Setup: Criar Processo de Revisão', async () => {
            const processoRevisao = await criarProcessoFixture(request, {
                unidade: UNIDADE_REVISAO,
                descricao,
                tipo: 'REVISAO',
                iniciar: true,
                diasLimite: 30
            });
            processoRevisaoId = processoRevisao.codigo;
        });

        await test.step('Verificar Botão Impacto', async () => {
            await login(page, CHEFE_REVISAO, SENHA_REVISAO);
            await page.goto(`/processo/${processoRevisaoId}/${UNIDADE_REVISAO}`);
            await expect(page).toHaveURL(new RegExp(String.raw`/processo/${processoRevisaoId}/${UNIDADE_REVISAO}$`));
            await AtividadeHelpers.navegarParaAtividades(page);

            await AtividadeHelpers.adicionarAtividade(page, 'Atividade Trigger');
            await AtividadeHelpers.verificarBotaoImpactoDropdown(page);
            await AtividadeHelpers.abrirModalImpactoEdicao(page);
            await AtividadeHelpers.fecharModalImpacto(page);
        });
    });
});
