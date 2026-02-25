package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.model.ComumRepo;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.CopiaMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.SgcPermissionEvaluator;

import sgc.subprocesso.model.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("SubprocessoAtividadeService")
@ExtendWith(MockitoExtension.class)
class SubprocessoAtividadeServiceTest {
    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private SubprocessoWorkflowService crudService;

    @Mock
    private MapaManutencaoService mapaManutencaoService;

    @Mock
    private CopiaMapaService copiaMapaService;

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @Mock
    private UsuarioFacade usuarioService;

    @Mock
    private ComumRepo repo;

    @Mock
    private SgcPermissionEvaluator permissionEvaluator;

    @Mock
    private SubprocessoWorkflowService validacaoService;

    @InjectMocks
    private SubprocessoAtividadeService service;

    @Nested
    @DisplayName("importarAtividades")
    class ImportarAtividadesTests {

        @Test
        @DisplayName("deve lançar exceção quando subprocesso destino não encontrado")
        void deveLancarExcecaoQuandoSubprocessoDestinoNaoEncontrado() {
            // Arrange
            Long codDestino = 1L;
            Long codOrigem = 2L;
            when(repo.buscar(Subprocesso.class, codDestino))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Subprocesso", codDestino));

            // Act & Assert
            assertThatThrownBy(() -> service.importarAtividades(codDestino, codOrigem))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Subprocesso")
                    .hasMessageContaining("1");

            verify(repo).buscar(Subprocesso.class, codDestino);
            verifyNoInteractions(copiaMapaService);
        }

