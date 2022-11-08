package com.assertsecurity.venariwatcher.httpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.assertsecurity.venariwatcher.utils.DateTimeUtils;
import com.assertsecurity.venariwatcher.utils.PayloadMapper;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * @Classname HttpServer
 * @Description HTTPServer supply .class file which execute command by Runtime.getRuntime.exec()
 * @Author welkin
 */
public class HttpServer implements Runnable{
    private Server _server;
    private String _httpAddr;
    private int _httpPort;

    public HttpServer(String httpAddr, int port) {
        _server = new Server(port);
        _httpAddr = httpAddr;
        _httpPort = port;
    }

    @Override
    public void run() {
        ServletHandler handler = new ServletHandler();
        _server.setHandler(handler);

        handler.addServletWithMapping(VerifyPayloadServlet.class, "/verify/*");
        handler.addServletWithMapping(FixPayloadServlet.class, "/fix/*");
        handler.addServletWithMapping(BatchVerifyPayloadServlet.class, "/batchverify");
        handler.addServletWithMapping(SetPayloadServlet.class, "/set/*");
        try {
            System.out.println(DateTimeUtils.getDateTimeString() + " [HTTP Server]>> Listening on " + _httpAddr + ":" + _httpPort);
            _server.start();
            _server.join();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static class BatchVerifyPayloadServlet extends HttpServlet {
        public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            boolean found = false;
            try 
            {
                String line;
                while ((line = reader.readLine()) != null) 
                {
                    UUID id = UUID.fromString(line);
                    if (PayloadMapper.Has(id))
                    {
                        sb.append(id.toString() + "\n");
                        found = true;
                    }
                }
            } 
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
                found = false;
            }
            finally 
            {
                reader.close();
            }
            if (found)
            {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("text/plain"); 
                PrintWriter out = response.getWriter(); 
                out.println(sb.toString()); 
            }
            else
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain"); 
                PrintWriter out = response.getWriter(); 
                out.println("Not Found"); 
            }
        }
    }

    public static class FixPayloadServlet extends HttpServlet {
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

            String filename = request.getRequestURI().substring(1);
            boolean found = false;
            try 
            {
                filename = filename.replace("fix/", "");
                UUID id = UUID.fromString(filename);
                if (PayloadMapper.Remove(id))
                {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("text/plain"); 
                    PrintWriter out = response.getWriter(); 
                    out.println(String.format("Fixed %s",id)); 
                    found = true;
                }
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
            if (!found)
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain"); 
                PrintWriter out = response.getWriter(); 
                out.println("Not Found"); 
            }
        }
    }

    public static class SetPayloadServlet extends HttpServlet {
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

            String filename = request.getRequestURI().substring(1);
            boolean found = false;
            try 
            {
                filename = filename.replace("set/", "");
                UUID id = UUID.fromString(filename);
                PayloadMapper.Set(id);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("text/plain"); 
                PrintWriter out = response.getWriter(); 
                out.println(String.format("Set %s",id)); 
                found = true;
        }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
            if (!found)
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain"); 
                PrintWriter out = response.getWriter(); 
                out.println("Not Set"); 
            }
        }
    }

    public static class VerifyPayloadServlet extends HttpServlet {
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
            String filename = request.getRequestURI().substring(1);
            boolean found = false;
            try 
            {
                filename = filename.replace("verify/", "");
                UUID id = UUID.fromString(filename);
                if (PayloadMapper.Has(id))
                {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("text/plain"); 
                    PrintWriter out = response.getWriter(); 
                    out.println(String.format("Found %s",id)); 
                    found = true;
                }
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
            if (!found)
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType("text/plain"); 
                PrintWriter out = response.getWriter(); 
                out.println("Not Found"); 
            }
        }
    }

}
