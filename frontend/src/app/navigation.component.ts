import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-navigation',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="navbar" *ngIf="authService.isAuthenticated()">
      <div class="nav-container">
        <div class="nav-brand">
          <h2>Sistema de Seguimiento de Pedidos</h2>
          <div class="user-info">
            <span class="user-name">{{ getUserName() }}</span>
            <span class="user-role" [class]="'role-' + getUserRole().toLowerCase().replace('role_', '')">
              {{ getRoleDisplay() }}
            </span>
          </div>
        </div>
        
        <div class="nav-links">
          <a routerLink="/dashboard" routerLinkActive="active" class="nav-link">
            Dashboard
          </a>
          
          <a routerLink="/pedidos" routerLinkActive="active" class="nav-link">
            Pedidos
          </a>
          
          <a routerLink="/tracking" routerLinkActive="active" class="nav-link">
            Tracking
          </a>
          
          <a routerLink="/clientes" routerLinkActive="active" class="nav-link" *ngIf="authService.getUserRole() === 'ROLE_ADMIN'">
            Clientes
          </a>
          
          <button class="nav-link logout-btn" (click)="logout()">
            Cerrar Sesión
          </button>
        </div>
      </div>
    </nav>
  `,
  styles: [`
    .navbar {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 1rem 0;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }
    
    .nav-container {
      max-width: 1200px;
      margin: 0 auto;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 2rem;
    }
    
    .nav-brand h2 {
      margin: 0;
      font-size: 1.5rem;
      font-weight: 600;
    }
    
    .user-info {
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      margin-top: 0.5rem;
    }
    
    .user-name {
      font-size: 0.9rem;
      opacity: 0.9;
    }
    
    .user-role {
      font-size: 0.75rem;
      padding: 0.2rem 0.5rem;
      border-radius: 12px;
      margin-top: 0.2rem;
      font-weight: 500;
    }
    
    .role-admin {
      background-color: #ff6b6b;
      color: white;
    }
    
    .role-cliente {
      background-color: #4ecdc4;
      color: white;
    }
    
    .nav-links {
      display: flex;
      gap: 0.5rem;
      align-items: center;
    }
    
    .nav-link {
      color: white;
      text-decoration: none;
      padding: 0.5rem 1rem;
      border-radius: 6px;
      transition: all 0.3s ease;
      font-weight: 500;
      background: none;
      border: none;
      cursor: pointer;
      font-size: 0.9rem;
    }
    
    .nav-link:hover {
      background-color: rgba(255, 255, 255, 0.2);
      transform: translateY(-1px);
    }
    
    .nav-link.active {
      background-color: rgba(255, 255, 255, 0.3);
      font-weight: 600;
    }
    
    .logout-btn {
      background-color: rgba(255, 255, 255, 0.1);
      border: 1px solid rgba(255, 255, 255, 0.3);
      margin-left: 1rem;
    }
    
    .logout-btn:hover {
      background-color: rgba(255, 255, 255, 0.2);
      border-color: rgba(255, 255, 255, 0.5);
    }
    
    @media (max-width: 768px) {
      .nav-container {
        flex-direction: column;
        gap: 1rem;
        padding: 0 1rem;
      }
      
      .nav-links {
        flex-wrap: wrap;
        justify-content: center;
      }
      
      .nav-brand {
        text-align: center;
      }
    }
  `]
})
export class NavigationComponent {
  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  getUserName(): string {
    const userInfo = this.authService.getUserInfo();
    return userInfo?.username || 'Usuario';
  }

  getUserRole(): string {
    return this.authService.getUserRole() || '';
  }

  getRoleDisplay(): string {
    const role = this.getUserRole();
    switch(role) {
      case 'ROLE_ADMIN': return 'Administrador';
      case 'ROLE_CLIENTE': return 'Cliente';
      default: return 'Usuario';
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}