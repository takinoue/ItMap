package cc.atte.itmap

import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ItMapApiService {
    @POST("{account}.json")
    //@Headers("Content-Type: application/json")
    fun postData(
        @Path("account") account: String,
        @Body body: ItMapApiModel.Request
    ): Call<ItMapApiModel.Response>

    companion object {
        const val BASE_URL = "https://www.atte.cc/itmap/"

        fun create(baseURL: String = BASE_URL): ItMapApiService {
            val gsonNulls = GsonBuilder().serializeNulls().create()
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gsonNulls))
                .baseUrl(baseURL)
                .build()

            return retrofit.create(ItMapApiService::class.java)
        }
    }
}