# Etapa 1: Compilar la aplicación de Angular
FROM node:20-alpine AS build

WORKDIR /app

# Copia solo los archivos de manifiesto de paquetes
COPY package*.json ./

# Instala las dependencias del proyecto. Esto aprovecha el caché de Docker.
RUN npm install

# Ahora, copia el resto del código fuente de tu aplicación
COPY . .

# Construye la aplicación para producción
RUN npm run build

# Etapa 2: Servir la aplicación con Nginx
FROM nginx:alpine

# Copia los archivos de la aplicación ya compilados desde la etapa de 'build'
# Ojo: Verifica que esta sea la ruta de salida correcta. Angular 17+ usa una subcarpeta 'browser'.
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Copia tu configuración personalizada de Nginx al lugar correcto
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expone el puerto 80, que es el puerto por defecto de Nginx
EXPOSE 80

# Inicia Nginx
CMD ["nginx", "-g", "daemon off;"]