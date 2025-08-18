import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  template: `
    <header class="header">
      <h1>Sistema de Pedidos</h1>
      <nav class="app-nav">
        <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
        <a routerLink="/clientes" routerLinkActive="active">Clientes</a>
        <a routerLink="/pedidos" routerLinkActive="active">Pedidos</a>
        <a routerLink="/tracking" routerLinkActive="active">Tracking</a>
        <a routerLink="/login" routerLinkActive="active">Salir</a>
      </nav>
      <div class="role-switch">
        <span>Rol actual: <strong>{{ role }}</strong></span>
        <button (click)="toggleRole()">Cambiar rol</button>
      </div>
    </header>
  `,
  styleUrls: ['./app-header.css']
})
export class AppHeaderComponent {
  role: string = 'cliente';
  toggleRole() {
    this.role = this.role === 'cliente' ? 'admin' : 'cliente';
  }
}
    