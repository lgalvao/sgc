package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;

import java.util.List;
import java.util.Optional;
import sgc.comum.repo.ComumRepo;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioConsultaService {
    private final UsuarioRepo usuarioRepo;
    private final ComumRepo repo;

    public Usuario buscarPorId(String titulo) {
        return repo.buscar(Usuario.class, titulo);
    }

    public Optional<Usuario> buscarPorIdOpcional(String titulo) {
        return usuarioRepo.findById(titulo);
    }

    public Usuario buscarPorIdComAtribuicoes(String titulo) {
        return repo.buscar(Usuario.class, titulo);
    }

    public Optional<Usuario> buscarPorIdComAtribuicoesOpcional(String titulo) {
        return usuarioRepo.findByIdWithAtribuicoes(titulo);
    }

    public List<Usuario> buscarPorUnidadeLotacao(Long codigoUnidade) {
        return usuarioRepo.findByUnidadeLotacaoCodigo(codigoUnidade);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepo.findByEmail(email);
    }

    public List<Usuario> buscarTodos() {
        return usuarioRepo.findAll();
    }

    public List<Usuario> buscarTodosPorIds(List<String> titulos) {
        return usuarioRepo.findAllById(titulos);
    }
}
