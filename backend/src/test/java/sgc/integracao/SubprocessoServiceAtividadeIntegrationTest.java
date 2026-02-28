package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.transaction.annotation.*;
import sgc.fixture.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
@Transactional
@DisplayName("Integração: SubprocessoService - Importação de Atividades")
class SubprocessoServiceAtividadeIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SubprocessoService subprocessoService;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Autowired
    private AtividadeRepo atividadeRepo;

    private Subprocesso subprocessoDestino;
    private Subprocesso subprocessoOrigem;

    @BeforeEach
    void setUp() {
        Unidade unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_ATIV");
        unidade.setNome("Unidade de Atividade");
        unidade = unidadeRepo.save(unidade);

        Usuario admin = usuarioRepo.findById("111111111111").orElseThrow();
        admin.setUnidadeAtivaCodigo(unidade.getCodigo());
        admin.setPerfilAtivo(Perfil.CHEFE);

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(admin, null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CHEFE")))
        );

        Processo processo = Processo.builder()
                .descricao("Processo Teste Ativ")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processo);

        subprocessoDestino = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.NAO_INICIADO)
                .processo(processo)
                .build();
        subprocessoRepo.save(subprocessoDestino);
        Mapa mapaDestino = new Mapa();
        mapaDestino.setSubprocesso(subprocessoDestino);
        mapaRepo.save(mapaDestino);
        subprocessoDestino.setMapa(mapaDestino);

        subprocessoOrigem = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO)
                .processo(processo)
                .build();
        subprocessoRepo.save(subprocessoOrigem);
        Mapa mapaOrigem = new Mapa();
        mapaOrigem.setSubprocesso(subprocessoOrigem);
        mapaRepo.save(mapaOrigem);
        subprocessoOrigem.setMapa(mapaOrigem);

        Atividade atividadeOrigem = Atividade.builder().mapa(mapaOrigem).descricao("Atividade Importada").build();
        atividadeRepo.save(atividadeOrigem);
    }

    @Test
    @DisplayName("importarAtividades: Deve importar atividades de outro mapa com sucesso")
    void importarAtividades_Sucesso() {
        subprocessoService.importarAtividades(subprocessoDestino.getCodigo(), subprocessoOrigem.getCodigo());

        Subprocesso destAtualizado = subprocessoRepo.findById(subprocessoDestino.getCodigo()).orElseThrow();
        assertThat(destAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        long countAtividadesDestino = atividadeRepo.findByMapa_Codigo(destAtualizado.getMapa().getCodigo()).size();
        assertThat(countAtividadesDestino).isEqualTo(1);
    }
}
