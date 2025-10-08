package sgc.alerta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.alerta.modelo.AlertaUsuario;
import sgc.alerta.modelo.AlertaUsuarioRepo;
import sgc.processo.modelo.Processo;
import sgc.sgrh.SgrhService;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaServiceImplTest {

    @Mock
    private AlertaRepo repositorioAlerta;

    @Mock
    private AlertaUsuarioRepo repositorioAlertaUsuario;

    @Mock
    private UnidadeRepo repositorioUnidade;

    @Mock
    private SgrhService servicoSgrh;

    @InjectMocks
    private AlertaServiceImpl servicoAlerta;

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
    void criarAlerta_comResponsavelCompleto_deveSalvarAlertaEAlertasDeUsuario() {
        when(repositorioUnidade.findById(10L)).thenReturn(Optional.of(unidadeDestino));
        when(servicoSgrh.buscarResponsavelUnidade(10L)).thenReturn(Optional.of(responsavelDto));
        when(repositorioAlerta.save(any(Alerta.class))).thenAnswer(invocation -> {
            Alerta alerta = invocation.getArgument(0);
            alerta.setCodigo(99L);
            return alerta;
        });

        Alerta alertaSalvo = servicoAlerta.criarAlerta(
                processo, "TESTE", 10L, "Descrição de teste", LocalDate.now());

        assertNotNull(alertaSalvo);
        assertEquals(processo, alertaSalvo.getProcesso());
        assertEquals(unidadeDestino, alertaSalvo.getUnidadeDestino());

        verify(repositorioAlerta, times(1)).save(any(Alerta.class));
        verify(repositorioAlertaUsuario, times(2)).save(any(AlertaUsuario.class));
    }

    @Test
    @DisplayName("Deve criar alerta apenas para o titular quando não houver substituto")
    void criarAlerta_comApenasTitular_deveSalvarAlertaEUmAlertaDeUsuario() {
        ResponsavelDto titularApenas = new ResponsavelDto(10L, "Gestor", "12345", null, null);
        when(repositorioUnidade.findById(10L)).thenReturn(Optional.of(unidadeDestino));
        when(servicoSgrh.buscarResponsavelUnidade(10L)).thenReturn(Optional.of(titularApenas));
        when(repositorioAlerta.save(any(Alerta.class))).thenAnswer(invocation -> {
            Alerta alerta = invocation.getArgument(0);
            alerta.setCodigo(99L);
            return alerta;
        });

        servicoAlerta.criarAlerta(processo, "TESTE", 10L, "Descrição", LocalDate.now());

        verify(repositorioAlerta, times(1)).save(any(Alerta.class));
        verify(repositorioAlertaUsuario, times(1)).save(any(AlertaUsuario.class));
    }

    @Test
    @DisplayName("Deve criar alerta mas não notificar ninguém se SGRH não retornar responsável")
    void criarAlerta_semResponsavel_deveSalvarApenasAlerta() {
        when(repositorioUnidade.findById(10L)).thenReturn(Optional.of(unidadeDestino));
        when(servicoSgrh.buscarResponsavelUnidade(10L)).thenReturn(Optional.empty());
        when(repositorioAlerta.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        servicoAlerta.criarAlerta(processo, "TESTE", 10L, "Descrição", LocalDate.now());

        verify(repositorioAlerta, times(1)).save(any(Alerta.class));
        verify(repositorioAlertaUsuario, never()).save(any(AlertaUsuario.class));
    }

    @Test
    @DisplayName("Deve criar alerta mesmo que SGRH lance uma exceção")
    void criarAlerta_comErroNoSgrh_deveSalvarApenasAlerta() {
        when(repositorioUnidade.findById(10L)).thenReturn(Optional.of(unidadeDestino));
        when(servicoSgrh.buscarResponsavelUnidade(10L)).thenThrow(new RuntimeException("Erro de comunicação com SGRH"));
        when(repositorioAlerta.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        servicoAlerta.criarAlerta(processo, "TESTE", 10L, "Descrição", LocalDate.now());

        verify(repositorioAlerta, times(1)).save(any(Alerta.class));
        verify(repositorioAlertaUsuario, never()).save(any(AlertaUsuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar alerta para unidade inexistente")
    void criarAlerta_unidadeInexistente_deveLancarIllegalArgumentException() {
        when(repositorioUnidade.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> servicoAlerta.criarAlerta(processo, "TESTE", 999L, "Descrição", LocalDate.now()));

        verify(repositorioAlerta, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar um alerta para unidade OPERACIONAL")
    void criarAlertasProcessoIniciado_unidadeOperacional_deveRetornarUmAlerta() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidadeDestino);
        subprocesso.setDataLimiteEtapa1(LocalDate.now());

        UnidadeDto unidadeDto = new UnidadeDto(10L, "Unidade Operacional", "UND-OP", null, "OPERACIONAL");
        when(servicoSgrh.buscarUnidadePorCodigo(10L)).thenReturn(Optional.of(unidadeDto));
        when(repositorioUnidade.findById(10L)).thenReturn(Optional.of(unidadeDestino));
        when(repositorioAlerta.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Alerta> alertas = servicoAlerta.criarAlertasProcessoIniciado(processo, List.of(subprocesso));

        assertEquals(1, alertas.size());
    }

    @Test
    @DisplayName("Deve retornar um alerta para unidade INTERMEDIARIA")
    void criarAlertasProcessoIniciado_unidadeIntermediaria_deveRetornarUmAlerta() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidadeDestino);
        subprocesso.setDataLimiteEtapa1(LocalDate.now());

        UnidadeDto unidadeDto = new UnidadeDto(10L, "Unidade Intermediaria", "UND-INT", null, "INTERMEDIARIA");
        when(servicoSgrh.buscarUnidadePorCodigo(10L)).thenReturn(Optional.of(unidadeDto));
        when(repositorioUnidade.findById(10L)).thenReturn(Optional.of(unidadeDestino));
        when(repositorioAlerta.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Alerta> alertas = servicoAlerta.criarAlertasProcessoIniciado(processo, List.of(subprocesso));

        assertEquals(1, alertas.size());
    }

    @Test
    @DisplayName("Deve retornar dois alertas para unidade INTEROPERACIONAL")
    void criarAlertasProcessoIniciado_unidadeInteroperacional_deveRetornarDoisAlertas() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(unidadeDestino);
        subprocesso.setDataLimiteEtapa1(LocalDate.now());

        UnidadeDto unidadeDto = new UnidadeDto(10L, "Unidade Interoperacional", "UND-INTEROP", null, "INTEROPERACIONAL");
        when(servicoSgrh.buscarUnidadePorCodigo(10L)).thenReturn(Optional.of(unidadeDto));
        when(repositorioUnidade.findById(10L)).thenReturn(Optional.of(unidadeDestino));
        when(repositorioAlerta.save(any(Alerta.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Alerta> alertas = servicoAlerta.criarAlertasProcessoIniciado(processo, List.of(subprocesso));

        assertEquals(2, alertas.size());
    }
}