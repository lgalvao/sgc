package sgc.integracao;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import sgc.diagnostico.model.AvaliacaoServidor;
import sgc.diagnostico.model.AvaliacaoServidorRepo;
import sgc.diagnostico.model.Diagnostico;
import sgc.diagnostico.model.DiagnosticoRepo;
import sgc.diagnostico.model.OcupacaoCritica;
import sgc.diagnostico.model.OcupacaoCriticaRepo;
import sgc.diagnostico.model.SituacaoAvaliacaoServidor;
import sgc.diagnostico.model.SituacaoCapacitacao;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.SubprocessoFixture;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

abstract class DiagnosticoCduIntegrationTestBase extends BaseIntegrationTest {

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected DiagnosticoRepo diagnosticoRepo;

    @Autowired
    protected AvaliacaoServidorRepo avaliacaoServidorRepo;

    @Autowired
    protected OcupacaoCriticaRepo ocupacaoCriticaRepo;

    @Autowired
    protected CompetenciaRepo competenciaRepo;

    protected Unidade unidade;
    protected Usuario servidor;
    protected Usuario outroServidor;
    protected Processo processo;
    protected Subprocesso subprocesso;
    protected Diagnostico diagnostico;
    protected Competencia competencia1;
    protected Competencia competencia2;

    protected void criarCenarioDiagnosticoBase(Long codigoUnidade, String tituloServidor, String tituloOutroServidor) {
        unidade = unidadeRepo.findById(codigoUnidade).orElseThrow();
        servidor = usuarioRepo.findById(tituloServidor).orElseThrow();
        outroServidor = usuarioRepo.findById(tituloOutroServidor).orElseThrow();
        garantirEmail(servidor);
        garantirEmail(outroServidor);
        usuarioRepo.saveAllAndFlush(List.of(servidor, outroServidor));

        processo = ProcessoFixture.novoProcesso();
        processo.setDescricao("Processo diagnóstico integração " + System.nanoTime());
        processo.setTipo(TipoProcesso.DIAGNOSTICO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataCriacao(LocalDateTime.now());
        processo.setDataLimite(LocalDateTime.now().plusDays(20));
        processo.adicionarParticipantes(Set.of(unidade));
        processo = processoRepo.saveAndFlush(processo);

        subprocesso = SubprocessoFixture.novoSubprocesso(processo, unidade);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusDays(15));
        subprocesso = subprocessoRepo.saveAndFlush(subprocesso);

        Mapa mapa = mapaRepo.saveAndFlush(Mapa.builder().subprocesso(subprocesso).build());
        subprocesso.setMapa(mapa);
        subprocesso = subprocessoRepo.saveAndFlush(subprocesso);

        competencia1 = competenciaRepo.saveAndFlush(Competencia.builder()
                .mapa(mapa)
                .descricao("Competência 1 " + System.nanoTime())
                .build());
        competencia2 = competenciaRepo.saveAndFlush(Competencia.builder()
                .mapa(mapa)
                .descricao("Competência 2 " + System.nanoTime())
                .build());

        diagnostico = Diagnostico.builder()
                .subprocesso(subprocesso)
                .avaliacaoServidores(List.of(
                        novaAvaliacao(servidor, competencia1),
                        novaAvaliacao(servidor, competencia2),
                        novaAvaliacao(outroServidor, competencia1),
                        novaAvaliacao(outroServidor, competencia2)
                ))
                .ocupacaoCriticas(List.of(
                        novaOcupacaoCritica(servidor, competencia1),
                        novaOcupacaoCritica(servidor, competencia2),
                        novaOcupacaoCritica(outroServidor, competencia1),
                        novaOcupacaoCritica(outroServidor, competencia2)
                ))
                .build();

        diagnostico.getAvaliacaoServidores().forEach(avaliacao -> avaliacao.setDiagnostico(diagnostico));
        diagnostico.getOcupacaoCriticas().forEach(ocupacao -> ocupacao.setDiagnostico(diagnostico));
        diagnostico = diagnosticoRepo.saveAndFlush(diagnostico);

        registrarMovimentacaoInicial(subprocesso);
        recarregarContexto();
    }

    protected void preencherAutoavaliacao(
            String tituloServidor,
            int importancia1,
            int dominio1,
            int importancia2,
            int dominio2,
            SituacaoAvaliacaoServidor situacao
    ) {
        List<AvaliacaoServidor> avaliacoes = avaliacaoServidorRepo.buscarAvaliacoesDoServidor(
                diagnostico.getCodigo(), tituloServidor);
        for (AvaliacaoServidor avaliacao : avaliacoes) {
            if (avaliacao.getCompetencia().getCodigo().equals(competencia1.getCodigo())) {
                avaliacao.setAutoimportancia(importancia1);
                avaliacao.setAutodominio(dominio1);
                avaliacao.setImportancia(importancia1);
                avaliacao.setDominio(dominio1);
            } else {
                avaliacao.setAutoimportancia(importancia2);
                avaliacao.setAutodominio(dominio2);
                avaliacao.setImportancia(importancia2);
                avaliacao.setDominio(dominio2);
            }
            avaliacao.calculaGap();
            avaliacao.setSituacaoServidor(situacao);
        }
        avaliacaoServidorRepo.saveAllAndFlush(avaliacoes);
        recarregarContexto();
    }

