package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sgc.comum.ComumRepo;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.SubprocessoElegivelDto;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.query.ConsultasSubprocessoService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoConsultaService")
class ProcessoConsultaServiceTest {

    @InjectMocks
    private ProcessoConsultaService processoConsultaService;

    @Mock
    private ProcessoRepo processoRepo;

    @Mock
    private ComumRepo repo;

    @Mock
    private ConsultasSubprocessoService queryService;

    @Mock
    private UsuarioFacade usuarioService;

    @Test
    @DisplayName("Deve buscar processo por ID")
    void deveBuscarProcessoCodigo() {
        Processo p = new Processo();
        when(repo.buscar(Processo.class, 1L)).thenReturn(p);
        assertThat(processoConsultaService.buscarProcessoCodigo(1L)).isEqualTo(p);
    }

    @Test
    @DisplayName("Deve buscar IDs de unidades em processos ativos")
    void buscarIdsUnidadesComProcessosAtivos_sucesso() {
        // Arrange
        Long processoIgnorar = 100L;
        List<Long> unidadesMock = List.of(1L, 2L, 3L);

        when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                anyList(), eq(processoIgnorar)
        )).thenReturn(unidadesMock);

        // Act
        Set<Long> resultado = processoConsultaService.buscarIdsUnidadesComProcessosAtivos(processoIgnorar);

        // Assert
        assertThat(resultado).hasSize(3).containsExactlyInAnyOrder(1L, 2L, 3L);

        verify(processoRepo).findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                Arrays.asList(SituacaoProcesso.EM_ANDAMENTO, SituacaoProcesso.CRIADO),
                processoIgnorar
        );
    }

    @Test
    @DisplayName("Deve retornar conjunto vazio se não houver processos ativos")
    void buscarIdsUnidadesEmProcessosAndamento_vazio() {
        // Arrange
        Long processoIgnorar = 100L;

        when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                anyList(), eq(processoIgnorar)
        )).thenReturn(List.of());

        // Act
        Set<Long> resultado = processoConsultaService.buscarIdsUnidadesComProcessosAtivos(processoIgnorar);

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve listar unidades bloqueadas por tipo")
    void deveUnidadesBloqueadasPorTipo() {
        when(processoRepo.findUnidadeCodigosBySituacaoAndTipo(SituacaoProcesso.EM_ANDAMENTO, TipoProcesso.MAPEAMENTO))
                .thenReturn(List.of(10L, 20L));

        List<Long> ids = processoConsultaService.unidadesBloqueadasPorTipo(TipoProcesso.MAPEAMENTO);

        assertThat(ids).containsExactly(10L, 20L);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há unidades bloqueadas por tipo")
    void deveRetornarListaVaziaQuandoNaoHaUnidadesBloqueadasPorTipo() {
        // Arrange
        when(processoRepo.findUnidadeCodigosBySituacaoAndTipo(SituacaoProcesso.EM_ANDAMENTO, TipoProcesso.REVISAO))
                .thenReturn(List.of());

        // Act
        List<Long> ids = processoConsultaService.unidadesBloqueadasPorTipo(TipoProcesso.REVISAO);

        // Assert
        assertThat(ids).isEmpty();
    }

    @Test
    @DisplayName("Deve listar subprocessos para Admin")
    void deveListarParaAdmin() {
        Usuario admin = Usuario.builder()
                .tituloEleitoral("admin")
                .perfilAtivo(Perfil.ADMIN)
                .build();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(admin);

        Subprocesso s1 = Subprocesso.builder()
                .situacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO)
                .unidade(Unidade.builder().nome("U1").sigla("S1").situacao(SituacaoUnidade.ATIVA).build())
                .build();
        s1.setCodigo(1L);

        when(queryService.listarPorProcessoESituacoes(eq(1L), anyList())).thenReturn(List.of(s1));

        List<SubprocessoElegivelDto> res = processoConsultaService.subprocessosElegiveis(1L);

        assertThat(res).hasSize(1);
        assertThat(res.getFirst().getCodigo()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve listar subprocessos para Usuário Comum por Unidade")
    void deveListarParaUsuarioComum() {
        Usuario user = Usuario.builder()
                .tituloEleitoral("user")
                .perfilAtivo(Perfil.GESTOR)
                .unidadeAtivaCodigo(100L)
                .build();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(user);

        Unidade u1 = Unidade.builder().nome("U1").sigla("S1").situacao(SituacaoUnidade.ATIVA).build();
        u1.setCodigo(100L);

        Subprocesso s1 = Subprocesso.builder().situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO).unidade(u1).build();
        s1.setCodigo(1L);

        when(queryService.listarPorProcessoUnidadeESituacoes(eq(1L), eq(100L), anyList())).thenReturn(List.of(s1));

        List<SubprocessoElegivelDto> res = processoConsultaService.subprocessosElegiveis(1L);

        assertThat(res).hasSize(1);
        assertThat(res.getFirst().getCodigo()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve listar processos ativos")
    void deveProcessosAndamento() {
        processoConsultaService.processosAndamento();
        verify(processoRepo).findBySituacao(SituacaoProcesso.EM_ANDAMENTO);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há processos ativos")
    void deveRetornarListaVaziaQuandoNaoHaProcessosAtivos() {
        // Arrange
        when(processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO)).thenReturn(List.of());

        // Act
        List<Processo> resultado = processoConsultaService.processosAndamento();

        // Assert
        assertThat(resultado).isEmpty();
        verify(processoRepo).findBySituacao(SituacaoProcesso.EM_ANDAMENTO);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há processos finalizados")
    void deveRetornarListaVaziaQuandoNaoHaProcessosFinalizados() {
        // Arrange
        Usuario admin = Usuario.builder().perfilAtivo(Perfil.ADMIN).build();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(admin);
        
        when(processoRepo.listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO))
                .thenReturn(List.of());

        // Act
        List<Processo> resultado = processoConsultaService.processosFinalizados();

        // Assert
        assertThat(resultado).isEmpty();
        verify(processoRepo).listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO);
    }

    @Test
    @DisplayName("Deve retornar Optional de processo por código")
    void deveBuscarProcessoCodigoOpt() {
        Processo p = new Processo();
        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        
        Optional<Processo> res = processoConsultaService.buscarProcessoCodigoOpt(1L);
        assertThat(res).isPresent().contains(p);
    }

    @Test
    @DisplayName("Deve buscar processos paginados")
    void deveListarProcessosPaginados() {
        Pageable pageable = Pageable.unpaged();
        when(processoRepo.findAll(pageable)).thenReturn(Page.empty());
        
        Page<Processo> res = processoConsultaService.processos(pageable);
        assertThat(res).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar processos iniciados por participantes")
    void deveBuscarProcessosIniciadosPorParticipantes() {
        List<Long> unidadeIds = List.of(1L);
        Pageable pageable = Pageable.unpaged();
        when(processoRepo.findDistinctByParticipantes_IdUnidadeCodigoInAndSituacaoNot(unidadeIds, SituacaoProcesso.CRIADO, pageable))
                .thenReturn(Page.empty());

        Page<Processo> res = processoConsultaService.processosIniciadosPorParticipantes(unidadeIds, pageable);
        assertThat(res).isEmpty();
    }
}
