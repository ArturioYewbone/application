#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <libpq-fe.h>

int main() {
    PGconn *conn;
    PGresult *res;

    // Устанавливаем соединение с базой данных
    conn = PQconnectdb("dbname=mydb user=myuser password=mypassword host=localhost");
// Проверка наличия ошибок при соединении
    if (PQstatus(conn) == CONNECTION_BAD) {
        fprintf(stderr, "Ошибка при соединении: %s\n", PQerrorMessage(conn));
        PQfinish(conn);
        exit(1);
    }
    // Выполнение SQL-запроса для создания базы данных
    res = PQexec(conn, "CREATE DATABASE iss");

    if (PQresultStatus(res) != PGRES_COMMAND_OK) {
        fprintf(stderr, "Ошибка при создании базы данных: %s", PQerrorMessage(conn));
    } else {
        printf("База данных успешно создана.\n");
    }

    // Освобождение ресурсов
    PQclear(res);
    PQfinish(conn);
    return 0;
}
