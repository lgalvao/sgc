package sesel.sgc.mapa.conhecimentos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sesel.sgc.mapa.ValidacaoErro;
import sesel.sgc.mapa.atividades.Atividade;
import sesel.sgc.mapa.atividades.AtividadeDto;
import sesel.sgc.mapa.atividades.AtividadeRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class ConhecimentoServiceTest {
    @Mock
    private ConhecimentoRepository conhecimentoRepository;

    @Mock
    private AtividadeRepository atividadeRepository;

    @InjectMocks
    private ConhecimentoService conhecimentoService;

    @BeforeEach
    void setUp() {
        // noinspection resource
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveLancarExcecaoQuandoAtividadeNaoEncontrada() {
        UUID atividadeId = UUID.randomUUID();
        AtividadeDto.ConhecimentoDto dto = new AtividadeDto.ConhecimentoDto();

        when(atividadeRepository.findById(atividadeId)).thenReturn(Optional.empty());

        assertThrows(ValidacaoErro.class, () -> conhecimentoService.salvarConhecimento(atividadeId, dto));
    }

    @Test
    void deveLancarExcecaoQuandoConhecimentoNaoEncontrado() {
        UUID atividadeId = UUID.randomUUID();
        UUID conhecimentoId = UUID.randomUUID();

        Atividade atividade = new Atividade();
        AtividadeDto.ConhecimentoDto dto = new AtividadeDto.ConhecimentoDto();
        dto.setId(conhecimentoId);

        when(atividadeRepository.findById(atividadeId)).thenReturn(Optional.of(atividade));
        when(conhecimentoRepository.findById(conhecimentoId)).thenReturn(Optional.empty());

        assertThrows(ValidacaoErro.class, () -> conhecimentoService.salvarConhecimento(atividadeId, dto));
    }

    @Test
    void deveLancarExcecaoQuandoConhecimentoNaoPertenceAAtividade() {
        UUID atividadeId = UUID.randomUUID();
        UUID conhecimentoId = UUID.randomUUID();

        Atividade atividade = new Atividade();
        atividade.setId(atividadeId);

        AtividadeDto.ConhecimentoDto dto = new AtividadeDto.ConhecimentoDto();
        dto.setId(conhecimentoId);

        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setId(conhecimentoId);

        Atividade atividadeDiferente = new Atividade();
        atividadeDiferente.setId(UUID.randomUUID());
        conhecimento.setAtividade(atividadeDiferente); // Atividade diferente, id não nulo

        when(atividadeRepository.findById(atividadeId)).thenReturn(Optional.of(atividade));
        when(conhecimentoRepository.findById(conhecimentoId)).thenReturn(Optional.of(conhecimento));

        assertThrows(ValidacaoErro.class, () -> conhecimentoService.salvarConhecimento(atividadeId, dto));
    }

    @Test
    void deveSalvarConhecimentoNovo() {
        UUID atividadeId = UUID.randomUUID();
        Atividade atividade = new Atividade();
        atividade.setId(atividadeId);

        AtividadeDto.ConhecimentoDto dto = new AtividadeDto.ConhecimentoDto();
        dto.setDescricao("Spring Boot");
        dto.setTipo(null); // Ajuste conforme enum disponível
        when(atividadeRepository.findById(atividadeId)).thenReturn(Optional.of(atividade));

        Conhecimento conhecimento = new Conhecimento();
        when(conhecimentoRepository.save(any())).thenReturn(conhecimento);

        AtividadeDto.ConhecimentoDto resultado = conhecimentoService.salvarConhecimento(atividadeId, dto);
        assertNotNull(resultado);
    }

    @Test
    void deveBuscarConhecimentoPorIdEAtividadeId() {
        UUID atividadeId = UUID.randomUUID();
        UUID conhecimentoId = UUID.randomUUID();
        Conhecimento conhecimento = new Conhecimento();
        when(conhecimentoRepository.findByIdAndAtividadeId(conhecimentoId, atividadeId))
                .thenReturn(Optional.of(conhecimento));
        Optional<AtividadeDto.ConhecimentoDto> resultado = conhecimentoService
                .buscarConhecimentoPorIdEAtividadeId(conhecimentoId, atividadeId);
        assertNotNull(resultado);
    }

    @Test
    void deveAtualizarConhecimentoExistenteComSucesso() {
        UUID atividadeId = UUID.randomUUID();
        UUID conhecimentoId = UUID.randomUUID();

        Atividade atividade = new Atividade();
        atividade.setId(atividadeId);

        AtividadeDto.ConhecimentoDto dto = new AtividadeDto.ConhecimentoDto();
        dto.setId(conhecimentoId);
        dto.setDescricao("Atualizado");

        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setId(conhecimentoId);
        conhecimento.setAtividade(atividade);

        when(atividadeRepository.findById(atividadeId)).thenReturn(Optional.of(atividade));
        when(conhecimentoRepository.findById(conhecimentoId)).thenReturn(Optional.of(conhecimento));

        // Simula o updateFromDto. Não precisa de when para void
        when(conhecimentoRepository.save(any())).thenReturn(conhecimento);
        AtividadeDto.ConhecimentoDto resultado = conhecimentoService.salvarConhecimento(atividadeId, dto);
        assertNotNull(resultado);
    }

    @Test
    void deveExcluirConhecimentoComSucesso() {
        UUID atividadeId = UUID.randomUUID();
        UUID conhecimentoId = UUID.randomUUID();
        Atividade atividade = new Atividade();
        atividade.setId(atividadeId);
        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setId(conhecimentoId);
        conhecimento.setAtividade(atividade);
        when(conhecimentoRepository.findById(conhecimentoId)).thenReturn(Optional.of(conhecimento));
        conhecimentoService.excluirConhecimento(conhecimentoId, atividadeId);
        // Se não lançar exceção, passou
    }

    @Test
    void deveLancarExcecaoAoExcluirConhecimentoDeOutraAtividade() {
        UUID atividadeId = UUID.randomUUID();
        UUID conhecimentoId = UUID.randomUUID();
        Atividade atividade = new Atividade();
        atividade.setId(UUID.randomUUID()); // id diferente
        Conhecimento conhecimento = new Conhecimento();
        conhecimento.setId(conhecimentoId);
        conhecimento.setAtividade(atividade);
        when(conhecimentoRepository.findById(conhecimentoId)).thenReturn(Optional.of(conhecimento));
        assertThrows(ValidacaoErro.class, () -> conhecimentoService.excluirConhecimento(conhecimentoId, atividadeId));
    }

    @Test
    void deveLancarExcecaoAoExcluirConhecimentoInexistente() {
        UUID atividadeId = UUID.randomUUID();
        UUID conhecimentoId = UUID.randomUUID();
        when(conhecimentoRepository.findById(conhecimentoId)).thenReturn(Optional.empty());
        assertThrows(ValidacaoErro.class, () -> conhecimentoService.excluirConhecimento(conhecimentoId, atividadeId));
    }
}