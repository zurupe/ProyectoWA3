export interface PedidoRequest {
  producto: string;
  clienteId: number;
  direccion: string;
  estado?: string;
}

export interface PedidoResponse {
  id: number;
  producto: string;
  clienteId: number;
  direccion: string;
  estado: string;
  fechaCreacion: string;
  fechaActualizacion: string;
}

export enum EstadoPedido {
  PENDIENTE = 'PENDIENTE',
  EN_PROCESO = 'EN_PROCESO',
  ENVIADO = 'ENVIADO',
  ENTREGADO = 'ENTREGADO',
  CANCELADO = 'CANCELADO'
}