import {z} from "zod";

const emailSchema = z.string().email();

export function validarEmail(email: string | null | undefined): boolean {
    if (!email) return false;
    return emailSchema.safeParse(email).success;
}

// Password rule: Minimum 8 chars, 1 letter, 1 number.
const passwordSchema = z.string()
    .min(8)
    .regex(/[A-Za-z]/)
    .regex(/\d/);

export function validarSenha(senha: string): boolean {
    if (!senha) return false;
    return passwordSchema.safeParse(senha).success;
}
