package sgc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.exception.DomainAccessDeniedException;
import sgc.exception.DomainNotFoundException;
import sgc.model.*;
import sgc.repository.AtividadeRepository;
import sgc.repository.ConhecimentoRepository;
import sgc.repository.MovimentacaoRepository;
import sgc.repository.SubprocessoRepository;
import sgc.dto.SubprocessoDetailDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubprocessoServiceTest {

    @Mock
    private SubprocessoRepository subprocessoRepository;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private AtividadeRepository atividadeRepository;

    @Mock
    private ConhecimentoRepository conhecimentoRepository;

    @InjectMocks
    private SubprocessoService subprocessoService;

    @Test
    void casoFeliz_retornaDetalhesComMovimentacoesEElementos() {
        // Arrange
        Long spId = 1L;
        Unidade unidade = new Unidade();
        unidade.setCodigo(10L);
        unidade.setNome("Unidade X");
        unidade.setSigla("UX");
        Usuario titular = new Usuario();
        titular.setTitulo("0001");
        titular.setNome("Titular X");
        titular.setEmail("titular@exemplo");
        titular.setRamal("1234");
        unidade.setTitular(titular);

        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(spId);
        sp.setUnidade(unidade);
        sp.setMapa(mapa);
        sp.setSituacaoId("EM_ANDAMENTO");
        sp.setDataLimiteEtapa1(LocalDate.of(2025, 12, 31));

        Movimentacao mov = new Movimentacao();
        mov.setCodigo(500L);
        mov.setDataHora(LocalDateTime.now());
        mov.setUnidadeDestino(unidade);
        mov.setDescricao("Mov desc");
        mov.setUnidadeOrigem(null);

        Atividade at = new Atividade();
        at.setCodigo(200L);
        at.setMapa(mapa);
        at.setDescricao("Atividade A");

        Conhecimento kc = new Conhecimento();
        kc.setCodigo(300L);
        kc.setAtividade(at);
        kc.setDescricao("Conhecimento 1");

        when(subprocessoRepository.findById(spId)).thenReturn(Optional.of(sp));
        when(movimentacaoRepository.findBySubprocessoCodigoOrderByDataHoraDesc(spId)).thenReturn(List.of(mov));
        when(atividadeRepository.findByMapaCodigo(mapa.getCodigo())).thenReturn(List.of(at));
        when(conhecimentoRepository.findAll()).thenReturn(List.of(kc));

        // Act
        SubprocessoDetailDTO dto = subprocessoService.getDetails(spId, "ADMIN", null);

        // Assert
        assertNotNull(dto);
        assertNotNull(dto.getUnidade());
        assertEquals(unidade.getCodigo(), dto.getUnidade().getCodigo());
        assertEquals("EM_ANDAMENTO", dto.getSituacao());
        assertNotNull(dto.getMovimentacoes());
        assertEquals(1, dto.getMovimentacoes().size());
        assertNotNull(dto.getElementosDoProcesso());
        // deve conter pelo menos a atividade e o conhecimento como elementos
        boolean temAtividade = dto.getElementosDoProcesso().stream().anyMatch(e -> "ATIVIDADE".equals(e.getTipo()));
        boolean temConhecimento = dto.getElementosDoProcesso().stream().anyMatch(e -> "CONHECIMENTO".equals(e.getTipo()));
        assertTrue(temAtividade, "Esperado elemento do tipo ATIVIDADE");
        assertTrue(temConhecimento, "Esperado elemento do tipo CONHECIMENTO");
    }

    @Test
    void casoNaoEncontrado_lancaDomainNotFoundException() {
        Long id = 99L;
        when(subprocessoRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(DomainNotFoundException.class, () -> subprocessoService.getDetails(id, "ADMIN", null));
    }

    @Test
    void casoSemPermissao_lancaDomainAccessDeniedExceptionParaGestorDeOutraUnidade() {
        Long spId = 2L;
        Unidade unidade = new Unidade();
        unidade.setCodigo(50L);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(spId);
        sp.setUnidade(unidade);

        when(subprocessoRepository.findById(spId)).thenReturn(Optional.of(sp));

        // usuário gestor com unidade diferente
        Long unidadeUsuario = 99L;
        assertThrows(DomainAccessDeniedException.class, () -> subprocessoService.getDetails(spId, "GESTOR", unidadeUsuario));
    }
}