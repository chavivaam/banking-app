import { HttpInterceptorFn } from '@angular/common/http';

const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS', 'TRACE']);

function getCsrfToken(): string | undefined {
  const entry = document.cookie
    .split(';')
    .map(c => c.trim())
    .find(c => c.startsWith('XSRF-TOKEN='));
  return entry ? decodeURIComponent(entry.slice('XSRF-TOKEN='.length)) : undefined;
}

export const csrfInterceptor: HttpInterceptorFn = (req, next) => {
  if (SAFE_METHODS.has(req.method)) return next(req);
  const token = getCsrfToken();
  return next(token ? req.clone({ headers: req.headers.set('X-XSRF-TOKEN', token) }) : req);
};
