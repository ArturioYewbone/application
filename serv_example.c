#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <curl/curl.h>

// Callback-функция для обработки ответа от сервера
size_t write_callback(void *contents, size_t size, size_t nmemb, void *userp) {
    size_t total_size = size * nmemb;
    char *data = (char *)userp;

    // Просто выводим ответ на экран
    printf("%s", data);
    return total_size;
}

int main() {
    CURL *curl;
    CURLcode res;

    // Инициализация библиотеки libcurl
    curl = curl_easy_init();
    if (curl) {
        // URL для запроса стоимости акций (замените на нужный URL)
        const char *url = "http://iss.moex.com/iss/history/engines/stock/markets/shares/boards/tqbr/securities.json?date=2013-12-20";

        // Устанавливаем URL
        curl_easy_setopt(curl, CURLOPT_URL, url);

        // Устанавливаем функцию обратного вызова для обработки ответа
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);

        // Создаем буфер для хранения ответа
        char data[4096];
        memset(data, 0, sizeof(data));

        // Устанавливаем указатель на буфер данных
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, data);

        // Выполняем запрос
        res = curl_easy_perform(curl);

        // Проверяем результат запроса
        if (res != CURLE_OK) {
            fprintf(stderr, "curl_easy_perform() failed: %s\n", curl_easy_strerror(res));
        }

        // Освобождаем ресурсы
        curl_easy_cleanup(curl);
    }

    return 0;
}