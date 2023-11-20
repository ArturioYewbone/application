#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <memory.h>

const static char HELLO[] = "Echo server (version 0.1)\n";
const static int HELLO_SIZE = strlen(HELLO);

int main(int argc, char **argv)
{
    int sockfd;
    struct sockaddr_in addr;
    char buf[255];

    if( argc != 2 ) {
        fprintf(stdout, "Usage: %s port\n", argv[0]);
        return 1;
    }

    memset(&addr, 0, sizeof(addr));
    addr.sin_family = AF_INET;
    addr.sin_port = htons(atoi(argv[1]));
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

    while( 1 ) {
        struct sockaddr *ca = NULL;
        socklen_t sz = 0;
        int fd;
        ssize_t k;

        if( (fd = accept(sockfd, ca, &sz)) < 0 ) {
            fprintf(stderr, "Unable to listen socket: %s\n",
                strerror(errno));
        } else if( send(fd, HELLO, HELLO_SIZE, 0) == HELLO_SIZE ) {
            int f = 1;
            while( f ) {
                memset(buf, 0, sizeof(buf));
                if( (k = recv(fd, buf, 254, 0)) > 0 ) {
                    printf("%s", buf);
                    if( strncmp(buf, "quit", 4) == 0 ) {
                        f = 0;
                    }
                }
            }

            close(fd);
        } else {
            fprintf(stderr, "Unable to send greeting message: %s\n",
                strerror(errno));
            close(fd);
        }
    } 

    close(sockfd);

    return 0;
}
