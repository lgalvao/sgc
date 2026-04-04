package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.transaction.annotation.*;
import sgc.*;
import sgc.alerta.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = Sgc.class)
@ActiveProfiles("test")
@Transactional
class SubprocessoTransicaoServiceCoverageIntegrationTest {

    @Autowired
    private SubprocessoTransicaoService transicaoService;

    @Autowired
    private SubprocessoRepo subprocessoRepo;

    @Autowired
    private ProcessoRepo processoRepo;

    @Autowired
    private MapaRepo mapaRepo;

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private MovimentacaoRepo movimentacaoRepo;

    @MockitoBean
    private SubprocessoValidacaoService validacaoService;

    @MockitoBean
    private SubprocessoNotificacaoService notificacaoService;

    @MockitoBean
    private UnidadeService unidadeService;

    @MockitoBean
    private HierarquiaService hierarquiaService;

    @MockitoBean
    private UsuarioFacade usuarioFacade;

    @MockitoBean
    private ImpactoMapaService impactoMapaService;

    @MockitoBean
    private MapaManutencaoService mapaManutencaoService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private AlertaFacade alertaService;

    private Processo criarProcessoPersistido(TipoProcesso tipo) {
        Processo processo = new Processo();
        processo.setDescricao("Processo teste");
        processo.setTipo(tipo);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processo.setDataLimite(LocalDateTime.now().plusDays(30));
        return processoRepo.save(processo);
    }

    private Usuario criarUsuarioPersistido(Unidade unidade) {
        Usuario usuario = usuarioRepo.getReferenceById("111111111111");
        usuario.setUnidadeAtivaCodigo(unidade.getCodigo());
        return usuario;
    }

