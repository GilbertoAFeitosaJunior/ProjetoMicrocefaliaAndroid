package br.com.mobi.redemicro.bo;

import android.content.Context;

import br.com.mobi.redemicro.bean.Noticia;
import br.com.mobi.redemicro.dao.NoticiaDao;
import mobi.stos.podataka_lib.interfaces.IOperations;
import mobi.stos.podataka_lib.service.AbstractService;

/**
 * Created by Allesson on 18/11/2016.
 */

public class NoticiaBo extends AbstractService<Noticia>{
    private NoticiaDao dao;

    public NoticiaBo(Context context){
        this.dao=new NoticiaDao(context);
    }
    @Override
    protected IOperations <Noticia> getDao(){
        return dao;
    }
}
