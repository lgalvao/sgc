package sgc.comum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.dto.AlertaDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.processo.SituacaoProcesso;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.processo.modelo.UnidadeProcessoRepo;
import sgc.sgrh.Usuario;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para PainelService")
class PainelServiceTest {

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private AlertaRepo alertaRepo;
    @Mock
    private UnidadeProcessoRepo unidadeProcessoRepo;
    @Mock
    private UnidadeRepo unidadeRepo;

    @InjectMocks
    private PainelService painelService;

    private Processo processo1, processo2, processoCriado;
    private Unidade unidade1;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        unidade1 = new Unidade("Unidade Teste", "UT");
        unidade1.setCodigo(1L);

        processo1 = new Processo("Processo 1", TipoProcesso.MAPEAMENTO, SituacaoProcesso.EM_ANDAMENTO, LocalDateTime.now());
        processo1.setCodigo(101L);
        processo2 = new Processo("Processo 2", TipoProcesso.REVISAO, SituacaoProcesso.FINALIZADO, LocalDateTime.now());
        processo2.setCodigo(102L);
        processoCriado = new Processo("Processo Criado", TipoProcesso.MAPEAMENTO, SituacaoProcesso.CRIADO, LocalDateTime.now());
        processoCriado.setCodigo(103L);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Deve retornar todos os processos para perfil ADMIN")
    void listarProcessos_admin_retornaTodos() {
        when(processoRepo.findAll()).thenReturn(List.of(processo1, processo2, processoCriado));
        when(unidadeProcessoRepo.findByProcessoCodigo(anyLong())).thenReturn(List.of(new UnidadeProcesso()));

        Page<ProcessoResumoDto> resultado = painelService.listarProcessos("ADMIN", null, pageable);

        assertEquals(3, resultado.getTotalElements());
    }

    @Test
    @DisplayName("Não deve retornar processos CRIADO para perfil não-ADMIN")
    void listarProcessos_naoAdmin_naoVeCriado() {
        when(unidadeRepo.findByUnidadeSuperiorCodigo(1L)).thenReturn(Collections.emptyList());
        when(unidadeProcessoRepo.findByUnidadeCodigoIn(List.of(1L))).thenReturn(List.of(new UnidadeProcesso(101L, 1L, "N", "S", "T", null, "A", null), new UnidadeProcesso(103L, 1L, "N", "S", "T", null, "A", null)));
        when(processoRepo.findAllById(List.of(101L, 103L))).thenReturn(List.of(processo1, processoCriado));
        when(unidadeProcessoRepo.findByProcessoCodigo(101L)).thenReturn(List.of(new UnidadeProcesso()));


        Page<ProcessoResumoDto> resultado = painelService.listarProcessos("GESTOR", 1L, pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Processo 1", resultado.getContent().get(0).getDescricao());
    }


    @Test
    @DisplayName("Deve retornar alertas filtrados por usuário")
    void deveRetornarAlertasFiltradosPorUsuario() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(123L);
        Alerta alerta = new Alerta();
        alerta.setUsuarioDestino(usuario);
        when(alertaRepo.findByUsuarioDestino_TituloEleitoral(123L, pageable)).thenReturn(new PageImpl<>(List.of(alerta)));

        Page<AlertaDto> resultado = painelService.listarAlertas("123", null, pageable);

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    @DisplayName("Deve retornar alertas filtrados por unidade e sub-unidades")
    void deveRetornarAlertasFiltradosPorUnidade() {
        Unidade subUnidade = new Unidade("Sub Unidade", "SU");
        subUnidade.setCodigo(2L);
        Alerta alertaUnidade = new Alerta();
        alertaUnidade.setUnidadeDestino(unidade1);
        Alerta alertaSubUnidade = new Alerta();
        alertaSubUnidade.setUnidadeDestino(subUnidade);

        when(unidadeRepo.findByUnidadeSuperiorCodigo(1L)).thenReturn(List.of(subUnidade));
        when(unidadeRepo.findByUnidadeSuperiorCodigo(2L)).thenReturn(Collections.emptyList());
        when(alertaRepo.findByUnidadeDestino_CodigoIn(List.of(2L, 1L), pageable)).thenReturn(new PageImpl<>(List.of(alertaUnidade, alertaSubUnidade)));

        Page<AlertaDto> resultado = painelService.listarAlertas(null, 1L, pageable);

        assertEquals(2, resultado.getTotalElements());
    }
}
