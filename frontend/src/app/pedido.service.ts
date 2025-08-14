import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PedidoService {
  private apiUrl = 'http://localhost:8083/api/pedidos';

  constructor(private http: HttpClient) {}

  getPedidos(): Observable<any> {
    return this.http.get(this.apiUrl);
  }

  createPedido(pedido: any): Observable<any> {
    return this.http.post(this.apiUrl, pedido);
  }

  getPedidoById(id: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }
}
