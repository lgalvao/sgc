import {defineStore} from 'pinia';
import {Perfil} from "@/types/tipos";
import * as usuarioService from '../services/usuarioService';
import {PerfilUnidade} from '@/mappers/sgrh';

export const usePerfilStore = defineStore('perfil', {
    state: () => ({
        servidorId: localStorage.getItem('idServidor') ? Number(localStorage.getItem('idServidor')) : null,
        perfilSelecionado: (localStorage.getItem('perfilSelecionado') || null) as Perfil | null,
        unidadeSelecionada: (localStorage.getItem('unidadeSelecionada') || null) as string | null,
        perfisUnidades: [] as PerfilUnidade[],
    }),
    actions: {
        setServidorId(novoId: number) {
            this.servidorId = novoId;
            localStorage.setItem('idServidor', novoId.toString());
        },
        setPerfilUnidade(perfil: Perfil, unidadeSigla: string) {
            this.perfilSelecionado = perfil;
            this.unidadeSelecionada = unidadeSigla;
            localStorage.setItem('perfilSelecionado', perfil);
            localStorage.setItem('unidadeSelecionada', unidadeSigla);
        },
        async loginCompleto(tituloEleitoral: string, senha: string) {
            const tituloEleitoralNum = Number(tituloEleitoral);
            const autenticado = await usuarioService.autenticar({tituloEleitoral: tituloEleitoralNum, senha});
            if (autenticado) {
                const perfisUnidades = await usuarioService.autorizar(tituloEleitoralNum);
                this.perfisUnidades = perfisUnidades;

                // Se houver apenas uma opção, seleciona automaticamente
                if (perfisUnidades.length === 1) {
                    const perfilUnidadeSelecionado = perfisUnidades[0];
                    console.log('Dados para entrar (loginCompleto):', {
                        tituloEleitoral: tituloEleitoralNum,
                        perfil: perfilUnidadeSelecionado.perfil,
                        unidadeCodigo: perfilUnidadeSelecionado.unidade.codigo,
                    });
                    await usuarioService.entrar({
                        tituloEleitoral: tituloEleitoralNum,
                        perfil: perfilUnidadeSelecionado.perfil,
                        unidadeCodigo: perfilUnidadeSelecionado.unidade.codigo,
                    });
                    this.setPerfilUnidade(perfilUnidadeSelecionado.perfil as Perfil, perfilUnidadeSelecionado.unidade.sigla);
                    this.setServidorId(tituloEleitoralNum);
                }
                return true;
            }
            return false;
        },

        async selecionarPerfilUnidade(tituloEleitoral: number, perfilUnidade: PerfilUnidade) {
            console.log('Dados para entrar (selecionarPerfilUnidade):', {
                tituloEleitoral: tituloEleitoral,
                perfil: perfilUnidade.perfil,
                unidadeCodigo: perfilUnidade.unidade.codigo,
            });
            await usuarioService.entrar({
                tituloEleitoral: tituloEleitoral,
                perfil: perfilUnidade.perfil,
                unidadeCodigo: perfilUnidade.unidade.codigo,
            });
            this.setPerfilUnidade(perfilUnidade.perfil as Perfil, perfilUnidade.unidade.sigla);
            this.setServidorId(tituloEleitoral);
        }
    },
});