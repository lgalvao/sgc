package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.Perfil;
import sgc.processo.dto.SubprocessoElegivelDto;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.query.ConsultasSubprocessoService;

import java.util.Arrays;
import java.util.List;
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
    private sgc.comum.repo.ComumRepo repo;

    @Mock
    private ConsultasSubprocessoService queryService;

    @Mock
    private UsuarioFacade usuarioService;

    @Mock
    private SubprocessoMapper subprocessoMapper;

    @Test
    @DisplayName("Deve buscar processo por ID")
    void deveBuscarProcessoCodigo() {
        sgc.processo.model.Processo p = new sgc.processo.model.Processo();
        when(repo.buscar(sgc.processo.model.Processo.class, 1L)).thenReturn(p);
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
                .perfilAtivo(sgc.organizacao.model.Perfil.ADMIN)
                .build();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(admin);

        Subprocesso s1 = Subprocesso.builder().situacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO).unidade(sgc.organizacao.model.Unidade.builder().nome("U1").sigla("S1").build()).build();
        s1.setCodigo(1L);

        when(queryService.listarPorProcessoESituacoes(eq(1L), anyList())).thenReturn(List.of(s1));
        when(subprocessoMapper.toElegivelDto(any())).thenReturn(SubprocessoElegivelDto.builder().codSubprocesso(1L).build());

        List<SubprocessoElegivelDto> res = processoConsultaService.subprocessosElegiveis(1L);

        assertThat(res).hasSize(1);
        assertThat(res.getFirst().getCodSubprocesso()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve listar subprocessos para Usuário Comum por Unidade")
    void deveListarParaUsuarioComum() {
        Usuario user = Usuario.builder()
                .tituloEleitoral("user")
                .perfilAtivo(sgc.organizacao.model.Perfil.GESTOR)
                .unidadeAtivaCodigo(100L)
                .build();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(user);

        sgc.organizacao.model.Unidade u1 = sgc.organizacao.model.Unidade.builder().nome("U1").sigla("S1").build();
        u1.setCodigo(100L);

        Subprocesso s1 = Subprocesso.builder().situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO).unidade(u1).build();
        s1.setCodigo(1L);

        when(queryService.listarPorProcessoUnidadeESituacoes(eq(1L), eq(100L), anyList())).thenReturn(List.of(s1));
        when(subprocessoMapper.toElegivelDto(any())).thenReturn(SubprocessoElegivelDto.builder().codSubprocesso(1L).build());

        List<SubprocessoElegivelDto> res = processoConsultaService.subprocessosElegiveis(1L);

        assertThat(res).hasSize(1);
        assertThat(res.getFirst().getCodSubprocesso()).isEqualTo(1L);
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
        List<sgc.processo.model.Processo> resultado = processoConsultaService.processosAndamento();

        // Assert
        assertThat(resultado).isEmpty();
        verify(processoRepo).findBySituacao(SituacaoProcesso.EM_ANDAMENTO);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há processos finalizados")
    void deveRetornarListaVaziaQuandoNaoHaProcessosFinalizados() {
        // Arrange
        when(processoRepo.listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO))
                .thenReturn(List.of());

        // Act
        List<sgc.processo.model.Processo> resultado = processoConsultaService.processosFinalizados();

        // Assert
        assertThat(resultado).isEmpty();
        verify(processoRepo).listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO);
    }
}
