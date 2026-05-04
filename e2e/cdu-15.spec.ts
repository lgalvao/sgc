import {expect, test} from './fixtures/complete-fixtures.js';
import {criarProcessoCadastroHomologadoFixture, validarProcessoFixture} from './fixtures/index.js';
import {acessarSubprocessoAdmin} from './helpers/helpers-analise.js';
import {verificarProcessoTabela} from './helpers/helpers-processos.js';
import {
    abrirAcaoMapa,
    criarCompetencia,
    disponibilizarMapa,
    editarCompetencia,
    excluirCompetenciaCancelando,
    excluirCompetenciaConfirmando,
    navegarParaMapa,
    removerAtividadeAssociada,
    verificarCompetenciaNoMapa
} from './helpers/helpers-mapas.js';
import {TEXTOS} from '../frontend/src/constants/textos.js';

test.describe.serial('CDU-15 - Manter mapa de competências', () => {
    const UNIDADE_ALVO = 'SECRETARIA_1';

    const timestamp = Date.now();
    const descProcesso = `Processo CDU-15 ${timestamp}`;

    const ATIVIDADE_1 = 'Atividade fixture 1';
    const ATIVIDADE_2 = 'Atividade fixture 2';
    const ATIVIDADE_3 = 'Atividade fixture 3';

    test('Setup data', async ({_resetAutomatico, request}) => {
        const processo = await criarProcessoCadastroHomologadoFixture(request, {
            descricao: descProcesso,
            unidade: UNIDADE_ALVO
        });
        validarProcessoFixture(processo, descProcesso);
    });

    test('Cenários CDU-15: Fluxo completo de manutenção do mapa pelo ADMIN', async ({
                                                                                        _resetAutomatico,
                                                                                        page,
                                                                                        _autenticadoComoAdmin
                                                                                    }) => {
        // CT-00 e CT-01: Acessar edição e verificar elementos
        await acessarSubprocessoAdmin(page, descProcesso, UNIDADE_ALVO);
        await navegarParaMapa(page);

        await expect(page.getByRole('heading', {name: TEXTOS.mapa.TITULO_TECNICO})).toBeVisible();
        await expect(page.getByTestId('btn-abrir-criar-competencia')).toBeVisible();

        const btnDisponibilizar = await abrirAcaoMapa(page, 'btn-mapa-acao-disponibilizar');
        await btnDisponibilizar.click();
        await expect(page.getByText(TEXTOS.mapa.ERRO_MAPA_SEM_COMPETENCIAS)).toBeVisible();

        // CT-01b: Verificar botões do modal de criação de competência (CDU-15 item 14)
        await page.getByTestId('btn-abrir-criar-competencia').click();
        const modalCriacao = page.getByTestId('mdl-criar-competencia');
        await expect(modalCriacao).toBeVisible();
        await expect(modalCriacao.getByTestId('btn-criar-competencia-salvar')).toBeVisible();
        await expect(modalCriacao.getByRole('button', {name: /Cancelar/i})).toBeVisible();
        await modalCriacao.getByRole('button', {name: /Cancelar/i}).click();
        await expect(modalCriacao).toBeHidden();

        // CT-02: Criar competência
        const compDesc = `Competência 1 ${timestamp}`;
        await criarCompetencia(page, compDesc, [ATIVIDADE_1]);
        await verificarCompetenciaNoMapa(page, compDesc, [ATIVIDADE_1]);

        // CT-02a: Ao sair do mapa e entrar novamente sem refresh, a competência criada
        // deve permanecer visível; isso protege contra regressão de cache do contexto.
        await page.getByRole('link', {name: UNIDADE_ALVO, exact: true}).click();
        await expect(page).toHaveURL(new RegExp(String.raw`/processo/\d+/${UNIDADE_ALVO}(?:\?.*)?$`));
        await expect(page.getByTestId('header-subprocesso')).toBeVisible();

        await navegarParaMapa(page);
        await verificarCompetenciaNoMapa(page, compDesc, [ATIVIDADE_1]);

        // Valida presença do conhecimento inline na vista unificada (MapaView)
        const cardCompetencia = page.locator('.competencia-card', {hasText: compDesc});
        const itemAtividade = cardCompetencia.locator('.atividade-associada-card-item', {hasText: ATIVIDADE_1});
        await expect(itemAtividade).toBeVisible();

        // Verifica o texto do conhecimento (fixture 1 tem 'Conhecimento fixture 1')
        await expect(itemAtividade.locator('.conhecimento-item')).toContainText(/Conhecimento fixture/i);

        await (await abrirAcaoMapa(page, 'btn-mapa-acao-disponibilizar')).click();
        await expect(page.getByText(TEXTOS.mapa.ERRO_ATIVIDADES_SEM_COMPETENCIA)).toBeVisible();

        // CT-02b: Remover a última atividade diretamente no card e manter disponibilização bloqueada
        await removerAtividadeAssociada(page, compDesc, ATIVIDADE_1);
        const cardSemAtividades = page.getByTestId('cad-mapa__card-competencia').filter({
            has: page.getByText(compDesc, {exact: true})
        });
        await expect(cardSemAtividades).toBeVisible();
        await expect(cardSemAtividades.locator('.atividade-associada-card-item')).toHaveCount(0);

        await (await abrirAcaoMapa(page, 'btn-mapa-acao-disponibilizar')).click();
        await expect(page.getByText(TEXTOS.mapa.ERRO_COMPETENCIA_SEM_ATIVIDADE)).toBeVisible();

        // CT-02c: Reassociar atividade via edição para continuar o fluxo
        await editarCompetencia(page, compDesc, compDesc, [ATIVIDADE_1]);
        await verificarCompetenciaNoMapa(page, compDesc, [ATIVIDADE_1]);

        // CT-03: Editar competência
        const newDesc = `Competência 1 Editada ${timestamp}`;
        await editarCompetencia(page, compDesc, newDesc, [ATIVIDADE_2]);
        await verificarCompetenciaNoMapa(page, newDesc, [ATIVIDADE_1, ATIVIDADE_2]);

        // CT-05: Validar cancelamento da Exclusão
        await excluirCompetenciaCancelando(page, newDesc);
        await expect(page.getByText(newDesc).first()).toBeVisible();

        // CT-04: Excluir competência com Confirmação
        await excluirCompetenciaConfirmando(page, newDesc);
        await (await abrirAcaoMapa(page, 'btn-mapa-acao-disponibilizar')).click();
        await expect(page.getByText(TEXTOS.mapa.ERRO_MAPA_SEM_COMPETENCIAS)).toBeVisible();

        // CT-06: Navegar para Disponibilização
        const compFinal = `Competência final ${timestamp}`;
        await criarCompetencia(page, compFinal, [ATIVIDADE_1, ATIVIDADE_2, ATIVIDADE_3]);
        await disponibilizarMapa(page);

        await expect(page).toHaveURL(/\/painel/);
        await verificarProcessoTabela(page, {
            descricao: descProcesso,
            situacao: 'Em andamento',
            tipo: 'Mapeamento'
        });
    });

});
