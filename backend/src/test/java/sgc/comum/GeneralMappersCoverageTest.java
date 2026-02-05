package sgc.comum;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import sgc.configuracao.mapper.ParametroMapper;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.Conhecimento;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.mapper.ProcessoDetalheMapper;
import sgc.processo.mapper.ProcessoMapper;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.Subprocesso;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("Testes de Cobertura para Mappers")
class GeneralMappersCoverageTest {

    private final ConhecimentoMapper conhecimentoMapper = Mappers.getMapper(ConhecimentoMapper.class);
    private final SubprocessoMapper subprocessoMapper = Mappers.getMapper(SubprocessoMapper.class);
    private final SubprocessoDetalheMapper subprocessoDetalheMapper = Mappers.getMapper(SubprocessoDetalheMapper.class);
    private final ProcessoDetalheMapper processoDetalheMapper = Mappers.getMapper(ProcessoDetalheMapper.class);
    private final ProcessoMapper processoMapper = Mappers.getMapper(ProcessoMapper.class);
    private final MapaAjusteMapper mapaAjusteMapper = Mappers.getMapper(MapaAjusteMapper.class);
    private final UsuarioMapper usuarioMapper = Mappers.getMapper(UsuarioMapper.class);
    private final MapaCompletoMapper mapaCompletoMapper = Mappers.getMapper(MapaCompletoMapper.class);
    private final ParametroMapper parametroMapper = Mappers.getMapper(ParametroMapper.class);

    @Test
    @DisplayName("ConhecimentoMapper - Cobertura de branches nulos")
    void conhecimentoMapperNulls() {
        assertThat(conhecimentoMapper.toResponse(null)).isNull();
        assertThat(conhecimentoMapper.toEntity((sgc.mapa.dto.CriarConhecimentoRequest) null)).isNull();
        assertThat(conhecimentoMapper.toEntity((sgc.mapa.dto.AtualizarConhecimentoRequest) null)).isNull();

        Conhecimento c = new Conhecimento();
        c.setAtividade(null);
        assertThat(conhecimentoMapper.toResponse(c).atividadeCodigo()).isNull();
    }

    @Test
    @DisplayName("SubprocessoMapper - Cobertura de branches nulos")
    void subprocessoMapperNulls() {
        assertThat(subprocessoMapper.toDto(null)).isNull();

        Subprocesso s = new Subprocesso();
        s.setProcesso(null);
        s.setUnidade(null);
        s.setMapa(null);

        var dto = subprocessoMapper.toDto(s);
        assertThat(dto.getCodProcesso()).isNull();
        assertThat(dto.getCodUnidade()).isNull();
        assertThat(dto.getCodMapa()).isNull();
    }

    @Test
    @DisplayName("SubprocessoDetalheMapper - Cobertura de branches nulos")
    void subprocessoDetalheMapperNulls() {
        assertThat(subprocessoDetalheMapper.toDto(null, null, null, null, null)).isNull();
        assertThat(subprocessoDetalheMapper.toUnidadeDto(null)).isNull();

        Subprocesso sp = new Subprocesso();
        sp.setProcesso(sgc.processo.model.Processo.builder().tipo(sgc.processo.model.TipoProcesso.MAPEAMENTO).build());
        sp.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO);
        sp.setUnidade(new Unidade());

        // Testa as combinações de nulos na linha 31 do Impl
        assertThat(subprocessoDetalheMapper.toDto(sp, null, null, null, null)).isNotNull();
        assertThat(subprocessoDetalheMapper.toDto(null, new Usuario(), null, null, null)).isNotNull();
        assertThat(subprocessoDetalheMapper.toDto(null, null, new Usuario(), null, null)).isNotNull();
        assertThat(subprocessoDetalheMapper.toDto(null, null, null, Collections.emptyList(), null)).isNotNull();
        assertThat(subprocessoDetalheMapper.toDto(null, null, null, null, sgc.subprocesso.dto.SubprocessoPermissoesDto.builder().build())).isNotNull();

