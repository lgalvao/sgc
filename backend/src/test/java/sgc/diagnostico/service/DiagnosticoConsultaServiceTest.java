package sgc.diagnostico.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.model.ComumRepo;
import sgc.diagnostico.dto.DiagnosticoEquipeDto;
import sgc.diagnostico.dto.DiagnosticoUnidadeDto;
import sgc.diagnostico.model.AvaliacaoServidor;
import sgc.diagnostico.model.AvaliacaoServidorRepo;
import sgc.diagnostico.model.Diagnostico;
import sgc.diagnostico.model.SituacaoAvaliacaoServidor;
import sgc.diagnostico.model.SituacaoCapacitacao;
import sgc.diagnostico.model.SituacaoCapacitacaoRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.Competencia;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.ResponsavelUnidadeService;
import sgc.organizacao.service.UnidadeService;
import sgc.processo.model.Processo;
import sgc.subprocesso.SubprocessoDtoMapper;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoConsultaService;
import sgc.subprocesso.service.SubprocessoVisualizacaoService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagnosticoConsultaServiceTest {

    @Mock ComumRepo repo;
    @Mock AvaliacaoServidorRepo avaliacaoRepo;
    @Mock SituacaoCapacitacaoRepo situacaoCapacitacaoRepo;
    @Mock SubprocessoConsultaService subprocessoConsultaService;
    @Mock SubprocessoDtoMapper subprocessoDtoMapper;
    @Mock DiagnosticoUsuarioContextoService usuarioContextoService;
    @Mock SubprocessoVisualizacaoService subprocessoVisualizacaoService;
    @Mock ResponsavelUnidadeService responsavelUnidadeService;
    @Mock UnidadeService unidadeService;

    @InjectMocks
    DiagnosticoConsultaService service;

    @Test
    @DisplayName("obterContexto deve usar o mapa vigente da unidade quando o subprocesso nao tiver mapa proprio")
    void obterContexto_deveUsarMapaVigenteDaUnidade() {
        Long codSubprocesso = 400L;
        Long codDiagnostico = 900L;
        Unidade unidade = unidade(12L, "ASSESSORIA_12", "Assessoria 12");
        Subprocesso subprocesso = subprocesso(codSubprocesso, unidade);
        Diagnostico diagnostico = diagnostico(codDiagnostico);
        Mapa mapaVigente = new Mapa();
        mapaVigente.setCompetencias(java.util.Set.of(competencia(1L, "Competência vigente")));

        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        when(unidadeService.buscarMapaVigente(unidade.getCodigo())).thenReturn(Optional.of(mapaVigente));

        var dto = service.obterContexto(codSubprocesso);

        assertThat(dto.competencias()).singleElement().satisfies(item -> {
            assertThat(item.competenciaCodigo()).isEqualTo(1L);
            assertThat(item.descricao()).isEqualTo("Competência vigente");
        });
    }

    @Test
    @DisplayName("obterContexto deve falhar com erro interno quando nao houver mapa vigente da unidade")
    void obterContexto_deveFalharSemMapaVigente() {
        Long codSubprocesso = 400L;
        Long codDiagnostico = 900L;
        Unidade unidade = unidade(12L, "ASSESSORIA_12", "Assessoria 12");
        Subprocesso subprocesso = subprocesso(codSubprocesso, unidade);
        Diagnostico diagnostico = diagnostico(codDiagnostico);

        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        when(unidadeService.buscarMapaVigente(unidade.getCodigo())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obterContexto(codSubprocesso))
                .hasMessageContaining("Processo de diagnóstico sem mapa vigente");
    }

    @Test
    @DisplayName("obterEquipe deve ocultar o responsável da unidade da lista de servidores")
    void obterEquipe_deveOcultarResponsavelDaUnidade() {
        Long codSubprocesso = 400L;
        Long codDiagnostico = 900L;
        Unidade unidade = unidade(12L, "ASSESSORIA_12", "Assessoria 12");
        Subprocesso subprocesso = subprocesso(codSubprocesso, unidade);
        Diagnostico diagnostico = diagnostico(codDiagnostico);

        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        when(avaliacaoRepo.listarPorDiagnostico(codDiagnostico)).thenReturn(List.of(
                avaliacao("151515", "Chefe da Unidade", 1L, SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA),
                avaliacao("242426", "Servidor Avaliado", 2L, SituacaoAvaliacaoServidor.CONSENSO_CRIADO)
        ));
        when(responsavelUnidadeService.buscarResponsavelUnidadeOpt(unidade.getCodigo()))
                .thenReturn(Optional.of(new UnidadeResponsavelDto(
                        unidade.getCodigo(), "151515", "Chefe da Unidade", null, null
                )));

        DiagnosticoEquipeDto dto = service.obterEquipe(codSubprocesso);

        assertThat(dto.servidores())
                .extracting(DiagnosticoEquipeDto.Item::servidorTitulo)
                .containsExactly("242426");
    }

    @Test
    @DisplayName("obterEquipe deve desabilitar manter consenso para servidor com avaliação impossibilitada")
    void obterEquipe_deveDesabilitarManterConsensoParaImpossibilitado() {
        Long codSubprocesso = 400L;
        Long codDiagnostico = 900L;
        Unidade unidade = unidade(12L, "ASSESSORIA_12", "Assessoria 12");
        Subprocesso subprocesso = subprocesso(codSubprocesso, unidade);
        Diagnostico diagnostico = diagnostico(codDiagnostico);

        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        when(avaliacaoRepo.listarPorDiagnostico(codDiagnostico)).thenReturn(List.of(
                avaliacao("242426", "Servidor Avaliado", 2L, SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA)
        ));
        when(responsavelUnidadeService.buscarResponsavelUnidadeOpt(unidade.getCodigo()))
                .thenReturn(Optional.empty());

        DiagnosticoEquipeDto dto = service.obterEquipe(codSubprocesso);

        assertThat(dto.servidores()).singleElement().satisfies(item -> {
            assertThat(item.podeManterConsenso()).isFalse();
            assertThat(item.podePermitirAvaliacao()).isTrue();
            assertThat(item.podeImpossibilitar()).isFalse();
        });
    }

    @Test
    @DisplayName("obterConsenso deve ocultar consenso espelhado da autoavaliação quando a chefia ainda não preencheu nada")
    void obterConsenso_deveOcultarConsensoEspelhadoDaAutoavaliacao() {
        Long codSubprocesso = 400L;
        Long codDiagnostico = 900L;
        Diagnostico diagnostico = diagnostico(codDiagnostico);
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("151515");
        AvaliacaoServidor avaliacao = avaliacao("242426", "Servidor Avaliado", 1L, SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        avaliacao.setAutoimportancia(4);
        avaliacao.setAutodominio(3);
        avaliacao.setImportancia(4);
        avaliacao.setDominio(3);
        avaliacao.setChefiaImportancia(null);
        avaliacao.setChefiaDominio(null);

        when(usuarioContextoService.usuarioAutenticado()).thenReturn(usuario);
        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(codDiagnostico, "242426")).thenReturn(List.of(avaliacao));

        var dto = service.obterConsenso(codSubprocesso, "242426");

        assertThat(dto.competencias()).singleElement().satisfies(item -> {
            assertThat(item.servidorImportancia()).isEqualTo(4);
            assertThat(item.servidorDominio()).isEqualTo(3);
            assertThat(item.consensoImportancia()).isNull();
            assertThat(item.consensoDominio()).isNull();
        });
    }

    @Test
    @DisplayName("obterConsenso nao deve expor autoavaliacao salva por autosave antes da conclusao")
    void obterConsenso_naoDeveExporRascunhoAutoavaliacaoAntesDaConclusao() {
        Long codSubprocesso = 403L;
        Long codDiagnostico = 903L;
        Diagnostico diagnostico = diagnostico(codDiagnostico);
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("151515");
        AvaliacaoServidor avaliacao = avaliacao("242426", "Servidor Avaliado", 1L, SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA);
        avaliacao.setAutoimportancia(4);
        avaliacao.setAutodominio(3);
        avaliacao.setImportancia(4);
        avaliacao.setDominio(3);

        when(usuarioContextoService.usuarioAutenticado()).thenReturn(usuario);
        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(codDiagnostico, "242426")).thenReturn(List.of(avaliacao));

        var dto = service.obterConsenso(codSubprocesso, "242426");

        assertThat(dto.competencias()).singleElement().satisfies(item -> {
            assertThat(item.servidorImportancia()).isNull();
            assertThat(item.servidorDominio()).isNull();
        });
    }

    @Test
    @DisplayName("obterConsenso deve expor autoavaliacao do servidor apos conclusao")
    void obterConsenso_deveExporAutoavaliacaoAposConclusao() {
        Long codSubprocesso = 404L;
        Long codDiagnostico = 904L;
        Diagnostico diagnostico = diagnostico(codDiagnostico);
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("151515");
        AvaliacaoServidor avaliacao = avaliacao("242426", "Servidor Avaliado", 1L, SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        avaliacao.setAutoimportancia(4);
        avaliacao.setAutodominio(3);

        when(usuarioContextoService.usuarioAutenticado()).thenReturn(usuario);
        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(codDiagnostico, "242426")).thenReturn(List.of(avaliacao));

        var dto = service.obterConsenso(codSubprocesso, "242426");

        assertThat(dto.competencias()).singleElement().satisfies(item -> {
            assertThat(item.servidorImportancia()).isEqualTo(4);
            assertThat(item.servidorDominio()).isEqualTo(3);
        });
    }

    @Test
    @DisplayName("obterConsenso nao deve usar o ultimo valor corrente do servidor como consenso quando nao houver consenso salvo")
    void obterConsenso_naoDeveUsarUltimoValorCorrenteComoConsenso() {
        Long codSubprocesso = 401L;
        Long codDiagnostico = 901L;
        Diagnostico diagnostico = diagnostico(codDiagnostico);
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("151515");
        AvaliacaoServidor avaliacao = avaliacao("242426", "Servidor Avaliado", 1L, SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        avaliacao.setAutoimportancia(4);
        avaliacao.setAutodominio(3);
        avaliacao.setChefiaImportancia(6);
        avaliacao.setChefiaDominio(5);
        avaliacao.setConsensoImportancia(null);
        avaliacao.setConsensoDominio(null);
        avaliacao.setImportancia(4);
        avaliacao.setDominio(3);

        when(usuarioContextoService.usuarioAutenticado()).thenReturn(usuario);
        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(codDiagnostico, "242426")).thenReturn(List.of(avaliacao));

        var dto = service.obterConsenso(codSubprocesso, "242426");

        assertThat(dto.competencias()).singleElement().satisfies(item -> {
            assertThat(item.servidorImportancia()).isEqualTo(4);
            assertThat(item.chefiaImportancia()).isEqualTo(6);
            assertThat(item.consensoImportancia()).isNull();
            assertThat(item.consensoDominio()).isNull();
        });
    }

    @Test
    @DisplayName("obterConsenso deve desabilitar concluir avaliacao quando o consenso ja foi criado")
    void obterConsenso_deveDesabilitarConclusaoQuandoConsensoCriado() {
        Long codSubprocesso = 402L;
        Long codDiagnostico = 902L;
        Diagnostico diagnostico = diagnostico(codDiagnostico);
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("151515");
        AvaliacaoServidor avaliacao = avaliacao("242426", "Servidor Avaliado", 1L, SituacaoAvaliacaoServidor.CONSENSO_CRIADO);

        when(usuarioContextoService.usuarioAutenticado()).thenReturn(usuario);
        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(codDiagnostico, "242426")).thenReturn(List.of(avaliacao));

        var dto = service.obterConsenso(codSubprocesso, "242426");

        assertThat(dto.podeConcluirAvaliacao()).isTrue();
        assertThat(dto.habilitarConcluirAvaliacao()).isFalse();
    }

    @Test
    @DisplayName("obterDiagnosticoUnidade deve ocultar o responsável da unidade de servidores e situações de capacitação")
    void obterDiagnosticoUnidade_deveOcultarResponsavelDaUnidade() {
        Long codSubprocesso = 400L;
        Long codDiagnostico = 900L;
        Unidade unidade = unidade(12L, "ASSESSORIA_12", "Assessoria 12");
        Subprocesso subprocesso = subprocesso(codSubprocesso, unidade);
        Diagnostico diagnostico = diagnostico(codDiagnostico);

        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        when(subprocessoConsultaService.listarMovimentacoes(subprocesso)).thenReturn(List.of());
        when(avaliacaoRepo.listarPorDiagnostico(codDiagnostico)).thenReturn(List.of(
                avaliacao("151515", "Chefe da Unidade", 1L, SituacaoAvaliacaoServidor.CONSENSO_APROVADO),
                avaliacao("242426", "Servidor Avaliado", 1L, SituacaoAvaliacaoServidor.CONSENSO_APROVADO)
        ));
        when(situacaoCapacitacaoRepo.listarPorDiagnostico(codDiagnostico)).thenReturn(List.of(
                situacaoCapacitacao("151515", "Chefe da Unidade", 1L),
                situacaoCapacitacao("242426", "Servidor Avaliado", 1L)
        ));
        when(responsavelUnidadeService.buscarResponsavelUnidadeOpt(unidade.getCodigo()))
                .thenReturn(Optional.of(new UnidadeResponsavelDto(
                        unidade.getCodigo(), "151515", "Chefe da Unidade", null, null
                )));

        DiagnosticoUnidadeDto dto = service.obterDiagnosticoUnidade(codSubprocesso);

        assertThat(dto.servidores())
                .extracting(item -> item.servidorTitulo())
                .containsExactly("242426");
        assertThat(dto.situacoesCapacitacao())
                .extracting(item -> item.servidorTitulo())
                .containsExactly("242426");
    }

    @Test
    @DisplayName("obterDiagnosticoUnidade deve desabilitar manter consenso para servidor com avaliação impossibilitada")
    void obterDiagnosticoUnidade_deveDesabilitarManterConsensoParaImpossibilitado() {
        Long codSubprocesso = 400L;
        Long codDiagnostico = 900L;
        Unidade unidade = unidade(12L, "ASSESSORIA_12", "Assessoria 12");
        Subprocesso subprocesso = subprocesso(codSubprocesso, unidade);
        Diagnostico diagnostico = diagnostico(codDiagnostico);

        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        when(subprocessoConsultaService.listarMovimentacoes(subprocesso)).thenReturn(List.of());
        when(avaliacaoRepo.listarPorDiagnostico(codDiagnostico)).thenReturn(List.of(
                avaliacao("242426", "Servidor Avaliado", 1L, SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA)
        ));
        when(situacaoCapacitacaoRepo.listarPorDiagnostico(codDiagnostico)).thenReturn(List.of(
                situacaoCapacitacao("242426", "Servidor Avaliado", 1L)
        ));
        when(responsavelUnidadeService.buscarResponsavelUnidadeOpt(unidade.getCodigo()))
                .thenReturn(Optional.empty());

        DiagnosticoUnidadeDto dto = service.obterDiagnosticoUnidade(codSubprocesso);

        assertThat(dto.servidores()).singleElement().satisfies(item -> {
            assertThat(item.podeManterConsenso()).isFalse();
            assertThat(item.podePermitirAvaliacao()).isTrue();
            assertThat(item.podeImpossibilitar()).isFalse();
        });
    }

    @Test
    @DisplayName("obterDiagnosticoUnidade deve ocultar consenso espelhado da autoavaliação antes do preenchimento da chefia")
    void obterDiagnosticoUnidade_deveOcultarConsensoEspelhadoDaAutoavaliacao() {
        Long codSubprocesso = 400L;
        Long codDiagnostico = 900L;
        Unidade unidade = unidade(12L, "ASSESSORIA_12", "Assessoria 12");
        Subprocesso subprocesso = subprocesso(codSubprocesso, unidade);
        Diagnostico diagnostico = diagnostico(codDiagnostico);
        AvaliacaoServidor avaliacao = avaliacao("242426", "Servidor Avaliado", 1L, SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        avaliacao.setAutoimportancia(4);
        avaliacao.setAutodominio(3);
        avaliacao.setImportancia(4);
        avaliacao.setDominio(3);
        avaliacao.setChefiaImportancia(null);
        avaliacao.setChefiaDominio(null);

        when(repo.buscar(Diagnostico.class, Map.of("subprocesso.codigo", codSubprocesso))).thenReturn(diagnostico);
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        when(subprocessoConsultaService.listarMovimentacoes(subprocesso)).thenReturn(List.of());
        when(avaliacaoRepo.listarPorDiagnostico(codDiagnostico)).thenReturn(List.of(avaliacao));
        when(situacaoCapacitacaoRepo.listarPorDiagnostico(codDiagnostico)).thenReturn(List.of());
        when(responsavelUnidadeService.buscarResponsavelUnidadeOpt(unidade.getCodigo())).thenReturn(Optional.empty());

        DiagnosticoUnidadeDto dto = service.obterDiagnosticoUnidade(codSubprocesso);

        assertThat(dto.servidores()).singleElement().satisfies(item ->
                assertThat(item.consenso()).singleElement().satisfies(consenso -> {
                    assertThat(consenso.importancia()).isNull();
                    assertThat(consenso.dominio()).isNull();
                }));
    }

    private Diagnostico diagnostico(Long codigo) {
        Diagnostico diagnostico = new Diagnostico();
        diagnostico.setCodigo(codigo);
        return diagnostico;
    }

    private Subprocesso subprocesso(Long codigo, Unidade unidade) {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(codigo);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);
        Processo processo = new Processo();
        processo.adicionarParticipantes(java.util.Set.of(unidade));
        subprocesso.setProcesso(processo);
        return subprocesso;
    }

    private Unidade unidade(Long codigo, String sigla, String nome) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setSigla(sigla);
        unidade.setNome(nome);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        return unidade;
    }

    private AvaliacaoServidor avaliacao(String titulo, String nome, Long competenciaCodigo, SituacaoAvaliacaoServidor situacao) {
        AvaliacaoServidor avaliacao = new AvaliacaoServidor();
        Usuario servidor = new Usuario();
        servidor.setTituloEleitoral(titulo);
        avaliacao.setServidor(servidor);
        avaliacao.setServidorNomeSnapshot(nome);
        Competencia competencia = new Competencia();
        competencia.setCodigo(competenciaCodigo);
        competencia.setDescricao("Competência " + competenciaCodigo);
        avaliacao.setCompetencia(competencia);
        avaliacao.setSituacaoServidor(situacao);
        avaliacao.setImportancia(4);
        avaliacao.setDominio(3);
        return avaliacao;
    }

    private SituacaoCapacitacao situacaoCapacitacao(String titulo, String nome, Long competenciaCodigo) {
        SituacaoCapacitacao situacao = new SituacaoCapacitacao();
        Usuario servidor = new Usuario();
        servidor.setTituloEleitoral(titulo);
        situacao.setServidor(servidor);
        situacao.setServidorNomeSnapshot(nome);
        Competencia competencia = new Competencia();
        competencia.setCodigo(competenciaCodigo);
        situacao.setCompetencia(competencia);
        return situacao;
    }

    private Competencia competencia(Long codigo, String descricao) {
        Competencia competencia = new Competencia();
        competencia.setCodigo(codigo);
        competencia.setDescricao(descricao);
        return competencia;
    }
}
