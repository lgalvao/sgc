package sgc.processo.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.cache.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.processo.model.*;

import java.util.Locale;

@Service
@Profile("hom")
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProcessoExclusaoCompletaService {
    private static final String SUBQUERY_SUBPROCESSOS = "(SELECT codigo FROM sgc.subprocesso WHERE processo_codigo = ?)";
    private static final String SUBQUERY_MAPAS = "(SELECT codigo FROM sgc.mapa WHERE subprocesso_codigo IN " + SUBQUERY_SUBPROCESSOS + ")";
    private static final String SUBQUERY_ATIVIDADES = "(SELECT codigo FROM sgc.atividade WHERE mapa_codigo IN " + SUBQUERY_MAPAS + ")";
    private static final String SUBQUERY_COMPETENCIAS = "(SELECT codigo FROM sgc.competencia WHERE mapa_codigo IN " + SUBQUERY_MAPAS + ")";
    private static final String SUBQUERY_DIAGNOSTICOS = "(SELECT codigo FROM sgc.diagnostico WHERE subprocesso_codigo IN " + SUBQUERY_SUBPROCESSOS + ")";

    private final JdbcTemplate jdbcTemplate;
    private final ProcessoRepo processoRepo;
    private final CacheManager cacheManager;

    public void excluirCompleto(Long codigo) {
        if (!processoRepo.existsById(codigo)) {
            throw new ErroEntidadeNaoEncontrada("Processo", codigo);
        }

        jdbcTemplate.update("""
                DELETE FROM sgc.alerta_usuario
                WHERE alerta_codigo IN (SELECT codigo FROM sgc.alerta WHERE processo_codigo = ?)""", codigo);
        jdbcTemplate.update("DELETE FROM sgc.alerta WHERE processo_codigo = ?", codigo);

        excluirDiagnosticoSePresente(codigo);

        jdbcTemplate.update("DELETE FROM sgc.conhecimento WHERE atividade_codigo IN " + SUBQUERY_ATIVIDADES, codigo);
        jdbcTemplate.update("DELETE FROM sgc.competencia_atividade WHERE atividade_codigo IN " + SUBQUERY_ATIVIDADES, codigo);
        jdbcTemplate.update("DELETE FROM sgc.competencia_atividade WHERE competencia_codigo IN " + SUBQUERY_COMPETENCIAS, codigo);
        jdbcTemplate.update("DELETE FROM sgc.unidade_mapa WHERE mapa_vigente_codigo IN " + SUBQUERY_MAPAS, codigo);
        jdbcTemplate.update("DELETE FROM sgc.atividade WHERE mapa_codigo IN " + SUBQUERY_MAPAS, codigo);
        jdbcTemplate.update("DELETE FROM sgc.competencia WHERE mapa_codigo IN " + SUBQUERY_MAPAS, codigo);
        jdbcTemplate.update("DELETE FROM sgc.mapa WHERE subprocesso_codigo IN " + SUBQUERY_SUBPROCESSOS, codigo);

        jdbcTemplate.update("DELETE FROM sgc.analise WHERE subprocesso_codigo IN " + SUBQUERY_SUBPROCESSOS, codigo);
        jdbcTemplate.update("DELETE FROM sgc.notificacao WHERE subprocesso_codigo IN " + SUBQUERY_SUBPROCESSOS, codigo);
        jdbcTemplate.update("DELETE FROM sgc.movimentacao WHERE subprocesso_codigo IN " + SUBQUERY_SUBPROCESSOS, codigo);
        jdbcTemplate.update("DELETE FROM sgc.subprocesso WHERE processo_codigo = ?", codigo);
        jdbcTemplate.update("DELETE FROM sgc.unidade_processo WHERE processo_codigo = ?", codigo);
        jdbcTemplate.update("DELETE FROM sgc.processo WHERE codigo = ?", codigo);

        limparCaches();
        log.warn("Processo {} e seus dependentes foram excluidos via endpoint administrativo de homologacao.", codigo);
    }

    private void excluirDiagnosticoSePresente(Long codigoProcesso) {
        if (tabelaExiste("AVALIACAO_SERVIDOR")) {
            jdbcTemplate.update("""
                    DELETE FROM sgc.avaliacao_servidor
                    WHERE diagnostico_codigo IN """ + SUBQUERY_DIAGNOSTICOS +
                    " OR competencia_codigo IN " + SUBQUERY_COMPETENCIAS, codigoProcesso, codigoProcesso);
        }

        if (tabelaExiste("OCUPACAO_CRITICA")) {
            jdbcTemplate.update("""
                    DELETE FROM sgc.ocupacao_critica
                    WHERE diagnostico_codigo IN """ + SUBQUERY_DIAGNOSTICOS +
                    " OR competencia_codigo IN " + SUBQUERY_COMPETENCIAS, codigoProcesso, codigoProcesso);
        }

        if (tabelaExiste("DIAGNOSTICO")) {
            jdbcTemplate.update("DELETE FROM sgc.diagnostico WHERE subprocesso_codigo IN " + SUBQUERY_SUBPROCESSOS, codigoProcesso);
        }
    }

    private boolean tabelaExiste(String nomeTabela) {
        Integer quantidade = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_tables WHERE table_name = ?",
                Integer.class,
                nomeTabela.toUpperCase(Locale.ROOT)
        );
        return quantidade != null && quantidade > 0;
    }

    private void limparCaches() {
        cacheManager.getCacheNames().forEach(nomeCache -> {
            Cache cache = cacheManager.getCache(nomeCache);
            if (cache != null) {
                cache.clear();
            }
        });
    }
}
