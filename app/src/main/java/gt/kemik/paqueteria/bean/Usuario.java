package gt.kemik.paqueteria.bean;

import java.io.Serializable;

/**
 * Created by HP240 on 17/11/2016.
 */

public class Usuario implements Serializable {
    private String mUsuario;
    private String mToken;
    private int mIdUsuario;

    public Usuario() {
    }

    public Usuario(String mUsuario, String mToken, int mIdUsuario) {
        this.mUsuario = mUsuario;
        this.mToken = mToken;
        this.mIdUsuario = mIdUsuario;
    }

    public String getmUsuario() {
        return mUsuario;
    }

    public void setmUsuario(String mUsuario) {
        this.mUsuario = mUsuario;
    }

    public String getmToken() {
        return mToken;
    }

    public void setmToken(String mToken) {
        this.mToken = mToken;
    }

    public int getmIdUsuario() {
        return mIdUsuario;
    }

    public void setmIdUsuario(int mIdUsuario) {
        this.mIdUsuario = mIdUsuario;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "mUsuario='" + mUsuario + '\'' +
                ", mToken='" + mToken + '\'' +
                ", mIdUsuario=" + mIdUsuario +
                '}';
    }
}
