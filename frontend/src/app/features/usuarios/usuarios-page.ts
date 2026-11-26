import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, FormsModule, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { ConfirmationService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule } from 'primeng/select';
import { SelectButtonModule } from 'primeng/selectbutton';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { Observable } from 'rxjs';
import { UserRole } from '../../core/auth/auth.models';
import { AuthService } from '../../core/auth/auth.service';
import { ApiError, Usuario } from '../../core/http/api.models';
import { UsuariosService } from './usuarios.service';

type Filtro = 'activos' | 'todos';
type Campo = 'nombre' | 'usuario' | 'contrasena' | 'rol';
interface ErrorApi { status: number; error?: ApiError; }

// Aplica el validador sobre el valor sin espacios alrededor, como hace el backend.
const recortado = (validador: ValidatorFn): ValidatorFn => (control) =>
  validador({ value: typeof control.value === 'string' ? control.value.trim() : control.value } as AbstractControl);

const PATRON_USUARIO = /^[a-z0-9._]{3,50}$/;
const PATRON_CONTRASENA = /^[\x21-\x7E]{4,72}$/;

const MENSAJES: Record<Campo, Record<string, string>> = {
  nombre: { required: 'El nombre es obligatorio.', maxlength: 'El nombre puede tener como máximo 100 caracteres.' },
  usuario: { required: 'El usuario es obligatorio.', pattern: 'El usuario tiene que tener de 3 a 50 caracteres: letras minúsculas, dígitos, punto o guion bajo.' },
  contrasena: { required: 'La contraseña es obligatoria.', pattern: 'La contraseña tiene que tener de 4 a 72 caracteres: letras sin tilde, dígitos o símbolos, sin espacios.' },
  rol: { required: 'El rol es obligatorio.' },
};

@Component({
  selector: 'app-usuarios-page',
  standalone: true,
  imports: [DatePipe, FormsModule, ReactiveFormsModule, ButtonModule, ConfirmDialogModule, DialogModule, InputTextModule, PasswordModule, ProgressSpinnerModule, SelectModule, SelectButtonModule, TableModule, TagModule],
  providers: [ConfirmationService],
  templateUrl: './usuarios-page.html',
  styleUrl: './usuarios-page.css',
})
export class UsuariosPage {
  private readonly servicio = inject(UsuariosService);
  private readonly auth = inject(AuthService);
  private readonly confirmacion = inject(ConfirmationService);

  protected readonly opcionesFiltro = [{ label: 'Activos', value: 'activos' }, { label: 'Todos', value: 'todos' }];
  protected readonly roles: UserRole[] = ['ADMIN', 'MOZO', 'COCINA', 'BARRA'];

  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly filtro = signal<Filtro>('activos');
  protected readonly cargando = signal(true);
  protected readonly error = signal('');
  protected readonly errorDialogo = signal('');
  protected readonly erroresCampo = signal<Record<string, string>>({});
  protected readonly procesando = signal(false);
  protected readonly dialogoVisible = signal(false);
  protected readonly editando = signal<Usuario | null>(null);
  protected readonly usuariosFiltrados = computed(() =>
    this.filtro() === 'activos' ? this.usuarios().filter((usuario) => !usuario.fechaBaja) : this.usuarios());

  protected readonly form = inject(FormBuilder).nonNullable.group({
    nombre: ['', [recortado(Validators.required), recortado(Validators.maxLength(100))]],
    usuario: ['', [recortado(Validators.required), recortado(Validators.pattern(PATRON_USUARIO))]],
    contrasena: [''],
    rol: [null as UserRole | null, Validators.required],
  });

  constructor() {
    this.form.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.erroresCampo.set({}));
    this.cargar();
  }

  protected esPropio(usuario: Usuario): boolean {
    return usuario.id === this.auth.currentUser()?.id;
  }

  protected nuevo(): void {
    this.abrirDialogo(null);
    this.form.controls.contrasena.setValidators([Validators.required, Validators.pattern(PATRON_CONTRASENA)]);
    this.form.controls.contrasena.updateValueAndValidity();
  }

  protected modificar(usuario: Usuario): void {
    this.abrirDialogo(usuario);
    this.form.patchValue({ nombre: usuario.nombre, usuario: usuario.usuario, rol: usuario.rol });
    this.form.controls.usuario.disable();
    if (this.esPropio(usuario)) this.form.controls.rol.disable();
    // Vacía conserva la contraseña actual.
    this.form.controls.contrasena.setValidators(Validators.pattern(PATRON_CONTRASENA));
    this.form.controls.contrasena.updateValueAndValidity();
  }

  protected guardar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const valor = this.form.getRawValue();
    const nombre = valor.nombre.trim();
    const rol = valor.rol as UserRole;
    const editando = this.editando();
    const operacion: Observable<Usuario> = editando
      ? this.servicio.modificar(editando.id, valor.contrasena ? { nombre, rol, contrasena: valor.contrasena } : { nombre, rol })
      : this.servicio.crear({ nombre, usuario: valor.usuario.trim(), contrasena: valor.contrasena, rol });

    this.procesando.set(true);
    this.errorDialogo.set('');
    operacion.subscribe({
      next: () => { this.procesando.set(false); this.dialogoVisible.set(false); this.cargar(); },
      error: (error: ErrorApi) => {
        this.procesando.set(false);
        if (error.status === 400 && error.error?.detalles) this.erroresCampo.set(error.error.detalles);
        else this.errorDialogo.set(error.error?.mensaje ?? 'No se pudo guardar el usuario.');
      },
    });
  }

  protected confirmarBaja(usuario: Usuario): void {
    this.confirmacion.confirm({
      header: 'Dar de baja',
      message: `¿Dar de baja a ${usuario.usuario}? No va a poder iniciar sesión hasta que lo reactives.`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Aceptar',
      rejectLabel: 'Cancelar',
      rejectButtonProps: { severity: 'secondary', outlined: true },
      acceptButtonProps: { severity: 'danger' },
      accept: () => this.ejecutar(this.servicio.darDeBaja(usuario.id), 'No se pudo dar de baja al usuario.'),
    });
  }

  protected reactivar(usuario: Usuario): void {
    this.ejecutar(this.servicio.reactivar(usuario.id), 'No se pudo reactivar al usuario.');
  }

  protected mensajeCampo(campo: Campo): string | null {
    const delBackend = this.erroresCampo()[campo];
    if (delBackend) return delBackend;
    const control = this.form.controls[campo];
    if (!control.errors || !(control.touched || control.dirty)) return null;
    return MENSAJES[campo][Object.keys(control.errors)[0]] ?? null;
  }

  private abrirDialogo(usuario: Usuario | null): void {
    this.editando.set(usuario);
    this.form.reset();
    this.form.enable();
    this.errorDialogo.set('');
    this.erroresCampo.set({});
    this.dialogoVisible.set(true);
  }

  private ejecutar(operacion: Observable<unknown>, mensajePorDefecto: string): void {
    this.procesando.set(true);
    this.error.set('');
    operacion.subscribe({
      next: () => { this.procesando.set(false); this.cargar(); },
      error: (error: ErrorApi) => { this.procesando.set(false); this.error.set(error.error?.mensaje ?? mensajePorDefecto); },
    });
  }

  private cargar(): void {
    this.servicio.listar().subscribe({
      next: (usuarios) => { this.usuarios.set(usuarios); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.error.set('No se pudieron cargar los usuarios.'); },
    });
  }
}
