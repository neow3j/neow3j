package io.neow3j.wallet;

import io.neow3j.protocol.Neow3j;
import io.neow3j.protocol.http.HttpService;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public class ResponseInterceptor implements Interceptor {

    private String address;

    public ResponseInterceptor(String address) {
        this.address = address;
    }

    public static Neow3j createNeow3jWithInceptor(String address) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new ResponseInterceptor(address)).build();
        HttpService httpService = new HttpService(httpClient);
        return Neow3j.build(httpService);
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {

        String body = getRequestBodyAsString(chain.request().body());
        String responseJson = null;
        try {
            if (body.contains("listplugins")) {
                responseJson = getResponseJson("listplugins_response.json");
            } else if (body.contains("getunspents")) {
                responseJson = getResponseJson("getunspents_response_" + address + ".json");
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return new okhttp3.Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_2)
                .code(200)
                .message("")
                .body(ResponseBody.create(MediaType.parse("application/json"), responseJson))
                .build();
    }

    private String getRequestBodyAsString(RequestBody body) throws IOException {
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        InputStream inputStream = buffer.inputStream();
        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        return new String(bytes, "UTF-8");
    }

    private String getResponseJson(String fileName) throws IOException, URISyntaxException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
        File file = new File(url.toURI());
        FileInputStream stream = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        stream.read(data);
        stream.close();
        return new String(data, "UTF-8");
    }
}
