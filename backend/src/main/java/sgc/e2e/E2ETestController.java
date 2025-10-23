package sgc.e2e;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sgc.alerta.modelo.AlertaRepo;
import sgc.alerta.modelo.AlertaUsuarioRepo;
import sgc.analise.modelo.AnaliseRepo;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.e2e.dto.LoginTestRequest;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.processo.SituacaoProcesso;
import sgc.processo.modelo.TipoProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.processo.modelo.UnidadeProcessoRepo;
import sgc.sgrh.Perfil;
import sgc.sgrh.SgrhService;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.sgrh.dto.PerfilDto;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.SituacaoUnidade;
import sgc.unidade.modelo.TipoUnidade;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/test")
@Profile("e2e")
@RequiredArgsConstructor
public class E2ETestController {

    private final UsuarioRepo usuarioRepo;
    private final E2ESeederService e2eSeederService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginTestRequest request) {
        Usuario usuario = usuarioRepo.findByTituloEleitoral(Long.parseLong(request.getUsername()))
                .orElseThrow(() -> new RuntimeException("Usuário de teste não encontrado: " + request.getUsername()));

        UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authReq);

        return ResponseEntity.ok("Login de teste realizado com sucesso para: " + usuario.getUsername());
    }



    @PostMapping("/seed")
    public ResponseEntity<Map<String, Object>> seed() {
        return ResponseEntity.ok(e2eSeederService.seedData());
    }
}