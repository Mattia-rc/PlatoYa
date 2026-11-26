import { UserRole } from '../auth/auth.models';

export interface ApiError { mensaje: string; codigo?: string; detalles?: Record<string, string>; }
export type EstadoMesa = 'LIBRE' | 'OCUPADA';
export type EstadoItem = 'PENDIENTE' | 'EN_PREPARACION' | 'LISTO' | 'ENTREGADO' | 'ANULADO';
export type Destino = 'COCINA' | 'BARRA';

export interface MesaResumen {
  id: number;
  numero: number;
  capacidad: number;
  estado: EstadoMesa;
  cuentaId?: number;
  estadoCuenta?: 'ABIERTA' | 'CUENTA_PEDIDA' | 'PAGADA' | 'ANULADA';
}

export interface KpiMesas {
  totalMesas: number;
  mesasLibres: number;
  mesasOcupadas: number;
  cuentasAbiertas: number;
  cuentasPedidas: number;
  ocupacionPorcentaje: number;
}

export interface ItemPreparacion {
  id: number;
  comandaId: number;
  numeroComanda: number;
  mesaNumero: number;
  nombreProducto: string;
  cantidad: number;
  observacion?: string;
  estado: EstadoItem;
  destino: Destino;
  fechaEnvio: string;
}

export interface KpiPreparacion {
  totalItems: number;
  pendientes: number;
  enPreparacion: number;
  listos: number;
  anulados: number;
  tiempoPromedioMinutos: number;
}

export interface AbrirMesaRequest { mesaId: number; }
export interface CambiarEstadoItemRequest { estado: EstadoItem; }
export interface Usuario { id: number; nombre: string; usuario: string; rol: UserRole; fechaBaja: string | null; }
export interface UsuarioAltaRequest { nombre: string; usuario: string; contrasena: string; rol: UserRole; }
export interface UsuarioModificacionRequest { nombre: string; rol: UserRole; contrasena?: string; }
