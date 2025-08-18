import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClienteService } from './cliente.service';
import { AuthService } from './auth.service';
import { ClienteResponse, ClienteRequest, EstadisticasClientes } from './models/cliente.model';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="dashboard-container">
      <h2 class="section-title">Gestión de Clientes</h2>
      
      <!-- Estadísticas para administradores -->
      <div *ngIf="isAdmin && estadisticas" class="stats-section">
        <div class="stat-card">
          <h3>{{estadisticas.clientesActivos}}</h3>
          <p>Clientes Activos</p>
        </div>
      </div>

      <!-- Sección de búsqueda y filtros -->
      <div class="search-section">
        <div class="search-box">
          <input 
            type="text" 
            [(ngModel)]="searchTerm" 
            (keyup.enter)="buscarClientes()"
            placeholder="Buscar por nombre, apellido o email..."
            class="search-input">
          <button (click)="buscarClientes()" class="btn-primary">Buscar</button>
        </div>
        
        <div class="filter-buttons">
          <button (click)="cargarTodosClientes()" class="btn-secondary">Todos</button>
          <button (click)="cargarClientesActivos()" class="btn-secondary">Solo Activos</button>
        </div>

        <div class="location-filter">
          <select [(ngModel)]="ciudadSeleccionada" (change)="filtrarPorCiudad()" class="select-input">
            <option value="">Todas las ciudades</option>
            <option value="Quito">Quito</option>
            <option value="Guayaquil">Guayaquil</option>
            <option value="Cuenca">Cuenca</option>
            <option value="Madrid">Madrid</option>
          </select>
        </div>
      </div>

      <!-- Formulario para crear cliente (solo usuarios autenticados) -->
      <div *ngIf="mostrarFormulario" class="form-section">
        <h3>{{editingCliente ? 'Editar Cliente' : 'Nuevo Cliente'}}</h3>
        <form (ngSubmit)="guardarCliente()" #clienteForm="ngForm" class="cliente-form">
          <div class="form-row">
            <div class="input-group">
              <label for="nombre">Nombre *</label>
              <input id="nombre" name="nombre" type="text" [(ngModel)]="nuevoCliente.nombre" required>
            </div>
            <div class="input-group">
              <label for="apellido">Apellido *</label>
              <input id="apellido" name="apellido" type="text" [(ngModel)]="nuevoCliente.apellido" required>
            </div>
          </div>
          
          <div class="form-row">
            <div class="input-group">
              <label for="email">Email *</label>
              <input id="email" name="email" type="email" [(ngModel)]="nuevoCliente.email" required>
            </div>
            <div class="input-group">
              <label for="telefono">Teléfono *</label>
              <input id="telefono" name="telefono" type="tel" [(ngModel)]="nuevoCliente.telefono" required>
            </div>
          </div>

          <div class="input-group">
            <label for="direccion">Dirección *</label>
            <input id="direccion" name="direccion" type="text" [(ngModel)]="nuevoCliente.direccion" required>
          </div>

          <div class="form-row">
            <div class="input-group">
              <label for="ciudad">Ciudad *</label>
              <input id="ciudad" name="ciudad" type="text" [(ngModel)]="nuevoCliente.ciudad" required>
            </div>
            <div class="input-group">
              <label for="pais">País *</label>
              <input id="pais" name="pais" type="text" [(ngModel)]="nuevoCliente.pais" required>
            </div>
          </div>

          <div class="input-group">
            <label for="codigoPostal">Código Postal</label>
            <input id="codigoPostal" name="codigoPostal" type="text" [(ngModel)]="nuevoCliente.codigoPostal">
          </div>

          <div class="form-actions">
            <button type="submit" [disabled]="!clienteForm.form.valid" class="btn-primary">
              {{editingCliente ? 'Actualizar' : 'Crear'}} Cliente
            </button>
            <button type="button" (click)="cancelarFormulario()" class="btn-secondary">Cancelar</button>
          </div>
        </form>
      </div>

      <!-- Botón para mostrar formulario -->
      <div class="action-buttons">
        <button (click)="mostrarFormulario = !mostrarFormulario" class="btn-primary">
          {{mostrarFormulario ? 'Ocultar' : 'Nuevo'}} Cliente
        </button>
      </div>

      <!-- Lista de clientes -->
      <div class="clientes-grid">
        <div *ngFor="let cliente of clientes" class="cliente-card">
          <div class="cliente-header">
            <h4>{{cliente.nombreCompleto}}</h4>
            <span class="status-badge" [class]="'status-' + (cliente.activo ? 'active' : 'inactive')">
              {{cliente.activo ? 'Activo' : 'Inactivo'}}
            </span>
          </div>
          
          <div class="cliente-details">
            <p><strong>Email:</strong> {{cliente.email}}</p>
            <p><strong>Teléfono:</strong> {{cliente.telefono}}</p>
            <p><strong>Dirección:</strong> {{cliente.direccion}}</p>
            <p><strong>Ubicación:</strong> {{cliente.ciudad}}, {{cliente.pais}}</p>
            <p *ngIf="cliente.codigoPostal"><strong>CP:</strong> {{cliente.codigoPostal}}</p>
            <p class="fecha-info">
              <small>Creado: {{cliente.fechaCreacion | date:'short'}}</small>
            </p>
          </div>

          <!-- Acciones (solo para administradores) -->
          <div *ngIf="isAdmin" class="cliente-actions">
            <button (click)="editarCliente(cliente)" class="btn-edit">Editar</button>
            <button (click)="cambiarEstado(cliente)" 
                    [class]="cliente.activo ? 'btn-warning' : 'btn-success'">
              {{cliente.activo ? 'Desactivar' : 'Activar'}}
            </button>
            <button (click)="eliminarCliente(cliente)" class="btn-danger">Eliminar</button>
          </div>
        </div>
      </div>

      <!-- Estados de carga y error -->
      <div *ngIf="loading" class="loading-message">
        <p>Cargando clientes...</p>
      </div>

      <div *ngIf="error" class="error-message">
        <p>{{error}}</p>
        <button (click)="cargarTodosClientes()" class="btn-secondary">Reintentar</button>
      </div>

      <div *ngIf="!loading && clientes.length === 0 && !error" class="empty-message">
        <p>No se encontraron clientes.</p>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .section-title {
      color: #333;
      margin-bottom: 20px;
      border-bottom: 2px solid #007bff;
      padding-bottom: 10px;
    }

    .stats-section {
      margin-bottom: 30px;
    }

    .stat-card {
      background: linear-gradient(135deg, #007bff, #0056b3);
      color: white;
      padding: 20px;
      border-radius: 8px;
      text-align: center;
      max-width: 200px;
    }

    .stat-card h3 {
      font-size: 2rem;
      margin: 0;
    }

    .search-section {
      background: #f8f9fa;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 20px;
    }

    .search-box {
      display: flex;
      gap: 10px;
      margin-bottom: 15px;
    }

    .search-input {
      flex: 1;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 14px;
    }

    .filter-buttons {
      display: flex;
      gap: 10px;
      margin-bottom: 15px;
    }

    .location-filter .select-input {
      padding: 8px 12px;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 14px;
    }

    .form-section {
      background: white;
      border: 1px solid #ddd;
      border-radius: 8px;
      padding: 20px;
      margin-bottom: 20px;
    }

    .cliente-form .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 15px;
      margin-bottom: 15px;
    }

    .input-group {
      display: flex;
      flex-direction: column;
    }

    .input-group label {
      font-weight: bold;
      margin-bottom: 5px;
      color: #333;
    }

    .input-group input {
      padding: 8px 12px;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 14px;
    }

    .form-actions {
      display: flex;
      gap: 10px;
      justify-content: flex-end;
      margin-top: 20px;
    }

    .action-buttons {
      margin-bottom: 20px;
    }

    .clientes-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
      gap: 20px;
    }

    .cliente-card {
      background: white;
      border: 1px solid #ddd;
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      transition: transform 0.2s;
    }

    .cliente-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(0,0,0,0.15);
    }

    .cliente-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;
      border-bottom: 1px solid #eee;
      padding-bottom: 10px;
    }

    .cliente-header h4 {
      margin: 0;
      color: #333;
    }

    .status-badge {
      padding: 4px 8px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: bold;
    }

    .status-active {
      background: #d4edda;
      color: #155724;
    }

    .status-inactive {
      background: #f8d7da;
      color: #721c24;
    }

    .cliente-details p {
      margin: 8px 0;
      color: #666;
    }

    .fecha-info {
      margin-top: 15px;
      padding-top: 10px;
      border-top: 1px solid #eee;
    }

    .cliente-actions {
      display: flex;
      gap: 8px;
      margin-top: 15px;
      flex-wrap: wrap;
    }

    .btn-primary {
      background: #007bff;
      color: white;
      border: none;
      padding: 10px 16px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
    }

    .btn-secondary {
      background: #6c757d;
      color: white;
      border: none;
      padding: 8px 12px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
    }

    .btn-edit {
      background: #ffc107;
      color: #212529;
      border: none;
      padding: 6px 12px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 12px;
    }

    .btn-success {
      background: #28a745;
      color: white;
      border: none;
      padding: 6px 12px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 12px;
    }

    .btn-warning {
      background: #fd7e14;
      color: white;
      border: none;
      padding: 6px 12px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 12px;
    }

    .btn-danger {
      background: #dc3545;
      color: white;
      border: none;
      padding: 6px 12px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 12px;
    }

    button:hover {
      opacity: 0.9;
    }

    button:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .loading-message, .error-message, .empty-message {
      text-align: center;
      padding: 40px;
      color: #666;
    }

    .error-message {
      background: #f8d7da;
      color: #721c24;
      border-radius: 4px;
    }

    @media (max-width: 768px) {
      .search-box {
        flex-direction: column;
      }

      .filter-buttons {
        flex-direction: column;
      }

      .cliente-form .form-row {
        grid-template-columns: 1fr;
      }

      .clientes-grid {
        grid-template-columns: 1fr;
      }

      .cliente-actions {
        justify-content: center;
      }
    }
  `]
})
export class ClientesComponent implements OnInit {
  clientes: ClienteResponse[] = [];
  estadisticas: EstadisticasClientes | null = null;
  nuevoCliente: ClienteRequest = this.initNuevoCliente();
  searchTerm: string = '';
  ciudadSeleccionada: string = '';
  mostrarFormulario: boolean = false;
  editingCliente: ClienteResponse | null = null;
  loading: boolean = false;
  error: string = '';
  isAdmin: boolean = false;

  constructor(
    private clienteService: ClienteService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.isAdmin = this.authService.getUserRole() === 'ROLE_ADMIN';
    this.cargarTodosClientes();
    if (this.isAdmin) {
      this.cargarEstadisticas();
    }
  }

  initNuevoCliente(): ClienteRequest {
    return {
      nombre: '',
      apellido: '',
      email: '',
      telefono: '',
      direccion: '',
      ciudad: '',
      pais: 'Ecuador',
      codigoPostal: ''
    };
  }

  cargarTodosClientes() {
    this.loading = true;
    this.error = '';
    
    this.clienteService.getClientes().subscribe({
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

  cargarClientesActivos() {
    this.loading = true;
    this.error = '';
    
    this.clienteService.getClientesActivos().subscribe({
      next: (data) => {
        this.clientes = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al cargar clientes activos';
        this.loading = false;
        console.error('Error:', err);
      }
    });
  }

  buscarClientes() {
    if (!this.searchTerm.trim()) {
      this.cargarTodosClientes();
      return;
    }

    this.loading = true;
    this.error = '';
    
    this.clienteService.searchClientes(this.searchTerm).subscribe({
      next: (data) => {
        this.clientes = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error en la búsqueda';
        this.loading = false;
        console.error('Error:', err);
      }
    });
  }

  filtrarPorCiudad() {
    if (!this.ciudadSeleccionada) {
      this.cargarTodosClientes();
      return;
    }

    this.loading = true;
    this.error = '';
    
    this.clienteService.getClientesPorCiudad(this.ciudadSeleccionada).subscribe({
      next: (data) => {
        this.clientes = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al filtrar por ciudad';
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

  guardarCliente() {
    if (this.editingCliente) {
      this.actualizarCliente();
    } else {
      this.crearCliente();
    }
  }

  crearCliente() {
    this.clienteService.createCliente(this.nuevoCliente).subscribe({
      next: (cliente) => {
        this.clientes.unshift(cliente);
        this.cancelarFormulario();
        if (this.isAdmin) {
          this.cargarEstadisticas();
        }
      },
      error: (err) => {
        this.error = 'Error al crear cliente';
        console.error('Error:', err);
      }
    });
  }

  actualizarCliente() {
    if (!this.editingCliente) return;
    
    this.clienteService.updateCliente(this.editingCliente.id, this.nuevoCliente).subscribe({
      next: (clienteActualizado) => {
        const index = this.clientes.findIndex(c => c.id === this.editingCliente!.id);
        if (index !== -1) {
          this.clientes[index] = clienteActualizado;
        }
        this.cancelarFormulario();
      },
      error: (err) => {
        this.error = 'Error al actualizar cliente';
        console.error('Error:', err);
      }
    });
  }

  editarCliente(cliente: ClienteResponse) {
    this.editingCliente = cliente;
    this.nuevoCliente = {
      nombre: cliente.nombre,
      apellido: cliente.apellido,
      email: cliente.email,
      telefono: cliente.telefono,
      direccion: cliente.direccion,
      ciudad: cliente.ciudad,
      pais: cliente.pais,
      codigoPostal: cliente.codigoPostal || ''
    };
    this.mostrarFormulario = true;
  }

  cambiarEstado(cliente: ClienteResponse) {
    if (!this.isAdmin) return;

    this.clienteService.cambiarEstadoCliente(cliente.id, !cliente.activo).subscribe({
      next: (clienteActualizado) => {
        const index = this.clientes.findIndex(c => c.id === cliente.id);
        if (index !== -1) {
          this.clientes[index] = clienteActualizado;
        }
        if (this.isAdmin) {
          this.cargarEstadisticas();
        }
      },
      error: (err) => {
        this.error = 'Error al cambiar estado del cliente';
        console.error('Error:', err);
      }
    });
  }

  eliminarCliente(cliente: ClienteResponse) {
    if (!this.isAdmin) return;
    
    if (confirm(`¿Estás seguro de eliminar a ${cliente.nombreCompleto}?`)) {
      this.clienteService.deleteCliente(cliente.id).subscribe({
        next: () => {
          this.clientes = this.clientes.filter(c => c.id !== cliente.id);
          if (this.isAdmin) {
            this.cargarEstadisticas();
          }
        },
        error: (err) => {
          this.error = 'Error al eliminar cliente';
          console.error('Error:', err);
        }
      });
    }
  }

  cancelarFormulario() {
    this.mostrarFormulario = false;
    this.editingCliente = null;
    this.nuevoCliente = this.initNuevoCliente();
    this.error = '';
  }
}