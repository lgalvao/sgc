package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.repo.ComumRepo;
import sgc.organizacao.model.*;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioConsultaService {
    private final UsuarioRepo usuarioRepo;
    private final ResponsabilidadeRepo responsabilidadeRepo;
    private final ComumRepo repo;
    private static final String ENTIDADE_USUARIO = "Usuário";

    public Usuario buscarPorId(String titulo) {
        return usuarioRepo.findById(titulo)
                .orElseThrow(ErroEntidadeNaoEncontrada.naoEncontrada(ENTIDADE_USUARIO, titulo));
    }

    public Optional<Usuario> buscarPorIdOpcional(String titulo) {
        return usuarioRepo.findById(titulo);
    }

    public Usuario buscarPorIdComAtribuicoes(String titulo) {
        return usuarioRepo.findByIdWithAtribuicoes(titulo)
                .orElseThrow(ErroEntidadeNaoEncontrada.naoEncontrada(ENTIDADE_USUARIO, titulo));
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

    public Usuario buscarChefePorUnidade(Long codigoUnidade, String siglaUnidade) {
        return repo.buscar(Responsabilidade.class, codigoUnidade).getUsuario();
    }

    public List<Usuario> buscarChefesPorUnidades(List<Long> codigosUnidades) {
        return responsabilidadeRepo.findByUnidadeCodigoIn(codigosUnidades).stream()
                .map(Responsabilidade::getUsuario)
                .toList();
    }

    public List<Usuario> buscarPorIdsComAtribuicoes(List<String> titulos) {
        return usuarioRepo.findByIdInWithAtribuicoes(titulos);
    }

    public List<Usuario> buscarTodosPorIds(List<String> titulos) {
        return usuarioRepo.findAllById(titulos);
    }
}
