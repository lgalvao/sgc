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
import sgc.processo.api.model.Processo;
import sgc.processo.api.model.TipoProcesso;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import sgc.mapa.api.CompetenciaMapaDto;
import sgc.mapa.api.MapaCompletoDto;
import sgc.mapa.api.SalvarMapaRequest;
import sgc.subprocesso.api.CompetenciaReq;

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

    @Nested
    @DisplayName("Salvar Mapa Subprocesso")
    class SalvarMapaSubprocesso {
        
        @Test
        @DisplayName("Deve alterar situação para MAPA_CRIADO quando mapa era vazio e receber competências")
        void deveAlterarSituacaoQuandoMapaEraVazioEReceberCompetencias() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            
            SalvarMapaRequest request = SalvarMapaRequest.builder()
                    .competencias(List.of(new CompetenciaMapaDto()))
                    .build();
            
            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of()); // Mapa vazio
            when(mapaService.salvarMapaCompleto(anyLong(), any(), any()))
                    .thenReturn(MapaCompletoDto.builder().build());
            
            // Act
            service.salvarMapaSubprocesso(id, request, "Usuario");
            
            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            verify(subprocessoRepo).save(sp);
        }
        
        @Test
        @DisplayName("Deve manter situação quando mapa já tinha competências")
        void deveManterSituacaoQuandoMapaJaTinhaCompetencias() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            
            SalvarMapaRequest request = SalvarMapaRequest.builder()
                    .competencias(List.of(new CompetenciaMapaDto()))
                    .build();
            
            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of(new Competencia())); // Já tem competências
            when(mapaService.salvarMapaCompleto(anyLong(), any(), any()))
                    .thenReturn(MapaCompletoDto.builder().build());
            
            // Act
            service.salvarMapaSubprocesso(id, request, "Usuario");
            
            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            verify(subprocessoRepo, never()).save(sp);
        }
        
        @Test
        @DisplayName("Deve manter situação quando request não tiver competências")
        void deveManterSituacaoQuandoRequestSemCompetencias() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            
            SalvarMapaRequest request = SalvarMapaRequest.builder()
                    .competencias(List.of()) // Sem competências
                    .build();
            
            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of());
            when(mapaService.salvarMapaCompleto(anyLong(), any(), any()))
                    .thenReturn(MapaCompletoDto.builder().build());
            
            // Act
            service.salvarMapaSubprocesso(id, request, "Usuario");
            
            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            verify(subprocessoRepo, never()).save(sp);
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando subprocesso em estado inválido para edição")
        void deveLancarExcecaoQuandoEstadoInvalidoParaEdicao() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(SituacaoSubprocesso.NAO_INICIADO);
            
            SalvarMapaRequest request = SalvarMapaRequest.builder()
                    .competencias(List.of())
                    .build();
            
            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            
            // Act & Assert
            assertThatThrownBy(() -> service.salvarMapaSubprocesso(id, request, "Usuario"))
                    .isInstanceOf(ErroMapaEmSituacaoInvalida.class);
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando subprocesso não tem mapa associado")
        void deveLancarExcecaoQuandoSemMapaAssociado() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            sp.setMapa(null); // Remove mapa
            
            SalvarMapaRequest request = SalvarMapaRequest.builder()
                    .competencias(List.of())
                    .build();
            
            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            
            // Act & Assert
            assertThatThrownBy(() -> service.salvarMapaSubprocesso(id, request, "Usuario"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }
    
    @Nested
    @DisplayName("Adicionar Competência")
    class AdicionarCompetencia {
        
        @Test
        @DisplayName("Deve alterar situação para MAPA_CRIADO quando mapa era vazio")
        void deveAlterarSituacaoQuandoMapaEraVazio() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            
            CompetenciaReq request = new CompetenciaReq();
            request.setDescricao("Nova competência");
            request.setAtividadesIds(List.of(1L));
            
            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of()); // Mapa vazio
            when(mapaService.obterMapaCompleto(anyLong(), anyLong()))
                    .thenReturn(MapaCompletoDto.builder().build());
            
            // Act
            service.adicionarCompetencia(id, request, "Usuario");
            
            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            verify(subprocessoRepo).save(sp);
            verify(competenciaService).adicionarCompetencia(any(), eq("Nova competência"), eq(List.of(1L)));
        }
        
        @Test
        @DisplayName("Deve manter situação quando mapa já tinha competências")
        void deveManterSituacaoQuandoMapaJaTinhaCompetencias() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            
            CompetenciaReq request = new CompetenciaReq();
            request.setDescricao("Nova competência");
            request.setAtividadesIds(List.of(1L));
            
            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of(new Competencia()));
            when(mapaService.obterMapaCompleto(anyLong(), anyLong()))
                    .thenReturn(MapaCompletoDto.builder().build());
            
            // Act
            service.adicionarCompetencia(id, request, "Usuario");
            
            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            verify(subprocessoRepo, never()).save(sp);
        }
        
        @Test
        @DisplayName("Deve manter situação quando situação não for CADASTRO_HOMOLOGADO")
        void deveManterSituacaoQuandoSituacaoNaoForCadastroHomologado() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            
            CompetenciaReq request = new CompetenciaReq();
            request.setDescricao("Nova competência");
            request.setAtividadesIds(List.of(1L));
            
            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of());
            when(mapaService.obterMapaCompleto(anyLong(), anyLong()))
                    .thenReturn(MapaCompletoDto.builder().build());
            
            // Act
            service.adicionarCompetencia(id, request, "Usuario");
            
            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            verify(subprocessoRepo, never()).save(sp);
        }
    }
    
    @Nested
    @DisplayName("Atualizar Competência")
    class AtualizarCompetencia {
        
        @Test
        @DisplayName("Deve atualizar competência com sucesso")
        void deveAtualizarCompetenciaComSucesso() {
            // Arrange
            Long id = 1L;
            Long codCompetencia = 10L;
            Subprocesso sp = criarSubprocessoComMapa(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            
            CompetenciaReq request = new CompetenciaReq();
            request.setDescricao("Competência atualizada");
            request.setAtividadesIds(List.of(1L, 2L));
            
            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            when(mapaService.obterMapaCompleto(anyLong(), anyLong()))
                    .thenReturn(MapaCompletoDto.builder().build());
            
            // Act
            MapaCompletoDto result = service.atualizarCompetencia(id, codCompetencia, request, "Usuario");
            
            // Assert
            assertThat(result).isNotNull();
            verify(competenciaService).atualizarCompetencia(eq(codCompetencia), eq("Competência atualizada"), eq(List.of(1L, 2L)));
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando subprocesso em estado inválido")
        void deveLancarExcecaoQuandoEstadoInvalido() {
            // Arrange
            Long id = 1L;
            Subprocesso sp = criarSubprocessoComMapa(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
            
            CompetenciaReq request = new CompetenciaReq();
            
            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            
            // Act & Assert
            assertThatThrownBy(() -> service.atualizarCompetencia(id, 10L, request, "Usuario"))
                    .isInstanceOf(ErroMapaEmSituacaoInvalida.class);
        }
    }
    
    @Nested
    @DisplayName("Remover Competência")
    class RemoverCompetencia {
        
        @Test
        @DisplayName("Deve alterar situação para CADASTRO_HOMOLOGADO quando mapa ficar vazio")
        void deveAlterarSituacaoQuandoMapaFicarVazio() {
            // Arrange
            Long id = 1L;
            Long codCompetencia = 10L;
            Subprocesso sp = criarSubprocessoComMapa(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            
            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of()); // Mapa ficou vazio
            when(mapaService.obterMapaCompleto(anyLong(), anyLong()))
                    .thenReturn(MapaCompletoDto.builder().build());
            
            // Act
            service.removerCompetencia(id, codCompetencia, "Usuario");
            
            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            verify(subprocessoRepo).save(sp);
            verify(competenciaService).removerCompetencia(codCompetencia);
        }
        
        @Test
        @DisplayName("Deve manter situação quando mapa ainda tiver competências")
        void deveManterSituacaoQuandoMapaAindaTiverCompetencias() {
            // Arrange
            Long id = 1L;
            Long codCompetencia = 10L;
            Subprocesso sp = criarSubprocessoComMapa(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            
            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of(new Competencia())); // Ainda tem
            when(mapaService.obterMapaCompleto(anyLong(), anyLong()))
                    .thenReturn(MapaCompletoDto.builder().build());
            
            // Act
            service.removerCompetencia(id, codCompetencia, "Usuario");
            
            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            verify(subprocessoRepo, never()).save(sp);
        }
        
        @Test
        @DisplayName("Deve manter situação quando não estiver em MAPA_CRIADO")
        void deveManterSituacaoQuandoNaoEstiverEmMapaCriado() {
            // Arrange
            Long id = 1L;
            Long codCompetencia = 10L;
            Subprocesso sp = criarSubprocessoComMapa(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            
            when(subprocessoRepo.findById(id)).thenReturn(Optional.of(sp));
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of()); // Vazio
            when(mapaService.obterMapaCompleto(anyLong(), anyLong()))
                    .thenReturn(MapaCompletoDto.builder().build());
            
            // Act
            service.removerCompetencia(id, codCompetencia, "Usuario");
            
            // Assert
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            verify(subprocessoRepo, never()).save(sp);
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