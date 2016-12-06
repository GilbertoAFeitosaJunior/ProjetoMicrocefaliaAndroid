package br.com.mobi.redemicro.bo;

import android.content.Context;

import br.com.mobi.redemicro.bean.Pergunta;
import br.com.mobi.redemicro.dao.PergutaDao;
import mobi.stos.podataka_lib.interfaces.IOperations;
import mobi.stos.podataka_lib.service.AbstractService;

/**
 * Created by Gilberto on 05/12/2016.
 */

public class PerguntaBo extends AbstractService<Pergunta> {
    private PergutaDao dao;

    public PerguntaBo(Context context){
        this.dao=new PergutaDao(context);
    }
    @Override
    protected IOperations<Pergunta> getDao(){
        return dao;
    }
}
