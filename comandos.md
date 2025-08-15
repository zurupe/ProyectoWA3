//para entrar a la bd de pedidos
docker exec -it mysql-pedido mysql -u pedido_user -p

# Access the PostgreSQL container shell
docker exec -it postgres-cliente bash

# Inside the container, connect to PostgreSQL
psql -U postgres -d auth_db

# Once in psql, list tables and query users
\dt
SELECT * FROM users;