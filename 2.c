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
#include <curl/curl.h>
#include <signal.h>
#include "/home/mpi/pin2106/Sorokin/libs/cJSON-master/cJSON.h" // Библиотека для работы с JSON
//gcc -g -Wall -I/usr/local/include/pgsql -o  2 2.c -lpq
//gcc -g -Wall -I/usr/local/include/pgsql -o  2 2.c -lpq -lcurl -I/home/mpi/pin2106/Sorokin/libs/cJSON-master/build -L/home/mpi/pin2106/Sorokin -lcjson
int sockfd;

struct sockaddr_in clientAddr;
socklen_t clientAddrLen = sizeof(clientAddr);
char *everyStock[600];

PGconn *conn;
const static int bufSize = 1024;
size_t dataSize=0;
PGresult* exSql(PGconn* conn, char* sql);
size_t curlWriteFunction(void* ptr, size_t size/*always==1*/, size_t nmemb, void* userdata)
{
    char** stringToWrite=(char**)userdata;
    const char* input=(const char*)ptr;
    if(nmemb==0) return 0;
    if(!*stringToWrite)
        *stringToWrite=malloc(nmemb+1);
    else
        *stringToWrite=realloc(*stringToWrite, dataSize+nmemb+1);
    memcpy(*stringToWrite+dataSize, input, nmemb);
    dataSize+=nmemb;
    (*stringToWrite)[dataSize]='\0';
    return nmemb;
}
void signal_handler(int signum){//ctrl+c
    if (signum == SIGINT){
        printf("Close server\n");
        if(sockfd != NULL){close(sockfd);}
        exit(0);
    }
}
void segfault_handler(int signal) {
    printf("Произошла ошибка сегментирования (Segmentation Fault)\n");
    if(sockfd != NULL){close(sockfd);}
    exit(0); // Выход из программы
}
int main(int argc, char **argv){ 
    PGresult *res;   
    
    int port = 45127;
    char *sen = "Server (version 0.3)\n";
    int sen_size = strlen(sen);
    
    
    
    struct sockaddr_in addr;
    char buf[255];
    char sql[250];
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
    
    signal(SIGINT, signal_handler);
    struct ifaddrs *ifap, *ifa;
    struct sockaddr_in *sa;
    char *add;
    if (getifaddrs(&ifap) == -1) {
        perror("getifaddrs");
        exit(EXIT_FAILURE);
    }
    pid_t pid;
    //if((pid=fork())<0){
      //  fprintf(stderr,"Unable to fork process: %s\n",strerror(errno));
    //}else if(pid == 0){
      //  do{
            printf("update stock array\n");
            createStock();
            
           // sleep(2 * 60);
            
       // }while (1);
    //}
    while( 1 ) {
        struct sockaddr *ca = NULL;
        socklen_t sz = 0;
        int fd;
        ssize_t k;
        
        
        if( (fd = accept(sockfd, ca, &sz)) < 0 ) {
            fprintf(stderr, "Unable to listen socket: %s\n",
            strerror(errno));
        }else if(send(fd, sen, sen_size, 0) == sen_size ) {
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
                int id_user;
                memset(buf, 0, sizeof(buf));
                int count_if = 0;
                if( (k = recv(fd, buf, 254, 0)) > 0 ) {
                    count_if = 0;
                    printf("buf:%s\n", buf); 
                    
                    char *temp = strtok(buf, " ");
                    command = temp; 
                    if(strncmp(command, "input", 5) == 0){
                        printf(":input\n");
                        temp = strtok(NULL, " ");
                        word1 = temp;
                        temp = strtok(NULL, " ");
                        word2 = temp;
                        sprintf(sql, "SELECT * FROM users WHERE log = '%s'", word1);
                        res = exSql(conn, sql);

                        // Получение результатов запроса
                        int rows = PQntuples(res);
                        int cols = PQnfields(res);
                        //printf("r:%i\n", rows);
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
                                id_user = atoi(PQgetvalue(res, 0, 0));
                                printf("user id %d\n");
                            }else{sen = "falsepas\n";}                            
                            printf("%s", sen);
                            sen_size = strlen(sen);
                            send(fd, sen, sen_size, 0);
                            continue;
                        }                   
                    }
                    if(strncmp(command, "createnewlog", 12) == 0){
                        printf(":createnewlog\n");
                        temp = strtok(NULL, " ");
                        word1 = temp;
                        temp = strtok(NULL, " ");
                        word2 = temp;
                        //memset(sql, 0, sizeof(sql));
                        sprintf(sql, "INSERT INTO users (log, pas) VALUES ('%s', '%s')", word1, word2);
                        res = exSql(conn, sql);
                        if(res == NULL){
                            sen = "createloger\n";
                            printf("%s", sen);
                            sen_size = strlen(sen);
                            send(fd, sen, sen_size, 0);
                            continue;
                        }
                        sen = "createlogsuc\n";
                        //PGresult *res = PQexecParams(conn, sql, 2, NULL, paramValues, paramLengths, paramFormats, 0);                        
                        printf("%s", sen);
                        sen_size = strlen(sen);
                        send(fd, sen, sen_size, 0);
                        continue;
                    }                     
                    if(strncmp(command, "quit", 4) == 0 ) {
                        getpeername(fd, (struct sockaddr*)&clientAddr, &clientAddrLen);
                        char clientIP[INET_ADDRSTRLEN];
                        fprintf(stdout, "Сlient %s disconnected\n", inet_ntoa(clientAddr.sin_addr));
                        f = 0;
                        continue;
                    }
                    if(strncmp(command, "every_open", 10) == 0){
                        printf(":every_open\n");
                        int tt = sizeof(everyStock) / sizeof(everyStock[0]);
                        printf("len: %d\n" , tt);
                        for(int i = 0; i < tt; i++){
                            if(everyStock[i] == NULL){
                                //printf("[%d] = 0\n", i);
                                char temp_sen[20];
                                snprintf(temp_sen, sizeof(temp_sen), "*%d", i);
                                printf("%s position send\n", temp_sen);
                                //sprintf(sen, "%d\n", i);
                                sen_size = strlen(temp_sen);
                                send(fd, temp_sen, sen_size, 0);
                                send(fd, "\n", 1, 0);
                                break;
                            }
                            //printf("el:%s, len:%d\n", everyStock[i], strlen(everyStock[i]));
                            send(fd, everyStock[i], strlen(everyStock[i]), 0);
                            send(fd, "\n", 1, 0);
                        }
                        sprintf(sql, "select sname from users join fav_comm on users.id = fav_comm.user_id JOIN fav ON fav_comm.fav_id = fav.id where users.id = '%d';", id_user);
                        res = exSql(conn, sql);
                        int rows = PQntuples(res);
                        if(rows < 1){continue;}
                        char temp [6];
                        for(int i = 0; i < rows; i++){
                            sprintf(temp, PQgetvalue(res, i, 0));
                            printf("%s\n", temp);
                            send(fd, temp, strlen(temp), 0);
                            send(fd, "\n", 1, 0);
                        }
                        send(fd, "*\n", 3, 0);
                        continue;
                    }
                    if(strncmp(command, "add_favor", 9) == 0){
                        temp = strtok(NULL, " ");
                        word1 = temp;
                        sprintf(sql, "SELECT * FROM fav");
                        res = exSql(conn, sql);
                        
                        int rows = PQntuples(res);
                        int flag_have = 0;
                        int id_fav = 0;
                        for(int i = 0; i < rows; i++){
                            char *sname = PQgetvalue(res, i, 1);
                            //printf("name %s - word %s len %d\n", sname, word1, strncmp(word1, sname, strlen(word1)));
                            
                            if(strncmp(word1, sname, strlen(word1)) == 0){
                                id_fav = atoi(PQgetvalue(res, i, 0));
                                printf("id %d\n", id_fav);
                                flag_have = 1;
                                break;
                            }
                        }
                        if(flag_have == 0){                            
                            sprintf(sql, "INSERT INTO fav (sname) VALUES ('%s')", word1);
                            res = exSql(conn, sql);
                            id_fav = atoi(PQgetvalue(res, rows, 0));
                        }
                        printf("id fav %d\n", id_fav);
                        //sql="INSERT INTO fav_comm (user_id, fav_id) VALUES ($1, $2)";
                        sprintf(sql, "INSERT INTO fav_comm (user_id, fav_id) VALUES ('%d', '%d')", id_user, id_fav);
                        printf("sql %s\n", sql);
                        res = exSql(conn, sql);
                        printf("suc res\n");
                        sen= "suc\n";
                        printf("%s", sen);
                        sen_size = strlen(sen);
                        send(fd, sen, sen_size, 0);
                        continue;
                    }
                    if(strncmp(command, "rem_favor", 9) == 0){
                        temp = strtok(NULL, " ");
                        word1 = temp;
                        sprintf(sql, "SELECT * FROM fav");
                        res = exSql(conn, sql);
                        
                        int rows = PQntuples(res);
                        int id_fav = 0;
                        for(int i = 0; i < rows; i++){
                            char *sname = PQgetvalue(res, i, 1);
                            if(strncmp(word1, sname, strlen(word1)) == 0){
                                id_fav = atoi(PQgetvalue(res, i, 0));
                                break;
                            }
                        }
                        //sql="INSERT INTO fav_comm (user_id, fav_id) VALUES ($1, $2)";
                        sprintf(sql, "DELETE FROM fav_comm WHERE user_id = '%d' AND fav_id = '%d'", id_user, id_fav);
                        printf("sql %s\n", sql);
                        res = exSql(conn, sql);
                        printf("suc res\n");
                        sen= "suc\n";
                        printf("%s", sen);
                        sen_size = strlen(sen);
                        send(fd, sen, sen_size, 0);
                        continue;
                    }
                }
                count_if++;
                if(count_if > 5){
                    f = 0;
                    break;
                }
                PQclear(res);
                PQfinish(conn);
                if(fd != NULL){close(fd);}
            }
            PQfinish(conn);
            // Освобождение ресурсов
            
        } else {
            fprintf(stderr, "Unable to send greeting message: %s\n",
                strerror(errno));
            if(fd != NULL){close(fd);}
        }
        if(fd != NULL){close(fd);}
    } 

    close(sockfd);

    return 0;
}
// ctrl c

