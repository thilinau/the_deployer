package it.codegen.util;

import java.io.Serializable;

public class CGPrivateKey implements Serializable {

    private java.security.PrivateKey privateKey;

    public CGPrivateKey(java.security.PrivateKey key){
        this.privateKey = key;
    }

    public java.security.PrivateKey getPrivateKey() {
        return privateKey;
    }
}
