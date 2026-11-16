package sesel.sgc.mapa.atividades;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import sesel.sgc.mapa.MapaUnidadeSituacao;
import sesel.sgc.mapa.ValidacaoErro;
import sesel.sgc.mapa.conhecimentos.Conhecimento;
import sesel.sgc.mapa.conhecimentos.ConhecimentoTipo;
import sesel.sgc.processounidade.ProcessoUnidade;
import sesel.sgc.processounidade.ProcessoUnidadeService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class AtividadeServiceTest {
    @Autowired
    private AtividadeService atividadeService;

    @MockitoBean
    private ProcessoUnidadeService processoUnidadeService;

    @BeforeEach
    void setUp() {
        when(processoUnidadeService.buscarProcessoUnidadePorId(any(UUID.class)))
                .thenReturn(new ProcessoUnidade());
    }

    @Test
    @Transactional
    @DirtiesContext
    void deveCadastrarAtividadeComConhecimento() {
        CadastroAtividadesDto request = new CadastroAtividadesDto();
        AtividadeDto atividade = new AtividadeDto();
        atividade.setDescricao("Atividade válida");

        AtividadeDto.ConhecimentoDto conhecimento = new AtividadeDto.ConhecimentoDto();
        conhecimento.setDescricao("Java");
        conhecimento.setTipo(null); // Ajuste conforme enum disponível
        atividade.setConhecimentos(List.of(conhecimento));
        request.setAtividades(List.of(atividade));
        request.setProcessoUnidadeId(UUID.randomUUID());

        // Não deve lançar exceção
        atividadeService.cadastrarAtividades(request);
    }

    @Test
    void deveBuscarAtividadesPorProcessoUnidade() {
        UUID processoUnidadeId = UUID.randomUUID();

        List<AtividadeDto> atividades = atividadeService.buscarAtividadesPorProcessoUnidade(processoUnidadeId);
        assertNotNull(atividades);
    }

    @Test
    void deveLancarExcecaoQuandoProcessoUnidadeEmSituacaoInvalida() {
        UUID processoUnidadeId = UUID.randomUUID();
        ProcessoUnidade processoUnidade = new ProcessoUnidade();
        processoUnidade.setSituacao(MapaUnidadeSituacao.FINALIZADA);
        when(processoUnidadeService.buscarProcessoUnidadePorId(processoUnidadeId)).thenReturn(processoUnidade);
        CadastroAtividadesDto request = new CadastroAtividadesDto();
        AtividadeDto atividade = new AtividadeDto();
        atividade.setDescricao("Atividade válida");
        AtividadeDto.ConhecimentoDto conhecimento = new AtividadeDto.ConhecimentoDto();
        conhecimento.setDescricao("Java");
        conhecimento.setTipo(ConhecimentoTipo.TECNICO);
        atividade.setConhecimentos(List.of(conhecimento));
        request.setAtividades(List.of(atividade));
        request.setProcessoUnidadeId(processoUnidadeId);
        assertThrows(ValidacaoErro.class, () -> atividadeService.cadastrarAtividades(request));
    }

    @Test
    void toDtoDeveRetornarDtoCorreto() {
        Atividade atividade = new Atividade();
        UUID atividadeId = UUID.randomUUID();
        atividade.setId(atividadeId);
        atividade.setDescricao("Descricao Teste");
        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setId(UUID.randomUUID());
        conhecimento.setDescricao("Conhecimento Teste");
        conhecimento.setTipo(ConhecimentoTipo.GERENCIAL);
        conhecimento.setAtividade(atividade);
        atividade.getConhecimentos().add(conhecimento);
        AtividadeDto dto = ReflectionTestUtils.invokeMethod(atividadeService, "toDto", atividade);
        assertNotNull(dto);
        assertEquals(atividadeId, dto.getId());
        assertEquals("Descricao Teste", dto.getDescricao());
        assertNotNull(dto.getConhecimentos());
        assertEquals(1, dto.getConhecimentos().size());
        AtividadeDto.ConhecimentoDto conhecimentoDto = dto.getConhecimentos().getFirst();
        assertEquals(conhecimento.getId(), conhecimentoDto.getId());
        assertEquals("Conhecimento Teste", conhecimentoDto.getDescricao());
        assertEquals(ConhecimentoTipo.GERENCIAL, conhecimentoDto.getTipo());
    }

    @Test
    void toDtoDeveRetornarNullQuandoAtividadeNula() {
        AtividadeDto dto = ReflectionTestUtils.invokeMethod(atividadeService, "toDto", (Atividade) null);
        assertNull(dto);
    }
}