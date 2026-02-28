package sgc.alerta;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.data.domain.*;
import sgc.alerta.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários consolidados do AlertaFacade.
 * <p>
 * Consolida testes de:
 * - AlertaServiceTest.java (15 testes)
 * - AlertaServiceUpdateTest.java (4 testes)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaFacade - Testes Unitários")
class AlertaFacadeTest {
    @Mock
    private AlertaService alertaService;

    @Mock
    private UsuarioFacade usuarioService;

    @Mock
    private OrganizacaoFacade unidadeService;

    @InjectMocks
    private AlertaFacade service;

    private void criarUnidadeRaizMock() {
        Unidade unidadeRaiz = new Unidade();
        unidadeRaiz.setCodigo(1L);
        unidadeRaiz.setSigla("ADMIN");
        when(unidadeService.unidadePorCodigo(1L)).thenReturn(unidadeRaiz);
    }

    @Nested
    @DisplayName("Método: criarAlerta")
    class CriarAlerta {
        @Test
        @DisplayName("Deve criar alerta com sucesso")
        void deveCriarAlertaComSucesso() {

            criarUnidadeRaizMock();
            Processo p = new Processo();
            Unidade u = new Unidade();
            u.setCodigo(1L);

            Alerta alertaSalvo = new Alerta();
            alertaSalvo.setCodigo(100L);
            when(alertaService.salvar(any())).thenReturn(alertaSalvo);


            Alerta resultado = service.criarAlertaAdmin(p, u, "desc");


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

            criarUnidadeRaizMock();
            Processo p = new Processo();
            p.setDescricao("Proc");
            Unidade u = new Unidade();
            u.setCodigo(1L);
            u.setTipo(TipoUnidade.OPERACIONAL);

            when(alertaService.salvar(any())).thenAnswer(i -> i.getArgument(0));


            service.criarAlertasProcessoIniciado(p, List.of(u));


            verify(alertaService).salvar(argThat(a -> "Início do processo".equals(a.getDescricao())));
        }

        @Test
        @DisplayName("Deve criar 2 alertas para interoperacional participante")
        void deveCriarDoisAlertasInteroperacional() {

            criarUnidadeRaizMock();
            Processo p = new Processo();
            Unidade u = Unidade.builder().tipo(TipoUnidade.INTEROPERACIONAL).build();
            u.setCodigo(1L);

            when(alertaService.salvar(any())).thenAnswer(i -> i.getArgument(0));


            List<Alerta> resultado = service.criarAlertasProcessoIniciado(p, List.of(u));


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

            Processo p = new Processo();
            p.setDescricao("P");
            Unidade uOrigem = new Unidade();
            uOrigem.setSigla("UO");
            Unidade uDestino = new Unidade();

            when(alertaService.salvar(any())).thenAnswer(i -> i.getArgument(0));


            service.criarAlertaCadastroDisponibilizado(p, uOrigem, uDestino);


            verify(alertaService).salvar(any());
        }

        @Test
        @DisplayName("Deve criar alerta de cadastro devolvido com sucesso")
        void deveCriarAlertaCadastroDevolvido() {

            criarUnidadeRaizMock();
            Processo p = new Processo();
            p.setDescricao("P");
            Unidade uDestino = new Unidade();

            when(alertaService.salvar(any())).thenAnswer(i -> i.getArgument(0));


            service.criarAlertaCadastroDevolvido(p, uDestino, "motivo");


            verify(alertaService).salvar(any());
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

            String usuarioTitulo = "123";
            Long codUnidade = 1L;

            Unidade unidade = new Unidade();
            unidade.setCodigo(codUnidade);

            Usuario usuario = new Usuario();
            usuario.setTituloEleitoral(usuarioTitulo);
            usuario.setUnidadeLotacao(unidade);

            Alerta alerta = new Alerta();
            alerta.setCodigo(100L);

            when(usuarioService.buscarPorTitulo(usuarioTitulo)).thenReturn(usuario);
            when(alertaService.porUnidadeDestino(codUnidade)).thenReturn(List.of(alerta));
            when(alertaService.alertasUsuarios(eq(usuarioTitulo), anyList())).thenReturn(List.of());


            List<Alerta> resultado = service.alertasPorUsuario(usuarioTitulo);


            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().getCodigo()).isEqualTo(100L);
            // Não deve salvar pois listar agora é somente leitura
            verify(alertaService, never()).salvarAlertaUsuario(any());
        }

