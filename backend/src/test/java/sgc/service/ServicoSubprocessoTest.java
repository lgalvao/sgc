package sgc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.Atividade;
import sgc.atividade.AtividadeMapper;
import sgc.atividade.RepositorioAtividade;
import sgc.comum.Usuario;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.conhecimento.Conhecimento;
import sgc.conhecimento.ConhecimentoMapper;
import sgc.conhecimento.ConhecimentoRepository;
import sgc.mapa.Mapa;
import sgc.subprocesso.*;
import sgc.unidade.Unidade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServicoSubprocessoTest {

    @Mock
    private SubprocessoRepository subprocessoRepository;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private RepositorioAtividade atividadeRepository;

    @Mock
    private ConhecimentoRepository conhecimentoRepository;

    @Mock
    private AtividadeMapper atividadeMapper;

    @Mock
    private ConhecimentoMapper conhecimentoMapper;

    @Mock
    private MovimentacaoMapper movimentacaoMapper;

    @Mock
    private SubprocessoMapper subprocessoMapper;

    @InjectMocks
    private ServicoSubprocesso servicoSubprocesso;

    @Test
    void casoFeliz_retornaDetalhesComMovimentacoesEElementos() {
        // Arrange
        Long spId = 1L;
        Unidade unidade = mock(Unidade.class);
        when(unidade.getCodigo()).thenReturn(10L);
        when(unidade.getNome()).thenReturn("Unidade X");
        when(unidade.getSigla()).thenReturn("UX");
        Usuario titular = new Usuario();
        titular.setTitulo("0001");
        titular.setNome("Titular X");
        titular.setEmail("titular@exemplo");
        titular.setRamal("1234");
        when(unidade.getTitular()).thenReturn(titular);

        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(spId);
        sp.setUnidade(unidade);
        sp.setMapa(mapa);
        sp.setSituacaoId("EM_ANDAMENTO");
        sp.setDataLimiteEtapa1(LocalDate.of(2025, 12, 31));

        Movimentacao mov = mock(Movimentacao.class);
        MovimentacaoDTO movDto = new MovimentacaoDTO(
                500L,
                LocalDateTime.now(),
                null, null, null,
                unidade.getCodigo(), unidade.getSigla(), unidade.getNome(),
                "Mov desc"
        );

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
        when(movimentacaoMapper.toDTO(any(Movimentacao.class))).thenReturn(movDto);
        when(atividadeRepository.findByMapaCodigo(mapa.getCodigo())).thenReturn(List.of(at));
        when(conhecimentoRepository.findAll()).thenReturn(List.of(kc));

        // Act
        SubprocessoDetalheDTO dto = servicoSubprocesso.obterDetalhes(spId, "ADMIN", null);

        // Assert
        assertNotNull(dto);
        assertNotNull(dto.getUnidade());
        assertEquals(unidade.getCodigo(), dto.getUnidade().getCodigo());
        assertEquals("EM_ANDAMENTO", dto.getSituacao());
        assertNotNull(dto.getMovimentacoes());
        assertEquals(1, dto.getMovimentacoes().size());
        assertEquals(movDto.getCodigo(), dto.getMovimentacoes().get(0).getCodigo());
        assertNotNull(dto.getElementosDoProcesso());
        boolean temAtividade = dto.getElementosDoProcesso().stream().anyMatch(e -> "ATIVIDADE".equals(e.getTipo()));
        boolean temConhecimento = dto.getElementosDoProcesso().stream().anyMatch(e -> "CONHECIMENTO".equals(e.getTipo()));
        assertTrue(temAtividade, "Esperado elemento do tipo ATIVIDADE");
        assertTrue(temConhecimento, "Esperado elemento do tipo CONHECIMENTO");
    }

    @Test
    void casoNaoEncontrado_lancaDomainNotFoundException() {
        Long id = 99L;
        when(subprocessoRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ErroDominioNaoEncontrado.class, () -> servicoSubprocesso.obterDetalhes(id, "ADMIN", null));
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

        Long unidadeUsuario = 99L;
        assertThrows(ErroDominioAccessoNegado.class, () -> servicoSubprocesso.obterDetalhes(spId, "GESTOR", unidadeUsuario));
    }
}