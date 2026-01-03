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
import sgc.mapa.model.TipoImpactoAtividade;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para AnalisadorCompetenciasService")
class AnalisadorCompetenciasServiceTest {

    @InjectMocks
    private AnalisadorCompetenciasService analisador;

    @Test
    @DisplayName("Deve construir mapa de atividade para competências corretamente")
    void deveConstruirMapaAtividadeCompetencias() {
        // Arrange
        Atividade a1 = new Atividade();
        a1.setCodigo(1L);

        Competencia c1 = new Competencia();
        c1.setCodigo(10L);
        c1.setDescricao("Competencia 1");

        // Configurando a relação bidirecional (ou unidirecional conforme a lógica de serviço espera)
        // O serviço itera sobre as competências e verifica c.getAtividades()
        Set<Atividade> atividades = new HashSet<>();
        atividades.add(a1);
        c1.setAtividades(atividades);

        List<Competencia> competencias = List.of(c1);

        // Act
        Map<Long, List<Competencia>> resultado = analisador.construirMapaAtividadeCompetencias(competencias);

        // Assert
        assertThat(resultado).containsKey(1L);
        assertThat(resultado.get(1L)).hasSize(1);
        assertThat(resultado.get(1L).getFirst().getDescricao()).isEqualTo("Competencia 1");
    }

    @Test
    @DisplayName("Deve identificar competências impactadas por remoção de atividade")
    void deveIdentificarImpactadasPorRemocao() {
        // Arrange
        AtividadeImpactadaDto removida = AtividadeImpactadaDto.builder()
                .codigo(1L)
                .tipoImpacto(TipoImpactoAtividade.REMOVIDA)
                .build();

        Competencia c1 = new Competencia();
        c1.setCodigo(10L);
        c1.setDescricao("Comp 1");

        // Simular que esta competência estava ligada à atividade removida
        Atividade a1 = new Atividade();
        a1.setCodigo(1L);

        Set<Atividade> atividades = new HashSet<>();
        atividades.add(a1);
        c1.setAtividades(atividades);

        List<Competencia> todasCompetencias = List.of(c1);
        List<AtividadeImpactadaDto> removidas = List.of(removida);
        List<AtividadeImpactadaDto> alteradas = List.of();
        List<Atividade> atividadesVigentes = List.of(a1);

        // Act
        List<CompetenciaImpactadaDto> resultado = analisador.identificarCompetenciasImpactadas(
                todasCompetencias, removidas, alteradas, atividadesVigentes);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getDescricao()).isEqualTo("Comp 1");
        // DTO não tem getImpacto, verificamos tipoImpacto
        assertThat(resultado.getFirst().getTipoImpacto()).isEqualTo(sgc.mapa.model.TipoImpactoCompetencia.ATIVIDADE_REMOVIDA);

        // Edge case
        assertFalse(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar vazio se não houver impactos")
    void deveRetornarVazioSeSemImpactos() {
         List<CompetenciaImpactadaDto> resultado = analisador.identificarCompetenciasImpactadas(
                List.of(), List.of(), List.of(), List.of());

         assertTrue(resultado.isEmpty());
    }
}
