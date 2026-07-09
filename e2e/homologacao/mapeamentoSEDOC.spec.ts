import {test} from "@playwright/test";
import {ROTAS, NOTIFICACOES} from "./funcoesGerais/constantes.js";
import {
    acessarURL,
    login,
    efetuarLogout,
    obterCredenciaisUsuario,
    criarProcesso,
    acessarSubprocessoGestor,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    navegarParaCadastro,
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro,
    aceitarCadastroMapeamento,
    homologarCadastroMapeamento,
    aguardarPainelCarregado,
    aguardarProcessoNoPainel,
    verificarNotificacao,
    verificarMovimentacao,
    verificarAlertaPainel,
    calcularDataLimiteFormatada,
    navegarParaMapa,
    criarCompetencia,
    disponibilizarMapa,
    validarMapa,
    aceitarOuHomologarMapa,
    acessarDetalhesProcesso,
    finalizarProcesso,
    verificarProcessoTabela,
} from "./funcoesGerais/auxil.js";

test.describe.configure({mode: 'serial'});

test.describe('Mapeamento - SEDOC - Caminho feliz', () => {
    const descricaoProcesso = 'Processo Mapeamento SEDOC E2E';
    const siglaUnidade = 'SEDOC';
    const credenciaisAdmin = obterCredenciaisUsuario('ADMIN');
    const credenciaisChefe = obterCredenciaisUsuario('ADMIN-CHEFE-SEDOC');
    const credenciaisGestorCoede = obterCredenciaisUsuario('GESTOR-COEDE');
    const credenciaisGestorSGP = obterCredenciaisUsuario('GESTOR-SGP');
    const dataLimiteFormatada = calcularDataLimiteFormatada(15);
    const atividadeDesc = `Atividade Automática SEDOC ${Date.now()}`;
    const conhecimentoDesc = `Conhecimento Automático SEDOC ${Date.now()}`;

    test('1. ADMIN: Cria e inicia o processo de mapeamento', async ({page}) => {
        await acessarURL(page, ROTAS.LOGIN);
        await login(page, credenciaisAdmin);
        await aguardarPainelCarregado(page);

        await criarProcesso(page, {
            descricao: descricaoProcesso,
            unidades: [siglaUnidade],
            expandir: ['SGP', 'COEDE'],
            incluirInteroperacional: false,
            interoperacionais: []
        });
        await verificarNotificacao(page, {
            origem: 'ADMIN',
            destino: siglaUnidade,
            processo: descricaoProcesso,
            assunto: NOTIFICACOES.INICIO_PROCESSO_DIRETO.ASSUNTO,
            interoperacional: false,
            textosCorpo: NOTIFICACOES.INICIO_PROCESSO_DIRETO.OBTER_CORPO(descricaoProcesso, dataLimiteFormatada)
        });
        await verificarNotificacao(page, {
            origem: 'ADMIN',
            destino: 'COEDE',
            processo: descricaoProcesso,
            assunto: NOTIFICACOES.INICIO_PROCESSO_SUBORDINADO.ASSUNTO,
            interoperacional: true,
            textosCorpo: NOTIFICACOES.INICIO_PROCESSO_SUBORDINADO.OBTER_CORPO(descricaoProcesso, siglaUnidade, dataLimiteFormatada)
        });
        await verificarNotificacao(page, {
            origem: 'ADMIN',
            destino: 'SGP',
            processo: descricaoProcesso,
            assunto: NOTIFICACOES.INICIO_PROCESSO_SUBORDINADO.ASSUNTO,
            interoperacional: true,
            textosCorpo: NOTIFICACOES.INICIO_PROCESSO_SUBORDINADO.OBTER_CORPO(descricaoProcesso, siglaUnidade, dataLimiteFormatada)
        });
        await acessarURL(page, ROTAS.PAINEL);
        await aguardarPainelCarregado(page);
        await acessarSubprocessoAdmin(page, descricaoProcesso, siglaUnidade);
        await verificarMovimentacao(page, 'ADMIN', siglaUnidade, 'Processo iniciado');
        await efetuarLogout(page);
    });

    test('2. CHEFE-SEDOC: Cadastrar atividades/conhecimentos e disponibilizar o cadastro', async ({page}) => {
        await acessarURL(page, ROTAS.LOGIN);
        await login(page, credenciaisChefe);
        await aguardarProcessoNoPainel(page, descricaoProcesso);
        await verificarAlertaPainel(page, 'Início do processo', descricaoProcesso);
        await acessarSubprocessoChefeDireto(page, descricaoProcesso, siglaUnidade);
        await navegarParaCadastro(page);
        await adicionarAtividade(page, atividadeDesc);
        await adicionarConhecimento(page, atividadeDesc, conhecimentoDesc);
        await disponibilizarCadastro(page);
        await acessarSubprocessoChefeDireto(page, descricaoProcesso, siglaUnidade);
        await verificarMovimentacao(page, siglaUnidade, 'COEDE', 'Cadastro disponibilizado');
        await efetuarLogout(page);
        await verificarNotificacao(page, {
            origem: siglaUnidade,
            destino: 'COEDE',
            processo: descricaoProcesso,
            assunto: NOTIFICACOES.DISPONIBILIZACAO_CADASTRO.ASSUNTO,
            interoperacional: false,
            textosCorpo: NOTIFICACOES.DISPONIBILIZACAO_CADASTRO.OBTER_CORPO(siglaUnidade, 'COEDE')
        });
        await efetuarLogout(page);
    });

    test('3. GESTOR-COEDE: Realiza o aceite do cadastro enviado pela SEDOC', async ({page}) => {
        await acessarURL(page, ROTAS.LOGIN);
        await login(page, credenciaisGestorCoede);
        await aguardarProcessoNoPainel(page, descricaoProcesso);
        await verificarAlertaPainel(page, `Cadastro da unidade ${siglaUnidade} disponibilizado para análise`, descricaoProcesso);
        await acessarSubprocessoGestor(page, descricaoProcesso, siglaUnidade);
        await navegarParaCadastro(page);
        await aceitarCadastroMapeamento(page, 'Aceito pelo Gestor da COEDE via teste E2E.');
        await acessarSubprocessoGestor(page, descricaoProcesso, siglaUnidade);
        await verificarMovimentacao(page, 'COEDE', 'SGP', 'Cadastro aceito');
        await efetuarLogout(page);
        await verificarNotificacao(page, {
            origem: 'COEDE',
            destino: 'SGP',
            processo: descricaoProcesso,
            assunto: NOTIFICACOES.ACEITE_CADASTRO.ASSUNTO,
            interoperacional: false,
            textosCorpo: NOTIFICACOES.ACEITE_CADASTRO.OBTER_CORPO(siglaUnidade, 'SGP')
        });
        await efetuarLogout(page);
    });

    test('4. GESTOR-SGP: Realiza o aceite do cadastro enviado pela SEDOC', async ({page}) => {
        await acessarURL(page, ROTAS.LOGIN);
        await login(page, credenciaisGestorSGP);
        await aguardarProcessoNoPainel(page, descricaoProcesso);
        await verificarAlertaPainel(page, `Cadastro da unidade ${siglaUnidade} submetido para análise`, descricaoProcesso);
        await acessarSubprocessoGestor(page, descricaoProcesso, siglaUnidade);
        await navegarParaCadastro(page);
        await aceitarCadastroMapeamento(page, 'Aceito pelo Gestor do SGP via teste E2E.');
        await acessarSubprocessoGestor(page, descricaoProcesso, siglaUnidade);
        await verificarMovimentacao(page, 'SGP', 'ADMIN', 'Cadastro aceito');
        await efetuarLogout(page);
        await verificarNotificacao(page, {
            origem: 'SGP',
            destino: 'ADMIN',
            processo: descricaoProcesso,
            assunto: NOTIFICACOES.ACEITE_CADASTRO.ASSUNTO,
            interoperacional: false,
            textosCorpo: NOTIFICACOES.ACEITE_CADASTRO.OBTER_CORPO(siglaUnidade, 'ADMIN')
        });
        await efetuarLogout(page);
    });

    test('5. ADMIN: Homologa o cadastro', async ({page}) => {
        await acessarURL(page, ROTAS.LOGIN);
        await login(page, credenciaisAdmin);
        await aguardarPainelCarregado(page);
        await verificarAlertaPainel(page, `Cadastro da unidade ${siglaUnidade} submetido para análise`, descricaoProcesso);
        await acessarSubprocessoAdmin(page, descricaoProcesso, siglaUnidade);
        await navegarParaCadastro(page);
        await homologarCadastroMapeamento(page, 'Homologado pelo Admin via teste E2E.');
        await verificarMovimentacao(page, 'ADMIN', 'ADMIN', 'Cadastro homologado');
        await efetuarLogout(page);
    });

    test('6. ADMIN: Disponibiliza o mapa de competências', async ({page}) => {
        await acessarURL(page, ROTAS.LOGIN);
        await login(page, credenciaisAdmin);
        await aguardarPainelCarregado(page);
        await acessarSubprocessoAdmin(page, descricaoProcesso, siglaUnidade);
        await navegarParaMapa(page);
        await criarCompetencia(page, 'Competência Automática SEDOC', []);
        await disponibilizarMapa(page);
        await acessarSubprocessoAdmin(page, descricaoProcesso, siglaUnidade);
        await verificarMovimentacao(page, 'ADMIN', siglaUnidade, 'Mapa disponibilizado para validação');
        await efetuarLogout(page);
        await verificarNotificacao(page, {
            origem: 'ADMIN',
            destino: siglaUnidade,
            processo: descricaoProcesso,
            assunto: NOTIFICACOES.MAPA_DISPONIBILIZADO_DIRETO.ASSUNTO,
            interoperacional: false,
            textosCorpo: NOTIFICACOES.MAPA_DISPONIBILIZADO_DIRETO.OBTER_CORPO(siglaUnidade)
        });
    });

    test('7. CHEFE-SEDOC: Valida o mapa de competências', async ({page}) => {
        await acessarURL(page, ROTAS.LOGIN);
        await login(page, credenciaisChefe);
        await aguardarProcessoNoPainel(page, descricaoProcesso);
        await verificarAlertaPainel(page, `Mapa de competências da unidade ${siglaUnidade} disponibilizado para validação`, descricaoProcesso);
        await acessarSubprocessoChefeDireto(page, descricaoProcesso, siglaUnidade);
        await navegarParaMapa(page);
        await validarMapa(page);
        await acessarSubprocessoChefeDireto(page, descricaoProcesso, siglaUnidade);
        await verificarMovimentacao(page, siglaUnidade, 'COEDE', 'Mapa validado');
        await efetuarLogout(page);
        await verificarNotificacao(page, {
            origem: siglaUnidade,
            destino: 'COEDE',
            processo: descricaoProcesso,
            assunto: NOTIFICACOES.MAPA_VALIDADO.ASSUNTO,
            interoperacional: false,
            textosCorpo: NOTIFICACOES.MAPA_VALIDADO.OBTER_CORPO(siglaUnidade, 'COEDE')
        });
        await efetuarLogout(page);
    });

    test('8. GESTOR-COEDE: Valida o mapa de competências', async ({page}) => {
        await acessarURL(page, ROTAS.LOGIN);
        await login(page, credenciaisGestorCoede);
        await aguardarProcessoNoPainel(page, descricaoProcesso);
        await verificarAlertaPainel(page, `Validação do mapa de competências da unidade ${siglaUnidade} aguardando análise`, descricaoProcesso);
        await acessarSubprocessoGestor(page, descricaoProcesso, siglaUnidade);
        await aceitarOuHomologarMapa(page, 'Validação do mapa aceita pelo Gestor da COEDE via E2E.');
        await acessarSubprocessoGestor(page, descricaoProcesso, siglaUnidade);
        await verificarMovimentacao(page, 'COEDE', 'SGP', 'Validação do mapa aceita');
        await efetuarLogout(page);
        await verificarNotificacao(page, {
            origem: 'COEDE',
            destino: 'SGP',
            processo: descricaoProcesso,
            assunto: NOTIFICACOES.MAPA_ACEITO.ASSUNTO,
            interoperacional: false,
            textosCorpo: NOTIFICACOES.MAPA_ACEITO.OBTER_CORPO(siglaUnidade, 'SGP')
        });

        await efetuarLogout(page);
    });

    test('9. GESTOR-SGP: Valida o mapa de competências', async ({page}) => {
        await acessarURL(page, ROTAS.LOGIN);
        await login(page, credenciaisGestorSGP);
        await aguardarProcessoNoPainel(page, descricaoProcesso);
        await verificarAlertaPainel(page, `Validação do mapa da unidade ${siglaUnidade} submetida para análise`, descricaoProcesso);
        await acessarSubprocessoGestor(page, descricaoProcesso, siglaUnidade);
        await aceitarOuHomologarMapa(page, 'Validação do mapa aceita pelo Gestor do SGP via E2E.');
        await acessarSubprocessoGestor(page, descricaoProcesso, siglaUnidade);
        await verificarMovimentacao(page, 'SGP', 'ADMIN', 'Validação do mapa aceita');
        await efetuarLogout(page);
        await verificarNotificacao(page, {
            origem: 'SGP',
            destino: 'ADMIN',
            processo: descricaoProcesso,
            assunto: NOTIFICACOES.MAPA_ACEITO.ASSUNTO,
            interoperacional: false,
            textosCorpo: NOTIFICACOES.MAPA_ACEITO.OBTER_CORPO(siglaUnidade, 'ADMIN')
        });
        await efetuarLogout(page);
    });

    test('10. ADMIN: Homologar o mapa de competências', async ({page}) => {
        await acessarURL(page, ROTAS.LOGIN);
        await login(page, credenciaisAdmin);
        await aguardarPainelCarregado(page);
        await verificarAlertaPainel(page, `Validação do mapa da unidade ${siglaUnidade} submetida para análise`, descricaoProcesso);
        await acessarSubprocessoAdmin(page, descricaoProcesso, siglaUnidade);
        await aceitarOuHomologarMapa(page, 'Mapa homologado pelo Admin via teste E2E.');
        await acessarSubprocessoAdmin(page, descricaoProcesso, siglaUnidade);
        await verificarMovimentacao(page, 'ADMIN', 'ADMIN', 'Mapa homologado');
        await efetuarLogout(page);
    });

    test('11. ADMIN: Finalizar o processo de mapeamento', async ({page}) => {
        await acessarURL(page, ROTAS.LOGIN);
        await login(page, credenciaisAdmin);
        await aguardarPainelCarregado(page);
        await acessarDetalhesProcesso(page, descricaoProcesso);
        await finalizarProcesso(page);
        await verificarProcessoTabela(page, {
            descricao: descricaoProcesso,
            situacao: 'Finalizado',
            tipo: 'Mapeamento'
        });
        await efetuarLogout(page);
    });
});
