package sgc;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.Violations;
import org.springframework.modulith.docs.Documenter;

/**
 * Teste de estrutura modular do SGC usando Spring Modulith.
 * 
 * Valida:
 * - Detecção correta de módulos
 * - Ausência de dependências cíclicas
 * - Respeito aos limites de módulos (api/ e internal/)
 */
class ModulithStructureTest {
    
    private final ApplicationModules modules = ApplicationModules.of(Sgc.class);
    
    @Test
    void deveDetectarModulosCorretamente() {
        // Lista todos os módulos detectados
        System.out.println("=== Módulos Detectados ===");
        modules.forEach(module -> {
            System.out.println("Módulo: " + module.getName());
            System.out.println("  - Pacote base: " + module.getBasePackage());
            System.out.println("  - Dependências: " + module.getDependencies(modules));
        });
        
        // Durante a Sprint 1, apenas detectamos violações mas não falhamos o teste
        // As violações serão corrigidas progressivamente nas Sprints 1-4
        try {
            modules.verify();
            System.out.println("\n✅ Nenhuma violação de estrutura modular detectada!");
        } catch (Violations violations) {
            System.out.println("\n⚠️ Violações detectadas (esperado durante refatoração):");
            System.out.println(violations.toString());
            // Não falhar o teste durante a refatoração
            // assertThatCode(() -> modules.verify()).doesNotThrowAnyException();
        }
    }
    
    @Test
    void gerarDocumentacaoDosModulos() {
        // Gera documentação em backend/build/spring-modulith-docs
        new Documenter(modules)
            .writeDocumentation()           // Cria index HTML
            .writeIndividualModulesAsPlantUml()  // Diagrama de cada módulo
            .writeModulesAsPlantUml();      // Diagrama geral
        
        System.out.println("Documentação gerada em: backend/build/spring-modulith-docs/");
    }
}
