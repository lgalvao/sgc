package sgc.subprocesso.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoDetalheMapper Coverage Tests")
class SubprocessoDetalheMapperCoverageTest {

    @InjectMocks
    private SubprocessoDetalheMapperImpl mapper;

    @Spy
    private MovimentacaoMapper movimentacaoMapper = new MovimentacaoMapperImpl();

    @Test
    @DisplayName("Deve retornar null quando todos os parâmetros principais são nulos")
    void deveRetornarNullQuandoTodosNulos() {
        assertThat(mapper.toDto(null, null, null, null, null)).isNull();
    }

    @Test
    @DisplayName("Deve cobrir branches de mapResponsavel")
    void deveCobrirMapResponsavel() {
        Unidade u = Unidade.builder().tituloTitular("123").build();
        Subprocesso sp = Subprocesso.builder().unidade(u).build();

        Usuario responsavel = Usuario.builder().tituloEleitoral("123").nome("Titular").build();
        var res1 = mapper.mapResponsavel(sp, responsavel);
        assertThat(res1.getTipoResponsabilidade()).isEqualTo("Titular");

        responsavel.setTituloEleitoral("456");
        var res2 = mapper.mapResponsavel(sp, responsavel);
        assertThat(res2.getTipoResponsabilidade()).isEqualTo("Substituição");

        assertThat(mapper.mapResponsavel(sp, null)).isNull();
    }

    @Test
    @DisplayName("Deve cobrir branches de mapLocalizacaoAtual")
    void deveCobrirMapLocalizacaoAtual() {
        assertThat(mapper.mapLocalizacaoAtual(null)).isEmpty();
        assertThat(mapper.mapLocalizacaoAtual(Collections.emptyList())).isEmpty();

        Movimentacao m1 = new Movimentacao();
        assertThat(mapper.mapLocalizacaoAtual(List.of(m1))).isEmpty();

        m1.setUnidadeDestino(Unidade.builder().sigla("DEST").build());
        assertThat(mapper.mapLocalizacaoAtual(List.of(m1))).isEqualTo("DEST");
    }

    @Test
    @DisplayName("Deve cobrir branches de mapPrazoEtapaAtual")
    void deveCobrirMapPrazoEtapaAtual() {
        LocalDateTime d1 = LocalDateTime.now();
        LocalDateTime d2 = d1.plusDays(1);

        Subprocesso sp = new Subprocesso();

        sp.setDataLimiteEtapa1(d1);
        sp.setDataLimiteEtapa2(d2);
        assertThat(mapper.mapPrazoEtapaAtual(sp)).isEqualTo(d1);

        sp.setDataLimiteEtapa1(null);
        assertThat(mapper.mapPrazoEtapaAtual(sp)).isEqualTo(d2);
    }

    @Test
    @DisplayName("Deve cobrir toDto com relacionamentos presentes")
    void deveCobrirToDtoCompleto() {
        Processo p = Processo.builder().tipo(TipoProcesso.MAPEAMENTO).descricao("Proc").build();
        Subprocesso sp = Subprocesso.builder()
                .processo(p)
                .unidade(Unidade.builder().sigla("U1").build())
                .situacao(sgc.subprocesso.model.SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO)
                .build();

        List<Movimentacao> movs = new ArrayList<>();
        movs.add(Movimentacao.builder().codigo(1L).build());

        SubprocessoDetalheDto dto = mapper.toDto(sp, null, null, movs, null);
        assertThat(dto).isNotNull();
        assertThat(dto.getProcessoDescricao()).isEqualTo("Proc");
        assertThat(dto.getMovimentacoes()).hasSize(1);
    }
}
