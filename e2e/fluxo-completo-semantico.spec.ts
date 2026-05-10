import type {Page} from '@playwright/test';
import {expect, test} from './fixtures/complete-fixtures.js';
import {
    login,
    loginComPerfil,
    reloginComPerfilSemLimparSpa,
    reloginSemLimparSpa,
    type Usuario,
    USUARIOS
} from './helpers/helpers-auth.js';
import {
    acessarDetalhesProcesso,
    criarProcesso,
    criarProcessoSimples,
    extrairProcessoCodigo,
    finalizarProcesso,
    iniciarProcesso,
    verificarDetalhesSubprocesso,
    verificarProcessoTabela
} from './helpers/helpers-processos.js';
import {resetDatabase} from './hooks/hooks-limpeza.js';
import {navegarParaSubprocesso} from './helpers/helpers-navegacao.js';
import {
    aceitarCadastroMapeamento,
    aceitarRevisao,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    homologarCadastroMapeamento
} from './helpers/helpers-analise.js';
import {
    abrirModalImpacto,
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro,
    fecharModalImpacto,
    navegarParaCadastro
} from './helpers/helpers-atividades.js';
import {
    aceitarOuHomologarMapa,
    criarCompetencia,
    disponibilizarMapa,
    editarCompetencia,
    navegarParaMapa,
    verificarCompetenciaNoMapa
} from './helpers/helpers-mapas.js';

