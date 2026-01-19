package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImpactoMapaServiceCoverageTest {

    @Mock private MapaRepo mapaRepo;
    @Mock private CompetenciaRepo competenciaRepo;
    @Mock private AtividadeService atividadeService;
    @Mock private AccessControlService accessControlService;

    @InjectMocks
    private ImpactoMapaService service;

    @Test
    @DisplayName("verificarImpactos - Sem Mapa Vigente")
    void verificarImpactos_SemMapaVigente() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setUnidade(new Unidade());
        subprocesso.getUnidade().setCodigo(1L);

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

        ImpactoMapaDto result = service.verificarImpactos(subprocesso, new Usuario());

        org.junit.jupiter.api.Assertions.assertFalse(result.isTemImpactos());
    }

    @Test
    @DisplayName("verificarImpactos - Conhecimentos Diferentes")
    void verificarImpactos_ConhecimentosDiferentes() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(new Unidade());
        subprocesso.getUnidade().setCodigo(1L);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(10L);
        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));

        Mapa mapaSubprocesso = new Mapa();
        mapaSubprocesso.setCodigo(20L);
        when(mapaRepo.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));

        // Atividades
        Atividade ativVigente = new Atividade();
        ativVigente.setCodigo(1L);
        ativVigente.setDescricao("Atividade A");
        ativVigente.setConhecimentos(List.of(new Conhecimento(1L, "C1", ativVigente)));

        Atividade ativAtual = new Atividade();
        ativAtual.setCodigo(2L);
        ativAtual.setDescricao("Atividade A"); // Same description
        ativAtual.setConhecimentos(List.of(new Conhecimento(2L, "C2", ativAtual))); // Different knowledge

        when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(List.of(ativVigente));
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(20L)).thenReturn(List.of(ativAtual));

        when(competenciaRepo.findByMapaCodigo(10L)).thenReturn(List.of());

        ImpactoMapaDto result = service.verificarImpactos(subprocesso, new Usuario());

        org.junit.jupiter.api.Assertions.assertTrue(result.isTemImpactos());
        org.junit.jupiter.api.Assertions.assertFalse(result.getAtividadesAlteradas().isEmpty());
    }

    @Test
    @DisplayName("verificarImpactos - Atividades Duplicadas")
    void verificarImpactos_AtividadesDuplicadas() {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(new Unidade());
        subprocesso.getUnidade().setCodigo(1L);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(10L);
        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));

        Mapa mapaSubprocesso = new Mapa();
        mapaSubprocesso.setCodigo(20L);
        when(mapaRepo.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));

        // Duplicate activities in current map
        Atividade ativ1 = new Atividade();
        ativ1.setCodigo(1L);
        ativ1.setDescricao("Atividade A");

        Atividade ativ2 = new Atividade();
        ativ2.setCodigo(2L);
        ativ2.setDescricao("Atividade A");

        when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(List.of());
        when(atividadeService.buscarPorMapaCodigoComConhecimentos(20L)).thenReturn(List.of(ativ1, ativ2));

        when(competenciaRepo.findByMapaCodigo(10L)).thenReturn(List.of());

        ImpactoMapaDto result = service.verificarImpactos(subprocesso, new Usuario());

        // Should handle duplicates without error
        org.junit.jupiter.api.Assertions.assertNotNull(result);
    }
}
