package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import sgc.fixture.UnidadeFixture;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.SubprocessoDetalheResponse;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.organizacao.model.Responsabilidade;
import sgc.organizacao.model.ResponsabilidadeRepo;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Obtenção de Contexto/Detalhes")
class SubprocessoServiceContextoIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoService subprocessoService;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private Unidade unidade;
    private Usuario admin;
    private Processo processo;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        // Find existing unit with a responsibility established in the seed.sql/data.sql
        // Unidade 8 (SEDESENV) has titular '3' (00000003)
        unidade = unidadeRepo.findById(8L).orElseThrow();

        admin = usuarioRepo.findById("3").orElseThrow(); // Using existing titular to avoid mock setups
        admin.setUnidadeAtivaCodigo(unidade.getCodigo());
        admin.setPerfilAtivo(Perfil.ADMIN);

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(admin, null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        processo = Processo.builder()
                .descricao("Processo Teste Contexto")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processo);

        subprocesso = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .processo(processo)
                .build();
        subprocessoRepo.save(subprocesso);
        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocesso);
        mapaRepo.save(mapa);
        subprocesso.setMapa(mapa);
    }

    @Test
    @DisplayName("obterDetalhes: Deve obter os detalhes de um subprocesso")
    void obterDetalhes_Sucesso() {
        SubprocessoDetalheResponse detalhes = subprocessoService.obterDetalhes(subprocesso.getCodigo(), admin);

        assertThat(detalhes).isNotNull();
        assertThat(detalhes.subprocesso().getCodigo()).isEqualTo(subprocesso.getCodigo());
        assertThat(detalhes.subprocesso().getSituacao().name()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO.name());
        assertThat(detalhes.subprocesso().getUnidade().getSigla()).isEqualTo(unidade.getSigla());
    }
}
