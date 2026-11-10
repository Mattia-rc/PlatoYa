import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { ApiService } from '../../core/http/api.service';
import { MesaResumen } from '../../core/http/api.models';
import { PollingService } from '../../core/polling/polling.service';
import { forkJoin } from 'rxjs';
import { KpiMesas } from '../../core/http/api.models';

@Component({ selector: 'app-mozo-dashboard', standalone: true, imports: [ButtonModule, ProgressSpinnerModule, TagModule], templateUrl: './mozo-dashboard.html', styleUrl: './mozo-dashboard.css' })
export class MozoDashboard {
  private readonly api = inject(ApiService);
  private readonly polling = inject(PollingService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly mesas = signal<MesaResumen[]>([]);
  protected readonly kpis = signal<KpiMesas | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');
  protected readonly mesaProcesando = signal<number | null>(null);

  constructor() { this.iniciarPolling(); }

  protected abrirMesa(mesa: MesaResumen): void {
    this.mesaProcesando.set(mesa.id);
    this.api.abrirMesa(mesa.id).subscribe({
      next: (actualizada) => { this.mesas.update((mesas) => mesas.map((item) => item.id === actualizada.id ? actualizada : item)); this.mesaProcesando.set(null); },
      error: (error: HttpErrorResponse) => { this.error.set(error.error?.mensaje ?? `No se pudo abrir la mesa ${mesa.numero}.`); this.mesaProcesando.set(null); },
    });
  }

  private iniciarPolling(): void {
    this.polling.poll(() => forkJoin({ mesas: this.api.obtenerMesas(), kpis: this.api.obtenerKpisMesas() }), this.destroyRef).subscribe({
      next: ({ mesas, kpis }) => { this.mesas.set(mesas); this.kpis.set(kpis); this.cargando.set(false); this.error.set(''); },
      error: () => { this.cargando.set(false); this.error.set('No se pudieron cargar las mesas. Se reintentará al actualizar la pantalla.'); },
    });
  }
}
