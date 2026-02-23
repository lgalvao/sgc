package sgc.subprocesso.service.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaFacade;
import sgc.analise.AnaliseFacade;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.MapaFacade;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.AccessControlService;
import sgc.subprocesso.dto.CompetenciaRequest;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoRequest;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("SubprocessoMapaWorkflowService")
class SubprocessoMapaWorkflowServiceTest {
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private MapaFacade mapaFacade;
    @Mock
    private SubprocessoTransicaoService transicaoService;
    @Mock
    private AnaliseFacade analiseFacade;
    @Mock
    private OrganizacaoFacade unidadeService;
    @Mock
    private SubprocessoValidacaoService validacaoService;

    @Mock
    private AccessControlService accessControlService;
    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private MovimentacaoRepo repositorioMovimentacao;
    @Mock
    private ImpactoMapaService impactoMapaService;

    @InjectMocks
    private SubprocessoMapaWorkflowService service;

    @Test
    @DisplayName("Deve falhar ao buscar subprocesso inexistente")
    void deveFalharSubprocessoInexistente() {
        when(crudService.buscarSubprocesso(999L))
                .thenThrow(new ErroEntidadeNaoEncontrada("Subprocesso", 999L));
        CompetenciaRequest request = CompetenciaRequest.builder().build();
        assertThatThrownBy(() -> service.adicionarCompetencia(999L, request))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    private Subprocesso mockSubprocesso(Long codigo) {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        return sp;
    }

    private Subprocesso mockSubprocesso(Long codigo, SituacaoSubprocesso situacao) {
        Subprocesso sp = mockSubprocesso(codigo);
        sp.setSituacao(situacao);
        return sp;
    }


    @Nested
    @DisplayName("Edição de Mapa")
    class EdicaoMapa {
        @Test
        @DisplayName("Deve salvar mapa com sucesso e alterar situação se novo")
        void deveSalvarMapa() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);

            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .competencias(List.of(SalvarMapaRequest.CompetenciaRequest.builder().build()))
                    .build();

            service.salvarMapaSubprocesso(1L, req);

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            verify(subprocessoRepo).save(sp);
            verify(mapaFacade).salvarMapaCompleto(10L, req);
        }

        @Test
        @DisplayName("Deve mudar para REVISAO_MAPA_AJUSTADO ao salvar se era vazio e REVISAO_CADASTRO_HOMOLOGADA")
        void deveMudarParaAjustadoRevisaoAoSalvar() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .competencias(List.of(SalvarMapaRequest.CompetenciaRequest.builder().build()))
                    .build();

