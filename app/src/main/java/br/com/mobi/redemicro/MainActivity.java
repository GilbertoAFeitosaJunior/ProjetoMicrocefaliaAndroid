package br.com.mobi.redemicro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.bo.UsuarioBo;
import br.com.mobi.redemicro.fragment.MeuPerfilFragment;
import br.com.mobi.redemicro.util.Constants;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SharedPreferences sharedPreferences;
    private UsuarioBo usuarioBo;
    private View headerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        sharedPreferences = getSharedPreferences(Constants.PREFERENCES, Context.MODE_PRIVATE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        exibirViewPerfil();


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        Fragment fragment = null;

        switch (id) {
            case R.id.nav_perfil:
                if (sharedPreferences.getString(Constants.ID_LOGIN, "").equals("OK")) {
                    fragment = new MeuPerfilFragment();
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                    MainActivity.this.finish();
                }
                break;
            case R.id.nav_gallery:
                startActivity(new Intent(this, CompletarPerfilActivity.class));
                break;
        }


        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void exibirViewPerfil() {
        usuarioBo = new UsuarioBo(this);
        Usuario usuario = usuarioBo.get(null, null);
        ImageView imageView = (ImageView) headerLayout.findViewById(R.id.imageViewPerfil);
        TextView nomePerfil = (TextView) headerLayout.findViewById(R.id.textViewPerfil);

        if(usuario!=null){
            Picasso.with(this).load(usuario.getFoto()).placeholder(android.R.drawable.ic_menu_camera).into(imageView);
            nomePerfil.setText(usuario.getNome());
        }else{
            Picasso.with(this).load(R.mipmap.ic_launcher).placeholder(android.R.drawable.ic_menu_camera).into(imageView);
            nomePerfil.setText(getString(R.string.app_name));
        }

    }
}
