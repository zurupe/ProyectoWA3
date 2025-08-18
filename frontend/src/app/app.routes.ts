import { Routes } from '@angular/router';
import { inject } from '@angular/core';

import { LoginComponent } from './login.component';
import { DashboardComponent } from './dashboard.component';
import { ClientesComponent } from './clientes.component';
import { PedidosComponent } from './pedidos.component';
import { TrackingComponent } from './tracking.component';
import { AdminSyncComponent } from './admin-sync.component';
import { AuthGuard } from './auth.guard';

// Guards como funciones
const authGuard = () => inject(AuthGuard).canActivate();
const adminGuard = () => inject(AuthGuard).canActivateAdmin();

export const routes: Routes = [
	{ path: '', redirectTo: 'login', pathMatch: 'full' },
	{ path: 'login', component: LoginComponent },
	{ 
		path: 'dashboard', 
		component: DashboardComponent,
		canActivate: [authGuard]
	},
	{ 
		path: 'clientes', 
		component: ClientesComponent,
		canActivate: [adminGuard]
	},
	{ 
		path: 'pedidos', 
		component: PedidosComponent,
		canActivate: [authGuard]
	},
	{ 
		path: 'tracking', 
		component: TrackingComponent,
		canActivate: [authGuard]
	},
	{ 
		path: 'admin-sync', 
		component: AdminSyncComponent,
		canActivate: [adminGuard]
	},
];
