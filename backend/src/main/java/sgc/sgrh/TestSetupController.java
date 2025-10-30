package sgc.sgrh;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller de teste para preparação de cenários E2E.
 * 
 * ATENÇÃO: Este controller está ativo APENAS com @Profile("e2e")
 * para garantir que não seja exposto em produção ou outros ambientes.
 * 
 * Os endpoints são idempotentes: inserem apenas se o registro não existir.
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Profile("e2e")
@Slf4j
public class TestSetupController {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Cria um usuário de teste se ele não existir.
     * 
     * Request body esperado:
     * {
     *   "tituloEleitoral": 777,
     *   "nome": "Teste Admin",
     *   "email": "teste@test.com",
     *   "ramal": "1234",
     *   "unidadeCodigo": 2,
     *   "perfis": ["ADMIN", "GESTOR"]
     * }
     * 
     * @param request Dados do usuário
     * @return ResponseEntity com mensagem de sucesso
     */
    @PostMapping("/usuarios")
    public ResponseEntity<Map<String, Object>> criarUsuario(@RequestBody Map<String, Object> request) {
        Long tituloEleitoral = ((Number) request.get("tituloEleitoral")).longValue();
        String nome = (String) request.get("nome");
        String email = (String) request.get("email");
        String ramal = (String) request.get("ramal");
        Integer unidadeCodigo = (Integer) request.get("unidadeCodigo");
        @SuppressWarnings("unchecked")
        List<String> perfis = (List<String>) request.get("perfis");

        // Verificar se o usuário já existe
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM SGC.USUARIO WHERE titulo_eleitoral = ?",
            Integer.class,
            tituloEleitoral
        );

        boolean created = false;
        if (count == null || count == 0) {
            // Inserir usuário
            jdbcTemplate.update(
                "INSERT INTO SGC.USUARIO (titulo_eleitoral, nome, email, ramal, unidade_codigo) VALUES (?, ?, ?, ?, ?)",
                tituloEleitoral, nome, email, ramal, unidadeCodigo
            );
            created = true;
            log.info("Usuário de teste criado: {} ({})", nome, tituloEleitoral);
        } else {
            log.info("Usuário de teste já existe: {} ({})", nome, tituloEleitoral);
        }

