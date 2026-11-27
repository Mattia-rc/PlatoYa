import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfirmationService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectButtonModule } from 'primeng/selectbutton';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { Observable } from 'rxjs';
import { ApiError, MesaAdministracion, MesaRequest } from '../../core/http/api.models';
import { MesasService } from './mesas.service';

type Filtro = 'activas' | 'todas';
type Campo = 'numero' | 'capacidad';
interface ErrorApi { status: number; error?: ApiError; }

@Component({
  selector: 'app-mesas-admin-page',
  standalone: true,
  imports: [DatePipe, FormsModule, ReactiveFormsModule, ButtonModule, ConfirmDialogModule, DialogModule, InputNumberModule, ProgressSpinnerModule, SelectButtonModule, TableModule, TagModule],
  providers: [ConfirmationService],
  templateUrl: './mesas-admin-page.html',
  styleUrl: './mesas-admin-page.css',
})
export class MesasAdminPage {
  private readonly servicio = inject(MesasService);
  private readonly confirmacion = inject(ConfirmationService);

  protected readonly opcionesFiltro = [{ label: 'Activas', value: 'activas' }, { label: 'Todas', value: 'todas' }];
  protected readonly mesas = signal<MesaAdministracion[]>([]);
  protected readonly filtro = signal<Filtro>('activas');
  protected readonly cargando = signal(true);
  protected readonly procesando = signal(false);
  protected readonly error = signal('');
  protected readonly errorDialogo = signal('');
  protected readonly erroresCampo = signal<Record<string, string>>({});
  protected readonly dialogoVisible = signal(false);
  protected readonly editando = signal<MesaAdministracion | null>(null);
  protected readonly mesasFiltradas = computed(() => this.filtro() === 'activas'
    ? this.mesas().filter((mesa) => !mesa.fechaBaja)
    : this.mesas());

  protected readonly form = inject(FormBuilder).nonNullable.group({
    numero: [1, [Validators.required, Validators.min(1)]],
    capacidad: [1, [Validators.required, Validators.min(1)]],
  });

  constructor() {
    this.cargar();
    this.form.valueChanges.subscribe(() => this.erroresCampo.set({}));
  }

  protected nueva(): void { this.abrirDialogo(null); }

  protected modificar(mesa: MesaAdministracion): void {
    this.abrirDialogo(mesa);
    this.form.setValue({ numero: mesa.numero, capacidad: mesa.capacidad });
  }

  protected guardar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const request = this.form.getRawValue() as MesaRequest;
    const mesa = this.editando();
    const operacion: Observable<MesaAdministracion> = mesa
      ? this.servicio.modificar(mesa.id, request)
      : this.servicio.crear(request);
    this.procesando.set(true);
    this.errorDialogo.set('');
    operacion.subscribe({
      next: () => { this.procesando.set(false); this.dialogoVisible.set(false); this.cargar(); },
      error: (error: ErrorApi) => {
        this.procesando.set(false);
        if (error.status === 400 && error.error?.detalles) this.erroresCampo.set(error.error.detalles);
        else this.errorDialogo.set(error.error?.mensaje ?? 'No se pudo guardar la mesa.');
      },
    });
  }

  protected confirmarBaja(mesa: MesaAdministracion): void {
    this.confirmacion.confirm({
      header: 'Dar de baja',
      message: `¿Dar de baja la mesa ${mesa.numero}? Podrás reactivarla más adelante.`,
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Aceptar', rejectLabel: 'Cancelar',
      rejectButtonProps: { severity: 'secondary', outlined: true },
      acceptButtonProps: { severity: 'danger' },
      accept: () => this.ejecutar(this.servicio.darDeBaja(mesa.id), 'No se pudo dar de baja la mesa.'),
    });
  }

  protected reactivar(mesa: MesaAdministracion): void {
    this.ejecutar(this.servicio.reactivar(mesa.id), 'No se pudo reactivar la mesa.');
  }

  protected mensajeCampo(campo: Campo): string | null {
    const backend = this.erroresCampo()[campo];
    if (backend) return backend;
    const control = this.form.controls[campo];
    if (!control.errors || !(control.touched || control.dirty)) return null;
    if (control.errors['required']) return campo === 'numero' ? 'El número es obligatorio.' : 'La capacidad es obligatoria.';
    return campo === 'numero' ? 'El número tiene que ser mayor que cero.' : 'La capacidad tiene que ser mayor que cero.';
  }

  private abrirDialogo(mesa: MesaAdministracion | null): void {
    this.editando.set(mesa);
    this.form.reset({ numero: 1, capacidad: 1 });
    this.errorDialogo.set('');
    this.erroresCampo.set({});
    this.dialogoVisible.set(true);
  }

  private ejecutar(operacion: Observable<unknown>, mensaje: string): void {
    this.procesando.set(true);
    this.error.set('');
    operacion.subscribe({
      next: () => { this.procesando.set(false); this.cargar(); },
      error: (error: ErrorApi) => { this.procesando.set(false); this.error.set(error.error?.mensaje ?? mensaje); },
    });
  }

  private cargar(): void {
    this.cargando.set(true);
    this.error.set('');
    this.servicio.listar().subscribe({
      next: (mesas) => { this.mesas.set(mesas); this.cargando.set(false); },
      error: () => { this.cargando.set(false); this.error.set('No se pudieron cargar las mesas.'); },
    });
  }
}
