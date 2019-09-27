package com.application.entities;

public class Passenger {

    private String id;
    private String nome;
    private String identidade;
    private String tipoIdentidade;
    private String dataNascimento;
    private String telefone;
    private String endereco;

    public Passenger(String nome, String identidade, String tipoIdentidade, String dataNascimento, String telefone) {
        this.nome = nome;
        this.identidade = identidade;
        this.tipoIdentidade = tipoIdentidade;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
    }


    public Passenger(String nome, String identidade, String tipoIdentidade, String dataNascimento, String telefone, String endereco) {
        this.nome = nome;
        this.identidade = identidade;
        this.tipoIdentidade = tipoIdentidade;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.endereco = endereco;
    }

    public Passenger() {
        //Must have a public no-argument constructor
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getIdentidade() {
        return identidade;
    }

    public String getTipoIdentidade() {
        return tipoIdentidade;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setId(String id) {
        this.id = id;
    }
}
