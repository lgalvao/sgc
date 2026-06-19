package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Aquece os caches das views organizacionais ao inicializar a aplicação.
 *
 * <p>Executado após o contexto Spring estar completamente pronto (ApplicationReadyEvent),
 * garantindo que os caches estejam populados antes do primeiro acesso de usuário.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheAquecimento implements ApplicationListener<ApplicationReadyEvent> {

    private final CacheViewsOrganizacaoService cacheViewsOrganizacaoService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            log.info("Iniciando aquecimento dos caches organizacionais...");
            cacheViewsOrganizacaoService.listarTodasUnidades();
            cacheViewsOrganizacaoService.listarTodosUsuarios();
            cacheViewsOrganizacaoService.listarTodasResponsabilidades();
            cacheViewsOrganizacaoService.listarTodosPerfisUnidade();
            log.info("Caches organizacionais aquecidos com sucesso.");
        } catch (Exception e) {
            log.warn("Falha no aquecimento inicial dos caches", e);
        }
    }
}
