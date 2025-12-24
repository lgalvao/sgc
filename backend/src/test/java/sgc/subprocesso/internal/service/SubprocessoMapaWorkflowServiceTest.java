package sgc.subprocesso.internal.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationEventPublisher;
import sgc.analise.AnaliseService;
import sgc.atividade.internal.model.Atividade;
import sgc.atividade.internal.model.AtividadeRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.internal.model.Competencia;
import sgc.mapa.internal.model.CompetenciaRepo;
import sgc.mapa.internal.model.Mapa;
import sgc.mapa.internal.service.CompetenciaService;
import sgc.mapa.MapaService;
import sgc.processo.api.eventos.*;
import sgc.processo.internal.model.Processo;
import sgc.processo.internal.model.TipoProcesso;
import sgc.sgrh.internal.model.Usuario;
import sgc.subprocesso.api.DisponibilizarMapaRequest;
import sgc.subprocesso.api.SubmeterMapaAjustadoReq;
import sgc.subprocesso.internal.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.internal.erros.ErroMapaNaoAssociado;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SubprocessoRepo;
import sgc.unidade.internal.model.Unidade;
import sgc.unidade.internal.model.UnidadeRepo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoMapaWorkflowService")
class SubprocessoMapaWorkflowServiceTest {

    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private CompetenciaRepo competenciaRepo;
    @Mock private AtividadeRepo atividadeRepo;
    @Mock private MapaService mapaService;
    @Mock private CompetenciaService competenciaService;
    @Mock private ApplicationEventPublisher publicadorDeEventos;
    @Mock private AnaliseService analiseService;
    @Mock private UnidadeRepo unidadeRepo;
    @Mock private SubprocessoService subprocessoService;

    @InjectMocks
    private SubprocessoMapaWorkflowService service;

    @Nested
    @DisplayName("Buscar Subprocesso")
    class BuscarSubprocesso {
        
        @Test
        @DisplayName("Deve lançar exceção quando subprocesso não encontrado")
        void deveLancarExcecaoQuandoSubprocessoNaoEncontrado() {
            // Arrange
            when(subprocessoRepo.findById(99L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> service.disponibilizarMapa(99L, null, null))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Subprocesso")
                .hasNoCause();
        }
    }

    @Nested
    @DisplayName("Disponibilizar Mapa")
    class DisponibilizarMapa {
        
        @Test
        @DisplayName("Deve disponibilizar mapa com sucesso")
        void deveDisponibilizarMapaComSucesso() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            
            DisponibilizarMapaRequest request = DisponibilizarMapaRequest.builder()
                    .dataLimite(LocalDate.now().plusDays(10))
                    .observacoes("obs")
                    .build();

            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(new Unidade()));
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(Collections.emptyList());
            when(atividadeRepo.findBySubprocessoCodigo(id)).thenReturn(Collections.emptyList());

