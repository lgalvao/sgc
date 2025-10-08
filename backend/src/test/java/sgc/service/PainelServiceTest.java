package sgc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import sgc.alerta.AlertaRepository;
import sgc.comum.PainelService;
import sgc.processo.*;
import sgc.processo.dto.ProcessoResumoDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PainelServiceTest {
    @Mock
    private RepositorioProcesso repositorioProcesso;

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private UnidadeProcessoRepository unidadeProcessoRepository;

    @InjectMocks
    private PainelService painelService;

    @Test
    void listarProcessos_casoFeliz_retornaTodos() {
        Processo p1 = new Processo();
        p1.setCodigo(1L);
        p1.setDescricao("P1");
        p1.setSituacao("CRIADO");
        p1.setTipo("MAPEAMENTO");
        p1.setDataCriacao(LocalDateTime.now());
        p1.setDataLimite(LocalDate.now());

        Processo p2 = new Processo();
        p2.setCodigo(2L);
        p2.setDescricao("P2");
        p2.setSituacao("FINALIZADO");
        p2.setTipo("MAPEAMENTO");
        p2.setDataCriacao(LocalDateTime.now());
        p2.setDataLimite(LocalDate.now());

        UnidadeProcesso up1 = new UnidadeProcesso();
        up1.setCodigo(10L);
        up1.setProcessoCodigo(1L);
        up1.setNome("Unidade 10");

        UnidadeProcesso up2 = new UnidadeProcesso();
        up2.setCodigo(11L);
        up2.setProcessoCodigo(2L);
        up2.setNome("Unidade 11");

        when(repositorioProcesso.findAll()).thenReturn(List.of(p1, p2));
        when(unidadeProcessoRepository.findByProcessoCodigo(1L)).thenReturn(List.of(up1));

        Pageable pageable = PageRequest.of(0, 10);
        Page<ProcessoResumoDTO> page = painelService.listarProcessos("USER", null, pageable);

        assertEquals(2, page.getTotalElements());
    }

    @Test
    void listarProcessos_admin_somenteCriado() {
        Processo p1 = new Processo();
        p1.setCodigo(1L);
        p1.setDescricao("P1");
        p1.setSituacao("CRIADO");

        Processo p2 = new Processo();
        p2.setCodigo(2L);
        p2.setDescricao("P2");
        p2.setSituacao("EM_ANDAMENTO");

        UnidadeProcesso up1 = new UnidadeProcesso();
        up1.setCodigo(10L);
        up1.setProcessoCodigo(1L);

        UnidadeProcesso up2 = new UnidadeProcesso();
        up2.setCodigo(11L);
        up2.setProcessoCodigo(2L);

        when(repositorioProcesso.findAll()).thenReturn(List.of(p1, p2));
        when(unidadeProcessoRepository.findByProcessoCodigo(1L)).thenReturn(List.of(up1));
        when(unidadeProcessoRepository.findByProcessoCodigo(2L)).thenReturn(List.of(up2));

        Pageable pageable = PageRequest.of(0, 10);
        Page<ProcessoResumoDTO> page = painelService.listarProcessos("ADMIN", null, pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals(1L, page.getContent().getFirst().getCodigo());
    }
}