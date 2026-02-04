package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEstadoImpossivel;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.AcaoEmBlocoRequest;
import sgc.processo.dto.AtualizarProcessoRequest;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.mapper.ProcessoMapper;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.service.SubprocessoFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessoFacadeCoverageTest {

    @InjectMocks
    private ProcessoFacade processoFacade;

    @Mock
    private ProcessoManutencaoService processoManutencaoService;

    @Mock
    private ProcessoMapper processoMapper;

    @Mock
    private ProcessoInicializador processoInicializador;

    @Mock
    private ProcessoConsultaService processoConsultaService;

    @Mock
    private UnidadeFacade unidadeService;

    @Mock
    private sgc.alerta.AlertaFacade alertaService;

    @Mock
    private SubprocessoFacade subprocessoFacade;

    @Mock
    private UsuarioFacade usuarioService;

    @Test
    @DisplayName("criar deve lançar ErroEstadoImpossivel quando mapper retornar null")
    void criar_DeveLancarException_QuandoMapperNull() {
        CriarProcessoRequest req = new CriarProcessoRequest("Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now().plusDays(10), List.of(1L));
        when(processoManutencaoService.criar(req)).thenReturn(new Processo());
        when(processoMapper.toDto(any())).thenReturn(null);

        assertThrows(ErroEstadoImpossivel.class, () -> processoFacade.criar(req));
    }

    @Test
    @DisplayName("atualizar deve lançar ErroEstadoImpossivel quando mapper retornar null")
    void atualizar_DeveLancarException_QuandoMapperNull() {
        AtualizarProcessoRequest req = new AtualizarProcessoRequest(1L, "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now().plusDays(10), List.of(1L));
        when(processoManutencaoService.atualizar(eq(1L), any())).thenReturn(new Processo());
        when(processoMapper.toDto(any())).thenReturn(null);

        assertThrows(ErroEstadoImpossivel.class, () -> processoFacade.atualizar(1L, req));
    }

    @Test
    @DisplayName("iniciarProcessoDiagnostico deve delegar para inicializador")
    void iniciarProcessoDiagnostico_DeveDelegar() {
        Long codigo = 1L;
        List<Long> unidades = List.of(2L, 3L);
        when(processoInicializador.iniciar(codigo, unidades)).thenReturn(List.of("OK"));

        var result = processoFacade.iniciarProcessoDiagnostico(codigo, unidades);
        
        assertEquals(1, result.size());
        verify(processoInicializador).iniciar(codigo, unidades);
    }

    @Test
    @DisplayName("buscarIdsUnidadesEmProcessosAtivos deve delegar para consulta service")
    void buscarIdsUnidadesEmProcessosAtivos_DeveDelegar() {
        Long codigoIgnorar = 1L;
        processoFacade.buscarIdsUnidadesEmProcessosAtivos(codigoIgnorar);
        verify(processoConsultaService).buscarIdsUnidadesEmProcessosAtivos(codigoIgnorar);
    }

    @Test
    @DisplayName("enviarLembrete deve formatar data N/A quando null")
    void enviarLembrete_DeveFormatarDataNA() {
        Long codProcesso = 1L;
        Long codUnidade = 2L;

        Processo processo = new Processo();
        processo.setDescricao("Proc");
        processo.setDataLimite(null);
        Unidade unidade = new Unidade();
        unidade.setCodigo(codUnidade);
        processo.setParticipantes(Set.of(unidade));

        when(processoConsultaService.buscarPorId(codProcesso)).thenReturn(processo);
        when(unidadeService.buscarEntidadePorId(codUnidade)).thenReturn(unidade);

        processoFacade.enviarLembrete(codProcesso, codUnidade);

        verify(alertaService).criarAlertaSedoc(eq(processo), eq(unidade), contains("N/A"));
    }

    @Test
    @DisplayName("executarAcaoEmBloco ignora ação null na categorização")
    void executarAcaoEmBloco_IgnoraAcaoNull() {
        Long codProcesso = 1L;
        // Correct constructor: (unidades, acao, data)
        AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
            List.of(10L),
            null, // Action is null
            LocalDate.now()
        );

        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        
        SubprocessoDto subDto = new SubprocessoDto();
        subDto.setCodUnidade(10L);
        subDto.setSituacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        when(subprocessoFacade.listarPorProcessoEUnidades(codProcesso, req.unidadeCodigos()))
            .thenReturn(List.of(subDto));

        processoFacade.executarAcaoEmBloco(codProcesso, req);

        verify(subprocessoFacade, never()).aceitarCadastroEmBloco(any(), any(), any());
        verify(subprocessoFacade, never()).homologarCadastroEmBloco(any(), any(), any());
    }
}