            // Act
            service.disponibilizarMapa(id, request, new Usuario());

            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
            verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaDisponibilizado.class));
        }

        @Test
        @DisplayName("Deve falhar quando subprocesso em estado inválido")
        void deveFalharQuandoEstadoInvalido() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);

            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

            DisponibilizarMapaRequest request = DisponibilizarMapaRequest.builder()
                    .dataLimite(LocalDate.now().plusDays(10))
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> service.disponibilizarMapa(id, request, new Usuario()))
                    .isInstanceOf(ErroMapaEmSituacaoInvalida.class);
        }

        @Test
        @DisplayName("Deve falhar quando competência sem atividades associadas")
        void deveFalharQuandoCompetenciaSemAtividades() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            
            Competencia compSemAtividade = new Competencia();
            compSemAtividade.setAtividades(new HashSet<>());
            
            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of(compSemAtividade));

            DisponibilizarMapaRequest request = DisponibilizarMapaRequest.builder()
                    .dataLimite(LocalDate.now().plusDays(10))
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> service.disponibilizarMapa(id, request, new Usuario()))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("competências")
                    .hasMessageContaining("atividade");
        }

        @Test
        @DisplayName("Deve falhar quando atividade não associada a competência")
        void deveFalharQuandoAtividadeNaoAssociada() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            
            Atividade atividadeSolta = new Atividade();
            atividadeSolta.setCodigo(100L);
            atividadeSolta.setDescricao("Atividade Solta");
            
            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(Collections.emptyList());
            when(atividadeRepo.findBySubprocessoCodigo(id)).thenReturn(List.of(atividadeSolta));

            DisponibilizarMapaRequest request = DisponibilizarMapaRequest.builder()
                    .dataLimite(LocalDate.now().plusDays(10))
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> service.disponibilizarMapa(id, request, new Usuario()))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("atividades")
                    .hasMessageContaining("competência");
        }
    }

    @Nested
    @DisplayName("Apresentar Sugestões")
    class ApresentarSugestoes {
        
        @Test
        @DisplayName("Deve registrar sugestões com sucesso")
        void deveRegistrarSugestoesComSucesso() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(null);
            sp.getUnidade().setUnidadeSuperior(new Unidade());

            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

            // Act
            service.apresentarSugestoes(id, "Sugestões de melhoria", new Usuario());

            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES);
            assertThat(sp.getMapa().getSugestoes()).isEqualTo("Sugestões de melhoria");
            verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaComSugestoes.class));
        }
    }

    @Nested
    @DisplayName("Validar Mapa")
    class ValidarMapa {
        
        @Test
        @DisplayName("Deve validar mapa com sucesso")
        void deveValidarMapaComSucesso() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(null);

            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

            // Act
            service.validarMapa(id, new Usuario());

            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaValidado.class));
        }
    }

    @Nested
    @DisplayName("Devolver Validação")
    class DevolverValidacao {
        
        @Test
        @DisplayName("Deve devolver mapa com justificativa")
        void deveDevolverMapaComJustificativa() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(null);
            sp.getUnidade().setUnidadeSuperior(new Unidade());

            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

            // Act
            service.devolverValidacao(id, "Precisa de ajustes", new Usuario());

            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
            verify(analiseService).criarAnalise(any());
            verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaDevolvido.class));
        }
    }

    @Nested
    @DisplayName("Aceitar Validação")
    class AceitarValidacao {
        
        @Test
        @DisplayName("Deve homologar quando não houver próxima unidade")
        void deveHomologarQuandoNaoHouverProximaUnidade() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(null);
            Unidade sup = new Unidade();
            sup.setSigla("SUP");
            sp.getUnidade().setUnidadeSuperior(sup);

            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

            // Act
            service.aceitarValidacao(id, new Usuario());

            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
        }

        @Test
        @DisplayName("Deve manter validado quando houver próxima unidade na hierarquia")
        void deveManterValidadoQuandoHouverProximaUnidade() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(null);
            Unidade sup = new Unidade();
            sup.setSigla("SUP");
            Unidade sup2 = new Unidade();
            sup.setUnidadeSuperior(sup2);
            sp.getUnidade().setUnidadeSuperior(sup);

            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

            // Act
            service.aceitarValidacao(id, new Usuario());

            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO);
            verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaAceito.class));
        }
    }

    @Nested
    @DisplayName("Homologar Validação")
    class HomologarValidacao {
        
        @Test
        @DisplayName("Deve homologar mapa com sucesso")
        void deveHomologarMapaComSucesso() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(null);

            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(new Unidade()));

            // Act
            service.homologarValidacao(id, new Usuario());

            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);
            verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaHomologado.class));
        }
    }

    @Nested
    @DisplayName("Submeter Mapa Ajustado")
    class SubmeterMapaAjustado {
        
        @Test
        @DisplayName("Deve submeter mapa ajustado com sucesso")
        void deveSubmeterMapaAjustadoComSucesso() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(null);

            SubmeterMapaAjustadoReq req = new SubmeterMapaAjustadoReq();
            req.setDataLimiteEtapa2(LocalDateTime.now().plusDays(10));

            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));

            // Act
            service.submeterMapaAjustado(id, req, new Usuario());

            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
            verify(publicadorDeEventos).publishEvent(any(EventoSubprocessoMapaAjustadoSubmetido.class));
        }
    }

    // === Helper Methods ===
    
    private Subprocesso criarSubprocessoComMapa(SituacaoSubprocesso situacao) {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        if (situacao != null) {
            sp.setSituacao(situacao);
        }
        
        Mapa mapa = new Mapa();
        mapa.setCodigo(10L);
        sp.setMapa(mapa);
        
        Unidade u = new Unidade();
        sp.setUnidade(u);
        
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);
        
        return sp;
    }
}