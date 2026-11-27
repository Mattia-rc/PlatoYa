import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PollingService } from './polling.service';

describe('PollingService', () => {
  it('continúa consultando después de un error', fakeAsync(() => {
    const service = TestBed.inject(PollingService);
    let intentos = 0;
    const recibidos: number[] = [];
    let errores = 0;

    TestBed.runInInjectionContext(() => {
      service.poll(() => ++intentos === 1
        ? throwError(() => new Error('error temporal'))
        : of(intentos), undefined, () => errores++).subscribe((valor) => recibidos.push(valor));
    });

    tick(0);
    expect(intentos).toBe(1);
    expect(errores).toBe(1);
    tick(environment.pollingIntervalMs);
    expect(intentos).toBe(2);
    expect(recibidos).toEqual([2]);
  }));
});
