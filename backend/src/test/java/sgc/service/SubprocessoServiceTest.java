package sgc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.analise.modelo.AnaliseCadastro;
import sgc.analise.modelo.AnaliseCadastroRepo;
import sgc.analise.modelo.AnaliseValidacaoRepo;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.modelo.SituacaoSubprocesso;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.comum.modelo.Usuario;
import sgc.conhecimento.dto.ConhecimentoMapper;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.mapa.ImpactoMapaService;
import sgc.mapa.modelo.Mapa;
import sgc.notificacao.NotificacaoServico;
import sgc.processo.modelo.Processo;
import sgc.subprocesso.SubprocessoService;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import org.springframework.context.ApplicationEventPublisher;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SubprocessoServiceTest {
    @Mock
    private SubprocessoRepo repositorioSubprocesso;

    @Mock
    private MovimentacaoRepo repositorioMovimentacao;

    @Mock
    private AtividadeRepo atividadeRepo;

    @Mock
    private ConhecimentoRepo repositorioConhecimento;

    @Mock
    private AnaliseCadastroRepo repositorioAnaliseCadastro;

    @Mock
    private AnaliseValidacaoRepo repositorioAnaliseValidacao;

    @Mock
    private CompetenciaRepo competenciaRepo;

    @Mock
    private CompetenciaAtividadeRepo competenciaAtividadeRepo;

    @Mock
    private NotificacaoServico notificacaoServico;

    @Mock
    private ApplicationEventPublisher publicadorDeEventos;

    @Mock
    private AlertaRepo repositorioAlerta;

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private AtividadeMapper atividadeMapper;

    @Mock
    private ConhecimentoMapper conhecimentoMapper;

    @Mock
    private MovimentacaoMapper movimentacaoMapper;

    @Mock
    private SubprocessoMapper subprocessoMapper;

    @Mock
    private ImpactoMapaService impactoMapaService;

    @InjectMocks
    private SubprocessoService subprocessoService;

    private static final String SUP = "SUP";
    private Unidade unidadeMock;
    private Unidade unidadeSuperiorMock;
    private Subprocesso subprocessoMock;
    private Usuario usuario;

    @BeforeEach
    void setupBasico() {
        Processo processoMock = mock(Processo.class);
        when(processoMock.getDescricao()).thenReturn("Processo de Teste");

        unidadeSuperiorMock = mock(Unidade.class);
        when(unidadeSuperiorMock.getSigla()).thenReturn(SUP);

        unidadeMock = mock(Unidade.class);
        when(unidadeMock.getCodigo()).thenReturn(10L);
        when(unidadeMock.getSigla()).thenReturn("UN");
        when(unidadeMock.getUnidadeSuperior()).thenReturn(unidadeSuperiorMock);

        subprocessoMock = mock(Subprocesso.class);
        when(subprocessoMock.getCodigo()).thenReturn(1L);
        when(subprocessoMock.getUnidade()).thenReturn(unidadeMock);
        when(subprocessoMock.getProcesso()).thenReturn(processoMock);

        usuario = new Usuario();
        usuario.setTitulo("analista_teste");
        usuario.setUnidade(unidadeSuperiorMock);

        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(subprocessoMock));

        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenAnswer(inv -> {
            Subprocesso sp = inv.getArgument(0);
            return new SubprocessoDto(
                sp.getCodigo(),
                sp.getProcesso() != null ? sp.getProcesso().getCodigo() : null,
                sp.getUnidade() != null ? sp.getUnidade().getCodigo() : null,
                sp.getMapa() != null ? sp.getMapa().getCodigo() : null,
                sp.getDataLimiteEtapa1(),
                sp.getDataFimEtapa1(),
                sp.getDataLimiteEtapa2(),
                sp.getDataFimEtapa2(),
                sp.getSituacao()
            );
        });
    }

    @Test
    void casoFeliz_retornaDetalhesComMovimentacoesEElementos() {
        Long spId = 1L;
        Unidade unidade = mock(Unidade.class);
        when(unidade.getCodigo()).thenReturn(10L);
        when(unidade.getNome()).thenReturn("Unidade X");
        when(unidade.getSigla()).thenReturn("UX");
        Usuario titular = new Usuario();
        titular.setTitulo("0001");
        titular.setNome("Titular X");
        titular.setEmail("titular@exemplo");
        titular.setRamal("1234");
        when(unidade.getTitular()).thenReturn(titular);

        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(spId);
        sp.setUnidade(unidade);
        sp.setMapa(mapa);
        sp.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
        sp.setDataLimiteEtapa1(LocalDate.of(2025, 12, 31));

        Movimentacao mov = mock(Movimentacao.class);
        MovimentacaoDto movDto = new MovimentacaoDto(
                500L,
                LocalDateTime.now(),
                null, null, null,
                unidade.getCodigo(), unidade.getSigla(), unidade.getNome(),
                "Mov desc"
        );

        Atividade at = new Atividade();
        at.setCodigo(200L);
        at.setMapa(mapa);
        at.setDescricao("Atividade A");

        Conhecimento kc = new Conhecimento();
        kc.setCodigo(300L);
        kc.setAtividade(at);
        kc.setDescricao("Conhecimento 1");

        when(repositorioSubprocesso.findById(spId)).thenReturn(Optional.of(sp));
        when(repositorioMovimentacao.findBySubprocessoCodigoOrderByDataHoraDesc(spId)).thenReturn(List.of(mov));
        when(movimentacaoMapper.toDTO(any(Movimentacao.class))).thenReturn(movDto);
        when(atividadeRepo.findByMapaCodigo(mapa.getCodigo())).thenReturn(List.of(at));
        when(repositorioConhecimento.findAll()).thenReturn(List.of(kc));

        SubprocessoDetalheDto dto = subprocessoService.obterDetalhes(spId, "ADMIN", null);

        assertNotNull(dto);
        assertNotNull(dto.unidade());
        assertEquals(unidade.getCodigo(), dto.unidade().codigo());
        assertEquals(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO.name(), dto.situacao());
        assertNotNull(dto.movimentacoes());
        assertEquals(1, dto.movimentacoes().size());
        assertEquals(movDto.codigo(), dto.movimentacoes().getFirst().codigo());
        assertNotNull(dto.elementosDoProcesso());
        boolean temAtividade = dto.elementosDoProcesso().stream().anyMatch(e -> "ATIVIDADE".equals(e.tipo()));
        boolean temConhecimento = dto.elementosDoProcesso().stream().anyMatch(e -> "CONHECIMENTO".equals(e.tipo()));
        assertTrue(temAtividade, "Esperado elemento do tipo ATIVIDADE");
        assertTrue(temConhecimento, "Esperado elemento do tipo CONHECIMENTO");
    }

    @Test
    void casoNaoEncontrado_lancaDomainNotFoundException() {
        Long id = 99L;
        when(repositorioSubprocesso.findById(id)).thenReturn(Optional.empty());
        assertThrows(ErroDominioNaoEncontrado.class, () -> subprocessoService.obterDetalhes(id, "ADMIN", null));
    }

    @Test
    void casoSemPermissao_lancaDomainAccessDeniedExceptionParaGestorDeOutraUnidade() {
        Long spId = 2L;
        Unidade unidade = new Unidade();
        unidade.setCodigo(50L);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(spId);
        sp.setUnidade(unidade);

        when(repositorioSubprocesso.findById(spId)).thenReturn(Optional.of(sp));

        Long unidadeUsuario = 99L;
        assertThrows(ErroDominioAccessoNegado.class, () -> subprocessoService.obterDetalhes(spId, "GESTOR", unidadeUsuario));
    }

    @Test
    void aceitarCadastro_deveNotificarESalvarAlerta() {
        setupBasico();
        when(subprocessoMock.getSituacao()).thenReturn(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);

        subprocessoService.aceitarCadastro(1L, "Observações de teste", "analista_teste");

        verify(repositorioAnaliseCadastro).save(any(AnaliseCadastro.class));
        verify(repositorioMovimentacao).save(any(Movimentacao.class));

        ArgumentCaptor<String> assuntoCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> corpoCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificacaoServico).enviarEmail(eq(SUP), assuntoCaptor.capture(), corpoCaptor.capture());
        assertEquals("SGC: Cadastro de atividades e conhecimentos da UN submetido para análise", assuntoCaptor.getValue());
        assertTrue(corpoCaptor.getValue().contains("foi submetido para análise por essa unidade"));

        ArgumentCaptor<Alerta> alertaCaptor = ArgumentCaptor.forClass(Alerta.class);
        verify(repositorioAlerta).save(alertaCaptor.capture());
        assertEquals("Cadastro de atividades e conhecimentos da unidade UN submetido para análise", alertaCaptor.getValue().getDescricao());
        assertEquals(unidadeSuperiorMock, alertaCaptor.getValue().getUnidadeDestino());
    }

    @Test
    void aceitarRevisaoCadastro_deveNotificarESalvarAlerta() {
        setupBasico();
        when(subprocessoMock.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

        subprocessoService.aceitarRevisaoCadastro(1L, "Obs teste", usuario);

        verify(repositorioAnaliseCadastro).save(any(AnaliseCadastro.class));
        verify(repositorioMovimentacao).save(any(Movimentacao.class));

        ArgumentCaptor<String> assuntoCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> corpoCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificacaoServico).enviarEmail(eq(SUP), assuntoCaptor.capture(), corpoCaptor.capture());
        assertEquals("SGC: Revisão do cadastro de atividades e conhecimentos da UN submetido para análise", assuntoCaptor.getValue());
        assertTrue(corpoCaptor.getValue().contains("A revisão do cadastro de atividades e conhecimentos da UN no processo Processo de Teste foi submetida para análise por essa unidade."));

        ArgumentCaptor<Alerta> alertaCaptor = ArgumentCaptor.forClass(Alerta.class);
        verify(repositorioAlerta).save(alertaCaptor.capture());
        assertEquals("Revisão do cadastro de atividades e conhecimentos da unidade UN submetida para análise", alertaCaptor.getValue().getDescricao());
        assertEquals(unidadeSuperiorMock, alertaCaptor.getValue().getUnidadeDestino());
    }

    @Test
    void disponibilizarMapa_deveEnviarEmailsEAlertasCorretos() {
        setupBasico();
        LocalDate dataLimite = LocalDate.of(2025, 10, 31);
        String dataLimiteFormatada = dataLimite.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Mapa mapa = mock(Mapa.class);
        when(subprocessoMock.getMapa()).thenReturn(mapa);
        when(subprocessoMock.getDataLimiteEtapa2()).thenReturn(dataLimite);
        when(subprocessoMock.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);

        Unidade sedoc = mock(Unidade.class);
        when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(sedoc));

        when(competenciaRepo.findByMapaCodigo(any())).thenReturn(new ArrayList<>());
        when(atividadeRepo.findByMapaCodigo(any())).thenReturn(new ArrayList<>());

        subprocessoService.disponibilizarMapa(1L, "obs", dataLimite, this.usuario);

        ArgumentCaptor<String> siglaCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> assuntoCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> corpoCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificacaoServico, times(2)).enviarEmail(siglaCaptor.capture(), assuntoCaptor.capture(), corpoCaptor.capture());

        List<String> siglas = siglaCaptor.getAllValues();
        List<String> assuntos = assuntoCaptor.getAllValues();
        List<String> corpos = corpoCaptor.getAllValues();

        // Email 1 (para a própria unidade)
        assertEquals("UN", siglas.getFirst());
        assertTrue(assuntos.getFirst().contains("Mapa de Competências da unidade UN disponibilizado para validação"));
        assertTrue(corpos.getFirst().contains("O mapa de competências da sua unidade, referente ao processo 'Processo de Teste', foi disponibilizado para validação."));
        assertTrue(corpos.getFirst().contains(dataLimiteFormatada));

        // Email 2 (para a unidade superior)
        assertEquals(SUP, siglas.get(1));
        assertTrue(assuntos.get(1).contains("Mapa de Competências da unidade UN disponibilizado para validação"));
        assertTrue(corpos.get(1).contains("O mapa de competências da unidade UN, referente ao processo 'Processo de Teste', foi disponibilizado para validação."));

        ArgumentCaptor<Alerta> alertaCaptor = ArgumentCaptor.forClass(Alerta.class);
        verify(repositorioAlerta).save(alertaCaptor.capture());
        assertEquals("Mapa de competências da unidade UN disponibilizado para análise", alertaCaptor.getValue().getDescricao());
        assertEquals(unidadeMock, alertaCaptor.getValue().getUnidadeDestino());
    }

    @Test
    void obterMapaParaAjuste_deveRetornarDtoCompleto() {
        setupBasico();
        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);
        when(subprocessoMock.getMapa()).thenReturn(mapa);

        Competencia comp = new Competencia();
        comp.setCodigo(200L);
        comp.setDescricao("Competencia Teste");
        when(competenciaRepo.findByMapaCodigo(100L)).thenReturn(List.of(comp));

        Atividade ativ = new Atividade();
        ativ.setCodigo(300L);
        ativ.setDescricao("Atividade Teste");
        when(atividadeRepo.findByMapaCodigo(100L)).thenReturn(List.of(ativ));

        Conhecimento con = new Conhecimento();
        con.setCodigo(400L);
        con.setDescricao("Conhecimento Teste");
        when(repositorioConhecimento.findByAtividadeCodigo(300L)).thenReturn(List.of(con));

        when(competenciaAtividadeRepo.existsById(any(CompetenciaAtividade.Id.class))).thenReturn(true);

        CompetenciaAtividade caLink = mock(CompetenciaAtividade.class);
        when(caLink.getAtividade()).thenReturn(ativ);
        when(competenciaAtividadeRepo.findByCompetenciaCodigo(comp.getCodigo())).thenReturn(List.of(caLink));

        MapaAjusteDto dto = subprocessoService.obterMapaParaAjuste(1L);

        assertNotNull(dto);
        assertEquals(100L, dto.mapaId());
        assertEquals(1, dto.competencias().size());
        assertEquals(1, dto.competencias().getFirst().atividades().size());
        assertEquals(1, dto.competencias().getFirst().atividades().getFirst().conhecimentos().size());
        assertTrue(dto.competencias().getFirst().atividades().getFirst().conhecimentos().getFirst().incluido());
    }

    @Test
    void salvarAjustesMapa_deveLimparEVincularNovamente() {
        setupBasico();
        when(subprocessoMock.getSituacao()).thenReturn(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
        List<CompetenciaAtividade> existingLinks = List.of(mock(CompetenciaAtividade.class));
        when(competenciaAtividadeRepo.findByCompetenciaCodigo(anyLong())).thenReturn(existingLinks);


        ConhecimentoAjusteDto conDtoIncluido = new ConhecimentoAjusteDto(400L, "Conhecimento 1", true);
        AtividadeAjusteDto ativDto = new AtividadeAjusteDto(300L, "Atividade 1", List.of(conDtoIncluido));
        CompetenciaAjusteDto compDto = new CompetenciaAjusteDto(200L, "Competencia 1", List.of(ativDto));
        List<CompetenciaAjusteDto> requestDtos = List.of(compDto);

        subprocessoService.salvarAjustesMapa(1L, requestDtos, "user_teste");

        verify(competenciaAtividadeRepo).deleteAll(existingLinks);
        verify(competenciaAtividadeRepo).save(any(CompetenciaAtividade.class));
        verify(subprocessoMock).setSituacao(SituacaoSubprocesso.MAPA_AJUSTADO);
        verify(repositorioSubprocesso).save(subprocessoMock);
    }

    @Test
    void salvarAjustesMapa_deveLancarExcecao_quandoSituacaoInvalida() {
        setupBasico();
        when(subprocessoMock.getSituacao()).thenReturn(SituacaoSubprocesso.NAO_INICIADO);

        assertThrows(IllegalStateException.class, () -> subprocessoService.salvarAjustesMapa(1L, new ArrayList<>(), "user_teste"));
    }

    @Test
    void importarAtividades_deveCopiarAtividadesEConhecimentos() {
        Unidade unidadeDestino = new Unidade();
        unidadeDestino.setCodigo(10L);
        Mapa mapaDestino = new Mapa();
        mapaDestino.setCodigo(100L);
        Subprocesso spDestino = new Subprocesso();
        spDestino.setCodigo(1L);
        spDestino.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
        spDestino.setMapa(mapaDestino);
        spDestino.setUnidade(unidadeDestino);
        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(spDestino));

        Unidade unidadeOrigem = new Unidade();
        unidadeOrigem.setCodigo(20L);
        unidadeOrigem.setSigla("ORIG");
        Mapa mapaOrigem = new Mapa();
        mapaOrigem.setCodigo(200L);
        Subprocesso spOrigem = new Subprocesso();
        spOrigem.setCodigo(2L);
        spOrigem.setMapa(mapaOrigem);
        spOrigem.setUnidade(unidadeOrigem);
        when(repositorioSubprocesso.findById(2L)).thenReturn(Optional.of(spOrigem));

        Atividade atividadeOrigem = new Atividade();
        atividadeOrigem.setCodigo(300L);
        atividadeOrigem.setDescricao("Atividade Original");
        when(atividadeRepo.findByMapaCodigo(200L)).thenReturn(List.of(atividadeOrigem));

        Conhecimento conhecimentoOrigem = new Conhecimento();
        conhecimentoOrigem.setCodigo(400L);
        conhecimentoOrigem.setDescricao("Conhecimento Original");
        when(repositorioConhecimento.findByAtividadeCodigo(300L)).thenReturn(List.of(conhecimentoOrigem));

        when(atividadeRepo.save(any(Atividade.class))).thenAnswer(inv -> {
            Atividade savedAtividade = inv.getArgument(0);
            savedAtividade.setCodigo(301L); // Simulate DB generating an ID
            return savedAtividade;
        });

        subprocessoService.importarAtividades(1L, 2L);

        ArgumentCaptor<Atividade> atividadeCaptor = ArgumentCaptor.forClass(Atividade.class);
        verify(atividadeRepo).save(atividadeCaptor.capture());
        assertEquals("Atividade Original", atividadeCaptor.getValue().getDescricao());
        assertEquals(mapaDestino, atividadeCaptor.getValue().getMapa());

        ArgumentCaptor<Conhecimento> conhecimentoCaptor = ArgumentCaptor.forClass(Conhecimento.class);
        verify(repositorioConhecimento).save(conhecimentoCaptor.capture());
        assertEquals("Conhecimento Original", conhecimentoCaptor.getValue().getDescricao());
        assertNotNull(conhecimentoCaptor.getValue().getAtividade());

        ArgumentCaptor<Movimentacao> movimentacaoCaptor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(repositorioMovimentacao).save(movimentacaoCaptor.capture());
        assertTrue(movimentacaoCaptor.getValue().getDescricao().contains("Importação de atividades do subprocesso #2"));
    }

    @Test
    void importarAtividades_deveLancarExcecao_quandoSituacaoDestinoInvalida() {
        Subprocesso spDestino = new Subprocesso();
        spDestino.setCodigo(1L);
        spDestino.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(spDestino));

        // Mock para o subprocesso de origem para evitar o NaoEncontrado
        Subprocesso spOrigem = new Subprocesso();
        spOrigem.setCodigo(2L);
        when(repositorioSubprocesso.findById(2L)).thenReturn(Optional.of(spOrigem));

        assertThrows(IllegalStateException.class, () -> subprocessoService.importarAtividades(1L, 2L));
    }
}