import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = '/api/auth';

  constructor(private http: HttpClient) {}

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials);
  }

  getProfile(): Observable<any> {
    return this.http.get(`${this.apiUrl}/me`);
  }

  getUsers(): Observable<any> {
    return this.http.get(`${this.apiUrl}/usuarios`);
  }

  isAuthenticated(): boolean {
    const token = localStorage.getItem('access_token');
    if (!token) return false;
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const now = Math.floor(Date.now() / 1000);
      return payload.exp > now;
    } catch (e) {
      return false;
    }
  }

  getUserRole(): string | null {
    const token = localStorage.getItem('access_token');
    if (!token) return null;
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.role || null;
    } catch (e) {
      return null;
    }
  }

  getUserInfo(): { username: string; role: string } | null {
    const token = localStorage.getItem('access_token');
    if (!token) return null;
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return {
        username: payload.sub || '',
        role: payload.role || ''
      };
    } catch (e) {
      return null;
    }
  }

  logout(): void {
    localStorage.removeItem('access_token');
  }
}
