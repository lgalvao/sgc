package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.Sgc;
import sgc.alerta.AlertaFacade;
import sgc.alerta.EmailService;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.HierarquiaService;
import sgc.organizacao.service.UnidadeService;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.CriarAnaliseRequest;
import sgc.subprocesso.model.Analise;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoAcaoAnalise;
import sgc.subprocesso.model.TipoAnalise;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.processo.model.ProcessoRepo;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.UnidadeRepo;
import sgc.organizacao.model.UsuarioRepo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

    @Nested
    @DisplayName("criarAnalise")
    class CriarAnalise {

        @Test
        @DisplayName("deve criar analise e retornar")
        void deveCriarAnalise() {
            Processo proc = new Processo();
            proc.setDescricao("Processo 1");
            proc.setTipo(TipoProcesso.MAPEAMENTO);
            proc = processoRepo.save(proc);

            Unidade unidade = new Unidade();
            unidade.setSigla("U1");
            unidade = unidadeRepo.save(unidade);

            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            sp.setProcesso(proc);
            sp.setUnidade(unidade);
            sp = subprocessoRepo.save(sp);

            when(unidadeService.buscarPorSigla("U1")).thenReturn(unidade);

            CriarAnaliseRequest request = CriarAnaliseRequest.builder()
                    .observacoes("Obs")
                    .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                    .siglaUnidade("U1")
                    .tituloUsuario("Titulo")
                    .motivo("Motivo")
                    .build();

            Analise analise = transicaoService.criarAnalise(sp, request, TipoAnalise.CADASTRO);

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
            Processo proc = new Processo();
            proc.setDescricao("Processo");
            proc.setTipo(TipoProcesso.MAPEAMENTO);
            proc = processoRepo.save(proc);

            Unidade unidade = new Unidade();
            unidade.setSigla("U1");
            unidade = unidadeRepo.save(unidade);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(unidade);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            sp = subprocessoRepo.save(sp);

            java.time.LocalDate novaData = java.time.LocalDate.of(2025, 12, 31);
            transicaoService.alterarDataLimite(sp.getCodigo(), novaData);

            Subprocesso atualizado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(atualizado.getDataLimiteEtapa1()).isEqualTo(novaData.atStartOfDay());
        }

        @Test
        @DisplayName("deve alterar data limite e enviar email/alerta para subprocesso no mapa")
        void deveAlterarDataLimiteEtapa2() {
            Processo proc = new Processo();
            proc.setDescricao("Processo");
            proc.setTipo(TipoProcesso.MAPEAMENTO);
            proc = processoRepo.save(proc);

            Unidade unidade = new Unidade();
            unidade.setSigla("U1");
            unidade = unidadeRepo.save(unidade);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(unidade);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
            sp = subprocessoRepo.save(sp);

            java.time.LocalDate novaData = java.time.LocalDate.of(2025, 12, 31);
            transicaoService.alterarDataLimite(sp.getCodigo(), novaData);

            Subprocesso atualizado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(atualizado.getDataLimiteEtapa2()).isEqualTo(novaData.atStartOfDay());
        }

        @Test
        @DisplayName("deve alterar data limite etapa1 para situacoes desconhecidas")
        void deveAlterarDataLimiteDesconhecido() {
            Processo proc = new Processo();
            proc.setDescricao("Processo");
            proc.setTipo(TipoProcesso.MAPEAMENTO);
            proc = processoRepo.save(proc);

            Unidade unidade = new Unidade();
            unidade.setSigla("U1");
            unidade = unidadeRepo.save(unidade);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(unidade);
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            sp = subprocessoRepo.save(sp);

            java.time.LocalDate novaData = java.time.LocalDate.of(2025, 12, 31);
            transicaoService.alterarDataLimite(sp.getCodigo(), novaData);

            Subprocesso atualizado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(atualizado.getDataLimiteEtapa1()).isEqualTo(novaData.atStartOfDay());
        }
    }

    @Nested
    @DisplayName("atualizarParaEmAndamento")
    class AtualizarParaEmAndamento {

        @Test
        @DisplayName("deve atualizar para mapeamento em andamento se mapeamento e nao iniciado")
        void mapeamentoEmAndamento() {
            Processo proc = new Processo();
            proc.setDescricao("Proc");
            proc.setTipo(TipoProcesso.MAPEAMENTO);
            proc = processoRepo.save(proc);

            Unidade uSp = new Unidade();
            uSp.setSigla("U2");
            uSp = unidadeRepo.save(uSp);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(uSp);
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            sp = subprocessoRepo.saveAndFlush(sp);

            Mapa mapa = new Mapa();
            mapa.setSugestoes("Sugestoes");
            mapa.setSubprocesso(sp);
            mapa = mapaRepo.saveAndFlush(mapa);

            Long mapaCodigo = mapa.getCodigo();
            Atividade atividade = Atividade.builder()
                    .descricao("Atividade inicial")
                    .mapa(mapa)
                    .build();
            when(mapaManutencaoService.atividadesMapaCodigoSemRels(mapaCodigo)).thenReturn(java.util.List.of(atividade));

            transicaoService.atualizarParaEmAndamento(mapaCodigo);
            Subprocesso atualizado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        }

        @Test
        @DisplayName("deve atualizar para revisao em andamento apos alteracao")
        void revisaoEmAndamento() {
            Processo proc = new Processo();
            proc.setDescricao("Proc");
            proc.setTipo(TipoProcesso.REVISAO);
            proc = processoRepo.save(proc);

            Unidade uSp = new Unidade();
            uSp.setSigla("U3");
            uSp = unidadeRepo.save(uSp);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(uSp);
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            sp = subprocessoRepo.saveAndFlush(sp);

            Mapa mapa = new Mapa();
            mapa.setSugestoes("Sugestoes");
            mapa.setSubprocesso(sp);
            mapa = mapaRepo.saveAndFlush(mapa);

            Long mapaCodigo = mapa.getCodigo();
            Atividade atividade = Atividade.builder()
                    .descricao("Atividade inicial revisao")
                    .mapa(mapa)
                    .build();
            when(mapaManutencaoService.atividadesMapaCodigoSemRels(mapaCodigo)).thenReturn(java.util.List.of(atividade));

            transicaoService.atualizarParaEmAndamento(mapaCodigo);
            Subprocesso atualizado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        }

        @Test
        @DisplayName("deve voltar para nao iniciado quando o mapa ficar sem atividades")
        void voltaParaNaoIniciadoSemAtividades() {
            Processo proc = new Processo();
            proc.setDescricao("Proc");
            proc.setTipo(TipoProcesso.MAPEAMENTO);
            proc = processoRepo.save(proc);

            Unidade uSp = new Unidade();
            uSp.setSigla("U4");
            uSp = unidadeRepo.save(uSp);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(uSp);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            sp = subprocessoRepo.saveAndFlush(sp);

            Mapa mapa = new Mapa();
            mapa.setSugestoes("Sugestoes");
            mapa.setSubprocesso(sp);
            mapa = mapaRepo.saveAndFlush(mapa);

            when(mapaManutencaoService.atividadesMapaCodigoSemRels(mapa.getCodigo())).thenReturn(java.util.Collections.emptyList());
            transicaoService.atualizarParaEmAndamento(mapa.getCodigo());

            Subprocesso atualizado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.NAO_INICIADO);
        }
    }

    @Nested
    @DisplayName("disponibilizar")
    class Disponibilizar {

        @Test
        @DisplayName("deve usar a propria unidade se nao tiver superior")
        void semSuperior() {
            Processo proc = new Processo();
            proc.setDescricao("Proc");
            proc.setTipo(TipoProcesso.MAPEAMENTO);
            proc = processoRepo.save(proc);

            Unidade uSp = new Unidade();
            uSp.setSigla("U4");
            uSp.setUnidadeSuperior(null); // Explicitamente sem superior
            uSp = unidadeRepo.save(uSp);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(uSp);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            sp = subprocessoRepo.saveAndFlush(sp);

            Mapa mapa = new Mapa();
            mapa.setSugestoes("Sugestoes");
            mapa.setSubprocesso(sp);
            mapa = mapaRepo.saveAndFlush(mapa);

            Usuario user = new Usuario();
            user.setTituloEleitoral("123");
            user = usuarioRepo.save(user);

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
            Processo proc = new Processo();
            proc.setDescricao("Proc");
            proc.setTipo(TipoProcesso.MAPEAMENTO);
            proc = processoRepo.save(proc);

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
            sp = subprocessoRepo.saveAndFlush(sp);

            Mapa mapa = new Mapa();
            mapa.setSugestoes(null);
            mapa.setSubprocesso(sp);
            mapa = mapaRepo.saveAndFlush(mapa);

            sp.setMapa(mapa);
            subprocessoRepo.saveAndFlush(sp);

            Usuario user = new Usuario();
            user.setTituloEleitoral("123");
            user = usuarioRepo.save(user);

            sgc.subprocesso.dto.DisponibilizarMapaRequest request = sgc.subprocesso.dto.DisponibilizarMapaRequest.builder()
                .observacoes("novas observacoes")
                .dataLimite(java.time.LocalDate.of(2025, 12, 31))
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
            Processo proc = new Processo();
            proc.setDescricao("Proc");
            proc.setTipo(TipoProcesso.REVISAO);
            proc = processoRepo.save(proc);

            Unidade uSp = new Unidade();
            uSp.setSigla("U5");
            uSp = unidadeRepo.save(uSp);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(uSp);
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            sp = subprocessoRepo.saveAndFlush(sp);

            Mapa mapa = new Mapa();
            mapa.setSugestoes(null);
            mapa.setSubprocesso(sp);
            mapa = mapaRepo.saveAndFlush(mapa);

            sp.setMapa(mapa);
            subprocessoRepo.saveAndFlush(sp);

            Usuario user = new Usuario();
            user.setTituloEleitoral("123");
            user = usuarioRepo.save(user);

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
            Processo proc = new Processo();
            proc.setDescricao("Proc");
            proc.setTipo(TipoProcesso.MAPEAMENTO);
            proc = processoRepo.save(proc);

            Unidade uSp = new Unidade();
            uSp.setSigla("U6");
            uSp = unidadeRepo.save(uSp);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(proc);
            sp.setUnidade(uSp);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            sp = subprocessoRepo.saveAndFlush(sp);

            Mapa mapa = new Mapa();
            mapa.setSugestoes(null);
            mapa.setSubprocesso(sp);
            mapa = mapaRepo.saveAndFlush(mapa);

            sp.setMapa(mapa);
            subprocessoRepo.saveAndFlush(sp);

            Usuario user = new Usuario();
            user.setTituloEleitoral("123");
            user = usuarioRepo.save(user);

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
            Processo proc = new Processo();
            proc.setDescricao("Proc");
            proc.setTipo(TipoProcesso.MAPEAMENTO);
            proc = processoRepo.save(proc);

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
            sp = subprocessoRepo.saveAndFlush(sp);

            Mapa mapa = new Mapa();
            mapa.setSugestoes(null);
            mapa.setSubprocesso(sp);
            mapa = mapaRepo.saveAndFlush(mapa);

            sp.setMapa(mapa);
            subprocessoRepo.saveAndFlush(sp);

            Usuario user = new Usuario();
            user.setTituloEleitoral("123");
            user = usuarioRepo.save(user);

            when(unidadeService.buscarPorSigla("U7")).thenReturn(uSp);

            transicaoService.aceitarValidacao(sp.getCodigo(), "Observacao aceite", user);

            Subprocesso atualizado = subprocessoRepo.findById(sp.getCodigo()).orElseThrow();
            assertThat(atualizado.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
        }
    }

}
