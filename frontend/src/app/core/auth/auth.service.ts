import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { ApiService } from '../http/api.service';
import { LoginRequest, LoginResponse, SessionUser, UserRole } from './auth.models';

const TOKEN_KEY = 'platoya_token';
const USER_KEY = 'platoya_usuario';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly currentUserState = signal<SessionUser | null>(this.restoreUser());
  readonly currentUser = this.currentUserState.asReadonly();
  readonly isAuthenticated = computed(() => Boolean(this.token && this.currentUserState()));

  get token(): string | null { return sessionStorage.getItem(TOKEN_KEY); }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.api.login(credentials).pipe(tap((response) => {
      sessionStorage.setItem(TOKEN_KEY, response.token);
      sessionStorage.setItem(USER_KEY, JSON.stringify(response.usuario));
      this.currentUserState.set(response.usuario);
    }));
  }

  logout(): void {
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
    this.currentUserState.set(null);
    void this.router.navigate(['/login']);
  }

  hasAnyRole(roles: UserRole[]): boolean {
    const role = this.currentUserState()?.rol;
    return Boolean(role && roles.includes(role));
  }

  homeForRole(role = this.currentUserState()?.rol): string {
    if (role === 'COCINA') return '/cocina';
    if (role === 'BARRA') return '/barra';
    return '/mozo';
  }

  private restoreUser(): SessionUser | null {
    const stored = sessionStorage.getItem(USER_KEY);
    if (!stored) return null;
    try { return JSON.parse(stored) as SessionUser; }
    catch { sessionStorage.removeItem(USER_KEY); return null; }
  }
}
