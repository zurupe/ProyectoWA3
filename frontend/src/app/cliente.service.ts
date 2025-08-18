import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cliente, ClienteRequest, ClienteResponse, EstadisticasClientes, EmailVerificacion } from './models/cliente.model';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private apiUrl = '/api/clientes';

  constructor(private http: HttpClient) {}

  getClientes(): Observable<ClienteResponse[]> {
    return this.http.get<ClienteResponse[]>(this.apiUrl);
  }

  getCliente(id: number): Observable<ClienteResponse> {
    return this.http.get<ClienteResponse>(`${this.apiUrl}/${id}`);
  }

  createCliente(cliente: ClienteRequest): Observable<ClienteResponse> {
    return this.http.post<ClienteResponse>(this.apiUrl, cliente);
  }

  updateCliente(id: number, cliente: ClienteRequest): Observable<ClienteResponse> {
    return this.http.put<ClienteResponse>(`${this.apiUrl}/${id}`, cliente);
  }

  deleteCliente(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  searchClientes(query: string): Observable<ClienteResponse[]> {
    return this.http.get<ClienteResponse[]>(`${this.apiUrl}/buscar?busqueda=${query}`);
  }

  getClientesActivos(): Observable<ClienteResponse[]> {
    return this.http.get<ClienteResponse[]>(`${this.apiUrl}/activos`);
  }

  getClientePorEmail(email: string): Observable<ClienteResponse> {
    return this.http.get<ClienteResponse>(`${this.apiUrl}/email/${email}`);
  }

  getClientesPorCiudad(ciudad: string): Observable<ClienteResponse[]> {
    return this.http.get<ClienteResponse[]>(`${this.apiUrl}/ciudad/${ciudad}`);
  }

  getClientesPorPais(pais: string): Observable<ClienteResponse[]> {
    return this.http.get<ClienteResponse[]>(`${this.apiUrl}/pais/${pais}`);
  }

  cambiarEstadoCliente(id: number, activo: boolean): Observable<ClienteResponse> {
    return this.http.patch<ClienteResponse>(`${this.apiUrl}/${id}/estado?activo=${activo}`, {});
  }

  verificarEmail(email: string): Observable<EmailVerificacion> {
    return this.http.get<EmailVerificacion>(`${this.apiUrl}/verificar-email/${email}`);
  }

  getEstadisticas(): Observable<EstadisticasClientes> {
    return this.http.get<EstadisticasClientes>(`${this.apiUrl}/estadisticas`);
  }
}