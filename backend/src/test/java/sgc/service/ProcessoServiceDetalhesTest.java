package sgc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.mapa.CopiaMapaService;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.notificacao.NotificacaoEmailService;
import sgc.notificacao.NotificacaoTemplateEmailService;
import sgc.processo.ProcessoService;
import sgc.processo.dto.ProcessoDetalheDto;
import sgc.processo.dto.ProcessoDetalheMapperCustom;
import sgc.processo.dto.ProcessoMapper;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.processo.modelo.UnidadeProcessoRepo;
import sgc.sgrh.SgrhService;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

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
 * Testes unitários para ProcessoService.obterDetalhes(...).
 */
public class ProcessoServiceDetalhesTest {
    private ProcessoRepo processoRepo;
    private UnidadeProcessoRepo unidadeProcessoRepo;
    private SubprocessoRepo subprocessoRepo;
    private ProcessoDetalheMapperCustom processoDetalheMapperCustom;

    private ProcessoService servico;

    @BeforeEach
    public void setup() {
        processoRepo = mock(ProcessoRepo.class);
        UnidadeRepo unidadeRepo = mock(UnidadeRepo.class);
        unidadeProcessoRepo = mock(UnidadeProcessoRepo.class);
        subprocessoRepo = mock(SubprocessoRepo.class);
        MapaRepo mapaRepo = mock(MapaRepo.class);
        MovimentacaoRepo movimentacaoRepo = mock(MovimentacaoRepo.class);
        UnidadeMapaRepo unidadeMapaRepo = mock(UnidadeMapaRepo.class);
        CopiaMapaService servicoDeCopiaDeMapa = mock(CopiaMapaService.class);
        ApplicationEventPublisher publicadorDeEventos = mock(ApplicationEventPublisher.class);
        NotificacaoEmailService servicoNotificacaoEmail = mock(NotificacaoEmailService.class);
        NotificacaoTemplateEmailService notificacaoTemplateEmailService = mock(NotificacaoTemplateEmailService.class);
        SgrhService sgrhService = mock(SgrhService.class);
        ProcessoMapper processoMapper = mock(ProcessoMapper.class);
        processoDetalheMapperCustom = mock(ProcessoDetalheMapperCustom.class);

        servico = new ProcessoService(
                processoRepo,
                unidadeRepo,
                unidadeProcessoRepo,
                subprocessoRepo,
                mapaRepo,
                movimentacaoRepo,
                unidadeMapaRepo,
                servicoDeCopiaDeMapa,
                publicadorDeEventos,
                servicoNotificacaoEmail,
                notificacaoTemplateEmailService,
                sgrhService,
                processoMapper,
                processoDetalheMapperCustom);
    }

    @Test
    public void obterDetalhes_deveRetornarDtoCompleto_quandoFluxoNormal() {
        Processo p = mock(Processo.class);
        LocalDateTime dataCriacao = LocalDateTime.now();
        LocalDate dataLimite = LocalDate.now().plusDays(7);

        when(p.getCodigo()).thenReturn(1L);
        when(p.getDescricao()).thenReturn("Proc Teste");
        when(p.getTipo()).thenReturn("MAPEAMENTO");
        when(p.getSituacao()).thenReturn("EM_ANDAMENTO");
        when(p.getDataCriacao()).thenReturn(dataCriacao);
        when(p.getDataLimite()).thenReturn(dataLimite);
        when(p.getDataFinalizacao()).thenReturn(null);

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

        ProcessoDetalheDto.UnidadeParticipanteDTO unidadeParticipanteDTO = new ProcessoDetalheDto.UnidadeParticipanteDTO(
                10L, "Diretoria X", "DX", null, "PENDENTE", LocalDate.now().plusDays(7), new ArrayList<>());
        ProcessoResumoDto processoResumoDTO = new ProcessoResumoDto(100L, null, "PENDENTE", null,
                LocalDate.now().plusDays(5), null, 10L, "Diretoria X");

        ProcessoDetalheDto processoDetalheDTO = new ProcessoDetalheDto(
                1L, "Proc Teste", "MAPEAMENTO", "EM_ANDAMENTO",
                dataLimite, dataCriacao, null,
                List.of(unidadeParticipanteDTO),
                List.of(processoResumoDTO));

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(unidadeProcessoRepo.findByProcessoCodigo(1L)).thenReturn(List.of(up));
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(sp));
        when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigo(1L, 10L)).thenReturn(true);
        Mockito.lenient().when(processoDetalheMapperCustom.toDetailDTO(eq(p), anyList(), anyList()))
                .thenReturn(processoDetalheDTO);

        ProcessoDetalheDto dto = servico.obterDetalhes(1L, "ADMIN", null);

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
        Processo p = new Processo();
        p.setCodigo(2L);
        p.setDescricao("Proc Teste 2");

        when(processoRepo.findById(2L)).thenReturn(Optional.of(p));
        when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigo(2L, 10L)).thenReturn(false);

        // Act & Assert
        assertThrows(ErroDominioAccessoNegado.class, () -> servico.obterDetalhes(2L, "GESTOR", 10L));
    }
}