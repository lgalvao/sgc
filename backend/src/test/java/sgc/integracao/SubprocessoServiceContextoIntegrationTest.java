package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

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
