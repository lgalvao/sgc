import {test} from './fixtures/complete-fixtures.js';
import {USUARIOS} from './helpers/helpers-auth.js';
import {resetDatabase} from './hooks/hooks-limpeza.js';

test.describe.serial('Jornada geral semântica - mapeamento e revisão ponta a ponta', () => {
    const SIGLA_SECAO = 'SECAO_111';
    const SIGLA_COORDENADORIA = 'COORD_11';
    const SIGLA_SECRETARIA = 'SECRETARIA_1';

    const ADMIN = USUARIOS.ADMIN_1_PERFIL;
    const CHEFE_SECAO = USUARIOS.CHEFE_SECAO_111;
    const GESTOR_COORDENADORIA = USUARIOS.GESTOR_COORD;
    const GESTOR_SECRETARIA = USUARIOS.GESTOR_SECRETARIA_1;

    const timestamp = Date.now();
    const descricaoProcessoMapeamento = `Jornada Geral Mapeamento ${timestamp}`;
    const descricaoProcessoRevisao = `Jornada Geral Revisao ${timestamp}`;

    test.beforeAll(async ({request}) => {
        await resetDatabase(request);
    });

    test('Fase 1 - ADMIN cria e inicia o processo de mapeamento da seção', async () => {
        void ADMIN;
        void SIGLA_SECAO;
        void descricaoProcessoMapeamento;

        test.fixme('Implementar criação e início do processo de mapeamento para a SECAO_111.');

        // Estrutura prevista:
        // 1. Login como ADMIN.
        // 2. Criar processo do tipo MAPEAMENTO para SECAO_111.
        // 3. Iniciar processo.
        // 4. Validar processo em andamento e subprocesso em "Não iniciado".
    });

    test('Fase 2 - CHEFE cadastra atividades e a hierarquia analisa o cadastro', async () => {
        void CHEFE_SECAO;
        void GESTOR_COORDENADORIA;
        void GESTOR_SECRETARIA;
        void SIGLA_COORDENADORIA;
        void SIGLA_SECRETARIA;

        test.fixme('Implementar cadastro inicial da SECAO_111, disponibilização e dois níveis de aceite antes da homologação do ADMIN.');

        // Estrutura prevista:
        // 1. CHEFE_SECAO_111 cadastra atividades e conhecimentos.
        // 2. CHEFE_SECAO_111 disponibiliza o cadastro.
        // 3. GESTOR_COORD registra primeiro aceite.
        // 4. GESTOR_SECRETARIA_1 registra segundo aceite.
        // 5. ADMIN homologa o cadastro.
    });

    test('Fase 3 - ADMIN cria o mapa do mapeamento e a hierarquia valida até a homologação', async () => {
        test.fixme('Implementar criação do mapa, disponibilização, validação do CHEFE, dois aceites de GESTOR e homologação final.');

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

        test.fixme('Implementar criação e início do processo de revisão usando a mesma SECAO_111 já mapeada.');

        // Estrutura prevista:
        // 1. Login como ADMIN.
        // 2. Criar processo do tipo REVISAO para SECAO_111.
        // 3. Iniciar processo.
        // 4. Validar que a revisão parte de mapa vigente.
    });

    test('Fase 5 - CHEFE revisa o cadastro com impacto real e a hierarquia homologa a revisão', async () => {
        test.fixme('Implementar alteração com impacto real no mapa, disponibilização da revisão e homologação do cadastro revisado.');

        // Estrutura prevista:
        // 1. CHEFE_SECAO_111 altera cadastro para gerar impacto real.
        // 2. CHEFE_SECAO_111 consulta impactos no mapa.
        // 3. CHEFE_SECAO_111 disponibiliza a revisão.
        // 4. GESTOR_COORD registra primeiro aceite.
        // 5. GESTOR_SECRETARIA_1 registra segundo aceite.
        // 6. ADMIN homologa a revisão do cadastro.
    });

    test('Fase 6 - ADMIN ajusta o mapa da revisão e a hierarquia valida novamente', async () => {
        test.fixme('Implementar ajuste do mapa revisado, disponibilização, validação e homologação final do mapa.');

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
        test.fixme('Implementar finalização do processo de revisão e consulta final pelos perfis principais.');

        // Estrutura prevista:
        // 1. ADMIN finaliza o processo de revisão.
        // 2. Validar processo finalizado.
        // 3. Validar mapa vigente atualizado da SECAO_111.
        // 4. Validar que CHEFE_SECAO_111, GESTOR_COORD e GESTOR_SECRETARIA_1 conseguem consultar o resultado final.
    });
});
