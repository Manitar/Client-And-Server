#include <connectionHandler.h>

using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

ConnectionHandler::ConnectionHandler(string host, short port): host_(host), port_(port), io_service_(), socket_(io_service_), opEnc(), terminate(false){}

ConnectionHandler::~ConnectionHandler() {
    close();
}

bool ConnectionHandler::connect() {
    std::cout << "Starting connect to "
              << host_ << ":" << port_ << std::endl;
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

//part to SEND

void ConnectionHandler::addString(std::string s, bool isString){
    for(int i=0; i<s.length(); i++){
        messageEnc.push_back(s.c_str()[i]);
    }
    if(isString)
        messageEnc.push_back('\0');
}

std::string ConnectionHandler::combineVectors(){
    std::string ans="";
    ans+=opEnc[0];
    ans+=opEnc[1];
    for(int i=0; i<messageEnc.size(); i++){
        ans+=messageEnc.at(i);
    }
    return ans;
}
std::string ConnectionHandler::mString(){
    std::string ans="";
    for(int i=0; i<messageEnc.size(); i++){
        ans+=messageEnc.at(i);
    }
    return ans;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {

            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    messageEnc.clear();
    return true;
}

//simply gets the line and tells us (in return) the op code that we have
short ConnectionHandler::getCode(std::string& line){
    std::string chose [11]= {"ADMINREG", "STUDENTREG","LOGIN","LOGOUT","COURSEREG","KDAMCHECK", "COURSESTAT", "STUDENTSTAT", "ISREGISTERED","UNREGISTER","MYCOURSES"};
    int space = line.find(' ');
    std::string opS =line;

    if(space>0)
        opS=line.substr(0,space);

    short code = 0;
    for (int i = 0; i < 11; i++) {
        if (opS==  chose[i])
            code = short(i + 1);
    }
    if(code == 10)
        code =14;
    return code;
}

bool ConnectionHandler::sendLine(std::string& line) {
//notice that we want to separate op & num from strings messages
    short code = getCode(line);
    std::string rest ="";
    shortToBytesB(code);
    if(code!=4 && code != 11)
        rest = line.substr(line.find(' ')+1);

    sendOneString(rest,code);

    //so far we added to messageENC all that needed.
    //we also added opENC properly
    //all i have to do now is to send the combind sequence to the server!

    //std::string toSend= combineVectors();
    //std::cout<<"toSend size = "<<toSend.length()<<"content : "<<toSend<<std::endl;
    //return sendFrameAscii(toSend, '\n');
    return sendFrameAscii(mString(), '\n');

}
void ConnectionHandler::sendOneString(std::string rest, short code){
    switch(code){
        case 1:
        case 2:
        case 3:
            //std::cout<<"1 | 2 | 3 : we have two string to encode!"<<std::endl;
            addString(rest.substr(0, rest.find(' ')), true);
            addString(rest.substr( rest.find(' ')+1), true);
            break;
        case 4:
            break;
        case 5:
        case 6:
        case 7:
        case 9:
        case 14:
            addString(rest, false);
            break;
        case 8:
            addString(rest, true);
            break;
        case 11:
            break;
        default:
            break;
    }

}

bool ConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {

    if(sendBytes(opEnc,2)) {
        if (sendBytes(frame.c_str(), frame.length()))
            return sendBytes(&delimiter, 1);
    }
    return false;

}

void ConnectionHandler::shortToBytesB(short num){
    opEnc[0] = ((num >> 8) & 0xFF);
    opEnc[1] = (num & 0xFF);
}



//read from server part:

bool ConnectionHandler::getLine(std::string& line) {
    if(! getFrameAscii(line, '\n'))
        return false;
    //here i want to set a print function
    //line - holds all the strings and nums as is
    //op_or holds the acd/err+num
    return printMessage(line);
    //return true;
}

bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    char ch;
    // Stop when we encounter the null character.
    // Notice that the null character is not appended to the frame string.
    int index=0;
    try {
        do{
            if(!getBytes(&ch, 1))
            {
                return false;
            }
            if(index<4){
                op_or_DEC.push_back(ch);
                index++;
            }else
            if(ch!='\0'&&ch!='\n') {
                frame.append(1, ch);
            }
        }while (delimiter != ch);
    } catch (std::exception& e) {
        std::cerr << "recv failed2 (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
            tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::printMessage(std::string & frame){
    char d = '|';
    short _op = bytesToShortB(0);
    short _or = bytesToShortB(2);
    std::string ack = "ERROR ";
    if(_op==13){
        std::cout<<ack<<_or<<std::endl;
        return true;
    }
    else{
        ack="ACK ";
        std::cout<<ack<<_or<<std::endl;

        switch (_or){
            case 4:
                close();
                break;
            case 6:
            case 9:
            case 11:
                std::cout<<frame.substr(0,frame.find(d))<<std::endl;
                break;
            case 8:
                std::cout<<frame.substr(0,frame.find(d))<<std::endl;
                std::cout<<frame.substr(frame.find(d)+1)<<std::endl;
                break;
            case 7:
                std::string s = frame.substr(frame.find(d)+1);
                std::cout<<frame.substr(0,frame.find(d))<<std::endl;
                std::cout<<s.substr(0,s.find(d))<<std::endl;
                std::cout<<s.substr(s.find(d)+1)<<std::endl;
                break;
        }
        return true;
    }
}

short ConnectionHandler::bytesToShortB(int index)
{
    short result = (short)((op_or_DEC[index] & 0xff) << 8);
    result += (short)(op_or_DEC[index+1] & 0xff);
    if(index == 2){
        op_or_DEC.clear();
    }
    if(result == 14)
        result =10;
    return result;
}

// Close down the connection properly.
void ConnectionHandler::close() {
    try{
        terminate = true;
        socket_.close();
    } catch (...) {
        //std::cout << "closing failed: connection already closed" << std::endl;
    }
}
bool ConnectionHandler::shouldTerminate(){
    return terminate;
}
