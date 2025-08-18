import { Component, OnInit } from '@angular/core';
import { PedidoService } from './pedido.service';
import { TrackingService } from './tracking.service';
import { AuthService } from './auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin-sync',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="dashboard-container" *ngIf="isAdmin">
      <h2 class="section-title">Panel de Sincronización Avanzada</h2>
      
      <!-- Comparación general de estados -->
      <div class="card">
        <h3>Estado General del Sistema</h3>
        <div class="system-status">
          <button 
            type="button" 
            class="btn-primary" 
            (click)="compararEstados()" 
            [disabled]="cargando"
          >
            {{ cargando ? 'Verificando...' : 'Verificar Consistencia General' }}
          </button>
          
          <div *ngIf="estadoSistema" class="status-result">
            <div class="status-header">
              <h4>Resultado de Verificación</h4>
              <div class="status-badge" [class]="estadoSistema.estado === 'CONSISTENTE' ? 'status-ok' : 'status-warning'">
                {{ estadoSistema.estado }}
              </div>
            </div>
            
            <div class="status-details">
              <div class="stat-item">
                <strong>Total Pedidos:</strong> {{ estadoSistema.totalPedidos }}
              </div>
              <div class="stat-item">
                <strong>Discrepancias:</strong> {{ estadoSistema.discrepanciasEncontradas }}
              </div>
              <div class="stat-item">
                <strong>Consistencia:</strong> {{ estadoSistema.consistenciaPercentage?.toFixed(1) }}%
              </div>
            </div>
            
            <div *ngIf="estadoSistema.discrepancias && estadoSistema.discrepancias.length > 0" class="discrepancias">
              <h5>Discrepancias Encontradas:</h5>
              <div *ngFor="let disc of estadoSistema.discrepancias" class="discrepancia-item">
                <strong>Pedido {{ disc.pedidoId }}:</strong> 
                MySQL: <span class="estado-mysql">{{ disc.estadoMySQL }}</span> → 
                Redis: <span class="estado-redis">{{ disc.estadoRedis }}</span>
                <button 
                  class="btn-small" 
                  (click)="repararPedido(disc.pedidoId)"
                  [disabled]="reparando"
                >
                  Reparar
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Sincronización masiva -->
      <div class="card">
        <h3>Sincronización Masiva</h3>
        <div class="sync-actions">
          <button 
            type="button" 
            class="btn-warning" 
            (click)="sincronizarTodos()" 
            [disabled]="sincronizando"
          >
            {{ sincronizando ? 'Sincronizando...' : 'Sincronizar Todos los Pedidos' }}
          </button>
          
          <div class="warning-text">
            <small>⚠️ Esta acción sincronizará todos los pedidos desde MySQL hacia Redis</small>
          </div>
        </div>
      </div>

      <!-- Herramientas individuales -->
      <div class="card">
        <h3>Herramientas de Pedido Individual</h3>
        <form (ngSubmit)="procesarPedidoIndividual()" #individualForm="ngForm" class="form">
          <div class="input-group">
            <label for="pedidoId">ID del Pedido *</label>
            <input 
              id="pedidoId" 
              name="pedidoId" 
              type="number" 
              [(ngModel)]="pedidoIdIndividual" 
              required 
              placeholder="ID del pedido"
              min="1"
            />
          </div>
          
          <div class="button-group">
            <button 
              type="button" 
              class="btn-secondary" 
              (click)="repararPedido(pedidoIdIndividual)" 
              [disabled]="!pedidoIdIndividual || reparando"
            >
              Reparar Tracking
            </button>
            
            <button 
              type="button" 
              class="btn-info" 
              (click)="sincronizarDesdeTracking(pedidoIdIndividual)" 
              [disabled]="!pedidoIdIndividual || sincronizando"
            >
              Sync desde Tracking
            </button>
            
            <button 
              type="button" 
              class="btn-success" 
              (click)="crearTrackingNuevo(pedidoIdIndividual)" 
              [disabled]="!pedidoIdIndividual || creando"
            >
              Crear Tracking
            </button>
          </div>
        </form>
      </div>

      <!-- Log de operaciones -->
      <div class="card" *ngIf="operacionesLog.length > 0">
        <h3>Log de Operaciones</h3>
        <div class="log-container">
          <div *ngFor="let log of operacionesLog; let i = index" 
               class="log-item" 
               [class]="log.tipo">
            <span class="log-time">{{ formatTime(log.timestamp) }}</span>
            <span class="log-message">{{ log.mensaje }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <div *ngIf="!isAdmin" class="no-access">
      <h2>Acceso Denegado</h2>
      <p>Esta sección requiere permisos de administrador.</p>
    </div>
  `,
  styles: [`
    .system-status {
      text-align: center;
      padding: 2rem;
    }
    
    .status-result {
      margin-top: 2rem;
      border: 1px solid #ddd;
      border-radius: 8px;
      padding: 1.5rem;
      background-color: #f9f9f9;
    }
    
    .status-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
    }
    
    .status-badge {
      padding: 0.5rem 1rem;
      border-radius: 20px;
      font-weight: bold;
      text-transform: uppercase;
    }
    
    .status-ok {
      background-color: #d4edda;
      color: #155724;
    }
    
    .status-warning {
      background-color: #fff3cd;
      color: #856404;
    }
    
    .status-details {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
      margin: 1rem 0;
    }
    
    .stat-item {
      background-color: white;
      padding: 1rem;
      border-radius: 6px;
      border: 1px solid #ddd;
      text-align: center;
    }
    
    .discrepancias {
      margin-top: 1.5rem;
      border-top: 1px solid #ddd;
      padding-top: 1rem;
    }
    
    .discrepancia-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem;
      border: 1px solid #ffc107;
      border-radius: 4px;
      margin: 0.5rem 0;
      background-color: #fff3cd;
    }
    
    .estado-mysql {
      color: #0066cc;
      font-weight: bold;
    }
    
    .estado-redis {
      color: #dc3545;
      font-weight: bold;
    }
    
    .sync-actions {
      text-align: center;
      padding: 2rem;
    }
    
    .warning-text {
      margin-top: 1rem;
      color: #856404;
    }
    
    .button-group {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
      justify-content: center;
    }
    
    .btn-small {
      padding: 0.25rem 0.5rem;
      font-size: 0.875rem;
      background-color: #17a2b8;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }
    
    .btn-small:hover {
      background-color: #138496;
    }
    
    .btn-small:disabled {
      background-color: #6c757d;
      cursor: not-allowed;
    }
    
    .btn-info {
      background-color: #17a2b8;
      color: white;
    }
    
    .btn-info:hover {
      background-color: #138496;
    }
    
    .btn-success {
      background-color: #28a745;
      color: white;
    }
    
    .btn-success:hover {
      background-color: #218838;
    }
    
    .btn-warning {
      background-color: #ffc107;
      color: #212529;
    }
    
    .btn-warning:hover {
      background-color: #e0a800;
    }
    
    .log-container {
      max-height: 300px;
      overflow-y: auto;
      background-color: #f8f9fa;
      border: 1px solid #ddd;
      border-radius: 4px;
      padding: 1rem;
    }
    
    .log-item {
      padding: 0.5rem;
      margin: 0.25rem 0;
      border-radius: 4px;
      display: flex;
      gap: 1rem;
    }
    
    .log-item.exito {
      background-color: #d4edda;
      border-left: 4px solid #28a745;
    }
    
    .log-item.error {
      background-color: #f8d7da;
      border-left: 4px solid #dc3545;
    }
    
    .log-item.info {
      background-color: #d1ecf1;
      border-left: 4px solid #17a2b8;
    }
    
    .log-time {
      font-size: 0.875rem;
      color: #666;
      white-space: nowrap;
    }
    
    .log-message {
      flex: 1;
    }
    
    .no-access {
      text-align: center;
      padding: 3rem;
      color: #dc3545;
    }
  `]
})
export class AdminSyncComponent implements OnInit {
  isAdmin = false;
  cargando = false;
  sincronizando = false;
  reparando = false;
  creando = false;
  
  estadoSistema: any = null;
  pedidoIdIndividual: number | null = null;
  
  operacionesLog: Array<{
    timestamp: Date;
    tipo: 'exito' | 'error' | 'info';
    mensaje: string;
  }> = [];

  constructor(
    private pedidoService: PedidoService,
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

  compararEstados() {
    this.cargando = true;
    this.pedidoService.compararEstados().subscribe({
      next: (estado) => {
        this.estadoSistema = estado;
        this.agregarLog('exito', `Verificación completada: ${estado.discrepanciasEncontradas} discrepancias encontradas`);
        this.cargando = false;
      },
      error: (error) => {
        console.error('Error al comparar estados:', error);
        this.agregarLog('error', 'Error al verificar consistencia del sistema');
        this.cargando = false;
      }
    });
  }

  sincronizarTodos() {
    if (!confirm('¿Estás seguro de que quieres sincronizar todos los pedidos? Esta operación puede tomar tiempo.')) {
      return;
    }

    this.sincronizando = true;
    this.pedidoService.sincronizarTodos().subscribe({
      next: (resultado) => {
        this.agregarLog('exito', 'Sincronización masiva completada exitosamente');
        this.sincronizando = false;
        // Recargar estado del sistema
        this.compararEstados();
      },
      error: (error) => {
        console.error('Error en sincronización masiva:', error);
        this.agregarLog('error', 'Error durante la sincronización masiva');
        this.sincronizando = false;
      }
    });
  }

  repararPedido(pedidoId: number | null) {
    if (!pedidoId) return;

    this.reparando = true;
    this.pedidoService.repararTracking(pedidoId).subscribe({
      next: (resultado) => {
        this.agregarLog('exito', `Tracking reparado para pedido ${pedidoId}`);
        this.reparando = false;
        // Actualizar estado si está visible
        if (this.estadoSistema) {
          this.compararEstados();
        }
      },
      error: (error) => {
        console.error('Error al reparar tracking:', error);
        this.agregarLog('error', `Error al reparar tracking del pedido ${pedidoId}`);
        this.reparando = false;
      }
    });
  }

  sincronizarDesdeTracking(pedidoId: number | null) {
    if (!pedidoId) return;

    this.sincronizando = true;
    this.pedidoService.sincronizarDesdeTracking(pedidoId).subscribe({
      next: (resultado) => {
        this.agregarLog('exito', `Pedido ${pedidoId} sincronizado desde tracking`);
        this.sincronizando = false;
      },
      error: (error) => {
        console.error('Error al sincronizar desde tracking:', error);
        this.agregarLog('error', `Error al sincronizar pedido ${pedidoId} desde tracking`);
        this.sincronizando = false;
      }
    });
  }

  crearTrackingNuevo(pedidoId: number | null) {
    if (!pedidoId) return;

    this.creando = true;
    this.trackingService.crearTrackingDesdePedido(pedidoId, 'PENDIENTE').subscribe({
      next: (resultado) => {
        this.agregarLog('exito', `Tracking creado para pedido ${pedidoId}`);
        this.creando = false;
      },
      error: (error) => {
        console.error('Error al crear tracking:', error);
        this.agregarLog('error', `Error al crear tracking para pedido ${pedidoId}`);
        this.creando = false;
      }
    });
  }

  procesarPedidoIndividual() {
    // Este método se puede usar para validación adicional si es necesario
  }

  agregarLog(tipo: 'exito' | 'error' | 'info', mensaje: string) {
    this.operacionesLog.unshift({
      timestamp: new Date(),
      tipo,
      mensaje
    });
    
    // Mantener solo los últimos 20 logs
    if (this.operacionesLog.length > 20) {
      this.operacionesLog = this.operacionesLog.slice(0, 20);
    }
  }

  formatTime(timestamp: Date): string {
    return timestamp.toLocaleTimeString('es-ES', { 
      hour: '2-digit', 
      minute: '2-digit',
      second: '2-digit'
    });
  }
}
