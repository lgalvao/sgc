import {defineStore} from 'pinia';
import {Perfil} from "@/types/tipos";
import * as usuarioService from '../services/usuarioService';
import {PerfilUnidade} from '@/mappers/sgrh';

export const usePerfilStore = defineStore('perfil', {
    state: () => ({
        servidorId: localStorage.getItem('idServidor') ? Number(localStorage.getItem('idServidor')) : null,
        perfilSelecionado: (localStorage.getItem('perfilSelecionado') || null) as Perfil | null,
        unidadeSelecionada: localStorage.getItem('unidadeSelecionada') ? Number(localStorage.getItem('unidadeSelecionada')) : null,
        perfisUnidades: [] as PerfilUnidade[],
    }),
    actions: {
        setServidorId(novoId: string | number) {
            this.servidorId = Number(novoId);
            localStorage.setItem('idServidor', String(novoId));
        },
        setPerfilUnidade(perfil: Perfil, unidadeCodigo: number) {
            this.perfilSelecionado = perfil;
            this.unidadeSelecionada = unidadeCodigo;
            localStorage.setItem('perfilSelecionado', perfil);
            localStorage.setItem('unidadeSelecionada', unidadeCodigo.toString());
        },
        setToken(token: string) {
            localStorage.setItem('jwtToken', token);
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
                    const loginResponse = await usuarioService.entrar({
                        tituloEleitoral: tituloEleitoralNum,
                        perfil: perfilUnidadeSelecionado.perfil,
                        unidadeCodigo: perfilUnidadeSelecionado.unidade.codigo,
                    });
                    this.setPerfilUnidade(loginResponse.perfil as Perfil, loginResponse.unidadeCodigo); // Usar loginResponse.codUnidade
                    this.setServidorId(loginResponse.tituloEleitoral); // Usar loginResponse.tituloEleitoral
                    this.setToken(loginResponse.token); // Adicionar esta linha
                }
                return true;
            }
            return false;
        },

        async selecionarPerfilUnidade(tituloEleitoral: number, perfilUnidade: PerfilUnidade) {
            const loginResponse = await usuarioService.entrar({
                tituloEleitoral: tituloEleitoral,
                perfil: perfilUnidade.perfil,
                unidadeCodigo: perfilUnidade.unidade.codigo,
            });
            this.setPerfilUnidade(loginResponse.perfil as Perfil, loginResponse.unidadeCodigo);
            this.setServidorId(loginResponse.tituloEleitoral);
            this.setToken(loginResponse.token);
        }
    },
});