package sgc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.mapa.CopiaMapaService;
import sgc.mapa.MapaRepository;
import sgc.mapa.UnidadeMapaRepository;
import sgc.notificacao.ServicoNotificacaoEmail;
import sgc.notificacao.ServicoDeTemplateDeEmail;
import sgc.processo.*;
import sgc.processo.dto.ProcessoDetalheDTO;
import sgc.processo.dto.ProcessoResumoDTO;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.MovimentacaoRepository;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;
import sgc.unidade.Unidade;
import sgc.unidade.UnidadeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para ServicoProcesso.obterDetalhes(...).
 */
public class ServicoProcessoDetalhesTest {
    private RepositorioProcesso repositorioProcesso;
    private UnidadeProcessoRepository unidadeProcessoRepository;
    private SubprocessoRepository subprocessoRepository;
    private ProcessoDetalheMapper processoDetalheMapper;

    private ServicoProcesso servico;

    @BeforeEach
    public void setup() {
        repositorioProcesso = mock(RepositorioProcesso.class);
        UnidadeRepository unidadeRepository = mock(UnidadeRepository.class);
        unidadeProcessoRepository = mock(UnidadeProcessoRepository.class);
        subprocessoRepository = mock(SubprocessoRepository.class);
        MapaRepository mapaRepository = mock(MapaRepository.class);
        MovimentacaoRepository movimentacaoRepository = mock(MovimentacaoRepository.class);
        UnidadeMapaRepository unidadeMapaRepository = mock(UnidadeMapaRepository.class);
        CopiaMapaService servicoDeCopiaDeMapa = mock(CopiaMapaService.class);
        ApplicationEventPublisher publicadorDeEventos = mock(ApplicationEventPublisher.class);
        ServicoNotificacaoEmail servicoNotificacaoEmail = mock(ServicoNotificacaoEmail.class);
        ServicoDeTemplateDeEmail servicoDeTemplateDeEmail = mock(ServicoDeTemplateDeEmail.class);
        SgrhService sgrhService = mock(SgrhService.class);
        ProcessoMapper processoMapper = mock(ProcessoMapper.class);
        processoDetalheMapper = mock(ProcessoDetalheMapper.class);

        servico = new ServicoProcesso(
                repositorioProcesso,
                unidadeRepository,
                unidadeProcessoRepository,
                subprocessoRepository,
                mapaRepository,
                movimentacaoRepository,
                unidadeMapaRepository,
                servicoDeCopiaDeMapa,
                publicadorDeEventos,
                servicoNotificacaoEmail,
                servicoDeTemplateDeEmail,
                sgrhService,
                processoMapper,
                processoDetalheMapper);
    }

    @Test
    public void obterDetalhes_deveRetornarDtoCompleto_quandoFluxoNormal() {
        // Arrange
        Processo p = mock(Processo.class);
        LocalDateTime dataCriacao = LocalDateTime.now();
        LocalDate dataLimite = LocalDate.now().plusDays(7);
        LocalDateTime dataFinalizacao = null;

        when(p.getCodigo()).thenReturn(1L);
        when(p.getDescricao()).thenReturn("Proc Teste");
        when(p.getTipo()).thenReturn("MAPEAMENTO");
        when(p.getSituacao()).thenReturn("EM_ANDAMENTO");
        when(p.getDataCriacao()).thenReturn(dataCriacao);
        when(p.getDataLimite()).thenReturn(dataLimite);
        when(p.getDataFinalizacao()).thenReturn(dataFinalizacao);

        UnidadeProcesso up = new UnidadeProcesso();
        up.setCodigo(10L);
        up.setNome("Diretoria X");
        up.setSigla("DX");
        up.setSituacao("PENDENTE");
        up.setUnidadeSuperiorCodigo(null);

        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setSigla("DX");
        unidade.setNome("Diretoria X");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setUnidade(unidade);
        sp.setSituacaoId("PENDENTE");
        sp.setDataLimiteEtapa1(LocalDate.now().plusDays(5));

        ProcessoDetalheDTO.UnidadeParticipanteDTO unidadeParticipanteDTO = new ProcessoDetalheDTO.UnidadeParticipanteDTO(
                10L, "Diretoria X", "DX", null, "PENDENTE", LocalDate.now().plusDays(7), new ArrayList<>());
        ProcessoResumoDTO processoResumoDTO = new ProcessoResumoDTO(100L, null, "PENDENTE", null,
                LocalDate.now().plusDays(5), null, 10L, "Diretoria X");

        ProcessoDetalheDTO processoDetalheDTO = new ProcessoDetalheDTO(
                1L, "Proc Teste", "MAPEAMENTO", "EM_ANDAMENTO",
                dataLimite, dataCriacao, dataFinalizacao,
                List.of(unidadeParticipanteDTO),
                List.of(processoResumoDTO));

        when(repositorioProcesso.findById(1L)).thenReturn(Optional.of(p));
        when(unidadeProcessoRepository.findByProcessoCodigo(1L)).thenReturn(List.of(up));
        when(subprocessoRepository.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(sp));
        when(subprocessoRepository.existsByProcessoCodigoAndUnidadeCodigo(1L, 10L)).thenReturn(true);
        Mockito.lenient().when(processoDetalheMapper.toDetailDTO(eq(p), anyList(), anyList()))
                .thenReturn(processoDetalheDTO);

        // Act
        ProcessoDetalheDTO dto = servico.obterDetalhes(1L, "ADMIN", null);

        // Assert
        assertNotNull(dto);
        assertEquals(p.getCodigo(), dto.getCodigo());
        assertNotNull(dto.getUnidades());
        assertTrue(dto.getUnidades().stream().anyMatch(u -> "DX".equals(u.getSigla())));
        assertNotNull(dto.getResumoSubprocessos());
        assertTrue(
                dto.getResumoSubprocessos().stream().anyMatch(s -> s.getCodigo() != null && s.getSituacao() != null));
    }

    @Test
    public void obterDetalhes_deveLancarExcecao_quandoGestorNaoAutorizado() {
        // Arrange
        Processo p = new Processo();
        p.setCodigo(2L);
        p.setDescricao("Proc Teste 2");

        when(repositorioProcesso.findById(2L)).thenReturn(Optional.of(p));
        when(subprocessoRepository.existsByProcessoCodigoAndUnidadeCodigo(2L, 10L)).thenReturn(false);

        // Act & Assert
        assertThrows(ErroDominioAccessoNegado.class, () -> servico.obterDetalhes(2L, "GESTOR", 10L));
    }
}