test.describe.serial('Jornada geral semântica - mapeamento e revisão ponta a ponta', () => {
    const SIT_PROCESSO = {
        CRIADO: 'Criado',
        EM_ANDAMENTO: 'Em andamento',
        FINALIZADO: 'Finalizado'
    } as const;

    const SIT_SUBPROCESSO = {
        NAO_INICIADO: /Não iniciado/i,
        CADASTRO_DISPONIBILIZADO: /Cadastro disponibilizado/i,
        CADASTRO_HOMOLOGADO: /Cadastro homologado/i
    } as const;

    const SIT_CADASTRO_DISPONIBILIZADO = 'Cadastro disponibilizado';
    const SIT_CADASTRO_HOMOLOGADO = 'Cadastro homologado';
    const SIGLA_ADMIN = 'ADMIN';

    const SIGLA_SECAO = 'SECAO_111';
    const SIGLA_COORDENADORIA = 'COORD_11';
    const SIGLA_SECRETARIA = 'SECRETARIA_1';

    const ADMIN = USUARIOS.ADMIN_1_PERFIL;
    const CHEFE_SECAO = USUARIOS.CHEFE_SECAO_111;
    const GESTOR_COORDENADORIA = USUARIOS.GESTOR_COORD;
    const GESTOR_SECRETARIA = USUARIOS.GESTOR_SECRETARIA_1;

    const timestamp = Date.now();
    const descProcesso = `Jornada Geral Mapeamento ${timestamp}`;
    const descricaoProcessoRevisao = `Jornada Geral Revisao ${timestamp}`;
    const DESC_ATIVIDADE = 'Atividade 1';
    const DESC_CONHECIMENTO = 'Conhecimento 1.1';
    const DESC_COMPETENCIA_INICIAL = `Competência técnica base ${timestamp}`;
    const DESC_ATIVIDADE_REVISAO = `Atividade revisão ${timestamp}`;
    const DESC_CONHECIMENTO_REVISAO = 'Conhecimento revisão 1';

    let codigoProcessoMapeamento = 0;
    let codigoProcessoRevisao = 0;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
    });

    const reabrirSubprocessoSemLimparSpa = async (page: Page, options: {
        usuario: Usuario;
        perfil?: string;
        reaproveitarSessaoAtual?: boolean;
        papel: 'admin' | 'chefe' | 'gestor';
        descricaoProcesso: string;
    }) => {
        if (options.reaproveitarSessaoAtual) {
            if (options.perfil) {
                await reloginComPerfilSemLimparSpa(page, options.usuario.titulo, options.usuario.senha, options.perfil);
            } else {
                await reloginSemLimparSpa(page, options.usuario.titulo, options.usuario.senha);
            }
        } else if (options.perfil) {
            await loginComPerfil(page, options.usuario.titulo, options.usuario.senha, options.perfil);
        } else {
            await login(page, options.usuario.titulo, options.usuario.senha);
        }

        if (options.papel === 'admin') {
            await acessarSubprocessoAdmin(page, options.descricaoProcesso, SIGLA_SECAO);
        } else if (options.papel === 'gestor') {
            await acessarSubprocessoGestor(page, options.descricaoProcesso, SIGLA_SECAO);
        } else {
            await acessarSubprocessoChefeDireto(page, options.descricaoProcesso, SIGLA_SECAO);
        }
    };

    const verificarConteudoVisivelDoMapa = async (page: Page, descricaoCompetencia: string, atividades: string[]) => {
        await expect(page.getByText(descricaoCompetencia, {exact: true}).first()).toBeVisible();

        for (const atividade of atividades) {
            await expect(page.getByText(atividade, {exact: true}).first()).toBeVisible();
        }
    };

    const adminCriaEDisponibilizaMapaInicial = async (page: Page) => {
        await login(page, ADMIN.titulo, ADMIN.senha);
        await acessarSubprocessoAdmin(page, descProcesso, SIGLA_SECAO);
        codigoProcessoMapeamento = await extrairProcessoCodigo(page);

        await navegarParaMapa(page);
        await criarCompetencia(page, DESC_COMPETENCIA_INICIAL, [DESC_ATIVIDADE]);
        await disponibilizarMapa(page);
    };

    const chefeConsultaEValidaMapaDisponibilizado = async (page: Page) => {
        await reabrirSubprocessoSemLimparSpa(page, {
            usuario: CHEFE_SECAO,
            reaproveitarSessaoAtual: true,
            papel: 'chefe',
            descricaoProcesso: descProcesso
        });

        await verificarDetalhesSubprocesso(page, {
            sigla: SIGLA_SECAO,
            situacao: 'Mapa disponibilizado',
            localizacao: SIGLA_SECAO
        });

        await navegarParaMapa(page);
        await verificarConteudoVisivelDoMapa(page, DESC_COMPETENCIA_INICIAL, [DESC_ATIVIDADE]);
        await page.getByTestId('btn-mapa-acoes').click();
        await page.getByTestId('btn-mapa-acao-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();
        await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
    };

    const coordenadoriaAceitaValidacaoDoMapa = async (page: Page) => {
        await login(page, GESTOR_COORDENADORIA.titulo, GESTOR_COORDENADORIA.senha);
        await acessarSubprocessoGestor(page, descProcesso, SIGLA_SECAO);

        await verificarDetalhesSubprocesso(page, {
            sigla: SIGLA_SECAO,
            situacao: 'Mapa validado',
            localizacao: SIGLA_COORDENADORIA
        });

        await aceitarOuHomologarMapa(page, 'Aceite da validação pela coordenadoria');
    };

    const secretariaAceitaValidacaoDoMapa = async (page: Page) => {
        await reabrirSubprocessoSemLimparSpa(page, {
            usuario: GESTOR_SECRETARIA,
            perfil: GESTOR_SECRETARIA.perfil!,
            reaproveitarSessaoAtual: true,
            papel: 'gestor',
            descricaoProcesso: descProcesso
        });

        await verificarDetalhesSubprocesso(page, {
            sigla: SIGLA_SECAO,
            situacao: 'Mapa validado',
            localizacao: SIGLA_SECRETARIA
        });

        await aceitarOuHomologarMapa(page, 'Aceite da validação pela secretaria');
    };

    const adminHomologaMapaEFinalizaMapeamento = async (page: Page) => {
        await login(page, ADMIN.titulo, ADMIN.senha);
        await acessarSubprocessoAdmin(page, descProcesso, SIGLA_SECAO);
        await aceitarOuHomologarMapa(page, 'Homologação final do mapa inicial');

        await page.getByRole('link', {name: /Painel/i}).click();
        await expect(page).toHaveURL(/\/painel/);
        await acessarDetalhesProcesso(page, descProcesso);
        await finalizarProcesso(page);
        await verificarProcessoTabela(page, {
            descricao: descProcesso,
            tipo: 'Mapeamento',
            situacao: SIT_PROCESSO.FINALIZADO
        });
    };

    const adminCriaEIniciaProcessoDeRevisao = async (page: Page) => {
        await login(page, ADMIN.titulo, ADMIN.senha);

        await criarProcesso(page, {
            descricao: descricaoProcessoRevisao,
            tipo: 'REVISAO',
            unidade: SIGLA_SECAO,
            expandir: [SIGLA_SECRETARIA, SIGLA_COORDENADORIA],
            iniciar: true
        });

        await verificarProcessoTabela(page, {
            descricao: descricaoProcessoRevisao,
            tipo: 'Revisão',
            situacao: SIT_PROCESSO.EM_ANDAMENTO
        });

        await acessarSubprocessoAdmin(page, descricaoProcessoRevisao, SIGLA_SECAO);
        codigoProcessoRevisao = await extrairProcessoCodigo(page);
    };

    const chefeConfereBaseVigenteEDisponibilizaRevisao = async (page: Page) => {
        await reabrirSubprocessoSemLimparSpa(page, {
            usuario: CHEFE_SECAO,
            reaproveitarSessaoAtual: true,
            papel: 'chefe',
            descricaoProcesso: descricaoProcessoRevisao
        });

        await verificarDetalhesSubprocesso(page, {
            sigla: SIGLA_SECAO,
            situacao: 'Não iniciado',
            localizacao: SIGLA_SECAO
        });

        await navegarParaCadastro(page);
        await expect(page.getByText(DESC_ATIVIDADE, {exact: true})).toBeVisible();
        await expect(page.getByTestId('cad-atividades__btn-impactos-mapa-edicao')).toBeVisible();

        await adicionarAtividade(page, DESC_ATIVIDADE_REVISAO);
        await adicionarConhecimento(page, DESC_ATIVIDADE_REVISAO, DESC_CONHECIMENTO_REVISAO);

        await abrirModalImpacto(page);
        await expect(page.getByTestId('modal-impacto-body')).toContainText(DESC_ATIVIDADE_REVISAO);
        await fecharModalImpacto(page);

        await disponibilizarCadastro(page);
    };

    const coordenadoriaAceitaRevisaoDoCadastro = async (page: Page) => {
        await reabrirSubprocessoSemLimparSpa(page, {
            usuario: GESTOR_COORDENADORIA,
            papel: 'gestor',
            descricaoProcesso: descricaoProcessoRevisao
        });

        await verificarDetalhesSubprocesso(page, {
            sigla: SIGLA_SECAO,
            situacao: 'Revisão do cadastro disponibilizada',
            localizacao: SIGLA_COORDENADORIA
        });

        await navegarParaCadastro(page);
        await expect(page.getByTestId('cad-atividades__btn-impactos-mapa-edicao')).toBeVisible();
        await abrirModalImpacto(page);
        await expect(page.getByTestId('modal-impacto-body')).toContainText(DESC_ATIVIDADE_REVISAO);
        await fecharModalImpacto(page);
        await aceitarRevisao(page, 'Aceite da revisão pela coordenadoria');
    };

    const secretariaAceitaRevisaoDoCadastro = async (page: Page) => {
        await reabrirSubprocessoSemLimparSpa(page, {
            usuario: GESTOR_SECRETARIA,
            perfil: GESTOR_SECRETARIA.perfil!,
            reaproveitarSessaoAtual: true,
            papel: 'gestor',
            descricaoProcesso: descricaoProcessoRevisao
        });

        await verificarDetalhesSubprocesso(page, {
            sigla: SIGLA_SECAO,
            situacao: 'Revisão do cadastro disponibilizada',
            localizacao: SIGLA_SECRETARIA
        });

        await navegarParaCadastro(page);
        await aceitarRevisao(page, 'Aceite da revisão pela secretaria');
    };

    const adminHomologaCadastroRevisado = async (page: Page) => {
        await login(page, ADMIN.titulo, ADMIN.senha);
        await acessarSubprocessoAdmin(page, descricaoProcessoRevisao, SIGLA_SECAO);

        await verificarDetalhesSubprocesso(page, {
            sigla: SIGLA_SECAO,
            situacao: 'Revisão do cadastro disponibilizada',
            localizacao: SIGLA_ADMIN
        });

        await navegarParaCadastro(page);
        await expect(page.getByTestId('cad-atividades__btn-impactos-mapa-edicao')).toBeVisible();
        await abrirModalImpacto(page);
        await expect(page.getByTestId('modal-impacto-body')).toContainText(DESC_ATIVIDADE_REVISAO);
        await fecharModalImpacto(page);
        await homologarCadastroMapeamento(page, 'Cadastro revisado homologado');

        await verificarDetalhesSubprocesso(page, {
            sigla: SIGLA_SECAO,
            situacao: 'Revisão do cadastro homologada',
            localizacao: SIGLA_ADMIN
        });
    };

    const adminAjustaEDisponibilizaMapaRevisado = async (page: Page) => {
        await login(page, ADMIN.titulo, ADMIN.senha);
        await acessarSubprocessoAdmin(page, descricaoProcessoRevisao, SIGLA_SECAO);

        await navegarParaMapa(page);
        await page.getByTestId('cad-mapa__btn-impactos-mapa').click();
        await expect(page.getByTestId('modal-impacto-body')).toContainText(DESC_ATIVIDADE_REVISAO);
        await page.getByTestId('btn-fechar-impacto').click();

        await editarCompetencia(page, DESC_COMPETENCIA_INICIAL, DESC_COMPETENCIA_INICIAL, [DESC_ATIVIDADE_REVISAO]);
        await verificarCompetenciaNoMapa(page, DESC_COMPETENCIA_INICIAL, [DESC_ATIVIDADE, DESC_ATIVIDADE_REVISAO]);

        await page.getByRole('link', {name: SIGLA_SECAO, exact: true}).click();
        await expect(page.getByTestId('header-subprocesso')).toBeVisible();

        await navegarParaMapa(page);
        await verificarCompetenciaNoMapa(page, DESC_COMPETENCIA_INICIAL, [DESC_ATIVIDADE, DESC_ATIVIDADE_REVISAO]);
        await disponibilizarMapa(page);
    };

    const chefeValidaMapaRevisado = async (page: Page) => {
        await reabrirSubprocessoSemLimparSpa(page, {
            usuario: CHEFE_SECAO,
            reaproveitarSessaoAtual: true,
            papel: 'chefe',
            descricaoProcesso: descricaoProcessoRevisao
        });

        await verificarDetalhesSubprocesso(page, {
            sigla: SIGLA_SECAO,
            situacao: 'Mapa disponibilizado',
            localizacao: SIGLA_SECAO
        });

        await navegarParaMapa(page);
        await verificarConteudoVisivelDoMapa(page, DESC_COMPETENCIA_INICIAL, [DESC_ATIVIDADE, DESC_ATIVIDADE_REVISAO]);
        await page.getByTestId('btn-mapa-acoes').click();
        await page.getByTestId('btn-mapa-acao-validar').click();
        await page.getByTestId('btn-validar-mapa-confirmar').click();
        await expect(page).toHaveURL(/\/painel(?:\?.*)?$/);
    };

    const coordenadoriaESecretariaAceitamMapaRevisado = async (page: Page) => {
        await login(page, GESTOR_COORDENADORIA.titulo, GESTOR_COORDENADORIA.senha);
        await acessarSubprocessoGestor(page, descricaoProcessoRevisao, SIGLA_SECAO);
        await aceitarOuHomologarMapa(page, 'Aceite do mapa revisado pela coordenadoria');

        await reabrirSubprocessoSemLimparSpa(page, {
            usuario: GESTOR_SECRETARIA,
            perfil: GESTOR_SECRETARIA.perfil!,
            reaproveitarSessaoAtual: true,
            papel: 'gestor',
            descricaoProcesso: descricaoProcessoRevisao
        });
        await aceitarOuHomologarMapa(page, 'Aceite do mapa revisado pela secretaria');
    };

    const adminHomologaMapaRevisado = async (page: Page) => {
        await login(page, ADMIN.titulo, ADMIN.senha);
        await acessarSubprocessoAdmin(page, descricaoProcessoRevisao, SIGLA_SECAO);
        await aceitarOuHomologarMapa(page, 'Homologação final do mapa revisado');

        await acessarSubprocessoAdmin(page, descricaoProcessoRevisao, SIGLA_SECAO);
        await verificarDetalhesSubprocesso(page, {
            sigla: SIGLA_SECAO,
            situacao: 'Mapa homologado',
            localizacao: SIGLA_ADMIN
        });
    };

    const consultarResultadoFinalComCacheQuente = async (page: Page, options: {
        usuario: Usuario;
        perfil?: string;
    }) => {
        await reabrirSubprocessoSemLimparSpa(page, {
            usuario: options.usuario,
            perfil: options.perfil
            ,
            papel: options.perfil ? 'gestor' : options.usuario === ADMIN ? 'admin' : options.usuario === CHEFE_SECAO ? 'chefe' : 'gestor',
            descricaoProcesso: descricaoProcessoRevisao
        });

        await verificarDetalhesSubprocesso(page, {
            sigla: SIGLA_SECAO,
            situacao: 'Mapa homologado'
        });

        await navegarParaMapa(page);
        await expect(page.getByTestId('btn-abrir-criar-competencia')).toBeHidden();
        await verificarConteudoVisivelDoMapa(page, DESC_COMPETENCIA_INICIAL, [DESC_ATIVIDADE, DESC_ATIVIDADE_REVISAO]);
    };

    test('Fase 1 - ADMIN cria e inicia o processo de mapeamento da seção', async ({page}) => {
        // O ADMIN faz login.
        await login(page, ADMIN.titulo, ADMIN.senha);

        // O ADMIN cria um novo processo de mapeamento com uma unidade.
        await criarProcessoSimples(page, {
            descricao: descProcesso, tipo: 'MAPEAMENTO', unidades: [SIGLA_SECAO]
        });

        // Após criar, o sistema retorna ao Painel.
        await expect(page).toHaveURL(/\/painel/);

        // No Painel, o processo deve aparecer ainda na situação inicial "Criado".
        await verificarProcessoTabela(page, {
            descricao: descProcesso, tipo: 'Mapeamento', situacao: SIT_PROCESSO.CRIADO
        });

        // O ADMIN abre os detalhes do processo recém-criado.
        await acessarDetalhesProcesso(page, descProcesso);

        // O ADMIN inicia o processo.
        await iniciarProcesso(page, descProcesso);

        // Depois da iniciação, o sistema retorna ao Painel.
        await expect(page).toHaveURL(/\/painel/);

        // No Painel, o processo deve aparecer como "Em andamento".
        await verificarProcessoTabela(page, {
            descricao: descProcesso, tipo: 'Mapeamento', situacao: SIT_PROCESSO.EM_ANDAMENTO
        });

        // O ADMIN reabre os detalhes do processo iniciado.
        await acessarDetalhesProcesso(page, descProcesso);

        // O ADMIN abre os detalhes do subprocesso criado para a seção participante.
        await navegarParaSubprocesso(page, SIGLA_SECAO);

        // O subprocesso da unidade deve existir, estar "Não iniciado" e localizado na própria unidade.
        await expect(page.getByTestId('header-subprocesso')).toBeVisible();
        await expect(page.getByTestId('subprocesso-header__txt-header-unidade')).toContainText(SIGLA_SECAO);
        await expect(page.getByTestId('subprocesso-header__txt-situacao')).toHaveText(SIT_SUBPROCESSO.NAO_INICIADO);
        await expect(page.getByTestId('subprocesso-header__txt-localizacao')).toContainText(SIGLA_SECAO);
    });

    test('Fase 2 - CHEFE cadastra atividades e a hierarquia analisa o cadastro', async ({page}) => {
        // O CHEFE da seção faz login.
        await login(page, CHEFE_SECAO.titulo, CHEFE_SECAO.senha);

        // O CHEFE abre o subprocesso da sua unidade.
        await acessarSubprocessoChefeDireto(page, descProcesso, SIGLA_SECAO);

        // O subprocesso deve começar "Não iniciado" e permanecer localizado na própria seção.
        await verificarDetalhesSubprocesso(page, {
            sigla: SIGLA_SECAO,
            situacao: 'Não iniciado',
            localizacao: SIGLA_SECAO
        });

        // O CHEFE abre a tela de atividades e conhecimentos.
        await navegarParaCadastro(page);

        // O CHEFE registra uma atividade e um conhecimento da unidade.
        await adicionarAtividade(page, DESC_ATIVIDADE);
        await adicionarConhecimento(page, DESC_ATIVIDADE, DESC_CONHECIMENTO);

        // O CHEFE disponibiliza o cadastro para análise das unidades superiores.
        await disponibilizarCadastro(page);

        // O GESTOR da coordenadoria faz login.
        await login(page, GESTOR_COORDENADORIA.titulo, GESTOR_COORDENADORIA.senha);

        // O GESTOR da coordenadoria abre o subprocesso da seção.
        await acessarSubprocessoGestor(page, descProcesso, SIGLA_SECAO);


        // O cadastro deve chegar à coordenadoria já disponibilizado para análise.
        await verificarDetalhesSubprocesso(page, {
            sigla: SIGLA_SECAO,
            situacao: SIT_CADASTRO_DISPONIBILIZADO,
            localizacao: SIGLA_COORDENADORIA
        });

        // O GESTOR da coordenadoria abre a tela de atividades e registra aceite.
        await navegarParaCadastro(page);
        await aceitarCadastroMapeamento(page, 'Aceite da COORD_11');

        // O GESTOR da secretaria faz login.
        await loginComPerfil(page, GESTOR_SECRETARIA.titulo, GESTOR_SECRETARIA.senha, GESTOR_SECRETARIA.perfil!);

        // O GESTOR da secretaria abre o subprocesso da mesma seção.
        await acessarSubprocessoGestor(page, descProcesso, SIGLA_SECAO);

        // O cadastro deve subir para a secretaria ainda como item pendente de análise.
        await verificarDetalhesSubprocesso(page, {
            sigla: SIGLA_SECAO,
            situacao: SIT_CADASTRO_DISPONIBILIZADO,
            localizacao: SIGLA_SECRETARIA
        });

        // O GESTOR da secretaria abre a tela de atividades e registra aceite.
        await navegarParaCadastro(page);
        await aceitarCadastroMapeamento(page, 'Obs. cadastro aceito');

        // O ADMIN faz login.
        await login(page, ADMIN.titulo, ADMIN.senha);

        // O ADMIN abre o subprocesso da seção.
        await acessarSubprocessoAdmin(page, descProcesso, SIGLA_SECAO);

        // O cadastro deve chegar ao ADMIN ainda disponibilizado para homologação final.
        await verificarDetalhesSubprocesso(page, {
            sigla: SIGLA_SECAO,
            situacao: SIT_CADASTRO_DISPONIBILIZADO,
            localizacao: SIGLA_ADMIN
        });

        // O ADMIN abre a tela de atividades e homologa o cadastro.
        await navegarParaCadastro(page);
        await homologarCadastroMapeamento(page, "Cadastro homologado OK");

        // Após homologar, o subprocesso deve indicar cadastro homologado.
        await verificarDetalhesSubprocesso(page, {
            sigla: SIGLA_SECAO,
            situacao: SIT_CADASTRO_HOMOLOGADO,
            localizacao: SIGLA_ADMIN
        });

        // Após a homologação, o card de mapa deve ficar disponível para a próxima etapa.
        await expect(page.getByTestId('card-subprocesso-mapa')).toBeVisible();
    });

    test('Fase 3 - ADMIN cria o mapa do mapeamento e a hierarquia valida até a homologação', async ({page}) => {
        await test.step('ADMIN estrutura e disponibiliza o primeiro mapa da seção', async () => {
            await adminCriaEDisponibilizaMapaInicial(page);
        });

        await test.step('CHEFE valida o mapa disponibilizado sem perder o contexto carregado da SPA', async () => {
            await chefeConsultaEValidaMapaDisponibilizado(page);
        });

        await test.step('COORDENADORIA e SECRETARIA registram os dois aceites hierárquicos', async () => {
            await coordenadoriaAceitaValidacaoDoMapa(page);
            await secretariaAceitaValidacaoDoMapa(page);
        });

        await test.step('ADMIN homologa o mapa e encerra o processo de mapeamento', async () => {
            await adminHomologaMapaEFinalizaMapeamento(page);
        });
    });

    test('Fase 4 - ADMIN cria e inicia o processo de revisão da mesma seção', async ({page}) => {
        await test.step('ADMIN abre a revisão já apoiada no mapa vigente da seção', async () => {
            await adminCriaEIniciaProcessoDeRevisao(page);
        });

        await test.step('CHEFE confirma que a revisão nasce com o cadastro vigente e botão de impactos disponível', async () => {
            await chefeConfereBaseVigenteEDisponibilizaRevisao(page);
        });
    });

    test('Fase 5 - CHEFE revisa o cadastro com impacto real e a hierarquia homologa a revisão', async ({page}) => {
        await test.step('COORDENADORIA e SECRETARIA aceitam a revisão do cadastro já com impacto visível', async () => {
            await coordenadoriaAceitaRevisaoDoCadastro(page);
            await secretariaAceitaRevisaoDoCadastro(page);
        });

        await test.step('ADMIN homologa o cadastro revisado e libera o ajuste do mapa', async () => {
            await adminHomologaCadastroRevisado(page);
        });
    });

    test('Fase 6 - ADMIN ajusta o mapa da revisão e a hierarquia valida novamente', async ({page}) => {
        await test.step('ADMIN ajusta o mapa revisado e confirma a persistência do novo vínculo ao reentrar na tela', async () => {
            await adminAjustaEDisponibilizaMapaRevisado(page);
        });

        await test.step('CHEFE valida e a hierarquia gestora aceita o mapa revisado', async () => {
            await chefeValidaMapaRevisado(page);
            await coordenadoriaESecretariaAceitamMapaRevisado(page);
        });

        await test.step('ADMIN homologa o mapa revisado', async () => {
            await adminHomologaMapaRevisado(page);
        });
    });

    test('Fase 7 - ADMIN finaliza a revisão e os perfis consultam o resultado final', async ({page}) => {
        await test.step('ADMIN encerra a revisão e confirma o processo finalizado no painel', async () => {
            await login(page, ADMIN.titulo, ADMIN.senha);
            await acessarDetalhesProcesso(page, descricaoProcessoRevisao);
            await finalizarProcesso(page);
            await verificarProcessoTabela(page, {
                descricao: descricaoProcessoRevisao,
                tipo: 'Revisão',
                situacao: SIT_PROCESSO.FINALIZADO
            });
        });

        await test.step('Os perfis principais consultam o mapa final no mesmo contexto quente da SPA', async () => {
            await consultarResultadoFinalComCacheQuente(page, {usuario: ADMIN});
            await consultarResultadoFinalComCacheQuente(page, {usuario: CHEFE_SECAO});
            await consultarResultadoFinalComCacheQuente(page, {usuario: GESTOR_COORDENADORIA});
            await consultarResultadoFinalComCacheQuente(page, {
                usuario: GESTOR_SECRETARIA,
                perfil: GESTOR_SECRETARIA.perfil!
            });
        });
    });
});
