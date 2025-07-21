import {defineStore} from 'pinia'

export const usePerfilStore = defineStore('perfil', {
    state: () => ({
        value: localStorage.getItem('perfil') || 'SEDOC',
    }),
    actions: {
        setPerfil(novoPerfil) {
            this.value = novoPerfil
            localStorage.setItem('perfil', novoPerfil)
        },
    },
}) 