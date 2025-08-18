import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PedidoRequest, PedidoResponse } from './models/pedido.model';

@Injectable({ providedIn: 'root' })
export class PedidoService {
  private apiUrl = '/api/pedidos';

  constructor(private http: HttpClient) {}

  /**
   * Obtener todos los pedidos (solo admin)
   */
  getPedidos(): Observable<PedidoResponse[]> {
    return this.http.get<PedidoResponse[]>(this.apiUrl);
  }

  /**
   * Crear nuevo pedido
   */
  createPedido(pedido: PedidoRequest): Observable<PedidoResponse> {
    return this.http.post<PedidoResponse>(this.apiUrl, pedido);
  }

  /**
   * Obtener pedido por ID
   */
  getPedidoById(id: number): Observable<PedidoResponse> {
    return this.http.get<PedidoResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * Obtener pedidos por cliente
   */
  getPedidosPorCliente(clienteId: number): Observable<PedidoResponse[]> {
    return this.http.get<PedidoResponse[]>(`${this.apiUrl}/cliente/${clienteId}`);
  }

  /**
   * Obtener pedidos por estado
   */
  getPedidosPorEstado(estado: string): Observable<PedidoResponse[]> {
    return this.http.get<PedidoResponse[]>(`${this.apiUrl}/estado/${estado}`);
  }

  /**
   * Actualizar estado del pedido (solo admin)
   */
  actualizarEstadoPedido(id: number, estado: string): Observable<PedidoResponse> {
    const params = new HttpParams().set('estado', estado);
    return this.http.put<PedidoResponse>(`${this.apiUrl}/${id}/estado`, null, { params });
  }

  /**
   * Comparar estados entre MySQL y Redis
   */
  compararEstados(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/compare-tracking`);
  }

  /**
   * Sincronizar todos los pedidos
   */
  sincronizarTodos(): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/sync-all`, {});
  }

  /**
   * Reparar tracking de un pedido específico
   */
  repararTracking(id: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${id}/repair-tracking`, {});
  }

  /**
   * Sincronizar desde tracking service
   */
  sincronizarDesdeTracking(id: number): Observable<PedidoResponse> {
    return this.http.put<PedidoResponse>(`${this.apiUrl}/${id}/sync-from-tracking`, {});
  }
}
