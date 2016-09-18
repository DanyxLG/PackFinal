package jmcoydxlg.packbooks;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.BaseMenuPresenter;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;
import java.security.cert.CertPathValidator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import static java.util.concurrent.TimeUnit.SECONDS;
import org.apache.commons.codec.binary.Base64;


import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import java.io.File;

public class CrearCuenta extends AppCompatActivity implements Validator.ValidationListener {

    private static String APP_DIRECTORY = "MyPictureApp/";
    private static String MEDIA_DIRECTORY = APP_DIRECTORY + "PictureApp";

    //Datos del usuario

    @NotEmpty(message = "Escriba su nombre")EditText registrarNombreET;

    @NotEmpty(message = "Escriba su Usuario")EditText registrarUsuarioET;

    @Email(message = "Email incorrecto")EditText registrarCorreoET;

    @Password(min = 8, scheme = Password.Scheme.ALPHA_NUMERIC_MIXED_CASE_SYMBOLS, message = "La contraseña debe tener de al menos 8 caracteres, un numero y un simbolo ")
    EditText registrarContrasenaET;

    @ConfirmPassword(message = "Contraseña no coincide")EditText registrarRepContrasenaET;

    @NotEmpty(message = "No puede estar vacio")EditText registrarTelefonoET;

    Validator Validacion;

    private final int MY_PERMISSIONS = 100;
    private final int PHOTO_CODE = 200;
    private final int SELECT_PICTURE = 300;

    private ImageButton registrarFotoPerfilIB;
    private LinearLayout LayoutCrearCuenta;
    private String mPath;

    public String usuarios, foto;
    public boolean eligioFoto=false;

