package gt.kemik.paqueteria;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gt.kemik.paqueteria.bean.Usuario;
import gt.kemik.paqueteria.volley.VolleyController;

public class MainActivity extends AppCompatActivity {

    //UI CONTROLLERS
    @Bind(R.id.usuarioText)
    EditText usuarioText;
    @Bind(R.id.contrasenaText)
    EditText  contrasenaText;


    public static final String SESSION_PREFERENCES = "Sessions";


    //LOG TAG
    private static final String TAG = "LOGIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this); //BIND BUTTERKNIFE
        //SHARED PREFS FOR SESSIONS SAVES
        final SharedPreferences sharprefs = getSharedPreferences(SESSION_PREFERENCES, Context.MODE_PRIVATE);

        //VERIFY IF USER IS LOGGED BEFORE
        if(isLogged()){
            Log.d(TAG, "onCreate: " + getSession().toString());
            goMenu(getSession());
        }
    }

    //ON loginBoton CLICK
    @OnClick(R.id.loginBoton)
    public void handleLogin(){
            doLogin();
    }

    //VERIFY IF AN EDIT TEXT IS EMPTY
    private boolean isEmpty(EditText input){
        if(input.getText().toString().matches("")){
            return true;
        }else{
            return false;
        }
    }

    //VERIFY IF ALL THE INPUTS ARE OK
    private boolean isCorrectLoginInput(){
        String response = "";
        if(isEmpty(usuarioText)){
            response += "El usuario está vacío";
        }
        if(isEmpty(contrasenaText)){
            response += "\n La contraseña está vacía";
        }
        if(!response.equals("")){
            Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    //DO POST FOR LOGIN
    private void doLogin(){
        //VERIFY IF ALL THE FIELDS ARE CORRECT
        if (isCorrectLoginInput()){
            //SET PARAMS FOR POST REQUEST
            Map<String, String> params = new HashMap<String, String>();
            params.put("usuario", usuarioText.getText().toString());
            params.put("contrasena", contrasenaText.getText().toString());
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, VolleyController.LOGIN_URL,
                    new JSONObject(params), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //ON SUCCESS
                    try {
                        //Lista con usuario que coinciden en usuario y contraseña
                        JSONArray responseArray = response.getJSONArray("user");
                        if (responseArray.length()>0){  //Revisa si existe algún usuario
                            JSONObject user = responseArray.getJSONObject(0);
                            //Si existe crea un objeto usuario para guardar la sesión
                            Usuario usuario = new Usuario(
                                    user.getString("nombre"),
                                    response.getString("token"),
                                    user.getInt("idUsuario"));
                            //Guarda la sesion
                            SharedPreferences sharpref = getSharedPreferences(SESSION_PREFERENCES, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharpref.edit();
                            editor.putBoolean("isLogged", true);
                            editor.putString("nombre", usuario.getmUsuario());
                            editor.putString("token", usuario.getmToken());
                            editor.putInt("idUsuario", usuario.getmIdUsuario());
                            editor.commit();
                            Log.d(TAG, "onResponse: " + usuario.toString());
                            goMenu(usuario);
                        } else {
                            //Si no existe muestra mensaje de usario y contraseña incorrectos
                            Toast.makeText(getApplicationContext(), getString(R.string.gt_kemik_error_userpassword), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Log.d(TAG, "Error parsinsing: " + e);
                        Toast.makeText(getApplicationContext(), getString(R.string.gt_kemik_error_genericerrorconnection), Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //ON ERROR
                    Toast.makeText(getApplicationContext(), getString(R.string.gt_kemik_error_connection), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onErrorResponse: " + error);
                }
            });
            //DO HTTP REQUEST
            VolleyController.getmInstance(getApplicationContext()).addToRequestQueque(request);
        }
    }

    // Return true if the user is logged before.
    private boolean isLogged(){
        SharedPreferences sharpref = getSharedPreferences(SESSION_PREFERENCES,Context.MODE_PRIVATE);
        return sharpref.getBoolean("isLogged", false);
    }

    //Return user in Session
    private Usuario getSession(){
        SharedPreferences sharpref = getSharedPreferences(SESSION_PREFERENCES, Context.MODE_PRIVATE);
        Usuario usuario = new Usuario(sharpref.getString("nombre", "none"),
                sharpref.getString("token", "none"),
                sharpref.getInt("idUusario", 2));
        return usuario;
    }

    //PASS TO MENU
    private void goMenu(Usuario usuario){
        Intent intent = new Intent(MainActivity.this, Menu.class);
        intent.putExtra("usuario", usuario);
        startActivity(intent);
    }
}
