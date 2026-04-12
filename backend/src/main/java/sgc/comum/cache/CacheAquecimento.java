package sgc.comum.cache;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.boot.context.event.*;
import org.springframework.context.*;
import org.springframework.context.event.*;
import org.springframework.stereotype.*;
import sgc.organizacao.service.*;

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
    private final UnidadeHierarquiaService unidadeHierarquiaService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            log.info("Iniciando aquecimento dos caches organizacionais...");
            cacheViewsOrganizacaoService.listarTodasUnidades();
            cacheViewsOrganizacaoService.listarTodosUsuarios();
            cacheViewsOrganizacaoService.listarTodasResponsabilidades();
            cacheViewsOrganizacaoService.listarTodosPerfisUnidade();
            unidadeHierarquiaService.buscarArvoreHierarquica();
            unidadeHierarquiaService.buscarMapaHierarquia();
            unidadeHierarquiaService.buscarMapaFilhoPai();
            log.info("Caches organizacionais aquecidos com sucesso.");
        } catch (Exception e) {
            log.warn("Falha no aquecimento inicial dos caches", e);
        }
    }
}
