package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.fixture.UnidadeFixture;
import sgc.fixture.UsuarioFixture;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.AtividadeRepo;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.organizacao.model.UsuarioPerfilRepo;
import sgc.organizacao.model.UsuarioRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private Unidade unidade;
    private Usuario admin;
    private Processo processo;
    private Subprocesso subprocessoDestino;
    private Subprocesso subprocessoOrigem;

    @BeforeEach
    void setUp() {
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_ATIV");
        unidade.setNome("Unidade de Atividade");
        unidade = unidadeRepo.save(unidade);

        admin = usuarioRepo.findById("111111111111").orElseThrow();
        admin.setUnidadeAtivaCodigo(unidade.getCodigo());
        admin.setPerfilAtivo(Perfil.CHEFE);

        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(admin, null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CHEFE")))
        );

        processo = Processo.builder()
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
