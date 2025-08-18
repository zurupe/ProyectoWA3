import { Routes } from '@angular/router';

import { LoginComponent } from './login.component';
import { DashboardComponent } from './dashboard.component';
import { ClientesComponent } from './clientes.component';
import { PedidosComponent } from './pedidos.component';
import { TrackingComponent } from './tracking.component';

export const routes: Routes = [
	{ path: '', redirectTo: 'login', pathMatch: 'full' },
	{ path: 'login', component: LoginComponent },
	{ path: 'dashboard', component: DashboardComponent },
	{ path: 'clientes', component: ClientesComponent },
	{ path: 'pedidos', component: PedidosComponent },
	{ path: 'tracking', component: TrackingComponent },
];
