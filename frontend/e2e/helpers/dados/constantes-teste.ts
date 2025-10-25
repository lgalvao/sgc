// (Mantenha as exportações SELETORES, TEXTOS, URLS, etc., existentes no arquivo)

export const USUARIOS = {
  ADMIN: {
    titulo: '6',
    nome: 'Ricardo Alves',
    senha: '123', // A senha é '123' para todos no ambiente de teste
    unidade: 'STIC',
  },
  GESTOR: {
    titulo: '8',
    nome: 'Paulo Horta',
    senha: '123',
    unidade: 'SEDESENV',
  },
  CHEFE_SGP: {
    titulo: '2',
    nome: 'Carlos Henrique Lima',
    senha: '123',
    unidade: 'SGP',
  },
  CHEFE_SEDESENV: {
    titulo: '3',
    nome: 'Fernanda Oliveira',
    senha: '123',
    unidade: 'SEDESENV',
  },
  SERVIDOR: {
    titulo: '1',
    nome: 'Ana Paula Souza',
    senha: '123',
    unidade: 'SESEL',
  },
  MULTI_PERFIL: {
    titulo: '999999999999',
    nome: 'Usuario Multi Perfil',
    senha: '123',
    perfis: ['ADMIN - STIC', 'GESTOR - STIC'], // Formato 'PERFIL - SIGLA' como na UI
  },
  CHEFE_SEDIA: {
    titulo: '10',
    nome: 'Paula Gonçalves',
    senha: '123',
    unidade: 'SEDIA',
  },
} as const;

// (Mantenha as exportações restantes como SELETORES_CSS)
