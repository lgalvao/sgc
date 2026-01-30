package sgc.organizacao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.model.Administrador;
import sgc.organizacao.model.AdministradorRepo;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdministradorService {
    private final AdministradorRepo administradorRepo;

    public List<Administrador> listarTodos() {
        return administradorRepo.findAll();
    }

    @Transactional
    public void adicionar(String usuarioTitulo) {
        if (administradorRepo.existsById(usuarioTitulo)) {
            throw new ErroValidacao("Usuário já é administrador");
        }

        Administrador administrador = Administrador.builder()
                .usuarioTitulo(usuarioTitulo)
                .build();
        administradorRepo.save(administrador);
    }

    @Transactional
    public void remover(String usuarioTitulo) {
        if (!administradorRepo.existsById(usuarioTitulo)) {
            throw new ErroValidacao("Usuário informado não é um administrador");
        }

        long totalAdministradores = administradorRepo.count();
        if (totalAdministradores <= 1) {
            throw new ErroValidacao("Não é permitido remover o único administrador do sistema");
        }

        administradorRepo.deleteById(usuarioTitulo);
    }

    public boolean isAdministrador(String usuarioTitulo) {
        return administradorRepo.existsById(usuarioTitulo);
    }
}
