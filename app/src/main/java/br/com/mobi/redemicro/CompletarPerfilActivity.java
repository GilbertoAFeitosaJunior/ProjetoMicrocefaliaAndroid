package br.com.mobi.redemicro;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.bo.UsuarioBo;


public class CompletarPerfilActivity extends AppCompatActivity {

    private ImageView imageView;
    private UsuarioBo usuarioBo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completar_perfil);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = (ImageView) findViewById(R.id.imageView);
        usuarioBo = new UsuarioBo(this);
        Usuario usuario = usuarioBo.get(null, null);



        if (TextUtils.isEmpty(usuario.getFoto())) {
            Picasso.with(this).load(usuario.getFoto()).placeholder(android.R.drawable.ic_menu_camera).into(imageView);
        }


    }

}
