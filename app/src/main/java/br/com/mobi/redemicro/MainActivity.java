package br.com.mobi.redemicro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.bo.UsuarioBo;
import br.com.mobi.redemicro.fragment.ForumFragment;
import br.com.mobi.redemicro.fragment.HopitaisFragment;
import br.com.mobi.redemicro.fragment.MeuPerfilFragment;
import br.com.mobi.redemicro.fragment.NoticiaFragment;
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

        getSupportFragmentManager().beginTransaction().replace(R.id.container, new NoticiaFragment()).commit();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main);
        exibirViewPerfil();
        navigationView.setItemIconTintList(null);

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
            case R.id.nav_noticias:
                fragment=new NoticiaFragment();
                getSupportActionBar().setSubtitle(R.string.noticias);

                break;
            case R.id.nav_forum:
                fragment=new ForumFragment();
                getSupportActionBar().setSubtitle(R.string.title_activity_forum);
                break;
            case R.id.nav_hospital:
                fragment=new HopitaisFragment();
                getSupportActionBar().setSubtitle(R.string.title_activity_hospital);
                break;
            case R.id.nav_page:
                String url = getString(R.string.link_page);
                Intent it;
                try {
                    MainActivity.this.getPackageManager().getPackageInfo("com.facebook.katana", 0);
                    it=new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/585550184977710"));
                } catch (Exception e) {
                    it= new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                }
                startActivity(it);
                break;
            case R.id.nav_share:
                String urlapp = getString(R.string.linkapp);
                String mensagem = getString(R.string.compartilhar_main) + "\n" + urlapp;
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, mensagem);
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, mensagem));
                break;
            case R.id.nav_avaliar:
                Toast.makeText(MainActivity.this,"Ainda não Disponível na loja",Toast.LENGTH_SHORT).show();
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
