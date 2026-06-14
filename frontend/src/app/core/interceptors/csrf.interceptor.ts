import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { switchMap } from 'rxjs';
import { CsrfService } from '../services/csrf.service';

const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS', 'TRACE']);

export const csrfInterceptor: HttpInterceptorFn = (req, next) => {
  if (SAFE_METHODS.has(req.method)) return next(req);

  return inject(CsrfService)
    .ensureToken()
    .pipe(switchMap(token => next(req.clone({ headers: req.headers.set('X-XSRF-TOKEN', token) }))));
};
