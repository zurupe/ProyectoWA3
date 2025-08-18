import { Component, OnInit } from '@angular/core';
import { ClienteService } from './cliente.service';
import { AuthService } from './auth.service';
import { CommonModule } from '@angular/common';
import { ClienteResponse, EstadisticasClientes } from './models/cliente.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard-container">
      <h2 class="section-title">Bienvenido al Sistema de Seguimiento de Pedidos</h2>
      <p>Utiliza el menú para navegar entre las funcionalidades:</p>
      <ul>
        <li>Crear y consultar pedidos</li>
        <li>Ver pedidos activos</li>
        <li>Consultar estado de pedidos</li>
        <li>Ver consistencia entre fuentes</li>
      </ul>
      <hr />
      
      <!-- Estadísticas para administradores -->
      <div *ngIf="isAdmin && estadisticas" class="stats-section">
        <h3>Estadísticas del Sistema</h3>
        <div class="stat-card">
          <h4>{{estadisticas.clientesActivos}}</h4>
          <p>Clientes Activos</p>
        </div>
      </div>

      <h3>Clientes Registrados</h3>
      <div *ngIf="loading" class="loading">Cargando clientes...</div>
      <div *ngIf="error" class="error">{{error}}</div>
      
      <table *ngIf="!loading && !error" class="table">
        <thead>
          <tr>
            <th>#</th>
            <th>Nombre Completo</th>
            <th>Email</th>
            <th>Ciudad</th>
            <th>Estado</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let cliente of clientes; let i = index">
            <td>{{ i + 1 }}</td>
            <td>{{ cliente.nombreCompleto }}</td>
            <td>{{ cliente.email }}</td>
            <td>{{ cliente.ciudad }}, {{ cliente.pais }}</td>
            <td>
              <span [class]="'status-' + (cliente.activo ? 'active' : 'inactive')">
                {{ cliente.activo ? 'Activo' : 'Inactivo' }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .stats-section {
      margin: 20px 0;
    }

    .stat-card {
      display: inline-block;
      background: linear-gradient(135deg, #007bff, #0056b3);
      color: white;
      padding: 20px;
      border-radius: 8px;
      text-align: center;
      margin-right: 20px;
    }

    .stat-card h4 {
      font-size: 2rem;
      margin: 0;
    }

    .stat-card p {
      margin: 5px 0 0 0;
    }

    .table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 10px;
    }

    .table th,
    .table td {
      padding: 12px;
      text-align: left;
      border-bottom: 1px solid #ddd;
    }

    .table th {
      background-color: #f8f9fa;
      font-weight: bold;
    }

    .status-active {
      color: #28a745;
      font-weight: bold;
    }

    .status-inactive {
      color: #dc3545;
      font-weight: bold;
    }

    .loading, .error {
      padding: 20px;
      text-align: center;
    }

    .error {
      color: #dc3545;
      background-color: #f8d7da;
      border-radius: 4px;
    }

    .loading {
      color: #666;
    }
  `]
})
export class DashboardComponent implements OnInit {
  clientes: ClienteResponse[] = [];
  estadisticas: EstadisticasClientes | null = null;
  loading: boolean = false;
  error: string = '';
  isAdmin: boolean = false;

  constructor(
    private clienteService: ClienteService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.isAdmin = this.authService.getUserRole() === 'ROLE_ADMIN';
    this.cargarClientes();
    if (this.isAdmin) {
      this.cargarEstadisticas();
    }
  }

  cargarClientes() {
    this.loading = true;
    this.error = '';
    
    this.clienteService.getClientesActivos().subscribe({
      next: (data) => {
        this.clientes = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar clientes';
        this.loading = false;
        console.error('Error:', err);
      }
    });
  }

  cargarEstadisticas() {
    this.clienteService.getEstadisticas().subscribe({
      next: (data) => {
        this.estadisticas = data;
      },
      error: (err) => {
        console.error('Error al cargar estadísticas:', err);
      }
    });
  }
}
