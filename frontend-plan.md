# Banking App — Frontend Technical Plan

## Overview

Angular single-page application that consumes the Spring Boot backend.
Authentication is session-based (Spring Security form login with cookies — no JWT).
Role-based UI: **USER** sees their own transactions and can create new ones;
**ADMIN** sees all transactions and can create new users.

---

## Tech Stack

| Concern | Choice |
|---|---|
| Framework | Angular (latest stable) — standalone components |
| State | Angular Signals |
| HTTP | `HttpClient` with functional interceptors |
| Routing | Angular Router with lazy-loaded feature routes |
| UI | Angular Material (minimal palette) |
| Forms | Reactive Forms (`FormBuilder`, `Validators`) |

---

## Project Structure

```
src/
├── app/
│   ├── core/
│   │   ├── interceptors/
│   │   │   ├── credentials.interceptor.ts   # withCredentials on every request
│   │   │   └── auth-error.interceptor.ts    # redirect to /login on 401 only
│   │   ├── guards/
│   │   │   ├── auth.guard.ts               # blocks unauthenticated users (used on /dashboard route)
│   │   │   └── admin.guard.ts              # helper — not on a route; imported by DashboardComponent for @if checks
│   │   ├── models/
│   │   │   ├── user.model.ts
│   │   │   └── transaction.model.ts
│   │   └── services/
│   │       ├── auth.service.ts
│   │       ├── transaction.service.ts
│   │       └── user.service.ts
│   ├── features/
│   │   ├── auth/
│   │   │   └── login/
│   │   │       ├── login.component.ts
│   │   │       ├── login.component.html
│   │   │       └── login.component.scss
│   │   └── dashboard/
│   │       ├── dashboard.component.ts       # shell, reads role signal
│   │       ├── dashboard.component.html
│   │       ├── dashboard.component.scss
│   │       ├── transaction-list/
│   │       │   ├── transaction-list.component.ts
│   │       │   ├── transaction-list.component.html
│   │       │   └── transaction-list.component.scss
│   │       ├── create-transaction/
│   │       │   ├── create-transaction.component.ts
│   │       │   ├── create-transaction.component.html
│   │       │   └── create-transaction.component.scss
│   │       └── add-user/                    # rendered only for ADMIN
│   │           ├── add-user.component.ts
│   │           ├── add-user.component.html
│   │           └── add-user.component.scss
│   ├── app.routes.ts
│   ├── app.config.ts                        # provideHttpClient, provideRouter
│   └── app.component.ts
```

---

## Models

### `user.model.ts`
```ts
export type Role = 'USER' | 'ADMIN';

export interface UserDTO {
  userId: number;
  username: string;
  role: Role;
}

export interface CreateUserRequest {
  userName: string;
  password: string;
  role: Role;
}
```

### `transaction.model.ts`
```ts
export type TransactionType = 'CREDIT' | 'DEBIT';

export interface TransactionDTO {
  id: number;
  userId: number;
  username: string;
  type: TransactionType;
  amount: number;
  description: string;
}

export interface TransactionRequest {
  type: TransactionType;
  amount: number;
  description: string;
}
```

---

## Interceptors

### 1. `credentials.interceptor.ts`

Attaches `withCredentials: true` to **every** outgoing request so the browser
sends the Spring Security session cookie automatically.

```ts
export const credentialsInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req.clone({ withCredentials: true }));
};
```

### 2. `auth-error.interceptor.ts`

Catches `401 Unauthorized` and redirects to the login page (clears local auth
state first).

`403 Forbidden` means the user *is* authenticated but lacks the required role —
it must **not** trigger a logout redirect; the component receiving it should
display an access-denied message instead.

```ts
export const authErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !router.url.startsWith('/login')) {
        // Only redirect when the user is not already on the login page.
        // Without this check, a failed login attempt triggers a redirect loop:
        // wrong password → 401 → navigate('/login') → already there → repeat.
        inject(AuthService).clearSession();
        router.navigate(['/login']);
      }
      // 403 = authenticated but wrong role — propagate, let the caller handle it
      return throwError(() => err);
    })
  );
};
```

### 3. CSRF — Angular built-in XSRF handling

Spring Security enables CSRF protection by default. Without a valid token every
state-changing request (`POST /login`, `POST /transaction/create`,
`POST /user/create`, `POST /logout`) will be rejected with **403 Forbidden**.

Angular's `HttpClient` has built-in XSRF support — no extra interceptor file is
needed. It reads the CSRF token from a cookie named `XSRF-TOKEN` and
automatically attaches it as `X-XSRF-TOKEN` on every mutating request.

