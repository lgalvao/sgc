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
import sgc.processo.dto.SubprocessoElegivelDto;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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
    private SubprocessoFacade subprocessoFacade;

    @Mock
    private UsuarioFacade usuarioService;

    @Test
    @DisplayName("Deve buscar IDs de unidades em processos ativos")
    void buscarIdsUnidadesEmProcessosAtivos_sucesso() {
        // Arrange
        Long processoIgnorar = 100L;
        List<Long> unidadesMock = List.of(1L, 2L, 3L);

        when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                anyList(), eq(processoIgnorar)
        )).thenReturn(unidadesMock);

        // Act
        Set<Long> resultado = processoConsultaService.buscarIdsUnidadesEmProcessosAtivos(processoIgnorar);

        // Assert
        assertThat(resultado).hasSize(3).containsExactlyInAnyOrder(1L, 2L, 3L);

        verify(processoRepo).findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                Arrays.asList(SituacaoProcesso.EM_ANDAMENTO, SituacaoProcesso.CRIADO),
                processoIgnorar
        );
    }

    @Test
    @DisplayName("Deve retornar conjunto vazio se não houver processos ativos")
    void buscarIdsUnidadesEmProcessosAtivos_vazio() {
        // Arrange
        Long processoIgnorar = 100L;

        when(processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                anyList(), eq(processoIgnorar)
        )).thenReturn(List.of());

        // Act
        Set<Long> resultado = processoConsultaService.buscarIdsUnidadesEmProcessosAtivos(processoIgnorar);

        // Assert
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve listar unidades bloqueadas por tipo")
    void deveListarUnidadesBloqueadasPorTipo() {
        when(processoRepo.findUnidadeCodigosBySituacaoAndTipo(SituacaoProcesso.EM_ANDAMENTO, sgc.processo.model.TipoProcesso.MAPEAMENTO))
                .thenReturn(List.of(10L, 20L));

        List<Long> ids = processoConsultaService.listarUnidadesBloqueadasPorTipo("MAPEAMENTO");

        assertThat(ids).containsExactly(10L, 20L);
    }

    @Test
    @DisplayName("Deve retornar lista vazia se não houver autenticação")
    void deveRetornarVazioSemAutenticacao() {
        SecurityContextHolder.clearContext();
        List<SubprocessoElegivelDto> res = processoConsultaService.listarSubprocessosElegiveis(1L);
        assertThat(res).isEmpty();
    }

    @Test
    @DisplayName("Deve listar subprocessos para Admin (apenas REVISAO_MAPA_AJUSTADO)")
    void deveListarParaAdmin() {
        mockAuth("admin", true);

        Subprocesso s1 = Subprocesso.builder().situacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO).unidade(new Unidade("U1", "S1")).build();
        s1.setCodigo(1L);

        when(subprocessoFacade.listarPorProcessoESituacao(1L, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO)).thenReturn(List.of(s1));

        List<SubprocessoElegivelDto> res = processoConsultaService.listarSubprocessosElegiveis(1L);

        assertThat(res).hasSize(1);
        assertThat(res.getFirst().getCodSubprocesso()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve listar subprocessos para Usuário Comum por Unidade")
    void deveListarParaUsuarioComum() {
        mockAuth("user", false);

        Unidade u1 = new Unidade("U1", "S1");
        u1.setCodigo(100L);

        Subprocesso s1 = Subprocesso.builder().situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO).unidade(u1).build();
        s1.setCodigo(1L);
        Subprocesso s2 = Subprocesso.builder().situacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA).unidade(u1).build();
        s2.setCodigo(2L);

        when(usuarioService.buscarPerfisUsuario("user")).thenReturn(List.of(
                PerfilDto.builder().unidadeCodigo(100L).build()
        ));

        when(subprocessoFacade.listarPorProcessoUnidadeESituacoes(eq(1L), eq(100L), anyList())).thenReturn(List.of(s1, s2));

        List<SubprocessoElegivelDto> res = processoConsultaService.listarSubprocessosElegiveis(1L);

        assertThat(res).hasSize(2);
        assertThat(res).extracting(SubprocessoElegivelDto::getCodSubprocesso).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("Deve retornar lista vazia se usuário comum não tiver unidade vinculada")
    void deveRetornarVazioUsuarioSemUnidade() {
        mockAuth("user_no_unit", false);
        when(usuarioService.buscarPerfisUsuario("user_no_unit")).thenReturn(List.of(
                PerfilDto.builder().unidadeCodigo(null).build()
        ));

        List<SubprocessoElegivelDto> res = processoConsultaService.listarSubprocessosElegiveis(1L);
        assertThat(res).isEmpty();
    }

    private void mockAuth(String username, boolean isAdmin) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        if (isAdmin) {
            when(auth.getAuthorities()).thenAnswer(m -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        } else {
            when(auth.getAuthorities()).thenAnswer(m -> List.of());
        }
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }
}
