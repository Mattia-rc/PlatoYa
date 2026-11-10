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
});
