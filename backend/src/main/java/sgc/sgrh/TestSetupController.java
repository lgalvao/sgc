package sgc.sgrh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Profile("e2e")
@RestController
@RequestMapping("/api/test")
public class TestSetupController {

    @Autowired
    private JdbcTemplate jdbc;

    @PostMapping("/usuarios")
    @Transactional
    public ResponseEntity<?> ensureUsuario(@RequestBody Map<String, Object> body) {
        // Accept fields: titulo (or username), nome (or displayName), unidade (codigo or unidadeCodigo), perfis (array of strings)
        long titulo = Long.parseLong(body.getOrDefault("titulo", body.getOrDefault("username", "0")).toString());
        String nome = body.getOrDefault("nome", body.getOrDefault("displayName", "Usuario Teste")).toString();
        Object unidadeObj = body.getOrDefault("unidadeCodigo", body.getOrDefault("unidade", 2));
        int unidadeCodigo = Integer.parseInt(unidadeObj.toString());

        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM SGC.USUARIO WHERE TITULO_ELEITORAL = ?", Integer.class, titulo);
        if (count == null || count == 0) {
            String email = nome.toLowerCase().replaceAll("\\s+", ".") + "@example.com";
            jdbc.update("INSERT INTO SGC.USUARIO (TITULO_ELEITORAL, NOME, EMAIL, RAMAL, unidade_codigo) VALUES (?,?,?,?,?)",
                    titulo, nome, email, "0000", unidadeCodigo);
            Object perfisObj = body.get("perfis");
            if (perfisObj instanceof List) {
                List<?> perfis = (List<?>) perfisObj;
                for (Object p : perfis) {
                    jdbc.update("INSERT INTO SGC.USUARIO_PERFIL (usuario_titulo_eleitoral, perfil) VALUES (?,?)", titulo, p.toString());
                }
            }
        }
        return ResponseEntity.ok(Map.of("titulo", titulo, "nome", nome, "unidadeCodigo", unidadeCodigo));
    }

    @PostMapping("/unidades")
    @Transactional
    public ResponseEntity<?> ensureUnidade(@RequestBody Map<String, Object> body) {
        // Accept fields: codigo (int), nome (String), sigla (String), tipo (String), unidade_superior_codigo (Integer)
        int codigo = Integer.parseInt(body.getOrDefault("codigo", body.getOrDefault("sigla", "0")).toString());
        String nome = body.getOrDefault("nome", body.getOrDefault("descricao", "UN" + codigo)).toString();
        String sigla = body.getOrDefault("sigla", "U" + codigo).toString();
        String tipo = body.getOrDefault("tipo", "OPERACIONAL").toString();
        Object superior = body.getOrDefault("unidade_superior_codigo", body.getOrDefault("unidadeSuperior", null));
        Integer superiorCodigo = superior != null ? Integer.parseInt(superior.toString()) : null;

        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM SGC.UNIDADE WHERE codigo = ?", Integer.class, codigo);
        if (count == null || count == 0) {
            if (superiorCodigo == null) {
                jdbc.update("INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (?,?,?,?,NULL)",
                        codigo, nome, sigla, tipo);
            } else {
                jdbc.update("INSERT INTO SGC.UNIDADE (codigo, NOME, SIGLA, TIPO, unidade_superior_codigo) VALUES (?,?,?,?,?)",
                        codigo, nome, sigla, tipo, superiorCodigo);
            }
        }
        return ResponseEntity.ok(Map.of("codigo", codigo, "nome", nome, "sigla", sigla));
    }

    @PostMapping("/processos")
    @Transactional
    public ResponseEntity<?> ensureProcesso(@RequestBody Map<String, Object> body) {
        // Accept fields: descricao, tipo, situacao, dataLimite (yyyy-MM-dd), unidades (list of unidade_codigo)
        String descricao = body.getOrDefault("descricao", "Processo Teste").toString();
        String tipo = body.getOrDefault("tipo", "MAPEAMENTO").toString();
        String situacao = body.getOrDefault("situacao", "CRIADO").toString();
        String dataLimiteStr = body.getOrDefault("dataLimite", LocalDate.now().plusDays(30).toString()).toString();

        // If a processo with same descricao exists, return it
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM SGC.PROCESSO WHERE descricao = ?", Integer.class, descricao);
        if (count != null && count > 0) {
            Integer existing = jdbc.queryForObject("SELECT CODIGO FROM SGC.PROCESSO WHERE descricao = ? LIMIT 1", Integer.class, descricao);
            return ResponseEntity.ok(Map.of("codigo", existing, "descricao", descricao));
        }

        jdbc.update("INSERT INTO SGC.PROCESSO (descricao, tipo, situacao, data_criacao, data_limite) VALUES (?,?,?,?,?)",
                descricao, tipo, situacao, LocalDate.now(), java.sql.Date.valueOf(dataLimiteStr));

        Integer codigo = jdbc.queryForObject("SELECT CODIGO FROM SGC.PROCESSO WHERE descricao = ? LIMIT 1", Integer.class, descricao);

        Object unidadesObj = body.get("unidades");
        if (unidadesObj instanceof List) {
            List<?> unidades = (List<?>) unidadesObj;
            for (Object u : unidades) {
                int unidadeCodigo = Integer.parseInt(u.toString());
                jdbc.update("INSERT INTO SGC.UNIDADE_PROCESSO (processo_codigo, unidade_codigo) VALUES (?,?)", codigo, unidadeCodigo);
            }
        }

        return ResponseEntity.ok(Map.of("codigo", codigo, "descricao", descricao));
    }
}