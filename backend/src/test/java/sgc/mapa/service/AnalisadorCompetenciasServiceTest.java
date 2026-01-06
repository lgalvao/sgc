package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.TipoImpactoCompetencia;

import java.util.Collections;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalisadorCompetenciasService Test")
class AnalisadorCompetenciasServiceTest {

    @InjectMocks
    private AnalisadorCompetenciasService analisador;

    @Test
    @DisplayName("Identificar impactos em atividade removida")
    void identificarImpactoRemovida() {
        Atividade a1 = new Atividade(); a1.setCodigo(1L); a1.setDescricao("Ativ 1");
        Competencia c1 = new Competencia(); c1.setCodigo(10L); c1.setDescricao("Comp 1");
        c1.setAtividades(java.util.Set.of(a1));

        AtividadeImpactadaDto rem = new AtividadeImpactadaDto();
        rem.setCodigo(1L); rem.setDescricao("Ativ 1");

        List<CompetenciaImpactadaDto> res = analisador.identificarCompetenciasImpactadas(
                List.of(c1),
                List.of(rem),
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getTipoImpacto()).isEqualTo(TipoImpactoCompetencia.ATIVIDADE_REMOVIDA);
    }

    @Test
    @DisplayName("Identificar impactos em atividade alterada")
    void identificarImpactoAlterada() {
        Atividade a1 = new Atividade(); a1.setCodigo(1L); a1.setDescricao("Ativ Nova");
        Competencia c1 = new Competencia(); c1.setCodigo(10L); c1.setDescricao("Comp 1");
        c1.setAtividades(java.util.Set.of(a1));

        AtividadeImpactadaDto alt = new AtividadeImpactadaDto();
        alt.setDescricao("Ativ Nova");
        alt.setDescricaoAnterior("Ativ Velha");

        // A lista de atividades vigentes deve conter a atividade com a descrição nova para fazer o match
        List<Atividade> vigentes = List.of(a1);

        List<CompetenciaImpactadaDto> res = analisador.identificarCompetenciasImpactadas(
                List.of(c1),
                Collections.emptyList(),
                List.of(alt),
                vigentes
        );

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getTipoImpacto()).isEqualTo(TipoImpactoCompetencia.ATIVIDADE_ALTERADA);
    }

    @Test
    @DisplayName("Identificar impacto misto (removida e alterada)")
    void identificarImpactoMisto() {
        Atividade a1 = new Atividade(); a1.setCodigo(1L); a1.setDescricao("Ativ Removida");
        Atividade a2 = new Atividade(); a2.setCodigo(2L); a2.setDescricao("Ativ Nova");

        Competencia c1 = new Competencia(); c1.setCodigo(10L); c1.setDescricao("Comp 1");
        // A competencia tem ambas atividades
        c1.setAtividades(java.util.Set.of(a1, a2));

        AtividadeImpactadaDto rem = new AtividadeImpactadaDto();
        rem.setCodigo(1L); rem.setDescricao("Ativ Removida");

        AtividadeImpactadaDto alt = new AtividadeImpactadaDto();
        alt.setDescricao("Ativ Nova"); alt.setDescricaoAnterior("Ativ Velha");

        List<Atividade> vigentes = List.of(a2);

        List<CompetenciaImpactadaDto> res = analisador.identificarCompetenciasImpactadas(
                List.of(c1),
                List.of(rem),
                List.of(alt),
                vigentes
        );

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getTipoImpacto()).isEqualTo(TipoImpactoCompetencia.IMPACTO_GENERICO);
    }

    @Test
    @DisplayName("Ignorar removida sem codigo")
    void ignorarRemovidaSemCodigo() {
        AtividadeImpactadaDto rem = new AtividadeImpactadaDto();
        rem.setCodigo(null);

        List<CompetenciaImpactadaDto> res = analisador.identificarCompetenciasImpactadas(
                List.of(), List.of(rem), List.of(), List.of()
        );
        assertThat(res).isEmpty();
    }

    @Test
    @DisplayName("Ignorar alterada sem descricao")
    void ignorarAlteradaSemDescricao() {
         AtividadeImpactadaDto alt = new AtividadeImpactadaDto();
         alt.setDescricao(null);
         List<CompetenciaImpactadaDto> res = analisador.identificarCompetenciasImpactadas(
                List.of(), List.of(), List.of(alt), List.of()
        );
        assertThat(res).isEmpty();
    }

    @Test
    @DisplayName("Ignorar alterada não encontrada em vigentes")
    void ignorarAlteradaNaoVigente() {
        AtividadeImpactadaDto alt = new AtividadeImpactadaDto();
        alt.setDescricao("X");
        List<CompetenciaImpactadaDto> res = analisador.identificarCompetenciasImpactadas(
                List.of(), List.of(), List.of(alt), List.of()
        );
        assertThat(res).isEmpty();
    }
}
