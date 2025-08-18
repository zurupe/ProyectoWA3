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
}
