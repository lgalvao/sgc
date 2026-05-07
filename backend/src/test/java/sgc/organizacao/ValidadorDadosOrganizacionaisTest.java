package sgc.organizacao;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.jdbc.core.namedparam.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidadorDadosOrganizacionaisTest {

    @Mock
    private CacheViewsOrganizacaoService cacheViewsOrganizacaoService;
    @Mock
    private UsuarioRepo usuarioRepo;
    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @InjectMocks
    private ValidadorDadosOrganizacionais validador;

    @Test
    @DisplayName("diagnosticar retorna sem violacoes quando nao ha unidades participantes")
    void diagnosticarSemViolacoes() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of());

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico).isEqualTo(DiagnosticoOrganizacionalDto.semViolacoes());
        verifyNoInteractions(usuarioRepo, namedParameterJdbcTemplate);
    }

    @Test
    @DisplayName("diagnosticar gera resumo amigavel quando ha apenas unidades sem responsavel")
    void diagnosticarApenasUnidadesSemResponsavel() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of(
                unidade(1L, "OPER1", TipoUnidade.OPERACIONAL, null),
                unidade(2L, "OPER2", TipoUnidade.OPERACIONAL, null)
        ));
        when(cacheViewsOrganizacaoService.listarTodasResponsabilidades()).thenReturn(List.of());
        when(namedParameterJdbcTemplate.queryForList(anyString(), anyMap())).thenReturn(List.of());

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isTrue();
        assertThat(diagnostico.quantidadeTiposViolacao()).isEqualTo(1);
        assertThat(diagnostico.quantidadeOcorrencias()).isEqualTo(2);
        assertThat(diagnostico.resumo()).contains("Há unidades atualmente sem responsável efetivo", "OPER1", "OPER2");
        assertThat(diagnostico.grupos())
                .singleElement()
                .satisfies(grupo -> {
                    assertThat(grupo.tipo()).isEqualTo("Unidade sem responsável");
                    assertThat(grupo.quantidadeOcorrencias()).isEqualTo(2);
                    assertThat(grupo.ocorrencias()).containsExactlyInAnyOrder("sigla=OPER1, tipo=OPERACIONAL", "sigla=OPER2, tipo=OPERACIONAL");
                });
        verify(usuarioRepo, never()).findAllById(anyCollection());
    }

    @Test
    @DisplayName("diagnosticar filtra perfil com usuario nulo quando unidade ja esta sem responsavel")
    void diagnosticarFiltraPerfilDerivadoDeUnidadeSemResponsavel() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of(unidade(1L, "INTER", TipoUnidade.INTERMEDIARIA, null)));
        when(cacheViewsOrganizacaoService.listarTodasResponsabilidades()).thenReturn(List.of());
        when(namedParameterJdbcTemplate.queryForList(anyString(), anyMap())).thenReturn(List.of(
                Map.of("perfil", "GESTOR", "unidade_codigo", 1L)
        ));

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.grupos())
                .extracting(GrupoViolacaoOrganizacionalDto::tipo)
                .contains("Unidade sem responsável")
                .doesNotContain("VW_USUARIO_PERFIL_UNIDADE com usuario_titulo nulo");
    }

    @Test
    @DisplayName("diagnosticar inclui perfil invalido quando problema nao e derivado de unidade sem responsavel")
    void diagnosticarIncluiPerfilInvalidoQuandoNaoDerivado() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of(unidade(10L, "OPER", TipoUnidade.OPERACIONAL, null)));
        when(cacheViewsOrganizacaoService.listarTodasResponsabilidades()).thenReturn(List.of(new ResponsabilidadeLeitura(10L, "111")));
        when(usuarioRepo.findAllById(anyCollection())).thenReturn(List.of(usuario("111")));
        when(namedParameterJdbcTemplate.queryForList(contains("FROM sgc.vw_usuario"), anyMap())).thenReturn(List.of());
        when(namedParameterJdbcTemplate.queryForList(contains("FROM sgc.vw_usuario_perfil_unidade"), anyMap())).thenReturn(List.of(
                Map.of("usuario_titulo", "111", "perfil", "NAO_EXISTE", "unidade_codigo", 10L)
        ));

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.grupos())
                .extracting(GrupoViolacaoOrganizacionalDto::tipo)
                .contains("VW_USUARIO_PERFIL_UNIDADE com perfil invalido");
        assertThat(diagnostico.grupos())
                .filteredOn(grupo -> grupo.tipo().equals("VW_USUARIO_PERFIL_UNIDADE com perfil invalido"))
                .singleElement()
                .satisfies(grupo -> assertThat(grupo.ocorrencias()).contains("usuario_titulo=111, perfil=NAO_EXISTE, unidade_codigo=10"));
    }

    @Test
    @DisplayName("diagnosticar acusa unidade intermediaria sem filhas e sem gestor")
    void diagnosticarUnidadeIntermediariaSemFilhasESemGestor() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of(
                unidade(100L, "INTER", TipoUnidade.INTERMEDIARIA, "111")
        ));
        when(cacheViewsOrganizacaoService.listarTodasResponsabilidades()).thenReturn(List.of(new ResponsabilidadeLeitura(100L, "111")));
        when(usuarioRepo.findAllById(anyCollection())).thenReturn(List.of(usuario("111")));
        when(namedParameterJdbcTemplate.queryForList(anyString(), anyMap())).thenReturn(List.of());

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.grupos())
                .extracting(GrupoViolacaoOrganizacionalDto::tipo)
                .contains("Unidade intermediaria sem filhas ativas participantes", "Unidade intermediaria sem perfil GESTOR");
        assertThat(diagnostico.quantidadeTiposViolacao()).isEqualTo(2);
        assertThat(diagnostico.quantidadeOcorrencias()).isEqualTo(2);
    }

    private static UnidadeHierarquiaLeitura unidade(Long codigo, String sigla, TipoUnidade tipo, String tituloTitular) {
        return new UnidadeHierarquiaLeitura(codigo, sigla, sigla, tituloTitular, tipo, SituacaoUnidade.ATIVA, null);
    }

    private static Usuario usuario(String tituloEleitoral) {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(tituloEleitoral);
        return usuario;
    }
}
