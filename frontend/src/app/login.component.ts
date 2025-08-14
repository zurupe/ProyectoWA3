import { Component } from '@angular/core';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="dashboard-container">
      <h2 class="section-title">Iniciar Sesión</h2>
      <form (ngSubmit)="login()" #loginForm="ngForm">
        <div class="input-group">
          <label for="username">Usuario</label>
          <input id="username" name="username" type="text" [(ngModel)]="credentials.username" required />
        </div>
        <div class="input-group">
          <label for="password">Contraseña</label>
          <input id="password" name="password" type="password" [(ngModel)]="credentials.password" required />
        </div>
        <button class="btn-primary" type="submit">Ingresar</button>
      </form>
      <div *ngIf="error" class="error-message">{{ error }}</div>
    </div>
  `
})
export class LoginComponent {
  credentials = { username: '', password: '' };
  error: string = '';

  constructor(private authService: AuthService, private router: Router) {}

  login() {
    this.authService.login(this.credentials).subscribe({
      next: (res: any) => {
        localStorage.setItem('access_token', res.access_token);
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.error = 'Usuario o contraseña incorrectos';
      }
    });
  }
}
