package br.com.mobi.redemicro.fragment;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import br.com.mobi.redemicro.DetalheNoticiaActivity;
import br.com.mobi.redemicro.R;
import br.com.mobi.redemicro.adapter.NoticiasAdapter;
import br.com.mobi.redemicro.bean.Noticia;
import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.bo.NoticiaBo;
import br.com.mobi.redemicro.bo.UsuarioBo;
import br.com.mobi.redemicro.util.Constants;
import br.com.mobi.redemicro.util.HttpAsyncTask;


public class NoticiaFragment extends Fragment {
    private NoticiaTask noticiaTask;
    private NoticiaTask pesquisaTask;
    private NoticiasAdapter adapter;

    private List<Noticia> noticiaList;
    private ListView listViewNoticias;
    private NoticiaBo noticiaBO;

    private boolean hasMore;
    private int page;
    private boolean loading;
    private String query;
    private Context context;
    private Usuario usuario;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        hasMore = true;
        page = 0;
        query = "";
        context = getContext();
        noticiaBO = new NoticiaBo(context);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (noticiaTask != null && !noticiaTask.isCancelled()) {
            noticiaTask.cancel(true);
        }
        if (pesquisaTask != null && !pesquisaTask.isCancelled()) {
            pesquisaTask.cancel(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        UsuarioBo usuarioBO = new UsuarioBo(context);
        usuario = usuarioBO.get(null, null);
        getActivity().invalidateOptionsMenu();

        noticiaTask = new NoticiaTask();
        noticiaTask.execute(null, null, null);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_noticias, menu);
        SearchManager manager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.pesquisa).getActionView();
        searchView.setSearchableInfo(manager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setOnQueryTextListener(new SearchFiltro());

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_noticias, null);
        listViewNoticias = (ListView) view.findViewById(R.id.listViewNoticias);

        listViewNoticias.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Noticia noticia = noticiaList.get(position);

                SharedPreferences.Editor preferences = context.getSharedPreferences(Constants.APP, Context.MODE_PRIVATE).edit();
                preferences.putInt("NOTICIA", noticia.getIdNoticia());
                preferences.commit();

                startActivity(new Intent(context, DetalheNoticiaActivity.class));
            }
        });

        listViewNoticias.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (loading && hasMore) {
                    if ((visibleItemCount + firstVisibleItem) >= totalItemCount) {
                        loading = false;
                        page++;
                        noticiaTask = new NoticiaTask();
                        noticiaTask.execute(null, null, null);
                    }
                }
            }
        });

        return view;
    }

    protected class SearchFiltro implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            if (TextUtils.isEmpty(s) || s.length() >= 3) {
                query = s;
                page = 0;
                loading = false;
                hasMore = true;

                pesquisaTask = new NoticiaTask();
                pesquisaTask.execute(null, null, null);
            }
            return false;
        }
    }

    private class NoticiaTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            System.out.println("**********************"+page);
            try {
                String url = getString(R.string.url_rest) + "noticia/chamada";
                HttpAsyncTask task = new HttpAsyncTask(url, context);
                task.addParams("query", query);
                task.addParams("page", page);
                try {
                    task.post(new HttpAsyncTask.FutureCallback() {
                        @Override
                        public void onCallback(Object jsonObject, int responseCode) {
                            if (responseCode == 200) {
                                try {
                                    noticiaList = new ArrayList<>();
                                    JSONArray jsonArray = (JSONArray) jsonObject;

                                    if (jsonArray.length() == 0) {
                                        hasMore = false;
                                    }

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject json = jsonArray.getJSONObject(i);
                                        Noticia noticia = new Noticia();

                                        noticia.setIdNoticia(json.getInt("idNoticia"));
                                        noticia.setTitulo(json.getString("titulo"));
                                        System.out.println("**********************"+noticia.getTitulo());
                                        noticia.setChamada(json.getString("chamada"));
                                        noticia.setQtdViews(json.getInt("qtdViews"));
                                        noticia.setQtdCurtida(json.getInt("qtdCurtida"));
                                        noticia.setQtdComentarios(json.getInt("qtdComentarios"));

                                        noticia.setFoto(json.getString("foto"));

                                        noticiaList.add(noticia);
                                    }
                                    noticiaBO.clean();
                                    noticiaBO.insert(noticiaList);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!isCancelled()) {
                createAdapter();
            }
            super.onPostExecute(aVoid);
        }
    }

    private void createAdapter() {
        if (page == 0) {

            noticiaList = noticiaBO.list();
            adapter = new NoticiasAdapter(noticiaList, context);
            listViewNoticias.setAdapter(adapter);
        } else {
            for (Noticia noticia : noticiaList) {
                //synchronized (adapter) {
                if (!adapter.contains(noticia)) {
                    adapter.add(noticia);
                    //  }
                }
            }
            adapter.notifyDataSetChanged();
        }
        loading = true;
    }
}
