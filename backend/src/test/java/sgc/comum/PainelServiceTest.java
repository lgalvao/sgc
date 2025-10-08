package sgc.comum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sgc.alerta.modelo.Alerta;
import sgc.alerta.modelo.AlertaRepo;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.processo.modelo.UnidadeProcessoRepo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do PainelService")
class PainelServiceTest {
    @Mock
    private ProcessoRepo processoRepo;

    @Mock
    private AlertaRepo alertaRepo;

    @Mock
    private UnidadeProcessoRepo unidadeProcessoRepo;

    @InjectMocks
    private PainelService painelService;

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando perfil for nulo")
    void deveLancarIllegalArgumentExceptionQuandoPerfilNulo() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> painelService.listarProcessos(null, null, PageRequest.of(0, 10))
        );
        
        assertEquals("O parâmetro 'perfil' é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando perfil for vazio")
    void deveLancarIllegalArgumentExceptionQuandoPerfilVazio() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> painelService.listarProcessos("", null, PageRequest.of(0, 10))
        );
        
        assertEquals("O parâmetro 'perfil' é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando perfil for em branco")
    void deveLancarIllegalArgumentExceptionQuandoPerfilEmBranco() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> painelService.listarProcessos("   ", null, PageRequest.of(0, 10))
        );
        
        assertEquals("O parâmetro 'perfil' é obrigatório", exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar todos os processos quando perfil não for ADMIN e unidade não for informada")
    void deveRetornarTodosOsProcessosQuandoPerfilNaoForAdminEUnidadeNaoInformada() {
        Processo processo1 = new Processo();
        processo1.setCodigo(1L);
        processo1.setDescricao("Processo 1");
        processo1.setSituacao("ATIVO");

        Processo processo2 = new Processo();
        processo2.setCodigo(2L);
        processo2.setDescricao("Processo 2");
        processo2.setSituacao("INATIVO");

        when(processoRepo.findAll()).thenReturn(Arrays.asList(processo1, processo2));
        when(unidadeProcessoRepo.findByProcessoCodigo(anyLong())).thenReturn(Collections.emptyList());

        Pageable pageable = PageRequest.of(0, 10);
        var result = painelService.listarProcessos("USUARIO", null, pageable);

        assertEquals(2, result.getTotalElements());
        verify(processoRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar apenas processos com situação CRIADO quando perfil for ADMIN")
    void deveRetornarApenasProcessosComSituacaoCriadoQuandoPerfilForAdmin() {
        Processo processo1 = new Processo();
        processo1.setCodigo(1L);
        processo1.setDescricao("Processo 1");
        processo1.setSituacao("CRIADO");

        Processo processo2 = new Processo();
        processo2.setCodigo(2L);
        processo2.setDescricao("Processo 2");
        processo2.setSituacao("ATIVO");

        when(processoRepo.findAll()).thenReturn(Arrays.asList(processo1, processo2));
        when(unidadeProcessoRepo.findByProcessoCodigo(anyLong())).thenReturn(Collections.emptyList());

        Pageable pageable = PageRequest.of(0, 10);
        var result = painelService.listarProcessos("ADMIN", null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getCodigo());
        verify(processoRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve filtrar processos por unidade quando codigoUnidade for informado")
    void deveFiltrarProcessosPorUnidadeQuandoCodigoUnidadeInformado() {
        Processo processo1 = new Processo();
        processo1.setCodigo(1L);
        processo1.setDescricao("Processo 1");
        processo1.setSituacao("ATIVO");

        Processo processo2 = new Processo();
        processo2.setCodigo(2L);
        processo2.setDescricao("Processo 2");
        processo2.setSituacao("ATIVO");

        UnidadeProcesso unidadeProcesso1 = new UnidadeProcesso();
        unidadeProcesso1.setCodigo(10L);
        unidadeProcesso1.setNome("Unidade 1");

        when(processoRepo.findAll()).thenReturn(Arrays.asList(processo1, processo2));
        when(unidadeProcessoRepo.findByProcessoCodigo(1L)).thenReturn(Arrays.asList(unidadeProcesso1));
        when(unidadeProcessoRepo.findByProcessoCodigo(2L)).thenReturn(Collections.emptyList());

        Pageable pageable = PageRequest.of(0, 10);
        var result = painelService.listarProcessos("USUARIO", 10L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getCodigo());
        assertEquals(10L, result.getContent().get(0).getUnidadeCodigo());
        assertEquals("Unidade 1", result.getContent().get(0).getUnidadeNome());
        verify(processoRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve aplicar paginação corretamente")
    void deveAplicarPaginacaoCorretamente() {
        // Criar uma lista com mais de 10 processos
        Processo[] processos = new Processo[15];
        for (int i = 0; i < 15; i++) {
            Processo processo = new Processo();
            processo.setCodigo((long) (i + 1));
            processo.setDescricao("Processo " + (i + 1));
            processo.setSituacao("ATIVO");
            processos[i] = processo;
        }

        when(processoRepo.findAll()).thenReturn(Arrays.asList(processos));
        when(unidadeProcessoRepo.findByProcessoCodigo(anyLong())).thenReturn(Collections.emptyList());

        Pageable pageable = PageRequest.of(1, 5); // Página 1 (índice 1), tamanho 5
        var result = painelService.listarProcessos("USUARIO", null, pageable);

        assertEquals(15, result.getTotalElements());
        assertEquals(5, result.getContent().size());
        assertEquals(1, result.getNumber()); // Número da página (começa em 0)
        verify(processoRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar página vazia quando índice inicial for maior que total")
    void deveRetornarPaginaVaziaQuandoIndiceInicialMaiorQueTotal() {
        Processo processo1 = new Processo();
        processo1.setCodigo(1L);
        processo1.setDescricao("Processo 1");
        processo1.setSituacao("ATIVO");

        when(processoRepo.findAll()).thenReturn(Arrays.asList(processo1));
        when(unidadeProcessoRepo.findByProcessoCodigo(anyLong())).thenReturn(Collections.emptyList());

        Pageable pageable = PageRequest.of(10, 5); // Página muito alta
        var result = painelService.listarProcessos("USUARIO", null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getContent().size());
        verify(processoRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar alertas filtrados por usuário")
    void deveRetornarAlertasFiltradosPorUsuario() {
        var alerta1 = new Alerta();
        alerta1.setCodigo(1L);
        alerta1.setDescricao("Alerta 1");
        alerta1.setDataHora(LocalDateTime.now());
        
        var alerta2 = new Alerta();
        alerta2.setCodigo(2L);
        alerta2.setDescricao("Alerta 2");
        alerta2.setDataHora(LocalDateTime.now());
        
        when(alertaRepo.findAll()).thenReturn(Arrays.asList(alerta1, alerta2));

        Pageable pageable = PageRequest.of(0, 10);
        var result = painelService.listarAlertas("USUARIO_TESTE", null, pageable);

        assertEquals(0, result.getTotalElements()); // Nenhum alerta associado ao usuário
        verify(alertaRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar alertas filtrados por unidade")
    void deveRetornarAlertasFiltradosPorUnidade() {
        var alerta1 = new Alerta();
        alerta1.setCodigo(1L);
        alerta1.setDescricao("Alerta 1");
        alerta1.setDataHora(LocalDateTime.now());
        
        var alerta2 = new Alerta();
        alerta2.setCodigo(2L);
        alerta2.setDescricao("Alerta 2");
        alerta2.setDataHora(LocalDateTime.now());
        
        when(alertaRepo.findAll()).thenReturn(Arrays.asList(alerta1, alerta2));

        Pageable pageable = PageRequest.of(0, 10);
        var result = painelService.listarAlertas(null, 10L, pageable);

        assertEquals(0, result.getTotalElements()); // Nenhum alerta associado à unidade
        verify(alertaRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar todos os alertas quando usuário e unidade forem nulos")
    void deveRetornarTodosAlertasQuandoUsuarioEUnidadeNulos() {
        var alerta1 = new Alerta();
        alerta1.setCodigo(1L);
        alerta1.setDescricao("Alerta 1");
        alerta1.setDataHora(LocalDateTime.now());
        
        var alerta2 = new Alerta();
        alerta2.setCodigo(2L);
        alerta2.setDescricao("Alerta 2");
        alerta2.setDataHora(LocalDateTime.now());
        
        when(alertaRepo.findAll()).thenReturn(Arrays.asList(alerta1, alerta2));

        Pageable pageable = PageRequest.of(0, 10);
        var result = painelService.listarAlertas(null, null, pageable);

        assertEquals(2, result.getTotalElements());
        verify(alertaRepo, times(1)).findAll();
    }
}