> **Backend requirement — add to `SecurityFilterChain`:**
> ```java
> .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
> ```
> `withHttpOnlyFalse()` makes the cookie readable by JavaScript so Angular can
> pick it up. Without this line the cookie is `HttpOnly` and Angular cannot
> read it, so every POST fails.

> **CSRF bootstrap dependency:** Spring writes the `XSRF-TOKEN` cookie on the
> first response it processes (including unauthenticated GETs). The
> `APP_INITIALIZER` call to `GET /user/me` guarantees the cookie is present
> before the user submits `POST /login`. If `fetchCurrentUser()` is ever
> removed from app init, the first login attempt will fail with 403.

All three concerns wired together in `app.config.ts`:
```ts
provideHttpClient(
  withInterceptors([credentialsInterceptor, authErrorInterceptor]),
  withXsrfConfiguration({ cookieName: 'XSRF-TOKEN', headerName: 'X-XSRF-TOKEN' })
)
```

> **CORS requirement:** `withCredentials: true` requires the backend to respond
> with the exact Angular origin, not a wildcard. Add to `SecurityFilterChain`:
> ```java
> .cors(cors -> cors.configurationSource(request -> {
>     CorsConfiguration cfg = new CorsConfiguration();
>     cfg.setAllowedOrigins(List.of("http://localhost:4200"));
>     cfg.setAllowedMethods(List.of("GET","POST","OPTIONS"));
>     cfg.setAllowCredentials(true);
>     cfg.setAllowedHeaders(List.of("*"));
>     return cfg;
> }))
> ```
> Browsers silently block credentialed requests to a `*` origin.

---

## Guards

### `auth.guard.ts` — functional guard
Redirects to `/login` if `AuthService.currentUser()` signal is null.

```ts
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.currentUser() ? true : router.createUrlTree(['/login']);
};
```

### `admin.guard.ts` — role helper (used in templates, not on a route)
This app uses a single `/dashboard` route for both roles; ADMIN vs USER content
is toggled with `@if` inside `DashboardComponent`. A `CanActivateFn` is not
needed on the route, but the role check is extracted here for reuse:

```ts
export const isAdmin = (): boolean =>
  inject(AuthService).currentUser()?.role === 'ADMIN';
```

`DashboardComponent` imports `isAdmin` and uses it in its template:
```html
@if (isAdmin()) { <app-add-user /> }
```

---

## Services

### `auth.service.ts`
Owns a `Signal<UserDTO | null>` that is the single source of truth for
the current session.

| Method | Backend call | Notes |
|---|---|---|
| `login(username, password)` | `POST /login` — **`application/x-www-form-urlencoded`** via `HttpParams` (not JSON) | Sets `currentUser` signal; see login flow below |
| `logout()` | `POST /logout` | Spring Security invalidates the session; clears signal, navigates to `/login` |
| `fetchCurrentUser()` | `GET /user/me` | Called on app init to restore session after page refresh |
| `clearSession()` | — | Used by `authErrorInterceptor` |

Signal declaration:
```ts
readonly currentUser = signal<UserDTO | null>(null);
```

#### App initialisation — session restore & CSRF bootstrap

`fetchCurrentUser()` must be called via `APP_INITIALIZER` so that:
- Route guards read a fully-resolved signal (no race condition)
- The GET request causes Spring to write the `XSRF-TOKEN` cookie **before** any `POST /login` is attempted (CSRF bootstrap)

```ts
// app.config.ts
{
  provide: APP_INITIALIZER,
  useFactory: (auth: AuthService) => () =>
    auth.fetchCurrentUser().pipe(catchError(() => of(null))),
  deps: [AuthService],
  multi: true
}
```

`catchError(() => of(null))` silently swallows the 401 that Spring returns for
an unauthenticated session. Without it, the interceptor catches the 401,
tries to navigate to `/login`, and the app breaks during startup.

#### Login request format

Spring Security's form login parser expects `username` and `password` as
form fields, **not** a JSON body. Use `HttpParams`:

```ts
login(username: string, password: string): Observable<UserDTO> {
  const body = new HttpParams()
    .set('username', username)
    .set('password', password);
  return this.http.post('/login', body, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
  }).pipe(switchMap(() => this.fetchCurrentUser()));
}
```

#### Login response & session restoration

Spring Security's default `formLogin(Customizer.withDefaults())` responds
with a **302 redirect** on both success and failure — not usable as-is by
an Angular SPA. Two required backend changes:

```java
.formLogin(form -> form
    .successHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_OK))
    .failureHandler((req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
)
.exceptionHandling(ex -> ex
    // Returns 401 instead of the default 302-to-login-page for unauthenticated requests
    .authenticationEntryPoint((req, res, e) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
    // Returns 403 instead of the default 302-to-/access-denied for authorisation failures.
    // Without this, Angular's catchError never fires — it receives a redirect to an HTML page.
    .accessDeniedHandler((req, res, e) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
)
```