        // Branches adicionais no mapper interface
        Usuario userSemTitulo = new Usuario();
        userSemTitulo.setTituloEleitoral(null);
        assertThat(subprocessoDetalheMapper.toDto(sp, userSemTitulo, null, null, null)).isNotNull();

        Subprocesso spSemUnidade = new Subprocesso();
        spSemUnidade.setUnidade(null);
        assertThat(subprocessoDetalheMapper.toDto(spSemUnidade, new Usuario(), null, null, null)).isNotNull();

        Subprocesso spSemProcesso = new Subprocesso();
        spSemProcesso.setProcesso(null);
        spSemProcesso.setSituacaoForcada(sgc.subprocesso.model.SituacaoSubprocesso.NAO_INICIADO);
        assertThat(subprocessoDetalheMapper.toDto(spSemProcesso, null, null, null, null)).isNotNull();
    }

    @Test
    @DisplayName("ProcessoDetalheMapper - Cobertura de branches nulos")
    void processoDetalheMapperNulls() {
        assertThat(processoDetalheMapper.toUnidadeParticipanteDto(null)).isNull();

        Unidade u = new Unidade();
        u.setUnidadeSuperior(null);
        assertThat(processoDetalheMapper.toUnidadeParticipanteDto(u).getCodUnidadeSuperior()).isNull();
    }

    @Test
    @DisplayName("ProcessoMapper - Cobertura de branches nulos")
    void processoMapperNulls() {
        assertThat(processoMapper.toDto(null)).isNull();
        assertThat(processoMapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("MapaAjusteMapper - Cobertura de branches nulos")
    void mapaAjusteMapperNulls() {
        assertThat(mapaAjusteMapper.toDto(null, null, null, null, null, null)).isNull();

        Subprocesso s = new Subprocesso();
        s.setMapa(null);
        s.setUnidade(null);

        var dto = mapaAjusteMapper.toDto(s, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
        assertThat(dto.getCodMapa()).isNull();
        assertThat(dto.getUnidadeNome()).isNull();

        // Branches do if no mapper interface
        assertThat(mapaAjusteMapper.toDto(null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyMap())).isNotNull();
        assertThat(mapaAjusteMapper.toDto(null, null, Collections.emptyList(), null, Collections.emptyList(), Collections.emptyMap())).isNotNull();
        assertThat(mapaAjusteMapper.toDto(null, null, Collections.emptyList(), Collections.emptyList(), null, Collections.emptyMap())).isNotNull();
        assertThat(mapaAjusteMapper.toDto(null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null)).isNotNull();
    }

    @Test
    @DisplayName("UsuarioMapper - Cobertura de branches nulos")
    void usuarioMapperNulls() {
        assertThat(usuarioMapper.toUnidadeDto(null, false)).isNull();
        assertThat(usuarioMapper.toUsuarioDto(null)).isNull();
        assertThat(usuarioMapper.toAtribuicaoTemporariaDto(null)).isNull();

        Unidade u = new Unidade();
        u.setUnidadeSuperior(null);
        assertThat(usuarioMapper.toUnidadeDto(u, true).getCodigoPai()).isNull();

        Usuario user = new Usuario();
        user.setUnidadeLotacao(null);
        assertThat(usuarioMapper.toUsuarioDto(user).unidadeCodigo()).isNull();
    }

    @Test
    @DisplayName("MapaCompletoMapper - Cobertura de branches nulos")
    void mapaCompletoMapperNulls() {
        assertThat(mapaCompletoMapper.toDto(null)).isNull();
    }

    @Test
    @DisplayName("ParametroMapper - Cobertura de branches nulos")
    void parametroMapperNulls() {
        assertThat(parametroMapper.toResponse(null)).isNull();

        // atualizarEntidade (void)
        parametroMapper.atualizarEntidade(null, new sgc.configuracao.model.Parametro());
    }
}
