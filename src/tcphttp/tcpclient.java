//TCPClient.java
package tcphttp;
import java.io.*;
import java.net.*;
class multipleclient
{
    public void clients(String url)
    {
            try
            {
                Socket socket=new Socket("localhost",1000);
		String FromServerh="",temp;
                String httpreq;
		BufferedReader inFromUser =new BufferedReader(new InputStreamReader(System.in));
		PrintWriter outToServer = new PrintWriter(socket.getOutputStream(),true);
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                HttpRequest ht=new HttpRequest(url);
                httpreq = ht.toString();
                System.out.println(httpreq);
		outToServer.println (httpreq) ;
                temp = inFromServer.readLine();
                while(temp.length()!=0)
                {
                     FromServerh=FromServerh+temp+"\n";
                     temp = inFromServer.readLine();
                }
                System.out.println(FromServerh);
                temp = inFromServer.readLine();
                while(temp!=null)//or true
                {
                     temp = inFromServer.readLine();
                     System.out.println(temp);
                }
            }
            catch(IOException e)
            {
                System.out.println("Exception caught"+e);
            }
    }
}
class client implements Runnable
{
        String url;
        multipleclient mulclient;
        public  client(multipleclient m,String data)
        {
                mulclient=m;
                url=data;
        }
        public void run()
        {
                mulclient.clients(url);   
        }
}
class tcpclient
{
	public static void main(String argv[]) 
	{
                int number,i;
                try
                {
                    multipleclient mclient=new multipleclient();
                    System.out.println("Enter the number of Http Requests:");
                    BufferedReader br =new BufferedReader(new InputStreamReader(System.in));
		    number=Integer.parseInt(br.readLine());
                    String url[]=new String[number];
                    for(i=0;i<number;i++)
                    {
                        System.out.println("Enter the Url ");
                        url[i]=br.readLine();
                    }
                    for(i=0;i<number;i++)
                    {
                        new Thread(new client(mclient,url[i])).start();
                    }
	        }
                catch(IOException e)
                {
                    System.out.println("Caught IoExcption"+e);
                }
           }
}