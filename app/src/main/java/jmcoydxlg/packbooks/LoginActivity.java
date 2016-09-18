
package jmcoydxlg.packbooks;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Password;

public class LoginActivity extends AppCompatActivity {
    DBHelper dbSQLITE;
    @Email(message = "Email inválido")EditText loginLoginET;
    EditText loginPasswordET;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        dbSQLITE = new DBHelper(this);
        loginLoginET=(EditText)findViewById(R.id.login_correoET);
        loginPasswordET=(EditText)findViewById(R.id.login_contrasenaET);
    }
    public void CrearCuenta (View v){
        Intent SignUp = new Intent(getApplicationContext(),CrearCuenta.class );
        startActivity(SignUp);
    }
    public void IniciarSesion(View v){
        Cursor res = dbSQLITE.selectVerTodos();
        if(res.getCount() == 0){
            Toast.makeText(LoginActivity.this, "No esta registrado", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean estado=false;
        int registro=0;
        while(res.moveToNext()){
            if(res.getString(4).equals(loginLoginET.getText().toString())&& res.getString(3).equals(loginPasswordET.getText().toString())){
                AdminLibros();
                registro=1;
            }
            else{
                estado=false;
            }
        }
        if(registro==1){
            Toast.makeText(LoginActivity.this, "Bienvenido", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(LoginActivity.this, "Datos erroneos", Toast.LENGTH_SHORT).show();
        }

    }
    public void AdminLibros (){
        Intent libros = new Intent(getApplicationContext(),MainActivityPackB.class );
        startActivity(libros);
    }
}
