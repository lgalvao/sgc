package sgc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.comum.modelo.Usuario;
import sgc.conhecimento.modelo.Conhecimento;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.mapa.modelo.Mapa;
import sgc.subprocesso.SubprocessoService;
import sgc.subprocesso.dto.MovimentacaoDto;
import sgc.subprocesso.dto.MovimentacaoMapper;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.analise.modelo.AnaliseCadastro;
import sgc.analise.modelo.AnaliseCadastroRepo;
import sgc.notificacao.NotificacaoService;
import sgc.processo.modelo.Processo;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sgc.subprocesso.dto.SubprocessoDto;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SubprocessoServiceTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private AtividadeRepo atividadeRepository;
    @Mock
    private ConhecimentoRepo conhecimentoRepo;
    @Mock
    private MovimentacaoMapper movimentacaoMapper;
    @Mock
    private NotificacaoService notificacaoService;
    @Mock
    private AlertaRepo alertaRepo;
    @Mock
    private AnaliseCadastroRepo analiseCadastroRepo;
    @Mock
    private sgc.analise.modelo.AnaliseValidacaoRepo analiseValidacaoRepo;
    @Mock
    private sgc.notificacao.modelo.NotificacaoRepo notificacaoRepo;
    @Mock
    private org.springframework.context.ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private sgc.atividade.dto.AtividadeMapper atividadeMapper;
    @Mock
    private sgc.conhecimento.dto.ConhecimentoMapper conhecimentoMapper;
    @Mock
    private sgc.subprocesso.dto.SubprocessoMapper subprocessoMapper;


    @InjectMocks
    private SubprocessoService subprocessoService;

    private Unidade unidadeMock;
    private Unidade unidadeSuperiorMock;
    private Subprocesso subprocessoMock;
    private Processo processoMock;

    void setupBasico() {
        processoMock = mock(Processo.class);
        when(processoMock.getDescricao()).thenReturn("Processo de Teste");

        unidadeSuperiorMock = mock(Unidade.class);
        when(unidadeSuperiorMock.getSigla()).thenReturn("SUP");

        unidadeMock = mock(Unidade.class);
        when(unidadeMock.getCodigo()).thenReturn(10L);
        when(unidadeMock.getSigla()).thenReturn("UN");
        when(unidadeMock.getUnidadeSuperior()).thenReturn(unidadeSuperiorMock);

        subprocessoMock = mock(Subprocesso.class);
        when(subprocessoMock.getCodigo()).thenReturn(1L);
        when(subprocessoMock.getUnidade()).thenReturn(unidadeMock);
        when(subprocessoMock.getProcesso()).thenReturn(processoMock);

        when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocessoMock));

        when(subprocessoMapper.toDTO(any(Subprocesso.class))).thenAnswer(inv -> {
            Subprocesso sp = inv.getArgument(0);
            SubprocessoDto dto = new SubprocessoDto();
            dto.setCodigo(sp.getCodigo());
            dto.setSituacaoId(sp.getSituacaoId());
            return dto;
        });
    }


    @Test
    void casoFeliz_retornaDetalhesComMovimentacoesEElementos() {
        // Arrange
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
        sp.setSituacaoId("EM_ANDAMENTO");
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

        when(subprocessoRepo.findById(spId)).thenReturn(Optional.of(sp));
        when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(spId)).thenReturn(List.of(mov));
        when(movimentacaoMapper.toDTO(any(Movimentacao.class))).thenReturn(movDto);
        when(atividadeRepository.findByMapaCodigo(mapa.getCodigo())).thenReturn(List.of(at));
        when(conhecimentoRepo.findAll()).thenReturn(List.of(kc));

        // Act
        SubprocessoDetalheDto dto = subprocessoService.obterDetalhes(spId, "ADMIN", null);

        // Assert
        assertNotNull(dto);
        assertNotNull(dto.getUnidade());
        assertEquals(unidade.getCodigo(), dto.getUnidade().getCodigo());
        assertEquals("EM_ANDAMENTO", dto.getSituacao());
        assertNotNull(dto.getMovimentacoes());
        assertEquals(1, dto.getMovimentacoes().size());
        assertEquals(movDto.getCodigo(), dto.getMovimentacoes().getFirst().getCodigo());
        assertNotNull(dto.getElementosDoProcesso());
        boolean temAtividade = dto.getElementosDoProcesso().stream().anyMatch(e -> "ATIVIDADE".equals(e.getTipo()));
        boolean temConhecimento = dto.getElementosDoProcesso().stream().anyMatch(e -> "CONHECIMENTO".equals(e.getTipo()));
        assertTrue(temAtividade, "Esperado elemento do tipo ATIVIDADE");
        assertTrue(temConhecimento, "Esperado elemento do tipo CONHECIMENTO");
    }

    @Test
    void casoNaoEncontrado_lancaDomainNotFoundException() {
        Long id = 99L;
        when(subprocessoRepo.findById(id)).thenReturn(Optional.empty());
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

        when(subprocessoRepo.findById(spId)).thenReturn(Optional.of(sp));

        Long unidadeUsuario = 99L;
        assertThrows(ErroDominioAccessoNegado.class, () -> subprocessoService.obterDetalhes(spId, "GESTOR", unidadeUsuario));
    }

    @Test
    void aceitarCadastro_deveNotificarESalvarAlerta() {
        // Arrange
        setupBasico();
        when(subprocessoMock.getSituacaoId()).thenReturn("CADASTRO_DISPONIBILIZADO");

        // Act
        subprocessoService.aceitarCadastro(1L, "Observações de teste", "analista_teste");

        // Assert
        verify(analiseCadastroRepo).save(any(AnaliseCadastro.class));
        verify(movimentacaoRepo).save(any(Movimentacao.class));

        ArgumentCaptor<String> assuntoCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> corpoCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificacaoService).enviarEmail(eq("SUP"), assuntoCaptor.capture(), corpoCaptor.capture());
        assertEquals("SGC: Cadastro da unidade UN aceito e aguardando homologação", assuntoCaptor.getValue());
        assertTrue(corpoCaptor.getValue().contains("foi aceito e está disponível para homologação"));

        ArgumentCaptor<Alerta> alertaCaptor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepo).save(alertaCaptor.capture());
        assertEquals("Cadastro da unidade UN aguardando homologação", alertaCaptor.getValue().getDescricao());
        assertEquals(unidadeSuperiorMock, alertaCaptor.getValue().getUnidadeDestino());
    }

    @Test
    void aceitarRevisaoCadastro_deveNotificarESalvarAlerta() {
        // Arrange
        setupBasico();
        when(subprocessoMock.getSituacaoId()).thenReturn("REVISAO_CADASTRO_DISPONIBILIZADA");

        // Act
        subprocessoService.aceitarRevisaoCadastro(1L, "Obs teste", "analista_teste");

        // Assert
        verify(analiseCadastroRepo).save(any(AnaliseCadastro.class));
        verify(movimentacaoRepo).save(any(Movimentacao.class));

        ArgumentCaptor<String> assuntoCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> corpoCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificacaoService).enviarEmail(eq("SUP"), assuntoCaptor.capture(), corpoCaptor.capture());
        assertEquals("SGC: Revisão de cadastro da UN aceita e aguardando homologação", assuntoCaptor.getValue());
        assertTrue(corpoCaptor.getValue().contains("foi aceita e está disponível para homologação"));

        ArgumentCaptor<Alerta> alertaCaptor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepo).save(alertaCaptor.capture());
        assertEquals("Revisão de cadastro da unidade UN aguardando homologação", alertaCaptor.getValue().getDescricao());
        assertEquals(unidadeSuperiorMock, alertaCaptor.getValue().getUnidadeDestino());
    }

    @Test
    void disponibilizarMapa_deveEnviarEmailsEAlertasCorretos() {
        // Arrange
        setupBasico();
        LocalDate dataLimite = LocalDate.of(2025, 10, 31);
        String dataLimiteFormatada = dataLimite.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        when(subprocessoMock.getMapa()).thenReturn(mock(Mapa.class));
        when(subprocessoMock.getDataLimiteEtapa2()).thenReturn(dataLimite);

        // Act
        subprocessoService.disponibilizarMapa(1L, "obs", dataLimite, "user");

        // Assert
        ArgumentCaptor<String> siglaCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> assuntoCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> corpoCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificacaoService, times(2)).enviarEmail(siglaCaptor.capture(), assuntoCaptor.capture(), corpoCaptor.capture());

        List<String> siglas = siglaCaptor.getAllValues();
        List<String> assuntos = assuntoCaptor.getAllValues();
        List<String> corpos = corpoCaptor.getAllValues();

        // Email 1 (para a própria unidade)
        assertEquals("UN", siglas.get(0));
        assertEquals("SGC: Mapa de Competências da sua unidade disponibilizado para validação", assuntos.get(0));
        assertTrue(corpos.get(0).contains("O mapa de competências da sua unidade (UN) foi disponibilizado para validação"));
        assertTrue(corpos.get(0).contains(dataLimiteFormatada));

        // Email 2 (para a unidade superior)
        assertEquals("SUP", siglas.get(1));
        assertEquals("SGC: Mapa de Competências da unidade UN disponibilizado para validação", assuntos.get(1));
        assertTrue(corpos.get(1).contains("O mapa de competências da unidade UN foi disponibilizado para validação"));
        assertTrue(corpos.get(1).contains("Acompanhe o processo no sistema."));

        ArgumentCaptor<Alerta> alertaCaptor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepo).save(alertaCaptor.capture());
        assertEquals("Seu mapa de competências está disponível para validação (Processo: Processo de Teste)", alertaCaptor.getValue().getDescricao());
        assertEquals(unidadeMock, alertaCaptor.getValue().getUnidadeDestino());
    }
}