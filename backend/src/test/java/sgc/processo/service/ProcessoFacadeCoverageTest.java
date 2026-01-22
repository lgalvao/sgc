package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.alerta.AlertaFacade;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.AtualizarProcessoRequest;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.eventos.EventoProcessoAtualizado;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoFacadeCoverageTest")
class ProcessoFacadeCoverageTest {

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private SubprocessoFacade subprocessoFacade;
    @Mock
    private ApplicationEventPublisher publicadorEventos;
    @Mock
    private sgc.processo.mapper.ProcessoMapper processoMapper;
    @Mock
    private ProcessoDetalheBuilder processoDetalheBuilder;
    @Mock
    private sgc.subprocesso.mapper.SubprocessoMapper subprocessoMapper;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private ProcessoInicializador processoInicializador;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private ProcessoAcessoService processoAcessoService;
    @Mock
    private ProcessoValidador processoValidador;
    @Mock
    private ProcessoFinalizador processoFinalizador;
    @Mock
    private ProcessoConsultaService processoConsultaService;

    @InjectMocks
    private ProcessoFacade facade;

    @Test
    @DisplayName("atualizar - Erro Situacao Invalida")
    void atualizar_ErroSituacaoInvalida() {
        Long codigo = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codigo);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        when(processoRepo.findById(codigo)).thenReturn(Optional.of(processo));

        AtualizarProcessoRequest req = AtualizarProcessoRequest.builder()
                .codigo(codigo)
                .descricao("Desc")
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataLimiteEtapa1(LocalDateTime.now())
                .unidades(List.of(1L))
                .build();

        assertThrows(ErroProcessoEmSituacaoInvalida.class, () -> facade.atualizar(codigo, req));
    }

    @Test
    @DisplayName("atualizar - Erro Validacao Unidades Sem Mapa")
    void atualizar_ErroValidacaoUnidadesSemMapa() {
        Long codigo = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codigo);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDescricao("Desc");

        when(processoRepo.findById(codigo)).thenReturn(Optional.of(processo));

        AtualizarProcessoRequest req = AtualizarProcessoRequest.builder()
                .codigo(codigo)
                .descricao("Desc")
                .tipo(TipoProcesso.REVISAO)
                .dataLimiteEtapa1(LocalDateTime.now())
                .unidades(List.of(10L))
                .build();

        when(processoValidador.getMensagemErroUnidadesSemMapa(any())).thenReturn(Optional.of("Erro Validacao"));

        assertThrows(ErroProcesso.class, () -> facade.atualizar(codigo, req));
    }

    @Test
    @DisplayName("atualizar - Sem Alteracoes")
    void atualizar_SemAlteracoes() {
        Long codigo = 1L;
        Long codUnidade = 10L;
        LocalDateTime dataLimite = LocalDate.of(2023, 1, 1).atStartOfDay();

        Processo processo = new Processo();
        processo.setCodigo(codigo);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDescricao("Descricao Original");
        processo.setDataLimite(dataLimite);

        Unidade unidade = new Unidade();
        unidade.setCodigo(codUnidade);
        processo.setParticipantes(Set.of(unidade));

        when(processoRepo.findById(codigo)).thenReturn(Optional.of(processo));
        when(unidadeService.buscarEntidadePorId(codUnidade)).thenReturn(unidade);
        when(processoRepo.saveAndFlush(any())).thenReturn(processo);

        AtualizarProcessoRequest req = AtualizarProcessoRequest.builder()
                .codigo(codigo)
                .descricao("Descricao Original")
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataLimiteEtapa1(dataLimite)
                .unidades(List.of(codUnidade))
                .build();

        facade.atualizar(codigo, req);

        // No event published
        verify(publicadorEventos, never()).publishEvent(any(EventoProcessoAtualizado.class));
    }

    @Test
    @DisplayName("enviarLembrete - Unidade Nao Participa")
    void enviarLembrete_UnidadeNaoParticipa() {
        Long codProcesso = 1L;
        Long codUnidade = 10L;

        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setParticipantes(Collections.emptySet());

        when(processoRepo.findById(codProcesso)).thenReturn(Optional.of(processo));
        when(unidadeService.buscarEntidadePorId(codUnidade)).thenReturn(new Unidade());

        assertThrows(ErroProcesso.class, () -> facade.enviarLembrete(codProcesso, codUnidade));
    }

    @Test
    @DisplayName("buscarIdsUnidadesEmProcessosAtivos - Cobertura linha 318")
    void buscarIdsUnidadesEmProcessosAtivos_Cobertura() {
        Long codProcessoIgnorar = 1L;
        facade.buscarIdsUnidadesEmProcessosAtivos(codProcessoIgnorar);
        verify(processoConsultaService).buscarIdsUnidadesEmProcessosAtivos(codProcessoIgnorar);
    }
}
