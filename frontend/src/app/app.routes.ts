import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: 'login', canActivate: [guestGuard], loadComponent: () => import('./features/login/login').then((m) => m.Login) },
  {
    path: '', canActivate: [authGuard], loadComponent: () => import('./layout/app-layout').then((m) => m.AppLayout),
    children: [
      { path: 'mozo', canActivate: [roleGuard(['MOZO', 'ADMIN'])], loadComponent: () => import('./features/mozo/mozo-dashboard').then((m) => m.MozoDashboard) },
      { path: 'cocina', canActivate: [roleGuard(['COCINA', 'ADMIN'])], loadComponent: () => import('./features/sector/sector-dashboard').then((m) => m.SectorDashboard), data: { sector: 'COCINA' } },
      { path: 'barra', canActivate: [roleGuard(['BARRA', 'ADMIN'])], loadComponent: () => import('./features/sector/sector-dashboard').then((m) => m.SectorDashboard), data: { sector: 'BARRA' } },
      { path: '', pathMatch: 'full', redirectTo: 'mozo' },
    ],
  },
  { path: '**', redirectTo: '' },
];
