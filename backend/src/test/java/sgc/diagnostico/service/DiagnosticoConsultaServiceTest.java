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
import sgc.mapa.model.Competencia;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.ResponsavelUnidadeService;
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

    @InjectMocks
    DiagnosticoConsultaService service;

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
}
