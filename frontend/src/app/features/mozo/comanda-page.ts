import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { TextareaModule } from 'primeng/textarea';
import { ApiService } from '../../core/http/api.service';
import { CrearItemComandaRequest, EstadoItem, ItemPreparacion, ProductoResumen } from '../../core/http/api.models';
import { PollingService } from '../../core/polling/polling.service';

interface LineaBorrador extends CrearItemComandaRequest {
  producto: ProductoResumen;
}

@Component({
  selector: 'app-comanda-page',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink, ButtonModule, InputNumberModule, ProgressSpinnerModule, SelectModule, TagModule, TextareaModule],
  templateUrl: './comanda-page.html',
  styleUrl: './comanda-page.css',
})
export class ComandaPage {
  private readonly api = inject(ApiService);
  private readonly polling = inject(PollingService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly route = inject(ActivatedRoute);

  protected readonly mesaId = Number(this.route.snapshot.paramMap.get('mesaId'));
  protected readonly mesaNumero = Number(this.route.snapshot.queryParamMap.get('numero')) || this.mesaId;
  protected readonly cuentaId = Number(this.route.snapshot.paramMap.get('cuentaId'));
  protected readonly productos = signal<ProductoResumen[]>([]);
  protected readonly lineas = signal<LineaBorrador[]>([]);
  protected readonly items = signal<ItemPreparacion[]>([]);
  protected readonly cargando = signal(true);
  protected readonly enviando = signal(false);
  protected readonly error = signal('');
  protected readonly exito = signal('');

  protected productoId: number | null = null;
  protected cantidad = 1;
  protected observacion = '';

  constructor() {
    this.cargarProductos();
    this.iniciarSeguimiento();
  }

  protected agregarLinea(): void {
    const producto = this.productos().find((item) => item.id === this.productoId);
    if (!producto || !this.cantidad || this.cantidad < 1) {
      this.error.set('Seleccioná un producto e indicá una cantidad válida.');
      return;
    }

    this.lineas.update((lineas) => [...lineas, {
      producto,
      productoId: producto.id,
      cantidad: this.cantidad,
      observacion: this.observacion.trim() || undefined,
    }]);
    this.productoId = null;
    this.cantidad = 1;
    this.observacion = '';
    this.error.set('');
    this.exito.set('');
  }

  protected quitarLinea(indice: number): void {
    this.lineas.update((lineas) => lineas.filter((_, posicion) => posicion !== indice));
  }

  protected enviarComanda(): void {
    if (!this.lineas().length) {
      this.error.set('Agregá al menos un producto antes de enviar la comanda.');
      return;
    }

    const items = this.lineas().map(({ producto: _, ...linea }) => linea);
    this.enviando.set(true);
    this.error.set('');
    this.exito.set('');
    this.api.enviarComanda(this.cuentaId, { items }).subscribe({
      next: (comanda) => {
        this.items.update((actuales) => [...actuales, ...comanda.items]);
        this.lineas.set([]);
        this.enviando.set(false);
        this.exito.set(`Comanda #${comanda.numero} enviada correctamente.`);
      },
      error: (error: HttpErrorResponse) => {
        this.enviando.set(false);
        this.error.set(error.error?.mensaje ?? 'No se pudo enviar la comanda.');
      },
    });
  }

  protected totalBorrador(): number {
    return this.lineas().reduce((total, linea) => total + linea.producto.precio * linea.cantidad, 0);
  }

  protected severidad(estado: EstadoItem): 'secondary' | 'info' | 'success' | 'danger' {
    if (estado === 'PENDIENTE') return 'secondary';
    if (estado === 'EN_PREPARACION') return 'info';
    if (estado === 'ANULADO') return 'danger';
    return 'success';
  }

  private cargarProductos(): void {
    this.api.obtenerProductos().subscribe({
      next: (productos) => this.productos.set(productos),
      error: (error: HttpErrorResponse) => this.error.set(error.error?.mensaje ?? 'No se pudo cargar la carta.'),
    });
  }

  private iniciarSeguimiento(): void {
    this.polling.poll(() => this.api.obtenerItemsCuenta(this.cuentaId), this.destroyRef).subscribe({
      next: (items) => {
        this.items.set(items);
        this.cargando.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.cargando.set(false);
        this.error.set(error.error?.mensaje ?? 'No se pudieron consultar los ítems de la cuenta.');
      },
    });
  }
}
