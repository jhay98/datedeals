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
    path: 'redeem/:couponCode',
    canActivate: [authGuard],
    loadComponent: () => import('./features/auth/redeem/redeem.component').then(m => m.RedeemComponent)
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
        path: 'businesses/create',
        loadComponent: () => import('./features/admin/business-create/business-create.component').then(m => m.BusinessCreateComponent)
      },
      {
        path: 'businesses/edit/:id',
        loadComponent: () => import('./features/admin/business-edit/business-edit.component').then(m => m.BusinessEditComponent)
      },
      {
        path: 'deals',
        loadComponent: () => import('./features/admin/deal-list/deal-list.component').then(m => m.DealListComponent)
      },
      {
        path: 'deals/create',
        loadComponent: () => import('./features/admin/deal-create/deal-create.component').then(m => m.DealCreateComponent)
      },
      {
        path: 'deals/edit/:id',
        loadComponent: () => import('./features/admin/deal-edit/deal-edit.component').then(m => m.DealEditComponent)
      },
      {
        path: 'coupons',
        loadComponent: () => import('./features/admin/coupons/coupons.component').then(m => m.AdminCouponsComponent)
      },
      {
        path: 'users',
        loadComponent: () => import('./features/admin/user-list/user-list.component').then(m => m.UserListComponent)
      },
      {
        path: 'users/create',
        loadComponent: () => import('./features/admin/user-create/user-create.component').then(m => m.UserCreateComponent)
      },
      {
        path: 'users/edit/:id',
        loadComponent: () => import('./features/admin/user-edit/user-edit.component').then(m => m.UserEditComponent)
      }
    ]
  },
  {
    path: '**',
    redirectTo: '/login'
  }
];