    DBHelper dbSQLITE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cuenta);

        dbSQLITE = new DBHelper(this);

        //Encuentra el imagenButton
        registrarFotoPerfilIB = (ImageButton) findViewById(R.id.registrar_imagenperfilIB);
        LayoutCrearCuenta = (LinearLayout) findViewById(R.id.LayoutCrearCuenta);

        //Guarda los datos del usuario
        registrarNombreET = (EditText) findViewById(R.id.registrar_nombreET);
        registrarUsuarioET = (EditText) findViewById(R.id.registrar_usuarioET);
        registrarContrasenaET = (EditText) findViewById(R.id.registrar_contrasenaET);
        registrarRepContrasenaET = (EditText) findViewById(R.id.registrar_repcontrasenaET);
        registrarCorreoET = (EditText) findViewById(R.id.registrar_correoET);
        registrarTelefonoET = (EditText) findViewById(R.id.registrar_telefonoET);

        //Asigna los permisos
        if (mayRequestStoragePermission())
            registrarFotoPerfilIB.setEnabled(true);
        else
            registrarFotoPerfilIB.setEnabled(false);

        registrarFotoPerfilIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MostrarOpciones();
            }
        });

        Validacion = new Validator(this);
        Validacion.setValidationListener(this);
    }

    private void MostrarOpciones() {
        final CharSequence[] opcion = {"Tomar foto", "Tomar de galería", "Cancelar"};
        final AlertDialog.Builder Builder = new AlertDialog.Builder(CrearCuenta.this);
        Builder.setTitle("Elige una opción");
        Builder.setItems(opcion, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int wich) {
                if (opcion[wich] == "Tomar foto") {
                    AbrirCamera();
                } else if (opcion[wich] == "Tomar de galería") {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("images/*");
                    startActivityForResult(intent.createChooser(intent, "Selecciona app de imagen"), SELECT_PICTURE);
                } else {
                    dialog.dismiss();
                }
            }
        });
        Builder.show();
    }

    private void AbrirCamera() {
        File archivo = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        boolean DirectorioCreado = archivo.exists();
        if (!DirectorioCreado) {
            DirectorioCreado = archivo.mkdirs();
        }
        if (DirectorioCreado) {
            Long timestamp = System.currentTimeMillis() / 1000;
            String NombreImagen = timestamp.toString() + ".jpg";
            mPath = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY
                    + File.separator + NombreImagen;
            File NuevoArchivo = new File(mPath);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(NuevoArchivo));
            startActivityForResult(intent, PHOTO_CODE);
        }
    }

    private boolean mayRequestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        if ((checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED))
            return true;
        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) || (shouldShowRequestPermissionRationale(CAMERA))) {
            Snackbar.make(LayoutCrearCuenta, "Los permisos son necesarios para poder usar la aplicación",
                    Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
                }
            });
        } else {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSIONS);
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path", mPath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mPath = savedInstanceState.getString("file_path");
    }

    @Override
    protected void onActivityResult(int solicitud, int resultado, Intent dato) {
        super.onActivityResult(solicitud, resultado, dato);
        if (resultado == RESULT_OK) {
            switch (solicitud) {
                case PHOTO_CODE:
                    MediaScannerConnection.scanFile(this,
                            new String[]{mPath}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned " + path + ":");
                                    Log.i("ExternalStorage", "-> Uri = " + uri);
                                }
                            });
                    Bitmap bitmap = BitmapFactory.decodeFile(mPath);
                    registrarFotoPerfilIB.setImageBitmap(bitmap);
                    foto = mPath;
                    eligioFoto=true;
                    break;
                case SELECT_PICTURE:
                    Uri path = dato.getData();
                    registrarFotoPerfilIB.setImageURI(path);
                    foto = path.toString();
                    System.out.println(foto);
                    eligioFoto=true;
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(CrearCuenta.this, "Permisos aceptados", Toast.LENGTH_SHORT).show();
                registrarFotoPerfilIB.setEnabled(true);
            }
        } else {
            MostrarExplicacion();
        }
    }

    private void MostrarExplicacion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CrearCuenta.this);
        builder.setTitle("Permisos denegados");
        builder.setMessage("Para usar las funciones de la app necesitas aceptar los permisos");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.show();
    }

    @Override
    public void onValidationSucceeded() {
        Toast.makeText(CrearCuenta.this, "Los campos se guardaron exitosamente", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        }


    }

    public void Registrarse(View v) {
        Validacion.validate();
    }

    public void ingresarUsuario(View v) {
        boolean todoBien = false;
        File fotoPerfilVariable;
        String foto64;
        registrarNombreET = (EditText) findViewById(R.id.registrar_nombreET);
        registrarUsuarioET = (EditText) findViewById(R.id.registrar_usuarioET);
        registrarContrasenaET = (EditText) findViewById(R.id.registrar_contrasenaET);
        registrarRepContrasenaET = (EditText) findViewById(R.id.registrar_repcontrasenaET);
        registrarCorreoET = (EditText) findViewById(R.id.registrar_correoET);
        registrarTelefonoET = (EditText) findViewById(R.id.registrar_telefonoET);
        registrarFotoPerfilIB = (ImageButton) findViewById(R.id.registrar_imagenperfilIB);
        if (registrarContrasenaET == registrarRepContrasenaET) {
            todoBien = true;
        }
        boolean estaInsertado =
                dbSQLITE.insertar
                        (registrarNombreET.getText().toString(),
                                registrarUsuarioET.getText().toString(),
                                registrarContrasenaET.getText().toString(),
                                registrarCorreoET.getText().toString(),
                                registrarTelefonoET.getText().toString(),
                                "proximamente"
                                );
        if(estaInsertado){
            Toast.makeText(CrearCuenta.this,"Registrado con éxito",Toast.LENGTH_LONG).show();
            Intent Login = new Intent(getApplicationContext(),LoginActivity.class );
            startActivity(Login);
        }
        else{
            Toast.makeText(CrearCuenta.this,"Lo sentimos ocurrió un error",Toast.LENGTH_LONG).show();
        }

    }
}

