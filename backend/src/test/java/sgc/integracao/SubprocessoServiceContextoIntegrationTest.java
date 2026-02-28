package sgc.integracao;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private Unidade unidade;
    private Usuario admin;
    private Subprocesso subprocesso;
    private Processo processo;

    @BeforeEach
    void setUp() {
        // Unidade 8 (SEDESENV) has titular '3' (00000003) in data.sql
        unidade = unidadeRepo.findById(8L).orElseThrow();

        admin = usuarioRepo.findById("3").orElseThrow();
        admin.setUnidadeAtivaCodigo(unidade.getCodigo());
        admin.setPerfilAtivo(Perfil.ADMIN);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
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
                .localizacaoAtual(unidade)
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
        assertThat(detalhes.permissoes()).isNotNull();
    }

    @Test
    @DisplayName("obterContextoEdicao: Deve obter o contexto completo para edição")
    void obterContextoEdicao_Sucesso() {
        ContextoEdicaoResponse contexto = subprocessoService.obterContextoEdicao(subprocesso.getCodigo());

        assertThat(contexto).isNotNull();
        assertThat(contexto.subprocesso().getCodigo()).isEqualTo(subprocesso.getCodigo());
        assertThat(contexto.detalhes()).isNotNull();
        assertThat(contexto.mapa()).isNotNull();
        assertThat(contexto.atividadesDisponiveis()).isNotNull();
    }

    @Test
    @DisplayName("obterPermissoesUI: Deve retornar todas as permissões falsas se o processo estiver FINALIZADO")
    void obterPermissoesUI_ProcessoFinalizado() {
        processo.setSituacao(SituacaoProcesso.FINALIZADO);
        processoRepo.save(processo);

        PermissoesSubprocessoDto permissoes = subprocessoService.obterPermissoesUI(subprocesso, admin);

        assertThat(permissoes.podeEditarCadastro()).isFalse();
        assertThat(permissoes.podeEditarMapa()).isFalse();
        assertThat(permissoes.podeHomologarMapa()).isFalse();
        assertThat(permissoes.podeReabrirCadastro()).isFalse();
    }

    @Test
    @DisplayName("obterPermissoesUI: Deve testar permissões de ADMIN na mesma unidade")
    void obterPermissoesUI_AdminMesmaUnidade() {
        admin.setPerfilAtivo(Perfil.ADMIN);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        PermissoesSubprocessoDto permissoes = subprocessoService.obterPermissoesUI(subprocesso, admin);

        assertThat(permissoes.podeHomologarCadastro()).isTrue();
        assertThat(permissoes.podeDevolverCadastro()).isTrue();
        assertThat(permissoes.podeAlterarDataLimite()).isTrue();
        assertThat(permissoes.podeReabrirCadastro()).isTrue();
        assertThat(permissoes.podeEnviarLembrete()).isTrue();
    }

    @Test
    @DisplayName("obterPermissoesUI: Deve testar permissões de CHEFE na mesma unidade")
    void obterPermissoesUI_ChefeMesmaUnidade() {
        admin.setPerfilAtivo(Perfil.CHEFE);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        PermissoesSubprocessoDto permissoes = subprocessoService.obterPermissoesUI(subprocesso, admin);

        assertThat(permissoes.podeEditarCadastro()).isTrue();
        assertThat(permissoes.podeDisponibilizarCadastro()).isTrue();
        assertThat(permissoes.podeAlterarDataLimite()).isFalse();
    }

    @Test
    @DisplayName("obterPermissoesUI: Deve testar permissões de GESTOR na mesma unidade")
    void obterPermissoesUI_GestorMesmaUnidade() {
        admin.setPerfilAtivo(Perfil.GESTOR);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        PermissoesSubprocessoDto permissoes = subprocessoService.obterPermissoesUI(subprocesso, admin);

        assertThat(permissoes.podeAceitarCadastro()).isTrue();
        assertThat(permissoes.podeDevolverCadastro()).isTrue();
        assertThat(permissoes.podeEnviarLembrete()).isTrue();
    }

    @Test
    @DisplayName("obterPermissoesUI: Deve testar visualização de impacto em REVISAO")
    void obterPermissoesUI_VisualizarImpacto() {
        // Unidade 8 established in data.sql has a mapping in UNIDADE_MAPA (mapa_vigente_codigo=1)
        // This should make temMapaVigente=true
        
        admin.setPerfilAtivo(Perfil.ADMIN);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

        PermissoesSubprocessoDto permissoes = subprocessoService.obterPermissoesUI(subprocesso, admin);

        assertThat(permissoes.podeVisualizarImpacto()).isTrue();
    }
}
