import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'business',
    canActivate: [authGuard],
    data: { role: 'BUSINESS' },
    children: [
      {
        path: 'coupons',
        loadComponent: () => import('./features/business/coupons/coupons.component').then(m => m.BusinessCouponsComponent)
      }
    ]
  },
  {
    path: 'admin',
    canActivate: [authGuard],
    data: { role: 'ADMIN' },
    children: [
      {
        path: 'businesses',
        loadComponent: () => import('./features/admin/business-list/business-list.component').then(m => m.BusinessListComponent)
      },
      {
        path: 'coupons',
        loadComponent: () => import('./features/admin/coupons/coupons.component').then(m => m.AdminCouponsComponent)
      },
      {
        path: 'deals/create',
        loadComponent: () => import('./features/admin/deal-create/deal-create.component').then(m => m.DealCreateComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];
