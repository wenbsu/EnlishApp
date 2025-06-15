package com.liaoyunan.englishapp.utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.http.Path;
import retrofit2.http.HeaderMap;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RetrofitRequest - 修正后的版本，支持自动反序列化 JSON 对象
 */
public class RetrofitRequestUtil<T> {

    private static final Map<String, Retrofit> retrofitCache = new ConcurrentHashMap<>();
    private ApiService apiService;
    private Class<T> mClass;
    private Gson gson;

    public interface ResponseListener<T> {
        void onResponse(T response);
    }

    public interface ErrorListener {
        void onError(String error);
    }

    public RetrofitRequestUtil(String baseUrl, Class<T> clazz) {
        this.mClass = clazz;
        this.gson = new Gson();
        String formattedBaseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.apiService = getRetrofitInstance(formattedBaseUrl).create(ApiService.class);
    }

    private Retrofit getRetrofitInstance(String baseUrl) {
        if (!retrofitCache.containsKey(baseUrl)) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            retrofitCache.put(baseUrl, retrofit);
        }
        return retrofitCache.get(baseUrl);
    }

    interface ApiService {
        @GET("{path}")
        Call<Object> get(@Path(value = "path", encoded = true) String path);

        @GET("{path}")
        Call<Object> getWithHeaders(@Path(value = "path", encoded = true) String path,
                                    @HeaderMap Map<String, String> headers);

        @POST("{path}")
        Call<Object> post(@Path(value = "path", encoded = true) String path, @Body Object body);
    }

    public void get(String path, ResponseListener<T> listener, ErrorListener errorListener) {
        Call<Object> call = apiService.get(formatPath(path));
        executeCall(call, listener, errorListener);
    }

    public void get(String path, Map<String, String> headers,
                    ResponseListener<T> listener, ErrorListener errorListener) {
        Call<Object> call = apiService.getWithHeaders(formatPath(path), headers);
        executeCall(call, listener, errorListener);
    }

    public void post(String path, Object body, ResponseListener<T> listener, ErrorListener errorListener) {
        Call<Object> call = apiService.post(formatPath(path), body);
        executeCall(call, listener, errorListener);
    }

    private String formatPath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private void executeCall(Call<Object> call, final ResponseListener<T> listener, final ErrorListener errorListener) {
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = gson.toJson(response.body());
                        T result = gson.fromJson(jsonResponse, mClass);
                        if (listener != null) {
                            listener.onResponse(result);
                        }
                    } catch (JsonSyntaxException e) {
                        if (errorListener != null) {
                            errorListener.onError("JSON解析错误: " + e.getMessage());
                        }
                    } catch (Exception e) {
                        if (errorListener != null) {
                            errorListener.onError("数据解析错误: " + e.getMessage());
                        }
                    }
                } else {
                    if (errorListener != null) {
                        errorListener.onError("请求失败: " + response.code() + " - " + response.message());
                    }
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                if (errorListener != null) {
                    errorListener.onError("网络错误: " + t.getMessage());
                }
            }
        });
    }
}
