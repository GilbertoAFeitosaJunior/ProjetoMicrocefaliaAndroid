package br.com.mobi.redemicro.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.mobi.redemicro.ForumActivity;
import br.com.mobi.redemicro.ForumMsgActivity;
import br.com.mobi.redemicro.LoginActivity;
import br.com.mobi.redemicro.R;
import br.com.mobi.redemicro.adapter.MensagemAdapter;
import br.com.mobi.redemicro.adapter.TopicosForumAdapter;
import br.com.mobi.redemicro.bean.Mensagem;
import br.com.mobi.redemicro.bean.TopicosForum;
import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.bo.UsuarioBo;
import br.com.mobi.redemicro.util.Constants;
import br.com.mobi.redemicro.util.HttpAsyncTask;

public class MsgFragment extends Fragment {

    private String msg = "";
    private UsuarioBo usuarioBO;
    private Usuario usuario;

    private int forumId;
    ListView listViewMensagem;
    ProgressDialog progressDialog;
    List<Mensagem> mensagemList;
    boolean opc;
    boolean loading;
    MensagemAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usuarioBO = new UsuarioBo(getContext());
        usuario = usuarioBO.get(null, null);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_msg, container, false);

        MensagemTask mensagemTask = new MensagemTask();
        mensagemTask.execute();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_mensagem, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_mensagem) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewHospital = layoutInflater.inflate(R.layout.msg_alert_dialog, null);
            final EditText edtMsg = (EditText) viewHospital.findViewById(R.id.edt_msg);

            usuario = usuarioBO.get(null, null);
            if (usuario == null) {
                FazerLogin();
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setView(viewHospital);

                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Comentar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        msg = edtMsg.getText().toString();
                        if (msg.equals("")) {
                            Toast.makeText(getContext(), getString(R.string.toast_completar), Toast.LENGTH_SHORT).show();
                        } else {
                            NewMsgTask newMsgTask=new NewMsgTask();
                            newMsgTask.execute(null,null,null);

                        }
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public class NewMsgTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String url = getString(R.string.url_rest) + "forum/mensagem";

            try {
                HttpAsyncTask httpAsyncTask = new HttpAsyncTask(url, getContext());
                httpAsyncTask.addParams("idUsuario", usuario.getId());
                httpAsyncTask.addParams("idTopico", forumId);
                httpAsyncTask.addParams("mensagem",msg);

                httpAsyncTask.post(new HttpAsyncTask.FutureCallback() {
                    @Override
                    public void onCallback(Object jsonObject, int responseCode) {
                        switch (responseCode) {
                            case 200:
                                Toast.makeText(getContext(),"Ok",Toast.LENGTH_SHORT).show();
                                break;
                            case 400:
                                Toast.makeText(getContext(),"erro",Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

        public void FazerLogin() {
            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
            alertDialog.setMessage(getString(R.string.deseja_fazer_login));

            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Sim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(getContext(), LoginActivity.class));
                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Não", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            alertDialog.show();
        }

    private class MensagemTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getString(R.string.carregando));
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                SharedPreferences preferences = getContext().getSharedPreferences(Constants.APP, Context.MODE_PRIVATE);
                forumId = preferences.getInt("FORUMTOPICO", 0);
                String url = getString(R.string.url_rest) + "forum/mensagem/" + forumId;
                HttpAsyncTask task = new HttpAsyncTask(url, getContext());

                try {
                    task.get(new HttpAsyncTask.FutureCallback() {
                        @Override
                        public void onCallback(Object jsonObject, int responseCode) {
                            if (responseCode == 200) {
                                try {
                                    mensagemList = new ArrayList<>();
                                    JSONArray jsonArray = (JSONArray) jsonObject;

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject json = jsonArray.getJSONObject(i);
                                        Mensagem mensagem = new Mensagem();
                                        mensagem.setNome(json.getString("nome"));
                                        mensagem.setFoto(json.getString("foto"));
                                        mensagem.setMensagem(json.getString("mensagem"));
                                        mensagem.setRevisado(json.getBoolean("revisado"));
                                        try {
                                            mensagem.setData(new Date(json.getLong("data")));
                                        } catch (Exception e) {
                                            e.getMessage();
                                        }

                                        mensagemList.add(mensagem);
                                        opc = true;
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    opc = false;

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (!isCancelled()) {
                createAdapterMsg();
            }

        }
    }

    private void createAdapterMsg() {
        listViewMensagem = (ListView) getView().findViewById(R.id.listViewMensagem);
        adapter = new MensagemAdapter(mensagemList, getContext());
        if (!opc) {
            Toast.makeText(getContext(), R.string.erro_internet, Toast.LENGTH_SHORT).show();
        } else {
            listViewMensagem.setAdapter(adapter);
        }

        loading = true;
    }

}
