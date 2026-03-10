// noinspection JSUnusedLocalSymbols

import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import * as AtividadeHelpers from './helpers/helpers-atividades.js';
import {fazerLogout} from './helpers/helpers-navegacao.js';
import {criarProcessoFinalizadoFixture, criarProcessoFixture} from './fixtures/fixtures-processos.js';

test.describe('CDU-08 - Manter cadastro de atividades e conhecimentos', () => {
    const UNIDADE_ALVO = 'ASSESSORIA_11';
    const UNIDADE_ORIGEM = 'ASSESSORIA_12';
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
        const processoOrigem2Descricao = `Processo Base FINALIZADO 2 ${timestamp}`;
        let processoOrigemId: number;
        let processoAlvoId: number;

        await test.step('1. Setup: Criar Processos Origem e Mapeamento Alvo', async () => {
            
            // Criar Processos Finalizados via Fixture (para importação)
            const procOrigem = await criarProcessoFinalizadoFixture(request, {
                unidade: UNIDADE_ORIGEM,
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

        await test.step('2.1 Verificar estado inicial do mapeamento', async () => {
            await expect(page.getByTestId('cad-atividades-empty-state')).toBeVisible();
            await AtividadeHelpers.verificarSituacaoSubprocesso(page, 'Não iniciado');
            await AtividadeHelpers.verificarBotaoDisponibilizar(page, false);
        });

        await test.step('3. Importar Atividades (Fluxo Múltiplo e Negativo)', async () => {
            const atividadeA = `Atividade Origem A - ${processoOrigemId}`;
            const atividadeB = `Atividade Origem B - ${processoOrigemId}`;
            
            // Verificar se múltiplos processos/unidades (operacionais/interoperacionais) aparecem nas opções (Passo 13.1 e 13.3)
            await AtividadeHelpers.verificarOpcoesImportacao(page, [
                { processo: processoOrigemDescricao, unidades: [UNIDADE_ORIGEM] },
                { processo: processoOrigem2Descricao, unidades: ['ASSESSORIA_21'] }
            ]);

            // Importar ambas as atividades com sucesso
            await AtividadeHelpers.importarAtividades(page, processoOrigemDescricao, UNIDADE_ORIGEM, [atividadeA, atividadeB]);

            // A importação deve atualizar imediatamente a situação e habilitar a disponibilização
            await AtividadeHelpers.verificarSituacaoSubprocesso(page, 'Cadastro em andamento');
            await AtividadeHelpers.verificarBotaoDisponibilizar(page, true);

            // Tentar importar de novo e esperar o erro (Fluxo Negativo / Regra de Duplicidade)
            await AtividadeHelpers.importarAtividadesComErroDuplicidade(page, processoOrigemDescricao, UNIDADE_ORIGEM, [atividadeA]);
        });

        const atividadeManual = `Atividade Manual ${timestamp}`;

        await test.step('4. Flexibilidade de Fluxo, Cadastro Manual e Validar Auto-save', async () => {
            // Adicionando várias atividades antes dos conhecimentos (Passo 10.1 flexibilidade)
            await AtividadeHelpers.adicionarAtividade(page, atividadeManual);
            
            // Validar mudança de situação após primeira ação (Passo 14)
            await AtividadeHelpers.verificarSituacaoSubprocesso(page, 'Cadastro em andamento');

            const atividadeManual2 = `Atividade Manual 2 ${timestamp}`;
            await AtividadeHelpers.adicionarAtividade(page, atividadeManual2);

            const conhecimento1 = `Conhecimento Manual ${timestamp}`;
            await AtividadeHelpers.adicionarConhecimento(page, atividadeManual, conhecimento1);
            const conhecimento2 = `Conhecimento Manual 2 ${timestamp}`;
            await AtividadeHelpers.adicionarConhecimento(page, atividadeManual2, conhecimento2);

            // Recarregar a página para atestar que os dados estão sendo persistidos
            await page.reload();

            // Ao recarregar, tudo o que foi inserido precisa estar lá
            await expect(page.getByText(atividadeManual, { exact: true }).first()).toBeVisible();
            await expect(page.getByText(atividadeManual2, { exact: true }).first()).toBeVisible();
            await expect(page.locator('.group-conhecimento', { hasText: conhecimento1 }).first()).toBeVisible();
            await expect(page.locator('.group-conhecimento', { hasText: conhecimento2 }).first()).toBeVisible();
            await expect(page.getByText(`Atividade Origem A - ${processoOrigemId}`, { exact: true }).first()).toBeVisible();
        });

        await test.step('5. Editar e Remover (Com cancelamentos visuais)', async () => {
            const atividadeEditada = `${atividadeManual} EDITADA`;
            const atividadeCancelada = `${atividadeManual} CANCELADA`;
            
            // Cancelar edição da atividade (Passo 11.1.2)
            await AtividadeHelpers.cancelarEdicaoAtividade(page, atividadeManual, atividadeCancelada);
            // Editar a atividade de fato (Passo 11.1.1)
            await AtividadeHelpers.editarAtividade(page, atividadeManual, atividadeEditada);

            const conhecimento1 = `Conhecimento Manual ${timestamp}`;
            const conhecimentoCancelado = `${conhecimento1} CANCELADO`;
            const conhecimento1Editado = `${conhecimento1} EDITADO`;

            // Cancelar edição do conhecimento (Passo 12.1-12.2)
            await AtividadeHelpers.cancelarEdicaoConhecimento(page, atividadeEditada, conhecimento1, conhecimentoCancelado);
            // Editar de fato
            await AtividadeHelpers.editarConhecimento(page, atividadeEditada, conhecimento1, conhecimento1Editado);
            
            // Remover conhecimento com diálogo (Passo 12.2)
            await AtividadeHelpers.removerConhecimento(page, atividadeEditada, conhecimento1Editado);
            
            // Remover atividade com cascata (Passo 11.2)
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
            
            // Verificar botão histórico de análise no modo de revisão (Seção 5)
            await AtividadeHelpers.verificarBotaoHistoricoAnalise(page);
        });
    });
});
