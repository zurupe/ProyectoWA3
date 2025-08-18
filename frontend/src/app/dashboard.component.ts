import { Component, OnInit } from '@angular/core';
import { ClienteService } from './cliente.service';
import { CommonModule } from '@angular/common';

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
      <h3>Clientes Registrados</h3>
      <table class="table">
        <thead>
          <tr>
            <th>#</th>
            <th>Nombre</th>
            <th>Email</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let cliente of clientes; let i = index">
            <td>{{ i + 1 }}</td>
            <td>{{ cliente.nombre }}</td>
            <td>{{ cliente.email }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class DashboardComponent implements OnInit {
  clientes: any[] = [];

  constructor(private clienteService: ClienteService) {}

  ngOnInit() {
    this.cargarClientes();
  }

  cargarClientes() {
    this.clienteService.getClientes().subscribe((data: any) => {
      this.clientes = data;
    });
  }
}
