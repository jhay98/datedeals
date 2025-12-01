import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }

  const requiredRole = route.data['role'];
  if (requiredRole) {
    const userRole = authService.getRole();
    if (userRole !== requiredRole) {
      // Redirect to appropriate dashboard based on role
      if (authService.isAdmin()) {
        router.navigate(['/admin/businesses']);
      } else {
        router.navigate(['/business/coupons']);
      }
      return false;
    }
  }

  return true;
};
