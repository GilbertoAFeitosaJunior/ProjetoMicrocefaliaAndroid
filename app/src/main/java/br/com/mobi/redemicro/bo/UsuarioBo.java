package br.com.mobi.redemicro.bo;

import android.content.Context;

import br.com.mobi.redemicro.bean.Usuario;
import br.com.mobi.redemicro.dao.UsuarioDao;
import mobi.stos.podataka_lib.interfaces.IOperations;
import mobi.stos.podataka_lib.service.AbstractService;

public class UsuarioBo extends AbstractService<Usuario> {

    private UsuarioDao dao;

    public UsuarioBo(Context context){
        this.dao = new UsuarioDao(context);
    }

    @Override
    protected IOperations<Usuario> getDao() {
        return dao;
    }
}
