
package tcphttp;
import java.util.regex.*;
public class Httpconparser
{
    final static String CRLF = "\r\n";
    String method;
    String url;
    String version;
    String headers;
    private String host;
    private int port;
    public Httpconparser(String req,String cachedate)
    {
         Pattern pat=Pattern.compile("[\n]");
         String [] splitted=pat.split(req);
         String line1,line2;
         line1=splitted[0];
         line2=splitted[1];
         splitted=line1.split(" ");
         method=splitted[0];
         url=splitted[1];
         version=splitted[2];
         splitted=line2.split(" ");
         host=splitted[1];
         port=Integer.parseInt(splitted[2].trim());
         headers="Host: "+host+" "+port+CRLF;
         headers += "If-Modified-Since: "+cachedate+CRLF;
    }
    public String getURL()
    {
        return url;
    }
    public String gethost()
    {
        return host;
    }
    public int getport()
    {
        return port;
    }
    public String toString()
    {
        String req = "";
        req = method + " " + url + " " + version + CRLF;
        req += headers;
        req += CRLF;
        return req;
    }
}


