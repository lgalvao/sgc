package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.processo.model.AcaoProcesso.*;
import static sgc.processo.model.SituacaoProcesso.*;
import static sgc.processo.model.TipoProcesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoService - Cobertura de Testes")
class ProcessoServiceCoverageTest {

    @Mock private ProcessoRepo processoRepo;
    @Mock private ComumRepo repo;
    @Mock private UnidadeService unidadeService;
    @Mock private SubprocessoService subprocessoService;
    @Mock private SubprocessoValidacaoService validacaoService;
    @Mock private UsuarioFacade usuarioService;
    @Mock private AlertaFacade servicoAlertas;
    @Mock private EmailService emailService;
    @Mock private EmailModelosService emailModelosService;
    @Mock private SgcPermissionEvaluator permissionEvaluator;
    @Mock private SubprocessoTransicaoService transicaoService;

    @InjectMocks
    private ProcessoService target;

    @Test
    @DisplayName("executarAcaoEmBloco deve lançar ErroAcessoNegado quando não houver permissão")
    void deveLancarErroAcessoNegado() {
        Long codProcesso = 100L;
        AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(1L), DISPONIBILIZAR, null);
        
        Subprocesso sp = mock(Subprocesso.class);
        when(subprocessoService.listarEntidadesPorProcessoEUnidades(eq(codProcesso), anyList()))
                .thenReturn(List.of(sp));
        
        when(permissionEvaluator.verificarPermissao(any(), anyList(), any())).thenReturn(false);

        assertThatThrownBy(() -> target.executarAcaoEmBloco(codProcesso, req))
                .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("finalizar deve notificar participantes")
    void deveNotificarParticipantesAoFinalizar() {
        Long codigo = 1L;
        Processo p = mock(Processo.class);
        when(p.getCodigo()).thenReturn(codigo);
        when(p.getDescricao()).thenReturn("Desc");
        when(p.getTipo()).thenReturn(DIAGNOSTICO);
        when(p.getSituacao()).thenReturn(EM_ANDAMENTO);
        
        UnidadeProcesso up = mock(UnidadeProcesso.class);
        when(up.getUnidadeCodigo()).thenReturn(10L);
        when(p.getParticipantes()).thenReturn(List.of(up));
        
        Unidade uni = new Unidade();
        uni.setCodigo(10L);
        when(unidadeService.porCodigos(anyList())).thenReturn(List.of(uni));
        
        when(repo.buscar(eq(Processo.class), eq(codigo))).thenReturn(p);
        
        SubprocessoValidacaoService.ValidationResult v = SubprocessoValidacaoService.ValidationResult.ofValido();
        when(validacaoService.validarSubprocessosParaFinalizacao(codigo)).thenReturn(v);
        
        target.finalizar(codigo);
        
        verify(p).setSituacao(FINALIZADO);
        verify(servicoAlertas).criarAlertaAdmin(eq(p), eq(uni), anyString());
        verify(processoRepo).save(p);
    }

    @Test
    @DisplayName("validarSelecaoBloco deve lançar ErroValidacao quando tamanhos diferirem")
    void deveLancarErroValidacaoEmBloco() {
        Long codProcesso = 100L;
        AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(List.of(1L, 2L), DISPONIBILIZAR, null);
        
        Subprocesso sp = mock(Subprocesso.class);
        Unidade u = new Unidade();
        u.setCodigo(1L);
        when(sp.getUnidade()).thenReturn(u);
        
        // Retorna apenas 1 subprocesso para 2 códigos solicitados
        when(subprocessoService.listarEntidadesPorProcessoEUnidades(eq(codProcesso), anyList()))
                .thenReturn(List.of(sp));

        assertThatThrownBy(() -> target.executarAcaoEmBloco(codProcesso, req))
                .isInstanceOf(ErroValidacao.class);
    }
}
