export interface Cliente {
  id?: number;
  nombre: string;
  apellido: string;
  email: string;
  telefono: string;
  direccion: string;
  ciudad: string;
  pais: string;
  codigoPostal?: string;
  activo?: boolean;
  fechaCreacion?: string;
  fechaActualizacion?: string;
  nombreCompleto?: string;
}

export interface ClienteRequest {
  nombre: string;
  apellido: string;
  email: string;
  telefono: string;
  direccion: string;
  ciudad: string;
  pais: string;
  codigoPostal?: string;
}

export interface ClienteResponse extends Cliente {
  id: number;
  activo: boolean;
  fechaCreacion: string;
  fechaActualizacion: string;
  nombreCompleto: string;
}

export interface EstadisticasClientes {
  clientesActivos: number;
}

export interface EmailVerificacion {
  email: string;
  disponible: boolean;
}