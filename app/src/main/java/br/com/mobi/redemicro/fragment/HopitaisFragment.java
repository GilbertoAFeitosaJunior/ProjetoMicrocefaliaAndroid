package br.com.mobi.redemicro.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import br.com.mobi.redemicro.HospitalActivity;
import br.com.mobi.redemicro.R;
import br.com.mobi.redemicro.adapter.CategoriaAdapter;
import br.com.mobi.redemicro.bean.Categoria;
import br.com.mobi.redemicro.util.Constants;
import br.com.mobi.redemicro.util.HttpAsyncTask;


public class HopitaisFragment extends Fragment {
    private List<Categoria> estadoList;
    List<Categoria> lista;
    private Context context;
    private boolean opc;
    private boolean loading;
    private CategoriaAdapter adapter;
    private ListView listViewEstados;

    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        context = getContext();
    }

    private void carregarLugares() {
        lista=new ArrayList<>();
        lista = estadoList;
        adapter = new CategoriaAdapter(lista,context);
        for(Categoria estado: lista){
            //synchronized (adapter) {
            if (!adapter.contains(estado)) {
                adapter.add(estado);
                //  }
            }
        }
        listViewEstados.setAdapter(adapter);
        this.listViewEstados.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,@Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_hopitais,null);
        EstadoTask estadoTask=new EstadoTask();
        estadoTask.execute();
        listViewEstados = (ListView) view.findViewById(R.id.listViewEstados);

        listViewEstados.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Categoria estado = estadoList.get(position);

                SharedPreferences.Editor preferences = context.getSharedPreferences(Constants.APP, Context.MODE_PRIVATE).edit();
                preferences.putInt("ESTADO", estado.getId());
                preferences.commit();

                startActivity(new Intent(context, HospitalActivity.class));
            }
        });
        return view;
    }
    private class EstadoTask extends AsyncTask<Void, Void, Void> {

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
                String url = getString(R.string.url_rest) + "hospital/estados";
                HttpAsyncTask task = new HttpAsyncTask(url, context);

                try {
                    task.get(new HttpAsyncTask.FutureCallback() {
                        @Override
                        public void onCallback(Object jsonObject, int responseCode) {
                            if (responseCode == 200) {
                                try {
                                    estadoList = new ArrayList<>();
                                    JSONArray jsonArray = (JSONArray) jsonObject;

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject json = jsonArray.getJSONObject(i);
                                        Categoria estado=new Categoria();

                                        estado.setId(json.getInt("id"));
                                        estado.setNome(json.getString("estado"));

                                        estadoList.add(estado);
                                        opc=true;
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    opc=false;

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
                createAdapter();
            }

        }
    }
    private void createAdapter() {
        carregarLugares();
        adapter = new CategoriaAdapter(estadoList, context);
        if (!opc) {
            Toast.makeText(context,R.string.erro_internet, Toast.LENGTH_SHORT).show();
        }else {
            for(Categoria estado: estadoList){
                //synchronized (adapter) {
                if (!adapter.contains(estado)) {
                    adapter.add(estado);
                    //  }
                }
            }
            listViewEstados.setAdapter(adapter);
        }

        loading = true;
    }
}
