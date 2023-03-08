package com.itbatia;

import java.net.URI;
import java.net.http.*;

import java.time.Duration;
import java.util.Scanner;
import java.util.concurrent.*;

import static java.time.temporal.ChronoUnit.SECONDS;

public class App {

    private final Scanner scanner = new Scanner(System.in);
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private void executeCheck() {
        long interval = getInterval();
        String url = getUrl();
        runCheck(interval, url);
    }

    private long getInterval() {
        System.out.println("Введите интервал в секундах: ");
        String input = scanner.nextLine();
        while (!input.matches("\\d+")) {
            System.out.println("Вы ввели некорректные данные. Введите число: ");
            input = scanner.nextLine();
        }
        return Long.parseLong(input);
    }

    private String getUrl() {
        System.out.println("Введите URL: ");
        String input = scanner.nextLine();
        String regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        while (!input.matches(regex)) {
            System.out.println("Вы ввели некорректный url. Повторите попытку: ");
            input = scanner.nextLine();
        }
        return input;
    }

    private void runCheck(long interval, String url) {
        executorService.scheduleAtFixedRate(() -> {
            try {
                healthcheck(url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, 0, interval, TimeUnit.SECONDS);
    }

    private void healthcheck(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.of(4, SECONDS))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        showResult(response.statusCode());
    }

    private void showResult(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) {
            System.out.println("Result: OK(" + statusCode + ")");
        } else if (statusCode >= 300 && statusCode < 400) {
            System.out.println("Result: REDIRECT(" + statusCode + ")");
        } else if (statusCode >= 400 && statusCode < 500) {
            System.out.println("Result: SERVER ERROR(" + statusCode + ")");
        } else if (statusCode >= 500) {
            System.out.println("Result: CLIENT ERROR(" + statusCode + ")");
        }
    }

    public static void main(String[] args) {
        new App().executeCheck();
    }
}
