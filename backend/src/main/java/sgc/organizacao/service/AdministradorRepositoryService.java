package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.model.Administrador;
import sgc.organizacao.model.AdministradorRepo;

import java.util.List;

/**
 * Serviço de acesso a dados para Administradores.
 * Encapsula o acesso ao repositório de administradores.
 */
@Service
@RequiredArgsConstructor
public class AdministradorRepositoryService {
    private final AdministradorRepo administradorRepo;

    @Transactional(readOnly = true)
    public List<Administrador> listarTodos() {
        return administradorRepo.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existePorTitulo(String titulo) {
        return administradorRepo.existsById(titulo);
    }

    @Transactional(readOnly = true)
    public long contar() {
        return administradorRepo.count();
    }

    @Transactional
    public Administrador salvar(Administrador administrador) {
        return administradorRepo.save(administrador);
    }

    @Transactional
    public void removerPorTitulo(String titulo) {
        administradorRepo.deleteById(titulo);
    }
}
