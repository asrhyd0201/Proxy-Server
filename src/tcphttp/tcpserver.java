package tcphttp;
import java.net.*;
import java.io.*;
import java.util.*;
class multiserver
{
      private static int num_of_reqs=0;
      private static String CRLF = "\r\n";
      private static Map cache,Searchcache,Insertcache;
      public multiserver()
      {
          try
          {
             cache = new HashMap();
             Searchcache=new HashMap();
             Insertcache=new HashMap();
          }
          catch (Exception e)
          {
                 System.err.println("Could not create hash map ");
                 System.exit(-1);
          }
      }
      public  void handlerequest(Socket client,multiserver m)throws Exception
      {
         Socket server = null;
         HttpParsing request = null;
         HttpResponse response = null;
         System.out.println("\n1 ");
         request = readRequest(client);
         if(request == null)
         {
             print();
             return;
         }
         System.out.println("\n2 ");
         server = sendRequestToServer(request);
         try
         {
             System.out.println("\n3 ");
             response = getResponseFromServer(server);
             System.out.println("Response: " + response.toString() + "\n");
             System.out.println("\n4 ");
             if(response.status == 200)
                            sendResponseToClient(response, client);
             else if(response.status == 302)
             {
                            sendToClient("Moved Permenanetly",client);
                            return;
             }
             else if(response.status == 400)
             {
                            sendToClient("Bad Request",client);
                            return;
             }
             else if(response.status == 301)
             {
                            sendToClient("Moved permenanetly",client);
                            return;
             }
             else if(response.status==404)
             {
                sendToClient("Bad Request",client);
                            return;
             }
             else if(response.status==504)
             {
                 sendToClient("Connection Timed out",client);
                            return;
             }
             System.out.println("\n5 ");
            
             addRequestResponseToCache(request,response);
             print();
             num_of_reqs++;
             server.close();
         }
         catch (IOException e)
         {
            return;
         }
     }
     private static void print()
     {
         long avginsert=0,avgsearch=0,num_i=0,num_s=0;
         int flag=0;
         try
         {
            System.out.println("\nInsertion Times of the URL'S");
            Iterator iter1 = Insertcache.entrySet().iterator();
            while(iter1.hasNext())
            {
                Map.Entry mEntry = (Map.Entry)iter1.next();
                HttpParsing req = (HttpParsing)mEntry.getKey();
                inserttime ins = (inserttime)mEntry.getValue();
                System.out.print(req.getURL()+"   ");
                System.out.println(ins.gettime());
                avginsert=avginsert+ins.gettime();
                num_i=num_i+1;
            }
            System.out.println("AVERAGE INSERT TIMES"+"  "+(float)avginsert/num_i);
            System.out.println("\nSearching Times of the URL'S");
            Iterator iter2 = Searchcache.entrySet().iterator();
            while(iter2.hasNext())
            {
                Map.Entry mEntry1 = (Map.Entry)iter2.next();
                HttpParsing req1 = (HttpParsing)mEntry1.getKey();
                searchtime st = (searchtime)mEntry1.getValue();
                System.out.print(req1.getURL()+"   ");
                System.out.println(st.gettime());
                avgsearch=avgsearch+st.gettime();
                num_s=num_s+1;
                flag=1;
            }
            if(flag==1)
                System.out.println("AVERAGE SEARCH TIMES"+"  "+(float)avgsearch/num_s);
        }
         catch(Exception e)
         {
              System.out.println(e);
         }
    }

