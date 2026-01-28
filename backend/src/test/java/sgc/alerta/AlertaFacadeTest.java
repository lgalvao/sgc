package sgc.alerta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import sgc.alerta.dto.AlertaDto;
import sgc.alerta.mapper.AlertaMapper;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuario;
import sgc.alerta.model.AlertaUsuarioRepo;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;

/**
 * Testes unitários consolidados do AlertaFacade.
 * <p>
 * Consolida testes de:
 * - AlertaServiceTest.java (15 testes)
 * - AlertaServiceUpdateTest.java (4 testes)
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaFacade - Testes Unitários")
class AlertaFacadeTest {
    @Mock
    private AlertaRepo alertaRepo;

    @Mock
    private AlertaUsuarioRepo alertaUsuarioRepo;

    @Mock
    private UsuarioFacade usuarioService;

    @Mock
    private AlertaMapper alertaMapper;

    @Mock
    private UnidadeFacade unidadeService;

    @InjectMocks
    private AlertaFacade service;

    private void criarSedocMock() {
        Unidade sedoc = new Unidade();
        sedoc.setCodigo(15L);
        sedoc.setSigla("SEDOC");
        when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(sedoc);
    }

    @Nested
    @DisplayName("Método: criarAlerta")
    class CriarAlerta {
        @Test
        @DisplayName("Deve criar alerta com sucesso")
        void deveCriarAlertaComSucesso() {
            // Given
            criarSedocMock();
            Processo p = new Processo();
            Unidade u = new Unidade();
            u.setCodigo(1L);

            when(alertaRepo.save(any())).thenReturn(new Alerta().setCodigo(100L));

            // When
            Alerta resultado = service.criarAlertaSedoc(p, u, "desc");

            // Then
            assertThat(resultado).isNotNull();
            verify(alertaRepo).save(any());
        }
    }

    @Nested
    @DisplayName("Método: criarAlertasProcessoIniciado")
    @SuppressWarnings("unused")
    class CriarAlertasProcessoIniciado {
        @Test
        @DisplayName("Deve criar alerta operacional com texto fixo")
        void deveCriarAlertaOperacional() {
            // Given
            criarSedocMock();
            Processo p = new Processo();
            p.setDescricao("Proc");
            Unidade u = new Unidade();
            u.setCodigo(1L);
            u.setTipo(TipoUnidade.OPERACIONAL);

            when(alertaRepo.save(any())).thenReturn(new Alerta());

            // When
            service.criarAlertasProcessoIniciado(p, List.of(u));

            // Then
            verify(alertaRepo).save(argThat(a -> "Início do processo".equals(a.getDescricao())));
        }

        @Test
        @DisplayName("Deve criar alerta para unidade participante e seus ancestrais")
        void deveCriarAlertaParaAncestrais() {
            // Given
            criarSedocMock();
            Processo p = new Processo();

            Unidade root = Unidade.builder().nome("Root").tipo(TipoUnidade.INTEROPERACIONAL).build();
            root.setCodigo(1L);

            Unidade filho = Unidade.builder().nome("Filho").tipo(TipoUnidade.OPERACIONAL).build();
            filho.setCodigo(2L);
            filho.setUnidadeSuperior(root);

            when(alertaRepo.save(any())).thenReturn(new Alerta());

            // When
            service.criarAlertasProcessoIniciado(p, List.of(filho));

            // Then
            // 1 alerta operacional para o filho
            verify(alertaRepo).save(argThat(a -> "Início do processo".equals(a.getDescricao())
                    && a.getUnidadeDestino().getCodigo().equals(2L)));

            // 1 alerta intermediário para o pai (root)
            verify(alertaRepo).save(argThat(a -> "Início do processo em unidades subordinadas".equals(a.getDescricao())
                    && a.getUnidadeDestino().getCodigo().equals(1L)));
        }

        @Test
        @DisplayName("Deve criar 2 alertas para interoperacional participante")
        void deveCriarDoisAlertasInteroperacional() {
            // Given
            criarSedocMock();
            Processo p = new Processo();
            Unidade u = Unidade.builder().tipo(TipoUnidade.INTEROPERACIONAL).build();
            u.setCodigo(1L);

            when(alertaRepo.save(any())).thenReturn(new Alerta());

            // When
            List<Alerta> resultado = service.criarAlertasProcessoIniciado(p, List.of(u));

            // Then
            assertThat(resultado).hasSize(2);
            verify(alertaRepo, times(2)).save(any());
        }
    }

    @Nested
    @DisplayName("Métodos: criação de alertas específicos")
    @SuppressWarnings("unused")
    class AlertasEspecificos {
        @Test
        @DisplayName("Deve criar alerta de cadastro disponibilizado com sucesso")
        void deveCriarAlertaCadastroDisponibilizado() {
            // Given
            Processo p = new Processo();
            p.setDescricao("P");
            Unidade uOrigem = new Unidade();
            uOrigem.setSigla("UO");
            Unidade uDestino = new Unidade();

            when(alertaRepo.save(any())).thenReturn(new Alerta());

            // When
            service.criarAlertaCadastroDisponibilizado(p, uOrigem, uDestino);

            // Then
            verify(alertaRepo).save(any());
        }

        @Test
        @DisplayName("Deve criar alerta de cadastro devolvido com sucesso")
        void deveCriarAlertaCadastroDevolvido() {
            // Given
            criarSedocMock();
            Processo p = new Processo();
            p.setDescricao("P");
            Unidade uDestino = new Unidade();

            when(alertaRepo.save(any())).thenReturn(new Alerta());

            // When
            service.criarAlertaCadastroDevolvido(p, uDestino, "motivo");

            // Then
            verify(alertaRepo).save(any());
        }

        @Test
        @DisplayName("Deve criar alerta de transição e retornar null se destino nulo")
        void deveRetornarNullSeDestinoNuloEmCriarAlertaTransicao() {
            Processo p = new Processo();
            Unidade uOrigem = new Unidade();
            Alerta resultado = service.criarAlertaTransicao(p, "desc", uOrigem, null);
            assertThat(resultado).isNull();
        }

        @Test
        @DisplayName("Deve criar alerta de transição com sucesso")
        void deveCriarAlertaTransicaoComSucesso() {
            Processo p = new Processo();
            Unidade uOrigem = new Unidade();
            Unidade uDestino = new Unidade();
            uDestino.setSigla("DEST");
            when(alertaRepo.save(any())).thenReturn(new Alerta());

            Alerta resultado = service.criarAlertaTransicao(p, "desc", uOrigem, uDestino);
            assertThat(resultado).isNotNull();
            verify(alertaRepo).save(any());
        }

        @Test
        @DisplayName("Deve criar alerta de alteração de data limite")
        void deveCriarAlertaAlteracaoDataLimite() {
            criarSedocMock();
            Processo p = new Processo();
            Unidade uDestino = new Unidade();
            when(alertaRepo.save(any())).thenReturn(new Alerta());

            service.criarAlertaAlteracaoDataLimite(p, uDestino, "20/10/2023", 1);
            verify(alertaRepo).save(any());
        }

        @Test
        @DisplayName("Deve criar alertas de reabertura")
        void deveCriarAlertasDeReabertura() {
            criarSedocMock();
            Processo p = new Processo();
            Unidade u = new Unidade();
            u.setSigla("U1");
            Unidade sup = new Unidade();

            when(alertaRepo.save(any())).thenReturn(new Alerta());

            service.criarAlertaReaberturaCadastro(p, u, "Justificativa Teste");
            service.criarAlertaReaberturaCadastroSuperior(p, sup, u);
            service.criarAlertaReaberturaRevisao(p, u, "Justificativa Teste");
            service.criarAlertaReaberturaRevisaoSuperior(p, sup, u);

            verify(alertaRepo, times(4)).save(any());
        }
    }

    @Nested
    @DisplayName("Método: listarAlertasPorUsuario")
    @SuppressWarnings("unused")
    class ListarAlertasPorUsuario {
        @Test
        @DisplayName("Deve listar alertas com sucesso")
        void deveListarAlertas() {
            // Given
            String usuarioTitulo = "123";
            Long codUnidade = 1L;

            Unidade unidade = new Unidade();
            unidade.setCodigo(codUnidade);

            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral(usuarioTitulo);
            usuario.setUnidadeLotacao(unidade);

            Alerta alerta = new Alerta();
            alerta.setCodigo(100L);

            when(usuarioService.buscarPorId(usuarioTitulo)).thenReturn(usuario);
            when(alertaRepo.findByUnidadeDestino_Codigo(codUnidade)).thenReturn(List.of(alerta));
            when(alertaUsuarioRepo.findByUsuarioAndAlertas(eq(usuarioTitulo), anyList())).thenReturn(List.of());
            when(alertaMapper.toDto(eq(alerta), any())).thenReturn(AlertaDto.builder().codigo(100L).build());

            // When
            List<AlertaDto> resultado = service.listarAlertasPorUsuario(usuarioTitulo);

            // Then
            assertThat(resultado).hasSize(1);
            // Não deve salvar pois listar agora é somente leitura
            verify(alertaUsuarioRepo, never()).save(any());
        }

        @Test
        @DisplayName("Deve listar com dataHoraLeitura se AlertaUsuario existente")
        void deveListarComDataHoraLeitura() {
            // Given
            String usuarioTitulo = "123";
            Long codUnidade = 1L;

            Unidade unidade = new Unidade();
            unidade.setCodigo(codUnidade);

            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral(usuarioTitulo);
            usuario.setUnidadeLotacao(unidade);

            Alerta alerta = new Alerta();
            alerta.setCodigo(100L);

            AlertaUsuario alertaUsuarioExistente = new AlertaUsuario();
            alertaUsuarioExistente.setId(AlertaUsuario.Chave.builder().alertaCodigo(100L).usuarioTitulo(usuarioTitulo).build());
            alertaUsuarioExistente.setAlerta(alerta);
            alertaUsuarioExistente.setDataHoraLeitura(LocalDateTime.now());

            when(usuarioService.buscarPorId(usuarioTitulo)).thenReturn(usuario);
            when(alertaRepo.findByUnidadeDestino_Codigo(codUnidade)).thenReturn(List.of(alerta));
            when(alertaUsuarioRepo.findByUsuarioAndAlertas(eq(usuarioTitulo), anyList())).thenReturn(List.of(alertaUsuarioExistente));
            when(alertaMapper.toDto(eq(alerta), any())).thenReturn(AlertaDto.builder().codigo(100L).build());

            // When
            List<AlertaDto> resultado = service.listarAlertasPorUsuario(usuarioTitulo);

            // Then
            assertThat(resultado).hasSize(1);
            verify(alertaUsuarioRepo, never()).save(any());
        }

        @Test
        @DisplayName("Deve retornar vazio se sem alertas")
        void deveRetornarVazioSeSemAlertas() {
            String usuarioTitulo = "123";
            Long codUnidade = 1L;

            Unidade unidade = new Unidade();
            unidade.setCodigo(codUnidade);

            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral(usuarioTitulo);
            usuario.setUnidadeLotacao(unidade);

            when(usuarioService.buscarPorId(usuarioTitulo)).thenReturn(usuario);
            when(alertaRepo.findByUnidadeDestino_Codigo(codUnidade)).thenReturn(List.of());

            List<AlertaDto> resultado = service.listarAlertasPorUsuario(usuarioTitulo);

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("Listagens Paginadas e Outros")
    @SuppressWarnings("unused")
    class OutrosMetodos {
        @Test
        @DisplayName("Listar por unidade paginado")
        void listarPorUnidadePaginado() {
            Pageable p = Pageable.unpaged();
            when(alertaRepo.findByUnidadeDestino_Codigo(1L, p)).thenReturn(Page.empty());
            assertThat(service.listarPorUnidade(1L, p)).isEmpty();
        }

        @Test
        @DisplayName("Obter data hora leitura")
        void obterDataHoraLeitura() {
            AlertaUsuario au = new AlertaUsuario();
            au.setDataHoraLeitura(LocalDateTime.now());
            when(alertaUsuarioRepo.findById(any())).thenReturn(Optional.of(au));
            assertThat(service.obterDataHoraLeitura(1L, "user")).isPresent();
        }
    }

    @Nested
    @DisplayName("Método: marcarComoLidos")
    @SuppressWarnings("unused")
    class MarcarComoLidos {
        @Test
        @DisplayName("Deve ignorar alerta inexistente")
        void deveIgnorarAlertaInexistente() {
            String titulo = "123";
            Long codigoInexistente = 999L;
            Usuario usuario = new Usuario();

            when(usuarioService.buscarPorId(titulo)).thenReturn(usuario);
            when(alertaUsuarioRepo.findById(any())).thenReturn(Optional.empty());
            when(alertaRepo.findById(codigoInexistente)).thenReturn(Optional.empty());

            service.marcarComoLidos(titulo, List.of(codigoInexistente));

            verify(alertaUsuarioRepo, never()).save(any());
        }

        @Test
        @DisplayName("Deve criar novo AlertaUsuario se não existir")
        void deveCriarNovoSeNaoExistir() {
            String titulo = "123";
            Long codigo = 100L;
            Usuario usuario = new Usuario();
            Alerta alerta = new Alerta();

            when(usuarioService.buscarPorId(titulo)).thenReturn(usuario);
            when(alertaUsuarioRepo.findById(any())).thenReturn(Optional.empty());
            when(alertaRepo.findById(codigo)).thenReturn(Optional.of(alerta));

            service.marcarComoLidos(titulo, List.of(codigo));

            ArgumentCaptor<AlertaUsuario> captor = ArgumentCaptor.forClass(AlertaUsuario.class);
            verify(alertaUsuarioRepo).save(captor.capture());

            AlertaUsuario salvo = captor.getValue();
            assertThat(salvo.getDataHoraLeitura()).isNotNull();
            assertThat(salvo.getUsuario()).isEqualTo(usuario);
            assertThat(salvo.getAlerta()).isEqualTo(alerta);
        }

        @Test
        @DisplayName("Deve marcar como lido se existente e não lido")
        void deveMarcarComoLidoSeExistenteNaoLido() {
            String titulo = "123";
            Long codigo = 100L;
            Usuario usuario = new Usuario();

            AlertaUsuario existente = new AlertaUsuario();
            existente.setDataHoraLeitura(null);
            existente.setUsuario(usuario);

            when(usuarioService.buscarPorId(titulo)).thenReturn(usuario);
            when(alertaUsuarioRepo.findById(any())).thenReturn(Optional.of(existente));

            service.marcarComoLidos(titulo, List.of(codigo));

            verify(alertaUsuarioRepo).save(existente);
            assertThat(existente.getDataHoraLeitura()).isNotNull();
        }

        @Test
        @DisplayName("Deve não fazer nada se já lido")
        void deveNaoFazerNadaSeJaLido() {
            String titulo = "123";
            Long codigo = 100L;
            Usuario usuario = new Usuario();

            AlertaUsuario existente = new AlertaUsuario();
            existente.setDataHoraLeitura(LocalDateTime.now());
            existente.setUsuario(usuario);

            when(usuarioService.buscarPorId(titulo)).thenReturn(usuario);
            when(alertaUsuarioRepo.findById(any())).thenReturn(Optional.of(existente));

            service.marcarComoLidos(titulo, List.of(codigo));

            verify(alertaUsuarioRepo, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Método: listarAlertasNaoLidos")
    @SuppressWarnings("unused")
    class ListarAlertasNaoLidos {
        @Test
        @DisplayName("Deve filtrar alertas lidos")
        void deveFiltrarAlertasLidos() {
            String titulo = "123";
            Unidade u = new Unidade();
            u.setCodigo(1L);
            Usuario usuario = new Usuario();
            usuario.setUnidadeLotacao(u);

            Alerta a1 = new Alerta();
            a1.setCodigo(1L);
            Alerta a2 = new Alerta();
            a2.setCodigo(2L);

            when(usuarioService.buscarPorId(titulo)).thenReturn(usuario);
            when(alertaRepo.findByUnidadeDestino_Codigo(1L)).thenReturn(List.of(a1, a2));

            // a1 lido, a2 nao lido
            AlertaUsuario au1 = new AlertaUsuario();
            au1.setId(AlertaUsuario.Chave.builder().alertaCodigo(1L).usuarioTitulo(titulo).build());
            au1.setDataHoraLeitura(LocalDateTime.now());
            when(alertaUsuarioRepo.findByUsuarioAndAlertas(eq(titulo), anyList())).thenReturn(List.of(au1));

            // Mocks do mapper
            AlertaDto dto1 = AlertaDto.builder().codigo(1L).dataHoraLeitura(LocalDateTime.now()).build();
            AlertaDto dto2 = AlertaDto.builder().codigo(2L).dataHoraLeitura(null).build();

            when(alertaMapper.toDto(eq(a1), any())).thenReturn(dto1);
            when(alertaMapper.toDto(eq(a2), any())).thenReturn(dto2);

            List<AlertaDto> result = service.listarAlertasNaoLidos(titulo);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getCodigo()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("Método: getSedoc (Lazy Load)")
    @SuppressWarnings("unused")
    class GetSedoc {
        @Test
        @DisplayName("Deve inicializar sedoc apenas na primeira chamada (lazy load)")
        void deveInicializarSedocLazyLoad() {
            Unidade sedocMock = new Unidade();
            sedocMock.setSigla("SEDOC");

            when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(sedocMock);
            when(alertaRepo.save(any())).thenReturn(new Alerta());

            // Primeira chamada: deve buscar sedoc
            service.criarAlertaSedoc(new Processo(), new Unidade(), "Teste");
            verify(unidadeService).buscarEntidadePorSigla("SEDOC");

            // Segunda chamada: não deve buscar novamente (cache)
            service.criarAlertaSedoc(new Processo(), new Unidade(), "Teste 2");
            verify(unidadeService, times(1)).buscarEntidadePorSigla("SEDOC");
        }
    }
}
