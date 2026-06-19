package sgc.integracao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.NotificacaoEmailRepo;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.fixture.UnidadeFixture;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoConsultaService;
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
    private SubprocessoConsultaService consultaService;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private AtividadeRepo atividadeRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;
    @Autowired
    private AlertaRepo alertaRepo;
    @Autowired
    private NotificacaoEmailRepo notificacaoEmailRepo;

    private Subprocesso subprocessoDestino;
    private Subprocesso subprocessoOrigem;
    private Usuario chefe;
    private Atividade atividadeOrigem;

    @BeforeEach
    void setUp() {
        Unidade unidade = UnidadeFixture.unidadePadrao();
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
                .descricao("Processo teste ativ")
                .tipo(TipoProcesso.MAPEAMENTO)
                .situacao(SituacaoProcesso.EM_ANDAMENTO)
                .dataLimite(LocalDateTime.now().plusDays(30))
                .build();
        processoRepo.save(processo);

        subprocessoDestino = Subprocesso.builder()
                .unidade(unidade)
                .situacao(SituacaoSubprocesso.NAO_INICIADO)
                .processo(processo)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(15))
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
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(15))
                .build();
        subprocessoRepo.save(subprocessoOrigem);
        Mapa mapaOrigem = new Mapa();
        mapaOrigem.setSubprocesso(subprocessoOrigem);
        mapaRepo.save(mapaOrigem);
        subprocessoOrigem.setMapa(mapaOrigem);

        atividadeOrigem = Atividade.builder().mapa(mapaOrigem).descricao("Atividade importada").build();
        atividadeRepo.save(atividadeOrigem);
    }

    private void registrarMovimentacaoInicialDoTeste(Subprocesso subprocesso) {
        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(subprocesso.getUnidade())
                .unidadeDestino(subprocesso.getUnidade())
                .usuario(chefe)
                .descricao("Movimentação inicial de teste")
                .build());
    }

    @Test
    @DisplayName("importarAtividades: Deve importar atividades de outro mapa com sucesso")
    void importarAtividades_Sucesso() {
        long totalMovimentacoesAntes = movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(subprocessoDestino.getCodigo()).size();
        long totalAlertasAntes = alertaRepo.count();
        long totalNotificacoesAntes = notificacaoEmailRepo
                .findBySubprocesso_CodigoOrderByDataHoraCriacaoDesc(subprocessoDestino.getCodigo(), Pageable.ofSize(100))
                .size();

        subprocessoService.importarAtividades(subprocessoDestino.getCodigo(), subprocessoOrigem.getCodigo(), List.of());

        Subprocesso destAtualizado = subprocessoRepo.findById(subprocessoDestino.getCodigo()).orElseThrow();
        assertThat(destAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        long countAtividadesDestino = atividadeRepo.findByMapa_Codigo(destAtualizado.getMapa().getCodigo()).size();
        assertThat(countAtividadesDestino).isEqualTo(1);
        assertThat(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(subprocessoDestino.getCodigo()))
                .hasSize((int) totalMovimentacoesAntes);
        assertThat(alertaRepo.count()).isEqualTo(totalAlertasAntes);
        assertThat(notificacaoEmailRepo
                .findBySubprocesso_CodigoOrderByDataHoraCriacaoDesc(subprocessoDestino.getCodigo(), Pageable.ofSize(100)))
                .hasSize((int) totalNotificacoesAntes);
    }

    @Test
    @DisplayName("importarAtividades: Deve importar somente as atividades selecionadas (codigosAtividades)")
    void importarAtividades_SeletivaSucesso() {
        Atividade atividadeExtra = Atividade.builder()
                .mapa(subprocessoOrigem.getMapa())
                .descricao("Atividade nao selecionada")
                .build();
        atividadeRepo.save(atividadeExtra);

        subprocessoService.importarAtividades(
                subprocessoDestino.getCodigo(),
                subprocessoOrigem.getCodigo(),
                List.of(atividadeOrigem.getCodigo())
        );

        Subprocesso destAtualizado = subprocessoRepo.findById(subprocessoDestino.getCodigo()).orElseThrow();
        long countAtividadesDestino = atividadeRepo.findByMapa_Codigo(destAtualizado.getMapa().getCodigo()).size();
        assertThat(countAtividadesDestino).isEqualTo(1);
        assertThat(atividadeRepo.findByMapa_Codigo(destAtualizado.getMapa().getCodigo()).getFirst().getDescricao())
                .isEqualTo("Atividade importada");
    }

    @Test
    @DisplayName("importarAtividades: Deve negar acesso se usuário não for o chefe da unidade de destino")
    void importarAtividades_NegarAcessoDestino() {
        chefe.setUnidadeAtivaCodigo(999L); // Different unit

        assertThatThrownBy(() -> subprocessoService.importarAtividades(subprocessoDestino.getCodigo(), subprocessoOrigem.getCodigo(), List.of()))
                .isInstanceOf(ErroAcessoNegado.class)
                .hasMessageContaining("Usuário não tem permissão para importar atividades");
    }

    @Test
    @DisplayName("importarAtividades: Deve negar acesso se usuário não tiver permissão de consulta na origem")
    void importarAtividades_NegarAcessoOrigem() {
        chefe.setPerfilAtivo(Perfil.SERVIDOR);
        assertThatThrownBy(() -> subprocessoService.importarAtividades(subprocessoDestino.getCodigo(), subprocessoOrigem.getCodigo(), List.of()))
                .isInstanceOf(ErroAcessoNegado.class)
                .hasMessageContaining("Usuário não tem permissão para importar atividades");
    }

    @Test
    @DisplayName("importarAtividades: Deve importar atividades de outro mapa para REVISAO")
    void importarAtividades_Revisao() {
        subprocessoDestino.getProcesso().setTipo(TipoProcesso.REVISAO);
        processoRepo.save(subprocessoDestino.getProcesso());

        subprocessoService.importarAtividades(subprocessoDestino.getCodigo(), subprocessoOrigem.getCodigo(), List.of());

        Subprocesso destAtualizado = subprocessoRepo.findById(subprocessoDestino.getCodigo()).orElseThrow();
        assertThat(destAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
    }

    @Test
    @DisplayName("importarAtividades: Nao deve alterar situacao se nao for NAO_INICIADO")
    void importarAtividades_SituacaoJaIniciada() {
        subprocessoDestino.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        subprocessoRepo.save(subprocessoDestino);
        registrarMovimentacaoInicialDoTeste(subprocessoDestino);

        subprocessoService.importarAtividades(subprocessoDestino.getCodigo(), subprocessoOrigem.getCodigo(), List.of());

        Subprocesso destAtualizado = subprocessoRepo.findById(subprocessoDestino.getCodigo()).orElseThrow();
        assertThat(destAtualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
    }

    @Test
    @DisplayName("importarAtividades: Falha se o destino não estiver em uma situação permitida para importação")
    void importarAtividades_FalhaSituacaoNaoPermitida() {
        subprocessoDestino.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        subprocessoRepo.save(subprocessoDestino);

        assertThatThrownBy(() -> subprocessoService.importarAtividades(subprocessoDestino.getCodigo(), subprocessoOrigem.getCodigo(), List.of()))
                .isInstanceOf(sgc.comum.erros.ErroValidacao.class)
                .hasMessageContaining("Situação do subprocesso não permite importação");
    }

    @Test
    @DisplayName("listarAtividadesSubprocesso: Deve listar atividades do subprocesso")
    void listarAtividadesSubprocesso_Sucesso() {
        subprocessoService.importarAtividades(subprocessoDestino.getCodigo(), subprocessoOrigem.getCodigo(), List.of());

        List<sgc.mapa.dto.AtividadeDto> atividades = consultaService.listarAtividadesSubprocesso(subprocessoDestino.getCodigo());

        assertThat(atividades).hasSize(1);
        assertThat(atividades.getFirst().descricao()).isEqualTo("Atividade importada");
    }
}
