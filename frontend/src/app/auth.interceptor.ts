import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpInterceptor,
  HttpHandler,
  HttpRequest,
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { inject } from '@angular/core';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('access_token');
    let clonedRequest = req;

    console.log('🔍 Interceptor - URL:', req.url);
    console.log('🔑 Token presente:', !!token);

    if (token) {
      clonedRequest = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      console.log('✅ Token añadido al request');
    } else {
      console.log('❌ No hay token disponible');
    }

    return next.handle(clonedRequest).pipe(
      catchError((error: HttpErrorResponse) => {
        console.log('❌ Error en request:', error.status, error.url);
        if (error.status === 401) {
          console.log('🚫 Error 401 - Token expirado o inválido');
          // Token expirado o inválido
          localStorage.removeItem('access_token');
          this.router.navigate(['/login']);
        }
        return throwError(() => error);
      })
    );
  }
}

// Nueva función interceptor para Angular 17+
export const authInterceptorFn: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('access_token');
  let clonedRequest = req;

  console.log('🔍 Interceptor - URL:', req.url);
  console.log('🔑 Token presente:', !!token);

  if (token) {
    clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    console.log('✅ Token añadido al request');
  } else {
    console.log('❌ No hay token disponible');
  }

  return next(clonedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      console.log('❌ Error en request:', error.status, error.url);
      if (error.status === 401) {
        console.log('🚫 Error 401 - Token expirado o inválido');
        // Token expirado o inválido
        localStorage.removeItem('access_token');
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
