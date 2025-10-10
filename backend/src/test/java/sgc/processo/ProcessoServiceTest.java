package sgc.processo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.enums.SituacaoProcesso;
import sgc.comum.enums.SituacaoSubprocesso;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.CopiaMapaService;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.notificacao.NotificacaoEmailService;
import sgc.notificacao.NotificacaoTemplateEmailService;
import sgc.processo.dto.*;
import sgc.processo.eventos.ProcessoCriadoEvento;
import sgc.processo.eventos.ProcessoFinalizadoEvento;
import sgc.processo.eventos.ProcessoIniciadoEvento;
import sgc.processo.modelo.ErroProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.UnidadeProcessoRepo;
import sgc.sgrh.SgrhService;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import sgc.processo.enums.TipoProcesso;
import sgc.unidade.enums.TipoUnidade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProcessoServiceTest {
    @Mock
    private ProcessoRepo processoRepo;

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private UnidadeProcessoRepo unidadeProcessoRepo;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private MapaRepo mapaRepo;

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;

    @Mock
    private CopiaMapaService servicoDeCopiaDeMapa;

    @Mock
    private org.springframework.context.ApplicationEventPublisher publicadorDeEventos;

    @Mock
    private NotificacaoEmailService servicoNotificacaoEmail;

    @Mock
    private NotificacaoTemplateEmailService notificacaoTemplateEmailService;

    @Mock
    private SgrhService sgrhService;

    @Mock
    private ProcessoMapper processoMapper;

    @Mock
    private ProcessoDetalheMapperCustom processoDetalheMapperCustom;

    private ProcessoService processoService;

    @BeforeEach
    void setUp() {
        processoService = new ProcessoService(
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
                processoDetalheMapperCustom
        );
    }

    @Test
    void criar_ProcessoValido_CriaComSucesso() {
        var dataLimite = LocalDate.now().plusDays(30);
        var requisicao = new CriarProcessoReq("Processo de Teste", "MAPEAMENTO", dataLimite, List.of(1L));

        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo de Teste");
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setDataCriacao(LocalDateTime.now());

        var dto = new ProcessoDto(1L, LocalDateTime.now(), null, null, "Processo de Teste", SituacaoProcesso.CRIADO, null);

        when(processoRepo.save(any(Processo.class))).thenReturn(processo);
        when(processoMapper.toDTO(any(Processo.class))).thenReturn(dto);

        ProcessoDto resultado = processoService.criar(requisicao);

        assertNotNull(resultado);
        assertEquals(1L, resultado.codigo());
        assertEquals("Processo de Teste", resultado.descricao());
        verify(processoRepo).save(any(Processo.class));
        verify(publicadorDeEventos).publishEvent(any(ProcessoCriadoEvento.class));
    }

    @Test
    void criar_SemDescricao_LancaConstraintViolationException() {
        var requisicao = new CriarProcessoReq("", "MAPEAMENTO", LocalDate.now().plusDays(30), List.of(1L));

        var exception = assertThrows(jakarta.validation.ConstraintViolationException.class,
            () -> processoService.criar(requisicao));
        
        assertEquals("A descrição do processo é obrigatória.", exception.getMessage());
    }

    @Test
    void criar_SemUnidades_LancaConstraintViolationException() {
        var requisicao = new CriarProcessoReq("Processo de Teste", "MAPEAMENTO", LocalDate.now().plusDays(30), Collections.emptyList());

        var exception = assertThrows(jakarta.validation.ConstraintViolationException.class,
            () -> processoService.criar(requisicao));
        
        assertEquals("Pelo menos uma unidade participante deve ser selecionada.", exception.getMessage());
    }

    @Test
    void criar_TipoRevisaoComUnidadeNaoEncontrada_LancaErroEntidadeNaoEncontrada() {
        var requisicao = new CriarProcessoReq("Processo de Teste", "REVISAO", LocalDate.now().plusDays(30), List.of(999L));

        when(unidadeRepo.findById(999L)).thenReturn(Optional.empty());

        var exception = assertThrows(ErroEntidadeNaoEncontrada.class,
            () -> processoService.criar(requisicao));
        
        assertEquals("Unidade com código 999 não foi encontrada.", exception.getMessage());
    }

    @Test
    void atualizar_ProcessoExisteAtualizaComSucesso() {
        var requisicao = new AtualizarProcessoReq(1L, "Processo Atualizado", "REVISAO", LocalDate.now().plusDays(45), List.of(1L));

        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo Original");
        processo.setSituacao(SituacaoProcesso.CRIADO);

        var dto = new ProcessoDto(1L, null, null, null, "Processo Atualizado", SituacaoProcesso.CRIADO, null);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(processoRepo.save(any(Processo.class))).thenReturn(processo);
        when(processoMapper.toDTO(any(Processo.class))).thenReturn(dto);

        ProcessoDto resultado = processoService.atualizar(1L, requisicao);

        assertNotNull(resultado);
        assertEquals("Processo Atualizado", resultado.descricao());
        verify(processoRepo).save(any(Processo.class));
    }

    @Test
    void atualizar_ProcessoNaoEncontrado_LancaErroEntidadeNaoEncontrada() {
        var requisicao = new AtualizarProcessoReq(999L, "Desc", "TIPO", null, List.of(1L));
        
        when(processoRepo.findById(999L)).thenReturn(Optional.empty());

        var exception = assertThrows(ErroEntidadeNaoEncontrada.class,
            () -> processoService.atualizar(999L, requisicao));
        
        assertEquals("Processo não encontrado: 999", exception.getMessage());
    }

    @Test
    void atualizar_ProcessoNaoCriado_LancaIllegalStateException() {
        var requisicao = new AtualizarProcessoReq(1L, "Desc", "TIPO", null, List.of(1L));

        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));

        var exception = assertThrows(IllegalStateException.class,
            () -> processoService.atualizar(1L, requisicao));
        
        assertEquals("Apenas processos na situação 'CRIADO' podem ser editados.", exception.getMessage());
    }

    @Test
    void apagar_ProcessoExisteRemoveComSucesso() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.CRIADO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));

        processoService.apagar(1L);

        verify(processoRepo).deleteById(1L);
    }

    @Test
    void apagar_ProcessoNaoEncontrado_LancaErroEntidadeNaoEncontrada() {
        when(processoRepo.findById(999L)).thenReturn(Optional.empty());

        var exception = assertThrows(ErroEntidadeNaoEncontrada.class,
            () -> processoService.apagar(999L));
        
        assertEquals("Processo não encontrado: 999", exception.getMessage());
    }

    @Test
    void apagar_ProcessoNaoCriado_LancaIllegalStateException() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));

        var exception = assertThrows(IllegalStateException.class,
            () -> processoService.apagar(1L));
        
        assertEquals("Apenas processos na situação 'CRIADO' podem ser removidos.", exception.getMessage());
    }

    @Test
    void obterPorId_ProcessoExiste_RetornaOptionalComDto() {
        Processo processo = new Processo();
        processo.setCodigo(1L);

        var dto = new ProcessoDto(1L, null, null, null, null, null, null);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(processoMapper.toDTO(processo)).thenReturn(dto);

        var resultado = processoService.obterPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().codigo());
    }

    @Test
    void obterPorId_ProcessoNaoExiste_RetornaOptionalVazio() {
        when(processoRepo.findById(1L)).thenReturn(Optional.empty());

        var resultado = processoService.obterPorId(1L);

        assertFalse(resultado.isPresent());
    }

    @Test
    void obterDetalhes_PerfilNulo_LancaErroDominioAccessoNegado() {
        var exception = assertThrows(ErroDominioAccessoNegado.class,
            () -> processoService.obterDetalhes(1L, null, null));
        
        assertEquals("Perfil inválido para acesso aos detalhes do processo.", exception.getMessage());
    }

    @Test
    void obterDetalhes_PerfilInvalido_LancaErroDominioAccessoNegado() {
        Processo processo = new Processo();
        processo.setCodigo(1L);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));

        var exception = assertThrows(ErroDominioAccessoNegado.class,
            () -> processoService.obterDetalhes(1L, "INVALIDO", null));
        
        assertEquals("Acesso negado. Perfil sem permissão para ver detalhes do processo.", exception.getMessage());
    }

    @Test
    void obterDetalhes_PerfilAdmin_RetornaDetalhes() {
        Processo processo = new Processo();
        processo.setCodigo(1L);

        var detalhes = new ProcessoDetalheDto(1L, null, null, null, null, null, null, List.of(), List.of());

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(unidadeProcessoRepo.findByProcessoCodigo(1L)).thenReturn(Collections.emptyList());
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(Collections.emptyList());
        when(processoDetalheMapperCustom.toDetailDTO(any(), any(), any())).thenReturn(detalhes);

        ProcessoDetalheDto resultado = processoService.obterDetalhes(1L, "ADMIN", null);

        assertNotNull(resultado);
        assertEquals(1L, resultado.codigo());
    }

    @Test
    void obterDetalhes_ProcessoNaoEncontrado_LancaErroEntidadeNaoEncontrada() {
        when(processoRepo.findById(999L)).thenReturn(Optional.empty());

        var exception = assertThrows(ErroEntidadeNaoEncontrada.class,
            () -> processoService.obterDetalhes(999L, "ADMIN", null));

        assertEquals("Processo não encontrado: 999", exception.getMessage());
    }

    @Test
    void obterDetalhes_PerfilGestorUnidadeParticipante_RetornaDetalhes() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        Long idUnidadeUsuario = 10L;

        var detalhes = new ProcessoDetalheDto(1L, null, null, null, null, null, null, List.of(), List.of());

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigo(1L, idUnidadeUsuario)).thenReturn(true);
        when(processoDetalheMapperCustom.toDetailDTO(any(), any(), any())).thenReturn(detalhes);

        ProcessoDetalheDto resultado = processoService.obterDetalhes(1L, "GESTOR", idUnidadeUsuario);

        assertNotNull(resultado);
        assertEquals(1L, resultado.codigo());
    }

    @Test
    void obterDetalhes_PerfilGestorUnidadeNaoParticipante_LancaErroDominioAccessoNegado() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        Long idUnidadeUsuario = 20L;

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.existsByProcessoCodigoAndUnidadeCodigo(1L, idUnidadeUsuario)).thenReturn(false);

        var exception = assertThrows(ErroDominioAccessoNegado.class,
            () -> processoService.obterDetalhes(1L, "GESTOR", idUnidadeUsuario));

        assertEquals("Acesso negado. Sua unidade não participa deste processo.", exception.getMessage());
    }

    @Test
    void iniciarProcessoMapeamento_Valido_IniciaComSucesso() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDataLimite(LocalDate.now().plusDays(30));

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setNome("Unidade Teste");
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
        when(unidadeProcessoRepo.findUnidadesInProcessosAtivos(List.of(1L))).thenReturn(Collections.emptyList());
        when(mapaRepo.save(any(Mapa.class))).thenReturn(mapa);
        when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimentacaoRepo.save(any(Movimentacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(processoRepo.save(any(Processo.class))).thenReturn(processo);
        when(processoMapper.toDTO(any(Processo.class))).thenAnswer(invocation -> {
            Processo p = invocation.getArgument(0);
            return new ProcessoDto(p.getCodigo(), null, null, null, null, p.getSituacao(), null);
        });

        ProcessoDto resultado = processoService.iniciarProcessoMapeamento(1L, List.of(1L));

        assertNotNull(resultado);
        assertEquals(1L, resultado.codigo());
        verify(processoRepo).save(argThat(p -> p.getSituacao() == SituacaoProcesso.EM_ANDAMENTO));
        verify(publicadorDeEventos).publishEvent(any(ProcessoIniciadoEvento.class));

        ArgumentCaptor<Subprocesso> subprocessoCaptor = ArgumentCaptor.forClass(Subprocesso.class);
        verify(subprocessoRepo).save(subprocessoCaptor.capture());
        Subprocesso subprocessoSalvo = subprocessoCaptor.getValue();
        assertEquals(SituacaoSubprocesso.NAO_INICIADO, subprocessoSalvo.getSituacao());
    }

    @Test
    void iniciarProcessoMapeamento_ProcessoNaoEncontrado_LancaErroEntidadeNaoEncontrada() {
        when(processoRepo.findById(999L)).thenReturn(Optional.empty());

        var exception = assertThrows(ErroEntidadeNaoEncontrada.class,
            () -> processoService.iniciarProcessoMapeamento(999L, List.of(1L)));
        
        assertEquals("Processo não encontrado: 999", exception.getMessage());
    }

    @Test
    void iniciarProcessoMapeamento_SemUnidades_LancaIllegalArgumentException() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.CRIADO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));

        var exception = assertThrows(IllegalArgumentException.class,
            () -> processoService.iniciarProcessoMapeamento(1L, null));
        
        assertEquals("A lista de unidades é obrigatória para iniciar o processo de mapeamento.", exception.getMessage());
    }

    @Test
    void iniciarProcessoMapeamento_UnidadeEmProcessoAtivo_LancaErroProcesso() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.CRIADO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(unidadeProcessoRepo.findUnidadesInProcessosAtivos(List.of(1L))).thenReturn(List.of(1L));

        var exception = assertThrows(ErroProcesso.class,
            () -> processoService.iniciarProcessoMapeamento(1L, List.of(1L)));
        
        assertTrue(exception.getMessage().contains("já participam de outro processo ativo"));
    }

    @Test
    void finalizar_ProcessoValido_FinalizaComSucesso() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        subprocesso.setMapa(mapa); // Adiciona o mapa necessário
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("UT");
        subprocesso.setUnidade(unidade);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigo(1L)).thenReturn(List.of(subprocesso));
        when(processoRepo.save(any(Processo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(processoMapper.toDTO(any(Processo.class))).thenAnswer(invocation -> {
            Processo p = invocation.getArgument(0);
            return new ProcessoDto(p.getCodigo(), null, null, null, null, p.getSituacao(), null);
        });

        ProcessoDto resultado = processoService.finalizar(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.codigo());
        verify(processoRepo).save(argThat(p -> p.getSituacao() == SituacaoProcesso.FINALIZADO));
        verify(publicadorDeEventos).publishEvent(any(ProcessoFinalizadoEvento.class));
    }

    @Test
    void finalizar_ProcessoNaoEncontrado_LancaErroEntidadeNaoEncontrada() {
        when(processoRepo.findById(999L)).thenReturn(Optional.empty());

        var exception = assertThrows(ErroEntidadeNaoEncontrada.class,
            () -> processoService.finalizar(999L));
        
        assertEquals("Processo não encontrado: 999", exception.getMessage());
    }

    @Test
        void finalizar_SemSubprocessosHomologados_LancaErroProcesso() {
            Processo processo = new Processo();
            processo.setCodigo(1L);
            processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
    
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(10L);
            subprocesso.setSituacao(SituacaoSubprocesso.NAO_INICIADO); // não homologado
            
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            unidade.setSigla("UT");
            subprocesso.setUnidade(unidade);
    
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            when(subprocessoRepo.findByProcessoCodigo(1L)).thenReturn(List.of(subprocesso));
    
            var exception = assertThrows(ErroProcesso.class,
                () -> processoService.finalizar(1L));
            
            assertTrue(exception.getMessage().contains("Não é possível encerrar o processo"));
        }
    
        // Testes para iniciarProcessoRevisao
    
        @Test
        void iniciarProcessoRevisao_Valido_IniciaComSucesso() {
            Processo processo = new Processo();
            processo.setCodigo(1L);
            processo.setSituacao(SituacaoProcesso.CRIADO);
            processo.setTipo(TipoProcesso.REVISAO);
            processo.setDataLimite(LocalDate.now().plusDays(30));
    
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            unidade.setTipo(TipoUnidade.OPERACIONAL);
    
            sgc.mapa.modelo.UnidadeMapa unidadeMapa = new sgc.mapa.modelo.UnidadeMapa(1L);
            unidadeMapa.setMapaVigenteCodigo(100L);
    
            Mapa mapaNovo = new Mapa();
            mapaNovo.setCodigo(200L);
    
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            when(unidadeProcessoRepo.findUnidadesInProcessosAtivos(List.of(1L))).thenReturn(Collections.emptyList());
            when(unidadeMapaRepo.findByUnidadeCodigo(1L)).thenReturn(Optional.of(unidadeMapa));
            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
            when(mapaRepo.save(any(Mapa.class))).thenReturn(mapaNovo);
            when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(inv -> inv.getArgument(0));
            when(processoRepo.save(any(Processo.class))).thenReturn(processo);
            when(processoMapper.toDTO(processo)).thenReturn(new ProcessoDto(null, null, null, null, null, null, null));

            processoService.iniciarProcessoRevisao(1L, List.of(1L));

            verify(processoRepo).save(argThat(p -> p.getSituacao() == SituacaoProcesso.EM_ANDAMENTO));
            verify(mapaRepo).save(any(Mapa.class));
            verify(subprocessoRepo).save(any(Subprocesso.class));
            verify(publicadorDeEventos).publishEvent(any(ProcessoIniciadoEvento.class));
        }

        @Test
        void iniciarProcessoRevisao_ProcessoNaoCriado_LancaIllegalStateException() {
            Processo processo = new Processo();
            processo.setCodigo(1L);
            processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
    
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
    
            var exception = assertThrows(IllegalStateException.class,
                () -> processoService.iniciarProcessoRevisao(1L, List.of(1L)));
    
            assertEquals("Apenas processos na situação 'CRIADO' podem ser iniciados.", exception.getMessage());
        }
    
        @Test
        void iniciarProcessoRevisao_SemUnidades_LancaIllegalArgumentException() {
            Processo processo = new Processo();
            processo.setCodigo(1L);
            processo.setSituacao(SituacaoProcesso.CRIADO);
    
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
    
            var exception = assertThrows(IllegalArgumentException.class,
                () -> processoService.iniciarProcessoRevisao(1L, Collections.emptyList()));
    
            assertEquals("A lista de unidades é obrigatória para iniciar o processo de revisão.", exception.getMessage());
        }
    
        @Test
        void iniciarProcessoRevisao_UnidadeSemMapaVigente_LancaErroProcesso() {
            Processo processo = new Processo();
            processo.setCodigo(1L);
            processo.setSituacao(SituacaoProcesso.CRIADO);
    
            Unidade unidade = new Unidade();
            unidade.setCodigo(1L);
            unidade.setSigla("UT");
    
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            when(unidadeProcessoRepo.findUnidadesInProcessosAtivos(List.of(1L))).thenReturn(Collections.emptyList());
            when(unidadeMapaRepo.findByUnidadeCodigo(1L)).thenReturn(Optional.empty());
            when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
    
    
            var exception = assertThrows(ErroProcesso.class,
                () -> processoService.iniciarProcessoRevisao(1L, List.of(1L)));
    
            assertEquals("A unidade UT não possui mapa vigente e não pode participar de um processo de revisão.", exception.getMessage());
        }
    
        @Test
        void iniciarProcessoRevisao_UnidadeNaoEncontrada_LancaErroEntidadeNaoEncontrada() {
            Processo processo = new Processo();
            processo.setCodigo(1L);
            processo.setSituacao(SituacaoProcesso.CRIADO);
    
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            when(unidadeProcessoRepo.findUnidadesInProcessosAtivos(List.of(999L))).thenReturn(Collections.emptyList());
            // A validação agora verifica a existência da unidade primeiro
            when(unidadeRepo.findById(999L)).thenReturn(Optional.empty());
    
            var exception = assertThrows(ErroEntidadeNaoEncontrada.class,
                () -> processoService.iniciarProcessoRevisao(1L, List.of(999L)));
    
            assertEquals("Unidade com código 999 não foi encontrada ao validar mapas vigentes.", exception.getMessage());
        }
    }