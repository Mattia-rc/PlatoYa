import { inject, Injectable } from '@angular/core';
import { delay, Observable, of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiService } from '../../core/http/api.service';
import { MesaAdministracion, MesaRequest } from '../../core/http/api.models';

@Injectable({ providedIn: 'root' })
export class MesasService {
  private readonly api = inject(ApiService);
  private readonly mockDelayMs = 350;
  private mockMesas: MesaAdministracion[] = [
    { id: 1, numero: 1, capacidad: 2, estado: 'LIBRE', fechaBaja: null },
    { id: 2, numero: 2, capacidad: 4, estado: 'LIBRE', fechaBaja: null },
    { id: 3, numero: 3, capacidad: 4, estado: 'LIBRE', fechaBaja: null },
    { id: 4, numero: 4, capacidad: 6, estado: 'LIBRE', fechaBaja: null },
    { id: 5, numero: 5, capacidad: 2, estado: 'LIBRE', fechaBaja: null },
    { id: 6, numero: 6, capacidad: 8, estado: 'LIBRE', fechaBaja: null },
    { id: 7, numero: 7, capacidad: 4, estado: 'LIBRE', fechaBaja: null },
    { id: 8, numero: 8, capacidad: 4, estado: 'LIBRE', fechaBaja: null },
  ];

  listar(): Observable<MesaAdministracion[]> {
    if (environment.useMocks) return of(structuredClone(this.mockMesas)).pipe(delay(this.mockDelayMs));
    return this.api.get<MesaAdministracion[]>('mesas', { incluirBajas: true });
  }

  crear(request: MesaRequest): Observable<MesaAdministracion> {
    if (environment.useMocks) {
      if (this.mockMesas.some((mesa) => mesa.numero === request.numero)) return this.repetida();
      const creada: MesaAdministracion = { id: Math.max(0, ...this.mockMesas.map((mesa) => mesa.id)) + 1, ...request, estado: 'LIBRE', fechaBaja: null };
      this.mockMesas = [...this.mockMesas, creada];
      return of(structuredClone(creada)).pipe(delay(this.mockDelayMs));
    }
    return this.api.post<MesaAdministracion, MesaRequest>('mesas', request);
  }

  modificar(id: number, request: MesaRequest): Observable<MesaAdministracion> {
    if (environment.useMocks) {
      if (this.mockMesas.some((mesa) => mesa.id !== id && mesa.numero === request.numero)) return this.repetida();
      return this.actualizarMock(id, (mesa) => ({ ...mesa, ...request }));
    }
    return this.api.put<MesaAdministracion, MesaRequest>(`mesas/${id}`, request);
  }

  darDeBaja(id: number): Observable<void> {
    if (environment.useMocks) {
      const mesa = this.mockMesas.find((actual) => actual.id === id);
      if (!mesa) return throwError(() => ({ status: 404, error: { mensaje: 'La mesa no existe.' } }));
      if (mesa.estado === 'OCUPADA') return throwError(() => ({ status: 409, error: { mensaje: 'No se puede dar de baja una mesa ocupada.' } }));
      this.mockMesas = this.mockMesas.map((actual) => actual.id === id ? { ...actual, fechaBaja: new Date().toISOString() } : actual);
      return of(undefined).pipe(delay(this.mockDelayMs));
    }
    return this.api.delete<void>(`mesas/${id}`);
  }

  reactivar(id: number): Observable<MesaAdministracion> {
    if (environment.useMocks) return this.actualizarMock(id, (mesa) => ({ ...mesa, fechaBaja: null }));
    return this.api.post<MesaAdministracion>(`mesas/${id}/reactivar`, {});
  }

  private actualizarMock(id: number, cambio: (mesa: MesaAdministracion) => MesaAdministracion): Observable<MesaAdministracion> {
    const mesa = this.mockMesas.find((actual) => actual.id === id);
    if (!mesa) return throwError(() => ({ status: 404, error: { mensaje: 'La mesa no existe.' } }));
    const actualizada = cambio(mesa);
    this.mockMesas = this.mockMesas.map((actual) => actual.id === id ? actualizada : actual);
    return of(structuredClone(actualizada)).pipe(delay(this.mockDelayMs));
  }

  private repetida(): Observable<never> {
    return throwError(() => ({ status: 409, error: { mensaje: 'Ya existe una mesa con ese número. Si está dada de baja, podés reactivarla.' } }));
  }
}
