package sgc.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.dto.AlertaMapper;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuario;
import sgc.alerta.model.AlertaUsuarioRepo;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários: AlertaService")
class AlertaServiceTest {
    @Mock
    private AlertaRepo alertaRepo;

    @Mock
    private AlertaUsuarioRepo alertaUsuarioRepo;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private AlertaMapper alertaMapper;

    @Mock
    private UnidadeService unidadeService;

    @InjectMocks
    private AlertaService service;

    private Unidade criarSedocMock() {
        Unidade sedoc = new Unidade();
        sedoc.setCodigo(15L);
        sedoc.setSigla("SEDOC");
        when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(sedoc);
        return sedoc;
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

            service.criarAlertaReaberturaCadastro(p, u);
            service.criarAlertaReaberturaCadastroSuperior(p, sup, u);
            service.criarAlertaReaberturaRevisao(p, u);
            service.criarAlertaReaberturaRevisaoSuperior(p, sup, u);

            verify(alertaRepo, times(4)).save(any());
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
            when(alertaRepo.findByUnidadeDestino_Codigo(codUnidade)).thenReturn(List.of(alerta));
            when(alertaUsuarioRepo.findById(any())).thenReturn(Optional.empty());
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
            alertaUsuarioExistente.setAlerta(alerta);
            alertaUsuarioExistente.setDataHoraLeitura(LocalDateTime.now());

            when(usuarioService.buscarPorId(usuarioTitulo)).thenReturn(usuario);
            when(alertaRepo.findByUnidadeDestino_Codigo(codUnidade)).thenReturn(List.of(alerta));
            when(alertaUsuarioRepo.findById(any())).thenReturn(Optional.of(alertaUsuarioExistente));
            when(alertaMapper.toDto(eq(alerta), any())).thenReturn(AlertaDto.builder().codigo(100L).build());

            // When
            List<AlertaDto> resultado = service.listarAlertasPorUsuario(usuarioTitulo);

            // Then
            assertThat(resultado).hasSize(1);
            verify(alertaUsuarioRepo, never()).save(any());
        }

        @Test
        @DisplayName("Deve retornar vazio se usuario nao tem unidade de lotacao")
        void deveRetornarVazioSeSemLotacao() {
            Usuario u = new Usuario();
            u.setUnidadeLotacao(null);
            when(usuarioService.buscarPorId("123")).thenReturn(u);
            assertThat(service.listarAlertasPorUsuario("123")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Listagens Paginadas e Outros")
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
}