    private static  HttpParsing readRequest(Socket client)
    {
        try
        {
            BufferedReader inFromClient =new BufferedReader(new InputStreamReader (client.getInputStream()));
            System.out.println("Reading request...");
            String data="",temp;
            temp=inFromClient.readLine();
            while(temp.length()!=0)
            {
                  data=data+temp+"\r\n";
                  temp=inFromClient.readLine();
            }
            data=data+"\r\n";
            System.out.println("data="+data);
            HttpParsing request = new HttpParsing(data);
            System.out.println("Got request.\n");
            HttpResponse cache_response = null;
            if((cache_response = getCachedResponse(request)) != null)
            {

                       System.out.println("Request is in cache.");
                       System.out.println("Sending Conditional Get request...");
                       System.out.println("Cond. GET REQUEST - \n");
                       Httpconparser r = new Httpconparser(data,cache_response.getdate());
                       Socket server = sendRequestToServercon(r);
                       HttpResponse con_get_response = getResponseFromServer(server);
                       System.out.println("Done sending Conditional Get request.\n");
                       System.out.println("Cond. GET RESPONSE - \n" +
                       con_get_response.toString() + "\n");
                       if(con_get_response.status == 304)
                       {
                           System.out.println("Server has no updated version.");
                           System.out.println("Using cached version as response.");
                           sendResponseToClient(cache_response,client);
                           addRequestResponseToCachem(request, cache_response);
                           server.close();
                           return null;
                       }
                       else if(con_get_response.status == 200)
                       {
                          System.out.println("Server has an updated version.");
                          System.out.println("Using updated version.");
                          sendResponseToClient(con_get_response, client);
                          System.out.println("Updating cache with server version.");
                          addRequestResponseToCachem(request, con_get_response);
                          server.close();
                          return null;
                      }
                      else if(con_get_response.status == 302)
                      {
                            sendToClient("Moved Permenanetly",client);
                            server.close();
                            return null;
                      }
                      else if(con_get_response.status == 400)
                      {
                            sendToClient("Bad Request",client);
                            server.close();
                            return null;
                      }

                      else if(con_get_response.status == 301)
                      {
                            sendToClient("Bad Request",client);
                            server.close();
                            return null;
                      }
                      else if(con_get_response.status == 407)
                      {
                            sendToClient("Proxy Authentication is Required",client);
                            server.close();
                            return null;
                      }
                      return null;
            }
            return request;
        }
        catch (IOException e)
        {
            System.out.println("Error reading request from client: " + e);
            return null;
        }
    }

