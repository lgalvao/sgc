package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.model.Administrador;
import sgc.organizacao.model.AdministradorRepo;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdministradorRepositoryService {
    private final AdministradorRepo administradorRepo;

    @Transactional(readOnly = true)
    public List<Administrador> findAll() {
        return administradorRepo.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsById(String usuarioTitulo) {
        return administradorRepo.existsById(usuarioTitulo);
    }

    @Transactional(readOnly = true)
    public long count() {
        return administradorRepo.count();
    }

    @Transactional
    public Administrador salvar(Administrador administrador) {
        return administradorRepo.save(administrador);
    }

    @Transactional
    public void deleteById(String usuarioTitulo) {
        administradorRepo.deleteById(usuarioTitulo);
    }
}
