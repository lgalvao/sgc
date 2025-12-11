package sgc.sgrh.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.sgrh.dto.EntrarReq;
import sgc.sgrh.dto.PerfilUnidade;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {
    private final UsuarioRepo usuarioRepo;
    private final UnidadeRepo unidadeRepo;
    private final sgc.sgrh.model.UsuarioPerfilRepo usuarioPerfilRepo;

    public boolean autenticar(String tituloEleitoral, String senha) {
        log.debug("Simulando autenticação para usuário: {}", tituloEleitoral);
        return true;
    }

    public List<PerfilUnidade> autorizar(String tituloEleitoral) {
        log.debug("Buscando autorizações (perfis e unidades) para o usuário: {}", tituloEleitoral);
        Usuario usuario =
                usuarioRepo
                        .findById(tituloEleitoral)
                        .orElseThrow(
                                () -> new ErroEntidadeNaoEncontrada("Usuário", tituloEleitoral));

        // Carregar atribuições da VIEW
        var atribuicoes = usuarioPerfilRepo.findByUsuarioTitulo(tituloEleitoral);
        usuario.setAtribuicoes(new java.util.HashSet<>(atribuicoes));

        return usuario.getTodasAtribuicoes().stream()
                .map(
                        atribuicao ->
                                new PerfilUnidade(
                                        atribuicao.getPerfil(),
                                        toUnidadeDto(atribuicao.getUnidade())))
                .toList();
    }

    private UnidadeDto toUnidadeDto(Unidade unidade) {
        return UnidadeDto.builder()
                .codigo(unidade.getCodigo())
                .nome(unidade.getNome())
                .sigla(unidade.getSigla())
                .codigoPai(
                        unidade.getUnidadeSuperior() != null
                                ? unidade.getUnidadeSuperior().getCodigo()
                                : null)
                .tipo(unidade.getTipo().name())
                .isElegivel(false)
                .build();
    }

    public void entrar(String tituloEleitoral, PerfilUnidade pu) {
        log.debug(
                "Usuário {} entrou. Perfil: {}, Unidade: {}",
                tituloEleitoral,
                pu.getPerfil(),
                pu.getSiglaUnidade());
    }

    public void entrar(EntrarReq request) {
        if (!unidadeRepo.existsById(request.getUnidadeCodigo())) {
            throw new ErroEntidadeNaoEncontrada(
                    "Unidade não encontrada, código: " + request.getUnidadeCodigo());
        }
        log.debug(
                "Usuário {} entrou via request. Perfil: {}, Unidade: {}",
                request.getTituloEleitoral(),
                request.getPerfil(),
                request.getUnidadeCodigo());
    }
}
