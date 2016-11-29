package gt.kemik.paqueteria.volley;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;


/**
 * Created by HP240 on 22/11/2016.
 */

public class VolleyController extends Application {
    private static VolleyController mInstance;
    private RequestQueue mRequestQueque;
    private ImageLoader mImageLoader;
    private static Context mCtx;
    private static String SERVIDOR_URL = "http://paquetes-93539.onmodulus.net";
    private static String API_VERSION = "/api/v1";
    public static String LOGIN_URL = SERVIDOR_URL + API_VERSION + "/usuario/login";
    public static String RECIBIR_URL = SERVIDOR_URL + API_VERSION + "/pedido/recibir";
    public static String ENTREGAR_URL = SERVIDOR_URL + API_VERSION + "/pedido/entregar";
    public static String INTENTAR_URL = SERVIDOR_URL + API_VERSION + "/pedido/intento";
    public static String DEVOLVER_URL = SERVIDOR_URL + API_VERSION + "/pedido/devolucion";

    private VolleyController(Context context){
        mCtx = context;
        mRequestQueque = getRequestQueque();

        mImageLoader = new ImageLoader(mRequestQueque,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(20);
                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized VolleyController getmInstance(Context context){
        if(mInstance==null){
            mInstance = new VolleyController(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueque(){
        if(mRequestQueque == null){
            mRequestQueque = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueque;
    }

    public <T> void addToRequestQueque(Request<T> req){
        getRequestQueque().add(req);
    }

    public ImageLoader getmImageLoader(){
        return mImageLoader;
    }
}
