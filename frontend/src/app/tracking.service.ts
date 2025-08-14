import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TrackingService {
  private apiUrl = 'http://localhost:8084/api/tracking';

  constructor(private http: HttpClient) {}

  getEstadoPedido(id: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }
}
