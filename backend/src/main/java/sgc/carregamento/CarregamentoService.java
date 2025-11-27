package sgc.carregamento;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuarioRepo;
import sgc.analise.model.AnaliseRepo;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.notificacao.model.NotificacaoRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioPerfil;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.model.*;
import sgc.unidade.model.*;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CarregamentoService {
    private final AlertaRepo alertaRepo;
    private final AlertaUsuarioRepo alertaUsuarioRepo;
    private final AnaliseRepo analiseRepo;
    private final AtividadeRepo atividadeRepo;
    private final AtribuicaoTemporariaRepo atribuicaoTemporariaRepo;
    private final CompetenciaRepo competenciaRepo;
    private final ConhecimentoRepo conhecimentoRepo;
    private final MapaRepo mapaRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final NotificacaoRepo notificacaoRepo;
    private final ProcessoRepo processoRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final UnidadeRepo unidadeRepo;
    private final UsuarioRepo usuarioRepo;
    private final VinculacaoUnidadeRepo vinculacaoUnidadeRepo;

    @Transactional
    public void carregarDados() {
        Mapa m1001 = mapaRepo.save(new Mapa());
        Mapa m1002 = mapaRepo.save(new Mapa());
        Mapa m1003 = mapaRepo.save(new Mapa());
        Mapa m1004 = mapaRepo.save(new Mapa());
        Mapa m201 = mapaRepo.save(new Mapa());

        Unidade u1 = unidadeRepo.save(new Unidade(null, "Tribunal Regional Eleitoral", "TRE", TipoUnidade.INTEROPERACIONAL, SituacaoUnidade.ATIVA, null));
        Unidade u2 = unidadeRepo.save(new Unidade(null, "Secretaria de Informática e Comunicações", "STIC", TipoUnidade.INTEROPERACIONAL, SituacaoUnidade.ATIVA, null));
        Unidade u100 = unidadeRepo.save(new Unidade(null, "ADMIN-UNIT", "ADMIN-UNIT", TipoUnidade.INTEROPERACIONAL, SituacaoUnidade.ATIVA, null));
        Unidade u200 = unidadeRepo.save(new Unidade(null, "Secretaria de Gestao de Pessoas", "SGP", TipoUnidade.INTERMEDIARIA, SituacaoUnidade.ATIVA, null));

        Unidade u3 = unidadeRepo.save(new Unidade(null, "Coordenadoria de Administracao", "COAD", TipoUnidade.INTERMEDIARIA, SituacaoUnidade.ATIVA, u2));
        Unidade u6 = unidadeRepo.save(new Unidade(null, "Coordenadoria de Sistemas", "COSIS", TipoUnidade.INTERMEDIARIA, SituacaoUnidade.ATIVA, u2));
        Unidade u7 = unidadeRepo.save(new Unidade(null, "Coordenadoria de Suporte e Infraestrutura", "COSINF", TipoUnidade.INTERMEDIARIA, SituacaoUnidade.ATIVA, u2));
        Unidade u14 = unidadeRepo.save(new Unidade(null, "Coordenadoria Jurídica", "COJUR", TipoUnidade.INTERMEDIARIA, SituacaoUnidade.ATIVA, u2));

        Unidade u101 = unidadeRepo.save(new Unidade(null, "GESTOR-UNIT", "GESTOR-UNIT", TipoUnidade.INTERMEDIARIA, SituacaoUnidade.ATIVA, u100));

        Unidade u201 = unidadeRepo.save(new Unidade(null, "Coordenadoria de Atenção ao Servidor", "CAS", TipoUnidade.INTEROPERACIONAL, SituacaoUnidade.ATIVA, u200));

        Unidade u4 = unidadeRepo.save(new Unidade(null, "Coordenadoria de Educação Especial", "COEDE", TipoUnidade.INTERMEDIARIA, SituacaoUnidade.ATIVA, u3));

        Unidade u5 = unidadeRepo.save(new Unidade(null, "Seção Magistrados e Requisitados", "SEMARE", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, u4));

        Unidade u8 = unidadeRepo.save(new Unidade(null, "Seção de Desenvolvimento de Sistemas", "SEDESENV", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, u6));
        Unidade u9 = unidadeRepo.save(new Unidade(null, "Seção de Dados e Inteligência Artificial", "SEDIA", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, u6));
        Unidade u10 = unidadeRepo.save(new Unidade(null, "Seção de Sistemas Eleitorais", "SESEL", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, u6));

        Unidade u11 = unidadeRepo.save(new Unidade(null, "Seção de Infraestrutura", "SENIC", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, u7));
        Unidade u12 = unidadeRepo.save(new Unidade(null, "Seção Jurídica", "SEJUR", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, u14));
        Unidade u102 = unidadeRepo.save(new Unidade(null, "SUB-UNIT", "SUB-UNIT", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, u101));

        unidadeRepo.save(new Unidade(null, "CDU04-UNIT", "CDU04-UNIT", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, u2));
        unidadeRepo.save(new Unidade(null, "CDU05-REV-UNIT", "CDU05-REV-UNIT", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, u2));
        unidadeRepo.save(new Unidade(null, "CDU05-SUB-UNIT", "CDU05-SUB-UNIT", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, u2));
        unidadeRepo.save(new Unidade(null, "CDU05-ALERT-UNIT", "CDU05-ALERT-UNIT", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, u2));
        unidadeRepo.save(new Unidade(null, "CDU05-READONLY-UNIT", "CDU05-READONLY-UNIT", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, u2));

        Usuario user1 = usuarioRepo.save(new Usuario("1", "Ana Paula Souza", "ana.souza@tre-pe.jus.br", "1234", u10));
        Usuario user2 = usuarioRepo.save(new Usuario("2", "Carlos Henrique Lima", "carlos.lima@tre-pe.jus.br", "2345", u200));
        Usuario user3 = usuarioRepo.save(new Usuario("3", "Fernanda Oliveira", "fernanda.oliveira@tre-pe.jus.br", "3456", u8));
        usuarioRepo.save(new Usuario("4", "João Batista Silva", "joao.silva@tre-pe.jus.br", "4567", u10));
        usuarioRepo.save(new Usuario("5", "Marina Dias", "marina.dias@tre-pe.jus.br", "5678", u5));
        Usuario user6 = usuarioRepo.save(new Usuario("6", "Ricardo Alves", "ricardo.alves@tre-pe.jus.br", "6789", u2));
        usuarioRepo.save(new Usuario("7", "Zeca Silva", "zeca.gado@tre-pe.jus.br", "7001", u2));
        Usuario user8 = usuarioRepo.save(new Usuario("8", "Paulo Horta", "paulo.horta@tre-pe.jus.br", "7002", u8));
        usuarioRepo.save(new Usuario("9", "Giuseppe Corleone", "giuseppe.corleone@tre-pe.jus.br", "7003", u8));
        usuarioRepo.save(new Usuario("10", "Paula Gonçalves", "paula.goncalves@tre-pe.jus.br", "7004", u9));
        usuarioRepo.save(new Usuario("11", "Herman Greely", "herman.greely@tre-pe.jus.br", "7005", u11));
        Usuario user12 = usuarioRepo.save(new Usuario("12", "Taís Condida", "tais.condida@tre-pe.jus.br", "7006", u11));
        usuarioRepo.save(new Usuario("13", "Mike Smith", "mike.smith@tre-pe.jus.br", "7007", u11));
        usuarioRepo.save(new Usuario("14", "Maroca Silva", "maroca.silva@tre-pe.jus.br", "7008", u2));
        usuarioRepo.save(new Usuario("15", "Roberto Santos", "roberto.santos@tre-pe.jus.br", "7009", u2));
        usuarioRepo.save(new Usuario("16", "Luciana Pereira", "luciana.pereira@tre-pe.jus.br", "7010", u6));
        usuarioRepo.save(new Usuario("17", "Fernando Costa", "fernando.costa@tre-pe.jus.br", "7011", u10));
        usuarioRepo.save(new Usuario("18", "Amanda Rodrigues", "amanda.rodrigues@tre-pe.jus.br", "7012", u14));
        usuarioRepo.save(new Usuario("19", "Diego Fernandes", "diego.fernandes@tre-pe.jus.br", "7013", u6));
        usuarioRepo.save(new Usuario("20", "Juliana Almeida", "juliana.almeida@tre-pe.jus.br", "7014", u2));
        usuarioRepo.save(new Usuario("21", "Rafael Moreira", "rafael.moreira@tre-pe.jus.br", "7015", u2));
        usuarioRepo.save(new Usuario("22", "Camila Barbosa", "camila.barbosa@tre-pe.jus.br", "7016", u10));
        usuarioRepo.save(new Usuario("23", "Thiago Carvalho", "thiago.carvalho@tre-pe.jus.br", "7017", u14));
        usuarioRepo.save(new Usuario("24", "Patrícia Lima", "patricia.lima@tre-pe.jus.br", "7018", u6));
        usuarioRepo.save(new Usuario("25", "Lucas Mendes", "lucas.mendes@tre-pe.jus.br", "7019", u2));
        usuarioRepo.save(new Usuario("26", "Beatriz Santos", "beatriz.santos@tre-pe.jus.br", "7020", u2));
        usuarioRepo.save(new Usuario("27", "Gustavo Oliveira", "gustavo.oliveira@tre-pe.jus.br", "7021", u10));
        usuarioRepo.save(new Usuario("28", "Carolina Souza", "carolina.souza@tre-pe.jus.br", "7022", u14));
        usuarioRepo.save(new Usuario("29", "Bruno Rodrigues", "bruno.rodrigues@tre-pe.jus.br", "7023", u6));
        usuarioRepo.save(new Usuario("30", "Mariana Costa", "mariana.costa@tre-pe.jus.br", "7024", u2));
        Usuario user1111 = usuarioRepo.save(new Usuario("111111111111", "Admin Teste", "admin.teste@tre-pe.jus.br", "1111", u100));
        Usuario user2222 = usuarioRepo.save(new Usuario("222222222222", "Gestor Teste", "gestor.teste@tre-pe.jus.br", "2222", u101));
        Usuario user6666 = usuarioRepo.save(new Usuario("666666666666", "Gestor COSIS", "gestor.cosis@tre-pe.jus.br", "6666", u6));
        Usuario user3333 = usuarioRepo.save(new Usuario("333333333333", "Chefe Teste", "chefe.teste@tre-pe.jus.br", "3333", u8));
        Usuario user1212 = usuarioRepo.save(new Usuario("121212121212", "Chefe SEJUR Teste", "chefe.sejur@tre-pe.jus.br", "1212", u12));
        Usuario user777 = usuarioRepo.save(new Usuario("777", "Chefe STIC Teste", "chefe.stic@tre-pe.jus.br", "7777", u2));
        Usuario user9999 = usuarioRepo.save(new Usuario("999999999999", "Usuario Multi Perfil", "multi.perfil@tre-pe.jus.br", "9999", u2));
        usuarioRepo.save(new Usuario("123456789012", "João Silva", "joao.silva@tre-pe.jus.br", "8001", u2));
        usuarioRepo.save(new Usuario("987654321098", "Maria Santos", "maria.santos@tre-pe.jus.br", "8002", u2));
        usuarioRepo.save(new Usuario("111222333444", "Pedro Oliveira", "pedro.oliveira@tre-pe.jus.br", "8003", u2));
        Usuario user50001 = usuarioRepo.save(new Usuario("50001", "João da Silva", null, null, u8));
        usuarioRepo.save(new Usuario("50002", "Maria Oliveira", null, null, u8));
        Usuario user50003 = usuarioRepo.save(new Usuario("50003", "Pedro Santos", null, null, u9));
        usuarioRepo.save(new Usuario("50004", "Ana Costa", null, null, u9));
        usuarioRepo.save(new Usuario("50005", "Carlos Pereira", null, null, u10));
        usuarioRepo.save(new Usuario("50006", "Juliana Lima", null, null, u10));

        user1.getAtribuicoes().add(UsuarioPerfil.builder().usuario(user1).unidade(user1.getUnidadeLotacao()).perfil(Perfil.SERVIDOR).build());
        usuarioRepo.save(user1);

        user2.getAtribuicoes().add(UsuarioPerfil.builder().usuario(user2).unidade(user2.getUnidadeLotacao()).perfil(Perfil.CHEFE).build());
        usuarioRepo.save(user2);

        user3.getAtribuicoes().add(UsuarioPerfil.builder().usuario(user3).unidade(user3.getUnidadeLotacao()).perfil(Perfil.CHEFE).build());
        usuarioRepo.save(user3);

        user6.getAtribuicoes().add(UsuarioPerfil.builder().usuario(user6).unidade(user6.getUnidadeLotacao()).perfil(Perfil.ADMIN).build());
        usuarioRepo.save(user6);

        user8.getAtribuicoes().add(UsuarioPerfil.builder().usuario(user8).unidade(user8.getUnidadeLotacao()).perfil(Perfil.GESTOR).build());
        usuarioRepo.save(user8);

        user777.getAtribuicoes().add(UsuarioPerfil.builder().usuario(user777).unidade(user777.getUnidadeLotacao()).perfil(Perfil.CHEFE).build());
        usuarioRepo.save(user777);

        user1111.getAtribuicoes().add(UsuarioPerfil.builder().usuario(user1111).unidade(user1111.getUnidadeLotacao()).perfil(Perfil.ADMIN).build());
        usuarioRepo.save(user1111);

        user2222.getAtribuicoes().add(UsuarioPerfil.builder().usuario(user2222).unidade(user2222.getUnidadeLotacao()).perfil(Perfil.GESTOR).build());
        usuarioRepo.save(user2222);

        user3333.getAtribuicoes().add(UsuarioPerfil.builder().usuario(user3333).unidade(user3333.getUnidadeLotacao()).perfil(Perfil.CHEFE).build());
        usuarioRepo.save(user3333);

        user1212.getAtribuicoes().add(UsuarioPerfil.builder().usuario(user1212).unidade(user1212.getUnidadeLotacao()).perfil(Perfil.CHEFE).build());
        usuarioRepo.save(user1212);

        user6666.getAtribuicoes().add(UsuarioPerfil.builder().usuario(user6666).unidade(user6666.getUnidadeLotacao()).perfil(Perfil.GESTOR).build());
        usuarioRepo.save(user6666);

        user9999.getAtribuicoes().add(UsuarioPerfil.builder().usuario(user9999).unidade(user9999.getUnidadeLotacao()).perfil(Perfil.ADMIN).build());
        user9999.getAtribuicoes().add(UsuarioPerfil.builder().usuario(user9999).unidade(user9999.getUnidadeLotacao()).perfil(Perfil.GESTOR).build());
        usuarioRepo.save(user9999);

        u2.setTitular(user777);
        unidadeRepo.save(u2);

        u6.setTitular(user6666);
        unidadeRepo.save(u6);

        u3.setTitular(user2);
        unidadeRepo.save(u3);

        u8.setTitular(user3333);
        unidadeRepo.save(u8);

        u9.setTitular(user3333);
        unidadeRepo.save(u9);

        u10.setTitular(user3333);
        unidadeRepo.save(u10);

        u11.setTitular(user12);
        unidadeRepo.save(u11);

        u12.setTitular(user1212);
        unidadeRepo.save(u12);

        u100.setTitular(user1111);
        unidadeRepo.save(u100);

        u101.setTitular(user2222);
        unidadeRepo.save(u101);

        u102.setTitular(user3333);
        unidadeRepo.save(u102);

        competenciaRepo.save(new Competencia(null, "Desenvolvimento em Java", m1001));
        competenciaRepo.save(new Competencia(null, "Desenvolvimento em Vue.js", m1001));

        competenciaRepo.save(new Competencia(null, "Análise de Dados", m1002));
        competenciaRepo.save(new Competencia(null, "Machine Learning", m1002));

        competenciaRepo.save(new Competencia(null, "Segurança da Informação", m1003));
        competenciaRepo.save(new Competencia(null, "Gestão de Projetos", m1003));

        Competencia c10007 = competenciaRepo.save(new Competencia(null, "Gestão Administrativa", m1004));

        Atividade a30000 = atividadeRepo.save(new Atividade(null, "Realizar atendimento presencial", m1004));
        conhecimentoRepo.save(new Conhecimento(null, "Atendimento ao público", a30000));
        a30000.getCompetencias().add(c10007);
        atividadeRepo.save(a30000);

        Competencia c20001 = competenciaRepo.save(new Competencia(null, "Gestão Administrativa", m201));
        Atividade a30001 = atividadeRepo.save(new Atividade(null, "Realizar atendimento presencial", m201));
        conhecimentoRepo.save(new Conhecimento(null, "Atendimento ao público", a30001));
        a30001.getCompetencias().add(c20001);
        atividadeRepo.save(a30001);

        Processo p50000 = processoRepo.save(new Processo(null, "Processo Teste A", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDateTime.now()));
        p50000.getParticipantes().add(u8);
        processoRepo.save(p50000);

        alertaRepo.save(new Alerta(null, p50000, user50001, "Alerta de teste para processo A", LocalDateTime.now()));

        Processo p50001 = processoRepo.save(new Processo(null, "Processo Teste B", TipoProcesso.MAPEAMENTO, SituacaoProcesso.FINALIZADO, LocalDateTime.now()));
        p50001.getParticipantes().add(u9);
        processoRepo.save(p50001);

        alertaRepo.save(new Alerta(null, p50001, user50003, "Alerta de teste para processo B", LocalDateTime.now()));

        Subprocesso sp60000 = subprocessoRepo.save(new Subprocesso(null, p50000, u8, m1001, SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, LocalDateTime.now()));
        movimentacaoRepo.save(new Movimentacao(null, sp60000, user50001, "INICIADO", LocalDateTime.now()));
    }

    @Transactional
    public void removerDados() {
        // Limpar relacionamentos e tabelas filhas primeiro
        // Break Unidade relationships
        unidadeRepo.findAll().forEach(u -> {
            u.setTitular(null);
            u.setMapaVigente(null);
        });
        unidadeRepo.flush();

        movimentacaoRepo.deleteAllInBatch();
        alertaUsuarioRepo.deleteAllInBatch();
        alertaRepo.deleteAllInBatch();
        analiseRepo.deleteAllInBatch();
        notificacaoRepo.deleteAllInBatch();
        subprocessoRepo.deleteAllInBatch();
        conhecimentoRepo.deleteAllInBatch();

        // Clear ManyToMany
        processoRepo.findAll().forEach(p -> p.getParticipantes().clear());
        processoRepo.flush();
        atividadeRepo.findAll().forEach(a -> a.getCompetencias().clear());
        atividadeRepo.flush();

        processoRepo.deleteAllInBatch();
        atividadeRepo.deleteAllInBatch();
        competenciaRepo.deleteAllInBatch();

        atribuicaoTemporariaRepo.deleteAllInBatch();
        vinculacaoUnidadeRepo.deleteAllInBatch();

        // Users have CascadeType.ALL on atribuicoes (UsuarioPerfil); standard deleteAll to trigger cascading deletes
        usuarioRepo.deleteAll();
        usuarioRepo.flush(); 
        
        unidadeRepo.deleteAllInBatch();
        mapaRepo.deleteAllInBatch();
    }
}