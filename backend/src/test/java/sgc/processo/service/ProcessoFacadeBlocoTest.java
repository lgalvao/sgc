package sgc.processo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaFacade;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.AcaoEmBlocoRequest;
import sgc.processo.mapper.ProcessoMapper;
import sgc.processo.model.AcaoProcesso;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Testes para ações em bloco do ProcessoFacade.
 * Foca em maximizar cobertura de branches nas operações em bloco.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoFacade - Ações em Bloco")
class ProcessoFacadeBlocoTest {
    @Mock
    private ProcessoConsultaService processoConsultaService;
    @Mock
    private ProcessoManutencaoService processoManutencaoService;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private SubprocessoFacade subprocessoFacade;
    @Mock
    private ProcessoMapper processoMapper;
    @Mock
    private ProcessoDetalheBuilder processoDetalheBuilder;
    @Mock
    private SubprocessoMapper subprocessoMapper;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private ProcessoInicializador processoInicializador;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private ProcessoAcessoService processoAcessoService;
    @Mock
    private ProcessoFinalizador processoFinalizador;

    private ProcessoFacade processoFacade;

    @BeforeEach
    void setUp() {
        processoFacade = new ProcessoFacade(
            processoConsultaService,
            processoManutencaoService,
            unidadeService,
            subprocessoFacade,
            processoMapper,
            processoDetalheBuilder,
            subprocessoMapper,
            usuarioService,
            processoInicializador,
            alertaService,
            processoAcessoService,
            processoFinalizador
        );
    }

    @Nested
    @DisplayName("Executar Ação em Bloco - DISPONIBILIZAR")
    class AcaoDisponibilizar {
        @Test
        @DisplayName("Deve disponibilizar mapas em bloco quando ação é DISPONIBILIZAR")
        void deveDisponibilizarMapasEmBloco() {
            // Arrange
            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral("12345678901");
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            LocalDate dataLimite = LocalDate.now().plusDays(30);
            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L, 2L, 3L),
                AcaoProcesso.DISPONIBILIZAR,
                dataLimite
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert
            ArgumentCaptor<DisponibilizarMapaRequest> captor = 
                ArgumentCaptor.forClass(DisponibilizarMapaRequest.class);
            verify(subprocessoFacade).disponibilizarMapaEmBloco(
                eq(List.of(1L, 2L, 3L)),
                eq(100L),
                captor.capture(),
                eq(usuario)
            );
            
            DisponibilizarMapaRequest captured = captor.getValue();
            assertThat(captured.dataLimite()).isNotNull();
            assertThat(captured.observacoes()).isEqualTo("Disponibilização em bloco");