        // Inserir perfis (idempotente)
        if (perfis != null && !perfis.isEmpty()) {
            for (String perfil : perfis) {
                Integer perfilCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM SGC.USUARIO_PERFIL WHERE usuario_titulo_eleitoral = ? AND perfil = ?",
                    Integer.class,
                    tituloEleitoral, perfil
                );

                if (perfilCount == null || perfilCount == 0) {
                    jdbcTemplate.update(
                        "INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES (?, ?)",
                        tituloEleitoral, perfil
                    );
                    log.info("Perfil {} adicionado ao usuário {}", perfil, tituloEleitoral);
                }
            }
        }

        return ResponseEntity.ok(Map.of(
            "created", created,
            "tituloEleitoral", tituloEleitoral,
            "message", created ? "Usuário criado" : "Usuário já existia"
        ));
    }

    /**
     * Cria uma unidade de teste se ela não existir.
     * 
     * Request body esperado:
     * {
     *   "codigo": 999,
     *   "nome": "Unidade Teste",
     *   "sigla": "UTEST",
     *   "tipo": "OPERACIONAL",
     *   "unidadeSuperiorCodigo": 2
     * }
     * 
     * @param request Dados da unidade
     * @return ResponseEntity com mensagem de sucesso
     */
    @PostMapping("/unidades")
    public ResponseEntity<Map<String, Object>> criarUnidade(@RequestBody Map<String, Object> request) {
        Integer codigo = (Integer) request.get("codigo");
        String nome = (String) request.get("nome");
        String sigla = (String) request.get("sigla");
        String tipo = (String) request.get("tipo");
        Integer unidadeSuperiorCodigo = (Integer) request.get("unidadeSuperiorCodigo");

        // Verificar se a unidade já existe
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM SGC.UNIDADE WHERE codigo = ?",
            Integer.class,
            codigo
        );

        boolean created = false;
        if (count == null || count == 0) {
            // Inserir unidade
            jdbcTemplate.update(
                "INSERT INTO SGC.UNIDADE (codigo, nome, sigla, tipo, unidade_superior_codigo) VALUES (?, ?, ?, ?, ?)",
                codigo, nome, sigla, tipo, unidadeSuperiorCodigo
            );
            created = true;
            log.info("Unidade de teste criada: {} ({})", sigla, codigo);
        } else {
            log.info("Unidade de teste já existe: {} ({})", sigla, codigo);
        }

        return ResponseEntity.ok(Map.of(
            "created", created,
            "codigo", codigo,
            "message", created ? "Unidade criada" : "Unidade já existia"
        ));
    }

    /**
     * Cria um processo de teste com suas unidades participantes.
     * 
     * Request body esperado:
     * {
     *   "descricao": "Processo de Teste",
     *   "tipo": "MAPEAMENTO",
     *   "situacao": "EM_ELABORACAO",
     *   "dataLimite": "2025-12-31T23:59:59",
     *   "unidadesCodigos": [2, 3, 8]
     * }
     * 
     * @param request Dados do processo
     * @return ResponseEntity com código do processo criado
     */
    @PostMapping("/processos")
    public ResponseEntity<Map<String, Object>> criarProcesso(@RequestBody Map<String, Object> request) {
        String descricao = (String) request.get("descricao");
        String tipo = (String) request.get("tipo");
        String situacao = (String) request.get("situacao");
        String dataLimiteStr = (String) request.get("dataLimite");
        @SuppressWarnings("unchecked")
        List<Integer> unidadesCodigos = (List<Integer>) request.get("unidadesCodigos");

        LocalDateTime dataLimite = null;
        if (dataLimiteStr != null && !dataLimiteStr.isEmpty()) {
            dataLimite = LocalDateTime.parse(dataLimiteStr);
        }

        // Verificar se já existe um processo com essa descrição exata
        Integer existingId = null;
        try {
            existingId = jdbcTemplate.queryForObject(
                "SELECT codigo FROM SGC.PROCESSO WHERE descricao = ? LIMIT 1",
                Integer.class,
                descricao
            );
        } catch (Exception e) {
            // Não encontrado, vamos criar
        }

        Integer processoCodigo;
        boolean created = false;

        if (existingId == null) {
            // Inserir processo
            LocalDateTime dataCriacao = LocalDateTime.now();
            
            jdbcTemplate.update(
                "INSERT INTO SGC.PROCESSO (descricao, tipo, situacao, data_criacao, data_limite) VALUES (?, ?, ?, ?, ?)",
                descricao, tipo, situacao, dataCriacao, dataLimite
            );

            // Obter o ID do processo recém-criado
            processoCodigo = jdbcTemplate.queryForObject(
                "SELECT codigo FROM SGC.PROCESSO WHERE descricao = ? ORDER BY codigo DESC LIMIT 1",
                Integer.class,
                descricao
            );
            created = true;
            log.info("Processo de teste criado: {} (ID: {})", descricao, processoCodigo);
        } else {
            processoCodigo = existingId;
            log.info("Processo de teste já existe: {} (ID: {})", descricao, processoCodigo);
        }

        // Inserir unidades participantes (idempotente)
        if (unidadesCodigos != null && !unidadesCodigos.isEmpty()) {
            for (Integer unidadeCodigo : unidadesCodigos) {
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM SGC.UNIDADE_PROCESSO WHERE processo_codigo = ? AND unidade_codigo = ?",
                    Integer.class,
                    processoCodigo, unidadeCodigo
                );

                if (count == null || count == 0) {
                    jdbcTemplate.update(
                        "INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES (?, ?)",
                        processoCodigo, unidadeCodigo
                    );
                    log.info("Unidade {} adicionada ao processo {}", unidadeCodigo, processoCodigo);
                }
            }
        }

        return ResponseEntity.ok(Map.of(
            "created", created,
            "codigo", processoCodigo,
            "message", created ? "Processo criado" : "Processo já existia"
        ));
    }
}
