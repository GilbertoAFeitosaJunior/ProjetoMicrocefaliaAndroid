package br.com.mobi.redemicro.bean;

import java.io.Serializable;
import java.util.Date;

import mobi.stos.podataka_lib.annotations.Column;
import mobi.stos.podataka_lib.annotations.Entity;
import mobi.stos.podataka_lib.annotations.PrimaryKey;

/**
 * Created by Gilberto on 05/12/2016.
 */
@Entity
public class Pergunta implements Serializable {

    @PrimaryKey (autoIncrement = false)
    private int idTopico;
    @Column
    private String titulo;
    @Column
    private String mensagem;
    @Column
    private Date dataAbertura;
    @Column
    private String nomeUsuario;
    @Column
    private String fotoUsuario;
    @Column
    private String nomeModerador;
    @Column
    private String especialidade;
    @Column
    private Date dataFechamento;
    @Column
    private boolean ativo;

    public int getIdTopico() {
        return idTopico;
    }

    public void setIdTopico(int idTopico) {
        this.idTopico = idTopico;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Date getDataAbertura() {
        return dataAbertura;
    }

    public void setDataAbertura(Date dataAbertura) {
        this.dataAbertura = dataAbertura;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getFotoUsuario() {
        return fotoUsuario;
    }

    public void setFotoUsuario(String fotoUsuario) {
        this.fotoUsuario = fotoUsuario;
    }

    public String getNomeModerador() {
        return nomeModerador;
    }

    public void setNomeModerador(String nomeModerador) {
        this.nomeModerador = nomeModerador;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public Date getDataFechamento() {
        return dataFechamento;
    }

    public void setDataFechamento(Date dataFechamento) {
        this.dataFechamento = dataFechamento;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
