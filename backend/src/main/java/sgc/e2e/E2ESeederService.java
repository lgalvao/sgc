package sgc.e2e;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.alerta.modelo.AlertaRepo;
import sgc.alerta.modelo.AlertaUsuarioRepo;
import sgc.analise.modelo.AnaliseRepo;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.processo.SituacaoProcesso;
import sgc.processo.modelo.TipoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.processo.modelo.UnidadeProcessoRepo;
import sgc.sgrh.Perfil;
import sgc.sgrh.SgrhService;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.SituacaoUnidade;
import sgc.unidade.modelo.TipoUnidade;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class E2ESeederService {

    private final UsuarioRepo usuarioRepo;
    private final UnidadeRepo unidadeRepo;
    private final ProcessoRepo processoRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final UnidadeProcessoRepo unidadeProcessoRepo;
    private final MapaRepo mapaRepo;
    private final UnidadeMapaRepo unidadeMapaRepo;
    private final AtividadeRepo atividadeRepo;
    private final ConhecimentoRepo conhecimentoRepo;
    private final CompetenciaRepo competenciaRepo;
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;
    private final AnaliseRepo analiseRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final AlertaRepo alertaRepo;
    private final AlertaUsuarioRepo alertaUsuarioRepo;

    public Map<String, Object> seedData() {
        // 1. Limpar dados existentes (ordem inversa para respeitar chaves estrangeiras)
        SgrhService.perfisMock.clear();
        alertaUsuarioRepo.deleteAll();
        alertaRepo.deleteAll();
        movimentacaoRepo.deleteAll();
        analiseRepo.deleteAll();
        competenciaAtividadeRepo.deleteAll();
        conhecimentoRepo.deleteAll();
        atividadeRepo.deleteAll();
        competenciaRepo.deleteAll();
        unidadeMapaRepo.deleteAll();
        mapaRepo.deleteAll();
        subprocessoRepo.deleteAll();
        unidadeProcessoRepo.deleteAll();
        processoRepo.deleteAll();

        // Remover titulares das unidades antes de deletar usuários
        List<Unidade> todasUnidades = unidadeRepo.findAll();
        todasUnidades.forEach(unidade -> unidade.setTitular(null));
        unidadeRepo.saveAll(todasUnidades);

        usuarioRepo.deleteAll();
        unidadeRepo.deleteAll();

        // 2. Criar Hierarquia de Unidades
        Unidade sedoc = new Unidade("SECRETARIA DE DOCUMENTAÇÃO", "SEDOC", null, TipoUnidade.INTEROPERACIONAL, SituacaoUnidade.ATIVA, null);
        sedoc = unidadeRepo.save(sedoc);

        Unidade sgp = new Unidade("SECRETARIA DE GESTÃO DE PESSOAS", "SGP", null, TipoUnidade.INTERMEDIARIA, SituacaoUnidade.ATIVA, sedoc);
        sgp = unidadeRepo.save(sgp);

        Unidade stic = new Unidade("SECRETARIA DE TECNOLOGIA DA INFORMAÇÃO E COMUNICAÇÃO", "STIC", null, TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, sgp);
        stic = unidadeRepo.save(stic);

        Unidade sesel = new Unidade("SEÇÃO DE SISTEMAS ELEITORAIS", "SESEL", null, TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, stic);
        sesel = unidadeRepo.save(sesel);
        System.out.println("Unidades salvas com códigos: SEDOC=" + sedoc.getCodigo() + ", SGP=" + sgp.getCodigo() + ", STIC=" + stic.getCodigo() + ", SESEL=" + sesel.getCodigo());

        // 3. Criar Usuários para cada Perfil
        Usuario admin = new Usuario(7L, "ADMINISTRADOR", "admin@sgc.com", "1007", null, Set.of(Perfil.ADMIN));
        Usuario gestor = new Usuario(2L, "GESTOR SGP", "gestor.sgp@sgc.com", "1002", null, Set.of(Perfil.GESTOR));
        Usuario chefe = new Usuario(1L, "CHEFE STIC", "chefe.stic@sgc.com", "1001", null, Set.of(Perfil.CHEFE));
        Usuario servidor = new Usuario(3L, "SERVIDOR STIC", "servidor.stic@sgc.com", "1003", null, Set.of(Perfil.SERVIDOR));
        Usuario multiPerfil = new Usuario(10L, "MULTI PERFIL", "multi@sgc.com", "1010", null, Set.of(Perfil.ADMIN, Perfil.GESTOR));

        usuarioRepo.saveAll(List.of(admin, gestor, chefe, servidor, multiPerfil));

        // Buscar usuários novamente para garantir que sejam entidades gerenciadas
        Usuario adminGerenciado = usuarioRepo.findByTituloEleitoral(admin.getTituloEleitoral()).orElseThrow();
        Usuario gestorGerenciado = usuarioRepo.findByTituloEleitoral(gestor.getTituloEleitoral()).orElseThrow();
        Usuario chefeGerenciado = usuarioRepo.findByTituloEleitoral(chefe.getTituloEleitoral()).orElseThrow();
        Usuario servidorGerenciado = usuarioRepo.findByTituloEleitoral(servidor.getTituloEleitoral()).orElseThrow();
        Usuario multiPerfilGerenciado = usuarioRepo.findByTituloEleitoral(multiPerfil.getTituloEleitoral()).orElseThrow();

        // Buscar unidades novamente para garantir que sejam entidades gerenciadas
        Unidade sedocGerenciada = unidadeRepo.findBySigla("SEDOC").orElseThrow();
        Unidade sgpGerenciada = unidadeRepo.findBySigla("SGP").orElseThrow();
        Unidade sticGerenciada = unidadeRepo.findBySigla("STIC").orElseThrow();
        Unidade seselGerenciada = unidadeRepo.findBySigla("SESEL").orElseThrow();

        // 4. Associar titulares às unidades E definir a unidade do usuário
        sedocGerenciada.setTitular(adminGerenciado);
        adminGerenciado.setUnidade(sedocGerenciada);

        sgpGerenciada.setTitular(gestorGerenciado);
        gestorGerenciado.setUnidade(sgpGerenciada);

        sticGerenciada.setTitular(chefeGerenciado);
        chefeGerenciado.setUnidade(sticGerenciada);

        servidorGerenciado.setUnidade(sticGerenciada);
        multiPerfilGerenciado.setUnidade(sedocGerenciada);

        usuarioRepo.saveAll(List.of(adminGerenciado, gestorGerenciado, chefeGerenciado, servidorGerenciado, multiPerfilGerenciado));
        unidadeRepo.saveAll(List.of(sedocGerenciada, sgpGerenciada, sticGerenciada));

        // 5. Criar Processos
        Processo p1 = new Processo("Processo teste revisão CDU-05", TipoProcesso.MAPEAMENTO, SituacaoProcesso.CRIADO, LocalDateTime.now());
        Processo p2 = new Processo("Revisão de mapa de competências STIC - 2024", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDateTime.now());
        Processo p3 = new Processo("Mapeamento de competências - 2025", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDateTime.now());
        Processo p4 = new Processo("Mapeamento inicial COJUR - 2025", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDateTime.now());
        processoRepo.saveAll(List.of(p1, p2, p3, p4));
        System.out.println("Processos salvos: " + p1.getCodigo() + ", " + p2.getCodigo() + ", " + p3.getCodigo() + ", " + p4.getCodigo());
        System.out.println("Total de processos no repositório após saveAll: " + processoRepo.count());

        // 6. Associar Processos às Unidades
        UnidadeProcesso up1 = new UnidadeProcesso(p1.getCodigo(), sedoc.getCodigo(), sedoc.getNome(), sedoc.getSigla(), String.valueOf(admin.getTituloEleitoral()), sedoc.getTipo(), sedoc.getSituacao().name(), sedoc.getUnidadeSuperior() != null ? sedoc.getUnidadeSuperior().getCodigo() : null);
        unidadeProcessoRepo.save(up1);

        UnidadeProcesso up2 = new UnidadeProcesso(p2.getCodigo(), stic.getCodigo(), stic.getNome(), stic.getSigla(), String.valueOf(chefe.getTituloEleitoral()), stic.getTipo(), stic.getSituacao().name(), stic.getUnidadeSuperior() != null ? stic.getUnidadeSuperior().getCodigo() : null);
        unidadeProcessoRepo.save(up2);

        UnidadeProcesso up3 = new UnidadeProcesso(p3.getCodigo(), sgp.getCodigo(), sgp.getNome(), sgp.getSigla(), String.valueOf(gestor.getTituloEleitoral()), sgp.getTipo(), sgp.getSituacao().name(), sgp.getUnidadeSuperior() != null ? sgp.getUnidadeSuperior().getCodigo() : null);
        unidadeProcessoRepo.save(up3);

        UnidadeProcesso up4 = new UnidadeProcesso(p4.getCodigo(), sedoc.getCodigo(), sedoc.getNome(), sedoc.getSigla(), String.valueOf(admin.getTituloEleitoral()), sedoc.getTipo(), sedoc.getSituacao().name(), sedoc.getUnidadeSuperior() != null ? sedoc.getUnidadeSuperior().getCodigo() : null);
        unidadeProcessoRepo.save(up4);

        // 7. Retornar os dados criados
        return Map.of(
                "message", "Banco de dados semeado para testes E2E.",
                "adminUsername", admin.getTituloEleitoral(),
                "gestorUsername", gestor.getTituloEleitoral(),
                "chefeUsername", chefe.getTituloEleitoral(),
                "servidorUsername", servidor.getTituloEleitoral(),
                "multiPerfilUsername", multiPerfil.getTituloEleitoral()
        );
    }
}
