import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { delay, of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse } from '../auth/auth.models';
import { AbrirMesaRequest, CambiarEstadoItemRequest, Destino, ItemPreparacion, KpiMesas, KpiPreparacion, MesaResumen } from './api.models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly mockDelayMs = 350;
  private readonly mockUsers: LoginResponse['usuario'][] = [
    { id: 1, nombre: 'Ana Administración', usuario: 'admin', rol: 'ADMIN' },
    { id: 2, nombre: 'Martín Mozo', usuario: 'mozo', rol: 'MOZO' },
    { id: 3, nombre: 'Carla Cocina', usuario: 'cocina', rol: 'COCINA' },
    { id: 4, nombre: 'Bruno Barra', usuario: 'barra', rol: 'BARRA' },
  ];
  private mockMesas: MesaResumen[] = [
    { id: 1, numero: 1, capacidad: 2, estado: 'LIBRE' },
    { id: 2, numero: 2, capacidad: 4, estado: 'OCUPADA', cuentaId: 101, estadoCuenta: 'ABIERTA' },
    { id: 3, numero: 3, capacidad: 4, estado: 'LIBRE' },
    { id: 4, numero: 4, capacidad: 6, estado: 'OCUPADA', cuentaId: 102, estadoCuenta: 'ABIERTA' },
    { id: 5, numero: 5, capacidad: 2, estado: 'LIBRE' },
    { id: 6, numero: 6, capacidad: 8, estado: 'LIBRE' },
    { id: 7, numero: 7, capacidad: 4, estado: 'OCUPADA', cuentaId: 103, estadoCuenta: 'CUENTA_PEDIDA' },
    { id: 8, numero: 8, capacidad: 4, estado: 'LIBRE' },
  ];
  private mockItems: ItemPreparacion[] = [
    { id: 1, comandaId: 201, numeroComanda: 1, mesaNumero: 2, nombreProducto: 'Hamburguesa completa', cantidad: 2, observacion: 'Una sin cebolla', estado: 'PENDIENTE', destino: 'COCINA', fechaEnvio: new Date(Date.now() - 9 * 60000).toISOString() },
    { id: 2, comandaId: 202, numeroComanda: 1, mesaNumero: 4, nombreProducto: 'Porción de papas', cantidad: 1, estado: 'EN_PREPARACION', destino: 'COCINA', fechaEnvio: new Date(Date.now() - 6 * 60000).toISOString() },
    { id: 3, comandaId: 203, numeroComanda: 2, mesaNumero: 7, nombreProducto: 'Pizza especial', cantidad: 1, observacion: 'Masa bien cocida', estado: 'PENDIENTE', destino: 'COCINA', fechaEnvio: new Date(Date.now() - 3 * 60000).toISOString() },
    { id: 4, comandaId: 201, numeroComanda: 1, mesaNumero: 2, nombreProducto: 'Cerveza pinta', cantidad: 2, estado: 'PENDIENTE', destino: 'BARRA', fechaEnvio: new Date(Date.now() - 9 * 60000).toISOString() },
    { id: 5, comandaId: 202, numeroComanda: 1, mesaNumero: 4, nombreProducto: 'Gaseosa', cantidad: 3, observacion: 'Dos sin hielo', estado: 'EN_PREPARACION', destino: 'BARRA', fechaEnvio: new Date(Date.now() - 5 * 60000).toISOString() },
  ];

  get<T>(endpoint: string, params?: Record<string, string | number | boolean>): Observable<T> {
    let httpParams = new HttpParams();
    Object.entries(params ?? {}).forEach(([key, value]) => httpParams = httpParams.set(key, String(value)));
    return this.http.get<T>(this.url(endpoint), { params: httpParams });
  }

  post<TResponse, TBody = unknown>(endpoint: string, body: TBody): Observable<TResponse> {
    return this.http.post<TResponse>(this.url(endpoint), body);
  }

  put<TResponse, TBody = unknown>(endpoint: string, body: TBody): Observable<TResponse> {
    return this.http.put<TResponse>(this.url(endpoint), body);
  }

  patch<TResponse, TBody = unknown>(endpoint: string, body: TBody): Observable<TResponse> {
    return this.http.patch<TResponse>(this.url(endpoint), body);
  }

  delete<T>(endpoint: string): Observable<T> {
    return this.http.delete<T>(this.url(endpoint));
  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    if (environment.useMocks) {
      const usuario = this.mockUsers.find((item) => item.usuario === credentials.usuario);
      if (!usuario || credentials.contrasena !== '1234') {
        return throwError(() => ({ status: 401, error: { mensaje: 'Usuario o contraseña incorrectos.' } }));
      }
      return of({ token: `mock-token-${usuario.rol.toLowerCase()}`, usuario }).pipe(delay(this.mockDelayMs));
    }
    return this.post<LoginResponse, LoginRequest>('auth/login', credentials);
  }

  obtenerMesas(): Observable<MesaResumen[]> {
    if (environment.useMocks) return of(structuredClone(this.mockMesas)).pipe(delay(this.mockDelayMs));
    return this.get<MesaResumen[]>('mesas');
  }

  obtenerKpisMesas(): Observable<KpiMesas> {
    if (environment.useMocks) {
      const mesasLibres = this.mockMesas.filter((mesa) => mesa.estado === 'LIBRE').length;
      const mesasOcupadas = this.mockMesas.filter((mesa) => mesa.estado === 'OCUPADA').length;
      const cuentasAbiertas = this.mockMesas.filter((mesa) => mesa.estadoCuenta === 'ABIERTA').length;
      const cuentasPedidas = this.mockMesas.filter((mesa) => mesa.estadoCuenta === 'CUENTA_PEDIDA').length;
      return of({
        totalMesas: this.mockMesas.length,
        mesasLibres,
        mesasOcupadas,
        cuentasAbiertas,
        cuentasPedidas,
        ocupacionPorcentaje: this.mockMesas.length ? Math.round((mesasOcupadas / this.mockMesas.length) * 100) : 0,
      }).pipe(delay(this.mockDelayMs));
    }
    return this.get<KpiMesas>('mesas/resumen');
  }

  abrirMesa(mesaId: number): Observable<MesaResumen> {
    if (environment.useMocks) {
      const mesa = this.mockMesas.find((item) => item.id === mesaId);
      if (!mesa || mesa.estado === 'OCUPADA') return throwError(() => ({ status: 409, error: { mensaje: 'La mesa ya está ocupada.' } }));
      const actualizada: MesaResumen = { ...mesa, estado: 'OCUPADA', cuentaId: 100 + mesaId, estadoCuenta: 'ABIERTA' };
      this.mockMesas = this.mockMesas.map((item) => item.id === mesaId ? actualizada : item);
      return of(structuredClone(actualizada)).pipe(delay(this.mockDelayMs));
    }
    return this.post<MesaResumen, AbrirMesaRequest>('cuentas', { mesaId });
  }

  obtenerItemsPorSector(destino: Destino): Observable<ItemPreparacion[]> {
    if (environment.useMocks) {
      const items = this.mockItems.filter((item) => item.destino === destino).sort((a, b) => a.fechaEnvio.localeCompare(b.fechaEnvio));
      return of(structuredClone(items)).pipe(delay(this.mockDelayMs));
    }
    return this.get<ItemPreparacion[]>('preparacion/items', { destino });
  }

  obtenerKpisPreparacion(destino: Destino): Observable<KpiPreparacion> {
    if (environment.useMocks) {
      const items = this.mockItems.filter((item) => item.destino === destino);
      return of({
        totalItems: items.length,
        pendientes: items.filter((item) => item.estado === 'PENDIENTE').length,
        enPreparacion: items.filter((item) => item.estado === 'EN_PREPARACION').length,
        listos: items.filter((item) => item.estado === 'LISTO').length,
        anulados: items.filter((item) => item.estado === 'ANULADO').length,
        tiempoPromedioMinutos: destino === 'COCINA' ? 14 : 6,
      }).pipe(delay(this.mockDelayMs));
    }
    return this.get<KpiPreparacion>('preparacion/resumen', { destino });
  }

  cambiarEstadoItem(itemId: number, estado: CambiarEstadoItemRequest['estado']): Observable<ItemPreparacion> {
    if (environment.useMocks) {
      const item = this.mockItems.find((actual) => actual.id === itemId);
      if (!item) return throwError(() => ({ status: 404, error: { mensaje: 'No se encontró el ítem.' } }));
      const actualizado = { ...item, estado };
      this.mockItems = this.mockItems.map((actual) => actual.id === itemId ? actualizado : actual);
      return of(structuredClone(actualizado)).pipe(delay(this.mockDelayMs));
    }
    return this.patch<ItemPreparacion, CambiarEstadoItemRequest>(`preparacion/items/${itemId}/estado`, { estado });
  }

  private url(endpoint: string): string {
    const cleanEndpoint = endpoint.replace(/^\/+/, '');
    return `${this.baseUrl}/${cleanEndpoint}`;
  }
}
