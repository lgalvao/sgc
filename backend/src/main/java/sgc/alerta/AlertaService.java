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

    Alerta salvar(Alerta alerta) {
        return alertaRepo.save(alerta);
    }

    List<Alerta> salvarTodos(List<Alerta> alertas) {
        return alertaRepo.saveAll(alertas);
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
     * Regra CDU-02: Para os perfis de gestão, lista apenas alertas coletivos
     * da unidade ativa.
     */
    public List<Alerta> listarParaGestao(Long codigoUnidade) {
        return alertaRepo.buscarAlertasDaUnidade(codigoUnidade);
    }

    public Page<Alerta> listarParaServidorPaginado(String usuarioTitulo, Pageable pageable) {
        return alertaRepo.buscarAlertasExclusivosDoUsuario(usuarioTitulo, pageable);
    }

    public Page<Alerta> listarParaGestaoPaginado(Long codigoUnidade, Pageable pageable) {
        return alertaRepo.buscarAlertasDaUnidade(codigoUnidade, pageable);
    }

    public Optional<AlertaUsuario> alertaUsuario(AlertaUsuario.Chave chave) {
        return alertaUsuarioRepo.findById(chave);
    }

    public List<AlertaUsuario> alertasUsuarios(String usuarioTitulo, List<Long> alertaCodigos) {
        return alertaUsuarioRepo.listarPorUsuarioEAlertas(usuarioTitulo, alertaCodigos);
    }

    public List<Alerta> listarPorCodigos(List<Long> codigos) {
        return alertaRepo.findAllById(codigos);
    }

    void salvarAlertaUsuario(AlertaUsuario au) {
        alertaUsuarioRepo.save(au);
    }

    List<AlertaUsuario> salvarAlertasUsuarios(List<AlertaUsuario> alertasUsuarios) {
        return alertaUsuarioRepo.saveAll(alertasUsuarios);
    }

    public Optional<LocalDateTime> dataHoraLeituraAlertaUsuario(Long codigoAlerta, String usuarioTitulo) {
        AlertaUsuario.Chave chave = AlertaUsuario.Chave.builder()
                .alertaCodigo(codigoAlerta)
                .usuarioTitulo(usuarioTitulo)
                .build();
        return alertaUsuarioRepo.findById(chave).map(AlertaUsuario::getDataHoraLeitura);
    }
}
