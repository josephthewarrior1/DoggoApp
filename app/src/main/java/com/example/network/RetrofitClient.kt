package com.example.doggo.network

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object RetrofitClient {
    private const val BASE_URL = "https://doggo-be.vercel.app/"

    // ✅ FIX: Buat mutable context
    private var appContext: Context? = null

    // ✅ TAMBAH: Init function yang WAJIB dipanggil
    fun init(context: Context) {
        appContext = context.applicationContext
        Log.d("RetrofitClient", "✅ RetrofitClient initialized")
    }

    // ✅ FIX: Lazy initialization dengan proper context check
    val instance: ApiService by lazy {
        if (appContext == null) {
            throw IllegalStateException("RetrofitClient not initialized! Call RetrofitClient.init(context) in Application class")
        }

        // ✅ TAMBAH: Logging interceptor untuk debugging
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("API", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        Log.d("RetrofitClient", "✅ Retrofit instance created")
        retrofit.create(ApiService::class.java)
    }

    // ✅ FIX: Auth Interceptor dengan proper context handling
    private class AuthInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()

            // ✅ FIX: Gunakan appContext yang sudah di-init
            val token = appContext?.getSharedPreferences("doggo_pref", Context.MODE_PRIVATE)
                ?.getString("user_token", null)

            Log.d("AuthInterceptor", "🔑 Token: ${if (token != null) "EXISTS" else "NULL"}")

            return if (!token.isNullOrEmpty()) {
                // Add authorization header
                val newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                Log.d("AuthInterceptor", "✅ Added Authorization header")
                chain.proceed(newRequest)
            } else {
                // No token, proceed without auth header
                Log.d("AuthInterceptor", "⚠️ No token found, proceeding without auth")
                chain.proceed(originalRequest)
            }
        }
    }
}