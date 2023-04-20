package bgu.spl.net.impl.BGRSServer;

public class Admin {

    private String username;
    private String password;
    private boolean login = false;

    public Admin(String _username, String _password){
        username = _username;
        password = _password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isLogin(){
        return login;
    }

    public synchronized boolean login(){
        if(login){
            return false;
        }
        login = true;
        return true;
    }

    public synchronized boolean logout(){
        if(!login){
            return false;
        }
        login = false;
        return true;
    }

}
