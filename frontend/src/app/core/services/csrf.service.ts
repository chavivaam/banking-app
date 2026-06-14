import { Injectable } from '@angular/core';
import { HttpBackend, HttpClient } from '@angular/common/http';
import { finalize, map, Observable, of, shareReplay } from 'rxjs';

function readCsrfCookie(): string | undefined {
  const entry = document.cookie
    .split(';')
    .map(c => c.trim())
    .find(c => c.startsWith('XSRF-TOKEN='));
  return entry ? decodeURIComponent(entry.slice('XSRF-TOKEN='.length)) : undefined;
}

@Injectable({ providedIn: 'root' })
export class CsrfService {
  private readonly http: HttpClient;
  private pending: Observable<string> | null = null;

  constructor(backend: HttpBackend) {
    // HttpBackend bypasses all interceptors — prevents circular dependency and
    // avoids infinite loops when ensureToken() is called from inside the interceptor.
    this.http = new HttpClient(backend);
  }

  /** Called once by APP_INITIALIZER to seed the XSRF-TOKEN cookie on startup. */
  bootstrap(): Observable<void> {
    return this.http.get<void>('/api/csrf', { withCredentials: true });
  }

  /**
   * Returns the current CSRF token, preflighting GET /api/csrf if the cookie
   * is absent. Concurrent callers share a single in-flight request.
   */
  ensureToken(): Observable<string> {
    const cookie = readCsrfCookie();
    if (cookie) return of(cookie);

    if (!this.pending) {
      this.pending = this.http
        .get<void>('/api/csrf', { withCredentials: true })
        .pipe(
          map(() => {
            const token = readCsrfCookie();
            if (!token) throw new Error('CSRF token unavailable after preflight');
            return token;
          }),
          finalize(() => { this.pending = null; }),
          shareReplay(1)
        );
    }

    return this.pending;
  }

  /**
   * Clears any in-flight prefetch. Called by logout so a stale pending request
   * from the previous session is not reused by the next user.
   */
  invalidate(): void {
    this.pending = null;
  }
}
