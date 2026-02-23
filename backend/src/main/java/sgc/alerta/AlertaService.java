package sgc.alerta;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuario;
import sgc.alerta.model.AlertaUsuarioRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service responsável pelas operações de persistência de alertas.
 *
 * <p>Este service encapsula o acesso aos repositórios {@link AlertaRepo} e {@link AlertaUsuarioRepo},
 * permitindo que {@link AlertaFacade} delegue operações de dados sem acessar repositórios diretamente.
 *
 * @see AlertaFacade
 * @see AlertaRepo
 * @see AlertaUsuarioRepo
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertaService {
    private final AlertaRepo alertaRepo;
    private final AlertaUsuarioRepo alertaUsuarioRepo;

    public Optional<Alerta> porCodigo(Long codigo) {
        return alertaRepo.findById(codigo);
    }

    public List<Alerta> porUnidadeDestino(Long codigoUnidade) {
        return alertaRepo.findByUnidadeDestino_Codigo(codigoUnidade);
    }

    public Page<Alerta> porUnidadeDestinoPaginado(Long codigoUnidade, Pageable pageable) {
        return alertaRepo.findByUnidadeDestino_Codigo(codigoUnidade, pageable);
    }

    public Optional<AlertaUsuario> alertaUsuario(AlertaUsuario.Chave chave) {
        return alertaUsuarioRepo.findById(chave);
    }

    public Optional<LocalDateTime> dataHoraLeituraAlertaUsuario(Long codigoAlerta, String usuarioTitulo) {
        AlertaUsuario.Chave chave = AlertaUsuario.Chave.builder()
                .alertaCodigo(codigoAlerta)
                .usuarioTitulo(usuarioTitulo)
                .build();

        return alertaUsuarioRepo.findById(chave).map(AlertaUsuario::getDataHoraLeitura);
    }

    public List<AlertaUsuario> alertasUsuarios(String usuarioTitulo, List<Long> alertaCodigos) {
        return alertaUsuarioRepo.findByUsuarioAndAlertas(usuarioTitulo, alertaCodigos);
    }

    @Transactional
    public AlertaUsuario salvarAlertaUsuario(AlertaUsuario alertaUsuario) {
        return alertaUsuarioRepo.save(alertaUsuario);
    }

    @Transactional
    public Alerta salvar(Alerta alerta) {
        return alertaRepo.save(alerta);
    }
}
