package br.com.mobi.redemicro.fragment;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import br.com.mobi.redemicro.R;
import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.bo.UsuarioBo;
import br.com.mobi.redemicro.util.Constants;


public class ExibirPerfilFragment extends Fragment {

    private ImageView foto_perfil;
    private TextView nome_perfil, email_perfil, phone_perfil, logradouro_perfil,
            numero_perfil, bairro_perfil, cidade_perfil, estado_perfil, pais_perfil;
    private Usuario usuario;
    private UsuarioBo usuarioBo;
    Context context;


    private OnFragmentInteractionListener mListener;


    public static ExibirPerfilFragment newInstance(String param1, String param2) {
        ExibirPerfilFragment fragment = new ExibirPerfilFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context=container.getContext();
        View view= inflater.inflate(R.layout.fragment_exibir_perfil, null);

        foto_perfil=(ImageView) view.findViewById(R.id.foto_perfil);
        nome_perfil=(TextView) view.findViewById(R.id.nome_perfil);
        email_perfil=(TextView) view.findViewById(R.id.email_perfil);
        phone_perfil=(TextView) view.findViewById(R.id.phone_perfil);
        logradouro_perfil=(TextView) view.findViewById(R.id.logradouro_perfil);
        numero_perfil=(TextView) view.findViewById(R.id.numero_perfil);
        bairro_perfil=(TextView) view.findViewById(R.id.bairro_perfil);
        cidade_perfil=(TextView) view.findViewById(R.id.cidade_perfil);
        estado_perfil=(TextView) view.findViewById(R.id.estado_perfil);
        pais_perfil=(TextView) view.findViewById(R.id.pais_perfil);

        usuarioBo=new UsuarioBo(context);
        usuario=usuarioBo.get(null,null);

        if(usuario.getFoto()!=null){
           // Picasso.with(context).load(usuario.getFoto()).into(foto_perfil);
        }
        if(usuario.getNome()!= null){
            nome_perfil.setText(usuario.getNome());
        }
        if (usuario.getEmail()!=null){
            email_perfil.setText(usuario.getEmail());
        }
        if (usuario.getDdd()!= null && usuario.getTelefone()!=null){
            phone_perfil.setText("("+usuario.getDdd()+") "+usuario.getTelefone());
        }
        if (usuario.getLogradouro()!=null){
            email_perfil.setText(usuario.getLogradouro());
        }
        if (usuario.getNumero()!=null){
            numero_perfil.setText(usuario.getNumero());
        }
        if (usuario.getBairro()!=null){
            bairro_perfil.setText(usuario.getBairro());
        }
        if (usuario.getCidade()!=null){
            cidade_perfil.setText(usuario.getCidade());
        }
        if (usuario.getEstado()!=null){
            estado_perfil.setText(usuario.getEstado());
        }

        if (usuario.getPais()!=null){
            pais_perfil.setText(usuario.getPais());
        }

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
