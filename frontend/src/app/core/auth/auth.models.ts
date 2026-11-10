export type UserRole = 'ADMIN' | 'MOZO' | 'COCINA' | 'BARRA';
export interface LoginRequest { usuario: string; contrasena: string; }
export interface SessionUser { id?: number; nombre: string; usuario: string; rol: UserRole; }
export interface LoginResponse { token: string; usuario: SessionUser; }
