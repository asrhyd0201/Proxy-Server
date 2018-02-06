
package tcphttp;
import java.io.*;
public class HttpResponse
{
    final static String CRLF = "\r\n";
    final static int BUF_SIZE = 8192;
    final static int MAX_OBJECT_SIZE = 1000000;
    String version, statusLine = "",headers = "",date;
    int status,counter=1;
    long time;
    String body;
    public HttpResponse(DataInputStream fromServer)
    {
        int length = -1;
        boolean gotStatusLine = false;
        try
        {
            String line =  fromServer.readLine();
            while (line.length() != 0)
            {
                if (!gotStatusLine)
                {
                    statusLine = line;
                    String[] tmp = statusLine.split(" ");
                    status = Integer.parseInt(tmp[1]);
                    gotStatusLine = true;
                }
                else
                    headers += line + CRLF;
                if (line.startsWith("Content-Length:") || line.startsWith("Content-length:"))
                {
                    String[] tmp = line.split(" ");
                    length = Integer.parseInt(tmp[1]);
                }
                if (line.startsWith("Date:") ||line.startsWith("date:"))
                {
                    String tmp = line;
                    date=tmp.replaceAll("Date:", "");
                }
                line = fromServer.readLine();
            }
        }
        catch (IOException e)
        {
            System.out.println("Error reading headers from server: " + e);
            return;
        }
        try
        {
            int i=0;
            String tmp;
            while((tmp=fromServer.readLine())!=null)
              body=body+tmp;
        }
        catch (IOException e)
        {
            System.out.println("Error reading response body: " + e);
            return;
        }
 }
    public String toString()
    {
        String res = "";
        res = statusLine + CRLF;
        res += headers;
        res += CRLF;
        return res;
    }
    public int getstatus()
    {
        return status;
    }
    public String getdate()
    {
        return date;
    }
    public String getbody()
    {
        return body;
    }
    public void increment()
    {
        counter=counter+1;
    }
    public int getcounter()
    {
       return counter;
    }
}
