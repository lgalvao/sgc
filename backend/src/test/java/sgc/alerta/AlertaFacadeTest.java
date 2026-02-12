package sgc.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.mapper.AlertaMapper;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaUsuario;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
    private AlertaService alertaService;

    @Mock
    private UsuarioFacade usuarioService;

    @Mock
    private AlertaMapper alertaMapper;

    @Mock
    private UnidadeFacade unidadeService;

    @InjectMocks
    private AlertaFacade service;

    private void criarUnidadeRaizMock() {
        Unidade unidadeRaiz = new Unidade();
        unidadeRaiz.setCodigo(1L);
        unidadeRaiz.setSigla("ADMIN");
        when(unidadeService.buscarEntidadePorId(1L)).thenReturn(unidadeRaiz);
    }

    @Nested
    @DisplayName("Método: criarAlerta")
    class CriarAlerta {
        @Test
        @DisplayName("Deve criar alerta com sucesso")
        void deveCriarAlertaComSucesso() {
            // Given
            criarUnidadeRaizMock();
            Processo p = new Processo();
            Unidade u = new Unidade();
            u.setCodigo(1L);

            Alerta alertaSalvo = new Alerta();
            alertaSalvo.setCodigo(100L);
            when(alertaService.salvar(any())).thenReturn(alertaSalvo);

            // When
            Alerta resultado = service.criarAlertaAdmin(p, u, "desc");

            // Then
            assertThat(resultado).isNotNull();
            verify(alertaService).salvar(any());
        }
    }

    @Nested
    @DisplayName("Método: criarAlertasProcessoIniciado")
    class CriarAlertasProcessoIniciado {
        @Test
        @DisplayName("Deve criar alerta operacional com texto fixo")
        void deveCriarAlertaOperacional() {
            // Given
            criarUnidadeRaizMock();
            Processo p = new Processo();
            p.setDescricao("Proc");
            Unidade u = new Unidade();
            u.setCodigo(1L);
            u.setTipo(TipoUnidade.OPERACIONAL);

            when(alertaService.salvar(any())).thenAnswer(i -> i.getArgument(0));

            // When
            service.criarAlertasProcessoIniciado(p, List.of(u));

            // Then
            verify(alertaService).salvar(argThat(a -> "Início do processo".equals(a.getDescricao())));
        }

        @Test
        @DisplayName("Deve criar alerta para unidade participante e seus ancestrais")
        void deveCriarAlertaParaAncestrais() {
            // Given
            criarUnidadeRaizMock();
            Processo p = new Processo();

            Unidade root = Unidade.builder().nome("Root").tipo(TipoUnidade.INTEROPERACIONAL).build();
            root.setCodigo(1L);

            Unidade filho = Unidade.builder().nome("Filho").tipo(TipoUnidade.OPERACIONAL).build();
            filho.setCodigo(2L);
            filho.setUnidadeSuperior(root);

            when(alertaService.salvar(any())).thenAnswer(i -> i.getArgument(0));

            // When
            service.criarAlertasProcessoIniciado(p, List.of(filho));

            // Then
            // 1 alerta operacional para o filho
            verify(alertaService).salvar(argThat(a -> "Início do processo".equals(a.getDescricao())
                    && a.getUnidadeDestino().getCodigo().equals(2L)));

            // 1 alerta intermediário para o pai (root)
            verify(alertaService).salvar(argThat(a -> "Início do processo em unidades subordinadas".equals(a.getDescricao())
                    && a.getUnidadeDestino().getCodigo().equals(1L)));
        }

        @Test
        @DisplayName("Deve criar 2 alertas para interoperacional participante")
        void deveCriarDoisAlertasInteroperacional() {
            // Given
            criarUnidadeRaizMock();
            Processo p = new Processo();
            Unidade u = Unidade.builder().tipo(TipoUnidade.INTEROPERACIONAL).build();
            u.setCodigo(1L);

            when(alertaService.salvar(any())).thenAnswer(i -> i.getArgument(0));

            // When
            List<Alerta> resultado = service.criarAlertasProcessoIniciado(p, List.of(u));

            // Then
            assertThat(resultado).hasSize(2);
            verify(alertaService, times(2)).salvar(any());
        }
    }

    @Nested
    @DisplayName("Métodos: criação de alertas específicos")
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

            when(alertaService.salvar(any())).thenAnswer(i -> i.getArgument(0));

            // When
            service.criarAlertaCadastroDisponibilizado(p, uOrigem, uDestino);

            // Then
            verify(alertaService).salvar(any());
        }

        @Test
        @DisplayName("Deve criar alerta de cadastro devolvido com sucesso")
        void deveCriarAlertaCadastroDevolvido() {
            // Given
            criarUnidadeRaizMock();
            Processo p = new Processo();
            p.setDescricao("P");
            Unidade uDestino = new Unidade();

            when(alertaService.salvar(any())).thenAnswer(i -> i.getArgument(0));

            // When
            service.criarAlertaCadastroDevolvido(p, uDestino, "motivo");

            // Then
            verify(alertaService).salvar(any());
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
            when(alertaService.salvar(any())).thenAnswer(i -> i.getArgument(0));

            Alerta resultado = service.criarAlertaTransicao(p, "desc", uOrigem, uDestino);
            assertThat(resultado).isNotNull();
            verify(alertaService).salvar(any());
        }

        @Test
        @DisplayName("Deve criar alerta de alteração de data limite")
        void deveCriarAlertaAlteracaoDataLimite() {
            criarUnidadeRaizMock();
            Processo p = new Processo();
            Unidade uDestino = new Unidade();
            when(alertaService.salvar(any())).thenAnswer(i -> i.getArgument(0));

            service.criarAlertaAlteracaoDataLimite(p, uDestino, "20/10/2023", 1);
            verify(alertaService).salvar(any());
        }

        @Test
        @DisplayName("Deve criar alertas de reabertura")
        void deveCriarAlertasDeReabertura() {
            criarUnidadeRaizMock();
            Processo p = new Processo();
            Unidade u = new Unidade();
            u.setSigla("U1");
            Unidade sup = new Unidade();

            when(alertaService.salvar(any())).thenAnswer(i -> i.getArgument(0));

            service.criarAlertaReaberturaCadastro(p, u, "Justificativa Teste");
            service.criarAlertaReaberturaCadastroSuperior(p, sup, u);
            service.criarAlertaReaberturaRevisao(p, u, "Justificativa Teste");
            service.criarAlertaReaberturaRevisaoSuperior(p, sup, u);

            verify(alertaService, times(4)).salvar(any());
        }
    }

    @Nested
    @DisplayName("Método: listarAlertasPorUsuario")
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
            when(alertaService.buscarPorUnidadeDestino(codUnidade)).thenReturn(List.of(alerta));
            when(alertaService.buscarPorUsuarioEAlertas(eq(usuarioTitulo), anyList())).thenReturn(List.of());
            when(alertaMapper.toDto(eq(alerta), any())).thenReturn(AlertaDto.builder().codigo(100L).build());

            // When
            List<AlertaDto> resultado = service.listarAlertasPorUsuario(usuarioTitulo);

            // Then
            assertThat(resultado).hasSize(1);
            // Não deve salvar pois listar agora é somente leitura
            verify(alertaService, never()).salvarAlertaUsuario(any());
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
            when(alertaService.buscarPorUnidadeDestino(codUnidade)).thenReturn(List.of(alerta));
            when(alertaService.buscarPorUsuarioEAlertas(eq(usuarioTitulo), anyList())).thenReturn(List.of(alertaUsuarioExistente));
            when(alertaMapper.toDto(eq(alerta), any())).thenReturn(AlertaDto.builder().codigo(100L).build());

            // When
            List<AlertaDto> resultado = service.listarAlertasPorUsuario(usuarioTitulo);

            // Then
            assertThat(resultado).hasSize(1);
            verify(alertaService, never()).salvarAlertaUsuario(any());
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
            when(alertaService.buscarPorUnidadeDestino(codUnidade)).thenReturn(List.of());

            List<AlertaDto> resultado = service.listarAlertasPorUsuario(usuarioTitulo);

            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("Listagens Paginadas e Outros")
    class OutrosMetodos {
        @Test
        @DisplayName("Listar por unidade paginado")
        void listarPorUnidadePaginado() {
            Pageable p = Pageable.unpaged();
            when(alertaService.buscarPorUnidadeDestino(1L, p)).thenReturn(Page.empty());
            assertThat(service.listarPorUnidade(1L, p)).isEmpty();
        }

        @Test
        @DisplayName("Obter data hora leitura")
        void obterDataHoraLeitura() {
            AlertaUsuario au = new AlertaUsuario();
            au.setDataHoraLeitura(LocalDateTime.now());
            when(alertaService.obterDataHoraLeitura(1L, "user")).thenReturn(Optional.of(au.getDataHoraLeitura()));
            assertThat(service.obterDataHoraLeitura(1L, "user")).isPresent();
        }
    }

    @Nested
    @DisplayName("Método: marcarComoLidos")
    class MarcarComoLidos {
        @Test
        @DisplayName("Deve ignorar alerta inexistente")
        void deveIgnorarAlertaInexistente() {
            String titulo = "123";
            Long codigoInexistente = 999L;
            Usuario usuario = new Usuario();

            when(usuarioService.buscarPorId(titulo)).thenReturn(usuario);
            when(alertaService.buscarAlertaUsuario(any())).thenReturn(Optional.empty());
            when(alertaService.buscarPorCodigo(codigoInexistente)).thenReturn(Optional.empty());

            service.marcarComoLidos(titulo, List.of(codigoInexistente));

            verify(alertaService, never()).salvarAlertaUsuario(any());
        }

        @Test
        @DisplayName("Deve criar novo AlertaUsuario se não existir")
        void deveCriarNovoSeNaoExistir() {
            String titulo = "123";
            Long codigo = 100L;
            Usuario usuario = new Usuario();
            Alerta alerta = new Alerta();

            when(usuarioService.buscarPorId(titulo)).thenReturn(usuario);
            when(alertaService.buscarAlertaUsuario(any())).thenReturn(Optional.empty());
            when(alertaService.buscarPorCodigo(codigo)).thenReturn(Optional.of(alerta));

            service.marcarComoLidos(titulo, List.of(codigo));

            ArgumentCaptor<AlertaUsuario> captor = ArgumentCaptor.forClass(AlertaUsuario.class);
            verify(alertaService).salvarAlertaUsuario(captor.capture());

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
            when(alertaService.buscarAlertaUsuario(any())).thenReturn(Optional.of(existente));

            service.marcarComoLidos(titulo, List.of(codigo));

            verify(alertaService).salvarAlertaUsuario(existente);
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
            when(alertaService.buscarAlertaUsuario(any())).thenReturn(Optional.of(existente));

            service.marcarComoLidos(titulo, List.of(codigo));

            verify(alertaService, never()).salvarAlertaUsuario(any());
        }
    }

    @Nested
    @DisplayName("Método: listarAlertasNaoLidos")
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
            when(alertaService.buscarPorUnidadeDestino(1L)).thenReturn(List.of(a1, a2));

            // a1 lido, a2 nao lido
            AlertaUsuario au1 = new AlertaUsuario();
            au1.setId(AlertaUsuario.Chave.builder().alertaCodigo(1L).usuarioTitulo(titulo).build());
            au1.setDataHoraLeitura(LocalDateTime.now());
            when(alertaService.buscarPorUsuarioEAlertas(eq(titulo), anyList())).thenReturn(List.of(au1));

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
    @DisplayName("Método: getUnidadeRaiz (Lazy Load)")
    class GetUnidadeRaiz {
    @Test
        @DisplayName("Deve inicializar unidadeRaiz apenas na primeira chamada (lazy load)")
        void deveInicializarUnidadeRaizLazyLoad() {
            Unidade unidadeRaizMock = new Unidade();
            unidadeRaizMock.setSigla("ADMIN");
            unidadeRaizMock.setCodigo(1L);

            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(unidadeRaizMock);
            when(alertaService.salvar(any())).thenAnswer(i -> i.getArgument(0));

            // Primeira chamada: deve buscar unidadeRaiz
            service.criarAlertaAdmin(new Processo(), new Unidade(), "Teste");
            verify(unidadeService).buscarEntidadePorId(1L);

            // Segunda chamada: não deve buscar novamente (cache)
            service.criarAlertaAdmin(new Processo(), new Unidade(), "Teste 2");
            // No mockito, verify acumula as chamadas, mas como é lazy, a segunda chamada não deve invocar o metodo no service.
            // Então verify(times(1)) deve ser correto para o total.
            verify(unidadeService, times(1)).buscarEntidadePorId(1L);
        }
    }
}
