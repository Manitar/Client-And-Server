#ifndef CONNECTION_HANDLER__
#define CONNECTION_HANDLER__
                                           
#include <string>
#include <iostream>
#include <vector>
#include <boost/asio.hpp>

using boost::asio::ip::tcp;

class ConnectionHandler {
private:
	const std::string host_;
	const short port_;
	boost::asio::io_service io_service_;   // Provides core I/O functionality
	tcp::socket socket_; 
    char opEnc [2];
    std::vector<char> messageEnc;
    std::vector<char> op_or_DEC;
    bool terminate = false;


public:
    int keepGoing = 0;
    ConnectionHandler(std::string host, short port);
    virtual ~ConnectionHandler();
 
    // Connect to the remote machine
    bool connect();
 
    // Read a fixed number of bytes from the server - blocking.
    // Returns false in case the connection is closed before bytesToRead bytes can be read.
    bool getBytes(char bytes[], unsigned int bytesToRead);
 
	// Send a fixed number of bytes from the client - blocking.
    // Returns false in case the connection is closed before all the data is sent.
    bool sendBytes(const char bytes[], int bytesToWrite);
	
    // Read an ascii line from the server
    // Returns false in case connection closed before a newline can be read.
    bool getLine(std::string& line);
	
	// Send an ascii line from the server
    // Returns false in case connection closed before all the data is sent.
    bool sendLine(std::string& line);
 
    // Get Ascii data from the server until the delimiter character
    // Returns false in case connection closed before null can be read.
    bool getFrameAscii(std::string& frame, char delimiter);
 
    // Send a message to the remote host.
    // Returns false in case connection is closed before all the data is sent.
    bool sendFrameAscii(const std::string& frame, char delimiter);
	
    // Close down the connection properly.
    void close();

    short bytesToShort(char *bytesArr, int index);

    void shortToBytes(short num, std::vector<char> &b);

    short getCode(std::string &line);

    void addString(std::string s, bool isString);

    std::string combineVectors();

    bool printMessage(std::string & frame);

    short bytesToShortB(int index);

    void shortToBytesB(short num);

    void sendOneString(std::string rest, short code);

    bool shouldTerminate();

    std::string mString();

    bool sendFrameAsciiB(char **bytes, char delimiter);

    short bytesToShortBB();
}; //class ConnectionHandler
 
#endif