        @Test
        @DisplayName("Deve listar com dataHoraLeitura se AlertaUsuario existente")
        void deveListarComDataHoraLeitura() {

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

            when(usuarioService.buscarPorTitulo(usuarioTitulo)).thenReturn(usuario);
            when(alertaService.porUnidadeDestino(codUnidade)).thenReturn(List.of(alerta));
            when(alertaService.alertasUsuarios(eq(usuarioTitulo), anyList())).thenReturn(List.of(alertaUsuarioExistente));


            List<Alerta> resultado = service.alertasPorUsuario(usuarioTitulo);


            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().getDataHoraLeitura()).isNotNull();
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

            when(usuarioService.buscarPorTitulo(usuarioTitulo)).thenReturn(usuario);
            when(alertaService.porUnidadeDestino(codUnidade)).thenReturn(List.of());

            List<Alerta> resultado = service.alertasPorUsuario(usuarioTitulo);

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
            when(alertaService.porUnidadeDestinoPaginado(1L, p)).thenReturn(Page.empty());
            assertThat(service.listarPorUnidade(1L, p)).isEmpty();
        }

        @Test
        @DisplayName("Obter data hora leitura")
        void obterDataHoraLeitura() {
            AlertaUsuario au = new AlertaUsuario();
            au.setDataHoraLeitura(LocalDateTime.now());
            when(alertaService.dataHoraLeituraAlertaUsuario(1L, "user")).thenReturn(Optional.of(au.getDataHoraLeitura()));
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

            when(usuarioService.buscarPorTitulo(titulo)).thenReturn(usuario);
            when(alertaService.alertaUsuario(any())).thenReturn(Optional.empty());
            when(alertaService.porCodigo(codigoInexistente)).thenReturn(Optional.empty());

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

            when(usuarioService.buscarPorTitulo(titulo)).thenReturn(usuario);
            when(alertaService.alertaUsuario(any())).thenReturn(Optional.empty());
            when(alertaService.porCodigo(codigo)).thenReturn(Optional.of(alerta));

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

            when(usuarioService.buscarPorTitulo(titulo)).thenReturn(usuario);
            when(alertaService.alertaUsuario(any())).thenReturn(Optional.of(existente));

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

            when(usuarioService.buscarPorTitulo(titulo)).thenReturn(usuario);
            when(alertaService.alertaUsuario(any())).thenReturn(Optional.of(existente));

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

            when(usuarioService.buscarPorTitulo(titulo)).thenReturn(usuario);
            when(alertaService.porUnidadeDestino(1L)).thenReturn(List.of(a1, a2));

            // a1 lido, a2 nao lido
            AlertaUsuario au1 = new AlertaUsuario();
            au1.setId(AlertaUsuario.Chave.builder().alertaCodigo(1L).usuarioTitulo(titulo).build());
            au1.setDataHoraLeitura(LocalDateTime.now());
            when(alertaService.alertasUsuarios(eq(titulo), anyList())).thenReturn(List.of(au1));

            List<Alerta> result = service.listarNaoLidos(titulo);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getCodigo()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("Método: getUnidadeRaiz (Lazy Load)")
    class GetUnidadeRaiz {
        @Test
        @DisplayName("Deve buscar unidadeRaiz para cada operação (sem cache lazy para sistema pequeno)")
        void deveBuscarUnidadeRaizParaCadaOperacao() {
            Unidade unidadeRaizMock = new Unidade();
            unidadeRaizMock.setSigla("ADMIN");
            unidadeRaizMock.setCodigo(1L);

            when(unidadeService.unidadePorCodigo(1L)).thenReturn(unidadeRaizMock);
            when(alertaService.salvar(any())).thenAnswer(i -> i.getArgument(0));

            // Primeira chamada: deve buscar unidadeRaiz
            service.criarAlertaAdmin(new Processo(), new Unidade(), "Teste");
            verify(unidadeService).unidadePorCodigo(1L);

            // Segunda chamada: busca novamente (sem cache lazy - simplificação para sistema pequeno)
            service.criarAlertaAdmin(new Processo(), new Unidade(), "Teste 2");
            verify(unidadeService, times(2)).unidadePorCodigo(1L);
        }
    }
}
