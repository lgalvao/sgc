package sgc.sgrh;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.sgrh.dto.EntrarRequest;
import sgc.sgrh.dto.*;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final SgrhService sgrhService;
    private final UnidadeRepo unidadeRepo;
    private final UsuarioRepo usuarioRepo;

    public void autenticar(long tituloEleitoral, String senha) {
        log.info("Simulando autenticação para: {}/{}", tituloEleitoral, senha);
        // A lógica de autenticação real seria implementada aqui.
    }

    public LoginResponse autorizar(long tituloEleitoral) {
        log.info("Buscando autorizações para o título: {}", tituloEleitoral);
        Usuario usuario = usuarioRepo.findById(tituloEleitoral)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Usuário não encontrado."));

        List<PerfilUnidade> pares = sgrhService.buscarPerfisUsuario(String.valueOf(tituloEleitoral)).stream()
            .map(dto -> {
                Unidade unidade = unidadeRepo.findById(dto.unidadeCodigo())
                    .orElseThrow(() -> new ErroDominioNaoEncontrado("Unidade não encontrada: " + dto.unidadeCodigo()));
                return PerfilUnidade.builder()
                    .perfil(Perfil.valueOf(dto.perfil()))
                    .unidade(unidade.getSigla())
                    .unidadeCodigo(unidade.getCodigo())
                    .build();
            })
            .collect(Collectors.toList());

        return LoginResponse.builder()
            .nome(usuario.getNome())
            .tituloEleitoral(usuario.getTituloEleitoral())
            .pares(pares)
            .build();
    }

    public UsuarioDto entrar(EntrarRequest request) {
        log.info("Finalizando login para: {}", request.getTituloEleitoral());
        Usuario usuario = usuarioRepo.findById(request.getTituloEleitoral())
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Usuário não encontrado."));

        // A lógica de geração de token JWT seria implementada aqui.
        String token = "jwt-token-simulado-para-" + request.getTituloEleitoral();

        return UsuarioDto.builder()
            .nome(usuario.getNome())
            .tituloEleitoral(usuario.getTituloEleitoral())
            .perfil(Perfil.valueOf(request.getPerfil()))
            .unidade(unidadeRepo.findById(request.getUnidadeCodigo()).get().getSigla())
            .token(token)
            .build();
    }
}