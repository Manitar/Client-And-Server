#include <stdlib.h>
#include <connectionHandler.h>
#include <thread>
/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
void readFromServer(ConnectionHandler &connectionHandler){
    while(1){
        std::string answer;
        if (!connectionHandler.getLine(answer)) {
            //std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        connectionHandler.keepGoing = 1;
        if(connectionHandler.shouldTerminate())
            break;
    }
}



void readFromKeyBoard(ConnectionHandler &connectionHandler) {
    while (1) {
        if (connectionHandler.shouldTerminate())
            break;
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        if (!connectionHandler.sendLine(line)) {
            //std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        //std::cout << "Sent " << len + 1 << " bytes to server" << std::endl;
        connectionHandler.keepGoing = -1;

        while (connectionHandler.keepGoing == (-1)) {}
    }
}

int main (int argc, char *argv[]) {
    if (argc < 3) {
        //std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }

    std::string host = argv[1];
    short port = atoi(argv[2]);
    
    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        //std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    std::thread keyboard(readFromKeyBoard , std::ref (connectionHandler));
    std::thread server(readFromServer , std::ref(connectionHandler));
    server.join();
    keyboard.join();

    //std::cout<<"finished"<<std::endl;
    return 0;
}