            // Não deve chamar os métodos de aceitar/homologar
            verify(subprocessoFacade, never()).aceitarCadastroEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade, never()).aceitarValidacaoEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade, never()).homologarCadastroEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade, never()).homologarValidacaoEmBloco(anyList(), anyLong(), any());
        }

        @Test
        @DisplayName("Deve retornar early quando ação é DISPONIBILIZAR sem executar lógica de aceite/homologação")
        void deveRetornarEarlyQuandoDisponibilizar() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L),
                AcaoProcesso.DISPONIBILIZAR,
                LocalDate.now()
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert - não deve buscar subprocessos
            verify(subprocessoFacade, never()).listarPorProcessoEUnidades(anyLong(), anyList());
        }
    }

    @Nested
    @DisplayName("Executar Ação em Bloco - ACEITAR")
    class AcaoAceitar {
        @Test
        @DisplayName("Deve aceitar cadastro quando subprocessos estão em MAPEAMENTO_CADASTRO_DISPONIBILIZADO")
        void deveAceitarCadastroQuandoMapeamentoCadastro() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoDto sp1 = SubprocessoDto.builder()
                .codUnidade(1L)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                .build();
            SubprocessoDto sp2 = SubprocessoDto.builder()
                .codUnidade(2L)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                .build();

            when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L, 2L))).thenReturn(List.of(sp1, sp2));

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L, 2L),
                AcaoProcesso.ACEITAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert
            verify(subprocessoFacade).aceitarCadastroEmBloco(List.of(1L, 2L), 100L, usuario);
            verify(subprocessoFacade, never()).aceitarValidacaoEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade, never()).homologarCadastroEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade, never()).homologarValidacaoEmBloco(anyList(), anyLong(), any());
        }

        @Test
        @DisplayName("Deve aceitar validação quando subprocessos estão em situação de mapa disponibilizado")
        void deveAceitarValidacaoQuandoMapaDisponibilizado() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoDto sp1 = SubprocessoDto.builder()
                .codUnidade(1L)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO)
                .build();
            SubprocessoDto sp2 = SubprocessoDto.builder()
                .codUnidade(2L)
                .situacao(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO)
                .build();

            when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L, 2L))).thenReturn(List.of(sp1, sp2));

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L, 2L),
                AcaoProcesso.ACEITAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert
            verify(subprocessoFacade).aceitarValidacaoEmBloco(List.of(1L, 2L), 100L, usuario);
            verify(subprocessoFacade, never()).aceitarCadastroEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade, never()).homologarCadastroEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade, never()).homologarValidacaoEmBloco(anyList(), anyLong(), any());
        }

        @Test
        @DisplayName("Deve separar aceite de cadastro e validação corretamente")
        void deveSepararAceiteCadastroEValidacao() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoDto spCadastro1 = SubprocessoDto.builder()
                .codUnidade(1L)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                .build();
            SubprocessoDto spCadastro2 = SubprocessoDto.builder()
                .codUnidade(2L)
                .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA)
                .build();
            SubprocessoDto spValidacao1 = SubprocessoDto.builder()
                .codUnidade(3L)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO)
                .build();
            SubprocessoDto spValidacao2 = SubprocessoDto.builder()
                .codUnidade(4L)
                .situacao(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO)
                .build();

            when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L, 2L, 3L, 4L)))
                .thenReturn(List.of(spCadastro1, spCadastro2, spValidacao1, spValidacao2));

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L, 2L, 3L, 4L),
                AcaoProcesso.ACEITAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert
            verify(subprocessoFacade).aceitarCadastroEmBloco(List.of(1L, 2L), 100L, usuario);
            verify(subprocessoFacade).aceitarValidacaoEmBloco(List.of(3L, 4L), 100L, usuario);
            verify(subprocessoFacade, never()).homologarCadastroEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade, never()).homologarValidacaoEmBloco(anyList(), anyLong(), any());
        }

        @Test
        @DisplayName("Deve aceitar quando subprocesso está em REVISAO_CADASTRO_HOMOLOGADA")
        void deveAceitarQuandoRevisaoCadastroHomologada() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoDto sp = SubprocessoDto.builder()
                .codUnidade(1L)
                .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA)
                .build();

            when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L),
                AcaoProcesso.ACEITAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert
            verify(subprocessoFacade).aceitarCadastroEmBloco(List.of(1L), 100L, usuario);
        }
    }

    @Nested
    @DisplayName("Executar Ação em Bloco - HOMOLOGAR")
    class AcaoHomologar {
        @Test
        @DisplayName("Deve homologar cadastro quando subprocessos estão em situação de cadastro")
        void deveHomologarCadastroQuandoCadastro() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoDto sp1 = SubprocessoDto.builder()
                .codUnidade(1L)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                .build();
            SubprocessoDto sp2 = SubprocessoDto.builder()
                .codUnidade(2L)
                .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA)
                .build();

            when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L, 2L))).thenReturn(List.of(sp1, sp2));

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L, 2L),
                AcaoProcesso.HOMOLOGAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert
            verify(subprocessoFacade).homologarCadastroEmBloco(List.of(1L, 2L), 100L, usuario);
            verify(subprocessoFacade, never()).homologarValidacaoEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade, never()).aceitarCadastroEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade, never()).aceitarValidacaoEmBloco(anyList(), anyLong(), any());
        }

        @Test
        @DisplayName("Deve homologar validação quando subprocessos estão em situação de mapa disponibilizado")
        void deveHomologarValidacaoQuandoMapaDisponibilizado() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoDto sp1 = SubprocessoDto.builder()
                .codUnidade(1L)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO)
                .build();
            SubprocessoDto sp2 = SubprocessoDto.builder()
                .codUnidade(2L)
                .situacao(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO)
                .build();

            when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L, 2L))).thenReturn(List.of(sp1, sp2));

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L, 2L),
                AcaoProcesso.HOMOLOGAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert
            verify(subprocessoFacade).homologarValidacaoEmBloco(List.of(1L, 2L), 100L, usuario);
            verify(subprocessoFacade, never()).homologarCadastroEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade, never()).aceitarCadastroEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade, never()).aceitarValidacaoEmBloco(anyList(), anyLong(), any());
        }

        @Test
        @DisplayName("Deve separar homologação de cadastro e validação corretamente")
        void deveSepararHomologacaoCadastroEValidacao() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoDto spCadastro1 = SubprocessoDto.builder()
                .codUnidade(1L)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                .build();
            SubprocessoDto spCadastro2 = SubprocessoDto.builder()
                .codUnidade(2L)
                .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA)
                .build();
            SubprocessoDto spValidacao1 = SubprocessoDto.builder()
                .codUnidade(3L)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO)
                .build();
            SubprocessoDto spValidacao2 = SubprocessoDto.builder()
                .codUnidade(4L)
                .situacao(SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO)
                .build();

            when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L, 2L, 3L, 4L)))
                .thenReturn(List.of(spCadastro1, spCadastro2, spValidacao1, spValidacao2));

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L, 2L, 3L, 4L),
                AcaoProcesso.HOMOLOGAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert
            verify(subprocessoFacade).homologarCadastroEmBloco(List.of(1L, 2L), 100L, usuario);
            verify(subprocessoFacade).homologarValidacaoEmBloco(List.of(3L, 4L), 100L, usuario);
            verify(subprocessoFacade, never()).aceitarCadastroEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade, never()).aceitarValidacaoEmBloco(anyList(), anyLong(), any());
        }

        @Test
        @DisplayName("Deve homologar cadastro quando em REVISAO_CADASTRO_HOMOLOGADA")
        void deveHomologarCadastroQuandoRevisaoHomologada() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoDto sp = SubprocessoDto.builder()
                .codUnidade(1L)
                .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA)
                .build();

            when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L),
                AcaoProcesso.HOMOLOGAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert
            verify(subprocessoFacade).homologarCadastroEmBloco(List.of(1L), 100L, usuario);
        }
    }

    @Nested
    @DisplayName("Casos de Branch - Listas Vazias")
    class ListasVazias {
        @Test
        @DisplayName("Não deve chamar aceitarCadastroEmBloco quando lista vazia")
        void naoDeveChamarAceitarCadastroQuandoListaVazia() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoDto sp = SubprocessoDto.builder()
                .codUnidade(1L)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO)
                .build();

            when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L),
                AcaoProcesso.ACEITAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert - lista de cadastro vazia, só validação
            verify(subprocessoFacade, never()).aceitarCadastroEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade).aceitarValidacaoEmBloco(List.of(1L), 100L, usuario);
        }

        @Test
        @DisplayName("Não deve chamar aceitarValidacaoEmBloco quando lista vazia")
        void naoDeveChamarAceitarValidacaoQuandoListaVazia() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoDto sp = SubprocessoDto.builder()
                .codUnidade(1L)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                .build();

            when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L),
                AcaoProcesso.ACEITAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert - lista de validação vazia, só cadastro
            verify(subprocessoFacade).aceitarCadastroEmBloco(List.of(1L), 100L, usuario);
            verify(subprocessoFacade, never()).aceitarValidacaoEmBloco(anyList(), anyLong(), any());
        }

        @Test
        @DisplayName("Não deve chamar homologarCadastroEmBloco quando lista vazia")
        void naoDeveChamarHomologarCadastroQuandoListaVazia() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoDto sp = SubprocessoDto.builder()
                .codUnidade(1L)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO)
                .build();

            when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L),
                AcaoProcesso.HOMOLOGAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert - lista de homologar cadastro vazia, só validação
            verify(subprocessoFacade, never()).homologarCadastroEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade).homologarValidacaoEmBloco(List.of(1L), 100L, usuario);
        }

        @Test
        @DisplayName("Não deve chamar homologarValidacaoEmBloco quando lista vazia")
        void naoDeveChamarHomologarValidacaoQuandoListaVazia() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoDto sp = SubprocessoDto.builder()
                .codUnidade(1L)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                .build();

            when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L),
                AcaoProcesso.HOMOLOGAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert - lista de homologar validação vazia, só cadastro
            verify(subprocessoFacade).homologarCadastroEmBloco(List.of(1L), 100L, usuario);
            verify(subprocessoFacade, never()).homologarValidacaoEmBloco(anyList(), anyLong(), any());
        }
    }

    @Nested
    @DisplayName("Casos de Situações Específicas")
    class SituacoesEspecificas {
        @Test
        @DisplayName("Não deve chamar nenhum método quando lista de unidades está vazia para ACEITAR")
        void naoDeveChamarNadaQuandoListaVaziaAceitar() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(),  // lista vazia
                AcaoProcesso.ACEITAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert - não deve chamar nenhum método de batch
            verify(subprocessoFacade, never()).listarPorProcessoEUnidades(anyLong(), anyList());
            verify(subprocessoFacade, never()).aceitarCadastroEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade, never()).aceitarValidacaoEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade, never()).homologarCadastroEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade, never()).homologarValidacaoEmBloco(anyList(), anyLong(), any());
        }

        @Test
        @DisplayName("Identificar MAPEAMENTO_CADASTRO_DISPONIBILIZADO como cadastro")
        void deveIdentificarMapeamentoCadastroComoSituacaoCadastro() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoDto sp = SubprocessoDto.builder()
                .codUnidade(1L)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO)
                .build();

            when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L),
                AcaoProcesso.ACEITAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert
            verify(subprocessoFacade).aceitarCadastroEmBloco(List.of(1L), 100L, usuario);
        }

        @Test
        @DisplayName("Deve identificar REVISAO_CADASTRO_DISPONIBILIZADA como cadastro")
        void deveIdentificarRevisaoCadastroComoSituacaoCadastro() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoDto sp = SubprocessoDto.builder()
                .codUnidade(1L)
                .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA)
                .build();

            when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L),
                AcaoProcesso.ACEITAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert
            verify(subprocessoFacade).aceitarCadastroEmBloco(List.of(1L), 100L, usuario);
        }

        @Test
        @DisplayName("Deve identificar REVISAO_CADASTRO_HOMOLOGADA como cadastro")
        void deveIdentificarRevisaoCadastroHomologadaComoSituacaoCadastro() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoDto sp = SubprocessoDto.builder()
                .codUnidade(1L)
                .situacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA)
                .build();

            when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L),
                AcaoProcesso.ACEITAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert
            verify(subprocessoFacade).aceitarCadastroEmBloco(List.of(1L), 100L, usuario);
        }

        @Test
        @DisplayName("Deve identificar outras situações como NÃO cadastro")
        void deveIdentificarOutrasSituacoesComoNaoCadastro() {
            // Arrange
            Usuario usuario = new Usuario();
            when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

            SubprocessoDto sp = SubprocessoDto.builder()
                .codUnidade(1L)
                .situacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO)
                .build();

            when(subprocessoFacade.listarPorProcessoEUnidades(100L, List.of(1L))).thenReturn(List.of(sp));

            AcaoEmBlocoRequest req = new AcaoEmBlocoRequest(
                List.of(1L),
                AcaoProcesso.ACEITAR,
                null
            );

            // Act
            processoFacade.executarAcaoEmBloco(100L, req);

            // Assert - vai para validação
            verify(subprocessoFacade, never()).aceitarCadastroEmBloco(anyList(), anyLong(), any());
            verify(subprocessoFacade).aceitarValidacaoEmBloco(List.of(1L), 100L, usuario);
        }
    }
}
