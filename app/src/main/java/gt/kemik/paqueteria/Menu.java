package gt.kemik.paqueteria;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gt.kemik.paqueteria.bean.Usuario;
import gt.kemik.paqueteria.volley.VolleyController;

public class Menu extends AppCompatActivity {

    //LOG TAG
    private static final String TAG = "MENU";



    //UI CONTROLLERS
    @Bind(R.id.btnEntregar)
    Button btnEntregar;
    @Bind(R.id.btnRecibir)
    Button btnRecibir;
    @Bind(R.id.btnIntentar)
    Button btnIntentar;
    @Bind(R.id.btnDevolver)
    Button btnDevolver;
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    //USER LOGGED
    Usuario usuario;

    //METHOD TO DO
    // 0 is null; 1 is recibir; 2 is entregar; 3 is intentar; 4 is devolver
    private int method = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        ButterKnife.bind(this); //BIND BUTTERKNIFE

        //SET USER LOGGED
        Intent intent = getIntent();
        usuario = (Usuario) intent.getSerializableExtra("usuario");

        //SET ACTION BAR
        setSupportActionBar(toolbar);
    }

    //SET MENU OPTIONS
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //MENU LISTENER ACTIONS
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.itemLogout:
                Log.d(TAG, "onOptionsItemSelected: clear");
                emptyLoginData();
                goLogin();
                return true;
            default:
                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onOptionsItemSelected: " + item);
                return super.onOptionsItemSelected(item);
        }
    }

    //ON CLICK RECIBIR
    @OnClick(R.id.btnRecibir)
    public void handleRecibir(){
        //SET METHOD FOR POST
        method = 1;
        scan("RECIBIR PRODUCTO");
    }

    //ON CLICK ENTREGAR
    @OnClick(R.id.btnEntregar)
    public void handleEntregar(){
        //SET METHOD FOR POST
        method = 2;
        scan("ENTREGAR PRODUCTO");
    }

    //ON CLICK INTENTAR
    @OnClick(R.id.btnIntentar)
    public void handleIntentar(){
        method = 3;
        scan("FALLO EN ENTREGA");
    }

    //ON CLICK DEVOLVER
    @OnClick(R.id.btnDevolver)
    public void handleDevolver(){
        method = 4;
        scan("DEVOLUCIÓN");
    }

    //EXECUTE AFTER SCAN()
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null){
            if (result.getContents()==null){
                Log.d(TAG, "onActivityResult: El usuario canceló el scanéo");
            } else {
                Log.d(TAG, "onActivityResult: " + result.getContents());
                doRequest(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //SCAN QRCODE
    private void scan(String prompt){
        final Activity activity = this;
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt(prompt);
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    //Do the reques for methods
    private void doRequest(String orden){
        if (method == 1){
            //EXECUTE RECIBIR PEDIDO
            doPost(orden, "Recolectado por " + usuario.getmUsuario(), VolleyController.RECIBIR_URL);
        } else if (method == 2){
            //EXECUTE ENTREGAR PEDIDO
            initComment(orden, "Entregado por " + usuario.getmUsuario() + " a", VolleyController.ENTREGAR_URL);
        } else if (method == 3){
            //GET COMMENT
            initComment(orden, usuario.getmUsuario(), VolleyController.INTENTAR_URL);
        } else if (method == 4){
            //EXECUTE DEVOLVER PRODUCTO
            initComment(orden, "Se devuelve el pedido, " + usuario.getmUsuario(), VolleyController.DEVOLVER_URL);
        }
    }

    //DO HTTP POST REQUEST FOR SET STATUS PACKAGE
    private void doPost(String orden, String comentario, String url){
        Map<String, String> params = new HashMap<String, String>();
        //PREPARE BODY FOR POST
        params.put("token", usuario.getmToken());
        params.put("idPedido", orden);
        params.put("idUsuario", Integer.toString(usuario.getmIdUsuario()));
        params.put("descripcion", comentario);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,
                new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getString("status").equals("200")){
                        Toast.makeText(getApplicationContext(), getString(R.string.gt_kemik_succes), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Is 200: " + response);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.gt_kemik_error_genericpost), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Not 200: " + response);
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "onResponse: Error en parsing: " + e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: Error de conexión");
                Toast.makeText(getApplicationContext(), getString(R.string.gt_kemik_error_connection), Toast.LENGTH_LONG).show();
            }
        });
        //DO HTTP REQUEST
        VolleyController.getmInstance(getApplicationContext()).addToRequestQueque(request);
    }

    //RETURN TO LOGIN ACTIVITY
    private void goLogin(){
        Intent intent = new Intent(Menu.this, MainActivity.class);
        startActivity(intent);
    }

    //EMPTY LOGIN DATA
    private void emptyLoginData(){
        SharedPreferences sharPref = getSharedPreferences(MainActivity.SESSION_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharPref.edit();
        editor.clear();
        editor.commit();
    }

    //GET COMMENTS FOR EVENTS
    private void initComment(final String orden, final String comment, final String url){
        View view = (LayoutInflater.from(Menu.this)).inflate(R.layout.user_input, null);

        final EditText userInput = (EditText) view.findViewById(R.id.userInputEditText);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(Menu.this);
        alertBuilder.setView(view);
        alertBuilder.setCancelable(false)
                .setPositiveButton(getApplicationContext().getText(R.string.gt_kemik_paqueteria_userinput_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //EXECUTE POST
                        doPost(orden, comment + ": " + userInput.getText().toString(), url);
                    }
                })
                .setNegativeButton(getApplicationContext().getText(R.string.gt_kemik_paqueteria_userinput_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //EXECUTE POST
                        doPost(orden, comment , url);
                    }
                });
        Dialog dialog = alertBuilder.create();
        dialog.show();
    }

}
