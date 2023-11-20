#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <memory.h>
#include <arpa/inet.h>
#include <ifaddrs.h>
#include "libpq-fe.h"
//gcc -g -Wall -I/usr/local/include/pgsql -o  2 2.c -lpq

int main(int argc, char **argv)
{
    int port = 45127;
    char *sen = "Server (version 0.1)\n";
    int sen_size = strlen(sen);
    PGconn *conn;
    PGresult *res;
    int sockfd;
    struct sockaddr_in addr;
    char buf[255];
    char sql[100];
    //char command[50];
    //char word1[50];
    //char word2[50];
    
    const char *dbname = PQdb(conn);
    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_port = htons(port);
    //addr.sin_addr.s_addr = inet_addr("82.179.140.18");
    addr.sin_addr.s_addr = INADDR_ANY;

    if( (sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0 ) {
        fprintf(stderr, "Unable to create socket: %s\n",
            strerror(errno));
        return 1;
    }
    if( bind(sockfd, (struct sockaddr *) &addr, sizeof(addr)) < 0 ) {
        fprintf(stderr, "Unable to bind socket: %s\n",
            strerror(errno));
        
        close(sockfd);
        return 1;
    }
    if( listen(sockfd, 5) < 0 ) {
        fprintf(stderr, "Unable to listen socket: %s\n",
                strerror(errno));
        close(sockfd);
        return 1;
    }
    struct sockaddr_in local_addr;
    socklen_t local_addr_len = sizeof(local_addr);
    if (getsockname(sockfd, (struct sockaddr*)&local_addr, &local_addr_len) == 0) {
        printf("Server IP: %s\n", inet_ntoa(local_addr.sin_addr)); // Вывести IP-адрес
        printf("Server Port: %d\n", ntohs(local_addr.sin_port));    // Вывести порт
    } else {
        fprintf(stderr, "Unable to get socket information: %s\n", strerror(errno));
    }
    
    struct ifaddrs *ifap, *ifa;
    struct sockaddr_in *sa;
    char *add;
    if (getifaddrs(&ifap) == -1) {
        perror("getifaddrs");
        exit(EXIT_FAILURE);
    }
    while( 1 ) {
        struct sockaddr *ca = NULL;
        socklen_t sz = 0;
        int fd;
        ssize_t k;
        pid_t pid;
        if( (fd = accept(sockfd, ca, &sz)) < 0 ) {
            fprintf(stderr, "Unable to listen socket: %s\n",
                strerror(errno));        
        } else if((pid=fork())<0){
            fprintf(stderr,"Unable to fork process: %s\n",strerror(errno));
        } else if(pid>0){
            close(fd);
        } else if( send(fd, sen, sen_size, 0) == sen_size ) {
            int f = 1;
            printClientIP(fd);
            while( f ) {
                conn = PQconnectdb("hostaddr=82.179.140.18 port=5432 dbname=iss user=mpi password=135a1");
                if (PQstatus(conn) == CONNECTION_BAD) {
                    fprintf(stderr, "Ошибка подключения: %s\n", PQerrorMessage(conn));
                    PQfinish(conn);
                    return 1;
                }
                
                char *command, *word1, *word2;
                memset(buf, 0, sizeof(buf));
                if( (k = recv(fd, buf, 254, 0)) > 0 ) {
                    //memset(command, 0, sizeof(command));
                    //memset(word1, 0, sizeof(word1));
                    //memset(word2, 0, sizeof(word2));
                    printf(":%s\n", buf); 
                    char *temp = strtok(buf, " ");
                    command = temp; 
                    printf("%s:%s:%i\n",temp, command, sizeof(command));
                    printf(":%i\n", strncmp(command, "createnewlog", 12));
                    if(strncmp(command, "input", 5) == 0){
                        temp = strtok(NULL, " ");
                        word1 = temp;
                        temp = strtok(NULL, " ");
                        word2 = temp;
                        // SQL-запрос для поиска данных в таблице
                        memset(sql, 0, sizeof(sql));
                        sprintf(sql, "SELECT * FROM users WHERE log = '%s'", word1);
                        // Выполнение SQL-запроса
                        res = PQexec(conn, sql);
                        if (PQresultStatus(res) != PGRES_TUPLES_OK) {
                            fprintf(stderr, "Ошибка выполнения SQL-запроса: %s\n", PQerrorMessage(conn));
                            PQclear(res);
                            PQfinish(conn);
                            return 1;
                        }

                        // Получение результатов запроса
                        int rows = PQntuples(res);
                        int cols = PQnfields(res);
                        
                        if(rows == 0){                            
                            sen = "falselog\n";
                            printf("%s", sen);
                            sen_size = strlen(sen);
                            send(fd, sen, sen_size, 0);
                            continue;
                        }else{
                            char *t_pas = PQgetvalue(res, 0, 2);
                            if(strncmp(t_pas, word2, sizeof(t_pas)) == 0){
                                sen = "true\n";
                            }else{sen = "falsepas\n";}                            
                            printf("%s", sen);
                            sen_size = strlen(sen);
                            send(fd, sen, sen_size, 0);
                            continue;
                        }                        
                    }
                    
                    if(strncmp(dbname, "iss", 3) != 0){
                        while (1){
                            printf("ss");
                            /* wait */
                        }
                        
                    }
                    if(strncmp(command, "createnewlog", 12) == 0){
                        temp = strtok(NULL, " ");
                        word1 = temp;
                        temp = strtok(NULL, " ");
                        word2 = temp;
                        memset(sql, 0, sizeof(sql));
                        sprintf(sql,"INSERT INTO users (log, pas) VALUES ($1, $2)");
                        const char *paramValues[2];
                        paramValues[0] = word1;
                        paramValues[1] = word2;

                        int paramLengths[2];
                        int paramFormats[2];

                        paramLengths[0] = strlen(word1);
                        paramLengths[1] = strlen(word2);
                        paramFormats[0] = 0;  // 0 для текстовых данных
                        paramFormats[1] = 0;

                        PGresult *res = PQexecParams(conn, sql, 2, NULL, paramValues, paramLengths, paramFormats, 0);

                        if (PQresultStatus(res) != PGRES_COMMAND_OK) {
                            fprintf(stderr, "Ошибка выполнения SQL-запроса: %s\n", PQerrorMessage(conn));
                            PQclear(res);
                            PQfinish(conn);
                            return 1;
                        }
                        sen = "suc\n";
                        printf("%s", sen);
                        sen_size = strlen(sen);
                        send(fd, sen, sen_size, 0);
                        continue;
                    }
                    if( strncmp(buf, "quit", 4) == 0 ) {
                        f = 0;
                    }
                }
                printf("not input\n");

            }
            PQclear(res);
            PQfinish(conn);
            close(fd);
            // Освобождение ресурсов
            
        } else {
            fprintf(stderr, "Unable to send greeting message: %s\n",
                strerror(errno));
            close(fd);
        }

    } 

    close(sockfd);

    return 0;
}
void printClientIP(int clientSocket) {
        struct sockaddr_in clientAddr;
        socklen_t clientAddrLen = sizeof(clientAddr);
        if (getpeername(clientSocket, (struct sockaddr*)&clientAddr, &clientAddrLen) == 0) {
            char clientIP[INET_ADDRSTRLEN];
            if (inet_ntop(AF_INET, &(clientAddr.sin_addr), clientIP, INET_ADDRSTRLEN) != NULL) {
                printf("Клиент подключился!\n");
                printf("IP-адресс клиента: %s\n", clientIP);
            } else {
                perror("inet_ntop");
            }
        } else {
            perror("getpeername"); 
        }
}
