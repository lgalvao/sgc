package sgc.diagnostico.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.model.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticoAvaliacaoServiceTest {

    @Mock DiagnosticoRepo diagnosticoRepo;
    @Mock AvaliacaoServidorRepo avaliacaoRepo;
    @Mock SituacaoCapacitacaoRepo situacaoCapacitacaoRepo;
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
    @DisplayName("salvarConsenso com detalhados: deve gravar chefia, consenso persistido e valor corrente sem concluir")
    void salvarConsenso_comDetalhados_deveGravarCamposDaChefia() {
        Long codSubprocesso = 3L;
        Long diagCodigo = 30L;
        String servidorTitulo = "servidor@titulo";

        Diagnostico diagnostico = diagnosticoComCodigo(diagCodigo);
        AvaliacaoServidor avaliacao = avaliacaoComNota(200L, 2, 2);

        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, servidorTitulo)).thenReturn(List.of(avaliacao));

        var detalhada = new ConsensoCompetenciaDto(200L, null, 2, 2, 4, 3, 4, 3);
        var request = new ConsensoRequest(
                List.of(detalhada)
        );

        service.salvarConsenso(codSubprocesso, request, servidorTitulo);

        assertThat(avaliacao.getChefiaImportancia()).isEqualTo(4);
        assertThat(avaliacao.getChefiaDominio()).isEqualTo(3);
        assertThat(avaliacao.getConsensoImportancia()).isEqualTo(4);
        assertThat(avaliacao.getConsensoDominio()).isEqualTo(3);
        assertThat(avaliacao.getImportancia()).isEqualTo(4);   // consenso
        assertThat(avaliacao.getDominio()).isEqualTo(3);        // consenso
        assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
    }

    @Test
    @DisplayName("salvarConsenso sem detalhados: deve usar competencias simples e igualar chefia ao consenso sem concluir")
    void salvarConsenso_semDetalhados_deveUsarCompetenciasSimples() {
        Long codSubprocesso = 4L;
        Long diagCodigo = 40L;
        String servidorTitulo = "servidor@titulo";

        Diagnostico diagnostico = diagnosticoComCodigo(diagCodigo);
        AvaliacaoServidor avaliacao = avaliacaoComNota(201L, 1, 1);

        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, servidorTitulo)).thenReturn(List.of(avaliacao));

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
        assertThat(avaliacao.getConsensoImportancia()).isEqualTo(5);
        assertThat(avaliacao.getConsensoDominio()).isEqualTo(3);
        assertThat(avaliacao.getImportancia()).isEqualTo(5);
        assertThat(avaliacao.getDominio()).isEqualTo(3);
        assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
    }

    @Test
    @DisplayName("salvarConsenso com subconjunto: deve permitir preenchimento em etapas sem exigir todas as competências nem concluir")
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
        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, servidorTitulo)).thenReturn(List.of(avaliacao1, avaliacao2));

        var request = new ConsensoRequest(
                List.of(ConsensoCompetenciaDto.builder()
                        .competenciaCodigo(competenciaPrimeira)
                        .servidorImportancia(autoimportanciaPrimeira)
                        .servidorDominio(autodominioPrimeira)
                        .chefiaImportancia(chefiaImportanciaPrimeira)
                        .chefiaDominio(chefiaDominioPrimeira)
                        .consensoImportancia(chefiaImportanciaPrimeira)
                        .consensoDominio(chefiaDominioPrimeira)
                        .build())
        );

        service.salvarConsenso(codSubprocesso, request, servidorTitulo);

        assertThat(avaliacao1.getChefiaImportancia()).isEqualTo(chefiaImportanciaPrimeira);
        assertThat(avaliacao1.getChefiaDominio()).isEqualTo(chefiaDominioPrimeira);
        assertThat(avaliacao1.getConsensoImportancia()).isEqualTo(chefiaImportanciaPrimeira);
        assertThat(avaliacao1.getConsensoDominio()).isEqualTo(chefiaDominioPrimeira);
        assertThat(avaliacao1.getImportancia()).isEqualTo(chefiaImportanciaPrimeira);
        assertThat(avaliacao1.getDominio()).isEqualTo(chefiaDominioPrimeira);
        assertThat(avaliacao2.getChefiaImportancia()).isNull();
        assertThat(avaliacao2.getChefiaDominio()).isNull();
        assertThat(avaliacao2.getConsensoImportancia()).isNull();
        assertThat(avaliacao2.getConsensoDominio()).isNull();
        assertThat(avaliacao2.getImportancia()).isEqualTo(autoimportanciaSegunda);
        assertThat(avaliacao2.getDominio()).isEqualTo(autodominioSegunda);
        assertThat(avaliacao1.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        assertThat(avaliacao2.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
    }

    @Test
    @DisplayName("concluirConsenso: deve validar preenchimento completo, concluir e notificar o servidor")
    void concluirConsenso_deveConcluirENotificar() {
        Long codSubprocesso = 42L;
        Long diagCodigo = 420L;
        String servidorTitulo = "servidor@titulo";

        Diagnostico diagnostico = diagnosticoComCodigo(diagCodigo);
        AvaliacaoServidor avaliacao1 = avaliacaoComNota(301L, 2, 2);
        AvaliacaoServidor avaliacao2 = avaliacaoComNota(302L, 3, 1);
        avaliacao1.setChefiaImportancia(5);
        avaliacao1.setChefiaDominio(4);
        avaliacao1.setConsensoImportancia(5);
        avaliacao1.setConsensoDominio(4);
        avaliacao1.setImportancia(5);
        avaliacao1.setDominio(4);
        avaliacao2.setChefiaImportancia(4);
        avaliacao2.setChefiaDominio(3);
        avaliacao2.setConsensoImportancia(4);
        avaliacao2.setConsensoDominio(3);
        avaliacao2.setImportancia(4);
        avaliacao2.setDominio(3);
        Subprocesso subprocesso = mock(Subprocesso.class);

        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, servidorTitulo)).thenReturn(List.of(avaliacao1, avaliacao2));
        when(subprocessoConsultaService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);

        service.concluirConsenso(codSubprocesso, servidorTitulo);

        assertThat(avaliacao1.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.CONSENSO_CRIADO);
        assertThat(avaliacao2.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.CONSENSO_CRIADO);
        verify(notificacaoService).notificarConsensoDisponivel(subprocesso, servidorTitulo);
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

    @Test
    @DisplayName("aprovarConsenso: deve falhar quando consenso ja estiver aprovado")
    void aprovarConsenso_deveFalharQuandoJaAprovado() {
        Long codSubprocesso = 51L;
        Long diagCodigo = 510L;
        String titulo = "servidor@titulo";

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);

        Diagnostico diagnostico = diagnosticoComCodigo(diagCodigo);
        AvaliacaoServidor av1 = avaliacaoVazia(300L);
        av1.setSituacaoServidor(SituacaoAvaliacaoServidor.CONSENSO_APROVADO);
        AvaliacaoServidor av2 = avaliacaoVazia(301L);
        av2.setSituacaoServidor(SituacaoAvaliacaoServidor.CONSENSO_APROVADO);

        when(usuarioContextoService.usuarioAutenticado()).thenReturn(usuario);
        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, titulo)).thenReturn(List.of(av1, av2));

        assertThatThrownBy(() -> service.aprovarConsenso(codSubprocesso))
                .isInstanceOf(ErroValidacao.class)
                .hasMessage(Mensagens.CONSENSO_JA_APROVADO);

        verify(avaliacaoRepo, never()).saveAll(anyList());
        verifyNoInteractions(notificacaoService);
    }

    // ─── impossibilitarAvaliacao ───────────────────────────────────────────

    @Test
    @DisplayName("impossibilitarAvaliacao: deve preservar os dados existentes e mudar situacao para AVALIACAO_IMPOSSIBILITADA")
    void impossibilitarAvaliacao_devePreservarDadosEMudarSituacao() {
        Long codSubprocesso = 6L;
        Long diagCodigo = 60L;
        String servidorTitulo = "servidor@titulo";
        String justificativa = "Servidor em licença médica";

        Diagnostico diagnostico = diagnosticoComCodigo(diagCodigo);
        AvaliacaoServidor avaliacao = avaliacaoComNota(400L, 3, 3);
        avaliacao.setGap(0);
        avaliacao.setChefiaImportancia(5);
        avaliacao.setChefiaDominio(4);
        avaliacao.setConsensoImportancia(4);
        avaliacao.setConsensoDominio(2);
        avaliacao.setImportancia(4);
        avaliacao.setDominio(2);

        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, servidorTitulo)).thenReturn(List.of(avaliacao));

        service.impossibilitarAvaliacao(codSubprocesso, servidorTitulo, justificativa);

        assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);
        assertThat(avaliacao.getImportancia()).isEqualTo(4);
        assertThat(avaliacao.getDominio()).isEqualTo(2);
        assertThat(avaliacao.getGap()).isEqualTo(0);
        assertThat(avaliacao.getChefiaImportancia()).isEqualTo(5);
        assertThat(avaliacao.getChefiaDominio()).isEqualTo(4);
        assertThat(avaliacao.getConsensoImportancia()).isEqualTo(4);
        assertThat(avaliacao.getConsensoDominio()).isEqualTo(2);
        assertThat(avaliacao.getSituacaoServidorAnterior()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        assertThat(avaliacao.getObservacao()).isEqualTo(justificativa);
    }

    @Test
    @DisplayName("reverterImpossibilidade: deve voltar para AUTOAVALIACAO_CONCLUIDA quando houver chefia preenchida mas consenso incompleto")
    void reverterImpossibilidade_deveVoltarParaAutoavaliacaoConcluidaQuandoConsensoIncompleto() {
        Long codSubprocesso = 7L;
        Long diagCodigo = 70L;
        String servidorTitulo = "servidor@titulo";

        Diagnostico diagnostico = diagnosticoComCodigo(diagCodigo);
        AvaliacaoServidor avaliacao = avaliacaoComNota(401L, 3, 2);
        avaliacao.setSituacaoServidor(SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);
        avaliacao.setSituacaoServidorAnterior(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        avaliacao.setChefiaImportancia(5);
        avaliacao.setChefiaDominio(4);
        avaliacao.setConsensoImportancia(null);
        avaliacao.setConsensoDominio(null);
        avaliacao.setImportancia(3);
        avaliacao.setDominio(2);
        avaliacao.setObservacao("Licença");

        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, servidorTitulo)).thenReturn(List.of(avaliacao));

        service.reverterImpossibilidade(codSubprocesso, servidorTitulo);

        assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        assertThat(avaliacao.getImportancia()).isEqualTo(3);
        assertThat(avaliacao.getDominio()).isEqualTo(2);
        assertThat(avaliacao.getChefiaImportancia()).isEqualTo(5);
        assertThat(avaliacao.getChefiaDominio()).isEqualTo(4);
        assertThat(avaliacao.getConsensoImportancia()).isNull();
        assertThat(avaliacao.getConsensoDominio()).isNull();
        assertThat(avaliacao.getSituacaoServidorAnterior()).isNull();
        assertThat(avaliacao.getObservacao()).isNull();
    }

    @Test
    @DisplayName("reverterImpossibilidade: deve restaurar AUTOAVALIACAO_CONCLUIDA quando a impossibilitacao ocorreu antes de concluir consenso manual")
    void reverterImpossibilidade_deveRestaurarSituacaoAnteriorQuandoHouverRascunhoManualSalvo() {
        Long codSubprocesso = 8L;
        Long diagCodigo = 80L;
        String servidorTitulo = "servidor@titulo";

        Diagnostico diagnostico = diagnosticoComCodigo(diagCodigo);
        AvaliacaoServidor avaliacao = avaliacaoComNota(402L, 3, 2);
        avaliacao.setChefiaImportancia(5);
        avaliacao.setChefiaDominio(4);
        avaliacao.setConsensoImportancia(4);
        avaliacao.setConsensoDominio(3);
        avaliacao.setImportancia(4);
        avaliacao.setDominio(3);

        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, servidorTitulo)).thenReturn(List.of(avaliacao));

        service.impossibilitarAvaliacao(codSubprocesso, servidorTitulo, "Licença");

        assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);

        service.reverterImpossibilidade(codSubprocesso, servidorTitulo);

        assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA);
        assertThat(avaliacao.getImportancia()).isEqualTo(3);
        assertThat(avaliacao.getDominio()).isEqualTo(2);
        assertThat(avaliacao.getChefiaImportancia()).isEqualTo(5);
        assertThat(avaliacao.getChefiaDominio()).isEqualTo(4);
        assertThat(avaliacao.getConsensoImportancia()).isEqualTo(4);
        assertThat(avaliacao.getConsensoDominio()).isEqualTo(3);
        assertThat(avaliacao.getSituacaoServidorAnterior()).isNull();
        assertThat(avaliacao.getObservacao()).isNull();
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

    @Test
    @DisplayName("reverterImpossibilidade: deve manter notas nulas ao reverter impossibilitação para servidor que nunca iniciou autoavaliação")
    void reverterImpossibilidade_semAutoavaliacaoPreenchida_deveManterCamposNulos() {
        Long codSubprocesso = 9L;
        Long diagCodigo = 90L;
        String servidorTitulo = "servidor@titulo";

        Diagnostico diagnostico = diagnosticoComCodigo(diagCodigo);
        AvaliacaoServidor avaliacao = avaliacaoVazia(501L);
        avaliacao.setSituacaoServidor(SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);
        avaliacao.setSituacaoServidorAnterior(SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA);
        avaliacao.setObservacao("Licença");

        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, servidorTitulo)).thenReturn(List.of(avaliacao));

        service.reverterImpossibilidade(codSubprocesso, servidorTitulo);

        assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA);
        assertThat(avaliacao.getImportancia()).isNull();
        assertThat(avaliacao.getDominio()).isNull();
        assertThat(avaliacao.getAutoimportancia()).isNull();
        assertThat(avaliacao.getAutodominio()).isNull();
        assertThat(avaliacao.getGap()).isNull();
        assertThat(avaliacao.getSituacaoServidorAnterior()).isNull();
        assertThat(avaliacao.getObservacao()).isNull();
    }

    @Test
    @DisplayName("reverterImpossibilidade: deve restaurar notas salvas temporariamente ao reverter impossibilitação para servidor que preencheu mas não concluiu autoavaliação")
    void reverterImpossibilidade_comAutoavaliacaoSalvaTemporariamente_deveRestaurarNotas() {
        Long codSubprocesso = 10L;
        Long diagCodigo = 100L;
        String servidorTitulo = "servidor@titulo";

        Diagnostico diagnostico = diagnosticoComCodigo(diagCodigo);
        AvaliacaoServidor avaliacao = avaliacaoVazia(601L);
        // O servidor salvou temporariamente as notas
        avaliacao.setAutoimportancia(4);
        avaliacao.setAutodominio(3);
        avaliacao.setImportancia(4);
        avaliacao.setDominio(3);
        avaliacao.calculaGap();
        // Mas a situação ainda era AUTOAVALIACAO_NAO_INICIADA quando foi impossibilitado
        avaliacao.setSituacaoServidor(SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);
        avaliacao.setSituacaoServidorAnterior(SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA);
        avaliacao.setObservacao("Licença");

        when(diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)).thenReturn(Optional.of(diagnostico));
        when(avaliacaoRepo.buscarAvaliacoesDoServidor(diagCodigo, servidorTitulo)).thenReturn(List.of(avaliacao));

        service.reverterImpossibilidade(codSubprocesso, servidorTitulo);

        assertThat(avaliacao.getSituacaoServidor()).isEqualTo(SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA);
        assertThat(avaliacao.getImportancia()).isEqualTo(4);
        assertThat(avaliacao.getDominio()).isEqualTo(3);
        assertThat(avaliacao.getAutoimportancia()).isEqualTo(4);
        assertThat(avaliacao.getAutodominio()).isEqualTo(3);
        assertThat(avaliacao.getGap()).isEqualTo(1); // 4 - 3 = 1
        assertThat(avaliacao.getSituacaoServidorAnterior()).isNull();
        assertThat(avaliacao.getObservacao()).isNull();
    }
}

