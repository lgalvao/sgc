package sgc.organizacao.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import sgc.organizacao.dto.MapaVigenteReferenciaDto;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.UnidadeMapaRepo;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("hom")
@Disabled("Teste diagnóstico manual para inspecionar dados reais do perfil hom.")
class UnidadeMapaVigenteHomExploracaoTest {
    private static final Long CODIGO_UNIDADE = 732L;

    @Autowired
    private UnidadeService unidadeService;

    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private sgc.organizacao.service.UnidadeHierarquiaService unidadeHierarquiaService;

    @Test
    void inspecionarMapaVigenteDaUnidade732() {
        System.out.println("==== Diagnóstico unidade " + CODIGO_UNIDADE + " ====");

        Unidade unidade = entityManager.find(Unidade.class, CODIGO_UNIDADE);
        System.out.println("unidade: " + (unidade == null ? null : unidade.getSigla() + " - " + unidade.getNome()));
        System.out.println("unidadeSuperior: " + (unidade == null || unidade.getUnidadeSuperior() == null
                ? null
                : unidade.getUnidadeSuperior().getCodigo() + " - " + unidade.getUnidadeSuperior().getSigla()));

        boolean temMapaVigente = unidadeService.temMapaVigente(CODIGO_UNIDADE);
        Optional<MapaVigenteReferenciaDto> referencia = unidadeService.buscarReferenciaMapaVigente(CODIGO_UNIDADE);
        Optional<UnidadeMapa> unidadeMapa = unidadeMapaRepo.findById(CODIGO_UNIDADE);
        Optional<UnidadeMapa> unidadeMapaComProcesso = unidadeMapaRepo.buscarMapaVigenteComProcesso(CODIGO_UNIDADE);

        System.out.println("temMapaVigente(): " + temMapaVigente);
        System.out.println("buscarReferenciaMapaVigente(): " + referencia.orElse(null));
        System.out.println("registro UNIDADE_MAPA existe: " + unidadeMapa.isPresent());
        System.out.println("registro UNIDADE_MAPA com processo existe: " + unidadeMapaComProcesso.isPresent());

        unidadeMapa.ifPresent(registro -> {
            System.out.println("mapaVigente.codigo: " + (registro.getMapaVigente() == null ? null : registro.getMapaVigente().getCodigo()));
            if (registro.getMapaVigente() != null && registro.getMapaVigente().getSubprocesso() != null) {
                Subprocesso sp = registro.getMapaVigente().getSubprocesso();
                Processo processo = sp.getProcesso();
                System.out.println("subprocesso.codigo: " + sp.getCodigo());
                System.out.println("subprocesso.situacao: " + sp.getSituacao());
                System.out.println("processo.codigo: " + (processo == null ? null : processo.getCodigo()));
                System.out.println("processo.tipo: " + (processo == null ? null : processo.getTipo()));
                System.out.println("processo.situacao: " + (processo == null ? null : processo.getSituacao()));
                System.out.println("processo.dataFinalizacao: " + (processo == null ? null : processo.getDataFinalizacao()));
            }
        });

        List<Object[]> subprocessosDaUnidade = entityManager.createQuery("""
                select sp.codigo, sp.situacao, p.codigo, p.tipo, p.situacao, p.dataFinalizacao
                from Subprocesso sp
                join sp.processo p
                where sp.unidade.codigo = :codigoUnidade
                order by p.codigo desc
                """, Object[].class)
                .setParameter("codigoUnidade", CODIGO_UNIDADE)
                .getResultList();

        List<Object[]> mapasVigentesDaUnidade = entityManager.createQuery("""
                select um.unidadeCodigo, mapa.codigo, sp.codigo, p.codigo, p.situacao, p.dataFinalizacao
                from UnidadeMapa um
                join um.mapaVigente mapa
                left join mapa.subprocesso sp
                left join sp.processo p
                where um.unidadeCodigo = :codigoUnidade
                """, Object[].class)
                .setParameter("codigoUnidade", CODIGO_UNIDADE)
                .getResultList();

        List<Object[]> processosFinalizadosDaUnidade = entityManager.createQuery("""
                select p.codigo, p.tipo, p.situacao, p.dataFinalizacao, sp.codigo, sp.situacao, mapa.codigo
                from Subprocesso sp
                join sp.processo p
                left join sp.mapa mapa
                where sp.unidade.codigo = :codigoUnidade
                  and p.situacao = sgc.processo.model.SituacaoProcesso.FINALIZADO
                order by p.dataFinalizacao desc nulls last, p.codigo desc
                """, Object[].class)
                .setParameter("codigoUnidade", CODIGO_UNIDADE)
                .getResultList();

        List<Long> descendentes = unidadeHierarquiaService.buscarIdsDescendentes(CODIGO_UNIDADE);
        List<Object[]> processosFinalizadosNasDescendentes = descendentes.isEmpty()
                ? List.of()
                : entityManager.createQuery("""
                        select sp.unidade.codigo, sp.unidade.sigla, p.codigo, p.tipo, p.situacao, p.dataFinalizacao, sp.codigo, sp.situacao, mapa.codigo
                        from Subprocesso sp
                        join sp.processo p
                        left join sp.mapa mapa
                        where sp.unidade.codigo in :codigosUnidades
                          and p.situacao = sgc.processo.model.SituacaoProcesso.FINALIZADO
                        order by p.dataFinalizacao desc nulls last, p.codigo desc
                        """, Object[].class)
                .setParameter("codigosUnidades", descendentes)
                .getResultList();

        System.out.println("unidade_mapa da unidade:");
        for (Object[] linha : mapasVigentesDaUnidade) {
            System.out.println("  unidade=" + linha[0]
                    + ", mapa=" + linha[1]
                    + ", subprocesso=" + linha[2]
                    + ", processo=" + linha[3]
                    + ", situacaoProcesso=" + linha[4]
                    + ", dataFinalizacao=" + linha[5]);
        }

        System.out.println("subprocessos da unidade:");
        for (Object[] linha : subprocessosDaUnidade) {
            System.out.println("  subprocesso=" + linha[0]
                    + ", situacaoSp=" + linha[1]
                    + ", processo=" + linha[2]
                    + ", tipo=" + linha[3]
                    + ", situacaoProcesso=" + linha[4]
                    + ", dataFinalizacao=" + linha[5]);
        }

        System.out.println("processos finalizados da unidade:");
        for (Object[] linha : processosFinalizadosDaUnidade) {
            System.out.println("  processo=" + linha[0]
                    + ", tipo=" + linha[1]
                    + ", situacaoProcesso=" + linha[2]
                    + ", dataFinalizacao=" + linha[3]
                    + ", subprocesso=" + linha[4]
                    + ", situacaoSp=" + linha[5]
                    + ", mapa=" + linha[6]);
        }

        System.out.println("descendentes da unidade: " + descendentes);
        System.out.println("processos finalizados nas descendentes:");
        for (Object[] linha : processosFinalizadosNasDescendentes) {
            System.out.println("  unidade=" + linha[0]
                    + " (" + linha[1] + ")"
                    + ", processo=" + linha[2]
                    + ", tipo=" + linha[3]
                    + ", situacaoProcesso=" + linha[4]
                    + ", dataFinalizacao=" + linha[5]
                    + ", subprocesso=" + linha[6]
                    + ", situacaoSp=" + linha[7]
                    + ", mapa=" + linha[8]);
        }
    }
}
