package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.model.TipoImpactoCompetencia;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Cobertura Extra: ImpactoMapaService")
class ImpactoMapaServiceCoverageTest {

    @Mock private MapaRepo mapaRepo;
    @Mock private CompetenciaRepo competenciaRepo;
    @Mock private MapaManutencaoService mapaManutencaoService;
    @Mock private AccessControlService accessControlService;

    @InjectMocks
    private ImpactoMapaService service;

    @Test
    @DisplayName("verificarImpactos: Falha quando mapa do subprocesso não existe")
    void verificarImpactos_MapaSubprocessoInexistente() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        Unidade u = new Unidade();
        u.setCodigo(100L);
        sp.setUnidade(u);

        when(mapaRepo.findMapaVigenteByUnidade(100L)).thenReturn(Optional.of(new Mapa()));
        when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verificarImpactos(sp, new Usuario()))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("verificarImpactos: Atividades duplicadas (mesma descrição) usa handler de colisão")
    void verificarImpactos_AtividadesDuplicadas() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        Unidade u = new Unidade();
        u.setCodigo(100L);
        sp.setUnidade(u);
        
        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(20L);
        
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(21L);

        when(mapaRepo.findMapaVigenteByUnidade(100L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSub));

        // Duplicatas na lista de atividades do mapa vigente
        Atividade a1 = new Atividade();
        a1.setCodigo(10L);
        a1.setDescricao("Mesma");
        
        Atividade a2 = new Atividade();
        a2.setCodigo(11L);
        a2.setDescricao("Mesma");
        
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(20L))
                .thenReturn(List.of(a1, a2));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(21L))
                .thenReturn(List.of());

        ImpactoMapaDto dto = service.verificarImpactos(sp, new Usuario());
        
        // Deve ter processado sem erro (handler de colisão keep-first)
        // Se tinha 2, removeu 1 (porque mapa atual vazio).
        // Na verdade, mapaVigentes map vai ter apenas 1 entrada ("Mesma" -> a1).
        // Mas detectarRemovidas itera sobre a LISTA de vigentes, entao remove as duas.
        assertThat(dto.atividadesRemovidas()).hasSize(2);
    }

    @Test
    @DisplayName("verificarImpactos: Atividade Alterada vinculada a Competencia gera impacto")
    void verificarImpactos_AtividadeAlteradaComCompetencia() {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(1L);
        Unidade u = new Unidade();
        u.setCodigo(100L);
        sp.setUnidade(u);
        
        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(20L);
        
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(21L);

        when(mapaRepo.findMapaVigenteByUnidade(100L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSub));

        // Atividade vigente
        Atividade aVigente = new Atividade();
        aVigente.setCodigo(10L);
        aVigente.setDescricao("Ativ 1");
        aVigente.setConhecimentos(List.of(new Conhecimento())); // Tem conhecimento

        // Atividade atual (mesma descricao, conhecimentos difs)
        Atividade aAtual = new Atividade();
        aAtual.setCodigo(10L); // mesmo codigo ou outro, importa descricao
        aAtual.setDescricao("Ativ 1");
        aAtual.setConhecimentos(List.of()); // Vazio -> alterada

        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(20L))
                .thenReturn(List.of(aVigente));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(21L))
                .thenReturn(List.of(aAtual));

        // Competencia vinculada a atividade vigente (ID 10L)
        Competencia comp = new Competencia();
        comp.setCodigo(50L);
        comp.setDescricao("Comp A");
        comp.setAtividades(Set.of(aVigente));
        
        when(competenciaRepo.findByMapaCodigo(20L)).thenReturn(List.of(comp));

        ImpactoMapaDto dto = service.verificarImpactos(sp, new Usuario());
        
        assertThat(dto.atividadesAlteradas()).hasSize(1);
        assertThat(dto.competenciasImpactadas()).hasSize(1);
        assertThat(dto.competenciasImpactadas().get(0).tiposImpacto())
            .contains(TipoImpactoCompetencia.ATIVIDADE_ALTERADA);
    }
}