        @Test
        @DisplayName("deve lançar exceção quando subprocesso origem não encontrado")
        void deveLancarExcecaoQuandoSubprocessoOrigemNaoEncontrado() {
            // Arrange
            Long codDestino = 1L;
            Long codOrigem = 2L;

            Subprocesso spDestino = criarSubprocesso(codDestino, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            when(repo.buscar(Subprocesso.class, codDestino)).thenReturn(spDestino);
            when(repo.buscar(Subprocesso.class, codOrigem))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Subprocesso", codOrigem));

            doReturn(true).when(permissionEvaluator).checkPermission(any(), eq(spDestino), eq("EDITAR_CADASTRO"));

            // Act & Assert
            assertThatThrownBy(() -> service.importarAtividades(codDestino, codOrigem))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Subprocesso")
                    .hasMessageContaining("2");

            verify(repo).buscar(Subprocesso.class, codDestino);
            verify(repo).buscar(Subprocesso.class, codOrigem);
            verifyNoInteractions(copiaMapaService);
        }

        @Test
        @DisplayName("deve lançar exceção quando sem permissão na origem")
        void deveLancarExcecaoQuandoSemPermissaoOrigem() {
            // Arrange
            Long codDestino = 1L;
            Long codOrigem = 2L;

            Subprocesso spDestino = criarSubprocesso(codDestino, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            Subprocesso spOrigem = criarSubprocesso(codOrigem, SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO);

            when(repo.buscar(Subprocesso.class, codDestino)).thenReturn(spDestino);
            when(repo.buscar(Subprocesso.class, codOrigem)).thenReturn(spOrigem);
            when(usuarioService.usuarioAutenticado()).thenReturn(new Usuario());

            doReturn(true).when(permissionEvaluator).checkPermission(any(), eq(spDestino), eq("EDITAR_CADASTRO"));
            doReturn(false).when(permissionEvaluator).checkPermission(any(), eq(spOrigem), eq("CONSULTAR_PARA_IMPORTACAO"));

            // Act & Assert
            assertThatThrownBy(() -> service.importarAtividades(codDestino, codOrigem))
                    .isInstanceOf(ErroAcessoNegado.class);

            verifyNoInteractions(copiaMapaService);
        }

        @Test
        @DisplayName("deve importar atividades com sucesso quando destino em MAPEAMENTO_CADASTRO_EM_ANDAMENTO")
        void deveImportarAtividadesComSucessoQuandoDestinoEmMapeamentoCadastro() {
            // Arrange
            Long codDestino = 1L;
            Long codOrigem = 2L;

            Mapa mapaOrigem = criarMapa(10L);
            Mapa mapaDestino = criarMapa(20L);

            Subprocesso spDestino = criarSubprocessoComMapa(codDestino, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, mapaDestino);
            Subprocesso spOrigem = criarSubprocessoComMapa(codOrigem, SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO, mapaOrigem);

            when(repo.buscar(Subprocesso.class, codDestino)).thenReturn(spDestino);
            when(repo.buscar(Subprocesso.class, codOrigem)).thenReturn(spOrigem);
            when(usuarioService.usuarioAutenticado()).thenReturn(new Usuario());
            when(movimentacaoRepo.save(any(Movimentacao.class))).thenReturn(new Movimentacao());
            doReturn(true).when(permissionEvaluator).checkPermission(any(), eq(spDestino), eq("EDITAR_CADASTRO"));
            doReturn(true).when(permissionEvaluator).checkPermission(any(), eq(spOrigem), eq("CONSULTAR_PARA_IMPORTACAO"));

            // Act
            service.importarAtividades(codDestino, codOrigem);

            // Assert
            verify(copiaMapaService).importarAtividadesDeOutroMapa(10L, 20L);
            verify(movimentacaoRepo).save(any(Movimentacao.class));
            verify(subprocessoRepo, never()).save(any(Subprocesso.class)); // Não deve salvar pois já está em cadastro
        }

        @Test
        @DisplayName("deve atualizar situação quando destino em NAO_INICIADO e tipo MAPEAMENTO")
        void deveAtualizarSituacaoQuandoDestinoNaoIniciadoETipoMapeamento() {
            // Arrange
            Long codDestino = 1L;
            Long codOrigem = 2L;

            Mapa mapaOrigem = criarMapa(10L);
            Mapa mapaDestino = criarMapa(20L);

            Processo processoMapeamento = criarProcesso(TipoProcesso.MAPEAMENTO);
            Subprocesso spDestino = criarSubprocessoComMapaEProcesso(codDestino, SituacaoSubprocesso.NAO_INICIADO, mapaDestino, processoMapeamento);
            Subprocesso spOrigem = criarSubprocessoComMapa(codOrigem, SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO, mapaOrigem);

            when(repo.buscar(Subprocesso.class, codDestino)).thenReturn(spDestino);
            when(repo.buscar(Subprocesso.class, codOrigem)).thenReturn(spOrigem);
            when(subprocessoRepo.save(any(Subprocesso.class))).thenReturn(spDestino);
            when(usuarioService.usuarioAutenticado()).thenReturn(new Usuario());
            when(movimentacaoRepo.save(any(Movimentacao.class))).thenReturn(new Movimentacao());
            doReturn(true).when(permissionEvaluator).checkPermission(any(), eq(spDestino), eq("EDITAR_CADASTRO"));
            doReturn(true).when(permissionEvaluator).checkPermission(any(), eq(spOrigem), eq("CONSULTAR_PARA_IMPORTACAO"));

            // Act
            service.importarAtividades(codDestino, codOrigem);

            // Assert
            ArgumentCaptor<Subprocesso> subprocessoCaptor = ArgumentCaptor.forClass(Subprocesso.class);
            verify(subprocessoRepo).save(subprocessoCaptor.capture());

            Subprocesso subprocessoSalvo = subprocessoCaptor.getValue();
            assertThat(subprocessoSalvo.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            verify(usuarioService).usuarioAutenticado();
            verify(copiaMapaService).importarAtividadesDeOutroMapa(10L, 20L);
            verify(movimentacaoRepo).save(any(Movimentacao.class));
        }

        @Test
        @DisplayName("deve atualizar situação quando destino em NAO_INICIADO e tipo REVISAO")
        void deveAtualizarSituacaoQuandoDestinoNaoIniciadoETipoRevisao() {
            // Arrange
            Long codDestino = 1L;
            Long codOrigem = 2L;

            Mapa mapaOrigem = criarMapa(10L);
            Mapa mapaDestino = criarMapa(20L);

            Processo processoRevisao = criarProcesso(TipoProcesso.REVISAO);
            Subprocesso spDestino = criarSubprocessoComMapaEProcesso(codDestino, SituacaoSubprocesso.NAO_INICIADO, mapaDestino, processoRevisao);
            Subprocesso spOrigem = criarSubprocessoComMapa(codOrigem, SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO, mapaOrigem);

            when(repo.buscar(Subprocesso.class, codDestino)).thenReturn(spDestino);
            when(repo.buscar(Subprocesso.class, codOrigem)).thenReturn(spOrigem);
            when(subprocessoRepo.save(any(Subprocesso.class))).thenReturn(spDestino);
            when(usuarioService.usuarioAutenticado()).thenReturn(new Usuario());
            when(movimentacaoRepo.save(any(Movimentacao.class))).thenReturn(new Movimentacao());
            doReturn(true).when(permissionEvaluator).checkPermission(any(), eq(spDestino), eq("EDITAR_CADASTRO"));
            doReturn(true).when(permissionEvaluator).checkPermission(any(), eq(spOrigem), eq("CONSULTAR_PARA_IMPORTACAO"));

            // Act
            service.importarAtividades(codDestino, codOrigem);

            // Assert
            ArgumentCaptor<Subprocesso> subprocessoCaptor = ArgumentCaptor.forClass(Subprocesso.class);
            verify(subprocessoRepo).save(subprocessoCaptor.capture());

            Subprocesso subprocessoSalvo = subprocessoCaptor.getValue();
            assertThat(subprocessoSalvo.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

            verify(usuarioService).usuarioAutenticado();
            verify(copiaMapaService).importarAtividadesDeOutroMapa(10L, 20L);
            verify(movimentacaoRepo).save(any(Movimentacao.class));
        }

        @Test
        @DisplayName("deve cair no default quando tipo de processo é DIAGNOSTICO")
        void deveCairNoDefaultQuandoTipoDiagnostico() {
            // Arrange
            Long codDestino = 1L;
            Long codOrigem = 2L;

            Mapa mapaOrigem = criarMapa(10L);
            Mapa mapaDestino = criarMapa(20L);

            Processo processoDiagnostico = criarProcesso(TipoProcesso.DIAGNOSTICO);
            Subprocesso spDestino = criarSubprocessoComMapaEProcesso(codDestino, SituacaoSubprocesso.NAO_INICIADO, mapaDestino, processoDiagnostico);
            Subprocesso spOrigem = criarSubprocessoComMapa(codOrigem, SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO, mapaOrigem);

            when(repo.buscar(Subprocesso.class, codDestino)).thenReturn(spDestino);
            when(repo.buscar(Subprocesso.class, codOrigem)).thenReturn(spOrigem);
            when(subprocessoRepo.save(any(Subprocesso.class))).thenReturn(spDestino);
            when(usuarioService.usuarioAutenticado()).thenReturn(new Usuario());
            when(movimentacaoRepo.save(any(Movimentacao.class))).thenReturn(new Movimentacao());
            doReturn(true).when(permissionEvaluator).checkPermission(any(), eq(spDestino), eq("EDITAR_CADASTRO"));
            doReturn(true).when(permissionEvaluator).checkPermission(any(), eq(spOrigem), eq("CONSULTAR_PARA_IMPORTACAO"));

            // Act
            service.importarAtividades(codDestino, codOrigem);

            // Assert
            verify(subprocessoRepo).save(spDestino);
            assertThat(spDestino.getSituacao()).isEqualTo(SituacaoSubprocesso.NAO_INICIADO); // Não muda no default
            verify(copiaMapaService).importarAtividadesDeOutroMapa(10L, 20L);
        }

        @Test
        @DisplayName("deve importar sem atualizar situação quando destino em REVISAO_CADASTRO_EM_ANDAMENTO")
        void deveImportarSemAtualizarQuandoDestinoEmRevisaoCadastro() {
            // Arrange
            Long codDestino = 1L;
            Long codOrigem = 2L;

            Mapa mapaOrigem = criarMapa(10L);
            Mapa mapaDestino = criarMapa(20L);

            Processo processoRevisao = criarProcesso(TipoProcesso.REVISAO);
            Subprocesso spDestino = criarSubprocessoComMapaEProcesso(codDestino, SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, mapaDestino, processoRevisao);
            Subprocesso spOrigem = criarSubprocessoComMapa(codOrigem, SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO, mapaOrigem);

            when(repo.buscar(Subprocesso.class, codDestino)).thenReturn(spDestino);
            when(repo.buscar(Subprocesso.class, codOrigem)).thenReturn(spOrigem);
            when(usuarioService.usuarioAutenticado()).thenReturn(new Usuario());
            when(movimentacaoRepo.save(any(Movimentacao.class))).thenReturn(new Movimentacao());
            doReturn(true).when(permissionEvaluator).checkPermission(any(), eq(spDestino), eq("EDITAR_CADASTRO"));
            doReturn(true).when(permissionEvaluator).checkPermission(any(), eq(spOrigem), eq("CONSULTAR_PARA_IMPORTACAO"));

            // Act
            service.importarAtividades(codDestino, codOrigem);

            // Assert
            verify(copiaMapaService).importarAtividadesDeOutroMapa(10L, 20L);
            verify(movimentacaoRepo).save(any(Movimentacao.class));
            verify(subprocessoRepo, never()).save(any(Subprocesso.class)); // Não deve salvar
        }
    }

    @Nested
    @DisplayName("listarAtividadesSubprocesso")
    class ListarAtividadesSubprocessoTests {

        @Test
        @DisplayName("deve listar atividades com conhecimentos")
        void deveListarAtividadesComConhecimentos() {
            // Arrange
            Long codSubprocesso = 1L;
            Long codMapa = 10L;

            Mapa mapa = criarMapa(codMapa);
            Subprocesso subprocesso = criarSubprocessoComMapa(codSubprocesso, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, mapa);

            Conhecimento conhecimento1 = criarConhecimento(100L, "Conhecimento 1");
            Conhecimento conhecimento2 = criarConhecimento(101L, "Conhecimento 2");

            Atividade atividade1 = criarAtividadeComConhecimentos(1L, "Atividade 1", List.of(conhecimento1));
            Atividade atividade2 = criarAtividadeComConhecimentos(2L, "Atividade 2", List.of(conhecimento2));

            when(subprocessoRepo.findByIdWithMapaAndAtividades(codSubprocesso)).thenReturn(Optional.of(subprocesso));
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(codMapa))
                    .thenReturn(List.of(atividade1, atividade2));

            // Act
            List<AtividadeDto> resultado = service.listarAtividadesSubprocesso(codSubprocesso);

            // Assert
            assertThat(resultado).hasSize(2);

            AtividadeDto dto1 = resultado.getFirst();
            assertThat(dto1.codigo()).isEqualTo(1L);
            assertThat(dto1.descricao()).isEqualTo("Atividade 1");
            assertThat(dto1.conhecimentos()).hasSize(1);
            assertThat(dto1.conhecimentos().getFirst().getCodigo()).isEqualTo(100L);
            assertThat(dto1.conhecimentos().getFirst().getDescricao()).isEqualTo("Conhecimento 1");

            AtividadeDto dto2 = resultado.get(1);
            assertThat(dto2.codigo()).isEqualTo(2L);
            assertThat(dto2.descricao()).isEqualTo("Atividade 2");
            assertThat(dto2.conhecimentos()).hasSize(1);
            assertThat(dto2.conhecimentos().getFirst().getCodigo()).isEqualTo(101L);

            verify(subprocessoRepo).findByIdWithMapaAndAtividades(codSubprocesso);
            verify(mapaManutencaoService).buscarAtividadesPorMapaCodigoComConhecimentos(codMapa);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há atividades")
        void deveRetornarListaVaziaQuandoNaoHaAtividades() {
            // Arrange
            Long codSubprocesso = 1L;
            Long codMapa = 10L;

            Mapa mapa = criarMapa(codMapa);
            Subprocesso subprocesso = criarSubprocessoComMapa(codSubprocesso, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, mapa);

            when(subprocessoRepo.findByIdWithMapaAndAtividades(codSubprocesso)).thenReturn(Optional.of(subprocesso));
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(codMapa))
                    .thenReturn(List.of());

            // Act
            List<AtividadeDto> resultado = service.listarAtividadesSubprocesso(codSubprocesso);

            // Assert
            assertThat(resultado).isEmpty();
            verify(subprocessoRepo).findByIdWithMapaAndAtividades(codSubprocesso);
            verify(mapaManutencaoService).buscarAtividadesPorMapaCodigoComConhecimentos(codMapa);
        }
    }

    // ============================================================================================
    // MÉTODOS AUXILIARES
    // ============================================================================================

    private Subprocesso criarSubprocesso(Long codigo, SituacaoSubprocesso situacao) {
        Unidade unidade = Unidade.builder()
                .codigo(1L)
                .sigla("SIGLA")
                .nome("Unidade Teste")
                .build();

        return Subprocesso.builder()
                .codigo(codigo)
                .situacao(situacao)
                .processo(criarProcesso(TipoProcesso.MAPEAMENTO))
                .unidade(unidade)
                .build();
    }

    private Subprocesso criarSubprocessoComMapa(Long codigo, SituacaoSubprocesso situacao, Mapa mapa) {
        Unidade unidade = Unidade.builder()
                .codigo(1L)
                .sigla("SIGLA")
                .nome("Unidade Teste")
                .build();

        return Subprocesso.builder()
                .codigo(codigo)
                .situacao(situacao)
                .processo(criarProcesso(TipoProcesso.MAPEAMENTO))
                .mapa(mapa)
                .unidade(unidade)
                .build();
    }

    private Subprocesso criarSubprocessoComMapaEProcesso(Long codigo, SituacaoSubprocesso situacao, Mapa mapa, Processo processo) {
        Unidade unidade = Unidade.builder()
                .codigo(1L)
                .sigla("SIGLA")
                .nome("Unidade Teste")
                .build();

        return Subprocesso.builder()
                .codigo(codigo)
                .situacao(situacao)
                .mapa(mapa)
                .processo(processo)
                .unidade(unidade)
                .build();
    }

    private Mapa criarMapa(Long codigo) {
        return Mapa.builder()
                .codigo(codigo)
                .build();
    }

    private Processo criarProcesso(TipoProcesso tipo) {
        return Processo.builder()
                .codigo(1L)
                .tipo(tipo)
                .build();
    }

    private Conhecimento criarConhecimento(Long codigo, String descricao) {
        return Conhecimento.builder()
                .codigo(codigo)
                .descricao(descricao)
                .build();
    }

    private Atividade criarAtividadeComConhecimentos(Long codigo, String descricao, List<Conhecimento> conhecimentos) {
        return Atividade.builder()
                .codigo(codigo)
                .descricao(descricao)
                .conhecimentos(new LinkedHashSet<>(conhecimentos))
                .build();
    }
}
