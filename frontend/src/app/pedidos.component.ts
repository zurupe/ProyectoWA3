import { Component } from '@angular/core';
import { PedidoService } from './pedido.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="dashboard-container">
      <h2 class="section-title">Pedidos</h2>
      <form (ngSubmit)="crearPedido()" #pedidoForm="ngForm">
        <div class="input-group">
          <label for="producto">Producto</label>
          <input id="producto" name="producto" type="text" [(ngModel)]="nuevoPedido.producto" required />
        </div>
        <div class="input-group">
          <label for="cliente">Cliente</label>
          <input id="cliente" name="cliente" type="text" [(ngModel)]="nuevoPedido.cliente" required />
        </div>
        <div class="input-group">
          <label for="direccion">Dirección</label>
          <input id="direccion" name="direccion" type="text" [(ngModel)]="nuevoPedido.direccion" required />
        </div>
        <button class="btn-primary" type="submit">Crear Pedido</button>
      </form>
      <hr />
      <h3>Pedidos Activos</h3>
      <table class="table">
        <thead>
          <tr>
            <th>Número</th>
            <th>Producto</th>
            <th>Cliente</th>
            <th>Dirección</th>
            <th>Estado</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let pedido of pedidos; let i = index">
            <td>{{ i + 1 }}</td>
            <td>{{ pedido.producto }}</td>
            <td>{{ pedido.cliente }}</td>
            <td>{{ pedido.direccion }}</td>
            <td>{{ pedido.estado }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class PedidosComponent {
  pedidos: any[] = [];
  nuevoPedido = { producto: '', cliente: '', direccion: '' };

  constructor(private pedidoService: PedidoService) {
    this.cargarPedidos();
  }

  cargarPedidos() {
    this.pedidoService.getPedidos().subscribe((data: any) => {
      this.pedidos = data;
    });
  }

  crearPedido() {
    this.pedidoService.createPedido(this.nuevoPedido).subscribe(() => {
      this.nuevoPedido = { producto: '', cliente: '', direccion: '' };
      this.cargarPedidos();
    });
  }
}
