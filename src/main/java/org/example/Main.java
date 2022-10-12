package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Main {
    private static String accessToken;
    private static String refreshToken;
    private static final String SERVER_HOST = "http://localhost:8080";

    public static void main(String[] args) throws IOException, InterruptedException {

        int command = 1;
        Scanner scanner = new Scanner(System.in);
        String username;
        String password;

        do {
            System.out.print("Enter username: ");
            username= scanner.next();
            System.out.print("Enter password: ");
            password = scanner.next();

        }while (!login(username, password));

        while (command != 0) {
            System.out.println("1) Home \n2) Devices \n3) Delete device");
            command = scanner.nextInt();
            switch (command) {
                case 1:
                    getHome();
                    break;
                case 2:
                    getDevice();
                    break;
                case 3:
                    System.out.println("Enter Device ID");
                    command = scanner.nextInt();
                    deleteDevice(command);
                    System.out.println("The Device has been deleted!");
                    break;

            }
        }
    }

    private static void getDevice() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url(SERVER_HOST + "/device")
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code()==200) {
                assert response.body() != null;
                System.out.println(new String(response.body().bytes()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void deleteDevice(Integer deviceId){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("deviceId",deviceId.toString())
                .build();
        Request request = new Request.Builder()
                .url(SERVER_HOST + "/device")
                .method("DELETE", body)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code()==200) {
                assert response.body() != null;
                System.out.println(new String(response.body().bytes()));
            }
            else
                System.out.println(response.code());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean login(String username, String password) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("username",username)
                .addFormDataPart("password",password)
                .build();
        Request request = new Request.Builder()
                .url(SERVER_HOST+"/auth/login")
                .method("POST", body)
                .addHeader("deviceName", "Android 9")
                .addHeader("appName", "Telegram 1.2")
                .build();
        try {

// jsonString is of type java.lang.String
//            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
// reader is of type java.io.Reader
//            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
// jsonReader is of type com.google.gson.stream.JsonReader
//            JsonObject jsonObject = JsonParser.parseReader(jsonReader).getAsJsonObject();

            Response response = client.newCall(request).execute();
            if (response.code()==200){

                // Deprecated version
//                JsonParser jsonParser = new JsonParser();
//                JsonObject object = (JsonObject) jsonParser.parse(new String(response.body().bytes()));

                JsonObject object = JsonParser.parseString(new String(response.body().bytes())).getAsJsonObject();

                if (object.get("statusCode").getAsInt()==0){
                    accessToken=object.getAsJsonObject("object").get("accessToken").getAsString();
                    refreshToken=object.getAsJsonObject("object").get("refreshToken").getAsString();
                    return true;
                }
                else {
                    System.out.println(object);
                }

            }else {
                System.out.println(response.code());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    public static void getHome(){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        Request request = new Request.Builder()
                .url(SERVER_HOST + "/home")
                .addHeader("Authorization", "Bearer "+accessToken)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code()==200){
                System.out.println(new String(response.body().bytes()));
            }else {
                System.out.println(response.code());
                getRefresh();
                getHome();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void getRefresh(){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("refreshToken",refreshToken)
                .build();
        Request request = new Request.Builder()
                .url(SERVER_HOST+"/auth/refresh")
                .method("POST", body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code()==200){
                JsonObject object = JsonParser.parseString(new String(response.body().bytes())).getAsJsonObject();

                if (object.get("statusCode").getAsInt()==0){
                    accessToken=object.getAsJsonObject("object").get("accessToken").getAsString();
                    refreshToken=object.getAsJsonObject("object").get("refreshToken").getAsString();
                }
                else {
                    System.out.println(object);
                }
            }else {
                System.out.println(response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}