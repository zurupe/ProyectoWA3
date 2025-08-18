import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

export interface TrackingUpdate {
  id: number;
  estado: string;
}

export interface ConsistencyCheck {
  pedidoId: number;
  estadoMySQL: string;
  estadoRedis: string;
  isConsistent: boolean;
  lastSync?: string;
}

@Injectable({ providedIn: 'root' })
export class TrackingService {
  private apiUrl = '/api/tracking';
  private pedidoApiUrl = '/api/pedidos';

  constructor(private http: HttpClient) {}

  /**
   * Obtener estado de tracking por ID de pedido (desde Redis)
   */
  getEstadoPedido(pedidoId: string | number): Observable<string> {
    return this.http.get<any>(`${this.apiUrl}/${pedidoId}`).pipe(
      map(response => response.estado)
    );
  }

  /**
   * Obtener estado de pedido desde MySQL
   */
  getEstadoPedidoMySQL(pedidoId: string | number): Observable<string> {
    return this.http.get<any>(`${this.pedidoApiUrl}/${pedidoId}`).pipe(
      map(pedido => pedido.estado)
    );
  }

  /**
   * Verificar consistencia entre MySQL y Redis
   */
  verificarConsistencia(pedidoId: string | number): Observable<ConsistencyCheck> {
    return forkJoin({
      mysql: this.getEstadoPedidoMySQL(pedidoId),
      redis: this.getEstadoPedido(pedidoId)
    }).pipe(
      map(({ mysql, redis }) => ({
        pedidoId: Number(pedidoId),
        estadoMySQL: mysql,
        estadoRedis: redis,
        isConsistent: mysql === redis,
        lastSync: new Date().toISOString()
      }))
    );
  }

  /**
   * Actualizar estado de tracking (solo admin)
   */
  actualizarEstadoPedido(pedidoId: string | number, estado: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/sync/${pedidoId}`, { estado });
  }

  /**
   * Crear tracking desde pedido
   */
  crearTrackingDesdePedido(pedidoId: string | number, estado: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/create-from-pedido/${pedidoId}`, { estado });
  }

  /**
   * Sincronizar tracking hacia pedido
   */
  sincronizarHaciaPedido(pedidoId: string | number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/sync-to-pedido/${pedidoId}`, {});
  }

  /**
   * Actualizar tracking desde pedido-service
   */
  actualizarTracking(trackingData: TrackingUpdate): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${trackingData.id}`, { estado: trackingData.estado });
  }

  /**
   * Sincronizar estado desde MySQL a Redis
   */
  sincronizarEstado(pedidoId: string | number): Observable<void> {
    return this.getEstadoPedidoMySQL(pedidoId).pipe(
      switchMap(estado => this.actualizarTracking({ id: Number(pedidoId), estado }))
    );
  }
}
