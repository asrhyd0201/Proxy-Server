
package tcphttp;
import java.net.*;
public class HttpRequest
{
    final static String CRLF = "\r\n";

    /** Store the request parameters */
    String firstLine = "";
    String method;
    String url;
    String version;
    String headers;

    /** Create HttpRequest by reading it from the client socket */
    public HttpRequest(String from)
    {
        firstLine = from;
        try
        {
           URL hp=new URL(firstLine);
           url=hp.getFile();
           headers=hp.getHost();
        }
        catch (MalformedURLException e)
        {
            System.out.println("Error reading request line: " + e);
        }
        method = "GET";
        version = "HTTP/1.0";
        headers="Host:"+" "+headers+" "+"80"+CRLF;
    }
    public String toString()
    {
        String req = "";
        req = method + " " + firstLine + " " + version + CRLF;
        req += headers;
        req += "Connection: close" + CRLF;
        req += CRLF;
        return req;
    }
}