void createStock(){
    char* data=0;
    CURL*const curl=curl_easy_init();
    // Инициализация библиотеки cJSON
    cJSON_InitHooks(NULL);

    if (curl) {
        const char *url = "https://iss.moex.com/iss/engines/stock/markets/shares/boards/tqbr/securities.json?iss.meta=off&iss.only=securities,marketdata&securities.columns=SECID,SHORTNAME,NAME&marketdata.columns==SECID,SHORTNAME,FULLNAME,LAST";
        
        curl_easy_setopt(curl, CURLOPT_URL, url);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &data);
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, &curlWriteFunction);

        if(curl_easy_perform(curl)!=CURLE_OK)
        {
            fprintf(stderr, "Failed to get web page\n");
            return 1;
        }
        curl_easy_cleanup(curl);
        if(!data)
        {
            fprintf(stderr, "Got no data\n");
            return 1;
        }
        // Разбор JSON-ответа
        
        cJSON *json = cJSON_Parse(data);
        if (json) {
            char *formatted_json = cJSON_Print(json); // Преобразовать JSON в форматированную строку
            char *temp = strtok(formatted_json, "[");
            int i = 0;
            int f = 0;
            temp = strtok(NULL, "[");
            temp = strtok(NULL, "[");
            while(temp != NULL){
                if(temp[strlen(temp) - 2] == ':'){
                    while(temp[strlen(temp) - 1] != ']'){
                        f = 1;
                        temp[strlen(temp) - 1] = '\0';
                    }
                    temp[strlen(temp) - 2] = '\0';
                }
                if(f == 1){
                    everyStock[i] = temp;
                    i++;
                    temp = strtok(NULL, "[");
                    temp = strtok(NULL, "[");
                    f = 0;
                    continue;
                }
                temp[strlen(temp) - 3] = '\0';
                everyStock[i] = temp;
                //printf("%s\n", temp);
                i++;                
                temp = strtok(NULL, "[");
            }            
        }
    }    
    free(data);
    // Освободить память, выделенную для ответа
    cJSON_InitHooks(NULL); // Освободить ресурсы cJSON
    curl_global_cleanup();
    printf("suc update stock array\n");
}

