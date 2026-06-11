import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !router.url.startsWith('/login')) {
        // Guard against redirect loop on the login page itself
        inject(AuthService).clearSession();
        router.navigate(['/login']);
      }
      return throwError(() => err);
    })
  );
};
