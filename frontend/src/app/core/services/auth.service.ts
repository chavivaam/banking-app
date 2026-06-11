import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, switchMap, tap } from 'rxjs';
import { UserDTO } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly currentUser = signal<UserDTO | null>(null);

  login(username: string, password: string): Observable<UserDTO> {
    const body = new HttpParams()
      .set('username', username)
      .set('password', password);
    return this.http.post('/login', body, {
      headers: new HttpHeaders({ 'Content-Type': 'application/x-www-form-urlencoded' })
    }).pipe(switchMap(() => this.fetchCurrentUser()));
  }

  logout(): void {
    this.http.post('/logout', null).subscribe({
      complete: () => {
        this.clearSession();
        this.router.navigate(['/login']);
      }
    });
  }

  fetchCurrentUser(): Observable<UserDTO> {
    return this.http.get<UserDTO>('/user/me').pipe(
      tap(user => this.currentUser.set(user))
    );
  }

  clearSession(): void {
    this.currentUser.set(null);
  }
}
