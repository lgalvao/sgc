package sgc.subprocesso.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaFacade;
import sgc.alerta.EmailService;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.model.ComumRepo;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.CopiaMapaService;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.mapa.service.MapaSalvamentoService;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.dto.CompetenciaRequest;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.MapaAjusteMapper;
import sgc.subprocesso.dto.SubmeterMapaAjustadoRequest;
import sgc.subprocesso.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoService - Mapa Workflow")
class SubprocessoServiceMapaTest {
    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private MovimentacaoRepo movimentacaoRepo;
    @Mock private ComumRepo repo;
    @Mock private AnaliseRepo analiseRepo;
    @Mock private AlertaFacade alertaService;
    @Mock private OrganizacaoFacade organizacaoFacade;
    @Mock private UsuarioFacade usuarioFacade;
    @Mock private ImpactoMapaService impactoMapaService;
    @Mock private CopiaMapaService copiaMapaService;
    @Mock private EmailService emailService;
    @Mock private MapaManutencaoService mapaManutencaoService;
    @Mock private MapaSalvamentoService mapaSalvamentoService;
    @Mock private MapaAjusteMapper mapaAjusteMapper;
    @Mock private SgcPermissionEvaluator permissionEvaluator;

    @InjectMocks
    private SubprocessoService service;

    @BeforeEach
    void setup() {
        service.setMapaManutencaoService(mapaManutencaoService);
        service.setSubprocessoRepo(subprocessoRepo);
        service.setMovimentacaoRepo(movimentacaoRepo);
        lenient().when(organizacaoFacade.buscarPorSigla(any())).thenReturn(new UnidadeDto());
    }

    @Test
    @DisplayName("Deve falhar ao buscar subprocesso inexistente")
    void deveFalharSubprocessoInexistente() {
        when(subprocessoRepo.findByIdWithMapaAndAtividades(999L)).thenReturn(Optional.empty());
        CompetenciaRequest request = CompetenciaRequest.builder().build();
        assertThatThrownBy(() -> service.adicionarCompetencia(999L, request))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    private Subprocesso mockSubprocesso(Long codigo) {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        when(subprocessoRepo.findByIdWithMapaAndAtividades(codigo)).thenReturn(Optional.of(sp));
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
            when(mapaSalvamentoService.salvarMapaCompleto(eq(10L), any())).thenReturn(mapa);

            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .competencias(List.of(SalvarMapaRequest.CompetenciaRequest.builder().build()))
                    .build();

            service.salvarMapaSubprocesso(1L, req);

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            verify(subprocessoRepo).save(sp);
            verify(mapaSalvamentoService).salvarMapaCompleto(10L, req);
        }

        @Test
        @DisplayName("Deve mudar para REVISAO_MAPA_AJUSTADO ao salvar se era vazio e REVISAO_CADASTRO_HOMOLOGADA")
        void deveMudarParaAjustadoRevisaoAoSalvar() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());
            when(mapaSalvamentoService.salvarMapaCompleto(eq(10L), any())).thenReturn(mapa);

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
            when(mapaSalvamentoService.salvarMapaCompleto(eq(10L), any())).thenReturn(mapa);

            SalvarMapaRequest req = SalvarMapaRequest.builder().competencias(List.of()).build();

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
        @DisplayName("Deve mudar para MAPEAMENTO_CADASTRO_HOMOLOGADO ao remover se ficou vazio e MAPEAMENTO_MAPA_CRIADO")
        void deveVoltarCadastroHomologadoAoRemover() {
            Subprocesso sp = mockSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            Mapa mapa = new Mapa();
            mapa.setCodigo(10L);
            sp.setMapa(mapa);
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of());
            when(mapaManutencaoService.buscarMapaPorCodigo(10L)).thenReturn(mapa);

            service.removerCompetencia(1L, 5L);

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
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
            a.setCompetencias(Set.of(c));
            c.setAtividades(new HashSet<>(List.of(a)));
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(10L)).thenReturn(List.of(a));

            Unidade u = new Unidade();
            u.setSigla("U1");
            sp.setUnidade(u);

            Unidade sedoc = new Unidade();
            sedoc.setCodigo(99L);
            sedoc.setSituacao(SituacaoUnidade.ATIVA);
            when(organizacaoFacade.buscarEntidadePorSigla("ADMIN")).thenReturn(sedoc);

            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder()
                    .dataLimite(LocalDate.now())
                    .observacoes("Obs")
                    .build();

            service.disponibilizarMapa(1L, req, new Usuario());

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
            verify(subprocessoRepo).save(sp);
            verify(movimentacaoRepo).save(any());
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

            verify(movimentacaoRepo).save(any());
            verify(analiseRepo).save(any());
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

            verify(movimentacaoRepo).save(any());
            verify(analiseRepo).save(any());
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
            when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L))
                    .thenReturn(List.of(mov));

            // Mock Unit lookup for Analise creation inside SubprocessoService.criarAnalise
            when(organizacaoFacade.buscarPorSigla("SUP")).thenReturn(new sgc.organizacao.dto.UnidadeDto());

            Usuario user = new Usuario();
            user.setTituloEleitoral("123");

            service.aceitarValidacao(1L, user);

            verify(analiseRepo).save(any());
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
            when(organizacaoFacade.buscarEntidadePorSigla("ADMIN")).thenReturn(admin);

            service.homologarValidacao(1L, new Usuario());

            verify(subprocessoRepo).save(sp);
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
            verify(movimentacaoRepo).save(any());
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

            // Setup validation mocks
            Competencia c = new Competencia();
            Atividade a = new Atividade(); a.setCodigo(1L);
            a.setCompetencias(Set.of(c));
            c.setAtividades(new HashSet<>(List.of(a)));
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(10L)).thenReturn(List.of(c));
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigo(10L)).thenReturn(List.of(a));

            when(organizacaoFacade.buscarEntidadePorSigla("ADMIN")).thenReturn(new Unidade());

            DisponibilizarMapaRequest req = DisponibilizarMapaRequest.builder().dataLimite(LocalDate.now()).build();
            service.disponibilizarMapaEmBloco(List.of(1L), req, new Usuario());

            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
            verify(subprocessoRepo).save(sp);
        }
    }
}