    protected void preencherConsenso(
            String tituloServidor,
            int chefiaImportancia1,
            int chefiaDominio1,
            int consensoImportancia1,
            int consensoDominio1,
            int chefiaImportancia2,
            int chefiaDominio2,
            int consensoImportancia2,
            int consensoDominio2,
            SituacaoAvaliacaoServidor situacao
    ) {
        List<AvaliacaoServidor> avaliacoes = avaliacaoServidorRepo.buscarAvaliacoesDoServidor(
                diagnostico.getCodigo(), tituloServidor);
        for (AvaliacaoServidor avaliacao : avaliacoes) {
            if (avaliacao.getCompetencia().getCodigo().equals(competencia1.getCodigo())) {
                avaliacao.setChefiaImportancia(chefiaImportancia1);
                avaliacao.setChefiaDominio(chefiaDominio1);
                avaliacao.setImportancia(consensoImportancia1);
                avaliacao.setDominio(consensoDominio1);
            } else {
                avaliacao.setChefiaImportancia(chefiaImportancia2);
                avaliacao.setChefiaDominio(chefiaDominio2);
                avaliacao.setImportancia(consensoImportancia2);
                avaliacao.setDominio(consensoDominio2);
            }
            avaliacao.calculaGap();
            avaliacao.setSituacaoServidor(situacao);
        }
        avaliacaoServidorRepo.saveAllAndFlush(avaliacoes);
        recarregarContexto();
    }

    protected void preencherOcupacoesCriticas(String tituloServidor, SituacaoCapacitacao situacao1, SituacaoCapacitacao situacao2) {
        List<OcupacaoCritica> ocupacoes = ocupacaoCriticaRepo.listarPorDiagnostico(diagnostico.getCodigo()).stream()
                .filter(ocupacao -> ocupacao.getServidor().getTituloEleitoral().equals(tituloServidor))
                .toList();
        for (OcupacaoCritica ocupacao : ocupacoes) {
            ocupacao.setSituacaoCapacitacao(ocupacao.getCompetencia().getCodigo().equals(competencia1.getCodigo())
                    ? situacao1
                    : situacao2);
        }
        ocupacaoCriticaRepo.saveAllAndFlush(ocupacoes);
        recarregarContexto();
    }

    protected List<AvaliacaoServidor> buscarAvaliacoes(String tituloServidor) {
        return avaliacaoServidorRepo.buscarAvaliacoesDoServidor(diagnostico.getCodigo(), tituloServidor);
    }

    protected List<OcupacaoCritica> buscarOcupacoes(String tituloServidor) {
        return ocupacaoCriticaRepo.listarPorDiagnostico(diagnostico.getCodigo()).stream()
                .filter(ocupacao -> ocupacao.getServidor().getTituloEleitoral().equals(tituloServidor))
                .toList();
    }

    protected void recarregarContexto() {
        entityManager.flush();
        entityManager.clear();
        processo = processoRepo.findById(processo.getCodigo()).orElseThrow();
        subprocesso = subprocessoRepo.findById(subprocesso.getCodigo()).orElseThrow();
        diagnostico = diagnosticoRepo.findById(diagnostico.getCodigo()).orElseThrow();
        competencia1 = competenciaRepo.findById(competencia1.getCodigo()).orElseThrow();
        competencia2 = competenciaRepo.findById(competencia2.getCodigo()).orElseThrow();
        unidade = unidadeRepo.findById(unidade.getCodigo()).orElseThrow();
        servidor = usuarioRepo.findById(servidor.getTituloEleitoral()).orElseThrow();
        outroServidor = usuarioRepo.findById(outroServidor.getTituloEleitoral()).orElseThrow();
    }

    private AvaliacaoServidor novaAvaliacao(Usuario usuario, Competencia competencia) {
        return AvaliacaoServidor.builder()
                .diagnostico(diagnostico)
                .servidor(usuario)
                .servidorNomeSnapshot(usuario.getNome())
                .competencia(competencia)
                .situacaoServidor(SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA)
                .build();
    }

    private OcupacaoCritica novaOcupacaoCritica(Usuario usuario, Competencia competencia) {
        return OcupacaoCritica.builder()
                .diagnostico(diagnostico)
                .servidor(usuario)
                .servidorNomeSnapshot(usuario.getNome())
                .unidadeCodigoSnapshot(unidade.getCodigo())
                .unidadeSiglaSnapshot(unidade.getSigla())
                .unidadeNomeSnapshot(unidade.getNome())
                .competencia(competencia)
                .build();
    }

    private void garantirEmail(Usuario usuario) {
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            usuario.setEmail("usuario.%s@tre-pe.jus.br".formatted(usuario.getTituloEleitoral()));
        }
    }
}
