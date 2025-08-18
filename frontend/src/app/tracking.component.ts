import { Component, OnInit } from '@angular/core';
import { TrackingService, ConsistencyCheck } from './tracking.service';
import { AuthService } from './auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-tracking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="dashboard-container">
      <h2 class="section-title">Tracking de Pedidos</h2>
      
      <!-- Consulta de estado -->
      <div class="card">
        <h3>Consultar Estado de Pedido</h3>
        <form (ngSubmit)="consultarEstado()" #trackingForm="ngForm" class="form">
          <div class="input-group">
            <label for="pedidoId">ID del Pedido *</label>
            <input 
              id="pedidoId" 
              name="pedidoId" 
              type="number" 
              [(ngModel)]="pedidoId" 
              required 
              placeholder="Ingresa el ID del pedido"
              min="1"
            />
          </div>
          <button 
            type="submit" 
            class="btn-primary" 
            [disabled]="!trackingForm.form.valid || consultando"
          >
            {{ consultando ? 'Consultando...' : 'Consultar Estado' }}
          </button>
          
          <button 
            type="button" 
            class="btn-secondary" 
            (click)="verificarConsistencia()" 
            [disabled]="!pedidoId || consultando"
            style="margin-left: 10px;"
          >
            Verificar Consistencia
          </button>
        </form>
      </div>

      <!-- Resultado de la consulta -->
      <div class="card" *ngIf="estadoActual || error">
        <h3>Resultado de la Consulta</h3>
        
        <div *ngIf="estadoActual && !error" class="tracking-result">
          <div class="pedido-info">
            <strong>Pedido ID:</strong> {{ pedidoIdConsultado }}
          </div>
          <div class="estado-container">
            <span class="estado-badge" [class]="'estado-' + estadoActual.toLowerCase()">
              {{ estadoActual }}
            </span>
          </div>
          <div class="estado-descripcion">
            {{ getDescripcionEstado(estadoActual) }}
          </div>
        </div>
        
        <div *ngIf="error" class="error-message">
          <strong>Error:</strong> {{ error }}
        </div>
      </div>

      <!-- Resultado de verificación de consistencia -->
      <div class="card" *ngIf="consistencyCheck">
        <h3>Verificación de Consistencia</h3>
        
        <div class="consistency-result">
          <div class="pedido-info">
            <strong>Pedido ID:</strong> {{ consistencyCheck.pedidoId }}
          </div>
          
          <div class="sources-comparison">
            <div class="source-info">
              <h4>MySQL (Fuente Autoritativa)</h4>
              <span class="estado-badge" [class]="'estado-' + consistencyCheck.estadoMySQL.toLowerCase()">
                {{ consistencyCheck.estadoMySQL }}
              </span>
            </div>
            
            <div class="source-info">
              <h4>Redis (Consulta Rápida)</h4>
              <span class="estado-badge" [class]="'estado-' + consistencyCheck.estadoRedis.toLowerCase()">
                {{ consistencyCheck.estadoRedis }}
              </span>
            </div>
          </div>
          
          <div class="consistency-status">
            <div *ngIf="consistencyCheck.isConsistent" class="consistency-ok">
              <strong>CONSISTENTE:</strong> Los datos están sincronizados correctamente
            </div>
            <div *ngIf="!consistencyCheck.isConsistent" class="consistency-warning">
              <strong>DESINCRONIZADO:</strong> Los estados no coinciden entre MySQL y Redis
              <div class="sync-actions" *ngIf="isAdmin">
                <button class="btn-warning" (click)="sincronizarDesdeMySQL()">
                  Sincronizar desde MySQL
                </button>
              </div>
            </div>
          </div>
          
          <div class="last-check">
            <small>Última verificación: {{ formatDate(consistencyCheck.lastSync || '') }}</small>
          </div>
        </div>
      </div>

      <!-- Panel de administración -->
      <div class="card" *ngIf="isAdmin">
        <h3>Panel de Administración - Actualizar Estado</h3>
        <form (ngSubmit)="actualizarEstado()" #adminForm="ngForm" class="form">
          <div class="input-group">
            <label for="adminPedidoId">ID del Pedido *</label>
            <input 
              id="adminPedidoId" 
              name="adminPedidoId" 
              type="number" 
              [(ngModel)]="adminPedidoId" 
              required 
              placeholder="ID del pedido a actualizar"
              min="1"
            />
          </div>
          
          <div class="input-group">
            <label for="nuevoEstado">Nuevo Estado *</label>
            <select 
              id="nuevoEstado" 
              name="nuevoEstado" 
              [(ngModel)]="nuevoEstado" 
              required
            >
              <option value="">Selecciona un estado</option>
              <option value="PENDIENTE">Pendiente</option>
              <option value="EN_PROCESO">En Proceso</option>
              <option value="ENVIADO">Enviado</option>
              <option value="ENTREGADO">Entregado</option>
              <option value="CANCELADO">Cancelado</option>
            </select>
          </div>
          
          <button 
            type="submit" 
            class="btn-secondary" 
            [disabled]="!adminForm.form.valid || actualizando"
          >
            {{ actualizando ? 'Actualizando...' : 'Actualizar Estado' }}
          </button>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .tracking-result {
      border: 1px solid #ddd;
      border-radius: 8px;
      padding: 1.5rem;
      background-color: #f9f9f9;
    }
    
    .pedido-info {
      font-size: 1.1rem;
      margin-bottom: 1rem;
    }
    
    .estado-container {
      margin: 1rem 0;
      text-align: center;
    }
    
    .estado-badge {
      display: inline-block;
      padding: 0.75rem 1.5rem;
      border-radius: 25px;
      font-weight: bold;
      font-size: 1.2rem;
      text-transform: uppercase;
      letter-spacing: 1px;
    }
    
    .estado-pendiente { background-color: #fff3cd; color: #856404; border: 2px solid #ffc107; }
    .estado-en_proceso { background-color: #d1ecf1; color: #0c5460; border: 2px solid #17a2b8; }
    .estado-enviado { background-color: #d4edda; color: #155724; border: 2px solid #28a745; }
    .estado-entregado { background-color: #d1e7dd; color: #0f5132; border: 2px solid #198754; }
    .estado-cancelado { background-color: #f8d7da; color: #721c24; border: 2px solid #dc3545; }
    
    .estado-descripcion {
      text-align: center;
      font-style: italic;
      color: #666;
      margin-top: 1rem;
    }
    
    .error-message {
      color: #dc3545;
      background-color: #f8d7da;
      border: 1px solid #f5c6cb;
      border-radius: 4px;
      padding: 1rem;
    }
    
    .btn-secondary {
      background-color: #6c757d;
      color: white;
      border: none;
      padding: 0.75rem 1.5rem;
      border-radius: 4px;
      cursor: pointer;
      font-size: 1rem;
    }
    
    .btn-secondary:hover {
      background-color: #5a6268;
    }
    
    .btn-secondary:disabled {
      background-color: #6c757d;
      opacity: 0.6;
      cursor: not-allowed;
    }
    
    .consistency-result {
      border: 1px solid #ddd;
      border-radius: 8px;
      padding: 1.5rem;
      background-color: #f9f9f9;
    }
    
    .sources-comparison {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
      margin: 1rem 0;
    }
    
    .source-info {
      text-align: center;
      padding: 1rem;
      border: 1px solid #ddd;
      border-radius: 6px;
      background-color: white;
    }
    
    .source-info h4 {
      margin: 0 0 0.5rem 0;
      font-size: 1rem;
    }
    
    .consistency-ok {
      background-color: #d4edda;
      color: #155724;
      padding: 1rem;
      border-radius: 6px;
      text-align: center;
      margin: 1rem 0;
    }
    
    .consistency-warning {
      background-color: #fff3cd;
      color: #856404;
      padding: 1rem;
      border-radius: 6px;
      text-align: center;
      margin: 1rem 0;
    }
    
    .status-icon {
      font-size: 1.5rem;
      margin-right: 0.5rem;
    }
    
    .sync-actions {
      margin-top: 1rem;
    }
    
    .btn-warning {
      background-color: #ffc107;
      color: #212529;
      border: none;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      cursor: pointer;
      font-weight: 500;
    }
    
    .btn-warning:hover {
      background-color: #e0a800;
    }
    
    .last-check {
      text-align: center;
      color: #666;
      font-style: italic;
      margin-top: 1rem;
    }
  `]
})
export class TrackingComponent implements OnInit {
  pedidoId: number | null = null;
  estadoActual: string | null = null;
  pedidoIdConsultado: number | null = null;
  error: string | null = null;
  consultando = false;
  
  // Verificación de consistencia
  consistencyCheck: ConsistencyCheck | null = null;
  
  // Panel de administración
  adminPedidoId: number | null = null;
  nuevoEstado: string = '';
  actualizando = false;
  
  isAdmin = false;

  constructor(
    private trackingService: TrackingService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.verificarRoles();
  }

  verificarRoles() {
    const userInfo = this.authService.getUserInfo();
    this.isAdmin = userInfo?.role === 'ROLE_ADMIN';
  }

  consultarEstado() {
    if (!this.pedidoId) {
      this.error = 'Por favor ingresa un ID de pedido válido';
      return;
    }

    this.consultando = true;
    this.error = null;
    this.estadoActual = null;

    this.trackingService.getEstadoPedido(this.pedidoId).subscribe({
      next: (estado) => {
        this.estadoActual = estado;
        this.pedidoIdConsultado = this.pedidoId;
        this.consultando = false;
      },
      error: (error) => {
        console.error('Error al consultar estado:', error);
        if (error.status === 404) {
          this.error = 'No se encontró información de tracking para este pedido';
        } else if (error.status === 401) {
          this.error = 'No tienes permisos para consultar este pedido';
        } else {
          this.error = 'Error al consultar el estado del pedido';
        }
        this.consultando = false;
      }
    });
  }

  actualizarEstado() {
    if (!this.adminPedidoId || !this.nuevoEstado) {
      alert('Por favor completa todos los campos');
      return;
    }

    this.actualizando = true;

    this.trackingService.actualizarEstadoPedido(this.adminPedidoId, this.nuevoEstado).subscribe({
      next: () => {
        alert('Estado actualizado exitosamente');
        this.adminPedidoId = null;
        this.nuevoEstado = '';
        this.actualizando = false;
        
        // Si estamos consultando el mismo pedido, actualizar la vista
        if (this.pedidoIdConsultado === this.adminPedidoId) {
          this.pedidoId = this.adminPedidoId;
          this.consultarEstado();
        }
      },
      error: (error) => {
        console.error('Error al actualizar estado:', error);
        alert('Error al actualizar el estado del pedido');
        this.actualizando = false;
      }
    });
  }

  verificarConsistencia() {
    if (!this.pedidoId) {
      this.error = 'Por favor ingresa un ID de pedido válido';
      return;
    }

    this.consultando = true;
    this.error = null;
    this.consistencyCheck = null;

    this.trackingService.verificarConsistencia(this.pedidoId).subscribe({
      next: (check) => {
        this.consistencyCheck = check;
        this.consultando = false;
      },
      error: (error) => {
        console.error('Error al verificar consistencia:', error);
        this.error = 'Error al verificar consistencia entre fuentes de datos';
        this.consultando = false;
      }
    });
  }

  sincronizarDesdeMySQL() {
    if (!this.consistencyCheck) return;

    const pedidoId = this.consistencyCheck.pedidoId;
    
    this.trackingService.sincronizarEstado(pedidoId).subscribe({
      next: () => {
        alert('Sincronización completada desde MySQL');
        // Verificar nuevamente para mostrar el estado actualizado
        this.verificarConsistencia();
      },
      error: (error) => {
        console.error('Error al sincronizar:', error);
        alert('Error al sincronizar el estado desde MySQL');
      }
    });
  }

  formatDate(dateString: string): string {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES') + ' ' + date.toLocaleTimeString('es-ES', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  }

  getDescripcionEstado(estado: string): string {
    const descripciones: { [key: string]: string } = {
      'PENDIENTE': 'Tu pedido ha sido recibido y está siendo procesado',
      'EN_PROCESO': 'Tu pedido está siendo preparado para envío',
      'ENVIADO': 'Tu pedido ha sido enviado y está en camino',
      'ENTREGADO': 'Tu pedido ha sido entregado exitosamente',
      'CANCELADO': 'Este pedido ha sido cancelado'
    };
    return descripciones[estado] || 'Estado del pedido';
  }
}
