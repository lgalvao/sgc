package sgc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.mapa.CopiaMapaService;
import sgc.mapa.MapaRepository;
import sgc.mapa.UnidadeMapaRepository;
import sgc.notificacao.EmailNotificationService;
import sgc.notificacao.EmailTemplateService;
import sgc.processo.*;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.MovimentacaoRepository;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;
import sgc.unidade.Unidade;
import sgc.unidade.UnidadeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para ProcessoService.getDetails(...)
 * <p>
 * - caso feliz: retorna ProcessoDetalheDTO com unidades e resumo de subprocessos
 * - caso sem permissão: lança ErroDominioAccessoNegado
 */
public class ProcessoServiceDetailsTest {
    private ProcessoRepository processoRepository;
    private UnidadeProcessoRepository unidadeProcessoRepository;
    private SubprocessoRepository subprocessoRepository;

    private ProcessoService service;

    @BeforeEach
    public void setup() {
        processoRepository = mock(ProcessoRepository.class);
        UnidadeRepository unidadeRepository = mock(UnidadeRepository.class);
        unidadeProcessoRepository = mock(UnidadeProcessoRepository.class);
        subprocessoRepository = mock(SubprocessoRepository.class);
        MapaRepository mapaRepository = mock(MapaRepository.class);
        MovimentacaoRepository movimentacaoRepository = mock(MovimentacaoRepository.class);
        UnidadeMapaRepository unidadeMapaRepository = mock(UnidadeMapaRepository.class);
        CopiaMapaService copiaMapaService = mock(CopiaMapaService.class);
        ApplicationEventPublisherStub publisher = new ApplicationEventPublisherStub();
        EmailNotificationService emailService = mock(EmailNotificationService.class);
        EmailTemplateService emailTemplateService = mock(EmailTemplateService.class);
        SgrhService sgrhService = mock(SgrhService.class);

        service = new ProcessoService(
                processoRepository,
                unidadeRepository,
                unidadeProcessoRepository,
                subprocessoRepository,
                mapaRepository,
                movimentacaoRepository,
                unidadeMapaRepository,
                copiaMapaService,
                publisher,
                emailService,
                emailTemplateService,
                sgrhService
        );
    }

    @Test
    public void testObterDetalhes_HappyPath() {
        // Arrange
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setDescricao("Proc Teste");
        p.setTipo("MAPEAMENTO");
        p.setSituacao("EM_ANDAMENTO");
        p.setDataCriacao(LocalDateTime.now());
        p.setDataLimite(LocalDate.now().plusDays(7));

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

        when(processoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(unidadeProcessoRepository.findByProcessoCodigo(1L)).thenReturn(List.of(up));
        when(subprocessoRepository.findByProcessoCodigoWithUnidade(1L)).thenReturn(List.of(sp));

        // Act
        ProcessoDetalheDTO dto = service.obterDetalhes(1L, "ADMIN", null);

        // Assert
        assertNotNull(dto);
        assertEquals(p.getCodigo(), dto.getCodigo());
        assertNotNull(dto.getUnidades());
        assertTrue(dto.getUnidades().stream().anyMatch(u -> "DX".equals(u.getSigla())));
        assertNotNull(dto.getResumoSubprocessos());
        assertTrue(dto.getResumoSubprocessos().stream().anyMatch(s -> s.getCodigo() != null && s.getSituacao() != null));
    }

    @Test
    public void testObterDetalhes_UnauthorizedForGestor() {
        // Arrange
        Processo p = new Processo();
        p.setCodigo(2L);
        p.setDescricao("Proc Teste 2");

        Unidade unidade = new Unidade();
        unidade.setCodigo(99L);
        unidade.setSigla("OX");
        unidade.setNome("Outra");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(200L);
        sp.setUnidade(unidade);
        sp.setSituacaoId("PENDENTE");

        when(processoRepository.findById(2L)).thenReturn(Optional.of(p));
        when(unidadeProcessoRepository.findByProcessoCodigo(2L)).thenReturn(List.of());
        when(subprocessoRepository.findByProcessoCodigoWithUnidade(2L)).thenReturn(List.of(sp));

        // Act & Assert: gestor da unidade 10 não está presente nos subprocessos -> acesso negado
        assertThrows(ErroDominioAccessoNegado.class, () -> service.obterDetalhes(2L, "GESTOR", 10L));
    }

    // Pequeno stub local para ApplicationEventPublisher (não usamos eventos nos testes)
    static class ApplicationEventPublisherStub implements org.springframework.context.ApplicationEventPublisher {
        @Override
        public void publishEvent(@NonNull Object event) {
        }
    }
}