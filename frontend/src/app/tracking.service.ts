import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TrackingUpdate {
  id: number;
  estado: string;
}

@Injectable({ providedIn: 'root' })
export class TrackingService {
  private apiUrl = '/api/tracking';

  constructor(private http: HttpClient) {}

  /**
   * Obtener estado de tracking por ID de pedido
   */
  getEstadoPedido(pedidoId: string | number): Observable<string> {
    return this.http.get(`${this.apiUrl}/${pedidoId}`, { responseType: 'text' });
  }

  /**
   * Actualizar estado de tracking (solo admin)
   */
  actualizarEstadoPedido(pedidoId: string | number, estado: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${pedidoId}`, estado, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  /**
   * Actualizar tracking desde pedido-service
   */
  actualizarTracking(trackingData: TrackingUpdate): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/update`, trackingData);
  }
}
