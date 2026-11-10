import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { PasswordModule } from 'primeng/password';
import { AuthService } from '../../core/auth/auth.service';

@Component({ selector: 'app-login', standalone: true, imports: [FormsModule, ButtonModule, InputTextModule, PasswordModule, MessageModule], templateUrl: './login.html', styleUrl: './login.css' })
export class Login {
  private readonly auth = inject(AuthService);
  protected usuario = '';
  protected contrasena = '';
  protected readonly cargando = signal(false);
  protected readonly error = signal('');

  protected ingresar(): void {
    if (!this.usuario.trim() || !this.contrasena) { this.error.set('Completá usuario y contraseña.'); return; }
    this.cargando.set(true);
    this.error.set('');
    this.auth.login({ usuario: this.usuario.trim().toLowerCase(), contrasena: this.contrasena.trim() }).subscribe({
      next: (response) => {
        this.cargando.set(false);
        window.location.assign(this.auth.homeForRole(response.usuario.rol));
      },
      error: (error: HttpErrorResponse) => { this.cargando.set(false); this.error.set(error.error?.mensaje ?? 'No se pudo iniciar sesión. Verificá los datos y el backend.'); },
    });
  }
}
