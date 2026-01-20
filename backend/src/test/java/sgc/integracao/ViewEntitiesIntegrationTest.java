package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.model.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Integração: Entidades de Views (SGC)")
class ViewEntitiesIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private VinculacaoUnidadeRepo vinculacaoUnidadeRepo;

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Deve persistir e consultar VW_UNIDADE")
    void testUnidadeView() {
        // Arrange
        Long id = 9901L;
        jdbcTemplate.update("INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO, matricula_titular, titulo_titular) VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, "Unidade de Teste View", "UTVIEW", TipoUnidade.OPERACIONAL.name(), SituacaoUnidade.ATIVA.name(), "12345", "999999999999");
        
        // Act & Assert
        Optional<Unidade> found = unidadeRepo.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getNome()).isEqualTo("Unidade de Teste View");
        assertThat(found.get().getSigla()).isEqualTo("UTVIEW");
        assertThat(found.get().getMatriculaTitular()).isEqualTo("12345");
        assertThat(found.get().getTituloTitular()).isEqualTo("999999999999");
        assertThat(found.get().getTipo()).isEqualTo(TipoUnidade.OPERACIONAL);
    }

    @Test
    @DisplayName("Deve persistir e consultar VW_USUARIO")
    void testUsuarioView() {
        // Arrange
        Long unidadeId = 9902L;
        jdbcTemplate.update("INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO) VALUES (?, ?, ?, ?, ?)",
                unidadeId, "Unidade Lotacao", "ULOT", TipoUnidade.OPERACIONAL.name(), SituacaoUnidade.ATIVA.name());

        String uniqueId = "999888777666";
        jdbcTemplate.update("INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, MATRICULA, unidade_lot_codigo) VALUES (?, ?, ?, ?, ?, ?)",
                uniqueId, "Usuario Teste View", "teste@view.com", "1234", "88888", unidadeId);
        
        // Act & Assert
        Optional<Usuario> found = usuarioRepo.findById(uniqueId);
        assertThat(found).isPresent();
        assertThat(found.get().getNome()).isEqualTo("Usuario Teste View");
        assertThat(found.get().getEmail()).isEqualTo("teste@view.com");
        assertThat(found.get().getUnidadeLotacao().getCodigo()).isEqualTo(unidadeId);
    }

    @Test
    @DisplayName("Deve persistir e consultar VW_VINCULACAO_UNIDADE")
    void testVinculacaoUnidadeView() {
        // Arrange
        Long unidadeAtualId = 9903L;
        jdbcTemplate.update("INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO) VALUES (?, ?, ?, ?, ?)",
                unidadeAtualId, "Unidade Atual", "UATUAL", TipoUnidade.OPERACIONAL.name(), SituacaoUnidade.ATIVA.name());
        
        Long unidadeAntId = 9904L;
        jdbcTemplate.update("INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO) VALUES (?, ?, ?, ?, ?)",
                unidadeAntId, "Unidade Anterior", "UANT", TipoUnidade.OPERACIONAL.name(), SituacaoUnidade.ATIVA.name());

        jdbcTemplate.update("INSERT INTO SGC.VW_VINCULACAO_UNIDADE (unidade_atual_codigo, unidade_anterior_codigo, demais_unidades_historicas) VALUES (?, ?, ?)",
                unidadeAtualId, unidadeAntId, "UANT -> UANT2 -> UANT3");

        // Act & Assert
        VinculacaoUnidadeId id = new VinculacaoUnidadeId(unidadeAtualId, unidadeAntId);
        Optional<VinculacaoUnidade> found = vinculacaoUnidadeRepo.findById(id);
        
        assertThat(found).isPresent();
        assertThat(found.get().getDemaisUnidadesHistoricas()).isEqualTo("UANT -> UANT2 -> UANT3");
    }

    @Test
    @DisplayName("Deve persistir e consultar VW_USUARIO_PERFIL_UNIDADE")
    void testUsuarioPerfilUnidadeView() {
        // Arrange
        Long unidadeId = 9905L;
        jdbcTemplate.update("INSERT INTO SGC.VW_UNIDADE (codigo, NOME, SIGLA, TIPO, SITUACAO) VALUES (?, ?, ?, ?, ?)",
                unidadeId, "Unidade Perfil", "UPERFIL", TipoUnidade.OPERACIONAL.name(), SituacaoUnidade.ATIVA.name());

        String uniqueId = "555444333222";
        jdbcTemplate.update("INSERT INTO SGC.VW_USUARIO (TITULO, NOME, EMAIL, RAMAL, MATRICULA, unidade_lot_codigo) VALUES (?, ?, ?, ?, ?, ?)",
                uniqueId, "Usuario Perfil", "perfil@view.com", "1234", "88888", unidadeId);

        jdbcTemplate.update("INSERT INTO SGC.VW_USUARIO_PERFIL_UNIDADE (usuario_titulo, unidade_codigo, perfil) VALUES (?, ?, ?)",
                uniqueId, unidadeId, Perfil.GESTOR.name());
        
        // Act & Assert
        List<UsuarioPerfil> perfis = usuarioPerfilRepo.findByUsuarioTitulo(uniqueId);
        assertThat(perfis).hasSize(1);
        assertThat(perfis.getFirst().getPerfil()).isEqualTo(Perfil.GESTOR);
        assertThat(perfis.getFirst().getUnidade().getCodigo()).isEqualTo(unidadeId);
    }
}
