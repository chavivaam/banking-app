import { Injectable } from '@angular/core';
import { HttpBackend, HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CsrfService {
  private readonly http: HttpClient;

  constructor(backend: HttpBackend) {
    // Bypass interceptors: authErrorInterceptor must not navigate to /login
    // during APP_INITIALIZER, and the CSRF bootstrap GET needs no CSRF header.
    this.http = new HttpClient(backend);
  }

  bootstrap(): Observable<void> {
    return this.http.get<void>('/api/csrf', { withCredentials: true });
  }
}
