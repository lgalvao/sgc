import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoDiagnosticoConcluidoFixture} from './fixtures/index.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {acessarDetalhesProcesso, obterAcaoBloco} from './helpers/helpers-processos.js';
import {verificarPaginaPainel} from './helpers/helpers-navegacao.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';

test.describe.serial('CDU-51 - aceitar diagnósticos em bloco', () => {
    const UNIDADE = 'ASSESSORIA_12';
    const DESCRICAO_PROCESSO = `Diagnóstico CDU-51 ${Date.now()}`;

    test('Setup data', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoDiagnosticoConcluidoFixture(request, {
            descricao: DESCRICAO_PROCESSO,
            unidade: UNIDADE
        });
        expect(processo.codigo).toBeGreaterThan(0);
    });

    test('Cenários CDU-51: GESTOR aceita diagnósticos em bloco', async ({_resetAutomatico, page}) => {
        await loginComPerfil(
            page,
            USUARIOS.GESTOR_SECRETARIA_1.titulo,
            USUARIOS.GESTOR_SECRETARIA_1.senha,
            'GESTOR - SECRETARIA_1'
        );

        await acessarDetalhesProcesso(page, DESCRICAO_PROCESSO);
        await expect(page.getByTestId('processo-info')).toBeVisible();

        const botaoAceitar = await obterAcaoBloco(page, 'btn-processo-aceitar-diagnosticos-bloco');
        await expect(botaoAceitar).toBeVisible();
        await expect(botaoAceitar).toBeEnabled();

        await botaoAceitar.click();

        const modal = page.locator('#modal-acao-bloco');
        await expect(modal).toHaveClass(/show/);
        await expect(modal.locator('table')).toBeVisible();
        await expect(modal.locator('tr', {hasText: UNIDADE})).toBeVisible();

        await modal.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modal).not.toHaveClass(/show/);
        await expect(page.getByTestId('processo-info')).toBeVisible();

        const botaoAceitarConfirmacao = await obterAcaoBloco(page, 'btn-processo-aceitar-diagnosticos-bloco');
        await botaoAceitarConfirmacao.click();
        await expect(modal).toHaveClass(/show/);
        await modal.getByTestId('btn-acao-bloco-confirmar').click();

        await verificarPaginaPainel(page);
        await expect(page.getByText('Diagnósticos aceitos')).toBeVisible();

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
        await verificarNotificacaoAdmin(page, {
            destinatario: 'ADMIN',
            assunto: 'Diagnósticos submetidos para análise',
            tipo: 'Diagnóstico aceito',
            trechoCorpo: UNIDADE
        });
    });
});
