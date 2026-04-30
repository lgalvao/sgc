import {expect, test} from './fixtures/complete-fixtures.js';
import {login, loginComPerfil, USUARIOS} from './helpers/helpers-auth.js';
import {
    acessarDetalhesProcesso,
    criarProcessoSimples,
    iniciarProcesso,
    verificarDetalhesSubprocesso,
    verificarProcessoTabela
} from './helpers/helpers-processos.js';
import {resetDatabase} from './hooks/hooks-limpeza.js';
import {navegarParaSubprocesso} from './helpers/helpers-navegacao.js';
import {
    aceitarCadastroMapeamento,
    acessarSubprocessoAdmin,
    acessarSubprocessoChefeDireto,
    acessarSubprocessoGestor,
    homologarCadastroMapeamento
} from './helpers/helpers-analise.js';
import {
    adicionarAtividade,
    adicionarConhecimento,
    disponibilizarCadastro,
    navegarParaCadastro
} from './helpers/helpers-atividades.js';

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

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
    });

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
        const DESC_ATIVIDADE = 'Atividade 1';
        const DESC_CONHECIMENTO = 'Conhecimento 1.1';

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

    test('Fase 3 - ADMIN cria o mapa do mapeamento e a hierarquia valida até a homologação', async () => {
        test.fixme(true, 'Implementar criação do mapa, disponibilização, validação do CHEFE, dois aceites de GESTOR e homologação final.');
        expect(true).toBe(true);

        // Estrutura prevista:
        // 1. ADMIN cria competência(s) para a SECAO_111.
        // 2. ADMIN disponibiliza o mapa.
        // 3. CHEFE_SECAO_111 valida o mapa.
        // 4. GESTOR_COORD registra aceite da validação.
        // 5. GESTOR_SECRETARIA_1 registra segundo aceite.
        // 6. ADMIN homologa o mapa.
        // 7. ADMIN finaliza o processo de mapeamento.
    });

    test('Fase 4 - ADMIN cria e inicia o processo de revisão da mesma seção', async () => {
        void descricaoProcessoRevisao;

        test.fixme(true, 'Implementar criação e início do processo de revisão usando a mesma SECAO_111 já mapeada.');
        expect(true).toBe(true);

        // Estrutura prevista:
        // 1. Login como ADMIN.
        // 2. Criar processo do tipo REVISAO para SECAO_111.
        // 3. Iniciar processo.
        // 4. Validar que a revisão parte de mapa vigente.
    });

    test('Fase 5 - CHEFE revisa o cadastro com impacto real e a hierarquia homologa a revisão', async () => {
        test.fixme(true, 'Implementar alteração com impacto real no mapa, disponibilização da revisão e homologação do cadastro revisado.');
        expect(true).toBe(true);

        // Estrutura prevista:
        // 1. CHEFE_SECAO_111 altera cadastro para gerar impacto real.
        // 2. CHEFE_SECAO_111 consulta impactos no mapa.
        // 3. CHEFE_SECAO_111 disponibiliza a revisão.
        // 4. GESTOR_COORD registra primeiro aceite.
        // 5. GESTOR_SECRETARIA_1 registra segundo aceite.
        // 6. ADMIN homologa a revisão do cadastro.
    });

    test('Fase 6 - ADMIN ajusta o mapa da revisão e a hierarquia valida novamente', async () => {
        test.fixme(true, 'Implementar ajuste do mapa revisado, disponibilização, validação e homologação final do mapa.');
        expect(true).toBe(true);

        // Estrutura prevista:
        // 1. ADMIN abre impactos no mapa.
        // 2. ADMIN ajusta o mapa para refletir a revisão.
        // 3. ADMIN disponibiliza o mapa revisado.
        // 4. CHEFE_SECAO_111 valida o mapa revisado.
        // 5. GESTOR_COORD registra primeiro aceite.
        // 6. GESTOR_SECRETARIA_1 registra segundo aceite.
        // 7. ADMIN homologa o mapa revisado.
    });

    test('Fase 7 - ADMIN finaliza a revisão e os perfis consultam o resultado final', async () => {
        test.fixme(true, 'Implementar finalização do processo de revisão e consulta final pelos perfis principais.');
        expect(true).toBe(true);

        // Estrutura prevista:
        // 1. ADMIN finaliza o processo de revisão.
        // 2. Validar processo finalizado.
        // 3. Validar mapa vigente atualizado da SECAO_111.
        // 4. Validar que CHEFE_SECAO_111, GESTOR_COORD e GESTOR_SECRETARIA_1 conseguem consultar o resultado final.
    });
});
