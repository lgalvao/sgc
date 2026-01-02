package sgc.usuario;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.usuario.dto.AdministradorDto;
import sgc.usuario.mapper.UsuarioMapper;
import sgc.usuario.model.Administrador;
import sgc.usuario.model.AdministradorRepo;
import sgc.usuario.model.Usuario;
import sgc.usuario.model.UsuarioRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdministradorService {
    private final AdministradorRepo administradorRepo;
    private final UsuarioRepo usuarioRepo;

    @Transactional(readOnly = true)
    public List<AdministradorDto> listarAdministradores() {
        log.debug("Listando todos os administradores");
        return administradorRepo.findAll().stream()
                .map(admin -> {
                    Usuario usuario = usuarioRepo.findById(admin.getUsuarioTitulo())
                            .orElse(null);
                    if (usuario == null) {
                        log.warn("Administrador {} não encontrado na base de usuários", admin.getUsuarioTitulo());
                        return null;
                    }
                    return toAdministradorDto(usuario);
                })
                .filter(dto -> dto != null)
                .toList();
    }

    @Transactional
    public AdministradorDto adicionarAdministrador(String usuarioTitulo) {
        log.info("Adicionando administrador: {}", usuarioTitulo);
        
        // Verificar se o usuário existe
        Usuario usuario = usuarioRepo.findById(usuarioTitulo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Usuário", usuarioTitulo));
        
        // Verificar se já é administrador
        if (administradorRepo.existsById(usuarioTitulo)) {
            throw new ErroValidacao("Usuário já é administrador");
        }
        
        Administrador administrador = new Administrador(usuarioTitulo);
        administradorRepo.save(administrador);
        
        log.info("Administrador {} adicionado com sucesso", usuarioTitulo);
        return toAdministradorDto(usuario);
    }

    @Transactional
    public void removerAdministrador(String usuarioTitulo, String usuarioAtualTitulo) {
        log.info("Removendo administrador: {}", usuarioTitulo);
        
        // Validar que o administrador existe
        if (!administradorRepo.existsById(usuarioTitulo)) {
            throw new ErroValidacao("Usuário não é administrador");
        }
        
        // Não permitir que um administrador remova a si mesmo
        if (usuarioTitulo.equals(usuarioAtualTitulo)) {
            throw new ErroValidacao("Não é permitido remover a si mesmo como administrador");
        }
        
        // Validar que sempre resta pelo menos um administrador
        long totalAdministradores = administradorRepo.count();
        if (totalAdministradores <= 1) {
            throw new ErroValidacao("Não é permitido remover o único administrador do sistema");
        }
        
        administradorRepo.deleteById(usuarioTitulo);
        log.info("Administrador {} removido com sucesso", usuarioTitulo);
    }

    @Transactional(readOnly = true)
    public boolean isAdministrador(String usuarioTitulo) {
        return administradorRepo.existsById(usuarioTitulo);
    }

    private AdministradorDto toAdministradorDto(Usuario usuario) {
        return AdministradorDto.builder()
                .tituloEleitoral(usuario.getTituloEleitoral())
                .nome(usuario.getNome())
                .matricula(usuario.getMatricula())
                .unidadeCodigo(usuario.getUnidadeLotacao() != null ? usuario.getUnidadeLotacao().getCodigo() : null)
                .unidadeSigla(usuario.getUnidadeLotacao() != null ? usuario.getUnidadeLotacao().getSigla() : null)
                .build();
    }
}
