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
import sgc.analise.model.TipoAcaoAnalise;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaFacade;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.CompetenciaRequest;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoRequest;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private UnidadeFacade unidadeService;
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
        Subprocesso sp = mock(Subprocesso.class);
        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        return sp;
    }


    @Nested
    @DisplayName("Edição de Mapa")
    class EdicaoMapa {
        @Test
        @DisplayName("Deve salvar mapa com sucesso e alterar situação se novo")
        void deveSalvarMapa() {
            Subprocesso sp = mockSubprocesso(1L);
            when(sp.getSituacao()).thenReturn(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            doCallRealMethod().when(sp).setSituacao(any());

            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .competencias(List.of(SalvarMapaRequest.CompetenciaRequest.builder().build()))
                    .build();

            service.salvarMapaSubprocesso(1L, req);

            verify(subprocessoRepo).save(sp);
            verify(mapaFacade).salvarMapaCompleto(10L, req);
        }

        @Test
        @DisplayName("Deve mudar para REVISAO_MAPA_AJUSTADO ao salvar se era vazio e REVISAO_CADASTRO_HOMOLOGADA")
        void deveMudarParaAjustadoRevisaoAoSalvar() {
            Subprocesso sp = mockSubprocesso(1L);
            when(sp.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            doCallRealMethod().when(sp).setSituacao(any());

            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .competencias(List.of(SalvarMapaRequest.CompetenciaRequest.builder().build()))
                    .build();

            service.salvarMapaSubprocesso(1L, req);

            verify(sp).setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            verify(subprocessoRepo).save(sp);
        }

        @Test
        @DisplayName("Deve mudar para MAPEAMENTO_MAPA_CRIADO ao adicionar competência se era vazio e MAPEAMENTO_CADASTRO_HOMOLOGADO")
        void deveMudarParaCriadoAoAdicionarComp() {
            Subprocesso sp = mockSubprocesso(1L);
            when(sp.getSituacao()).thenReturn(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            doCallRealMethod().when(sp).setSituacao(any());

            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            CompetenciaRequest req = CompetenciaRequest.builder().descricao("D").atividadesIds(List.of(1L)).build();
            service.adicionarCompetencia(1L, req);

            verify(sp).setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            verify(subprocessoRepo).save(sp);
        }

        @Test
        @DisplayName("Deve mudar para MAPEAMENTO_CADASTRO_HOMOLOGADO ao remover se ficou vazio e MAPEAMENTO_MAPA_CRIADO")
        void deveVoltarCadastroHomologadoAoRemover() {
            Subprocesso sp = mockSubprocesso(1L);
            when(sp.getSituacao()).thenReturn(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            doCallRealMethod().when(sp).setSituacao(any());

            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());

            service.removerCompetencia(1L, 5L);

            verify(sp).setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            verify(subprocessoRepo).save(sp);
        }

        @Test
        @DisplayName("Deve atualizar competência com sucesso")
        void deveAtualizarCompetencia() {
            Subprocesso sp = mockSubprocesso(1L);
            when(sp.getSituacao()).thenReturn(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            CompetenciaRequest req = CompetenciaRequest.builder().descricao("U").atividadesIds(List.of(1L)).build();
            service.atualizarCompetencia(1L, 50L, req);

            verify(mapaManutencaoService).atualizarCompetencia(50L, "U", List.of(1L));
        }
    }

    @Nested
    @DisplayName("Disponibilização de Mapa")
    class Disponibilizacao {
        @Test
        @DisplayName("Deve falhar ao disponibilizar se competência sem atividade")
        void deveFalharCompetenciaSemAtividade() {
            Subprocesso sp = mockSubprocesso(1L);
            when(sp.getSituacao()).thenReturn(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            Competencia c = new Competencia();
            c.setAtividades(new HashSet<>()); // Vazia
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));

            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder().dataLimite(LocalDate.now()).build();
            
            assertThatThrownBy(() -> service.disponibilizarMapa(1L, req, new Usuario()))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("pelo menos uma atividade");
        }
        @Test
        @DisplayName("Deve disponibilizar mapa com sucesso")
        void deveDisponibilizarMapa() {
            Subprocesso sp = mockSubprocesso(1L);
            when(sp.getSituacao()).thenReturn(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            doCallRealMethod().when(sp).setSituacao(any());

            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            when(sp.getProcesso()).thenReturn(p);

            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            Competencia c = new Competencia();
            Atividade a = new Atividade();
            a.setCodigo(100L);
            c.setAtividades(new HashSet<>(List.of(a)));
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(10L)).thenReturn(List.of(a));

            Unidade u = mock(Unidade.class);
            when(sp.getUnidade()).thenReturn(u);

            Unidade sedoc = new Unidade();
            sedoc.setCodigo(99L);
            sedoc.setSituacao(SituacaoUnidade.ATIVA);
            when(unidadeService.buscarEntidadePorSigla("ADMIN")).thenReturn(sedoc);

            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder()
                    .dataLimite(LocalDate.now())
                    .observacoes("Obs")
                    .build();

            service.disponibilizarMapa(1L, req, new Usuario());

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
            Subprocesso sp = mockSubprocesso(1L);
            when(sp.getSituacao()).thenReturn(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);

            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

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
    }

    @Nested
    @DisplayName("Workflow de Validação")
    class WorkflowValidacao {
        @Test
        @DisplayName("Deve aceitar validação e transitar se não é topo")
        void deveAceitarETransitarSeNaoTopo() {
            Subprocesso sp = mockSubprocesso(1L);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            when(sp.getProcesso()).thenReturn(p);

            Unidade u = mock(Unidade.class);
            when(sp.getUnidade()).thenReturn(u);

            Unidade superior = mock(Unidade.class);
            Unidade proxima = mock(Unidade.class);
            when(u.getUnidadeSuperior()).thenReturn(superior);
            when(superior.getUnidadeSuperior()).thenReturn(proxima);

            service.aceitarValidacao(1L, new Usuario());

            verify(transicaoService).registrarAnaliseETransicao(argThat(req -> req.sp().equals(sp) &&
                    req.novaSituacao() == SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO &&
                    req.tipoTransicao() == TipoTransicao.MAPA_VALIDACAO_ACEITA));
        }

        @Test
        @DisplayName("Deve devolver validação quando unidade superior é null")
        void deveDevolverValidacaoSuperiorNull() {
            Subprocesso sp = mockSubprocesso(1L);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            when(sp.getProcesso()).thenReturn(p);

            Unidade u = mock(Unidade.class);
            when(sp.getUnidade()).thenReturn(u);
            when(u.getUnidadeSuperior()).thenReturn(null);

            service.devolverValidacao(1L, "J", new Usuario());

            verify(transicaoService).registrarAnaliseETransicao(argThat(req -> req.unidadeDestinoTransicao().equals(u)));
        }

        @Test
        @DisplayName("Deve apresentar sugestões quando unidade superior é null")
        void deveApresentarSugestoesSuperiorNull() {
            Subprocesso sp = mockSubprocesso(1L);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            when(sp.getProcesso()).thenReturn(p);

            Mapa mapa = mock(Mapa.class);
            when(sp.getMapa()).thenReturn(mapa);

            Unidade u = mock(Unidade.class);
            when(sp.getUnidade()).thenReturn(u);
            when(u.getUnidadeSuperior()).thenReturn(null);

            service.apresentarSugestoes(1L, "S", new Usuario());

            verify(transicaoService).registrar(argThat(cmd -> cmd.destino().equals(u)));
        }

        @Test
        @DisplayName("Deve aceitar validação e homologar se não tem proxima unidade")
        void deveAceitarEHomologarSeNaoProxima() {
            Subprocesso sp = mockSubprocesso(1L);
            when(sp.getSituacao()).thenCallRealMethod();
            doCallRealMethod().when(sp).setSituacao(any());

            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            when(sp.getProcesso()).thenReturn(p);

            Unidade u = mock(Unidade.class);
            when(u.getSigla()).thenReturn("U1");
            when(sp.getUnidade()).thenReturn(u);

            Unidade superior = mock(Unidade.class);
            when(superior.getSigla()).thenReturn("SUP");
            when(u.getUnidadeSuperior()).thenReturn(superior);
            when(superior.getUnidadeSuperior()).thenReturn(null); // Fim da cadeia

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
            Subprocesso sp = mockSubprocesso(1L);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            when(sp.getProcesso()).thenReturn(p);

            Unidade admin = new Unidade();
            admin.setSigla("ADMIN");
            when(unidadeService.buscarEntidadePorSigla("ADMIN")).thenReturn(admin);

            service.homologarValidacao(1L, new Usuario());

            verify(subprocessoRepo).save(sp);
            verify(transicaoService).registrar(argThat(cmd -> cmd.tipo() == TipoTransicao.MAPA_HOMOLOGADO));
        }

        @Test
        @DisplayName("Deve submeter mapa ajustado com data limite")
        void deveSubmeterAjustadoComDataLimite() {
            Subprocesso sp = mockSubprocesso(1L);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.REVISAO);
            when(sp.getProcesso()).thenReturn(p);
            doCallRealMethod().when(sp).setSituacao(any());

            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sp.getMapa()).thenReturn(mapa);

            Unidade u = mock(Unidade.class);
            when(sp.getUnidade()).thenReturn(u);

            LocalDateTime dataLimite = LocalDateTime.now().plusDays(5);
            SubmeterMapaAjustadoRequest req = SubmeterMapaAjustadoRequest.builder()
                    .justificativa("J")
                    .dataLimiteEtapa2(dataLimite)
                    .build();

            service.submeterMapaAjustado(1L, req, new Usuario());

            verify(sp).setDataLimiteEtapa2(dataLimite);
            verify(sp).setSituacao(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO);
        }
    }

    @Nested
    @DisplayName("Operações em Bloco")
    class OperacoesEmBloco {
        @Test
        @DisplayName("Deve disponibilizar mapa em bloco")
        void deveDisponibilizarMapaEmBloco() {
            Subprocesso sp = mockSubprocesso(1L);
            when(sp.getSituacao()).thenReturn(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            when(sp.getProcesso()).thenReturn(p);
            when(sp.getMapa()).thenReturn(mock(Mapa.class));
            when(sp.getUnidade()).thenReturn(mock(Unidade.class));
            when(unidadeService.buscarEntidadePorSigla("ADMIN")).thenReturn(new Unidade());

            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder().dataLimite(LocalDate.now()).build();
            service.disponibilizarMapaEmBloco(List.of(1L), req, new Usuario());

            verify(subprocessoRepo).save(sp);
        }

        @Test
        @DisplayName("Deve aceitar validação em bloco")
        void deveAceitarValidacaoEmBloco() {
            Subprocesso sp = mockSubprocesso(1L);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            when(sp.getProcesso()).thenReturn(p);
            Unidade u = mock(Unidade.class);
            when(sp.getUnidade()).thenReturn(u);
            
            Unidade sup = mock(Unidade.class);
            when(u.getUnidadeSuperior()).thenReturn(sup);
            when(sup.getUnidadeSuperior()).thenReturn(mock(Unidade.class)); // Garante proximaUnidade != null

            service.aceitarValidacaoEmBloco(List.of(1L), new Usuario());

            verify(transicaoService).registrarAnaliseETransicao(any());
        }

        @Test
        @DisplayName("Deve homologar validação em bloco")
        void deveHomologarValidacaoEmBloco() {
            Subprocesso sp = mockSubprocesso(1L);
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);
            when(sp.getProcesso()).thenReturn(p);
            when(unidadeService.buscarEntidadePorSigla("ADMIN")).thenReturn(new Unidade());

            service.homologarValidacaoEmBloco(List.of(1L), new Usuario());

            verify(subprocessoRepo).save(sp);
        }
    }
}


