package sgc.alerta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.processo.Processo;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.Subprocesso;
import sgc.unidade.Unidade;
import sgc.unidade.UnidadeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaServiceImplTest {

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private AlertaUsuarioRepository alertaUsuarioRepository;

    @Mock
    private UnidadeRepository unidadeRepository;

    @Mock
    private SgrhService sgrhService;

    @InjectMocks
    private AlertaServiceImpl alertaService;

    private Processo processo;
    private Unidade unidadeDestino;
    private ResponsavelDto responsavelDto;

    @BeforeEach
    void setUp() {
        processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Teste de Processo");

        unidadeDestino = new Unidade();
        unidadeDestino.setCodigo(10L);
        unidadeDestino.setSigla("UND");

        responsavelDto = new ResponsavelDto(10L, "Gestor", "12345", "Substituto", "54321");
    }

    @Test
    @DisplayName("Deve criar alerta e notificar titular e substituto quando houver responsável")
    void criarAlerta_ComResponsavelCompleto_DeveSalvarAlertaEAlertasUsuario() {
        // Given
        when(unidadeRepository.findById(10L)).thenReturn(Optional.of(unidadeDestino));
        when(sgrhService.buscarResponsavelUnidade(10L)).thenReturn(Optional.of(responsavelDto));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> {
            Alerta alerta = invocation.getArgument(0);
            alerta.setCodigo(99L); // Simula a persistência e atribuição de um ID
            return alerta;
        });

        // When
        Alerta alertaSalvo = alertaService.criarAlerta(
                processo, "TESTE", 10L, "Descrição de teste", LocalDate.now());

        // Then
        assertNotNull(alertaSalvo);
        assertEquals(processo, alertaSalvo.getProcesso());
        assertEquals(unidadeDestino, alertaSalvo.getUnidadeDestino());

        verify(alertaRepository, times(1)).save(any(Alerta.class));
        verify(alertaUsuarioRepository, times(2)).save(any(AlertaUsuario.class));
    }

    @Test
    @DisplayName("Deve criar alerta apenas para o titular quando não houver substituto")
    void criarAlerta_ComApenasTitular_DeveSalvarAlertaEUmAlertaUsuario() {
        // Given
        ResponsavelDto titularApenas = new ResponsavelDto(10L, "Gestor", "12345", null, null);
        when(unidadeRepository.findById(10L)).thenReturn(Optional.of(unidadeDestino));
        when(sgrhService.buscarResponsavelUnidade(10L)).thenReturn(Optional.of(titularApenas));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> {
            Alerta alerta = invocation.getArgument(0);
            alerta.setCodigo(99L);
            return alerta;
        });

        // When
        alertaService.criarAlerta(processo, "TESTE", 10L, "Descrição", LocalDate.now());

        // Then
        verify(alertaRepository, times(1)).save(any(Alerta.class));
        verify(alertaUsuarioRepository, times(1)).save(any(AlertaUsuario.class));
    }

    @Test
    @DisplayName("Deve criar alerta mas não notificar ninguém se SGRH não retornar responsável")
    void criarAlerta_SemResponsavel_DeveSalvarApenasAlerta() {
        // Given
        when(unidadeRepository.findById(10L)).thenReturn(Optional.of(unidadeDestino));
        when(sgrhService.buscarResponsavelUnidade(10L)).thenReturn(Optional.empty());
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        alertaService.criarAlerta(processo, "TESTE", 10L, "Descrição", LocalDate.now());

        // Then
        verify(alertaRepository, times(1)).save(any(Alerta.class));
        verify(alertaUsuarioRepository, never()).save(any(AlertaUsuario.class));
    }

    @Test
    @DisplayName("Deve criar alerta mesmo que SGRH lance uma exceção")
    void criarAlerta_ComErroNoSgrh_DeveSalvarApenasAlerta() {
        // Given
        when(unidadeRepository.findById(10L)).thenReturn(Optional.of(unidadeDestino));
        when(sgrhService.buscarResponsavelUnidade(10L)).thenThrow(new RuntimeException("Erro de comunicação com SGRH"));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        alertaService.criarAlerta(processo, "TESTE", 10L, "Descrição", LocalDate.now());

        // Then
        verify(alertaRepository, times(1)).save(any(Alerta.class));
        verify(alertaUsuarioRepository, never()).save(any(AlertaUsuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar alerta para unidade inexistente")
    void criarAlerta_UnidadeInexistente_DeveLancarIllegalArgumentException() {
        // Given
        when(unidadeRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> alertaService.criarAlerta(processo, "TESTE", 999L, "Descrição", LocalDate.now()));

        verify(alertaRepository, never()).save(any());
    }

    // Testes para criarAlertasProcessoIniciado

    @Test
    @DisplayName("Deve retornar um alerta para unidade OPERACIONAL")
    void criarAlertasProcessoIniciado_UnidadeOperacional_DeveRetornarUmAlerta() {
        // Given
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidadeDestino);
        subprocesso.setDataLimiteEtapa1(LocalDate.now());

        UnidadeDto unidadeDto = new UnidadeDto(10L, "Unidade Operacional", "UND-OP", null, "OPERACIONAL");
        when(sgrhService.buscarUnidadePorCodigo(10L)).thenReturn(Optional.of(unidadeDto));
        when(unidadeRepository.findById(10L)).thenReturn(Optional.of(unidadeDestino));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        List<Alerta> alertas = alertaService.criarAlertasProcessoIniciado(processo, List.of(subprocesso));

        // Then
        assertEquals(1, alertas.size());
    }

    @Test
    @DisplayName("Deve retornar um alerta para unidade INTERMEDIARIA")
    void criarAlertasProcessoIniciado_UnidadeIntermediaria_DeveRetornarUmAlerta() {
        // Given
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidadeDestino);
        subprocesso.setDataLimiteEtapa1(LocalDate.now());

        UnidadeDto unidadeDto = new UnidadeDto(10L, "Unidade Intermediaria", "UND-INT", null, "INTERMEDIARIA");
        when(sgrhService.buscarUnidadePorCodigo(10L)).thenReturn(Optional.of(unidadeDto));
        when(unidadeRepository.findById(10L)).thenReturn(Optional.of(unidadeDestino));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        List<Alerta> alertas = alertaService.criarAlertasProcessoIniciado(processo, List.of(subprocesso));

        // Then
        assertEquals(1, alertas.size());
    }

    @Test
    @DisplayName("Deve retornar dois alertas para unidade INTEROPERACIONAL")
    void criarAlertasProcessoIniciado_UnidadeInteroperacional_DeveRetornarDoisAlertas() {
        // Given
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidadeDestino);
        subprocesso.setDataLimiteEtapa1(LocalDate.now());

        UnidadeDto unidadeDto = new UnidadeDto(10L, "Unidade Interoperacional", "UND-INTEROP", null, "INTEROPERACIONAL");
        when(sgrhService.buscarUnidadePorCodigo(10L)).thenReturn(Optional.of(unidadeDto));
        when(unidadeRepository.findById(10L)).thenReturn(Optional.of(unidadeDestino));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        List<Alerta> alertas = alertaService.criarAlertasProcessoIniciado(processo, List.of(subprocesso));

        // Then
        assertEquals(2, alertas.size());
    }
}