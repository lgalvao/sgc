package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.organizacao.model.UsuarioPerfilRepo;
import sgc.organizacao.model.UsuarioRepo;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsuarioRepositoryService {
    private static final String ENTIDADE_USUARIO = "Usuário";
    private final UsuarioRepo usuarioRepo;
    private final UsuarioPerfilRepo usuarioPerfilRepo;

    @Transactional(readOnly = true)
    public Optional<Usuario> findById(String titulo) {
        return usuarioRepo.findById(titulo);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(String titulo) {
        return usuarioRepo.findById(titulo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_USUARIO, titulo));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> findByIdWithAtribuicoes(String titulo) {
        return usuarioRepo.findByIdWithAtribuicoes(titulo);
    }

    @Transactional(readOnly = true)
    public List<Usuario> findByIdInWithAtribuicoes(List<String> titulos) {
        return usuarioRepo.findByIdInWithAtribuicoes(titulos);
    }

    @Transactional(readOnly = true)
    public List<Usuario> findAllById(List<String> titulos) {
        return usuarioRepo.findAllById(titulos);
    }

    @Transactional(readOnly = true)
    public List<Usuario> findByUnidadeLotacaoCodigo(Long codigoUnidade) {
        return usuarioRepo.findByUnidadeLotacaoCodigo(codigoUnidade);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepo.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> chefePorCodUnidade(Long codigoUnidade) {
        return usuarioRepo.chefePorCodUnidade(codigoUnidade);
    }

    @Transactional(readOnly = true)
    public List<Usuario> findChefesByUnidadesCodigos(List<Long> unidadesCodigos) {
        return usuarioRepo.findChefesByUnidadesCodigos(unidadesCodigos);
    }

    @Transactional(readOnly = true)
    public List<UsuarioPerfil> findByUsuarioTitulo(String usuarioTitulo) {
        return usuarioPerfilRepo.findByUsuarioTitulo(usuarioTitulo);
    }

    @Transactional(readOnly = true)
    public List<UsuarioPerfil> findByUsuarioTituloIn(List<String> titulos) {
        return usuarioPerfilRepo.findByUsuarioTituloIn(titulos);
    }

    @Transactional
    public Usuario salvar(Usuario usuario) {
        return usuarioRepo.save(usuario);
    }

    @Transactional
    public void deletar(String titulo) {
        usuarioRepo.deleteById(titulo);
    }
}
