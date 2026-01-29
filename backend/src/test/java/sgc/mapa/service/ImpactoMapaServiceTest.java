package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.AccessControlService;
import sgc.seguranca.acesso.Acao;
import sgc.subprocesso.model.Subprocesso;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImpactoMapaServiceTest {

    @Mock private MapaRepo mapaRepo;
    @Mock private CompetenciaRepo competenciaRepo;
    @Mock private MapaManutencaoService mapaManutencaoService;
    @Mock private AccessControlService accessControlService;

    @InjectMocks
    private ImpactoMapaService impactoMapaService;

    @Test
    @DisplayName("Deve retornar sem impacto se não houver mapa vigente")
    void semMapaVigente() {
        Usuario usuario = new Usuario();
        Subprocesso subprocesso = new Subprocesso();
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

        ImpactoMapaDto result = impactoMapaService.verificarImpactos(subprocesso, usuario);

        assertNotNull(result);
        assertTrue(result.atividadesInseridas().isEmpty());
        assertTrue(result.atividadesRemovidas().isEmpty());
        assertTrue(result.atividadesAlteradas().isEmpty());
        
        verify(accessControlService).verificarPermissao(usuario, Acao.VERIFICAR_IMPACTOS, subprocesso);
    }

    @Test
    @DisplayName("Deve detectar atividade inserida")
    void deveDetectarInserida() {
        Usuario usuario = new Usuario();
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(200L);

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.findBySubprocessoCodigo(10L)).thenReturn(Optional.of(mapaSub));

        // Vigente: vazio
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L)).thenReturn(Collections.emptyList());
        
        // Atual: 1 atividade
        Atividade nova = new Atividade();
        nova.setCodigo(1L);
        nova.setDescricao("Nova");
        nova.setConhecimentos(Collections.emptyList());
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(200L)).thenReturn(Collections.singletonList(nova));

        when(competenciaRepo.findByMapaCodigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaDto result = impactoMapaService.verificarImpactos(subprocesso, usuario);

        assertEquals(1, result.atividadesInseridas().size());
        assertEquals("Nova", result.atividadesInseridas().get(0).descricao());
    }

    @Test
    @DisplayName("Deve detectar atividade removida e impacto em competência")
    void deveDetectarRemovida() {
        Usuario usuario = new Usuario();
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(200L);

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.findBySubprocessoCodigo(10L)).thenReturn(Optional.of(mapaSub));

        // Vigente: 1 atividade "Antiga"
        Atividade antiga = new Atividade();
        antiga.setCodigo(1L);
        antiga.setDescricao("Antiga");
        antiga.setConhecimentos(Collections.emptyList());
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L)).thenReturn(Collections.singletonList(antiga));
        
        // Atual: vazio (foi removida)
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(200L)).thenReturn(Collections.emptyList());

        // Competencia ligada à atividade antiga
        Competencia comp = new Competencia();
        comp.setCodigo(50L);
        comp.setDescricao("Comp A");
        comp.setAtividades(java.util.Set.of(antiga));
        when(competenciaRepo.findByMapaCodigo(100L)).thenReturn(Collections.singletonList(comp));

        ImpactoMapaDto result = impactoMapaService.verificarImpactos(subprocesso, usuario);

        assertEquals(1, result.atividadesRemovidas().size());
        assertEquals("Antiga", result.atividadesRemovidas().get(0).descricao());
        
        assertEquals(1, result.competenciasImpactadas().size());
        assertEquals("Comp A", result.competenciasImpactadas().get(0).descricao());
    }

    @Test
    @DisplayName("Deve detectar atividade alterada (conhecimentos diferentes)")
    void deveDetectarAlterada() {
        Usuario usuario = new Usuario();
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(200L);

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.findBySubprocessoCodigo(10L)).thenReturn(Optional.of(mapaSub));

        // Vigente: Ativ A com Conhecimento C1
        Atividade ativVigente = new Atividade();
        ativVigente.setCodigo(1L);
        ativVigente.setDescricao("Ativ A");
        Conhecimento c1 = new Conhecimento();
        c1.setDescricao("C1");
        ativVigente.setConhecimentos(Collections.singletonList(c1));
        
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L)).thenReturn(Collections.singletonList(ativVigente));
        
        // Atual: Ativ A com Conhecimento C2 (alterado)
        Atividade ativAtual = new Atividade();
        ativAtual.setCodigo(2L); // ID pode ser diferente se for recriado, mas descrição igual
        ativAtual.setDescricao("Ativ A");
        Conhecimento c2 = new Conhecimento();
        c2.setDescricao("C2");
        ativAtual.setConhecimentos(Collections.singletonList(c2));
        
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(200L)).thenReturn(Collections.singletonList(ativAtual));

        when(competenciaRepo.findByMapaCodigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaDto result = impactoMapaService.verificarImpactos(subprocesso, usuario);

        assertEquals(1, result.atividadesAlteradas().size());
        assertEquals("Ativ A", result.atividadesAlteradas().get(0).descricao());
    }
}
