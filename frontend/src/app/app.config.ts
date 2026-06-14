import { APP_INITIALIZER, ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import { catchError, of } from 'rxjs';
import { routes } from './app.routes';
import { credentialsInterceptor } from './core/interceptors/credentials.interceptor';
import { csrfInterceptor } from './core/interceptors/csrf.interceptor';
import { authErrorInterceptor } from './core/interceptors/auth-error.interceptor';
import { CsrfService } from './core/services/csrf.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideAnimations(),
    provideHttpClient(
      withInterceptors([credentialsInterceptor, csrfInterceptor, authErrorInterceptor])
    ),
    {
      provide: APP_INITIALIZER,
      useFactory: (csrf: CsrfService) => () => csrf.bootstrap().pipe(catchError(() => of(null))),
      deps: [CsrfService],
      multi: true
    }
  ]
};
