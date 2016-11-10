package br.com.mobi.redemicro.fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import br.com.mobi.redemicro.CompletarPerfilActivity;
import br.com.mobi.redemicro.LoginActivity;
import br.com.mobi.redemicro.MainActivity;
import br.com.mobi.redemicro.R;
import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.bo.UsuarioBo;
import br.com.mobi.redemicro.util.Constants;
import br.com.mobi.redemicro.util.HttpAsyncTask;


public class MeuPerfilFragment extends Fragment {

    private ImageView foto_perfil;
    private TextView nome_perfil, email_perfil, data_perfil, ddd_perfil, telefone_perfil, logradouro_perfil,
            numero_perfil, bairro_perfil, cidade_perfil,cep_perfil, estado_perfil, pais_perfil;
    private Usuario usuario;
    private UsuarioBo usuarioBO;
    private Context context;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_perfil, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ic_editar_perfil) {
            startActivity(new Intent(getContext(), CompletarPerfilActivity.class));

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meu_perfil, container, false);

        context=container.getContext();

        foto_perfil=(ImageView) view.findViewById(R.id.foto_perfil);
        nome_perfil=(TextView) view.findViewById(R.id.nome_perfil);
        email_perfil=(TextView) view.findViewById(R.id.email_perfil);
        data_perfil=(TextView) view.findViewById(R.id.data_perfil);
        ddd_perfil=(TextView) view.findViewById(R.id.ddd_perfil);
        telefone_perfil=(TextView) view.findViewById(R.id.telefone_perfil);
        logradouro_perfil=(TextView) view.findViewById(R.id.logradouro_perfil);
        numero_perfil=(TextView) view.findViewById(R.id.numero_perfil);
        bairro_perfil=(TextView) view.findViewById(R.id.bairro_perfil);
        cidade_perfil=(TextView) view.findViewById(R.id.cidade_perfil);
        cep_perfil=(TextView) view.findViewById(R.id.cep_perfil);
        estado_perfil=(TextView) view.findViewById(R.id.estado_perfil);
        pais_perfil=(TextView) view.findViewById(R.id.pais_perfil);

        usuarioBO=new UsuarioBo(context);
        usuario=usuarioBO.get(null,null);

            if (usuario.getFoto() != null) {
                Picasso.with(context).load(usuario.getFoto()).into(foto_perfil);
            }
            if (usuario.getNome() != null) {
                nome_perfil.setText(usuario.getNome());
            }
            if (usuario.getEmail() != null) {
                email_perfil.setText(usuario.getEmail());
            }
            if (usuario.getDatanascimento() != null) {
                data_perfil.setText(new SimpleDateFormat("dd/MM/yyyy").format(usuario.getDatanascimento()));
            }
            if (usuario.getDdd() != null) {
                ddd_perfil.setText(usuario.getDdd());
            }
            if (usuario.getTelefone() != null) {
                telefone_perfil.setText(usuario.getTelefone());
            }
            if (usuario.getLogradouro() != null) {
                logradouro_perfil.setText(usuario.getLogradouro());
            }
            if (usuario.getNumero() != null) {
                numero_perfil.setText(usuario.getNumero());
            }
            if (usuario.getBairro() != null) {
                bairro_perfil.setText(usuario.getBairro());
            }
            if (usuario.getCidade() != null) {
                cidade_perfil.setText(usuario.getCidade());
            }
            if (usuario.getCep() != null) {
                cep_perfil.setText(usuario.getCep());
            }
            if (usuario.getEstado() != null) {
                estado_perfil.setText(usuario.getEstado());
            }

            if (usuario.getPais() != null) {
                pais_perfil.setText(usuario.getPais());
            }
            ExibirPerfilTask exibirPerfilTask=new ExibirPerfilTask();
            exibirPerfilTask.execute();

        return  view;
    }

    private class ExibirPerfilTask extends AsyncTask<Void, Void, Boolean> {
        private boolean opc;

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getString(R.string.url_rest) + "usuario/exibir/"+usuario.getId(); //lembrar de pedir a gilberto o link

            try {
                HttpAsyncTask task = new HttpAsyncTask(url, context);

                try {
                    task.get(new HttpAsyncTask.FutureCallback() {
                        @Override
                        public void onCallback(Object jsonObject, int responseCode) throws JSONException {
                            JSONObject json = (JSONObject) jsonObject;

                            usuario.setId(json.getInt("id"));
                            usuario.setNome(json.getString("nome"));
                            usuario.setEmail(json.getString("email"));
                            usuario.setFoto(json.getString("foto"));
                            usuario.setIdgoogle(json.getString("idgoogle"));
                            usuario.setDdd(json.getString("ddd"));
                            usuario.setTelefone(json.getString("telefone"));
                            usuario.setLogradouro(json.getString("logradouro"));
                            usuario.setNumero(json.getString("numero"));
                            usuario.setBairro(json.getString("bairro"));
                            usuario.setCidade(json.getString("cidade"));
                            usuario.setCep(json.getString("cep"));
                            usuario.setEstado(json.getString("estado"));
                            usuario.setPais(json.getString("pais"));
                            if (!json.getString("datanascimento").equals("null")) {
                                try {
                                    usuario.setDatanascimento(new SimpleDateFormat("yyyy-MM-dd").parse(json.getString("datanascimento")));
                                    //new Date(json)
                                    //Log.i("chave", valor);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            usuarioBO.clean();
                            usuarioBO.insert(usuario);
                            opc = true;

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();// quando não tiver conexão com internet
                    opc = false;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return opc;
        }
    }

}
