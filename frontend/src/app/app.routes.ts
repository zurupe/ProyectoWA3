import { Routes } from '@angular/router';
import { LoginComponent } from './login.component';
import { DashboardComponent } from './dashboard.component';
import { PedidosComponent } from './pedidos.component';
import { TrackingComponent } from './tracking.component';
import { AuthGuard } from './auth.guard';

export const routes: Routes = [
	{ path: '', redirectTo: 'login', pathMatch: 'full' },
	{ path: 'login', component: LoginComponent },
	{ path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
	{ path: 'pedidos', component: PedidosComponent, canActivate: [AuthGuard] },
	{ path: 'tracking', component: TrackingComponent, canActivate: [AuthGuard] },
];
