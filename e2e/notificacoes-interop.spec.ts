import {expect, test} from './fixtures/complete-fixtures.js';
import {login, USUARIOS} from './helpers/helpers-auth.js';
import {criarProcesso} from './helpers/helpers-processos.js';
import {verificarNotificacaoAdmin} from './helpers/helpers-notificacoes-admin.js';

test.describe('Regressão - notificações de início para unidade interoperacional', () => {
    test('deve gerar notificações separadas para a unidade participante e para as subordinadas da interoperacional', async ({
        _resetAutomatico,
        page
    }) => {
        const descricao = `Regressão interoperacional ${Date.now()}`;

        await login(page, USUARIOS.ADMIN_1_PERFIL.titulo, USUARIOS.ADMIN_1_PERFIL.senha);

        await criarProcesso(page, {
            descricao,
            tipo: 'MAPEAMENTO',
            diasLimite: 30,
            unidadesComEquipePropriaParticipantes: ['SECRETARIA_2'],
            unidade: 'ASSESSORIA_21',
            expandir: ['SECRETARIA_2'],
            iniciar: true
        });

        await verificarNotificacaoAdmin(page, {
            destinatario: 'ASSESSORIA_21',
            assunto: /^Início de processo de mapeamento de competências$/i,
            tipo: 'Início do processo',
            trechoCorpo: new RegExp(`Comunicamos o início do processo\\s+${descricao}\\s+para a sua unidade`, 'i')
        });

        await verificarNotificacaoAdmin(page, {
            destinatario: 'SECRETARIA_2',
            assunto: /^Início de processo de mapeamento de competências$/i,
            tipo: 'Início do processo',
            trechoCorpo: new RegExp(`Comunicamos o início do processo\\s+${descricao}\\s+para a sua unidade`, 'i')
        });

        await verificarNotificacaoAdmin(page, {
            destinatario: 'SECRETARIA_2',
            assunto: /^Início de processo de mapeamento de competências em unidades subordinadas$/i,
            tipo: 'Início do processo',
            trechoCorpo: /Estas unidades já podem iniciar o cadastro de atividades e conhecimentos/i
        });
    });
});
