package sgc.alerta;

import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertaService {
    private final AlertaRepo alertaRepo;
    private final AlertaUsuarioRepo alertaUsuarioRepo;

    public Alerta salvar(Alerta alerta) {
        return alertaRepo.save(alerta);
    }

    public Optional<Alerta> porCodigo(Long codigo) {
        return alertaRepo.findById(codigo);
    }

    /**
     * Regra CDU-02: Para o perfil SERVIDOR, lista apenas os alertas destinados 
     * individualmente ao seu título de eleitor.
     */
    public List<Alerta> listarParaServidor(String usuarioTitulo) {
        return alertaRepo.buscarAlertasExclusivosDoUsuario(usuarioTitulo);
    }

    /**
     * Regra CDU-02: Para os demais perfis, lista alertas coletivos da unidade 
     * E alertas individuais do usuário.
     */
    public List<Alerta> listarParaGestao(Long codigoUnidade, String usuarioTitulo) {
        return alertaRepo.buscarAlertasDaUnidadeEIndividuais(codigoUnidade, usuarioTitulo);
    }

    public Page<Alerta> listarParaServidorPaginado(String usuarioTitulo, Pageable pageable) {
        return alertaRepo.buscarAlertasExclusivosDoUsuario(usuarioTitulo, pageable);
    }

    public Page<Alerta> listarParaGestaoPaginado(Long codigoUnidade, String usuarioTitulo, Pageable pageable) {
        return alertaRepo.buscarAlertasDaUnidadeEIndividuais(codigoUnidade, usuarioTitulo, pageable);
    }

    public Optional<AlertaUsuario> alertaUsuario(AlertaUsuario.Chave chave) {
        return alertaUsuarioRepo.findById(chave);
    }

    public List<AlertaUsuario> alertasUsuarios(String usuarioTitulo, List<Long> alertaCodigos) {
        return alertaUsuarioRepo.findByUsuarioAndAlertas(usuarioTitulo, alertaCodigos);
    }

    public AlertaUsuario salvarAlertaUsuario(AlertaUsuario au) {
        return alertaUsuarioRepo.save(au);
    }

    public Optional<LocalDateTime> dataHoraLeituraAlertaUsuario(Long codigoAlerta, String usuarioTitulo) {
        AlertaUsuario.Chave chave = AlertaUsuario.Chave.builder()
                .alertaCodigo(codigoAlerta)
                .usuarioTitulo(usuarioTitulo)
                .build();
        return alertaUsuarioRepo.findById(chave).map(AlertaUsuario::getDataHoraLeitura);
    }
}