void printClientIP(int clientSocket) {     
    if (getpeername(clientSocket, (struct sockaddr*)&clientAddr, &clientAddrLen) == 0) {
        char clientIP[INET_ADDRSTRLEN];
        if (inet_ntop(AF_INET, &(clientAddr.sin_addr), clientIP, INET_ADDRSTRLEN) != NULL) {
            printf("IP-адресс клиента: %s\n", clientIP);
        } else {
            perror("inet_ntop");
        }
    } else {
        perror("getpeername"); 
    }
}

// Выполнение sql-запроса
PGresult* exSql(PGconn* conn, char* sql){
    PGresult *result;
    char* er = (char*)malloc(100);
    //char* result = (char*)malloc(bufSize);
    //memset(result, 0, bufSize);

    // Начать транзакцию
    PGresult* res = PQexec(conn, "BEGIN TRANSACTION");
    if (PQresultStatus(res) != PGRES_COMMAND_OK){
        fprintf(stderr, "BEGIN command failed: %s", PQerrorMessage(conn));
        exit(1);
    }

    // Очистить res, чтобы избежать утечки памяти
    PQclear(res);

    // Выполнить запрос
    res = PQexec(conn, sql);

    if (PQresultStatus(res) == PGRES_COMMAND_OK){
        int affected_rows = atoi(PQcmdTuples(res));
        snprintf(er, bufSize, "%d", affected_rows);
    }else if (PQresultStatus(res) == PGRES_TUPLES_OK){
        printf("TRANSACTION suc\n");
        result = PQcopyResult(res, PG_COPYRES_ATTRS | PG_COPYRES_TUPLES);
        
    }else{
        fprintf(stderr, "Sql command failed: %s", PQerrorMessage(conn));
        snprintf(er, bufSize, "%d", -1);
    }

    // Очистить res
    PQclear(res);

    // Завершить транзакцию
    res = PQexec(conn, "COMMIT");
    PQclear(res);

    return result;
}

