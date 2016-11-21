package br.com.mobi.redemicro.dao;

import android.content.Context;

import br.com.mobi.redemicro.bean.Noticia;
import br.com.mobi.redemicro.bo.NoticiaBo;
import mobi.stos.podataka_lib.repository.AbstractRepository;

/**
 * Created by Allesson on 18/11/2016.
 */

public class NoticiaDao extends AbstractRepository<Noticia> {
    public NoticiaDao (Context context){
        super(context,Noticia.class);
    }
}