    private static Socket sendRequestToServer(HttpParsing request)
    {
        try {
            /** Open socket and write request to socket */
            System.out.println("Sending request to server...");
            Socket server=null;
            if(request.gethost().contains("iiit"))
              server =  new Socket(request.gethost(),80);
            else
              server =  new Socket("hostelproxy.iiit.ac.in",8080);
            System.out.println(request);
            DataOutputStream toServer =
                       new DataOutputStream(server.getOutputStream());
            toServer.writeBytes(request.toString());
            System.out.println("Finished sending request to server.\n");
            return server;
           }
           catch (UnknownHostException e)
           {
              System.out.println("Unknown host: " + request.gethost());
              System.out.println(e);
              return null;
           }
           catch (Exception e)
           {
               System.out.println("Error writing request to server: " + e);
               return null;
           }
    }
     private static Socket sendRequestToServercon(Httpconparser request)
     {
        try {
            /** Open socket and write request to socket */
            System.out.println("Sending request to server...");
            Socket server=null;
            if(request.gethost().contains("iiit"))
              server =  new Socket(request.gethost(),80);
            else
              server =  new Socket("hostelproxy.iiit.ac.in",8080);
            System.out.println(request);
            DataOutputStream toServer =
                       new DataOutputStream(server.getOutputStream());
            toServer.writeBytes(request.toString());
            System.out.println("Finished sending request to server.\n");
            return server;
           }
           catch (UnknownHostException e)
           {
              System.out.println("Unknown host: " + request.gethost());
              System.out.println(e);
              return null;
           }
           catch (Exception e)
           {
               System.out.println("Error writing request to server: " + e);
               return null;
           }
    }
    /** Read response and forward it to client */
    private static HttpResponse getResponseFromServer(Socket server){
        try {
            DataInputStream fromServer =
                    new DataInputStream(server.getInputStream());
            HttpResponse response = new HttpResponse(fromServer);
            return response;
        }
        catch (IOException e)
        {
            System.out.println("Error writing response to client: " + e);
            return null;
        }
    }
    private static  void sendResponseToClient(HttpResponse response, Socket client)
    {
        try
        {
            String send_data,temp;
            System.out.println("Sending Response to Client");
            send_data = response.toString();
            PrintWriter outToClient =new PrintWriter(client.getOutputStream(),true);
            outToClient.println(send_data);
            System.out.println("length"+send_data.length());
            System.out.println("Writing Headers to client...");
            System.out.println("Writing Body to client...");
            int i,j=0;
            for(i=0;j+i<response.getbody().length();i+=31)
            {
                temp=response.getbody().substring(j,j+i);
                j=i;
                outToClient.println(temp);
                System.out.println(temp);
                
            };
        }
        catch (IOException e)
        {
            System.out.println("Error writing response to client: " + e);
        }
    }
    private static  void sendToClient(String str, Socket client)
    {
        try
        {
            System.out.println("Sending Response to Client");
            PrintWriter outToClient =new PrintWriter(client.getOutputStream(),true);
            outToClient.println(str);
            System.out.println("Writing Headers to client...");
            System.out.println("Writing Body to client...");
            System.out.println("Done sending Response to Client");
        }
        catch (IOException e)
        {
            System.out.println("Error writing response to client: " + e);
        }
    }
    private static void addRequestResponseToCache(HttpParsing request, HttpResponse response)
    {
        Date date=new Date();
        long insb,insa,msec;
        
        insb=date.getTime();
        if(num_of_reqs<100)/*Total Number of requests and corresponding responses that can be put in cache*/
        {
            //System.out.println("num_of_reqs"+num_of_reqs);
            cache.put(request, response);
            System.out.println("Added request and response to cache.");
            insa=date.getTime();
            msec=insb-insa;
            inserttime ins=new inserttime(msec);
            Insertcache.put(request,ins);
        }
        else
        {
            int flag=0;
            HttpResponse res = null;
            Iterator iter1 = cache.entrySet().iterator();
            while(iter1.hasNext())
            {
                Map.Entry mEntry = (Map.Entry)iter1.next();
                HttpParsing req = (HttpParsing)mEntry.getKey();
                res = (HttpResponse)mEntry.getValue();
                if(res.getcounter()==1)
                {
                   cache.remove(req);
                   flag=1;
                   System.out.println("Requests with least number of visits is removed from cache");
                }
            }
            if(flag==1)
            {
                cache.remove(request);
                System.out.println("Requests with least number of visits is not present in cache and current request is not inserted");
            }
        }
    }
   private static void addRequestResponseToCachem(HttpParsing request, HttpResponse response)
   {
         Iterator iter2 = cache.entrySet().iterator();
         while(iter2.hasNext())
         {
                Map.Entry mEntry = (Map.Entry)iter2.next();
                HttpParsing r1 = (HttpParsing)mEntry.getKey();
                //System.out.println("r1"+r1.gethost()+":"+"req"+request.gethost());
                if(r1.gethost().compareTo(request.gethost())==0)
                {
                     HttpResponse resu = (HttpResponse)cache.get(r1);
                     resu.increment();
                     addRequestResponseToCache(r1, resu);
                     break;
                }
         }
         Iterator iter3 = cache.entrySet().iterator();
         while(iter3.hasNext())
         {
                Map.Entry mEntry = (Map.Entry)iter3.next();
                HttpParsing requ = (HttpParsing)mEntry.getKey();
                HttpResponse res = (HttpResponse)mEntry.getValue();
                System.out.print("Host:"+requ.gethost()+" ");
                System.out.print("No of Visits:"+res.getcounter()+"  ");
                System.out.println("HIT RATIO:"+(float)res.getcounter()/(res.getcounter()+1));
        }
   }
   private static HttpResponse getCachedResponse(HttpParsing request)
   {
        try
        {
            long searchb,searcha,msec;
            int flag=0;
            Date date=new Date();
            
            System.out.println("Checking if request is in memory cache...");
            HttpResponse response = null;
            Iterator iter = cache.entrySet().iterator();
            System.out.println("Requested Host: "+request.gethost());
            searchb=date.getTime();
            while(iter.hasNext())
            {
                Map.Entry mEntry = (Map.Entry)iter.next();
                HttpParsing r = (HttpParsing)mEntry.getKey();
                if(request.gethost().equals(r.gethost()))
                {
                     if(request.getURL().equals(r.getURL()))
                     {
                          searcha=date.getTime();
                          msec=searchb-searcha;
                          searchtime ins=new searchtime(msec);
                          Searchcache.put(request, ins);

                          System.out.println("Requested URL: "+request.getURL());
                          System.out.println("IN CACHE!!!!! ");
                          response = (HttpResponse)mEntry.getValue();
                          System.out.println("Finished checking request.\n");
                          return response;
                     }
                }
            }
            
            System.out.println("URL not in Cache.\n");
            System.out.println("Finished checking request.\n");
            return response;
        }
        catch (Exception e)
        {
            System.out.println("Error reading request from client: " + e);
            return null;
        }
    }
    
}
class server extends Thread
{
        Socket socket;
        multiserver mserver;
        public  server(multiserver m,Socket s)
        {
                mserver=m;
                socket=s;
        }
        public void run()
        {
            try
            {
                  synchronized(mserver)
                  {
                     mserver.handlerequest(socket,mserver);
                  }
            }
            catch(Exception e)
            {
                System.out.println("Exception Caught in server"+e);
            }
        }
}
class tcpserver
{
       public static void main(String args[])
       {
            try
            {
                 multiserver mserver=new multiserver();
                 ServerSocket Server = new ServerSocket (1000,6);
                 while (true)
                 {
                     //client = Server.accept();
                     //tcp.handlerequest(client);
                       new server(mserver,Server.accept()).start();
                 }
            }
            catch (Exception e)
            {
                 System.err.println("Could not listen on port: ");
                 System.exit(-1);
            }
        }
}