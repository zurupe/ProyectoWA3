
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = '/api/auth';

  constructor(private http: HttpClient) {}

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials).pipe(
      tap((res: any) => {
        if (res.access_token) {
          localStorage.setItem('access_token', res.access_token);
        }
        if (res.refresh_token) {
          localStorage.setItem('refresh_token', res.refresh_token);
        }
      })
    );
  }

  refreshToken(): Observable<any> {
    const refresh_token = localStorage.getItem('refresh_token');
    return this.http.post(`${this.apiUrl}/refresh`, { refresh_token }).pipe(
      tap((res: any) => {
        if (res.access_token) {
          localStorage.setItem('access_token', res.access_token);
        }
        if (res.refresh_token) {
          localStorage.setItem('refresh_token', res.refresh_token);
        }
      })
    );
  }

  logout() {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
  }

  getProfile(): Observable<any> {
    return this.http.get(`${this.apiUrl}/profile`);
  }
}
