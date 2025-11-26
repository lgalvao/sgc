package sgc.carregamento;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuarioRepo;
import sgc.analise.model.AnaliseRepo;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.MapaRepo;
import sgc.notificacao.model.NotificacaoRepo;
import sgc.processo.model.ProcessoRepo;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.AtribuicaoTemporariaRepo;
import sgc.unidade.model.UnidadeRepo;
import sgc.unidade.model.VinculacaoUnidadeRepo;

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
        mapaRepo.save(new sgc.mapa.model.Mapa(1001L));
        mapaRepo.save(new sgc.mapa.model.Mapa(1002L));
        mapaRepo.save(new sgc.mapa.model.Mapa(1003L));
        mapaRepo.save(new sgc.mapa.model.Mapa(1004L));
        mapaRepo.save(new sgc.mapa.model.Mapa(201L));

        unidadeRepo.save(new sgc.unidade.model.Unidade(1L, "Tribunal Regional Eleitoral", "TRE", sgc.unidade.model.TipoUnidade.INTEROPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, null));
        unidadeRepo.save(new sgc.unidade.model.Unidade(2L, "Secretaria de Informática e Comunicações", "STIC", sgc.unidade.model.TipoUnidade.INTEROPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, null));
        unidadeRepo.save(new sgc.unidade.model.Unidade(100L, "ADMIN-UNIT", "ADMIN-UNIT", sgc.unidade.model.TipoUnidade.INTEROPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, null));
        unidadeRepo.save(new sgc.unidade.model.Unidade(200L, "Secretaria de Gestao de Pessoas", "SGP", sgc.unidade.model.TipoUnidade.INTERMEDIARIA, sgc.unidade.model.SituacaoUnidade.ATIVA, null));

        sgc.unidade.model.Unidade unidade2 = unidadeRepo.findById(2L).get();
        unidadeRepo.save(new sgc.unidade.model.Unidade(3L, "Coordenadoria de Administracao", "COAD", sgc.unidade.model.TipoUnidade.INTERMEDIARIA, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade2));
        unidadeRepo.save(new sgc.unidade.model.Unidade(6L, "Coordenadoria de Sistemas", "COSIS", sgc.unidade.model.TipoUnidade.INTERMEDIARIA, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade2));
        unidadeRepo.save(new sgc.unidade.model.Unidade(7L, "Coordenadoria de Suporte e Infraestrutura", "COSINF", sgc.unidade.model.TipoUnidade.INTERMEDIARIA, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade2));
        unidadeRepo.save(new sgc.unidade.model.Unidade(14L, "Coordenadoria Jurídica", "COJUR", sgc.unidade.model.TipoUnidade.INTERMEDIARIA, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade2));

        sgc.unidade.model.Unidade unidade100 = unidadeRepo.findById(100L).get();
        unidadeRepo.save(new sgc.unidade.model.Unidade(101L, "GESTOR-UNIT", "GESTOR-UNIT", sgc.unidade.model.TipoUnidade.INTERMEDIARIA, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade100));

        sgc.unidade.model.Unidade unidade200_2 = unidadeRepo.findById(200L).get();
        unidadeRepo.save(new sgc.unidade.model.Unidade(201L, "Coordenadoria de Atenção ao Servidor", "CAS", sgc.unidade.model.TipoUnidade.INTEROPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade200_2));

        sgc.unidade.model.Unidade unidade3 = unidadeRepo.findById(3L).get();
        unidadeRepo.save(new sgc.unidade.model.Unidade(4L, "Coordenadoria de Educação Especial", "COEDE", sgc.unidade.model.TipoUnidade.INTERMEDIARIA, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade3));

        sgc.unidade.model.Unidade unidade4 = unidadeRepo.findById(4L).get();
        unidadeRepo.save(new sgc.unidade.model.Unidade(5L, "Seção Magistrados e Requisitados", "SEMARE", sgc.unidade.model.TipoUnidade.OPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade4));

        sgc.unidade.model.Unidade unidade6_2 = unidadeRepo.findById(6L).get();
        unidadeRepo.save(new sgc.unidade.model.Unidade(8L, "Seção de Desenvolvimento de Sistemas", "SEDESENV", sgc.unidade.model.TipoUnidade.OPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade6_2));
        unidadeRepo.save(new sgc.unidade.model.Unidade(9L, "Seção de Dados e Inteligência Artificial", "SEDIA", sgc.unidade.model.TipoUnidade.OPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade6_2));
        unidadeRepo.save(new sgc.unidade.model.Unidade(10L, "Seção de Sistemas Eleitorais", "SESEL", sgc.unidade.model.TipoUnidade.OPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade6_2));

        sgc.unidade.model.Unidade unidade7 = unidadeRepo.findById(7L).get();
        unidadeRepo.save(new sgc.unidade.model.Unidade(11L, "Seção de Infraestrutura", "SENIC", sgc.unidade.model.TipoUnidade.OPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade7));

        sgc.unidade.model.Unidade unidade14_2 = unidadeRepo.findById(14L).get();
        unidadeRepo.save(new sgc.unidade.model.Unidade(12L, "Seção Jurídica", "SEJUR", sgc.unidade.model.TipoUnidade.OPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade14_2));
        unidadeRepo.save(new sgc.unidade.model.Unidade(13L, "Seção de Processos", "SEPRO", sgc.unidade.model.TipoUnidade.OPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade14_2));
        unidadeRepo.save(new sgc.unidade.model.Unidade(15L, "Seção de Documentação", "SEDOC", sgc.unidade.model.TipoUnidade.OPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade2));

        sgc.unidade.model.Unidade unidade101_2 = unidadeRepo.findById(101L).get();
        unidadeRepo.save(new sgc.unidade.model.Unidade(102L, "SUB-UNIT", "SUB-UNIT", sgc.unidade.model.TipoUnidade.OPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade101_2));

        sgc.unidade.model.Unidade unidade201 = unidadeRepo.findById(201L).get();
        unidadeRepo.save(new sgc.unidade.model.Unidade(202L, "Seção de Atenção ao Servidor", "SAS", sgc.unidade.model.TipoUnidade.OPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade201));

        unidadeRepo.save(new sgc.unidade.model.Unidade(900L, "CDU04-UNIT", "CDU04-UNIT", sgc.unidade.model.TipoUnidade.OPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade2));
        unidadeRepo.save(new sgc.unidade.model.Unidade(901L, "CDU05-REV-UNIT", "CDU05-REV-UNIT", sgc.unidade.model.TipoUnidade.OPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade2));
        unidadeRepo.save(new sgc.unidade.model.Unidade(902L, "CDU05-SUB-UNIT", "CDU05-SUB-UNIT", sgc.unidade.model.TipoUnidade.OPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade2));
        unidadeRepo.save(new sgc.unidade.model.Unidade(903L, "CDU05-ALERT-UNIT", "CDU05-ALERT-UNIT", sgc.unidade.model.TipoUnidade.OPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade2));
        unidadeRepo.save(new sgc.unidade.model.Unidade(904L, "CDU05-READONLY-UNIT", "CDU05-READONLY-UNIT", sgc.unidade.model.TipoUnidade.OPERACIONAL, sgc.unidade.model.SituacaoUnidade.ATIVA, unidade2));

        sgc.unidade.model.Unidade unidade10 = unidadeRepo.findById(10L).get();
        usuarioRepo.save(new sgc.sgrh.model.Usuario("1", "Ana Paula Souza", "ana.souza@tre-pe.jus.br", "1234", unidade10));
        sgc.unidade.model.Unidade unidade200 = unidadeRepo.findById(200L).get();
        usuarioRepo.save(new sgc.sgrh.model.Usuario("2", "Carlos Henrique Lima", "carlos.lima@tre-pe.jus.br", "2345", unidade200));
        sgc.unidade.model.Unidade unidade8 = unidadeRepo.findById(8L).get();
        usuarioRepo.save(new sgc.sgrh.model.Usuario("3", "Fernanda Oliveira", "fernanda.oliveira@tre-pe.jus.br", "3456", unidade8));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("4", "João Batista Silva", "joao.silva@tre-pe.jus.br", "4567", unidade10));
        sgc.unidade.model.Unidade unidade5 = unidadeRepo.findById(5L).get();
        usuarioRepo.save(new sgc.sgrh.model.Usuario("5", "Marina Dias", "marina.dias@tre-pe.jus.br", "5678", unidade5));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("6", "Ricardo Alves", "ricardo.alves@tre-pe.jus.br", "6789", unidade2));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("7", "Zeca Silva", "zeca.gado@tre-pe.jus.br", "7001", unidade2));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("8", "Paulo Horta", "paulo.horta@tre-pe.jus.br", "7002", unidade8));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("9", "Giuseppe Corleone", "giuseppe.corleone@tre-pe.jus.br", "7003", unidade8));
        sgc.unidade.model.Unidade unidade9 = unidadeRepo.findById(9L).get();
        usuarioRepo.save(new sgc.sgrh.model.Usuario("10", "Paula Gonçalves", "paula.goncalves@tre-pe.jus.br", "7004", unidade9));
        sgc.unidade.model.Unidade unidade11 = unidadeRepo.findById(11L).get();
        usuarioRepo.save(new sgc.sgrh.model.Usuario("11", "Herman Greely", "herman.greely@tre-pe.jus.br", "7005", unidade11));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("12", "Taís Condida", "tais.condida@tre-pe.jus.br", "7006", unidade11));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("13", "Mike Smith", "mike.smith@tre-pe.jus.br", "7007", unidade11));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("14", "Maroca Silva", "maroca.silva@tre-pe.jus.br", "7008", unidade2));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("15", "Roberto Santos", "roberto.santos@tre-pe.jus.br", "7009", unidade2));
        sgc.unidade.model.Unidade unidade6 = unidadeRepo.findById(6L).get();
        usuarioRepo.save(new sgc.sgrh.model.Usuario("16", "Luciana Pereira", "luciana.pereira@tre-pe.jus.br", "7010", unidade6));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("17", "Fernando Costa", "fernando.costa@tre-pe.jus.br", "7011", unidade10));
        sgc.unidade.model.Unidade unidade14 = unidadeRepo.findById(14L).get();
        usuarioRepo.save(new sgc.sgrh.model.Usuario("18", "Amanda Rodrigues", "amanda.rodrigues@tre-pe.jus.br", "7012", unidade14));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("19", "Diego Fernandes", "diego.fernandes@tre-pe.jus.br", "7013", unidade6));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("20", "Juliana Almeida", "juliana.almeida@tre-pe.jus.br", "7014", unidade2));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("21", "Rafael Moreira", "rafael.moreira@tre-pe.jus.br", "7015", unidade2));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("22", "Camila Barbosa", "camila.barbosa@tre-pe.jus.br", "7016", unidade10));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("23", "Thiago Carvalho", "thiago.carvalho@tre-pe.jus.br", "7017", unidade14));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("24", "Patrícia Lima", "patricia.lima@tre-pe.jus.br", "7018", unidade6));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("25", "Lucas Mendes", "lucas.mendes@tre-pe.jus.br", "7019", unidade2));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("26", "Beatriz Santos", "beatriz.santos@tre-pe.jus.br", "7020", unidade2));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("27", "Gustavo Oliveira", "gustavo.oliveira@tre-pe.jus.br", "7021", unidade10));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("28", "Carolina Souza", "carolina.souza@tre-pe.jus.br", "7022", unidade14));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("29", "Bruno Rodrigues", "bruno.rodrigues@tre-pe.jus.br", "7023", unidade6));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("30", "Mariana Costa", "mariana.costa@tre-pe.jus.br", "7024", unidade2));
        sgc.unidade.model.Unidade unidade100_2 = unidadeRepo.findById(100L).get();
        usuarioRepo.save(new sgc.sgrh.model.Usuario("111111111111", "Admin Teste", "admin.teste@tre-pe.jus.br", "1111", unidade100_2));
        sgc.unidade.model.Unidade unidade101 = unidadeRepo.findById(101L).get();
        usuarioRepo.save(new sgc.sgrh.model.Usuario("222222222222", "Gestor Teste", "gestor.teste@tre-pe.jus.br", "2222", unidade101));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("666666666666", "Gestor COSIS", "gestor.cosis@tre-pe.jus.br", "6666", unidade6));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("333333333333", "Chefe Teste", "chefe.teste@tre-pe.jus.br", "3333", unidade8));
        sgc.unidade.model.Unidade unidade12 = unidadeRepo.findById(12L).get();
        usuarioRepo.save(new sgc.sgrh.model.Usuario("121212121212", "Chefe SEJUR Teste", "chefe.sejur@tre-pe.jus.br", "1212", unidade12));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("777", "Chefe STIC Teste", "chefe.stic@tre-pe.jus.br", "7777", unidade2));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("999999999999", "Usuario Multi Perfil", "multi.perfil@tre-pe.jus.br", "9999", unidade2));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("123456789012", "João Silva", "joao.silva@tre-pe.jus.br", "8001", unidade2));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("987654321098", "Maria Santos", "maria.santos@tre-pe.jus.br", "8002", unidade2));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("111222333444", "Pedro Oliveira", "pedro.oliveira@tre-pe.jus.br", "8003", unidade2));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("50001", "João da Silva", null, null, unidade8));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("50002", "Maria Oliveira", null, null, unidade8));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("50003", "Pedro Santos", null, null, unidade9));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("50004", "Ana Costa", null, null, unidade9));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("50005", "Carlos Pereira", null, null, unidade10));
        usuarioRepo.save(new sgc.sgrh.model.Usuario("50006", "Juliana Lima", null, null, unidade10));

        sgc.sgrh.model.Usuario usuario1 = usuarioRepo.findById("1").get();
        usuario1.getAtribuicoes().add(sgc.sgrh.model.UsuarioPerfil.builder().usuario(usuario1).unidade(usuario1.getUnidadeLotacao()).perfil(sgc.sgrh.model.Perfil.SERVIDOR).build());
        usuarioRepo.save(usuario1);

        sgc.sgrh.model.Usuario usuario2_2 = usuarioRepo.findById("2").get();
        usuario2_2.getAtribuicoes().add(sgc.sgrh.model.UsuarioPerfil.builder().usuario(usuario2_2).unidade(usuario2_2.getUnidadeLotacao()).perfil(sgc.sgrh.model.Perfil.CHEFE).build());
        usuarioRepo.save(usuario2_2);

        sgc.sgrh.model.Usuario usuario3_2 = usuarioRepo.findById("3").get();
        usuario3_2.getAtribuicoes().add(sgc.sgrh.model.UsuarioPerfil.builder().usuario(usuario3_2).unidade(usuario3_2.getUnidadeLotacao()).perfil(sgc.sgrh.model.Perfil.CHEFE).build());
        usuarioRepo.save(usuario3_2);

        sgc.sgrh.model.Usuario usuario6_3 = usuarioRepo.findById("6").get();
        usuario6_3.getAtribuicoes().add(sgc.sgrh.model.UsuarioPerfil.builder().usuario(usuario6_3).unidade(usuario6_3.getUnidadeLotacao()).perfil(sgc.sgrh.model.Perfil.ADMIN).build());
        usuarioRepo.save(usuario6_3);

        sgc.sgrh.model.Usuario usuario8_2 = usuarioRepo.findById("8").get();
        usuario8_2.getAtribuicoes().add(sgc.sgrh.model.UsuarioPerfil.builder().usuario(usuario8_2).unidade(usuario8_2.getUnidadeLotacao()).perfil(sgc.sgrh.model.Perfil.GESTOR).build());
        usuarioRepo.save(usuario8_2);

        sgc.sgrh.model.Usuario usuario777 = usuarioRepo.findById("777").get();
        usuario777.getAtribuicoes().add(sgc.sgrh.model.UsuarioPerfil.builder().usuario(usuario777).unidade(usuario777.getUnidadeLotacao()).perfil(sgc.sgrh.model.Perfil.CHEFE).build());
        usuarioRepo.save(usuario777);

        sgc.sgrh.model.Usuario usuario111111111111 = usuarioRepo.findById("111111111111").get();
        usuario111111111111.getAtribuicoes().add(sgc.sgrh.model.UsuarioPerfil.builder().usuario(usuario111111111111).unidade(usuario111111111111.getUnidadeLotacao()).perfil(sgc.sgrh.model.Perfil.ADMIN).build());
        usuarioRepo.save(usuario111111111111);

        sgc.sgrh.model.Usuario usuario222222222222 = usuarioRepo.findById("222222222222").get();
        usuario222222222222.getAtribuicoes().add(sgc.sgrh.model.UsuarioPerfil.builder().usuario(usuario222222222222).unidade(usuario222222222222.getUnidadeLotacao()).perfil(sgc.sgrh.model.Perfil.GESTOR).build());
        usuarioRepo.save(usuario222222222222);

        sgc.sgrh.model.Usuario usuario333333333333 = usuarioRepo.findById("333333333333").get();
        usuario333333333333.getAtribuicoes().add(sgc.sgrh.model.UsuarioPerfil.builder().usuario(usuario333333333333).unidade(usuario333333333333.getUnidadeLotacao()).perfil(sgc.sgrh.model.Perfil.CHEFE).build());
        usuarioRepo.save(usuario333333333333);

        sgc.sgrh.model.Usuario usuario121212121212 = usuarioRepo.findById("121212121212").get();
        usuario121212121212.getAtribuicoes().add(sgc.sgrh.model.UsuarioPerfil.builder().usuario(usuario121212121212).unidade(usuario121212121212.getUnidadeLotacao()).perfil(sgc.sgrh.model.Perfil.CHEFE).build());
        usuarioRepo.save(usuario121212121212);

        sgc.sgrh.model.Usuario usuario666666666666 = usuarioRepo.findById("666666666666").get();
        usuario666666666666.getAtribuicoes().add(sgc.sgrh.model.UsuarioPerfil.builder().usuario(usuario666666666666).unidade(usuario666666666666.getUnidadeLotacao()).perfil(sgc.sgrh.model.Perfil.GESTOR).build());
        usuarioRepo.save(usuario666666666666);

        sgc.sgrh.model.Usuario usuario999999999999 = usuarioRepo.findById("999999999999").get();
        usuario999999999999.getAtribuicoes().add(sgc.sgrh.model.UsuarioPerfil.builder().usuario(usuario999999999999).unidade(usuario999999999999.getUnidadeLotacao()).perfil(sgc.sgrh.model.Perfil.ADMIN).build());
        usuario999999999999.getAtribuicoes().add(sgc.sgrh.model.UsuarioPerfil.builder().usuario(usuario999999999999).unidade(usuario999999999999.getUnidadeLotacao()).perfil(sgc.sgrh.model.Perfil.GESTOR).build());
        usuarioRepo.save(usuario999999999999);

        unidade2.setTitular(usuarioRepo.findById("777").get());
        unidadeRepo.save(unidade2);

        unidade6.setTitular(usuarioRepo.findById("666666666666").get());
        unidadeRepo.save(unidade6);

        unidade3.setTitular(usuarioRepo.findById("2").get());
        unidadeRepo.save(unidade3);

        unidade8.setTitular(usuarioRepo.findById("333333333333").get());
        unidadeRepo.save(unidade8);

        unidade9.setTitular(usuarioRepo.findById("333333333333").get());
        unidadeRepo.save(unidade9);

        unidade10.setTitular(usuarioRepo.findById("333333333333").get());
        unidadeRepo.save(unidade10);

        unidade11.setTitular(usuarioRepo.findById("12").get());
        unidadeRepo.save(unidade11);

        unidade12.setTitular(usuarioRepo.findById("121212121212").get());
        unidadeRepo.save(unidade12);

        unidade100.setTitular(usuarioRepo.findById("111111111111").get());
        unidadeRepo.save(unidade100);

        unidade101.setTitular(usuarioRepo.findById("222222222222").get());
        unidadeRepo.save(unidade101);

        sgc.unidade.model.Unidade unidade102 = unidadeRepo.findById(102L).get();
        unidade102.setTitular(usuarioRepo.findById("333333333333").get());
        unidadeRepo.save(unidade102);

        sgc.mapa.model.Mapa mapa1001_2 = mapaRepo.findById(1001L).get();
        competenciaRepo.save(new sgc.mapa.model.Competencia(10001L, "Desenvolvimento em Java", mapa1001_2));
        competenciaRepo.save(new sgc.mapa.model.Competencia(10002L, "Desenvolvimento em Vue.js", mapa1001_2));

        sgc.mapa.model.Mapa mapa1002 = mapaRepo.findById(1002L).get();
        competenciaRepo.save(new sgc.mapa.model.Competencia(10003L, "Análise de Dados", mapa1002));
        competenciaRepo.save(new sgc.mapa.model.Competencia(10004L, "Machine Learning", mapa1002));

        sgc.mapa.model.Mapa mapa1003 = mapaRepo.findById(1003L).get();
        competenciaRepo.save(new sgc.mapa.model.Competencia(10005L, "Segurança da Informação", mapa1003));
        competenciaRepo.save(new sgc.mapa.model.Competencia(10006L, "Gestão de Projetos", mapa1003));

        sgc.mapa.model.Mapa mapa1004 = mapaRepo.findById(1004L).get();
        competenciaRepo.save(new sgc.mapa.model.Competencia(10007L, "Gestão Administrativa", mapa1004));

        atividadeRepo.save(new sgc.atividade.model.Atividade(30000L, "Realizar atendimento presencial", mapa1004));
        sgc.atividade.model.Atividade atividade30000 = atividadeRepo.findById(30000L).get();
        conhecimentoRepo.save(new sgc.atividade.model.Conhecimento(40000L, "Atendimento ao público", atividade30000));
        sgc.mapa.model.Competencia competencia10007 = competenciaRepo.findById(10007L).get();
        atividade30000.getCompetencias().add(competencia10007);
        atividadeRepo.save(atividade30000);

        sgc.mapa.model.Mapa mapa201 = mapaRepo.findById(201L).get();
        competenciaRepo.save(new sgc.mapa.model.Competencia(20001L, "Gestão Administrativa", mapa201));
        atividadeRepo.save(new sgc.atividade.model.Atividade(30001L, "Realizar atendimento presencial", mapa201));
        sgc.atividade.model.Atividade atividade30001 = atividadeRepo.findById(30001L).get();
        conhecimentoRepo.save(new sgc.atividade.model.Conhecimento(40001L, "Atendimento ao público", atividade30001));
        sgc.mapa.model.Competencia competencia20001 = competenciaRepo.findById(20001L).get();
        atividade30001.getCompetencias().add(competencia20001);
        atividadeRepo.save(atividade30001);

        processoRepo.save(new sgc.processo.model.Processo(50000L, "Processo Teste A", sgc.processo.model.TipoProcesso.MAPEAMENTO, sgc.processo.model.SituacaoProcesso.EM_ANDAMENTO, java.time.LocalDateTime.now()));
        sgc.processo.model.Processo processo50000 = processoRepo.findById(50000L).get();
        processo50000.getParticipantes().add(unidade8);
        processoRepo.save(processo50000);

        sgc.sgrh.model.Usuario usuario50001 = usuarioRepo.findById("50001").get();
        alertaRepo.save(new sgc.alerta.model.Alerta(70000L, processo50000, usuario50001, "Alerta de teste para processo A", java.time.LocalDateTime.now()));

        processoRepo.save(new sgc.processo.model.Processo(50001L, "Processo Teste B", sgc.processo.model.TipoProcesso.MAPEAMENTO, sgc.processo.model.SituacaoProcesso.FINALIZADO, java.time.LocalDateTime.now()));
        sgc.processo.model.Processo processo50001 = processoRepo.findById(50001L).get();
        processo50001.getParticipantes().add(unidade9);
        processoRepo.save(processo50001);

        sgc.sgrh.model.Usuario usuario50003 = usuarioRepo.findById("50003").get();
        alertaRepo.save(new sgc.alerta.model.Alerta(70001L, processo50001, usuario50003, "Alerta de teste para processo B", java.time.LocalDateTime.now()));

        sgc.mapa.model.Mapa mapa1001 = mapaRepo.findById(1001L).get();
        subprocessoRepo.save(new sgc.subprocesso.model.Subprocesso(60000L, processo50000, unidade8, mapa1001, sgc.subprocesso.model.SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO, java.time.LocalDateTime.now()));
        sgc.subprocesso.model.Subprocesso subprocesso60000 = subprocessoRepo.findById(60000L).get();
        movimentacaoRepo.save(new sgc.subprocesso.model.Movimentacao(80000L, subprocesso60000, usuario50001, "INICIADO", java.time.LocalDateTime.now()));
    }

    @Transactional
    public void removerDados() {
        // Limpar relacionamentos e tabelas filhas primeiro
        movimentacaoRepo.deleteAllInBatch();
        alertaUsuarioRepo.deleteAllInBatch();
        alertaRepo.deleteAllInBatch();
        analiseRepo.deleteAllInBatch();
        notificacaoRepo.deleteAllInBatch();
        subprocessoRepo.deleteAllInBatch();
        conhecimentoRepo.deleteAllInBatch();

        // Limpar tabelas de junção ManyToMany
        processoRepo.findAll().forEach(p -> p.getParticipantes().clear());
        processoRepo.flush();
        atividadeRepo.findAll().forEach(a -> a.getCompetencias().clear());
        atividadeRepo.flush();

        processoRepo.deleteAllInBatch();
        atividadeRepo.deleteAllInBatch();
        competenciaRepo.deleteAllInBatch();

        atribuicaoTemporariaRepo.deleteAllInBatch();
        vinculacaoUnidadeRepo.deleteAllInBatch();

        // Remover referências de FK em Unidade antes de deletar Usuarios e Mapas
        unidadeRepo.findAll().forEach(u -> {
            u.setTitular(null);
            u.setMapaVigente(null);
        });
        unidadeRepo.flush();

        // Finalmente, deletar as entidades principais
        usuarioRepo.deleteAllInBatch();
        unidadeRepo.deleteAllInBatch();
        mapaRepo.deleteAllInBatch();
    }

}
