import {expect, test} from './fixtures/complete-fixtures.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {
    acessarDetalhesProcesso,
    criarProcesso,
    finalizarProcesso,
    obterAcaoBloco,
    verificarProcessoTabela
} from './helpers/helpers-processos.js';
import {
    navegarParaSubprocesso,
    verificarAlertaPainel,
    verificarPaginaPainel,
    verificarToast
} from './helpers/helpers-navegacao.js';
import {adicionarAtividade, adicionarConhecimento, disponibilizarCadastro, navegarParaCadastro} from './helpers/helpers-atividades.js';
import {
    abrirAcaoMapa,
    abrirValidacaoMapa,
    criarCompetencia,
    navegarParaMapa
} from './helpers/helpers-mapas.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

test.describe.serial('Tramitação ponta a ponta em bloco', () => {
    test('Mapeamento completo deve tramitar em bloco até o ADMIN com comunicações visíveis', async ({
        _resetAutomatico,
        page
    }) => {
        test.setTimeout(90000);

        const siglaUnidade = 'SECAO_221';
        const descricaoProcesso = `Mapeamento bloco ponta a ponta ${Date.now()}`;
        const atividade = `Atividade bloco ${Date.now()}`;
        const conhecimento = `Conhecimento bloco ${Date.now()}`;
        const competencia = `Competência bloco ${Date.now()}`;

        await test.step('ADMIN cria e inicia o processo', async () => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await criarProcesso(page, {
                descricao: descricaoProcesso,
                tipo: 'MAPEAMENTO',
                unidade: [siglaUnidade],
                expandir: ['SECRETARIA_2', 'COORD_22'],
                iniciar: true,
                unidadesComEquipePropriaParticipantes: []
            });
            await verificarProcessoTabela(page, {
                descricao: descricaoProcesso,
                situacao: 'Em andamento',
                tipo: 'Mapeamento'
            });
        });

        await test.step('CHEFE preenche atividades e disponibiliza o cadastro', async () => {
            await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
            await acessarDetalhesProcesso(page, descricaoProcesso);
            await navegarParaSubprocesso(page, siglaUnidade);
            await navegarParaCadastro(page);
            await adicionarAtividade(page, atividade);
            await adicionarConhecimento(page, atividade, conhecimento);
            await disponibilizarCadastro(page);
        });

        await test.step('GESTOR da coordenadoria registra aceite em bloco do cadastro', async () => {
            await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
            await acessarDetalhesProcesso(page, descricaoProcesso);
            const botaoAceitar = await obterAcaoBloco(page, 'btn-processo-aceitar-bloco');
            await expect(botaoAceitar).toBeEnabled();
            await botaoAceitar.click();
            await page.locator('#modal-acao-bloco').getByRole('button', {name: TEXTOS.acaoBloco.aceitar.BOTAO}).click();
            await verificarToast(page, TEXTOS.sucesso.CADASTROS_ACEITOS_EM_BLOCO);
            await verificarPaginaPainel(page);
        });

        await test.step('GESTOR da secretaria registra aceite em bloco do cadastro até o ADMIN', async () => {
            await loginComPerfil(
                page,
                USUARIOS.GESTOR_SECRETARIA_2.titulo,
                USUARIOS.GESTOR_SECRETARIA_2.senha,
                USUARIOS.GESTOR_SECRETARIA_2.perfil!
            );
            await acessarDetalhesProcesso(page, descricaoProcesso);
            const botaoAceitar = await obterAcaoBloco(page, 'btn-processo-aceitar-bloco');
            await expect(botaoAceitar).toBeEnabled();
            await botaoAceitar.click();
            await page.locator('#modal-acao-bloco').getByRole('button', {name: TEXTOS.acaoBloco.aceitar.BOTAO}).click();
            await verificarToast(page, TEXTOS.sucesso.CADASTROS_ACEITOS_EM_BLOCO);
            await verificarPaginaPainel(page);
        });

        await test.step('ADMIN recebe notificação e alerta do aceite final do cadastro em bloco', async () => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await verificarNotificacaoAdmin(page, {
                destinatario: 'ADMIN',
                assunto: `Cadastro de atividades e conhecimentos da ${siglaUnidade} submetido para análise`,
                tipo: 'Cadastro aceito',
                trechoCorpo: `O cadastro de atividades e conhecimentos da sua unidade no processo ${descricaoProcesso} foi aceito e submetido para análise pela unidade superior imediata.`
            });
            await page.getByTestId('nav-link-painel').click();
            await verificarAlertaPainel(page, new RegExp(`Cadastro da unidade ${siglaUnidade} submetido para análise`, 'i'));
        });

        await test.step('ADMIN homologa os cadastros em bloco', async () => {
            await acessarDetalhesProcesso(page, descricaoProcesso);
            const botaoHomologar = await obterAcaoBloco(page, 'btn-processo-homologar-bloco');
            await expect(botaoHomologar).toBeEnabled();
            await botaoHomologar.click();
            await page.locator('#modal-acao-bloco').getByRole('button', {name: TEXTOS.acaoBloco.homologar.BOTAO}).click();
            await verificarToast(page, TEXTOS.sucesso.CADASTROS_HOMOLOGADOS_EM_BLOCO);
            await verificarPaginaPainel(page);
        });

        await test.step('ADMIN cria o mapa e disponibiliza em bloco', async () => {
            await acessarDetalhesProcesso(page, descricaoProcesso);
            await navegarParaSubprocesso(page, siglaUnidade);
            await navegarParaMapa(page);
            await criarCompetencia(page, competencia, [atividade]);
            await page.goto('/painel');
            await acessarDetalhesProcesso(page, descricaoProcesso);

            const botaoDisponibilizar = await obterAcaoBloco(page, 'btn-processo-disponibilizar-bloco');
            await expect(botaoDisponibilizar).toBeEnabled();
            await botaoDisponibilizar.click();

            const modal = page.locator('#modal-acao-bloco');
            await expect(modal).toBeVisible();
            await modal.getByLabel(/Data limite/i).fill('2099-12-31');
            await modal.getByRole('button', {name: TEXTOS.acaoBloco.disponibilizar.BOTAO}).click();

            await verificarToast(page, TEXTOS.sucesso.MAPAS_DISPONIBILIZADOS_EM_BLOCO);
            await verificarPaginaPainel(page);
        });

        await test.step('CHEFE valida o mapa', async () => {
            await login(page, USUARIOS.CHEFE_SECAO_221.titulo, USUARIOS.CHEFE_SECAO_221.senha);
            await acessarDetalhesProcesso(page, descricaoProcesso);
            await navegarParaSubprocesso(page, siglaUnidade);
            await navegarParaMapa(page);
            await abrirValidacaoMapa(page);
            await page.getByTestId('btn-validar-mapa-confirmar').click();
            await verificarPaginaPainel(page);
        });

        await test.step('GESTOR da coordenadoria registra aceite em bloco da validação do mapa', async () => {
            await login(page, USUARIOS.GESTOR_COORD_22.titulo, USUARIOS.GESTOR_COORD_22.senha);
            await acessarDetalhesProcesso(page, descricaoProcesso);
            const botaoAceitar = await obterAcaoBloco(page, 'btn-processo-aceitar-mapas-bloco');
            await expect(botaoAceitar).toBeEnabled();
            await botaoAceitar.click();
            await page.locator('#modal-acao-bloco').getByRole('button', {name: TEXTOS.acaoBloco.aceitar.BOTAO}).click();
            await verificarToast(page, TEXTOS.sucesso.MAPAS_ACEITOS_EM_BLOCO);
            await verificarPaginaPainel(page);
        });

        await test.step('GESTOR da secretaria registra aceite em bloco da validação do mapa até o ADMIN', async () => {
            await loginComPerfil(
                page,
                USUARIOS.GESTOR_SECRETARIA_2.titulo,
                USUARIOS.GESTOR_SECRETARIA_2.senha,
                USUARIOS.GESTOR_SECRETARIA_2.perfil!
            );
            await acessarDetalhesProcesso(page, descricaoProcesso);
            const botaoAceitar = await obterAcaoBloco(page, 'btn-processo-aceitar-mapas-bloco');
            await expect(botaoAceitar).toBeEnabled();
            await botaoAceitar.click();
            await page.locator('#modal-acao-bloco').getByRole('button', {name: TEXTOS.acaoBloco.aceitar.BOTAO}).click();
            await verificarToast(page, TEXTOS.sucesso.MAPAS_ACEITOS_EM_BLOCO);
            await verificarPaginaPainel(page);
        });

        await test.step('ADMIN recebe notificação e alerta do aceite final da validação do mapa em bloco', async () => {
            await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);
            await verificarNotificacaoAdmin(page, {
                destinatario: 'ADMIN',
                assunto: `Validação do mapa de competências da ${siglaUnidade} submetida para análise`,
                tipo: 'Validação do mapa aceita',
                trechoCorpo: `A validação do mapa de competências da sua unidade no processo ${descricaoProcesso} foi aceita e submetida para análise pela unidade superior imediata.`
            });
            await page.getByTestId('nav-link-painel').click();
            await verificarAlertaPainel(page, new RegExp(`Validação do mapa da unidade ${siglaUnidade} submetida para análise`, 'i'));
        });

        await test.step('ADMIN homologa os mapas em bloco e finaliza o processo', async () => {
            await acessarDetalhesProcesso(page, descricaoProcesso);
            const botaoHomologar = await obterAcaoBloco(page, 'btn-processo-homologar-mapas-bloco');
            await expect(botaoHomologar).toBeEnabled();
            await botaoHomologar.click();
            await page.getByRole('dialog').getByRole('button', {name: /^Homologar$/i}).click();
            await verificarPaginaPainel(page);

            await acessarDetalhesProcesso(page, descricaoProcesso);
            await finalizarProcesso(page);
            await verificarProcessoTabela(page, {
                descricao: descricaoProcesso,
                situacao: 'Finalizado',
                tipo: 'Mapeamento'
            });
        });
    });
});
