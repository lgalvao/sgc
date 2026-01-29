package sgc.organizacao.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Administrador;
import sgc.organizacao.model.AdministradorRepo;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministradorRepositoryServiceTest {

    @Mock private AdministradorRepo repo;
    @InjectMocks private AdministradorRepositoryService service;

    @Test
    void findAll() {
        service.findAll();
        verify(repo).findAll();
    }

    @Test
    void existsById() {
        service.existsById("123");
        verify(repo).existsById("123");
    }

    @Test
    void count() {
        service.count();
        verify(repo).count();
    }

    @Test
    void salvar() {
        Administrador admin = new Administrador();
        when(repo.save(admin)).thenReturn(admin);
        service.salvar(admin);
        verify(repo).save(admin);
    }

    @Test
    void deleteById() {
        service.deleteById("123");
        verify(repo).deleteById("123");
    }
}
