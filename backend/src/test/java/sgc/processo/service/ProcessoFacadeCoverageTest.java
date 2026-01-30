package sgc.processo.service;

import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.AtualizarProcessoRequest;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.model.Processo;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import sgc.alerta.AlertaFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.processo.mapper.ProcessoMapper;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.service.SubprocessoFacade;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoFacadeCoverageTest")
class ProcessoFacadeCoverageTest {
    @Mock
    private ProcessoManutencaoService processoManutencaoService;
    @Mock
    private ProcessoConsultaService processoConsultaService;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private SubprocessoFacade subprocessoFacade;
    @Mock
    private ProcessoMapper processoMapper;
    @Mock
    private ProcessoDetalheBuilder processoDetalheBuilder;
    @Mock
    private SubprocessoMapper subprocessoMapper;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private ProcessoInicializador processoInicializador;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private ProcessoAcessoService processoAcessoService;
    @Mock
    private ProcessoFinalizador processoFinalizador;

    private ProcessoFacade facade;

    @BeforeEach
    void setUp() throws Exception {
        facade = new ProcessoFacade(
            processoConsultaService,
            processoManutencaoService,
            unidadeService,
            subprocessoFacade,
            processoMapper,
            processoDetalheBuilder,
            subprocessoMapper,
            usuarioService,
            processoInicializador,
            alertaService,
            processoAcessoService,
            processoFinalizador
        );

        Field selfField = ProcessoFacade.class.getDeclaredField("self");
        selfField.setAccessible(true);
        selfField.set(facade, facade);
    }

    @Test
    @DisplayName("atualizar - Erro Situacao Invalida")
    void atualizar_ErroSituacaoInvalida() {
        Long codigo = 1L;
        AtualizarProcessoRequest req = AtualizarProcessoRequest.builder()
                .codigo(codigo)
                .build();

        when(processoManutencaoService.atualizar(eq(codigo), any()))
            .thenThrow(new ErroProcessoEmSituacaoInvalida("Erro"));

        assertThrows(ErroProcessoEmSituacaoInvalida.class, () -> facade.atualizar(codigo, req));
    }

    @Test
    @DisplayName("atualizar - Erro Validacao Unidades Sem Mapa")
    void atualizar_ErroValidacaoUnidadesSemMapa() {
        Long codigo = 1L;
        AtualizarProcessoRequest req = AtualizarProcessoRequest.builder()
                .codigo(codigo)
                .build();

        when(processoManutencaoService.atualizar(eq(codigo), any()))
            .thenThrow(new ErroProcesso("Erro Validacao"));

        var exception = assertThrows(ErroProcesso.class, () -> facade.atualizar(codigo, req));
        assertTrue(exception.getMessage().contains("Erro Validacao"));
    }

    @Test
    @DisplayName("atualizar - Sem Alteracoes")
    void atualizar_SemAlteracoes() {
        Long codigo = 1L;
        AtualizarProcessoRequest req = AtualizarProcessoRequest.builder()
                .codigo(codigo)
                .build();
        Processo processo = new Processo();
        processo.setCodigo(codigo);

        when(processoManutencaoService.atualizar(eq(codigo), any())).thenReturn(processo);
        when(processoMapper.toDto(processo)).thenReturn(ProcessoDto.builder().codigo(codigo).build());

        ProcessoDto result = facade.atualizar(codigo, req);

        assertNotNull(result);
        assertEquals(codigo, result.getCodigo());
        verify(processoManutencaoService).atualizar(eq(codigo), any());
    }

    @Test
    @DisplayName("enviarLembrete - Unidade Nao Participa")
    void enviarLembrete_UnidadeNaoParticipa() {
        Long codProcesso = 1L;
        Long codUnidade = 10L;

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setParticipantes(Collections.emptySet());

        when(processoConsultaService.buscarPorId(codProcesso)).thenReturn(processo);
        when(unidadeService.buscarEntidadePorId(codUnidade)).thenReturn(new Unidade());

        var exception = assertThrows(ErroProcesso.class, () -> facade.enviarLembrete(codProcesso, codUnidade));
        assertTrue(exception.getMessage().contains("não participa"));
    }

    @Test
    @DisplayName("buscarIdsUnidadesEmProcessosAtivos - Cobertura linha 318")
    void buscarIdsUnidadesEmProcessosAtivos_Cobertura() {
        Long codProcessoIgnorar = 1L;
        facade.buscarIdsUnidadesEmProcessosAtivos(codProcessoIgnorar);
        verify(processoConsultaService).buscarIdsUnidadesEmProcessosAtivos(codProcessoIgnorar);
    }
}