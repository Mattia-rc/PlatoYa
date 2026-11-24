import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { ApiService } from './api.service';

describe('ApiService en modo mock', () => {
  let service: ApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient()] });
    service = TestBed.inject(ApiService);
  });

  it('permite ingresar como admin con la contraseña de demostración', async () => {
    const response = await firstValueFrom(service.login({ usuario: 'admin', contrasena: '1234' }));

    expect(response.usuario.rol).toBe('ADMIN');
    expect(response.token).toBe('mock-token-admin');
  });

  it('envía una comanda y la incorpora al seguimiento de la cuenta', async () => {
    const productos = await firstValueFrom(service.obtenerProductos());
    const hamburguesa = productos.find((producto) => producto.nombre === 'Hamburguesa completa');

    expect(hamburguesa).toBeDefined();
    const comanda = await firstValueFrom(service.enviarComanda(101, {
      items: [{ productoId: hamburguesa!.id, cantidad: 1, observacion: 'sin cebolla' }],
    }));
    const items = await firstValueFrom(service.obtenerItemsCuenta(101));

    expect(comanda.numero).toBe(2);
    expect(comanda.items[0].estado).toBe('PENDIENTE');
    expect(comanda.items[0].destino).toBe('COCINA');
    expect(items.some((item) => item.id === comanda.items[0].id)).toBeTrue();
  });

  it('refleja en cocina los cambios de estado del ítem enviado', async () => {
    const comanda = await firstValueFrom(service.enviarComanda(101, {
      items: [{ productoId: 3, cantidad: 1, observacion: 'sin cebolla' }],
    }));
    const item = comanda.items[0];

    await firstValueFrom(service.cambiarEstadoItem(item.id, 'EN_PREPARACION'));
    const actualizado = await firstValueFrom(service.cambiarEstadoItem(item.id, 'LISTO'));
    const itemsCocina = await firstValueFrom(service.obtenerItemsPorSector('COCINA'));
    const itemsCuenta = await firstValueFrom(service.obtenerItemsCuenta(101));

    expect(actualizado.estado).toBe('LISTO');
    expect(itemsCocina.find((actual) => actual.id === item.id)?.estado).toBe('LISTO');
    expect(itemsCuenta.find((actual) => actual.id === item.id)?.estado).toBe('LISTO');
  });
});
