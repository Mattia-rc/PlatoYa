import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { ApiService } from '../../core/http/api.service';
import { Destino, EstadoItem, ItemPreparacion } from '../../core/http/api.models';
import { PollingService } from '../../core/polling/polling.service';
import { forkJoin } from 'rxjs';
import { KpiPreparacion } from '../../core/http/api.models';

@Component({ selector: 'app-sector-dashboard', standalone: true, imports: [DatePipe, ButtonModule, ProgressSpinnerModule, TagModule], templateUrl: './sector-dashboard.html', styleUrl: './sector-dashboard.css' })
export class SectorDashboard {
  private readonly api = inject(ApiService);
  private readonly polling = inject(PollingService);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly sector = inject(ActivatedRoute).snapshot.data['sector'] as Destino;
  protected readonly items = signal<ItemPreparacion[]>([]);
  protected readonly kpis = signal<KpiPreparacion | null>(null);
  protected readonly cargando = signal(true);
  protected readonly procesando = signal<number | null>(null);
  protected readonly error = signal('');

  constructor() { this.iniciarPolling(); }

  protected cambiarEstado(item: ItemPreparacion, estado: EstadoItem): void {
    this.procesando.set(item.id);
    this.api.cambiarEstadoItem(item.id, estado).subscribe({
      next: (actualizado) => { this.items.update((items) => items.map((actual) => actual.id === actualizado.id ? actualizado : actual)); this.procesando.set(null); },
      error: (error: HttpErrorResponse) => { this.error.set(error.error?.mensaje ?? 'No se pudo actualizar el estado del ítem.'); this.procesando.set(null); },
    });
  }

  protected severity(estado: EstadoItem): 'secondary' | 'info' | 'success' | 'danger' {
    return estado === 'PENDIENTE' ? 'secondary' : estado === 'EN_PREPARACION' ? 'info' : estado === 'ANULADO' ? 'danger' : 'success';
  }

  private iniciarPolling(): void {
    this.polling.poll(() => forkJoin({ items: this.api.obtenerItemsPorSector(this.sector), kpis: this.api.obtenerKpisPreparacion(this.sector) }), this.destroyRef).subscribe({
      next: ({ items, kpis }) => { this.items.set(items); this.kpis.set(kpis); this.cargando.set(false); this.error.set(''); },
      error: () => { this.cargando.set(false); this.error.set(`No se pudo cargar la pantalla de ${this.sector.toLowerCase()}.`); },
    });
  }
}
