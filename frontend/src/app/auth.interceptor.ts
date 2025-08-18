import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpInterceptor,
  HttpHandler,
  HttpRequest,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService, private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem('access_token');
    let authReq = req;
    if (token) {
      authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }
    return next.handle(authReq).pipe(
      catchError((error: any) => {
        if (error instanceof HttpErrorResponse && error.status === 401) {
          const refreshToken = localStorage.getItem('refresh_token');
          if (refreshToken) {
            // Intentar refrescar el token
            return this.authService.refreshToken().pipe(
              switchMap((res: any) => {
                if (res.access_token) {
                  const retryReq = req.clone({
                    setHeaders: {
                      Authorization: `Bearer ${res.access_token}`
                    }
                  });
                  return next.handle(retryReq);
                } else {
                  this.authService.logout();
                  this.router.navigate(['/login']);
                  return throwError(() => error);
                }
              }),
              catchError(err => {
                this.authService.logout();
                this.router.navigate(['/login']);
                return throwError(() => err);
              })
            );
          } else {
            this.authService.logout();
            this.router.navigate(['/login']);
            return throwError(() => error);
          }
        }
        return throwError(() => error);
      })
    );
  }
}
