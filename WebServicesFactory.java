import com.example.mystashapp.mystashappproject.Constant_util;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WebServicesFactory {
    private static WebService instance;

    public static WebService getInstance() {
        if (instance == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constant_util.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            instance = retrofit.create(WebService.class);
        }
        return instance;
    }
}
