package sgc.diagnostico.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.model.*;
import sgc.mapa.model.Competencia;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoConsultaService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticoAvaliacaoServiceTest {

    @Mock DiagnosticoRepo diagnosticoRepo;
    @Mock AvaliacaoServidorRepo avaliacaoRepo;
    @Mock OcupacaoCriticaRepo ocupacaoRepo;
    @Mock DiagnosticoGapService gapService;
    @Mock DiagnosticoValidacaoService validacaoService;
    @Mock DiagnosticoNotificacaoService notificacaoService;
    @Mock SubprocessoConsultaService subprocessoConsultaService;
    @Mock DiagnosticoUsuarioContextoService usuarioContextoService;

    @InjectMocks
    DiagnosticoAvaliacaoService service;

    // Helpers para construir objetos de teste

    private Diagnostico diagnosticoComCodigo(Long codigo) {
        Diagnostico d = new Diagnostico();
        d.setCodigo(codigo);
        return d;
    }

    private Competencia competenciaComCodigo(Long codigo) {
        Competencia c = new Competencia();
        c.setCodigo(codigo);
        return c;
    }

    private AvaliacaoServidor avaliacaoVazia(Long competenciaCodigo) {
        AvaliacaoServidor a = new AvaliacaoServidor();
        a.setCompetencia(competenciaComCodigo(competenciaCodigo));
        a.setSituacaoServidor(SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA);
        return a;
    }

    private AvaliacaoServidor avaliacaoComNota(Long competenciaCodigo, Integer autoimportancia, Integer autodominio) {
        AvaliacaoServidor a = avaliacaoVazia(competenciaCodigo);
        a.setAutoimportancia(autoimportancia);
        a.setAutodominio(autodominio);
        a.setImportancia(autoimportancia);
        a.setDominio(autodominio);
        a.setSituacaoServidor(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        return a;
    }

    // ─── salvarAutoavaliacao ───────────────────────────────────────────────

    @Test
    @DisplayName("salvarAutoavaliacao: deve gravar autoimportancia e autodominio mantendo iguais a importancia e dominio")
    void salvarAutoavaliacao_deveGravarAutoimportanciaEAutodominio() {
        Long codSubprocesso = 1L;
        Long diagCodigo = 10L;
        String titulo = "servidor@titulo";

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);

        Diagnostico diagnostico = diagnosticoComCodigo(diagCodigo);
        AvaliacaoServidor avaliacao = avaliacaoVazia(100L);
        Subprocesso subprocesso = new Subprocesso();
        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.DIAGNOSTICO);
        subprocesso.setProcesso(processo);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);

        when(usuarioContextoService.usuarioAutenticado()).thenReturn(usuario);
        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, titulo)).thenReturn(List.of(avaliacao));
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);

        var request = new AutoavaliacaoRequest(List.of(
                AvaliacaoCompetenciaDto.builder()
                        .competenciaCodigo(100L)
                        .importancia(4)
                        .dominio(2)
                        .build()
        ));

        service.salvarAutoavaliacao(codSubprocesso, request);

        assertThat(avaliacao.getAutoimportancia()).isEqualTo(4);
        assertThat(avaliacao.getAutodominio()).isEqualTo(2);
        assertThat(avaliacao.getImportancia()).isEqualTo(4);
        assertThat(avaliacao.getDominio()).isEqualTo(2);
        assertThat(subprocesso.getSituacao()).isEqualTo(SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);
        verify(avaliacaoRepo).saveAll(anyList());
    }

    // ─── concluirAutoavaliacao ─────────────────────────────────────────────

    @Test
    @DisplayName("concluirAutoavaliacao: deve mudar situacao de todas as avaliacoes para AUTOAVALIACAO_CONCLUIDA")
    void concluirAutoavaliacao_deveMudarSituacaoParaConcluida() {
        Long codSubprocesso = 2L;
        Long diagCodigo = 20L;
        String titulo = "servidor@titulo";

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);

        Diagnostico diagnostico = diagnosticoComCodigo(diagCodigo);
        AvaliacaoServidor av1 = avaliacaoVazia(101L);
        AvaliacaoServidor av2 = avaliacaoVazia(102L);

        Subprocesso subprocesso = mock(Subprocesso.class);

        when(usuarioContextoService.usuarioAutenticado()).thenReturn(usuario);
        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, titulo)).thenReturn(List.of(av1, av2));
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        doNothing().when(validacaoService).validarAutoavaliacaoCompleta(diagCodigo, titulo);

        service.concluirAutoavaliacao(codSubprocesso);

        assertThat(av1.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        assertThat(av2.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
    }

    // ─── salvarConsenso ────────────────────────────────────────────────────

    @Test
    @DisplayName("salvarConsenso com detalhados: deve gravar chefiaImportancia, chefiaDominio, importancia (consenso) e dominio (consenso)")
    void salvarConsenso_comDetalhados_deveGravarCamposDaChefia() {
        Long codSubprocesso = 3L;
        Long diagCodigo = 30L;
        String servidorTitulo = "servidor@titulo";

        Diagnostico diagnostico = diagnosticoComCodigo(diagCodigo);
        AvaliacaoServidor avaliacao = avaliacaoComNota(200L, 2, 2);

        Subprocesso subprocesso = mock(Subprocesso.class);

        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, servidorTitulo)).thenReturn(List.of(avaliacao));
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);

        var detalhada = new ConsensoCompetenciaDto(200L, 2, 2, 4, 3, 4, 3);
        var request = new ConsensoRequest(
                List.of(detalhada)
        );

        service.salvarConsenso(codSubprocesso, request, servidorTitulo);

        assertThat(avaliacao.getChefiaImportancia()).isEqualTo(4);
        assertThat(avaliacao.getChefiaDominio()).isEqualTo(3);
        assertThat(avaliacao.getImportancia()).isEqualTo(4);   // consenso
        assertThat(avaliacao.getDominio()).isEqualTo(3);        // consenso
        assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.CONSENSO_CRIADO);
    }

    @Test
    @DisplayName("salvarConsenso sem detalhados: deve usar competencias simples e igualar chefia ao consenso")
    void salvarConsenso_semDetalhados_deveUsarCompetenciasSimples() {
        Long codSubprocesso = 4L;
        Long diagCodigo = 40L;
        String servidorTitulo = "servidor@titulo";

        Diagnostico diagnostico = diagnosticoComCodigo(diagCodigo);
        AvaliacaoServidor avaliacao = avaliacaoComNota(201L, 1, 1);

        Subprocesso subprocesso = mock(Subprocesso.class);

        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, servidorTitulo)).thenReturn(List.of(avaliacao));
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);

        var detalhada = ConsensoCompetenciaDto.builder()
                .competenciaCodigo(201L)
                .consensoImportancia(5)
                .consensoDominio(3)
                .chefiaImportancia(5)
                .chefiaDominio(3)
                .build();
        var request = new ConsensoRequest(List.of(detalhada));

        service.salvarConsenso(codSubprocesso, request, servidorTitulo);

        assertThat(avaliacao.getChefiaImportancia()).isEqualTo(5);
        assertThat(avaliacao.getChefiaDominio()).isEqualTo(3);
        assertThat(avaliacao.getImportancia()).isEqualTo(5);
        assertThat(avaliacao.getDominio()).isEqualTo(3);
        assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.CONSENSO_CRIADO);
    }

    @Test
    @DisplayName("salvarConsenso com subconjunto: deve permitir preenchimento em etapas sem exigir todas as competências")
    void salvarConsenso_comSubconjunto_devePermitirPreenchimentoEmEtapas() {
        Long codSubprocesso = 41L;
        Long diagCodigo = 410L;
        String servidorTitulo = "servidor@titulo";
        Long competenciaPrimeira = 301L;
        Long competenciaSegunda = 302L;
        int autoimportanciaPrimeira = 2;
        int autodominioPrimeira = 2;
        int autoimportanciaSegunda = 3;
        int autodominioSegunda = 1;
        int chefiaImportanciaPrimeira = 5;
        int chefiaDominioPrimeira = 4;

        Diagnostico diagnostico = diagnosticoComCodigo(diagCodigo);
        AvaliacaoServidor avaliacao1 = avaliacaoComNota(competenciaPrimeira, autoimportanciaPrimeira, autodominioPrimeira);
        AvaliacaoServidor avaliacao2 = avaliacaoComNota(competenciaSegunda, autoimportanciaSegunda, autodominioSegunda);
        Subprocesso subprocesso = mock(Subprocesso.class);

        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, servidorTitulo)).thenReturn(List.of(avaliacao1, avaliacao2));
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);

        var request = new ConsensoRequest(
                List.of(ConsensoCompetenciaDto.builder()
                        .competenciaCodigo(competenciaPrimeira)
                        .autoimportancia(autoimportanciaPrimeira)
                        .autodominio(autodominioPrimeira)
                        .chefiaImportancia(chefiaImportanciaPrimeira)
                        .chefiaDominio(chefiaDominioPrimeira)
                        .consensoImportancia(chefiaImportanciaPrimeira)
                        .consensoDominio(chefiaDominioPrimeira)
                        .build())
        );

        service.salvarConsenso(codSubprocesso, request, servidorTitulo);

        assertThat(avaliacao1.getChefiaImportancia()).isEqualTo(chefiaImportanciaPrimeira);
        assertThat(avaliacao1.getChefiaDominio()).isEqualTo(chefiaDominioPrimeira);
        assertThat(avaliacao1.getImportancia()).isEqualTo(chefiaImportanciaPrimeira);
        assertThat(avaliacao1.getDominio()).isEqualTo(chefiaDominioPrimeira);
        assertThat(avaliacao2.getChefiaImportancia()).isNull();
        assertThat(avaliacao2.getChefiaDominio()).isNull();
        assertThat(avaliacao2.getImportancia()).isEqualTo(autoimportanciaSegunda);
        assertThat(avaliacao2.getDominio()).isEqualTo(autodominioSegunda);
        assertThat(avaliacao1.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.CONSENSO_CRIADO);
        assertThat(avaliacao2.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.CONSENSO_CRIADO);
    }

    // ─── aprovarConsenso ───────────────────────────────────────────────────

    @Test
    @DisplayName("aprovarConsenso: deve mudar situacao de todas as avaliacoes para CONSENSO_APROVADO")
    void aprovarConsenso_deveMudarSituacaoParaAprovado() {
        Long codSubprocesso = 5L;
        Long diagCodigo = 50L;
        String titulo = "servidor@titulo";

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);

        Diagnostico diagnostico = diagnosticoComCodigo(diagCodigo);
        AvaliacaoServidor av1 = avaliacaoVazia(300L);
        av1.setSituacaoServidor(SituacaoAvaliacaoServidor.CONSENSO_CRIADO);
        AvaliacaoServidor av2 = avaliacaoVazia(301L);
        av2.setSituacaoServidor(SituacaoAvaliacaoServidor.CONSENSO_CRIADO);

        Subprocesso subprocesso = mock(Subprocesso.class);

        when(usuarioContextoService.usuarioAutenticado()).thenReturn(usuario);
        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, titulo)).thenReturn(List.of(av1, av2));
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);

        service.aprovarConsenso(codSubprocesso);

        assertThat(av1.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.CONSENSO_APROVADO);
        assertThat(av2.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.CONSENSO_APROVADO);
    }

    // ─── impossibilitarAvaliacao ───────────────────────────────────────────

    @Test
    @DisplayName("impossibilitarAvaliacao: deve limpar campos de nota e mudar situacao para AVALIACAO_IMPOSSIBILITADA")
    void impossibilitarAvaliacao_deveLimparCamposEMudarSituacao() {
        Long codSubprocesso = 6L;
        Long diagCodigo = 60L;
        String servidorTitulo = "servidor@titulo";
        String justificativa = "Servidor em licença médica";

        Diagnostico diagnostico = diagnosticoComCodigo(diagCodigo);
        AvaliacaoServidor avaliacao = avaliacaoComNota(400L, 3, 3);
        avaliacao.setGap(0);

        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, servidorTitulo)).thenReturn(List.of(avaliacao));

        service.impossibilitarAvaliacao(codSubprocesso, servidorTitulo, justificativa);

        assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);
        assertThat(avaliacao.getImportancia()).isNull();
        assertThat(avaliacao.getDominio()).isNull();
        assertThat(avaliacao.getGap()).isNull();
        assertThat(avaliacao.getObservacao()).isEqualTo(justificativa);
    }

    // ─── findBySubprocessoCodigo não encontrado ────────────────────────────

    @Test
    @DisplayName("salvarConsenso: deve lancar ErroEntidadeNaoEncontrada quando diagnostico nao existir")
    void salvarConsenso_diagnosticoInexistente_deveLancarErro() {
        when(diagnosticoRepo.findBySubprocessoCodigo(anyLong())).thenReturn(Optional.empty());

        var request = new ConsensoRequest(List.of());

        assertThatThrownBy(() -> service.salvarConsenso(99L, request, "titulo"))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
}
