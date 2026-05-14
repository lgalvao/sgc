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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.anyCollection;

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
    @DisplayName("diagnosticar retorna sem violações quando não há unidades participantes")
    void diagnosticarSemViolacoes() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of());

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isFalse();
        assertThat(diagnostico.resumo()).isEmpty();
        assertThat(diagnostico.quantidadeTiposViolacao()).isZero();
        assertThat(diagnostico.quantidadeOcorrencias()).isZero();
        assertThat(diagnostico.grupos()).isEmpty();
        verifyNoInteractions(usuarioRepo, namedParameterJdbcTemplate);
    }

    @Test
    @DisplayName("diagnosticar gera resumo amigável quando há apenas unidades sem responsável")
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
    @DisplayName("diagnosticar filtra perfil com usuário nulo quando unidade já está sem responsável")
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
    @DisplayName("diagnosticar inclui perfil inválido quando problema não é derivado de unidade sem responsável")
    void diagnosticarIncluiPerfilInvalidoQuandoNaoDerivado() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of(unidade(10L, "OPER", TipoUnidade.OPERACIONAL, null)));
        when(cacheViewsOrganizacaoService.listarTodasResponsabilidades()).thenReturn(List.of(new ResponsabilidadeLeitura(10L, "111")));
        when(usuarioRepo.findAllById(anyCollection())).thenReturn(List.of(usuario("111")));
        when(namedParameterJdbcTemplate.queryForList(anyString(), anyMap())).thenAnswer(invocacao -> {
            Map<?, ?> parametros = invocacao.getArgument(1);
            if (parametros.containsKey("titulos")) {
                return List.of();
            }
            if (parametros.containsKey("codigos")) {
                return List.of(Map.of("usuario_titulo", "111", "perfil", "NAO_EXISTE", "unidade_codigo", 10L));
            }
            return List.of();
        });

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
    @DisplayName("diagnosticar acusa unidade intermediária sem filhas e sem gestor")
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

    @Test
    @DisplayName("diagnosticar cobre cenários inválidos da view de perfis e duplicidade de título")
    void diagnosticarCobreInvalidacoesDaViewDePerfis() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of(
                new UnidadeHierarquiaLeitura(1L, "INT", "INT", "111", TipoUnidade.INTERMEDIARIA, SituacaoUnidade.ATIVA, null),
                new UnidadeHierarquiaLeitura(2L, "OP", "OP", "111", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, 1L)
        ));
        when(cacheViewsOrganizacaoService.listarTodasResponsabilidades()).thenReturn(List.of(
                new ResponsabilidadeLeitura(1L, "111"),
                new ResponsabilidadeLeitura(2L, "111")
        ));
        when(usuarioRepo.findAllById(anyCollection())).thenReturn(List.of(usuario("111")));
        when(namedParameterJdbcTemplate.queryForList(anyString(), anyMap())).thenAnswer(invocacao -> {
            Map<?, ?> parametros = invocacao.getArgument(1);
            if (parametros.containsKey("titulos")) {
                return List.of(Map.of("TITULO", "111"));
            }
            if (parametros.containsKey("codigos")) {
                Map<String, Object> semUsuario = new HashMap<>();
                semUsuario.put("perfil", "GESTOR");
                semUsuario.put("unidade_codigo", 2L);
                Map<String, Object> semPerfil = new HashMap<>();
                semPerfil.put("usuario_titulo", "111");
                semPerfil.put("unidade_codigo", 2L);
                Map<String, Object> semUnidade = new HashMap<>();
                semUnidade.put("usuario_titulo", "111");
                semUnidade.put("perfil", "GESTOR");
                return List.of(
                        Map.of("USUARIO_TITULO", "222", "PERFIL", "GESTOR", "UNIDADE_CODIGO", 1L),
                        Map.of("USUARIO_TITULO", "222", "PERFIL", "GESTOR", "UNIDADE_CODIGO", 1L),
                        semUsuario,
                        semPerfil,
                        semUnidade,
                        Map.of("usuario_titulo", "111", "perfil", "NAO_EXISTE", "unidade_codigo", 2L)
                );
            }
            return List.of();
        });

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.grupos())
                .extracting(GrupoViolacaoOrganizacionalDto::tipo)
                .contains(
                        "VW_USUARIO com titulo duplicado",
                        "VW_USUARIO_PERFIL_UNIDADE com usuario_titulo nulo",
                        "VW_USUARIO_PERFIL_UNIDADE com perfil nulo",
                        "VW_USUARIO_PERFIL_UNIDADE com unidade_codigo nulo",
                        "VW_USUARIO_PERFIL_UNIDADE com perfil invalido",
                        "VW_USUARIO_PERFIL_UNIDADE com chave duplicada",
                        "Responsavel de unidade intermediaria sem perfil GESTOR correspondente"
                );
    }

    @Test
    @DisplayName("diagnosticar não acusa intermediária quando responsável possui perfil gestor correspondente")
    void diagnosticarNaoAcusaIntermediariaQuandoResponsavelPossuiGestorCorrespondente() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of(
                new UnidadeHierarquiaLeitura(1L, "INT", "INT", "111", TipoUnidade.INTERMEDIARIA, SituacaoUnidade.ATIVA, null),
                new UnidadeHierarquiaLeitura(2L, "OP", "OP", "111", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, 1L)
        ));
        when(cacheViewsOrganizacaoService.listarTodasResponsabilidades()).thenReturn(List.of(
                new ResponsabilidadeLeitura(1L, "111"),
                new ResponsabilidadeLeitura(2L, "111")
        ));
        when(usuarioRepo.findAllById(anyCollection())).thenReturn(List.of(usuario("111")));
        when(namedParameterJdbcTemplate.queryForList(anyString(), anyMap())).thenAnswer(invocacao -> {
            Map<?, ?> parametros = invocacao.getArgument(1);
            if (parametros.containsKey("codigos")) {
                return List.of(Map.of("usuario_titulo", "111", "perfil", "GESTOR", "unidade_codigo", 1L));
            }
            return List.of();
        });

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isFalse();
        assertThat(diagnostico.grupos()).isEmpty();
    }

    @Test
    @DisplayName("diagnosticar ignora unidades fora do escopo participante")
    void diagnosticarIgnoraUnidadesForaDoEscopoParticipante() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of(
                new UnidadeHierarquiaLeitura(1L, "INAT", "INAT", "111", TipoUnidade.OPERACIONAL, SituacaoUnidade.INATIVA, null),
                new UnidadeHierarquiaLeitura(2L, "RAIZ", "RAIZ", "111", TipoUnidade.RAIZ, SituacaoUnidade.ATIVA, null),
                new UnidadeHierarquiaLeitura(3L, "OP", "OP", "111", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, null)
        ));
        when(cacheViewsOrganizacaoService.listarTodasResponsabilidades()).thenReturn(List.of(new ResponsabilidadeLeitura(3L, "111")));
        when(usuarioRepo.findAllById(anyCollection())).thenReturn(List.of(usuario("111")));
        when(namedParameterJdbcTemplate.queryForList(anyString(), anyMap())).thenReturn(List.of());

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isFalse();
        verify(usuarioRepo).findAllById(List.of("111"));
    }

    @Test
    @DisplayName("diagnosticar usa resumo genérico quando não consegue extrair sigla")
    void diagnosticarUsaResumoGenericoQuandoNaoExtraiSigla() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of(
                unidade(1L, " ", TipoUnidade.OPERACIONAL, null)
        ));
        when(cacheViewsOrganizacaoService.listarTodasResponsabilidades()).thenReturn(List.of());
        when(namedParameterJdbcTemplate.queryForList(anyString(), anyMap())).thenReturn(List.of());

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.possuiViolacoes()).isTrue();
        assertThat(diagnostico.resumo()).startsWith("Foram encontradas");
        assertThat(diagnostico.resumo()).doesNotContain("Há unidades atualmente sem responsável efetivo");
    }

    private static UnidadeHierarquiaLeitura unidade(Long codigo, String sigla, TipoUnidade tipo, String tituloTitular) {
        return new UnidadeHierarquiaLeitura(codigo, sigla, sigla, tituloTitular, tipo, SituacaoUnidade.ATIVA, null);
    }

    private static Usuario usuario(String tituloEleitoral) {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(tituloEleitoral);
        return usuario;
    }

    @Test
    @DisplayName("diagnosticar lida com unidade_codigo como String na view de perfis")
    void diagnosticarLidaComUnidadeCodigoComoStringNaViewDePerfis() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of(
                unidade(10L, "OPER", TipoUnidade.OPERACIONAL, "111")
        ));
        when(cacheViewsOrganizacaoService.listarTodasResponsabilidades()).thenReturn(List.of(new ResponsabilidadeLeitura(10L, "111")));
        when(usuarioRepo.findAllById(anyCollection())).thenReturn(List.of(usuario("111")));
        when(namedParameterJdbcTemplate.queryForList(anyString(), anyMap())).thenAnswer(invocacao -> {
            Map<?, ?> parametros = invocacao.getArgument(1);
            if (parametros.containsKey("titulos")) {
                return List.of();
            }
            if (parametros.containsKey("codigos")) {
                // unidade_codigo como String para testar ramo toString em lerLong
                return List.of(Map.of("usuario_titulo", "111", "perfil", "INVALIDO", "UNIDADE_CODIGO", "10"));
            }
            return List.of();
        });

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.grupos())
                .extracting(GrupoViolacaoOrganizacionalDto::tipo)
                .contains("VW_USUARIO_PERFIL_UNIDADE com perfil invalido");
    }

    @Test
    @DisplayName("diagnosticar ignora linha de titulo duplicado com titulo nulo ou vazio")
    void diagnosticarIgnoraLinhaDeTituloDuplicadoComTituloNuloOuVazio() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of(
                unidade(5L, "OPER", TipoUnidade.OPERACIONAL, "555")
        ));
        when(cacheViewsOrganizacaoService.listarTodasResponsabilidades()).thenReturn(List.of(new ResponsabilidadeLeitura(5L, "555")));
        when(usuarioRepo.findAllById(anyCollection())).thenReturn(List.of(usuario("555")));
        when(namedParameterJdbcTemplate.queryForList(anyString(), anyMap())).thenAnswer(invocacao -> {
            Map<?, ?> parametros = invocacao.getArgument(1);
            if (parametros.containsKey("titulos")) {
                // linha com titulo nulo: deve ser ignorada na contagem de duplicatas
                Map<String, Object> linhaNula = new HashMap<>();
                linhaNula.put("titulo", null);
                return List.of(linhaNula);
            }
            return List.of();
        });

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.grupos())
                .extracting(GrupoViolacaoOrganizacionalDto::tipo)
                .doesNotContain("VW_USUARIO com titulo duplicado");
    }

    @Test
    @DisplayName("diagnosticar nao reporta intermediaria quando responsabilidade nao existe mas possui gestor")
    void diagnosticarNaoReportaIntermediariaQuandoResponsabilidadeNaoExisteMasPossuiGestor() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of(
                new UnidadeHierarquiaLeitura(1L, "INT", "INT", "111", TipoUnidade.INTERMEDIARIA, SituacaoUnidade.ATIVA, null),
                new UnidadeHierarquiaLeitura(2L, "OP", "OP", "111", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, 1L)
        ));
        // Sem responsabilidade para a unidade intermediaria
        when(cacheViewsOrganizacaoService.listarTodasResponsabilidades()).thenReturn(List.of(
                new ResponsabilidadeLeitura(2L, "111")
        ));
        when(usuarioRepo.findAllById(anyCollection())).thenReturn(List.of(usuario("111")));
        when(namedParameterJdbcTemplate.queryForList(anyString(), anyMap())).thenAnswer(invocacao -> {
            Map<?, ?> parametros = invocacao.getArgument(1);
            if (parametros.containsKey("codigos")) {
                return List.of(Map.of("usuario_titulo", "111", "perfil", "GESTOR", "unidade_codigo", 1L));
            }
            return List.of();
        });

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.grupos())
                .extracting(GrupoViolacaoOrganizacionalDto::tipo)
                .doesNotContain("Unidade intermediaria sem perfil GESTOR")
                .doesNotContain("Responsavel de unidade intermediaria sem perfil GESTOR correspondente");
    }

    @Test
    @DisplayName("diagnosticar acusa titular referenciado ausente na view de usuarios")
    void diagnosticarAcusaTitularReferenciadoAusenteNaViewDeUsuarios() {
        when(cacheViewsOrganizacaoService.listarTodasUnidades()).thenReturn(List.of(
                unidade(7L, "OPER7", TipoUnidade.OPERACIONAL, "777")
        ));
        when(cacheViewsOrganizacaoService.listarTodasResponsabilidades()).thenReturn(List.of());
        when(usuarioRepo.findAllById(anyCollection())).thenReturn(List.of()); // usuario nao existe
        when(namedParameterJdbcTemplate.queryForList(anyString(), anyMap())).thenReturn(List.of());

        DiagnosticoOrganizacionalDto diagnostico = validador.diagnosticar();

        assertThat(diagnostico.grupos())
                .extracting(GrupoViolacaoOrganizacionalDto::tipo)
                .contains("Titular referenciado ausente na VW_USUARIO");
    }
}
