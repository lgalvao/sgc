package sgc.fixture;

import sgc.alerta.model.Alerta;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;

import java.time.LocalDateTime;

public class AlertaFixture {

    public static Alerta alertaPadrao(Processo processo) {
        Alerta alerta = new Alerta();
        alerta.setCodigo(1L);
        alerta.setProcesso(processo);
        alerta.setDescricao("Alerta de Teste");
        alerta.setDataHora(LocalDateTime.now());
        alerta.setUnidadeOrigem(UnidadeFixture.unidadePadrao());
        alerta.setUnidadeDestino(UnidadeFixture.unidadePadrao());
        return alerta;
    }

    public static Alerta alertaParaUsuario(Processo processo, Usuario usuario) {
        Alerta alerta = alertaPadrao(processo);
        alerta.setUsuarioDestino(usuario);
        // Define também a unidade de destino para que o alerta apareça na busca por unidade
        alerta.setUnidadeDestino(usuario.getUnidadeLotacao());
        alerta.setDescricao("Alerta para Usuario " + usuario.getNome());
        return alerta;
    }

    public static Alerta alertaParaUnidade(Processo processo, Unidade unidade) {
        Alerta alerta = alertaPadrao(processo);
        alerta.setUnidadeDestino(unidade);
        alerta.setDescricao("Alerta para Unidade " + unidade.getNome());
        return alerta;
    }
}
