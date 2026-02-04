package sgc.subprocesso.mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import sgc.analise.model.Analise;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.dto.MapaAjusteDto;
import sgc.subprocesso.model.Subprocesso;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-04T16:19:37-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.3.0.jar, environment: Java 25.0.1 (Amazon.com Inc.)"
)
@Component
public class MapaAjusteMapperImpl implements MapaAjusteMapper {

    @Override
    public MapaAjusteDto toDto(Subprocesso sp, Analise analise, List<Competencia> competencias, List<Atividade> atividades, List<Conhecimento> conhecimentos, Map<Long, Set<Long>> associacoes) {
        if ( sp == null && analise == null && competencias == null && atividades == null && conhecimentos == null ) {
            return null;
        }

        MapaAjusteDto.MapaAjusteDtoBuilder mapaAjusteDto = MapaAjusteDto.builder();

        if ( sp != null ) {
            mapaAjusteDto.codMapa( spMapaCodigo( sp ) );
            mapaAjusteDto.unidadeNome( spUnidadeNome( sp ) );
        }
        if ( analise != null ) {
            mapaAjusteDto.justificativaDevolucao( analise.getObservacoes() );
        }
        mapaAjusteDto.competencias( mapCompetencias(competencias, atividades, conhecimentos, associacoes) );

        return mapaAjusteDto.build();
    }

    private Long spMapaCodigo(Subprocesso subprocesso) {
        Mapa mapa = subprocesso.getMapa();
        if ( mapa == null ) {
            return null;
        }
        return mapa.getCodigo();
    }

    private String spUnidadeNome(Subprocesso subprocesso) {
        Unidade unidade = subprocesso.getUnidade();
        if ( unidade == null ) {
            return null;
        }
        return unidade.getNome();
    }
}
