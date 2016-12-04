package br.com.mobi.redemicro.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import br.com.mobi.redemicro.DetalheNoticiaActivity;
import br.com.mobi.redemicro.ForumActivity;
import br.com.mobi.redemicro.R;
import br.com.mobi.redemicro.adapter.CategoriaAdapter;
import br.com.mobi.redemicro.bean.Categoria;
import br.com.mobi.redemicro.util.Constants;
import br.com.mobi.redemicro.util.HttpAsyncTask;

public class ForumFragment extends Fragment {

    private List<Categoria> forumList;
    private Context context;
    private boolean opc;
    private boolean loading;
    private CategoriaAdapter adapter;
    private ListView listViewForum;

    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        context=getContext();
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forum, null);

        listViewForum = (ListView) view.findViewById(R.id.listViewForum);

        ForumTask forumTask=new ForumTask();
        forumTask.execute(null,null,null);

        listViewForum.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Categoria forum = forumList.get(position);

                SharedPreferences.Editor preferences = context.getSharedPreferences(Constants.APP, Context.MODE_PRIVATE).edit();
                preferences.putInt("FORUM", forum.getId());
                preferences.commit();

                startActivity(new Intent(context, ForumActivity.class));
            }
        });
        return view;
    }
    private class ForumTask extends AsyncTask<Void, Void, Void> {

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
                String url = getString(R.string.url_rest) + "forum/categoria";
                HttpAsyncTask task = new HttpAsyncTask(url, context);

                try {
                    task.get(new HttpAsyncTask.FutureCallback() {
                        @Override
                        public void onCallback(Object jsonObject, int responseCode) {
                            if (responseCode == 200) {
                                try {
                                    forumList = new ArrayList<>();
                                    JSONArray jsonArray = (JSONArray) jsonObject;

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject json = jsonArray.getJSONObject(i);
                                        Categoria forum=new Categoria();

                                        forum.setId(json.getInt("id"));
                                        forum.setNome(json.getString("nome"));

                                        forumList.add(forum);
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
        adapter = new CategoriaAdapter(forumList, context);
        if (!opc) {
            Toast.makeText(context,R.string.erro_internet, Toast.LENGTH_SHORT).show();
        }else {
            for(Categoria forum: forumList){
                //synchronized (adapter) {
                if (!adapter.contains(forum)) {
                    adapter.add(forum);
                    //  }
                }
            }
            listViewForum.setAdapter(adapter);
        }

        loading = true;
    }
}
