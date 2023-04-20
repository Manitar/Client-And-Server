package bgu.spl.net.impl.BGRSServer;

public class ServerProtocol implements MessagingProtocol<String> {


    private String username = null;
    //When we login, turn username into the actual username.
    //When we logout, terminate client.
    private boolean terminate = false;
    //When we login, if it is admin, we turn admin into true.
    public ServerProtocol(){

    }
    /**
     * process the given message
     * @param msg the received message
     * @return the response to send or null if no response is expected by the client
     */
    @Override
    public String process(String msg) {

        Database database = Database.getInstance();
        int opNum = getOpCode(msg);
        //now msg holds the entire message without the op code
        if(opNum!=4 && opNum!=11)
            msg = msg.substring(msg.indexOf('|') + 1);

        //Cases 4 and 11
        switch (opNum) {
            //LOGOUT
            case 4: {
                //Logs out. If we are not logged in, send error. Else, logout and send ACK.
                // Error reasons:
                // 1) User isn't logged in
                if(username!=null){
                    if(Database.LOGOUT(username)){
                        terminate = true;
                        //ACK 4
                        return "12|4|";
                }
                 }
                //ERR 4
                return "13|4|";
            }
            //MYCOURSES
            case 11: {
                // Error reasons:
                // 1) User isn't logged in
                // 2) User is admin
                //Check admin through database
                String s = Database.MYCOURSES(username);
                if(!s.equals("error")) {
                    //ACK 11
                    return "12|11|" + s;
                }

                return "13|11|";
            }
        }

        //If the part after the op| is a number
        //Cases: 5, 6, 7, 9, 10 are of the type: op|num
        int courseNum=0;
        int s_zero = msg.indexOf('\0');
        if(s_zero<0) {
            courseNum = Integer.parseInt(msg.substring(msg.indexOf('|') + 1));
        }
        switch (opNum) {
            //COURSEREG
            case 5: {
                // Error reasons:
                // 1) No such course exists
                // 2) No seats are available
                // 3) Student does not have all kdam courses
                // 4) User is not admin
                // All gets checked in protocol
                if (username!=null && Database.COURSEREG(username, courseNum)) {
                        return "12|5|";
                }
                return "13|5|";
            }
            //KDAMCHECK (STUDENT ONLY)
            case 6: {
                // Error reasons:
                // 1) User not logged in
                // 2) Course does not exist
                // 3) User is admin
                String s = Database.KDAMCHECK(username, courseNum);
                if (!s.equals("error")) {
                    //ACK 6
                    return "12|6|" + s;
                }
                //ERR 6
                return "13|6|";
            }
            //COURSESTAT (Admin only)
            case 7: {
                // Error reasons:
                // 1) User isn't logged in
                // 2) User is not admin
                // 3) Course doesn't exist

                //Example:
                //
                //Course: (342) How To Train Your Dragon
                //
                //Seats Available: 22/25
                //
                //Students Registered: [ahufferson, hhhaddock, thevast] //if there are no students registered yet,
                //simply print []
                if(username!=null){
                    String s = Database.COURSESTAT(username, courseNum);
                    if (!s.equals("error"))
                        return "12|7|" + s;
                }
                return "13|7|";
            }
            //ISREGISTERED
            case 9: {
                // Error reasons:
                // 1) User not logged in
                // 2) User is admin
                // 3) Username doesn't exist
                if(username!=null){
                String s = Database.ISREGISTERED(username, courseNum);
                //ACK 9
                //Function can return "error", but will not because we already checked "userExists".
                if (!s.equals("error"))
                    return "12|9|" + s;
                }
                //ERR 9
                return "13|9|";
            }
            //UNREGISTER
            case 10: {
                // Error reasons:
                // 1) User not logged in
                // 2) Username isn't registered
                // 3) Student isn't registered to courseNum
                // 4) User is admin
                if (username!=null && Database.UNREGISTER(username, courseNum)) {
                    //ACK 10
                    return "12|10|";
                }
                //ERR 10
                return "13|10|";
            }
            //STUDENTSTAT
            case 8: {
                //Only for Case 8, where case 8 is OP | String \0
                //Error messages:
                // 1) User is not admin
                // 2) Student we are looking for is not registered
                if(username!=null){
                String s = Database.STUDENTSTAT(username, msg.substring(0, msg.indexOf('\0')));
                if (!s.equals("error"))
                    //ACK 8
                    return "12|8|" + s;
                }
                return "13|8|";
            }
            //ADMINREG
            case 1:{
                //Error problems:
                // 1) Admin is already registered
                String user = msg.substring(0,msg.indexOf('\0'));
                String pass = msg.substring(msg.indexOf('\0')+1,msg.lastIndexOf('\0'));
                if(username==null && Database.ADMINREG(user, pass) ){
                    return "12|1|";
                }
                return "13|1|";
            }
            //STUDENTREG
            case 2:{
                // Error problems:
                // 1) Student is already registered
                String user = msg.substring(0,msg.indexOf('\0'));
                String pass = msg.substring(msg.indexOf('\0')+1,msg.lastIndexOf('\0'));
                if(username==null && Database.STUDENTREG(user, pass)){
                    return "12|2|";
                }
                return "13|2|";
            }
            //LOGIN
            case 3: {
                // Error problems:
                // 1) User doesn't exist
                // 2) User is already logged in
                // 3) Password wrong
                String user = msg.substring(0,msg.indexOf('\0'));
                String pass = msg.substring(msg.indexOf('\0')+1,msg.lastIndexOf('\0'));
                if(username==null && Database.LOGIN(user, pass) ){
                    username = user;
                    return "12|3|";
                }
                return "13|3|";
            }
        }
        return null;
    }



    private int getOpCode(String s){
        int code = Integer.parseInt(s.substring(0,s.indexOf('|')));
        if(code == 14)
            code =10;
        return code;
    }

    @Override
    public boolean shouldTerminate() {
        return terminate;
    }
}