    private void registrarMovimentacaoInicial(Subprocesso subprocesso) {
        Usuario usuario = usuarioRepo.getReferenceById("111111111111");
        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocesso)
                .unidadeOrigem(subprocesso.getUnidade())
                .unidadeDestino(subprocesso.getUnidade())
                .usuario(usuario)
                .descricao("Movimentação inicial de teste")
                .build());
    }

    @Nested
    @DisplayName("criarAnalise")
    class CriarAnalise {

        @Test
        @DisplayName("deve criar analise e retornar")
        void deveCriarAnalise() {
            Processo proc = criarProcessoPersistido(TipoProcesso.MAPEAMENTO);

            Unidade unidade = new Unidade();
            unidade.setSigla("U1");
            unidade = unidadeRepo.save(unidade);

            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            sp.setProcesso(proc);
            sp.setUnidade(unidade);
            sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(30));
            sp = subprocessoRepo.save(sp);

            when(unidadeService.buscarPorSigla("U1")).thenReturn(unidade);

            Usuario user = new Usuario();
            user.setTituloEleitoral("Titulo");
            user.setUnidadeAtivaCodigo(unidade.getCodigo());

            CriarAnaliseRequest request = CriarAnaliseRequest.builder()
                    .observacoes("Obs")
                    .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                    .motivo("Motivo")
                    .build();

            Analise analise = transicaoService.criarAnalise(sp, request, TipoAnalise.CADASTRO, user);

            assertThat(analise).isNotNull();
            assertThat(analise.getSubprocesso()).isEqualTo(sp);
            assertThat(analise.getObservacoes()).isEqualTo("Obs");
            assertThat(analise.getAcao()).isEqualTo(TipoAcaoAnalise.ACEITE_MAPEAMENTO);
            assertThat(analise.getUsuarioTitulo()).isEqualTo("Titulo");
            assertThat(analise.getMotivo()).isEqualTo("Motivo");
            assertThat(analise.getTipo()).isEqualTo(TipoAnalise.CADASTRO);
            assertThat(analise.getDataHora()).isNotNull();
            assertThat(analise.getCodigo()).isNotNull();
        }
    }

    @Nested
    @DisplayName("alterarDataLimite")
    class AlterarDataLimite {

        @Test
        @DisplayName("deve alterar data limite e enviar email/alerta para subprocesso em mapeamento")
        void deveAlterarDataLimiteEtapa1() {
            Processo proc = criarProcessoPersistido(TipoProcesso.MAPEAMENTO);

            Unidade unidade = new Unidade();
            unidade.setSigla("U1");
            unidade = unidadeRepo.save(unidade);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(unidade);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(30));
            sp = subprocessoRepo.save(sp);
            registrarMovimentacaoInicial(sp);

            java.time.LocalDate novaData = java.time.LocalDate.now().plusDays(30);
            transicaoService.alterarDataLimite(sp.getCodigo(), novaData);

            Subprocesso atualizado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(atualizado.getDataLimiteEtapa1()).isEqualTo(novaData.atStartOfDay());
        }

        @Test
        @DisplayName("deve alterar data limite e enviar email/alerta para subprocesso no mapa")
        void deveAlterarDataLimiteEtapa2() {
            Processo proc = criarProcessoPersistido(TipoProcesso.MAPEAMENTO);

            Unidade unidade = new Unidade();
            unidade.setSigla("U1");
            unidade = unidadeRepo.save(unidade);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(unidade);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
            sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(30));
            sp = subprocessoRepo.save(sp);
            registrarMovimentacaoInicial(sp);

            java.time.LocalDate novaData = java.time.LocalDate.now().plusDays(30);
            transicaoService.alterarDataLimite(sp.getCodigo(), novaData);

            Subprocesso atualizado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(atualizado.getDataLimiteEtapa2()).isEqualTo(novaData.atStartOfDay());
        }

        @Test
        @DisplayName("deve alterar data limite etapa1 para situacoes desconhecidas")
        void deveAlterarDataLimiteDesconhecido() {
            Processo proc = criarProcessoPersistido(TipoProcesso.MAPEAMENTO);

            Unidade unidade = new Unidade();
            unidade.setSigla("U1");
            unidade = unidadeRepo.save(unidade);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(unidade);
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(30));
            sp = subprocessoRepo.save(sp);

            java.time.LocalDate novaData = java.time.LocalDate.now().plusDays(30);
            transicaoService.alterarDataLimite(sp.getCodigo(), novaData);

            Subprocesso atualizado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(atualizado.getDataLimiteEtapa1()).isEqualTo(novaData.atStartOfDay());
        }
    }


    @Nested
    @DisplayName("disponibilizar")
    class Disponibilizar {

        @Test
        @DisplayName("deve disponibilizar cadastro com unidade superior valida")
        void comSuperior() {
            Processo proc = criarProcessoPersistido(TipoProcesso.MAPEAMENTO);

            Unidade uSup = new Unidade();
            uSup.setSigla("ADMIN");
            uSup = unidadeRepo.save(uSup);

            Unidade uSp = new Unidade();
            uSp.setSigla("U4");
            uSp.setUnidadeSuperior(uSup);
            uSp = unidadeRepo.save(uSp);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(uSp);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(30));
            sp = subprocessoRepo.saveAndFlush(sp);
            registrarMovimentacaoInicial(sp);

            Mapa mapa = new Mapa();
            mapa.setSugestoes("Sugestoes");
            mapa.setSubprocesso(sp);
            mapa = mapaRepo.saveAndFlush(mapa);

            Usuario user = criarUsuarioPersistido(uSp);

            // Need to flush and let Hibernate re-associate in a new query context so mapping exists
            subprocessoRepo.flush();
            sp.setMapa(mapa);
            subprocessoRepo.saveAndFlush(sp);

            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(mapa.getCodigo()))
                    .thenReturn(java.util.List.of());

            transicaoService.disponibilizarCadastro(sp.getCodigo(), user);

            Subprocesso atualizado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        }
    }

    @Nested
    @DisplayName("disponibilizarMapa")
    class DisponibilizarMapa {

        @Test
        @DisplayName("deve disponibilizar mapa e salvar observacoes")
        void comObservacoes() {
            Processo proc = criarProcessoPersistido(TipoProcesso.MAPEAMENTO);

            Unidade uAdmin = new Unidade();
            uAdmin.setSigla("ADMIN");
            uAdmin = unidadeRepo.save(uAdmin);
            when(unidadeService.buscarPorSigla("ADMIN")).thenReturn(uAdmin);

            Unidade uSp = new Unidade();
            uSp.setSigla("U4");
            uSp = unidadeRepo.save(uSp);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(uSp);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(1));
            sp = subprocessoRepo.saveAndFlush(sp);
            registrarMovimentacaoInicial(sp);

            Mapa mapa = new Mapa();
            mapa.setSugestoes(null);
            mapa.setSubprocesso(sp);
            mapa = mapaRepo.saveAndFlush(mapa);

            sp.setMapa(mapa);
            subprocessoRepo.saveAndFlush(sp);

            Usuario user = criarUsuarioPersistido(uSp);

            proc.setDataCriacao(java.time.LocalDateTime.now().plusDays(5));
            proc = processoRepo.saveAndFlush(proc);

            sgc.subprocesso.dto.DisponibilizarMapaRequest request = sgc.subprocesso.dto.DisponibilizarMapaRequest.builder()
                .observacoes("novas observacoes")
                .dataLimite(proc.getDataCriacao().toLocalDate().plusDays(1))
                .build();

            transicaoService.disponibilizarMapa(sp.getCodigo(), request, user);

            Subprocesso atualizado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
            assertThat(atualizado.getMapa().getSugestoes()).isEqualTo("novas observacoes");
        }
    }

    @Nested
    @DisplayName("submeterMapaAjustado")
    class SubmeterMapaAjustado {

        @Test
        @DisplayName("deve submeter e alterar a data limite")
        void comDataLimite() {
            Processo proc = criarProcessoPersistido(TipoProcesso.REVISAO);

            Unidade uSp = new Unidade();
            uSp.setSigla("U5");
            uSp = unidadeRepo.save(uSp);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(uSp);
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(30));
            sp = subprocessoRepo.saveAndFlush(sp);
            registrarMovimentacaoInicial(sp);

            Mapa mapa = new Mapa();
            mapa.setSugestoes(null);
            mapa.setSubprocesso(sp);
            mapa = mapaRepo.saveAndFlush(mapa);

            sp.setMapa(mapa);
            subprocessoRepo.saveAndFlush(sp);

            Usuario user = criarUsuarioPersistido(uSp);

            sgc.subprocesso.dto.SubmeterMapaAjustadoRequest request = sgc.subprocesso.dto.SubmeterMapaAjustadoRequest.builder()
                .justificativa("ajustes realizados")
                .dataLimiteEtapa2(java.time.LocalDateTime.of(2025, 12, 31, 0, 0))
                .build();

            transicaoService.submeterMapaAjustado(sp.getCodigo(), request, user);

            Subprocesso atualizado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);
            assertThat(atualizado.getDataLimiteEtapa2()).isEqualTo(java.time.LocalDateTime.of(2025, 12, 31, 0, 0));
        }
    }

    @Nested
    @DisplayName("devolverValidacao")
    class DevolverValidacao {

        @Test
        @DisplayName("deve devolver validacao do mapa corretamente")
        void deveDevolver() {
            Processo proc = criarProcessoPersistido(TipoProcesso.MAPEAMENTO);

            Unidade uSp = new Unidade();
            uSp.setSigla("U6");
            uSp = unidadeRepo.save(uSp);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(uSp);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(30));
            sp = subprocessoRepo.saveAndFlush(sp);
            registrarMovimentacaoInicial(sp);

            Mapa mapa = new Mapa();
            mapa.setSugestoes(null);
            mapa.setSubprocesso(sp);
            mapa = mapaRepo.saveAndFlush(mapa);

            sp.setMapa(mapa);
            subprocessoRepo.saveAndFlush(sp);

            Usuario user = criarUsuarioPersistido(uSp);

            when(unidadeService.buscarPorSigla("U6")).thenReturn(uSp);

            transicaoService.devolverValidacao(sp.getCodigo(), "justificativa erro", user);

            Subprocesso atualizado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        }
    }

    @Nested
    @DisplayName("aceitarValidacao")
    class AceitarValidacao {

        @Test
        @DisplayName("deve aceitar validacao do mapa com superior")
        void deveAceitarValidacao() {
            Processo proc = criarProcessoPersistido(TipoProcesso.MAPEAMENTO);

            Unidade uSup = new Unidade();
            uSup.setSigla("USUP");
            uSup = unidadeRepo.save(uSup);

            Unidade uSp = new Unidade();
            uSp.setSigla("U7");
            uSp.setUnidadeSuperior(uSup);
            uSp = unidadeRepo.save(uSp);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(uSp);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            sp.setDataLimiteEtapa1(LocalDateTime.now().plusDays(30));
            sp = subprocessoRepo.saveAndFlush(sp);
            registrarMovimentacaoInicial(sp);

            Mapa mapa = new Mapa();
            mapa.setSugestoes(null);
            mapa.setSubprocesso(sp);
            mapa = mapaRepo.saveAndFlush(mapa);

            sp.setMapa(mapa);
            subprocessoRepo.saveAndFlush(sp);

            Usuario user = criarUsuarioPersistido(uSp);

            when(unidadeService.buscarPorSigla("U7")).thenReturn(uSp);

            transicaoService.aceitarValidacao(sp.getCodigo(), "Observacao aceite", user);

            Subprocesso atualizado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        }
    }

}
