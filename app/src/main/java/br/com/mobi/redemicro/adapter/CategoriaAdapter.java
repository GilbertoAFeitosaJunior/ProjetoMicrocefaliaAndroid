package br.com.mobi.redemicro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import br.com.mobi.redemicro.R;
import br.com.mobi.redemicro.bean.Categoria;

/**
 * Created by Allesson on 02/12/2016.
 */

public class CategoriaAdapter extends BaseAdapter {
    private List<Categoria> lista;
    private LayoutInflater layoutInflater;
    private Context context;

    public CategoriaAdapter(List<Categoria> lista, Context context) {

        this.lista = lista;
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public Object getItem(int position) {
        return lista.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.layout_categoria_adapter, null);

        Categoria categoria = lista.get(position);

        TextView nome = (TextView) view.findViewById(R.id.nomeCategoria);
        nome.setText(categoria.getNome());

        return view;
    }

    @Override
    public int getCount() {
        return lista == null ? 0 : lista.size();
    }

    public void add(Categoria categoria) {
        lista.add(categoria);
    }

    public boolean contains(Categoria categoria) {
        if (lista == null) return false;

        for (Categoria entity : lista) {
            if (entity.getId() == categoria.getId()) {
                return true;
            }
        }
        return false;
    }
}
