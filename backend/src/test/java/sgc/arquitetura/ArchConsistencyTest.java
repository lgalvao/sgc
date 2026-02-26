package sgc.arquitetura;

import com.fasterxml.jackson.annotation.*;
import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.core.importer.*;
import com.tngtech.archunit.junit.*;
import com.tngtech.archunit.lang.*;
import jakarta.persistence.*;
import org.jspecify.annotations.*;
import org.junit.jupiter.api.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.*;

@Tag("integration")
@AnalyzeClasses(packages = "sgc", importOptions = {
        ImportOption.DoNotIncludeTests.class,
        ImportOption.DoNotIncludeJars.class}
)
public class ArchConsistencyTest {
    @ArchTest
    static final ArchRule controllers_should_not_access_repositories = noClasses()
            .that()
            .haveNameMatching(".*Controller")
            .should()
            .accessClassesThat()
            .haveNameMatching(".*Repo");

    @ArchTest
    static final ArchRule comum_package_should_not_contain_business_logic = noClasses()
            .that()
            .resideInAPackage("sgc.comum..")
            .should()
            .haveNameMatching(".*Controller")
            .orShould()
            .haveNameMatching(".*Service");

    @ArchTest
    static final ArchRule services_should_not_access_other_modules_repositories = classes()
            .that()
            .haveSimpleNameEndingWith("Service")
            .should(new ArchCondition<>("only access repositories of their own module") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    String itemPackage = item.getPackageName();
                    String itemModule = extractModule(itemPackage);

                    if (itemModule == null) return;

                    for (Dependency dependency : item.getDirectDependenciesFromSelf()) {
                        JavaClass targetClass = dependency.getTargetClass();
                        if (targetClass.getSimpleName().endsWith("Repo")) {
                            String dependencyPackage = targetClass.getPackageName();
                            String dependencyModule = extractModule(dependencyPackage);

                            if (dependencyModule != null && !dependencyModule.equals(itemModule) && !dependencyModule.equals("comum")) {
                                String message = String.format(
                                        "Service %s (module %s) accesses Repository %s (module %s)",
                                        item.getName(), itemModule, targetClass.getName(), dependencyModule);
                                events.add(SimpleConditionEvent.violated(dependency, message));
                            }
                        }
                    }
                }

                private String extractModule(String packageName) {
                    if (!packageName.startsWith("sgc.")) return null;

                    int firstDot = packageName.indexOf('.');
                    if (firstDot == -1) return null;

                    int secondDot = packageName.indexOf('.', firstDot + 1);
                    if (secondDot == -1) return packageName.substring(firstDot + 1);

                    return packageName.substring(firstDot + 1, secondDot);
                }
            });

    @ArchTest
    static final ArchRule controllers_e_services_devem_estar_em_pacotes_null_marked = classes()
            .that().haveSimpleNameEndingWith("Controller")
            .or().haveSimpleNameEndingWith("Service")
            .should(new ArchCondition<>("residir em pacote anotado com @NullMarked") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    JavaPackage javaPackage = item.getPackage();
                    boolean pacoteNullMarked = javaPackage.isAnnotatedWith(NullMarked.class);

                    if (!pacoteNullMarked) {
                        String mensagem = String.format("%s não está em um pacote @NullMarked (pacote: %s)",
                                item.getSimpleName(), javaPackage.getName());
                        events.add(SimpleConditionEvent.violated(item, mensagem));
                    }
                }
            })
            .because("Controllers e Services devem estar em pacotes @NullMarked para garantir null-safety");



    @ArchTest
    static final ArchRule facades_should_have_facade_suffix = classes()
            .that()
            .resideInAPackage("..service..")
            .and()
            .areAnnotatedWith(Service.class)
            .and()
            .haveSimpleNameContaining("Facade")
            .should()
            .haveSimpleNameEndingWith("Facade")
            .allowEmptyShould(true)
            .because("Facade classes should have 'Facade' as suffix for consistency");

    @ArchTest
    static final ArchRule dtos_should_not_be_jpa_entities = noClasses()
            .that()
            .haveSimpleNameEndingWith("Dto")
            .should()
            .beAnnotatedWith(Entity.class)
            .because("DTOs should never be JPA entities - use separate entity classes");

    /**
     * Garante que entidades JPA só sejam retornadas por controllers quando houver
     * {@code @JsonView} explícito no metodo.
     */
    @ArchTest
    static final ArchRule controllers_should_not_return_jpa_entities_without_json_view = methods()
            .that()
            .arePublic()
            .and()
            .areDeclaredInClassesThat()
            .areAnnotatedWith(RestController.class)
            .should(new ArchCondition<>("not return JPA entities without @JsonView") {
                @Override
                public void check(JavaMethod method, ConditionEvents events) {
                    JavaClass retorno = method.getRawReturnType();
                    boolean retornaEntidade = retorno.isAnnotatedWith(Entity.class);
                    boolean possuiJsonView = method.isAnnotatedWith(JsonView.class);
                    if (retornaEntidade && !possuiJsonView) {
                        String mensagem = String.format(
                                "Método %s.%s retorna entidade JPA (%s) sem @JsonView",
                                method.getOwner().getSimpleName(), method.getName(), retorno.getSimpleName());
                        events.add(SimpleConditionEvent.violated(method, mensagem));
                    }
                }
            })
            .because("Entidades JPA só podem ser expostas em controllers com @JsonView explícito");

    /**
     * Garante nomenclatura consistente de Controllers.
     */
    @ArchTest
    static final ArchRule controllers_should_have_controller_suffix = classes()
            .that()
            .areAnnotatedWith(RestController.class)
            .should()
            .haveSimpleNameEndingWith("Controller")
            .because("Controllers should have 'Controller' suffix for consistency");

    /**
     * Garante nomenclatura consistente de Repositories.
     */
    @ArchTest
    static final ArchRule repositories_should_have_repo_suffix = classes()
            .that()
            .areAssignableTo(JpaRepository.class)
            .should()
            .haveSimpleNameEndingWith("Repo")
            .because("Repositories should have 'Repo' suffix for consistency");


    /**
     * Verifica ausência de ciclos internos nos pacotes workflow de cada módulo.
     *
     * <p>Garante que pacotes internos de service (workflow, crud, etc.) não criem
     * dependências circulares entre si dentro do mesmo módulo.
     *
     * <p><b>Nota:</b> @Lazy pode ser usado para quebrar ciclos internos quando
     * houver necessidade legítima de referências mútuas.
     */
    @ArchTest
    static final ArchRule no_cycles_within_service_packages = slices()
            .matching("sgc.(*).service.(**)")
            .should()
            .beFreeOfCycles()
            .allowEmptyShould(true);
}
