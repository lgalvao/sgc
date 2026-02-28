package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.ErroAcessoNegado;
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
    private AtividadeRepo atividadeRepo;

    private Subprocesso subprocessoDestino;
    private Subprocesso subprocessoOrigem;
    private Usuario chefe;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        unidade = UnidadeFixture.unidadePadrao();
        unidade.setCodigo(null);
        unidade.setSigla("TEST_ATIV");
        unidade.setNome("Unidade de Atividade");
        unidade = unidadeRepo.save(unidade);

        chefe = usuarioRepo.findById("111111111111").orElseThrow();
        chefe.setUnidadeAtivaCodigo(unidade.getCodigo());
        chefe.setPerfilAtivo(Perfil.CHEFE);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(chefe, null, List.of(new SimpleGrantedAuthority("ROLE_CHEFE")))
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
                .localizacaoAtual(unidade)
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
                .localizacaoAtual(unidade)
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

    @Test
    @DisplayName("importarAtividades: Deve negar acesso se usuário não for o chefe da unidade de destino")
    void importarAtividades_NegarAcessoDestino() {
        chefe.setUnidadeAtivaCodigo(999L); // Different unit

        assertThatThrownBy(() -> subprocessoService.importarAtividades(subprocessoDestino.getCodigo(), subprocessoOrigem.getCodigo()))
                .isInstanceOf(ErroAcessoNegado.class)
                .hasMessageContaining("Usuário não tem permissão para importar atividades");
    }

    @Test
    @DisplayName("importarAtividades: Deve negar acesso se usuário não tiver permissão de consulta na origem")
    void importarAtividades_NegarAcessoOrigem() {
        chefe.setPerfilAtivo(Perfil.SERVIDOR);
        assertThatThrownBy(() -> subprocessoService.importarAtividades(subprocessoDestino.getCodigo(), subprocessoOrigem.getCodigo()))
                .isInstanceOf(ErroAcessoNegado.class)
                .hasMessageContaining("Usuário não tem permissão para importar atividades");
    }

    @Test
    @DisplayName("importarAtividades: Deve importar atividades de outro mapa para REVISAO")
    void importarAtividades_Revisao() {
        subprocessoDestino.getProcesso().setTipo(TipoProcesso.REVISAO);
        processoRepo.save(subprocessoDestino.getProcesso());

        subprocessoService.importarAtividades(subprocessoDestino.getCodigo(), subprocessoOrigem.getCodigo());

        Subprocesso destAtualizado = subprocessoRepo.findById(subprocessoDestino.getCodigo()).orElseThrow();
        assertThat(destAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
    }

    @Test
    @DisplayName("importarAtividades: Nao deve alterar situacao se nao for NAO_INICIADO")
    void importarAtividades_SituacaoJaIniciada() {
        subprocessoDestino.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.save(subprocessoDestino);

        subprocessoService.importarAtividades(subprocessoDestino.getCodigo(), subprocessoOrigem.getCodigo());

        Subprocesso destAtualizado = subprocessoRepo.findById(subprocessoDestino.getCodigo()).orElseThrow();
        assertThat(destAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
    }

    @Test
    @DisplayName("importarAtividades: Falha se o destino não estiver em uma situação permitida para importação")
    void importarAtividades_FalhaSituacaoNaoPermitida() {
        subprocessoDestino.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocessoRepo.save(subprocessoDestino);

        assertThatThrownBy(() -> subprocessoService.importarAtividades(subprocessoDestino.getCodigo(), subprocessoOrigem.getCodigo()))
                .isInstanceOf(sgc.comum.erros.ErroValidacao.class)
                .hasMessageContaining("Situação do subprocesso não permite importação");
    }

    @Test
    @DisplayName("listarAtividadesSubprocesso: Deve listar atividades do subprocesso")
    void listarAtividadesSubprocesso_Sucesso() {
        subprocessoService.importarAtividades(subprocessoDestino.getCodigo(), subprocessoOrigem.getCodigo());
        
        List<sgc.mapa.dto.AtividadeDto> atividades = subprocessoService.listarAtividadesSubprocesso(subprocessoDestino.getCodigo());
        
        assertThat(atividades).hasSize(1);
        assertThat(atividades.getFirst().descricao()).isEqualTo("Atividade Importada");
    }
}