            service.salvarMapaSubprocesso(1L, req);

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            verify(subprocessoRepo).save(sp);
        }

        @Test
        @DisplayName("Não deve alterar situação ao salvar mapa se não era vazio")
        void deveManterSituacaoSeNaoVazioAoSalvar() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(new Competencia()));

            SalvarMapaRequest req = SalvarMapaRequest.builder().competencias(List.of()).build();

            service.salvarMapaSubprocesso(1L, req);

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            verify(subprocessoRepo, never()).save(any());
        }

        @Test
        @DisplayName("Não deve alterar situação ao salvar mapa se era vazio mas não tem novas competências")
        void deveManterSituacaoSeVazioMasSemNovasAoSalvar() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            SalvarMapaRequest req = SalvarMapaRequest.builder().competencias(List.of()).build();

            service.salvarMapaSubprocesso(1L, req);

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            verify(subprocessoRepo, never()).save(any());
        }

        @Test
        @DisplayName("Não deve alterar situação ao salvar mapa se não era vazio mesmo com novas competências")
        void deveManterSituacaoSeNaoVazioMasComNovasAoSalvar() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(new Competencia()));

            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .competencias(List.of(SalvarMapaRequest.CompetenciaRequest.builder().build()))
                    .build();

            service.salvarMapaSubprocesso(1L, req);

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            verify(subprocessoRepo, never()).save(any());
        }

        @Test
        @DisplayName("Deve adicionar competência e mudar status para MAPEAMENTO_MAPA_CRIADO")
        void deveAdicionarCompMudarStatusMapeamento() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            CompetenciaRequest req = CompetenciaRequest.builder().descricao("D").atividadesIds(List.of(1L)).build();
            service.adicionarCompetencia(1L, req);

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            verify(subprocessoRepo).save(sp);
        }

        @Test
        @DisplayName("Deve mudar para REVISAO_MAPA_AJUSTADO ao adicionar competência se era vazio e REVISAO_CADASTRO_HOMOLOGADA")
        void deveMudarParaAjustadoAoAdicionarCompRevisao() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            CompetenciaRequest req = CompetenciaRequest.builder().descricao("D").atividadesIds(List.of(1L)).build();
            service.adicionarCompetencia(1L, req);

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            verify(subprocessoRepo).save(sp);
        }

        @Test
        @DisplayName("Não deve alterar situação ao adicionar competência se já não era vazio")
        void deveManterSituacaoSeNaoVazioAoAdicionar() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(new Competencia()));
            when(mapaFacade.mapaPorCodigo(10L)).thenReturn(mapa);

            CompetenciaRequest req = CompetenciaRequest.builder().descricao("D").build();
            service.adicionarCompetencia(1L, req);

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            verify(subprocessoRepo, never()).save(any());
        }

        @Test
        @DisplayName("Deve mudar para MAPEAMENTO_CADASTRO_HOMOLOGADO ao remover se ficou vazio e MAPEAMENTO_MAPA_CRIADO")
        void deveVoltarCadastroHomologadoAoRemover() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());
            when(mapaFacade.mapaPorCodigo(10L)).thenReturn(mapa);

            service.removerCompetencia(1L, 5L);

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            verify(subprocessoRepo).save(sp);
        }

        @Test
        @DisplayName("Não deve mudar situação ao remover competência se não ficou vazio")
        void deveManterSituacaoSeNaoFicouVazioAoRemover() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(new Competencia()));
            when(mapaFacade.mapaPorCodigo(10L)).thenReturn(mapa);

            service.removerCompetencia(1L, 5L);

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            verify(subprocessoRepo, never()).save(any());
        }

        @Test
        @DisplayName("Deve mudar para REVISAO_CADASTRO_HOMOLOGADA ao remover se ficou vazio e REVISAO_MAPA_AJUSTADO")
        void deveVoltarRevisaoHomologadaAoRemover() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());
            when(mapaFacade.mapaPorCodigo(10L)).thenReturn(mapa);

            service.removerCompetencia(1L, 5L);

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            verify(subprocessoRepo).save(sp);
        }

        @Test
        @DisplayName("Deve atualizar competência com sucesso")
        void deveAtualizarCompetencia() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);

            CompetenciaRequest req = CompetenciaRequest.builder().descricao("U").atividadesIds(List.of(1L)).build();
            service.atualizarCompetencia(1L, 50L, req);

            verify(mapaManutencaoService).atualizarCompetencia(50L, "U", List.of(1L));
        }
    }

    @Nested
    @DisplayName("Disponibilização de Mapa")
    class Disponibilizacao {
        @Test
        @DisplayName("Deve disponibilizar mapa com sucesso")
        void deveDisponibilizarMapa() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            sp.setProcesso(p);

            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);

            Competencia c = new Competencia();
            Atividade a = new Atividade();
            a.setCodigo(100L);
            c.setAtividades(new HashSet<>(List.of(a)));
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(10L)).thenReturn(List.of(a));

            Unidade u = new Unidade();
            u.setSigla("U1");
            sp.setUnidade(u);

            Unidade sedoc = new Unidade();
            sedoc.setCodigo(99L);
            sedoc.setSituacao(SituacaoUnidade.ATIVA);
            when(unidadeService.buscarEntidadePorSigla("ADMIN")).thenReturn(sedoc);

            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder()
                    .dataLimite(LocalDate.now())
                    .observacoes("Obs")
                    .build();

            service.disponibilizarMapa(1L, req, new Usuario());

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
            verify(subprocessoRepo).save(sp);
            verify(transicaoService).registrar(argThat(cmd ->
                    cmd.sp().equals(sp) &&
                            cmd.tipo() == TipoTransicao.MAPA_DISPONIBILIZADO &&
                            "Obs".equals(cmd.observacoes())
            ));
        }

        @Test
        @DisplayName("Deve falhar ao disponibilizar se atividade não associada")
        void deveFalharAtividadeNaoAssociada() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);

            Competencia c = new Competencia();
            Atividade a1 = new Atividade(); a1.setCodigo(1L); a1.setDescricao("A1");
            c.setAtividades(new HashSet<>(List.of(a1)));
            
            Atividade a2 = new Atividade(); a2.setCodigo(2L); a2.setDescricao("A2");
            
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(10L)).thenReturn(List.of(a1, a2));

            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder().dataLimite(LocalDate.now()).build();
            
            assertThatThrownBy(() -> service.disponibilizarMapa(1L, req, new Usuario()))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("A2");
        }

        @Test
        @DisplayName("Deve falhar ao disponibilizar se competência sem atividade")
        void deveFalharCompetenciaSemAtividade() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);

            Competencia c = new Competencia();
            c.setAtividades(new HashSet<>());
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));

            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder().dataLimite(LocalDate.now()).build();
            
            assertThatThrownBy(() -> service.disponibilizarMapa(1L, req, new Usuario()))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("pelo menos uma atividade");
        }
    }

    @Nested
    @DisplayName("Workflow de Validação")
    class WorkflowValidacao {
        @Test
        @DisplayName("Deve aceitar validação e transitar se não é topo")
        void deveAceitarETransitarSeNaoTopo() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            sp.setProcesso(p);

            Unidade u = new Unidade();
            sp.setUnidade(u);

            Unidade superior = new Unidade();
            Unidade proxima = new Unidade();
            u.setUnidadeSuperior(superior);
            superior.setUnidadeSuperior(proxima);

            service.aceitarValidacao(1L, new Usuario());

            verify(transicaoService).registrarAnaliseETransicao(argThat(req -> req.sp().equals(sp) &&
                    req.novaSituacao() == SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO &&
                    req.tipoTransicao() == TipoTransicao.MAPA_VALIDACAO_ACEITA));
        }

        @Test
        @DisplayName("Deve devolver validação quando unidade superior é null")
        void deveDevolverValidacaoSuperiorNull() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            sp.setProcesso(p);

            Unidade u = new Unidade();
            sp.setUnidade(u);
            u.setUnidadeSuperior(null);

            service.devolverValidacao(1L, "J", new Usuario());

            verify(transicaoService).registrarAnaliseETransicao(argThat(req -> req.unidadeDestinoTransicao().equals(u) &&
                    req.novaSituacao() == SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO));
        }

        @Test
        @DisplayName("Deve apresentar sugestões quando unidade superior é null")
        void deveApresentarSugestoesSuperiorNull() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            sp.setProcesso(p);

            Mapa mapa = new Mapa();
            sp.setMapa(mapa);

            Unidade u = new Unidade();
            sp.setUnidade(u);
            u.setUnidadeSuperior(null);

            service.apresentarSugestoes(1L, "S", new Usuario());

            verify(transicaoService).registrar(argThat(cmd -> cmd.destino().equals(u)));
        }

        @Test
        @DisplayName("Deve validar mapa quando unidade superior é null")
        void deveValidarMapaSuperiorNull() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            sp.setProcesso(p);

            Unidade u = new Unidade();
            sp.setUnidade(u);
            u.setUnidadeSuperior(null);

            service.validarMapa(1L, new Usuario());

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            verify(transicaoService).registrar(argThat(cmd -> cmd.destino().equals(u)));
        }

        @Test
        @DisplayName("Deve aceitar validação e homologar se não tem proxima unidade")
        void deveAceitarEHomologarSeNaoProxima() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            sp.setProcesso(p);

            Unidade u = new Unidade();
            u.setSigla("U1");
            sp.setUnidade(u);

            Unidade superior = new Unidade();
            superior.setSigla("SUP");
            u.setUnidadeSuperior(superior);
            superior.setUnidadeSuperior(null);

            // Simula que o processo já está na unidade superior (que não tem próxima)
            Movimentacao mov = Movimentacao.builder().unidadeDestino(superior).build();
            when(repositorioMovimentacao.findFirstBySubprocessoCodigoOrderByDataHoraDesc(1L))
                    .thenReturn(Optional.of(mov));

            Usuario user = new Usuario();
            user.setTituloEleitoral("123");

            service.aceitarValidacao(1L, user);

            verify(analiseFacade).criarAnalise(eq(sp), any());
            verify(subprocessoRepo).save(sp);
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        }

        @Test
        @DisplayName("Deve homologar mapa com sucesso")
        void deveHomologarMapa() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            sp.setProcesso(p);

            Unidade admin = new Unidade();
            admin.setSigla("ADMIN");
            when(unidadeService.buscarEntidadePorSigla("ADMIN")).thenReturn(admin);

            service.homologarValidacao(1L, new Usuario());

            verify(subprocessoRepo).save(sp);
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
            verify(transicaoService).registrar(argThat(cmd -> cmd.tipo() == TipoTransicao.MAPA_HOMOLOGADO));
        }
    }

    @Nested
    @DisplayName("Workflow de Validação - Casos REVISAO")
    class WorkflowValidacaoRevisao {
        @Test
        @DisplayName("Deve validar mapa REVISAO")
        void deveValidarMapaRevisao() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.REVISAO);
            sp.setProcesso(p);

            Unidade u = new Unidade();
            sp.setUnidade(u);
            u.setUnidadeSuperior(null);

            service.validarMapa(1L, new Usuario());

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_VALIDADO);
        }

        @Test
        @DisplayName("Deve devolver validação REVISAO")
        void deveDevolverValidacaoRevisao() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_VALIDADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.REVISAO);
            sp.setProcesso(p);

            Unidade u = new Unidade();
            sp.setUnidade(u);
            u.setUnidadeSuperior(null);

            service.devolverValidacao(1L, "J", new Usuario());

            verify(transicaoService).registrarAnaliseETransicao(argThat(req -> req.sp().equals(sp) &&
                    req.novaSituacao() == SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO));
        }

        @Test
        @DisplayName("Deve aceitar validação e homologar REVISAO se não tem proxima unidade")
        void deveAceitarEHomologarRevisaoSeNaoProxima() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_VALIDADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.REVISAO);
            sp.setProcesso(p);

            Unidade u = new Unidade();
            u.setSigla("U1");
            sp.setUnidade(u);
            u.setUnidadeSuperior(null); 

            service.aceitarValidacao(1L, new Usuario());

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO);
        }
    }

    @Nested
    @DisplayName("Submeter Mapa Ajustado")
    class SubmeterAjuste {
        @Test
        @DisplayName("Deve submeter mapa ajustado sem data limite")
        void deveSubmeterAjustadoSemDataLimite() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.REVISAO);
            sp.setProcesso(p);

            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);

            Unidade u = new Unidade();
            sp.setUnidade(u);

            SubmeterMapaAjustadoRequest req = SubmeterMapaAjustadoRequest.builder()
                    .justificativa("J")
                    .dataLimiteEtapa2(null)
                    .build();

            service.submeterMapaAjustado(1L, req, new Usuario());

            assertThat(sp.getDataLimiteEtapa2()).isNull();
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);
        }

        @Test
        @DisplayName("Deve submeter mapa ajustado com data limite")
        void deveSubmeterAjustadoComDataLimite() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.REVISAO);
            sp.setProcesso(p);

            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);

            Unidade u = new Unidade();
            sp.setUnidade(u);

            LocalDateTime dataLimite = LocalDateTime.now().plusDays(5);
            SubmeterMapaAjustadoRequest req = SubmeterMapaAjustadoRequest.builder()
                    .justificativa("J")
                    .dataLimiteEtapa2(dataLimite)
                    .build();

            service.submeterMapaAjustado(1L, req, new Usuario());

            assertThat(sp.getDataLimiteEtapa2()).isEqualTo(dataLimite);
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);
        }
    }

    @Nested
    @DisplayName("Operações em Bloco")
    class OperacoesEmBloco {
        @Test
        @DisplayName("Deve disponibilizar mapa em bloco")
        void deveDisponibilizarMapaEmBloco() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            sp.setProcesso(p);
            
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);
            
            Unidade u = new Unidade();
            sp.setUnidade(u);
            
            when(unidadeService.buscarEntidadePorSigla("ADMIN")).thenReturn(new Unidade());

            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder().dataLimite(LocalDate.now()).build();
            service.disponibilizarMapaEmBloco(List.of(1L), req, new Usuario());

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
            verify(subprocessoRepo).save(sp);
        }

        @Test
        @DisplayName("Deve aceitar validação em bloco")
        void deveAceitarValidacaoEmBloco() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            sp.setProcesso(p);
            Unidade u = new Unidade();
            sp.setUnidade(u);
            
            Unidade sup = new Unidade();
            u.setUnidadeSuperior(sup);
            sup.setUnidadeSuperior(new Unidade()); 

            service.aceitarValidacaoEmBloco(List.of(1L), new Usuario());

            verify(transicaoService).registrarAnaliseETransicao(any());
        }

        @Test
        @DisplayName("Deve homologar validação em bloco")
        void deveHomologarValidacaoEmBloco() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            sp.setProcesso(p);
            when(unidadeService.buscarEntidadePorSigla("ADMIN")).thenReturn(new Unidade());

            service.homologarValidacaoEmBloco(List.of(1L), new Usuario());

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
            verify(subprocessoRepo).save(sp);
        }
    }
}
