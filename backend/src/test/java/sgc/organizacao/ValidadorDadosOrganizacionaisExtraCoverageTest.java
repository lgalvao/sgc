package sgc.organizacao;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.jdbc.core.namedparam.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidadorDadosOrganizacionais - Cobertura Adicional")
class ValidadorDadosOrganizacionaisExtraCoverageTest {

    @Mock private UnidadeRepo unidadeRepo;
    @Mock private UsuarioRepo usuarioRepo;
    @Mock private ResponsabilidadeRepo responsabilidadeRepo;
    @Mock private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @InjectMocks
    private ValidadorDadosOrganizacionais target;

    @Test
    @DisplayName("Deve cobrir merge de responsabilidades duplicadas para mesma unidade")
    void deveCobrirMergeResponsabilidadesDuplicadas() {
        UnidadeHierarquiaLeitura u = new UnidadeHierarquiaLeitura(1L, "Nome", "U1", null, TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, null);
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(List.of(u));
        
        ResponsabilidadeLeitura r1 = new ResponsabilidadeLeitura(1L, "T1");
        ResponsabilidadeLeitura r2 = new ResponsabilidadeLeitura(1L, "T2");
        when(responsabilidadeRepo.listarLeiturasPorCodigosUnidade(anyList())).thenReturn(List.of(r1, r2));
        
        when(usuarioRepo.findAllById(anyList())).thenReturn(Collections.emptyList());
        when(namedParameterJdbcTemplate.queryForList(anyString(), ArgumentMatchers.<Map<String, ?>>any())).thenReturn(Collections.emptyList());

        DiagnosticoOrganizacionalDto res = target.diagnosticar();
        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("Deve cobrir extração de sigla em diagnostico resumo")
    void deveCobrirExtracaoSiglaResumo() {
        UnidadeHierarquiaLeitura u = new UnidadeHierarquiaLeitura(1L, "Nome", "U1", null, TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, null);
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(List.of(u));
        when(responsabilidadeRepo.listarLeiturasPorCodigosUnidade(anyList())).thenReturn(Collections.emptyList());

        DiagnosticoOrganizacionalDto res = target.diagnosticar();
        assertThat(res.resumo()).contains("U1");
    }

    @Test
    @DisplayName("Deve cobrir unidade intermediaria com gestor e responsabilidade")
    void deveCobrirIntermediariaComGestorEResponsabilidade() {
        UnidadeHierarquiaLeitura u1 = new UnidadeHierarquiaLeitura(1L, "INT", "INT", null, TipoUnidade.INTERMEDIARIA, SituacaoUnidade.ATIVA, null);
        UnidadeHierarquiaLeitura u2 = new UnidadeHierarquiaLeitura(2L, "OPE", "OPE", null, TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, 1L);

        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(List.of(u1, u2));
        
        ResponsabilidadeLeitura r1 = new ResponsabilidadeLeitura(1L, "GESTOR1");
        when(responsabilidadeRepo.listarLeiturasPorCodigosUnidade(anyList())).thenReturn(List.of(r1));
        
        when(usuarioRepo.findAllById(anyList())).thenReturn(Collections.emptyList());
        
        Map<String, Object> p1 = new HashMap<>();
        p1.put("USUARIO_TITULO", "GESTOR1");
        p1.put("PERFIL", "GESTOR");
        p1.put("UNIDADE_CODIGO", 1L);
        
        when(namedParameterJdbcTemplate.queryForList(contains("vw_usuario_perfil_unidade"), ArgumentMatchers.<Map<String, ?>>any()))
                .thenReturn(List.of(p1));
        when(namedParameterJdbcTemplate.queryForList(contains("FROM sgc.vw_usuario\n"), ArgumentMatchers.<Map<String, ?>>any()))
                .thenReturn(Collections.emptyList());

        DiagnosticoOrganizacionalDto res = target.diagnosticar();
        assertThat(res.grupos().stream().noneMatch(g -> g.tipo().contains("Unidade intermediaria"))).isTrue();
    }
}
