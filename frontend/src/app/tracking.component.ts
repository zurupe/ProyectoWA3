import { Component } from '@angular/core';
import { TrackingService } from './tracking.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-tracking',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard-container">
      <h2 class="section-title">Tracking de Pedido</h2>
      <form (ngSubmit)="consultarEstado()" #trackingForm="ngForm">
        <div class="input-group">
          <label for="numero">Número de Pedido</label>
          <input id="numero" name="numero" type="text" [(ngModel)]="pedidoId" required />
        </div>
        <button class="btn-primary" type="submit">Consultar Estado</button>
      </form>
      <div *ngIf="estado">
        <h3>Estado Actual</h3>
        <span class="status-indicator" [ngClass]="{ synced: estado.sincronizado, outdated: !estado.sincronizado }">
          {{ estado.estado }}
        </span>
        <span *ngIf="estado.sincronizado" class="status-indicator synced">Sincronizado</span>
        <span *ngIf="!estado.sincronizado" class="status-indicator outdated">Desactualizado</span>
      </div>
    </div>
  `
})
export class TrackingComponent {
  pedidoId: string = '';
  estado: any = null;

  constructor(private trackingService: TrackingService) {}

  consultarEstado() {
    this.trackingService.getEstadoPedido(this.pedidoId).subscribe((data: any) => {
      this.estado = data;
    });
  }
}