With this in place:
- Successful `POST /login` → **200 OK** (session cookie set) → Angular calls `GET /user/me`
- Failed login → **401** → `authErrorInterceptor` does *not* redirect (it's the login page itself calling this); `LoginComponent` reads the error and shows a message
- Unauthenticated access to any protected route → **401** → interceptor redirects to `/login`

#### `/user/me` endpoint — needs to be added to the backend

`UserService.getCurrentUser()` already exists in the TSD but is not exposed
as a controller route. Add this to `UserController`:

```java
@GetMapping("/me")
public UserDTO me(@AuthenticationPrincipal HUser user) {
    return new UserDTO(user.getId(), user.getUsername(), user.getRole());
}
```

This endpoint is also how the Angular app restores auth state on page refresh
(session cookie still valid → `GET /user/me` succeeds → signal populated).

### `transaction.service.ts`

| Method | Backend call | Who calls it |
|---|---|---|
| `getMyTransactions()` | `GET /transaction/getAllTransactions` | USER |
| `getAllTransactions()` | `GET /transaction/getAllUsersTransactions` | ADMIN |
| `createTransaction(req)` | `POST /transaction/create` | USER |

### `user.service.ts`

| Method | Backend call | Who calls it |
|---|---|---|
| `createUser(req)` | `POST /user/create` | ADMIN |
| `getAllUsers()` | `GET /user/getAllUsers` | ADMIN |

---

## Routing (`app.routes.ts`)

```ts
export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component')
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard.component')
  },
  { path: '**', redirectTo: 'login' }
];
```

The `admin.guard` is applied **inside** the dashboard component via `@if`
conditionals rather than a separate route, keeping the URL surface simple.

---

## Components

### `LoginComponent`
- Reactive form: `username` + `password` fields
- Calls `AuthService.login()`, navigates to `/dashboard` on success
- Shows inline error on failed login (401)

### `DashboardComponent`
- Reads `currentUser()` signal to decide layout
- **USER layout:** CreateTransaction form (left) + TransactionList table (right)
- **ADMIN layout:** TransactionList table (full width) + AddUser form (right panel)
- Navbar with username display and Logout button

### `TransactionListComponent`
- Input signal: `transactions: TransactionDTO[]`
- Renders an Angular Material table
- ADMIN view includes a **Username** column; USER view omits it
- Columns: Date | Type (chip — green CREDIT / red DEBIT) | Amount | Description

### `CreateTransactionComponent`
- Reactive form: `type` (select: CREDIT / DEBIT), `amount` (number), `description` (text)
- On submit calls `TransactionService.createTransaction()`, emits a `created` output signal
- Parent refreshes transaction list on `created`

### `AddUserComponent` *(ADMIN only)*
- Reactive form: `userName`, `password`, `role` (select: USER / ADMIN)
- Calls `UserService.createUser()`
- Shows success snackbar or inline error

---

## UI Design Notes

- **Theme:** Angular Material with a navy-blue primary and gold accent — clean banking aesthetic.
- **Layout:** Full-viewport flex column. Sticky top navbar. Main content area is a 2-column grid on desktop, single column on mobile.
- **Forms:** All inputs use `mat-form-field` with outline appearance. Submit buttons are full-width inside their card.
- **Table:** `mat-table` with alternating row shading, sortable Amount column.
- **Feedback:** `MatSnackBar` for success/error toasts; inline `mat-error` under form fields for validation messages.
- **Loading state:** `MatProgressBar` at the top of the table while data is fetching.

---

## Backend API Reference (from TSD)

| Angular call | HTTP | Spring endpoint | Role | Notes |
|---|---|---|---|---|
| Login | `POST /login` | Spring Security form login | All | form-encoded; needs custom success/failure handlers (see AuthService) |
| Current user | `GET /user/me` | `UserController` *(add this route)* | Authenticated | Restores session on refresh; maps to `UserService.getCurrentUser()` |
| Create transaction | `POST /transaction/create` | `TransactionController` | USER | |
| My transactions | `GET /transaction/getAllTransactions` | `TransactionController` | USER | |
| All transactions | `GET /transaction/getAllUsersTransactions` | `TransactionController` | ADMIN | |
| Create user | `POST /user/create` | `UserController` | ADMIN | |
| All users | `GET /user/getAllUsers` | `UserController` | ADMIN | |
| Logout | `POST /logout` | Spring Security built-in | Authenticated | Invalidates session server-side |

Spring Security manages the session cookie; no manual token handling is needed.
`credentialsInterceptor` attaches `withCredentials: true` to every request,
and Angular's built-in XSRF support handles CSRF tokens automatically.
