import { DestroyRef, inject, Injectable } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, EMPTY, Observable, switchMap, timer } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PollingService {
  poll<T>(request: () => Observable<T>, destroyRef = inject(DestroyRef), onError?: (error: unknown) => void): Observable<T> {
    return timer(0, environment.pollingIntervalMs).pipe(
      switchMap(() => request().pipe(catchError((error) => {
        onError?.(error);
        return EMPTY;
      }))),
      takeUntilDestroyed(destroyRef),
    );
  }
}
