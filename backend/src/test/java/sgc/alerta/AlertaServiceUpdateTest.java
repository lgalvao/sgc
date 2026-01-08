package sgc.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.dto.AlertaMapper;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuario;
import sgc.alerta.model.AlertaUsuarioRepo;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Adicionais: AlertaService")
class AlertaServiceUpdateTest {

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

    @BeforeEach
    void setUp() {
        service = new AlertaService(alertaRepo, alertaUsuarioRepo, usuarioService, alertaMapper, unidadeService);
    }

    @Test
    @DisplayName("marcarComoLidos: deve ignorar alerta inexistente")
    void marcarComoLidosIgnoraAlertaInexistente() {
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
    @DisplayName("marcarComoLidos: deve criar novo AlertaUsuario se não existir")
    void marcarComoLidosCriaNovo() {
        String titulo = "123";
        Long codigo = 100L;
        Usuario usuario = new Usuario();
        Alerta alerta = new Alerta();

        when(usuarioService.buscarPorId(titulo)).thenReturn(usuario);
        // Primeira chamada vazio (não existe), segunda chamada retorna vazio também para forçar a lógica do orElseGet
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
    @DisplayName("listarAlertasNaoLidos: deve filtrar alertas lidos")
    void listarAlertasNaoLidos() {
        String titulo = "123";
        Unidade u = new Unidade();
        u.setCodigo(1L);
        Usuario usuario = new Usuario();
        usuario.setUnidadeLotacao(u);

        Alerta a1 = new Alerta(); a1.setCodigo(1L);
        Alerta a2 = new Alerta(); a2.setCodigo(2L);

        when(usuarioService.buscarPorId(titulo)).thenReturn(usuario);
        when(alertaRepo.findByUnidadeDestino_Codigo(1L)).thenReturn(List.of(a1, a2));

        // a1 lido, a2 nao lido
        when(alertaUsuarioRepo.findById(eq(new AlertaUsuario.Chave(1L, titulo))))
            .thenReturn(Optional.of(new AlertaUsuario().setDataHoraLeitura(LocalDateTime.now())));
        when(alertaUsuarioRepo.findById(eq(new AlertaUsuario.Chave(2L, titulo))))
            .thenReturn(Optional.empty());

        // Mocks do mapper
        AlertaDto dto1 = AlertaDto.builder().codigo(1L).dataHoraLeitura(LocalDateTime.now()).build();
        AlertaDto dto2 = AlertaDto.builder().codigo(2L).dataHoraLeitura(null).build();

        when(alertaMapper.toDto(eq(a1), any())).thenReturn(dto1);
        when(alertaMapper.toDto(eq(a2), any())).thenReturn(dto2);

        List<AlertaDto> result = service.listarAlertasNaoLidos(titulo);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCodigo()).isEqualTo(2L);
    }

    @Test
    @DisplayName("getSedoc: deve inicializar sedoc se nulo")
    void getSedocLazyLoad() {
        // Para testar o lazy load do campo privado 'sedoc', precisamos garantir que ele começa nulo.
        // Como o serviço é instanciado a cada teste pelo @InjectMocks, ele deve estar nulo.
        // Precisamos chamar um método que use getSedoc(), como criarAlertaSedoc.

        Unidade sedocMock = new Unidade();
        sedocMock.setSigla("SEDOC");

        when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(sedocMock);
        when(alertaRepo.save(any())).thenReturn(new Alerta());

        service.criarAlertaSedoc(new Processo(), new Unidade(), "Teste");

        verify(unidadeService).buscarEntidadePorSigla("SEDOC");

        // Segunda chamada não deve buscar novamente
        service.criarAlertaSedoc(new Processo(), new Unidade(), "Teste 2");
        verify(unidadeService, times(1)).buscarEntidadePorSigla("SEDOC");
    }
}
