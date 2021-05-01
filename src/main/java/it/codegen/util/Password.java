package it.codegen.util;

import java.io.Serializable;

public class Password implements Serializable {

    private byte[] encryptedPassword;

    public Password(byte[] encryptedPassword){
        this.encryptedPassword = encryptedPassword;
    }

    public byte[] getEncryptedPassword() {
        return encryptedPassword;
    }

}
