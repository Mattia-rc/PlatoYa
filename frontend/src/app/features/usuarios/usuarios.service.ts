import { inject, Injectable } from '@angular/core';
import { delay, Observable, of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiService } from '../../core/http/api.service';
import { Usuario, UsuarioAltaRequest, UsuarioModificacionRequest } from '../../core/http/api.models';

@Injectable({ providedIn: 'root' })
export class UsuariosService {
  private readonly api = inject(ApiService);
  private readonly mockDelayMs = 350;
  // Los mocks no replican las reglas del backend, salvo el 409 por usuario repetido.
  private mockUsuarios: Usuario[] = [
    { id: 1, nombre: 'Ana Administración', usuario: 'admin', rol: 'ADMIN', fechaBaja: null },
    { id: 2, nombre: 'Martín Mozo', usuario: 'mozo', rol: 'MOZO', fechaBaja: null },
    { id: 3, nombre: 'Carla Cocina', usuario: 'cocina', rol: 'COCINA', fechaBaja: null },
    { id: 4, nombre: 'Bruno Barra', usuario: 'barra', rol: 'BARRA', fechaBaja: null },
  ];

  listar(): Observable<Usuario[]> {
    if (environment.useMocks) {
      const ordenados = [...this.mockUsuarios].sort((a, b) => a.usuario.localeCompare(b.usuario));
      return of(structuredClone(ordenados)).pipe(delay(this.mockDelayMs));
    }
    return this.api.get<Usuario[]>('usuarios');
  }

  crear(request: UsuarioAltaRequest): Observable<Usuario> {
    if (environment.useMocks) {
      if (this.mockUsuarios.some((usuario) => usuario.usuario === request.usuario)) {
        return throwError(() => ({ status: 409, error: { mensaje: 'Ya existe un usuario con ese nombre de usuario. Si está dado de baja, podés reactivarlo.' } }));
      }
      const creado: Usuario = { id: Math.max(...this.mockUsuarios.map((usuario) => usuario.id)) + 1, nombre: request.nombre, usuario: request.usuario, rol: request.rol, fechaBaja: null };
      this.mockUsuarios = [...this.mockUsuarios, creado];
      return of(structuredClone(creado)).pipe(delay(this.mockDelayMs));
    }
    return this.api.post<Usuario, UsuarioAltaRequest>('usuarios', request);
  }

  modificar(id: number, request: UsuarioModificacionRequest): Observable<Usuario> {
    if (environment.useMocks) return this.actualizarMock(id, (usuario) => ({ ...usuario, nombre: request.nombre, rol: request.rol }));
    return this.api.put<Usuario, UsuarioModificacionRequest>(`usuarios/${id}`, request);
  }

  darDeBaja(id: number): Observable<void> {
    if (environment.useMocks) {
      this.mockUsuarios = this.mockUsuarios.map((usuario) => usuario.id === id ? { ...usuario, fechaBaja: new Date().toISOString() } : usuario);
      return of(undefined).pipe(delay(this.mockDelayMs));
    }
    return this.api.delete<void>(`usuarios/${id}`);
  }

  reactivar(id: number): Observable<Usuario> {
    if (environment.useMocks) return this.actualizarMock(id, (usuario) => ({ ...usuario, fechaBaja: null }));
    return this.api.post<Usuario>(`usuarios/${id}/reactivar`, {});
  }

  private actualizarMock(id: number, cambio: (usuario: Usuario) => Usuario): Observable<Usuario> {
    const usuario = this.mockUsuarios.find((actual) => actual.id === id);
    if (!usuario) return throwError(() => ({ status: 404, error: { mensaje: 'El usuario no existe.' } }));
    const actualizado = cambio(usuario);
    this.mockUsuarios = this.mockUsuarios.map((actual) => actual.id === id ? actualizado : actual);
    return of(structuredClone(actualizado)).pipe(delay(this.mockDelayMs));
  }
}
