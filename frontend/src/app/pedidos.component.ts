import { Component, OnInit } from '@angular/core';
import { PedidoService } from './pedido.service';
import { AuthService } from './auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PedidoRequest, PedidoResponse, EstadoPedido } from './models/pedido.model';

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="dashboard-container">
      <h2 class="section-title">Gestión de Pedidos</h2>
      
      <!-- Formulario para crear pedido -->
      <div class="card" *ngIf="isAdmin || isCliente">
        <h3>Crear Nuevo Pedido</h3>
        <form (ngSubmit)="crearPedido()" #pedidoForm="ngForm" class="form">
          <div class="input-group">
            <label for="producto">Producto *</label>
            <input 
              id="producto" 
              name="producto" 
              type="text" 
              [(ngModel)]="nuevoPedido.producto" 
              required 
              placeholder="Nombre del producto"
            />
          </div>
          
          <div class="input-group">
            <label for="clienteId">ID Cliente *</label>
            <input 
              id="clienteId" 
              name="clienteId" 
              type="number" 
              [(ngModel)]="nuevoPedido.clienteId" 
              required 
              placeholder="ID del cliente"
              min="1"
            />
          </div>
          
          <div class="input-group">
            <label for="direccion">Dirección de Entrega *</label>
            <textarea 
              id="direccion" 
              name="direccion" 
              [(ngModel)]="nuevoPedido.direccion" 
              required 
              placeholder="Dirección completa de entrega"
              rows="3"
            ></textarea>
          </div>
          
          <button 
            type="submit" 
            class="btn-primary" 
            [disabled]="!pedidoForm.form.valid"
          >
            Crear Pedido
          </button>
        </form>
      </div>

      <!-- Filtros -->
      <div class="card">
        <h3>Filtros</h3>
        <div class="filters">
          <div class="input-group">
            <label for="filtroEstado">Filtrar por Estado:</label>
            <select id="filtroEstado" [(ngModel)]="filtroEstado" (ngModelChange)="aplicarFiltros()">
              <option value="">Todos los estados</option>
              <option value="PENDIENTE">Pendiente</option>
              <option value="PROCESANDO">Procesando</option>
              <option value="ENVIADO">Enviado</option>
              <option value="ENTREGADO">Entregado</option>
              <option value="CANCELADO">Cancelado</option>
            </select>
          </div>
          
          <div class="input-group" *ngIf="isAdmin">
            <label for="filtroCliente">Filtrar por Cliente:</label>
            <input 
              id="filtroCliente" 
              type="number" 
              [(ngModel)]="filtroClienteId" 
              (ngModelChange)="aplicarFiltros()"
              placeholder="ID del cliente"
              min="1"
            />
          </div>
        </div>
      </div>

      <!-- Lista de pedidos -->
      <div class="card">
        <h3>Pedidos 
          <span class="badge">{{ pedidosFiltrados.length }}</span>
        </h3>
        
        <div class="table-container">
          <table class="table" *ngIf="pedidosFiltrados.length > 0; else noPedidos">
            <thead>
              <tr>
                <th>ID</th>
                <th>Producto</th>
                <th>Cliente</th>
                <th>Dirección</th>
                <th>Estado</th>
                <th>Fecha Creación</th>
                <th>Última Actualización</th>
                <th *ngIf="isAdmin">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let pedido of pedidosFiltrados">
                <td>{{ pedido.id }}</td>
                <td>{{ pedido.producto }}</td>
                <td>{{ pedido.clienteId }}</td>
                <td>{{ pedido.direccion }}</td>
                <td>
                  <span [class]="'status-' + pedido.estado.toLowerCase()">
                    {{ pedido.estado }}
                  </span>
                </td>
                <td>{{ formatDate(pedido.fechaCreacion) }}</td>
                <td>{{ formatDate(pedido.fechaActualizacion) }}</td>
                <td *ngIf="isAdmin">
                  <select 
                    [(ngModel)]="pedido.estado" 
                    (ngModelChange)="actualizarEstado(pedido.id, $event)"
                    class="estado-select"
                  >
                    <option value="PENDIENTE">Pendiente</option>
                    <option value="PROCESANDO">Procesando</option>
                    <option value="ENVIADO">Enviado</option>
                    <option value="ENTREGADO">Entregado</option>
                    <option value="CANCELADO">Cancelado</option>
                  </select>
                </td>
              </tr>
            </tbody>
          </table>
          
          <ng-template #noPedidos>
            <div class="no-data">
              <p>No hay pedidos que mostrar</p>
              <p *ngIf="filtroEstado || filtroClienteId">Intenta cambiar los filtros</p>
            </div>
          </ng-template>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .filters {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
    }
    
    .badge {
      background: #007bff;
      color: white;
      padding: 0.25rem 0.5rem;
      border-radius: 12px;
      font-size: 0.875rem;
    }
    
    .status-pendiente { color: #ffc107; font-weight: bold; }
    .status-procesando { color: #17a2b8; font-weight: bold; }
    .status-enviado { color: #007bff; font-weight: bold; }
    .status-entregado { color: #28a745; font-weight: bold; }
    .status-cancelado { color: #dc3545; font-weight: bold; }
    
    .estado-select {
      padding: 0.25rem;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 0.875rem;
    }
    
    .table-container {
      overflow-x: auto;
    }
    
    .no-data {
      text-align: center;
      padding: 2rem;
      color: #666;
    }
  `]
})
export class PedidosComponent implements OnInit {
  pedidos: PedidoResponse[] = [];
  pedidosFiltrados: PedidoResponse[] = [];
  nuevoPedido: PedidoRequest = { 
    producto: '', 
    clienteId: 1, 
    direccion: '',
    estado: 'PENDIENTE'
  };
  
  filtroEstado: string = '';
  filtroClienteId: number | null = null;
  
  isAdmin = false;
  isCliente = false;

  constructor(
    private pedidoService: PedidoService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.verificarRoles();
    this.cargarPedidos();
  }

  verificarRoles() {
    const userInfo = this.authService.getUserInfo();
    this.isAdmin = userInfo?.role === 'ROLE_ADMIN';
    this.isCliente = userInfo?.role === 'ROLE_CLIENTE';
  }

  cargarPedidos() {
    this.pedidoService.getPedidos().subscribe({
      next: (data) => {
        this.pedidos = data;
        this.aplicarFiltros();
      },
      error: (error) => {
        console.error('Error al cargar pedidos:', error);
        alert('Error al cargar pedidos. Verifica tu conexión.');
      }
    });
  }

  crearPedido() {
    if (!this.nuevoPedido.producto || !this.nuevoPedido.clienteId || !this.nuevoPedido.direccion) {
      alert('Por favor completa todos los campos requeridos');
      return;
    }

    this.pedidoService.createPedido(this.nuevoPedido).subscribe({
      next: (pedido) => {
        alert('Pedido creado exitosamente');
        this.nuevoPedido = { 
          producto: '', 
          clienteId: 1, 
          direccion: '',
          estado: 'PENDIENTE'
        };
        this.cargarPedidos();
      },
      error: (error) => {
        console.error('Error al crear pedido:', error);
        alert('Error al crear pedido. Verifica los datos.');
      }
    });
  }

  actualizarEstado(id: number, nuevoEstado: string) {
    if (!this.isAdmin) {
      alert('No tienes permisos para actualizar estados');
      return;
    }

    this.pedidoService.actualizarEstadoPedido(id, nuevoEstado).subscribe({
      next: (pedidoActualizado) => {
        alert('Estado actualizado exitosamente');
        this.cargarPedidos();
      },
      error: (error) => {
        console.error('Error al actualizar estado:', error);
        alert('Error al actualizar estado del pedido');
        this.cargarPedidos(); // Recargar para revertir cambios en UI
      }
    });
  }

  aplicarFiltros() {
    this.pedidosFiltrados = this.pedidos.filter(pedido => {
      const matchEstado = !this.filtroEstado || pedido.estado === this.filtroEstado;
      const matchCliente = !this.filtroClienteId || pedido.clienteId === this.filtroClienteId;
      return matchEstado && matchCliente;
    });
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES') + ' ' + date.toLocaleTimeString('es-ES', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  }
}
