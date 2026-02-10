package sgc.subprocesso.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.SubprocessoDetalheDto;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class SubprocessoDetalheMapperTest {

    private SubprocessoDetalheMapper mapper;

    @Mock
    private MovimentacaoMapper movimentacaoMapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(SubprocessoDetalheMapper.class);
        ReflectionTestUtils.setField(mapper, "movimentacaoMapper", movimentacaoMapper);
    }

    @Test
    @DisplayName("Deve mapear toDto básico com sucesso")
    void toDto() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        SubprocessoPermissoesDto permissoes = SubprocessoPermissoesDto.builder().build();

        SubprocessoDetalheDto dto = mapper.toDto(sp, null, null, Collections.emptyList(), permissoes);

        assertThat(dto).isNotNull();
        assertThat(dto.getSituacao()).isEqualTo("MAPEAMENTO_CADASTRO_EM_ANDAMENTO");
        assertThat(dto.getTipoProcesso()).isEqualTo("MAPEAMENTO");
    }

    @Nested
    @DisplayName("Mapeamento de Responsável")
    class ResponsavelTests {
        @Test
        @DisplayName("Deve mapear responsável titular")
        void deveMapearResponsavelTitular() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            Unidade unidade = new Unidade();
            unidade.setTituloTitular("123");
            sp.setUnidade(unidade);
            Processo processo = new Processo();
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            sp.setProcesso(processo);

            Usuario responsavel = new Usuario();
            responsavel.setTituloEleitoral("123");
            responsavel.setNome("Titular");

            SubprocessoDetalheDto dto = mapper.toDto(sp, responsavel, null, null, null);

            assertThat(dto.getResponsavel()).isNotNull();
            assertThat(dto.getResponsavel().getTipoResponsabilidade()).isEqualTo("Titular");
        }

        @Test
        @DisplayName("Deve mapear responsável substituto")
        void deveMapearResponsavelSubstituto() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            Unidade unidade = new Unidade();
            unidade.setTituloTitular("123");
            sp.setUnidade(unidade);
            Processo processo = new Processo();
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            sp.setProcesso(processo);

            Usuario responsavel = new Usuario();
            responsavel.setTituloEleitoral("456"); 
            responsavel.setNome("Substituto");

            SubprocessoDetalheDto dto = mapper.toDto(sp, responsavel, null, null, null);

            assertThat(dto.getResponsavel()).isNotNull();
            assertThat(dto.getResponsavel().getTipoResponsabilidade()).isEqualTo("Substituição");
        }
    }

    @Nested
    @DisplayName("Mapeamento de Localização")
    class LocalizacaoTests {
        @Test
        @DisplayName("Deve mapear localização atual")
        void deveMapearLocalizacaoAtual() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            Processo processo = new Processo();
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            sp.setProcesso(processo);

            Unidade destino = new Unidade();
            destino.setSigla("DEST");
            Movimentacao mov = new Movimentacao();
            mov.setUnidadeDestino(destino);

            SubprocessoDetalheDto dto = mapper.toDto(sp, null, null, List.of(mov), null);

            assertThat(dto.getLocalizacaoAtual()).isEqualTo("DEST");
        }

        @Test
        @DisplayName("Deve mapear localização atual vazia se sem destino")
        void deveMapearLocalizacaoAtualVaziaSeSemDestino() {
            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            Processo processo = new Processo();
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            sp.setProcesso(processo);

            Movimentacao mov = new Movimentacao();
            mov.setUnidadeDestino(null);

            SubprocessoDetalheDto dto = mapper.toDto(sp, null, null, List.of(mov), null);

            assertThat(dto.getLocalizacaoAtual()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Prazos e Cobertura Extra")
    class CoberturaExtraTests {
        @Test
        @DisplayName("Deve cobrir branches de mapPrazoEtapaAtual")
        void deveCobrirMapPrazoEtapaAtual() {
            java.time.LocalDateTime d1 = java.time.LocalDateTime.now();
            java.time.LocalDateTime d2 = d1.plusDays(1);

            Subprocesso sp = new Subprocesso();
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
            sp.setProcesso(Processo.builder().tipo(TipoProcesso.MAPEAMENTO).build());

            sp.setDataLimiteEtapa1(d1);
            sp.setDataLimiteEtapa2(d2);
            
            SubprocessoDetalheDto dto1 = mapper.toDto(sp, null, null, null, null);
            assertThat(dto1.getPrazoEtapaAtual()).isEqualTo(d1);

            sp.setDataLimiteEtapa1(null);
            SubprocessoDetalheDto dto2 = mapper.toDto(sp, null, null, null, null);
            assertThat(dto2.getPrazoEtapaAtual()).isEqualTo(d2);
        }
    }
}
