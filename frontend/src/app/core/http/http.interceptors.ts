import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../auth/auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(AuthService).token;
  return next(token ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request);
};
export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  return next(request).pipe(catchError((error: HttpErrorResponse) => {
    if (error.status === 401 && auth.isAuthenticated()) auth.logout();
    return throwError(() => error);
  }));
};
