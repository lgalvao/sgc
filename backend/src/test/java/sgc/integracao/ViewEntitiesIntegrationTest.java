package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioPerfil;
import sgc.sgrh.model.UsuarioPerfilRepo;
import sgc.sgrh.model.UsuarioRepo;
import sgc.sgrh.model.Perfil;
import sgc.unidade.model.SituacaoUnidade;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;
import sgc.unidade.model.VinculacaoUnidade;
import sgc.unidade.model.VinculacaoUnidadeRepo;
import sgc.unidade.model.VinculacaoUnidadeId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Integração: Entidades de Views (SGC)")
public class ViewEntitiesIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private VinculacaoUnidadeRepo vinculacaoUnidadeRepo;

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Test
    @DisplayName("Deve persistir e consultar VW_UNIDADE")
    void testUnidadeView() {
        // Arrange
        Unidade unidade = new Unidade();
        unidade.setNome("Unidade de Teste View");
        unidade.setSigla("UTVIEW");
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setMatriculaTitular("12345");
        unidade.setTituloTitular("999999999999");
        unidade.setDataInicioTitularidade(LocalDateTime.now());
        
        // Act
        Unidade saved = unidadeRepo.save(unidade);
        
        // Assert
        Optional<Unidade> found = unidadeRepo.findById(saved.getCodigo());
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
        Unidade unidade = new Unidade("Unidade Lotacao", "ULOT");
        unidadeRepo.save(unidade);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123456789012");
        usuario.setNome("Usuario Teste View");
        usuario.setEmail("teste@view.com");
        usuario.setRamal("1234");
        usuario.setMatricula("88888");
        usuario.setUnidadeLotacao(unidade);
        
        // Act
        usuarioRepo.save(usuario);
        
        // Assert
        Optional<Usuario> found = usuarioRepo.findById("123456789012");
        assertThat(found).isPresent();
        assertThat(found.get().getNome()).isEqualTo("Usuario Teste View");
        assertThat(found.get().getEmail()).isEqualTo("teste@view.com");
        assertThat(found.get().getUnidadeLotacao().getCodigo()).isEqualTo(unidade.getCodigo());
    }

    @Test
    @DisplayName("Deve persistir e consultar VW_VINCULACAO_UNIDADE")
    void testVinculacaoUnidadeView() {
        // Arrange
        // Precisamos criar as unidades referenciadas primeiro por causa das FKs
        Unidade unidadeAtual = new Unidade("Unidade Atual", "UATUAL");
        unidadeRepo.save(unidadeAtual);
        
        Unidade unidadeAnterior = new Unidade("Unidade Anterior", "UANT");
        unidadeRepo.save(unidadeAnterior);

        VinculacaoUnidade vinculacao = new VinculacaoUnidade();
        vinculacao.setUnidadeAtualCodigo(unidadeAtual.getCodigo());
        vinculacao.setUnidadeAnteriorCodigo(unidadeAnterior.getCodigo());
        vinculacao.setDemaisUnidadesHistoricas("UANT -> UANT2 -> UANT3");

        // Act
        vinculacaoUnidadeRepo.save(vinculacao);

        // Assert
        VinculacaoUnidadeId id = new VinculacaoUnidadeId(unidadeAtual.getCodigo(), unidadeAnterior.getCodigo());
        Optional<VinculacaoUnidade> found = vinculacaoUnidadeRepo.findById(id);
        
        assertThat(found).isPresent();
        assertThat(found.get().getDemaisUnidadesHistoricas()).isEqualTo("UANT -> UANT2 -> UANT3");
    }

    @Test
    @DisplayName("Deve persistir e consultar VW_USUARIO_PERFIL_UNIDADE")
    void testUsuarioPerfilUnidadeView() {
        // Arrange
        Unidade unidade = new Unidade("Unidade Perfil", "UPERFIL");
        unidadeRepo.save(unidade);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("987654321098");
        usuario.setNome("Usuario Perfil");
        usuario.setUnidadeLotacao(unidade);
        usuarioRepo.save(usuario);

        UsuarioPerfil perfil = new UsuarioPerfil();
        perfil.setUsuarioTitulo("987654321098");
        perfil.setUnidadeCodigo(unidade.getCodigo());
        perfil.setPerfil(Perfil.GESTOR);
        
        // Act
        usuarioPerfilRepo.save(perfil);

        // Assert
        List<UsuarioPerfil> perfis = usuarioPerfilRepo.findByUsuarioTitulo("987654321098");
        assertThat(perfis).hasSize(1);
        assertThat(perfis.get(0).getPerfil()).isEqualTo(Perfil.GESTOR);
        assertThat(perfis.get(0).getUnidade().getCodigo()).isEqualTo(unidade.getCodigo());
    }
}
