import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from './auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, CommonModule],
  template: `
    <header class="header">
      <h1>Sistema de Pedidos</h1>
      <nav class="app-nav">
        <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
        <a routerLink="/clientes" routerLinkActive="active" *ngIf="isAdmin">Clientes</a>
        <a routerLink="/pedidos" routerLinkActive="active">Pedidos</a>
        <a routerLink="/tracking" routerLinkActive="active">Tracking</a>
        <a routerLink="/admin-sync" routerLinkActive="active" *ngIf="isAdmin" class="admin-link">🔧 Sincronización</a>
        <a routerLink="/login" routerLinkActive="active" class="logout-link">Salir</a>
      </nav>
      <div class="role-info">
        <span>Usuario: <strong>{{ username }}</strong></span>
        <span>Rol: <strong>{{ role }}</strong></span>
      </div>
    </header>
  `,
  styleUrls: ['./app-header.css']
})
export class AppHeaderComponent {
  role: string = '';
  username: string = '';
  isAdmin: boolean = false;

  constructor(private authService: AuthService) {
    this.loadUserInfo();
  }

  loadUserInfo() {
    const userInfo = this.authService.getUserInfo();
    if (userInfo) {
      this.username = userInfo.username || 'Usuario';
      this.role = userInfo.role === 'ROLE_ADMIN' ? 'Administrador' : 'Cliente';
      this.isAdmin = userInfo.role === 'ROLE_ADMIN';
    }
  }
}
    